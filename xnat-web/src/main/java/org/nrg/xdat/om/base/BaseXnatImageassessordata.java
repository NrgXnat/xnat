/*
 * org.nrg.xdat.om.base.BaseXnatImageassessordata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 12:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.auto.AutoXnatImageassessordata;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.StringUtils;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
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

    
    public boolean validateSessionId(){
        String session_id = this.getImagesessionId();
        
        if (session_id!=null){
			session_id=StringUtils.RemoveChar(session_id, '\'');
            String query = "SELECT ID FROM xnat_imageSessiondata WHERE ID='";
            String login =null;
            if (this.getUser()!=null){
                login = this.getUser().getUsername();
            }
            
            try {
               final String idCOUNT= (String)PoolDBUtils.ReturnStatisticQuery(query + session_id + "';", "id", this.getDBName(), login);
                if (idCOUNT!=null){
                    return true;
                }
                
                final String project = this.getProject();
                if (project!=null){
                	query = "SELECT id FROM xnat_experimentData WHERE label='" +
                    session_id +"' AND project='" + project + "';";
		            String new_session_id= (String)PoolDBUtils.ReturnStatisticQuery(query, "id", this.getDBName(), login);
		            if (new_session_id!=null){
		                this.setImagesessionId(new_session_id);
		                return true;
		            }
                	
                    query = "SELECT sharing_share_xnat_experimentda_id FROM xnat_experimentData_share WHERE label='" +
                            session_id +"' AND project='" + project + "';";
                    new_session_id= (String)PoolDBUtils.ReturnStatisticQuery(query, "sharing_share_xnat_experimentda_id", this.getDBName(), login);
                    if (new_session_id!=null){
                        this.setImagesessionId(new_session_id);
                        return true;
                    }
                }
            } catch (SQLException e) {
                logger.error("",e);
            } catch (Exception e) {
                logger.error("",e);
            }
        }
        
        return false;
    }
    
    public ArrayList getCatalogSummary() throws Exception{
		String query="SELECT xnat_abstractresource_id,label,element_name ";
    	query+=", 'resources'::TEXT AS category, '" + this.getId()+"'::TEXT AS cat_id";
		query+=" FROM xnat_experimentdata_resource map " +
		" LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id" +
		" LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id";
		query+= " WHERE xnat_experimentdata_id='"+this.getId() + "'";
		
		XFTTable t = XFTTable.Execute(query, this.getDBName(), "system");
		
		return t.rowHashs();
    }
    
    
    public void deleteFiles(UserI u, EventMetaI ci) throws Exception{
    	super.deleteFiles(u,ci);
    	
    	final String rootPath=ArcSpecManager.GetInstance().getArchivePathForProject(this.getProject());
    	
    	for(XnatAbstractresourceI abstRes:this.getResources_resource()){
    		((XnatAbstractresource)abstRes).deleteWithBackup(rootPath, u,ci);
    	}
    	
    	for(XnatAbstractresourceI abstRes:this.getOut_file()){
    		((XnatAbstractresource)abstRes).deleteWithBackup(rootPath, u,ci);
    	}
    	
    	// XNAT-1382: Delete the root Assessor Directory if it is empty.
    	String assessorDir = this.getArchiveDirectoryName();
    	String sessionDir = this.getExpectedSessionDir().getAbsolutePath();
    	if(!StringUtils.IsEmpty(sessionDir) && !StringUtils.IsEmpty(assessorDir)){
    		File f = new File(sessionDir + "/ASSESSORS/" + assessorDir);
    		if(f.exists() && f.isDirectory() && f.list().length == 0){
    			FileUtils.DeleteFile(f);
    		}
    	}
    }


	public File getExpectedSessionDir() throws InvalidArchiveStructure,UnknownPrimaryProjectException{
		return this.getImageSessionData().getExpectedSessionDir();
	}

	@Override
	public void preSave() throws Exception{
		if(StringUtils.IsEmpty(this.getId())){
			throw new IllegalArgumentException("Please specify an ID for your experiment.");
		}	
		
		if(XFT.getBooleanProperty("security.require_image_assessor_labels", false) && StringUtils.IsEmpty(this.getLabel())){
			throw new IllegalArgumentException("Please specify a label for your experiment.");
		}
		
		if(!StringUtils.IsAlphaNumericUnderscore(getId())){
			throw new IllegalArgumentException("Identifiers cannot use special characters.");
		}
		
		if(!StringUtils.IsEmpty(this.getLabel()) && !StringUtils.IsAlphaNumericUnderscore(getLabel())){
			throw new IllegalArgumentException("Labels cannot use special characters.");
		}
		
		if(this.getImageSessionData()==null){
			throw new Exception("Unable to identify image session for:" + this.getImagesessionId());
		}
		
		final XnatProjectdata proj = this.getPrimaryProject(false);
		if(proj==null){
			throw new Exception("Unable to identify project for:" + this.getProject());
		}
		
		checkUniqueLabel();
		
		final String expectedPath=this.getExpectedSessionDir().getAbsolutePath().replace('\\', '/');
		
		for(final XnatAbstractresourceI res: this.getResources_resource()){
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
	}

	public String getResourceCatalogRootPathByLabel( String label) {
		String rtn = super.getResourceCatalogRootPathByLabel(label);
        if (rtn == null) {
        	//Check if catalog is at the out file level
        	Iterator misc = this.getOut_file().iterator();
            while(misc.hasNext())          {
                Object file = misc.next();
           	    if (file instanceof XnatResourcecatalog) {
           	    	String tag = ((XnatResourcecatalog)file).getLabel();
           	    	if (tag != null && tag.equals(label)) {
           	    		rtn =((XnatResourcecatalog)file).getUri();
           	    		int index = rtn.lastIndexOf("/");
           	    		if (index != -1)
           	    			rtn = rtn.substring(0, index);
           	    		break;
           	    	}
        	    }
            }
        }
        return rtn;
	}

}
