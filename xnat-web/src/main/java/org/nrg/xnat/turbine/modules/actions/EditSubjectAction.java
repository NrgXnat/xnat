/*
 * web: org.nrg.xnat.turbine.modules.actions.EditSubjectAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.turbine.modules.ScreenLoader;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatProjectparticipantI;
import org.nrg.xdat.model.XnatSubjectdataAddidI;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.modules.screens.EditScreenA;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.nrg.xnat.utils.WorkflowUtils;

/**
 * @author Tim
 *
 */
public class EditSubjectAction extends SecureAction {
    static Logger logger = Logger.getLogger(EditSubjectAction.class);

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @SuppressWarnings("deprecation")
    public void doPerform(RunData data, Context context) throws Exception {
        final UserI user = TurbineUtils.getUser(data);
        ItemI found = null;

        if (TurbineUtils.HasPassedParameter("tag", data)){
            context.put("tag", TurbineUtils.GetPassedParameter("tag", data));
        }
        try {
            final EditScreenA screen = (EditScreenA) ScreenLoader.getInstance().getInstance("XDATScreen_edit_xnat_subjectData");
            
            final XFTItem newItem = (XFTItem)screen.getEmptyItem(data);
            
           // TurbineUtils.OutputDataParameters(data);
            
            final PopulateItem populater = PopulateItem.Populate(data,"xnat:subjectData",true,newItem);
            boolean hasError = false;
            String message = null;
            
            if (populater.hasError())
            {
                hasError = true;
                message = populater.getError().getMessage();
            }
            
            found = populater.getItem();

            if (hasError)
            {
                TurbineUtils.SetEditItem(found,data);
                System.out.println("'" + message + "'");
                if (! message.endsWith("/addID : Required Field"))
                {
            	    data.addMessage(message);
            	    if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
            	    {
            	        data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
            	    }
                    if(TurbineUtils.HasPassedParameter("destination", data))data.getParameters().add("destination", (String)TurbineUtils.GetPassedParameter("destination", data));
            	    return;
                }
            }
            
            final Iterator addIDS = found.getChildItems("xnat:subjectData/addID").iterator();
            while (addIDS.hasNext())
            {
                final ItemI addID = (ItemI)addIDS.next();
                if (addID.getProperty("addID") == null)
                {
                    addID.setProperty("name",null);
                }
            }
            
            final Iterator addFields = found.getChildItems("xnat:subjectData/fields/field").iterator();
            while (addFields.hasNext())
            {
                final ItemI addID = (ItemI)addFields.next();
                if (addID.getProperty("field") == null)
                {
                    addID.setProperty("name",null);
                }
            }
            
        
            ((XFTItem)found).removeEmptyItems();
            
            logger.debug("EditSubjectAction: \n" + found.toString());
      // System.out.println("EditSubjectAction: PRE DOB:" + found.getProperty("dob"));
            //System.out.println("EditSubjectAction: PRE YOB:" + found.getProperty("yob"));
            
            if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("dob_estimated",data))!=null)
            {
            	final Date year = (Date)found.getProperty("demographics/dob");
                found.setProperty("demographics/yob",new Integer(year.getYear() + 1900));
                found.setProperty("demographics/dob",null);
            }
            
//            if((found.getProperty("demographics/yob")!=null && !found.getProperty("demographics/yob").equals("NULL"))
//            		|| (found.getProperty("demographics/age")!=null && !found.getProperty("demographics/age").equals("NULL"))){
//                found.setProperty("demographics/dob",null);
//            }
            if((found.getProperty("demographics/dob")!=null && !found.getProperty("demographics/dob").equals("NULL"))
            && (found.getProperty("demographics/yob")!=null && !found.getProperty("demographics/yob").equals("NULL"))
            && (found.getProperty("demographics/age")!=null && !found.getProperty("demographics/age").equals("NULL"))) {
                String dobSelection = (String) org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("dob_group", data);
                if ("dob-input".equals(dobSelection)) {
                    found.setProperty("demographics/yob", "NULL");
                    found.setProperty("demographics/age", "NULL");
                }
                if ("yob-input".equals(dobSelection)) {
                    found.setProperty("demographics/dob", "NULL");
                    found.setProperty("demographics/age", "NULL");
                }
                if ("age-input".equals(dobSelection)) {
                    found.setProperty("demographics/dob", "NULL");
                    found.setProperty("demographics/yob", "NULL");
                }
            }

            //System.out.println("EditSubjectAction: POST DOB:" + found.getProperty("dob"));
            //System.out.println("EditSubjectAction: POST YOB:" + found.getProperty("yob"));
            
            final XnatSubjectdata subject = new XnatSubjectdata(found);

