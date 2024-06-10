/*
 * web: org.nrg.xnat.turbine.modules.actions.ManageEntryAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.log4j.Logger;
import org.apache.turbine.modules.ScreenLoader;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.turbine.modules.actions.ModifyItem;
import org.nrg.xdat.turbine.modules.screens.EditScreenA;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.*;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.nrg.xnat.utils.WorkflowUtils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class ManageEntryAccess extends ModifyItem {
    static Logger logger = Logger.getLogger(ManageEntryAccess.class);

    
    public void save(XFTItem first,RunData data, Context context) throws InvalidItemException,Exception{
        ArrayList<XFTItem>al = first.getChildItems("sharing/share");
        
		PersistentWorkflowI wrk=WorkflowUtils.buildOpenWorkflow(TurbineUtils.getUser(data), first, newEventInstance(data,EventUtils.CATEGORY.DATA,EventUtils.CONFIGURED_PROJECT_SHARING));
		EventMetaI c=wrk.buildEvent();
		PersistentWorkflowUtils.save(wrk, c);
		
        try {
			for (XFTItem i : al){
			    if (first.getGenericSchemaElement().instanceOf("xnat:subjectData")){
			        i.setProperty("subject_id", first.getProperty("ID"));
			    }else{
			        i.setProperty("sharing_share_xnat_experimentda_id", first.getProperty("ID"));
			    }
				            
            Authorizer.getInstance().authorizeSave(first, TurbineUtils.getUser(data));
            
            SaveItemHelper.authorizedSave(i, TurbineUtils.getUser(data),false,false,c);

			}
			WorkflowUtils.complete(wrk, c);
		} catch (Exception e) {
			WorkflowUtils.fail(wrk, c);
			throw e;
		}
    }
    
    public void doPerform(RunData data, Context context) throws Exception
    {
        XFTItem first =null;
        preserveVariables(data,context);
        //TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
        //parameter specifying elementAliass and elementNames
        try {
            String header = "ELEMENT_";
            int counter = 0;
            Hashtable hash = new Hashtable();
            while (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(header + counter,data)) != null)
            {
                String elementToLoad = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(header + counter++,data));
                Integer numberOfInstances = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedInteger(elementToLoad,data));
                if (numberOfInstances != null && numberOfInstances.intValue()!=0)
                {
                    int subCount = 0;
                    while (subCount != numberOfInstances.intValue())
                    {
                        hash.put(elementToLoad + (subCount++),elementToLoad);
                    }
                }else{
                    hash.put(elementToLoad,elementToLoad);
                }
            }
            
            if (hash.size()==0){
                throw new Exception("Missing ELEMENT_0 property.");
            }
            
            String screenName = null;
            if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null && !((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)).equals(""))
            {
                screenName = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)).substring(0,((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)).lastIndexOf(".vm"));
            }
            
            InvalidValueException error = null;
            ArrayList al = new ArrayList();
            Enumeration keys = hash.keys();
            while(keys.hasMoreElements())
            {
                String key = (String)keys.nextElement();
                String element = (String)hash.get(key);
                SchemaElement e = SchemaElement.GetElement(element);
                
                PopulateItem populater = null;
                if (screenName == null)
                {
                    populater = PopulateItem.Populate(data,element,true);
                }else{
                    if (screenName.equals("XDATScreen_edit_" + e.getFormattedName()))
                    {
                        EditScreenA screen = (EditScreenA) ScreenLoader.getInstance().getInstance(screenName);
                        XFTItem newItem = (XFTItem)screen.getEmptyItem(data);
                        populater = PopulateItem.Populate(data,element,true,newItem);
                    }else{
                        populater = PopulateItem.Populate(data,element,true);
                    }
                }
                
                if (populater.hasError())
                {
                    error = populater.getError();
                }
                
                al.add(populater.getItem());
            }
            first = (XFTItem)al.get(0);
            try {
                preProcess(first,data,context);
            } catch (RuntimeException e1) {
                logger.error("",e1);
            }
            
            if (error!=null)
            {
                handleException(data,first,error);
                return;
            }

//            if (removedReference)
//            {
//                data.getSession().setAttribute(this.getReturnEditItemIdentifier(),first);
//                if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
//                {
//                    data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
//                }
//                return;
//            }

            ValidationResultsI vr = null;
            
            if (vr != null)
            {
                data.getSession().setAttribute(this.getReturnEditItemIdentifier(),first);
                context.put("vr",vr);
                if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
                {
                    data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
                }
            }else{
                try {
                    try {
                        preSave(first.getItem(),data,context);
                    } catch (CriticalException e) {
                        throw e;
                    } catch (RuntimeException e) {
                        logger.error("",e);
                    }
                    save(first,data,context);
                } catch (Exception e) {
                    handleException(data,first,error);
                    return;
                }
                try {
                    postProcessing(first,data,context);
                } catch (Exception e) {
                    logger.error("",e);
                    data.setMessage(e.getMessage());
                }
            }
        } catch (XFTInitException e) {
            handleException(data,first,e);
            return;
        } catch (ElementNotFoundException e) {
            handleException(data,first,e);
            return;
        } catch (FieldNotFoundException e) {
            handleException(data,first,e);
            return;
        } catch (Exception e) {
            handleException(data,first,e);
            return;
        }
    }
}
