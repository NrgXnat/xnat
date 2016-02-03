/*
 * org.nrg.xnat.turbine.modules.actions.CacheImageData
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.actions;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.io.File;

/**
 * @author timo
 *
 */
public class CacheImageData extends SecureAction {
    private final static String PREARC_PAGE = "XDATScreen_prearchives.vm";

    private final Logger logger = Logger.getLogger(CacheImageData.class);

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doPerform(RunData data, Context context) throws Exception {
        final String folder = (String)TurbineUtils.GetPassedParameter("folder",data);
        final String investigator = (String)TurbineUtils.GetPassedParameter("investigator",data);
        String root = (String)TurbineUtils.GetPassedParameter("root",data);
        if (null != folder && null != root)
        {
            final String prearchive_path;
            
            if (data.getParameters().containsKey("project")) {
                final String project = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data));
                String path = null;
                try {
                    path = ArcSpecManager.GetInstance().getPrearchivePathForProject(project);
                } catch (Throwable e) {
                    logger.error(e);
                }
                prearchive_path = path;
            } else
              prearchive_path = null;
            
            context.put("investigator",investigator);
            
            
            String mvFolder = null;
            
            File dir = new File(prearchive_path);
            
            if (dir.exists())
            {
                if (!root.equals("NONE")){
                    String[] folders = dir.list();
                    for (int i=0;i<folders.length;i++){
                        if (folders[i].equals(root))
                        {
                            root = folders[i];
                            break;
                        }
                    }
                }
            }
            
            if (root.equals("NONE")){
                if (dir.exists())
                {
                    String[] folders = dir.list();
                    for (int i=0;i<folders.length;i++){
                        if (folders[i].equals(folder))
                        {
                            mvFolder =prearchive_path + folders[i];
                            break;
                        }
                    }
                }
            }else{
                dir = new File(prearchive_path + root);
                if (dir.exists())
                {
                    String[] folders = dir.list();
                    for (int i=0;i<folders.length;i++){
                        if (folders[i].equals(folder))
                        {
                            mvFolder =prearchive_path + root + File.separator + folders[i];
                            break;
                        }
                    }
                }
            }
            
            
            if (mvFolder ==null)
            {
                data.setMessage("Unknown folder: " + folder);
                data.getParameters().setString("investigator",investigator);
                data.setScreenTemplate(PREARC_PAGE);
                return;
            }else{
                String cache_path = ArcSpecManager.GetInstance().getGlobalCachePath();
                if (!cache_path.endsWith(File.separator)){
                    cache_path += File.separator;
                }
                if (root.equals("NONE"))
                    root = "SCANNER";
                File temp = new File(cache_path + root);
                if (!temp.exists()){
                    temp.mkdir();
                }
                File f = new File(mvFolder);
                File parent = f.getParentFile();
                if (f.exists())
                {
                    try {
                        FileUtils.MoveDir(f,new File(cache_path + root + File.separator + folder),true);
                        data.setMessage("Folder Removed: " + folder);
                                                
                        mvFolder+=".xml";
                        File prearcXML = new File(mvFolder);
                        if(prearcXML.exists()){
                            String cachePath =cache_path + root + File.separator + folder+ File.separator + folder + ".xml";
                            
                            File cacheXML=new File(cachePath);
                            org.nrg.xft.utils.FileUtils.MoveFile(prearcXML, cacheXML, true);
                        }
                        
                        if (f.exists())
                        {
                            f.delete();
                        }
                        
                        if (parent.exists())
                        {
                            if (parent.list().length==0)
                            {
                                parent.delete();
                            }
                        }
                        
                        data.getParameters().setString("investigator",investigator);
                        data.setScreenTemplate(PREARC_PAGE);
                        return;
                    } catch (Exception e) {
                        data.setMessage("ERROR: " + e.getMessage());
                        data.getParameters().setString("investigator",investigator);
                        data.setScreenTemplate(PREARC_PAGE);
                        return;
                    }
                }else{
                    data.setMessage("Unknown folder: " + folder);
                    data.getParameters().setString("investigator",investigator);
                    data.setScreenTemplate(PREARC_PAGE);
                    return;
                }
            }
        }else{
            data.setMessage("Unknown folder: " + folder);
            data.getParameters().setString("investigator",investigator);
            data.setScreenTemplate(PREARC_PAGE);
            return;
        }
    }

}