            final CriteriaCollection cc = new CriteriaCollection("OR");
            
//          --BEGIN CHANGE
            if (subject.getLabel()!=null && subject.getProject()!=null){
                final CriteriaCollection subcc = new CriteriaCollection("AND");
                subcc.addClause("xnat:subjectData/project",subject.getProject());
                subcc.addClause("xnat:subjectData/label",subject.getLabel());
                cc.add(subcc);
            }
            //--END CHANGE
            
            if (subject.getAddid().size()>0)
            {
                for (final XnatSubjectdataAddidI addID:subject.getAddid())
                {
                    final CriteriaCollection subCC = new CriteriaCollection("AND");
                    subCC.addClause("xnat:subjectData/addID/name",addID.getName());
                    subCC.addClause("xnat:subjectData/addID/addID",addID.getAddid());
                    cc.add(subCC);
                }
            }
            
            if (!subject.getSharing_share().isEmpty())
            {
                for (final XnatProjectparticipantI addID: subject.getSharing_share())
                {
                    final CriteriaCollection subCC = new CriteriaCollection("AND");
                    if (addID.getLabel()!=null){
                        subCC.addClause("xnat:subjectData/sharing/share/project",addID.getProject());
                        subCC.addClause("xnat:subjectData/sharing/share/label",addID.getLabel());
                        cc.add(subCC);
                    }
                }
            }
            
            if (cc.size()>0)
            {
                final ItemCollection items = ItemSearch.GetItems("xnat:subjectData",cc,TurbineUtils.getUser(data),false);
                
                if (subject.getId()==null)
                {
                    //new item
                    if (items.size() >0)
                    {
                        final ArrayList matches = BaseElement.WrapItems(items.getItems());
                                                
                        context.put("matches",matches);
                        TurbineUtils.SetEditItem(found,data);
                        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
                        {
                            data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
                        }
                        if(TurbineUtils.HasPassedParameter("destination", data))data.getParameters().add("destination", (String)TurbineUtils.GetPassedParameter("destination", data));
                        data.addMessage("Matched previous subject. Save aborted.");
                        return;
                    }
                }else{
                    
                    if (items.size() >0)
                    {
                        if (items.size() > 1)
                        {
                            final ArrayList matches = BaseElement.WrapItems(items.getItems());
                            context.put("matches",matches);
                            TurbineUtils.SetEditItem(found,data);
                            if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
                            {
                                data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
                            }
                            if(TurbineUtils.HasPassedParameter("destination", data))data.getParameters().add("destination", (String)TurbineUtils.GetPassedParameter("destination", data));
                            data.addMessage("Matched previous subject. Save aborted.");
                            return;
                        }else{
                            final ItemI match = (ItemI)items.getFirst();
                            
                            if (! match.getStringProperty("xnat:subjectData.ID").equalsIgnoreCase(found.getStringProperty("xnat:subjectData.ID")))
                            {
                                final ArrayList matches = new ArrayList();
                                
                                matches.add(BaseElement.GetGeneratedItem(match));
                                
                                context.put("matches",matches);
                                TurbineUtils.SetEditItem(found,data);
                                if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
                                {
                                    data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
                                }
                                if(TurbineUtils.HasPassedParameter("destination", data))data.getParameters().add("destination", (String)TurbineUtils.GetPassedParameter("destination", data));
                                data.addMessage("Matched previous subject. Save aborted.");
                                return;
                            }
                        }
                    }
                }
            }
           
            if (! Permissions.canCreate(TurbineUtils.getUser(data),found))
            {
                TurbineUtils.SetEditItem(found,data);
                if(TurbineUtils.HasPassedParameter("destination", data))data.getParameters().add("destination", (String)TurbineUtils.GetPassedParameter("destination", data));
                data.addMessage("Invalid create permissions for this item.");
                if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
                {
                    data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
                }
                return;
            }
            
            boolean isNew=true;
            if (subject.getId()==null)
            {
                //ASSIGN A PARTICIPANT ID
                String s = XnatSubjectdata.CreateNewID();
                found.setProperty("xnat:subjectData.ID",s);
            }
            
            if(found.getCurrentDBVersion(false)!=null){
                isNew=false;
            }
            
            boolean removedReference = false;
            XFTItem first = (XFTItem)found;
            
            PersistentWorkflowI wrk = WorkflowUtils.getOrCreateWorkflowData(null, user, first.getXSIType(), subject.getId(), subject.getProject(), newEventInstance(data, EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(subject.getXSIType(), isNew)));
            EventMetaI ci=wrk.buildEvent();

