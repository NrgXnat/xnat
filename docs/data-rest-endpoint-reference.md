# `/data` + `/REST` Endpoint Reference (Restlet)

Auto-extracted from `xnat-web/.../restlet/XNATApplication.java` and the resource classes, then
curated for the migration-critical resources. Companion to
[migration-test-coverage-analysis.md](migration-test-coverage-analysis.md).

**Scope & fidelity.** XNAT's Restlet `/data` API is *not* self-describing (no OpenAPI/Swagger,
unlike `/xapi`). This table is reconstructed by static analysis: paths from the router, HTTP methods
from `allow{Get,Post,Put,Delete}()` overrides resolved up the `extends` chain, and query params from
`getQueryVariable(...)`/`handleParam(...)`/`URIManager` constants. It is a **best-effort lower bound** —
params built dynamically or read reflectively may be missing; a `?` on a method means the
`allow…()` override is conditional (context-dependent).

**Every resource also accepts these common query params** (from `SecureResource`): `_lock, _obsolete, _unlock, activate, allowDataDeletion, concealHiddenFields, dateFormat, format, hideTopBar, inbody, includeDetails, includeFiles, includeHeaders, includeHistory, limit, moveAssessors, offset, populateFromDB, removeFiles, req_format, requested_screen, sortBy` …

**Totals:** 86 resource classes, 203 path templates.

