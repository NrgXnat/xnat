/*
 * core: org.nrg.xft.schema.Wrappers.XMLWrapper.XMLFieldData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema.Wrappers.XMLWrapper;
import org.nrg.xft.schema.XMLType;
import org.nrg.xft.schema.design.XFTFieldWrapper;

import java.util.ArrayList;
/**
 * Data field used to summarize information for XML display.
 * 
 * @author Tim
 */
public class XMLFieldData {
	private String sqlName = null;
	private Integer levels = null;
	private boolean attribute = false;
	private boolean reference = false;
	private String xmlFieldName = null;
	private boolean multiple = false;
	private ArrayList layers = null;
	private XMLType xmlType = null;
	private boolean childXMLNode = true;
	private boolean extension = false;
	private boolean required = false;
	private XFTFieldWrapper field = null;
	/**
	 * @return Returns whether the field is an attribute
	 */
	public boolean isAttribute() {
		return attribute;
	}


	/**
	 * number of levels down from the element that the field can be found.
	 * @return Returns the Integer number of levels down from the element that the field can be found
	 */
	public Integer getLevels() {
		return levels;
	}

	/**
	 * @return Returns whether the field is a multiple
	 */
	public boolean isMultiple() {
		return multiple;
	}

	/**
	 * @return Returns whether the field is a reference
	 */
	public boolean isReference() {
		return reference;
	}

	/**
	 * @return Returns the XML field name (with optional prefix)
	 */
	public String getXmlFieldName(String prefix) {
	    if (prefix==null)
	    {
			return xmlFieldName;
	    }else{
	        return prefix+":" + xmlFieldName;
	    }
	}

	/**
	 * @return Returns XMLType
	 */
	public XMLType getXmlType() {
		return xmlType;
	}

	/**
	 * @param b
	 */
	public void setAttribute(boolean b) {
		attribute = b;
	}


	/**
	 * @param integer
	 */
	public void setLevels(Integer integer) {
		levels = integer;
	}

	/**
	 * @param b
	 */
	public void setMultiple(boolean b) {
		multiple = b;
	}

	/**
	 * @param b
	 */
	public void setReference(boolean b) {
		reference = b;
	}

	/**
	 * @param string
	 */
	public void setXmlFieldName(String string) {
		xmlFieldName = string;
	}

	/**
	 * @param type
	 */
	public void setXmlType(XMLType type) {
		xmlType = type;
	}

	/**
	 * [0]
	 * @return Returns sql name
	 */
	public String getSqlName() {
		return sqlName;
	}

	/**
	 * @param string
	 */
	public void setSqlName(String string) {
		sqlName = string;
	}

	/**
	 * Names of nodes which a reader will have to pass through to get the child node.
	 * @return Returns ArrayList of names of nodes which a reader will have to pass through to get the child node
	 */
	public ArrayList getLayers() {
		return layers;
	}

	/**
	 * Names of nodes which a reader will have to pass through to get the child node.
	 * @param list
	 */
	public void setLayers(ArrayList list) {
		layers = list;
	}

	/**
	 * @return Returns whether the field is a child XML node
	 */
	public boolean isChildXMLNode() {
		return childXMLNode;
	}

	/**
	 * @param b
	 */
	public void setChildXMLNode(boolean b) {
		childXMLNode = b;
	}

	/**
	 * @return Returns whether the field is an extension
	 */
	public boolean isExtension() {
		return extension;
	}

	/**
	 * @param b
	 */
	public void setExtension(boolean b) {
		extension = b;
	}

	/**
	 * @return Returns whether the field is required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param b
	 */
	public void setRequired(boolean b) {
		required = b;
	}

	/**
	 * @param b
	 */
	public void setRequired(String b) {
		if (b.equalsIgnoreCase("true"))
		{
			required = true;
		}else{
			required = false;
		}
	}

	/**
	 * @return Returns the field
	 */
	public XFTFieldWrapper getField() {
		return field;
	}

	/**
	 * @param wrapper
	 */
	public void setField(XFTFieldWrapper wrapper) {
		field = wrapper;
	}

	public String toString()
	{
		return "XML:'" + this.getXmlFieldName(null) + "' ,SQL:'" + this.getSqlName() + "' ,TYPE:'" + this.getXmlType().toString() + "'";
	}
}

