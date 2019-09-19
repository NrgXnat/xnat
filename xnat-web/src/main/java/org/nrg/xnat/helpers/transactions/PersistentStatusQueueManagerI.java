/*
 * web: org.nrg.xnat.helpers.transactions.PersistentStatusQueueManagerI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.transactions;

import org.nrg.xnat.status.StatusList;

public interface PersistentStatusQueueManagerI {
	StatusList storeStatusQueue(final String id, final StatusList statusList) throws IllegalArgumentException;
	StatusList retrieveStatusQueue(final String id) throws IllegalArgumentException;
	StatusList deleteStatusQueue(final String id) throws IllegalArgumentException;
}
