//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 22, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.search.ItemSearch;

/**
 * @author Tim
 *
 */
public class RemoveItemReference extends SecureAction {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doPerform(RunData data, Context context) throws Exception {
        String parentCol = data.getParameters().getString("parentCol");
        Object parentValue = data.getParameters().getObject("parentValue");
        String childCol = data.getParameters().getString("childCol");
        Object childValue = data.getParameters().getObject("childValue");
        
        if (TurbineUtils.HasPassedParameter("parentCol",data) && TurbineUtils.HasPassedParameter("parentValue",data)
            && TurbineUtils.HasPassedParameter("childCol",data) && TurbineUtils.HasPassedParameter("childValue",data))
        {
            ItemCollection parentItems = ItemSearch.GetItems(parentCol,parentValue,TurbineUtils.getUser(data),false);
            ItemCollection childItems = ItemSearch.GetItems(childCol,childValue,TurbineUtils.getUser(data),false);
            
            if (parentItems.size()>0 && childItems.size()>0)
            {
                ItemI parent = parentItems.getFirst();
                ItemI child = childItems.getFirst();
                
                DBAction.RemoveItemReference(parent.getItem(),null,child.getItem(),TurbineUtils.getUser(data));
                
                data.setMessage("Item adjusted.");
            	data.setScreenTemplate("Index.vm");
            }else{
                TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
                data.setMessage("Invalid parameters.");
            	data.setScreenTemplate("Index.vm");
            }
        }else{
            TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
            data.setMessage("Missing required parameters.");
        	data.setScreenTemplate("Index.vm");
        }
    }

}

