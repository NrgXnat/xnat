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
				PersistentWorkflowI wrk=null;
				if(o.getItem().instanceOf("xnat:experimentData") || o.getItem().instanceOf("xnat:subjectData")){
					wrk=PersistentWorkflowUtils.buildOpenWorkflow(TurbineUtils.getUser(data),o.getItem(), this.newEventInstance(data, EventUtils.CATEGORY.DATA,"Deprecated Delete Action"));
				}
				
				final EventMetaI ci;
				if(wrk!=null){
					ci=wrk.buildEvent();
				}else{
					ci=EventUtils.ADMIN_EVENT(TurbineUtils.getUser(data));
				}
				
				try {
					SaveItemHelper.unauthorizedDelete(o.getItem(), TurbineUtils.getUser(data),ci);
                    
                    PersistentWorkflowUtils.complete(wrk,ci);
                    data.setMessage("<p>Item Deleted.</p>");
    			  	data.setScreenTemplate("Index.vm");
                } catch (RuntimeException e1) {
                    logger.error("",e1);
                    data.setMessage(e1.getMessage());
                    PersistentWorkflowUtils.fail(wrk,ci);
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
