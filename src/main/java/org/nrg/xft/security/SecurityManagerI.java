/*
 * org.nrg.xft.security.SecurityManagerI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.security;

import java.util.ArrayList;

/**
 * @author Tim
 *
 */
public interface SecurityManagerI {
    /**
     * ArrayList of Strings (fullXMLNames of secured elements i.e. xnat:investigatorData)
     * @return
     */
    public ArrayList getSecurityElements();
    public boolean isSecurityElement(String elementName);
}
