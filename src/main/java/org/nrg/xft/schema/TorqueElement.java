/*
 * core: org.nrg.xft.schema.TorqueElement
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
public class TorqueElement extends XFTWebAppElement {
	private String javaNamingMethod = "";
	private String baseClass = "";
	private String basePeer = "";
	private String alias = "";
	private boolean isAbstract = false;
	
	
	/**
	 * Please Use XFTWebAppElement
	 */
	public TorqueElement()
	{
	}
	
	/**
	 * Populates the values for the constructed object from the attributes of the NamedNodeMap.
	 * 'native' is the default for idMethod, otherwise the rest default to an empty string.
	 * @param nnm
	 */
	public TorqueElement(NamedNodeMap nnm)
	{
		this.setJavaName(NodeUtils.GetAttributeValue(nnm,"javaName",""));
		this.setIdMethod(NodeUtils.GetAttributeValue(nnm,"idMethod","native"));
		javaNamingMethod = NodeUtils.GetAttributeValue(nnm,"javaNamingMethod","");
		baseClass = NodeUtils.GetAttributeValue(nnm,"baseClass","");
		basePeer = NodeUtils.GetAttributeValue(nnm,"basePeer","");
		alias = NodeUtils.GetAttributeValue(nnm,"alias","");
	}
	/**
	 * alias for Torque Schema
	 * @return Returns alias String
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * baseClass for Torque Schema
	 * @return Returns base class String
	 */
	public String getBaseClass() {
		return baseClass;
	}

	/**
	 * basePeer for Torque Schema
	 * @return Returns base peer String
	 */
	public String getBasePeer() {
		return basePeer;
	}

	/**
	 * @deprecated
	 * isAbstract for Torque Schema
	 * @return Returns whether the element is abstract
	 */
	public boolean isAbstract() {
		return isAbstract;
	}

	/**
	 * @deprecated
	 * javaNamingMethod for Torque Schema
	 * @return Returns the java naming method String
	 */
	public String getJavaNamingMethod() {
		return javaNamingMethod;
	}


	/**
	 * @deprecated
	 * @param string
	 */
	public void setAlias(String string) {
		alias = string;
	}

	/**
	 * @deprecated
	 * @param string
	 */
	public void setBaseClass(String string) {
		baseClass = string;
	}

	/**
	 * @deprecated
	 * @param string
	 */
	public void setBasePeer(String string) {
		basePeer = string;
	}

	/**
	 * @deprecated
	 * @param b
	 */
	public void setAbstract(boolean b) {
		isAbstract = b;
	}
	/**
	 * @deprecated
	 * @param s
	 */
	public void setAbstract(String s ){
		if (s.equalsIgnoreCase("true"))
		{
			isAbstract = true;
		}else
		{
			isAbstract = false;
		}
	}

	/**
	 * @deprecated
	 * @param string
	 */
	public void setJavaNamingMethod(String string) {
		javaNamingMethod = string;
	}

	


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * @deprecated
	 */
	public String toString()
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append("TorqueElement\n");
		sb.append("alias:").append(this.getAlias()).append("\n");
		sb.append("baseClass:").append(this.getBaseClass()).append("\n");
		sb.append("basePeer:").append(this.getBasePeer()).append("\n");
		sb.append("idMethod:").append(this.getIdMethod()).append("\n");
		sb.append("javaName:").append(this.getJavaName()).append("\n");
		sb.append("javaNamingMethod:").append(this.getJavaNamingMethod()).append("\n");
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.schema.XFTWebAppElement#toString(java.lang.String)
	 */
	/**
	 * @deprecated
	 * @param header
	 */
	public String toString(String header)
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append(header).append("TorqueElement\n");
		if(getAlias() != "")
			sb.append(header).append("alias:").append(this.getAlias()).append("\n");
		if(getBaseClass() != "")
			sb.append(header).append("baseClass:").append(this.getBaseClass()).append("\n");
		if(getBasePeer() != "")
			sb.append(header).append("basePeer:").append(this.getBasePeer()).append("\n");
		if(getIdMethod() != "")
			sb.append(header).append("idMethod:").append(this.getIdMethod()).append("\n");
		if(getJavaName() != "")
			sb.append(header).append("javaName:").append(this.getJavaName()).append("\n");
		if(getJavaNamingMethod() != "")
			sb.append(header).append("javaNamingMethod:").append(this.getJavaNamingMethod()).append("\n");
		return sb.toString();
	}

	public Node toXML(Document doc)
	{
		Node main = doc.createElement("webapp-element");
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"java-name",this.getJavaName()));
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"id-method",this.getIdMethod()));
		if(getAlias() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"alias",this.getAlias()));
		if(getBaseClass() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"baseClass",this.getBaseClass()));
		if(getBasePeer() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"basePeer",this.getBasePeer()));
		if(getJavaNamingMethod() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"javaNamingMethod",this.getJavaNamingMethod()));
		return main;
	}
}

