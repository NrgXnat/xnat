/*
 * org.nrg.xdat.turbine.modules.screens.EditScreenA
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.StringUtils;


/**
 * @author Tim
 *
 */
public abstract class EditScreenA extends SecureScreen {
	protected ItemI item = null;
	/**
	 * ArrayList of Object[3] {xmlPath,option,(Possible Values)ArrayList of ArrayList(2){value,display},defaultVALUE}
	 * @return
	 */
	public abstract String getElementName();
	public abstract void finalProcessing(RunData data, Context context);
	
	public ItemI getEmptyItem(RunData data) throws Exception
	{
	    String s = getElementName();
		return XFTItem.NewItem(s,TurbineUtils.getUser(data));
	}
    
    public String getStringIdentifierForPassedItem(RunData data){
        if(TurbineUtils.HasPassedParameter("tag", data)){
            return (String)TurbineUtils.GetPassedParameter("tag", data);
        }else{
            return "edit_item";
        }
    }
	
	public void doBuildTemplate(RunData data, Context context)
	{
		try {
            if (TurbineUtils.HasPassedParameter("destination", data)){
                context.put("destination", TurbineUtils.GetPassedParameter("destination", data));
            }
		    context.put("edit_screen",data.getScreen());

            if (TurbineUtils.HasPassedParameter("tag", data)){
                context.put("tag", TurbineUtils.GetPassedParameter("tag", data));
            }
			
			if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("source",data)) != null)
			{
				context.put("source", ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("source",data)));
			}
			
			item = null;

            if (!getStringIdentifierForPassedItem(data).equals("edit_item")){
                item = (ItemI)data.getSession().getAttribute(getStringIdentifierForPassedItem(data));
                if (item !=null){
                    data.getSession().removeAttribute(getStringIdentifierForPassedItem(data));
                }else{
                    item =(ItemI) TurbineUtils.GetEditItem(data);
                }
                
            }else{
                item = (ItemI)TurbineUtils.GetEditItem(data);
            }
            
            if (item !=null)
            {
                if((!item.getXSIType().equals(getElementName())) && (!(item.getItem()).getGenericSchemaElement().isExtensionOf(GenericWrapperElement.GetElement(getElementName())))){
                    item = null;
                }
            }
			
			if (item== null)
			{
				if(XFT.VERBOSE) System.out.println("No edit item passed... looking for item passed by variables");
				try {
					ItemI temp = TurbineUtils.GetItemBySearch(data);
                    if (temp !=null){
                        if (temp.getXSIType().equalsIgnoreCase(getElementName()))
                        {
                            item = temp;
                        }
                    }
				} catch (Exception e1) {
				}
			}
			context.put("edit_screen",StringUtils.getLocalClassName(this.getClass()) + ".vm");
			if (item == null)
			{
				try {
				    String s = getElementName();
					item = getEmptyItem(data);
					
					
					if(XFT.VERBOSE)  System.out.println("No passed item found.\nCreated New Item (" + item.getXSIType() +  ")");
					SchemaElementI se = SchemaElement.GetElement(item.getXSIType());

					context.put("item",item);
					context.put("element",se);
					context.put("search_element",s);

	            	context.put("om",BaseElement.GetGeneratedItem(item));
					finalProcessing(data,context);
				} catch (Exception e) {
					e.printStackTrace();
					data.setMessage("Invalid Search Parameters: Error creating item.");
					data.setScreen("Index");
					TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
				}
			}else{
				try {
					SchemaElementI se = SchemaElement.GetElement(item.getXSIType());
					if (context.get("source") == null)
					{
						context.put("source","XDATScreen_report_" + se.getFormattedName() + ".vm");
					}
					context.put("item",item);
					context.put("element",org.nrg.xdat.schema.SchemaElement.GetElement(item.getXSIType()));
					context.put("search_element",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_element",data)));
					context.put("search_field",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_field",data)));
					context.put("search_value",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data)));

	            	context.put("om",BaseElement.GetGeneratedItem(item));
					finalProcessing(data,context);
				} catch (Exception e) {
					e.printStackTrace();
					data.setMessage("Invalid Search Parameters: No Data Item Found.");
					data.setScreen("Index");
					TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			data.setMessage("Invalid Search Parameters: No Data Item Found.");
			data.setScreen("Index");
			TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
		}

        this.preserveVariables(data, context);
	}
}

