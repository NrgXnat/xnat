//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Jul 25, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;

/**
 * @author Tim
 *
 */
public class ModifyPassword extends SecureAction {

    static Logger logger = Logger.getLogger(ModifyPassword.class);
	public void doPerform(RunData data, Context context) throws Exception
	{
		//TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
		//parameter specifying elementAliass and elementNames
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
		
		InvalidValueException error = null;
		ArrayList al = new ArrayList();
		Enumeration keys = hash.keys();
		while(keys.hasMoreElements())
		{
			String key = (String)keys.nextElement();
			String element = (String)hash.get(key);
			PopulateItem populater = null;
			populater = PopulateItem.Populate(data,element,true);
            if (populater.hasError())
            {
                error = populater.getError();
            }            
            al.add(populater.getItem());
		}
		XFTItem first = (XFTItem)al.get(0);
		
		if (error!=null)
		{
		    TurbineUtils.SetEditItem(first,data);
		    data.addMessage(error.getMessage());
		    if (data.getParameters().getString("edit_screen") !=null)
		    {
		        data.setScreenTemplate(data.getParameters().getString("edit_screen"));
		    }
		    return;
		}

		ValidationResults vr = null;

		Iterator iter = al.iterator();
		while (iter.hasNext())
		{
			ItemI found = (ItemI)iter.next();
			ValidationResults temp = found.validate();
			if (! temp.isValid())
			{
			   vr = temp;
			    break;
			}
		}
		
		if (vr != null)
		{
		    TurbineUtils.SetEditItem(first,data);
		    context.put("vr",vr);
		    if (data.getParameters().getString("edit_screen") !=null)
		    {
		        data.setScreenTemplate(data.getParameters().getString("edit_screen"));
		    }
		}else{
		    iter = al.iterator();
			while (iter.hasNext())
			{
				ItemI found = (ItemI)iter.next();
				
				if (found.getBooleanProperty("primary_password.encrypt",true))
				{
					String tempPass = found.getStringProperty("primary_password");
					found.setProperty("primary_password",XDATUser.EncryptString(tempPass,"SHA-256"));
				}
				
				try {
					found.save(TurbineUtils.getUser(data),false,false);
				} catch (Exception e) {
					logger.error("Error Storing " + found.getXSIType(),e);
				}
			}
			
			SchemaElementI se = SchemaElement.GetElement(first.getXSIType());
			if (se.getGenericXFTElement().getType().getLocalPrefix().equalsIgnoreCase("xdat"))
			{
			    ElementSecurity.refresh();
			}
			data.setMessage("Password changed.");
			data.setScreenTemplate("Index.vm");
		}
	}

}
