//Copyright 2007 Washington University School of Medicine All Rights Reserved
/*
 * Created on Jun 18, 2007
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.StringUtils;

public class XDATScreen_change_email extends SecureScreen {

    public void doBuildTemplate(RunData data, Context context)
    {
        try {            
            UserI user = TurbineUtils.getUser(data);
            
            context.put("edit_screen",StringUtils.getLocalClassName(this.getClass()) + ".vm");
            try {
                    SchemaElementI se = SchemaElement.GetElement(Users.getUserDataType());
                    context.put("item",user);
                    context.put("element",org.nrg.xdat.schema.SchemaElement.GetElement(Users.getUserDataType()));
                    context.put("search_element",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_element",data)));
                    context.put("search_field",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_field",data)));
                    context.put("search_value",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data)));

                } catch (Exception e) {
                    e.printStackTrace();
                    data.setMessage("Invalid Search Parameters: No Data Item Found.");
                    data.setScreen("Index");
                    TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
                }

        } catch (Exception e) {
            e.printStackTrace();
            data.setMessage("Invalid Search Parameters: No Data Item Found.");
            data.setScreen("Index");
            TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
        }
    }

}
