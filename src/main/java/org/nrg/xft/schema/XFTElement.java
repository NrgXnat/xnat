/*
 * core: org.nrg.xft.schema.XFTElement
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema;
import org.apache.log4j.Logger;
import org.nrg.xft.XFTItem;
import org.nrg.xft.meta.XFTMetaElement;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter;
import org.nrg.xft.schema.design.XFTNode;
import org.nrg.xft.utils.NodeUtils;
import org.nrg.xft.utils.XMLUtils;
import org.nrg.xft.utils.XftStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.*;
/**
 * @author Tim
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class XFTElement extends XFTNode{
	private static final String TRUE = "true";
	private static final String STRING = "string";
	private static final String EXTENSION = ":extension";
	private static final String BASE = "base";
	private static final String XS_RESTRICTION = "xs:restriction";
	private static final String XS_ANY_URI = "xs:anyURI";
	private static final String NONE = "none";
	private static final String ANY_URI = "anyURI";
	private static final String TYPE = "type";
	private static final String FALSE = "false";
	private static final String SKIP_SQL = "skipSql";
	private static final String QUARANTINE = "quarantine";
	private static final String STORE_HISTORY = "storeHistory";
	private static final String IGNORE_WARNINGS = "ignoreWarnings";
	private static final String MATCH_BY_VALUES = "matchByValues";
	private static final String _ADDIN = "addin";
	private static final String ABSTRACT = "abstract";
	private static final String XDAT_TORQUE_ELEMENT = "xdat:torqueElement";
	private static final String XDAT_SQL_ELEMENT = "xdat:sqlElement";
	private static final String XDAT_ELEMENT = "xdat:element";
	private static final String XS_DOCUMENTATION = "xs:documentation";
	private static final String XDAT_DESCRIPTION = "xdat:description";
	private static final String NAME = "name";
	static org.apache.log4j.Logger logger = Logger.getLogger(XFTElement.class);
	public final static String XML_TAG_PREFIX="xdat";
	private XFTMetaElement metaElement = null;
	private String name = "";
	private String code = "";
	private String briefDescription = "";
	private String fullDescription = "";
	private String xsDescription="";
	private Hashtable fields = new Hashtable();
	private XFTWebAppElement webAppElement = null;
	private XFTSqlElement sqlElement = null;
	private int sequence = 100;
	private XMLType type = null;

	private boolean matchByValues = false;
	private boolean extension = false;
	private XMLType extensionType = null;

	private boolean isANoChildElement = false;
	private boolean isExtended = false;
	private boolean hasExtensionElement = false;
	private boolean createdChild = false;
	private boolean skipSQL = false;
	private Boolean quarantine = null;
	private boolean preLoad = false;
	private boolean hasBase = false;
//	private ArrayList impliedParents = null;
//	private ArrayList assessorRefs = null;

	private String schemaPrefix = "xs";
	private String addin = "";

    private boolean isAbstract=false;
	private XFTSchema schema = null;

	private boolean ignoreWarnings = false;
	private boolean storeHistory = true;
	/**
	 * Creates an empty XFTElement for the given XFTSchema.  Its webAppElement and sqlElement are
	 * instanciated with default constructors.
	 * @param s
	 */
	public XFTElement(XFTSchema s)
	{
		webAppElement = new TorqueElement();
		sqlElement = new XFTSqlElement();
		schema=s;
	}

	/**
	 * Constructs and populates a XFTElement based on the information stored in the XML DOM Node.
	 *
	 * <BR><BR>This is the primary means of creating a XFTElement.  All of the XFTElement's properties,
	 * will be populated by the values in the node.  It will populate any child XFTFields which the element
	 * contains.
	 *
	 * @param node XML DOM Node
	 * @param prefix XMLNS prefix of standard data types
	 * @param s XFTSchema (parent)
	 */
	public XFTElement(Node node, String prefix,XFTSchema s)
	{
		this(node,prefix,s,null);
	}

	/**
	 * Constructs and populates a XFTElement based on the information stored in the XML DOM Node.
	 *
	 * <BR><BR>This is the primary means of creating a XFTElement.  All of the XFTElement's properties,
	 * will be populated by the values in the node.  It will populate any child XFTFields which the element
	 * contains.
	 *
	 * @param node XML DOM Node
	 * @param prefix XMLNS prefix of standard data types
	 * @param s XFTSchema (parent)
	 */
	public XFTElement(Node node, String prefix,XFTSchema s, String header)
	{
		try {
			schema=s;
			schemaPrefix = prefix;
			if (NodeUtils.NodeHasName(node))
			{
				name = node.getAttributes().getNamedItem(NAME).getNodeValue();
			}
			if (header!=null)
			{
			    name = header + "_" + name;
			}
			logger.debug("Loading xml element '" + name + "'...");
			//XFT:description
			Node docInfo = NodeUtils.GetLevel4Child(node,XDAT_DESCRIPTION);
			if (docInfo != null)
			{
				fillDescription(docInfo.getAttributes());
			}
			
			Node schemaDoc=NodeUtils.GetLevel2Child(node,XS_DOCUMENTATION);
			if(schemaDoc!=null){
				this.setXsDescription(NodeUtils.GetNodeText(schemaDoc));
			}

			Node coreElement = NodeUtils.GetLevel3Child(node,XDAT_ELEMENT);
			Node sqlElement = NodeUtils.GetLevel4Child(node,XDAT_SQL_ELEMENT);
			Node torqueElement = NodeUtils.GetLevel4Child(node,XDAT_TORQUE_ELEMENT);
			if (coreElement != null)
			{
                this.setAbstract(NodeUtils.GetBooleanAttributeValue(coreElement,ABSTRACT,false));
				this.setAddin(NodeUtils.GetAttributeValue(coreElement,_ADDIN,""));
				this.setMatchByValues(NodeUtils.GetBooleanAttributeValue(coreElement,MATCH_BY_VALUES,false));
				this.setIgnoreWarnings(NodeUtils.GetBooleanAttributeValue(coreElement,IGNORE_WARNINGS,false));
				this.setStoreHistory(NodeUtils.GetBooleanAttributeValue(coreElement,STORE_HISTORY,true));
				if (NodeUtils.HasAttribute(coreElement,QUARANTINE))
				    this.setQuarantineSetting(NodeUtils.GetBooleanAttributeValue(coreElement,QUARANTINE,false));
				this.setSkipSQL(NodeUtils.GetAttributeValue(coreElement,SKIP_SQL,FALSE));
				this.setDisplayIdentifiers(XftStringUtils.CommaDelimitedStringToArrayList(NodeUtils.GetAttributeValue(coreElement, "displayIdentifiers", "")));
			}
			if (sqlElement != null)
			{
				XFTSqlElement xSQLE = new XFTSqlElement(sqlElement.getAttributes());
				this.setSqlElement(xSQLE);
			}
			if (torqueElement != null)
			{
				XFTWebAppElement xWebE = new TorqueElement(torqueElement.getAttributes());
				this.setWebAppElement(xWebE);
			}

			//GetFields
			fields = XFTField.GetFields(node,"",this,prefix,s);


			if (node.getAttributes().getNamedItem(TYPE) != null)
			{
				XFTField xf = null;
				xf = new XFTDataField(node,"",prefix,s,this);
                if (NodeUtils.GetAttributeValue(node, TYPE, NONE).indexOf(ANY_URI)!=-1){
                    xf.getRule().setBaseType(XS_ANY_URI);
                }
				xf.setMaxOccurs("");
				xf.setMinOccurs("");
				xf.setFullName(xf.getName());
				xf.setParent(this);
				fields.put(xf.getName(),xf);
			}

			if (NodeUtils.GetLevel2Child(node,XS_RESTRICTION) != null)
			{
                XFTField xf = null;
                xf = new XFTDataField(node,"",prefix,s,this);
                if (NodeUtils.GetAttributeValue(node, BASE, NONE).indexOf(ANY_URI)!=-1){
                    xf.getRule().setBaseType(XS_ANY_URI);
                }
                xf.setMaxOccurs("");
                xf.setMinOccurs("");
                xf.setFullName(xf.getName());
                xf.setParent(this);
                fields.put(xf.getName(),xf);
//				XFTField xf = null;
//				xf = XFTField.AddField(node,"",this,prefix,s,false);
//				xf.setMaxOccurs("");
//				xf.setMinOccurs("");
//				xf.setFullName(xf.getName());
//				xf.setParent(this);
//				fields.put(xf.getName(),xf);
			}

			Node extensionElement = NodeUtils.GetLevel3Child(node,prefix + EXTENSION);
			if (extensionElement == null)
			{
				extensionElement = NodeUtils.GetLevel2Child(node,prefix + EXTENSION);
			}
			if (extensionElement != null)
			{
				//IS AN EXTENSION
				XFTField xf = null;
				String uncleanedType = NodeUtils.GetAttributeValue(extensionElement.getAttributes(),BASE,NONE);

				Node refType=NodeWrapper.FindNode(uncleanedType,s);
				if (refType!=null)
				{
				   //NAMED TYPE WITH MAX OCCURS
					if (XFTField.IsRefOnly(refType,prefix,s))
					{
						this.setExtension(true);
						String ref = XFTField.GetRefName(refType,prefix,s);
						Node refNode = NodeWrapper.FindNode(ref,s);
						if (refNode != null)
						{
							XFTReferenceField xRF = new XFTReferenceField(refNode,s);
							xRF.setName(XMLType.CleanType(uncleanedType));
							xRF.setChildXMLNode(false);
							xRF.setPrimaryKey(TRUE);
							this.setIdMethod(NONE);
							xRF.setOnDelete("SET NULL");
//							xRF.setXMLType(new XMLType(uncleanedType,s));
							xf = xRF;
							this.setExtensionType(xf.getXMLType());
						}else{
							XFTReferenceField xRF = new XFTReferenceField(node,s);
							xRF.setName(XMLType.CleanType(uncleanedType));
							xRF.setChildXMLNode(false);
							xRF.setPrimaryKey(TRUE);
							this.setIdMethod(NONE);
							xRF.setOnDelete("SET NULL");
//							xRF.setXMLType(new XMLType(uncleanedType,s));
							xf = xRF;
							this.setExtensionType(xf.getXMLType());
						}
					}else if (refType.getNodeName().equalsIgnoreCase(prefix+":simpleType"))
					{
						if (refType.hasChildNodes())
						{
							for(int i=0;i<refType.getChildNodes().getLength();i++)
							{
								//level 1
								Node child1 = refType.getChildNodes().item(i);
								if (child1.getNodeName().equalsIgnoreCase(prefix+":list"))
								{
									XFTDataField temp = XFTDataField.GetEmptyField();
									temp.setXMLType(s.getBasicDataType(STRING));
									temp.setSize("250");
									temp.setName(NodeUtils.GetAttributeValue(refType,NAME,""));
									temp.setFullName(temp.getName());
									temp.setExtension(true);

									xf = temp;
								}else if(child1.getNodeName().equalsIgnoreCase(prefix+":restriction"))
								{
									xf = new XFTDataField(refType,"",prefix,s,this);
									xf.setFullName(xf.getName());
								}else if (child1.getNodeName().equalsIgnoreCase(prefix+":union"))
                                {
                                    XFTDataField temp = XFTDataField.GetEmptyField();
                                    temp.setXMLType(s.getBasicDataType(STRING));
                                    temp.setSize("250");
                                    temp.setName(NodeUtils.GetAttributeValue(refType,NAME,""));
                                    temp.setFullName(temp.getName());
                                    temp.setExtension(true);

                                    xf = temp;
                                }
							}
						}
					}else
					{
						XFTReferenceField xRF = new XFTReferenceField(node,s);
						xRF.setName(XMLType.CleanType(uncleanedType));
						xRF.setXMLType(new XMLType(uncleanedType,s));
						xRF.setChildXMLNode(false);
						xRF.setPrimaryKey(TRUE);
						this.setIdMethod(NONE);
						xRF.setOnDelete("CASCADE");
						xf = xRF;
						this.setExtension(true);
						this.setExtensionType(xRF.getXMLType());
					}
				}else
				{
					   //STANDARD DATA TYPE
					XFTDataField data = XFTDataField.GetEmptyField();
					data.setName(node.getAttributes().getNamedItem(NAME).getNodeValue());
					data.setXMLType(new XMLType(NodeUtils.GetAttributeValue(extensionElement,BASE,prefix + ":string"),s));
					data.setExtension(true);
					data.setProperties(node,prefix,s);
					xf = data;
					this.setANoChildElement(true);
				}
				xf.setMaxOccurs("");
				xf.setMinOccurs(NodeUtils.GetAttributeValue(node,"minOccurs",""));
				xf.setFullName(xf.getName());
				xf.setParent(this);
				fields.put(xf.getName(),xf);

				if (extensionElement.hasChildNodes())
				{
					Hashtable temp = XFTField.GetFields(extensionElement,"",this,prefix,s);
					java.util.Enumeration enumer = temp.keys();
					while (enumer.hasMoreElements())
					{
						String st = (String)enumer.nextElement();
						XFTField tempXF = (XFTField)temp.get(st);
						fields.put(st,tempXF);
					}
				}
			}

			if (! NodeUtils.GetAttributeValue(node,"mixed",NONE).equalsIgnoreCase(NONE))
			{
				XFTDataField data = XFTDataField.GetEmptyField();
				data.setName(node.getAttributes().getNamedItem(NAME).getNodeValue());
				data.setXMLType(s.getBasicDataType(STRING));
				data.setSize("255");
				data.setMaxOccurs("");
				data.setMinOccurs("");
				data.setFullName(data.getName());
				data.setParent(this);
				fields.put(data.getName(),data);
			}

			setType(getName(),s);
		} catch (RuntimeException e) {
		    logger.error(e);
		    e.printStackTrace();
			RuntimeException e1 = new RuntimeException("Error in Element: " +node.getAttributes().getNamedItem(NAME).getNodeValue() + "\n" + e.getMessage());
			throw e1;
		}
	}

	/**
	 * sets a new type : new XMLType(name,s)
	 * @param name
	 * @param s
	 */
	private void setType(String name,XFTSchema s)
	{
		type = new XMLType(name,s);
	}

	/**
	 *  schema target namespace prefix + local xml element name.
	 * @return Returns the XMLType of the element
	 */
	public XMLType getType()
	{
		return type;
	}

	/**
	 * sets code, briefDescription and fullDescription from nnm attributes.
	 * @param nnm
	 */
	public void fillDescription(NamedNodeMap nnm)
	{
		code = NodeUtils.GetAttributeValue(nnm,"code","");
		briefDescription = NodeUtils.GetAttributeValue(nnm,"briefdescription","");
		fullDescription = NodeUtils.GetAttributeValue(nnm,"fulldescription","");
	}
	/**
	 * @return Returns the String brief description for the element
	 */
	public String getBriefDescription() {
		return briefDescription;
	}

	/**
	 * @return Returns the String code for this element
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return Returns String full description of this element
	 */
	public String getFullDescription() {
		return fullDescription;
	}

	/**
	 * @return Returns String name of this element
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param string
	 */
	public void setBriefDescription(String string) {
		briefDescription = string;
	}

	/**
	 * @param string
	 */
	public void setCode(String string) {
		code = string;
	}

	/**
	 * @param string
	 */
	public void setFullDescription(String string) {
		fullDescription = string;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
		setType(getName(),this.schema);
	}

	/**
	 * key = XFTField.getName(), value = XFTField
	 * @return Returns Hastable of the fields for this element
	 */
	public Hashtable getFields() {
		return fields;
	}

	/**
	 * puts the XFTField into the hashtable of fields and sets this element as the Field's parent.
	 * @param xf
	 */
	public void addField(XFTField xf)
	{
		fields.put(xf.getName(),xf);
		xf.setParent(this);
	}

	/**
	 * native: auto increment, none: non-auto increment
	 * @param s
	 */
	public void setIdMethod(String s)
	{
		if (this.getWebAppElement() == null)
		{
			setWebAppElement(new TorqueElement());
		}
		getWebAppElement().setIdMethod(s);
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

	/**
	 * @param header
	 * @return Returns String representation of the element
	 */
	public String toString(String header)
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append("\n\n\n").append(header).append("------------- ELEMENT --------------------\n");
		sb.append(header).append("XFTElement\n");
		sb.append(header).append("------------- ELEMENT --------------------\n");
		sb.append(header).append("name:").append(this.getName()).append("\n");
		sb.append(header).append("XmlType:").append(this.getType().getFullForeignType());
		if (getCode() != "")
			sb.append(header).append("code:").append(this.getCode()).append("\n");
		if (getBriefDescription() != "")
			sb.append(header).append("briefDescription:").append(this.getBriefDescription()).append("\n");
		if (getFullDescription() != "")
			sb.append(header).append("fullDescription:").append(this.getFullDescription()).append("\n");
		if (getSqlElement() != null)
			sb.append(getSqlElement().toString(header + "\t"));
		if (getWebAppElement() != null)
			sb.append(getWebAppElement().toString(header + "\t"));

		Iterator i = this.getSortedFields().iterator();
		while (i.hasNext())
		{
			sb.append("\n ------------- FIELD --------------------\n");
			sb.append(((XFTField)i.next()).toString(header + "\t"));
		}
		return sb.toString();
	}

	public Node toXML(Document doc)
	{
		Node main = doc.createElement("element");
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,NAME,this.getName()));
		if (getType()!=null)
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,TYPE,this.getType().getFullForeignType()));

		Node props = doc.createElement("properties");
		main.appendChild(props);

		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"code",this.getCode()));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"brief",this.getBriefDescription()));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"full",this.getFullDescription()));

		if (getAddin()!=null)
			props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,_ADDIN,this.getAddin()));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "created-child", XftStringUtils.ToString(this.createdChild)));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "extension", XftStringUtils.ToString(this.extension)));
		if (extensionType!=null)
			props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"extension-type",this.extensionType.getFullForeignType()));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "has-extension-element", XftStringUtils.ToString(this.hasExtensionElement)));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "is-a-no-child-element", XftStringUtils.ToString(this.isANoChildElement)));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "is-extended", XftStringUtils.ToString(this.isExtended)));
		props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "skip-sql", XftStringUtils.ToString(this.skipSQL)));

		if (getSqlElement()!=null)
			props.appendChild(getSqlElement().toXML(doc));
		if (getWebAppElement() != null)
			props.appendChild(getWebAppElement().toXML(doc));

		Iterator i = this.getSortedFields().iterator();
		while (i.hasNext())
		{
			main.appendChild(((XFTField)i.next()).toXML(doc));
		}
		return main;
	}

	/**
	 * Object which stores specialized info about the element.
	 * @return Returns XFTSqlElement which stores specialized info about the element
	 */
	public XFTSqlElement getSqlElement() {
		return sqlElement;
	}

	/**
	 * Object which stores specialized info about the element.
	 * @return Returns XFTWebAppElement which stores specialized info about the element
	 */
	public XFTWebAppElement getWebAppElement() {
		return webAppElement;
	}

	/**
	 * @param element
	 */
	public void setSqlElement(XFTSqlElement element) {
		sqlElement = element;
	}

	/**
	 * @param element
	 */
	public void setWebAppElement(XFTWebAppElement element) {
		webAppElement = element;
	}

	/**
	 * sequence relative to other elements in the parent schema.
	 * @return Returns int sequence relative to other elements in the parent schema
	 */
	public int getSequence() {
		return sequence;
	}

	/**
	 * @param i
	 */
	public void setSequence(int i) {
		sequence = i;
	}

	/**
	 * return an ArrayList of the XFTFields in order by sequence
	 * @return ArrayList of XFTFields
	 */
	public ArrayList getSortedFields()
	{
		ArrayList temp = new ArrayList();
		temp.addAll(getFields().values());
		Collections.sort(temp,XFTField.SequenceComparator);
		return temp;
	}

	public final static Comparator SequenceComparator = new Comparator() {
	  public int compare(Object mr1, Object mr2) throws ClassCastException {
		  try{
			int value1 = ((XFTElement)mr1).getSequence();
			int value2 = ((XFTElement)mr2).getSequence();

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
			  throw new ClassCastException("Error Comparing Sequence");
		  }
	  }
	};

