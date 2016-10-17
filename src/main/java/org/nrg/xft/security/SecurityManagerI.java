/*
 * core: org.nrg.xft.security.SecurityManagerI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
     * @return Returns a list of the fullXMLNames of secured elements
     */
    public ArrayList getSecurityElements();
    public boolean isSecurityElement(String elementName);
}