## /projects

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **ProjSubExptList** | `/projects/{PROJECT_ID}/experiments`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments` | POST, GET? | `columns`, `fields`, `xsiType` |
| **ProjectAccessibilityResource** | `/projects/{PROJECT_ID}/accessibility`<br>`/projects/{PROJECT_ID}/accessibility/{ACCESS_LEVEL}` | GET, PUT | — |
| **ProjectGroupResource** | `/projects/{PROJECT_ID}/groups`<br>`/projects/{PROJECT_ID}/groups/{GROUP_ID}` | POST, PUT, DELETE, GET? | — |
| **ProjectListResource** | `/projects` | GET, POST | `accessibility`, `activeSince`, `admin`, `allDataOverride`, `collaborator`, `columns`, `dataType`, `favorite`, `fields`, `member`, `permissions`, `prearc_code`, `recent`, `restrict`, `users`, `xsiType` |
| **ProjectMemberResource** | `/projects/{PROJECT_ID}/users/{GROUP_ID}/{USER_ID}`<br>`/projects/{PROJECT_ID}/users/{GROUP_ID}/{USER_ID}/{DISPLAY_HIDDEN_USERS}` | GET? | `sendemail` |
| **ProjectPARListResource** | `/projects/{PROJECT_ID}/pars` | GET? | — |
| **ProjectResource** | `/projects/{PROJECT_ID}` | PUT, DELETE, GET? | `accessibility`, `testHyphen`, `xsiType` |
| **ProjectSearchResource** | `/projects/{PROJECT_ID}/searches/{SEARCH_ID}` | PUT, DELETE, GET? | — |
| **ProjectSubjectList** | `/projects/{PROJECT_ID}/subjects` | POST, GET? | `columns`, `fields`, `xsiType` |
| **ProjectUserListResource** | `/projects/{PROJECT_ID}/users`<br>`/projects/{PROJECT_ID}/users/{DISPLAY_HIDDEN_USERS}` | GET? | `includeAllDataAccess` |
| **ProtocolResource** | `/projects/{PROJECT_ID}/protocols/{PROTOCOL_ID}` | PUT, DELETE, GET? | `dataType`, `gender` |
| **ScanTypeListing** | `/projects/{PROJECT_ID}/scan_types`<br>`/scan_types` | GET? | `table` |
| **SubjAssessmentResource** | `/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}` | PUT, DELETE, GET? | `columns`, `fields`, `label`, `overwrite`, `primary`, `subject_ID`, `xsiType` |
| **SubjectResource** | `/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}`<br>`/subjects/{SUBJECT_ID}` | PUT, DELETE, GET? | `gender`, `label` |

## /experiments

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **CatalogResource** | `/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/resources/{RESOURCE_ID}`<br>`/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/resources/{RESOURCE_ID}`<br>`/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/resources/{RESOURCE_ID}`<br>`/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/resources/{RESOURCE_ID}`<br>`/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/resources/{RESOURCE_ID}`<br>`/experiments/{EXPT_ID}/resources/{RESOURCE_ID}`<br>`/projects/{PROJECT_ID}/experiments/{EXPT_ID}/resources/{RESOURCE_ID}`<br>`/projects/{PROJECT_ID}/resources/{RESOURCE_ID}`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/resources/{RESOURCE_ID}`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/resources/{RESOURCE_ID}`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/resources/{RESOURCE_ID}`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/resources/{RESOURCE_ID}`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/resources/{RESOURCE_ID}`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/resources/{RESOURCE_ID}`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/resources/{RESOURCE_ID}`<br>`/subjects/{SUBJECT_ID}/resources/{RESOURCE_ID}` | POST, PUT, DELETE, GET? | `all`, `content`, `description`, `includeRootPath`, `tags` |
| **CatalogResourceList** | `/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/resources`<br>`/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/resources`<br>`/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/resources`<br>`/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/resources`<br>`/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/resources`<br>`/experiments/{EXPT_ID}/resources`<br>`/projects/{PROJECT_ID}/experiments/{EXPT_ID}/resources`<br>`/projects/{PROJECT_ID}/resources`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/resources`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/resources`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/resources`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/resources`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/resources`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/resources`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/resources`<br>`/subjects/{SUBJECT_ID}/resources` | POST, PUT, GET? | `all`, `cache_file_stats`, `content`, `description`, `file_stats`, `tags` |
| **DIRResource** | `/experiments/{EXPT_ID}/DIR`<br>`/experiments/{EXPT_ID}/XAR`<br>`/projects/{PROJECT_ID}/experiments/{EXPT_ID}/DIR`<br>`/projects/{PROJECT_ID}/experiments/{EXPT_ID}/XAR` | GET? | `recursive` |
| **ExperimentListResource** | `/experiments` | GET? | `columns`, `fields`, `recent`, `xsiType` |
| **ExperimentResource** | `/experiments/{EXPT_ID}`<br>`/projects/{PROJECT_ID}/experiments/{EXPT_ID}` | GET? | `label`, `overwrite`, `primary`, `subject_ID`, `xsiType` |
| **ExptAssessmentResource** | `/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}` | PUT, DELETE, GET? | `label`, `primary`, `xsiType` |
| **FileList** | `/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/files`<br>`/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/resources/{RESOURCE_ID}/files`<br>`/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/files`<br>`/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/resources/{RESOURCE_ID}/files`<br>`/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/files`<br>`/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/resources/{RESOURCE_ID}/files`<br>`/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/files`<br>`/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/resources/{RESOURCE_ID}/files`<br>`/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/files`<br>`/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/resources/{RESOURCE_ID}/files`<br>`/experiments/{EXPT_ID}/files`<br>`/experiments/{EXPT_ID}/resources/{RESOURCE_ID}/files`<br>`/projects/{PROJECT_ID}/experiments/{EXPT_ID}/resources/{RESOURCE_ID}/files`<br>`/projects/{PROJECT_ID}/files`<br>`/projects/{PROJECT_ID}/resources/{RESOURCE_ID}/files`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/files`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/resources/{RESOURCE_ID}/files`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/files`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/resources/{RESOURCE_ID}/files`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/files`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/resources/{RESOURCE_ID}/files`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/files`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/resources/{RESOURCE_ID}/files`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/files`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/resources/{RESOURCE_ID}/files`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/files`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/resources/{RESOURCE_ID}/files`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/files`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/resources/{RESOURCE_ID}/files`<br>`/subjects/{SUBJECT_ID}/files`<br>`/subjects/{SUBJECT_ID}/resources/{RESOURCE_ID}/files` | POST, PUT, DELETE, GET? | `all`, `async`, `content`, `delete`, `description`, `extract`, `file_content`, `file_format`, `improved`, `includeRootPath`, `index`, `listContents`, `notify`, `overwrite`, `projectIncludedInPath`, `reference`, `simplified`, `structure`, `subjectIncludedInPath`, `tags` |
| **ProjSubExptAsstList** | `/experiments/{ASSESSED_ID}/assessors`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors` | POST, GET? | `columns`, `fields`, `xsiType` |
| **ReconList** | `/experiments/{ASSESSED_ID}/reconstructions`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions` | POST, GET? | `columns`, `fields`, `type`, `xsiType` |
| **ReconResource** | `/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}` | PUT, DELETE, GET? | `type`, `xsiType` |
| **ScanDIRResource** | `/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/DICOMDIR`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/DICOMDIR` | DELETE, GET? | `type`, `xsiType` |
| **ScanList** | `/experiments/{ASSESSED_ID}/scans`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/scans` | POST, GET? | `columns`, `fields`, `type`, `xsiType` |
| **ScanResource** | `/experiments/{ASSESSED_ID}/scans/{SCAN_ID}`<br>`/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/scans/{SCAN_ID}` | PUT, DELETE, GET? | `type`, `xsiType` |

## /subjects

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **SubjectListResource** | `/subjects` | GET? | `columns`, `fields`, `xsiType` |

## /services

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **AliasTokenRestlet** | `/services/tokens/{OPERATION}`<br>`/services/tokens/{OPERATION}/user/{USERNAME}`<br>`/services/tokens/{OPERATION}/{TOKEN}`<br>`/services/tokens/{OPERATION}/{TOKEN}/{SECRET}` | GET? | — |
| **ArchiveValidator** | `/services/validate-archive` | POST | `dest` |
| **Archiver** | `/services/archive` | POST | `dest`, `overwrite`, `overwrite_files` |
| **AuditRestlet** | `/services/audit` | GET? | — |
| **AuthenticationRestlet** | `/services/auth` | POST, PUT | `authenticatorId`, `j_password`, `j_username`, `login_method`, `password`, `provider`, `username` |
| **DicomDump** | `/services/dicomdump` | GET? | `SCAN_ID` |
| **EcatDump** | `/services/ecatdump` | GET? | `SCAN_ID` |
| **FeatureDefinitionRestlet** | `/services/features` | POST, GET? | `group`, `tag`, `type` |
| **Importer** | `/services/import` | POST | `src`, `transaction` |
| **IpWhitelist** | `/services/ipwhitelist` | PUT, GET? | — |
| **MailRestlet** | `/services/mail/send` | POST | — |
| **MoveFiles** | `/services/move-files` | POST, GET? | — |
| **PrearchiveBatchDelete** | `/services/prearchive/delete` | POST | — |
| **PrearchiveBatchMove** | `/services/prearchive/move` | POST | — |
| **PrearchiveBatchRebuild** | `/services/prearchive/rebuild` | POST | — |
| **RefreshCatalog** | `/services/refresh/catalog` | GET, POST | `append`, `checksum`, `delete`, `options`, `populateStats`, `resource` |
| **ScanQualityLabelRestlet** | `/services/scan-quality-labels`<br>`/services/scan-quality-labels/{PROJECT_ID}` | GET? | — |
| **SendEmailVerification** | `/services/sendEmailVerification` | POST, GET?, PUT?, DELETE? | `email` |
| **SessionCountRestlet** | `/services/sessions`<br>`/services/sessions/{USERNAME}` | GET? | — |
| **TriageApprovalRestlet** | `/services/triage/approve` | POST, PUT, GET? | `PROJECT_ID` |
| **TriageRestlet** | `/services/triage/projects/{PROJECT}/resources`<br>`/services/triage/projects/{PROJECT}/resources/{XNAME}`<br>`/services/triage/projects/{PROJECT}/resources/{XNAME}/files`<br>`/services/triage/projects/{PROJECT}/resources/{XNAME}/files/{FILE}` | GET, POST, PUT, DELETE | — |
| **VerifyExtensionsRestlet** | `/services/extensions/verify` | GET? | — |
| **WorkflowsRestlet** | `/services/workflows`<br>`/services/workflows/workflowid/{WORKFLOW_PRIMARY_KEY}`<br>`/services/workflows/{PIPELINE_NAME}` | GET? | `display`, `experiment`, `latest_by_param`, `project`, `status` |

## /search

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **CachedSearchColumnResource** | `/search/{CACHED_SEARCH_ID}/{COLUMN}` | GET? | — |
| **CachedSearchResource** | `/search/{CACHED_SEARCH_ID}` | GET? | `sortOrder` |
| **SavedSearchListResource** | `/search/saved` | GET? | `all`, `includeTag`, `user` |
| **SavedSearchResource** | `/search/saved/{SEARCH_ID}` | PUT, DELETE, GET? | `dv`, `guiStyle`, `project`, `refresh`, `saveAs`, `sortOrder` |
| **SearchElementListResource** | `/search/elements` | POST, GET? | `complexType`, `extends`, `name`, `plural`, `prefix`, `readable`, `secured`, `singular`, `task`, `used` |
| **SearchFieldListResource** | `/search/elements/{ELEMENT_NAME}` | PUT, GET? | `addSqlQueryValue`, `code`, `description`, `fieldId`, `header`, `plural`, `projectScope`, `searchable`, `showAll`, `singular`, `value` |
| **SearchFieldsVersionListResource** | `/search/elements/{ELEMENT_NAME}/versions` | PUT, GET? | `listViews`, `v2` |
| **SearchResource** | `/search` | POST | `cache`, `refresh`, `sortOrder` |

## /automation

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **ScriptResource** | `/automation/scripts`<br>`/automation/scripts/{SCRIPT_ID}`<br>`/automation/scripts/{SCRIPT_ID}/{VERSION}` | PUT, DELETE, GET? | — |
| **ScriptRunnerResource** | `/automation/runners`<br>`/automation/runners/{LANGUAGE}`<br>`/automation/runners/{LANGUAGE}/{VERSION}` | GET? | — |
| **ScriptTriggerResource** | `/automation/handlers`<br>`/automation/handlers/{EVENT_ID}`<br>`/automation/triggers`<br>`/automation/triggers/{TRIGGER_ID}`<br>`/projects/{PROJECT_ID}/automation/handlers`<br>`/projects/{PROJECT_ID}/automation/handlers/{EVENT_ID}` | PUT, DELETE, GET? | — |
| **ScriptTriggerTemplateResource** | `/automation/templates`<br>`/automation/templates/{TEMPLATE_ID}`<br>`/projects/{PROJECT_ID}/automation/templates`<br>`/projects/{PROJECT_ID}/automation/templates/{TEMPLATE_ID}` | PUT, DELETE, GET? | — |
| **ScriptVersionsResource** | `/automation/scriptVersions`<br>`/automation/scriptVersions/{SCRIPT_ID}` | GET? | — |
| **WorkflowEventResource** | `/automation/workflows`<br>`/automation/workflows/{SPEC}` | GET? | — |

## /workflows

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **WorkflowResource** | `/workflows`<br>`/workflows/{WORKFLOW_ID}` | GET, PUT | — |

## /prearchive

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **PrearcSessionListResource** | `/prearchive` | PUT, GET? | `tag` |
| **RecentPrearchiveSessions** | `/prearchive/experiments` | GET? | — |

## /pars

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **PARList** | `/pars` | GET? | — |
| **PARResource** | `/pars/{PAR_ID}` | PUT, GET? | `accept`, `decline` |

## /config

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **ConfigResource** | `/config`<br>`/config/{TOOL_NAME}`<br>`/config/{TOOL_NAME}/{PATH_TO_FILE}`<br>`/projects/{PROJECT_ID}/config`<br>`/projects/{PROJECT_ID}/config/{TOOL_NAME}`<br>`/projects/{PROJECT_ID}/config/{TOOL_NAME}/{PATH_TO_FILE}` | PUT, DELETE, GET? | `action`, `contents`, `defaultToSiteWide`, `meta`, `status`, `unversioned`, `version` |
| **DicomEdit** | `/config/edit/image/dicom/{RESOURCE}`<br>`/config/edit/projects/{PROJECT_ID}/image/dicom/{RESOURCE}` | GET, PUT | `PROJECT_ID`, `all`, `script`, `status` |
| **ProjectArchive** | `/config/{PROJECT_ID}/archive_spec`<br>`/projects/{PROJECT_ID}/archive_spec` | GET? | — |

## /user

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **UserCacheResource** | `/user/cache/resources`<br>`/user/cache/resources/{XNAME}`<br>`/user/cache/resources/{XNAME}/files`<br>`/user/cache/resources/{XNAME}/files/{FILE}` | GET, POST, PUT, DELETE | `extract` |
| **UserRolesRestlet** | `/user/{USER_ID}/roles` | POST, GET? | `roles` |
| **UserSessionId** | `/user/{USER_ID}/sessions` | DELETE, GET? | — |
| **UserSettingsRestlet** | `/user`<br>`/user/actions/{ACTION}`<br>`/user/actions/{USER_ID}/{ACTION}`<br>`/user/{USER_ID}` | GET? | — |

## /users

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **UserFavoriteResource** | `/users/favorites/{DATA_TYPE}/{PROJECT_ID}` | PUT, DELETE | — |
| **UserFavoritesList** | `/users/favorites/{DATA_TYPE}` | GET? | — |
| **UserListResource** | `/users` | GET | — |

## /investigators

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **InvestigatorListResource** | `/investigators` | GET | — |

## /scanners

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **ScannerListing** | `/scanners` | GET? | `table` |

## /status

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **SQListenerRepresentation** | `/status/{TRANSACTION_ID}` | POST, DELETE, GET? | — |

## /auth

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **UserAuth** | `/auth` | GET | — |

## /JSESSION

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **UserSession** | `/JSESSION` | POST, DELETE, GET? | — |

## /routing

| Resource | Path(s) | Methods | Resource-specific query params |
|---|---|---|---|
| **StudyRoutingRestlet** | `/routing`<br>`/routing/{STUDY_INSTANCE_UID}` | PUT, DELETE, GET? | — |

---

## Curated details — migration-critical resources

Hand-verified parameters for the endpoints most exercised by the migration (their params are read via
`handleParam`/`URIManager` constants downstream, so the auto-table under-reports them).

### `POST /data/services/import` — `Importer`
The DICOM/session import entry point. Body = the payload (a DICOM/zip/XAR, or multipart). Params (query or form):

| Param | Meaning |
|---|---|
| `import-handler` | importer to use (`DICOM-zip`, `SI` session-importer, `gradual-DICOM`, …) |
| `dest` | destination URI (`/prearchive`, `/archive/projects/{ID}`, …) |
| `project` | target project id |
| `subject` / `SUBJECT_ID` | target subject (label or id) |
| `EXPT_LABEL` | session/experiment label |
| `overwrite` | `none` \| `append` \| `delete` |
| `prearchive_code` | auto-archive behaviour |
| `quarantine`, `Direct-Archive` | routing flags |
| `PREVENT_ANON`, `PREVENT_AUTO_COMMIT` | skip site anon / skip auto-commit |
| `rename`, `inbody` | file-naming / in-body payload flags |
| `SOURCE` | upload source tag |
| `src` | pre-stored file reference (async path) |
| `http-session-listener`, `transaction` | progress-listener / transaction record id |
| `action=commit` | commit a built prearchive session |

### `POST /data/services/prearchive/{delete,move,rebuild}` — `PrearchiveBatch*`
Batch prearchive operations. **These declare no variants** — the class fixed by the SecureResource
no-variant bridge (pre-fix these returned `405`).

| Param | Meaning |
|---|---|
| `src` | session reference(s), repeatable: `/prearchive/projects/{PROJECT}/{TIMESTAMP}/{FOLDER}` |
| `async` | run asynchronously (`true`/`false`) |
| `newProject` | (**move** only) destination project |

### `POST /data/search` (+ `/data/search/saved/{ID}/results`) — `SearchResource`
Runs a search. Body = search-document XML or a stored-search bundle (the form-wrapped-XML fix lives here).

| Param | Meaning |
|---|---|
| `format` | `xml` \| `json` \| `csv` \| `html` |
| `limit`, `offset` | paging |
| `sortBy`, `sortOrder` | sort column / direction |
| `cache`, `refresh` | use / rebuild the cached result table |

> Not curated: `DicomDump`/`EcatDump` (`src`, `field`) and the annotation-based extension routes read params
> in ways not statically confirmed here — see the resource classes directly.
