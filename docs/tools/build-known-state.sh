#!/usr/bin/env bash
#
# build-known-state.sh — populate an EMPTY XNAT with a known, reproducible data set
# via the REST API (httpie), so it can serve as the fixture for golden-master testing.
#
# It exercises the migration-critical /data + /REST surface end to end:
#   users -> projects (+accessibility +membership) -> subjects (+demographics)
#         -> imaging sessions (shells, varied modality) -> scans -> resources -> files
#         -> stored searches (/data/search/saved) -> project config (/data/.../config)
#
# Structure built (deterministic labels): 3 users, 5 projects, 2 subjects/project,
# 2–3 sessions/subject, each session with a scan and a resource file. ~5 projects x
# 2 subjects x ~2.5 sessions = ~25 sessions. Plus 2 site stored searches and config
# (series-import-filter, custom tool, project anon script) on 2 projects.
#
# The search/config steps are best-effort (warn-and-continue) so a schema hiccup on an
# optional enrichment never aborts the core hierarchy build.
#
# Imaging data: most sessions are empty *shells* (xsiType only) — enough to exercise the
# experiment/scan/resource/file endpoints. In addition, one REAL MR session is imported
# from the repo's DICOM test fixtures (the "DHead" head-MR study under
# xnat-web/src/test/resources/dicom-web) via /data/services/import. Only MR exists in-repo,
# so CT/PET stay shells. Set WITH_DICOM=0 to skip the real import; add your own datasets
# via SESSION_DATASETS[] (see the import_dicom_zip hook).
#
# Requirements: httpie (`http`), an empty XNAT, admin credentials.
# Usage:
#   BASE_URL=http://localhost:8080 ADMIN_USER=admin ADMIN_PASS=secret ./build-known-state.sh
#   ./build-known-state.sh --dry-run          # print what it would do, make no changes
#
# Companion to data-rest-endpoint-reference.md / .json and golden_master.py.

set -euo pipefail

# ---- configuration ------------------------------------------------------------------
BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-}"
AUTH="${ADMIN_USER}:${ADMIN_PASS}"
DRY_RUN=false
[[ "${1:-}" == "--dry-run" ]] && DRY_RUN=true

# Password given to every created user (dev fixture only).
USER_PASS="${USER_PASS:-Fixture_pw_1!}"

# httpie output: quiet by default. Set HTTP_PRINT=hb (headers+body) to debug failures.
PRINT_OPT="--print=${HTTP_PRINT:-}"

# Users: username|first|last|project-role  (role is how they'll be shared onto projects)
USERS=(
  "kstate_owner|Olivia|Owner|Owners"
  "kstate_member|Max|Member|Members"
  "kstate_collab|Cora|Collaborator|Collaborators"
)

# Projects: id|accessibility  (cycles public/protected/private for access-path coverage)
PROJECTS=(
  "kstate_p1|public"
  "kstate_p2|protected"
  "kstate_p3|private"
  "kstate_p4|public"
  "kstate_p5|protected"
)

# Session modalities to cycle through (drives xsiType for session + scan).
MODALITIES=(mr ct pet)

# Datasets for real imaging import (leave empty to build shells only). Format:
#   "PROJECT|SUBJECT|SESSION|/abs/path/to/dicom.zip"
SESSION_DATASETS=()

# Real DICOM available in-repo: the well-known "DHead" MR study (shared StudyInstanceUID,
# SOP class = MR Image Storage). Imports as one real MR session. Only MR exists in-repo,
# so CT/PET sessions stay shells. Set WITH_DICOM=0 to skip real import (shells only).
WITH_DICOM="${WITH_DICOM:-1}"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# PROJECT|SUBJECT|SESSION_LABEL|space-separated repo-relative globs of DICOM files
DICOM_SOURCES=(
  "kstate_p1|kstate_p1_sub1|kstate_p1_sub1_MRreal|xnat-web/src/test/resources/dicom-web/1.MR.head_DHead.4.*.dcm"
)

# ---- plumbing -----------------------------------------------------------------------
C_OK=$'\033[32m'; C_WARN=$'\033[33m'; C_ERR=$'\033[31m'; C_DIM=$'\033[2m'; C_RST=$'\033[0m'
declare -i N_USER=0 N_PROJ=0 N_SUBJ=0 N_SESS=0 N_SCAN=0 N_RES=0 N_FILE=0 N_SEARCH=0 N_CONFIG=0

