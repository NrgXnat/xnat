//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Dec 16, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;

/**
 * @author Tim
 *
 */
public class DeleteAction extends SecureAction {
    static Logger logger = Logger.getLogger(DeleteAction.class);

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doPerform(RunData data, Context context) throws Exception {
        preserveVariables(data,context);
        ItemI o = null;
	    try {
			o = TurbineUtils.GetItemBySearch(data,true);
			if (o != null)
			{		  				
				try {
					SaveItemHelper.unauthorizedDelete(o.getItem(), TurbineUtils.getUser(data),this.newEventInstance(data, EventUtils.CATEGORY.DATA,"Deprecated Delete Action"));
                    
                    data.setMessage("<p>Item Deleted.</p>");
    			  	data.setScreenTemplate("Index.vm");
                } catch (RuntimeException e1) {
                    logger.error("",e1);
                    data.setMessage(e1.getMessage());
                }
			}else{
			  	logger.error("No Item Found.");
			    data.setMessage("<p>No Item Found.</p>");
			  	TurbineUtils.OutputDataParameters(data);
			  	data.setScreenTemplate("Error.vm");
			}
		} catch (Exception e) {
			logger.error("DeleteAction",e);
			data.setScreenTemplate("Error.vm");
		}
    }

}
