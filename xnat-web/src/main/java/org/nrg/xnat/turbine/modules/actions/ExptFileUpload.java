/*
 * web: org.nrg.xnat.turbine.modules.actions.ExptFileUpload
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatCatalogTagBean;
import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xdat.model.CatCatalogTagI;
import org.nrg.xdat.om.XnatAbstractresourceTag;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.zip.TarUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.turbine.utils.XNATUtils;
import org.nrg.xnat.utils.WorkflowUtils;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Calendar;
import java.util.zip.ZipOutputStream;

public class ExptFileUpload extends SecureAction {

    private static final Logger logger = Logger.getLogger(ExptFileUpload.class);

    @Override
    public void doPerform(RunData data, Context context) throws Exception{        
        ParameterParser params = data.getParameters();
        HttpSession session = data.getSession();
        String uploadID= null;
        if (params.get("ID")!=null && !params.get("ID").equals("")){
            uploadID=params.get("ID");
            session.setAttribute(uploadID + "Upload", 0);
            session.setAttribute(uploadID + "Extract", 0);
            session.setAttribute(uploadID + "Analyze", 0);
        }
        if (uploadID!=null)session.setAttribute(uploadID + "Upload", 0);
            try {
                FileItem fi = params.getFileItem("image_archive");
                if (fi != null) {
                    String cache_path = ArcSpecManager.GetInstance().getGlobalCachePath();

                    if (!cache_path.endsWith(File.separator)) {
                        cache_path += File.separator;
                    }                    
                    
                    cache_path +="user_uploads" + File.separator + uploadID + File.separator;
                    File dir = new File(cache_path);
                    
                    if (!dir.exists()){
                        if (!dir.mkdirs()) {
                            logger.warn("It appears that I failed to create the directory: " + dir.getAbsolutePath() + ". If there's some error later, this may be why.");
                        }
                    }

                    String filename = fi.getName();

                    int index = filename.lastIndexOf('\\');
                    if (index < filename.lastIndexOf('/')) {
                        index = filename.lastIndexOf('/');
                    }
                    if (index > 0) {
                        filename = filename.substring(index+1);
                    }

                    if (logger.isInfoEnabled()) {
                        logger.info("Uploading file " + filename + " to folder " + dir.getAbsolutePath());
                    }

                    String compression_method = ".zip";
                    final String normalized = filename.toLowerCase();
                    if (normalized.endsWith(".tar")) {
                        compression_method = ".tar";
                    } else if (normalized.endsWith(".tgz") || normalized.endsWith(".tar.gz")) {
                        compression_method = ".tgz";
                    } else if (filename.contains(".")) {
                        compression_method = filename.substring(filename.lastIndexOf("."));
                    }

                    if (uploadID != null) {
                        session.setAttribute(uploadID + "Upload", 100);
                    }

                    if (compression_method.equalsIgnoreCase(".tar") ||
                        compression_method.equalsIgnoreCase(".gz") ||
                        compression_method.equalsIgnoreCase(".tgz") ||
                        compression_method.equalsIgnoreCase(".zip") ||
                        compression_method.equalsIgnoreCase(".zar")) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("Extracting file: " + filename);
                        }

                        InputStream is = fi.getInputStream();

                        ZipI zipper;
                        if (compression_method.equalsIgnoreCase(".tar")) {
                            zipper = new TarUtils();
                        } else if (compression_method.equalsIgnoreCase(".tgz")) {
                            zipper = new TarUtils();
                            zipper.setCompressionMethod(ZipOutputStream.DEFLATED);
                        } else {
                            zipper = new ZipUtils();
                        }
                        
                        try {
                            zipper.extract(is, cache_path);
                        } catch (Throwable e1) {
                            error(e1,data);
                            session.setAttribute(uploadID + "Extract", -1);
                            session.setAttribute(uploadID + "Analyze", -1);
                            return;
                        }
                    } else {
                        //PLACE UPLOADED IMAGE INTO FOLDER
                        File uploaded = new File(cache_path + filename) ;
                        fi.write(uploaded);
                    }

                    if (uploadID != null) {
                        session.setAttribute(uploadID + "Extract", 100);
                    }

                    fi.delete();

                    String tag = (String)TurbineUtils.GetPassedParameter("tags", data);
                    this.addTag(dir, tag);
                    
                    logger.debug("File Upload Complete.");

                    data.setMessage("File Uploaded.");
                    context.put("search_element", TurbineUtils.GetPassedParameter("search_element",data));
                    context.put("search_field", TurbineUtils.GetPassedParameter("search_field",data));
                    context.put("search_value", TurbineUtils.GetPassedParameter("search_value",data));
                    context.put("uploadID",uploadID);
                    context.put("destination","ExptUploadConfirm.vm");
                    data.setScreenTemplate("FileUploadSummary.vm");
                }
            } catch (FileNotFoundException e) {
                error(e,data);
                session.setAttribute(uploadID + "Upload", -1);
                session.setAttribute(uploadID + "Extract", -1);
                session.setAttribute(uploadID + "Analyze", -1);
            } catch (IOException e) {
                error(e,data);
                session.setAttribute(uploadID + "Upload", -1);
                session.setAttribute(uploadID + "Extract", -1);
                session.setAttribute(uploadID + "Analyze", -1);
            } catch (RuntimeException e) {
                error(e,data);
                session.setAttribute(uploadID + "Upload", -1);
                session.setAttribute(uploadID + "Extract", -1);
                session.setAttribute(uploadID + "Analyze", -1);
            }
    }
    
    public void addTag(File dir,String tag) {
        
        CatCatalogBean cat;

        if (dir.exists())
        {
            int counter=0;
            
            File[] listFiles = dir.listFiles();
            for (final File listFile : listFiles) {
                if (!listFile.isDirectory()) {
                    if (listFile.getName().endsWith(".xml")) {
                        if (listFile.exists()) {
                            try {
                                FileInputStream fis = new FileInputStream(listFile);
                                XDATXMLReader reader = new XDATXMLReader();
                                BaseElement base = reader.parse(fis);


                                if (base instanceof CatCatalogBean) {
                                    cat = (CatCatalogBean) base;

                                    CatCatalogTagBean tagBean = new CatCatalogTagBean();
                                    tagBean.setTag(tag);
                                    cat.addTags_tag(tagBean);

                                    FileWriter fw = new FileWriter(listFile);
                                    cat.toXML(fw, true);
                                    fw.close();

                                    counter++;
                                }
                            } catch (FileNotFoundException e) {
                                logger.error("", e);
                            } catch (IOException e) {
                                logger.error("", e);
                            } catch (SAXException e) {
                                logger.error("", e);
                            }
                        }
                    }
                } else {
                    for (int j = 0; j < listFile.listFiles().length; j++) {
                        if (!listFile.listFiles()[j].isDirectory()) {
                            if (listFile.listFiles()[j].getName().endsWith(".xml")) {
                                File xml = listFile.listFiles()[j];
                                if (xml.exists()) {
                                    try {
                                        FileInputStream fis = new FileInputStream(xml);
                                        XDATXMLReader reader = new XDATXMLReader();
                                        BaseElement base = reader.parse(fis);


                                        if (base instanceof CatCatalogBean) {
                                            counter++;
                                        }
                                    } catch (FileNotFoundException e) {
                                        logger.error("", e);
                                    } catch (IOException e) {
                                        logger.error("", e);
                                    } catch (SAXException e) {
                                        logger.error("", e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            
            if (counter==0){
            	cat = new CatCatalogBean();
            	if (dir.exists())
                {
                    for (File f: dir.listFiles())
                    {
                    	XNATUtils.populateCatalogBean(cat, "", f);
                    }
                    
                    if (tag != null){
                        CatCatalogTagBean tagBean = new CatCatalogTagBean();
                        tagBean.setTag(tag);
                        cat.addTags_tag(tagBean);
                    }
                    
                    try {
						File catF = new File(dir,"generated_catalog.xml");
						FileWriter fw = new FileWriter(catF);
						cat.toXML(fw, true);
						fw.close();
					} catch (IOException e) {
						logger.error("",e);
					}
                }
            }
        }
    }

    public void doFinalize(RunData data, Context context) throws Exception {
        ItemI temp = TurbineUtils.GetItemBySearch(data,false);
        XnatImagesessiondata tempMR = (XnatImagesessiondata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(temp);
        
        String uploadID= ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("uploadID",data));

        
        String cache_path = ArcSpecManager.GetInstance().getGlobalCachePath();
 
        
        cache_path +="user_uploads" + File.separator + uploadID + File.separator;
        File dir = new File(cache_path);
        if (dir.exists())
        {
            int counter=0; 

            String arcPath= tempMR.getRelativeArchivePath();
            arcPath= FileUtils.AppendSlash(arcPath);
            String destinationPath=arcPath + "/UPLOADS/" + uploadID + "/" ;
            
            File[] listFiles = dir.listFiles();
            for (final File listFile : listFiles) {
                if (!listFile.isDirectory()) {
                    if (listFile.getName().endsWith(".xml") || listFile.getName().endsWith(".xcat")) {
                        File xml = listFile;
                        if (xml.exists()) {
                            FileInputStream fis = new FileInputStream(xml);
                            XDATXMLReader reader = new XDATXMLReader();
                            try {
                                BaseElement base = reader.parse(fis);

                                if (base instanceof CatCatalogBean) {
                                    CatCatalogBean cBean = (CatCatalogBean) base;
                                    XnatResourcecatalog cat = new XnatResourcecatalog((UserI) TurbineUtils.getUser(data));

                                    if (cBean.getId() != null) {
                                        cat.setLabel(cBean.getId());
                                    } else {
                                        cat.setLabel(Calendar.getInstance().getTime().toString());
                                    }

                                    for (CatCatalogTagI tag : cBean.getTags_tag()) {
                                        XnatAbstractresourceTag t = new XnatAbstractresourceTag((UserI) TurbineUtils.getUser(data));

                                        t.setTag(tag.getTag());

                                        cat.setTags_tag(t);
                                    }

                                    cat.setUri(destinationPath + xml.getName());
                                    tempMR.setResources_resource(cat);
                                    counter++;
                                }
                            } catch (Throwable e) {
                                logger.error("", e);
                            }
                        }
                    }
                } else {
                    for (int j = 0; j < listFile.listFiles().length; j++) {
                        if (!listFile.listFiles()[j].isDirectory()) {
                            if (listFile.listFiles()[j].getName().endsWith(".xml") || listFile.getName().endsWith(".xcat")) {
                                File xml = listFile.listFiles()[j];
                                if (xml.exists()) {
                                    FileInputStream fis = new FileInputStream(xml);
                                    XDATXMLReader reader = new XDATXMLReader();
                                    try {
                                        BaseElement base = reader.parse(fis);

                                        if (base instanceof CatCatalogBean) {
                                            CatCatalogBean cBean = (CatCatalogBean) base;
                                            XnatResourcecatalog cat = new XnatResourcecatalog((UserI) TurbineUtils.getUser(data));

                                            if (cBean.getId() != null) {
                                                cat.setLabel(cBean.getId());
                                            } else {
                                                cat.setLabel(Calendar.getInstance().getTime().toString());
                                            }

                                            for (CatCatalogTagI tag : cBean.getTags_tag()) {
                                                XnatAbstractresourceTag t = new XnatAbstractresourceTag((UserI) TurbineUtils.getUser(data));

                                                t.setTag(tag.getTag());

                                                cat.setTags_tag(t);
                                            }

                                            cat.setUri(destinationPath + listFile.getName() + "/" + xml.getName());
                                            tempMR.setResources_resource(cat);
                                            counter++;
                                        }
                                    } catch (Throwable e) {
                                        logger.error("", e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (counter>0){
            	PersistentWorkflowI wrk=WorkflowUtils.buildOpenWorkflow(TurbineUtils.getUser(data), tempMR.getItem(), newEventInstance(data, EventUtils.CATEGORY.DATA, EventUtils.ADDED_MISC_FILES));
				EventMetaI c=wrk.buildEvent();

                try {
	                File dest = new File(FileUtils.AppendRootPath(tempMR.getArchiveRootPath(),destinationPath));
	                FileUtils.MoveDir(dir, dest, true);
	                FileUtils.DeleteFile(dir);
                
                	SaveItemHelper.authorizedSave(tempMR,TurbineUtils.getUser(data),false,false,c);                    
                	PersistentWorkflowUtils.complete(wrk, c);
                    data.setMessage("Files successfully uploaded.");
                } catch (Exception e) {
                    PersistentWorkflowUtils.fail(wrk, c);
                    error(e,data);
                }
                
                if (tempMR.getProject()!=null){
                    data.getParameters().setString("project", tempMR.getProject());
                }

                final CatalogService catalogService = XDAT.getContextService().getBean(CatalogService.class);
                catalogService.refreshResourceCatalog(XDAT.getUserDetails(), "/archive/experiments/" + tempMR.getId());

                if (TurbineUtils.HasPassedParameter("destination", data)){
                    this.redirectToReportScreen((String)TurbineUtils.GetPassedParameter("destination", data), tempMR.getItem(), data);
                }else{
                    this.redirectToReportScreen(tempMR.getItem(), data);
                }
            }
        }
    }
}
