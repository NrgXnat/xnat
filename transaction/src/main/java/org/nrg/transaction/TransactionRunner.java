/*
 * transaction: org.nrg.transaction.Run
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.transaction;


public final class TransactionRunner<T> {
	public T runTransaction (Transaction<T> t) throws TransactionException, RollbackException {
		T result;
		Transaction<T> n = t;
		try {
			result = n.run();
			while ((n = n.getNext()) != null){
				result = n.run();
			}
			return result;
		}
		catch (TransactionException e) {
			n.rollback();
			Transaction<T> p = n;
			while ((p = p.getPrev()) != null){
				p.rollback();
			}
			throw e;
		}
	}
}
