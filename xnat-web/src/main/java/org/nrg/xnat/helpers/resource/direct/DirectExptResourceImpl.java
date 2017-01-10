/*
 * web: org.nrg.xnat.helpers.resource.direct.DirectExptResourceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.resource.direct;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatExperimentdata.UnknownPrimaryProjectException;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;

/**
 * @author timo
 *
 */
public class DirectExptResourceImpl extends ResourceModifierA {
	private final XnatProjectdata proj;
	private final XnatExperimentdata expt;
	
	public DirectExptResourceImpl(final XnatProjectdata proj,final XnatExperimentdata expt,final boolean overwrite, final UserI user, final EventMetaI ci){
		super(overwrite,user,ci);
		this.proj=proj;
		this.expt=expt;
	}
	
	public XnatProjectdata getProject(){
		return proj;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xnat.helpers.resource.direct.DirectResourceModifierA#buildDestinationPath()
	 */
	@Override
	public String buildDestinationPath() throws InvalidArchiveStructure,UnknownPrimaryProjectException {
		return FileUtils.AppendRootPath(expt.getCurrentSessionFolder(true),"RESOURCES/");
	}

	/* (non-Javadoc)
   * @see org.nrg.xnat.helpers.resource.direct.DirectResourceModifierA#addResource(org.nrg.xdat.om.XnatResource, org.nrg.xdat.security.UserI)
   */
   @Override
   public boolean addResource(XnatResource resource, final String type, UserI user) throws Exception {
       //create a light copy of the experiment, and save that.
       XFTItem item = XFTItem.NewItem(expt.getXSIType(), user);
       XnatExperimentdata new_expt=(XnatExperimentdata) BaseElement.GetGeneratedItem(item);

       new_expt.setId(expt.getId());
       new_expt.setLabel(expt.getLabel());
       new_expt.setProject(expt.getProject());
       if (expt instanceof XnatSubjectassessordata) {
           ((XnatSubjectassessordata) new_expt).setSubjectId(((XnatSubjectassessordata) expt).getSubjectId());
       } else if (expt instanceof XnatImageassessordata) {
           ((XnatImageassessordata) new_expt).setImagesessionId(((XnatImageassessordata) expt).getImagesessionId());            
       }
       new_expt.setResources_resource(resource);

       SaveItemHelper.authorizedSave(new_expt,user, false, false,ci);

       return true;
   }



	/* (non-Javadoc)
	 * @see org.nrg.xnat.helpers.resource.direct.ResourceModifierA#getResourceById(java.lang.Integer)
	 */
	@Override
	public XnatAbstractresourceI getResourceById(Integer i, final String type) {
		for(XnatAbstractresourceI res: this.expt.getResources_resource()){
			if(res.getXnatAbstractresourceId().equals(i)){
				return res;
			}
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xnat.helpers.resource.direct.ResourceModifierA#getResourceByLabel(java.lang.String)
	 */
	@Override
	public XnatAbstractresourceI getResourceByLabel(String lbl, final String type) {
		for(XnatAbstractresourceI res: this.expt.getResources_resource()){
			if(StringUtils.isNotEmpty(res.getLabel()) && res.getLabel().equals(lbl)){
				return res;
			}
		}
		
		return null;
	}
}
