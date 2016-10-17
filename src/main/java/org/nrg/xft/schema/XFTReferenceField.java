/*
 * core: org.nrg.xft.schema.XFTReferenceField
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema;

import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter;
import org.nrg.xft.utils.NodeUtils;
import org.nrg.xft.utils.XMLUtils;
import org.nrg.xft.utils.XftStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Iterator;
public class XFTReferenceField extends XFTField {
	private String name = "";
	private XFTElement refElement = null;
	private XMLType type = null;
	

	private boolean createdChild = false;
	private boolean childXMLNode = true;
	private boolean isInLineRepeaterElement = false;
	
	/**
	 * No population
	 */
	private XFTReferenceField()
	{
		//logger.debug("Creating Reference Field");
	}
	
	/**
	 * If the node has a defined type, then the type is used to find the referenced element.
	 * If the referenced node is only a reference to another node, then the second node becomes
	 * the referenced node (takes out the middle man).  If the node has no declared type, then it
	 * uses the ref attribute to specify the referenced element.  Finally, it calls the setProperties()
	 * method to set the properties according the the passed in node.
	 * 
	 * @param node Node to use for Reference Field creation
	 * @param s parent schema
	 */
	public XFTReferenceField(Node node,XFTSchema s)
	{
		NamedNodeMap nnm = node.getAttributes();
		boolean hasDeclaredType = false;
		
		if (NodeUtils.HasAttribute(node,"type"))
		{
			hasDeclaredType=true;
		}
		
		if (nnm != null)
		{
			if (hasDeclaredType)
			{
				name = NodeUtils.GetCleanedAttributeValue(nnm,"name","");
				String t = NodeUtils.GetAttributeValue(nnm,"type","");
				XMLType xmlType = new XMLType(t,s);
				Node refType=NodeWrapper.FindNode(t,s);
				if (refType != null)
				{
					//REFERENCE TO ANOTHER ELEMENT
					Node refNode = NodeUtils.GetGroupNode( refType);
					if (refNode == null)
					{
						//basic named complex type
						name = NodeUtils.GetCleanedAttributeValue(nnm,"name","");
						type = xmlType;
					}else
					{
						//complex type is only a containor for ref to group
						name = NodeUtils.GetCleanedAttributeValue(nnm,"name","");
						type = new XMLType(NodeUtils.GetAttributeValue(refNode.getAttributes(),"ref",""),s);
					}
				}else
				{
					//standard data type element with max occurs > 1
					name = NodeUtils.GetCleanedAttributeValue(nnm,"name","");
					type = new XMLType(name,s);
				}
			}else
			{
				if (! NodeUtils.GetAttributeValue(nnm,"ref","").equalsIgnoreCase(""))
				{
					if (! NodeUtils.GetAttributeValue(nnm,"name","").equalsIgnoreCase(""))
					{ 
						name = NodeUtils.GetCleanedAttributeValue(nnm,"name","");
					}else{
						name = NodeUtils.GetCleanedAttributeValue(nnm,"ref","");
					}
					type = new XMLType(NodeUtils.GetAttributeValue(nnm,"ref",""),s);
				}else
				{
					name = NodeUtils.GetCleanedAttributeValue(nnm,"name","");
					type = new XMLType(NodeUtils.GetAttributeValue(nnm,"name",""),s);
				}
			}
			
		}
		logger.debug("Creating Reference Field:'" + name +"'");
		
		setProperties(node,s);		
	}
	
	/**
	 * Sets the field's properties according the attributes of the given field.
	 * @param node
	 * @param s
	 */
	public void setProperties(Node node,XFTSchema s)
	{
		NamedNodeMap nnm = node.getAttributes();
		setUse(NodeUtils.GetAttributeValue(nnm,"use",""));
		setFixed(NodeUtils.GetAttributeValue(nnm,"fixed",""));
		this.setMaxOccurs(NodeUtils.GetAttributeValue(nnm,"maxOccurs",""));
		this.setMinOccurs(NodeUtils.GetAttributeValue(nnm,"minOccurs",""));

		if (this.getMaxOccurs().equalsIgnoreCase("unbounded"))
		{
			Node sqlElement = NodeUtils.GetLevel4Child(node,XFTElement.XML_TAG_PREFIX + ":sqlElement");
			Node torqueElement = NodeUtils.GetLevel4Child(node,XFTElement.XML_TAG_PREFIX + ":torqueElement");
			if (sqlElement != null)
			{
				XFTSqlField xSQLE = new XFTSqlField(sqlElement);
				this.setSqlField(xSQLE);
			}
			if (torqueElement != null)
			{
				XFTWebAppField xWebE = new TorqueField(torqueElement);			
				this.setWebAppField(xWebE);
			}
		}

		Node XFTField = NodeUtils.GetLevel3Child(node,XFTElement.XML_TAG_PREFIX + ":field");
		Node XFTSqlField = NodeUtils.GetLevel4Child(node,XFTElement.XML_TAG_PREFIX + ":sqlField");
		Node XFTTorqueField = NodeUtils.GetLevel4Child(node,XFTElement.XML_TAG_PREFIX + ":torqueField");
		Node XFTRelation = NodeUtils.GetLevel4Child(node,XFTElement.XML_TAG_PREFIX + ":relation");

		Node XFTDescription = NodeUtils.GetLevel2Child(node,s.getXMLNS()+":documentation");
		if (XFTDescription != null)
		{
			this.setDescription(XFTDescription.getNodeValue());
		}

		XFTRule xr = new XFTRule(node,s.getXMLNS(),s);
		this.setRule(xr);

		if (XFTField != null)
		{
			NamedNodeMap columnNNM = XFTField.getAttributes();
			this.setDisplayName(NodeUtils.GetAttributeValue(columnNNM,"displayName",""));
			this.setExpose(NodeUtils.GetAttributeValue(columnNNM,"expose",""));
			this.setRequired(NodeUtils.GetAttributeValue(columnNNM,"required",""));
			this.setSize(NodeUtils.GetAttributeValue(columnNNM,"size",""));
			this.setUnique(NodeUtils.GetAttributeValue(columnNNM,"unique",""));
			this.setUniqueComposite(NodeUtils.GetAttributeValue(columnNNM,"uniqueComposite",""));
			this.setXmlOnly(NodeUtils.GetAttributeValue(columnNNM,"xmlOnly",""));
			this.setXmlDisplay(NodeUtils.GetAttributeValue(columnNNM,"xmlDisplay","always"));
			this.setOnlyRoot(NodeUtils.GetBooleanAttributeValue(XFTField,"rootOnly",false));
            this.setPreventLoop(NodeUtils.GetBooleanAttributeValue(XFTField,"preventLoop",false));
            this.setPossibleLoop(NodeUtils.GetBooleanAttributeValue(XFTField,"possibleLoop",false));

//			this.setBaseElement(NodeUtils.GetAttributeValue(columnNNM,"baseElement",""));
//			this.setBaseCol(NodeUtils.GetAttributeValue(columnNNM,"baseCol",""));
			this.setLocalMap(NodeUtils.GetBooleanAttributeValue(XFTField,"local_map",false));
		}

		if (XFTSqlField != null)
		{
			this.setSqlField(new XFTSqlField(XFTSqlField));
		}

		if (XFTTorqueField != null)
		{
			this.setWebAppField(new TorqueField(XFTTorqueField));
		}

		this.setOnDelete("SET NULL");
		if (XFTRelation != null)
		{
			this.setRelation(new XFTRelation(XFTRelation));
		}
	}
	/**
	 * Gets the refElement using the XMLType
	 * @return Returns the XFTElement refElement
	 */
	public XFTElement getRefElement() throws org.nrg.xft.exception.ElementNotFoundException,org.nrg.xft.exception.XFTInitException{
		if (refElement == null)
		{
			try {
				refElement = XFTMetaManager.FindElement(getXMLType());
			} catch (org.nrg.xft.exception.ElementNotFoundException e) {
				throw e;
			}
		}
		return refElement;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.schema.XFTField#getName()
	 */
	public String getName()
	{
		return name;
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
	
	public Node toXML(Document doc)
	{
		Node main = doc.createElement("reference-field");
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
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "child-xml-node", XftStringUtils.ToString(this.childXMLNode)));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "created-child", XftStringUtils.ToString(this.createdChild)));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "in-line-repeater", XftStringUtils.ToString(this.isInLineRepeaterElement)));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "local-map", XftStringUtils.ToString(this.isLocalMap())));
		
		
		main.appendChild(this.getRule().toXML(doc));
		main.appendChild(this.getSqlField().toXML(doc));
		main.appendChild(this.getWebAppField().toXML(doc));
		
		return main;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.schema.XFTField#toString(java.lang.String)
	 */
	public String toString(String header)
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append(header).append("XFTReferenceField\n");
		sb.append(header).append(this.displayAttribute()).append(" name:").append(this.getName()).append(" FullName:").append(this.getFullName()).append("\n");
		sb.append(header).append("sequence:").append(this.getSequence()).append("\n");
		if (getXMLType() != null)
			sb.append(header).append("type:").append(this.getXMLType().getFullLocalType()).append("\n");
		if (getUse() != "")
			sb.append(header).append("use:").append(this.getUse()).append("\n");
		if (getMaxOccurs() != "")
			sb.append(header).append("maxOccurs:").append(this.getMaxOccurs()).append("\n");
		if (getMinOccurs() != "")
			sb.append(header).append("minOccurs:").append(this.getMinOccurs()).append("\n");
		if (getSize() != "")
			sb.append(header).append("size:").append(this.getSize()).append("\n");
		if (getDescription() != "")
			sb.append(header).append("description:").append(this.getDescription()).append("\n");
		if (getDisplayName() != "")
			sb.append(header).append("displayName:").append(this.getDisplayName()).append("\n");
		if (getFixed() != "")
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
		return sb.toString();
	}

	/**
	 * Get an empty reference (onDelete defaults to cascade)
	 * @return Returns an empty reference
	 */
	public static XFTReferenceField GetEmptyRef()
	{
		XFTReferenceField field = new XFTReferenceField();
		XFTRelation rel = new XFTRelation();
		rel.setOnDelete("SET NULL");
		field.setRelation(rel);
		return field;
	}
	
	/**
	 * Gets the XMLType for the referenced element
	 * @return Returns the XMLType
	 */
	public XMLType getXMLType() {
		return type;
	}

	/**
	 * Gets the XMLType for the referenced element
	 * @param type
	 */
	public void setXMLType(XMLType type) {
		this.type = type;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * Whether the referenced element will have its own XML DOM Node.
	 * @return Returns whether the referenced element will have its own XML DOM Node
	 */
	public boolean isChildXMLNode() {
		return childXMLNode;
	}

	/**
	 * Whether the referenced element will have its own XML DOM Node.
	 * @param b
	 */
	public void setChildXMLNode(boolean b) {
		childXMLNode = b;
	}

	/**
	 * Whether the referenced element is a repeating basic data type.
	 * @return Returns whether the referenced element is a repeating basic data type
	 */
	public boolean isInLineRepeaterElement() {
		return isInLineRepeaterElement;
	}

	/**
	 * Whether the referenced element is a repeating basic data type.
	 * @param b
	 */
	public void setInLineRepeaterElement(boolean b) {
		isInLineRepeaterElement = b;
	}

	/**
	 * if this field references a created Child Element then true, else false.
	 * @return Returns whether the field references a created Child Element
	 */
	public boolean isCreatedChild() {
		return createdChild;
	}

	/**
	 * if this field references a created Child Element then true, else false.
	 * @param b
	 */
	public void setCreatedChild(boolean b) {
		createdChild = b;
	}
	
	public XFTField clone(XFTElement e, boolean accessLayers)
	{
		XFTReferenceField clone = XFTReferenceField.GetEmptyRef();
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

