/*
 * org.nrg.xdat.security.SecurityManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.security;

import java.util.ArrayList;

import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xft.security.SecurityManagerI;

/**
 * @author Tim
 */
public class SecurityManager implements SecurityManagerI {
    public static final String          READ                 = "read";
    public static final String          CREATE               = "create";
    public static final String          EDIT                 = "edit";
    public static final String          DELETE               = "delete";
    public static final String          ACTIVATE             = "active";
    private static      SecurityManager instance             = null;
    private             ArrayList       securityElementNames = null;

    private SecurityManager() throws Exception {

    }

    public static SecurityManager GetInstance() throws Exception {
        if (instance == null) {
            Init();
        }
        return instance;
    }

    public static void Init() throws Exception {
        instance = new SecurityManager();
    }

    /**
     * @return ArrayList of XFTItems
     * @throws Exception When something goes wrong.
     */
    @SuppressWarnings("unused")
    public static ArrayList getSecurityRoles() throws Exception {
        return DisplaySearch.SearchForItems(SchemaElement.GetElement(org.nrg.xft.XFT.PREFIX + ":role_type"), null);
    }

    public ArrayList getSecurityElements() {
        if (this.securityElementNames == null) {
            try {
                securityElementNames = ElementSecurity.GetSecurityElements();
            } catch (Exception e) {
                securityElementNames = new ArrayList();
            }
        }

        return securityElementNames;
    }

    public boolean isSecurityElement(String elementName) {
        return getSecurityElements().contains(elementName);
    }
}

