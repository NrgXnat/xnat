/*
 * web: org.nrg.xnat.turbine.utils.XNATSessionPopulater
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.xml.sax.SAXException;


/**
 * @author timo
 * 
 */
public class XNATSessionPopulater {
		private final UserI user;
		private final File xml;
		private final String project;
		private final boolean nullifySubject;
	
		/**
		 * Use this object to populate a Session object from the prearchive.  The xml File can point to the xml or the session directory.
		 * 
		 * It fixes the scan types, usability, label, and subject id.
		 * 
		 * @param user
		 * @param xml
		 * @param project
		 * @param nullifySubject
		 */
		public XNATSessionPopulater(UserI user, File xml, String project,boolean nullifySubject){
			this.user=user;
			this.xml=(xml.getPath().endsWith(".xml"))?xml:new File(xml.getPath()+".xml");
			this.project=project;
			this.nullifySubject=nullifySubject;
		}
		
		public XnatImagesessiondata populate() throws IOException,SAXException{			
			final SAXReader reader = new SAXReader(user);
	        final XFTItem item = reader.parse(xml.getAbsolutePath());

	        XnatImagesessiondata imageSessionData =(XnatImagesessiondata) BaseElement.GetGeneratedItem(item);
	        imageSessionData.fixScanTypes();

	        final List<String> scanQualityLabels = ScanQualityUtils.getQualityLabels(project, user);
	        for(final XnatImagescandataI scan: imageSessionData.getScans_scan()){
	        	if(!XNATUtils.hasValue(scan.getQuality())){
	        		((XnatImagescandata)scan).setQuality(scanQualityLabels.get(0));
	        	}
	        }
	        
	        //if no primary project set, insert from context
	        if (null != project){
	            imageSessionData.setProject(project);
	        }
	        
	        if(XNATUtils.hasValue(imageSessionData.getId())){
	        	if(!XNATUtils.hasValue(imageSessionData.getLabel())){
	        		imageSessionData.setLabel(imageSessionData.getId());
	        	}
	        	imageSessionData.setId("");
	        }

	        if (imageSessionData.getSubjectId()!=null && imageSessionData.getSubjectId().startsWith("INVALID:")) {
	            imageSessionData.setSubjectId(imageSessionData.getSubjectId().substring(8).trim());
	        }


	        if(XNATUtils.hasValue(imageSessionData.getSubjectId())){
	        	imageSessionData.setSubjectId(XnatSubjectdata.cleanValue(imageSessionData.getSubjectId()));
	        	
	        	if (!XNATUtils.hasValue(imageSessionData.getSubjectId()))
	            {
	                imageSessionData.setSubjectId("NULL");
	            }
	        }else{
	            imageSessionData.setSubjectId("NULL");
	        }
	        
	        if(!XNATUtils.hasValue(imageSessionData.getSubjectId())){
	        	if(XNATUtils.hasValue(imageSessionData.getDcmpatientname())){
	        		imageSessionData.setSubjectId(XnatSubjectdata.cleanValue(imageSessionData.getDcmpatientname()));

	            	if (!XNATUtils.hasValue(imageSessionData.getSubjectId()))
	                {
	                    imageSessionData.setSubjectId("NULL");
	                }
	        	}
	        }
	        
	    	if((!imageSessionData.validateSubjectId()) && nullifySubject){
	            imageSessionData.setSubjectId("NULL");
	    	}
	        
	        return imageSessionData;
		}
		

}