# Projects to receive config (subset, for variety). Index into PROJECTS by id.
CONFIG_PROJECTS=("kstate_p1" "kstate_p2")

log()  { printf '%s\n' "$*"; }
step() { printf '%s>>%s %s\n' "$C_DIM" "$C_RST" "$*"; }

# req METHOD URL [body-file]  — httpie call with admin auth and status checking.
req() {
  local method="$1" url="$2" body="${3:-}"
  if $DRY_RUN; then
    printf '%s[dry-run]%s %s %s%s\n' "$C_DIM" "$C_RST" "$method" "$url" "${body:+ <$body}"
    return 0
  fi
  local -a cmd=(http --check-status "$PRINT_OPT" --timeout=60 -a "$AUTH" "$method" "$url")
  if [[ -n "$body" ]]; then
    "${cmd[@]}" < "$body"
  else
    "${cmd[@]}" --ignore-stdin
  fi
}

# req_soft METHOD URL [body-file] — like req, but best-effort: a failure warns and
# continues instead of aborting the build (used for optional enrichments).
req_soft() {
  local method="$1" url="$2" body="${3:-}"
  if $DRY_RUN; then
    printf '%s[dry-run]%s %s %s%s\n' "$C_DIM" "$C_RST" "$method" "$url" "${body:+ <$body}"
    return 0
  fi
  local -a cmd=(http --check-status "$PRINT_OPT" --timeout=60 -a "$AUTH" "$method" "$url")
  local rc=0
  if [[ -n "$body" ]]; then "${cmd[@]}" < "$body" || rc=$?; else "${cmd[@]}" --ignore-stdin || rc=$?; fi
  [[ $rc -eq 0 ]] || echo "    ${C_WARN}warn${C_RST} $method $url (exit $rc) — skipped"
  return 0
}

# body_soft METHOD URL CONTENT-TYPE <<content — best-effort call with an inline body.
body_soft() {
  local method="$1" url="$2" ctype="$3"
  local tmp; tmp=$(mktemp); cat > "$tmp"
  if $DRY_RUN; then
    printf '%s[dry-run]%s %s %s %s\n' "$C_DIM" "$C_RST" "$method" "$url" "$ctype"; rm -f "$tmp"; return 0
  fi
  local rc=0
  http --check-status "$PRINT_OPT" --timeout=60 -a "$AUTH" "$method" "$url" "Content-Type:$ctype" < "$tmp" || rc=$?
  rm -f "$tmp"
  [[ $rc -eq 0 ]] || echo "    ${C_WARN}warn${C_RST} $method $url (exit $rc) — skipped"
  return 0
}

# xsiType helpers
sess_type() { printf 'xnat:%sSessionData' "$1"; }
scan_type() { printf 'xnat:%sScanData' "$1"; }

# ---- phases -------------------------------------------------------------------------
preflight() {
  step "Preflight: httpie + XNAT reachability"
  command -v http >/dev/null 2>&1 || { echo "${C_ERR}httpie ('http') not found — install it (pip install httpie).${C_RST}"; exit 1; }
  [[ -n "$ADMIN_PASS" ]] || { echo "${C_ERR}ADMIN_PASS is empty — set it in the environment.${C_RST}"; exit 1; }
  if ! $DRY_RUN; then
    http --check-status "$PRINT_OPT" --ignore-stdin --timeout=15 -a "$AUTH" GET "$BASE_URL/xapi/siteConfig/siteId" \
      || { echo "${C_ERR}Cannot reach/auth $BASE_URL — check BASE_URL/creds and that XNAT is up.${C_RST}"; exit 1; }
  fi
  log "${C_OK}ok${C_RST} — target: $BASE_URL as $ADMIN_USER"
}

create_users() {
  step "Users"
  local spec u first last
  for spec in "${USERS[@]}"; do
    IFS='|' read -r u first last _role <<< "$spec"
    # POST /xapi/users — JSON UserI (Spring MVC user-management API)
    if $DRY_RUN; then
      log "  [dry-run] POST /xapi/users ($u)"
    else
      printf '{"username":"%s","firstName":"%s","lastName":"%s","email":"%s@example.org","password":"%s","enabled":true,"verified":true}' \
        "$u" "$first" "$last" "$u" "$USER_PASS" \
        | http --check-status "$PRINT_OPT" --timeout=60 -a "$AUTH" POST "$BASE_URL/xapi/users" Content-Type:application/json
    fi
    log "  ${C_OK}user${C_RST} $u"; N_USER+=1
  done
}

