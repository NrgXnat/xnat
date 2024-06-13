/*
 * prefs: org.nrg.prefs.transformers.CheckItemTypeException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.transformers;

import org.nrg.prefs.entities.PreferenceInfo;

/**
 * Indicates that a value type submitted to the {@link PreferenceTransformer#handles(Class)} method is a container of
 * items (e.g. a list or map) and the type of the target can't be determined. To handle this exception, try calling
 * the <b>handles()</b> method again with the {@link PreferenceInfo#getItemType()} value.
 */
public class CheckItemTypeException extends Exception {
}
