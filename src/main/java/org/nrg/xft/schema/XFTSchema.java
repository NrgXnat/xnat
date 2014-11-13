/*
 * org.nrg.xft.schema.XFTSchema
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 8/28/13 3:20 PM
 */


package org.nrg.xft.schema;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xft.db.DBConfig;
import org.nrg.xft.db.DBPool;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter;
import org.nrg.xft.schema.design.XFTElementWrapper;
import org.nrg.xft.schema.design.XFTFactoryI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.NodeUtils;
import org.nrg.xft.utils.StringUtils;
import org.nrg.xft.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class XFTSchema {
	static org.apache.log4j.Logger logger = Logger.getLogger(XFTSchema.class);
	private XFTWebAppSchema webAppSchema = null;
	private Hashtable elementsByName = new Hashtable();
	private Hashtable elementsByCode = new Hashtable();
	private Hashtable elementsByJavaName = new Hashtable();
	private Hashtable elementsBySQLName = new Hashtable();
	
	protected static ArrayList tempElements = new ArrayList();
	//private Hashtable refConnections = null;
	
	//private Hashtable imports = new Hashtable();
	
	private Hashtable uRIToAbbr = new Hashtable();
	private Hashtable abbrToURI = new Hashtable();
	
	private XFTDataModel dataModel = null;
	
	private String targetNamespaceURI = "";
	private String targetPrefix = "";
	private String xmlns = "xs";
    
    private boolean nullXMLNSPrefix = false;
	
	private DBConfig config = null;
	
	/**
	 * Constructs an XFTSchema object from the XML DOM Document. Stores each root level node
	 * in the XML DOM Document as a NodeWrapper. All import nodes are added as additional 
	 * XFTDataModels. Every root level node of type element, group, complexType, and
	 * attributeGroup will become a XFTElement if it contains more than a simple reference.
	 * @param doc XML DOM Document
	 * @param dir Directory of the xsd document
	 * @param data parent
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public XFTSchema(Document doc,String dir,XFTDataModel data) throws XFTInitException,ElementNotFoundException {
		this.setDataModel(data);
		final Element rootElement = doc.getDocumentElement();
				
		//Set XMLSchema prefix
		NamedNodeMap attributes = rootElement.getAttributes();
		for (int i=0;i<attributes.getLength();i++)
		{
			Node attribute = attributes.item(i);
			if (attribute.getNodeValue().equalsIgnoreCase("http://www.w3.org/2001/XMLSchema"))
			{
				String attName= attribute.getNodeName();
				if (attName.indexOf(":") != -1)
				{
					attName = attName.substring(attName.indexOf(":")+1);
				}
                this.setXMLNS(attName);
                continue;
			}
			if (attribute.getNodeName().equalsIgnoreCase("targetNamespace"))
			{
				this.targetNamespaceURI = attribute.getNodeValue();	
			}
			//schemaAttributes.put(attribute.getNodeValue(),attribute.getNodeName());
			
			if (attribute.getNodeName().startsWith("xmlns:"))
			{
				String abbr = attribute.getNodeName().substring(attribute.getNodeName().indexOf(":") + 1);
				this.uRIToAbbr.put(attribute.getNodeValue(),abbr);
				this.abbrToURI.put(abbr,attribute.getNodeValue());	
			}
		}
		
		try {
			if (! targetNamespaceURI.equalsIgnoreCase(""))
			{
				targetPrefix = (String)uRIToAbbr.get(targetNamespaceURI);
				if (targetPrefix == null)
					targetPrefix="";
				XFTMetaManager.AddURIToPrefixMapping(targetNamespaceURI,targetPrefix);
			}
		} catch (RuntimeException e) {
		    RuntimeException e1 = new RuntimeException("Error processing node:" + targetNamespaceURI + " " + targetPrefix + "\n" + e.getMessage());
			throw e1;
		}
		

		NodeList elements = rootElement.getChildNodes();
		
		//Load empty NodeWrappers
		for(int i =0; i<elements.getLength();i++)
		{
			Node element = elements.item(i);
			if (NodeUtils.NodeHasName(element))
			{
				NodeWrapper.AddNode(NodeUtils.GetNodeName(element),null,this);
			}
		}
		
		Iterator includeNodes = NodeUtils.GetLevelNNodes(rootElement,getXMLNS() + ":include",1).iterator();
		while (includeNodes.hasNext())
		{
			Node n = (Node)includeNodes.next();
			String schemaLocal = NodeUtils.GetAttributeValue(n,"schemaLocation","");
			String tempDir = null;
			if (schemaLocal.indexOf(File.separator) == -1)
			{
				if (dir.endsWith(File.separator))
				{
					tempDir =dir + schemaLocal;
				}else{
					tempDir =dir + File.separator + schemaLocal;	
				}
			}else
			{
				tempDir = schemaLocal;
			}
			File f = new File(tempDir);
			if (f.exists())
			{
				String fileName = StringUtils.GetFileName(tempDir);
				if (XFTManager.GetDataModels().get(fileName) == null)
				{
					XFTDataModel model = new XFTDataModel();
					model.setFileName(fileName);
					model.setFileLocation(StringUtils.GetDirName(tempDir));
					model.setDb(this.getDataModel().getDb());

					XFTManager.GetDataModels().put(model.getFileName(),model);
					try {
						model.setSchema();
					} catch (XFTInitException e) {
						e.printStackTrace();
					} catch (ElementNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}		
		
		int counter = 1;
		logger.debug("Loading Schema '" + data.getFileName() +"' FROM '" + dir + "'...");		
		//Iterate through the nodes to build the XFTElements
		for(int i =0; i<elements.getLength();i++)
		{
			tempElements = new ArrayList();
			Node element = elements.item(i);
			NamedNodeMap nnm = element.getAttributes();
			
			if (NodeUtils.NodeHasName(element))
			{
				if (element.getNodeName().indexOf("element") != -1)
				{					
					if (NodeUtils.NodeHasComplexContent(element,this.getXMLNS()))
					{
						XFTElement xe = new XFTElement(element,this.getXMLNS(),this);
						xe.setSequence(counter++);
						addElement(xe);
						XFTManager.AddRootElement(targetPrefix + ":" + xe.getName(),targetPrefix + ":" + NodeUtils.GetNodeName(element));
						NodeWrapper.AddNode(NodeUtils.GetNodeName(element),xe.getName(),this);
					}else
					{
						if (isRootSingleElement(element))
						{
							String elementName = NodeUtils.GetAttributeValue(element,"name","");
							String elementType = this.getRootElementType(element);
							
							if (elementType.equalsIgnoreCase(this.getXMLNS() + ":simpleType"))
							{
								XFTElement xe = new XFTElement(element,this.getXMLNS(),this);
								xe.setSequence(counter++);
								addElement(xe);
								XFTManager.AddRootElement(this.targetPrefix + ":" + xe.getName(),this.targetPrefix + ":" + NodeUtils.GetNodeName(element));
								NodeWrapper.AddNode(NodeUtils.GetNodeName(element),xe.getName(),this);
							}else{
								Node refType = NodeWrapper.FindNode(elementType,this);
								if (refType==null)
								{
									if (elementType != null && elementType.indexOf(":")!=-1)
									{
										String prefix = elementType.substring(0,elementType.indexOf(":"));
										if (this.getXMLNS().equalsIgnoreCase(prefix))
										{
											XFTElement xe = new XFTElement(element,this.getXMLNS(),this);
											xe.setSequence(counter++);
											addElement(xe);
											NodeWrapper.AddNode(NodeUtils.GetNodeName(element),xe.getName(),this);
											XFTManager.AddRootElement(this.targetPrefix + ":" + xe.getName(),this.targetPrefix + ":" + NodeUtils.GetNodeName(element));
										}else{
											throw new RuntimeException("Unknown reference: " + elementName + " (" + elementType + ")");
										}
									}else{
										throw new RuntimeException("Unknown reference: " + elementName + " (" + elementType + ")");
									}
								}else{
									try {
										if (XFTField.IsRefOnly(refType,this.getXMLNS(),this))
										{	
											XFTReferenceManager.AddProperName(elementName,XFTField.GetRefName(refType,this.getXMLNS(),this));
											XFTManager.AddRootElement(XFTField.GetRefName(refType,this.getXMLNS(),this),this.targetPrefix + ":" + NodeUtils.GetNodeName(element));
										}else
										{	
											XFTReferenceManager.AddProperName(elementName,elementType);
											XFTManager.AddRootElement(elementType,this.targetPrefix + ":" + NodeUtils.GetNodeName(element));
										}
									} catch (RuntimeException e) {
									    RuntimeException e1 = new RuntimeException("Error processing node:" + targetNamespaceURI + " " + targetPrefix + "\n" + e.getMessage());
										throw e1;
									}
								}
							}							
						}else{
//							String elementName = NodeUtils.GetAttributeValue(element,"name","");
//							String elementType = NodeUtils.GetAttributeValue(element,"type","");
////							NO ELEMENT CREATED	
//							if (elementType.equalsIgnoreCase(""))
//							{
//								Node n = NodeUtils.GetLevel2Child(element,"xs:restriction");
//								if (n!= null)
//								{
//									elementType = NodeUtils.GetAttributeValue(n,"base","");
//								}
//							}
//							if ((elementName != "") && (elementType != ""))
//							{
//								Node refType = NodeWrapper.FindNode(elementType,this);
//								if (refType==null)
//								{
//									if (elementType != null && elementType.indexOf(":")!=-1)
//									{
//										String prefix = elementType.substring(0,elementType.indexOf(":"));
//										if (this.getXMLNS().equalsIgnoreCase(prefix))
//										{
//											XFTElement xe = new XFTElement(element,this.getXMLNS(),this);
//											xe.setSequence(counter++);
//											addElement(xe);
//											NodeWrapper.AddNode(NodeUtils.GetNodeName(element),xe.getName(),this);
//										}else{
//											throw new RuntimeException("Unknown reference: " + elementName + " (" + elementType + ")");
//										}
//									}else{
//										throw new RuntimeException("Unknown reference: " + elementName + " (" + elementType + ")");
//									}
//								}else{
//									try {
//										if (XFTField.IsRefOnly(refType,this.getXMLNS(),this))
//										{	
//											XFTReferenceManager.AddProperName(elementName,XFTField.GetRefName(refType,this.getXMLNS(),this));
//										}else
//										{	
//											XFTReferenceManager.AddProperName(elementName,elementType);
//										}
//									} catch (RuntimeException e) {
//										RuntimeException e1 = new RuntimeException("Error processing node:" + NodeUtils.GetNodeName(refType) + " (" + elementName + "," + elementType + ")");
//										e1.setStackTrace(e.getStackTrace());
//										throw e1;
//									}
//								}
//							}
						}
						
					}
				}else if (element.getNodeName().indexOf("group") != -1)
				{
					XFTElement xe = new XFTElement(element,this.getXMLNS(),this);
					xe.setSequence(counter++);

					addElement(xe);
					NodeWrapper.AddNode(NodeUtils.GetNodeName(element),xe.getName(),this);
				}else if (element.getNodeName().indexOf("attributeGroup") != -1)
				{
					XFTElement xe = new XFTElement(element,this.getXMLNS(),this);
					xe.setSequence(counter++);
					addElement(xe);
					NodeWrapper.AddNode(NodeUtils.GetNodeName(element),xe.getName(),this);
				}else if (element.getNodeName().indexOf("complexType") != -1)
				{
					if (! XFTField.IsRefOnly(element,this.getXMLNS(),this))
					{
						XFTElement xe = new XFTElement(element,this.getXMLNS(),this);
						xe.setSequence(counter++);
						addElement(xe);
						NodeWrapper.AddNode(NodeUtils.GetNodeName(element),xe.getName(),this);
					}else
					{
						//NO ELEMENT CREATED
						NodeWrapper.AddNode(NodeUtils.GetNodeName(element),XFTField.GetRefName(element,this.getXMLNS(),this),this);
					}
				}else
				{
				 	// NO ELEMENT CREATED	
					NodeWrapper.AddNode(NodeUtils.GetNodeName(element),null,this);
				}
			}		
			
			if (tempElements.size() > 0)
			{
				for(int j=0;j<tempElements.size();j++)
				{
					XFTElement xe = (XFTElement)tempElements.get(j);
					xe.setSequence(counter++);
					addElement(xe);
				}
			}
		}
		
		logger.debug("Finished Loading Schema '" + data.getFileName() + "'.");
	}
	
	public boolean isRootSingleElement(Node element) 
	{
		boolean temp = false;
		String elementName = NodeUtils.GetAttributeValue(element,"name","");
		String elementType = NodeUtils.GetAttributeValue(element,"type","");
//		NO ELEMENT CREATED	
		if (elementType.equalsIgnoreCase(""))
		{
			Node n = NodeUtils.GetLevel1Child(element,this.getXMLNS() + ":simpleType");
			if (n!= null)
			{
				return true;
			}else{
			    Node extension = NodeUtils.GetLevel3Child(element,this.getXMLNS()+":extension");
			    
			    try {
                    if (NodeUtils.ExtensionHasAddOns(extension))
                    {
                        return false;
                    }else{
                        return true;
                    }
                } catch (RuntimeException e) {
                    throw new RuntimeException("XNAT Schema Load Error in element '" + elementName + "'");
                }
			}
		}else{
			return true;
		}
	}
	
	public String getRootElementType(Node element)
	{
		String elementType = NodeUtils.GetAttributeValue(element,"type","");
//		NO ELEMENT CREATED	
		if (elementType.equalsIgnoreCase(""))
		{
			Node n = NodeUtils.GetLevel2Child(element,this.getXMLNS() + ":restriction");
			if (n!= null)
			{
				elementType = NodeUtils.GetAttributeValue(n,"base","");
			}
		}
		if ((elementType != ""))
		{
			return elementType;
		}else{
			Node n = NodeUtils.GetLevel1Child(element,this.getXMLNS() + ":simpleType");
			if (n!= null)
			{
				elementType = this.getXMLNS() + ":simpleType";
			}
			
			if ((elementType != ""))
			{
				return elementType;
			}else{
				Node extension = NodeUtils.GetLevel3Child(element,this.getXMLNS() + ":extension");
				if (extension!= null)
				{
				    elementType = NodeUtils.GetAttributeValue(extension,"base","");
				}
			}
		}
		
		return elementType;
	}
	
	/**
	 * @return
	 */
	private DBConfig getDBConfig()
	{
		if (config == null)
		{
			config = DBPool.GetDBConfig(this.getDataModel().getDb());
		}
		return config;
	}

	/**
	 * @return
	 */
	public String getDbType() {
		if (getDBConfig()!= null)
		{
			return getDBConfig().getType();
		}else{
			return "";
		}
	}

	/**
	 * key=XFTElement.getLocalXMLName(),value=XFTElement
	 * @return
	 */
	public Hashtable getElementsByName() {
		return elementsByName;
	}

	/**
	 * @param xe
	 */
	public void addElement(XFTElement xe)
	{
	
		if (xe.getAddin().equalsIgnoreCase("extension") && this.getTargetNamespacePrefix().equalsIgnoreCase("xdat"))
		{
			XFTManager.SetElementTable(xe);
		}
		
		elementsByName.put(xe.getName().toLowerCase(),xe);
		if (xe.getCode() != "")
			elementsByCode.put(xe.getCode(),xe);
		
		boolean addedJavaName = false;
		if (xe.getWebAppElement() != null)
		{
			if (xe.getWebAppElement().getJavaName() != "")
			{
				elementsByJavaName.put(xe.getWebAppElement().getJavaName().toLowerCase(),xe);
				addedJavaName = true;
			}
		}
		
		if (! addedJavaName)
		{
			if (xe.getSqlElement() != null)
			{
				if (xe.getSqlElement().getName() != "")
				{
					elementsByJavaName.put(StringUtils.FormatStringToClassName(xe.getSqlElement().getName()).toLowerCase(),xe);
					addedJavaName = true;
				}
			}
			
			if (! addedJavaName)
			{
				elementsByJavaName.put(StringUtils.FormatStringToClassName(xe.getName()).toLowerCase(),xe);
			}
		}
		
		boolean addedSQLName = false;
		if (xe.getSqlElement() != null)
		{
			if (xe.getSqlElement().getName() != null)
			{
				elementsBySQLName.put(xe.getSqlElement().getName().toLowerCase(),xe);
				addedSQLName = true;
			}
		}
		
		if (! addedSQLName)
		{
			if (xe.hasParent())
			{
				if (xe.getWebAppElement() != null)
				{
					if (xe.getWebAppElement().getJavaName() != "")
					{
						elementsBySQLName.put(xe.getWebAppElement().getJavaName().toLowerCase(),xe);
						addedSQLName = true;
					}
				}
			}
			
			if (! addedSQLName)
			{
				elementsBySQLName.put(xe.getName().toLowerCase(),xe);
			}
		}
		
	}
	
	/**
	 * @return
	 */
	protected Hashtable getElementsByCode() {
		return elementsByCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
//		java.lang.StringBuffer sb = new StringBuffer();
//		sb.append("\nXFTSchema\n");
//		if (getDbType() != "")
//			sb.append("dbType:").append(this.getDbType()).append("\n");
//		if (getWebAppSchema() != null)
//			sb.append(this.getWebAppSchema().toString("\t"));
//		
//		Iterator i = this.getSortedElements().iterator();
//		while (i.hasNext())
//		{
//			sb.append(((XFTElement)i.next()).toString("\t"));
//		}
//		return sb.toString();
		Document doc = toXML();
		return XMLUtils.DOMToString(doc);
	}
	

	public Node toXML(Document doc)
	{
		Node main = doc.createElement("schema");
		if (getDbType() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"dbType",this.getDbType()));
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"prefix",this.getTargetNamespacePrefix()));
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"uri",this.getTargetNamespaceURI()));
		Iterator i = this.getSortedElements().iterator();
		while (i.hasNext())
		{
			main.appendChild(((XFTElement)i.next()).toXML(doc));
		}
		return main;
	}
	
	public Document toXML()
	{
		XMLWriter writer = new XMLWriter();
		Document doc =writer.getDocument();
		
		doc.appendChild(this.toXML(doc));

		
		return doc;
	}

	
	/**
	 * @return
	 */
	public XFTWebAppSchema getWebAppSchema() {
		return webAppSchema;
	}

	/**
	 * @param schema
	 */
	public void setWebAppSchema(XFTWebAppSchema schema) {
		webAppSchema = schema;
	}

	/**
	 * @param xef
	 * @return
	 */
	protected ArrayList getWrappedElementsByName(XFTFactoryI xef)
	{
		return xef.loadElements(this.getElementsByName().values());
	}
	
	/**
	 * @param xef
	 * @return
	 */
	protected ArrayList getWrappedElementsByCode(XFTFactoryI xef)
	{
		return xef.loadElements(this.getElementsByCode().values());
	}
	
	/**
	 * @param xef
	 * @param type
	 * @return
	 */
	protected XFTElementWrapper getWrappedElementByCode(XFTFactoryI xef,String type)
	{
		XFTElement xe = (XFTElement)getElementsByCode().get(type.toLowerCase());
		return xef.wrapElement(xe);
	}
	
	/**
	 * @param xef
	 * @param name
	 * @return
	 */
	protected XFTElementWrapper getWrappedElementByName(XFTFactoryI xef,String name)
	{
		XFTElement xe = (XFTElement)getElementsByName().get(name.toLowerCase());
		return xef.wrapElement(xe);
	}
	
	/**
	 * @param xef
	 * @param name
	 * @return
	 */
	protected XFTElementWrapper getWrappedElementBySQLName(XFTFactoryI xef,String name)
	{
		XFTElement xe = (XFTElement)getElementsBySQLName().get(name.toLowerCase());
		return xef.wrapElement(xe);
	}

	/**
	 * @param xef
	 * @param o
	 * @return
	 */
	protected XFTElementWrapper getWrappedElementByObject(XFTFactoryI xef,Object o)
	{
		String temp = StringUtils.getLocalClassName(o.getClass()).toLowerCase();
		XFTElement xe = (XFTElement)getElementsByJavaName().get(temp);
		return xef.wrapElement(xe);
	}

	/**
	 * @param xef
	 * @param name
	 * @return
	 */
	protected XFTElementWrapper getWrappedElementByJavaName(XFTFactoryI xef,String name)
	{
		XFTElement xe = (XFTElement)getElementsByJavaName().get(name.toLowerCase());
		return xef.wrapElement(xe);
	}
	
	/**
	 * @return ArrayList of XFTElements
	 */
	public ArrayList getSortedElements()
	{
		ArrayList temp = new ArrayList();
		temp.addAll(getElementsByName().values());
		Collections.sort(temp,XFTElement.SequenceComparator);
		return temp;
	}
	
	/**
	 * @return ArrayList of Strings
	 */
	public ArrayList getSortedElementNames()
	{
		ArrayList al = new ArrayList();
		Iterator temp = getSortedElements().iterator();
		while (temp.hasNext())
		{
			al.add(((XFTElement)temp.next()).getName());
		}
		al.trimToSize();
		return al;
	}

	/**
	 * @param xef
	 * @return ArrayList of XFTElementWrappers
	 */
	public ArrayList getWrappedElementsSorted(XFTFactoryI xef)
	{
		ArrayList temp = new ArrayList();
		temp.addAll(getElementsByName().values());
		Collections.sort(temp,XFTElement.SequenceComparator);
		return xef.loadElements(temp);
	}
	
	/**
	 * @return
	 */
	protected Hashtable getElementsByJavaName() {
		return elementsByJavaName;
	}
