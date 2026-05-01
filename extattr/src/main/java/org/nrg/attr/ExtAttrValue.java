/*
 * ExtAttr: org.nrg.attr.ExtAttrValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.util.Map;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public interface ExtAttrValue {
    /**
     * Gets the name-to-value mapping for all attribute values on this value.
     *
     * @return The name-to-value map
     */
    Map<String, String> getAttrs();

    /**
     * Gets the name associated with this value.
     *
     * @return name
     */
    String getName();

    /**
     * Gets the text value.
     *
     * @return value
     */
    String getText();
}
