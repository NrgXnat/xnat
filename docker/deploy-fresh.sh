#!/bin/bash
#
# deploy-fresh.sh — FRESH (DESTRUCTIVE) redeploy. Runs ON the target box (dave-alldev, dave-xl, …);
# the banner prints the actual hostname so you can see which box you are about to wipe.
#
# Clean-slate deploy: WIPES the database AND all data (/opt/data archive,
# prearchive, cache, build), then reinstalls the WAR + plugins from staging
# (~david, populated by the local docker/deploy.sh) and restarts XNAT.
#
# Hardened vs the original:
#   * fail-fast (set -euo pipefail) — never continues past an error
#   * preflight validation — refuses to wipe if staging (WAR/plugins) is missing
#     or short, so it can't leave the box with nothing installed
#   * typed confirmation — you must type FRESH (it drops the DB and all data)
#   * automatic pre-wipe backup of the LIVE PLUGINS ONLY (tar) to /home/david/backups.
#     The DB is NOT backed up (it's dropped/recreated) — snapshot it separately
#     (pg_dump / EBS) beforehand if you need a recovery point.
#   * content-wipes via `find -delete` — preserves the data dirs' ownership/perms
#     (recreating them as root would break XNAT's writes)
#
# For a NON-destructive, code-only update (keeps DB + data) use deploy-update.sh.
#
set -euo pipefail

