/*
 * web: org.nrg.xnat.restlet.actions.XarImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions;

import org.apache.log4j.Logger;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.*;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.zip.TarUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.WorkflowUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.zip.ZipOutputStream;

@ImporterHandler(handler = ImporterHandlerA.XAR_IMPORTER)
public class XarImporter extends ImporterHandlerA implements Callable<List<String>> {

	private static final Logger logger = Logger.getLogger(XarImporter.class);

	private final FileWriterWrapperI fw;
	private final UserI user;
	final Map<String,Object> params;
	private final ArrayList<String> urlList;

	/**
	 *
	 * @param listenerControl     The listener.
	 * @param u                   The user.
     * @param fw                  The file writer wrapper.
     * @param params              Parameters to the operation, including overwrite, 'append' means overwrite, but
     *                            preserve un-modified content (don't delete anything), 'delete' means delete the
     *                            pre-existing content. Should include project, subject_ID and label if session is null.
	 */
	public XarImporter(final Object listenerControl, final UserI u, final FileWriterWrapperI fw, final Map<String,Object> params) {
		super(listenerControl, u, fw, params);
		this.user=u;
		this.fw=fw;
		this.params=params;
		this.urlList= new ArrayList<>();
	}

	//@SuppressWarnings("serial")
	public List<String> call() throws ClientException,ServerException{

		try {
			final List<String> returnList = processXarFile();
			this.completed("Successfully imported session from XAR");
			return returnList;
		} catch (ClientException e) {
			this.failed(e.getMessage());
			throw e;
		} catch (ServerException e) {
			this.failed(e.getMessage());
			throw e;
		} catch (Throwable e) {
			logger.error("",e);
			throw new ServerException(e.getMessage(),new Exception());
		}

	}

	private List<String> processXarFile() throws ClientException,ServerException {

        String cachepath = ArcSpecManager.GetInstance().getGlobalCachePath();
        Date d = Calendar.getInstance().getTime();
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyyMMdd_HHmmss");
        String uploadID = formatter.format(d);
        cachepath+="user_uploads/"+user.getID() + "/" + uploadID + "/";

        File destination = new File(cachepath);
        final File original = new File(cachepath);

        final String fileName = fw.getName();
		ZipI zipper = getZipper(fileName);

        try {
        	zipper.extract(fw.getInputStream(),cachepath);
        } catch (Exception e) {
        	throw new ClientException("Archive file is corrupt or not a valid archive file type.");
        }

        String[] fileList=destination.list();
        if (fileList==null || fileList.length==0) {
        	throw new ClientException("Archive file contains no files.");
        }

        if (destination.listFiles().length==1 && destination.listFiles()[0].isDirectory()){
            destination = destination.listFiles()[0];
        }

    	//
    	// PROCESS EXTRACTED FILE
    	//

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
                       	//urlList.add("/archive/experiments/" + ((XnatExperimentdata)om).getId());
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
        	if (fileList==null || fileList.length==0) {
        		throw new ClientException("XAR can only include data for one imaging session.");
            }
        }

        if (itemsByType.size()==0){
       		throw new ClientException("Unable to locate XNAT xml document.");
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
                   	urlList.add("/archive/experiments/" + session.getId());

                    if (session.getProject()==null){
                    	throw new ClientException("Could not process XAR file - Invalid project tag.");
                    }

                    if (session.getSubjectData()==null){
                    	throw new ClientException("Could not process XAR file - Invalid subject.");
                    }

                    try {
                    	dest_path = session.getCurrentSessionFolder(true);
                    } catch (Exception e) {
						throw new ServerException("Server Error:  Invalid Archive Structure");
					}

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
                   	urlList.add("/archive/experiments/" + session.getId());


                    if (session!=null)
						try {
							dest_path = FileUtils.AppendRootPath(session.getCurrentSessionFolder(true), "SCANS/" + scan.getId() +"/");
						} catch (Exception e) {
							throw new ServerException(e.getMessage());
						}
					else{
                    	throw new ClientException("All XNAT xml documents must reference a valid Imaging Session.");
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
                   	urlList.add("/archive/experiments/" + session.getId());

                    if (session!=null)
						try {
							dest_path = FileUtils.AppendRootPath(session.getCurrentSessionFolder(true), "PROCESSED/" + uploadID +"/");
						} catch (Exception e) {
							throw new ServerException(e.getMessage());
						}
					else{
                    	throw new ClientException("All XNAT xml documents must reference a valid Imaging Session.");
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
                   	//urlList.add("/archive/experiments/" + session.getId());
					urlList.add("/archive/experiments/" + session.getId() + "/assessors/" + scan.getId());

                    if (scan.getProject()==null){
                    	throw new ClientException("Could not process XAR file - Invalid project tag.");
                    }

                    if (session!=null)
						try {
							dest_path = FileUtils.AppendRootPath(session.getCurrentSessionFolder(true), "ASSESSORS/" + uploadID +"/");
						} catch (Exception e) {
							throw new ServerException(e.getMessage());
						}
					else{
                    	throw new ClientException("All XNAT xml documents must reference a valid Imaging Session.");
                    }
                    for (XFTItem resource: scan.getItem().getChildrenOfType("xnat:abstractResource")){
                        XnatAbstractresource res =(XnatAbstractresource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(resource);
                        res.prependPathsWith(FileUtils.AppendSlash(dest_path));
                    }

                }

            }

            if (multiSession){

               	throw new ClientException("XAR can only include data for one imaging session.");

            }else{

                //COPY ALL UPLOADED FILES
                File dest = new File(dest_path);
                if (!dest.exists())
                    dest.mkdirs();
				try {
	                for(ItemI item : items){
	                	PersistentWorkflowI wrk=PersistentWorkflowUtils.buildOpenWorkflow(user, item.getItem(), EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getType((String)params.get(EventUtils.EVENT_TYPE),EventUtils.TYPE.WEB_SERVICE), EventUtils.STORE_XAR, (String)params.get(EventUtils.EVENT_REASON), (String)params.get(EventUtils.EVENT_COMMENT)));
                		EventMetaI c=wrk.buildEvent();
                        try {
                        	//SaveItemHelper.unauthorizedSave(item, user, false, true,c);

                            if(item.getItem()!=null && item.getItem().instanceOf(XnatImagescandata.SCHEMA_ELEMENT_NAME)){
                                final String parentId = item.getStringProperty(XnatImagescandata.SCHEMA_ELEMENT_NAME + "/image_session_ID");
                                if(parentId!=null) {
                                    final XnatExperimentdata parent = XnatImagesessiondata.getXnatExperimentdatasById(parentId, user, false);
                                    if (parent != null) {
                                        final XnatImagesessiondata scanSession = (XnatImagesessiondata) parent;
                                        scanSession.addScans_scan(new XnatImagescandata(item));
                                        SaveItemHelper.authorizedSave(scanSession, user, false, true, c);
                                    }
                                }
                            }
                            else {
                                SaveItemHelper.unauthorizedSave(item, user, false, true, c);
                            }

							WorkflowUtils.complete(wrk, c);
						} catch (Exception e) {
							WorkflowUtils.fail(wrk, c);
							// Re-throw exception.  Should not proceed as if complete successfully.
							throw(e);
						}
	                }
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
				} catch (Exception e) {
					throw new ServerException("ERROR:  Server-side exception (" + e.toString() + ")");
				}
            }

        }else{

            //MULTIPLE DOCUMENT TYPEs... so files must be moved separately
          	throw new ClientException("Multiple data types cannot share a single XAR.  Please separate files into separate XARs");

        }

        FileUtils.DeleteFile(original);
		return urlList;

	}

	private ZipI getZipper(String fileName) {

		// Assume file name represents correct compression method
        String file_extension = null;
        if (fileName!=null && fileName.indexOf(".")!=-1) {
        	file_extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        	if (Arrays.asList(XDAT.getSiteConfigPreferences().getZipExtensionsAsArray()).contains(file_extension)) {
        		return new ZipUtils();
	        } else if (file_extension.equalsIgnoreCase(".tar")) {
        		return new TarUtils();
	        } else if (file_extension.equalsIgnoreCase(".gz")) {
	        	TarUtils zipper = new TarUtils();
	        	zipper.setCompressionMethod(ZipOutputStream.DEFLATED);
	        	return zipper;
	        }
        }
        // Assume zip-compression for unnamed inbody files
        return new ZipUtils();

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
