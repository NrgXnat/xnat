package org.nrg.transaction;

public interface OperationI<A> {
	public void run (A a) throws Throwable ;
}
