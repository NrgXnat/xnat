/*
 * core: org.nrg.xft.schema.XFTWebAppField
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema;

import org.nrg.xft.utils.NodeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class XFTWebAppField {
	private String javaName = "";
	public abstract String toString(String header);
	/**
	 * @return Returns the field's java name String
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

	public Node toXML(Document doc)
	{
		Node main = doc.createElement("webapp-field");
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"java-name",javaName));
		return main;
	}
	
	public abstract XFTWebAppField clone(XFTElement e);
}

