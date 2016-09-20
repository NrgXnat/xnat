/*
 * core: org.nrg.xft.utils.DateUtilsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.utils;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;
import static org.junit.Assert.fail;


public class DateUtilsTest {

	@Test
	public void testGreaterThan() throws ParseException {
		
		Date d1 = DateUtils.parseDateTime("2011-12-22 16:07:09.327");
		Date d2 = DateUtils.parseDateTime("2011-12-22 16:07:10.327");
		
		if(!DateUtils.isOnOrAfter(d2,d1)){
			fail(d1.toString() +"<"+ d2.toString());
		}
	}

	@Test
	public void testLessThan() throws ParseException {
		
		Date d1 = DateUtils.parseDateTime("2011-12-22 16:07:09.327");
		Date d2 = DateUtils.parseDateTime("2011-12-22 16:07:00.327");
		
		if(!DateUtils.isOnOrBefore(d2,d1)){
			fail(d1.toString() +">"+ d2.toString());
		}
	}

	@Test
	public void testGreaterThanNull() throws ParseException {
		
		Date d1 = DateUtils.parseDateTime("2011-12-22 16:07:09.327");
		
		if(!DateUtils.isOnOrAfter(null,d1)){
			fail(d1.toString() +"<"+ null);
		}
	}

	@Test
	public void testLessThanNull() throws ParseException {
		
		Date d1 = DateUtils.parseDateTime("2011-12-22 16:07:09.327");
		
		if(!DateUtils.isOnOrBefore(d1,null)){
			fail(d1.toString() +">"+ null);
		}
	}

	@Test
	public void testEqualsNull() throws ParseException {		
		if(!DateUtils.isOnOrBefore(null,null)){
			fail(null +"="+ null);
		}
	}

	@Test
	public void testEquals() throws ParseException {
		
		Date d1 = DateUtils.parseDateTime("2011-12-22 16:07:09.327");
		Date d2 = DateUtils.parseDateTime("2011-12-22 16:07:09.327");
		
		if(!DateUtils.isOnOrBefore(d2,d1)){
			fail(d1.toString() +">"+ d2.toString());
		}
	}
}
