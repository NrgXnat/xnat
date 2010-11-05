/**
 * Copyright (c) 2009,2010 Washington University
 */
package org.nrg.attr;

import java.util.Map;
import java.util.Set;

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
    
    @Deprecated
    Set<String> getAttrNames();
    @Deprecated
    String getAttrValue(String name);
    
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
