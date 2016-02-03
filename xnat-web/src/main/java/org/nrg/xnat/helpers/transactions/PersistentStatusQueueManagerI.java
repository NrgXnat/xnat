/*
 * org.nrg.xnat.helpers.transactions.PersistentStatusQueueManagerI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.transactions;

import org.nrg.status.StatusList;

public interface PersistentStatusQueueManagerI {
	public StatusList storeStatusQueue(final String id, final StatusList sq) throws IllegalArgumentException;
	public StatusList retrieveStatusQueue(final String id) throws IllegalArgumentException;
	public StatusList deleteStatusQueue(final String id) throws IllegalArgumentException;
}
