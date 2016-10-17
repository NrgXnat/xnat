/*
 * core: org.nrg.xft.schema.TorqueSchema
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema;

import org.nrg.xft.utils.NodeUtils;
import org.w3c.dom.NamedNodeMap;

public class TorqueSchema extends XFTWebAppSchema {
	private String defaultIdMethod = "";
	private String baseClass = "";
	private String basePeer = "";
	private String javaPackage = "";
	
	/**
	 * @deprecated
	 * Populates the javaPackage from the javaPackage attribute in the NamedNodeMap.  All other
	 * fields are set to an empty string.
	 * @param nnm
	 */
	public TorqueSchema(NamedNodeMap nnm)
	{
		defaultIdMethod = "";//NOT IN XDAT xnat:db_info
		baseClass = "";//NOT IN XDAT xnat:db_info
		basePeer = "";//NOT IN XDAT xnat:db_info
		javaPackage = NodeUtils.GetAttributeValue(nnm,"javaPackage","");
	}
	
	/**
	 * @deprecated
	 * @return Returns the base class String
	 */
	public String getBaseClass() {
		return baseClass;
	}

	/**
	 * @deprecated
	 * @return Returns the base peer String
	 */
	public String getBasePeer() {
		return basePeer;
	}

	/**
	 * @deprecated
	 * @return Returns the default ID method
	 */
	public String getDefaultIdMethod() {
		return defaultIdMethod;
	}

	/**
	 * @deprecated
	 * @return Returns the java package String
	 */
	public String getJavaPackage() {
		return javaPackage;
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
	 * @param string
	 */
	public void setDefaultIdMethod(String string) {
		defaultIdMethod = string;
	}

	/**
	 * @deprecated
	 * @param string
	 */
	public void setJavaPackage(String string) {
		javaPackage = string;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append("TorqueSchema\n");
		sb.append("baseClass:").append(this.getBaseClass()).append("\n");
		sb.append("basePeer:").append(this.getBasePeer()).append("\n");
		sb.append("defaultIdMethod:").append(this.getDefaultIdMethod()).append("\n");
		sb.append("javaPackage:").append(this.getJavaPackage()).append("\n");
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.schema.XFTWebAppSchema#toString(java.lang.String)
	 */
	public String toString(String header)
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append(header).append("TorqueSchema\n");
		if(getBaseClass() != "")
			sb.append(header).append("baseClass:").append(this.getBaseClass()).append("\n");
		if(getBasePeer() != "")
			sb.append(header).append("basePeer:").append(this.getBasePeer()).append("\n");
		if(getDefaultIdMethod() != "")
			sb.append(header).append("defaultIdMethod:").append(this.getDefaultIdMethod()).append("\n");
		if(getJavaPackage() != "")
			sb.append(header).append("javaPackage:").append(this.getJavaPackage()).append("\n");
		return sb.toString();
	}

}

