//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Nov 18, 2004
 */
package org.nrg.xft.references;

import org.nrg.xft.schema.XMLType;
import org.nrg.xft.schema.Wrappers.GenericWrapper.*;

/**
 * Specification of a column in a XFTManyToManyReference and its reference to a foreign
 * element.
 * 
 * @author Tim
 *
 */
public class XFTMappingColumn {
	//0:sql_name,1:xml_type,2:foreign_table,3:foreign_key_sql_name,4:foreignKey(GenericWrapperField)
	private String mapping_sql_name = "";
	private XMLType xml_type = null;
	private GenericWrapperElement foreignElement = null;
	private GenericWrapperField foreignKey = null;
	
	
	/**
	 * @return
	 */
	public GenericWrapperElement getForeignElement() {
		return foreignElement;
	}

	/**
	 * @return
	 */
	public GenericWrapperField getForeignKey() {
		return foreignKey;
	}

	/**
	 * @return
	 */
	public String getLocalSqlName() {
		return mapping_sql_name;
	}

	/**
	 * @return
	 */
	public XMLType getXmlType() {
		return xml_type;
	}

	/**
	 * @param element
	 */
	public void setForeignElement(GenericWrapperElement element) {
		foreignElement = element;
	}

	/**
	 * @param field
	 */
	public void setForeignKey(GenericWrapperField field) {
		foreignKey = field;
	}

	/**
	 * @param string
	 */
	public void setLocalSqlName(String string) {
		mapping_sql_name = string;
	}

	/**
	 * @param type
	 */
	public void setXmlType(XMLType type) {
		xml_type = type;
	}

}

