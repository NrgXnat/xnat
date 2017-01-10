/*
 * web: org.nrg.xnat.restlet.actions.PullSessionDataFromHeaders
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.*;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.nrg.xnat.archive.XNATSessionBuilder;
import org.nrg.xnat.exceptions.ValidationException;
import org.nrg.xnat.restlet.util.XNATRestConstants;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;


/**
 * @author Timothy R. Olsen <olsent@wustl.edu>
 *
 */
public class PullSessionDataFromHeaders implements Callable<Boolean> {
	static Logger logger = Logger.getLogger(PullSessionDataFromHeaders.class);

	private final XnatImagesessiondata tempMR;
	private final UserI user;
	private boolean allowDataDeletion;
	private final boolean overwrite,isInPrearchive;
	private final EventMetaI c;

	public PullSessionDataFromHeaders(final XnatImagesessiondata mr, final UserI user, boolean allowDataDeletion, final boolean overwrite, final boolean isInPrearchive,EventMetaI c){
		this.tempMR=mr;
		this.user=user;
		this.allowDataDeletion=allowDataDeletion;
		this.overwrite=overwrite;
		this.isInPrearchive=isInPrearchive;
		this.c=c;
	}


	/**
	 * This method will pull header values from DICOM (or ECAT) and update the session xml accordingly.  It assumes the files are already in the archive and properly referenced from the session xml.  This would usually be run after you've added the files via the REST API.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ValidationException: Scan invalid according to schema requirements (including xdat tags)
	 * @throws Exception
	 */
	public Boolean call()
	throws IOException,SAXException,ValidationException,Exception {
		//identify session directory location based on existing scans, the deriveSessionDir() method will return null if it can't load it from the scans.
		final String derivedSessionDir=tempMR.deriveSessionDir();

		if (derivedSessionDir==null){
			throw new Exception("Unable to derive session directory");
		}

		final File sessionDir=new File(derivedSessionDir);

		//build session xml document for data in the session directory
		final String timestamp= XNATRestConstants.getPrearchiveTimestamp();
		final File xml = new File(sessionDir,tempMR.getLabel()+ "_"+ timestamp+".xml");

		final XNATSessionBuilder builder= new XNATSessionBuilder(sessionDir,xml,tempMR.getProject(),isInPrearchive);
		builder.call();

		//this should really throw a specific execution object
		if (!xml.exists() || xml.length()==0) {
			throw new Exception("Unable to locate DICOM or ECAT files");
		}

		//build image session object from generated xml
		final SAXReader reader = new SAXReader(user);
		final XFTItem temp2 = reader.parse(xml.getAbsolutePath());
		final XnatImagesessiondata newmr = (XnatImagesessiondata)BaseElement.GetGeneratedItem(temp2);

		if(overwrite)
		{
			//this will ignore the pre-existing session and store the newly generated xml in place of the old one.
			//this will delete references added resources (snapshots, reconstructions, assessments, etc)
			allowDataDeletion=false;//why is this set to false when you want to override?
			newmr.setId(tempMR.getId());
		}else{
			//copy values from old session, to new session
			newmr.copyValuesFrom(tempMR);

			for (final XnatImagescandataI newscan : newmr.getSortedScans()){
				final XnatImagescandata oldScan = tempMR.getScanById(newscan.getId());
				
				//copy values from old session, to new session
				//if oldScan is null, then a new scan has been discovered and old values are not present to maintain.
				if(oldScan!=null){
					if(!oldScan.getXSIType().equals(newscan.getXSIType())){
						throw new Exception(String.format("Modification of scan modality (%s to %s) not supported.",oldScan.getXSIType(),newscan.getXSIType()));
					}
					
					((XnatImagescandata)newscan).setXnatImagescandataId(oldScan.getXnatImagescandataId());

					//if allowDataDeletion=true, then new file tags will replace old ones (modifications to content, format, etc will not be preserved).
					if(!allowDataDeletion){
						//in the current code, the new file entries should not be maintained. The old ones are assumed to be correct and not needing updates.
						//the content, format, and description of the new file entries will be preserved if the old ones were null.
						if(newscan.getFile().size()>0){
							final XnatResource newcat=(XnatResource)newscan.getFile().get(0);

							final XnatAbstractresourceI oldCat=oldScan.getFile().get(0);
							if(oldCat instanceof XnatResource){
								if(StringUtils.isBlank(((XnatResource)oldCat).getContent()) && StringUtils.isNotBlank(newcat.getContent()))
									((XnatResource)oldCat).setContent(newcat.getContent());
								if(StringUtils.isBlank(((XnatResource)oldCat).getFormat()) && StringUtils.isNotBlank(newcat.getFormat()))
									((XnatResource)oldCat).setFormat(newcat.getFormat());
								if(StringUtils.isBlank(((XnatResource)oldCat).getDescription()) && StringUtils.isNotBlank(newcat.getDescription()))
									((XnatResource)oldCat).setDescription(newcat.getDescription());
							}

							while(newscan.getFile().size()>0)((XnatImagescandata)newscan).removeFile(0);

							//replace new files (catalogs) with old ones.
							((XnatImagescandata)newscan).setFile((XnatAbstractresource)oldCat);
						}else{
							while(newscan.getFile().size()>0)((XnatImagescandata)newscan).removeFile(0);

						}
					}
				}
			}

			newmr.setId(tempMR.getId());
		}

		//if any scan types are null, they will be filled according to the standard logic.
		newmr.fixScanTypes();        

		//xml validation
		final ValidationResultsI vr = newmr.validate();        

		if (vr != null && !vr.isValid())
		{
			throw new ValidationException(vr.toString());
		}else{
			final XnatProjectdata proj = newmr.getProjectData();
			if(SaveItemHelper.authorizedSave(newmr,user,false,allowDataDeletion,c)){
				try {
					MaterializedView.deleteByUser(user);

					if(proj.getArcSpecification().getQuarantineCode()!=null && proj.getArcSpecification().getQuarantineCode().equals(1)){
						newmr.quarantine(user);
					}
				} catch (Exception e) {
					logger.error("",e);
				}
			}

		}

		return Boolean.TRUE;
	}
}
