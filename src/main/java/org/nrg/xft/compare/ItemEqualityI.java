/**
 * Copyright 2010 Washington University
 */
package org.nrg.xft.compare;

import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;

/**
 * @author timo
 *
 */
public interface ItemEqualityI {
	public boolean isEqualTo(final XFTItem newI, final XFTItem oldI) throws XFTInitException, ElementNotFoundException, FieldNotFoundException,Exception;
}
