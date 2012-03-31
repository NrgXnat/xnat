//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 4, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions; 

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.apache.turbine.modules.ScreenLoader;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.turbine.modules.screens.EditScreenA;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;

/**
 * @author Tim
 *
 */
public class ModifyItem  extends SecureAction {
	static Logger logger = Logger.getLogger(ModifyItem.class);

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
            while (data.getParameters().get(header + counter) != null)
            {
            	String elementToLoad = data.getParameters().getString(header + counter++);
            	Integer numberOfInstances = data.getParameters().getIntObject(elementToLoad);
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

            String screenName = null;
            if (data.getParameters().getString("edit_screen") !=null && !data.getParameters().getString("edit_screen").equals(""))
            {
                screenName = data.getParameters().getString("edit_screen").substring(0,data.getParameters().getString("edit_screen").lastIndexOf(".vm"));
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
            

            XFTItem dbVersion = first.getCurrentDBVersion();

			final PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, TurbineUtils.getUser(data), first,newEventInstance(data, EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(first.getXSIType(), dbVersion==null)));
	    	EventMetaI c=wrk.buildEvent();
            PersistentWorkflowUtils.save(wrk,c);

            boolean removedReference = false;
            Object[] keysArray = data.getParameters().getKeys();
            for (int i=0;i<keysArray.length;i++)
            {
            	String key = (String)keysArray[i];
            	if (key.toLowerCase().startsWith("remove_"))
            	{
            	    int index = key.indexOf("=");
            	    String field = key.substring(index+1);
            	    Object value = data.getParameters().getObject(key);
            	    logger.debug("FOUND REMOVE: " + field + " " + value);
            	    ItemCollection items =ItemSearch.GetItems(field,value,TurbineUtils.getUser(data),false);
            	    if (items.size() > 0)
            	    {
            	        ItemI toRemove = items.getFirst();
            	        DBAction.RemoveItemReference(dbVersion.getItem(),null,toRemove.getItem(),TurbineUtils.getUser(data),c);
            	        first.removeItem(toRemove);
            	        removedReference = true;
            	    }else{
            	        logger.debug("ITEM NOT FOUND:" + key + "="+ value);
            	    }
            	}
            }

//            if (removedReference)
//            {
//                data.getSession().setAttribute(this.getReturnEditItemIdentifier(),first);
//                if (data.getParameters().getString("edit_screen") !=null)
//                {
//                    data.setScreenTemplate(data.getParameters().getString("edit_screen"));
//                }
//                return;
//            }

            ValidationResults vr = null;

            	ValidationResults temp = first.validate();
            	if (! temp.isValid())
            	{
            	   vr = temp;
            	}

            if (vr != null)
            {
                data.getSession().setAttribute(this.getReturnEditItemIdentifier(),first);
                context.put("vr",vr);
                if (data.getParameters().getString("edit_screen") !=null)
                {
                    data.setScreenTemplate(data.getParameters().getString("edit_screen"));
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
                    save(first,data,context,c);
                    PersistentWorkflowUtils.complete(wrk,c);
					MaterializedView.DeleteByUser(TurbineUtils.getUser(data));
        		} catch (Exception e) {
                    PersistentWorkflowUtils.fail(wrk,c);
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

    public void handleException(RunData data,XFTItem first,Throwable error){
        handleException(data, first, error, getReturnEditItemIdentifier());
    }

    public void preSave(XFTItem item,RunData data, Context context) throws Exception{}

    public void postProcessing(XFTItem item,RunData data, Context context) throws Exception{
        SchemaElementI se = SchemaElement.GetElement(item.getXSIType());
        if (se.getGenericXFTElement().getType().getLocalPrefix().equalsIgnoreCase("xdat"))
        {
            ElementSecurity.refresh();
        }else if (se.getFullXMLName().equals("xnat:investigatorData")|| se.getFullXMLName().equals("xnat:projectData")){
            ElementSecurity.refresh();
        }

        //item = item.getCurrentDBVersion(false);

        //data = TurbineUtils.setDataItem(data,item);

        if (TurbineUtils.HasPassedParameter("destination", data)){
            this.redirectToReportScreen((String)TurbineUtils.GetPassedParameter("destination", data), item, data);
        }else{
            this.redirectToReportScreen(DisplayItemAction.GetReportScreen(se), item, data);
        }
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
        first.save(TurbineUtils.getUser(data),false,allowDataDeletion(),c);
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

