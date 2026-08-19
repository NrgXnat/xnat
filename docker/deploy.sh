#!/bin/bash

# Deploy target. Override for another box, e.g.:
#   TARGET=david@dave-xl ./docker/deploy.sh
TARGET="${TARGET:-david@dave-alldev}"
BASE="/Users/drm/projects/xnatworks"

echo "Staging to $TARGET"

# Continue past individual failures (a missing/unbuilt jar or a transfer error
# shouldn't abort the rest), but warn on each and report a summary at the end.
fails=0
warn() { printf '  !! WARN: %s\n' "$*" >&2; }

# XNAT web application (WAR) -> staging home
if ! scp $BASE/rtv/xnat/xnat-web/build/libs/*.war $TARGET:; then
    warn "WAR upload failed (xnat-web/build/libs/*.war)"
    fails=$((fails + 1))
fi

# Deployed plugins: each entry is a build/libs jar glob relative to $BASE.
# Jar names don't all follow their dir name (ohif-viewer, batch-launch,
# pipeline_engine_ui, xsync-plugin-all, *-fat vs *-xpl), so list them explicitly.
PLUGINS=(
    audit_trail_plugin/build/libs/audit_trail_plugin-*-xpl.jar
    container-service/build/libs/container-service-*-fat.jar
    xnat_cr_plugin/build/libs/xnat_cr_plugin-*-xpl.jar
    ohif-viewer-xnat-plugin/build/libs/ohif-viewer-*-fat.jar
    xnat-jupyterhub-plugin/build/libs/xnat-jupyterhub-plugin-1.3.5-SNAPSHOT.jar
    query_tracker_plugin/build/libs/query_tracker_plugin-1.0.1-SNAPSHOT-xpl.jar
    visit_template_plugin/build/libs/visit_template_plugin-1.1.0-SNAPSHOT-xpl.jar
    dicom_edit_plugin/build/libs/dicom_edit_plugin-1.1.0-SNAPSHOT-xpl.jar
    docker_swarm_plugin/build/libs/docker_swarm_plugin-1.1.0-SNAPSHOT-xpl.jar
    esign_plugin/build/libs/esign_plugin-1.0.0-SNAPSHOT-xpl.jar
    pII_review_plugin/build/libs/pII_review_plugin-1.2.0-SNAPSHOT-xpl.jar
    dashboards_plugin/build/libs/dashboards_plugin-1.2.0-SNAPSHOT-xpl.jar
    copy_project_settings_plugin/build/libs/copy_project_settings_plugin-1.2.0-SNAPSHOT-xpl.jar
    resources_tab_plugin/build/libs/resources_tab_plugin-1.2.0-SNAPSHOT-xpl.jar
    raphaeljs_plugin/build/libs/raphaeljs_plugin-1.1.0-SNAPSHOT-xpl.jar
    ldap-auth-plugin/build/libs/ldap-auth-plugin-*-xpl.jar
    xnat-dicomweb-plugin/build/libs/xnat-dicomweb-plugin-1.4.0-SNAPSHOT-xpl.jar
    mfa_plugin/build/libs/mfa-plugin-*-xpl.jar
    xnatx-batch-launch-plugin/build/libs/batch-launch-*-xpl.jar
    pipeline_engine_plugin/build/libs/pipeline_engine_ui-*-xpl.jar
    dicom-query-retrieve/build/libs/dicom-query-retrieve-*-xpl.jar
    xsync/build/libs/xsync-plugin-all-*.jar
)

for p in "${PLUGINS[@]}"; do
    if ! scp $BASE/$p $TARGET:plugins/; then
        warn "plugin upload failed: $p"
        fails=$((fails + 1))
    fi
done

if [ "$fails" -gt 0 ]; then
    printf '\n*** %d upload(s) FAILED — see WARN lines above ***\n' "$fails" >&2
    exit 1
fi
echo "All uploads succeeded ($((${#PLUGINS[@]} + 1)) files: WAR + ${#PLUGINS[@]} plugins)."
