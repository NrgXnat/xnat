/*
 * transaction: org.nrg.transaction.help.RunTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.transaction.help;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.nrg.transaction.RollbackException;
import org.nrg.transaction.TransactionRunner;
import org.nrg.transaction.Transaction;
import org.nrg.transaction.TransactionException;



public class TransactionRunnerTest {
	@Test
	public final void testRollbackA() {
		final ArrayList<String> s = new ArrayList<String>();
		Transaction a = new Transaction() {
			@Override
			public Void run() throws TransactionException {
				s.add("Run a");
				throw new TransactionException();
			}
			@Override
			public void rollback() throws RollbackException {
				s.add("Rollback a");
			}
		};
		Transaction b = new Transaction() {
			@Override
			public Void run() throws TransactionException {
				s.add("Run b");
				return null;
			}
			@Override
			public void rollback() throws RollbackException {
				s.add("Rollback b");
			}
		};
		
		Transaction c = new Transaction() {
			@Override
			public Void run() throws TransactionException {
				s.add("Run c");
				return null;
			}
			@Override
			public void rollback() throws RollbackException {
				s.add("Rollback c");
			}
		};
		a.bind(b).bind(c);
		try {
			new TransactionRunner<Void>().runTransaction(a);
		} catch (RollbackException e) {
			fail("");
		} catch (TransactionException e) {
			String x = StringUtils.join(s, ',');
			assertEquals(x, "Run a,Rollback a");
		}
	}
	
	@Test
	public final void testRollbackB() {
		final ArrayList<String> s = new ArrayList<String>();
		Transaction a = new Transaction() {
			@Override
			public Void run() throws TransactionException {
				s.add("Run a");
				return null;
			}
			@Override
			public void rollback() throws RollbackException {
				s.add("Rollback a");
			}
		};
		Transaction b = new Transaction() {
			@Override
			public Void run() throws TransactionException {
				s.add("Run b");
				throw new TransactionException();
			}
			@Override
			public void rollback() throws RollbackException {
				s.add("Rollback b");
			}
		};
		
		Transaction c = new Transaction() {
			@Override
			public Void run() throws TransactionException {
				s.add("Run c");
				return null;
			}
			@Override
			public void rollback() throws RollbackException {
				s.add("Rollback c");
			}
		};
		a.bind(b).bind(c);
		try {
			new TransactionRunner<Void>().runTransaction(a);
		} catch (RollbackException e) {
			fail("");
		} catch (TransactionException e) {
			String x = StringUtils.join(s, ',');
			assertEquals(x, "Run a,Run b,Rollback b,Rollback a");
		}
	}
	
	@Test
	public final void testRollbackC() {
		final ArrayList<String> s = new ArrayList<String>();
		Transaction a = new Transaction() {
			@Override
			public Void run() throws TransactionException {
				s.add("Run a");
				return null;
			}
			@Override
			public void rollback() throws RollbackException {
				s.add("Rollback a");
			}
		};
		Transaction b = new Transaction() {
			@Override
			public Void run() throws TransactionException {
				s.add("Run b");
				return null;
			}
			@Override
			public void rollback() throws RollbackException {
				s.add("Rollback b");
			}
		};
		
		Transaction c = new Transaction() {
			@Override
			public Void run() throws TransactionException {
				s.add("Run c");
				throw new TransactionException();
			}
			@Override
			public void rollback() throws RollbackException {
				s.add("Rollback c");
			}
		};
		a.bind(b).bind(c);
		try {
			new TransactionRunner<Void>().runTransaction(a);
		} catch (RollbackException e) {
			fail("");
		} catch (TransactionException e) {
			String x = StringUtils.join(s, ',');
			assertEquals(x, "Run a,Run b,Run c,Rollback c,Rollback b,Rollback a");
		}
	}
	@Test
	public final void testRunSuccessful() {
		final ArrayList<String> s = new ArrayList<String>();
		Transaction a = new Transaction() {
			@Override
			public Void run() throws TransactionException {
				s.add("Run a");
				return null;
			}
			@Override
			public void rollback() throws RollbackException {
				s.add("Rollback a");
			}
		};
		Transaction b = new Transaction() {
			@Override
			public Void run() throws TransactionException {
				s.add("Run b");
				return null;
			}
			@Override
			public void rollback() throws RollbackException {
				s.add("Rollback b");
			}
		};
		
		Transaction c = new Transaction() {
			@Override
			public Void run() throws TransactionException {
				s.add("Run c");
				return null;
			}
			@Override
			public void rollback() throws RollbackException {
				s.add("Rollback c");
			}
		};
		a.bind(b).bind(c);
		try {
			new TransactionRunner<Void>().runTransaction(a);
		} catch (RollbackException e) {
			fail("");
		} catch (TransactionException e) {
			fail("");
		}
		String x = StringUtils.join(s, ',');
		assertEquals(x, "Run a,Run b,Run c");
	}
}
