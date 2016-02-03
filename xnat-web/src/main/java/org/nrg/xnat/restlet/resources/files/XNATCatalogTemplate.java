/*
 * org.nrg.xnat.restlet.resources.files.XNATCatalogTemplate
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources.files;

import org.nrg.xdat.om.*;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.utils.StringUtils;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.direct.DirectResourceModifierBuilder;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierBuilderI;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class XNATCatalogTemplate extends XNATTemplate {
	XFTTable catalogs=null;
	
	ArrayList<String> resource_ids=null;
	ArrayList<XnatAbstractresource> resources=new ArrayList<XnatAbstractresource>();
		
	public XNATCatalogTemplate(Context context, Request request,
			Response response,boolean allowAll) {
		super(context, request, response);
		
		String resourceID= (String)getParameter(request,"RESOURCE_ID");

		if(resourceID!=null){
			resource_ids=new ArrayList<String>();
			for(String s:StringUtils.CommaDelimitedStringToArrayList(resourceID, true)){
				resource_ids.add(s);
				}
			}
			
			try {
			catalogs=this.loadCatalogs(resource_ids,true,allowAll);
			} catch (Exception e) {
	            logger.error("",e);
			}
	}
	
	
	public String getBaseURI() throws ElementNotFoundException{
		StringBuffer sb =new StringBuffer("/data");
		if(proj!=null && sub!=null){
			sb.append("/projects/");
			sb.append(proj.getId());
			sb.append("/subjects/");
			sb.append(sub.getId());
		}
		if(recons.size()>0){
			sb.append("/experiments/");
			int aC=0;
			for(XnatExperimentdata assessed:this.assesseds){
				if(aC++>0)sb.append(",");
			sb.append(assessed.getId());
			}
			sb.append("/reconstructions/");
			int sC=0;
			for(XnatReconstructedimagedata recon:recons){
				if(sC++>0)sb.append(",");
			sb.append(recon.getId());
			}
			if(type!=null){
				sb.append("/" + type);
			}
		}else if(scans.size()>0){
			sb.append("/experiments/");
			int aC=0;
			for(XnatExperimentdata assessed:this.assesseds){
				if(aC++>0)sb.append(",");
			sb.append(assessed.getId());
			}
			sb.append("/scans/");
			int sC=0;
			for(XnatImagescandata scan:scans){
				if(sC++>0)sb.append(",");
			sb.append(scan.getId());
			}
		}else if(expts.size()>0){
			if(assesseds.size()>0){
				sb.append("/experiments/");
				int aC=0;
				for(XnatExperimentdata assessed:this.assesseds){
					if(aC++>0)sb.append(",");
				sb.append(assessed.getId());
				}
				sb.append("/assessors/");
				int eC=0;
				for(XnatExperimentdata expt:this.expts){
					if(eC++>0)sb.append(",");
				sb.append(expt.getId());
				}
				if(type!=null){
					sb.append("/" + type);
				}
			}else{
				sb.append("/experiments/");
				int eC=0;
				for(XnatExperimentdata expt:this.expts){
					if(eC++>0)sb.append(",");
				sb.append(expt.getId());
			}
			}
		}else if(sub!=null){
			
		}else if(proj!=null){
			sb.append("/projects/");
			sb.append(proj.getId());
		}
		return sb.toString();
	}



    protected File getFileOnLocalFileSystem(String fullPath) {
        File f = new File(fullPath);
        if (!f.exists()){
            if (!fullPath.endsWith(".gz")){
            	f= new File(fullPath + ".gz");
            	if (!f.exists()){
            		return null;
            	}
            }else{
                return null;
            }
        }
        
        return f;
    }
	
    public XnatResourceInfo buildResourceInfo(EventMetaI ci){
		final String description;
	    if(this.getQueryVariable("description")!=null){
	    	description=this.getQueryVariable("description");
	    }else{
	    	description=null;
	    }
	    
	    final String format;
	    if(this.getQueryVariable("format")!=null){
	    	format=this.getQueryVariable("format");
	    }else{
	    	format=null;
	    }
	    
	    final String content;
	    if(this.getQueryVariable("content")!=null){
	    	content=this.getQueryVariable("content");
	    }else{
	    	content=null;
	    }
	    
	    String[] tags;
	    if(this.getQueryVariables("tags")!=null){
	    	tags = this.getQueryVariables("tags");
	    }else{
	    	tags=null;
	    }
        
	    Date d=EventUtils.getEventDate(ci, false);
		return XnatResourceInfo.buildResourceInfo(description, format, content, tags,user,d,d,EventUtils.getEventId(ci));
	}
			
	protected ResourceModifierA buildResourceModifier(final boolean overwrite,EventMetaI ci) throws Exception{
		XnatImagesessiondata assessed=null;
			
		if(this.assesseds.size()==1)assessed=(XnatImagesessiondata)assesseds.get(0);
			
		//this should allow dependency injection - TO
		final ResourceModifierBuilderI builder=new DirectResourceModifierBuilder();
		
		if(recons.size()>0){
			//reconstruction						
			builder.setRecon(assessed,recons.get(0), type);
		}else if(scans.size()>0){
			//scan
			builder.setScan(assessed, scans.get(0));
		}else if(expts.size()>0){
			final XnatExperimentdata expt=this.expts.get(0);
//			experiment
			
			if(expt.getItem().instanceOf("xnat:imageAssessorData")){
				if(assessed==null){
					assessed=((XnatImageassessordata)expt).getImageSessionData();
				}

				builder.setAssess((XnatImagesessiondata)assessed, (XnatImageassessordata)expt, type);
			}else{
				builder.setExpt((proj!=null)?proj:expt.getProjectData(), expt);
			}
		}else if(sub!=null){
			builder.setSubject(proj, sub);
		}else if(proj!=null){
			builder.setProject(proj);
			}else{
			throw new Exception("Unknown resource");
		}
		
		return builder.buildResourceModifier(overwrite,user,ci);
	}
}
