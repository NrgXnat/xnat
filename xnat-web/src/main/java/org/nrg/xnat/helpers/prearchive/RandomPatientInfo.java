/*
 * org.nrg.xnat.helpers.prearchive.RandomPatientInfo
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.prearchive;
import java.util.Calendar;
import java.util.Random;


public final class RandomPatientInfo {
	
	static String randomName (String prefix) {
		Random r = new Random();
		return prefix + Math.abs(r.nextInt());
	}
	static String[] randomNames (String prefix, int numNames) {
		String[] names = new String[numNames];
		for (int i = 0; i < names.length; i++) {
			names[i] = RandomPatientInfo.randomName(prefix);
		}
		return names;
	}
	
	static PrearcUtils.PrearcStatus randomStatus () {
		PrearcUtils.PrearcStatus[] statuses = {PrearcUtils.PrearcStatus.RECEIVING, PrearcUtils.PrearcStatus.READY, PrearcUtils.PrearcStatus.ERROR}; 
		return statuses[RandomPatientInfo.randomIndex(statuses)];
	}
	
	static String randomTimestamp() {
		java.util.Date d = RandomPatientInfo.randomDate();
		return (new java.sql.Timestamp(d.getTime())).toString();
		
	}
	static String[] sequentialNames(String prefix, int numNames) {
		String[] out = new String[numNames];
		for (int i = 0; i < numNames; i++) {
			out[i] = prefix + i;
		}
		return out;
	}

	static int randomIndex (Object[] os) {
		Random r = new Random();
		return (int) (r.nextInt(os.length -1));
	}
	
	static java.util.Date randomDate () {
		Calendar cdr = Calendar.getInstance();
		cdr.set(Calendar.YEAR, 2000);
		long val1 = cdr.getTimeInMillis();
		
		java.util.Date now = new java.util.Date();
		long val2 = now.getTime();
		
		Random r = new Random();
		long randomTS = (long) ((r.nextDouble() * (val2-val1)) + val1);
		return new java.util.Date(randomTS);
	}
	
	static java.util.Date after(java.util.Date d) {
		java.util.Date now = new java.util.Date();
		long val2 = now.getTime();
		long ts = d.getTime();
		Random r = new Random();
		long randomTS = (long) ((r.nextDouble() * (val2-ts)) + ts);
		return new java.util.Date(randomTS);
	}
}	
