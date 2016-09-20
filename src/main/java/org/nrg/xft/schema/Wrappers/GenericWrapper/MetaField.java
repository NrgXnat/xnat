/*
 * core: org.nrg.xft.schema.Wrappers.GenericWrapper.MetaField
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema.Wrappers.GenericWrapper;

import org.nrg.xft.identifier.Identifier;

/**
 * @author Tim
 *
 */
public class MetaField implements Identifier {
	String xmlPathName = "";
	String sqlName = "";
	String localType = "";
	boolean isReferenceIdField= false;
	GenericWrapperField field = null;
	
	/**
	 * @return Returns the field.
	 */
	public GenericWrapperField getField() {
		return field;
	}
	/**
	 * @param field The field to set.
	 */
	public void setField(GenericWrapperField field) {
		this.field = field;
	}
	
	public String getId()
	{
		return getXmlPathName();
	}
	
	/**
	 * @return Returns the xmlPathName.
	 */
	public String getXmlPathName() {
		return xmlPathName;
	}
	/**
	 * @param xmlPathName The xmlPathName to set.
	 */
	public void setXmlPathName(String xmlPathName) {
		this.xmlPathName = xmlPathName;
	}
	/**
	 * @return Returns the isReferenceIdField.
	 */
	public boolean isReferenceIdField() {
		return isReferenceIdField;
	}
	/**
	 * @param isReferenceIdField The isReferenceIdField to set.
	 */
	public void setReferenceIdField(boolean isReferenceIdField) {
		this.isReferenceIdField = isReferenceIdField;
	}
	/**
	 * @return Returns the localType.
	 */
	public String getLocalType() {
		return localType;
	}
	/**
	 * @param localType The localType to set.
	 */
	public void setLocalType(String localType) {
		this.localType = localType;
	}
	/**
	 * @return Returns the sqlName.
	 */
	public String getSqlName() {
		return sqlName;
	}
	/**
	 * @param sqlName The sqlName to set.
	 */
	public void setSqlName(String sqlName) {
		this.sqlName = sqlName;
	}
}

