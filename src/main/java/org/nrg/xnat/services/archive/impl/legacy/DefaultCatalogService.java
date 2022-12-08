/*
 * web: org.nrg.xnat.services.archive.impl.legacy.DefaultCatalogService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.archive.impl.legacy;

import static org.nrg.xft.event.EventUtils.*;
import static org.nrg.xft.event.EventUtils.TYPE.WEB_FORM;
import static org.nrg.xnat.helpers.resource.XnatResourceInfoMap.getFilesAsXnatResourceInfoMap;
import static org.nrg.xnat.restlet.util.XNATRestConstants.getPrearchiveTimestamp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.impl.ValidationHandler;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.model.CatCatalogI;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.cache.UserDataCache;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ValidationException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.ValidationUtils.XFTValidator;
import org.nrg.xft.utils.XMLValidator;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.archive.ResourceData;
import org.nrg.xnat.exceptions.UnsupportedRemoteFilesOperationException;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.XnatResourceInfoMap;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.*;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.services.archive.RemoteFilesService;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.turbine.utils.XNATUtils;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xnat.utils.ThreadAndProcessFileLock;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.data.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@inheritDoc}
 */

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
@Service
@Slf4j
public class DefaultCatalogService implements CatalogService {
    @Autowired
    public DefaultCatalogService(final SiteConfigPreferences preferences, final NamedParameterJdbcTemplate parameterized, final CacheManager cacheManager, final UserDataCache userDataCache) {
        _preferences = preferences;
        _parameterized = parameterized;
        _cache = cacheManager.getCache(CATALOG_SERVICE_CACHE);
        _userDataCache = userDataCache;
    }

    @Autowired(required = false)
    public void setRemoteFilesService(final RemoteFilesService remoteFilesService) {
        _remoteFilesService = remoteFilesService;
    }

