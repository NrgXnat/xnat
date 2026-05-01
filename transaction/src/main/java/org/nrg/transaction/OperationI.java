/*
 * transaction: org.nrg.transaction.OperationI
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.transaction;

public interface OperationI<A> {
	public void run (A a) throws Throwable ;
}
