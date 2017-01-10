/*
 * web: org.nrg.xnat.helpers.dicom.DicomDump
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.dicom;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dcm4che2.data.ElementDictionary;
import org.nrg.action.ClientException;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcTableBuilder;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xnat.utils.CatalogUtils.CatEntryFilterI;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.restlet.util.Template;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


public final class DicomDump extends SecureResource {
    // "src" attribute the contains the uri to the desired resources
    private static final String SRC_ATTR = "src";
    private static final String FIELD_PARAM = "field";
    // image type supported.
    private static final List<String> imageTypes = new ArrayList<>();

    private static final ElementDictionary TAG_DICTIONARY = ElementDictionary.getDictionary();

    // The global environment
    private final Env env;

    /**
     * A global environment that contains the type of request has made and a
     * map of the parsed "src" uri.
     *
     * @author aditya
     */
    @SuppressWarnings("unused")
    class Env {
        Map<String, Object> attrs = new HashMap<>();
        HeaderType h;
        ArchiveType a;
        ResourceType r;
        final String uri;
        final Map<Integer, Set<String>> fields;

        Env(String uri, Map<Integer, Set<String>> fields) {
            this.uri = uri;
            this.a = ArchiveType.UNKNOWN;
            this.h = HeaderType.UNKNOWN;
            this.r = ResourceType.UNKNOWN;
            this.fields = fields;
            this.determineArchiveType();
            this.determineHeaderType();
            this.determineResourceType();
        }

        ArchiveType getArchiveType() {
            return this.a;
        }

        HeaderType getHeaderType() {
            return this.h;
        }

        ResourceType getResourceType() {
            return this.r;
        }

        void determineArchiveType() {
            if (this.uri.startsWith("/prearchive/")) {
                this.a = ArchiveType.PREARCHIVE;
            } else if (this.uri.startsWith("/archive/")) {
                this.a = ArchiveType.ARCHIVE;
            } else {
                this.a = ArchiveType.UNKNOWN;
            }
        }

        /**
         * If a summary is requested then the resource type defaults to SCAN.
         */
        void determineResourceType() {
            if (this.a != ArchiveType.UNKNOWN && this.h != HeaderType.UNKNOWN) {
                this.r = ResourceType.SCAN;
            }
        }

        /**
         * Find a matching template and update the global environment
         *
         * @param _h The header type.
         */
        void visit(HeaderType _h) {
            for (final Template t : _h.getTemplates()) {
                if (t.match(this.uri) != -1) {
                    t.parse(this.uri, this.attrs);
                    this.h = _h;
                    this.r = _h.getResourceType(t);
                    break;
                }
            }
        }

        void determineHeaderType() {
            for (HeaderType h : HeaderType.values()) {
                if (this.h == HeaderType.UNKNOWN) {
                    this.visit(h);
                }
            }
        }
    }


    /**
     * The dicom dump requested, either a specific file, or a general summary of the session
     *
     * @author aditya
     */
    private enum HeaderType {
        FILE("/prearchive/projects/{PROJECT_ID}/{TIMESTAMP}/{EXPT_ID}/scans/{SCAN_ID}/resources/DICOM/files/{FILENAME}",
                "/archive/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/scans/{SCAN_ID}/resources/DICOM/files/{FILENAME}",
                "/archive/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/assessors/{SCAN_ID}/resources/DICOM/files/{FILENAME}",
                "/archive/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/recons/{SCAN_ID}/resources/DICOM/files/{FILENAME}",
                "/prearchive/projects/{PROJECT_ID}/{TIMESTAMP}/{EXPT_ID}/scans/{SCAN_ID}/resources/DICOM/files/{FILENAME}"
        ) {
            private static final String FILENAME_PARAM = "FILENAME";

            @Override
            CatFilterWithPath getFilter(final Env env, final UserI user) {
                final Object filename = env.attrs.get(FILENAME_PARAM);
                return new CatFilterWithPath() {
                    public boolean accept(CatEntryI entry) {
                        final File f = CatalogUtils.getFile(entry, path);
                        return f.getName().equals(filename);
                    }
                };
            }
        },

        SCAN("/archive/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/scans/{SCAN_ID}",
                "/archive/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/assessors/{SCAN_ID}",
                "/archive/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/recons/{SCAN_ID}",
                "/archive/projects/{PROJECT_ID}/experiments/{EXPT_ID}/scans/{SCAN_ID}",
                "/archive/projects/{PROJECT_ID}/experiments/{EXPT_ID}/assessors/{SCAN_ID}",
                "/archive/projects/{PROJECT_ID}/experiments/{EXPT_ID}/recons/{SCAN_ID}",
                "/prearchive/projects/{PROJECT_ID}/{TIMESTAMP}/{EXPT_ID}/scans/{SCAN_ID}"
        ),

        SESSION("/archive/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}",
                "/archive/projects/{PROJECT_ID}/experiments/{EXPT_ID}",
                "/prearchive/projects/{PROJECT_ID}/{TIMESTAMP}/{EXPT_ID}"
        ),

        UNKNOWN() {
            @Override
            String retrieve(Env env, UserI user) {
                return null;
            }
        };

        private final ImmutableMap<Template, ResourceType> templates;

        HeaderType(final String... templates) {
            // Convert the provided string templates to Template objects
            final ImmutableMap.Builder<Template, ResourceType> builder = ImmutableMap.builder();
            for (final String st : templates) {
                final Template t = new Template(st, Template.MODE_STARTS_WITH);
                final ResourceType r;
                if (st.contains("scans")) {
                    r = ResourceType.SCAN;
                } else if (st.contains("assessors")) {
                    r = ResourceType.ASSESSOR;
                } else if (st.contains("recons")) {
                    r = ResourceType.RECON;
                } else {
                    r = ResourceType.UNKNOWN;
                }
                builder.put(t, r);
            }
            this.templates = builder.build();
        }


        /**
         * The URI templates associated with type
         *
         * @return The available templates.
         */
        final List<Template> getTemplates() {
            return Lists.newArrayList(templates.keySet());
        }

        /**
         * Based on the matching template output the correct resource type
         *
         * @param matchingTemplate The template to match.
         * @return The resource type matching the submitted template.
         */
        final ResourceType getResourceType(final Template matchingTemplate) {
            final ResourceType r = templates.get(matchingTemplate);
            return null == r ? ResourceType.UNKNOWN : r;
        }

        /**
         * Returns the filter used to determine whether to use a provided catalog entry.
         * Default implementation always passes.
         *
         * @param env  The environment object.
         * @param user The user.
         * @return The catalog filter.
         */
        CatFilterWithPath getFilter(Env env, UserI user) {
            return alwaysCatWithPath;
        }

        /**
         * Retrieve the file path to the first matching file.
         * <p/>
         * Returns null if no DICOM file is found.
         *
         * @param env  The environment object.
         * @param user The user.
         * @return The file path to the first matching file.
         * @throws ClientException
         * @throws IOException
         * @throws InvalidPermissionException
         * @throws Exception
         */
        String retrieve(final Env env, final UserI user) throws Exception {
            for (final File f : env.r.getFiles(env, user, getFilter(env, user), 1)) {
                if (null != f) {
                    return f.getAbsolutePath();
                }
            }
            return null;
        }
    }

    /**
     * The location of the requested session.
     * <p/>
     * This class does some slightly unkosher things with global variables.
     * <p/>
     * There are two global class variables, one holding the root path to a resource and the image session object associated
     * with requested session. These variables are updated *implicitly* by functions in the class, so the call order of the methods
     * in this class is important.
     * <p/>
     * Please read the method comments for more details.
     *
     * @author aditya
     */
    private enum ArchiveType {
        PREARCHIVE() {
            @Override
            CatCatalogI getCatalog(XnatResourcecatalogI r) {
                this.rootPath = CatalogUtils.getCatalogFile(this.x.getPrearchivepath(), r).getParentFile().getAbsolutePath();
                return CatalogUtils.getCleanCatalog(this.x.getPrearchivepath(), r, false);
            }

            @Override
            XnatImagesessiondataI retrieve(Env env, UserI user) throws Exception {
                String project = (String) env.attrs.get("PROJECT_ID");
                String experiment = (String) env.attrs.get("EXPT_ID");
                String timestamp = (String) env.attrs.get("TIMESTAMP");
                File sessionDIR;
                File srcXML;
                sessionDIR = PrearcUtils.getPrearcSessionDir(user, project, timestamp, experiment, false);
                srcXML = new File(sessionDIR.getAbsolutePath() + ".xml");
                XnatImagesessiondataI x = PrearcTableBuilder.parseSession(srcXML);
                this.x = x;
                return x;
            }
        },
        ARCHIVE() {
            @Override
            CatCatalogI getCatalog(XnatResourcecatalogI r) {
                this.rootPath = (new File(r.getUri())).getParent();
                return CatalogUtils.getCleanCatalog(this.rootPath, r, true);
            }

            @Override
            XnatImagesessiondataI retrieve(Env env, UserI user) throws Exception {
                String project = (String) env.attrs.get("PROJECT_ID");
                String experiment = (String) env.attrs.get("EXPT_ID");
                XnatImagesessiondata x = (XnatImagesessiondata) XnatExperimentdata.GetExptByProjectIdentifier(project, experiment, user, false);
                if (x == null || null == x.getId()) {
                    x = (XnatImagesessiondata) XnatExperimentdata.getXnatExperimentdatasById(experiment, user, false);
                    if (x != null && !x.hasProject(project)) {
                        x = null;
                    }
                }
                if (x == null) {
                    throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND,
                            "Experiment or project not found",
                            new Exception("Experiment or project not found"));
                }
                this.x = x;
                return x;
            }
        },

        UNKNOWN() {
            @Override
            CatCatalogI getCatalog(XnatResourcecatalogI r) {
                return null;
            }

            @Override
            XnatImagesessiondataI retrieve(Env env, UserI user) {
                return null;
            }
        };

        XnatImagesessiondataI x = null;
        String rootPath = null;

        /**
         * Retrieve the catalog for this resource. Additionally this also updates the
         * "rootPath" global class variable. This function is dependent on the
         * {@link XnatImageassessordataI} having been populated.
         *
         * @param r The resource catalog reference.
         * @return The catalog for the resource.
         */
        abstract CatCatalogI getCatalog(XnatResourcecatalogI r);

        /**
         * Retrieve the image session object for this session. Additionally this also updates the
         * XnatImagesessiondataI global.
         *
         * @param env  The environment object.
         * @param user The user.
         * @return The image session object.
         * @throws ClientException
         * @throws IOException
         * @throws InvalidPermissionException
         * @throws Exception
         */
        abstract XnatImagesessiondataI retrieve(Env env, UserI user) throws Exception;
    }

    /**
     * The type of resource requested.
     *
     * @author aditya
     */
    private enum ResourceType {
        SCAN {
            Iterable<File> getFiles(Env env, UserI user, CatFilterWithPath filter, int enough) throws Exception {
                final XnatImagesessiondataI x = env.a.retrieve(env, user);
                final List<File> files = new ArrayList<>();
                final Object scanID = env.attrs.get(URIManager.SCAN_ID);
                for (final XnatImagescandataI scan : x.getScans_scan()) {
                    if (null == scanID || scanID.equals(scan.getId())) {
                        final List<XnatResourcecatalogI> resources = scan.getFile();
                        files.addAll(this.findMatchingFile(env, resources, filter, enough));
                        if (files.size() >= enough) {
                            return files;
                        }
                    }
                }
                return files;
            }
        },

        ASSESSOR {
            Iterable<File> getFiles(Env env, UserI user, CatFilterWithPath filter, int enough) throws Exception {
                final XnatImagesessiondataI x = env.a.retrieve(env, user);
                final Object id = env.attrs.get(URIManager.SCAN_ID);
                final List<File> files = new ArrayList<>();
                for (XnatImageassessordataI assessor : x.getAssessors_assessor()) {
                    if (null == id || id.equals(assessor.getId())) {
                        final List<XnatResourcecatalogI> resources = assessor.getResources_resource();
                        files.addAll(this.findMatchingFile(env, resources, filter, enough));
                        if (files.size() >= enough) {
                            return files;
                        }
                        final List<XnatResourcecatalogI> in_resources = assessor.getIn_file();
                        files.addAll(this.findMatchingFile(env, in_resources, filter, enough));
                        if (files.size() >= enough) {
                            return files;
                        }
                        final List<XnatResourcecatalogI> out_resources = assessor.getOut_file();
                        files.addAll(this.findMatchingFile(env, out_resources, filter, enough));
                        if (files.size() >= enough) {
                            return files;
                        }
                    }
                }
                return files;
            }
        },

        RECON {
            Iterable<File> getFiles(Env env, UserI user, CatFilterWithPath filter, int enough) throws Exception {
                final XnatImagesessiondataI x = env.a.retrieve(env, user);
                final Object id = env.attrs.get(URIManager.SCAN_ID);
                final Collection<File> files = new ArrayList<>();
                for (XnatReconstructedimagedataI recon : x.getReconstructions_reconstructedimage()) {
                    if (null == id || id.equals(recon.getId())) {
                        List<XnatResourcecatalogI> in_resources = recon.getIn_file();
                        files.addAll(this.findMatchingFile(env, in_resources, filter, enough));
                        if (files.size() >= enough) {
                            return files;
                        }
                        List<XnatResourcecatalogI> out_resources = recon.getOut_file();
                        files.addAll(this.findMatchingFile(env, out_resources, filter, enough));
                        if (files.size() >= enough) {
                            return files;
                        }
                    }
                }
                return files;
            }
        },

        UNKNOWN {
            Iterable<File> getFiles(Env env, UserI user, CatFilterWithPath filter, int enough) {
                return Collections.emptyList();
            }
        };

        List<File> findMatchingFile(final Env env, final List<XnatResourcecatalogI> resources, final CatFilterWithPath filter, final int enough) {
            final List<File> files = Lists.newArrayList();
            for (XnatResourcecatalogI resource : resources) {
                final String type = resource.getLabel();
                if (imageTypes.contains(type)) {
                    final CatCatalogI catalog = env.a.getCatalog(resource);
                    filter.setPath(env.a.rootPath);
                    for (CatEntryI match : CatalogUtils.getEntriesByFilter(catalog, filter)) {
                        files.add(CatalogUtils.getFile(match, env.a.rootPath));
                        if (files.size() >= enough) {
                            return files;
                        }
                    }
                }
            }
            return files;
        }

        /**
         * Retrieve the DICOM files at this resource level.
         *
         * @param env    The environment object.
         * @param user   The user.
         * @param filter The filter.
         * @param enough How many files is enough?
         * @return A collection of matching files.
         * @throws ClientException
         * @throws IOException
         * @throws InvalidPermissionException
         * @throws Exception
         */
        abstract Iterable<File> getFiles(Env env, UserI user, CatFilterWithPath filter, int enough) throws Exception;
    }

    private static ImmutableMap<Integer, Set<String>> getFields(String[] fieldVals) {
        ImmutableMap.Builder<Integer, Set<String>> fieldsb = ImmutableMap.builder();
        for (final String field : fieldVals) {
            final String[] parts = field.split(":");
            final String tag_s = parts[0];
            final Set<String> subs = Sets.newHashSet();
            subs.addAll(Arrays.asList(parts).subList(1, parts.length));

            int tag;
            try {
                tag = TAG_DICTIONARY.tagForName(tag_s);
            } catch (IllegalArgumentException e) {
                try {
                    tag = Integer.parseInt(tag_s, 16);
                } catch (NumberFormatException e1) {
                    throw new IllegalArgumentException("not a valid DICOM attribute tag: " + tag_s, e1);
                }
            }
            fieldsb.put(tag, subs);
        }
        return fieldsb.build();
    }

    public DicomDump(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);

        if (!this.containsQueryVariable(DicomDump.SRC_ATTR)) {
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Please set the src parameter");
            env = null;
            return;
        }

        final Map<Integer, Set<String>> fields;
        try {
            fields = getFields(getQueryVariables(FIELD_PARAM));
        } catch (IllegalArgumentException e) {
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
            env = null;
            return;
        }

        this.env = new Env(this.getQueryVariable(DicomDump.SRC_ATTR), fields);

        try {
            final String dumpImageTypes = XDAT.getSiteConfigurationProperty("dumpImageTypes", "DICOM, secondary");
            Collections.addAll(imageTypes, dumpImageTypes.split("\\s*,\\s*"));
        } catch (ConfigServiceException e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, "Error trying to get site configuration property");
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Error trying to get site configuration property", e);
        }

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    public boolean allowPost() {
        return false;
    }

    public boolean allowPut() {
        return false;
    }

    /**
     * Enhance {@link CatEntryFilterI} to include a path
     * in its environment.
     * <p/>
     * This is required because by the time we get down to
     * iterating through the catalog entries we've lost
     * access to the absolute path to the resource.
     *
     * @author aditya
     */
    static abstract class CatFilterWithPath implements CatEntryFilterI {
        String path;

        public void setPath(String path) {
            this.path = path;
        }

        public abstract boolean accept(CatEntryI entry);
    }

    final static CatFilterWithPath alwaysCatWithPath = new CatFilterWithPath() {
        {
            setPath(null);
        }

        public boolean accept(CatEntryI entry) {
            return true;
        }
    };

    public Representation represent(final Variant variant) {
        final MediaType mt = overrideVariant(variant);
        try {
            String file = this.env.h.retrieve(this.env, getUser());
            DicomHeaderDump d = new DicomHeaderDump(file, env.fields);
            final XFTTable t = d.render();
            return this.representTable(t, mt, new Hashtable<String, Object>());
        } catch (FileNotFoundException e) {
            this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, e);
            return null;
        } catch (IOException e) {
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
            return null;
        } catch (ClientException e) {
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
            return null;
        } catch (Throwable e) {
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
            return null;
        }
    }
}
