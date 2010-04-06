//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on May 3, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions; 

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementAccessManager;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.PermissionCriteria;
import org.nrg.xdat.security.PermissionItem;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTool;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;

/**
 * @author Tim
 *
 */
public class ElementSecurityWizard extends SecureAction {
	static Logger logger = Logger.getLogger(ElementSecurityWizard.class);

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doPerform(RunData data, Context context) throws Exception {
    }

    /**
     * @param data
     * @param context
     * @throws Exception
     */
    public void doStep1(RunData data, Context context) throws Exception{
        PopulateItem populater = PopulateItem.Populate(data,org.nrg.xft.XFT.PREFIX + ":element_security",true);
		ItemI found = populater.getItem();

		String s = (String)found.getProperty("element_name");
		if (XFTTool.ValidateElementName(s))
		{
		    TurbineUtils.SetEditItem(found,data);
		    data.setScreenTemplate("XDATScreen_ES_Wizard1.vm");
		}else{
		    data.setScreenTemplate("XDATScreen_add_xdat_element_security.vm");
		}
    }

    /**
     * @param data
     * @param context
     * @throws Exception
     */
    public void doStep2(RunData data, Context context) throws Exception{
        PopulateItem populater = PopulateItem.Populate(data,org.nrg.xft.XFT.PREFIX + ":element_security",true);
		ItemI found = populater.getItem();

		if (found.getBooleanProperty("secure").booleanValue())
		{
		    SchemaElement se = SchemaElement.GetElement(found.getStringProperty("element_name"));

            if (se.hasField(se.getFullXMLName() + "/project") && se.hasField(se.getFullXMLName() + "/sharing/share/project")){
                found.setProperty("primary_security_fields.primary_security_field__0",se.getFullXMLName() + "/project");
                found.setProperty("primary_security_fields.primary_security_field__1",se.getFullXMLName() + "/sharing/share/project");
            }else{
                String s=se.getDefaultPrimarySecurityField();
                if (s!=null)
                {
                    found.setProperty("primary_security_fields.primary_security_field__0",s);
                }
            }
		}

		ValidationResults vr = found.validate();
		if (vr.isValid())
		{
		    TurbineUtils.SetEditItem(found,data);
		    data.setScreenTemplate("XDATScreen_ES_Wizard2.vm");
		}else{
		    TurbineUtils.SetEditItem(found,data);
		    context.put("vr",vr);
		    data.setScreenTemplate("XDATScreen_ES_Wizard1.vm");
		}
    }

