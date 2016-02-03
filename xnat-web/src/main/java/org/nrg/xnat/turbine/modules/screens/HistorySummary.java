/*
 * org.nrg.xnat.turbine.modules.screens.HistorySummary
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
import org.nrg.xft.presentation.FlattenedItemA;
import org.nrg.xft.presentation.FlattenedItemI;
import org.nrg.xft.presentation.ItemMerger;
import org.nrg.xft.presentation.ItemPropBuilder;
import org.nrg.xnat.itemBuilders.FullFileHistoryBuilder;
import org.nrg.xnat.presentation.DateBasedSummaryBuilder;

import java.util.Arrays;
import java.util.List;

public class HistorySummary extends SecureReport {

	@Override
	public void finalProcessing(RunData data, Context context) {
		try {

			List<FlattenedItemI> items=
				Arrays.asList(
					ItemMerger.merge(
							ItemPropBuilder.build(item.getItem(), FlattenedItemA.GET_ALL,Arrays.asList(new FullFileHistoryBuilder()))));
			
			context.put("change_sets",(new DateBasedSummaryBuilder(null)).call(items));
		} catch (Exception e) {
			logger.error("",e);
		}
		
	}

}