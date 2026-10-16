/*
 * DicomEdit: FunctionArgumentVariableErrorTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.dicomedit;

import org.dcm4che3.data.Tag;
import org.junit.Test;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultSeverity;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * XNAT-8499: variables appearing as a <em>function argument</em> or a <em>list element</em> were
 * broken in two separate ways, both fixed here.
 *
 * <p><b>They were never supplied from the context.</b>
 * {@code DiscoverVariablesTreeVisitor.visitFunction} returned without visiting its children, so an
 * identifier nested in a function argument was never registered as an external variable — and that
 * set is exactly what {@code BaseScriptApplicator} seeds from the {@code MizerContext}. An injected
 * variable used only inside a function call therefore could not work on any site, under any
 * configuration: {@code (0010,0010) := format["{0}^{1}", subject, session]} always failed even with
 * both values supplied. Discovery now descends, and {@code visitVariable} registers what it finds.
 *
 * <p><b>Failures in those positions produced a raw NPE.</b>
 * {@code DicomEditParseTreeVisitor.visitVariable} dereferenced {@code getVariable(id).getValue()}
 * with no guard, giving
 * {@code Cannot invoke "...Variable.getValue()" because ... getVariable(String) is null}. It now
 * resolves through {@code extractVariableValue}, the same guarded path {@code visitIdValue} uses,
 * so an unsupplied variable in these positions reports the same actionable error as one on the
 * right-hand side of an assignment.
 *
 * <p>Per the grammar, {@code variable} appears as {@code idValue} (already guarded),
 * {@code variableArgument} and {@code list_element}; this covers the latter two.
 *
 * <p>The {@code stillResolves} tests are the regression guard that matters most: these changes
 * alter both discovery and resolution for <em>every</em> function argument and list element in
 * every script, so the happy paths are pinned explicitly.
 */
public class FunctionArgumentVariableErrorTests {

    private static final ResourceManager RESOURCES = ResourceManager.getInstance();

    /** Has SeriesDescription "t1_mpr_1mm_p2_pos50", Manufacturer "SIEMENS", PatientName "Sample Patient". */
    private static final File DICOM_FILE = RESOURCES.getTestResourceFile(
            "dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");

    private static final String RAW_NPE = "Cannot invoke";

    /** Applies the statements, supplying `subject` from the context only when asked. */
    private AnonymizationResult apply(final String subject, final String... statements) throws Exception {
        return applicator(subject, statements).apply(DicomObjectFactory.newInstance(DICOM_FILE));
    }

    private BaseScriptApplicator applicator(final String subject, final String... statements) throws Exception {
        final MizerContextWithScript context = new MizerContextWithScript();
        final List<String> script = new ArrayList<>();
        script.add("version \"6.1\"");
        script.addAll(Arrays.asList(statements));
        context.setScript(script);
        context.setElement("project", "PROJ01");
        context.setElement("session", "SESS01");
        if (subject != null) {
            context.setElement("subject", subject);
        }
        return BaseScriptApplicator.getInstance(context);
    }

    // ---- injected variables inside functions and lists are now supplied ------------------

    @Test
    public void injectedVariableInsideAFunctionIsDiscovered() throws Exception {
        assertTrue("a variable referenced only inside a function argument must be discovered"
                   + " as external, or it is never seeded from the context",
                applicator("SUBJ01", "(0010,0010) := format[\"{0}\", subject]")
                        .getExternalVariableNames().contains("subject"));
    }

    @Test
    public void injectedVariableInsideAFunctionIsSupplied() throws Exception {
        final AnonymizationResult result = apply("SUBJ01", "(0010,0010) := format[\"x{0}y\", subject]");
        assertEquals("expected success, got: " + result.getMessage(),
                AnonymizationResultSeverity.SUCCESS, result.getSeverity());
        assertEquals("xSUBJ01y", result.getDicomObject().getString(Tag.PatientName));
    }

