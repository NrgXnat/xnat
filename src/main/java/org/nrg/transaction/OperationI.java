/*
 * org.nrg.transaction.OperationI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:31 PM
 */
package org.nrg.transaction;

public interface OperationI<A> {
	public void run (A a) throws Throwable ;
}
