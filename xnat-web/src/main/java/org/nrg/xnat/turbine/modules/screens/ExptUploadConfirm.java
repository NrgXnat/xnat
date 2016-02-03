/*
 * org.nrg.xnat.turbine.modules.screens.ExptUploadConfirm
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ExptUploadConfirm extends SecureReport {
    static org.apache.log4j.Logger logger = Logger.getLogger(ExptUploadConfirm.class);

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @Override
    public void finalProcessing(RunData data, Context context) {
        String cache_path = ArcSpecManager.GetInstance().getGlobalCachePath();
        if (!cache_path.endsWith(File.separator)){
            cache_path += File.separator;
        }                    

        String uploadID= ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("uploadID",data));
        context.put("uploadID", uploadID);
        cache_path +="user_uploads" + File.separator + uploadID + File.separator;
        File dir = new File(cache_path);
        context.put("directory", dir);

        
        CatCatalogBean cat = null;
        
        if (dir.exists())
        {
            int counter=0;
            
            File[] listFiles = dir.listFiles();
            for (int i=0;i<listFiles.length;i++)
            {
                if(!listFiles[i].isDirectory())
                {
                    if (listFiles[i].getName().endsWith(".xml"))
                    {
                        File xml = listFiles[i];
                        if (xml.exists())
                        {
                            try {
                                FileInputStream fis = new FileInputStream(xml);
                                XDATXMLReader reader = new XDATXMLReader();
                                BaseElement base = reader.parse(fis);

                                
                                if (base instanceof CatCatalogBean){            						
                                    counter++;
                                }
                            } catch (FileNotFoundException e) {
                                logger.error("",e);
                            } catch (IOException e) {
                                logger.error("",e);
                            } catch (SAXException e) {
                                logger.error("",e);
                            }
                        }
                    }
                }else{
                    for (int j=0;j<listFiles[i].listFiles().length;j++)
                    {
                        if(!listFiles[i].listFiles()[j].isDirectory())
                        {
                            if (listFiles[i].listFiles()[j].getName().endsWith(".xml"))
                            {
                                File xml = listFiles[i].listFiles()[j];
                                if (xml.exists())
                                {
                                    try {
                                        FileInputStream fis = new FileInputStream(xml);
                                        XDATXMLReader reader = new XDATXMLReader();
                                        BaseElement base = reader.parse(fis);

                                        
                                        if (base instanceof CatCatalogBean){
                                        	cat = (CatCatalogBean)base;
                                            counter++;
                                        }
                                    } catch (FileNotFoundException e) {
                                        logger.error("",e);
                                    } catch (IOException e) {
                                        logger.error("",e);
                                    } catch (SAXException e) {
                                        logger.error("",e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            context.put("catalogCount", counter);
        }
        
        
    }

}
