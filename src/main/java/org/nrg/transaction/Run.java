/*
 * org.nrg.transaction.Run
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.transaction;


public final class Run {
	public static void runTransaction (Transaction t) throws TransactionException, RollbackException {
		Transaction n = t;
		try {
			n.run();
			while ((n = n.getNext()) != null){
				n.run(); 
			}
		}
		catch (TransactionException e) {
			n.rollback();
			Transaction p = n;
			while ((p = p.getPrev()) != null){
				p.rollback();
			}
			throw e;
		}
	}
}
