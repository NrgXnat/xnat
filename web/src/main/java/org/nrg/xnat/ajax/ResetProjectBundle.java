/*
 * web: org.nrg.xnat.ajax.ResetProjectBundle
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.ajax;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.model.XnatFielddefinitiongroupFieldI;
import org.nrg.xdat.model.XnatFielddefinitiongroupI;
import org.nrg.xdat.om.XdatSearchField;
import org.nrg.xdat.om.XnatDatatypeprotocol;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;

public class ResetProjectBundle {
    private Logger logger = Logger.getLogger(ResetProjectBundle.class);

    public void execute(HttpServletRequest req, HttpServletResponse response,ServletConfig sc) throws IOException{
        String protocolID = req.getParameter("protocol");
        UserI user = XDAT.getUserDetails();
        if (user!=null){
            XnatDatatypeprotocol protocol = XnatDatatypeprotocol.getXnatDatatypeprotocolsByXnatAbstractprotocolId(protocolID, user, true);
            XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(protocol.getProject(), user, false);
            
            XdatStoredSearch xss = XdatStoredSearch.GetPreLoadedSearch(protocol.getId(), true);
            boolean modified=false;
            if(xss!=null){
            	for(XnatFielddefinitiongroupI group : protocol.getDefinitions_definition()){
                    for(XnatFielddefinitiongroupFieldI field : group.getFields_field()){
                    	                        String fieldID=null;
                        if (field.getType().equals("custom"))
                        {
                            fieldID=protocol.getDatatypeSchemaElement().getSQLName().toUpperCase() + "_FIELD_MAP="+field.getName().toLowerCase();
                        }else{
                            try {
                                SchemaElement se=SchemaElement.GetElement(protocol.getDataType());
                                
                                try {
                                    DisplayField df=se.getDisplayFieldForXMLPath(field.getXmlpath());
                                    if (df!=null){
                                        fieldID=df.getId();
                                    }
                                } catch (Exception e) {
                                    logger.error("",e);
                                }
                            } catch (XFTInitException e) {
                                logger.error("",e);
                            } catch (ElementNotFoundException e) {
                                logger.error("",e);
                            }
                        }
                        
                        if(xss.getField(protocol.getDataType(), fieldID)==null){
                        	XdatSearchField xsf = new XdatSearchField(protocol.getUser());
                            xsf.setElementName(protocol.getDataType());
                            if (fieldID!=null){
                                xsf.setFieldId(fieldID);

                                xsf.setHeader(field.getName());
                                xsf.setType(field.getDatatype());
                                xsf.setSequence(xss.getSearchField().size());
                                if (field.getType().equals("custom"))xsf.setValue(field.getName().toLowerCase());
                                try {
                                    xss.setSearchField(xsf);
                                	System.out.println("LOADED " + field.getXmlpath());
                                	modified=true;
                                } catch (Exception e) {
                                    logger.error("",e);
                                	System.out.println("FAILED to load " + field.getXmlpath());
                                }
                            }else{
                            	System.out.println("FAILED to load " + field.getXmlpath());
                            }
                        }
                    }
                }
            }
        	
        	if(xss!=null && modified){
                try {
                	SaveItemHelper.unauthorizedSave(xss,user, true, true,EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, EventUtils.TYPE.WEB_FORM, "Reset project searches"));
                    
                    //XdatStoredSearch.ReplacePreLoadedSearch(xss);
                    
                    final String[] groups = {project.getId() + "_" + XnatProjectdata.OWNER_GROUP,project.getId() + "_" + XnatProjectdata.MEMBER_GROUP,project.getId() + "_" + XnatProjectdata.COLLABORATOR_GROUP};

                    for(int i=0;i<groups.length;i++){
                        String group = groups[i];
                        UserGroupI g =Groups.getGroup(group);
                        if (g!=null && UserHelper.getSearchHelperService().getSearchesForGroup(g).size()>0)
                        {
                            UserHelper.getSearchHelperService().replacePreLoadedSearchForGroup(g, xss);
                        }
                    }
                    response.setContentType("text/plain");
                    response.getWriter().write("Bundle refreshed.");
                    return;
                } catch (Exception e) {
                    logger.error("",e);
                }
        	}else{
                response.setContentType("text/plain");
                response.getWriter().write("Bundle not modified.");
                return;
        	}
        }

        response.setContentType("text/plain");
        response.getWriter().write("Bundle refresh failed.");
    }
}
