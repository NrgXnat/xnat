/*
 * web: org.nrg.xnat.helpers.xmlpath.XMLPathShortcutsI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.xmlpath;

import java.util.Map;

/**
 * Interface for translating XMLPath shortcuts.
 */
public interface XMLPathShortcutsI {
    /**
     * Identifies the fields in the submitted parameters object according to the submitted type.
     *
     * @param params   The fields to check to see if they match fields on the indicated data type.
     * @param type     The data type to check.
     * @param readOnly Whether read-only fields should be included.
     * @return The parameters that match known fields.
     */
    Map<String, Object> identifyFields(final Map<String, Object> params, final String type, boolean readOnly);

    /**
     * Gets all of the shortcuts for the indicated data type.
     *
     * @param type     The data type.
     * @param readOnly Whether read-only fields should be included.
     * @return The shortcuts for the indicated data type.
     */
    Map<String, String> getShortcuts(final String type, final boolean readOnly);

    /**
     * Gets a map of all of the fields for the indicated type that include an embedded data type. This is helpful when
     * setting attributes that require a specific data type to be specified when they're being created.
     *
     * @param type The data type.
     * @return A map of all of the fields for the indicated type that include an embedded data type.
     */
    Map<String, String> getEmbeddedTypeShortcuts(final String type);
}
