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
import org.w3c.dom.Node;

/**
 * This class contains additional details about an XFTElement that relate directly 
 * to the element's use in an external webapp.
 * 
 * @author Tim
 */
public class XFTWebAppElement {
	private String javaName = "";
	private String idMethod = "native";
	/**
	 * @return
	 */
	public String getJavaName() {
		return javaName;
	}

	/**
	 * @param string
	 */
	public void setJavaName(String string) {
		javaName = string;
	}

	/**
	 * @return
	 */
	public String getIdMethod() {
		return idMethod;
	}

	/**
	 * @param string
	 */
	public void setIdMethod(String string) {
		idMethod = string;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append("XDATWebAppElement\n");
		sb.append("javaName:").append(this.getJavaName()).append("\n");

		return sb.toString();
	}

	/**
	 * @param header
	 * @return
	 */
	public String toString(String header)
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append(header +"XDATWebAppElement\n");
		sb.append(header +"javaName:").append(this.getJavaName()).append("\n");

		return sb.toString();
	}

	public Node toXML(Document doc)
	{
		Node main = doc.createElement("webapp-element");
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"java-name",javaName));
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"id-method",idMethod));
		return main;
	}
}

