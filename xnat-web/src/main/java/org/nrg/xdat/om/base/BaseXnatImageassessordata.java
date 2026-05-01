/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatImageassessordata
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatImageassessordata;
import org.nrg.xdat.shared.OmUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"rawtypes"})
public abstract class BaseXnatImageassessordata extends AutoXnatImageassessordata{

	public BaseXnatImageassessordata(ItemI item)
	{
		super(item);
	}

	public BaseXnatImageassessordata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatImageassessordata(UserI user)
	 **/
	public BaseXnatImageassessordata()
	{}

	public BaseXnatImageassessordata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}



    private XnatImagesessiondata mr = null;

    public XnatImagesessiondata getImageSessionData()
    {
        if (mr==null)
        {
            ArrayList al = XnatImagesessiondata.getXnatImagesessiondatasByField("xnat:imageSessionData/ID",this.getImagesessionId(),this.getUser(),false);
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

    
    @SuppressWarnings( {"unused", "deprecation"})
	public boolean validateSessionId(){
        final String sessionId = StringUtils.remove(getImagesessionId(), "'");
        if (StringUtils.isNotBlank(sessionId)){
            final String login = getUser() != null ? getUser().getUsername() : null;
            try {
				final String idCOUNT = (String)PoolDBUtils.ReturnStatisticQuery("SELECT ID FROM xnat_imageSessiondata WHERE ID='" + sessionId + "'", "id", getDBName(), login);
                if (StringUtils.isNotBlank(idCOUNT)){
                    return true;
                }
                
                final String project = getProject();
                if (StringUtils.isNotBlank(project)){
					final String newSessionId = (String) PoolDBUtils.ReturnStatisticQuery("SELECT id FROM xnat_experimentData WHERE label='" + sessionId + "' AND project='" + project + "'", "id", getDBName(), login);
					if (StringUtils.isNotBlank(newSessionId)) {
						setImagesessionId(newSessionId);
						return true;
					}

					final String sharedSessionId = (String) PoolDBUtils.ReturnStatisticQuery("SELECT sharing_share_xnat_experimentda_id FROM xnat_experimentData_share WHERE label='" + sessionId + "' AND project='" + project + "'", "sharing_share_xnat_experimentda_id", getDBName(), login);
					if (StringUtils.isNotBlank(sharedSessionId)) {
                        setImagesessionId(sharedSessionId);
                        return true;
                    }
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return false;
    }

	public void deleteFiles(final UserI user, final EventMetaI event) throws Exception {
		final List<XnatAbstractresourceI> resources = Stream.concat(getResources_resource().stream(), getOut_file().stream()).collect(Collectors.toList());
		// If there are no resources to delete, then skip the rest of this.
		if (resources.isEmpty()) {
			return;
		}
		final String                      projectId   = getProject();
		final String                      rootPath    = ArcSpecManager.GetInstance().getArchivePathForProject(projectId);
		final File                        archivePath = getExpectedSessionDir().toPath().resolve("ASSESSORS").resolve(getArchiveDirectoryName()).toFile();
		OmUtils.deleteResourceFiles(user, rootPath, projectId, archivePath, resources, event);
	}

	public File getExpectedSessionDir() throws InvalidArchiveStructure,UnknownPrimaryProjectException{
		return getImageSessionData().getExpectedSessionDir();
	}

	@Override
	public void preSave() throws Exception{
		if(StringUtils.isBlank(getId())){
			throw new IllegalArgumentException("Please specify an ID for your experiment.");
		}	
		
		if(XDAT.getSiteConfigPreferences().getRequireImageAssessorLabels() && StringUtils.isBlank(getLabel())){
			throw new IllegalArgumentException("Please specify a label for your experiment.");
		}
		
		if(!XftStringUtils.isValidId(getId())){
			throw new IllegalArgumentException("Identifiers cannot use special characters.");
		}
		
		if(!StringUtils.isBlank(getLabel()) && !XftStringUtils.isValidId(getLabel())){
			throw new IllegalArgumentException("Labels cannot use special characters.");
		}
		
		if(getImageSessionData()==null){
			throw new Exception("Unable to identify image session for:" + getImagesessionId());
		}
		
		final XnatProjectdata proj = getPrimaryProject(false);
		if(proj==null){
			throw new Exception("Unable to identify project for:" + getProject());
		}
		
		checkUniqueLabel();
		
		final String expectedPath=getExpectedSessionDir().getAbsolutePath().replace('\\', '/');
		OmUtils.validateXnatAbstractResources(expectedPath, getResources_resource());
		OmUtils.validateXnatAbstractResources(expectedPath, getOut_file());
	}

	public String getResourceCatalogRootPathByLabel(final String label) {
		final String rootPath = super.getResourceCatalogRootPathByLabel(label);
		return StringUtils.isNotBlank(rootPath) ? rootPath : getUriForMatchingCatalog(label, getOut_file());
	}

}
