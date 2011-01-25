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
	Map<String,String> getAttrs();
	String getName();
	String getText();
}
