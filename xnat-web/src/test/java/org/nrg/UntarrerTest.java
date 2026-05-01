/*
 * PrearcImporter: org.nrg.UntarrerTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Untar;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.framework.status.LoggerStatusReporter;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class UntarrerTest {
	private final static File sourceDir = new File("src/test/data");
	private final static File workingDir = new File("target/test-data");
	private final static File destDir = new File("target/test-dest");

	private final static String baseName = "subdir1";
	private final static Map<String,String> methods = new HashMap<String,String>();
	static {
		methods.put(".tar", "none");
		methods.put(".tar.gz", "gzip");
		methods.put(".tar.bz2", "bzip2");
	}

	final Project project = new Project();

	public UntarrerTest() {
		project.setBaseDir(new File("."));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		removeWorkingDir();
		workingDir.mkdirs();
		assertTrue(workingDir.isDirectory());

		final Copy copy = new Copy();
		copy.setProject(project);
		copy.setTodir(workingDir);
		for (final String suffix : methods.keySet()) {
			copy.setFile(new File(sourceDir, baseName + suffix));
			copy.execute();
		}
	}

	private final void removeDir(final File dir) {
		final Delete delete = new Delete();
		delete.setProject(project);
		delete.setDir(dir);
		delete.execute();
		assertFalse(dir.exists());
	}

	@After
	public final void removeWorkingDir() throws Exception {
		removeDir(workingDir);
	}

	@After
	public final void removeDestDir() {
		removeDir(destDir);
	}

	/**
	 * Test method for {@link org.nrg.Untarrer#unpack(java.io.File, java.io.File)}.
	 */
	@Test
	public final void testUnpackFileFile() {
		for (final Map.Entry<String,String> me : methods.entrySet()) {
			removeDestDir();

			final Untarrer u = new Untarrer(me.getValue(), project);
			u.addStatusListener(new LoggerStatusReporter(Untarrer.class));

			final File typeDir = new File(destDir, me.getValue());
			assertFalse(typeDir.exists());
			typeDir.mkdirs();
			final File dd = new File(typeDir, baseName);
			final File tf = new File(dd, "file1.txt");

			assertFalse(tf.exists());
			u.unpack(new File(workingDir, baseName + me.getKey()), typeDir);
			assertTrue(tf.exists());

			removeDestDir();
		}

		// TODO: find a way to test/verify file locking in the Ant tasks.
	}

	/**
	 * Test method for {@link org.nrg.Untarrer#Untarrer(java.lang.String, org.apache.tools.ant.Project)}.
	 */
	@Test
	public final void testUntarrer() {
		for (final String method : new Untar.UntarCompressionMethod().getValues()) {
			assertNotNull(new Untarrer(method, project));
		}
		try {
			new Untarrer("no-such-method", project);
			fail("created Untarrer with invalid compression method");
		} catch (IllegalArgumentException e) {}
	}
}