            Object[] keysArray = data.getParameters().getKeys();
            for (int i=0;i<keysArray.length;i++)
            {
                final String key = (String)keysArray[i];
                if (key.toLowerCase().startsWith("remove_"))
                {
                    final int index = key.indexOf("=");
                    final String field = key.substring(index+1);
                    final Object value = org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(key,data);
                    logger.debug("FOUND REMOVE: " + field + " " + value);
                    final ItemCollection items =ItemSearch.GetItems(field,value,TurbineUtils.getUser(data),false);
                    if (items.size() > 0)
                    {
                    	wrk.setPipelineName("Remove Item");
                    	PersistentWorkflowUtils.save(wrk, ci);
                    	
                    	try {
							final ItemI toRemove = items.getFirst();
                    	SaveItemHelper.unauthorizedRemoveChild(first.getItem(),null,toRemove.getItem(),TurbineUtils.getUser(data),ci);
							first.removeItem(toRemove);
							removedReference = true;
							PersistentWorkflowUtils.complete(wrk,ci);
						} catch (Exception e) {
							PersistentWorkflowUtils.fail(wrk,ci);
						}
                    }else{
                        logger.debug("ITEM NOT FOUND:" + key + "="+ value);
                    }
                }
            }

            if (removedReference)
            {
                data.getSession().setAttribute("edit_item",first);
                if(TurbineUtils.HasPassedParameter("destination", data))data.getParameters().add("destination", (String)TurbineUtils.GetPassedParameter("destination", data));
                if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
                {
                    data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
                }
                return;
            }
            
            final ValidationResultsI vr = found.validate();
            
            if (vr != null && !vr.isValid())
            {
                TurbineUtils.SetEditItem(first,data);
                context.put("vr",vr);
                if(TurbineUtils.HasPassedParameter("destination", data))data.getParameters().add("destination", (String)TurbineUtils.GetPassedParameter("destination", data));
                if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
                {
                    data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
                }
            }else{
            	try {
                	PersistentWorkflowUtils.save(wrk, ci);
            		
            		XnatSubjectdata sub=new XnatSubjectdata(found);
            		if(!Permissions.canEdit(user, sub)){
            			error(new InvalidPermissionException("Unable to save subject " + sub.getId()),data);
            		}
            		
            
            		this.preSave(user, sub, TurbineUtils.GetDataParameterHash(data),wrk.buildEvent());
            		
            		SaveItemHelper.authorizedSave(sub,TurbineUtils.getUser(data),false,false,ci);            		
            		PersistentWorkflowUtils.complete(wrk,ci);
            		
					MaterializedView.deleteByUser(user);
					
            		ItemI temp1 =found.getCurrentDBVersion(false);
            		if (temp1 != null)
            		{
            		    first = (XFTItem)temp1;
            		}
            	} catch (Exception e) {
            		PersistentWorkflowUtils.fail(wrk,ci);
            		logger.error("Error Storing " + found.getXSIType(),e);
            		
            		data.setMessage("Error Saving item.");
                    TurbineUtils.SetEditItem(found,data);
                    if(TurbineUtils.HasPassedParameter("destination", data))data.getParameters().add("destination", (String)TurbineUtils.GetPassedParameter("destination", data));
                    
                    if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
                    {
                        data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
                    }
                    return;
            	}
            	final SchemaElement se = SchemaElement.GetElement(first.getXSIType());
            	data = TurbineUtils.setDataItem(data,first);
            	data = TurbineUtils.SetSearchProperties(data,first);
            	if (TurbineUtils.HasPassedParameter("source", data))
            	{
                	data.setScreenTemplate((String)TurbineUtils.GetPassedParameter("source", data));
                	data.getParameters().add("confirmed","true");
            	}else if(TurbineUtils.HasPassedParameter("destination", data)){
                    this.redirectToReportScreen((String)TurbineUtils.GetPassedParameter("destination", data), first, data);
            	}else{
                    this.redirectToReportScreen(first, data);
            	}
            }
        } catch (Exception e) {
            logger.error("",e);
            data.setMessage(e.getMessage());
            if(TurbineUtils.HasPassedParameter("destination", data))data.getParameters().add("destination", (String)TurbineUtils.GetPassedParameter("destination", data));
            TurbineUtils.SetEditItem(found,data);
            if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
            {
                data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
            }
        }
    }

	public interface PreSaveAction {
		public void execute(UserI user, XnatSubjectdata src, Map<String,String> params,EventMetaI event) throws Exception;
	}
	
	private void preSave(UserI user, XnatSubjectdata src, Map<String,String> params,EventMetaI event) throws Exception{
		 List<Class<?>> classes = Reflection.getClassesForPackage("org.nrg.xnat.actions.subjectEdit.preSave");

    	 if(classes!=null && classes.size()>0){
			 for(Class<?> clazz: classes){
				 if(PreSaveAction.class.isAssignableFrom(clazz)){
					 PreSaveAction action=(PreSaveAction)clazz.newInstance();
					 action.execute(user,src,params,event);
				 }
			 }
		 }
	}
}
