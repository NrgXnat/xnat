/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_data_management
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author timo
 *
 */
public class XDATScreen_data_management extends SecureScreen {
    static org.apache.log4j.Logger logger = Logger.getLogger(XDATScreen_data_management.class);

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
		try {
            String project = (String)TurbineUtils.GetPassedParameter("project",data);
            String prearchive_path= null;
            if (project==null){
                prearchive_path=ArcSpecManager.GetInstance().getGlobalPrearchivePath();
            }else{
                prearchive_path=ArcSpecManager.GetInstance().getPrearchivePathForProject(project);
                context.put("project", project);
            }
            context.put("prearchive_path", prearchive_path);
            
            UserI user =TurbineUtils.getUser(data);
            Long count =new Long(0);
            Map sm = new Hashtable();
            File dir = new File(prearchive_path);
            if (dir.exists())
            {
                File[] folders = dir.listFiles();
                for (int i=0;i<folders.length;i++){
                    if (folders[i].isDirectory()){
                        File[] files=folders[i].listFiles();
                        if (files.length==0){
                            folders[i].delete();
                        }else{
                            ArrayList folderSummary = new ArrayList();
                            folderSummary.add(folders[i].getName());
                            int readable = 0;
                            ArrayList subFolders = new ArrayList();
                            long last = System.currentTimeMillis();
                            for(int j=0;j<files.length;j++){
                                File f = files[j];
                                if (f.isDirectory()){                                        
                                    ArrayList al = new ArrayList();
                                    al.add(f.getName());
                                    subFolders.add(al);
                                    readable++;
                                }
                            }
                            if (readable>0){
                                folderSummary.add(subFolders);
                                Long orderBy=null;
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
                                    Date d = sdf.parse(folders[i].getName());
                                    orderBy = d.getTime();
                                    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("MM/dd/yyyy HH:mm:ss");
                                    folderSummary.add(formatter.format(d));
                                } catch (RuntimeException e) {
                                    logger.error("",e);
                                    folderSummary.add(folders[i].getName());
                                }
                                if (orderBy==null)
                                    orderBy = count++;
                                sm.put(orderBy, folderSummary);
                            }
                        }
                        
                        
                    }
                }
                
            }
            
            TreeMap sort = new TreeMap(sm);
            List values =new ArrayList();
            values.addAll(sort.values());
            Collections.reverse(values);
            context.put("folders",values);
            
            DisplaySearch search = UserHelper.getSearchHelperService().getSearchForUser(TurbineUtils.getUser(data),"wrk:workflowData","listing");
            search.addCriteria("wrk:workflowData.pipeline_name","=","Transfer");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE,-30);
            Date d=calendar.getTime();
            search.addCriteria("wrk:workflowData", "LAST_MODIFIED", ">", d);
                
                
                search.execute(new org.nrg.xdat.presentation.HTMLPresenter(TurbineUtils.GetContext(),false),TurbineUtils.getUser(data).getLogin());
                
                TurbineUtils.setSearch(data,search);
                
                XFTTableI table = search.getPresentedTable();

                Hashtable tableProps = new Hashtable();
                tableProps.put("bgColor","white"); 
                tableProps.put("border","0"); 
                tableProps.put("cellPadding","0"); 
                tableProps.put("cellSpacing","0"); 
                tableProps.put("width","95%"); 
                
                context.put("dataTable",table.toHTML(false,"FFFFFF","DEDEDE",tableProps,(search.getCurrentPageNum() * search.getRowsPerPage())+ 1));
            
			context.put("prearchive",prearchive_path);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
