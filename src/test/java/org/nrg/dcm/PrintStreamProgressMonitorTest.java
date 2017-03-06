/*
 * DicomDB: org.nrg.dcm.PrintStreamProgressMonitorTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.nrg.progress.PrintStreamProgressMonitor;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class PrintStreamProgressMonitorTest {
	private static final String LINE_SEP = System.getProperty("line.separator");
	/**
	 * Test method for {@link org.nrg.progress.PrintStreamProgressMonitor#close()}.
	 */
	@Test
	public void testClose() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		final PrintStreamProgressMonitor mon = new PrintStreamProgressMonitor(ps);
		assertEquals("", baos.toString());
		mon.close();
		assertEquals(" complete" + LINE_SEP, baos.toString());
		
		baos.reset();
		assertEquals("", baos.toString());
		mon.setNote("testing");
		assertEquals("", baos.toString());
		mon.close();
		assertEquals("testing complete" + LINE_SEP, baos.toString());
	}

	/**
	 * Test method for {@link org.nrg.progress.PrintStreamProgressMonitor#isCanceled()}.
	 */
	@Test
	public void testIsCanceled() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		final PrintStreamProgressMonitor mon = new PrintStreamProgressMonitor(ps);
		assertFalse(mon.isCanceled());
		mon.setNote("testing");
		mon.setMaximum(1);
		mon.setProgress(1);
		assertFalse(mon.isCanceled());
	}

	/**
	 * Test method for {@link org.nrg.progress.PrintStreamProgressMonitor#setMaximum(int)}.
	 */
	@Test
	public void testSetMaximum() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		final PrintStreamProgressMonitor mon = new PrintStreamProgressMonitor(ps);
		mon.setMaximum(1);
		mon.setProgress(0);
		assertEquals(" (0/1)" + LINE_SEP, baos.toString());
		baos.reset();
		mon.setMaximum(2);
		mon.setProgress(1);
		assertEquals(" (1/2)" + LINE_SEP, baos.toString());
	}

	/**
	 * Test method for {@link org.nrg.progress.PrintStreamProgressMonitor#setMinimum(int)}.
	 */
	@Test
	public void testSetMinimum() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		final PrintStreamProgressMonitor mon = new PrintStreamProgressMonitor(ps);
		mon.setMinimum(0);
		mon.setMaximum(1);
		mon.setProgress(0);
		assertEquals(" (0/1)" + LINE_SEP, baos.toString());
		baos.reset();
		mon.setMinimum(1);
		mon.setMaximum(2);
		mon.setProgress(1);
		assertEquals(" (1/[1,2])" + LINE_SEP, baos.toString());
	}

	/**
	 * Test method for {@link org.nrg.progress.PrintStreamProgressMonitor#setNote(java.lang.String)}.
	 */
	@Test
	public void testSetNote() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		final PrintStreamProgressMonitor mon = new PrintStreamProgressMonitor(ps);
		mon.setMinimum(0);
		mon.setMaximum(1);
		mon.setProgress(0);
		assertEquals(" (0/1)" + LINE_SEP, baos.toString());
		baos.reset();
		mon.setNote("testing");
		mon.setProgress(0);
		assertEquals("testing (0/1)" + LINE_SEP, baos.toString());
	}

	/**
	 * Test method for {@link org.nrg.progress.PrintStreamProgressMonitor#setProgress(int)}.
	 */
	@Test
	public void testSetProgress() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		final PrintStreamProgressMonitor mon = new PrintStreamProgressMonitor(ps);
		mon.setMinimum(0);
		mon.setMaximum(1);
		mon.setProgress(0);
		assertEquals(" (0/1)" + LINE_SEP, baos.toString());
	}
}
