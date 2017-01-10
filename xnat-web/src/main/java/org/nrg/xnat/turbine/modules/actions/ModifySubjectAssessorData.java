/*
 * web: org.nrg.xnat.turbine.modules.actions.ModifySubjectAssessorData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatSubjectassessordata;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.actions.DisplayItemAction;
import org.nrg.xdat.turbine.modules.actions.ModifyItem;
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
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.nrg.xnat.utils.WorkflowUtils;

public class ModifySubjectAssessorData extends ModifyItem {
    static Logger logger = Logger.getLogger(ModifySubjectAssessorData.class);

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doPerform(RunData data, Context context) throws Exception {
       XFTItem found = null;
        try {
            String element0 = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("element_0",data));
            if (element0==null){
                this.handleException(data, null, new Exception("Configuration Exception.<br><br> Please create an &lt;input&gt; with name 'ELEMENT_0' and value 'SOME XSI:TYPE' in your form.  This will tell the Submit action, which data type it is looking for."));
            }
            
            PopulateItem populater = PopulateItem.Populate(data,element0,false);
            
            found = populater.getItem();

            if (populater.hasError())
            {
                handleException(data,(XFTItem)found,populater.getError());
                return;
            }
            
            try {
                preProcess(found,data,context);
            } catch (RuntimeException e1) {
                logger.error("",e1);
            }

            XFTItem dbVersion = found.getCurrentDBVersion();
            if(dbVersion==null){
            	if(StringUtils.isNotEmpty(found.getStringProperty("project")) && StringUtils.isNotEmpty(found.getStringProperty("label"))){
            		//check for match by label
                	XnatExperimentdata expt=XnatExperimentdata.GetExptByProjectIdentifier(found.getStringProperty("project"), found.getStringProperty("label"), TurbineUtils.getUser(data), false);
                	if(expt!=null){
                        logger.error("Duplicate experiment with label "+ found.getStringProperty("label"));
                        data.setMessage("Please use a unique session ID.  "+ found.getStringProperty("label") +" is already in use.");
                        handleException(data,(XFTItem)found,null);
                	}
            	}
            }
            
            PersistentWorkflowI wrk=null;
            if(dbVersion!=null){
                try {
                    dynamicCompare(TurbineUtils.getUser(data), dbVersion, found);
                }
                catch (CompareException e) {
                    data.setMessage(e.getMessage());
                    handleException(data, found, null);
                    return;
                }

            	wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, TurbineUtils.getUser(data), found,newEventInstance(data, EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(found.getXSIType(), dbVersion==null)));
    	    	EventMetaI c=wrk.buildEvent();
                boolean removedReference = false;
                Object[] keysArray = data.getParameters().getKeys();
                try {
    				for (int i=0;i<keysArray.length;i++)
    				{
    				    String key = (String)keysArray[i];
    				    if (key.toLowerCase().startsWith("remove_"))
    				    {
    				        int index = key.indexOf("=");
    				        String field = key.substring(index+1);
                        Object value = org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(key,data);
    				        logger.debug("FOUND REMOVE: " + field + " " + value);
    				        ItemCollection items =ItemSearch.GetItems(field,value,TurbineUtils.getUser(data),false);
    				        if (items.size() > 0)
    				        {
    				            ItemI toRemove = items.getFirst();
                            SaveItemHelper.unauthorizedRemoveChild(dbVersion.getItem(),null,toRemove.getItem(),TurbineUtils.getUser(data),c);
    				            found.removeItem(toRemove);
    				            removedReference = true;
    				        }else{
    				            logger.debug("ITEM NOT FOUND:" + key + "="+ value);
    				        }
    				    }
    				}
    			} catch (Exception e1) {
    				WorkflowUtils.fail(wrk, c);
    				throw e1;
    			}

                if (removedReference)
                {
                    TurbineUtils.SetEditItem(found,data);
                    if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
                    {
                        data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
                    }
                    WorkflowUtils.complete(wrk, c);
                    return;
                }
            }else{
            	found.setProperty("ID", XnatExperimentdata.CreateNewID());
            	
            	wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, TurbineUtils.getUser(data), found,newEventInstance(data, EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(found.getXSIType(), dbVersion==null)));
            }
            
            XnatSubjectassessordata sa = (XnatSubjectassessordata)BaseElement.GetGeneratedItem(found);
            
            
            if (sa.getProject()!=null){
                data.getParameters().setString("project", sa.getProject());
            }
            
            ValidationResultsI vr = found.validate();
            EventMetaI c=wrk.buildEvent();
            
            if (vr.isValid())
            {
                try {
                    if(!Permissions.canEdit(TurbineUtils.getUser(data), sa)){
                    	error(new InvalidPermissionException("Unable to modify experient "+ sa.getId()),data);
                    	return;
                    }
                	
                    try {
                        preSave(found,data,context);
                    } catch (CriticalException e) {
                        throw e;
                    } catch (RuntimeException e) {
                        logger.error("",e);
                        throw e;
                    }
                	
                    try {
                    	dynamicPreSave(TurbineUtils.getUser(data),sa,TurbineUtils.GetDataParameterHash(data), wrk);
                    } catch (CriticalException e) {
                        throw e;
                    } catch (RuntimeException e) {
                        logger.error("",e);
                        throw e;
                    }
                    
                    wrk.setLaunchTime(Calendar.getInstance().getTime());
                    
                    try {
                    SaveItemHelper.authorizedSave(found,TurbineUtils.getUser(data),false,allowDataDeletion(),c);
					} catch (Exception e1) {
						WorkflowUtils.fail(wrk, c);
						throw e1;
					}
                    
                    WorkflowUtils.complete(wrk, c);
                    
            		try {
            			MaterializedView.deleteByUser(TurbineUtils.getUser(data));
            		} catch (DBPoolException e) {
                        logger.error("",e);
            		}

                    found = found.getCurrentDBVersion(false);

					postProcessing(found, data, context);
					
					dynamicPostSave(TurbineUtils.getUser(data),sa,TurbineUtils.GetDataParameterHash(data), wrk);

                    SchemaElement se = SchemaElement.GetElement(found.getXSIType());
                    if (TurbineUtils.HasPassedParameter("destination", data)){
                        super.redirectToReportScreen((String)TurbineUtils.GetPassedParameter("destination", data), found, data);
                    }else{
                        redirectToReportScreen(DisplayItemAction.GetReportScreen(se), found, data);
                    }
                } catch (Exception e) {
                    handleException(data,(XFTItem)found,e);
                }
            }else{
                TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
                logger.error(vr.toString());
                context.put("vr",vr);
                handleException(data,(XFTItem)found,null);
            }
        } catch (Exception e) {
            logger.error("",e);
            data.setMessage("Error: Item save failed.  See log for details.");
            handleException(data,(XFTItem)found,null);
        }
    }

    public interface CompareAction {
        public void execute(UserI user, XFTItem from, XFTItem to) throws Exception ;
    }

    private void dynamicCompare(UserI user, XFTItem from, XFTItem to) throws Exception{
        List<Class<?>> classes = Reflection.getClassesForPackage("org.nrg.xnat.actions.sessionEdit.compare");

        if(classes!=null && classes.size()>0){
            for(Class<?> clazz: classes){
                if(CompareAction.class.isAssignableFrom(clazz)){
                    CompareAction action=(CompareAction)clazz.newInstance();
                    action.execute(user,from,to);
                }
            }
        }
    }

    public static class CompareException extends Exception {
        public CompareException(String message) {
            super(message);
        }
    }

	public interface PreSaveAction {
		public void execute(UserI user, XnatSubjectassessordata src, Map<String,String> params,PersistentWorkflowI wrk) throws Exception;
	}
	
	private void dynamicPreSave(UserI user, XnatSubjectassessordata src, Map<String,String> params,PersistentWorkflowI wrk) throws Exception{
		 List<Class<?>> classes = Reflection.getClassesForPackage("org.nrg.xnat.actions.sessionEdit.preSave");

    	 if(classes!=null && classes.size()>0){
			 for(Class<?> clazz: classes){
				 if(PreSaveAction.class.isAssignableFrom(clazz)){
					 PreSaveAction action=(PreSaveAction)clazz.newInstance();
					 action.execute(user,src,params,wrk);
				 }
			 }
		 }
	}

	public interface PostSaveAction {
		public void execute(UserI user, XnatSubjectassessordata src, Map<String,String> params,PersistentWorkflowI wrk) throws Exception;
	}
	
	private void dynamicPostSave(UserI user, XnatSubjectassessordata src, Map<String,String> params,PersistentWorkflowI wrk) throws Exception{
		 List<Class<?>> classes = Reflection.getClassesForPackage("org.nrg.xnat.actions.sessionEdit.postSave");

    	 if(classes!=null && classes.size()>0){
			 for(Class<?> clazz: classes){
				 if(PostSaveAction.class.isAssignableFrom(clazz)){
					 PostSaveAction action=(PostSaveAction)clazz.newInstance();
					 action.execute(user,src,params,wrk);
				 }
			 }
		 }
	}
    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.actions.ModifyItem#preProcess(org.nrg.xft.XFTItem, org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @Override
    public void preProcess(XFTItem item, RunData data, Context context) {
        super.preProcess(item, data, context);
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.actions.ModifyItem#handleException(org.apache.turbine.util.RunData, org.nrg.xft.XFTItem, java.lang.Throwable)
     */
    @Override
    public void handleException(RunData data, XFTItem first, Throwable error) {

        try {
            if (first!=null){
                String part_id = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("part_id",data));
                if (part_id==null){
                    if (first.getStringProperty("subject_ID")!=null){
                        part_id = first.getStringProperty("subject_ID");
                    }
                }
                if (part_id!=null){
                    ItemI part = ItemSearch.GetItems("xnat:subjectData.ID",part_id,TurbineUtils.getUser(data),false).getFirst();
                    TurbineUtils.SetParticipantItem(part,data);
                }
                TurbineUtils.SetEditItem(first,data);
            }
            if (error!=null)
                data.setMessage(error.getMessage());
            if (first!=null)
                data.setScreenTemplate("XDATScreen_edit_" + first.getGenericSchemaElement().getFormattedName() + ".vm");
            else
                data.setScreenTemplate("Index.vm");
        } catch (Exception e) {
            logger.error("",e);
            super.handleException(data, first, e);
        }
    }
    
    
}
