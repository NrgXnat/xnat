//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 17, 2004
 */
package org.nrg.xft.schema;

import org.nrg.xft.utils.NodeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
/**
 * This class contains additional details about an XFTElement that relate directly 
 * to the element's DB use.
 * 
 * @author Tim
 */
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
	 * @return
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
	 * @return
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

