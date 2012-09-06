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
import org.nrg.xdat.security.XDATUser.PasswordComplexityException;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidPermissionException;
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
		
		XDATUser user=TurbineUtils.getUser(data);
		if(user==null){
			error(new Exception("User 'null' cannot change password."), data);
		}

		if(user.getUsername().equals("guest")){
			error(new Exception("Guest account password must be managed in the administration section."), data);
		}
		
		String header = "ELEMENT_";
		int counter = 0;
		Hashtable hash = new Hashtable();
		while (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(header + counter,data)) != null)
		{
			String elementToLoad = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(header + counter++,data));
			Integer numberOfInstances = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedInteger(elementToLoad,data,null));
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
		    if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
		    {
		        data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
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
		    if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)) !=null)
		    {
		        data.setScreenTemplate(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("edit_screen",data)));
		    }
		}else{
		    iter = al.iterator();
			while (iter.hasNext())
			{
				ItemI found = (ItemI)iter.next();
				
				XDATUser authenticatedUser=TurbineUtils.getUser(data);
				try {
					XDATUser.ModifyUser(authenticatedUser, found,EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified User Password"));
				} catch (InvalidPermissionException e) {
					notifyAdmin(authenticatedUser, data,403,"Possible Authorization Bypass event", "User attempted to modify a user account other then his/her own.  This typically requires tampering with the HTTP form submission process.");
					return;
				} catch (PasswordComplexityException e){

					data.setMessage( e.getMessage());
					data.setScreenTemplate("XDATScreen_MyXNAT.vm");
					return;
	
				} catch (Exception e) {
					logger.error("Error Storing User", e);
					return;
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
