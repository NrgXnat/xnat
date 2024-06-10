/*
 * web: org.nrg.xnat.helpers.transactions.TransactionUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.transactions;

import org.apache.commons.lang3.StringUtils;

public class TransactionUtils {
	
	private static final String TRANSACTION_PREFIX = "_TR";

	public static String buildTransactionID(final String s) throws IllegalArgumentException{
		if(StringUtils.isEmpty(s))throw new IllegalArgumentException();
		
		if(!s.startsWith(TRANSACTION_PREFIX))
			return TRANSACTION_PREFIX+s;
		else
			return s;
	}
}
