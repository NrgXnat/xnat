/*
 * core: org.nrg.xft.schema.XFTSqlElement
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema;

import org.nrg.xft.utils.NodeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
public class XFTSqlElement {
	private String name = "";

	public XFTSqlElement()
	{}
	
	/**
	 * Constructs the XFTSqlElement using the NamedNodeMap's name attribute as the sql name.
	 * @param nnm
	 */
	public XFTSqlElement(NamedNodeMap nnm)
	{
		name = NodeUtils.GetAttributeValue(nnm,"name","");
	}
	
	/**
	 * sql name
	 * @return Returns this element's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * sql name
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append("XDATSqlElement\n");
		sb.append("name:").append(this.getName()).append("\n");
		
		return sb.toString();
	}

	/**
	 * @param header
	 * @return Returns a String representation of this element
	 */
	public String toString(String header)
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append(header).append("XDATSqlElement\n");
		sb.append(header).append("name:").append(this.getName()).append("\n");
		
		return sb.toString();
	}

	public Node toXML(Document doc)
	{
		Node main = doc.createElement("sql-element");
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"sql-name",name));
		return main;
	}
}

