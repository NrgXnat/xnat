#!/bin/bash
#
# deploy-update.sh — NON-DESTRUCTIVE on-box install. Runs ON the target box (dave-alldev, dave-xl, …).
#
# Swaps the WAR + plugins from the staging area (~david, populated by the local
# docker/deploy.sh) and restarts XNAT. Unlike deploy.sh / deploy-fresh.sh it does
# NOT drop the database and does NOT wipe /opt/data (archive, prearchive, cache,
# build) — the current working state (DB + data) is preserved.
#
# It replaces application CODE only (WAR + plugin jars). To revert the code, push
# the previous artifacts and re-run. To protect DB/data, snapshot before running
# (pg_dump xnat + EBS snapshot).
#
# Usage (on the target box, as david):  ./deploy-update.sh
#
set -euo pipefail

STAGE_WAR=$(ls /home/david/*.war 2>/dev/null | head -1 || true)
STAGE_PLUGINS=/home/david/plugins
PLUGIN_COUNT=$(ls "$STAGE_PLUGINS"/*.jar 2>/dev/null | wc -l | tr -d ' ')

WEBAPPS=/home/xnat/tomcat10/webapps
LIVE_PLUGINS=/home/xnat/plugins
MIN_PLUGINS=20   # required-22 set (20 required + dicom-query-retrieve + xsync) minus a small margin

# --- Safety guards: never wipe a live dir from an empty/short staging area ---
if [ -z "$STAGE_WAR" ]; then
    echo "ERROR: no WAR found in /home/david — did the local deploy.sh run? Aborting." >&2
    exit 1
fi
if [ "$PLUGIN_COUNT" -lt "$MIN_PLUGINS" ]; then
    echo "ERROR: only $PLUGIN_COUNT plugin jars staged in $STAGE_PLUGINS (expected >= $MIN_PLUGINS)." >&2
    echo "       Refusing to wipe $LIVE_PLUGINS from an incomplete staging area. Aborting." >&2
    exit 1
fi

echo "=========================================================================="
echo " NON-DESTRUCTIVE UPDATE (DB + /opt/data are preserved)"
echo "   WAR:            $STAGE_WAR"
echo "   plugins staged: $PLUGIN_COUNT jars"
echo "   -> $WEBAPPS/ROOT.war and $LIVE_PLUGINS/ will be replaced"
echo "   -> database and /opt/data are LEFT UNTOUCHED"
echo "=========================================================================="
read -r -p "Proceed? [y/N] " ok
[ "$ok" = "y" ] || [ "$ok" = "Y" ] || { echo "aborted."; exit 0; }

sudo sh -c "
    set -e
    echo '== stop tomcat =='
    systemctl stop tomcat

    echo '== replace webapp (code only — /opt/data untouched) =='
    rm -rf '$WEBAPPS/ROOT' '$WEBAPPS/ROOT.war'
    cp '$STAGE_WAR' '$WEBAPPS/ROOT.war'
    chmod a+r '$WEBAPPS/ROOT.war'

    echo '== replace plugins =='
    rm -rf $LIVE_PLUGINS/*
    cp $STAGE_PLUGINS/* $LIVE_PLUGINS/
    chown xnat:xnat $LIVE_PLUGINS/*
    chmod a+r $LIVE_PLUGINS/*

    echo '== rotate logs (safe; not data) =='
    rm -f /home/xnat/tomcat10/logs/* /home/xnat/logs/* 2>/dev/null || true

    echo '== start tomcat =='
    systemctl start tomcat
"

echo "Update complete. Tailing catalina.out (Ctrl-C to stop)…"
sudo tail -f /home/xnat/tomcat10/logs/catalina.out