STAGE_WAR=$(ls /home/david/*.war 2>/dev/null | head -1 || true)
STAGE_PLUGINS=/home/david/plugins
PLUGIN_COUNT=$(ls "$STAGE_PLUGINS"/*.jar 2>/dev/null | wc -l | tr -d ' ')

WEBAPPS=/home/xnat/tomcat10/webapps
LIVE_PLUGINS=/home/xnat/plugins
# NOTE: /opt/data/pipeline is deliberately NOT in this list. It is a separate ext4 mount holding
# the pipeline ENGINE INSTALL (~62MB: catalog, bin, lib, ant-tools, nrg-tools, pipeline.config),
# not run-generated data. `find -mindepth 1 -delete` would wipe its contents, and nothing in this
# script — or in XNAT's startup — puts them back, so pipeline tests that reference e.g.
# /opt/data/pipeline/catalog/validation_tools/Validate.xml would fail on every fresh deploy until
# the tree was restored by hand. It was briefly added on 2026-08-18 and backed out on 2026-08-19.
DATA_DIRS="/opt/data/archive /opt/data/prearchive /opt/data/cache /opt/data/build"
BACKUP_DIR=/home/david/backups
MIN_PLUGINS=20   # required-22 set (20 required + dicom-query-retrieve + xsync) minus a small margin
DBNAME=xnat
DBUSER=xnat

# --- Preflight: validate BEFORE any destruction ---
[ -n "$STAGE_WAR" ] || { echo "ERROR: no WAR in /home/david — run the local deploy.sh first. Aborting." >&2; exit 1; }
if [ "$PLUGIN_COUNT" -lt "$MIN_PLUGINS" ]; then
    echo "ERROR: only $PLUGIN_COUNT plugin jars in $STAGE_PLUGINS (expected >= $MIN_PLUGINS)." >&2
    echo "       Refusing to wipe and reinstall from an incomplete staging area. Aborting." >&2
    exit 1
fi
for t in systemctl dropdb createdb tar gzip find; do
    command -v "$t" >/dev/null 2>&1 || { echo "ERROR: required tool '$t' not found. Aborting." >&2; exit 1; }
done

STAMP=$(date +%Y%m%d-%H%M%S)

echo "=========================================================================="
echo " FRESH (DESTRUCTIVE) DEPLOY — $(hostname -s)  [$(hostname -I 2>/dev/null | awk '{print $1}')]"
echo "   WILL DROP the '$DBNAME' database"
echo "   WILL WIPE $DATA_DIRS"
echo "   WILL REPLACE webapp + plugins from staging:"
echo "       WAR:     $STAGE_WAR"
echo "       plugins: $PLUGIN_COUNT jars"
echo "   Pre-wipe backup (live plugins only; DB is NOT backed up) -> $BACKUP_DIR"
echo "=========================================================================="
read -r -p "Type FRESH to proceed (anything else aborts): " ok
[ "$ok" = "FRESH" ] || { echo "aborted."; exit 0; }

sudo sh -c "
    set -e

    echo '== pre-wipe backup (live plugins only; DB is NOT backed up) =='
    mkdir -p '$BACKUP_DIR'
    if [ -d '$LIVE_PLUGINS' ]; then
        tar czf '$BACKUP_DIR/plugins-fresh-$STAMP.tgz' -C '$LIVE_PLUGINS' . 2>/dev/null || true
    fi
    echo 'plugins backup written: $BACKUP_DIR/plugins-fresh-$STAMP.tgz'

    echo '== stop tomcat =='
    systemctl stop tomcat

    echo '== wipe webapp + logs + data (contents only; dirs/ownership preserved) =='
    find $WEBAPPS -mindepth 1 -delete
    find /home/xnat/tomcat10/logs -mindepth 1 -delete 2>/dev/null || true
    # Wiping the data dirs has to cope with BOTH box layouts, which need opposite privileges:
    #   * /opt/data on an NFS root_squash mount (dave-alldev): root is squashed to nobody and cannot
    #     delete xnat's files, so the delete must run as xnat. Test runs also leave read-only result
    #     dirs (e.g. xnat_rest_run), hence the chmod first.
    #   * /opt/data on a local filesystem (dave-xl): containers (e.g. container-service DICOM
    #     extraction) leave ROOT-owned files under /opt/data/build. xnat cannot delete those; root can.
    # Doing only one of the two aborts the deploy mid-wipe on the other box — which is exactly how this
    # script left dave-xl down on 2026-08-19. So: try as root, then as xnat, neither fatal on its own,
    # and only fail if something actually survives both passes.
    for d in $DATA_DIRS; do
        [ -d \"\$d\" ] || continue
        chmod -R u+rwX \"\$d\" 2>/dev/null || true
        find \"\$d\" -mindepth 1 -delete 2>/dev/null || true
        su xnat -s /bin/sh -c \"cd /tmp; chmod -R u+rwX '\$d' 2>/dev/null; find '\$d' -mindepth 1 -delete\" 2>/dev/null || true
        remaining=\$(find \"\$d\" -mindepth 1 2>/dev/null | wc -l)
        if [ \"\$remaining\" -gt 0 ]; then
            echo \"ERROR: \$remaining item(s) under \$d survived both the root and xnat wipe passes:\" >&2
            find \"\$d\" -mindepth 1 -printf '%u:%g %m %p\\n' 2>/dev/null | head -5 >&2
            exit 1
        fi
    done
    find /home/xnat/logs -mindepth 1 -delete 2>/dev/null || true

    echo '== recreate database =='
    # The '$DBNAME' database is owned by the postgres superuser (the '$DBUSER' role lacks
    # ownership and CREATEDB), so drop/recreate as postgres and hand ownership to '$DBUSER'
    # via -O. (dave-alldev differs from bin-tomcat10, where the '$DBUSER' role owned the DB.)
    sudo -u postgres dropdb '$DBNAME'
    sudo -u postgres createdb -O '$DBUSER' '$DBNAME'

    echo '== install WAR =='
    cp '$STAGE_WAR' '$WEBAPPS/ROOT.war'
    chmod a+r '$WEBAPPS/ROOT.war'

    echo '== install plugins =='
    find $LIVE_PLUGINS -mindepth 1 -delete
    cp $STAGE_PLUGINS/* $LIVE_PLUGINS/
    chown xnat:xnat $LIVE_PLUGINS/*
    chmod a+r $LIVE_PLUGINS/*

    echo '== start tomcat =='
    systemctl start tomcat
"

echo "Fresh deploy complete. The '$DBNAME' database was dropped/recreated and was NOT backed up —"
echo "  snapshot it beforehand (pg_dump / EBS) if you need a recovery point. Live-plugin backup: $BACKUP_DIR/plugins-fresh-$STAMP.tgz"
echo "Tailing catalina.out (Ctrl-C to stop)…"
sudo tail -f /home/xnat/tomcat10/logs/catalina.out
