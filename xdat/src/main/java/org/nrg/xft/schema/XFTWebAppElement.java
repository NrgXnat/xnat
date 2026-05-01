/*
 * core: org.nrg.xft.schema.XFTWebAppElement
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema;

import org.nrg.xft.utils.NodeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XFTWebAppElement {
	private String javaName = "";
	private String idMethod = "native";
	/**
	 * @return Returns the java name as a String
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
	 * @return Returns the method ID as a String
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
	 * @return Returns the element expressed as a String
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

