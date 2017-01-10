/*
 * web: org.nrg.xnat.turbine.modules.screens.StoreXAR
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.screens.RawScreen;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.parser.ParameterParser;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatReconstructedimagedata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.zip.TarUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.WorkflowUtils;
import org.xml.sax.SAXException;

public class StoreXAR extends RawScreen {
    static org.apache.log4j.Logger logger = Logger.getLogger(ArcPut.class);
    UserI user=null;

    @Override
    public void doOutput(RunData data)  {
        final HttpServletResponse response = data.getResponse();
        response.setContentType("text/xml");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter pw = null;

        try{
            pw = response.getWriter();
            user = TurbineUtils.getUser(data);

            if (user==null){
                pw.println("<UploadResponse status=\"ERROR\" CODE=\"103\">");
                pw.println("<message>User Session Missing</message>");
                pw.println("</UploadResponse>");
                return;
            }

            final ParameterParser params = data.getParameters();

            final FileItem fi = params.getFileItem("archive");


            if (fi != null )
            {
                String filename = fi.getName();

                int index = filename.lastIndexOf('\\');
                if (index< filename.lastIndexOf('/'))index = filename.lastIndexOf('/');
                if(index>0)filename = filename.substring(index+1);

                String cachepath= ArcSpecManager.GetInstance().getGlobalCachePath();
                Date d = Calendar.getInstance().getTime();

                java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyyMMdd_HHmmss");
                String uploadID = formatter.format(d);

                cachepath+="user_uploads/"+user.getID() + "/" + uploadID + "/";

                File destination = new File(cachepath);
                final File original = new File(cachepath);

                if(!destination.exists()){
                    destination.mkdirs();
                }

                String compression_method = ".zip";
                if (filename.indexOf(".")!=-1){
                    compression_method = filename.substring(filename.lastIndexOf("."));
                }

                InputStream is = fi.getInputStream();
                System.out.println("Extracting file.");

                ZipI zipper = null;
                if (compression_method.equalsIgnoreCase(".tar")){
                    zipper = new TarUtils();
                }else if (compression_method.equalsIgnoreCase(".gz")){
                    zipper = new TarUtils();
                    zipper.setCompressionMethod(ZipOutputStream.DEFLATED);
                }else{
                    zipper = new ZipUtils();
                }

                try {
                    zipper.extract(is,cachepath);
                } catch (Throwable e1) {
                    pw.println("<UploadResponse status=\"ERROR\" CODE=\"102\">");
                    pw.println("<message>" + e1.getMessage() + "</message>");
                    pw.println("</UploadResponse>");
                    return;
                }

                fi.delete();

                if (destination.listFiles()==null){
                    pw.println("<UploadResponse status=\"ERROR\" CODE=\"102\">");
                    pw.println("<message>No files found</message>");
                    pw.println("</UploadResponse>");
                    return;
                }

                if (destination.listFiles().length==1 && destination.listFiles()[0].isDirectory()){
                    destination = destination.listFiles()[0];
                }

                final Hashtable<String,ArrayList<ItemI>> itemsByType = new Hashtable<String,ArrayList<ItemI>>();

                final ArrayList<File> dirs = new ArrayList<File>();
                final ArrayList<File> extraFiles = new ArrayList<File>();

                for (File f: destination.listFiles()){
                    if (f.isDirectory()){
                        dirs.add(f);
                    }else{
                        if (f.getName().toLowerCase().endsWith(".xml")){
                            SAXReader reader = new SAXReader(user);
                            try {
                                XFTItem item = reader.parse(f.getAbsolutePath());
                                ItemI om = org.nrg.xdat.base.BaseElement.GetGeneratedItem(item);
                                if (om instanceof XnatImagesessiondata){
                                    if (!itemsByType.containsKey("SESSION")){
                                        itemsByType.put("SESSION", new ArrayList<ItemI>());
                                    }

                                    ArrayList<ItemI> items = itemsByType.get("SESSION");

                                    items.add(om);
                                }else if (om instanceof XnatImagescandata){
                                    if (!itemsByType.containsKey("SCAN")){
                                        itemsByType.put("SCAN", new ArrayList<ItemI>());
                                    }

                                    ArrayList<ItemI> items = itemsByType.get("SCAN");

                                    items.add(om);
                                }else if (om instanceof XnatReconstructedimagedata){
                                    if (!itemsByType.containsKey("RECON")){
                                        itemsByType.put("RECON", new ArrayList<ItemI>());
                                    }

                                    ArrayList<ItemI> items = itemsByType.get("RECON");

                                    items.add(om);
                                }else if (om instanceof XnatImageassessordata){
                                    if (!itemsByType.containsKey("ASSESSOR")){
                                        itemsByType.put("ASSESSOR", new ArrayList<ItemI>());
                                    }

                                    ArrayList<ItemI> items = itemsByType.get("ASSESSOR");

                                    items.add(om);
                                }
                            } catch (IOException e) {
                                logger.error("",e);
                                extraFiles.add(f);
                            } catch (SAXException e) {
                                logger.error("",e);
                                extraFiles.add(f);
                            }
                        }else{
                            extraFiles.add(f);
                        }
                    }
                }

                if (itemsByType.containsKey("SESSION")&& itemsByType.get("SESSION").size()>1){
                    pw.println("<UploadResponse status=\"ERROR\" CODE=\"105\">");
                    pw.println("<message>XAR can only include data for one imaging session</message>");
                    pw.println("</UploadResponse>");
                    return;
                }

                if (itemsByType.size()==0){
                    pw.println("<UploadResponse status=\"ERROR\" CODE=\"105\">");
                    pw.println("<message>Unable to locate XNAT xml document</message>");
                    pw.println("</UploadResponse>");
                    return;
                }else if (itemsByType.size()==1){
                    //ONLY ONE DOCUMENT TYPE... so files can all be moved together

                    ArrayList<ItemI> items = itemsByType.get(itemsByType.keys().nextElement());

                    String dest_path = null;

                    boolean multiSession = false;
                    XnatImagesessiondata session = null;

                    if (itemsByType.containsKey("SESSION")){
                        if (items.size()==1){
                            session =(XnatImagesessiondata)items.get(0);
                            this.populateSession(session);
                            if (session.getProject()==null){
                                pw.println("<UploadResponse status=\"ERROR\" CODE=\"105\">");
                                pw.println("<message>Invalid project tag</message>");
                                pw.println("</UploadResponse>");
                                return;
                            }

                            if (session.getSubjectData()==null){
                                pw.println("<UploadResponse status=\"ERROR\" CODE=\"105\">");
                                pw.println("<message>Invalid subject</message>");
                                pw.println("</UploadResponse>");
                                return;
                            }

                            dest_path = session.getCurrentSessionFolder(true);
                        }else{
                            multiSession=true;
                        }
                                                
                        for (XFTItem resource: session.getItem().getChildrenOfType("xnat:abstractResource")){
                            XnatAbstractresource res =(XnatAbstractresource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(resource);
                            res.prependPathsWith(FileUtils.AppendSlash(dest_path));
                        }
                    }else if (itemsByType.containsKey("SCAN")){
                        for(ItemI om : items){
                            XnatImagescandata scan = (XnatImagescandata)om;

                            if (session==null){
                                session = scan.getImageSessionData();
                            }else{
                                if (!session.getId().equals(scan.getImageSessionId())){
                                    multiSession=true;
                                }
                            }

                            if (session!=null)
                                dest_path = FileUtils.AppendRootPath(session.getCurrentSessionFolder(true), "SCANS/" + uploadID +"/");
                            else{
                                pw.println("<UploadResponse status=\"ERROR\" CODE=\"105\">");
                                pw.println("<message>All XNAT xml documents must reference a valid Imaging Session</message>");
                                pw.println("</UploadResponse>");
                                return;
                            }
                            
                            for (XFTItem resource: scan.getItem().getChildrenOfType("xnat:abstractResource")){
                                XnatAbstractresource res =(XnatAbstractresource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(resource);
                                res.prependPathsWith(FileUtils.AppendSlash(dest_path));
                            }
                        }
                    }else if (itemsByType.containsKey("RECON")){
                        for(ItemI om : items){
                            XnatReconstructedimagedata scan = (XnatReconstructedimagedata)om;

                            if (session==null){
                                session = scan.getImageSessionData();
                            }else{
                                if (!session.getId().equals(scan.getImageSessionId())){
                                    multiSession=true;
                                }
                            }

                            if (session!=null)
                                dest_path = FileUtils.AppendRootPath(session.getCurrentSessionFolder(true), "PROCESSED/" + uploadID +"/");
                            else{
                                pw.println("<UploadResponse status=\"ERROR\" CODE=\"105\">");
                                pw.println("<message>All XNAT xml documents must reference a valid Imaging Session</message>");
                                pw.println("</UploadResponse>");
                                return;
                            }
                            
                            for (XFTItem resource: scan.getItem().getChildrenOfType("xnat:abstractResource")){
                                XnatAbstractresource res =(XnatAbstractresource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(resource);
                                res.prependPathsWith(FileUtils.AppendSlash(dest_path));
                            }
                        }
                    }else if (itemsByType.containsKey("ASSESSOR")){
                        for(ItemI om : items){
                            XnatImageassessordata scan = (XnatImageassessordata)om;
                            this.populateAssessor(scan);
                            if (session==null){
                                session = scan.getImageSessionData();
                            }else{
                                if (!session.getId().equals(scan.getImagesessionId())){
                                    multiSession=true;
                                }
                            }

                            if (scan.getProject()==null){
                                pw.println("<UploadResponse status=\"ERROR\" CODE=\"105\">");
                                pw.println("<message>Invalid project tag</message>");
                                pw.println("</UploadResponse>");
                                return;
                            }

                            if (session!=null)
                                dest_path = FileUtils.AppendRootPath(session.getCurrentSessionFolder(true), "ASSESSORS/" + uploadID +"/");
                            else{
                                pw.println("<UploadResponse status=\"ERROR\" CODE=\"105\">");
                                pw.println("<message>All XNAT xml documents must reference a valid Imaging Session</message>");
                                pw.println("</UploadResponse>");
                                return;
                            }
                            
                            for (XFTItem resource: scan.getItem().getChildrenOfType("xnat:abstractResource")){
                                XnatAbstractresource res =(XnatAbstractresource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(resource);
                                res.prependPathsWith(FileUtils.AppendSlash(dest_path));
                            }
                        }
                    }

                    if (multiSession){
                        pw.println("<UploadResponse status=\"ERROR\" CODE=\"105\">");
                        pw.println("<message>XAR can only include data for one imaging session</message>");
                        pw.println("</UploadResponse>");
                        return;
                    }else{
                        //COPY ALL UPLOADED FILES
                        File dest = new File(dest_path);
                        if (!dest.exists())
                            dest.mkdirs();

                        if (dirs.size()==1 && extraFiles.size()==0){
                            //CONTAINER FOLDER
                        	File[] children = dirs.get(0).listFiles();
                        	if (children!=null){
                            	for(File child : children){
                            		if (child.isDirectory())
                            			FileUtils.MoveDir(child, new File(dest,child.getName()), true);
                            		else
                            			FileUtils.MoveFile(child, new File(dest,child.getName()), true);
                            	}
                        	}
                        }else{



                            for(File dir : dirs){

                                FileUtils.MoveDir(dir, new File(dest,dir.getName()), true);

                            }



                            for(File f : extraFiles){

                                FileUtils.MoveFile(f, new File(dest,f.getName()), true);

                            }

                        }



                        for(ItemI item : items){

                        	if(Permissions.canEdit(user, item)){
                        		PersistentWorkflowI wrk=PersistentWorkflowUtils.buildOpenWorkflow(user, item.getItem(), SecureAction.newEventInstance(data,EventUtils.CATEGORY.DATA, EventUtils.STORE_XAR));
                        		EventMetaI c=wrk.buildEvent();
                                try {
									SaveItemHelper.unauthorizedSave(item,user, false, true,c);

									WorkflowUtils.complete(wrk, c);
								} catch (Exception e) {
									WorkflowUtils.fail(wrk, c);
								}
                        	}

                        }

                    }

                }else{

                    //MULTIPLE DOCUMENT TYPEs... so files must be moved separately

                    pw.println("<UploadResponse status=\"COMPLETE\" CODE=\"0\">");

                    pw.println("<message>Error multiple data types cannot share a single XAR.  Please separate files into separate XARs</message>");

                    pw.println("</UploadResponse>");

                    return;

                }



                FileUtils.DeleteFile(original);



                pw.println("<UploadResponse status=\"COMPLETE\" CODE=\"0\">");

                pw.println("<message>Upload Complete</message>");

                pw.println("</UploadResponse>");

                return;

            }

        } catch (FileNotFoundException e) {

            logger.error(e);

            pw.println("<UploadResponse status=\"ERROR\" CODE=\"105\">");

            pw.println("<message>" + e.getMessage() + "</message>");

            pw.println("</UploadResponse>");

            return;

        } catch (IOException e) {

            logger.error(e);

            pw.println("<UploadResponse status=\"ERROR\" CODE=\"106\">");

            pw.println("<message>" + e.getMessage() + "</message>");

            pw.println("</UploadResponse>");

            return;

        } catch (XFTInitException e) {

            logger.error(e);

            pw.println("<UploadResponse status=\"ERROR\" CODE=\"107\">");

            pw.println("<message>" + e.getMessage() + "</message>");

            pw.println("</UploadResponse>");

            return;

        } catch (ElementNotFoundException e) {

            logger.error(e);

            pw.println("<UploadResponse status=\"ERROR\" CODE=\"108\">");

            pw.println("<message>" + e.getMessage() + "</message>");

            pw.println("</UploadResponse>");

            return;

        } catch (DBPoolException e) {

            logger.error(e);

            pw.println("<UploadResponse status=\"ERROR\" CODE=\"109\">");

            pw.println("<message>" + e.getMessage() + "</message>");

            pw.println("</UploadResponse>");

            return;

        } catch (SQLException e) {

            logger.error(e);

            pw.println("<UploadResponse status=\"ERROR\" CODE=\"110\">");

            pw.println("<message>" + e.getMessage() + "</message>");

            pw.println("</UploadResponse>");

            return;

        } catch (FieldNotFoundException e) {

            logger.error(e);

            pw.println("<UploadResponse status=\"ERROR\" CODE=\"111\">");

            pw.println("<message>" + e.getMessage() + "</message>");

            pw.println("</UploadResponse>");

            return;

        } catch (FailedLoginException e) {

            logger.error(e);

            pw.println("<UploadResponse status=\"ERROR\" CODE=\"112\">");

            pw.println("<message>" + e.getMessage() + "</message>");

            pw.println("</UploadResponse>");

            return;

        } catch (Exception e) {

            logger.error(e);

            pw.println("<UploadResponse status=\"ERROR\" CODE=\"113\">");

            pw.println("<message>" + e.getMessage() + "</message>");

            pw.println("</UploadResponse>");

            return;

        }

    }



    @Override

    protected String getContentType(RunData data) {

        return "text/xml";

    }



    private void populateSession(XnatImagesessiondata session){

        if (session.getProject()==null){

            return;

        }



        session.validateSubjectId();



        final XnatSubjectdata subject = session.getSubjectData();



        if (subject==null){

            return;

        }

        if (session.getProject()==null){
            session.setProject(subject.getProject());
        }
        
        try {
       
            if (session.getId()==null || session.getId().equals("")){

                session.setId(XnatExperimentdata.CreateNewID());

            }
        } catch (Exception e) {

            logger.error("",e);

        }

    }







    private void populateAssessor(XnatImageassessordata temp){

        if (temp.getProject()==null){

            return;

        }

        temp.validateSessionId();



        final XnatImagesessiondata session = temp.getImageSessionData();



        if (session==null){

            return;

        }



        if (temp.getProject()==null){

            temp.setProject(session.getProject());

        }



        try {
            if (temp.getId()==null || temp.getId().equals("")){

                temp.setId(XnatExperimentdata.CreateNewID());

            }
        } catch (Exception e) {

            logger.error("",e);

        }

    }

}
