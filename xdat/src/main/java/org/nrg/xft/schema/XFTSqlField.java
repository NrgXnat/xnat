/*
 * core: org.nrg.xft.schema.XFTSqlField
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema;

import org.nrg.xft.utils.NodeUtils;
import org.nrg.xft.utils.XftStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
public class XFTSqlField {
	private String sqlName = "";
	private String type = "";
	private String key = "";
	private String defaultValue = "";
	private String primaryKey = "";
	private String autoIncrement = "";
	private String index = "";
	
	public XFTSqlField()
	{
	}
	
	/**
	 * Constructs the XFTSqlField based on the attributes of the XML DOM Node.
	 * @param node
	 */
	public XFTSqlField(Node node)
	{
		sqlName = NodeUtils.GetAttributeValue(node,"name","").intern();
		type = NodeUtils.GetAttributeValue(node,"type","").intern();
		key = NodeUtils.GetAttributeValue(node,"key","").intern();
		defaultValue = NodeUtils.GetAttributeValue(node,"default","").intern();
		setPrimaryKey(NodeUtils.GetAttributeValue(node,"primaryKey","").intern());
		setAutoIncrement(NodeUtils.GetAttributeValue(node,"autoIncrement","").intern());
		setIndex(NodeUtils.GetAttributeValue(node,"index","").intern());
	}

	public XFTSqlField clone(XFTElement e)
	{
		XFTSqlField clone = new XFTSqlField();
		clone.setSqlName(this.getSqlName());
		clone.setType(this.getType());
		clone.setKey(this.getKey());
		clone.setDefaultValue(this.getDefaultValue());
		clone.setPrimaryKey(this.getPrimaryKey());
		clone.setAutoIncrement(this.getAutoIncrement());
		clone.setIndex(this.getIndex());
		return clone;
	}
	/**
	 * @return Returns the default value
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return Returns the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param string
	 */
	public void setDefaultValue(String string) {
		defaultValue = string;
	}

	/**
	 * @param string
	 */
	public void setKey(String string) {
		key = string;
	}

	/**
	 * @return Returns the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param string
	 */
	public void setType(String string) {
		type = string;
	}

	/**
	 * @return Returns the sql name
	 */
	public String getSqlName() {
		return sqlName;
	}

	/**
	 * @param string
	 */
	public void setSqlName(String string) {
		sqlName = XftStringUtils.CleanForSQL(string);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append("XDATSqlField\n");
		sb.append("defaultValue:").append(this.getDefaultValue()).append("\n");
		sb.append("key:").append(this.getKey()).append("\n");
		sb.append("sqlName:").append(this.getSqlName()).append("\n");
		sb.append("type:").append(this.getType()).append("\n");
		sb.append("autoIncrement:").append(this.getAutoIncrement()).append("\n");
		sb.append("index:").append(this.getIndex()).append("\n");
		sb.append("primaryKey:").append(this.getPrimaryKey()).append("\n");
		
		return sb.toString();
	}

	/**
	 * @param header
	 * @return Returns a String representation of the field
	 */
	public String toString(String header)
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append(header).append("XDATSqlField\n");
		if (getDefaultValue() != "")
			sb.append(header).append("defaultValue:").append(this.getDefaultValue()).append("\n");
		if (getKey() != "")
			sb.append(header).append("key:").append(this.getKey()).append("\n");
		if (getSqlName() != "")
			sb.append(header).append("sqlName:").append(this.getSqlName()).append("\n");
		if (getType() != "")
			sb.append(header).append("type:").append(this.getType()).append("\n");
		if (getAutoIncrement() != "")
			sb.append(header).append("autoIncrement:").append(this.getAutoIncrement()).append("\n");
		if (getIndex() != "")
			sb.append(header).append("index:").append(this.getIndex()).append("\n");
		if (getPrimaryKey() != "")
			sb.append(header).append("primaryKey:").append(this.getPrimaryKey()).append("\n");
		
		return sb.toString();
	}
	

	
	public Node toXML(Document doc)
	{
		Node main = doc.createElement("sql-field");
		if (getDefaultValue() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"defaultValue",this.getDefaultValue()));
		if (getKey() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"key",this.getKey()));
		if (getSqlName() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"sqlName",this.getSqlName()));
		if (getType() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"type",this.getType()));
		if (getAutoIncrement() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"autoIncrement",this.getAutoIncrement()));
		if (getIndex() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"index",this.getIndex()));
		if (getPrimaryKey() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"primaryKey",this.getPrimaryKey()));
		
		return main;
	}

	/**
	 * @return Returns whether the field should auto increment (expressed as a String)
	 */
	public String getAutoIncrement() {
		return autoIncrement;
	}

	/**
	 * @return Returns index as a String
	 */
	public String getIndex() {
		return index;
	}

	/**
	 * @return Returns primary key
	 */
	public String getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * @param string
	 */
	public void setAutoIncrement(String string) {
		autoIncrement = string;
	}

	/**
	 * @param string
	 */
	public void setIndex(String string) {
		index = string;
	}

	/**
	 * @param string
	 */
	public void setPrimaryKey(String string) {
		primaryKey = string;
	}

}

