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
import org.w3c.dom.NamedNodeMap;

/**
	 * @deprecated
 * This class is used to defined the customized properties needed for Torque schema generation.
 * 
 * <BR><BR>Properties for this element are defined within a physical schema file as a sub element of 
 * the &#60;xft:schema&#62; called &#60;xft:torqueSchema&#62;.  The properties match one-to-one to the attributes of that element.
 * If torque is not being used, then this class will not be used.
 * @author Tim
 */
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
	 * @return
	 */
	public String getBaseClass() {
		return baseClass;
	}

	/**
	 * @deprecated
	 * @return
	 */
	public String getBasePeer() {
		return basePeer;
	}

	/**
	 * @deprecated
	 * @return
	 */
	public String getDefaultIdMethod() {
		return defaultIdMethod;
	}

	/**
	 * @deprecated
	 * @return
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

