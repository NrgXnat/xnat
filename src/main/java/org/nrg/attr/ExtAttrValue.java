/**
 * Copyright (c) 2009,2011 Washington University
 */
package org.nrg.attr;

import java.util.Map;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public interface ExtAttrValue {
    /**
     * Gets the name->value mapping for all attribute values on this value.
     * @return name->value map
     */
    Map<String,String> getAttrs();

    /**
     * Gets the name associated with this value.
     * @return name
     */
    String getName();

    /**
     * Gets the text value.
     * @return value
     */
    String getText();
}
