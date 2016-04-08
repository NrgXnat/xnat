/*
 * org.nrg.xft.schema.XFTField
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/30/13 5:36 PM
 */


package org.nrg.xft.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xft.identifier.Identifier;
import org.nrg.xft.schema.design.XFTNode;
import org.nrg.xft.utils.NodeUtils;
import org.nrg.xft.utils.XftStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
public abstract class XFTField extends XFTNode implements Identifier{
	static org.apache.log4j.Logger logger = Logger.getLogger(XFTField.class);
	public abstract String toString(String header);

	private String displayName = "";
	private String description = "";
	private String use = "";
	private String fixed = "";
	private String size = "";
	private String maxOccurs = "";
	private String minOccurs = "";
	private int sequence = 0;
	private String expose = "";
	private String unique = "";
	private String uniqueComposite = "";
	private String required = "";
	private String fullName = "";
	private String xmlOnly = "";
	private String baseElement = "";
	private String baseCol = "";

	private Boolean hasChildren = null;
	private Boolean hasAttributes = null;

	private boolean isAttribute = false;
	private Hashtable childFields = new Hashtable();

	private boolean extension = false;

	private String xmlDisplay = "always";//always,never,root

	private XFTSqlField sqlField = null;
	private XFTRelation relation = null;
	private XFTWebAppField webAppField = null;
	private XFTRule rule = null;

	private boolean filter = false;
	private boolean localMap = false;

    private boolean preventLoop = false;
    private boolean possibleLoop = false;

	private boolean onlyRoot = false;
	public boolean isOnlyRoot() {
		return onlyRoot;
	}

	public void setOnlyRoot(boolean onlyRoot) {
		this.onlyRoot = onlyRoot;
	}
	private String _finalSqlName=null;
	/**
	 * @return Returns the localMap.
	 */
	public boolean isLocalMap() {
		return localMap;
	}

	/**
	 * @param localMap The localMap to set.
	 */
	public void setLocalMap(boolean localMap) {
		this.localMap = localMap;
	}
	/**
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Display label for this field
	 * @return
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @return
	 */
	public String getFixed() {
		return fixed;
	}

	/**
	 * @return
	 */
	public String getMaxOccurs() {
		return maxOccurs;
	}

	/**
	 * @return
	 */
	public String getMinOccurs() {
		return minOccurs;
	}

	/**
	 * @return
	 */
	public String getSize() {
		return size;
	}

	/**
	 * @return
	 */
	public String getUse() {
		return use;
	}

	/**
	 * @param string
	 */
	public void setDescription(String string) {
		description = XftStringUtils.intern(string);
	}

	/**
	 * @param string
	 */
	public void setDisplayName(String string) {
		displayName = XftStringUtils.intern(string);
	}

	/**
	 * @param string
	 */
	public void setFixed(String string) {
		fixed = XftStringUtils.intern(string);
	}

	/**
	 * @param i
	 */
	public void setMaxOccurs(String i) {
		maxOccurs = XftStringUtils.intern(i);
	}

	/**
	 * @param i
	 */
	public void setMinOccurs(String i) {
		minOccurs = XftStringUtils.intern(i);
	}

	/**
	 * @param i
	 */
	public void setSize(String i) {
		size = XftStringUtils.intern(i);
	}

	/**
	 * @param string
	 */
	public void setUse(String string) {
		use = XftStringUtils.intern(string);
		if (use.equalsIgnoreCase("optional"))
		{
			this.setMinOccurs("0");
		}
	}

	/**
	 * (never null)
	 * @return
	 */
	public XFTRule getRule() {
		if (rule == null)
			return new XFTRule();
		else
			return rule;
	}

	/**
	 * (never null)
	 * @return
	 */
	public XFTSqlField getSqlField() {
		if (sqlField == null)
		{
			sqlField = new XFTSqlField();
		}
		return sqlField;
	}

	/**
	 * (never null)
	 * @return
	 */
	public XFTWebAppField getWebAppField() {
		if (webAppField == null)
		{
			webAppField = new TorqueField();
		}
		return webAppField;
	}

	/**
	 * @param rule
	 */
	public void setRule(XFTRule rule) {
		this.rule = rule;
	}

	/**
	 * @param field
	 */
	public void setSqlField(XFTSqlField field) {
		sqlField = field;
	}

	/**
	 * @param field
	 */
	public void setWebAppField(XFTWebAppField field) {
		webAppField = field;
	}

	/**
	 * sequence in relation to other fields at this level in this element.
	 * @return
	 */
	public int getSequence() {
		return sequence;
	}

	/**
	 * sequence in relation to other fields at this level in this element.
	 * @param i
	 */
	public void setSequence(int i) {
		sequence = i;
	}

	public void setOnDelete(String s)
	{
	    if (relation == null)
	    {
	        relation = new XFTRelation();
	    }
	    relation.setOnDelete(s);
	}

	/**
	 * @return
	 */
	public abstract String getName();

	/**
	 * whether or not this field is an XML attribute.
	 * @return
	 */
	public boolean isAttribute() {
		return isAttribute;
	}

	/**
	 * @param b
	 */
	public void setAttribute(boolean b) {
		isAttribute = b;
		if (isAttribute)
		{
		    if (this.use==null || use.equals(""))
		    {
		        use="optional";
		        minOccurs="0";
		    }
		}
	}

	/**
	 * Searches the XML DOM Node for elements and attributes to turn into XFTFields.
	 * This method can handle elements, groups, attributeGroups, simpleTypes, complexTypes,
	 * extensions, and sequences.
	 *
	 * @param node Node to search
	 * @param header Used to add parent field names to child full names.
	 * @param parent XFTField or XFTElement
	 * @param prefix XMLNS prefix
	 * @param s owner Schema
	 * @return
	 */
	public static Hashtable GetFields(Node node,String header,XFTNode parent, String prefix,XFTSchema s)
	{
		Hashtable fields = new Hashtable();
		if (node.getNodeName().equalsIgnoreCase(prefix+":element"))
		{
			fields = XFTField.HandleComplexType(node,header,parent,prefix,s);
		}else if (node.getNodeName().equalsIgnoreCase(prefix+":group"))
		{
			fields = XFTField.HandleComplexType(node,header,parent,prefix,s);
		}else if (node.getNodeName().equalsIgnoreCase(prefix+":attributeGroup"))
		{
			fields = XFTField.HandleComplexType(node,header,parent,prefix,s);
		}else if (node.getNodeName().equalsIgnoreCase(prefix+":simpleType"))
		{
			//SEQUENCE NODE WITH IN A NAMED GROUP
			if (node.hasChildNodes())
		  	{
				for(int i=0;i<node.getChildNodes().getLength();i++)
				{
					//level 1
					Node child1 = node.getChildNodes().item(i);
					if (child1.hasChildNodes())
					{
						if (child1.getNodeName().equalsIgnoreCase(prefix+":list"))
						{
							XFTDataField temp = XFTDataField.GetEmptyField();
							temp.setXMLType(s.getBasicDataType("string"));
							temp.setSize("250");
							temp.setName(NodeUtils.GetAttributeValue(node,"name",""));
							temp.setFullName(header + "_" + temp.getName());
							fields.put(temp.getName(),temp);
						}
					}
				}
		  	}
		}else if (node.getNodeName().equalsIgnoreCase(prefix+":complexType"))
		{
			fields = XFTField.HandleComplexType(node,header,parent,prefix,s);
		}else if (node.getNodeName().equalsIgnoreCase(prefix+":extension"))
		{
			fields = XFTField.HandleComplexType(node,header,parent,prefix,s);
		}else if (node.getNodeName().equalsIgnoreCase(prefix+":sequence"))
		{
			fields = XFTField.HandleComplexType(node,header,parent,prefix,s);
		}else if (node.getNodeName().equalsIgnoreCase(prefix+":choice"))
		{
			fields = XFTField.HandleComplexType(node,header,parent,prefix,s);
		}
		return fields;
	}

