/*
 * DicomEdit: org.nrg.dcm.io.BatchExporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.io;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.nrg.dcm.edit.Action;
import org.nrg.dcm.edit.Statement;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.framework.exceptions.CanceledOperationException;
import org.nrg.framework.io.EditProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class BatchExporter implements Runnable {
    private static final Exception canceledException = new CanceledOperationException();
    private final Logger logger = LoggerFactory.getLogger(BatchExporter.class);
    private final DicomObjectExporter objectExporter;
    private final List<Statement> statements;
    private final Iterator<?> toExport;
    private EditProgressMonitor pm = null;
    private int progress = -1;
    private final Map<Object,Throwable> failures = Maps.newLinkedHashMap();
    private boolean isPending = true;

    public BatchExporter(final DicomObjectExporter objectExporter, final Iterable<Statement> statements, final Iterator<?> toExport) {
        this.objectExporter = objectExporter;
        this.statements = Lists.newArrayList(statements);
        Collections.reverse(this.statements);
        this.toExport = toExport;
    }

    public BatchExporter(final DicomObjectExporter objectExporter,
            final List<Statement> statements, final Iterable<?> toExport) {
        this(objectExporter, statements, toExport.iterator());
    }

    public void setProgressMonitor(final EditProgressMonitor pm, final int progress) {
        this.pm = pm;
        this.progress = progress;
    }

    /**
     * Checks the status of the export operation.
     * @return true if the operation is running or waiting to start; false if finished
     */
    public boolean isPending() { return isPending; }

    /**
     * Returns a description of which exportables failed and why; if empty, all data
     * were exported successfully.
     * @return Map Object to Exception mapping exportables to the cause of export failure
     */
    public Map<?,Throwable> getFailures() {
        if (isPending) {
            throw new IllegalStateException("export not yet complete");
        } else {
            return failures;
        }
    }

    /**
     * Returns the progress monitor progress value.
     * @return the most recent argument to setProgress()
     */
    public int getProgress() { return progress; }

    public void run() {
        final EditProgressMonitor pm = this.pm;

        EXPORT_LOOP: while (toExport.hasNext())  {
            final Object object = toExport.next();
            if (null != pm) {
                if (pm.isCanceled()) {
                    failures.put(object, canceledException);
                    while (toExport.hasNext()) {
                        failures.put(toExport.next(), canceledException);
                    }
                    isPending = true;
                    return;
                } else {
                    pm.setNote(object.toString());
                }
            }
            logger.trace("Checking {}", object);

            final File         file;
            final DicomObjectI dicomObject;
            try {
                if (object instanceof File file1) {
                    file = file1;
                    dicomObject = DicomObjectFactory.newInstance(file);
                } else if (object instanceof URI uri) {
                    if ("file".equals(uri.getScheme())) {
                        file = new File(uri);
                        dicomObject = DicomObjectFactory.newInstance(file);
                    } else {
                        file = null;
                        try (final InputStream input = uri.toURL().openStream()) {
                            dicomObject = DicomObjectFactory.newInstance(input);
                        }
                    }
                } else if (object instanceof DicomObjectI) {
                    file = null;
                    dicomObject = (DicomObjectI)object;
//                    dicomObject = DicomObjectFactory.newInstance((DicomObject) object);
                } else {
                    failures.put(object, new IllegalArgumentException("cannot export class " + object.getClass().getName()));
                    continue EXPORT_LOOP;
                }
            } catch (Throwable e) {
                logger.debug("load failed for " + object, e);
                failures.put(object, e);
                continue EXPORT_LOOP;
            }

            try {
                for (final Action a : Statement.getActions(statements, file, dicomObject)) {
                    a.apply();
                }
            } catch (Throwable e) {
                logger.debug("script application failed for " + object, e);
                failures.put(object, e);
            }

            try {
                logger.trace("exporting {}", object);
                objectExporter.export(dicomObject, file);
                progress++;
                if (null != pm) {
                    pm.setProgress(progress);
                }
            } catch (Throwable e) {
                logger.debug("export failed for " + object, e);
                failures.put(object, e);
            }
        }

        objectExporter.close();
        if (null != pm) {
            pm.close();
        }

        logger.debug("Export completed");
        if (!failures.isEmpty()) {
            logger.debug("Failures: {}", failures);
        }
        isPending = false;
        
    }
}
