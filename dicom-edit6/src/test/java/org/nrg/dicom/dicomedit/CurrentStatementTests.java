/*
 * DicomEdit: CurrentStatementTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.dicomedit;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * The statement {@link DicomEditParseTreeVisitor} reports as current, which the applicator puts in its error message.
 * The applicator reuses one visitor for every object it is applied to, so the statement has to belong to the visit in
 * progress: a failure before the first statement of a later visit must not be reported at the last statement of an
 * earlier one.
 */
public class CurrentStatementTests {
    @Test
    public void theCurrentStatementIsTheLastOneEntered() throws Exception {
        final DicomEditParseTreeVisitor visitor = new DicomEditParseTreeVisitor();
        visitor.visit(new DE6ScriptParser().parse(Arrays.asList("first := \"a\"", "second := \"b\"")));

        assertEquals(Integer.valueOf(2), visitor.getCurrentStatementLine());
        assertEquals("second:=\"b\"", visitor.getCurrentStatementText());
    }

    @Test
    public void aNewVisitForgetsTheLastVisitsStatement() throws Exception {
        final DicomEditParseTreeVisitor visitor = new DicomEditParseTreeVisitor();
        visitor.visit(new DE6ScriptParser().parse(Arrays.asList("first := \"a\"", "second := \"b\"")));
        visitor.visit(new DE6ScriptParser().parse(Collections.emptyList()));

        assertNull(visitor.getCurrentStatementLine());
        assertNull(visitor.getCurrentStatementText());
    }
}
