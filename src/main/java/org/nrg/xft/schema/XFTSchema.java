/*
 * core: org.nrg.xft.schema.XFTSchema
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.schema;

import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter;
import org.nrg.xft.schema.design.XFTElementWrapper;
import org.nrg.xft.schema.design.XFTFactoryI;
import org.nrg.xft.utils.NodeUtils;
import org.nrg.xft.utils.XMLUtils;
import org.nrg.xft.utils.XftStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import java.util.*;
public class XFTSchema {
	private static final Logger          logger             = LoggerFactory.getLogger(XFTSchema.class);
	private              XFTWebAppSchema webAppSchema       = null;
	private              Hashtable       elementsByName     = new Hashtable();
	private              Hashtable       elementsByCode     = new Hashtable();
	private              Hashtable       elementsByJavaName = new Hashtable();
	private              Hashtable       elementsBySQLName  = new Hashtable();
	
	protected static ArrayList tempElements = new ArrayList();
	//private Hashtable refConnections = null;
	
	//private Hashtable imports = new Hashtable();
	
	private final Map<String, String> uRIToAbbr = new HashMap<>();
	private final Map<String, String> abbrToURI = new HashMap<>();
	
	private XFTDataModel dataModel = null;
	
	private String targetNamespaceURI = "";
	private String targetPrefix = "";
	private String xmlns = "xs";

	/**
	 * Constructs an XFTSchema object from the XML DOM Document. Stores each root level node
	 * in the XML DOM Document as a NodeWrapper. All import nodes are added as additional 
	 * XFTDataModels. Every root level node of type element, group, complexType, and
	 * attributeGroup will become a XFTElement if it contains more than a simple reference.
	 * @param doc XML DOM Document
	 * @param data parent
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public XFTSchema(Document doc,XFTDataModel data) throws XFTInitException,ElementNotFoundException {
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
				if (attName.contains(":"))
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
				targetPrefix = uRIToAbbr.get(targetNamespaceURI);
				if (targetPrefix == null)
					targetPrefix="";
				XFTMetaManager.AddURIToPrefixMapping(targetNamespaceURI,targetPrefix);
			}
		} catch (RuntimeException e) {
			final String message = "Error processing node:" + targetNamespaceURI + " " + targetPrefix + "\n" + e.getMessage();
			logger.error(message, e);
			throw new RuntimeException(message);
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
//
//		REMOVED 3/30/16: Now that all schemas are automatically loaded from the classpath, we shouldn't need to follow include statements		
//        for (final Object object : NodeUtils.GetLevelNNodes(rootElement, getXMLNS() + ":include", 1)) {
//            final Node n = (Node) object;
//            String schemaLocal = NodeUtils.GetAttributeValue(n, "schemaLocation", "");
//            String tempDir;
//            if (!schemaLocal.contains(File.separator)) {
//                if (dir.endsWith(File.separator)) {
//                    tempDir = dir + schemaLocal;
//                } else {
//                    tempDir = dir + File.separator + schemaLocal;
//                }
//            } else {
//                tempDir = schemaLocal;
//            }
//            File f = new File(tempDir);
//            if (f.exists()) {
//                String fileName = StringUtils.GetFileName(tempDir);
//                if (XFTManager.GetDataModels().get(fileName) == null) {
//                    XFTDataModel model = new XFTDataModel();
//                    model.setFileName(fileName);
//                    model.setFileLocation(StringUtils.GetDirName(tempDir));
//                    model.setDb(this.getDataModel().getDb());
//
//                    XFTManager.GetDataModels().put(model.getFileName(), model);
//                    try {
//                        model.setSchema();
//                    } catch (XFTInitException e) {
//                        logger.error("An error occurred initializing XFT", e);
//                    } catch (ElementNotFoundException e) {
//                        logger.error("An element could not be found", e);
//                    }
//                }
//            }
//        }
		
		int counter = 1;
		logger.debug("Loading Schema '" + data.getFileName() +"'...");		
		//Iterate through the nodes to build the XFTElements
		for(int i =0; i<elements.getLength();i++)
		{
			tempElements = new ArrayList();
			Node element = elements.item(i);

            if (NodeUtils.NodeHasName(element))
			{
				if (element.getNodeName().contains("element"))
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
									if (elementType.contains(":"))
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
                                            final String message = "Unknown reference: " + elementName + " (" + elementType + ")";
                                            logger.error(message);
                                            throw new RuntimeException(message);
										}
									}else{
                                        final String message = "Unknown reference: " + elementName + " (" + elementType + ")";
                                        logger.error(message);
                                        throw new RuntimeException(message);
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
                                        final String message = "Error processing node:" + targetNamespaceURI + " " + targetPrefix + "\n" + e.getMessage();
                                        logger.error(message);
                                        throw new RuntimeException(message);
									}
								}
							}							
						}
					}
				}else if (element.getNodeName().contains("group"))
				{
					XFTElement xe = new XFTElement(element,this.getXMLNS(),this);
					xe.setSequence(counter++);

					addElement(xe);
					NodeWrapper.AddNode(NodeUtils.GetNodeName(element),xe.getName(),this);
				}else if (element.getNodeName().contains("attributeGroup"))
				{
					XFTElement xe = new XFTElement(element,this.getXMLNS(),this);
					xe.setSequence(counter++);
					addElement(xe);
					NodeWrapper.AddNode(NodeUtils.GetNodeName(element),xe.getName(),this);
				}else if (element.getNodeName().contains("complexType"))
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
                for (Object tempElement : tempElements) {
                    XFTElement xe = (XFTElement) tempElement;
                    xe.setSequence(counter++);
                    addElement(xe);
                }
			}
		}
		
		logger.debug("Finished Loading Schema '" + data.getFileName() + "'.");
	}
	
	public boolean isRootSingleElement(Node element) 
	{
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
                    return !NodeUtils.ExtensionHasAddOns(extension);
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
		if (elementType != null && !elementType.equals(""))
		{
			return elementType;
		}else{
			Node n = NodeUtils.GetLevel1Child(element,this.getXMLNS() + ":simpleType");
			if (n!= null)
			{
				elementType = this.getXMLNS() + ":simpleType";
			}

            if (elementType != null && !elementType.equals(""))
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
	 * @return The database type.
	 */
	public String getDbType() {
		return "postgresql";
	}

	/**
	 * key=XFTElement.getLocalXMLName(),value=XFTElement
	 * @return The available elements by name.
	 */
	public Hashtable getElementsByName() {
		return elementsByName;
	}

	/**
	 * @param xe The element to add.
	 */
	public void addElement(XFTElement xe)
	{
	
		if (xe.getAddin().equalsIgnoreCase("extension") && this.getTargetNamespacePrefix().equalsIgnoreCase("xdat"))
		{
			XFTManager.SetElementTable(xe);
		}
		
		elementsByName.put(xe.getName().toLowerCase(),xe);
        if (xe.getCode() != null && !xe.getCode().equals("")) {
            elementsByCode.put(xe.getCode(), xe);
        }
		
		boolean addedJavaName = false;
		if (xe.getWebAppElement() != null)
		{
            final String javaName = xe.getWebAppElement().getJavaName();
            if (javaName != null && !javaName.equals(""))
			{
				elementsByJavaName.put(javaName.toLowerCase(),xe);
				addedJavaName = true;
			}
		}
		
		if (! addedJavaName)
		{
			if (xe.getSqlElement() != null)
			{
                final String name = xe.getSqlElement().getName();
                if (name != null && !name.equals(""))
				{
					elementsByJavaName.put(XftStringUtils.FormatStringToClassName(name).toLowerCase(), xe);
					addedJavaName = true;
				}
			}
			
			if (! addedJavaName)
			{
				elementsByJavaName.put(XftStringUtils.FormatStringToClassName(xe.getName()).toLowerCase(), xe);
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
                    final String javaName = xe.getWebAppElement().getJavaName();
                    if (javaName != null && !javaName.equals(""))
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
	 * @return Elements arranged by code.
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
		if (getDbType() != null && !getDbType().equals("")) {
            main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc, "dbType", this.getDbType()));
        }
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"prefix",this.getTargetNamespacePrefix()));
		main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"uri",this.getTargetNamespaceURI()));
        for (final Object o : this.getSortedElements()) {
            main.appendChild(((XFTElement) o).toXML(doc));
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
	 * @return The schema.
	 */
	public XFTWebAppSchema getWebAppSchema() {
		return webAppSchema;
	}

	/**
	 * @param schema The schema.
	 */
	public void setWebAppSchema(XFTWebAppSchema schema) {
		webAppSchema = schema;
	}

	/**
	 * @param xef The factory.
	 * @return The elements.
	 */
	protected ArrayList getWrappedElementsByName(XFTFactoryI xef)
	{
		return xef.loadElements(this.getElementsByName().values());
	}
	
	/**
	 * @param xef The factory.
	 * @return The elements.
	 */
	protected ArrayList getWrappedElementsByCode(XFTFactoryI xef)
	{
		return xef.loadElements(this.getElementsByCode().values());
	}
	
	/**
	 * @param xef     The factory.
	 * @param type    The type to retrieve.
	 * @return The elements.
	 */
	protected XFTElementWrapper getWrappedElementByCode(XFTFactoryI xef,String type)
	{
		XFTElement xe = (XFTElement)getElementsByCode().get(type.toLowerCase());
		return xef.wrapElement(xe);
	}
	
	/**
	 * @param xef     The factory.
	 * @param name    The name.
	 * @return The element.
	 */
	protected XFTElementWrapper getWrappedElementByName(XFTFactoryI xef,String name)
	{
		XFTElement xe = (XFTElement)getElementsByName().get(name.toLowerCase());
		return xef.wrapElement(xe);
	}
	
	/**
	 * @param xef     The factory.
	 * @param name    The name.
	 * @return The element.
	 */
	protected XFTElementWrapper getWrappedElementBySQLName(XFTFactoryI xef,String name)
	{
		XFTElement xe = (XFTElement)getElementsBySQLName().get(name.toLowerCase());
		return xef.wrapElement(xe);
	}

	/**
	 * @param xef    The factory.
	 * @param o      The object.
	 * @return The element.
	 */
	protected XFTElementWrapper getWrappedElementByObject(XFTFactoryI xef,Object o)
	{
		String temp = XftStringUtils.getLocalClassName(o.getClass()).toLowerCase();
		XFTElement xe = (XFTElement)getElementsByJavaName().get(temp);
		return xef.wrapElement(xe);
	}

	/**
	 * @param xef     The factory.
	 * @param name    The name.
	 * @return The element.
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
	public List<String> getSortedElementNames()
	{
		List<String> al = new ArrayList<>();
        for (final Object temp : getSortedElements()) {
			al.add(((XFTElement)temp).getName());
		}
		return al;
	}

	/**
	 * @param xef    The factory.
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
	 * @return The elements.
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
	 * @return The elements.
	 */
	protected Hashtable getElementsBySQLName() {
		return elementsBySQLName;
	}
	
	/**
	 * @return The XML namespace.
	 */
	public String getXMLNS() {
		return xmlns;
	}
	

	/**
	 * @param string    The namespace to set.
	 */
	public void setXMLNS(String string) {
		xmlns = string;
	}

	/**
	 * @return The data model.
	 */
	public XFTDataModel getDataModel() {
		return dataModel;
	}

	/**
	 * @param data    The data model to set.
	 */
	public void setDataModel(XFTDataModel data) {
		dataModel = data;
	}

	/**
	 * @return The target namespace prefix.
	 */
	public String getTargetNamespacePrefix() {
		return targetPrefix;
	}

	/**
	 * @return The target namespace URI.
	 */
	public String getTargetNamespaceURI() {
		return targetNamespaceURI;
	}
	
	/**
	 * @param prefix    The prefix to search for.
	 * @return The URI for the indicated prefix.
	 */
	public String getURIForPrefix(String prefix)
	{
		return this.abbrToURI.get(prefix);
	}
	
	/**
	 * @param uri    The URI to retrieve.
	 * @return The prefix for the URI.
	 */
	@SuppressWarnings("unused")
    public String getPrefixForURI(String uri)
	{
		return this.uRIToAbbr.get(uri);
	}

	/**
	 * Gets an XMLType with this schema's XMLNS prefix and the specified type.
	 * @param type    The type to retrieve.
	 * @return The requested {@link XMLType type}.
	 */
	public XMLType getBasicDataType(String type)
	{
		return new XMLType(this.getXMLNS() + ":" + type,this);
	}
}

