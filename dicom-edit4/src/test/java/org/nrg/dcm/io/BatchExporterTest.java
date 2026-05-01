/*
 * DicomEdit: org.nrg.dcm.io.BatchExporterTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.io;

import com.google.common.collect.Lists;
import org.easymock.MockControl;
import org.junit.Before;
import org.junit.Test;
import org.nrg.dcm.edit.Action;
import org.nrg.dicomtools.exceptions.AttributeMissingException;
import org.nrg.dcm.edit.Operation;
import org.nrg.dcm.edit.Statement;
import org.nrg.framework.io.FileWalkIterator;
import org.nrg.test.workers.resources.ResourceManager;
import org.nrg.framework.io.EditProgressMonitor;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class BatchExporterTest {
    private static final ResourceManager _resourceManager = ResourceManager.getInstance();

    private final int            nExports;
    private final Iterator<File> sampleDataIterator;

    private MockControl exporterControl,
            exportsIteratorControl,
            progressMonitorControl,
            actionControl,
            operationControl;

    public BatchExporterTest() {
        final File resource = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
        assertNotNull(resource);
        final File dataFolder = resource.getParentFile();
        assertNotNull(dataFolder);
        final String[] contents = dataFolder.list();
        assertNotNull(contents);
        nExports = contents.length;
        sampleDataIterator = new FileWalkIterator(dataFolder, null);
    }

    @Before
    public void setUp() {
        exporterControl = MockControl.createControl(DicomObjectExporter.class);
        exportsIteratorControl = MockControl.createControl(Iterator.class);
        progressMonitorControl = MockControl.createControl(EditProgressMonitor.class);
        actionControl = MockControl.createControl(Action.class);
        operationControl = MockControl.createControl(Operation.class);
    }

    /**
     * Test method for {@link BatchExporter#BatchExporter(DicomObjectExporter, List, Iterable)}.
     */
    @Test
    public void testBatchExporterDicomObjectExporterStatementListIterator() {
        final DicomObjectExporter exporter = (DicomObjectExporter) exporterControl.getMock();
        final List<Statement> statements = Lists.newArrayList();
        @SuppressWarnings("unchecked")
        final Iterator<File> exports = (Iterator<File>) exportsIteratorControl.getMock();
        final BatchExporter batchExporter = new BatchExporter(exporter, statements, exports);
        assertNotNull(batchExporter);
    }

    /**
     * Test method for {@link BatchExporter#setProgressMonitor(EditProgressMonitor, int)}.
     */
    @Test
    public void testSetProgressMonitor() {
        final DicomObjectExporter exporter = (DicomObjectExporter) exporterControl.getMock();
        final List<Statement> statements = Lists.newArrayList();
        @SuppressWarnings("unchecked")
        final Iterator<File> exports = (Iterator<File>) exportsIteratorControl.getMock();
        final BatchExporter batchExporter = new BatchExporter(exporter, statements, exports);
        final EditProgressMonitor pm = (EditProgressMonitor) progressMonitorControl.getMock();
        batchExporter.setProgressMonitor(pm, 0);
        assertTrue(0 == batchExporter.getProgress());
        batchExporter.setProgressMonitor(pm, 42);
        assertTrue(42 == batchExporter.getProgress());
    }

    /**
     * Test method for {@link BatchExporter#isPending()}.
     */
    @Test
    public void testIsPending() {
        final DicomObjectExporter exporter = (DicomObjectExporter) exporterControl.getMock();
        final List<Statement> statements = Lists.newArrayList();
        @SuppressWarnings("unchecked")
        final Iterator<File> exports = (Iterator<File>) exportsIteratorControl.getMock();
        final BatchExporter batchExporter = new BatchExporter(exporter, statements, exports);
        assertTrue(batchExporter.isPending());
        batchExporter.run();
        assertFalse(batchExporter.isPending());
    }

    /**
     * Test method for {@link BatchExporter#getFailures()}.
     */
    @Test
    public void testGetFailures() {
        final DicomObjectExporter exporter = (DicomObjectExporter) exporterControl.getMock();
        final List<Statement> statements = Lists.newArrayList();
        @SuppressWarnings("unchecked")
        final Iterator<File> exports = (Iterator<File>) exportsIteratorControl.getMock();
        final BatchExporter batchExporter = new BatchExporter(exporter, statements, exports);
        try {
            batchExporter.getFailures();
            fail("missed expected IllegalStateException");
        } catch (IllegalStateException ignored) {
        }
        batchExporter.run();
        assertTrue(batchExporter.getFailures().isEmpty());
    }

    /**
     * Test method for {@link BatchExporter#run()}.
     */
    @Test
    public void testRun() throws Exception {
        final DicomObjectExporter exporter = (DicomObjectExporter) exporterControl.getMock();
        final List<Statement> statements = Collections.emptyList();
//        statements.getActions(null, null);
//        statementsControl.setMatcher(MockControl.ALWAYS_MATCHER);
//        statementsControl.setReturnValue(Collections.emptyList(), nExports);
//        statementsControl.replay();

        final BatchExporter batchExporter = new BatchExporter(exporter, statements, sampleDataIterator);
        batchExporter.run();
        assertTrue(batchExporter.getFailures().isEmpty());
    }

    /**
     * Test method for {@link BatchExporter#run()}.
     */
    @Test
    public void testRunWithExportErrors() throws Exception {
        final DicomObjectExporter exporter = (DicomObjectExporter) exporterControl.getMock();
        exporter.export(null, null);
        exporterControl.setMatcher(MockControl.ALWAYS_MATCHER);
        exporterControl.setThrowable(new OutOfMemoryError(), nExports);
        exporter.close();
        exporterControl.setVoidCallable();
        exporterControl.replay();

        final List<Statement> statements = Lists.newArrayList();
//        final StatementList statements = (StatementList) statementsControl.getMock();
//        statements.getActions(null, null);
//        statementsControl.setMatcher(MockControl.ALWAYS_MATCHER);
//        statementsControl.setReturnValue(Collections.emptyList(), nExports);
//        statementsControl.replay();

        final BatchExporter batchExporter = new BatchExporter(exporter, statements, sampleDataIterator);
        batchExporter.run();
        assertTrue(nExports == batchExporter.getFailures().size());
    }

    /**
     * Test method for {@link BatchExporter#run()}.
     */
    @Test
    public void testRunWithScriptErrors() throws Exception {
        final int nWorking = 5;

        final DicomObjectExporter exporter = (DicomObjectExporter) exporterControl.getMock();
        exporter.export(null, null);
        exporterControl.setMatcher(MockControl.ALWAYS_MATCHER);
        exporterControl.setVoidCallable(nExports);
        exporter.close();
        exporterControl.setVoidCallable();
        exporterControl.replay();

        final Action action = (Action) actionControl.getMock();
        action.apply();
        actionControl.setVoidCallable(nWorking);
        actionControl.replay();

        final Operation operation = (Operation) operationControl.getMock();
        operation.makeAction(null);
        operationControl.setMatcher(MockControl.ALWAYS_MATCHER);
        operationControl.setReturnValue(action, nWorking);
        operationControl.setDefaultThrowable(new AttributeMissingException(0));
        operationControl.replay();

        final Statement statement = new Statement(null, operation);
        final List<Statement> statements = Lists.newArrayList(statement);
        final BatchExporter batchExporter = new BatchExporter(exporter, statements, sampleDataIterator);
        batchExporter.run();
        assertEquals(nExports - nWorking, batchExporter.getFailures().size());
    }
}
