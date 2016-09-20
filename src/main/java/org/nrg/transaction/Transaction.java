/*
 * org.nrg.transaction.Transaction
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.transaction;


public abstract class Transaction {
	public Transaction prev = null;
	public Transaction next = null;
	
	public abstract void run() throws TransactionException;
	public abstract void rollback() throws RollbackException;
	
	public Transaction bind(Transaction t) {
		this.setNext(t);
		t.setPrev(this);
		return t;
	}

	public Transaction getNext() {
		return this.next;
	}

	public Transaction getPrev() {
		return this.prev;
	}
	
	public void setPrev(Transaction p) {
		this.prev = p;
	}
	
	public void setNext(Transaction n) {
		this.next = n;
	}
}
