/*
 * PrearcImporter: org.nrg.SessionXMLBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.nrg.dcm.xnat.DICOMSessionBuilder;
import org.nrg.dcm.xnat.XnatAttrDef;
import org.nrg.ecat.xnat.PETSessionBuilder;
import org.nrg.session.SessionBuilder.NoUniqueSessionException;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.slf4j.LoggerFactory;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class SessionXMLBuilder {
	private final static int NUM_THREADS = 4;
	private final static ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

	private SessionXMLBuilder() { throw new UnsupportedOperationException(); }

	private static final XnatAttrDef[] buildConstDefs(final Map<String,String> values) {
		final Collection<XnatAttrDef> defs = new ArrayList<XnatAttrDef>(values.size());
		for (final Map.Entry<String,String> me : values.entrySet()) {
			defs.add(new XnatAttrDef.Constant(me.getKey(), me.getValue()));
		}
		return defs.toArray(new XnatAttrDef[0]);
	}

	private static final XnatImagesessiondataBean tryDicom(final File dir, final XnatAttrDef[] defs) {
		try {
			final DICOMSessionBuilder builder = new DICOMSessionBuilder(dir, defs);
			try {
				return build(builder);
			} finally {
				builder.close();
			}
		} catch (Throwable e) {
			LoggerFactory.getLogger(SessionXMLBuilder.class).trace("no valid DICOM series found in " + dir, e);
			return null;
		}
	}

	private static final XnatImagesessiondataBean tryEcat(final File dir, final Map<String,String> fields) {
		try {
			final PETSessionBuilder builder = new PETSessionBuilder(dir, null, fields.get("project"));
			try {
				return build(builder);
			} finally {
				// TODO: dispose?
			}
		} catch (Throwable e) {
			LoggerFactory.getLogger(SessionXMLBuilder.class).trace("no valid ECAT data found in " + dir, e);
			return null;
		}
	}

	public static boolean build(final File dir, final File xml, final Map<String,String> fields)
	throws IOException {
		final XnatAttrDef[] defs = buildConstDefs(fields);
		XnatImagesessiondataBean bean = tryDicom(dir, defs);
		if (null == bean) {
			bean = tryEcat(dir, fields);
		}
		if (null == bean) {
			LoggerFactory.getLogger(SessionXMLBuilder.class).info("No DICOM or ECAT data found in " + dir);
			return false;
		} else {
			final Writer writer = new FileWriter(xml);
			try {
				bean.toXML(writer, true);
				return true;
			} finally {
				writer.close();
			}
		}
	}

	static XnatImagesessiondataBean build(final Callable<XnatImagesessiondataBean> builder)
	throws InterruptedException,ExecutionException {
		return executor.submit(builder).get();
	}

	static boolean run(final Runnable builder)
	throws IOException,SQLException,NoUniqueSessionException {
		try {
			return null == executor.submit(builder).get();
		} catch (InterruptedException e) {
			return false;
		} catch (ExecutionException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof IOException) {
				throw (IOException)cause;
			} else if (cause instanceof SQLException) {
				throw (SQLException)cause;
			} else if (cause instanceof NoUniqueSessionException) {
				throw (NoUniqueSessionException)cause;
			} else {
				LoggerFactory.getLogger(SessionXMLBuilder.class).warn("unexpected exception type", e);
				throw new RuntimeException(cause);
			}
		}
	}
}