	/**
	 * @return
	 */
	public Hashtable getChildFields() {
		return childFields;
	}

	/**
	 * Inserts the XFTField into this field's collection of children.
	 * @param xf
	 */
	public void addChildField(XFTField xf)
	{
		this.childFields.put(xf.getName(),xf);
	}

	/**
	 * key = XFTField.getName(),value = XFTField
	 * @param hashtable
	 */
	public void setChildFields(Hashtable hashtable) {
		childFields = hashtable;
	}

	/**
	 * 'Attribute:' if it is an attribute, otherwise 'Element:'
	 * @return
	 */
	public String displayAttribute()
	{
		if (isAttribute)
			return "Attribute:";
		else
			return "Element:";
	}

	/**
	 * If this field is the child of another XFTField, then that parent's xml name
	 * will be joined to this field's xml name to create a 'full' name.
	 * @return
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * removes the first character if it is '_'
	 * @param string
	 */
	public void setFullName(String string) {
		if (string.startsWith("_"))
		{
			string = string.substring(1);
		}
		fullName = string;
	}

	public void setPrimaryKey(String s)
	{
		if (this.getSqlField() == null)
		{
			setSqlField(new XFTSqlField());
		}
		getSqlField().setPrimaryKey(s);
	}

	/**
	 * @return
	 */
	public XFTRelation getRelation() {
	    return relation;
	}

	/**
	 * @param relation
	 */
	public void setRelation(XFTRelation relation) {
		this.relation = relation;
	}

	/**
	 * ArrayList of XFTField children ordered by sequence
	 * @return ArrayList of XFTFields
	 */
	public ArrayList getSortedFields()
	{
		ArrayList temp = new ArrayList();
		temp.addAll(this.getChildFields().values());
		Collections.sort(temp,XFTField.SequenceComparator);
		return temp;
	}

	public final static Comparator SequenceComparator = new Comparator() {
	  public int compare(Object mr1, Object mr2) throws ClassCastException {
		  try{
			int value1 = ((XFTField)mr1).getSequence();
			int value2 = ((XFTField)mr2).getSequence();

			if (value1 > value2)
			  {
				  return 1;
			  }else if(value1 < value2)
			  {
				  return -1;
			  }else
			  {
				  return 0;
			  }
		  }catch(Exception ex)
		  {
			  throw new ClassCastException("Error Comparing LabIds");
		  }
	  }
	};
	/**
	 * @return
	 */
	public String getExpose() {
		return expose;
	}

	/**
	 * @return
	 */
	public String getRequired() {
		return required;
	}

	/**
	 * @return
	 */
	public String getUnique() {
		return unique;
	}

	/**
	 * @return
	 */
	public String getUniqueComposite() {
		return uniqueComposite;
	}

	/**
	 * @param string
	 */
	public void setExpose(String string) {
		expose = string;
	}

	/**
	 * @param string
	 */
	public void setRequired(String string) {
		required = string;
	}

	/**
	 * @param string
	 */
	public void setUnique(String string) {
		unique = string;
	}

	/**
	 * @param string
	 */
	public void setUniqueComposite(String string) {
		uniqueComposite = string;
	}

	/**
	 * If field has children which ARE NOT attributes, then true, else false.
	 * @return
	 */
	public boolean hasChildren()
	{
		if (hasChildren == null)
		{
		    if (this.getSortedFields().size()==0)
		        hasChildren = Boolean.FALSE;
		    else
		        hasChildren = Boolean.TRUE;
//			Iterator iter = this.getSortedFields().iterator();
//			while (iter.hasNext())
//			{
//				XFTField xf = (XFTField)iter.next();
//				if (! xf.isAttribute())
//				{
//					hasChildren = Boolean.TRUE;
//					break;
//				}
//			}
		}
		return hasChildren.booleanValue();
	}

	/**
	 * If field has children which ARE attributes, then true, else false.
	 * @return
	 */
	public boolean hasAttributes()
	{
		if (hasAttributes == null)
		{
			hasAttributes = Boolean.FALSE;
			Iterator iter = this.getSortedFields().iterator();
			while (iter.hasNext())
			{
				XFTField xf = (XFTField)iter.next();
				if (xf.isAttribute())
				{
					hasAttributes = Boolean.TRUE;
					break;
				}
			}
		}
		return hasAttributes.booleanValue();
	}

	/**
	 * if this field is displayed in XML only (not in SQL)
	 * @return
	 */
	public String getXmlOnly() {
		return xmlOnly;
	}

	/**
	 * if this field is displayed in XML only (not in SQL)
	 * @param string
	 */
	public void setXmlOnly(String string) {
		xmlOnly = string;
	}

	/**
	 * name of the field which this field relates to.
	 * @return
	 */
	public String getBaseCol() {
		return baseCol;
	}

	/**
	 * dot Syntax specification of the element which this field relates to.
	 * @return
	 */
	public String getBaseElement() {
		return baseElement;
	}

	/**
	 * name of the field which this field relates to.
	 * @param string
	 */
	public void setBaseCol(String string) {
		baseCol = string;
//		if (baseCol !=null && !baseCol.equals(""))
//		{
//		    this.getParentElement().setHasBase(true);
//		}
	}

	/**
	 * dot Syntax specification of the element which this field relates to.
	 * @param string
	 */
	public void setBaseElement(String string) {
		baseElement = string;
//		if (baseElement !=null && !baseElement.equals(""))
//		{
//		    this.getParentElement().setHasBase(true);
//		}
	}

