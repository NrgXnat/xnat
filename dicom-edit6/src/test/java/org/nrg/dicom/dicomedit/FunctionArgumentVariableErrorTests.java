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
 * XNAT-8499: variables appearing as a <em>function argument</em> or a <em>list element</em>
 * resolve through {@link DicomEditParseTreeVisitor#visitVariable}, which dereferenced
 * {@code getVariable(id).getValue()} without a guard. An undefined or null-valued variable in
 * those positions produced a raw NPE:
 * <pre>
 * Cannot invoke "org.nrg.dicom.mizer.variables.Variable.getValue()" because the return value of
 * "org.nrg.dicom.dicomedit.DicomEditParseTreeVisitor.getVariable(String)" is null
 * </pre>
 * Routing that node through {@code extractVariableValue} gives these positions the same
 * actionable errors as the right-hand side of an assignment already had.
 *
 * <p>Per the grammar, {@code variable} appears as {@code idValue} (already guarded),
 * {@code variableArgument} (a function argument) and {@code list_element} — so this covers the
 * latter two.
 *
 * <p>An identifier nested in a function argument is never registered by variable discovery, so it
 * is genuinely unknown at evaluation time rather than merely null, and is reported as
 * {@code Unknown variable: 'x'}.
 *
 * <p>The three {@code stillResolves} tests are the regression guard that matters most here:
 * this change alters resolution for <em>every</em> function argument and list element in every
 * script, so the happy paths are pinned explicitly.
 *
 * <p>Note a separate, pre-existing defect this does <em>not</em> fix: because discovery does not
 * descend into function arguments, an injected variable used only inside a function call is never
 * supplied from the context even when routing rules resolve it. The message is now clear, but the
 * script still cannot work. See {@link #injectedVariableUsedOnlyInAFunctionIsNeverSupplied()}.
 */
public class FunctionArgumentVariableErrorTests {

    private static final ResourceManager RESOURCES = ResourceManager.getInstance();

    /** Has SeriesDescription "t1_mpr_1mm_p2_pos50" and Manufacturer "SIEMENS". */
    private static final File DICOM_FILE = RESOURCES.getTestResourceFile(
            "dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");

    private static final String RAW_NPE = "Cannot invoke";

    /** Applies the statements with `subject` supplied only when asked. */
    private AnonymizationResult apply(final String subject, final String... statements) throws Exception {
        final MizerContextWithScript context = new MizerContextWithScript();
        final List<String> script = new ArrayList<>();
        script.add("version \"6.1\"");
        script.addAll(Arrays.asList(statements));
        context.setScript(script);
        context.setElement("project", "PROJ01");
        if (subject != null) {
            context.setElement("subject", subject);
        }
        return BaseScriptApplicator.getInstance(context).apply(DicomObjectFactory.newInstance(DICOM_FILE));
    }

    // ---- happy paths: resolution through this node must be unchanged --------------------

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
        assertTrue("expected the Manufacturer value to be interpolated, got: "
                   + result.getDicomObject().getString(Tag.SeriesDescription),
                result.getDicomObject().getString(Tag.SeriesDescription).startsWith("x"));
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

    // ---- the fix -------------------------------------------------------------------------

    @Test
    public void undefinedVariableInFunctionArgumentIsNamed() throws Exception {
        final String message = apply(null, "(0008,103e) := format[\"x{0}y\", subject]").getMessage();
        assertTrue("error should name the unknown variable, but was: " + message,
                message.contains("Unknown variable: 'subject'"));
    }

    @Test
    public void undefinedVariableInFunctionArgumentDoesNotLeakRawNullPointerText() throws Exception {
        final String message = apply(null, "(0008,103e) := format[\"x{0}y\", subject]").getMessage();
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

    @Test
    public void undefinedVariableInListElementIsNamed() throws Exception {
        final String message = apply(null,
                "tags := { subject, (0008,0070) }",
                "foo := collectValues[ tags]").getMessage();
        assertTrue("error should name the unknown variable, but was: " + message,
                message.contains("Unknown variable: 'subject'"));
        assertFalse("error should not surface raw NPE text, but was: " + message,
                message.contains(RAW_NPE));
    }

    @Test
    public void nullValuedVariableInFunctionArgumentIsNamed() throws Exception {
        // Here `subject` IS registered (it is used bare on the later line), so it is defined
        // but null when the earlier function argument evaluates it.
        final String message = apply(null,
                "(0008,103e) := format[\"x{0}y\", subject]",
                "(0010,0010) := subject").getMessage();
        assertTrue("error should report the null variable by name, but was: " + message,
                message.contains("Variable 'subject' is null."));
        assertFalse("error should not surface raw NPE text, but was: " + message,
                message.contains(RAW_NPE));
    }

    // ---- documents a defect this change does NOT fix -------------------------------------

    @Test
    public void injectedVariableUsedOnlyInAFunctionIsNeverSupplied() throws Exception {
        // `subject` IS supplied by the context, but discovery never registers it because it
        // appears only inside a function argument, so it is never seeded. Pre-existing defect;
        // this test documents it so a future fix has something to flip.
        final AnonymizationResult result = apply("SUBJ01", "(0008,103e) := format[\"x{0}y\", subject]");
        assertEquals("known defect: supplying subject does not help when it is only used"
                     + " inside a function argument", AnonymizationResultSeverity.ERROR, result.getSeverity());
        assertTrue("expected the unknown-variable message, but was: " + result.getMessage(),
                result.getMessage().contains("Unknown variable: 'subject'"));
    }
}
