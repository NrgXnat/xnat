/*
 * org.nrg.xft.schema.XFTDataField
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.schema;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter;
import org.nrg.xft.schema.design.XFTNode;
import org.nrg.xft.utils.NodeUtils;
import org.nrg.xft.utils.StringUtils;
import org.nrg.xft.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
public class XFTDataField extends XFTField {
	static org.apache.log4j.Logger logger = Logger.getLogger(XFTDataField.class);
	private String name = "";
	private XMLType type = null;
	
	private XFTDataField()
	{
		//org.nrg.xft.logger.debug("Creating Data Field");
	}
	
	/**
	 * Used to construct and populate a Data Field from the supplied node.
	 * @param node corresponding XML Node
	 * @param header header used for full name specification
	 * @param prefix of schema XMLNS
	 * @param s parent schema
	 */
	public XFTDataField(Node node, String header,String prefix,XFTSchema s, XFTNode parent)
	{
	    //this.setParent(parent);
		NamedNodeMap nnm = node.getAttributes();
		if (nnm != null)
		{
			name = NodeUtils.GetAttributeValue(nnm,"name","").intern();
			if (! NodeUtils.GetAttributeValue(nnm,"type","").equalsIgnoreCase(""))
				type = new XMLType(NodeUtils.GetAttributeValue(nnm,"type","").intern(),s);
			setUse(NodeUtils.GetAttributeValue(nnm,"use","").intern());
			setFixed(NodeUtils.GetAttributeValue(nnm,"fixed","").intern());
			this.setMaxOccurs(NodeUtils.GetAttributeValue(nnm,"maxOccurs","").intern());
			this.setMinOccurs(NodeUtils.GetAttributeValue(nnm,"minOccurs","").intern());
			
			String defaultValue = NodeUtils.GetAttributeValue(nnm,"default","").intern();
			if (! defaultValue.equalsIgnoreCase(""))
			{
				this.getSqlField().setDefaultValue(defaultValue);
			}
		}
		
		logger.debug("Creating Data Field: '"+ name + "'");
	
		setProperties(node,prefix,s);
		
		this.setParent(parent);
		this.setChildFields(XFTField.GetFields(node,header + "_"+ this.name,(XFTNode)this,prefix,s));
		validateSQLName();
	}
	
	public void setProperties(Node node,String prefix, XFTSchema s)
	{
	    Node xftField = NodeUtils.GetLevel3Child(node,XFTElement.XML_TAG_PREFIX + ":field");
		Node xftSqlField = NodeUtils.GetLevel4Child(node,XFTElement.XML_TAG_PREFIX + ":sqlField");
		Node xftTorqueField = NodeUtils.GetLevel4Child(node,XFTElement.XML_TAG_PREFIX + ":torqueField");
		Node xftRelation = NodeUtils.GetLevel4Child(node,XFTElement.XML_TAG_PREFIX + ":relation");
		
		Node xftDescription = NodeUtils.GetLevel2Child(node,prefix+":documentation");
		if (xftDescription != null)
		{
			if (xftDescription.hasChildNodes())
				this.setDescription(xftDescription.getChildNodes().item(0).getNodeValue());
		}
		
		if (xftField != null)
		{
			NamedNodeMap columnNNM = xftField.getAttributes();
			this.setDisplayName(NodeUtils.GetAttributeValue(columnNNM,"displayName",""));
			this.setExpose(NodeUtils.GetAttributeValue(columnNNM,"expose",""));
			this.setRequired(NodeUtils.GetAttributeValue(columnNNM,"required",""));
			this.setSize(NodeUtils.GetAttributeValue(columnNNM,"size",""));
			this.setUnique(NodeUtils.GetAttributeValue(columnNNM,"unique",""));
			this.setUniqueComposite(NodeUtils.GetAttributeValue(columnNNM,"uniqueComposite",""));
			this.setXmlOnly(NodeUtils.GetAttributeValue(columnNNM,"xmlOnly",""));
			this.setXmlDisplay(NodeUtils.GetAttributeValue(columnNNM,"xmlDisplay","always"));

			this.setBaseElement(NodeUtils.GetAttributeValue(columnNNM,"baseElement",""));
			this.setBaseCol(NodeUtils.GetAttributeValue(columnNNM,"baseCol",""));
			this.setLocalMap(NodeUtils.GetBooleanAttributeValue(xftField,"local_map",false));
			
			this.setFilter(NodeUtils.GetBooleanAttributeValue(xftField,"filter",false));
		}

		XFTRule xr = new XFTRule(node,prefix,s);
		this.setRule(xr);
		if ((xr.getBaseType() != "") && (this.getXMLType() == null))
		{
			this.setXMLType(new XMLType(xr.getBaseType(),s));
		}
		
		if ((xr.getMaxLength() != "") && (this.getSize() == ""))
		{
			this.setSize(xr.getMaxLength());
		}
		
		if ((xr.getLength() != "") && (this.getSize() == ""))
		{
			this.setSize(xr.getLength());
		}
		
		if (xftSqlField != null)
		{
			this.setSqlField(new XFTSqlField(xftSqlField));
		}
		
		if (xftTorqueField != null)
		{
			this.setWebAppField(new TorqueField(xftTorqueField));
		}
		
		if (xftRelation != null)
		{
			this.setRelation(new XFTRelation(xftRelation));
		}
		
		Node simpleTypeElement = NodeUtils.GetLevel1Child(node,prefix + ":simpleType");
		if (simpleTypeElement != null)
		{
			logger.debug("Loading simpleType element");
			if (simpleTypeElement.hasChildNodes())
			{
				for(int i=0;i<simpleTypeElement.getChildNodes().getLength();i++)
				{
					//level 1
					Node child1 = simpleTypeElement.getChildNodes().item(i);
					if (child1.getNodeName().equalsIgnoreCase(prefix+":list"))
					{
						setXMLType(s.getBasicDataType("string"));
						setSize("250");
					}else if (getXMLType() == null && (! hasChildren()))
					{
						//UNKNOWN TYPE
						setXMLType(s.getBasicDataType("string"));
						setSize("250");
					}
				}
			}
		}
	}
	
	/**
	 * Validates the SQL name of the field.  If it is a protected DB value (i.e. 'user') it is changed here.
	 * Performed during XFTDataField(with node) construction.
	 */
	public void validateSQLName()
	{
		if (getName().equalsIgnoreCase("user") && getSQLName().equalsIgnoreCase("user"))
		{
			if (getSqlField()==null)
			{
				setSqlField(new XFTSqlField());
			}
            if (getSqlField().getSqlName().equals(""))
                getSqlField().setSqlName("user_name");
		}

		if (getName().equalsIgnoreCase("order") && getSQLName().equalsIgnoreCase("order"))
		{
			if (getSqlField()==null)
			{
				setSqlField(new XFTSqlField());
			}
            if (getSqlField().getSqlName().equals(""))
                getSqlField().setSqlName("order1");
		}

        if (getName().equalsIgnoreCase("group") && getSQLName().equalsIgnoreCase("group"))
        {
            if (getSqlField()==null)
            {
                setSqlField(new XFTSqlField());
            }
            
            if (getSqlField().getSqlName().equals(""))
                getSqlField().setSqlName("_group");
        }
	}
    
    /**
     * If this field has a specified sql name it is returned, Else an empty string is returned.
     * @return
     */ 
    public String getXMLSqlNameValue()
    {
        String _return = "";
        if (getSqlField() != null)
        {
            _return = getSqlField().getSqlName();
        }
        
        return _return;
    }
    
    /**
     * If this field has a specified sql name, it is returned (lower case).  Else, the full name of the field is returned.
     * @return
     */
    public String getSQLName()
    {
        String temp = "";
        if (this.getXMLSqlNameValue() != "")
        {
            temp =  this.getXMLSqlNameValue().toLowerCase();
        }
        
        if (temp.equalsIgnoreCase(""))
            temp =  getFullName().toLowerCase();
        
        if (temp != null)
        {
            if (temp.length()>63)
            {
                temp = temp.substring(0,63);
            }
        }
        temp = StringUtils.CleanForSQL(temp);
        return temp;
    }
    
	
	/**
	 * local xml name of the field.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * XMLType of the field (i.e. XMLType('xs:string'))
	 * @return
	 */
	public XMLType getXMLType() {
		return type;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = StringUtils.intern(string);
	}

	/**
	 * @param string
	 */
	public void setXMLType(XMLType t) {
		type = t;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		Document doc = toXML();
		return XMLUtils.DOMToString(doc);
	}
	
	public Document toXML()
	{
		XMLWriter writer = new XMLWriter();
		Document doc =writer.getDocument();
		
		doc.appendChild(this.toXML(doc));
		
		return doc;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.schema.XFTField#toString(java.lang.String)
	 */
	public String toString(String header)
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append(header).append("XFTDataField\n");
		sb.append(header).append(this.displayAttribute()).append("name:").append(this.getName()).append("\n").append(header).append(" FullName:").append(this.getFullName()).append("\n");
		sb.append(header).append("sequence:").append(this.getSequence()).append("\n");
		if(getXMLType() != null)
			sb.append(header).append("type:").append(this.getXMLType().getFullLocalType()).append("\n");
		if(getUse() != "")
			sb.append(header).append("use:").append(this.getUse()).append("\n");
		if(getMaxOccurs() != "")
			sb.append(header).append("maxOccurs:").append(this.getMaxOccurs()).append("\n");
		if(getMinOccurs() != "")
			sb.append(header).append("minOccurs:").append(this.getMinOccurs()).append("\n");
		if(getSize() != "")
			sb.append(header).append("size:").append(this.getSize()).append("\n");
		if(getDescription() != "")
			sb.append(header).append("description:").append(this.getDescription()).append("\n");
		if(getDisplayName() != "")
			sb.append(header).append("displayName:").append(this.getDisplayName()).append("\n");
		if(getFixed() != "")
			sb.append(header).append("fixed:").append(this.getFixed()).append("\n");
		if(getRequired() != "")
			sb.append(header).append("required:").append(this.getRequired()).append("\n");
		if(getExpose() != "")
			sb.append(header).append("expose:").append(this.getExpose()).append("\n");
		if(getUnique() != "")
			sb.append(header).append("unique:").append(this.getUnique()).append("\n");
		if(getUniqueComposite() != "")
			sb.append(header).append("uniqueComposite:").append(this.getUniqueComposite()).append("\n");
		
		if (getRule() != null)
			sb.append("Rule:").append(this.getRule().toString(header + "\t"));
		if (getSqlField() != null)
			sb.append("SQLField:").append(this.getSqlField().toString(header + "\t"));
		if (getWebAppField() != null)
			sb.append("WebAppField:").append(this.getWebAppField().toString(header + "\t"));
		if (getRelation() != null)
			sb.append("Relation:").append(this.getRelation().toString(header + "\t"));
			
		Iterator i = this.getSortedFields().iterator();
		while (i.hasNext())
		{
			sb.append(((XFTField)i.next()).toString(header + "\t"));
		}	
		
		return sb.toString();
	}

	
	public Node toXML(Document doc)
	{
		Node main = doc.createElement("data-field");
		if (getXMLType()!=null)
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"type",this.getXMLType().getFullForeignType()));
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"name",this.getName()));
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"full-name",this.getFullName()));
		
		Node props = doc.createElement("properties");
		main.appendChild(props);
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"use",getUse()));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"maxOccurs",this.getMaxOccurs()));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"minOccurs",this.getMinOccurs()));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"sequence",Integer.toString(this.getSequence())));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"size",this.getSize()));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"description",this.getDescription()));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"displayName",this.getDisplayName()));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"fixed",this.getFixed()));
		if(getRequired() != "")
			props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"required",this.getRequired()));
		if(getExpose() != "")
			props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"expose",this.getExpose()));
		if(getUnique() != "")
			props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"unique",this.getUnique()));
		if(getUniqueComposite() != "")
			props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"uniqueComposite",this.getUniqueComposite()));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"local-map",org.nrg.xft.utils.StringUtils.ToString(this.isLocalMap())));
		
		if (getRule() != null)
			main.appendChild(this.getRule().toXML(doc));
		if (getSqlField() != null)
			main.appendChild(this.getSqlField().toXML(doc));
		if (getWebAppField() != null)
			main.appendChild(this.getWebAppField().toXML(doc));
		
		if (getRelation() != null)
			main.appendChild(this.getRelation().toXML(doc));
			
		Iterator i = this.getSortedFields().iterator();
		while (i.hasNext())
		{
			main.appendChild((((XFTField)i.next()).toXML(doc)));
		}	
				
		return main;
	}

	/**
	 * Constructs a new empty primary key field.  AutoIncrement = true, PrimaryKey= true, XMLType = integer
	 * @param prefix XMLNS prefix
	 * @param s parent Schema
	 * @return
	 */
	public static XFTDataField GetEmptyKey(String prefix,XFTSchema s)
	{
		XFTDataField field = new XFTDataField();
		XFTSqlField sql = new XFTSqlField();
		sql.setAutoIncrement("true");
		sql.setPrimaryKey("true");
		field.setSqlField(sql);
		field.setXMLType(s.getBasicDataType("integer"));
		
		return field;
	}

	/**
	 * Constructs an empty data field.
	 * @return
	 */
	public static XFTDataField GetEmptyField()
	{
		XFTDataField field = new XFTDataField();
		XFTSqlField sql = new XFTSqlField();
		field.setSqlField(sql);
		
		return field;
	}
	
	public XFTField clone(XFTElement e, boolean accessLayers)
	{
		XFTDataField clone = GetEmptyField();
		clone.setName(name);
		clone.setFullName(getFullName());
		clone.setUse(getUse());
		clone.setXMLType(type);
		clone.setMaxOccurs(getMaxOccurs());
		clone.setMinOccurs(getMinOccurs());
		clone.setFixed(getFixed());
		clone.setDescription(getDescription());
		
		clone.setDisplayName(getDisplayName());
		clone.setExpose(getExpose());
		clone.setRequired(getRequired());
		clone.setSize(getSize());
		clone.setUnique(getUnique());
		clone.setUniqueComposite(getUniqueComposite());
		clone.setXmlOnly(getXmlOnly());
		clone.setXmlDisplay(getXmlDisplay());

		clone.setBaseElement(getBaseElement());
		clone.setBaseCol(getBaseCol());
	
		XFTRule xr = getRule();
		clone.setRule(xr);
		if ((xr.getBaseType() != "") && (clone.getXMLType() == null))
		{
			clone.setXMLType(new XMLType(xr.getBaseType(),e.getSchema()));
		}
		
		if ((xr.getMaxLength() != "") && (clone.getSize() == ""))
		{
			clone.setSize(xr.getMaxLength());
		}
		
		if ((xr.getLength() != "") && (clone.getSize() == ""))
		{
			clone.setSize(xr.getLength());
		}
		
		if (getSqlField() != null)
		{
			clone.setSqlField((XFTSqlField)getSqlField().clone(e));
		}
		
		if (getWebAppField() != null)
		{
			clone.setWebAppField((XFTWebAppField)getWebAppField().clone(e));
		}
		
		if (getRelation() != null)
		{
			clone.setRelation((XFTRelation)getRelation().clone(e));
		}
		
		if (accessLayers)
		{
			int counter = 2002;
			Iterator fields = this.getSortedFields().iterator();
			while (fields.hasNext())
			{
				XFTField field = (XFTField)fields.next();
				if (field.isLocalMap())
				{
					XFTReferenceField ref = XFTReferenceField.GetEmptyRef();
					ref.setName(field.getName());
					ref.setXMLType(e.getType());
					ref.setFullName(field.getName());
					ref.setMinOccurs("0");
					ref.getRelation().setOnDelete("SET NULL");
					ref.setParent(clone);
					ref.setSequence(counter++);
					ref.getSqlField().setPrimaryKey(field.getSqlField().getPrimaryKey());
					clone.addChildField(ref);
				}else
				{
					clone.addChildField((XFTField)field.clone(e,true));
				}
			}
		}
		
		return clone;
	}
	
	public String getId()
	{
		return this.getFullName();
	}
}