    /**
     * @param data
     * @param context
     * @throws Exception
     */
    public void doStep3(RunData data, Context context) throws Exception{
        PopulateItem populater = PopulateItem.Populate(data,org.nrg.xft.XFT.PREFIX + ":element_security",true);
		ItemI found = populater.getItem();

		ValidationResults vr = found.validate();
		if (vr.isValid())
		{
		    int counter = 0;
		    ArrayList coll = found.getChildItems("xdat:element_security.element_actions.element_action");
		    Iterator iter = coll.iterator();
		    boolean foundError = false;
		    while (iter.hasNext())
		    {
		        XFTItem item = (XFTItem)iter.next();
		        String actionName = item.getStringProperty("element_action_name");
		        String templateName = "XDATScreen_"+ actionName + "_" + SchemaElement.GetElement(found.getStringProperty("xdat:element_security.element_name")).getSQLName() + ".vm";
		        boolean foundScreen = false;
		        if (Velocity.templateExists("/screens/" + templateName))
	    		{
		            foundScreen = true;
	    		}else if(Velocity.templateExists("/screens/XDATScreen_"+ actionName + ".vm")){
	    		    foundScreen = true;
	    		}

	    		if (foundScreen)
	    		{
			        item.setDirectProperty("sequence",new Integer(counter++));
	    		}else{
	    		    String s = "xdat:element_security.element_actions.element_action__"+counter++;
	    		    vr.addResult(null,"Unable to locate template '"+ templateName +"' or '"+ "XDATScreen_"+ actionName + ".vm'",s + ".element_action_name",(String)null);
	    		    foundError = true;
	    		}
		    }

		    if (TurbineUtils.HasPassedParameter("edit",data))
		    {
		        if (data.getParameters().getString("edit").equalsIgnoreCase("1"))
		        {
		            int count = found.getChildItems("xdat:element_security.element_actions.element_action").size();
		            String s = "xdat:element_security.element_actions.element_action__"+count;

		            String templateName = "XDATScreen_edit_" + SchemaElement.GetElement(found.getStringProperty("xdat:element_security.element_name")).getFormattedName() + ".vm";
		    		if (Velocity.templateExists("/screens/" + templateName))
		    		{
			            found.setProperty(s + ".element_action_name","edit");
			            found.setProperty(s + ".display_name","Edit");
			            found.setProperty(s + ".sequence",new Integer(count));
			            found.setProperty(s + ".image","e.gif");
			            found.setProperty(s + ".secureAccess","edit");
		    		}else{
		    		    vr.addResult(null,templateName + " Was Not Found.",s + ".element_action_name",(String)null);
		    		    TurbineUtils.SetEditItem(found,data);
		    		    context.put("vr",vr);
		    		    data.setScreenTemplate("XDATScreen_ES_Wizard2.vm");
		    		    return;
		    		}
		        }
		    }

		    if (foundError)
		    {
		        TurbineUtils.SetEditItem(found,data);
    		    context.put("vr",vr);
    		    data.setScreenTemplate("XDATScreen_ES_Wizard2.vm");
    		    return;
		    }

		    if (TurbineUtils.HasPassedParameter("xml",data))
		    {
		        if (data.getParameters().getString("xml").equalsIgnoreCase("1"))
		        {
		            int count = found.getChildItems("xdat:element_security.element_actions.element_action").size();
		            String s = "xdat:element_security.element_actions.element_action__"+count;

		            found.setProperty(s + ".element_action_name","xml");
			        found.setProperty(s + ".display_name","View XML");
			        found.setProperty(s + ".grouping","View");
		            found.setProperty(s + ".sequence",new Integer(count));
		            found.setProperty(s + ".image","r.gif");

		            count++;

		            s = "xdat:element_security.element_actions.element_action__"+count;

		            found.setProperty(s + ".element_action_name","xml_file");
			        found.setProperty(s + ".display_name","Download XML");
		            found.setProperty(s + ".sequence",new Integer(count));
		            found.setProperty(s + ".image","save.gif");
		        }
		    }

		    if (TurbineUtils.HasPassedParameter("activate",data))
		    {
		        if (data.getParameters().getString("activate").equalsIgnoreCase("1"))
		        {
		            int count = found.getChildItems("xdat:element_security.element_actions.element_action").size();

		            String s = "xdat:element_security.element_actions.element_action__"+count;

		            found.setProperty(s + ".element_action_name","activate");
		            found.setProperty(s + ".display_name","Activate");
		            found.setProperty(s + ".sequence",new Integer(count));
		        }
		    }

		    if (TurbineUtils.HasPassedParameter("email_report",data))
		    {
		        if (data.getParameters().getString("email_report").equalsIgnoreCase("1"))
		        {
		            int count = found.getChildItems("xdat:element_security.element_actions.element_action").size();

		            String s = "xdat:element_security.element_actions.element_action__"+count;

		            found.setProperty(s + ".element_action_name","email_report");
		            found.setProperty(s + ".display_name","Email");
		            found.setProperty(s + ".sequence",new Integer(count));
		            found.setProperty(s + ".image","right2.gif");
		            found.setProperty(s + ".popup","always");
		        }
		    }

		    try{
		        found.setProperty("element_security_set_element_se_xdat_security_id",TurbineUtils.GetSystemID());
		    }catch (RuntimeException e)
		    {

		    }

		    if(found.getBooleanProperty("secure").booleanValue())
		    {
		        ArrayList al = found.getChildItems(org.nrg.xft.XFT.PREFIX + ":element_security.primary_security_fields.primary_security_field");
		        if (al.size()==0)
		        {
				    TurbineUtils.SetEditItem(found,data);
				    data.setScreenTemplate("XDATScreen_ES_Wizard4.vm");
				    return;
		        }
		    }
		    
		    boolean saved=false;
		    try {
				found.save(TurbineUtils.getUser(data),false,false);
				saved=true;
			} catch (Exception e) {
				logger.error("Error Storing " + found.getXSIType(),e);
			}

			SchemaElementI se = SchemaElement.GetElement(found.getXSIType());
			
			if(saved){
				ElementSecurity.refresh();
				ElementSecurity es=ElementSecurity.GetElementSecurity(found.getStringProperty("element_name"));
				es.initExistingPermissions(TurbineUtils.getUser(data).getLogin());
				TurbineUtils.getUser(data).initGroups();
			}
			
			data = TurbineUtils.setDataItem(data,found);
			data = TurbineUtils.SetSearchProperties(data,found);
			data.setScreenTemplate(DisplayItemAction.GetReportScreen(se));

//		    TurbineUtils.SetEditItem(found,data);
//		    data.setScreenTemplate("XDATScreen_ES_Wizard4.vm");
		}else{
		    TurbineUtils.SetEditItem(found,data);
		    context.put("vr",vr);
		    data.setScreenTemplate("XDATScreen_ES_Wizard2.vm");
		}
    }

