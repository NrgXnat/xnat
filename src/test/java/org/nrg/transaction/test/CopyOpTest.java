/*
 * anonymize: org.nrg.dcm.test.CopyOpTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.transaction.test;


import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.transaction.OperationI;
import org.nrg.transaction.RollbackException;
import org.nrg.transaction.TransactionRunner;
import org.nrg.transaction.TransactionException;
import org.nrg.transaction.operations.CopyOp;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class CopyOpTest {
	static File tmpDir = new File(System.getProperty("java.io.tmpdir"));
	static File dirA;
	static File dirB;
	static File dirC;
	static File backupDir;

	@Before
	public void setUp() throws Exception {
		dirA = new File(tmpDir,"a");
		dirB = new File(tmpDir,"b");
		dirC = new File(tmpDir,"c");
		if (dirA.exists()) {
			FileUtils.deleteDirectory(dirA);
		}
		if(dirB.exists()) {
			FileUtils.deleteDirectory(dirB);
		}
		if (dirC.exists()) {
			FileUtils.deleteDirectory(dirC);
		}
		
		backupDir = new File(tmpDir,"backup");
		if (backupDir.exists()) {
			FileUtils.deleteDirectory(backupDir);
		}
		
		dirA.mkdirs();
		dirB.mkdirs();
		dirC.mkdirs();
		backupDir.mkdirs();
		addToFile(dirA, "Hello Java");
		addToFile(dirB, "Hello Java");
		addToFile(dirC, "Hello Java");
	}	

	@After
	public void tearDown() throws Exception {
		if (dirA != null && dirA.exists()) {
			FileUtils.deleteDirectory(dirA);
		}
		if (dirB != null && dirB.exists()) {
			FileUtils.deleteDirectory(dirB);
		}
		if (dirC != null && dirC.exists()) {
			FileUtils.deleteDirectory(dirC);
		}
		if (backupDir != null && backupDir.exists()) {
			FileUtils.deleteDirectory(backupDir);
		}
	}
	
	public void addToFile(File f, String l) throws IOException {
		FileWriter fstream = new FileWriter(new File(f, "out.txt"),false);
		BufferedWriter out = new BufferedWriter(fstream);
		
		out.write(l);
		out.close();
	}
	
	public String readFile(File f) throws IOException{
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(
				new FileReader(new File(f, "out.txt")));
		char[] buf = new char[1024];
		int numRead=0;
		while((numRead=reader.read(buf)) != -1){
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}
	
	@Test
	public final void testCleanUpSingleDirectory() throws RollbackException, TransactionException {
		OperationI<Map<String,File>> o = new OperationI<Map<String,File>>() {
			public void run(Map<String,File> s) throws Exception {}
		};
		CopyOp copyOp = new CopyOp(o, backupDir, dirA);
		new TransactionRunner<Void>().runTransaction(copyOp);
		assertFalse(backupDir.exists());
	}
	
	@Test 
	public final void testNonEmptyBackupDirectory() throws RollbackException, TransactionException, IOException {
		File f = new File(backupDir, "donttouch");
		f.mkdirs();
		addToFile(f, "Hello world");
		
		OperationI<Map<String,File>> o = new OperationI<Map<String,File>>() {
			public void run(Map<String,File> s) throws Exception {}
		};
		CopyOp copyOp = new CopyOp(o,backupDir, dirA);
		new TransactionRunner<Void>().runTransaction(copyOp);
		assertTrue(backupDir.exists());
		assertTrue(backupDir.list().length == 1);
		assertTrue(f.exists());
	}
	
	@Test
	public final void successfulSingleDirTest() {
		OperationI<Map<String,File>> o = new OperationI<Map<String,File>>() {
			@Override
			public void run(Map<String,File> s) throws IOException {
				for (String n : s.keySet()) {
					File f = s.get(n);
					FileWriter fstream = new FileWriter(new File(f, "out.txt"),false);
					BufferedWriter out = new BufferedWriter(fstream);
					
					out.write("Goodbye Java");
					out.close();
				}
			}
		};
		
		CopyOp op = new CopyOp(o,backupDir, dirA);
		
		try {
			new TransactionRunner<Void>().runTransaction(op);
		} catch (RollbackException e) {
			fail("");
		} catch (TransactionException e) {
			fail("");
		}
		String fileContents = null;
		try {
			fileContents = readFile(dirA);
		} catch (IOException e) {
			fail("");
		}
		assertEquals(fileContents,"Goodbye Java");
	}
	
	@Test
	public final void nullFileTest() {
		OperationI<Map<String,File>> o = new OperationI<Map<String,File>>() {
			@Override
			public void run(Map<String,File> fs) throws Throwable {
				for (String n : fs.keySet()) {
					if (fs.get(n) != null) {
						File f = fs.get(n);
						FileWriter fstream = new FileWriter(new File(f, "out.txt"),false);
						BufferedWriter out = new BufferedWriter(fstream);
						
						out.write("Goodbye Java");
						out.close();
					}
				}
			}
		};
		
		Map<String,File> fs = new HashMap<String,File>();
		fs.put("dirA", dirA);
		fs.put("nonExistent", null);
		CopyOp op = new CopyOp(o,backupDir,fs);
		
		try {
			new TransactionRunner<Void>().runTransaction(op);
			fail();
		} catch (RollbackException e) {
			fail(e.getMessage());
		} catch (TransactionException e) {
			// should be thrown
		}
		for (File f : fs.values()) {
			String fileContents = null;
			if (f != null) {
				try {
					fileContents = readFile(f);
				} catch (IOException e) {
					fail("");
				}
				assertEquals("Hello Java", fileContents);
			}
		}
	}
	
	@Test
	public final void fileSetTest() {
		OperationI<Map<String,File>> o = new OperationI<Map<String,File>>() {
			@Override
			public void run(Map<String,File> fs) throws Throwable {
				for (String n : fs.keySet()) {
					if (fs.get(n) != null) {
						File f = fs.get(n);
						FileWriter fstream = new FileWriter(new File(f, "out.txt"),false);
						BufferedWriter out = new BufferedWriter(fstream);
						
						out.write("Goodbye Java");
						out.close();
					}
				}
			}
		};
		Set<File> fs = new HashSet<File>();
		fs.add(dirA);
		fs.add(dirB);

		CopyOp op = new CopyOp(o,backupDir,fs);
		
		try {
			new TransactionRunner<Void>().runTransaction(op);
		} catch (RollbackException e) {
			fail(e.getMessage());
		} catch (TransactionException e) {
			fail(e.getMessage());
		}
		for (File f : fs) {
			String fileContents = null;
			if (f != null) {
				try {
					fileContents = readFile(f);
				} catch (IOException e) {
					fail("");
				}
				assertEquals(fileContents,"Goodbye Java");
			}
		}
	}
	
	@Test
	public final void unSuccessfulSingleDirTest() {
		OperationI<Map<String,File>> o = new OperationI<Map<String,File>>() {
			@Override
			public void run(Map<String,File> s) throws Throwable {
				for (String n : s.keySet()) {
					File f = s.get(n);
					FileWriter fstream = new FileWriter(new File(f, "out.txt"),false);
					BufferedWriter out = new BufferedWriter(fstream);
					
					out.write("Goodbye Java");
					out.close();
				}
				throw new Exception();
				
			}
		};
		
		CopyOp op = new CopyOp(o,backupDir,dirA);
		
		try {
			new TransactionRunner<Void>().runTransaction(op);
		} catch (RollbackException e) {
			fail(e.getMessage());
		} catch (TransactionException e) {

		}
		String fileContents = null;
		try {
			fileContents = readFile(dirA);
		} catch (IOException e) {
			fail("");
		}
		assertEquals(fileContents,"Hello Java");
	}
	
	@Test
	public void successfulMultipleDirTest() {
		Map<String,File> fs = new HashMap<String,File>();
		fs.put("dirA", dirA);
		fs.put("dirB", dirB);
		fs.put("dirC", dirC);

		OperationI<Map<String,File>> o = new OperationI<Map<String,File>>() {
			@Override
			public void run(Map<String,File> s) throws IOException {
				for (String n : s.keySet()) {
					File f = s.get(n);
					FileWriter fstream = new FileWriter(new File(f, "out.txt"),false);
					BufferedWriter out = new BufferedWriter(fstream);
					
					out.write("Goodbye Java");
					out.close();
				}
			}
		};
		
		CopyOp op = new CopyOp(o,backupDir,fs);
		
		try {
			new TransactionRunner<Void>().runTransaction(op);
		} catch (RollbackException e) {
			fail("");
		} catch (TransactionException e) {
			fail("");
		}
		for (File f : fs.values()) {
			String fileContents = null;
			try {
				fileContents = readFile(f);
			} catch (IOException e) {
				fail("");
			}
			assertEquals(fileContents,"Goodbye Java");	
		}
	}
	
	
	@Test
	public void unsuccessfulMultipleDirTest() {
		Map<String,File> fs = new HashMap<String,File>();
		fs.put("dirA", dirA);
		fs.put("dirB", dirB);
		fs.put("dirC", dirC);

		OperationI<Map<String,File>> o = new OperationI<Map<String,File>>() {
			@Override
			public void run(Map<String,File> s) throws Exception {
				for (String n : s.keySet()) {
					File f = s.get(n);
					FileWriter fstream = new FileWriter(new File(f, "out.txt"),false);
					BufferedWriter out = new BufferedWriter(fstream);
					
					out.write("Goodbye Java");
					out.close();
				}
				throw new Exception();
			}
		};
		
		CopyOp op = new CopyOp(o,backupDir,fs);
		
		try {
			new TransactionRunner<Void>().runTransaction(op);
		} catch (RollbackException e) {
			fail(e.getMessage());
		} catch (TransactionException e) {

		}
		
		for (File f : fs.values()) {
			String fileContents = null;
			try {
				fileContents = readFile(dirA);
			} catch (IOException e) {
				fail("");
			}
			assertEquals(fileContents,"Hello Java");	
		}
	}
}
