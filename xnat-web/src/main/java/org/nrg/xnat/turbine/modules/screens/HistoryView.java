/*
 * org.nrg.xnat.turbine.modules.screens.HistoryView
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
import org.nrg.xft.presentation.*;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xnat.itemBuilders.FullFileHistoryBuilder;

import java.util.Arrays;
import java.util.Date;

public class HistoryView extends SecureReport {
 
	@Override
	public void finalProcessing(RunData data, Context context) {
		try {
			FlattenedItemA.FilterI filter=null;
			final Date date;
			if(TurbineUtils.HasPassedParameter("as_of_date", data)){
				date=DateUtils.parseDateTime(TurbineUtils.GetPassedParameter("as_of_date", data).toString());
			
				filter= new FlattenedItemA.FilterI() {
					@Override
					public boolean accept(FlattenedItemI i) {
						if(i.getStartDate()==null && i.getEndDate()==null){
							return true;
						}else if(i.getStartDate()==null || i.getEndDate()==null){
							if(i.getStartDate()!=null && DateUtils.isOnOrAfter(date,i.getStartDate())){
								return true;
							}else if(i.getEndDate()!=null && DateUtils.isOnOrBefore(date,i.getEndDate())){
								return true;
							}else{
								return false;
							}
						}else{ 
							boolean after=DateUtils.isOnOrAfter(date,i.getStartDate());
							boolean before=DateUtils.isOnOrBefore(date,i.getEndDate());
							if(after && before){
								return true;
							}else{
								return false;
							}
						}
					}
				};
				
			}else{
				date=null;
			}
					
			context.put("built_html",
					ItemHtmlBuilder.build(date,Arrays.asList(
							ItemMerger.merge(
									ItemFilterer.filter(
											ItemPropBuilder.build(item.getItem(), FlattenedItemA.GET_ALL,Arrays.asList(new FullFileHistoryBuilder())),filter)))));
			
		} catch (Exception e) {
			logger.error("",e);
		}
		
	}

}
