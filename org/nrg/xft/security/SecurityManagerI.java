//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Dec 5, 2005
 *
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