    @Override
    public Map<String, String> buildCatalogForResources(final UserI user, final Map<String, List<String>> resourceMap, final boolean withSize) throws InsufficientPrivilegesException {
        final CatCatalogBean catalog = new CatCatalogBean();
        final UserI          resolvedUser;
        try {
            resolvedUser = ObjectUtils.defaultIfNull(user, Users.getGuest());
        } catch (UserNotFoundException | UserInitException e) {
            throw new InsufficientPrivilegesException(user == null ? "No user found" : user.getUsername());
        }

        catalog.setId(String.format(CATALOG_FORMAT, resolvedUser.getLogin(), getPrearchiveTimestamp()));

        final DownloadArchiveOptions options = DownloadArchiveOptions.getOptions(resourceMap.get("options"));
        catalog.setDescription(options.getDescription());

        final List<String> sessions        = resourceMap.get("sessions");
        final List<String> scanTypes       = resourceMap.get("scan_types");
        final List<String> scanFormats     = resourceMap.get("scan_formats");
        final List<String> resources       = resourceMap.get("resources");
        final List<String> reconstructions = resourceMap.get("reconstructions");
        final List<String> assessors       = resourceMap.get("assessors");

        //Unescape scan types and formats so that special characters will not lead to 403s (for example if there is a degree sign in a scan type)
        final List<String> unescapedScanTypes   = unescapeList(scanTypes);
        final List<String> unescapedScanFormats = unescapeList(scanFormats);

        final AtomicLong totalSize              = new AtomicLong();
        final AtomicLong resourcesOfUnknownSize = new AtomicLong();

        final Map<String, Map<String, Map<String, String>>> projects = parseAndVerifySessions(resolvedUser, sessions, unescapedScanTypes, unescapedScanFormats);

        for (final String project : projects.keySet()) {
            final Map<String, Map<String, String>> subjects = projects.get(project);
            for (final String subject : subjects.keySet()) {
                final Map<String, String> sessionMap = subjects.get(subject);
                final Set<String>         sessionIds = sessionMap.keySet();
                for (final String sessionId : sessionIds) {
                    final String label = sessionMap.get(sessionId);

                    final CatCatalogBean sessionCatalog = new CatCatalogBean();
                    sessionCatalog.setId(sessionId);
                    sessionCatalog.setDescription("Project: " + project + ", subject: " + subject + ", label: " + label);

                    CatCatalogI               sessionsByScanTypesAndFormats    = null;
                    final Map<String, Object> sessionsByScanTypesAndFormatsMap = getSessionScans(project, subject, label, sessionId, unescapedScanTypes, unescapedScanFormats, options);
                    if (sessionsByScanTypesAndFormatsMap != null) {
                        Object catalogObj = sessionsByScanTypesAndFormatsMap.get("catalog");
                        if (catalogObj != null && CatCatalogI.class.isAssignableFrom(catalogObj.getClass())) {
                            sessionsByScanTypesAndFormats = (CatCatalogI) catalogObj;
                        }
                        if (withSize) {
                            try {
                                Object sizeObj = sessionsByScanTypesAndFormatsMap.get("size");
                                long   objSize = Long.parseLong(sizeObj.toString());
                                totalSize.getAndAdd(objSize);
                            } catch (Exception ignored) {
                            }
                            try {
                                Object unknownObj   = sessionsByScanTypesAndFormatsMap.get("resourcesOfUnknownSize");
                                long   unknownCount = Long.parseLong(unknownObj.toString());
                                resourcesOfUnknownSize.getAndAdd(unknownCount);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    if (sessionsByScanTypesAndFormats != null) {
                        addSafeEntrySet(sessionCatalog, sessionsByScanTypesAndFormats);
                    }

                    CatCatalogI               resourcesCatalog = null;
                    final Map<String, Object> resourcesMap     = getRelatedData(project, subject, label, sessionId, resources, "resources", options);
                    if (resourcesMap != null) {
                        Object catalogObj = resourcesMap.get("catalog");
                        if (catalogObj != null && CatCatalogI.class.isAssignableFrom(catalogObj.getClass())) {
                            resourcesCatalog = (CatCatalogI) catalogObj;
                        }
                        if (withSize) {
                            try {
                                Object sizeObj = resourcesMap.get("size");
                                long   objSize = Long.parseLong(sizeObj.toString());
                                totalSize.getAndAdd(objSize);
                            } catch (Exception ignored) {
                            }
                            try {
                                Object unknownObj   = resourcesMap.get("resourcesOfUnknownSize");
                                long   unknownCount = Long.parseLong(unknownObj.toString());
                                resourcesOfUnknownSize.getAndAdd(unknownCount);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    if (resourcesCatalog != null) {
                        addSafeEntrySet(sessionCatalog, resourcesCatalog);
                    }

                    CatCatalogI               reconstructionsCatalog = null;
                    final Map<String, Object> reconstructionsMap     = getRelatedData(project, subject, subject, sessionId, reconstructions, "reconstructions", options);
                    if (reconstructionsMap != null) {
                        Object catalogObj = reconstructionsMap.get("catalog");
                        if (catalogObj != null && CatCatalogI.class.isAssignableFrom(catalogObj.getClass())) {
                            reconstructionsCatalog = (CatCatalogI) catalogObj;
                        }
                        if (withSize) {
                            try {
                                Object sizeObj = reconstructionsMap.get("size");
                                long   objSize = Long.parseLong(sizeObj.toString());
                                totalSize.getAndAdd(objSize);
                            } catch (Exception ignored) {
                            }
                            try {
                                Object unknownObj   = reconstructionsMap.get("resourcesOfUnknownSize");
                                long   unknownCount = Long.parseLong(unknownObj.toString());
                                resourcesOfUnknownSize.getAndAdd(unknownCount);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    if (reconstructionsCatalog != null) {
                        addSafeEntrySet(sessionCatalog, reconstructionsCatalog);
                    }

                    CatCatalogI               assessorsCatalog = null;
                    final Map<String, Object> assessorsMap     = getSessionAssessors(project, subject, sessionId, assessors, options, resolvedUser);
                    if (assessorsMap != null) {
                        Object catalogObj = assessorsMap.get("catalog");
                        if (catalogObj != null && CatCatalogI.class.isAssignableFrom(catalogObj.getClass())) {
                            assessorsCatalog = (CatCatalogI) catalogObj;
                        }
                        if (withSize) {
                            try {
                                Object sizeObj = assessorsMap.get("size");
                                long   objSize = Long.parseLong(sizeObj.toString());
                                totalSize.getAndAdd(objSize);
                            } catch (Exception ignored) {
                            }
                            try {
                                Object unknownObj   = assessorsMap.get("resourcesOfUnknownSize");
                                long   unknownCount = Long.parseLong(unknownObj.toString());
                                resourcesOfUnknownSize.getAndAdd(unknownCount);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    if (assessorsCatalog != null) {
                        addSafeEntrySet(sessionCatalog, assessorsCatalog);
                    }

                    catalog.addSets_entryset(sessionCatalog);
                }
            }
        }

        storeToCache(resolvedUser, catalog);

        final Map<String, String> idAndSize = new HashMap<>();
        idAndSize.put("id", catalog.getId());
        idAndSize.put("size", totalSize.toString());
        idAndSize.put("resourcesOfUnknownSize", resourcesOfUnknownSize.toString());
        return idAndSize;
    }

    @Override
    public CatCatalogI getCachedCatalog(final UserI user, final String catalogId)
            throws InsufficientPrivilegesException {
        final CatCatalogI catalog = getFromCache(user, catalogId);
        if (catalog == null) {
            throw new InsufficientPrivilegesException(user.getUsername());
        }
        return catalog;
    }

    @Override
    public long getCatalogSize(final UserI user, final String catalogId)
            throws InsufficientPrivilegesException, IOException {
        final CatCatalogI catalog = getFromCache(user, catalogId);
        if (catalog == null) {
            throw new InsufficientPrivilegesException(user.getUsername());
        }
        final StringWriter writer = new StringWriter();
        if (catalog instanceof CatCatalogBean) {
            ((CatCatalogBean) catalog).toXML(writer, true);
        } else {
            try {
                catalog.toXML(writer);
            } catch (Exception e) {
                throw new IOException("An error occurred trying to access the catalog " + catalogId + " for the user " + user.getLogin());
            }
        }
        return writer.toString().length();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResources(final UserI user, final String parentUri, final File resource,
                                               final String label, final String description, final String format,
                                               final String content, final String... tags) throws Exception {
        return _insertResources(user, null, parentUri, getFilesAsXnatResourceInfoMap(Collections.singletonList(resource)), null, false, false, label, description, format, content, tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResources(final UserI user, final String parentUri, final Collection<File> resources,
                                               final String label, final String description, final String format,
                                               final String content, final String... tags) throws Exception {
        return _insertResources(user, null, parentUri, getFilesAsXnatResourceInfoMap(resources), null, false, false, label, description, format, content, tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResources(final UserI user, final String parentUri, final Collection<File> resources,
                                               @Nullable final Integer parentEventId, final boolean preserveDirectories,
                                               final String label, final String description, final String format,
                                               final String content, final String... tags) throws Exception {
        return _insertResources(user, null, parentUri, getFilesAsXnatResourceInfoMap(resources), parentEventId, preserveDirectories, false,
                                label, description, format, content, tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResources(final UserI user, final String parentUri, final Collection<File> resources, @Nullable final Integer parentEventId, final boolean preserveDirectories, final boolean uploadToRemote, final String label, final String description, final String format, final String content, final String... tags) throws Exception {
        return _insertResources(user, null, parentUri, getFilesAsXnatResourceInfoMap(resources), parentEventId, preserveDirectories, uploadToRemote, label, description, format, content, tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResources(final UserI user, final XnatResourcecatalog catalog, final File resource) throws Exception {
        return _insertResources(user, catalog, null, getFilesAsXnatResourceInfoMap(Collections.singletonList(resource)), null, false, false, null, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResources(final UserI user, final XnatResourcecatalog catalog, final Collection<File> resources) throws Exception {
        return _insertResources(user, catalog, null, getFilesAsXnatResourceInfoMap(resources), null, false, false, null, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResources(final UserI user, final XnatResourcecatalog catalog, final Collection<File> resources, final boolean preserveDirectories) throws Exception {
        return _insertResources(user, catalog, null, getFilesAsXnatResourceInfoMap(resources), null, preserveDirectories, false, null, null, null, null);
    }

    @Override
    public XnatResourcecatalog insertResources(final UserI user, final String parentUri, final XnatResourceInfo descriptor, final String label, final String description, final String format, final String content, final String... tags) throws Exception {
        return _insertResources(user, null, parentUri, new XnatResourceInfoMap(descriptor), null, false, false, null, null, null, null);
    }

    @Override
    public XnatResourcecatalog insertResources(final UserI user, final String parentUri, final XnatResourceInfoMap resourceMap, @Nullable final Integer parentEventId, final boolean preserveDirectories, final String label, final String description, final String format, final String content, final String... tags) throws Exception {
        return _insertResources(user, null, parentUri, resourceMap, parentEventId, preserveDirectories, false, label, description, format, content, tags);
    }

    @Override
    public XnatResourcecatalog insertResources(final UserI user, final String parentUri, final XnatResourceInfoMap resourceMap, @Nullable final Integer parentEventId, final boolean preserveDirectories, final boolean uploadToRemote, final String label, final String description, final String format, final String content, final String... tags) throws Exception {
        return _insertResources(user, null, parentUri, resourceMap, parentEventId, preserveDirectories, uploadToRemote, label, description, format, content, tags);
    }

    @Override
    public XnatResourcecatalog insertResources(final UserI user, final String parentUri, final XnatResourceInfoMap resourceMap, final String label, final String description, final String format, final String content, final String... tags) throws Exception {
        return _insertResources(user, null, parentUri, resourceMap, null, false, false, label, description, format, content, tags);
    }

    @Override
    public XnatResourcecatalog insertResources(final UserI user, final XnatResourcecatalog catalog, final XnatResourceInfoMap resourceMap) throws Exception {
        return _insertResources(user, catalog, null, resourceMap, null, false, false, null, null, null, null);
    }

    @Override
    public XnatResourcecatalog insertResources(final UserI user, final XnatResourcecatalog catalog, final XnatResourceInfoMap resourceMap, final boolean preserveDirectories) throws Exception {
        return _insertResources(user, catalog, null, resourceMap, null, preserveDirectories, false, null, null, null, null);
    }

    @Override
    public XnatResourcecatalog insertResources(final UserI user, final XnatResourcecatalog catalog, final XnatResourceInfo descriptor) throws Exception {
        return _insertResources(user, catalog, null, new XnatResourceInfoMap(descriptor), null, false, false, null, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResourceStreams(final UserI user, final XnatResourcecatalog catalog, final String name, final InputStreamSource source) throws Exception {
        return _insertResources(user, catalog, null, new XnatResourceInfoMap(name, source), null, false, false, null, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResourceStreams(final UserI user, final XnatResourcecatalog catalog, final Map<String, ? extends InputStreamSource> sources) throws Exception {
        return _insertResources(user, catalog, null, new XnatResourceInfoMap(sources), null, false, false, null, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResourceStreams(final UserI user, final XnatResourcecatalog catalog, final Map<String, ? extends InputStreamSource> sources, final boolean preserveDirectories) throws Exception {
        return _insertResources(user, catalog, null, new XnatResourceInfoMap(sources), null, preserveDirectories, false, null, null, null, null);
    }

    @Override
    public XnatResourcecatalog insertResourceStreams(final UserI user, final XnatResourcecatalog catalog, final XnatResourceInfo descriptor) throws Exception {
        return _insertResources(user, catalog, null, new XnatResourceInfoMap(descriptor), null, false, false, null, null, null, null);
    }

    @Override
    public XnatResourcecatalog insertResourceStreams(final UserI user, final XnatResourcecatalog catalog, final XnatResourceInfoMap resourceMap) throws Exception {
        return _insertResources(user, catalog, null, resourceMap, null, false, false, null, null, null, null);
    }

    @Override
    public XnatResourcecatalog insertResourceStreams(final UserI user, final XnatResourcecatalog catalog, final XnatResourceInfoMap resourceMap, final boolean preserveDirectories) throws Exception {
        return _insertResources(user, catalog, null, resourceMap, null, preserveDirectories, false, null, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("Duplicates")
    public XnatResourcecatalog createResourceCatalog(final UserI user, final String label, final String description, final String format, final String content, final String... tags) throws Exception {
        final XFTItem             item    = XFTItem.NewItem("xnat:resourceCatalog", user);
        final XnatResourcecatalog catalog = (XnatResourcecatalog) BaseElement.GetGeneratedItem(item);
        catalog.setLabel(label);

        if (StringUtils.isNotBlank(description)) {
            catalog.setDescription(description);
        }
        if (StringUtils.isNotBlank(format)) {
            catalog.setFormat(format);
        }
        if (StringUtils.isNotBlank(content)) {
            catalog.setContent(content);
        }
        for (final String tag : tags) {
            if (StringUtils.isNotBlank(tag)) {
                for (final String subtag : tag.split("\\s*,\\s*")) {
                    final XnatAbstractresourceTag resourceTag = new XnatAbstractresourceTag(user);
                    if (subtag.contains("=")) {
                        final String[] atoms = subtag.split("=", 2);
                        resourceTag.setName(atoms[0]);
                        resourceTag.setTag(atoms[1]);
                    } else if (subtag.contains(":")) {
                        final String[] atoms = subtag.split(":", 2);
                        resourceTag.setName(atoms[0]);
                        resourceTag.setTag(atoms[1]);
                    } else {
                        resourceTag.setTag(subtag);
                    }
                    catalog.setTags_tag(resourceTag);
                }
            }
        }
        return catalog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog createAndInsertResourceCatalog(final UserI user, final String parentUri,
                                                              @Nullable Integer parentEventId, final String label,
                                                              final String description, final String format,
                                                              final String content, final String... tags) throws Exception {

        ResourceData resourceData = getResourceDataFromUri(parentUri);
        File         parentDir    = resourceData.getItem().getExpectedCurrentDirectory();
        Files.createDirectories(parentDir.toPath());
        File lockFile = new File(parentDir.toString(), ".resourcecheck" + label);
        try {
            final ThreadAndProcessFileLock fl = ThreadAndProcessFileLock.getThreadAndProcessFileLock(lockFile,
                                                                                                     false);
            fl.tryLock(2L, TimeUnit.MINUTES);
            try {
                // Test if catalog already exists
                XnatResourcecatalog catalog = null;

                for (XnatAbstractresourceI res : resourceData.getXnatUri().getResources(false)) {
                    if (!(res instanceof XnatResourcecatalog)) {
                        continue;
                    }
                    if (res.getLabel().equals(label)) {
                        catalog = (XnatResourcecatalog) res;
                        break;
                    }
                }
                // If it doesn't exist, create it
                if (catalog == null) {
                    catalog = createResourceCatalog(user, label, description, format, content, tags);
                    insertResourceCatalog(user, parentUri, catalog, parentEventId);
                }
                return catalog;
            } finally {
                fl.unlock();
            }
        } finally {
            ThreadAndProcessFileLock.removeThreadAndProcessFileLock(lockFile);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResourceCatalog(final UserI user, final String parentUri,
                                                     final XnatResourcecatalog catalog)
            throws Exception {
        return insertResourceCatalog(user, parentUri, catalog, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResourceCatalog(final UserI user, final String parentUri,
                                                     final XnatResourcecatalog catalog, @Nullable Integer parentEventId)
            throws Exception {
        return insertResourceCatalog(user, parentUri, catalog, parentEventId, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResourceCatalog(final UserI user, final String parentUri, final XnatResourcecatalog catalog,
                                                     @Nullable Integer parentEventId, final Map<String, String> parameters)
            throws Exception {

        ResourceData                    resourceData = getResourceDataFromUri(parentUri);
        final URIManager.ArchiveItemURI resourceURI  = resourceData.getXnatUri();

        try {
            if (!Permissions.canEdit(user, resourceData.getItem())) {
                throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, "The user " + user.getLogin() +
                                                                         " does not have permission to edit the resource " + parentUri + ".");
            }
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException(Status.SERVER_ERROR_INTERNAL, "An error occurred try to check the user " +
                                                                    user.getLogin() + " permissions for resource " + parentUri + ".");
        }

        final Class<? extends URIManager.ArchiveItemURI> parentClass = resourceURI.getClass();
        final BaseElement                                parent;
        try {
            if (ReconURII.class.isAssignableFrom(parentClass)) {
                parent = ((ReconURII) resourceURI).getRecon();
            } else if (ScanURII.class.isAssignableFrom(parentClass)) {
                parent = ((ScanURII) resourceURI).getScan();
            } else if (AssessorURII.class.isAssignableFrom(parentClass)) {
                parent = ((AssessorURII) resourceURI).getAssessor();
            } else if (ExperimentURII.class.isAssignableFrom(parentClass)) {
                parent = ((ExperimentURII) resourceURI).getExperiment();
            } else if (SubjectURII.class.isAssignableFrom(parentClass)) {
                parent = ((SubjectURII) resourceURI).getSubject();
            } else if (ProjectURII.class.isAssignableFrom(parentClass)) {
                parent = ((ProjectURII) resourceURI).getProject();
            } else {
                log.error("The URI is of an unknown type: " + resourceURI.getClass().getName());
                return null;
            }
        } catch (Exception e) {
            log.error("An error occurred creating the catalog with label {} for resource {}, please check the server logs.", catalog.getLabel(), parentUri);
            return null;
        }

        return insertResourceCatalog(user, parent, catalog, parentEventId, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResourceCatalog(final UserI user, final BaseElement item,
                                                     final XnatResourcecatalog catalog)
            throws Exception {
        return insertResourceCatalog(user, item, catalog, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResourceCatalog(final UserI user, final BaseElement item,
                                                     final XnatResourcecatalog catalog, @Nullable Integer parentEventId)
            throws Exception {
        return insertResourceCatalog(user, item, catalog, parentEventId, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResourceCatalog(final UserI user, final BaseElement parent,
                                                     final XnatResourcecatalog catalog, @Nullable Integer parentEventId,
                                                     final Map<String, String> parameters) throws Exception {
        final XFTItem item             = parent.getItem();
        final boolean isScan           = item.instanceOf(XnatImagescandata.SCHEMA_ELEMENT_NAME);
        final boolean isReconstruction = item.instanceOf(XnatReconstructedimagedata.SCHEMA_ELEMENT_NAME);
        final boolean isExperiment     = item.instanceOf(XnatExperimentdata.SCHEMA_ELEMENT_NAME);
        final boolean isProject        = item.instanceOf(XnatProjectdata.SCHEMA_ELEMENT_NAME);
        final boolean isSubject        = item.instanceOf(XnatSubjectdata.SCHEMA_ELEMENT_NAME);

        final boolean useParentForUploadId = isScan || isReconstruction;

        final String uploadId;
        if (useParentForUploadId) {
            final Object id = item.getProperty("ID");
            uploadId = StringUtils.isNotBlank((String) id) ? (String) id : getPrearchiveTimestamp();
        } else {
            uploadId = StringUtils.isNotBlank(catalog.getLabel()) ? catalog.getLabel() : getPrearchiveTimestamp();
        }

        final EventDetails        event      = new EventDetails(CATEGORY.DATA, TYPE.PROCESS, CREATE_RESOURCE, "Catalog service invoked", "");
        final PersistentWorkflowI workflow   = PersistentWorkflowUtils.getOrCreateWorkflowData(parentEventId, user, item.getItem(), getScanId(catalog), event);
        final EventMetaI          eventMetaI = workflow.buildEvent();

        try {
            if (isExperiment) {
                final XnatExperimentdata experiment = (XnatExperimentdata) parent;
                event.setComment("Created experiment resource " + uploadId + " for " + experiment.getId() + " at " + catalog.getUri());
                insertExperimentResourceCatalog(user, experiment, catalog, uploadId, eventMetaI, parameters == null ? Collections.emptyMap() : parameters);
            } else if (isScan) {
                final XnatImagescandata scan = (XnatImagescandata) parent;
                event.setComment("Created scan resource " + uploadId + " for image session " + scan.getImageSessionId() + " scan " + scan.getId() + " at " + catalog.getUri());
                insertScanResourceCatalog(user, scan, catalog, uploadId, eventMetaI);
            } else if (isProject) {
                final XnatProjectdata project = (XnatProjectdata) parent;
                event.setComment("Created project resource " + uploadId + " for project " + project.getId() + " at " + catalog.getUri());
                insertProjectResourceCatalog(user, project, catalog, uploadId, eventMetaI);
            } else if (isSubject) {
                final XnatSubjectdata subject = (XnatSubjectdata) parent;
                event.setComment("Created subject resource " + uploadId + " for " + subject.getId() + " at " + catalog.getUri());
                insertSubjectResourceCatalog(user, subject, catalog, uploadId, eventMetaI, parameters == null ? Collections.emptyMap() : parameters);
            } else if (isReconstruction) {
                final XnatReconstructedimagedata reconstruction = (XnatReconstructedimagedata) parent;
                event.setComment("Created reconstruction resource " + uploadId + " for image session " + reconstruction.getImageSessionId() + " reconstruction " + reconstruction.getId() + " at " + catalog.getUri());
                insertReconstructionResourceCatalog(user, reconstruction, catalog, uploadId, eventMetaI, parameters == null ? Collections.emptyMap() : parameters);
            }
            return catalog;
        } catch (Exception e) {
            log.error("An error occurred creating the catalog with label {} for resource {}, please check the server logs.", catalog.getLabel(), parent.getItem().getIDValue(), e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshResourceCatalog(final UserI user, final String resource, final Operation... operations) throws ServerException, ClientException {
        _refreshCatalog(user, resource, Arrays.asList(operations));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshResourceCatalog(final UserI user, final String resource, final Collection<Operation> operations) throws ServerException, ClientException {
        _refreshCatalog(user, resource, operations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshResourceCatalogs(final UserI user, final List<String> resources, final Operation... operations) throws ServerException, ClientException {
        for (final String resource : resources) {
            _refreshCatalog(user, resource, Arrays.asList(operations));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshResourceCatalogs(final UserI user, final List<String> resources, final Collection<Operation> operations) throws ServerException, ClientException {
        for (final String resource : resources) {
            _refreshCatalog(user, resource, operations);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceData getResourceDataFromUri(final String uriString) throws ClientException {
        return getResourceDataFromUri(uriString, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog getDicomResourceCatalog(final String sessionId, final String scanId) throws ClientException {
        XnatResourcecatalog catalog = getResourceCatalog(sessionId, scanId, "DICOM");
        return (catalog != null) ? catalog : getResourceCatalog(sessionId, scanId, "secondary");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog getResourceCatalog(final String sessionId, final String scanId, final String label) throws ClientException {
        final ResourceData resourceData = getResourceDataFromUri(EXPERIMENT_ROOT_URI + sessionId + "/scans/" + scanId + "/resources/" + label, true);
        return (resourceData != null) ? resourceData.getCatalogResource() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceData getResourceDataFromUri(final String uriString, final boolean acceptFileUri) throws ClientException {
        //Is it a valid resource?
        final URIManager.DataURIA uri;
        try {
            uri = UriParserUtils.parseURI(uriString);
        } catch (MalformedURLException e) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "Malformed URI: " + uriString);
        }
        if (!(uri instanceof URIManager.ArchiveItemURI)) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid URI: " + uriString);
        }

        final URIManager.ArchiveItemURI xnatUri = (URIManager.ArchiveItemURI) uri;

        //What is its security item?
        final ArchivableItem item = xnatUri.getSecurityItem();
        if (item == null) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "Cannot locate archivable item securing "
                                                                       + uriString);
        }

        // Is it a catalog resource?
        XnatResourcecatalog catRes           = null;
        String              resourceFilePath = null;
        if (xnatUri instanceof ResourceURII) {
            // Do we have the path to a file?
            resourceFilePath = ((ResourceURII) xnatUri).getResourceFilePath();

            if (StringUtils.isNotEmpty(resourceFilePath) && !resourceFilePath.equals("/")) {
                if (!acceptFileUri) {
                    throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "Resource URI: " + uriString +
                                                                               " is a file; you should provide the path to a resource (leave off the *_catalog.xml).");
                }
            } else {
                resourceFilePath = null;
            }

            XnatAbstractresourceI resource = ((ResourceURII) xnatUri).getXnatResource();
            // Allow a null resource; throw exception if we have a resource file path and we have a non-catalog resource
            if (resource != null) {
                if (resource instanceof XnatResourcecatalog) {
                    catRes = (XnatResourcecatalog) resource;
                }
            }

            if (resourceFilePath != null && catRes == null) {
                // This shouldn't happen - cannot reference a file outside a catalog resource, right?
                throw new ClientException("File " + resourceFilePath + " does not appear to be within a catalog " +
                                          "resource.");
            }
        }

        return new ResourceData(uri, xnatUri, item, catRes, resourceFilePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pullResourceCatalogsToDestination(final UserI user, final String uriString,
                                                  final String archiveRelativeDir,
                                                  @Nullable String destinationDir)
            throws ServerException, ClientException {

        if (_remoteFilesService == null) {
            throw new ServerException("No remote filesystems configured for this site; all catalogs must be local");
        }

        final ResourceData resourceData = getResourceDataFromUri(uriString, true);

        final URIManager.ArchiveItemURI resourceURI = resourceData.getXnatUri();
        final ArchivableItem            item        = resourceData.getItem();

        checkPermissionsOnItem(user, item, SecurityManager.READ, uriString);

        XnatResourcecatalog catRes           = resourceData.getCatalogResource();
        String              resourceFilePath = resourceData.getResourceFilePath();
        if (resourceFilePath != null) {
            // We have just one file, but in order to pull it, we need to determine the URL via the catalog (bummer)
            CatalogUtils.CatalogData                  catalogData = CatalogUtils.CatalogData.getOrCreate(item, catRes);
            Map<String, CatalogUtils.CatalogMapEntry> catalogMap  = CatalogUtils.buildCatalogMap(catalogData);
            CatalogUtils.CatalogMapEntry              mapEntry    = catalogMap.get(resourceFilePath);
            try {
                if (mapEntry == null) {
                    throw new FileNotFoundException();
                }
                destinationDir = StringUtils.defaultIfBlank(destinationDir, archiveRelativeDir);
                _remoteFilesService.pullFile(mapEntry.entry.getUri(), destinationDir, item.getProject());
            } catch (FileNotFoundException e) {
                throw new ClientException("Unable to pull file indicated by " + uriString);
            }
        } else {
            final List<XnatAbstractresourceI> resources = catRes != null ? Collections.singletonList(catRes) : resourceURI.getResources(true);
            _remoteFilesService.pullItem(item, resources, archiveRelativeDir, destinationDir);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasRemoteFiles(final UserI user, final String uriString) throws ClientException, ServerException {
        if (_remoteFilesService == null) {
            return false;
        }

        final ResourceData resourceData = getResourceDataFromUri(uriString, true);
        checkPermissionsOnItem(user, resourceData.getItem(), SecurityManager.READ, uriString);
        XnatResourcecatalog catRes = resourceData.getCatalogResource();
        if (catRes != null) {
            // catalog resource, just check it
            return _remoteFilesService.catalogHasRemoteFiles(catRes);
        } else {
            // other kind of resource (mr session, etc), check its children for anything remote
            for (final XnatAbstractresourceI resource : resourceData.getXnatUri().getResources(true)) {
                if (!(resource instanceof XnatResourcecatalog)) {
                    continue;
                }
                if (_remoteFilesService.catalogHasRemoteFiles((XnatResourcecatalog) resource)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public XFTItem insertXmlObject(final UserI user, final InputStream input, final boolean allowDataDeletion,
                                   final Map<String, ?> parameters) throws Exception {
        return insertXmlObject(user, input, allowDataDeletion, parameters, null);
    }

    @Override
    public XFTItem insertXmlObject(final UserI user, final InputStream input, final boolean allowDataDeletion,
                                   final Map<String, ?> parameters, @Nullable Integer parentEventId) throws Exception {
        final File temporary = File.createTempFile("xml-import", ".xml");
        try {
            try (final FileWriter writer = new FileWriter(temporary)) {
                IOUtils.copy(input, writer, Charset.defaultCharset());
            }
            log.debug("Copied XML to temporary file {}", temporary.getPath());
            return insertXmlObject(user, temporary, allowDataDeletion, parameters, parentEventId);
        } finally {
            if (temporary.delete()) {
                log.debug("Successfully deleted temporary file at {}", temporary.getPath());
            } else {
                log.debug("Something failed when trying to delete temporary file at {}", temporary.getPath());
            }
        }

    }

    @Override
    public XFTItem insertXmlObject(final UserI user, final File inputFile, final boolean allowDataDeletion,
                                   final Map<String, ?> parameters, @Nullable Integer parentEventId) throws Exception {
        try (final FileInputStream validatorInput = new FileInputStream(inputFile)) {
            final ValidationHandler handler = new XMLValidator().validateInputStream(validatorInput);
            if (!handler.assertValid()) {
                throw handler.getErrors().get(0);
            }
        }

        try (final FileInputStream parserInput = new FileInputStream(inputFile)) {
            final SAXReader reader = new SAXReader(user);
            final XFTItem   item   = reader.parse(parserInput);

            final boolean isExperiment = item.instanceOf(XnatExperimentdata.SCHEMA_ELEMENT_NAME);
            final boolean isSubject    = item.instanceOf(XnatSubjectdata.SCHEMA_ELEMENT_NAME);
            final boolean isProject    = item.instanceOf(XnatProjectdata.SCHEMA_ELEMENT_NAME);
            final boolean isScan       = item.instanceOf(XnatImagescandata.SCHEMA_ELEMENT_NAME);

            final ValidationResults validation = XFTValidator.Validate(item);

            // An "invalid" object could actually be valid if it is a new experiment or subject and the only
            // validation failure is the lack of ID, so let's check for that scenario.
            final boolean generateId = (isExperiment || isSubject) && !validation.isValid() && validation.getErrors().size() == 1 && validation.hasField("ID");
            if (generateId) {
                validation.removeError("ID");
            }

            final String primaryKey = item.getIDValue();
            final String xsiType    = item.getXSIType();

            final boolean isCreate;
            if (isExperiment) {
                final String persistedXsiType = getXsiTypeForExperimentId(primaryKey);
                isCreate = StringUtils.isBlank(persistedXsiType);
                if (!isCreate && !item.instanceOf(persistedXsiType)) {
                    throw new ClientException(Status.CLIENT_ERROR_CONFLICT, "Trying to insert XML for an object of type '" +
                                                                            xsiType + "' and ID '" + primaryKey + "', but that ID already exists with type '" +
                                                                            persistedXsiType + "', which is not compatible.");
                }
            } else if (isSubject) {
                isCreate = !Permissions.verifySubjectExists(_parameterized, primaryKey);
            } else if (isProject) {
                isCreate = !Permissions.verifyProjectExists(_parameterized, primaryKey);
            } else if (isScan) {
                // We actually operate on a session, so it's always an update
                isCreate = false;
            } else {
                // For items other than projects, subjects, and experiments, e.g. stored searches
                final String table  = item.getGenericSchemaElement().getSQLName();
                final String column = StringUtils.defaultIfBlank(item.getPKString(), "id").split("=")[0];
                isCreate = !checkObjectExists(table, column, primaryKey);
            }

            log.info("Loaded XML item: {}. This looks to be a '{}' operation.", item.getProperName(),
                     isCreate ? "create" : "update");

            if (!validation.isValid()) {
                throw new ValidationException(validation);
            }

            log.info("Validation: PASSED");

            final boolean quarantine = item.getGenericSchemaElement().isQuarantine();

            String eventItemIdValue = primaryKey;
            String eventItemXsiType = xsiType;
            if (isProject && isCreate) {
                // Project creation
                final XnatProjectdata project = new XnatProjectdata(item);
                final EventMetaI eventMeta = PersistentWorkflowUtils.getOrCreateWorkflowData(parentEventId,
                                                                                             user, AutoXnatProjectdata.SCHEMA_ELEMENT_NAME, project.getId(), project.getId(),
                                                                                             newEventInstance(CATEGORY.PROJECT_ADMIN, parameters)).buildEvent();
                XnatProjectdata.createProject(project, user, allowDataDeletion, false, eventMeta, "private");
            } else {
                EventDetails event      = newEventInstance(CATEGORY.SIDE_ADMIN, STORE_XML, parameters);
                EventMetaI   eventMetaI = null;
                if (parentEventId != null || item.getItem().instanceOf(XnatExperimentdata.SCHEMA_ELEMENT_NAME)
                    || item.getItem().instanceOf(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
                    try {
                        final PersistentWorkflowI workflow = PersistentWorkflowUtils.getOrCreateWorkflowData(parentEventId, user, item.getItem(), event);
                        if (workflow != null) {
                            eventMetaI = workflow.buildEvent();
                        }
                    } catch (Exception e) {
                        //Ignore, instead create workflow within save methods
                    }
                }

                if (isScan) {
                    final String parentId = item.getStringProperty(XnatImagescandata.SCHEMA_ELEMENT_NAME + "/image_session_ID");
                    if (StringUtils.isBlank(parentId)) {
                        throw new ClientException("Cannot insert scan: image_session_ID is not populated");
                    }

                    XnatImagesessiondata session = XnatImagesessiondata.getXnatImagesessiondatasById(parentId, user, false);
                    if (session == null) {
                        throw new ClientException("Cannot insert scan: experiment ID " + parentId + " doesn't refer to a valid image session");
                    }

                    final boolean preventDel = !StringUtils.contains(StringUtils.defaultIfBlank(_preferences.getValue("security.prevent-data-deletion-override"), "[]"), session.getItem().getStatus()) &&
                                               BooleanUtils.toBooleanDefaultIfNull(_preferences.getBooleanValue("security.prevent-data-deletion"), false);
                    final String            scanId = item.getStringProperty(XnatImagescandata.SCHEMA_ELEMENT_NAME + "/ID");
                    final XnatImagescandata scan   = session.getScanById(scanId);
                    if (scan != null) {
                        if (allowDataDeletion) {
                            // Authorize
                            if (!Permissions.canDelete(user, session) || preventDel) {
                                throw new ClientException("User account doesn't have permission to modify session " +
                                                          parentId + " to replace scan " + scanId + " and/or deletion disabled for session");
                            }

                            // Delete
                            XNATUtils.removeScanFromSessionAndDeleteFiles(session, scan, user, eventMetaI);
                            // Refresh session object after deleting scan
                            session = (XnatImagesessiondata) XnatExperimentdata.getXnatExperimentdatasById(parentId, user, false);
                        } else {
                            throw new ClientException("Cannot insert scan into session " + parentId + ": ID " +
                                                      scanId + "in use. Rerun with allowDataDeletion or choose a new ID");
                        }
                    }
                    item.setProperty(XnatImagescandata.SCHEMA_ELEMENT_NAME + "/project", session.getProject());
                    session.addScans_scan(new XnatImagescandata(item));
                    if (eventMetaI != null) {
                        SaveItemHelper.unauthorizedSave(session, user, false, quarantine,
                                                        false, allowDataDeletion, eventMetaI);
                    } else {
                        SaveItemHelper.unauthorizedSave(session, user, false, quarantine,
                                                        false, allowDataDeletion, event);
                    }
                    eventItemIdValue = session.getId();
                    eventItemXsiType = session.getXSIType();
                } else {
                    if (generateId) {
                        if (isExperiment) {
                            item.setProperty(XnatExperimentdata.SCHEMA_ELEMENT_NAME + "/ID", XnatExperimentdata.CreateNewID());
                        } else {
                            item.setProperty(XnatSubjectdata.SCHEMA_ELEMENT_NAME + "/ID", XnatSubjectdata.CreateNewID());
                        }
                    }
                    try {
                        if (eventMetaI != null) {
                            SaveItemHelper.unauthorizedSave(item, user, false, quarantine,
                                                            false, allowDataDeletion, eventMetaI);
                        } else {
                            SaveItemHelper.unauthorizedSave(item, user, false, quarantine,
                                                            false, allowDataDeletion, event);
                        }
                    } catch (ClientException e) {
                        log.error("Error occurred while saving submitted item. Status {}: '{}'", e.getStatus(), e.getMessage());
                        throw e;
                    }
                    // ID might have been created by the save
                    eventItemIdValue = StringUtils.defaultIfBlank(eventItemIdValue, item.getIDValue());
                }

                XDAT.triggerXftItemEvent(xsiType, eventItemIdValue, isCreate ? XftItemEvent.CREATE : XftItemEvent.UPDATE);
            }

            log.debug("Item '{}' of type {} successfully stored", eventItemXsiType, eventItemIdValue);

            final SchemaElementI schemaElement = SchemaElement.GetElement(xsiType);
            if (StringUtils.equalsIgnoreCase(schemaElement.getGenericXFTElement().getType().getLocalPrefix(), "xdat")
                || StringUtils.equalsAnyIgnoreCase(schemaElement.getFullXMLName(),
                                                   "xnat:investigatorData", "xnat:projectData")) {
                ElementSecurity.refresh();
            }

            return item;
        }
    }

    @Override
    public void checkPermissionsOnItem(final UserI user, final ArchivableItem item,
                                       @Nonnull final String accessType, final String resourceName)
            throws ServerException, ClientException {
        try {
            if (!Permissions.can(user, item, accessType)) {
                throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, "The user " + user.getLogin() +
                                                                         " does not have permission to " + accessType + " the resource " + resourceName + " for item " +
                                                                         item.getId());
            }
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException(Status.SERVER_ERROR_INTERNAL, "An error occurred try to check the user " +
                                                                    user.getLogin() + " permissions for resource " + resourceName + " for item " + item.getId());
        }
    }

    private XnatResourcecatalog _insertResources(final UserI user, final XnatResourcecatalog existing, final String parentUri, final XnatResourceInfoMap resourceMap, @Nullable Integer parentEventId, final boolean preserveDirectories, final boolean uploadToRemote, final String label, final String description, final String catalogFormat, final String catalogContent, final String... tags) throws Exception {
        final XnatResourcecatalog catalog;
        final String              uri;
        if (existing != null) {
            catalog = existing;
            uri = UriParserUtils.getArchiveUri(catalog.getParent());
        } else {
            catalog = createAndInsertResourceCatalog(user, parentUri, parentEventId, label, description, catalogFormat, catalogContent, tags);
            uri = parentUri;
        }

        boolean doLocalAdd = !uploadToRemote;
        if (uploadToRemote) {
            try {
                if (_remoteFilesService == null) {
                    throw new UnsupportedRemoteFilesOperationException("No remote filesystems configured for this site; " +
                                                                       "all catalogs must be local");
                }
                _remoteFilesService.pushProcessingOutputsAndAddUrlsToCatalog(user, getResourceDataFromUri(parentUri).getItem(), catalog, resourceMap, preserveDirectories, parentEventId);
            } catch (Exception e) {
                // For any exception, default to local add
                if (!(e instanceof UnsupportedRemoteFilesOperationException)) {
                    log.error("Error uploading {} resources to remote and adding to {} by URL", resourceMap.size(), catalog.getUri(), e);
                }
                doLocalAdd = true;
            }
        }

        if (!doLocalAdd) {
            return catalog;
        }

        final File    destination      = new File(catalog.getUri()).getParentFile();
        final boolean extractByDefault = resourceMap.isDefaultExtractCompressedResource();
        for (final Map.Entry<String, XnatResourceInfo> entry : resourceMap.entrySet()) {
            final String            resourceName = entry.getKey();
            final XnatResourceInfo  descriptor   = entry.getValue();
            final boolean           extract      = BooleanUtils.toBooleanDefaultIfNull(descriptor.getExtract(), extractByDefault);
            final InputStreamSource source       = descriptor.getSource();
            if (source instanceof ByteArrayResource) {
                FileUtils.copyInputStreamToFile(new ByteArrayInputStream(((ByteArrayResource) source).getByteArray()), destination.toPath().resolve(resourceName).toFile());
            } else if (source instanceof Resource) {
                final Resource resource = (Resource) source;
                try {
                    final File    file        = resource.getFile();
                    final boolean isDirectory = file.isDirectory();
                    if (isDirectory && preserveDirectories) {
                        FileUtils.copyDirectoryToDirectory(file, destination);
                    } else if (isDirectory) {
                        FileUtils.copyDirectory(file, destination);
                    } else if (ZipUtils.isCompressedFile(file.getName()) && extract) {
                        ZipUtils.extractFile(file, destination.toPath());
                    } else {
                        FileUtils.copyFileToDirectory(file, destination);
                    }
                } catch (IOException e) {
                    log.error("Error copying {} to {}, attempting to copy as input stream", resource.getFilename(),
                            destination, e);
                    FileUtils.copyInputStreamToFile(source.getInputStream(), destination.toPath().resolve(resourceName).toFile());
                }
            } else if (source instanceof MultipartFile) {
                final MultipartFile multipartFile    = (MultipartFile) source;
                final String        originalFilename = multipartFile.getOriginalFilename();
                if (ZipUtils.isCompressedFile(originalFilename) && extract) {
                    final File tempDirectory = Files.createTempDirectory(Long.toString(Calendar.getInstance().getTimeInMillis())).toFile();
                    tempDirectory.deleteOnExit();
                    final File tempZipFile = new File(tempDirectory, originalFilename);
                    tempZipFile.deleteOnExit();
                    multipartFile.transferTo(tempZipFile);
                    ZipUtils.extractFile(tempZipFile, destination.toPath());
                } else {
                    FileUtils.copyInputStreamToFile(source.getInputStream(), destination.toPath().resolve(originalFilename).toFile());
                }
            }
        }

        refreshResourceCatalog(user, uri, catalog, resourceMap, parentEventId);
        return catalog;
    }

    private Boolean checkObjectExists(final String table, final String column, final String primaryKey) {
        try {
            return _parameterized.queryForObject(Permissions.getObjectExistsQuery(table, column, "objectId"), new MapSqlParameterSource("objectId", primaryKey), Boolean.class);
        } catch (BadSqlGrammarException e) {
            log.error("Tried to query table {} column {} for the value '{}', but got a bad grammar exception. " +
                      "Returning false.", table, column, primaryKey, e);
            return false;
        }
    }

    /**
     * Searches for the specified ID in the XNAT experiment table and returns the experiment's XSI type if it exists. If the ID doesn't
     * exist in the experiment table, this method returns null.
     *
     * @param id The ID of the experiment to test.
     *
     * @return The XSI type of the experiment if it exists, null if it doesn't exist.
     */
    private String getXsiTypeForExperimentId(final String id) {
        try {
            return _parameterized.queryForObject(QUERY_FIND_XSI_TYPE_FOR_EXPERIMENT_ID, new MapSqlParameterSource("id", id), String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Returns a list with all non-blank items unescaped from HTML 4 encoding.
     *
     * @param list The list of strings to be unescaped.
     *
     * @return The list of unescaped strings.
     */
    private List<String> unescapeList(final List<String> list) {
        if (list == null || list.isEmpty()) {
            return Collections.singletonList("");
        }
        final List<String> unescaped = new ArrayList<>();
        for (final String item : list) {
            if (StringUtils.isNotBlank(item)) {
                unescaped.add(StringEscapeUtils.unescapeHtml4(item));
            } else if (item == null) {
                unescaped.add(null);
            }
        }
        return unescaped;
    }

    @SuppressWarnings("SameParameterValue")
    private EventDetails newEventInstance(final CATEGORY category, final Map<String, ?> parameters) {
        return newEventInstance(category, (String) parameters.get(EVENT_ACTION), parameters);
    }

    private EventDetails newEventInstance(final CATEGORY category, final String action, final Map<String, ?> parameters) {
        return EventUtils.newEventInstance(category, getType((String) parameters.get(EVENT_TYPE), WEB_FORM), action, (String) parameters.get(EVENT_REASON), (String) parameters.get(EVENT_COMMENT));
    }

    private void refreshResourceCatalog(final UserI user, final String parentURI, XnatResourcecatalog catalog, final XnatResourceInfoMap resourceMap,
                                        @Nullable Integer parentEventId, final Operation... operations) throws ServerException, ClientException {
        _refreshCatalog(user, parentURI, Arrays.asList(operations), catalog, resourceMap, parentEventId);
    }

    /**
     * Performs the actual work of refreshing all catalogs within a resourcePath.
     *
     * @param user         The user requesting the refresh operation.
     * @param resourcePath The archive path for the resource to refresh.
     * @param operations   The operations to be performed.
     *
     * @throws ClientException When an error occurs that is caused somehow by the requested operation.
     * @throws ServerException When an error occurs in the system during the refresh operation.
     */
    private void _refreshCatalog(final UserI user, final String resourcePath, final Collection<Operation> operations) throws ServerException, ClientException {
        _refreshCatalog(user, resourcePath, operations, null, null, null);
    }

    /**
     * Performs the actual work of refreshing a single catalog within resourcePath
     *
     * @param user         The user requesting the refresh operation.
     * @param resourcePath The archive path for the resource to refresh.
     * @param operations   The operations to be performed.
     * @param catalog      If null, refresh all catalogs in resourcePath. Otherwise, just refresh this one
     *
     * @throws ClientException When an error occurs that is caused somehow by the requested operation.
     * @throws ServerException When an error occurs in the system during the refresh operation.
     */
    private void _refreshCatalog(final UserI user, final String resourcePath, final Collection<Operation> operations,
                                 @Nullable XnatAbstractresourceI catalog, @Nullable final XnatResourceInfoMap resourceMap, @Nullable Integer parentEventId)
            throws ServerException, ClientException {

        PersistentWorkflowI workflow = null;
        try {
            ResourceData                    resourceData = getResourceDataFromUri(resourcePath);
            final URIManager.ArchiveItemURI resourceURI  = resourceData.getXnatUri();
            final ArchivableItem            item         = resourceData.getItem();

            checkPermissionsOnItem(user, item, SecurityManager.EDIT, resourcePath);

            List<XnatAbstractresourceI> resources = resourceURI.getResources(true);

            String reason = "Refreshed catalog for resource " + resourceURI.getUri();
            if (catalog != null) {
                boolean found = false;
                for (XnatAbstractresourceI res : resources) {
                    if (res.getXnatAbstractresourceId().equals(catalog.getXnatAbstractresourceId())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new ClientException("Unauthorized: attempted to refresh catalog " + catalog.getLabel() +
                                              " which is not a resource of " + item.getId());
                }
                reason = "Refreshed " + catalog.getLabel() + " catalog for resource " + resourceURI.getUri();
                resources = Collections.singletonList(catalog);
            }

            final EventDetails event = new EventDetails(CATEGORY.DATA, TYPE.PROCESS, "Catalog(s) Refreshed",
                                                        reason, "");
            if (resources.isEmpty()) {
                log.warn("Trying to refresh the resources for catalog '{}', but no resources were returned for the " +
                         "calculated URI: {}", resourcePath, resourceURI.getUri());
            }

            final Collection<Operation> list = getOperations(operations);

            final boolean append        = list.contains(Operation.Append);
            final boolean checksum      = list.contains(Operation.Checksum);
            final boolean delete        = list.contains(Operation.Delete);
            final boolean populateStats = list.contains(Operation.PopulateStats);

            workflow = PersistentWorkflowUtils.getOrCreateWorkflowData(parentEventId, user, item.getItem(), getScanId(catalog), event);

            // Note that resources will contain only catalog if that is specified
            final String project         = item.getProject();
            final String archiveRootPath = item.getArchiveRootPath();
            for (final XnatAbstractresourceI resource : resources) {
                refreshResourceCatalog((XnatAbstractresource) resource, resourceMap, project, archiveRootPath,
                                       populateStats, checksum, delete, append, user, workflow.buildEvent());
            }

            if (parentEventId == null) {
                log.info("Completed event {} workflow with item {} for user {}", parentEventId, item.getId(), user.getUsername());
                WorkflowUtils.complete(workflow, workflow.buildEvent());
            }
        } catch (ClientException e) {
            failWorkflow(parentEventId, workflow);
            throw e;
        } catch (PersistentWorkflowUtils.JustificationAbsent justificationAbsent) {
            failWorkflow(parentEventId, workflow);
            throw new ClientException("No justification was provided for the refresh action, " +
                                      "but is required by the system configuration.");
        } catch (PersistentWorkflowUtils.ActionNameAbsent actionNameAbsent) {
            failWorkflow(parentEventId, workflow);
            throw new ClientException("No action name was provided for the refresh action, " +
                                      "but is required by the system configuration.");
        } catch (PersistentWorkflowUtils.IDAbsent idAbsent) {
            failWorkflow(parentEventId, workflow);
            throw new ClientException("No workflow ID was provided for the refresh action, " +
                                      "but is required by the system configuration.");
        } catch (BaseXnatExperimentdata.UnknownPrimaryProjectException e) {
            failWorkflow(parentEventId, workflow);
            throw new ClientException("Couldn't find the primary project for the specified resource " + resourcePath);
        } catch (Exception e) {
            failWorkflow(parentEventId, workflow);
            throw new ServerException("An error occurred trying to save the workflow for the refresh operation.", e);
        }
    }

    private void failWorkflow(Integer parentEventId, PersistentWorkflowI workflow) {
        if (parentEventId == null && workflow != null) {
            try {
                WorkflowUtils.fail(workflow, workflow.buildEvent());
            } catch (Exception e) {
                log.error("Unable to update workflow", e);
            }
        }
    }

    private void insertProjectResourceCatalog(final UserI user, final XnatProjectdata project, final XnatResourcecatalog resourceCatalog, final String uploadId, final EventMetaI ci) throws Exception {
        final XnatProjectdata working;
        if (project.getUser() == null) {
            working = XnatProjectdata.getProjectByIDorAlias(project.getId(), user, true);
        } else {
            working = project;
        }

        final String resourceFolder = resourceCatalog.getLabel();

        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(uploadId);

        final Path path = Paths.get(working.getRootArchivePath(), "resources");
        final File destination = resourceFolder != null
                                 ? path.resolve(Paths.get(resourceFolder, catalog.getId() + "_catalog.xml")).toFile()
                                 : path.resolve(catalog.getId() + "_catalog.xml").toFile();

        destination.getParentFile().mkdirs();

        try (final FileWriter writer = new FileWriter(destination)) {
            catalog.toXML(writer, true);
        } catch (IOException e) {
            throw new IOException("An error occurred trying to write the catalog to the file " + destination.getAbsolutePath(), e);
        }

        resourceCatalog.setUri(destination.getAbsolutePath());

        try {
            working.setResources_resource(resourceCatalog);
            SaveItemHelper.authorizedSave(working, user, false, false, ci);
        } catch (Exception e) {
            throw new Exception("An error occurred trying to set the in/out status on the project " + project.getId(), e);
        }
    }

    private void insertSubjectResourceCatalog(final UserI user, final XnatSubjectdata subject, final XnatResourcecatalog resourceCatalog, final String uploadId, final EventMetaI ci, final Map<String, String> parameters) throws Exception {
        final String resourceFolder = resourceCatalog.getLabel();
        final XnatProjectdata project = parameters.containsKey("project")
                                        ? XnatProjectdata.getProjectByIDorAlias(parameters.get("project"), user, false)
                                        : subject.getPrimaryProject(false);

        CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(uploadId);

        final Path path = Paths.get(project.getRootArchivePath(), "subjects", subject.getArchiveDirectoryName());
        final File destination = resourceFolder != null
                                 ? path.resolve(Paths.get(resourceFolder, catalog.getId() + "_catalog.xml")).toFile()
                                 : path.resolve(catalog.getId() + "_catalog.xml").toFile();

        destination.getParentFile().mkdirs();

        try (final FileWriter writer = new FileWriter(destination)) {
            catalog.toXML(writer, true);
        } catch (IOException e) {
            throw new Exception("An error occurred trying to write the catalog to the file " + destination.getAbsolutePath(), e);
        }

        resourceCatalog.setUri(destination.getAbsolutePath());

        try {
            final XnatSubjectdata copy = subject.getLightCopy();
            copy.setResources_resource(resourceCatalog);
            SaveItemHelper.authorizedSave(copy, user, false, false, ci);
        } catch (Exception e) {
            throw new Exception("An error occurred trying to set the in/out status on the project " + project.getId(), e);
        }
    }

    private void insertExperimentResourceCatalog(final UserI user, final XnatExperimentdata experiment, final XnatResourcecatalog resourceCatalog, final String uploadId, final EventMetaI event, final Map<String, String> parameters) throws Exception {
        final boolean isImageAssessor = experiment.getItem().instanceOf(XnatImageassessordata.SCHEMA_ELEMENT_NAME);
        final String  resourceFolder  = resourceCatalog.getLabel();
        final String  experimentId    = experiment.getId();

        final Path path;
        if (isImageAssessor) {
            final XnatImagesessiondata parent = ((XnatImageassessordata) experiment).getImageSessionData();
            path = Paths.get(parent.getCurrentSessionFolder(true), "ASSESSORS", experiment.getArchiveDirectoryName());
        } else {
            path = Paths.get(experiment.getCurrentSessionFolder(true), "RESOURCES");
        }

        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(uploadId);

        final File destination = resourceFolder != null
                                 ? path.resolve(Paths.get(resourceFolder, catalog.getId() + "_catalog.xml")).toFile()
                                 : path.resolve(catalog.getId() + "_catalog.xml").toFile();

        destination.getParentFile().mkdirs();

        try (final FileWriter writer = new FileWriter(destination)) {
            catalog.toXML(writer, true);
        } catch (IOException e) {
            throw new IOException("An error occurred trying to write the catalog to the file " + destination.getAbsolutePath(), e);
        }

        resourceCatalog.setUri(destination.getAbsolutePath());

        if (isImageAssessor) {
            try {
                final XnatImageassessordata assessor = (XnatImageassessordata) experiment.getLightCopy();
                if (parameters.containsKey("type") && StringUtils.equals("in", parameters.get("type"))) {
                    assessor.setIn_file(resourceCatalog);
                } else {
                    assessor.setOut_file(resourceCatalog);
                }
                SaveItemHelper.authorizedSave(assessor, user, false, false, event);
            } catch (Exception e) {
                throw new Exception("An error occurred trying to set the in/out status on the image assessor " + experimentId, e);
            }
        } else {
            try {
                final XnatExperimentdata copy = experiment.getLightCopy();
                copy.setResources_resource(resourceCatalog);
                SaveItemHelper.authorizedSave(copy, user, false, false, event);
            } catch (Exception e) {
                throw new Exception("An error occurred trying to set the in/out status on the experiment " + experimentId, e);
            }
        }
    }

    private void insertScanResourceCatalog(final UserI user, final XnatImagescandata scan, final XnatResourcecatalog resourceCatalog, final String uploadId, final EventMetaI ci) throws Exception {
        final String resourceFolder = resourceCatalog.getLabel();

        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(uploadId);

        final XnatImagesessiondata session = scan.getImageSessionData();

        final Path path = Paths.get(session.getCurrentSessionFolder(true), "SCANS", scan.getId());
        final File destination = resourceFolder != null
                                 ? path.resolve(Paths.get(resourceFolder, catalog.getId() + "_catalog.xml")).toFile()
                                 : path.resolve(catalog.getId() + "_catalog.xml").toFile();

        destination.getParentFile().mkdirs();

        try (final FileWriter writer = new FileWriter(destination)) {
            catalog.toXML(writer, true);
        } catch (IOException e) {
            throw new IOException("An error occurred trying to write the catalog to the file " + destination.getAbsolutePath(), e);
        }

        resourceCatalog.setUri(destination.getAbsolutePath());

        if (scan.getFile().isEmpty()) {
            if (resourceCatalog.getContent() == null && scan.getType() != null) {
                resourceCatalog.setContent("RAW");
            }
        }

        try {
            scan.setFile(resourceCatalog);
            SaveItemHelper.authorizedSave(scan, user, false, false, ci);
        } catch (Exception e) {
            throw new Exception("An error occurred trying to save the scan " + scan.getId() + " for session " + scan.getImageSessionData().getLabel(), e);
        }
    }

    private void insertReconstructionResourceCatalog(final UserI user, final XnatReconstructedimagedata reconstruction, final XnatResourcecatalog resourceCatalog, final String uploadId, final EventMetaI ci, final Map<String, String> parameters) throws Exception {
        final String               resourceFolder = resourceCatalog.getLabel();
        final XnatImagesessiondata session        = reconstruction.getImageSessionData();
        final Path                 path           = Paths.get(session.getCurrentSessionFolder(true), "ASSESSORS", "PROCESSED", uploadId);

        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(uploadId);

        final File destination = resourceFolder != null
                                 ? path.resolve(Paths.get(resourceFolder, catalog.getId() + "_catalog.xml")).toFile()
                                 : path.resolve(catalog.getId() + "_catalog.xml").toFile();

        destination.getParentFile().mkdirs();

        try (final FileWriter writer = new FileWriter(destination)) {
            catalog.toXML(writer, true);
        } catch (IOException e) {
            throw new IOException("An error occurred trying to write the catalog to the file " + destination.getAbsolutePath(), e);
        }

        resourceCatalog.setUri(destination.getAbsolutePath());

        try {
            if (parameters.containsKey("type") && StringUtils.equals("in", parameters.get("type"))) {
                reconstruction.setIn_file(resourceCatalog);
            } else {
                reconstruction.setOut_file(resourceCatalog);
            }
            SaveItemHelper.authorizedSave(reconstruction, user, false, false, ci);
        } catch (Exception e) {
            throw new Exception("An error occurred trying to set the in/out status on the reconstructed image data " + reconstruction.getId(), e);
        }
    }

    private void refreshResourceCatalog(final XnatAbstractresource resource, final XnatResourceInfoMap resourceMap,
                                        final String projectId, final String projectPath, final boolean populateStats,
                                        final boolean checksums, final boolean removeMissingFiles, final boolean addUnreferencedFiles,
                                        final UserI user, final EventMetaI now) throws ServerException {
        long startTime = Calendar.getInstance().getTimeInMillis();

        if (resource instanceof XnatResourcecatalog) {
            File lockFile = new File(((XnatResourcecatalog) resource).getUri() + ".refresh");
            try {
                final ThreadAndProcessFileLock fl = ThreadAndProcessFileLock.getThreadAndProcessFileLock(lockFile,
                                                                                                         false);
                fl.tryLock(30L, TimeUnit.SECONDS);
                final CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate(projectPath,
                        (XnatResourcecatalog) resource, projectId);
                try {
                    CatalogUtils.refreshAndWriteCatalog(catalogData, user, resourceMap, now, addUnreferencedFiles,
                            removeMissingFiles, populateStats, checksums);
                } catch (Exception e) {
                    throw new ServerException("An error occurred writing the catalog file " +
                            catalogData.catFile.getAbsolutePath(), e);
                } finally {
                    fl.unlock();
                }
            } catch (IOException e) {
                log.error("Unable to obtain lock for catalog refresh: {}", resource.getLabel(), e);
            } finally {
                ThreadAndProcessFileLock.removeThreadAndProcessFileLock(lockFile);
            }
        } else if (populateStats) {
            if (CatalogUtils.populateStats(resource, projectPath)) {
                try {
                    resource.save(user, false, false, now);
                } catch (Exception e) {
                    throw new ServerException("An error occurred saving the resource " +
                                              resource.getFullPath(projectPath), e);
                }
            }
        } else {
            throw new ServerException("Resource " + resource + " is not a catalog");
        }

        log.debug("refreshResourceCatalog runtime: {} ms", (Calendar.getInstance().getTimeInMillis() - startTime));
    }

    private CatCatalogI getFromCache(final UserI user, final String catalogId) {
        final Cache.ValueWrapper cached = _cache.get(String.format(CATALOG_CACHE_KEY_FORMAT, user.getUsername(), catalogId));
        try {
            if (cached == null) {
                final File cacheFile = _userDataCache.getUserDataCacheFile(user, Paths.get("catalogs", catalogId + ".xml"));
                if (cacheFile.exists()) {
                    final CatCatalogBean catalog = new CatalogUtils.CatalogData(cacheFile, null).catBean;
                    storeToCache(user, catalog);
                    return catalog;
                }
            } else {
                final File file = (File) cached.get();
                if (file.exists()) {
                    return new CatalogUtils.CatalogData(file, null).catBean;
                }
            }
        } catch (ServerException e) {
            log.info("User {} requested catalog {} but that doesn't exist", user.getUsername(), catalogId);
        }
        return null;
    }

    private void storeToCache(final UserI user, final CatCatalogBean catalog) {
        final String catalogId = catalog.getId();
        final File   file      = _userDataCache.getUserDataCacheFile(user, Paths.get("catalogs", catalogId + ".xml"));
        file.getParentFile().mkdirs();

        try {
            try (final FileWriter writer = new FileWriter(file)) {
                catalog.toXML(writer, true);
            }
            _cache.put(String.format(CATALOG_CACHE_KEY_FORMAT, user.getUsername(), catalogId), file);
        } catch (IOException e) {
            log.error("An error occurred writing the catalog " + catalogId + " for user " + user.getLogin(), e);
        }

    }

    private Map<String, Map<String, Map<String, String>>> parseAndVerifySessions(final UserI user, final List<String> sessions, final List<String> scanTypes, final List<String> scanFormats) throws InsufficientPrivilegesException {
        // If there's any one of these that is null or has no value, that should
        // filter out everything, so don't even bother with the whole exercise.
        if (isAnyBlankList(sessions, scanTypes, scanFormats)) {
            return Collections.emptyMap();
        }

        final Multimap<String, String>                      matchingSessions = ArrayListMultimap.create();
        final Map<String, String[]>                         subjectLabelMap  = new HashMap<>();
        final Map<String, Map<String, Map<String, String>>> sessionMap       = new HashMap<>();

        for (final String sessionInfo : sessions) {
            final String[] atoms     = sessionInfo.split(":");
            final String   projectId = atoms[0];
            final String   subject   = atoms[1];
            final String   label     = atoms[2];
            final String   sessionId = atoms[3];

            try {
                final Multimap<String, String> accessMap = Permissions.verifyAccessToSessions(_parameterized, user, Collections.singletonList(sessionId), projectId);
                if (!accessMap.containsKey(projectId)) {
                    throw new InsufficientPrivilegesException(user.getUsername(), sessionId);
                } else if (accessMap.get(projectId).contains(sessionId)) {
                    //user has access to the session
                    matchingSessions.put(projectId, sessionId);
                    subjectLabelMap.put(projectId + ":" + sessionId, new String[]{subject, label});
                } else {
                    throw new InsufficientPrivilegesException(user.getUsername(), sessionId);
                }
            } catch (InsufficientPrivilegesException e) {
                throw e;
            } catch (Exception e) {
                log.error("An unexpected error occurred while trying to resolve read access for user " + user.getUsername() + " on project " + projectId);
                throw new NrgServiceRuntimeException(NrgServiceError.Unknown, e);
            }
        }

        for (final String projectId : matchingSessions.keySet()) {
            // First, can the user access the project they specified for the session at all?
            try {
                if (!Permissions.canReadProject(user, projectId)) {
                    throw new InsufficientPrivilegesException(user.getUsername(), projectId);
                }
            } catch (InsufficientPrivilegesException e) {
                throw e;
            } catch (Exception e) {
                log.error("An unexpected error occurred while trying to resolve read access for user " + user.getUsername() + " on project " + projectId);
                throw new NrgServiceRuntimeException(NrgServiceError.Unknown, e);
            }

            final Set<String> sessionIds = new HashSet<>(matchingSessions.get(projectId));

            // Now verify that the experiment is either in or shared into the specified project.
            final MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("sessionIds", sessionIds);
            parameters.addValue("projectId", projectId);

            final List<String> scanTypesWithNullRemoved   = cleanupScanTypes(scanTypes);
            final List<String> scanFormatsWithNullRemoved = cleanupScanFormats(scanFormats);

            parameters.addValue("scanTypes", scanTypesWithNullRemoved);
            parameters.addValue("scanFormats", scanFormatsWithNullRemoved);

            final String              scanTypesClause        = getScanQueryClause(scanTypes, scanTypesWithNullRemoved, SCAN_TYPE_CLAUSES);
            final String              scanFormatsClause      = getScanQueryClause(scanFormats, scanFormatsWithNullRemoved, SCAN_FORMAT_CLAUSES);
            final Map<String, String> clauses                = ImmutableMap.of("scanTypesClause", scanTypesClause, "scanFormatsClause", scanFormatsClause);
            final String              sessionsQueryToPerform = StringSubstitutor.replace(QUERY_FIND_SESSIONS_BY_TYPE_AND_FORMAT, clauses);
            final String              scansQueryToPerform    = StringSubstitutor.replace(QUERY_FIND_SCANS_BY_SESSION, clauses);

            final Set<String> matching   = new HashSet<>(_parameterized.queryForList(sessionsQueryToPerform, parameters, String.class));
            final Set<String> difference = Sets.difference(sessionIds, matching);
            if (!difference.isEmpty()) {
                //Check whether the mismatch was merely due to the lack of scans for those sessions.
                final MapSqlParameterSource scanParameters = new MapSqlParameterSource();
                scanParameters.addValue("sessionIds", difference);
                scanParameters.addValue("scanTypes", scanTypesWithNullRemoved);
                scanParameters.addValue("scanFormats", scanFormatsWithNullRemoved);
                final Set<String> matchingScans = new HashSet<>(_parameterized.queryForList(scansQueryToPerform, scanParameters, String.class));
                if (!matchingScans.isEmpty()) {
                    //The mismatch was not entirely due to sessions not having scans of those types/formats.
                    throw new InsufficientPrivilegesException(user.getUsername(), difference);
                }
            }

            for (final String sessionId : sessionIds) {
                if (matchingSessions.get(projectId).contains(sessionId)) {
                    final Map<String, Map<String, String>> projectMap;
                    if (sessionMap.containsKey(projectId)) {
                        projectMap = sessionMap.get(projectId);
                    } else {
                        projectMap = new HashMap<>();
                        sessionMap.put(projectId, projectMap);
                    }
                    final String[]            subjectLabel = subjectLabelMap.get(projectId + ":" + sessionId);
                    final String              subject      = subjectLabel[0];
                    final String              label        = subjectLabel[1];
                    final Map<String, String> subjectMap;
                    if (projectMap.containsKey(subject)) {
                        subjectMap = projectMap.get(subject);
                    } else {
                        subjectMap = new HashMap<>();
                        projectMap.put(subject, subjectMap);
                    }
                    subjectMap.put(sessionId, label);
                }
            }
        }

        return sessionMap;
    }

    // Clean up scan formats before we search for them in the database
    private List<String> cleanupScanFormats(List<String> scanFormats) {
        // Remove null
        return scanFormats.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    // Clean up scan types before we search for them in the database
    private List<String> cleanupScanTypes(List<String> scanTypes) {
        // Remove null values and replace "\" with "\\"
        return scanTypes.stream().filter(Objects::nonNull).map(type -> StringUtils.replace(type, "\\", "\\\\")).collect(Collectors.toList());
    }

    private Map<String, Object> getSessionScans(final String project, final String subject, final String label, final String session, final List<String> scanTypes, final List<String> scanFormats, final DownloadArchiveOptions options) {
        if (StringUtils.isBlank(session)) {
            throw new NrgServiceRuntimeException(NrgServiceError.Uninitialized, "Got a blank session to retrieve, that shouldn't happen.");
        }

        // If there's any one of these that is null or has no value, that should
        // filter out everything, so don't even bother with the whole exercise.
        if (isAnyBlankList(scanTypes, scanFormats)) {
            return null;
        }

        final AtomicLong totalSize              = new AtomicLong();
        final AtomicLong resourcesOfUnknownSize = new AtomicLong();

        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId("RAW");
        try {
            final MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("sessionId", session);

            final List<String> scanTypesWithNullRemoved   = cleanupScanTypes(scanTypes);
            final List<String> scanFormatsWithNullRemoved = cleanupScanFormats(scanFormats);

            parameters.addValue("scanTypes", scanTypesWithNullRemoved);
            parameters.addValue("scanFormats", scanFormatsWithNullRemoved);

            final String scanTypesClause     = getScanQueryClause(scanTypes, scanTypesWithNullRemoved, SCAN_TYPE_CLAUSES);
            final String scanFormatsClause   = getScanQueryClause(scanFormats, scanFormatsWithNullRemoved, SCAN_FORMAT_CLAUSES);
            final String scansQueryToPerform = StringSubstitutor.replace(QUERY_FIND_SCANS_BY_TYPE_AND_FORMAT, ImmutableMap.of("scanTypesClause", scanTypesClause, "scanFormatsClause", scanFormatsClause));

            final List<Map<String, Object>> scans = _parameterized.queryForList(scansQueryToPerform, parameters);
            if (scans.isEmpty()) {
                return null;
            }

            for (final Map<String, Object> scan : scans) {
                final CatEntryBean entry    = new CatEntryBean();
                final String       scanId   = (String) scan.get("scan_id");
                String             scanType = (String) scan.get("scan_type");
                final Long         scanSize = (Long) scan.get("size");
                final String       resource = URLEncoder.encode(StringUtils.defaultIfBlank((String) scan.get("resource"), "NULL"), "UTF-8");

                if (options.isSimplified()) {
                    entry.setName(getPath(options, project, subject, label, "scans", scanId, "resources", resource));
                } else {
                    // Include Series Description in the folder name for "Non Simplified" Download

                    // ScanType may have characters not conformant to naming conventions on the OS
                    // InvalidPathException should take care of that
                    if (null == scanType) {
                        entry.setName(getPath(options, project, subject, label, "scans", scanId, "resources", resource));
                    } else {

                        // First clean up the scan type string so we don't break the download
                        // Replace '\','/','(',')',',', and spaces with underscores
                        scanType = scanType.replaceAll("[\\\\/(), ]", "_");
                        String path;
                        try {
                            path = getPath(options, project, subject, label, "scans", scanId + "-" + scanType, "resources", resource);
                        } catch (InvalidPathException ipe) {
                            path = getPath(options, project, subject, label, "scans", scanId, "resources", resource);
                        }
                        if (null != path) {
                            entry.setName(path);
                        }
                    }
                }

                entry.setUri("/archive/experiments/" + session + "/scans/" + scanId + "/resources/" + resource + "/files");
                log.debug("Created session scan entry for project {} session {} scan {} ({}) with name {}: {}", project, session, scanId, resource, entry.getName(), entry.getUri());
                catalog.addEntries_entry(entry);

                if (scanSize != null) {
                    totalSize.getAndAdd(scanSize);
                } else {
                    resourcesOfUnknownSize.getAndIncrement();
                }
            }
        } catch (UnsupportedEncodingException ignored) {
            //
        }
        if (catalog.getEntries_entry().isEmpty()) {
            return null;
        } else {
            final Map<String, Object> catalogAndSize = new HashMap<>();
            catalogAndSize.put("catalog", catalog);
            catalogAndSize.put("size", totalSize.longValue());
            catalogAndSize.put("resourcesOfUnknownSize", resourcesOfUnknownSize.longValue());
            return catalogAndSize;
        }
    }

    private Map<String, Object> getRelatedData(final String project, final String subject, final String label, final String session, final List<String> resources, final String type, final DownloadArchiveOptions options) {
        if (resources == null || resources.isEmpty()) {
            return null;
        }

        // Limit the resource URIs to those associated with the current session.
        final MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("sessionId", session);
        parameters.addValue("resourceIds", resources);
        final List<Map<String, Object>> existing = _parameterized.queryForList(QUERY_SESSION_RESOURCES, parameters);
        if (existing.isEmpty()) {
            return null;
        }

        final AtomicLong totalSize              = new AtomicLong();
        final AtomicLong resourcesOfUnknownSize = new AtomicLong();

        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(StringUtils.upperCase(type));

        for (final Map<String, Object> resourceMap : existing) {
            try {
                final CatEntryBean entry          = new CatEntryBean();
                final String       resourceString = resourceMap.get("resource").toString();
                final String       resourceId     = URLEncoder.encode(resourceString, "UTF-8");
                final Long         resourceSize   = (Long) resourceMap.get("size");
                entry.setName(getPath(options, project, subject, label, "resources", resourceId));
                entry.setUri("/archive/experiments/" + session + "/resources/" + resourceId + "/files");
                log.debug("Created resource entry for project {} session {} resource {} of type {} with name {}: {}", project, session, resourceString, type, entry.getName(), entry.getUri());
                catalog.addEntries_entry(entry);
                if (resourceSize != null) {
                    totalSize.getAndAdd(resourceSize);
                } else {
                    resourcesOfUnknownSize.getAndIncrement();
                }
            } catch (UnsupportedEncodingException ignored) {
                //
            }
        }
        if (catalog.getEntries_entry().isEmpty()) {
            return null;
        } else {
            final Map<String, Object> catalogAndSize = new HashMap<>();
            catalogAndSize.put("catalog", catalog);
            catalogAndSize.put("size", totalSize.longValue());
            catalogAndSize.put("resourcesOfUnknownSize", resourcesOfUnknownSize.longValue());
            return catalogAndSize;
        }
    }

    private Map<String, Object> getSessionAssessors(final String project, final String subject, final String sessionId, final List<String> assessorTypes, final DownloadArchiveOptions options, final UserI user) {
        if ((assessorTypes == null) || assessorTypes.isEmpty()) {
            return null;
        }

        final AtomicLong totalSize              = new AtomicLong();
        final AtomicLong resourcesOfUnknownSize = new AtomicLong();

        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(StringUtils.upperCase("assessors"));

        // Limit the resource URIs to those associated with the current session.
        final MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("projectId", project);
        parameters.addValue("sessionId", sessionId);
        parameters.addValue("assessorTypes", assessorTypes);
        final List<Map<String, Object>> resources = _parameterized.queryForList(QUERY_SESSION_ASSESSORS, parameters);

        try {
            for (final Map<String, Object> resource : resources) {
                final CatEntryBean entry            = new CatEntryBean();
                final String       resourceLabel    = URLEncoder.encode(resource.get("resource_label").toString(), "UTF-8");
                final String       assessorLabel    = URLEncoder.encode(resource.get("assessor_label").toString(), "UTF-8");
                final String       sessionLabel     = URLEncoder.encode(resource.get("session_label").toString(), "UTF-8");
                final boolean      isSharedResource = Boolean.parseBoolean(resource.get("is_shared").toString());
                final String       resourceProject  = resource.get("project").toString();
                final Long         scanSize         = (Long) resource.get("size");
                try {
                    if (Permissions.canReadProject(user, resourceProject) && Permissions.canRead(user, resource.get("xsi").toString() + (isSharedResource ? "/sharing/share/project" : "/project"), resourceProject)) {
                        entry.setName(getPath(options, project, subject, sessionLabel, "assessors", assessorLabel, "resources", resourceLabel));
                        entry.setUri(StringSubstitutor.replace("/archive/experiments/${session_id}/assessors/${assessor_id}/out/resources/${resource_label}/files", resource));
                        log.debug("Created session assessor entry for project {} session {} assessor {} resource {} with name {}: {}", project, sessionId, assessorLabel, resourceLabel, entry.getName(), entry.getUri());
                        catalog.addEntries_entry(entry);
                        if (scanSize != null) {
                            totalSize.getAndAdd(scanSize);
                        } else {
                            resourcesOfUnknownSize.getAndIncrement();
                        }
                    }
                } catch (Exception e) {
                    log.warn("An error occurred trying to get session assessors for a project.", e);
                }
            }
        } catch (UnsupportedEncodingException ignored) {
            //
        }
        if (catalog.getEntries_entry().isEmpty()) {
            return null;
        } else {
            HashMap<String, Object> catalogAndSize = new HashMap<>();
            catalogAndSize.put("catalog", catalog);
            catalogAndSize.put("size", totalSize.longValue());
            catalogAndSize.put("resourcesOfUnknownSize", resourcesOfUnknownSize.longValue());
            return catalogAndSize;
        }
    }

    private String getPath(final DownloadArchiveOptions options, final String project, final String subject, final String element, final String... elements) {
        final Path base;
        if (options.isSimplified()) {
            final String[] reduced = new String[elements.length / 2];
            for (int index = 1; index < elements.length; index += 2) {
                reduced[(index - 1) / 2] = elements[index];
            }
            base = Paths.get(element, reduced);
        } else {
            base = Paths.get(element, elements);
        }
        if (options.isProjectIncludedInPath()) {
            return Paths.get(project, subject).resolve(base).toString();
        } else if (options.isSubjectIncludedInPath()) {
            return Paths.get(subject).resolve(base).toString();
        } else {
            return base.toString();
        }
    }

    private void addSafeEntrySet(final CatCatalogI catalog, final CatCatalogI innerCatalog) {
        try {
            innerCatalog.setDescription(catalog.getDescription() + " " + innerCatalog.getId());
            catalog.addSets_entryset(innerCatalog);
        } catch (Exception e) {
            log.warn("An error occurred trying to add a catalog entry to another catalog.", e);
        }
    }

    private static String getScanQueryClause(final List<String> scans, final List<String> scansWithNullRemoved, final Map<String, String> clauses) {
        if (scansWithNullRemoved.isEmpty()) {
            return clauses.get(KEY_NULL_ONLY);
        }
        return !ListUtils.isEqualList(scans, scansWithNullRemoved) ? clauses.get(KEY_WITH_NULLS) : clauses.get(KEY_NO_NULLS);
    }

    private static boolean isAnyBlankList(final List<?>... lists) {
        return Arrays.stream(lists).anyMatch(list -> list == null || list.isEmpty());
    }

    private Collection<Operation> getOperations(final Collection<Operation> operations) {
        // The default is All, so if they specified nothing, give them all.
        if (operations == null || operations.isEmpty()) {
            if (_preferences.getChecksums()) {
                return Operation.ALL;
            } else {
                List<Operation> ops = new ArrayList<>(Operation.ALL);
                ops.remove(Operation.Checksum);
                return ops;
            }
        }

        // If ANY of the operations are All, give them all as well.
        // If All isn't specified, just return a list of the actual values.
        return operations.stream().anyMatch(operation -> operation == Operation.All) ? Operation.ALL : operations;
    }

    private static String getScanId(final XnatAbstractresourceI resource) {
        if (!(resource instanceof XnatResourcecatalog)) {
            return null;
        }
        final XnatResourcecatalog catalog = (XnatResourcecatalog) resource;
        final String uri = catalog.getUri();
        if (StringUtils.isBlank(uri)) {
            return null;
        }
        final Matcher matcher = SCAN_PATTERN.matcher(uri);
        return matcher.matches() ? matcher.group("scanId") : null;
    }

    private static final Pattern SCAN_PATTERN             = Pattern.compile("^.*/SCANS/(?<scanId>[^/]+).*$");
    private static final String  EXPERIMENT_ROOT_URI      = "/archive/experiments/";
    private static final String  CATALOG_FORMAT           = "%s-%s";
    private static final String  CATALOG_SERVICE_CACHE    = DefaultCatalogService.class.getSimpleName() + "Cache";
    private static final String  CATALOG_CACHE_KEY_FORMAT = DefaultCatalogService.class.getSimpleName() + ".%s.%s";

    private static final String              CLAUSE_SCAN_TYPES              = "scan.type IN (:scanTypes)";
    private static final String              CLAUSE_NULL_SCAN_TYPES         = "scan.type IS NULL";
    private static final String              CLAUSE_SCAN_TYPES_WITH_NULLS   = "(" + CLAUSE_SCAN_TYPES + " OR " + CLAUSE_NULL_SCAN_TYPES + ")";
    private static final String              CLAUSE_SCAN_FORMATS            = "res.label IN (:scanFormats)";
    private static final String              CLAUSE_NULL_SCAN_FORMATS       = "res.label IS NULL";
    private static final String              CLAUSE_SCAN_FORMATS_WITH_NULLS = "(" + CLAUSE_SCAN_FORMATS + " OR " + CLAUSE_NULL_SCAN_FORMATS + ")";
    private static final String              KEY_NO_NULLS                   = "noNulls";
    private static final String              KEY_WITH_NULLS                 = "withNulls";
    private static final String              KEY_NULL_ONLY                  = "nullOnly";
    private static final Map<String, String> SCAN_FORMAT_CLAUSES            = ImmutableMap.of(KEY_NO_NULLS, CLAUSE_SCAN_FORMATS, KEY_WITH_NULLS, CLAUSE_SCAN_FORMATS_WITH_NULLS, KEY_NULL_ONLY, CLAUSE_NULL_SCAN_FORMATS);
    private static final Map<String, String> SCAN_TYPE_CLAUSES              = ImmutableMap.of(KEY_NO_NULLS, CLAUSE_SCAN_TYPES, KEY_WITH_NULLS, CLAUSE_SCAN_TYPES_WITH_NULLS, KEY_NULL_ONLY, CLAUSE_NULL_SCAN_TYPES);

    private static final String QUERY_FIND_SCANS_BY_SESSION               = "SELECT DISTINCT image_session_id " +
                                                                            "FROM xnat_imagescandata scan " +
                                                                            "  LEFT JOIN xnat_abstractResource res ON scan.xnat_imagescandata_id = res.xnat_imagescandata_xnat_imagescandata_id " +
                                                                            "WHERE " +
                                                                            "  image_session_id IN (:sessionIds) AND " +
                                                                            "  ${scanTypesClause} AND " +
                                                                            "  ${scanFormatsClause}";
    private static final String QUERY_FIND_SESSIONS_BY_TYPE_AND_FORMAT    = "SELECT DISTINCT scan.image_session_id AS session_id " +
                                                                            "FROM xnat_imagescandata scan " +
                                                                            "  LEFT JOIN xnat_abstractResource res ON scan.xnat_imagescandata_id = res.xnat_imagescandata_xnat_imagescandata_id " +
                                                                            "  LEFT JOIN xnat_imagesessiondata session ON scan.image_session_id = session.id " +
                                                                            "  LEFT JOIN xnat_experimentdata expt ON session.id = expt.id " +
                                                                            "  LEFT JOIN xnat_experimentdata_share share ON share.sharing_share_xnat_experimentda_id = expt.id " +
                                                                            "WHERE " +
                                                                            "  expt.id IN (:sessionIds) AND " +
                                                                            "  (share.project = :projectId OR expt.project = :projectId) AND " +
                                                                            "  ${scanTypesClause} AND " +
                                                                            "  ${scanFormatsClause}";
    private static final String QUERY_FIND_SCANS_BY_TYPE_AND_FORMAT       = "SELECT " +
                                                                            "  scan.id   AS scan_id, " +
                                                                            "  scan.type AS scan_type, " +
                                                                            "  coalesce(res.label, res.xnat_abstractresource_id :: VARCHAR) AS resource," +
                                                                            "  res.file_size AS size " +
                                                                            "FROM xnat_imagescandata scan " +
                                                                            "  JOIN xnat_abstractResource res ON scan.xnat_imagescandata_id = res.xnat_imagescandata_xnat_imagescandata_id " +
                                                                            "WHERE " +
                                                                            "  scan.image_session_id = :sessionId AND " +
                                                                            "  ${scanTypesClause} AND " +
                                                                            "  ${scanFormatsClause} " +
                                                                            "ORDER BY scan_id";
    private static final String QUERY_FIND_XSI_TYPE_FOR_EXPERIMENT_ID     = "SELECT " +
                                                                            "  xme.element_name " +
                                                                            "FROM xnat_experimentData expt " +
                                                                            "  LEFT JOIN xdat_meta_element xme ON expt.extension=xme.xdat_meta_element_id " +
                                                                            "WHERE expt.id = :id";
    private static final String QUERY_SESSION_RESOURCES                   = "SELECT res.label resource, " +
                                                                            "  res.file_size AS size " +
                                                                            "FROM xnat_abstractresource res " +
                                                                            "  LEFT JOIN xnat_experimentdata_resource exptRes " +
                                                                            "    ON exptRes.xnat_abstractresource_xnat_abstractresource_id = res.xnat_abstractresource_id " +
                                                                            "  LEFT JOIN xnat_experimentdata expt ON expt.id = exptRes.xnat_experimentdata_id " +
                                                                            "WHERE expt.ID = :sessionId AND res.label IN (:resourceIds)";
    private static final String QUERY_SESSION_ASSESSORS                   = "SELECT " +
                                                                            "  abstract.xnat_abstractresource_id AS resource_id, " +
                                                                            "  coalesce(abstract.label, abstract.xnat_abstractresource_id :: VARCHAR) AS resource_label, " +
                                                                            "  assessor.id AS assessor_id, " +
                                                                            "  coalesce(share.label, assessor.label) AS assessor_label, " +
                                                                            "  session.id AS session_id, " +
                                                                            "  session.label AS session_label, " +
                                                                            "  :projectId AS project, " +
                                                                            "  CASE WHEN share.project = :projectId THEN TRUE ELSE FALSE END AS is_shared, " +
                                                                            "  xme.element_name AS xsi, " +
                                                                            "  abstract.file_size AS size " +
                                                                            "FROM " +
                                                                            "  xnat_abstractresource abstract " +
                                                                            "  LEFT JOIN img_assessor_out_resource imgOut ON imgOut.xnat_abstractresource_xnat_abstractresource_id = abstract.xnat_abstractresource_id " +
                                                                            "  LEFT JOIN xnat_imageassessordata imgAssessor ON imgOut.xnat_imageassessordata_id = imgAssessor.id " +
                                                                            "  LEFT JOIN xnat_experimentdata assessor ON assessor.id = imgAssessor.id " +
                                                                            "  LEFT JOIN xnat_experimentdata session ON session.id = imgAssessor.imagesession_id " +
                                                                            "  LEFT JOIN xnat_experimentdata_share share ON assessor.id = share.sharing_share_xnat_experimentda_id " +
                                                                            "  LEFT JOIN xdat_meta_element xme ON assessor.extension = xme.xdat_meta_element_id " +
                                                                            "WHERE " +
                                                                            "  :projectId IN (share.project, assessor.project) AND " +
                                                                            "  xme.element_name IN (:assessorTypes) AND " +
                                                                            "  session.id = :sessionId";
    @SuppressWarnings({"unused", "RedundantSuppression"})
    private static final String QUERY_SESSION_ASSESSORS_WITH_ALL_PROJECTS = "SELECT " +
                                                                            "  abstract.xnat_abstractresource_id AS resource_id, " +
                                                                            "  coalesce(abstract.label, abstract.xnat_abstractresource_id :: VARCHAR) AS resource_label, " +
                                                                            "  assessor.id AS assessor_id, " +
                                                                            "  assessor.label AS assessor_label, " +
                                                                            "  session.id AS session_id, " +
                                                                            "  session.label AS session_label, " +
                                                                            "  assessor.project AS project, " +
                                                                            "  xme.element_name AS xsi, " +
                                                                            "  abstract.file_size AS size " +
                                                                            "FROM " +
                                                                            "  xnat_abstractresource abstract " +
                                                                            "  LEFT JOIN img_assessor_out_resource imgOut ON imgOut.xnat_abstractresource_xnat_abstractresource_id = abstract.xnat_abstractresource_id " +
                                                                            "  LEFT JOIN xnat_imageassessordata imgAssessor ON imgOut.xnat_imageassessordata_id = imgAssessor.id " +
                                                                            "  LEFT JOIN xnat_experimentdata assessor ON assessor.id = imgAssessor.id " +
                                                                            "  LEFT JOIN xnat_experimentdata session ON session.id = imgAssessor.imagesession_id " +
                                                                            "  LEFT JOIN xdat_meta_element xme ON assessor.extension = xme.xdat_meta_element_id " +
                                                                            "WHERE " +
                                                                            "  xme.element_name IN (:assessorTypes) AND " +
                                                                            "  session.id = :sessionId " +
                                                                            "UNION " +
                                                                            "SELECT " +
                                                                            "  abstract.xnat_abstractresource_id AS resource_id, " +
                                                                            "  coalesce(abstract.label, abstract.xnat_abstractresource_id :: VARCHAR) AS resource_label, " +
                                                                            "  assessor.id AS assessor_id, " +
                                                                            "  share.label AS assessor_label, " +
                                                                            "  session.id AS session_id, " +
                                                                            "  session.label AS session_label, " +
                                                                            "  share.project AS project, " +
                                                                            "  xme.element_name AS xsi, " +
                                                                            "  abstract.file_size AS size " +
                                                                            "FROM " +
                                                                            "  xnat_abstractresource abstract " +
                                                                            "  LEFT JOIN img_assessor_out_resource imgOut ON imgOut.xnat_abstractresource_xnat_abstractresource_id = abstract.xnat_abstractresource_id " +
                                                                            "  LEFT JOIN xnat_imageassessordata imgAssessor ON imgOut.xnat_imageassessordata_id = imgAssessor.id " +
                                                                            "  LEFT JOIN xnat_experimentdata assessor ON assessor.id = imgAssessor.id " +
                                                                            "  LEFT JOIN xnat_experimentdata session ON session.id = imgAssessor.imagesession_id " +
                                                                            "  LEFT JOIN xnat_experimentdata_share share ON assessor.id = share.sharing_share_xnat_experimentda_id " +
                                                                            "  LEFT JOIN xdat_meta_element xme ON assessor.extension = xme.xdat_meta_element_id " +
                                                                            "WHERE " +
                                                                            "  share.label IS NOT NULL AND " +
                                                                            "  xme.element_name IN (:assessorTypes) AND " +
                                                                            "  session.id = :sessionId ";

    private final SiteConfigPreferences      _preferences;
    private final NamedParameterJdbcTemplate _parameterized;
    private final Cache                      _cache;
    private final UserDataCache              _userDataCache;
    private       RemoteFilesService         _remoteFilesService = null;
}