//
//	/**
//	 * @return
//	 */
//	public Hashtable getRefConnections() {
//		if (refConnections == null)
//			refConnections = RWrapperUtils.GetRefConnections(this);
//		return refConnections;
//	}
//	
//	protected ArrayList getRefs(String name)
//	{
//		if (getRefConnections().containsKey(name))
//		{
//			return (ArrayList) getRefConnections().get(name); 
//		}else
//		{
//			return new ArrayList();
//		}
//	}

	/**
	 * @return
	 */
	protected Hashtable getElementsBySQLName() {
		return elementsBySQLName;
	}
	
	/**
	 * Writes schema to text file in the XFTManager's source directory called schema.txt.
	 */
	public static void OutputSchema()
	{
		try {
			StringBuffer sb = new StringBuffer();
			Enumeration enumer = XFTManager.GetDataModels().keys();
			while(enumer.hasMoreElements())
			{
				sb.append(((XFTDataModel)XFTManager.GetDataModels().get(enumer.nextElement())).getSchema().toString());
			}
			FileUtils.OutputToFile(sb.toString(),XFTManager.GetInstance().getSourceDir() + "schema.txt");
		} catch (org.nrg.xft.exception.XFTInitException e) {
			logger.error("",e);
		} catch (Exception e) {
			logger.error("",e);
		}
	}

	/**
	 * @return
	 */
	public String getXMLNS() {
		return xmlns;
	}
	

	/**
	 * @param string
	 */
	public void setXMLNS(String string) {
		xmlns = string;
	}

	/**
	 * @return
	 */
	public XFTDataModel getDataModel() {
		return dataModel;
	}

	/**
	 * @param string
	 */
	public void setDataModel(XFTDataModel data) {
		dataModel = data;
	}

	/**
	 * @return
	 */
	public String getTargetNamespacePrefix() {
		return targetPrefix;
	}

	/**
	 * @return
	 */
	public String getTargetNamespaceURI() {
		return targetNamespaceURI;
	}
	
	/**
	 * @param prefix
	 * @return
	 */
	public String getURIForPrefix(String prefix)
	{
		return (String)this.abbrToURI.get(prefix);
	}
	
	/**
	 * @param prefix
	 * @return
	 */
	public String getPrefixForURI(String uri)
	{
		return (String)this.uRIToAbbr.get(uri);
	}
	
	
	/**
	 * Gets an XMLType with this schema's XMLNS prefix and the specified type.
	 * @param type
	 * @return
	 */
	public XMLType getBasicDataType(String type)
	{
		return new XMLType(this.getXMLNS() + ":" + type,this);
	}
	
	public String getJavaPackageName()
	{
	    String packageName = this.getDataModel().getPackageName();
	    if (packageName == null || packageName.equals(""))
	    {
	        packageName = "org.nrg." + this.targetPrefix.toLowerCase() + ".om";
	    }
	    return packageName;
	}
}