//	/**
//	 * @return
//	 */
//	public ArrayList getAssessorRefs() {
//		if (assessorRefs == null)
//		{
//			Iterator temp = getSchema().getRefs(this.getName()).iterator();
//			assessorRefs = new ArrayList();
//			while (temp.hasNext())
//			{
//				String[] inner = (String[])temp.next();
//				if (inner[0].toString().equalsIgnoreCase("assessor"))
//				{
//					assessorRefs.add(inner[1]);
//				}
//			}
//		}
//		return assessorRefs;
//	}

//	/**
//	 * @return
//	 */
//	public ArrayList getImpliedParents() {
//		if (impliedParents == null)
//		{
//			Iterator temp = getSchema().getRefs(this.getName()).iterator();
//			impliedParents = new ArrayList();
//			while (temp.hasNext())
//			{
//				String[] inner = (String[])temp.next();
//				if (inner[0].toString().equalsIgnoreCase("child"))
//				{
//					impliedParents.add(inner[1]);
//				}
//			}
//		}
//		return impliedParents;
//	}

	/**
	 * If this element was not defined at the root level of the schema, but was instead defined as a child
 	* of another element (usually with maxOccurs &#62;1), then its createdChild property will be true.
	 * @return Returns whether
	 */
	public boolean isCreatedChild() {
		return createdChild;
	}

	/**
	 * If this element was not defined at the root level of the schema, but was instead defined as a child
 	* of another element (usually with maxOccurs &#62;1), then its createdChild property will be true.
	 * @param b
	 */
	public void setCreatedChild(boolean b) {
		createdChild = b;
	}

	/**
	 * XMLNS property of the parent schema.
	 * @return Returns the schema prefix String
	 */
	public String getSchemaPrefix() {
		return schemaPrefix;
	}

	/**
	 * @return Returns the XFTSchema
	 */
	public XFTSchema getSchema() {
		return schema;
	}

	/**
	 * parent XFTDataModel
	 * @return Returns the XFTDataModel
	 */
	public XFTDataModel getDataModel() {
		return this.schema.getDataModel();
	}

	/**
	 * If this element does not correspond to a unique XML node then its isANoChildElement will be true.
	 * @return Returns true if this element does not correspond to a unique XML node
	 */
	public boolean isANoChildElement() {
		return isANoChildElement;
	}

	/**
	 * If this element does not correspond to a unique XML node then its isANoChildElement will be true.
	 * @param b
	 */
	public void setANoChildElement(boolean b) {
		isANoChildElement = b;
	}

	/**
	 * If the element is an extension of another element, then its extension is true and the
	 * extended elements XMLType is stored in the exstensionType element.  If this element is extended
	 * by another element then its isExtended field is true.  If it is extended and is not an extension
	 * itself, then it will contain an additional field (reference to XFT_Element) and its hasExtensionElement
	 * will be true.
	 * @return Returns whether the element is an extension of another element
	 */
	public boolean isExtension() {
		return extension;
	}

	/**
	 * If the element is an extension of another element, then its extension is true and the
	 * extended elements XMLType is stored in the exstensionType element.  If this element is extended
	 * by another element then its isExtended field is true.  If it is extended and is not an extension
	 * itself, then it will contain an additional field (reference to XFT_Element) and its hasExtensionElement
	 * will be true.
	 * @return Returns the extended element's XMLType
	 */
	public XMLType getExtensionType() {
		return extensionType;
	}


	/**
	 * If the element is an extension of another element, then its extension is true and the
	 * extended elements XMLType is stored in the exstensionType element.  If this element is extended
	 * by another element then its isExtended field is true.  If it is extended and is not an extension
	 * itself, then it will contain an additional field (reference to XFT_Element) and its hasExtensionElement
	 * will be true.
	 * @param b
	 */
	public void setExtension(boolean b) {
		extension = b;
	}

	/**
	 * If the element is an extension of another element, then its extension is true and the
	 * extended elements XMLType is stored in the exstensionType element.  If this element is extended
	 * by another element then its isExtended field is true.  If it is extended and is not an extension
	 * itself, then it will contain an additional field (reference to XFT_Element) and its hasExtensionElement
	 * will be true.
	 * @param type
	 */
	public void setExtensionType(XMLType type) {
		extensionType = type;
	}

	/**
	 * If the element is an extension of another element, then its extension is true and the
	 * extended elements XMLType is stored in the exstensionType element.  If this element is extended
	 * by another element then its isExtended field is true.  If it is extended and is not an extension
	 * itself, then it will contain an additional field (reference to XFT_Element) and its hasExtensionElement
	 * will be true.
	 * @return Returns whether the element is extended by another element
	 */
	public boolean isExtended() {
		return isExtended;
	}

	/**
	 * If the element is an extension of another element, then its extension is true and the
	 * extended elements XMLType is stored in the exstensionType element.  If this element is extended
	 * by another element then its isExtended field is true.  If it is extended and is not an extension
	 * itself, then it will contain an additional field (reference to XFT_Element) and its hasExtensionElement
	 * will be true.
	 * @param b
	 */
	public void setExtended(boolean b) {
		isExtended = b;
	}

	/**
	 * Creates a XFTReferenceField to the XFT_Elements element (if this element is extended).
	 */
	public void initializeExtensionField()
	{
		if (! isExtended())
		{
			setExtended(true);
			if (XFTManager.GetElementTable() != null)
			{
				XFTReferenceField field = XFTReferenceField.GetEmptyRef();
				field.setName(XFTItem.EXTENDED_ITEM);
				field.setXMLType(XFTManager.GetElementTable().getType());
				field.setFullName(XFTItem.EXTENDED_ITEM);
				field.setMinOccurs("0");
				field.setExpose(FALSE);
				field.setOnDelete("SET NULL");
				field.setParent(this);
				field.setSequence(1000);
				field.getSqlField().setSqlName(XFTItem.EXTENDED_FIELD_NAME);
				addField(field);
				setHasExtensionElement(true);
			}

		}
	}

	/**
	 * Get Meta Element which describes this item.
	 * @return Returns the XFTMetaElement
	 */
	public XFTMetaElement getMetaElement() {
		return metaElement;
	}

	/**
	 * @param element
	 */
	public void setMetaElement(XFTMetaElement element) {
		metaElement = element;
	}

	/**
	 * @return Returns whether the element has an extension element
	 */
	public boolean hasExtensionElement() {
		return hasExtensionElement;
	}

	/**
	 * @param b
	 */
	public void setHasExtensionElement(boolean b) {
		hasExtensionElement = b;
	}
	/**
	 * @param b
	 */
	public void setSkipSQL(boolean b) {
		skipSQL = b;
	}
	/**
	 * @param s
	 */
	public void setSkipSQL(String s ){
		if (s.equalsIgnoreCase(TRUE) || s.equalsIgnoreCase("t") || s.equalsIgnoreCase("1"))
		{
			skipSQL = true;
		}else
		{
			skipSQL = false;
		}
	}

	public boolean isSkipSQL()
	{
		return skipSQL;
	}
	/**
	 * @return Returns the addin.
	 */
	public String getAddin() {
		return addin;
	}
	/**
	 * @param addin The addin to set.
	 */
	public void setAddin(String addin) {
		this.addin = addin;
	}
	
	List<String> displayIdentifiers=null;
	/**
	 * @return Returns the displayIdentifiers.
	 */
	public List<String> getDisplayIdentifiers() {
		return displayIdentifiers;
	}
	/**
	 * @param displayIdentifiers The displayIdentifiers to set.
	 */
	public void setDisplayIdentifiers(List<String> displayIdentifiers) {
		this.displayIdentifiers = displayIdentifiers;
	}

	public XFTElement clone(XFTElement e,boolean history)
	{
		XFTElement clone = new XFTElement(e.getSchema());
		if(history){
            clone.setAddin("history");
		}else{
			clone.setAddin("generated");
		}
		clone.setName(e.getName() + "_" + this.getName());
		clone.setType(clone.getName(),e.getSchema());

		if (e.getSqlElement() != null)
		{
			if (! e.getSqlElement().getName().equalsIgnoreCase(""))
			{
				clone.getSqlElement().setName(e.getSqlElement().getName() + "_" + this.getName());
			}
		}

		if (e.isExtension())
		{
			clone.setExtension(true);
			clone.setExtensionType(e.getExtensionType());
		}

		int counter = 1002;
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
				clone.addField(ref);
			}else
			{
				clone.addField((XFTField)field.clone(e,true));
			}
		}

		return clone;
	}

    /**
     * @return Returns the ignoreWarnings.
     */
    public boolean isIgnoreWarnings() {
        return ignoreWarnings;
    }

    /**
     * @param ignoreWarnings The ignoreWarnings to set.
     */
    public void setIgnoreWarnings(boolean ignoreWarnings) {
        this.ignoreWarnings = ignoreWarnings;
    }

    /**
     * @return Returns the quarantine.
     */
    public boolean isQuarantine() {
        return isQuarantine(false);
    }

    /**
     * @return Returns the quarantine.
     */
    public boolean isQuarantine(boolean defaultValue) {
        if (quarantine ==null)
        {
            return defaultValue;
        }else{
            return quarantine.booleanValue();
        }
    }
    /**
     * @param quarantine The quarantine to set.
     */
    public void setQuarantineSetting(boolean quarantine) {
        this.quarantine = new Boolean(quarantine);
    }

    public boolean isAutoActivate()
    {
        if (quarantine ==null)
        {
            return false;
        }else{
            if (quarantine.booleanValue())
            {
                return false;
            }else{
                return true;
            }
        }
    }

    public boolean hasQurantineSetting()
    {
        if (this.quarantine==null)
        {
            return false;
        }else{
            return true;
        }
    }
    /**
     * @return Returns the preLoad.
     */
    public boolean isPreLoad() {
        return preLoad;
    }
    /**
     * @param preLoad The preLoad to set.
     */
    public void setPreLoad(boolean preLoad) {
        this.preLoad = preLoad;
    }


	public boolean isRootElement()
	{
	    if (XFTManager.GetRootElementsHash().containsKey(this.getType().getFullForeignType()))
	    {
	        return true;
	    }else{
	        return false;
	    }
	}

    /**
     * If this element can be a root element and contains a hidden (base column) reference.
     * @return Returns the rootWithBase.
     */
    public boolean canBeRoot() {
        if (this.isRootElement())
        {
            return true;
        }else{
            return false;
        }
    }

    public boolean hasBase()
    {
        if (hasBase)
        {
            return true;
        }else{
            return false;
        }
    }

    /**
     * @param hasBase The hasBase to set.
     */
    public void setHasBase(boolean hasBase) {
        this.hasBase = hasBase;
    }
    /**
     * @return Returns the matchByValues.
     */
    public boolean isMatchByValues() {
        return matchByValues;
    }
    /**
     * @param matchByValues The matchByValues to set.
     */
    public void setMatchByValues(boolean matchByValues) {
        this.matchByValues = matchByValues;
    }

    /**
     * @return Returns the storeHistory.
     */
    public boolean storeHistory() {
        return storeHistory;
    }
    /**
     * @param storeHistory The storeHistory to set.
     */
    public void setStoreHistory(boolean storeHistory) {
        this.storeHistory = storeHistory;
    }

    /**
     * @return the isAbstract
     */
    public boolean isAbstract() {
        return isAbstract;
    }

    /**
     * @param isAbstract the isAbstract to set
     */
    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

	public String getXsDescription() {
		return xsDescription;
	}

	public void setXsDescription(String xsDescription) {
		this.xsDescription = xsDescription;
	}
    
    
}

