/*
 * core: org.nrg.xft.schema.XFTRule
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
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
public class XFTRule {
	private String baseType = "";
	private String mask = "";
	private String message = "";
	private String maxLength = "";
	private String minLength = "";
	private String length = "";
	private String maxInclusive = "";
	private String minInclusive = "";
	
	private ArrayList possibleValues = new ArrayList();
	
	/**
	 * Populates the properties from the given XML DOM Node.
	 * @param node XML DOM Node
	 * @param prefix XMLNS
	 * @param s parent schema
	 */
	public XFTRule(Node node, String prefix,XFTSchema s)
	{
		Node rule = NodeUtils.GetLevel4Child(node,"xdat:rule");
		if (rule != null)
		{
			mask = NodeUtils.GetAttributeValue(rule,"value","");
			message = NodeUtils.GetAttributeValue(rule,"message","");
		}
		
		Node restrict = NodeUtils.GetLevel2Child(node,prefix+":restriction");
		if (restrict == null)
		{
			restrict = NodeUtils.GetLevel1Child(node,prefix+":restriction");
		}
		
			
		Node extension = NodeUtils.GetLevel3Child(node,prefix+":extension");
		if (extension != null)
		{
			baseType = NodeUtils.GetAttributeValue(extension,"base","");
		}
		
		if (restrict != null)
		{
	        setRestrictionProps(restrict,prefix);
		}
		
		if(rule!=null)
		{
		    restrict = NodeUtils.GetLevel1Child(rule,prefix+":restriction");
		    
		    if (restrict != null)
			{
		        setRestrictionProps(restrict,prefix);
			}
		}
		
		if (restrict != null)
		{
			String temp = NodeUtils.GetAttributeValue(restrict.getAttributes(),"base",prefix+":string");
			Node refType=NodeWrapper.FindNode(temp,s);
			if (refType!=null)
			{
				baseType = prefix+":string";
				length="255";
			}else
			{
				baseType = NodeUtils.GetAttributeValue(restrict,"base","");
			}
		}
	}

	public XFTRule()
	{}
	
	public void setRestrictionProps(Node restrict,String prefix)
	{
	    NodeList children = restrict.getChildNodes();
		if (children != null)
		{
			for (int i=0;i<children.getLength();i++)
			{
				Node child = children.item(i);
				if (child.getNodeName().equalsIgnoreCase(prefix+":maxLength"))
				{
					maxLength = NodeUtils.GetAttributeValue(child,"value","");
				}
				if (child.getNodeName().equalsIgnoreCase(prefix+":minLength"))
				{
					minLength = NodeUtils.GetAttributeValue(child,"value","");
				}
				if (child.getNodeName().equalsIgnoreCase(prefix+":enumeration"))
				{
					possibleValues.add(NodeUtils.GetAttributeValue(child,"value","0"));
				}
				if (child.getNodeName().equalsIgnoreCase(prefix+":pattern"))
				{
					mask = NodeUtils.GetAttributeValue(child,"value","0");
				}
				if (child.getNodeName().equalsIgnoreCase(prefix+":length"))
				{
					length = NodeUtils.GetAttributeValue(child,"value","");
				}
				if (child.getNodeName().equalsIgnoreCase(prefix+":minInclusive"))
				{
					minInclusive = NodeUtils.GetAttributeValue(child,"value","");
				}
				if (child.getNodeName().equalsIgnoreCase(prefix+":maxInclusive"))
				{
					maxInclusive = NodeUtils.GetAttributeValue(child,"value","");
				}
			}
		}
	}
		
	/**
	 * @return Returns the base type String
	 */
	public String getBaseType() {
		return baseType;
	}

	/**
	 * @return Returns the length String
	 */
	public String getLength() {
		return length;
	}

	/**
	 * @return Returns the mask String
	 */
	public String getMask() {
		return mask;
	}

	/**
	 * @return Returns the maximum length String
	 */
	public String getMaxLength() {
		return maxLength;
	}

	/**
	 * @return Returns the message String
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return Returns the minimum length String
	 */
	public String getMinLength() {
		return minLength;
	}

	/**
	 * @return Returns the list of all possible values
	 */
	public ArrayList getPossibleValues() {
		return possibleValues;
	}

	/**
	 * @param string
	 */
	public void setBaseType(String string) {
		baseType = string;
	}

	/**
	 * @param i
	 */
	public void setLength(String i) {
		length = i;
	}

	/**
	 * @param string
	 */
	public void setMask(String string) {
		mask = string;
	}

	/**
	 * @param i
	 */
	public void setMaxLength(String i) {
		maxLength = i;
	}

	/**
	 * @param string
	 */
	public void setMessage(String string) {
		message = string;
	}

	/**
	 * @param i
	 */
	public void setMinLength(String i) {
		minLength = i;
	}
	
	/**
	 * @param s
	 */
	public void addPossibleValue(String s)
	{
		possibleValues.add(s);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append("XDATRule\n");
		sb.append("length:").append(this.getLength()).append("\n");
		sb.append("maxLength:").append(this.getMaxLength()).append("\n");
		sb.append("minLength:").append(this.getMinLength()).append("\n");
		sb.append("baseType:").append(this.getBaseType()).append("\n");
		sb.append("mask:").append(this.getMask()).append("\n");
		sb.append("message:").append(this.getMessage()).append("\n");
		
		Iterator i = this.getPossibleValues().iterator();
		while (i.hasNext())
		{
			sb.append("possibleValue").append(i.next().toString()).append("\n");
		}
		return sb.toString();
	}

	/**
	 * @param header
	 * @return Returns this XFTRule expressed as a String
	 */
	public String toString(String header)
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append(header).append("XDATRule\n");
		if (getLength() != "")
			sb.append(header).append("length:").append(this.getLength()).append("\n");
		if (getMaxLength() != "")
			sb.append(header).append("maxLength:").append(this.getMaxLength()).append("\n");
		if (getMinLength() != "")
			sb.append(header).append("minLength:").append(this.getMinLength()).append("\n");
		if (getBaseType() != "")
			sb.append(header).append("baseType:").append(this.getBaseType()).append("\n");
		if (getMask() != "")
			sb.append(header).append("mask:").append(this.getMask()).append("\n");
		if (getMessage() != "")
			sb.append(header).append("message:").append(this.getMessage()).append("\n");
		if (getMaxInclusive() != "")
			sb.append(header).append("maxInclusive:").append(this.getMaxInclusive()).append("\n");
		if (getMinInclusive() != "")
			sb.append(header).append("minInclusive:").append(this.getMinInclusive()).append("\n");
		
		Iterator i = this.getPossibleValues().iterator();
		while (i.hasNext())
		{
			sb.append(header).append("possibleValue").append(i.next().toString()).append("\n");
		}
		return sb.toString();
	}
	

	
	public Node toXML(Document doc)
	{
		Node main = doc.createElement("rule");
		if (getLength() != "")
			main.appendChild(NodeUtils.CreateTextNode(doc,"length",this.getLength()));
		if (getMaxLength() != "")
			main.appendChild(NodeUtils.CreateTextNode(doc,"maxLength",this.getMaxLength()));
		if (getMinLength() != "")
			main.appendChild(NodeUtils.CreateTextNode(doc,"minLength",this.getMinLength()));
		if (getBaseType() != "")
			main.appendChild(NodeUtils.CreateTextNode(doc,"baseType",this.getBaseType()));
		if (getMask() != "")
			main.appendChild(NodeUtils.CreateTextNode(doc,"mask",this.getMask()));
		if (getMessage() != "")
			main.appendChild(NodeUtils.CreateTextNode(doc,"message",this.getMessage()));
		if (getMaxInclusive() != "")
			main.appendChild(NodeUtils.CreateTextNode(doc,"maxInclusive",this.getMaxInclusive()));
		if (getMinInclusive() != "")
			main.appendChild(NodeUtils.CreateTextNode(doc,"minInclusive",this.getMinInclusive()));
		
		if (getPossibleValues().size() > 0){
			Iterator i = this.getPossibleValues().iterator();
			Node pv = NodeUtils.CreateEmptyNode(doc,"possible-values");
			while (i.hasNext())
			{
				pv.appendChild(NodeUtils.CreateTextNode(doc,"possible-value",i.next().toString()));
			}
			main.appendChild(pv);
		}
		
		return main;
	}
	/**
	 * @return Returns the maximum inclusive String
	 */
	public String getMaxInclusive() {
		return maxInclusive;
	}

	/**
	 * @return Returns the minimum inclusive String
	 */
	public String getMinInclusive() {
		return minInclusive;
	}

	/**
	 * @param string
	 */
	public void setMaxInclusive(String string) {
		maxInclusive = string;
	}

	/**
	 * @param string
	 */
	public void setMinInclusive(String string) {
		minInclusive = string;
	}

}

