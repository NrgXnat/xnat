//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Aug 12, 2005
 *
 */
package org.nrg.xft.exception;

import org.nrg.xft.XFTItem;

/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class InvalidItemException extends Exception {

    public org.nrg.xft.ItemI item = null;
    public InvalidItemException(org.nrg.xft.ItemI i) {
    	super("Invalid Item: '" + ((XFTItem)i).toXML_String() + "'\n\n Needs appropriate security field(s).");
    	item = i;	
    }

}
