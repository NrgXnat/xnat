//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Feb 2, 2005
 *
 */
package org.nrg.xft.security;

import org.nrg.xft.ItemI;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.CriteriaCollection;

/**
 * @author Tim
 *
 */
public interface UserI {
	public Integer getID();
	public String getUsername();
	public ItemI secureItem(ItemI item) throws IllegalAccessException,org.nrg.xft.exception.MetaDataException;
	public boolean canRead(ItemI item) throws InvalidItemException,Exception;
	public boolean canEdit(ItemI item) throws InvalidItemException,Exception;
	public boolean canCreate(ItemI item) throws InvalidItemException,Exception;
	public boolean canActivate(ItemI item) throws InvalidItemException,Exception;
	public boolean canDelete(ItemI item) throws InvalidItemException,Exception;
	public String canStoreItem(ItemI item,boolean descend) throws InvalidItemException,Exception;
	public CriteriaCollection getCriteriaForBackendRead(SchemaElementI rootElement) throws Exception;
	
	/**
	 * @return
	 */
	public String getFirstname();

	/**
	 * @return
	 */
	public String getLastname();

	/**
	 * @return
	 */
	public String getEmail();
}

