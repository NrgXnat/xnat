/*
 * org.nrg.xdat.om.base.BaseXnatReconstructedimagedata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatExperimentdata.UnknownPrimaryProjectException;
import org.nrg.xdat.om.base.auto.AutoXnatReconstructedimagedata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.StringUtils;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatReconstructedimagedata extends AutoXnatReconstructedimagedata {

	public BaseXnatReconstructedimagedata(ItemI item)
	{
		super(item);
	}

	public BaseXnatReconstructedimagedata(UserI user)
	{
		super(user);
	}

	public BaseXnatReconstructedimagedata()
	{}

	public BaseXnatReconstructedimagedata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    private XnatImagesessiondata mr = null;

    public XnatImagesessiondata getImageSessionData()
    {
        if (mr==null)
        {
            ArrayList al = XnatImagesessiondata.getXnatImagesessiondatasByField("xnat:imageSessionData/ID",this.getImageSessionId(),this.getUser(),false);
            if (al.size()>0)
            {
                mr = (XnatImagesessiondata)al.get(0);
            }
        }

        return mr;
    }
    
    public void setImageSessionData(XnatImagesessiondata ses){
    	mr=ses;
    }

    public ArrayList getOutFileByContent(String content) {

        ArrayList files = new ArrayList();

        List outFiles = getOut_file();

        if (outFiles == null || outFiles.size() == 0) return files;

        for (int i = 0 ; i < outFiles.size(); i++) {

            XnatAbstractresource absrsc = (XnatAbstractresource) outFiles.get(i);

            String rcontent = null;

            if (absrsc instanceof XnatResource) {

                XnatResource resource = (XnatResource)outFiles.get(i);

                rcontent = resource.getContent();

            }

            if (rcontent != null && content != null && rcontent.equals(content)) {

                files.add(absrsc);

            }

        }

        return files;

   }

    public ArrayList getComputationByName(String name) {

        ArrayList rtn = new ArrayList();

        List datums = this.getComputations_datum();

        if (datums == null || datums.size() == 0) {

            return rtn;

        }

        for (int i = 0; i < datums.size(); i++) {

            XnatComputationdata aDatum = (XnatComputationdata)datums.get(i);

            if (aDatum.getName().equals(name)) {

                rtn.add(datums.get(i));

            }

        }

        return rtn;

    }


	public File getExpectedSessionDir() throws InvalidArchiveStructure, UnknownPrimaryProjectException{
		return this.getImageSessionData().getExpectedSessionDir();
	}
	@Override
	public void preSave() throws Exception{
		super.preSave();
		if(this.getImageSessionData()==null){
			throw new Exception("Unable to identify image session for:" + this.getImageSessionId());
		}
		final String expectedPath=this.getExpectedSessionDir().getAbsolutePath().replace('\\', '/');
		
		validate(expectedPath);
	}
	
	public void validate(String expectedPath) throws Exception{
		
		if(StringUtils.IsEmpty(this.getId())){
			throw new IllegalArgumentException();
		}	
		
		if(!StringUtils.IsAlphaNumericUnderscore(getId())){
			throw new IllegalArgumentException("Identifiers cannot use special characters.");
		}
		
		for(final XnatAbstractresourceI res: this.getOut_file()){
			final String uri;
			if(res instanceof XnatResource){
				uri=((XnatResource)res).getUri();
			}else if(res instanceof XnatResourceseries){
				uri=((XnatResourceseries)res).getPath();
			}else{
				continue;
			}
			
			FileUtils.ValidateUriAgainstRoot(uri,expectedPath,"URI references data outside of the project:" + uri);
		}
		
		for(final XnatAbstractresourceI res: this.getIn_file()){
			final String uri;
			if(res instanceof XnatResource){
				uri=((XnatResource)res).getUri();
			}else if(res instanceof XnatResourceseries){
				uri=((XnatResourceseries)res).getPath();
			}else{
				continue;
	}
			
			FileUtils.ValidateUriAgainstRoot(uri,expectedPath,"URI references data outside of the project:" + uri);
}
	}
}
