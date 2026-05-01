/*
 * transaction: org.nrg.transaction.Transaction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.transaction;


public abstract class Transaction <T> {
	public Transaction<T> prev = null;
	public Transaction<T> next = null;
	
	public abstract T run() throws TransactionException;
	public abstract void rollback() throws RollbackException;
	
	public Transaction<T> bind(Transaction<T> t) {
		this.setNext(t);
		t.setPrev(this);
		return t;
	}

	public Transaction<T> getNext() {
		return this.next;
	}

	public Transaction<T> getPrev() {
		return this.prev;
	}
	
	public void setPrev(Transaction<T> p) {
		this.prev = p;
	}
	
	public void setNext(Transaction<T> n) {
		this.next = n;
	}
}
