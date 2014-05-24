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
 * This class contains additional details about an XFTField that relate directly 
 * to the field's use in an external webapp.
 * 
 * @author Tim
 */
public abstract class XFTWebAppField {
	private String javaName = "";
	public abstract String toString(String header);
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

	public Node toXML(Document doc)
	{
		Node main = doc.createElement("webapp-field");
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"java-name",javaName));
		return main;
	}
	
	public abstract XFTWebAppField clone(XFTElement e);
}

