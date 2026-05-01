/*
 * PrearcImporter: org.nrg.UncompressorTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.GUnzip;
import org.apache.tools.ant.taskdefs.BUnzip2;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.nrg.framework.status.LoggerStatusReporter;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class UncompressorTest {
	private final static File sourceDir = new File("src/test/data");
	private final static File workingDir = new File("target/test-data");
	private final static File file1 = new File(sourceDir, "file1.txt");
	private final static String gzFile1Name = "file1-gz.txt.gz";
	private final static String ugzFile1Name = "file1-gz.txt";
	private final static String bzFile1Name = "file1-bz.txt.bz2";
	private final static String ubzFile1Name = "file1-bz.txt";
	private final static String[] compressedFileNames = { gzFile1Name, bzFile1Name };

	final Project project = new Project();

	public UncompressorTest() {
		project.setBaseDir(new File(".").getAbsoluteFile());
	}

	@Before
	public final void createWorkingDir() {
		removeWorkingDir();

		workingDir.mkdirs();
		assertTrue(workingDir.isDirectory());

		final Copy copy = new Copy();
		copy.setProject(project);
		copy.setTodir(workingDir);
		for (final String name : compressedFileNames) {
			copy.setFile(new File(sourceDir, name));
			copy.execute();
		}
	}

	@After
	public final void removeWorkingDir() {
		final Delete delete = new Delete();
		delete.setProject(project);
		delete.setDir(workingDir);
		delete.execute();
		assertFalse(workingDir.exists());
	}

	/**
	 * Test method for {@link org.nrg.Uncompressor#unpack(java.io.File, java.io.File)}.
	 */
	@Test
	public final void testUnpackFileFile() {
		final Uncompressor gzu = new Uncompressor(new GUnzip(), project);
		gzu.addStatusListener(new LoggerStatusReporter(Uncompressor.class));

		final File ugzf = new File(workingDir, ugzFile1Name);
		ugzf.delete();
		assertFalse(ugzf.exists());
		final File gzf = new File(workingDir, gzFile1Name);
		gzu.unpack(gzf, ugzf);
		assertTrue(ugzf.isFile());
		assertEquals(ugzf.length(), file1.length());
		// TODO: better to compare contents, maybe checksum?

		final Uncompressor bzu = new Uncompressor(new BUnzip2(), project);

		final File ubzf = new File(workingDir, ubzFile1Name);
		ubzf.delete();
		assertFalse(ubzf.exists());
		final File bzf = new File(workingDir, bzFile1Name);
		bzu.unpack(bzf);
		assertTrue(ubzf.isFile());
		assertEquals(ubzf.length(), file1.length());
	}

	/**
	 * Test method for {@link org.nrg.Uncompressor#Uncompressor(org.apache.tools.ant.taskdefs.Unpack, org.apache.tools.ant.Project)}.
	 */
	@Test
	public final void testUncompressor() {
		final Uncompressor gzu = new Uncompressor(new GUnzip(), project);
		assertNotNull(gzu);
		final Uncompressor bzu = new Uncompressor(new BUnzip2(), project);
		assertNotNull(bzu);
	}

}
