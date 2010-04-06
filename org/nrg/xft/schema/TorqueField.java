//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
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
 * This class is used to defined the customized properties needed for Torque schema generation.
 * 
 * <BR><BR>Properties for this field are defined within a physical schema file as a sub element of 
 * the &#60;xft:field&#62; called &#60;xft:torqueField&#62;.  The properties match one-to-one to the attributes of that element.
 * If torque is not being used, then this class will not be used.
 * @author Tim
 */
public class TorqueField extends XFTWebAppField {
	private String defaultValue = "";
	private String inheritance = "";
	private String inheritanceKey = "";
	private String inheritanceClass = "";
	private String inheritanceExtends = "";
	
	public TorqueField()
	{
	}
	
	/**
	 * Populates constructed object from the attributes of the given Node
	 * @param node
	 */
	public TorqueField(Node node)
	{
		defaultValue = NodeUtils.GetAttributeValue(node,"default","");
		inheritance = NodeUtils.GetAttributeValue(node,"inheritance","");
		this.setJavaName(NodeUtils.GetAttributeValue(node,"javaName",""));
	}
	
	public XFTWebAppField clone(XFTElement e)
	{
		TorqueField clone = new TorqueField();
		clone.setJavaName(getJavaName());
		clone.setDefaultValue(getDefaultValue());
		clone.setInheritance(getInheritance());
		return clone;
	}
	
	/**
	 * @return
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return
	 */
	public String getInheritance() {
		return inheritance;
	}

	/**
	 * @return
	 */
	public String getInheritanceClass() {
		return inheritanceClass;
	}

	/**
	 * @return
	 */
	public String getInheritanceExtends() {
		return inheritanceExtends;
	}

	/**
	 * @return
	 */
	public String getInheritanceKey() {
		return inheritanceKey;
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
	public void setInheritance(String string) {
		inheritance = string;
	}

	/**
	 * @param string
	 */
	public void setInheritanceClass(String string) {
		inheritanceClass = string;
	}

	/**
	 * @param string
	 */
	public void setInheritanceExtends(String string) {
		inheritanceExtends = string;
	}

	/**
	 * @param string
	 */
	public void setInheritanceKey(String string) {
		inheritanceKey = string;
	}



	/**
	 */
	public String toString()
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append("TorqueField\n");
		sb.append("defaultValue:").append(this.getDefaultValue()).append("\n");
		sb.append("inheritance:").append(this.getInheritance()).append("\n");
		sb.append("inheritanceClass:").append(this.getInheritanceClass()).append("\n");
		sb.append("inheritanceExtends:").append(this.getInheritanceExtends()).append("\n");
		sb.append("inheritanceKey:").append(this.getInheritanceKey()).append("\n");
		sb.append("javaName:").append(this.getJavaName()).append("\n");
		return sb.toString();
	}


	/**
	 * @param header
	 */
	public String toString(String header)
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append(header).append("TorqueField\n");
		if(getDefaultValue() != "")
			sb.append(header).append("defaultValue:").append(this.getDefaultValue()).append("\n");
		if(getInheritance() != "")
			sb.append(header).append("inheritance:").append(this.getInheritance()).append("\n");
		if(getInheritanceClass() != "")
			sb.append(header).append("inheritanceClass:").append(this.getInheritanceClass()).append("\n");
		if(getInheritanceExtends() != "")
			sb.append(header).append("inheritanceExtends:").append(this.getInheritanceExtends()).append("\n");
		if(getInheritanceKey() != "")
			sb.append(header).append("inheritanceKey:").append(this.getInheritanceKey()).append("\n");
		if(getJavaName() != "")
			sb.append(header).append("javaName:").append(this.getJavaName()).append("\n");
		return sb.toString();
	}


	public Node toXML(Document doc)
	{
		Node main = doc.createElement("webapp-field");
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"java-name",getJavaName()));
		if(getDefaultValue() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"defaultValue",this.getDefaultValue()));
		if(getInheritance() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"inheritance",this.getInheritance()));
		if(getInheritanceClass() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"inheritanceClass",this.getInheritanceClass()));
		if(getInheritanceExtends() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"inheritanceExtends",this.getInheritanceExtends()));
		if(getInheritanceKey() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"inheritanceKey",this.getInheritanceKey()));
		
		return main;
	}
}

