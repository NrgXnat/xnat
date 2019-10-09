/*
 * core: org.nrg.xdat.turbine.modules.actions.ModifyItem
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.modules.ScreenLoader;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XdatElementSecurity;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.turbine.modules.screens.EditScreenA;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author Tim
 *
 */
@Slf4j
public class ModifyItem  extends SecureAction {
    private String returnEditItemIdentifier="edit_item";

    public void preProcess(XFTItem item,RunData data, Context context){

    }

    public boolean allowDataDeletion(){
        return false;
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
            while (TurbineUtils.GetPassedParameter(header + counter, data) != null)
            {
            	String elementToLoad = ((String)TurbineUtils.GetPassedParameter(header + counter++,data));
            	Integer numberOfInstances = TurbineUtils.GetPassedInteger(elementToLoad, data, null);
            	if (numberOfInstances != null && numberOfInstances != 0)
            	{
            		int subCount = 0;
            		while (subCount != numberOfInstances)
            		{
            			hash.put(elementToLoad + (subCount++),elementToLoad);
            		}
            	}else{
            		hash.put(elementToLoad,elementToLoad);
            	}
            }

            String screenName = null;
            if (TurbineUtils.GetPassedParameter("edit_screen", data) != null && !TurbineUtils.GetPassedParameter("edit_screen", data).equals(""))
            {
                screenName = ((String)TurbineUtils.GetPassedParameter("edit_screen",data)).substring(0,((String)TurbineUtils.GetPassedParameter("edit_screen",data)).lastIndexOf(".vm"));
            }

            InvalidValueException error = null;
            ArrayList al = new ArrayList();
            Enumeration keys = hash.keys();
            while(keys.hasMoreElements())
            {
            	String key = (String)keys.nextElement();
            	String element = (String)hash.get(key);
            	SchemaElement e = SchemaElement.GetElement(element);

            	PopulateItem populater;
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
                log.error("", e1);
            }

            if (error!=null)
            {
                handleException(data,first,error);
                return;
            }
            

            XFTItem dbVersion = first.getCurrentDBVersion();

            final PersistentWorkflowI wrk;
            if(first.instanceOf("xnat:experimentData") || first.instanceOf("xnat:subjectData")){
    			wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, XDAT.getUserDetails(), first,newEventInstance(data, EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(first.getXSIType(), dbVersion==null)));
            }else{
            	wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, XDAT.getUserDetails(), first.getXSIType(),PersistentWorkflowUtils.getID(first),PersistentWorkflowUtils.getExternalId(first),newEventInstance(data, EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.getAddModifyAction(first.getXSIType(), dbVersion==null)));
            }
            
            final EventMetaI c=wrk.buildEvent();

            final Object[] keysArray = data.getParameters().getKeys();
            for (final Object o : keysArray) {
                String key = (String) o;
                if (key.toLowerCase().startsWith("remove_")) {
                    int    index = key.indexOf("=");
                    String field = key.substring(index + 1);
                    Object value = TurbineUtils.GetPassedParameter(key, data);
                    log.debug("FOUND REMOVE: " + field + " " + value);
                    ItemCollection items = ItemSearch.GetItems(field, value, XDAT.getUserDetails(), false);
                    if (dbVersion != null && items.size() > 0) {
                        ItemI toRemove = items.getFirst();
                        SaveItemHelper.unauthorizedRemoveChild(dbVersion.getItem(), null, toRemove.getItem(), XDAT.getUserDetails(), c);
                        first.removeItem(toRemove);
                    } else {
                        log.debug("ITEM NOT FOUND:" + key + "=" + value);
                    }
                }
            }

            final ValidationResultsI vr = first.validate();
            if (!vr.isValid()) {
                data.getSession().setAttribute(this.getReturnEditItemIdentifier(),first);
                context.put("vr",vr);
                if (TurbineUtils.GetPassedParameter("edit_screen", data) != null) {
                    data.setScreenTemplate(((String)TurbineUtils.GetPassedParameter("edit_screen",data)));
                }
            }else{
        		try {
                    try {
                        preSave(first.getItem(),data,context);
                    } catch (RuntimeException e) {
                        log.error("", e);
                    }
                    save(first,data,context,c);
                    PersistentWorkflowUtils.confirmID(first, wrk);
                    PersistentWorkflowUtils.complete(wrk,c);
                    MaterializedView.deleteByUser(XDAT.getUserDetails());
        		} catch (Exception e) {
                    PersistentWorkflowUtils.confirmID(first, wrk);
                    PersistentWorkflowUtils.fail(wrk,c);
                    handleException(data,first,error);
                    return;
        		}
                try {
                    postProcessing(first,data,context);
                } catch (Exception e) {
                    log.error("", e);
                    data.setMessage(e.getMessage());
                }
            }
        } catch (Exception e) {
            handleException(data, first, e);
        }
    }

    public void handleException(RunData data,XFTItem first,Throwable error){
        handleException(data, first, error, getReturnEditItemIdentifier());
    }

    public void preSave(XFTItem item,RunData data, Context context) throws Exception{}

    public void postProcessing(XFTItem item,RunData data, Context context) throws Exception{
        final SchemaElementI se = SchemaElement.GetElement(item.getXSIType());
        if (se.getGenericXFTElement().getType().getLocalPrefix().equalsIgnoreCase("xdat")) {
            ElementSecurity.refresh();
            if (StringUtils.equals(se.getFullXMLName(), XdatElementSecurity.SCHEMA_ELEMENT_NAME)) {
                XDAT.triggerXftItemEvent(item, XftItemEvent.UPDATE);
            }
        } else if (StringUtils.equalsAny(se.getFullXMLName(), "xnat:investigatorData", "xnat:projectData")) {
            ElementSecurity.refresh();
        }

        redirectToReportScreen(StringUtils.defaultIfBlank((String) TurbineUtils.GetPassedParameter("destination", data), DisplayItemAction.GetReportScreen(se)), item, data);
    }

    /**
     * @return the returnEditItemIdentifier
     */
    public String getReturnEditItemIdentifier() {
        return returnEditItemIdentifier;
    }

    /**
     * @param returnEditItemIdentifier the returnEditItemIdentifier to set
     */
    public void setReturnEditItemIdentifier(String returnEditItemIdentifier) {
        this.returnEditItemIdentifier = returnEditItemIdentifier;
    }

    public void save(XFTItem first,RunData data, Context context, EventMetaI c) throws InvalidItemException,Exception{
        SaveItemHelper.unauthorizedSave(first, XDAT.getUserDetails(), false, allowDataDeletion(), c);
    }

    @SuppressWarnings("serial")
    public class CriticalException extends Exception{

        /**
         * @param message
         */
        public CriticalException(String message) {
            super(message);
        }

    }
}

