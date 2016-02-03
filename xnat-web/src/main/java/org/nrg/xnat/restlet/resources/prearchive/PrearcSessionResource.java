/*
 * org.nrg.xnat.restlet.resources.prearchive.PrearcSessionResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/21/14 9:53 AM
 */
package org.nrg.xnat.restlet.resources.prearchive;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.turbine.util.TurbineException;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xnat.archive.FinishImageUpload;
import org.nrg.xnat.helpers.prearchive.*;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase.SyncFailedException;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.restlet.actions.PrearcImporterA.PrearcSession;
import org.nrg.xnat.restlet.representations.StandardTurbineScreen;
import org.nrg.xnat.restlet.representations.ZipRepresentation;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public final class PrearcSessionResource extends SecureResource {
    private static final String PROJECT_ATTR = "PROJECT_ID";
    private static final String SESSION_TIMESTAMP = "SESSION_TIMESTAMP";
    private static final String SESSION_LABEL = "SESSION_LABEL";
    private static final String VISIT = "VISIT";
    private static final String PROTOCOL = "PROTOCOL";
    private static final String TIMEZONE = "TIMEZONE";
    private static final String SOURCE = "SOURCE";


    public static final String POST_ACTION_SET = "set-status";
    public static final String POST_ACTION_RESET = "reset-status";
    public static final String POST_ACTION_BUILD = "build";
    public static final String POST_ACTION_MOVE = "move";
    public static final String POST_ACTION_COMMIT = "commit";

    private final Logger logger = LoggerFactory.getLogger(PrearcSessionResource.class);

    private final String project, timestamp, session;

    /**
     * @param context
     * @param request
     * @param response
     */
    public PrearcSessionResource(Context context, Request request,
            Response response) {
        super(context, request, response);

        // Project, timestamp, session are explicit in the request
        final String p = (String)getParameter(request,PROJECT_ATTR);
        project = p.equalsIgnoreCase(PrearcUtils.COMMON) ? null : p;
        timestamp = (String)getParameter(request,SESSION_TIMESTAMP);
        session = (String)getParameter(request,SESSION_LABEL);
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.APPLICATION_ZIP));
        getVariants().add(new Variant(MediaType.APPLICATION_GNU_ZIP));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    @Override
    public final boolean allowPost() { return true; }

    @Override
    public final boolean allowDelete() { return true; }

    private final Map<String,Object> params= Maps.newHashMap();

    String action=null;

    @Override
    public void handleParam(final String key, final Object value) throws ClientException {
        if ("action".equals(key)) {
            action=(String)value;
        } else {
            params.put(key,value);
        }
    }

    @Override
    public void handlePost(){
        try {
            loadBodyVariables();
            loadQueryVariables();
            
            final Representation entity=this.getRequest().getEntity();
            if(entity!=null){
                final String json = entity.getText();
                if (!Strings.isNullOrEmpty(json)) {
                    loadParams(json);
                }
            }
        } catch (ClientException e1) {
            logger.error("",e1);
            this.getResponse().setStatus(e1.getStatus(), e1);
        } catch (IOException e) {
            logger.error("", e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
        }

        final File sessionDir;
        try {
            sessionDir = PrearcUtils.getPrearcSessionDir(user, project, timestamp, session,false);
        } catch (InvalidPermissionException e) {
            logger.error("",e);
            this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, e.getMessage());
            return;
        } catch (Exception e) {
            logger.error("",e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
            return;
        }



        if (POST_ACTION_BUILD.equals(action)) {
            try {
                if (PrearcDatabase.setStatus(session, timestamp, project, PrearcUtils.PrearcStatus.BUILDING)) {
                    PrearcDatabase.buildSession(sessionDir, session, timestamp, project, (String) params.get(VISIT), (String) params.get(PROTOCOL), (String) params.get(TIMEZONE), (String) params.get(SOURCE));
                    PrearcUtils.resetStatus(user, project, timestamp, session,true);
                    returnString(wrapPartialDataURI(PrearcUtils.buildURI(project,timestamp,session)), MediaType.TEXT_URI_LIST,Status.SUCCESS_OK);
                } else {
                    this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "session document locked");
                }
            } catch (InvalidPermissionException e) {
                logger.error("",e);
                PrearcUtils.log(project, timestamp, session, e);
                this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, e.getMessage());
            } catch (Exception e) {
                logger.error("",e);
                PrearcUtils.log(project, timestamp, session, e);
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
            }
        } else if (POST_ACTION_RESET.equals(action)) {
            try {
                final String tag= getQueryVariable("tag");
                PrearcUtils.resetStatus(user, project, timestamp, session,tag,true);
                returnString(wrapPartialDataURI(PrearcUtils.buildURI(project,timestamp,session)), MediaType.TEXT_URI_LIST,Status.SUCCESS_OK);
            } catch (InvalidPermissionException e) {
                logger.error("",e);
                PrearcUtils.log(project, timestamp, session, e);
                this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, e.getMessage());
            } catch (Exception e) {
                logger.error("",e);
                PrearcUtils.log(project, timestamp, session, e);
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
            }
        } else if (POST_ACTION_MOVE.equals(action)) {
            String newProj=this.getQueryVariable("newProject");

            if(StringUtils.isNotEmpty(newProj)){
                //TODO: convert ALIAS to project ID (if necessary)
            }

            try {
                if(!PrearcUtils.canModify(user, newProj)){
                    this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Unable to modify session data for destination project.");
                    return;
                }
                if(PrearcDatabase.setStatus(session, timestamp, project, PrearcStatus.MOVING)){
                    PrearcDatabase.moveToProject(session, timestamp, (project==null)?"Unassigned":project, newProj);
                    returnString(wrapPartialDataURI(PrearcUtils.buildURI(newProj,timestamp,session)), MediaType.TEXT_URI_LIST,Status.REDIRECTION_PERMANENT);
                }				
            } catch (SyncFailedException e) {
                logger.error("",e);
                PrearcUtils.log(project, timestamp, session, e);
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
            } catch (SQLException e) {
                logger.error("",e);
                PrearcUtils.log(project, timestamp, session, e);
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
            } catch (SessionException e) {
                logger.error("",e);
                PrearcUtils.log(project, timestamp, session, e);
                this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
            } catch (Exception e) {
                logger.error("",e);
                PrearcUtils.log(project, timestamp, session, e);
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
            }			
        }else if (POST_ACTION_COMMIT.equals(action)) {
            try {
                if (PrearcDatabase.setStatus(session, timestamp, project, PrearcUtils.PrearcStatus.BUILDING)) {
                    final SessionData sd = PrearcDatabase.getSession(session, timestamp, project);
                    if (null == sd.getAutoArchive() && !Strings.isNullOrEmpty(project)) {
                        final XnatProjectdata p = XnatProjectdata.getProjectByIDorAlias(project, user, false);
                        PrearcDatabase.setAutoArchive(session, timestamp, project, PrearchiveCode.code(p.getArcSpecification().getPrearchiveCode()));
                    }
                    PrearcDatabase.buildSession(sessionDir, session, timestamp, project, (String) params.get(VISIT), (String) params.get(PROTOCOL), (String) params.get(TIMEZONE), (String) params.get(SOURCE));
                    PrearcUtils.resetStatus(user, project, timestamp, session,true);

                    final FinishImageUpload uploader=new FinishImageUpload(null, user, new PrearcSession(project,timestamp,session,params,user), null, false, true, false);
                    try {
                        if(uploader.isAutoArchive()){
                            returnString(wrapPartialDataURI(uploader.call()),Status.REDIRECTION_PERMANENT);
                        }else{
                            returnString(wrapPartialDataURI(uploader.call()), MediaType.TEXT_URI_LIST,Status.SUCCESS_OK);
                        }
                    } finally {
                        uploader.close();
                    }
                } else {
                    this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "session document locked");
                }
            } catch (ActionException e) {
                logger.error("",e);
                setResponseStatus(e);
            } catch (InvalidPermissionException e) {
                logger.error("",e);
                this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, e.getMessage());
            } catch (SyncFailedException e) {
                logger.error("",e);
            	if(e.getCause()!=null && e.getCause() instanceof ActionException){
            		setResponseStatus((ActionException)e.getCause());
            	}else{
                    this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
            	}
            } catch (Exception e) {
                logger.error("",e);
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
            }
        }  else if (POST_ACTION_SET.equals(action)){
            try {
                PrearcDatabase.setStatus(session,timestamp,project,(String)this.params.get("status"));
            }
            catch (Exception e) {
                this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
            }
        } else {
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
                    "unsupported action on prearchive session: " + action);
        }
    }

    @Override
    public void handleDelete() {
    	if(StringUtils.isNotEmpty(filepath)){
    		this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "");
    		return;
    	}
    	
        try {
            //checks if the user can access this session
            PrearcUtils.getPrearcSessionDir(user, project, timestamp, session,false);
        } catch (InvalidPermissionException e) {
            logger.error("",e);
            this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, e.getMessage());
            return;
        } catch (Exception e) {
            logger.error("",e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
            return;
        }

        try {
            if(!PrearcUtils.canModify(user, project)){
                this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Unable to modify session data for destination project.");
                return;
            }

            PrearcDatabase.deleteSession(session, timestamp, project);
        } catch (SessionException e) {
            logger.error("",e);
            this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, e.getMessage());
            return;
        } catch (Exception e) {
            logger.error("",e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
            return;
        }
    }


    /*
     * (non-Javadoc)
     * @see org.restlet.resource.Resource#represent(org.restlet.resource.Variant)
     */
    @SuppressWarnings("serial")
    @Override
    public Representation getRepresentation(final Variant variant){
        final File sessionDir;
        try {
            sessionDir = PrearcUtils.getPrearcSessionDir(user, project, timestamp, session,false);
        } catch (InvalidPermissionException e) {
            this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, e.getMessage());
            return null;
        } catch (Exception e) {
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
            return null;
        }

        String screen=this.getQueryVariable("screen");
        String popup=StringUtils.equalsIgnoreCase(this.getQueryVariable("popup"), "true") ? "true":"false";
        
        final MediaType mt = overrideVariant(variant);
        
        //add GET support for log files
        if(StringUtils.isNotEmpty(filepath)){
        	if(filepath.startsWith("logs/") && filepath.length()>5){
        		final String logId=filepath.substring(5);
        		
        		final String contents;
        		if(logId.equals("last")){
        			contents=PrearcUtils.getLastLog(project, timestamp, session);
        		}else{
        			contents=PrearcUtils.getLog(project, timestamp, session,logId);
        		}
        		
        		return new StringRepresentation(contents, mt);
        	}else if(filepath.equals("logs")){
        		final XFTTable tb=new XFTTable();
        		if(this.getQueryVariable("template")==null || this.getQueryVariable("template").equals("details")){
            		tb.initTable(new String[]{"id","date","entry"});
            		
            		try {
						for(File log:PrearcUtils.getLogs(project,timestamp,session)){
							Date timestamp=new Date(log.lastModified());
							String id=log.getName().substring(0,log.getName().indexOf(".log"));
							tb.insertRow(new Object[]{id,timestamp,FileUtils.readFileToString(log)});
						}
                        tb.sort("date","ASC");
						tb.resetRowCursor();
					} catch (IOException e) {
						this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
		                return null;
					}
        		}else{
        			tb.initTable(new String[]{"id"});
            		
            		for(String id:PrearcUtils.getLogIds(project,timestamp,session)){
            			tb.rows().add(new Object[]{id});
            		}
        		}
        		
        		
        		return representTable(tb,mt,new Hashtable<String,Object>());
        	}
        }
        
        if (MediaType.TEXT_HTML.equals(mt) || StringUtils.isNotEmpty(screen)) 
        {
            // Return the session XML, if it exists

            if(StringUtils.isEmpty(screen)){
                screen="XDATScreen_brief_xnat_imageSessionData.vm";
            }else if (screen.equals("XDATScreen_uploaded_xnat_imageSessionData.vm")){
                if(project==null){
                    this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Projects in the unassigned folder cannot be archived.");
                    return null;
                }
                getResponse().redirectSeeOther(getContextPath()+String.format("/app/action/LoadImageData/project/%s/timestamp/%s/folder/%s/popup/%s",project,timestamp,session,popup));
                return null;
            }

            try {
            	Map<String,Object> params=Maps.newHashMap();
            	for(String key: this.getQueryVariableKeys()){
            		params.put(key, this.getQueryVariable(key));
            	}
            	params.put("project",project);
            	params.put("timestamp",timestamp);
            	params.put("folder",session);
                return new StandardTurbineScreen(MediaType.TEXT_HTML, getRequest(), user, screen, params);
            } catch (TurbineException e) {
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
                return null;
            }

        }else if (MediaType.TEXT_XML.equals(mt)) {
            // Return the session XML, if it exists
            final File sessionXML = new File(sessionDir.getPath() + ".xml");
            if (!sessionXML.isFile()) {
                this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,	"The named session exists, but its XNAT session document is not available. The session is likely invalid or incomplete.");
                return null;
            }
            return new FileRepresentation(sessionXML, variant.getMediaType(), 0);
        } else if (MediaType.APPLICATION_JSON.equals(mt)) {
            List<SessionDataTriple> l = new ArrayList<SessionDataTriple>();
            l.add(new SessionDataTriple().setFolderName(sessionDir.getName()).setProject(project).setTimestamp(timestamp));
            XFTTable table = null;
            try {
                table = PrearcUtils.convertArrayLtoTable(PrearcDatabase.buildRows(l));
            } catch (Exception e) {
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
            } 
            return this.representTable(table, MediaType.APPLICATION_JSON, new Hashtable<String,Object>());
        } 
        else if (MediaType.APPLICATION_GNU_ZIP.equals(mt) || MediaType.APPLICATION_ZIP.equals(mt)) {
            final ZipRepresentation zip;
        	try{
	        	zip = new ZipRepresentation(mt, sessionDir.getName(),identifyCompression(null));
			} catch (ActionException e) {
				logger.error("",e);
				this.setResponseStatus(e);
				return null;
			}
            zip.addFolder(sessionDir.getName(), sessionDir);
            return zip;
        } else {
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Requested type " + mt + " is not supported");
            return null;
        }
    }
}
