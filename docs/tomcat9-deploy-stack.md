# Tomcat 9 deploy stack — runtime validation of the Phase 0 migration

Brings up the migrated XNAT WAR (Restlet 2.5.2 / Turbine 5.1 / Velocity 2.4.1, still **javax /
Tomcat 9**) with PostgreSQL + ActiveMQ, so the runtime seams that a compile can't prove get
exercised. This is the validation step after `feature/turbine-5x` reached compile-green + the
Turbine service-container boot test (see `phase0-compile-migration-summary.md`).

## What's here
| File | Role |
|---|---|
| `Dockerfile` (repo root, pre-existing) | Tomcat 9 / jdk21 image; expects `docker-context/xnat.war` |
| `docker/make-xnat-config.sh`, `docker/entrypoint.sh` (pre-existing) | seeds `xnat-conf.properties`, timezone |
| `docker-compose.yml` | **new** — wires `xnat-postgresql` + `xnat-activemq` + `xnat` |
| `docker/stage-war.sh` | **new** — builds `:xnat-web:war` → `docker-context/xnat.war` |

## Prerequisites
- Docker (with `docker compose` v2) and ~4–6 GB free RAM for the XNAT container.
- Java 21 on the host (the Gradle toolchain) to build the WAR.

## Run
```bash
./docker/stage-war.sh          # build + stage the WAR (or: ./gradlew :xnat-web:war first, then --skip-build)
docker compose up --build      # start db + broker + xnat
# first boot runs Hibernate schema creation (hbm2ddl=update) + XNAT init — allow several minutes
```
Then open **http://localhost:8080**. First boot lands on the XNAT initial-setup / login page —
which is itself a Turbine+Velocity screen, so reaching it already exercises the render chain.

Logs: `docker compose logs -f xnat`. (The CI image rewrites `logback.xml` to a ConsoleAppender via
`scripts/edit-log.py`, which isn't in this repo; if app logs don't appear on stdout, tail the file
inside the container: `docker compose exec xnat sh -lc 'tail -F /data/xnat/home/logs/*.log'`.)

Teardown: `docker compose down` (add `-v` to also drop the db + data volumes for a clean re-run).

## Smoke-test checklist — mapped to the migration seams
Most of this is automated by **`./docker/health-check.sh`** (walks the endpoints below, prints
PASS/FAIL/SKIP, exits non-zero on any failure). It waits for readiness first, so you can run it
right after `docker compose up`:
```bash
XNAT_USER=admin XNAT_PASS=admin ./docker/health-check.sh
```
The table below is what it checks (data-dependent rows are SKIPped and left for manual verification).
Each row targets a specific piece that only a running server can validate.

| Check | Exercises | Migration piece |
|---|---|---|
| Reach the login / setup page at `/` | Turbine `PageLoader` → Velocity 2.4.1 render via `CustomClasspathResourceLoader` | Velocity 2 SPI (`getResourceReader`), YAAFI VelocityService |
| Log in as admin; load the main dashboard `/app/template/Index.vm` | Screen render, `SecureScreen`/`VelocitySecureScreen`, `PipelineData` | RunData→PipelineData codemod, `TurbineUtils.getVelocityContext` |
| `GET /data/projects?format=json` (and `format=xml`, `format=html`) | Restlet resource dispatch + content negotiation | **`SecureResource`→`ServerResource` shim** (`get(Variant)`↔`represent`), `XnatServerResourceFinder` |
| `POST`/`PUT` to a writable `/data/...` resource | 1.1 `handlePost/handlePut` bridge + 405 on disallowed methods | shim `post/put(Variant)`→`handleX`, `getAllowedMethods()` |
| Download a session/scan as zip/tar (`.../files?format=zip`) | `ZipRepresentation` Content-Disposition | Restlet 2.x Disposition API |
| Load an `/app` screen served through a Restlet resource | `TurbineScreenRepresentation` builds RunData + renders | **RestletRunData bridge** (`RunDataService.getRunData("restlet",…)`, `ServletUtils`, `Response.getCurrent()`) |
| Multipart upload (e.g. issue report attachment, XML/CSV import) | Fulcrum parser `getPart` + `Files.copy` | parser `getFileItem`→`getPart` migration |
| Confirm startup logs are clean | log4j2 (Turbine) vs Logback (XNAT) coexistence | **log4j2 ↔ Logback reconciliation** (watch for binding warnings / `NoSuchMethodError`) |

## Known things to watch on first boot
- **log4j2 ↔ Logback**: Turbine 5.1 requires `log4j-core`; XNAT logs via Logback/SLF4J. Watch the
  startup log for `SLF4J: Class path contains multiple bindings` or a log4j `NoSuchMethodError`.
  This is the one Phase-0b reconciliation item still open.
- **Schema init** is slow on the first `up`; the app is not reachable until it finishes.
- The stack is **dev-only** (default `xnat`/`xnat` DB creds, unauthenticated broker). Do not expose it.

## Golden-master capture/check
`./docker/golden-capture.sh` drives `docs/tools/golden_master.py` against the running stack: it hits a
curated set of `/app` screens + `/data`/`/xapi` endpoints, normalizes volatile bits (CSRF, session id,
timestamps, cache-busters), and stores/compares each response.
```bash
XNAT_USER=admin XNAT_PASS=admin ./docker/golden-capture.sh update   # capture baselines (on known-good)
./docker/golden-capture.sh check                                    # diff current vs baselines
```
Baselines land in `docs/goldens/` (override with `GOLDEN_DIR`). **They are tied to the underlying
data** — capture and check against the *same* seeded/snapshotted DB, else diffs reflect data changes,
not migration bugs. For a true migration diff, capture on the pre-migration (`develop`/Turbine 2.3.3)
build, then `check` on the migrated build against the same DB. Edit the `ENDPOINTS` list in the script
to add data-specific rows (a report screen for a known project id, a zip download, etc.).

## Not covered here
Full automated regression — `git@github.com:NrgXnat/xnat-rest-tests.git` (the 1,314-test REST suite over
`/data` + `/xapi`), run against this running stack once the smoke tests + golden checks pass.