    /**
     * @param data
     * @param context
     * @throws Exception
     */
    public void doStep4(RunData data, Context context) throws Exception{
        PopulateItem populater = PopulateItem.Populate(data,org.nrg.xft.XFT.PREFIX + ":element_security",true);
		ItemI found = populater.getItem();

		ValidationResults vr = found.validate();
		if (vr.isValid())
		{
		    int counter = 0;
		    ArrayList coll = found.getChildItems("xdat:element_security.element_actions.element_action");
		    Iterator iter = coll.iterator();
		    while (iter.hasNext())
		    {
		        XFTItem item = (XFTItem)iter.next();
		        item.setDirectProperty("sequence",new Integer(counter++));
		    }

		    TurbineUtils.SetEditItem(found,data);
		    data.setScreenTemplate("XDATScreen_ES_Wizard4.vm");
		}else{
		    TurbineUtils.SetEditItem(found,data);
		    context.put("vr",vr);
		    logger.debug(found.toString());
		    data.setScreenTemplate("XDATScreen_ES_Wizard3.vm");
		}
    }
//  REMOVED 12/10/09 while refactoring the UserGroup-ElementAccess code.  I don't think it is used anywhere anymore.
//    /**
//     * @param data
//     * @param context
//     * @throws Exception
//     */
//    public void doStep5(RunData data, Context context) throws Exception{
//        XDATUser primaryUser =TurbineUtils.getUser(data);
//        String eName = (String)TurbineUtils.GetPassedParameter("element_name",data);
//        ElementSecurity es = ElementSecurity.GetElementSecurity(eName);
//
//        Iterator users = XdatUser.getAllXdatUsers(null,false).iterator();
//        while(users.hasNext())
//        {
//            try {
//                XDATUser u = new XDATUser(((XdatUser)users.next()).getItem());
//                Hashtable props = TurbineUtils.GetDataParameterHash(data);
//
//                ArrayList criteria = new ArrayList();
//    			ElementAccessManager eam = u.getAccessManager(es.getElementName());
//
//    			ArrayList permissionItems = es.getPermissionItems(u.getLogin());
//    			Iterator permissions = permissionItems.iterator();
//    			while (permissions.hasNext())
//    			{
//    				PermissionItem pi = (PermissionItem)permissions.next();
//    				PermissionCriteria pc = new PermissionCriteria();
//
//    				pc.setField(pi.getFullFieldName());
//    				pc.setFieldValue(pi.getValue());
//    				String s = u.getLogin()+ "_" + pi.getFullFieldName() + "_" + pi.getValue();
//    				if (props.get(s.toLowerCase() + "_r") != null)
//    				{
//    			    	pc.setRead(true);
//    				}else{
//    					pc.setRead(false);
//    				}
//    				if (props.get(s.toLowerCase() + "_c") != null)
//    				{
//    					pc.setCreate(true);
//    				}else{
//    					pc.setCreate(false);
//    				}
//    				if (props.get(s.toLowerCase() + "_e") != null)
//    				{
//    					pc.setEdit(true);
//    				}else{
//    					pc.setEdit(false);
//    				}
//    				if (props.get(s.toLowerCase() + "_d") != null)
//    				{
//    					pc.setDelete(true);
//    				}else{
//    					pc.setDelete(false);
//    				}
//    			    if (props.get(s.toLowerCase() + "_a") != null)
//    				{
//    				    pc.setActivate(true);
//    				}else{
//    				    pc.setActivate(false);
//    				}
//    				if (props.get(s.toLowerCase() + "_type") != null)
//    				{
//    				   pc.setComparisonType(props.get(s.toLowerCase() + "_type"));
//    				}
//    				u.addRootPermission(es.getElementName(),pc);
//    			}
//
//    			u.save(null,true,true,false,false);
//            } catch (Exception e) {
//                logger.error("",e);
//            }
//        }
//    }
}