create_project() {
  local id="$1" access="$2"
  # PUT /data/projects/{ID} with a minimal xnat:projectData document.
  local xml
  xml=$(printf '<xnat:projectData xmlns:xnat="http://nrg.wustl.edu/xnat" ID="%s">\n  <xnat:name>%s</xnat:name>\n  <xnat:description>Known-state fixture project %s</xnat:description>\n</xnat:projectData>' "$id" "$id" "$id")
  if $DRY_RUN; then
    log "  [dry-run] PUT /data/projects/$id (+accessibility=$access)"
  else
    printf '%s' "$xml" | http --check-status "$PRINT_OPT" --timeout=60 -a "$AUTH" PUT "$BASE_URL/data/projects/$id" Content-Type:application/xml
    req PUT "$BASE_URL/data/projects/$id/accessibility/$access"
  fi
  log "  ${C_OK}project${C_RST} $id ($access)"; N_PROJ+=1
}

share_members() {
  local id="$1" spec u role
  for spec in "${USERS[@]}"; do
    IFS='|' read -r u _first _last role <<< "$spec"
    # PUT /data/projects/{ID}/users/{GROUP}/{USER}
    req PUT "$BASE_URL/data/projects/$id/users/$role/$u"
    log "    ${C_DIM}+${role%s} $u${C_RST}"
  done
}

create_subject() {
  local proj="$1" label="$2" gender="$3"
  # PUT /data/projects/{ID}/subjects/{LABEL}?gender=...
  req PUT "$BASE_URL/data/projects/$proj/subjects/$label?gender=$gender"
  log "    ${C_OK}subject${C_RST} $label"; N_SUBJ+=1
}

create_session_shell() {
  local proj="$1" subj="$2" label="$3" modality="$4" date="$5"
  local st sc; st=$(sess_type "$modality"); sc=$(scan_type "$modality")
  # PUT experiment (session) under the subject — SubjAssessmentResource
  req PUT "$BASE_URL/data/projects/$proj/subjects/$subj/experiments/$label?xsiType=$st&date=$date&label=$label"
  N_SESS+=1
  # One scan on the session — ScanResource
  req PUT "$BASE_URL/data/projects/$proj/subjects/$subj/experiments/$label/scans/1?xsiType=$sc&type=${modality^^}&series_description=${modality^^}_series"
  N_SCAN+=1
  # A resource collection + a small file — CatalogResource / FileList
  req PUT "$BASE_URL/data/projects/$proj/subjects/$subj/experiments/$label/resources/NOTES?format=TEXT&content=DOC"
  N_RES+=1
  if ! $DRY_RUN; then
    local tmp; tmp=$(mktemp)
    printf 'known-state fixture note for %s/%s/%s (%s)\n' "$proj" "$subj" "$label" "$modality" > "$tmp"
    http --check-status "$PRINT_OPT" --timeout=60 -a "$AUTH" \
      PUT "$BASE_URL/data/projects/$proj/subjects/$subj/experiments/$label/resources/NOTES/files/readme.txt?inbody=true" \
      Content-Type:text/plain < "$tmp"
    rm -f "$tmp"
  fi
  N_FILE+=1
  log "      ${C_OK}session${C_RST} $label [$modality] +scan +resource-file"
}

# POST a DICOM zip to the import service (content-type aware, best-effort). Explicit
# SUBJECT_ID keeps the subject non-null so the site anon script can't NPE.
post_dicom_zip() {
  local proj="$1" subj="$2" sess="$3" zip="$4"
  local url="$BASE_URL/data/services/import?import-handler=DICOM-zip&dest=/archive&PROJECT_ID=$proj&SUBJECT_ID=$subj&EXPT_LABEL=$sess&overwrite=append&rename=true"
  if $DRY_RUN; then printf '%s[dry-run]%s POST %s <%s\n' "$C_DIM" "$C_RST" "$url" "$zip"; return 0; fi
  local rc=0
  http --check-status "$PRINT_OPT" --timeout=300 -a "$AUTH" POST "$url" Content-Type:application/zip < "$zip" || rc=$?
  [[ $rc -eq 0 ]] || echo "    ${C_WARN}warn${C_RST} import $proj/$subj/$sess (exit $rc) — skipped"
  return 0
}

