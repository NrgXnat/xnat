/*
 * web: org.nrg.xnat.restlet.actions.PullScanDataFromHeaders
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.nrg.xnat.archive.XNATSessionBuilder;
import org.nrg.xnat.exceptions.MultipleScanException;
import org.nrg.xnat.exceptions.ValidationException;
import org.nrg.xnat.restlet.util.XNATRestConstants;
import org.xml.sax.SAXException;


/**
 * @author Timothy R. Olsen <olsent@wustl.edu>
 *
 */
public class PullScanDataFromHeaders implements Callable<Boolean> {
	static Logger logger = Logger.getLogger(PullScanDataFromHeaders.class);
	
	private final XnatImagescandata tempMR;
	private final UserI user;
	private final boolean allowDataDeletion,isInPrearchive;
	private final EventMetaI c;
	
	public PullScanDataFromHeaders(final XnatImagescandata scan, final UserI user, boolean allowDataDeletion,boolean isInPrearchive,EventMetaI c){
		this.tempMR=scan;
		this.user=user;
		this.allowDataDeletion=allowDataDeletion;
		this.isInPrearchive=isInPrearchive;
		this.c=c;
	}

	/**
	 * This method will pull header values from DICOM (or ECAT) and update the scan xml accordingly.  It assumes the files are already in the archive and properly referenced from the session xml.  This would usually be run after you've added the files via the REST API.
	 * WARNINGS: 
	 *    This method will not update session level parameters
	 *    This method will fail if the scan directory contains more than one scan or session.
	 * 
	 * @throws IOException: Error accessing files
	 * @throws SAXException: Error parsing generated xml
	 * @throws MultipleScanException
     * @throws ValidationException: Scan invalid according to schema requirements (including xdat tags)
	 * @throws Exception When something goes wrong.
	 */
	public Boolean call() throws IOException,SAXException,MultipleScanException,ValidationException,Exception{
		final File scanDir=new File(tempMR.deriveScanDir());
        
		//build timestamped file for SessionBuilder output.
		final String timestamp = XNATRestConstants.getPrearchiveTimestamp();
		final File xml = new File(scanDir, tempMR.getId() + "_" + timestamp + ".xml");
		
		//run DICOM builder
		final XNATSessionBuilder builder= new XNATSessionBuilder(scanDir,xml,tempMR.getImageSessionData().getProject(),isInPrearchive);
		builder.call();
		
	    if(!xml.exists() || xml.length()==0){
	    	new Exception("Unable to locate DICOM or ECAT files");
	    }
	    
		final SAXReader reader = new SAXReader(user);
		final XFTItem temp2 = reader.parse(xml.getAbsolutePath());
		final XnatImagesessiondata newmr = (XnatImagesessiondata)BaseElement.GetGeneratedItem(temp2);
        XnatImagescandata newscan=null;
        
		
    	if(newmr.getScans_scan().size()>1){
    		throw new MultipleScanException();
    	}else{
    		newscan=(XnatImagescandata)newmr.getScans_scan().get(0);
    	}
             
    	if(!tempMR.getXSIType().equals(newscan.getXSIType())){
			throw new Exception(String.format("Modification of scan modality ({} to {}) not supported.",tempMR.getXSIType(),newscan.getXSIType()));
		}
    	
        newscan.copyValuesFrom(tempMR);
        newscan.setImageSessionId(tempMR.getImageSessionId());
        newscan.setId(tempMR.getId());
        newscan.setXnatImagescandataId(tempMR.getXnatImagescandataId());
    	
	    if(!allowDataDeletion){
    		while(newscan.getFile().size()>0)newscan.removeFile(0);
		}

        final ValidationResultsI vr = newmr.validate();        
        
        if (vr != null && !vr.isValid())
        {
            throw new ValidationException(vr.toString());
        }else{
        	final XnatImagesessiondata mr=tempMR.getImageSessionData();
        	final XnatProjectdata proj = mr.getProjectData();
        	if(SaveItemHelper.authorizedSave(newscan,user,false,allowDataDeletion,c)){
				try {
				MaterializedView.deleteByUser(user);

				if(proj.getArcSpecification().getQuarantineCode()!=null && proj.getArcSpecification().getQuarantineCode().equals(1)){
					mr.quarantine(user);
				}
					} catch (Exception e) {
						logger.error("",e);
					}
			}
        }

        return Boolean.TRUE;
	}
}
