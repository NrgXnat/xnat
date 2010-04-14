//Copyright 2007 Washington University School of Medicine All Rights Reserved
/*
 * Created on Sep 12, 2007
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatRoleType;
import org.nrg.xdat.om.XdatUserGroupid;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.search.ItemSearch;

public class ModifyUserGroups extends SecureAction {

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
//      TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
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
        
        
        PopulateItem populater = PopulateItem.Populate(data,org.nrg.xft.XFT.PREFIX + ":user",true);
        ItemI found = populater.getItem();
        try {
            ItemSearch search = new ItemSearch();
            search.setAllowMultiples(false);
            search.setElement("xdat:user");
            search.addCriteria("xdat:user.login",found.getProperty("login"));
            
            ItemI temp = search.exec().getFirst();

            XDATUser oldUser = new XDATUser(temp);
            XDATUser newUser = new XDATUser(found);
            
            if (oldUser.checkRole("Administrator")){
                if (!newUser.checkRole("Administrator")){
                    Iterator iter= oldUser.getAssignedRoles_assignedRole().iterator();
                    while(iter.hasNext()){
                        XdatRoleType role = (XdatRoleType)iter.next();
                        if (role.getStringProperty("role_name").equals("Administrator")){
                            //DBAction.DeleteItem(role.getItem(), TurbineUtils.getUser(data));
                            DBAction.RemoveItemReference(oldUser.getItem(), "xdat:user/assigned_roles/assigned_role", role.getItem(), TurbineUtils.getUser(data));
                        }
                    }
                }
            }
            
            ArrayList<XdatUserGroupid> newGroups = newUser.getGroups_groupid();
            ArrayList<XdatUserGroupid> oldGroups = oldUser.getGroups_groupid();
            
            for(XdatUserGroupid uGroup : oldGroups){
                boolean matched=false;
                for (XdatUserGroupid newGroup:newGroups){
                    if (newGroup.getGroupid().equals(uGroup.getGroupid())){
                        matched=true;
                    }
                }
                
                if (!matched){
                    DBAction.DeleteItem(uGroup.getItem(), TurbineUtils.getUser(data));
                }
            }

            found.save(TurbineUtils.getUser(data),false,false);
            
            found.getItem().removeEmptyItems();
            
            
            
//            UserCache.Clear();
            
            
//          if (temp == null)
//          {
//              AdminUtils.sendNewUserEmailMessage(found.getStringProperty("login"),found.getStringProperty("email"));
//              
//              
//          }
        } catch (Exception e) {
            logger.error("Error Storing User",e);
        }
        
        data.getParameters().setString("search_element",org.nrg.xft.XFT.PREFIX + ":user");
        data.getParameters().setString("search_field",org.nrg.xft.XFT.PREFIX + ":user.login");
        
        data.getParameters().setString("search_value",found.getProperty(org.nrg.xft.XFT.PREFIX + ":user" + XFT.PATH_SEPERATOR + "login").toString());
        data.setAction("DisplayItemAction");
        VelocityAction action = (VelocityAction) ActionLoader.getInstance().getInstance("DisplayItemAction");
        action.doPerform(data, context);
    }

}
