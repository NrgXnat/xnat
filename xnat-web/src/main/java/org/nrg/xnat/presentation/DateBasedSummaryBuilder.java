/*
 * org.nrg.xnat.presentation.DateBasedSummaryBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.presentation;

import org.nrg.xft.presentation.FlattenedItemI;

import java.util.Date;
import java.util.List;
import java.util.Map;


public class DateBasedSummaryBuilder extends ChangeSummaryBuilderA {

	public DateBasedSummaryBuilder(EventBuilderI b) {
		super(b);
	}

	public static Map<Date,ChangeSummary> build(List<FlattenedItemI> items,EventBuilderI b) throws Exception{
		return (new DateBasedSummaryBuilder(b)).call(items);
	}
	
}