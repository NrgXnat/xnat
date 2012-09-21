//Copyright 2007 Washington University School of Medicine All Rights Reserved
/*
 * Created on Jun 18, 2007
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.turbine.om.security.User;
import org.apache.turbine.util.RunData;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.xdat.entities.XDATUserDetails;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementAction;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;

public class ModifyEmail extends SecureAction {

    static Logger logger = Logger.getLogger(ModifyEmail.class);
    public void doPerform(RunData data, Context context) throws Exception
    {
        //TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
        //parameter specifying elementAliass and elementNames
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
                	if(found.getProperty("email")!=null &&  !found.getProperty("email").equals(authenticatedUser.getEmail())){
                		String newemail = found.getStringProperty("email");
                		if(!newemail.contains("@")){
                			data.setMessage("Please enter a valid email address.");
                			data.setScreenTemplate("XDATScreen_MyXNAT.vm");
                			return;
                		}
                		
                		String old=authenticatedUser.getEmail();
	                	XDATUser.ModifyUser(authenticatedUser, found,EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified User Email"));
	                	ItemI item = TurbineUtils.getUser(data);
	                	item.setProperty("xdat:user.email", newemail);
                	
	                	AdminUtils.sendUserHTMLEmail("Email address changed.", "Your email address was successfully changed to "+found.getProperty("email") + ".", true, new String[]{old,newemail});
                	}else{
                        data.setMessage("Email address unchanged.");
                        data.setScreenTemplate("Index.vm");
                	}
                
                } catch (InvalidPermissionException e) {
        			notifyAdmin(authenticatedUser, data,403,"Possible Authorization Bypass event", "User attempted to modify a user account other then his/her own.  This typically requires tampering with the HTTP form submission process.");
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
            data.setMessage("Email changed.");
            data.setScreenTemplate("Index.vm");
        }
    }

}