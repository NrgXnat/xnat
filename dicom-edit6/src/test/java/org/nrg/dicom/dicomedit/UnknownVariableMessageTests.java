/*
 * DicomEdit: UnknownVariableMessageTests
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
import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for the "Unknown variable" message formatter introduced in Phase 4.
 */
public class UnknownVariableMessageTests {

    @Test
    public void messageNamesTheUnknownIdentifier() {
        final String msg = DicomEditParseTreeVisitor.buildUnknownVariableMessage("foo", Collections.emptyList());
        assertTrue(msg, msg.contains("'foo'"));
    }

    @Test
    public void messageReportsNoneDefinedWhenScopeIsEmpty() {
        final String msg = DicomEditParseTreeVisitor.buildUnknownVariableMessage("foo", Collections.emptyList());
        assertTrue(msg, msg.contains("No variables are defined"));
    }

    @Test
    public void messageListsAvailableVariables() {
        final String msg = DicomEditParseTreeVisitor.buildUnknownVariableMessage(
                "foo", new HashSet<>(Arrays.asList("bar", "baz")));
        assertTrue(msg, msg.contains("Defined variables:"));
        assertTrue(msg, msg.contains("bar"));
        assertTrue(msg, msg.contains("baz"));
    }

    @Test
    public void messageSuggestsCloseMatch() {
        final String msg = DicomEditParseTreeVisitor.buildUnknownVariableMessage(
                "foo", new HashSet<>(Arrays.asList("foe", "qux")));
        assertTrue(msg, msg.contains("did you mean 'foe'"));
    }

    @Test
    public void messageOmitsSuggestionWhenNoCloseMatch() {
        final String msg = DicomEditParseTreeVisitor.buildUnknownVariableMessage(
                "foo", new HashSet<>(Arrays.asList("totallyUnrelated", "somethingElse")));
        assertFalse(msg, msg.contains("did you mean"));
    }

    @Test
    public void variableListIsSortedForStableOutput() {
        // Use a target that's far from every candidate so no "did you mean" suggestion fires
        // and skews the ordering of name occurrences in the message.
        final String msg = DicomEditParseTreeVisitor.buildUnknownVariableMessage(
                "qqqqqq", new HashSet<>(Arrays.asList("zeta", "alpha", "mu")));
        // Restrict the search to the "Defined variables: [...]" section so any incidental
        // mention of names elsewhere (e.g. in a suggestion) doesn't affect the order check.
        final int listStart = msg.indexOf("Defined variables:");
        assertTrue("expected 'Defined variables:' section in: " + msg, listStart >= 0);
        final String section = msg.substring(listStart);
        final int idxA = section.indexOf("alpha");
        final int idxM = section.indexOf("mu");
        final int idxZ = section.indexOf("zeta");
        assertTrue("ordering in " + section, idxA >= 0 && idxA < idxM && idxM < idxZ);
    }
}