	public boolean hasBase()
	{
	    if (baseElement != null && baseElement != "")
	    {
	        if (baseCol != null && baseCol != "")
		    {
		        return true;
		    }
	    }

	    return false;
	}

	/**
	 * recursive method which counts the number of elements and attributes in this node.
	 * @param n Node to search
	 * @param prefix XMLNS prefix
	 * @return count of attributes and elements
	 */
	public static int GetElementCount(Node n, String prefix)
	{
		int counter = 0;

		if (n.getNodeName().equalsIgnoreCase(prefix + ":element"))
		{
			counter++;
		}else if (n.getNodeName().equalsIgnoreCase(prefix + ":group"))
		{
			if (n.hasChildNodes())
			{
				for (int i=0;i<n.getChildNodes().getLength();i++)
				{
					Node child1 = n.getChildNodes().item(i);
					if (child1.getNodeName().equalsIgnoreCase(prefix + ":sequence"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								counter = counter + GetElementCount(child2,prefix);
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":choice"))
					{
						counter++;
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":all"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								counter = counter + GetElementCount(child2,prefix);
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":group"))
					{
						counter++;
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":complexType"))
					{
						counter = counter + GetElementCount(child1,prefix);
					}
				}
			}
		}else if (n.getNodeName().equalsIgnoreCase(prefix + ":complexType"))
		{
			if (n.hasChildNodes())
			{
				for (int i=0;i<n.getChildNodes().getLength();i++)
				{
					Node child1 = n.getChildNodes().item(i);
					if (child1.getNodeName().equalsIgnoreCase(prefix + ":sequence"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								counter = counter + GetElementCount(child2,prefix);
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":choice"))
					{
						counter++;
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":all"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								counter = counter + GetElementCount(child2,prefix);
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":group"))
					{
						counter++;
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":attribute"))
					{
						counter++;
					}
				}
			}
		}else if (n.getNodeName().equalsIgnoreCase(prefix + ":sequence"))
		{
			if (n.hasChildNodes())
			{
				for (int i=0;i<n.getChildNodes().getLength();i++)
				{
					Node child1 = n.getChildNodes().item(i);
					if (child1.getNodeName().equalsIgnoreCase(prefix + ":sequence"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								counter = counter + GetElementCount(child2,prefix);
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":choice"))
					{
						counter++;
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":all"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								counter = counter + GetElementCount(child2,prefix);
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":group"))
					{
						counter++;
					}
				}
			}
		}else if (n.getNodeName().equalsIgnoreCase(prefix + ":simpleType"))
		{
			counter++;
		}else if (n.getNodeName().equalsIgnoreCase(prefix + ":choice"))
		{
			counter+=2;
		}

		return counter;
	}

	/**
	 * recursive method which checks to see if this Node is more than a single reference to another node.
	 * @param n Node to search
	 * @param prefix XMLNS
	 * @param s parent schema
	 * @return
	 */
	public static boolean IsRefOnly(Node n,String prefix,XFTSchema s)
	{
		boolean _return = false;
		int refs = 0;
		int count = GetElementCount(n,prefix);
		if (count==1)
		{
			if (n.getNodeName().equalsIgnoreCase(prefix + ":element"))
			{
				if (! NodeUtils.GetAttributeValue(n,"ref","none").equalsIgnoreCase("none"))
				{
					return true;
				}else if (! NodeUtils.GetAttributeValue(n,"type","none").equalsIgnoreCase("none"))
				{
					if (NodeUtils.GetAttributeValue(n,"maxOccurs","1").equalsIgnoreCase("1"))
					{
						String type = NodeUtils.GetAttributeValue(n.getAttributes(),"type","none");
						Node refType=NodeWrapper.FindNode(type,s);
						if (refType!=null)
						{
							return true;
						}
					}
				}
			}else if (n.getNodeName().equalsIgnoreCase(prefix + ":group"))
			{
				if (! NodeUtils.GetAttributeValue(n,"ref","none").equalsIgnoreCase("none"))
				{
					return true;
				}

				if (n.hasChildNodes())
				{
					for (int i=0;i<n.getChildNodes().getLength();i++)
					{
						Node child1 = n.getChildNodes().item(i);
						if (child1.getNodeName().equalsIgnoreCase(prefix + ":sequence"))
						{
							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":choice"))
						{
							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":all"))
						{
							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":group"))
						{
							if (! NodeUtils.GetAttributeValue(child1,"ref","none").equalsIgnoreCase("none"))
							{
								return true;
							}

							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":complexType"))
						{
							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}
					}
				}
			}else if (n.getNodeName().equalsIgnoreCase(prefix + ":complexType"))
			{
				if (n.hasChildNodes())
				{
					for (int i=0;i<n.getChildNodes().getLength();i++)
					{
						Node child1 = n.getChildNodes().item(i);
						if (child1.getNodeName().equalsIgnoreCase(prefix + ":sequence"))
						{
							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":choice"))
						{
							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":all"))
						{
							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":group"))
						{
							if (! NodeUtils.GetAttributeValue(child1,"ref","none").equalsIgnoreCase("none"))
							{
								return true;
							}

							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":complexType"))
						{
							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}
					}
				}
			}else if (n.getNodeName().equalsIgnoreCase(prefix + ":sequence"))
			{
				if (n.hasChildNodes())
				{
					for (int i=0;i<n.getChildNodes().getLength();i++)
					{
						Node child1 = n.getChildNodes().item(i);
						if (child1.getNodeName().equalsIgnoreCase(prefix + ":sequence"))
						{
							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":choice"))
						{
							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":all"))
						{
							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":group"))
						{
							if (! NodeUtils.GetAttributeValue(child1,"ref","none").equalsIgnoreCase("none"))
							{
								return true;
							}

							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":complexType"))
						{
							if (child1.hasChildNodes())
							{
								for (int j=0;j<child1.getChildNodes().getLength();j++)
								{
									Node child2 = child1.getChildNodes().item(j);
									_return =  IsRefOnly(child2,prefix,s);
									if (_return) return true;
								}
							}
						}
					}
				}
			}
		}

		return _return;
	}

	/**
	 * recursive method which gets the name of the first referenced element.
	 * @param n Node to search
	 * @param prefix XMLNS
	 * @param s parent schema
	 * @return
	 */
	public static String GetRefName(Node n, String prefix,XFTSchema s)
	{
		String _return = null;
		if (n.getNodeName().equalsIgnoreCase(prefix + ":element"))
		{
			if (! NodeUtils.GetAttributeValue(n,"ref","none").equalsIgnoreCase("none"))
			{
				return NodeUtils.GetAttributeValue(n.getAttributes(),"ref","none");
			}else if (! NodeUtils.GetAttributeValue(n,"type","none").equalsIgnoreCase("none"))
			{
				String type = NodeUtils.GetAttributeValue(n.getAttributes(),"type","none");
				Node refType=NodeWrapper.FindNode(type,s);
				if (refType!=null)
				{
					return type;
				}
			}
		}else if (n.getNodeName().equalsIgnoreCase(prefix + ":group"))
		{
			if (! NodeUtils.GetAttributeValue(n,"ref","none").equalsIgnoreCase("none"))
			{
				return NodeUtils.GetAttributeValue(n.getAttributes(),"ref","none");
			}

			if (n.hasChildNodes())
			{
				for (int i=0;i<n.getChildNodes().getLength();i++)
				{
					Node child1 = n.getChildNodes().item(i);
					if (child1.getNodeName().equalsIgnoreCase(prefix + ":sequence"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								_return =  GetRefName(child2,prefix,s);
								if (_return != null) return _return;
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":choice"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								if (! NodeUtils.GetAttributeValue(child2,"name","none").equalsIgnoreCase("spliceFrom"))
								{
									_return =  GetRefName(child2,prefix,s);
									if (_return != null) return _return;
								}
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":all"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								_return =  GetRefName(child2,prefix,s);
								if (_return != null) return _return;
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":group"))
					{
						if (! NodeUtils.GetAttributeValue(child1,"ref","none").equalsIgnoreCase("none"))
						{
							return NodeUtils.GetAttributeValue(n.getAttributes(),"ref","none");
						}

						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								_return =  GetRefName(child2,prefix,s);
								if (_return != null) return _return;
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":complexType"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								_return =  GetRefName(child2,prefix,s);
								if (_return != null) return _return;
							}
						}
					}
				}
			}
		}else if (n.getNodeName().equalsIgnoreCase(prefix + ":complexType"))
		{
			if (n.hasChildNodes())
			{
				for (int i=0;i<n.getChildNodes().getLength();i++)
				{
					Node child1 = n.getChildNodes().item(i);
					if (child1.getNodeName().equalsIgnoreCase(prefix + ":sequence"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								_return =  GetRefName(child2,prefix,s);
								if (_return != null) return _return;
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":choice"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								if (! NodeUtils.GetAttributeValue(child2,"name","none").equalsIgnoreCase("spliceFrom"))
								{
									_return =  GetRefName(child2,prefix,s);
									if (_return != null) return _return;
								}
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":all"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								_return =  GetRefName(child2,prefix,s);
								if (_return != null) return _return;
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":group"))
					{
						if (! NodeUtils.GetAttributeValue(child1,"ref","none").equalsIgnoreCase("none"))
						{
							return NodeUtils.GetAttributeValue(child1.getAttributes(),"ref","none");
						}

						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								_return =  GetRefName(child2,prefix,s);
								if (_return != null) return _return;
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":complexType"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								_return =  GetRefName(child2,prefix,s);
								if (_return != null) return _return;
							}
						}
					}
				}
			}
		}	else if (n.getNodeName().equalsIgnoreCase(prefix + ":sequence"))
		{
			if (n.hasChildNodes())
			{
				for (int i=0;i<n.getChildNodes().getLength();i++)
				{
					Node child1 = n.getChildNodes().item(i);
					if (child1.getNodeName().equalsIgnoreCase(prefix + ":sequence"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								_return =  GetRefName(child2,prefix,s);
								if (_return != null) return _return;
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":choice"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								if (! NodeUtils.GetAttributeValue(child2,"name","none").equalsIgnoreCase("spliceFrom"))
								{
									_return =  GetRefName(child2,prefix,s);
									if (_return != null) return _return;
								}
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":all"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								_return =  GetRefName(child2,prefix,s);
								if (_return != null) return _return;
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":group"))
					{
						if (! NodeUtils.GetAttributeValue(child1,"ref","none").equalsIgnoreCase("none"))
						{
							return NodeUtils.GetAttributeValue(child1.getAttributes(),"ref","none");
						}

						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								_return =  GetRefName(child2,prefix,s);
								if (_return != null) return _return;
							}
						}
					}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":complexType"))
					{
						if (child1.hasChildNodes())
						{
							for (int j=0;j<child1.getChildNodes().getLength();j++)
							{
								Node child2 = child1.getChildNodes().item(j);
								_return =  GetRefName(child2,prefix,s);
								if (_return != null) return _return;
							}
						}
					}
				}
			}
		}
		return _return;
	}

	/**
	 * Returns a hashtable of XFTFields (key=XFTField.getName(),value=XFTField)
	 * <BR><BR>Checks for child nodes of type complexType, element, attribute, sequence,
	 * choice, all, group, simpleContent, attributeGroup.  If it is an element, attribute, choice,
	 * simpleContent, or all then it uses the AddField method to add the appropriate XFTFields.  Otherwise,
	 * it recursively calls itself on the child node to get further children.
	 *
	 * @param node Node to search
	 * @param header from parent fields
	 * @param parent XFTField or XFTElement
	 * @param prefix XMLNS
	 * @param s parent schema
	 * @return Hashtable of XFTFields
	 */
	public static Hashtable HandleComplexType(Node node,String header,XFTNode parent, String prefix,XFTSchema s)
	{
		Hashtable fields = new Hashtable();

		int counter = 1;
		if (node.hasChildNodes())
		{
			for (int j=0;j<node.getChildNodes().getLength();j++)
			{
				Node child1 = node.getChildNodes().item(j);
				if (child1.getNodeName().equalsIgnoreCase(prefix + ":complexType"))
				{
					logger.debug("Loading complexType element");
					Hashtable temp = XFTField.HandleComplexType(child1,header,parent,prefix,s);
					Enumeration enumer = temp.keys();
					while (enumer.hasMoreElements())
					{
						String st = (String)enumer.nextElement();
						XFTField xf = (XFTField)temp.get(st);
						fields.put(st,xf);
					}
				}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":element"))
				{
					XFTField xf = AddField(child1,header,parent,prefix,s);
					if (xf != null)
					{
						xf.setSequence(counter++);
						fields.put(xf.getName(),xf);
					}
				}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":sequence"))
				{
					logger.debug("Loading sequence element");
					if (child1.hasChildNodes())
					{
						for (int k=0;k<child1.getChildNodes().getLength();k++)
						{
							Node child2 = child1.getChildNodes().item(k);
							if (child2.getNodeName().equalsIgnoreCase(prefix + ":choice"))
							{
								logger.debug("Loading choice element");
								if (child2.hasChildNodes())
								{
									for (int l=0;l<child2.getChildNodes().getLength();l++)
									{
										Node child3 = child2.getChildNodes().item(l);
										if ((! NodeUtils.GetAttributeValue(child3.getAttributes(),"name","none").equalsIgnoreCase("spliceFrom")) && (! NodeUtils.GetAttributeValue(child3.getAttributes(),"type","none").equalsIgnoreCase(prefix + ":IDREF")))
										{
											XFTField xf = AddField(child3,header,parent,prefix,s);
											if (xf != null)
											{
												xf.setMinOccurs("0");
												xf.setSequence(counter++);
												fields.put(xf.getName(),xf);
											}
										}else if (NodeUtils.GetAttributeValue(child3.getAttributes(),"type","none").equalsIgnoreCase(prefix + ":IDREF"))
										{
											XFTDataField xf = XFTDataField.GetEmptyField();
											xf.setXMLType(s.getBasicDataType("IDREF"));
											xf.setSize("250");
											xf.setName(NodeUtils.GetAttributeValue(child3,"name",""));
											xf.setFullName(header + "_" + xf.getName());
											xf.setSequence(counter++);
											xf.setXmlOnly("true");
											xf.setParent(parent);
											xf.setMinOccurs("0");
											fields.put(xf.getName(),xf);
										}
									}
								}
							}else if (child2.getNodeName().equalsIgnoreCase(prefix+":sequence"))
							{
								if ((NodeUtils.GetAttributeValue(child2,"maxOccurs","none") != "none") && (!NodeUtils.GetAttributeValue(node,"maxOccurs","none").equalsIgnoreCase("1")))
								{
									//SEQUENCE WITH-IN A SEQUENCE MAXOCCURS > 1
									XFTField xf = AddField(child2,header,parent,prefix,s);
									XFTReferenceField xRF = (XFTReferenceField)xf;
									xRF.setChildXMLNode(false);
									xRF.setInLineRepeaterElement(true);
									if (xf != null)
									{
										xf.setSequence(counter++);
										fields.put(xf.getName(),xf);
									}
								}else{
									//SEQUENCE WITH IN A SEQUENCE
									Hashtable temp = XFTField.HandleComplexType(child2,header,parent,prefix,s);
									Enumeration enumer = temp.keys();
									while (enumer.hasMoreElements())
									{
										String st = (String)enumer.nextElement();
										XFTField xf = (XFTField)temp.get(st);
										xf.setSequence(counter++);
										xf.setMinOccurs("0");
										fields.put(st,xf);
									}
								}
							}else
							{
								XFTField xf = AddField(child2,header,parent,prefix,s);
								if (xf != null)
								{
									xf.setSequence(counter++);
									fields.put(xf.getName(),xf);
								}
							}

						}
					}
				}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":choice"))
				{
					logger.debug("Loading choice element");
					if (child1.hasChildNodes())
					{
					    if ((NodeUtils.GetAttributeValue(child1,"maxOccurs","none") != "none") && (!NodeUtils.GetAttributeValue(node,"maxOccurs","none").equalsIgnoreCase("1")))
						{
							//SEQUENCE WITH IN A SEQUENCE MAXOCCURS > 1
							XFTField xf = AddField(child1,header,parent,prefix,s);
							XFTReferenceField xRF = (XFTReferenceField)xf;
							xRF.setChildXMLNode(false);
							xRF.setInLineRepeaterElement(true);
							if (xf != null)
							{
								xf.setSequence(counter++);
								fields.put(xf.getName(),xf);
							}
						}else{
						    for (int k=0;k<child1.getChildNodes().getLength();k++)
							{
								Node child2 = child1.getChildNodes().item(k);
								if ((! NodeUtils.GetAttributeValue(child2.getAttributes(),"name","none").equalsIgnoreCase("spliceFrom")) && (! NodeUtils.GetAttributeValue(child2.getAttributes(),"type","none").equalsIgnoreCase(prefix + ":IDREF")))
								{
									XFTField xf = AddField(child2,header,parent,prefix,s);
									if (xf != null)
									{
										xf.setSequence(counter++);
										xf.setMinOccurs("0");
										fields.put(xf.getName(),xf);
									}
								}else if (NodeUtils.GetAttributeValue(child2.getAttributes(),"type","none").equalsIgnoreCase(prefix + ":IDREF"))
								{
									XFTDataField xf = XFTDataField.GetEmptyField();
									xf.setXMLType(s.getBasicDataType("IDREF"));
									xf.setSize("250");
									xf.setName(NodeUtils.GetAttributeValue(child2,"name",""));
									xf.setFullName(header + "_" + xf.getName());
									xf.setSequence(counter++);
									xf.setXmlOnly("true");
									xf.setParent(parent);
									xf.setMinOccurs("0");
									if (NodeUtils.HasAttribute(child2,"maxOccurs"))
									{
										if (! NodeUtils.GetAttributeValue(child2,"maxOccurs","1").equalsIgnoreCase("1"))
										{
											xf.setMaxOccurs(NodeUtils.GetAttributeValue(child2,"maxOccurs","1"));
										}
									}
									fields.put(xf.getName(),xf);
								}
							}
						}

					}
				}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":all"))
				{
					logger.debug("Loading all element");
					if (child1.hasChildNodes())
					{
						for (int k=0;k<child1.getChildNodes().getLength();k++)
						{
							Node child2 = child1.getChildNodes().item(k);
							XFTField xf = AddField(child2,header,parent,prefix,s);
							if (xf != null)
							{
								xf.setSequence(counter++);
								fields.put(xf.getName(),xf);
							}
						}
					}
				}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":group"))
				{
					logger.debug("Loading group element");
					XFTField xf = AddField(child1,header,parent,prefix,s);
					if (xf != null)
					{
						xf.setSequence(counter++);
						fields.put(xf.getName(),xf);
					}
				}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":attribute"))
				{
					logger.debug("Loading attribute element");
					XFTField xf = AddField(child1,header,parent,prefix,s);
					xf.setAttribute(true);
					if (xf != null)
					{
						xf.setSequence(counter++);
						fields.put(xf.getName(),xf);
					}
				}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":simpleContent"))
				{
					logger.debug("Loading simpleContent element");
					if (child1.hasChildNodes())
					{
						for (int k=0;k<child1.getChildNodes().getLength();k++)
						{
							Node child2= child1.getChildNodes().item(k);
							if (child2.getNodeName().equalsIgnoreCase(prefix+":extension"))
							{
								logger.debug("Loading extension element");
								for (int i=0;i<child2.getChildNodes().getLength();i++)
								{
									Node child3= child2.getChildNodes().item(i);
									if (child3.getNodeName().equalsIgnoreCase(prefix + ":attribute"))
									{
										logger.debug("Loading attribute element");
										XFTField xf = AddField(child3,header,parent,prefix,s);
										if (xf != null)
										{
											xf.setAttribute(true);
											xf.setSequence(counter++);
											fields.put(xf.getName(),xf);
										}
									}
								}
							}
						}
					}
				}else if (child1.getNodeName().equalsIgnoreCase(prefix + ":attributeGroup"))
				{
					logger.debug("Loading attributeGroup element");
					XFTField xf = AddField(child1,header,parent,prefix,s);
					if (xf != null)
					{
						xf.setAttribute(true);
						xf.setSequence(counter++);
						fields.put(xf.getName(),xf);
					}
				}
			}
		}

		return fields;
	}
	/**
	 * Returns one created XFTField or null.
	 * <BR><BR>If this node is an element, attribute, group, simpleContent, or sequence then
	 * it creates a corresponding XFTField.
	 * @param node Node to add
	 * @param header header of parent fields
	 * @param parent XFTField or XFTElement
	 * @param prefix XMLNS
	 * @param s parent schema
	 * @return
	 */
	public static XFTField AddField(Node node,String header,XFTNode parent, String prefix,XFTSchema s)
	{
	    return AddField(node,header,parent,prefix,s,true);
	}

	/**
	 * Returns one created XFTField or null.
	 * <BR><BR>If this node is an element, attribute, group, simpleContent, or sequence then
	 * it creates a corresponding XFTField.
	 * @param node Node to add
	 * @param header header of parent fields
	 * @param parent XFTField or XFTElement
	 * @param prefix XMLNS
	 * @param s parent schema
	 * @return
	 */
	public static XFTField AddField(Node node,String header,XFTNode parent, String prefix,XFTSchema s, boolean allowMaxOccursElement)
	{
		XFTField xf = null;

		if (node.getNodeName().equalsIgnoreCase(prefix+":element") || node.getNodeName().equalsIgnoreCase(prefix+":attribute") || node.getNodeName().equalsIgnoreCase(prefix+":attributeGroup"))
		{
            //System.out.println(NodeUtils.GetAttributeValue(node.getAttributes(),"name","none"));
            Node restriction = NodeUtils.GetLevel2Child(node, prefix +":restriction");
			//ELEMENT TO ADD
			if (NodeUtils.GetAttributeValue(node,"ref","none") != "none")
			{
				logger.debug("Loading simple ref element");
				xf = new XFTReferenceField(node,s);
			}else if (((! NodeUtils.GetAttributeValue(node,"type","none").equalsIgnoreCase("none")) || (restriction !=null && !NodeUtils.GetAttributeValue(restriction, "base", "none").equals("none"))) && (! NodeUtils.GetAttributeValue(node,"maxOccurs","none").equalsIgnoreCase("none")) && (!NodeUtils.GetAttributeValue(node,"maxOccurs","none").equalsIgnoreCase("1")))
			{

				String type = NodeUtils.GetAttributeValue(node.getAttributes(),"type","none");
                if (type.equals("none")){
                    type=NodeUtils.GetAttributeValue(restriction, "base", "none");
                }
				Node refType=NodeWrapper.FindNode(type,s);
				if (refType!=null)
				{
					//NAMED TYPE WITH MAX OCCURS
					if (XFTField.IsRefOnly(refType,prefix,s))
					{
						logger.debug("Loading namedType which includes simple ref/group element and max occurs > 1");

						String ref = XFTField.GetRefName(refType,prefix,s);
						Node refNode = NodeWrapper.FindNode(ref,s);
						if (refNode != null)
						{
							XFTReferenceField xRF = new XFTReferenceField(refNode,s);
							xRF.setProperties(node,s);
							xRF.setName(NodeUtils.GetAttributeValue(node,"name",""));
							xRF.setMaxOccurs("unbounded");
							xRF.setOnDelete("SET NULL");
							xf = xRF;
						}else{
							XFTReferenceField xRF = new XFTReferenceField(node,s);
							xRF.setProperties(node,s);
							xRF.setMaxOccurs("unbounded");
							xRF.setOnDelete("SET NULL");
							xf = xRF;
						}
					}else
					{
						logger.debug("Loading ref element with max occurs > 1");
						XFTReferenceField xRF = new XFTReferenceField(refType,s);
						xRF.setProperties(node,s);
						if (NodeUtils.NodeHasName(node))
						{
							xRF.setName(NodeUtils.GetNodeName(node));
						}
						xRF.setXMLType(new XMLType(type,s));
						xRF.setOnDelete("SET NULL");
						xRF.setMaxOccurs("unbounded");
						//xRF.setRefName(type);
						xf = xRF;
					}
				}else
				{
					//STANDARD DATA TYPE WITH MAX OCCURS
					logger.debug("Loading standard data type element with max occurs > 1");
					XFTReferenceField xRF = new XFTReferenceField(node,s);
					xRF.setInLineRepeaterElement(true);
					xRF.setMaxOccurs("unbounded");
//					xRF.setName(NodeUtils.GetCleanedAttributeValue(node.getAttributes(),"name",""));
//					xRF.setXMLType(new XMLType(NodeUtils.GetAttributeValue(node.getAttributes(),"name",""),s));
					XFTElement xe = null;
					if (parent.getParent()==null && (!(parent instanceof XFTElement)))
					{
					    xe= new XFTElement(node,prefix,s);
					}else{
					    xe= new XFTElement(node,prefix,s,parent.getParentElement().getName());
					}
					xRF.setXMLType(xe.getType());
					xe.setCreatedChild(true);
					xRF.setCreatedChild(true);
					xRF.setOnDelete("SET NULL");
					XFTSchema.tempElements.add(xe);
					xf = xRF;
					xe.setParent(xf);
				}
			}else if ((! NodeUtils.GetAttributeValue(node,"maxOccurs","none").equalsIgnoreCase("none")) && (!NodeUtils.GetAttributeValue(node,"maxOccurs","none").equalsIgnoreCase("1")) && allowMaxOccursElement )
			{
			    String temp = NodeUtils.GetAttributeValue(node,"maxOccurs","none");
				logger.debug("Loading element with max occurs > 1 but no type defined");
				XFTReferenceField xRF = new XFTReferenceField(node,s);
				xRF.setMaxOccurs("unbounded");
				//xRF.setRefName(NodeUtils.GetAttributeValue(node,"name",""));
				XFTElement xe = null;
				if (parent.getParent()==null && (!(parent instanceof XFTElement)))
				{
				    xe= new XFTElement(node,prefix,s);
				}else{
				    xe= new XFTElement(node,prefix,s,parent.getParentElement().getName());
				}
				xRF.setXMLType(xe.getType());
				xRF.setOnDelete("SET NULL");
				xe.setCreatedChild(true);
				xRF.setCreatedChild(true);
				XFTSchema.tempElements.add(xe);
				xf = xRF;
				xe.setParent(xf);
			}else{
				if (! NodeUtils.GetAttributeValue(node,"type","none").equalsIgnoreCase("none"))
				{
					logger.debug("Loading namedType element");
//					NAMED TYPE
					String type = NodeUtils.GetAttributeValue(node.getAttributes(),"type","none");
				    Node refType=NodeWrapper.FindNode(type,s);
				    if (refType!=null)
				    {
							logger.debug("Loading namedType element");
						   //NAMED TYPE
							if (XFTField.IsRefOnly(refType,prefix,s))
							{
								logger.debug("Loading namedType which inludes a ref/group only");
								String ref = XFTField.GetRefName(refType,prefix,s);
								Node refNode = NodeWrapper.FindNode(ref,s);
								if (refNode != null)
								{
									XFTReferenceField xRF = new XFTReferenceField(refNode,s);
									xRF.setProperties(node,s);
									xRF.setName(NodeUtils.GetAttributeValue(node,"name",""));
									xf = xRF;
								}else{
									XFTReferenceField xRF = new XFTReferenceField(node,s);
									xf = xRF;
								}
							}else if (refType.getNodeName().equalsIgnoreCase(prefix+":simpleType"))
							{
								logger.debug("Loading simpleType element");
								if (refType.hasChildNodes())
								{
									for(int i=0;i<refType.getChildNodes().getLength();i++)
									{
										//level 1
										Node child1 = refType.getChildNodes().item(i);
										if (child1.getNodeName().equalsIgnoreCase(prefix+":list"))
										{
											XFTDataField temp = XFTDataField.GetEmptyField();
											temp.setXMLType(s.getBasicDataType("string"));
											temp.setSize("250");
											if (NodeUtils.NodeHasName(node))
											{
												temp.setName(NodeUtils.GetNodeName(node));
											}else
											{
												temp.setName(NodeUtils.GetAttributeValue(refType,"name",""));
											}
											if (NodeUtils.HasAttribute(node,"minOccurs"))
											{
												temp.setMinOccurs(NodeUtils.GetAttributeValue(node,"minOccurs",""));
											}
											temp.setFullName(header + "_" + temp.getName());
											temp.validateSQLName();
											xf = temp;
										}else if(child1.getNodeName().equalsIgnoreCase(prefix+":restriction"))
										{
											xf = new XFTDataField(refType,header,prefix,s,parent);
											if (NodeUtils.NodeHasName(node))
											{
												xf.setName(NodeUtils.GetNodeName(node));
											}else
											{
												xf.setName(NodeUtils.GetAttributeValue(refType,"name",""));
											}
											if (NodeUtils.HasAttribute(node,"minOccurs"))
											{
												xf.setMinOccurs(NodeUtils.GetAttributeValue(node,"minOccurs",""));
											}
											xf.setFullName(header + "_" + xf.getName());
										}
									}
								}
							}else
							{
								if (NodeUtils.GetAttributeValue(refType,"mixed","false").equalsIgnoreCase("true"))
								{
									//MIXED TYPE NODE - Turn into String
									logger.debug("Converting Mixed Type to Common Data Type (String)");
									XFTDataField temp = XFTDataField.GetEmptyField();
									temp.setXMLType(s.getBasicDataType("string"));
									temp.setSize("250");
									if (NodeUtils.HasAttribute(node,"minOccurs"))
									{
										temp.setMinOccurs(NodeUtils.GetAttributeValue(node,"minOccurs",""));
									}
									temp.setName(NodeUtils.GetAttributeValue(node,"name",""));
									temp.setFullName(header + "_" + temp.getName());
									temp.validateSQLName();
									xf = temp;
								}else
								{
									logger.debug("Loading ref element");
									XFTReferenceField xRF = new XFTReferenceField(node,s);
									//xRF.setRefName(type);
									xf = xRF;
								}
							}
				    }else
				    {
						logger.debug("Loading data element");
						   //STANDARD DATA TYPE
						xf = new XFTDataField(node,header,prefix,s,parent);
                        if (type.indexOf("anyURI")!=-1){
                            xf.getRule().setBaseType("xs:anyURI");
                        }
						if (xf.getXMLType() == null && (! xf.hasChildren()))
						{
							xf.setXMLType(s.getBasicDataType("string"));
						}

						if (xf.hasBase())
						{
						    parent.getAbsoluteParent().setHasBase(true);
						}
				    }
				}else
				{
//					Node extension = XFTManager.GetLevel3Child();
//					if ()//HAS EXTENSION
//					{
//					}
					logger.debug("Loading data element");
					xf = new XFTDataField(node,header,prefix,s,parent);

					if (xf.getXMLType() == null && (! xf.hasChildren()))
					{
						xf.setXMLType(s.getBasicDataType("string"));
					}

					if (xf.hasBase())
					{
					    parent.getAbsoluteParent().setHasBase(true);
					}
				}
			}
		}else if (node.getNodeName().equalsIgnoreCase(prefix+":group"))
		{
			logger.debug("Loading group element");
			if (NodeUtils.GetAttributeValue(node,"ref","none") != "none")
			{
				String type = NodeUtils.GetAttributeValue(node.getAttributes(),"ref","none");
				Node refType=NodeWrapper.FindNode(type,s);
				if (refType!=null)
				{
					//NAMED TYPE WITH MAX OCCURS
					if (XFTField.IsRefOnly(refType,prefix,s))
					{
						logger.debug("Loading namedType element with max occurs");
						String ref = XFTField.GetRefName(refType,prefix,s);
						XFTReferenceField xRF = new XFTReferenceField(node,s);
						xRF.setChildXMLNode(false);
						//xRF.setRefName(ref);
						xf = xRF;
					}else
					{
						logger.debug("Loading ref element");
						XFTReferenceField xRF = new XFTReferenceField(node,s);
						//xRF.setRefName(type);
						xRF.setChildXMLNode(false);
						xf = xRF;
					}
				}
			}
		}else if (node.getNodeName().equalsIgnoreCase(prefix+":simpleContent"))
		{
			logger.debug("Loading simpleContent element");
			//SIMPLE CONTENT WITH IN A COMPLEX TYPE
			for(int j=0;j<node.getChildNodes().getLength();j++)
			{
				Node child3 = node.getChildNodes().item(j);
				if (child3.getNodeName().equalsIgnoreCase(prefix+":extension"))
				{
					logger.debug("Loading extension element");
					//EXTENSION NODE
					if ((NodeUtils.GetAttributeValue(node,"maxOccurs","none") != "none")  && (!NodeUtils.GetAttributeValue(node,"maxOccurs","none").equalsIgnoreCase("1")) )
					{
						//ONE TO MANY
						XFTDataField temp = XFTDataField.GetEmptyField();
						temp.setXMLType(new XMLType(NodeUtils.GetAttributeValue(child3,"base",""),s));
						temp.setName(NodeUtils.GetAttributeValue(node,"name",""));
						temp.setFullName(header + "_" + temp.getName());
						temp.validateSQLName();
						xf = temp;
						int k =1;
						for(int m=0;m<child3.getChildNodes().getLength();m++)
						{
							Node child4 = child3.getChildNodes().item(m);
							if (child4.getNodeName().equalsIgnoreCase(prefix+":attribute"))
							{
								XFTField sub = null;
								if (NodeUtils.GetAttributeValue(child4,"ref","none") != "none")
								{
									sub = new XFTReferenceField(child4,s);
								}else
								{
									sub = new XFTDataField(child4,header,prefix,s,parent);
								}
								sub.setAttribute(true);
								sub.setMaxOccurs("unbounded");
								sub.setFullName(header + "_" + sub.getName());
								sub.setSequence(k++ + 100);
								sub.setParent(temp);
								xf.addChildField(sub);

								if (xf.hasBase())
								{
								    parent.getAbsoluteParent().setHasBase(true);
								}
							}
						}
					}
				}
			}
		}else if (node.getNodeName().equalsIgnoreCase(prefix+":sequence"))
		{
			logger.debug("Loading sequence element with max occurs > 1");
			if (IsRefOnly(node,prefix,s))
			{
				logger.debug("Loading sequence which includes simple ref/group element and max occurs > 1");
				String ref = XFTField.GetRefName(node,prefix,s);
				XFTReferenceField xRF = new XFTReferenceField(node,s);
				xRF.setMaxOccurs("unbounded");
				if (xRF.getName().equalsIgnoreCase(""))
				{
					xRF.setName(XMLType.CleanType(ref));
				}
				if (xRF.getXMLType().getLocalType().equalsIgnoreCase(""))
				{
					xRF.setXMLType(new XMLType(ref,s));
				}
				//xRF.setRefName(ref);
				xf = xRF;
			}else{
				XFTReferenceField xRF = new XFTReferenceField(node,s);
				xRF.setName(parent.getParentElement().getName() + "_sequence");
				xRF.setXMLType(new XMLType(parent.getParentElement().getName() + "_sequence",s));
				xRF.setMaxOccurs("unbounded");
//				xRF.setName(NodeUtils.GetCleanedAttributeValue(node.getAttributes(),"name",""));
//				xRF.setXMLType(new XMLType(NodeUtils.GetAttributeValue(node.getAttributes(),"name",""),s));
				XFTElement xe = new XFTElement(node,prefix,s);
				xe.setName(parent.getParentElement().getName() + "_sequence");
				xe.setCreatedChild(true);
				xRF.setCreatedChild(true);
				XFTSchema.tempElements.add(xe);
				xf = xRF;
				xe.setParent(xf);
			}
		}else if (node.getNodeName().equalsIgnoreCase(prefix+":choice"))
		{
			logger.debug("Loading choice element with max occurs > 1");
			if (IsRefOnly(node,prefix,s))
			{
				logger.debug("Loading choice which includes simple ref/group element and max occurs > 1");
				String ref = XFTField.GetRefName(node,prefix,s);
				XFTReferenceField xRF = new XFTReferenceField(node,s);
				xRF.setMaxOccurs("unbounded");
				if (xRF.getName().equalsIgnoreCase(""))
				{
					xRF.setName(XMLType.CleanType(ref));
				}
				if (xRF.getXMLType().getLocalType().equalsIgnoreCase(""))
				{
					xRF.setXMLType(new XMLType(ref,s));
				}
				//xRF.setRefName(ref);
				xf = xRF;
			}else{
				XFTReferenceField xRF = new XFTReferenceField(node,s);
				xRF.setName(parent.getAbsoluteParent().getName() + "_choice");
				xRF.setXMLType(new XMLType(parent.getAbsoluteParent().getName() + "_choice",s));
				xRF.setMaxOccurs("unbounded");
//				xRF.setName(NodeUtils.GetCleanedAttributeValue(node.getAttributes(),"name",""));
//				xRF.setXMLType(new XMLType(NodeUtils.GetAttributeValue(node.getAttributes(),"name",""),s));
				XFTElement xe = new XFTElement(node,prefix,s);
				xe.setName(parent.getAbsoluteParent().getName() + "_choice");
				xe.setCreatedChild(true);
				xRF.setCreatedChild(true);
				XFTSchema.tempElements.add(xe);
				xf = xRF;
				xe.setParent(xf);
			}
		}

		if (xf != null)
		{
			xf.setFullName(header + "_" + xf.getName());
			xf.setParent(parent);
		}
		return xf;
	}


	/**
	 * @return
	 */
	public abstract XMLType getXMLType();

	/**
	 * @param type
	 */
	public abstract void setXMLType(XMLType type);

	/**
	 * @param string
	 */
	public abstract void setName(String string);

	/**
	 * if field is an extension of the parent element which was a created child.
	 * @return
	 */
	public boolean isExtension() {
		return extension;
	}

	/**
	 * if field is an extension of the parent element which was a created child.
	 * @param b
	 */
	public void setExtension(boolean b) {
		extension = b;
	}

	/**
	 *'always','never' or 'root' (only displayed when parent element is root element)
	 * @return
	 */
	public String getXmlDisplay() {
		return xmlDisplay;
	}

	/**
	 *'always','never' or 'root' (only displayed when parent element is root element)
	 * @param string
	 */
	public void setXmlDisplay(String string) {
		xmlDisplay = string;
	}



    /**
     * @return Returns the filter.
     */
    public boolean isFilter() {
        return filter;
    }
    /**
     * @param filter The filter to set.
     */
    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    /**
     * @return Returns the _finalSqlName.
     */
    public String getFinalSqlName() {
        return _finalSqlName;
    }
    /**
     * @param sqlName The _finalSqlName to set.
     */
    public void setFinalSqlName(String sqlName) {
        _finalSqlName = sqlName;
    }

	public abstract Node toXML(Document doc);
	public abstract XFTField clone(XFTElement s, boolean accessLayers);

    /**
     * @return the preventLoop
     */
    public boolean getPreventLoop() {
        return preventLoop;
    }

    /**
     * @param preventLoop the preventLoop to set
     */
    public void setPreventLoop(boolean preventLoop) {
        this.preventLoop = preventLoop;
    }

    /**
     * @return the possibleLoop
     */
    public boolean isPossibleLoop() {
        return possibleLoop;
    }

    /**
     * @param possibleLoop the possibleLoop to set
     */
    public void setPossibleLoop(boolean possibleLoop) {
        this.possibleLoop = possibleLoop;
    }
}

