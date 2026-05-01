/*
 * PrearcImporter: org.nrg.Uncompressor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Unpack;

/**
 * Uncompresses a file using a specific Ant Unpack method.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class Uncompressor extends Unpacker {
	private final Unpack unpacker;

	public Uncompressor(final Unpack unpacker, final Project project) {
		this.unpacker = unpacker;
		this.unpacker.setProject(project);
	}

	public void unpack(final File file, final File destination) {
		publishStatus(file, "extracting");
		unpacker.setSrc(file);
		if (destination != null) {
			destination.getParentFile().mkdirs();
			unpacker.setDest(destination);
		}

		try {
			unpacker.execute();
			file.delete();
		} catch (BuildException e) {
			publishFailure(file, e.getMessage());
		}
	}
}
