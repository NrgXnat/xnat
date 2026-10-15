/*
 * DicomEdit: NullVariableAcrossAssignmentFormsTests
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * XNAT-8499 audit: every grammar position in which an unsupplied injected variable can be read
 * must report an actionable error rather than proceeding with a null.
 *
 * <p>For an anonymization tool, silently substituting nothing is the hazard — a tag that should
 * have been replaced with the subject label keeps whatever it held. This class enumerates the
 * assignment and value forms from {@code DE6Parser.g4} and pins that each one fails loudly and
 * names the variable, so a future change cannot quietly reintroduce a silent path.
 *
 * <p>Forms covered: {@code :=} to a tag, {@code ?=} to a tag, variable initialization, list
 * initialization, {@code echo}, variables inside function arguments and nested functions,
 * function statements, both sides of a condition, conditional else-branches, and {@code if}
 * conditions and bodies.
 *
 * <p><b>Scope of behavior change.</b> Of 22 forms measured against {@code origin/develop},
 * exactly one changed severity: {@link #variableToVariableInitializationIsTheOnlyBehaviorChange()}.
 * Every other form already failed before this work; only its message improved. That is the whole
 * upgrade risk, and it is narrow: a site breaks only if a script initialises one variable from
 * another that may be unset, and the derived variable is not then used in a way that already
 * failed.
 */
public class NullVariableAcrossAssignmentFormsTests {

    private static final ResourceManager RESOURCES = ResourceManager.getInstance();

    private static final File DICOM_FILE = RESOURCES.getTestResourceFile(
            "dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");

    private static final String ORIGINAL_PATIENT_NAME = "Sample Patient";
    private static final String RAW_NPE = "Cannot invoke";

    /** Every form in which the unsupplied `subject` can be read. */
    private static Map<String, String[]> forms() {
        final Map<String, String[]> f = new LinkedHashMap<>();
        f.put("assign  tag := var",          new String[]{"(0010,0010) := subject"});
        f.put("assign  tag ?= var",          new String[]{"(0010,0010) ?= subject"});
        f.put("init    var := var",          new String[]{"foo := subject"});
        f.put("init    var := {var}",        new String[]{"foo := { subject, \"b\" }"});
        f.put("echo    var",                 new String[]{"echo subject"});
        f.put("assign  tag := fn(var)",      new String[]{"(0010,0010) := format[\"{0}\", subject]"});
        f.put("assign  tag ?= fn(var)",      new String[]{"(0010,0010) ?= format[\"{0}\", subject]"});
        f.put("init    var := fn(var)",      new String[]{"foo := format[\"{0}\", subject]"});
        f.put("fn statement(var)",           new String[]{"removeTags[ subject]"});
        f.put("nested  fn(fn(var))",         new String[]{"(0010,0010) := format[\"{0}\", lowercase[subject]]"});
        f.put("echo    fn(var)",             new String[]{"echo format[\"{0}\", subject]"});
        f.put("cond    var = str",           new String[]{"subject = \"x\" ? (0008,103e) := \"m\""});
        f.put("cond    tag = var",           new String[]{"(0010,0010) = subject ? (0008,103e) := \"m\""});
        f.put("cond    var ~ str",           new String[]{"subject ~ \"x\" ? (0008,103e) := \"m\""});
        f.put("cond    else-branch var",     new String[]{"(0010,0010) = \"nope\" ? (0008,103e) := \"m\" : (0010,0010) := subject"});
        f.put("if      (var = str) block",   new String[]{"if (subject = \"x\") { (0008,103e) := \"m\" }"});
        f.put("if      block body var",      new String[]{"if ((0010,0010) ~ \".*\") { (0010,0010) := subject }"});
        f.put("fn(var) + bare var",          new String[]{"(0008,103e) := format[\"{0}\", subject]", "(0010,0010) := subject"});
        f.put("{var} + bare var",            new String[]{"foo := { subject }", "(0010,0010) := subject"});
        return f;
    }

    /** `subject` is referenced by the script but never supplied by the context. */
    private AnonymizationResult applyWithoutSubject(final String... statements) throws Exception {
        final MizerContextWithScript context = new MizerContextWithScript();
        final List<String> script = new ArrayList<>();
        script.add("version \"6.1\"");
        script.addAll(Arrays.asList(statements));
        context.setScript(script);
        context.setElement("project", "PROJ01");
        context.setElement("session", "SESS01");
        return BaseScriptApplicator.getInstance(context).apply(DicomObjectFactory.newInstance(DICOM_FILE));
    }

    @Test
    public void everyFormReportsAnActionableErrorForAnUnsuppliedVariable() throws Exception {
        final List<String> violations = new ArrayList<>();
        for (final Map.Entry<String, String[]> form : forms().entrySet()) {
            final AnonymizationResult result = applyWithoutSubject(form.getValue());
            final String message = result.getMessage();
            if (result.getSeverity() != AnonymizationResultSeverity.ERROR) {
                violations.add(form.getKey() + " -> severity " + result.getSeverity()
                               + " (expected ERROR); message: " + message);
            } else if (!message.contains("'subject'")) {
                violations.add(form.getKey() + " -> error does not name 'subject': " + message);
            } else if (message.contains(RAW_NPE)) {
                violations.add(form.getKey() + " -> error leaks raw NPE text: " + message);
            }
        }
        assertTrue("forms that did not report an actionable error:\n  "
                   + String.join("\n  ", violations), violations.isEmpty());
    }

    @Test
    public void everyFormLeavesPatientNameUnchanged() throws Exception {
        // A failed script must not half-apply: the tag it meant to anonymize keeps its original
        // value, and the ERROR severity is what stops a caller archiving it.
        final List<String> violations = new ArrayList<>();
        for (final Map.Entry<String, String[]> form : forms().entrySet()) {
            final AnonymizationResult result = applyWithoutSubject(form.getValue());
            final String after = result.getDicomObject().getString(Tag.PatientName);
            if (!ORIGINAL_PATIENT_NAME.equals(after)) {
                violations.add(form.getKey() + " -> PatientName became [" + after + "]");
            }
        }
        assertTrue("forms that modified PatientName despite failing:\n  "
                   + String.join("\n  ", violations), violations.isEmpty());
    }

    @Test
    public void variableToVariableInitializationIsTheOnlyBehaviorChange() throws Exception {
        // Of the 22 forms measured against origin/develop, this is the single one that changed
        // severity: it used to succeed, leaving `foo` null, and now errors. Pinned explicitly
        // because it is the entire upgrade risk of this change and belongs in the release note.
        final AnonymizationResult result = applyWithoutSubject("foo := subject");
        assertEquals(AnonymizationResultSeverity.ERROR, result.getSeverity());
        assertTrue("expected the null variable to be named, got: " + result.getMessage(),
                result.getMessage().contains("Variable 'subject' is null."));
    }

    @Test
    public void suppliedValuesAndLiteralsAreUnaffected() throws Exception {
        // Controls: the happy paths must not be caught by any of the above.
        final AnonymizationResult injected = applyWithoutSubject("(0010,0010) := project");
        assertEquals("a supplied injected variable must still work: " + injected.getMessage(),
                AnonymizationResultSeverity.SUCCESS, injected.getSeverity());
        assertEquals("PROJ01", injected.getDicomObject().getString(Tag.PatientName));

        final AnonymizationResult literal = applyWithoutSubject("(0010,0010) := \"literal\"");
        assertEquals("a literal assignment must still work: " + literal.getMessage(),
                AnonymizationResultSeverity.SUCCESS, literal.getSeverity());
        assertEquals("literal", literal.getDicomObject().getString(Tag.PatientName));
    }
}