# Hook for user-supplied datasets (SESSION_DATASETS[]).
import_dicom_zip() {
  local proj="$1" subj="$2" sess="$3" zip="$4"
  [[ -f "$zip" ]] || { echo "${C_WARN}dataset not found, skipping: $zip${C_RST}"; return 0; }
  step "Import DICOM $zip -> $proj/$subj/$sess"
  post_dicom_zip "$proj" "$subj" "$sess" "$zip"
  N_SESS+=1
}

# Real DICOM from the repo test fixtures (the DHead MR study). Stages matched files
# into a temp zip and imports one real MR session per DICOM_SOURCES entry.
import_repo_dicom() {
  [[ "$WITH_DICOM" == "1" ]] || { log "  ${C_DIM}(WITH_DICOM=0 — real DICOM import skipped)${C_RST}"; return 0; }
  command -v zip >/dev/null 2>&1 || { echo "  ${C_WARN}warn${C_RST} 'zip' not found — skipping real DICOM import"; return 0; }
  step "Real DICOM import (in-repo DHead MR study)"
  local spec proj subj sess globs g f
  for spec in "${DICOM_SOURCES[@]:-}"; do
    [[ -z "$spec" ]] && continue
    IFS='|' read -r proj subj sess globs <<< "$spec"
    local -a files=()
    for g in $globs; do for f in "$REPO_ROOT"/$g; do [[ -f "$f" ]] && files+=("$f"); done; done
    if [[ ${#files[@]} -eq 0 ]]; then echo "  ${C_WARN}warn${C_RST} no DICOM matched '$globs' — skipping $sess"; continue; fi
    if $DRY_RUN; then log "  [dry-run] zip ${#files[@]} file(s) -> import $proj/$subj/$sess"; N_SESS+=1; continue; fi
    local zip; zip="$(mktemp -u).zip"
    zip -j -q "$zip" "${files[@]}"
    post_dicom_zip "$proj" "$subj" "$sess" "$zip"
    rm -f "$zip"
    log "  ${C_OK}session${C_RST} $sess [mr, REAL DICOM: ${#files[@]} file(s)]"; N_SESS+=1
  done
}

build_hierarchy() {
  local pi=0 mi=0
  local proj access
  for pspec in "${PROJECTS[@]}"; do
    IFS='|' read -r proj access <<< "$pspec"
    step "Project $proj"
    create_project "$proj" "$access"
    share_members "$proj"
    # two subjects per project
    local s
    for s in 1 2; do
      local subj="${proj}_sub${s}" gender; [[ $((s % 2)) -eq 0 ]] && gender=female || gender=male
      create_subject "$proj" "$subj" "$gender"
      # subject 1 -> 3 sessions, subject 2 -> 2 sessions (variety)
      local n; [[ $s -eq 1 ]] && n=3 || n=2
      local k
      for ((k=1; k<=n; k++)); do
        local modality="${MODALITIES[$((mi % ${#MODALITIES[@]}))]}"; mi=$((mi+1))
        local sess="${subj}_${modality}${k}"
        local date; printf -v date '20%02d-%02d-%02d' $((20+pi)) $(( (k%12)+1 )) $(( (s*3)+1 ))
        create_session_shell "$proj" "$subj" "$sess" "$modality" "$date"
      done
    done
    pi=$((pi+1))
  done
  # Optional real-data imports
  local d
  for d in "${SESSION_DATASETS[@]:-}"; do
    [[ -z "$d" ]] && continue
    IFS='|' read -r dp ds de dz <<< "$d"
    import_dicom_zip "$dp" "$ds" "$de" "$dz"
  done
}

# One search field element for a stored-search bundle.
search_field() {
  local elem="$1" fid="$2" seq="$3" type="$4" header="$5"
  printf '  <xdat:search_field><xdat:element_name>%s</xdat:element_name><xdat:field_ID>%s</xdat:field_ID><xdat:sequence>%s</xdat:sequence><xdat:type>%s</xdat:type><xdat:header>%s</xdat:header></xdat:search_field>\n' \
    "$elem" "$fid" "$seq" "$type" "$header"
}

create_stored_searches() {
  step "Stored searches"
  local ns="http://nrg.wustl.edu/security"
  # 1) All MR sessions
  {
    printf '<xdat:stored_search xmlns:xdat="%s" ID="kstate_mr_sessions" brief-description="Known-state: all MR sessions" allow-diff-columns="0" secure="false">\n' "$ns"
    printf '  <xdat:root_element_name>xnat:mrSessionData</xdat:root_element_name>\n'
    search_field xnat:mrSessionData SESSION_ID    0 string Session
    search_field xnat:mrSessionData SUBJECT_LABEL 1 string Subject
    search_field xnat:mrSessionData PROJECT       2 string Project
    search_field xnat:mrSessionData DATE          3 date   Date
    printf '</xdat:stored_search>\n'
  } | body_soft PUT "$BASE_URL/data/search/saved/kstate_mr_sessions" application/xml
  log "  ${C_OK}search${C_RST} kstate_mr_sessions (MR sessions)"; N_SEARCH+=1
  # 2) All subjects
  {
    printf '<xdat:stored_search xmlns:xdat="%s" ID="kstate_subjects" brief-description="Known-state: all subjects" allow-diff-columns="0" secure="false">\n' "$ns"
    printf '  <xdat:root_element_name>xnat:subjectData</xdat:root_element_name>\n'
    search_field xnat:subjectData SUBJECT_LABEL 0 string Subject
    search_field xnat:subjectData PROJECT       1 string Project
    printf '</xdat:stored_search>\n'
  } | body_soft PUT "$BASE_URL/data/search/saved/kstate_subjects" application/xml
  log "  ${C_OK}search${C_RST} kstate_subjects (subjects)"; N_SEARCH+=1
}

configure_projects() {
  step "Project config"
  local proj
  for proj in "${CONFIG_PROJECTS[@]}"; do
    # (a) series import filter — ConfigResource, tool 'seriesImportFilter'.
    # Contents are a flat string->string JSON map (DicomFilterService.buildSeriesImportFilter);
    # mode is blacklist|whitelist|modalityMap, enabled is a *string*, and 'list' is a regex
    # block (patterns newline-separated; a single alternation works on one line, default field
    # is SeriesDescription).
    printf '{"mode":"blacklist","enabled":"true","list":"localizer|SCOUT"}' \
      | body_soft PUT "$BASE_URL/data/projects/$proj/config/seriesImportFilter/config?inbody=true" application/json
    # (b) a plain custom config entry — exercises ConfigResource generically
    printf '{"autoArchive":false,"note":"known-state fixture"}' \
      | body_soft PUT "$BASE_URL/data/projects/$proj/config/kstate-tool/settings?inbody=true" application/json
    # (c) project DICOM anon script + enable — DicomEdit config
    printf '//known-state fixture project anon script\nversion "6.1"\n(0010,0010) := "Anonymized^Fixture"\n' \
      | body_soft PUT "$BASE_URL/data/config/edit/projects/$proj/image/dicom/script" text/plain
    printf 'true' \
      | body_soft PUT "$BASE_URL/data/config/edit/projects/$proj/image/dicom/status" text/plain
    log "  ${C_OK}config${C_RST} $proj (seriesImportFilter, custom tool, anon script+status)"; N_CONFIG+=1
  done
}

summary() {
  log ""
  log "${C_OK}=== known state built ===${C_RST}"
  log "  users:${N_USER}  projects:${N_PROJ}  subjects:${N_SUBJ}  sessions:${N_SESS}  scans:${N_SCAN}  resources:${N_RES}  files:${N_FILE}"
  log "  stored-searches:${N_SEARCH}  configured-projects:${N_CONFIG}"
  $DRY_RUN && log "  ${C_WARN}(dry-run: nothing was written)${C_RST}"
  log ""
  log "  Next: capture the golden master, e.g."
  log "    ${C_DIM}docker/golden-capture.sh update${C_RST}"
}

# ---- main ---------------------------------------------------------------------------
preflight
create_users
build_hierarchy
import_repo_dicom
create_stored_searches
configure_projects
summary
