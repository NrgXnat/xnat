/*
 * org.nrg.xnat.turbine.modules.screens.WorkflowHistorySummary
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xnat.itemBuilders.WorkflowBasedHistoryBuilder;
import org.nrg.xnat.itemBuilders.WorkflowBasedHistoryBuilder.WorkflowView;

import java.util.*;


public class WorkflowHistorySummary extends SecureReport {
 
	@Override
	public void finalProcessing(final RunData data, Context context) {
		try {
			final String id=(TurbineUtils.HasPassedParameter("key", data))?(String)TurbineUtils.GetPassedParameter("key", data):item.getStringProperty("ID");
			List<WorkflowView> wvs=new ArrayList<WorkflowView>();
			
			Object o=TurbineUtils.GetPassedParameter("includeFiles", data);
			Object details=TurbineUtils.GetPassedParameter("includeDetails", data);
			final boolean includeFiles=(o==null || Boolean.valueOf(o.toString()));
			final boolean includedetails=(details==null || Boolean.valueOf(details.toString()));
			
			Map<Number,WorkflowView> map=(new WorkflowBasedHistoryBuilder(item,id,TurbineUtils.getUser(data),includeFiles,includedetails)).call();
			wvs.addAll(map.values());
			
			Collections.sort(wvs, new Comparator<WorkflowView>() {
				@Override
				public int compare(WorkflowView arg0, WorkflowView arg1) {
					try {
						return DateUtils.compare(arg0.getDate(), arg1.getDate());
					} catch (Exception e) {
						return -1;
					}
				}
			});
			context.put("change_sets",wvs);
			
			if(TurbineUtils.HasPassedParameter("hideTopBar",data)){
				context.put("hideTopBar", Boolean.valueOf((String)TurbineUtils.GetPassedParameter(("hideTopBar"), data)));
			}
		} catch (Exception e) {
			logger.error("",e);
		}
		
	}
}