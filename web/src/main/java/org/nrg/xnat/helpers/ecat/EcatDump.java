package org.nrg.xnat.helpers.ecat;
/*
 * org.nrg.xnat.helpers.ecat.EcatDump
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/3/14 9:54 AM
 */

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dcm4che2.data.ElementDictionary;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
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
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xnat.utils.CatalogUtils.CatEntryFilterI;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.restlet.util.Template;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public final class EcatDump extends SecureResource {
	//dump all discovered fields from all ecat files in session,scan etc
	private static final String SUMMARY_VALUE = "true";
	private static final String SUMMARY_ATTR = "summary";
    // "src" attribute the contains the uri to the desired resources
    private static final String SRC_ATTR = "src";
    private static final String FIELD_PARAM = "field";
    // image type supported.
    private static final String imageType = "ECAT";
    private static final int MAXFILENUMBER=10000;
    private static final ElementDictionary TAG_DICTIONARY = ElementDictionary.getDictionary();

    // The global environment
    private final Env env;

    /**
     * A global environment that contains the type of request has made and a
     * map of the parsed "src" uri.
     * @author aditya
     *
     */	
    class Env {
        Map<String,Object> attrs = new HashMap<String, Object>();
        HeaderType h;
        ArchiveType a;
        ResourceType r;
        final String uri; 
        final Map<Integer,Set<String>> fields;

        Env(String uri, Map<Integer,Set<String>> fields){
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
        void determineArchiveType () {
            if (this.uri.startsWith("/prearchive/")){
                this.a = ArchiveType.PREARCHIVE;
            }
            else if (this.uri.startsWith("/archive/")) {
                this.a = ArchiveType.ARCHIVE;
            }
            else {
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
         * @param _h
         */
        void visit (HeaderType _h) {
            List<Template> ts = _h.getTemplates();
            Iterator<Template> i = ts.iterator();
            while (i.hasNext()) {
                Template t = i.next();
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
                else {
                    // the uri has been parsed so quit looking
                }
            }
        }

        @Nullable
        String getProject() {
            final Object proj = env.attrs.get("PROJECT_ID");
            return proj != null ? (String) proj : null;
        }
    }


    /**
     * The ecat dump requested, either a specific file, or a general summary of the session
     * @author aditya
     *
     */
    private static enum HeaderType {
        FILE("/prearchive/projects/{PROJECT_ID}/{TIMESTAMP}/{EXPT_ID}/scans/{SCAN_ID}/resources/ECAT/files/{FILENAME}",
                "/archive/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/scans/{SCAN_ID}/resources/ECAT/files/{FILENAME}",
                "/archive/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/assessors/{SCAN_ID}/resources/ECAT/files/{FILENAME}",
                "/archive/projects/{PROJECT_ID}/subjects/{SUBJECT_ID}/experiments/{EXPT_ID}/recons/{SCAN_ID}/resources/ECAT/files/{FILENAME}",
                "/prearchive/projects/{PROJECT_ID}/{TIMESTAMP}/{EXPT_ID}/scans/{SCAN_ID}/resources/ECAT/files/{FILENAME}"
        ){
            private static final String FILENAME_PARAM = "FILENAME";

            @Override
            CatFilterWithPath getFilter(final Env env, final UserI user) {
                final Object filename = env.attrs.get(FILENAME_PARAM);
                final String project = env.getProject();
                return new CatFilterWithPath() {
                    public boolean accept(CatEntryI entry) {
                        final File f = CatalogUtils.getFile(entry, path, project);
                        return f != null && f.getName().equals(filename);
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
        
        UNKNOWN(){
            @Override
            String retrieve(Env env, UserI user) { return null; }
        };

        private final ImmutableMap<Template,ResourceType> templates;

        private HeaderType(final String...templates) {
            // Convert the provided string templates to Template objects
            final ImmutableMap.Builder<Template,ResourceType> builder = ImmutableMap.builder();
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
         * @return
         */
        final List<Template> getTemplates() { return Lists.newArrayList(templates.keySet()); }

        /**
         * Based on the matching template output the correct resource type 
         * @param matchingTemplate
         * @return
         */
        final ResourceType getResourceType(final Template matchingTemplate) {
            final ResourceType r = templates.get(matchingTemplate);
            return null == r ? ResourceType.UNKNOWN : r;
        }

        /**
         * Returns the filter used to determine whether to use a provided catalog entry.
         * Default implementation always passes.
         * @param env
         * @param user
         * @return
         */
        CatFilterWithPath getFilter(Env env, UserI user) { return alwaysCatWithPath; }
        
        /**
         * Retrieve the file path to the first matching file.
         * 
         * Returns null if no ECAT file is found.
         * 
         * @param env
         * @param user
         * @return
         * @throws ClientException
         * @throws IOException
         * @throws InvalidPermissionException
         * @throws Exception
         */
        String retrieve(final Env env, final UserI user) throws Exception {
            final Iterable<File> matches = env.r.getFiles(env, user, getFilter(env, user), 1);
            for (final Iterator<File> fi = matches.iterator(); fi.hasNext(); ) {
                final File f = fi.next();
                if (null != f) {
                    return f.getAbsolutePath();
                }
            }
            return null;    
        }
        
        Iterable<File> retrieveAll(final Env env, final UserI user) throws Exception {
            final Iterable<File> matches = env.r.getFiles(env, user, getFilter(env, user), MAXFILENUMBER);
           
            return matches;    
        }
    };

    /**
     * The location of the requested session.
     *  
     * This class does some slightly unkosher things with global variables.
     * 
     * There are two global class variables, one holding the root path to a resource and the image session object associated
     * with requested session. These variables are updated *implicitly* by functions in the class, so the call order of the methods
     * in this class is important.
     *    
     * Please read the method comments for more details. 
     * @author aditya
     *
     */
    private static enum ArchiveType {
        PREARCHIVE(){
            @Override
            CatCatalogI getCatalog(XnatResourcecatalogI r) {
                CatalogUtils.CatalogData catalogData;
                try {
                    catalogData = CatalogUtils.CatalogData.getOrCreateAndClean(this.x.getPrearchivepath(), r, false, this.x.getProject()
                    );
                } catch (ServerException e) {
                    return null;
                }
                this.rootPath = catalogData.catPath;
                return catalogData.catBean;
            }

            @Override
            XnatImagesessiondataI retrieve(Env env, UserI user) throws Exception, IOException, InvalidPermissionException{
                String project = (String) env.attrs.get("PROJECT_ID");
                String experiment = (String)env.attrs.get("EXPT_ID");
                String timestamp = (String) env.attrs.get("TIMESTAMP");
                File sessionDIR;
                File srcXML;
                sessionDIR = PrearcUtils.getPrearcSessionDir(user, project, timestamp, experiment,false);
                srcXML=new File(sessionDIR.getAbsolutePath()+".xml");
                XnatImagesessiondataI x = PrearcTableBuilder.parseSession(srcXML);
                this.x = x;
                return x;
            }
        },
        ARCHIVE(){
            @Override
            CatCatalogI getCatalog(XnatResourcecatalogI r) {
                this.rootPath = (new File(r.getUri())).getParent();
                CatalogUtils.CatalogData catalogData;
                try {
                    catalogData = CatalogUtils.CatalogData.getOrCreateAndClean(this.rootPath, r, true, this.x.getProject()
                    );
                } catch (ServerException e) {
                    return null;
                }
                this.rootPath = catalogData.catPath;
                return catalogData.catBean;
            }

            @Override
            XnatImagesessiondataI retrieve(Env env, UserI user) throws ClientException,IOException,InvalidPermissionException,Exception {
                String project = (String) env.attrs.get("PROJECT_ID");
                String experiment = (String)env.attrs.get("EXPT_ID");
                XnatImagesessiondata x = (XnatImagesessiondata) XnatExperimentdata.GetExptByProjectIdentifier(project, experiment,user, false);
                if (x == null || null == x.getId()) {
                    x = (XnatImagesessiondata) XnatExperimentdata.getXnatExperimentdatasById(experiment, user, false);
                    if (x != null && !x.hasProject(project)) {
                        x = null;
                    }
                }
                if (x == null) {
                    throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, 
                            "Experiment or project not found", 
                            new Exception ("Experiment or project not found"));
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
        String rootPath=null;

        /**
         * Retrieve the catalog for this resource. Additionally this also updates the 
         * "rootPath" global class variable. This function is dependent on the 
         * {@link XnatImageassessordataI} having been populated.
         * @param r
         * @return
         */
        abstract CatCatalogI getCatalog(XnatResourcecatalogI r);

        /**
         * Retrieve the image session object for this session. Additionally this also updates the
         * XnatImagesessiondataI global. 
         * @param env
         * @param user
         * @return
         * @throws ClientException
         * @throws IOException
         * @throws InvalidPermissionException
         * @throws Exception
         */
        abstract XnatImagesessiondataI retrieve(Env env, UserI user) throws ClientException, IOException, InvalidPermissionException, Exception;
    };

    /**
     * The type of resource requested.
     * @author aditya
     *
     */
    private static enum ResourceType {
        SCAN {
            Iterable<File> getFiles(Env env, UserI user, CatFilterWithPath filter, int enough) throws Exception {
            	final XnatImagesessiondataI x = env.a.retrieve(env, user);
                final List<File> files = new ArrayList<File>();
                final Object scanID = env.attrs.get(URIManager.SCAN_ID);
                for (final XnatImagescandataI scan : x.getScans_scan()){
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
                final List<File> files = new ArrayList<File>();
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
                final XnatImagesessiondataI x = env.a.retrieve(env,user);
                final Object id = env.attrs.get(URIManager.SCAN_ID);
                final Collection<File> files = new ArrayList<File>();
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
            for (XnatResourceI resource : resources) {
                final String eUrl = resource.getUri();
                String prePath=ArcSpecManager.GetInstance().getGlobalPrearchivePath();
                if (eUrl.contains(EcatDump.imageType)) {
                	File rfile=new File (resource.getUri());
                	if (rfile.exists()){
                		files.add(rfile);
                		if (files.size() >= enough) {
                            return files;
                        }
                	} 
                	String project=(String) env.attrs.get("PROJECT_ID");
                	String timestamp=(String) env.attrs.get("TIMESTAMP");
                	String expt_id=(String) env.attrs.get("EXPT_ID");
                	
                	rfile = new File(prePath+File.separator+project+File.separator+timestamp+File.separator+expt_id+File.separator+resource.getUri());
                	if (rfile.exists()){
                		files.add(rfile);
                		if (files.size() >= enough) {
                            return files;
                        }
                	}
                   
                }
            }
            return files;
        }

        /**
         * Retrieve the ECAT files at this resource level.
         * @param user
         * @param filter
         * @param enough
         * @return
         * @throws ClientException
         * @throws IOException
         * @throws InvalidPermissionException
         * @throws Exception
         */
        abstract Iterable<File> getFiles(Env env, 
                UserI user, 
                CatFilterWithPath filter,
                int enough) 
                throws ClientException, 
                IOException, 
                InvalidPermissionException, 
                Exception;
    }

    private static ImmutableMap<Integer,Set<String>> getFields(String[] fieldVals) {
        ImmutableMap.Builder<Integer,Set<String>> fieldsb = ImmutableMap.builder();
        for (final String field : fieldVals) {
            final String[] parts = field.split(":");
            final String tag_s = parts[0];
            final Set<String> subs = Sets.newHashSet();
            for (int i = 1; i < parts.length; i++) {
                subs.add(parts[i]);
            }

            int tag;
            try {
                tag = TAG_DICTIONARY.tagForName(tag_s);
            } catch (IllegalArgumentException e) {
                try {
                    tag = Integer.parseInt(tag_s, 16);
                } catch (NumberFormatException e1) {
                    throw new IllegalArgumentException("not a valid ecat attribute tag: " + tag_s, e1);
                }
            }
            fieldsb.put(tag, subs);
        }
        return fieldsb.build();
    }

    public EcatDump(Context context, Request request, Response response) {
        super(context, request, response);

        if (!this.containsQueryVariable(EcatDump.SRC_ATTR)) {
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Please set the src parameter");
            env = null;
            return;
        }

        final Map<Integer,Set<String>> fields;
        try {
            fields = getFields(getQueryVariables(FIELD_PARAM));
        } catch (IllegalArgumentException e) {
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
            env = null;
            return;
        }

        this.env = new Env(this.getQueryVariable(EcatDump.SRC_ATTR), fields);   

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));
         
    }

    
    
    
    
    
    
    public boolean allowPost() { return false; }
    public boolean allowPut() { return false; }

    /**
     * Enhance {@link CatEntryFilterI} to include a path
     * in its environment.
     * 
     * This is required because by the time we get down to 
     * iterating through the catalog entries we've lost
     * access to the absolute path to the resource. 
     * @author aditya
     *
     */
    static abstract class CatFilterWithPath implements CatEntryFilterI {
        String path;
        public void setPath (String path) {this.path = path;}
        public abstract boolean accept(CatEntryI entry);
    }

    final static CatFilterWithPath alwaysCatWithPath = new CatFilterWithPath() {
        { setPath(null); }
        public boolean accept(CatEntryI entry) { return true; }
    };

    public Representation represent(final Variant variant) {
        final MediaType mt = overrideVariant(variant);
        XFTTable t = new XFTTable();
        try {
        	//need extra param
        	String summary=this.getQueryVariable(EcatDump.SUMMARY_ATTR);
            if (SUMMARY_VALUE.equals(summary)){
            	Iterable<File> files = this.env.h.retrieveAll(this.env, this.getUser());
            	EcatSummaryHeaderDump d = new EcatSummaryHeaderDump(files, env.fields);
                t = d.render();
            }else{//default..
            	String file = this.env.h.retrieve(this.env, this.getUser());
                EcatHeaderDump d = new EcatHeaderDump(file, env.fields);
                t = d.render();
            }
        }
        catch (FileNotFoundException e){
            this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, e);
            return null;
        }
        catch (IOException e){
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
            return null;
        }
        catch (ClientException e) {
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
            return null;
        }
        catch (Throwable e) {
        	this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
            return null;
        }

        return this.representTable(t, mt, new Hashtable<String,Object>());
    }
}