    @Test
    public void twoInjectedVariablesInOneFunctionAreSupplied() throws Exception {
        // The shape that could not work at all before: PatientName built from subject and session.
        final AnonymizationResult result = apply("SUBJ01", "(0010,0010) := format[\"{0}^{1}\", subject, session]");
        assertEquals("expected success, got: " + result.getMessage(),
                AnonymizationResultSeverity.SUCCESS, result.getSeverity());
        assertEquals("SUBJ01^SESS01", result.getDicomObject().getString(Tag.PatientName));
    }

    @Test
    public void injectedVariableInsideNestedFunctionsIsSupplied() throws Exception {
        final AnonymizationResult result = apply("SUBJ01", "(0010,0010) := format[\"{0}\", lowercase[subject]]");
        assertEquals("expected success, got: " + result.getMessage(),
                AnonymizationResultSeverity.SUCCESS, result.getSeverity());
        assertEquals("subj01", result.getDicomObject().getString(Tag.PatientName));
    }

    @Test
    public void injectedVariableInAListElementIsDiscovered() throws Exception {
        assertTrue(applicator("SUBJ01", "tags := { subject }", "(0010,0010) := format[\"{0}\", subject]")
                .getExternalVariableNames().contains("subject"));
    }

    // ---- happy paths: resolution through these nodes must be unchanged -------------------

    @Test
    public void definedVariableInFunctionArgumentStillResolves() throws Exception {
        final AnonymizationResult result = apply(null,
                "foo := \"abc\"",
                "(0008,103e) := format[\"x{0}y\", foo]");
        assertEquals("expected success, got: " + result.getMessage(),
                AnonymizationResultSeverity.SUCCESS, result.getSeverity());
        assertEquals("xabcy", result.getDicomObject().getString(Tag.SeriesDescription));
    }

    @Test
    public void tagInFunctionArgumentStillResolves() throws Exception {
        final AnonymizationResult result = apply(null, "(0008,103e) := format[\"x{0}y\", (0008,0070)]");
        assertEquals("expected success, got: " + result.getMessage(),
                AnonymizationResultSeverity.SUCCESS, result.getSeverity());
        assertEquals("xSIEMENSy", result.getDicomObject().getString(Tag.SeriesDescription));
    }

    @Test
    public void listOfTagsStillResolves() throws Exception {
        final AnonymizationResult result = apply(null,
                "tags := { (0010,0010), (0008,0070) }",
                "removeTags[ tags]");
        assertEquals("expected success, got: " + result.getMessage(),
                AnonymizationResultSeverity.SUCCESS, result.getSeverity());
        assertFalse("removeTags should have removed PatientName",
                result.getDicomObject().contains(Tag.PatientName));
    }

    // ---- unsupplied variables in these positions now report an actionable error ----------

    @Test
    public void unsuppliedVariableInFunctionArgumentIsNamed() throws Exception {
        final String message = apply(null, "(0008,103e) := format[\"x{0}y\", subject]").getMessage();
        assertTrue("error should name the variable, but was: " + message,
                message.contains("Variable 'subject' is null."));
        assertFalse("error should not surface raw NPE text, but was: " + message,
                message.contains(RAW_NPE));
    }

    @Test
    public void unsuppliedVariableInListElementIsNamed() throws Exception {
        final String message = apply(null,
                "tags := { subject, (0008,0070) }",
                "foo := collectValues[ tags]").getMessage();
        assertTrue("error should name the variable, but was: " + message,
                message.contains("Variable 'subject' is null."));
        assertFalse("error should not surface raw NPE text, but was: " + message,
                message.contains(RAW_NPE));
    }

    @Test
    public void errorInFunctionArgumentNamesLineAndStatement() throws Exception {
        final String message = apply(null, "(0008,103e) := format[\"x{0}y\", subject]").getMessage();
        assertTrue("error should locate the failure at line 2, but was: " + message,
                message.contains("line 2"));
        assertTrue("error should quote the failing statement, but was: " + message,
                message.contains("format[\"x{0}y\",subject]"));
    }
}
