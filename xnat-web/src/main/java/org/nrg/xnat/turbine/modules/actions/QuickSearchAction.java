/*
 * web: org.nrg.xnat.turbine.modules.actions.QuickSearchAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.modules.actions.DisplaySearchAction;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tim
 *
 */
public class QuickSearchAction extends SecureAction {
	private static final Logger logger = LoggerFactory.getLogger(QuickSearchAction.class);

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.actions.SearchA#setupSearch(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doPerform(RunData data, Context context) {
        preserveVariables(data,context);
        String searchValue = ((String) TurbineUtils.GetPassedParameter("searchValue",data));
        UserI user = XDAT.getUserDetails();
        if (searchValue==null || searchValue.equalsIgnoreCase(""))
        {
            data.setMessage("Please specify a search value.");
            data.setScreenTemplate("Index.vm");
        }else{
            searchValue = searchValue.toLowerCase().trim();
            logger.info("{} requested quick search for value: {}", user.getLogin(), searchValue);

            if(PoolDBUtils.HackCheck(searchValue))
            {
			    AdminUtils.sendAdminEmail(user,"Possible SQL Injection Attempt", "VALUE:" + searchValue);
            	this.error(new Exception("Illegal Search Value(" + searchValue +")"), data);
            	return;
            }
            
            if(searchValue.contains("'") || searchValue.contains("\\")){
                logger.info("{} requested quick search contained invalid character, terminating: {}", user.getLogin(), searchValue);
            	data.setMessage("Invalid character '");
                data.setScreenTemplate("Index.vm");
                return;
            }
            try {
            	String projectQ="SELECT DISTINCT id FROM xnat_projectData prj LEFT JOIN xnat_projectdata_alias ali ON prj.id=ali.aliases_alias_xnat_projectdata_id WHERE lower(id)='" + searchValue +"' OR  lower(secondary_id)='" + searchValue +"' OR  lower(name)='" + searchValue +"' OR  lower(alias)='" + searchValue +"' OR lower(keywords) LIKE '%" + searchValue +"%'";
                logger.info("Preparing to run quick search query for projects: {}", projectQ);
            	XFTTable table =TableSearch.Execute(projectQ, user.getDBName(), user.getLogin());
                secureTable(user, table, new String[]{"id"}, "xnat:projectData/ID");
            	if(table.size()>0){
            		if(table.size()==1){
                		Hashtable row = table.rowHashs().get(0);
                        String projectId = (String) row.get("id");
                        data.getParameters().setString("search_value", projectId);
                        data.getParameters().setString("search_element", "xnat:projectData");
                        data.getParameters().setString("search_field", "xnat:projectData.ID");
                        // Here we're just bouncing the user straight to the subjectData page for this one result
                        String rdirurl = String.format("%s/app/action/DisplayItemAction/search_value/%s/search_element/xnat:projectData/search_field/xnat:projectData.ID", data.getContextPath(), projectId);
                        data.setRedirectURI(rdirurl);
                        return;
            		}else{
            			DisplaySearch search = new DisplaySearch();
            			search.setDisplay("listing");
            			search.setRootElement("xnat:projectData");
            			CriteriaCollection cc = new CriteriaCollection("OR");
            			cc.addClause("xnat:projectData/ID", "=", searchValue);
            			cc.addClause("xnat:projectData/name", "=", searchValue);
            			cc.addClause("xnat:projectData/secondary_id", "=", searchValue);
            			cc.addClause("xnat:projectData/aliases/alias/alias", "=", searchValue);
            			cc.addClause("xnat:projectData/keywords", "LIKE", searchValue);
            			search.addCriteria(cc);
            			
            			XdatStoredSearch xss = search.convertToStoredSearch("");
            			            			
	            		DisplaySearchAction dsa = new DisplaySearchAction();
	            		
	            		data.getRequest().setAttribute("xss",xss);
	            		
	            		dsa.doPerform(data, context);
	            		return;
            		}
            	}
            	
                String query = "SELECT DISTINCT s.ID, 'xnat:subjectData' AS element_name, ids,s.project AS project,projects,s.label FROM xnat_subjectData s LEFT JOIN xnat_subjectdata_addid addid ON s.id=addid.xnat_subjectdata_id LEFT JOIN xnat_projectparticipant pp ON s.id=pp.subject_id LEFT JOIN (SELECT project_group.subject_id AS id, btrim(xs_a_concat(project_group.ids || ', '::text), ', '::text) AS ids, btrim(xs_a_concat(('<'::text || project_group.projects) || '>, '::text), ', '::text) AS projects  FROM ( SELECT ((COALESCE(xnat_projectparticipant.label, xnat_projectparticipant.subject_id)::text || ' ('::text) || btrim(xs_a_concat(xnat_projectparticipant.project::text || ', '::text), ', '::text)) || ')'::text AS ids, btrim(xs_a_concat(('<'::text || xnat_projectparticipant.project::text) || '>, '::text), ', '::text) AS projects, xnat_projectparticipant.subject_id FROM xnat_projectparticipant GROUP BY COALESCE(xnat_projectparticipant.label, xnat_projectparticipant.subject_id), xnat_projectparticipant.subject_id) project_group GROUP BY project_group.subject_id) xnat_projs ON s.id=xnat_projs.id WHERE LOWER(s.ID) = '"+ searchValue +"' OR  LOWER(addid) = '"+ searchValue +"' OR LOWER(s.label) = '"+ searchValue +"' OR LOWER(pp.label) = '"+ searchValue +"' UNION SELECT DISTINCT expt.ID,element_name,ids,expt.project,projects,expt.label FROM xnat_experimentData expt LEFT JOIN xnat_experimentData_share proj ON expt.id=proj.sharing_share_xnat_experimentda_id LEFT JOIN xdat_meta_element me ON expt.extension=me.xdat_meta_element_id LEFT JOIN ( SELECT project_group.sharing_share_xnat_experimentda_id AS id, btrim(xs_a_concat(project_group.ids || ', '::text), ', '::text) AS ids, btrim(xs_a_concat(project_group.projects || ', '::text), ', '::text) AS projects   FROM ( SELECT ((COALESCE(proj.label, sharing_share_xnat_experimentda_id)::text || ' ('::text) || btrim(xs_a_concat(proj.project::text || ', '::text), ', '::text)) || ')'::text AS ids, btrim(xs_a_concat(('<'::text || proj.project::text) || '>, '::text), ','::text) AS projects, sharing_share_xnat_experimentda_id           FROM xnat_experimentdata_share proj      LEFT JOIN xnat_experimentdata expt ON proj.sharing_share_xnat_experimentda_id::text = expt.id::text  GROUP BY COALESCE(proj.label, sharing_share_xnat_experimentda_id), sharing_share_xnat_experimentda_id) project_group  GROUP BY project_group.sharing_share_xnat_experimentda_id) xnat_projects ON expt.id=xnat_projects.id WHERE LOWER(expt.ID) = '"+ searchValue +"' OR LOWER(expt.label) = '"+ searchValue +"' OR LOWER(proj.label) = '"+ searchValue +"' ORDER BY element_name;";
                logger.info("Preparing to run quick search query for subjects: {}", query);

                table =TableSearch.Execute(query,user.getDBName(),user.getLogin());
                secureTable(user, table, new String[]{"project","projects"}, "xnat:subjectData/project");

                if (table.size()>0)
                {
                    if (table.size()==1)
                    {
                        Hashtable row = table.rowHashs().get(0);
                        String elementId = (String) row.get("id");
                        String elementName = (String) row.get("element_name");
                        data.getParameters().setString("search_value", elementId);
                        data.getParameters().setString("search_element", elementName);
                        data.getParameters().setString("search_field", elementName + ".ID");
                        // Here we're just bouncing the user straight to the subjectData page for this one result
                        String rdirurl = String.format("%s/app/action/DisplayItemAction/search_value/%s/search_element/%s/search_field/%s.ID", data.getContextPath(), elementId, elementName, elementName);
                        data.getResponse().sendRedirect(rdirurl);
                        return;
                    }
                }else{
                    query = "SELECT DISTINCT s.ID, 'xnat:subjectData' AS element_name, ids,s.project AS project,projects,s.label FROM xnat_subjectData s LEFT JOIN xnat_subjectdata_addid addid ON s.id=addid.xnat_subjectdata_id LEFT JOIN xnat_projectparticipant pp ON s.id=pp.subject_id LEFT JOIN (SELECT project_group.subject_id AS id, btrim(xs_a_concat(project_group.ids || ', '::text), ', '::text) AS ids, btrim(xs_a_concat(('<'::text || project_group.projects) || '>, '::text), ', '::text) AS projects  FROM ( SELECT ((COALESCE(xnat_projectparticipant.label, xnat_projectparticipant.subject_id)::text || ' ('::text) || btrim(xs_a_concat(xnat_projectparticipant.project::text || ', '::text), ', '::text)) || ')'::text AS ids, btrim(xs_a_concat(('<'::text || xnat_projectparticipant.project::text) || '>, '::text), ', '::text) AS projects, xnat_projectparticipant.subject_id FROM xnat_projectparticipant GROUP BY COALESCE(xnat_projectparticipant.label, xnat_projectparticipant.subject_id), xnat_projectparticipant.subject_id) project_group GROUP BY project_group.subject_id) xnat_projs ON s.id=xnat_projs.id WHERE LOWER(s.ID) LIKE '%"+ searchValue +"%' OR  LOWER(addid) LIKE '%"+ searchValue +"%' OR LOWER(s.label) LIKE '%"+ searchValue +"%' OR LOWER(pp.label) LIKE '%"+ searchValue +"%' UNION SELECT DISTINCT expt.ID,element_name,ids,expt.project,projects,expt.label FROM xnat_experimentData expt LEFT JOIN xnat_experimentData_share proj ON expt.id=proj.sharing_share_xnat_experimentda_id LEFT JOIN xdat_meta_element me ON expt.extension=me.xdat_meta_element_id LEFT JOIN ( SELECT project_group.sharing_share_xnat_experimentda_id AS id, btrim(xs_a_concat(project_group.ids || ', '::text), ', '::text) AS ids, btrim(xs_a_concat(project_group.projects || ', '::text), ', '::text) AS projects   FROM ( SELECT ((COALESCE(proj.label, sharing_share_xnat_experimentda_id)::text || ' ('::text) || btrim(xs_a_concat(proj.project::text || ', '::text), ', '::text)) || ')'::text AS ids, btrim(xs_a_concat(('<'::text || proj.project::text) || '>, '::text), ','::text) AS projects, sharing_share_xnat_experimentda_id           FROM xnat_experimentdata_share proj      LEFT JOIN xnat_experimentdata expt ON proj.sharing_share_xnat_experimentda_id::text = expt.id::text  GROUP BY COALESCE(proj.label, sharing_share_xnat_experimentda_id), sharing_share_xnat_experimentda_id) project_group  GROUP BY project_group.sharing_share_xnat_experimentda_id) xnat_projects ON expt.id=xnat_projects.id WHERE LOWER(expt.ID) LIKE '%"+ searchValue +"%' OR LOWER(expt.label) LIKE '%"+ searchValue +"%' OR LOWER(proj.label) LIKE '%"+ searchValue +"%' ORDER BY element_name;";
                    logger.info("Preparing to run second quick search query for subjects: {}", query);

                    table =TableSearch.Execute(query,user.getDBName(),user.getLogin());
                    secureTable(user, table, new String[]{"project","projects"}, "xnat:subjectData/project");
                }
                
                int totalCount = 0;
                
                if (table.size()>0)
                {
                    if (table.size()==1)
                    {
                        Hashtable row = table.rowHashs().get(0);
                        String elementId = (String) row.get("id");
                        String elementName = (String) row.get("element_name");
                        data.getParameters().setString("search_value", elementId);
                        data.getParameters().setString("search_element", elementName);
                        data.getParameters().setString("search_field", elementName + ".ID");
                        // Here we're just bouncing the user straight to the subjectData page for this one result
                        String rdirurl = String.format("%s/app/action/DisplayItemAction/search_value/%s/search_element/%s/search_field/%s.ID", data.getContextPath(), elementId, elementName, elementName);
                        data.setRedirectURI(rdirurl);
                    }else{
                        Hashtable<String,Hashtable<String,ArrayList<ItemI>>> hash = new Hashtable<>();
                        Hashtable<String,ArrayList<String>> typeProjectMapping = new Hashtable<>();
                        Hashtable<String,String> elementNames = new Hashtable<>();
                        table.resetRowCursor();
                        
                        while (table.hasMoreRows()){
                            Object[] row = table.nextRow();
                            
                            String id = (String)row[0];
                            String element_name = (String)row[1];
                            String ids = (String)row[2];
                            String project = (String)row[3];
                            String projects = (String)row[4];
                            String label = (String)row[5];
                            
                            ArrayList<String> localProjects = new ArrayList<String>();
                            
                            XFTItem i = XFTItem.NewItem(element_name, user);
                            i.setProperty(element_name + "/ID",id);
                            
                            if (project!=null){
                                i.setProperty(element_name + "/project",project);
                            }
                            if (label!=null){
                                i.setProperty(element_name + "/label",label);
                            }
                            
                            
                            int projectCounter=0;
                            if (ids!=null)
                            while (ids.indexOf(")")!=-1){
                                String idSet = ids.substring(0,ids.indexOf(")"));
                                if (ids.length()>(ids.indexOf(")")+2)){
                                    ids = ids.substring(ids.indexOf(")")+2);
                                }else{
                                    ids="";
                                }
                                
                                String identifier = idSet.substring(0,idSet.indexOf(" ("));
                                idSet = idSet.substring(idSet.indexOf(" (")+2);
                                ArrayList<String> ps = XftStringUtils.CommaDelimitedStringToArrayList(idSet, true);
                                
                                for (String p : ps){
                                	if(Permissions.canRead(user,element_name + "/sharing/share/project", p)){
                                        i.setProperty(element_name + "/sharing/share[" + projectCounter +"]/project", p);
                                        localProjects.add(p);
                                        i.setProperty(element_name + "/sharing/share[" + projectCounter++ +"]/label", identifier);
                                  	}
                                }
                            }
                            
//                            if (!user.canRead(i)){
//                                i.setProperty(element_name + "/ID","*****");
//                                
//                                for (int j=0;j<projectCounter;j++){
//                                    i.setProperty(element_name + "/sharing/share[" + j +"]/identifier", "*****");
//                                }
//                            }
                            
                            if (Permissions.canRead(user, i)){
                                if (!hash.containsKey(element_name)){
                                    elementNames.put(element_name, ElementSecurity.GetPluralDescription(element_name));
                                    hash.put(element_name, new Hashtable<String,ArrayList<ItemI>>());
                                }
                                
                                if (!typeProjectMapping.containsKey(element_name)){
                                    typeProjectMapping.put(element_name, new ArrayList<String>());
                                }
                                
                                for (String p : localProjects){
                                    if (!typeProjectMapping.get(element_name).contains(p)){
                                        typeProjectMapping.get(element_name).add(p);
                                    }
                                    if (hash.get(element_name).get(p)==null){
                                        hash.get(element_name).put(p,new ArrayList<ItemI>());
                                        totalCount++;
                                    }
                                    hash.get(element_name).get(p).add(BaseElement.GetGeneratedItem(i));
                                }

                                if (hash.get(element_name).get(project)==null){
                                    hash.get(element_name).put(project,new ArrayList<ItemI>());
                                    totalCount++;
                                }
                                hash.get(element_name).get(project).add(BaseElement.GetGeneratedItem(i));


                            }
                        }

                        Map pMappings = UserHelper.getUserHelperService(user).getCachedItemValuesHash("xnat:projectData", null, false, "xnat:projectData/ID", "xnat:projectData/secondary_ID");
                        
                        context.put("matches", hash);
                        context.put("elementNames", elementNames);
                        int displayCount = 0;
                        for(Hashtable projects : hash.values()){
                            for(Object items : projects.values()){
                                displayCount += ((ArrayList)items).size();
                            }
                        }
                        context.put("searchTerm", searchValue);
                        context.put("matchCount", displayCount);
                        context.put("typeProjectMapping", typeProjectMapping);
                        context.put("pMappings", pMappings);
                        data.setScreenTemplate("QuickSearchMatch.vm");
                    }
                }else{
                    data.setMessage("No matching items found.");
                }
            } catch (ElementNotFoundException e1) {
                logger.error("",e1);
                data.setMessage(e1.getMessage());
            } catch (XFTInitException e1) {
                logger.error("",e1);
                data.setMessage(e1.getMessage());
            } catch (FieldNotFoundException e1) {
                logger.error("",e1);
                data.setMessage(e1.getMessage());
            } catch (Exception e1) {
                logger.error("",e1);
                data.setMessage(e1.getMessage());
            }
        }
    }

    /**
     * Remove rows that this user can't see
     * @param user
     * @param t
     * @param projColumns
     * @param xsiPath
     */
    private void secureTable(UserI user, XFTTable t, String[] projColumns, String xsiPath) {
        List<Integer> toRemove=Lists.newArrayList();

        try {
            if(t.size()>0){
                t.resetRowCursor();

                List<Integer> columns=Lists.newArrayList();
                for(String projColumn: projColumns){
                    Integer pC=t.getColumnIndex(projColumn);
                    if(pC!=null){
                        columns.add(pC);
                    }
                }

                for(int rowC=0;rowC<t.rows().size();rowC++){
                    boolean canRead=false;
                    for(Integer pC: columns){
                        String pId=(String)t.rows().get(rowC)[pC];
                        if(StringUtils.isNotBlank(pId)){
                            String[] projects=pId.split(", ");
                            for(String project:projects){
                                if(StringUtils.isNotBlank(project)){
                                    project=project.replace(">", "");
                                    project=project.replace("<", "");
                                    if(Permissions.canRead(user,xsiPath,project)){
                                        canRead=true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if(!canRead){
                        toRemove.add(rowC);
                    }
                }

                Collections.reverse(toRemove);

                for(Integer i:toRemove){
                    t.removeRow(i);
                }

            }
        } catch (Exception e) {
            logger.error("",e);
        }
    }

    public void doQuickview(RunData data,Context context)
    {
       
    }
}
