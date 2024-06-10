
//Author: James Dickson <james@radiologics.com>
package org.nrg.xnat.turbine.modules.screens;

/**
 * @author james@radiologics.com
 *
 * Screen class for the TriageResource used to display triage entries.
 */
public class TriageResources extends CustomTableScreen {

	/* (non-Javadoc)
	 * @see org.apache.turbine.modules.screens.VelocitySecureScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 * Override of standard build method used to prep context for the vm file.
	 */
	/*@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		if(data.getParameters().getObject("hideTopBar")!=null){
			context.put("hideTopBar", Boolean.valueOf((String)TurbineUtils.GetPassedParameter(("hideTopBar"), data)));
		}
		if(data.getParameters().getObject("message")!=null){
			context.put("message", (String)TurbineUtils.GetPassedParameter(("message"), data));
		}
		XFTTable table=(XFTTable)((RestletRunData)data).retrieveObject("table");
		if(table!=null){
			context.put("table", table);
		}
		
		if(TurbineUtils.HasPassedParameter("key",data)){
			context.put("key", (String)TurbineUtils.GetPassedParameter(("key"), data));
		}
		
		if(TurbineUtils.HasPassedParameter("xsiType",data)){
			context.put("xsiType", (String)TurbineUtils.GetPassedParameter(("xsiType"), data));
		}
	}*/
}
