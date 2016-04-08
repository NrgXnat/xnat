/*
 * org.nrg.xdat.om.base.BaseXnatPvisitdata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatExperimentdataShareI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.auto.AutoXnatPvisitdata;
import org.nrg.xdat.security.SecurityValues;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.identifier.IDGeneratorFactory;
import org.nrg.xft.identifier.IDGeneratorI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.XftStringUtils;

public class BaseXnatPvisitdata extends AutoXnatPvisitdata implements Comparable {

	public ArrayList<ItemI> getVisits()
	{
		return null;
	
	}
	
	public BaseXnatPvisitdata(ItemI item)
	{
		super(item);
	}

	public BaseXnatPvisitdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatHdscandata(UserI user)
	 **/
	public BaseXnatPvisitdata()
	{}

	public BaseXnatPvisitdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

	
    public static XnatPvisitdata GetVisitByProjectIdentifier(String project, String identifier,UserI user,boolean preLoad){
        if(StringUtils.isBlank(identifier)){
        	return null;
        }
        
    	CriteriaCollection cc=new CriteriaCollection("OR");
            	
    	CriteriaCollection subcc1 = new CriteriaCollection("AND");
        subcc1.addClause("xnat:pvisitData/project", project);
        subcc1.addClause("xnat:pvisitData/label", identifier);
        
        cc.add(subcc1);
            	
    	CriteriaCollection subcc2 = new CriteriaCollection("AND");
    	subcc2.addClause("xnat:pvisitData/sharing/share/project", project);
    	subcc2.addClause("xnat:pvisitData/sharing/share/label", identifier);
        
        cc.add(subcc2);

        ArrayList al =  XnatPvisitdata.getXnatPvisitdatasByField(cc, user, preLoad);
        al = BaseElement.WrapItems(al);
        if (al.size()>0){
           return (XnatPvisitdata)al.get(0);
        }else{
            return null;
        }

    }
    
 	public XnatSubjectdata getSubjectData()
 	{
 		XnatSubjectdata subject = null;
 	    
         if (getSubjectId()!=null)
         {
             ArrayList al = XnatSubjectdata.getXnatSubjectdatasByField("xnat:subjectData/ID",this.getSubjectId(),this.getUser(),false);
             if (al.size()>0)
             {
                 subject = (XnatSubjectdata)al.get(0);
             }
         }
 	    
 	    return subject;
 	}
    
    public static synchronized String CreateNewID() throws Exception{
    	IDGeneratorI generator = IDGeneratorFactory.GetIDGenerator("org.nrg.xnat.turbine.utils.IDGenerator");
    	generator.setTable("xnat_pvisitData");
    	generator.setDigits(5);
    	generator.setColumn("id");
    	return generator.generateIdentifier();
    }
	
    public String delete(BaseXnatProjectdata proj, UserI user, boolean removeFiles,EventMetaI c){
    	BaseXnatPvisitdata visit=this;
    	if(this.getItem().getUser()!=null){
    		visit=new BaseXnatPvisitdata(this.getCurrentDBVersion(true));
    	}
    	
    	String msg=visit.canDelete(proj,user);

    	if(msg!=null){
    		logger.error(msg);
    		return msg;
    	}
    	
    	if(visit.getProject() != null && !visit.getProject().equals(proj.getId())){
			try {
				SecurityValues values = new SecurityValues();
				values.put(this.getXSIType() + "/project", proj.getId());
				
				if (!Permissions.canDelete(user,visit) && !Permissions.canDelete(user,this.getSchemaElement(),values))
				{
					return "User cannot delete experiments for project " + proj.getId();
				}
				
				int index = 0;
				int match = -1;
				for(XnatExperimentdataShareI pp : visit.getSharing_share()){
					if(pp.getProject().equals(proj.getId())){
						SaveItemHelper.authorizedRemoveChild(visit.getItem(), "xnat:experimentData/sharing/share", ((XnatExperimentdataShare)pp).getItem(), user,c);
						match=index;
						break;
					}
					index++;
				}
				
				if(match==-1)return null;
				
				//TODO:at this point, retrieve all experiments associated with this visit and disassociate them.
				
				this.removeSharing_share(match);
				return null;
			} catch (SQLException e) {
				logger.error("",e);
				return e.getMessage();
			} catch (Exception e) {
				logger.error("",e);
				return e.getMessage();
			}
		}else{
			try {
			
				if(!Permissions.canDelete(user,this)){
					return "User account doesn't have permission to delete this experiment.";
				}
							
				if(removeFiles){
					this.deleteFiles(user,c);
				}
				
				String visitId=visit.getId();
		        
				//find any experiments that reference this visit and delete the reference.
				try {
					
					final QueryOrganizer qo = new QueryOrganizer("xnat:experimentdata",user,ViewManager.ALL);
					qo.addField("xnat:experimentdata/ID");

					CriteriaCollection where=new CriteriaCollection("AND");

					CriteriaCollection cc= new CriteriaCollection("OR");
					
					CriteriaCollection cc2=new CriteriaCollection("AND");
					cc2.addClause("xnat:experimentdata/sharing/share/project", proj.getId());
					cc2.addClause("xnat:experimentdata/sharing/share/visit", visit.getId());
					
					CriteriaCollection cc3=new CriteriaCollection("AND");
					cc3.addClause("xnat:experimentdata/project", proj.getId());
					cc3.addClause("xnat:experimentdata/visit", visit.getId());
					
					cc.addClause(cc2);
					cc.addClause(cc3);
					
					where.addClause(cc);
					qo.setWhere(where);

					String query=qo.buildQuery();
					
					String exptIdColumn= qo.getFieldAlias("xnat:experimentdata/ID");

					XFTTable table=XFTTable.Execute(query, user.getDBName(), user.getUsername());
					//TODO: these are the visits we're going to have to disassociate.
					table.resetRowCursor();
	                while (table.hasMoreRows())
	                {
	                	final Hashtable row = table.nextRowHash();
	                	
	                    try {
	   	                    final Object exptID = row.get(exptIdColumn.toLowerCase());  //fun, we have to do a toLowerCase because getFieldAlias returns a different case than Execute creates. Hopefully, it will always lowercase it. I am guessing.
	                        //grab the experiment, clear out visit and save...
	   	                    XnatExperimentdata frank = XnatExperimentdata.getXnatExperimentdatasById(exptID.toString(), user, true);    
	   	                    if(org.apache.commons.lang.StringUtils.equalsIgnoreCase(frank.getVisit(), visitId)){
                                XnatExperimentdata new_expt = frank.getLightCopy();
                                new_expt.setProperty("xnat:experimentdata/visit", "NULL");
	   	                    	SaveItemHelper.authorizedSave(new_expt, user, false, false, c);
	   	                    } else {
	   	                    	//the visit must be in the shared project, then. So, we need to clear that out...
	   	                    	//TODO: implement this.
	   	                    	throw new Exception("Unable to delete a shared project's visit from an experiment because the feature is not implemented.");
	   	                    }
	                    } catch (XFTInitException e) {
	                        logger.error("",e);
	                    } catch (ElementNotFoundException e) {
	                        logger.error("",e);
	                    }
	                }
					
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (DBPoolException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
					

				SaveItemHelper.authorizedDelete(visit.getItem().getCurrentDBVersion(), user,c);

				Users.clearCache(user);
				MaterializedView.deleteByUser(user);
				
			} catch (SQLException e) {
				logger.error("",e);
				return e.getMessage();
			} catch (Exception e) {
				logger.error("",e);
				return e.getMessage();
			}
		}
    	return null;
    }
    
	public int hashCode() {
        return new HashCodeBuilder(11, 37). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
        	append(this.getVisitName()).
        	append(this.getSubjectId()).
        	append(this.getVisitType()).
            
            toHashCode();
    }

	public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        BaseXnatPvisitdata rhs = (BaseXnatPvisitdata) obj;
        return new EqualsBuilder().
            // if deriving: appendSuper(super.equals(obj)).
        	append(this.getVisitName(), rhs.getVisitName()).
        	append(this.getSubjectId(), rhs.getSubjectId()).
            append(this.getVisitType(), rhs.getVisitType()).
            
            isEquals();
    }

	@Override
	public int compareTo(Object obj) {
		BaseXnatPvisitdata rhs = (BaseXnatPvisitdata) obj;
		
		return new CompareToBuilder().
				//if deriving: .appendSuper(super.compareTo(o)).
				append(this.getVisitName(), rhs.getVisitName()).
				append(this.getSubjectId(), rhs.getSubjectId()).
				append(this.getVisitType(), rhs.getVisitType()).
	            toComparison();
	}
}
