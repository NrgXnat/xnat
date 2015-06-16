/*
 * org.nrg.xft.security.UserI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 12:00 PM
 */


package org.nrg.xft.security;

import org.nrg.xft.ItemI;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.CriteriaCollection;

/**
 * @author Tim
 *
 */
public interface UserI {
	Integer getID();
	String getUsername();
    String getFirstname();
    String getLastname();
    String getEmail();
	ItemI secureItem(ItemI item) throws IllegalAccessException,org.nrg.xft.exception.MetaDataException;
	boolean canRead(ItemI item) throws Exception;
	boolean canEdit(ItemI item) throws Exception;
	boolean canCreate(ItemI item) throws Exception;
	boolean canActivate(ItemI item) throws Exception;
	boolean canDelete(ItemI item) throws Exception;
	boolean can(ItemI item,String action) throws Exception;
	String canStoreItem(ItemI item,boolean descend) throws Exception;
	CriteriaCollection getCriteriaForBackendRead(SchemaElementI rootElement) throws Exception;
    boolean isGuest();
}

