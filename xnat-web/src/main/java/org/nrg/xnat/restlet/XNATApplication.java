/*
 * web: org.nrg.xnat.restlet.XNATApplication
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.helpers.dicom.DicomDump;
import org.nrg.xnat.helpers.editscript.DicomEdit;
import org.nrg.xnat.restlet.actions.UserSessionId;
import org.nrg.xnat.restlet.guard.XnatSecureGuard;
import org.nrg.xnat.restlet.resources.*;
import org.nrg.xnat.restlet.resources.files.*;
import org.nrg.xnat.restlet.resources.prearchive.*;
import org.nrg.xnat.restlet.resources.search.*;
import org.nrg.xnat.restlet.services.*;
import org.nrg.xnat.restlet.services.mail.MailRestlet;
import org.nrg.xnat.restlet.services.prearchive.PrearchiveBatchDelete;
import org.nrg.xnat.restlet.services.prearchive.PrearchiveBatchMove;
import org.nrg.xnat.restlet.services.prearchive.PrearchiveBatchRebuild;
import org.nrg.xnat.restlet.transaction.monitor.SQListenerRepresentation;
import org.restlet.*;
import org.restlet.resource.Resource;
import org.restlet.util.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class XNATApplication extends Application {
    public static final String PREARC_PROJECT_URI = "/prearchive/projects/{PROJECT_ID}";
    public static final String PREARC_SESSION_URI = PREARC_PROJECT_URI + "/{SESSION_TIMESTAMP}/{SESSION_LABEL}";

    @SuppressWarnings("WeakerAccess")
    @JsonIgnore
    public XNATApplication(Context parentContext) {
        super(parentContext);
    }

    @Override
    public synchronized Restlet createRoot() {
        Router securedRouter = new Router(getContext());

        initializeRouteTable();

        addRoutes(securedRouter);

        List<Class<? extends Resource>> publicRoutes = addExtensionRoutes(securedRouter);

        Router rootRouter = new Router(getContext());

        XnatSecureGuard guard = new XnatSecureGuard();
        guard.setNext(securedRouter);
        rootRouter.attach(guard);

        if (isRestMockServiceEnabled()) {
            _log.debug("Found UI.show-mock-rest-config set to true, mapping configured mock REST service routes.");
            addConfiguredRoutes(rootRouter);
        }

        addPublicRoutes(rootRouter, publicRoutes);

        if (_log.isInfoEnabled()) {
            _log.info("Configured the following routes:\n" + _routeBuffer.toString());
        }

        return rootRouter;
    }

    private void initializeRouteTable() {
        _routeBuffer = new StringBuilder("URI\tClass\tMatching Mode").append(System.getProperty("line.separator"));
    }

    private boolean isRestMockServiceEnabled() {
        try {
            final String showMockRestConfig = XDAT.getSiteConfigurationProperty("UI.show-mock-rest-config");
            return !StringUtils.isBlank(showMockRestConfig) && Boolean.parseBoolean(showMockRestConfig);
        } catch (ConfigServiceException e) {
            return false;
        }
    }

    private void attachArchiveURI(final Router router, final String uri, final Class<? extends Resource> clazz) {
        attachURI(router, uri.intern(), clazz);
        attachURI(router, ("/archive" + uri).intern(), clazz);
    }

    private void attachURIs(final Router router, final Class<? extends Resource> clazz, final String... uris) {
        for (final String uri : uris) {
            attachURI(router, uri, clazz);
        }
    }

    private void attachURI(final Router router, final String uri, final Class<? extends Resource> clazz) {
        attachURI(router, uri, clazz, null);
    }

    private void attachURI(final Router router, final String uri, final Class<? extends Resource> clazz, Integer matchingMode) {
        if (_log.isInfoEnabled()) {
            logAttachedRoute(uri, clazz, matchingMode);
        }
        Route route = router.attach(uri.intern(), clazz);
        if (matchingMode != null) {
            route.setMatchingMode(matchingMode);
        }
    }

    private void logAttachedRoute(final String uri, Class<? extends Resource> clazz, final Integer matchingMode) {
        _routeBuffer.append(uri.intern()).append("\t").append(clazz.getCanonicalName()).append("\t");
        if (matchingMode != null) {
            _routeBuffer.append(getMatchingMode(matchingMode));
        }
        _routeBuffer.append(System.getProperty("line.separator"));
    }

    private String getMatchingMode(final Integer matchingMode) {
        if (matchingMode == null) {
            return null;
        }
        switch (matchingMode) {
            case Template.MODE_EQUALS:
                return "Equals";
            case Template.MODE_STARTS_WITH:
                return "Starts with";
        }
        return null;
    }

    private void addRoutes(final Router router) {
        attachArchiveURI(router, "/investigators", InvestigatorListResource.class);

        //BEGIN ---- Pipelines section
        attachArchiveURI(router, "/projects/{PROJECT_ID}/pipelines", ProjectPipelineListResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/pipelines/{STEP_ID}/experiments/{EXPT_ID}", ProjtExptPipelineResource.class);
        //END ---- Pipelines section
        attachArchiveURI(router, "/config/edit/image/dicom/{RESOURCE}", DicomEdit.class);
        attachArchiveURI(router, "/config/edit/projects/{PROJECT_ID}/image/dicom/{RESOURCE}", DicomEdit.class);
        attachArchiveURI(router, "/config/{PROJECT_ID}/archive_spec", ProjectArchive.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/archive_spec", ProjectArchive.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/experiments", ProjSubExptList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/experiments/{EXPT_ID}", ExperimentResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/users", ProjectUserListResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/users/{DISPLAY_HIDDEN_USERS}", ProjectUserListResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/users/{GROUP_ID}/{USER_ID}", ProjectMemberResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/users/{GROUP_ID}/{USER_ID}/{DISPLAY_HIDDEN_USERS}", ProjectMemberResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/searches/{SEARCH_ID}", ProjectSearchResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/groups", ProjectGroupResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/groups/{GROUP_ID}", ProjectGroupResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects", ProjectSubjectList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}", SubjectResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments", ProjSubExptList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}", SubjAssessmentResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors", ProjSubExptAsstList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}", ExptAssessmentResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/scans", ScanList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/scans/{SCAN_ID}", ScanResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/DICOMDIR", ScanDIRResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions", ReconList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}", ReconResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/accessibility", ProjectAccessibilityResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/accessibility/{ACCESS_LEVEL}", ProjectAccessibilityResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}", ProjectResource.class);
        attachArchiveURI(router, "/projects", ProjectListResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/scan_types", ScanTypeListing.class);
        attachArchiveURI(router, "/scan_types", ScanTypeListing.class);
        attachArchiveURI(router, "/scanners", ScannerListing.class);

        attachArchiveURI(router, "/projects/{PROJECT_ID}/protocols/{PROTOCOL_ID}", ProtocolResource.class);

        attachArchiveURI(router, "/experiments", ExperimentListResource.class);
        attachArchiveURI(router, "/experiments/{EXPT_ID}", ExperimentResource.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/scans", ScanList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/scans/{SCAN_ID}", ScanResource.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/DICOMDIR", ScanDIRResource.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/reconstructions", ReconList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}", ReconResource.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/assessors", ProjSubExptAsstList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}", ExptAssessmentResource.class);

        attachArchiveURI(router, "/subjects/{SUBJECT_ID}", SubjectResource.class);
        attachArchiveURI(router, "/subjects", SubjectListResource.class);

        //resources
        attachArchiveURI(router, "/projects/{PROJECT_ID}/resources", CatalogResourceList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/resources", CatalogResourceList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/resources", CatalogResourceList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/resources", CatalogResourceList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/resources", CatalogResourceList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/resources", CatalogResourceList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/resources", CatalogResourceList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/resources", CatalogResourceList.class);
        attachArchiveURI(router, "/experiments/{EXPT_ID}/resources", CatalogResourceList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/resources", CatalogResourceList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/resources", CatalogResourceList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/resources", CatalogResourceList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/resources", CatalogResourceList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/resources", CatalogResourceList.class);
        attachArchiveURI(router, "/subjects/{SUBJECT_ID}/resources", CatalogResourceList.class);

        //resources (catalogs)
        attachArchiveURI(router, "/projects/{PROJECT_ID}/resources/{RESOURCE_ID}", CatalogResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/resources/{RESOURCE_ID}", CatalogResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/resources/{RESOURCE_ID}", CatalogResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/resources/{RESOURCE_ID}", CatalogResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/resources/{RESOURCE_ID}", CatalogResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/resources/{RESOURCE_ID}", CatalogResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/resources/{RESOURCE_ID}", CatalogResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/resources/{RESOURCE_ID}", CatalogResource.class);
        attachArchiveURI(router, "/experiments/{EXPT_ID}/resources/{RESOURCE_ID}", CatalogResource.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/resources/{RESOURCE_ID}", CatalogResource.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/resources/{RESOURCE_ID}", CatalogResource.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/resources/{RESOURCE_ID}", CatalogResource.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/resources/{RESOURCE_ID}", CatalogResource.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/resources/{RESOURCE_ID}", CatalogResource.class);
        attachArchiveURI(router, "/subjects/{SUBJECT_ID}/resources/{RESOURCE_ID}", CatalogResource.class);

        //resource files
        attachArchiveURI(router, "/projects/{PROJECT_ID}/resources/{RESOURCE_ID}/files", FileList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/resources/{RESOURCE_ID}/files", FileList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/resources/{RESOURCE_ID}/files", FileList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/resources/{RESOURCE_ID}/files", FileList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/resources/{RESOURCE_ID}/files", FileList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/resources/{RESOURCE_ID}/files", FileList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/resources/{RESOURCE_ID}/files", FileList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/resources/{RESOURCE_ID}/files", FileList.class);
        attachArchiveURI(router, "/experiments/{EXPT_ID}/resources/{RESOURCE_ID}/files", FileList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/resources/{RESOURCE_ID}/files", FileList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/resources/{RESOURCE_ID}/files", FileList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/resources/{RESOURCE_ID}/files", FileList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/resources/{RESOURCE_ID}/files", FileList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/resources/{RESOURCE_ID}/files", FileList.class);

        attachArchiveURI(router, "/subjects/{SUBJECT_ID}/resources/{RESOURCE_ID}/files", FileList.class);

        //file short-cut
        attachArchiveURI(router, "/projects/{PROJECT_ID}/files", FileList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/files", FileList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/files", FileList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/files", FileList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/files", FileList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/files", FileList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/files", FileList.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/files", FileList.class);
        attachArchiveURI(router, "/experiments/{EXPT_ID}/files", FileList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/{TYPE}/files", FileList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/assessors/{EXPT_ID}/files", FileList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/{TYPE}/files", FileList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/reconstructions/{RECON_ID}/files", FileList.class);
        attachArchiveURI(router, "/experiments/{ASSESSED_ID}/scans/{SCAN_ID}/files", FileList.class);

        attachArchiveURI(router, "/subjects/{SUBJECT_ID}/files", FileList.class);

        attachURI(router, "/users", UserListResource.class);
        attachURI(router, "/users/favorites/{DATA_TYPE}", UserFavoritesList.class);
        attachURI(router, "/users/favorites/{DATA_TYPE}/{PROJECT_ID}", UserFavoriteResource.class);

        attachURI(router, "/search", SearchResource.class);
        attachURI(router, "/search/elements", SearchElementListResource.class);
        attachURI(router, "/search/elements/{ELEMENT_NAME}", SearchFieldListResource.class);
        attachURI(router, "/search/elements/{ELEMENT_NAME}/versions", SearchFieldsVersionListResource.class);
        attachURI(router, "/search/saved", SavedSearchListResource.class);
        attachURI(router, "/search/saved/{SEARCH_ID}", SavedSearchResource.class);
        attachURI(router, "/search/{CACHED_SEARCH_ID}", CachedSearchResource.class);
        attachURI(router, "/search/{CACHED_SEARCH_ID}/{COLUMN}", CachedSearchColumnResource.class);

        attachURI(router, "/pars", PARList.class);
        attachURI(router, "/pars/{PAR_ID}", PARResource.class);
        attachURI(router, "/projects/{PROJECT_ID}/pars", ProjectPARListResource.class);

        attachURI(router, "/JSESSION", UserSession.class);
        attachURI(router, "/auth", UserAuth.class);

        attachURI(router, "/prearchive", PrearcSessionListResource.class);
        attachURI(router, "/prearchive/experiments", RecentPrearchiveSessions.class);
        attachURI(router, PREARC_PROJECT_URI, PrearcSessionListResource.class);
        attachURI(router, PREARC_SESSION_URI, PrearcSessionResource.class);
        attachURI(router, PREARC_SESSION_URI + "/resources", PrearcSessionResourcesList.class);
        attachURI(router, PREARC_SESSION_URI + "/scans", PrearcScansListResource.class);
        attachURI(router, PREARC_SESSION_URI + "/scans/{SCAN_ID}", PrearcScanResource.class);
        attachURI(router, PREARC_SESSION_URI + "/scans/{SCAN_ID}/resources", PrearcScanResourceList.class);
        attachURI(router, PREARC_SESSION_URI + "/scans/{SCAN_ID}/resources/{RESOURCE_ID}/files", PrearcSessionResourceFiles.class);

        attachArchiveURI(router, "/experiments/{EXPT_ID}/DIR", DIRResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/experiments/{EXPT_ID}/DIR", DIRResource.class);
        attachArchiveURI(router, "/experiments/{EXPT_ID}/XAR", DIRResource.class);
        attachArchiveURI(router, "/projects/{PROJECT_ID}/experiments/{EXPT_ID}/XAR", DIRResource.class);
        attachArchiveURI(router, "/user/{USER_ID}/sessions", UserSessionId.class);//GET returns number of user sessions

        // Users Cache Space
        attachURI(router, "/user/cache/resources", UserCacheResource.class);
        attachURI(router, "/user/cache/resources/{XNAME}", UserCacheResource.class);
        attachURI(router, "/user/cache/resources/{XNAME}/files", UserCacheResource.class);
        attachURI(router, "/user/cache/resources/{XNAME}/files/{FILE}", UserCacheResource.class);

        // Configuration Service
        attachURI(router, "/config", ConfigResource.class);
        attachURI(router, "/config/{TOOL_NAME}", ConfigResource.class);
        attachURI(router, "/config/{TOOL_NAME}/{PATH_TO_FILE}", ConfigResource.class, Template.MODE_STARTS_WITH);
        attachURI(router, "/projects/{PROJECT_ID}/config", ConfigResource.class);
        attachURI(router, "/projects/{PROJECT_ID}/config/{TOOL_NAME}", ConfigResource.class);
        attachURI(router, "/projects/{PROJECT_ID}/config/{TOOL_NAME}/{PATH_TO_FILE}", ConfigResource.class, Template.MODE_STARTS_WITH);

        // System services
        attachURI(router, "/services/import", Importer.class);
        attachURI(router, "/services/archive", Archiver.class);
        attachURI(router, "/services/validate-archive", ArchiveValidator.class);
        attachURI(router, "/services/prearchive/move", PrearchiveBatchMove.class);
        attachURI(router, "/services/prearchive/delete", PrearchiveBatchDelete.class);
        attachURI(router, "/services/prearchive/rebuild", PrearchiveBatchRebuild.class);
        attachURI(router, "/services/move-files", MoveFiles.class);
        attachURI(router, "/services/dicomdump", DicomDump.class);
        // attachURI(router, "/services/logging/{" + Analytics.EVENT_KEY + "}", RemoteLoggingRestlet.class);
        attachURI(router, "/services/mail/send", MailRestlet.class);
        attachURI(router, "/services/tokens/{OPERATION}", AliasTokenRestlet.class);
        attachURI(router, "/services/tokens/{OPERATION}/user/{USERNAME}", AliasTokenRestlet.class);
        attachURI(router, "/services/tokens/{OPERATION}/{TOKEN}", AliasTokenRestlet.class);
        attachURI(router, "/services/tokens/{OPERATION}/{TOKEN}/{SECRET}", AliasTokenRestlet.class);
        attachURI(router, "/services/audit", AuditRestlet.class);
        attachURI(router, "/services/refresh/catalog", RefreshCatalog.class);
        attachURI(router, "/services/features", FeatureDefinitionRestlet.class);

        attachURIs(router, ScriptRunnerResource.class, "/automation/runners", "/automation/runners/{LANGUAGE}", "/automation/runners/{LANGUAGE}/{VERSION}");
        attachURIs(router, ScriptResource.class, "/automation/scripts", "/automation/scripts/{SCRIPT_ID}", "/automation/scripts/{SCRIPT_ID}/{VERSION}");
        attachURIs(router, ScriptVersionsResource.class, "/automation/scriptVersions", "/automation/scriptVersions/{SCRIPT_ID}");
        attachURIs(router, WorkflowEventResource.class, "/automation/workflows", "/automation/workflows/{SPEC}");
        attachURIs(router, ScriptTriggerResource.class, "/automation/handlers",
                "/automation/handlers/{EVENT_ID}",
                "/automation/triggers",
                "/automation/triggers/{TRIGGER_ID}",
                "/projects/{PROJECT_ID}/automation/handlers",
                "/projects/{PROJECT_ID}/automation/handlers/{EVENT_ID}");
        attachURIs(router, ScriptTriggerTemplateResource.class, "/automation/templates",
                "/automation/templates/{TEMPLATE_ID}",
                "/projects/{PROJECT_ID}/automation/templates",
                "/projects/{PROJECT_ID}/automation/templates/{TEMPLATE_ID}");

        attachURI(router, "/status/{TRANSACTION_ID}", SQListenerRepresentation.class);

        attachURI(router, "/workflows", WorkflowResource.class);
        attachURI(router, "/workflows/{WORKFLOW_ID}", WorkflowResource.class);
    }

    /**
     * This method walks the packages defined in * {@link XnatRestletExtensions} beans 
     * (see org.nrg.xnat.configuration.RootConfig) and attempts to find extensions for the
     * set of available REST services.
     * @param router The URL router for the restlet servlet.
     * @return A list of classes that should be attached unprotected, i.e. publicly accessible.
     */
    @SuppressWarnings({"unchecked", "Duplicates"})
    private List<Class<? extends Resource>> addExtensionRoutes(Router router) {
        final Set<String>               packages   = new HashSet<>();
        final XnatRestletExtensionsBean extensions = XDAT.getContextService().getBean(XnatRestletExtensionsBean.class);
        if (extensions.size() > 0) {
            packages.addAll(extensions.getPackages());
        } else {
            final XnatRestletExtensions extension = XDAT.getContextService().getBean("defaultXnatRestletExtensions", XnatRestletExtensions.class);
            if (extension != null) {
                packages.addAll(extension.getPackages());
            }
        }
        if (packages.size() == 0) {
            packages.add("org.nrg.xnat.restlet.extensions");
        }

        final List<Class<? extends Resource>> classes = new ArrayList<>();
        final List<Class<? extends Resource>> publicClasses = new ArrayList<>();

        for (final String pkg : packages) {
            try {
                final List<Class<?>> classesForPackage = Reflection.getClassesForPackage(pkg);
                if (_log.isDebugEnabled()) {
                    _log.debug("Found " + classesForPackage.size() + " classes for package: " + pkg);
                }
                for (final Class<?> clazz : classesForPackage) {
                    if (Resource.class.isAssignableFrom(clazz)) {
                        if (_log.isDebugEnabled()) {
                            _log.debug("Found resource class: " + clazz.getName());
                        }
                        classes.add((Class<? extends Resource>) clazz);
                    }
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }

        for (Class<? extends Resource> clazz : classes) {
            if (clazz.isAnnotationPresent(XnatRestlet.class)) {
                XnatRestlet annotation = clazz.getAnnotation(XnatRestlet.class);
                final String[] urls = annotation.value();
                if (urls.length == 0) {
                    throw new RuntimeException("The restlet extension class " + clazz.getName() + " has no URLs configured.");
                }
                if (annotation.secure()) {
                    if (_log.isDebugEnabled()) {
                        _log.debug("Found XnatRestlet-annotated secure function class " + clazz.getName() + " for URLs: " + Joiner.on(", ").join(urls));
                    }
                    attachPath(router, clazz, annotation);
                } else {
                    if (_log.isDebugEnabled()) {
                        _log.debug("Found XnatRestlet-annotated public function class " + clazz.getName() + " for URLs: " + Joiner.on(", ").join(urls));
                    }
                    publicClasses.add(clazz);
                }
            }else if (clazz.isAnnotationPresent(XnatRestlet2.class)) {
                XnatRestlet2 annotation = clazz.getAnnotation(XnatRestlet2.class);
                final XnatRestletURI[] urls = annotation.value();
                if (urls.length == 0) {
                    throw new RuntimeException("The restlet extension class " + clazz.getName() + " has no URLs configured.");
                }
                if (annotation.secure()) {
                    if (_log.isDebugEnabled()) {
                        _log.debug("Found XnatRestlet-annotated secure function class " + clazz.getName() + " for URLs: " + Joiner.on(", ").join(urls));
                    }
                    attachPath(router, clazz, annotation);
                } else {
                    if (_log.isDebugEnabled()) {
                        _log.debug("Found XnatRestlet-annotated public function class " + clazz.getName() + " for URLs: " + Joiner.on(", ").join(urls));
                    }
                    publicClasses.add(clazz);
                }
            }
        }

        return publicClasses;
    }

    private void attachPath(Router router, Class<? extends Resource> clazz) {
        attachPath(router, clazz, clazz.getAnnotation(XnatRestlet.class));
    }

    private void attachPath(Router router, Class<? extends Resource> clazz, XnatRestlet annotation) {
        String[] paths = annotation.value();
        boolean required = annotation.required();
        if (paths.length == 0) {
            String message = "You must specify a value for the XnatRestlet annotation to indicate the hosting path for the restlet extension in class: " + clazz.getName();
            if (required) {
                throw new NrgServiceRuntimeException(message);
            } else {
                _log.error(message);
            }
        } else {
            for (String path : paths) {
                attachURI(router, path, clazz);
            }
        }
    }

    private void attachPath(Router router, Class<? extends Resource> clazz, XnatRestlet2 annotation) {
        XnatRestletURI[] paths = annotation.value();
        boolean required = annotation.required();
        if (paths.length == 0) {
            String message = "You must specify a value for the XnatRestlet annotation to indicate the hosting path for the restlet extension in class: " + clazz.getName();
            if (required) {
                throw new NrgServiceRuntimeException(message);
            } else {
                _log.error(message);
            }
        } else {
            for (XnatRestletURI path : paths) {
                attachURI(router, path.value(), clazz,path.matchingMode());
            }
        }
    }

    private void addPublicRoutes(final Router router, List<Class<? extends Resource>> publicRoutes) {
        if (publicRoutes == null) {
            return;
        }

        for (Class<? extends Resource> route : publicRoutes) {
            attachPath(router, route);
        }
    }

    /**
     * Takes URLs from the mock REST system configuration and maps them into the REST service router.
     * Results for the calls are handled by the {@link org.nrg.xnat.restlet.resources.RestMockCallMapRestlet}
     * implementation.
     *
     * @param router The REST service router.
     */
    private void addConfiguredRoutes(final Router router) {
        Map<String, String> callMap = RestMockCallMapRestlet.getRestMockCallMap();
        if (callMap != null) {
            for (String mapping : callMap.keySet()) {
                if (_log.isDebugEnabled()) {
                    _log.debug("Adding route for mock REST call: " + mapping);
                }
                attachURI(router, mapping, RestMockCallMapRestlet.class);
            }
        } else if (_log.isDebugEnabled()) {
            _log.debug("No mock REST call configuration found.");
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(XNATApplication.class);
    private StringBuilder _routeBuffer;
}
