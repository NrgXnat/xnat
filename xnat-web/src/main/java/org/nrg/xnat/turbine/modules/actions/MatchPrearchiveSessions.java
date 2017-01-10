/*
 * web: org.nrg.xnat.turbine.modules.actions.MatchPrearchiveSessions
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.BatchTransfer;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.io.File;

public class MatchPrearchiveSessions extends SecureAction {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocitySecureAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        Integer num= TurbineUtils.GetPassedInteger("num_sessions", data);
        BatchTransfer bt = new BatchTransfer(TurbineUtils.GetFullServerPath(), TurbineUtils.GetSystemName(), XDAT.getSiteConfigPreferences().getAdminEmail());
        UserI user =TurbineUtils.getUser(data);
        bt.setUser(user);
        String project = (String)TurbineUtils.GetPassedParameter("project", data);
        if (num !=null)
        {
            for (int h = 0; h <= num; h++)
            {
                String sessionFolder = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("session" + h,data));
                String match = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("match" + h,data));
                
                if (sessionFolder !=null && !sessionFolder.equals("") && match!=null && !match.equals(""))
                {
                    String timestampName= sessionFolder.substring(0,sessionFolder.indexOf("/"));
                    String sessionName= sessionFolder.substring(sessionFolder.indexOf("/")+1);
                    
                    
                    String prearchive_path= ArcSpecManager.GetInstance().getPrearchivePathForProject(project);
                    
                    if (!prearchive_path.endsWith(File.separator)){
                        prearchive_path += File.separator;
                    }
                    
                    File dir = new File(prearchive_path);
                    
                    if (dir.exists())
                    {
                        //PREARCHIVE ROOT
                        File[] timestamps = dir.listFiles();
                        for (int i=0;i<timestamps.length;i++){
                            if (timestamps[i].isDirectory() && timestamps[i].getName().equals(timestampName)){
                                //TIMESTAMP FOLDER
                                File[] sessions=timestamps[i].listFiles();
                                if (sessions.length==0){
                                }else{
                                    for(int j=0;j<sessions.length;j++)
                                    {
                                        if (sessions[j].isDirectory() && sessions[j].getName().equals(sessionName))
                                        {
                                            File session = sessions[j];
                                            
                                            File xml = new File(session.getAbsolutePath() + ".xml");
                                            File txt = new File(session.getAbsolutePath() + ".txt");
                                            if (xml.exists() && !txt.exists())
                                            {
                                                SAXReader reader = new SAXReader(TurbineUtils.getUser(data));
                                                XFTItem item = reader.parse(xml.getAbsolutePath());

                                                XnatImagesessiondata mr = XnatImagesessiondata.getXnatImagesessiondatasById(match, user, false);
                                                
                                                int k = 0;
                                                while(TurbineUtils.HasPassedParameter("match" + h + ".scan" + k + "_id", data)){
                                                    String id = (String)TurbineUtils.GetPassedParameter("match" + h + ".scan" + k + "_id", data);
                                                    String use = (String)TurbineUtils.GetPassedParameter("match" + h + ".scan" + k + "_use", data);
                                                    String t = (String)TurbineUtils.GetPassedParameter("match" + h + ".scan" + k + "_type", data);
                                                    
                                                    XnatImagescandata scan = mr.getScanById(id);
                                                    if (scan==null){
                                                        XnatImagesessiondata tempMR= (XnatImagesessiondata)BaseElement.GetGeneratedItem(item);
                                                        scan = tempMR.getScanById(id);
                                                        if (scan!=null){
                                                            mr.setScans_scan(scan);
                                                        }
                                                    }
                                                    if (use!=null && !use.equals("")){
                                                        scan.setQuality(use);
                                                    }
                                                    if (t!=null && !t.equals("")){
                                                        scan.setType(t);
                                                    }
                                                    k++;
                                                }
                                                
                                                txt.createNewFile();
                                                bt.addSession(mr, session);
                                                
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        
        if (bt.count()>0)
        {
            System.out.println("Starting Batch Transfer Thread.");
            Thread thread = new Thread(bt);
            thread.start();
            
            redirectToScreen("BatchMatchProcessing.vm", data);
        }else{
            redirectToScreen("PrearchiveMatch.vm",data);
        }
    }

}
