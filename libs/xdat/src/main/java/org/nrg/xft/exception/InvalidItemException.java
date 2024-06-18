/*
 * core: org.nrg.xft.exception.InvalidItemException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.exception;

import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;

/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class InvalidItemException extends XftItemException {
    public final ItemI item;
    public InvalidItemException(final ItemI item) {
    	super("Invalid Item: '" + ((XFTItem)item).toXML_String() + "'\n\n Needs appropriate security field(s).");
        this.item = item;
    }
}
