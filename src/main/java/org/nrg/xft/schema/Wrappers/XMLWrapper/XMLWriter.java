/*
 * core: org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.schema.Wrappers.XMLWrapper;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTPseudonymManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.XMLUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

@Slf4j
public class XMLWriter {
	/**
	 * Object used to generate a new XML DOM Document
	 */
	public XMLWriter() {
		try {
			_builder = XDAT.getSerializerService().getDocumentBuilder();
		} catch (ParserConfigurationException e) {
			log.error("An error occurred creating the SAX parser", e);
		}
	}

	/**
	 * Returns a new XML DOM Document
	 * @return Returns the document.
	 */
	public Document getDocument()
	{
		if (doc == null)
		{
			doc = _builder.newDocument();
		}
		return doc;
	}

	public Element createElement(String s)
	{
		return doc.createElement(s);
	}

//	/**
//	 * Translates the item to an XML DOM and outputs it to the destination File.
//	 * @param item
//	 * @param destinationFile
//	 * @throws org.nrg.xft.exception.XFTInitException
//	 * @throws org.nrg.xft.exception.ElementNotFoundException
//	 * @throws Exception
//	 */
//	public static void StoreXFTItemToXMLFile(XFTItem item,String destinationFile,boolean limited) throws org.nrg.xft.exception.XFTInitException,org.nrg.xft.exception.ElementNotFoundException,Exception
//	{
//		//item.extend();
//		Document doc = ItemToDOM(item,true,limited);
//		XMLUtils.DOMToFile(doc,destinationFile);
//	}

	/**
	 * Translates the XFTItems in the ArrayList into a XML DOM Documents and writes them to files
	 * in the destinationDir using the XFTItem's proper name and its pk value to determine the
	 * file name.
	 * @param al                The list of items to write to files.
	 * @param destinationDir    The destination folder for the files
	 * @param limited           This parameter is ignored.
	 * @param prettyPrint       Indicates whether the XML should be "pretty printed".
	 * @throws Exception When something goes wrong.
     * @deprecated Use {@link #StoreXFTItemListToXMLFile(ArrayList, String, boolean)} instead.
	 */
    @Deprecated
    public static int StoreXFTItemListToXMLFile(ArrayList al, String destinationDir, @SuppressWarnings("UnusedParameters") boolean limited, boolean prettyPrint) throws Exception {
        return StoreXFTItemListToXMLFile(al, destinationDir, prettyPrint);
    }

    /**
     * Translates the XFTItems in the ArrayList into a XML DOM Documents and writes them to files
     * in the destinationDir using the XFTItem's proper name and its pk value to determine the
     * file name.
     * @param al                The list of items to write to files.
     * @param destinationDir    The destination folder for the files
     * @param prettyPrint       Indicates whether the XML should be "pretty printed".
     * @throws Exception When something goes wrong.
     */
    public static int StoreXFTItemListToXMLFile(ArrayList al, String destinationDir, boolean prettyPrint) throws Exception {
        int i = 2;
		Iterator iter = al.iterator();
		while (iter.hasNext())
		{
			XFTItem item = (XFTItem)iter.next();
			//item.extend();

	//FileUtils.OutputToFile(item.toString(),destinationDir + File.separator + "searchedItem.xml");

			String fileName = item.getFileName() + "";
				Iterator pks = item.getPkNames().iterator();
				while (pks.hasNext())
				{
					String pkName = (String)pks.next();
					if (item.getProperty(pkName) != null)
					{
						fileName += "_" + item.getProperty(pkName).toString();
					}
				}

				File dir = new File(destinationDir);
				int counter = 0;
				String finalName = fileName + ".xml";
				boolean exists = FileUtils.SearchFolderForChild(dir,finalName);
				while(exists)
				{
					finalName = fileName + "_v" + (counter++) + ".xml";
					exists =FileUtils.SearchFolderForChild(dir,finalName);
				}

			if (! destinationDir.endsWith(File.separator))
				destinationDir += File.separator;


			File f1 = new java.io.File(destinationDir + finalName);
			java.io.FileOutputStream fs = new java.io.FileOutputStream(f1);
			BufferedOutputStream bos = new BufferedOutputStream(fs);
			SAXWriter writer = new SAXWriter(bos,true);
			writer.setAllowSchemaLocation(true);
			writer.setLocation("http://cnda.wustl.edu/cnda_xnat/schemas/");
			writer.write(item);
			bos.flush();
			bos.close();

//				Document doc = ItemToDOM(item,true,limited);
//				XMLUtils.DOMToFile(doc,destinationDir + finalName);
			if (prettyPrint)
        	{
        	    File f2= new File(destinationDir + finalName);
        	     XMLUtils.PrettyPrintDOM(f2);
        	}

			if(XFT.VERBOSE)System.out.println("Created File: " + destinationDir + finalName);
			i = 0;
		}

		return i;
	}

	/**
	 * Translates item to a node and assigns it to a new Document.
	 * @param item
	 * @return Returns the new Document
	 * @throws org.nrg.xft.exception.XFTInitException
	 * @throws org.nrg.xft.exception.ElementNotFoundException
	 */
	private Document translateItemToDOM(XFTItem item,boolean allowSchemaLocation, String location,boolean limited) throws org.nrg.xft.exception.XFTInitException,org.nrg.xft.exception.ElementNotFoundException,FieldNotFoundException
	{
		Document doc = null;
		doc = _builder.newDocument();
		boolean withPrefix = false;
		if (allowSchemaLocation)
		{
		    withPrefix=true;
		}
		doc.appendChild(itemToNode(item,doc,item.getProperName(),true,allowSchemaLocation,location,withPrefix,limited));

		return doc;
	}

	/**
	 * Method used to translate an XFTItem to an XML DOM Document.
	 * @param item
	 * @return Returns the XML DOM Document
	 * @throws org.nrg.xft.exception.XFTInitException
	 * @throws org.nrg.xft.exception.ElementNotFoundException
	 */
	public static Document ItemToDOM(XFTItem item, boolean allowSchemaLocation,boolean limited) throws org.nrg.xft.exception.XFTInitException,org.nrg.xft.exception.ElementNotFoundException,FieldNotFoundException
	{
		XMLWriter writer = new XMLWriter();
		Document doc = writer.translateItemToDOM(item,allowSchemaLocation,null,limited);
		return doc;
	}
//
	public static Document ItemListToDOM(ArrayList items)
	{
	    return ItemListToDOM(items,false);
	}

	public static Document ItemListToDOM(ArrayList items,boolean limited)
	{
	    XMLWriter writer = new XMLWriter();
	    Document doc = writer.getDocument();
	    Node root = doc.createElement("data");

	    Node attribute = doc.createAttribute("xmlns:xsi");
		attribute.setNodeValue("http://www.w3.org/2001/XMLSchema-instance");
		root.getAttributes().setNamedItem(attribute);

		Enumeration enumer = XFTMetaManager.getPrefixEnum();
		while(enumer.hasMoreElements())
		{
		    String prefix = (String)enumer.nextElement();
		    String uri = XFTMetaManager.TranslatePrefixToURI(prefix);

		    attribute = doc.createAttribute("xmlns:"+prefix);
			attribute.setNodeValue(uri);
			root.getAttributes().setNamedItem(attribute);
		}

		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		attribute = doc.createAttribute("current_date");
		attribute.setNodeValue(df.format(Calendar.getInstance().getTime()));
		root.getAttributes().setNamedItem(attribute);

	    doc.appendChild(root);
		Iterator iter = items.iterator();
		while (iter.hasNext())
		{
		    XFTItem temp = (XFTItem)iter.next();
		    try {
                Node n = writer.itemToNode(temp,doc,temp.getXSIType(),false,false,null,false,limited);
                root.appendChild(n);
            } catch (DOMException e) {
                log.error("", e);
            } catch (XFTInitException e) {
                log.error("", e);
            } catch (ElementNotFoundException e) {
                log.error("", e);
            } catch (FieldNotFoundException e) {
                log.error("", e);
            }
		}
		return doc;
	}

	/**
	 * Method used to translate an XFTItem to an XML DOM Document.
	 * @param item
	 * @return Returns the XML DOM Document
	 * @throws org.nrg.xft.exception.XFTInitException
	 * @throws org.nrg.xft.exception.ElementNotFoundException
	 */
	public static Document ItemToDOM(XFTItem item, boolean allowSchemaLocation,String location,boolean limited) throws org.nrg.xft.exception.XFTInitException,org.nrg.xft.exception.ElementNotFoundException,FieldNotFoundException
	{
		XMLWriter writer = new XMLWriter();
		Document doc = writer.translateItemToDOM(item,allowSchemaLocation,location,limited);
		return doc;
	}

	/**
	 * Translates all of the properties of the item to XML nodes (elements or attributes) and assigns them
	 * to a new XML DOM Node.  If the alias has a value, it is used as the name of the root element.  If
	 * it isRoot, then the schemaLocation and xsi attributes are added.  All populated sub-items are
	 * translated to nodes and added to the parent node accordingly.
	 * @param item
	 * @param doc
	 * @param alias
	 * @param isRoot
	 * @return Returns the new XML DOM Node
	 * @throws org.nrg.xft.exception.XFTInitException
	 * @throws org.nrg.xft.exception.ElementNotFoundException
	 */
	private Node itemToNode(XFTItem item, Document doc,String alias, boolean isRoot, boolean allowSchemaLocation,String location, boolean withPrefix, boolean limited) throws org.nrg.xft.exception.XFTInitException,org.nrg.xft.exception.ElementNotFoundException,FieldNotFoundException
	{

		XMLWrapperElement element = (XMLWrapperElement)XFTMetaManager.GetWrappedElementByName(XMLWrapperFactory.GetInstance(),item.getXSIType());

		Node main = null;

		Hashtable aliases = new Hashtable();

		if ((alias != null) && (!alias.equalsIgnoreCase("")))
		{
		    if(alias.indexOf(":")!=-1)
		    {
		        if (withPrefix)
		        {
					main = doc.createElement(alias);
					aliases.put(alias,element.getName());
		        }else{
		            alias = alias.substring(alias.indexOf(":")+1);
					main = doc.createElement(alias);
					aliases.put(alias,element.getName());
		        }

		    }else{
		        if (withPrefix)
		        {
			        String tempAlias = element.getSchemaTargetNamespacePrefix() +":" + alias;
					main = doc.createElement(tempAlias);
					aliases.put(tempAlias,element.getName());
		        }else{
		            main = doc.createElement(alias);
					aliases.put(alias,element.getName());
		        }
		    }
		}else
		{
		    if (withPrefix)
		    {
				alias = element.getFullXMLName();
	            main = doc.createElement(alias);
				aliases.put(alias,element.getName());
		    }else{
		        alias = element.getName();
	            main = doc.createElement(alias);
				aliases.put(alias,element.getName());
		    }
		}

		//ADD SCHEMA SPECIFICATION ATTRIBUTES
		if (allowSchemaLocation && isRoot)
		{
			Node attribute = doc.createAttribute("xsi:schemaLocation");
			attribute.setNodeValue(XFT.GetAllSchemaLocations(location));
//			if (location == null)
//				attribute.setNodeValue(element.getSchema().getTargetNamespaceURI()+" "+element.getSchema().getDataModel().getFullFileSpecification());
//			else
//				attribute.setNodeValue(element.getSchema().getTargetNamespaceURI()+" "+location + element.getSchema().getDataModel().getFolderName() + "/" + element.getSchema().getDataModel().getFileName());
			main.getAttributes().setNamedItem(attribute);

			attribute = doc.createAttribute("xmlns:xsi");
			attribute.setNodeValue("http://www.w3.org/2001/XMLSchema-instance");
			main.getAttributes().setNamedItem(attribute);


			Enumeration enumer = XFTMetaManager.getPrefixEnum();
			while(enumer.hasMoreElements())
			{
			    String prefix = (String)enumer.nextElement();
			    String uri = XFTMetaManager.TranslatePrefixToURI(prefix);

			    attribute = doc.createAttribute("xmlns:"+prefix);
				attribute.setNodeValue(uri);
				main.getAttributes().setNamedItem(attribute);
			}
		}

		Iterator attributes = element.getAttributes().iterator();
		while (attributes.hasNext())
		{
			XMLWrapperField attField = (XMLWrapperField)attributes.next();
			if (attField.isReference())
			{
					XFTItem ref = (XFTItem)item.getProperty(attField.getId());
					if (ref != null)
					{
					    if((!limited) || (!ref.canBeRootWithBase()))
					    {
					        Node node =itemToNode(ref,doc,attField.getName(),false,allowSchemaLocation,location,withPrefix,limited);
					        CopyFields(node,main);
					    }
					}
			}else
			{
				Node attr = getAttribute(item,doc,attField,withPrefix);
				if (attr != null)
				{
					if (attr.getNodeValue() != null)
					{
						if (! attr.getNodeValue().equalsIgnoreCase(""))
						{
							main.getAttributes().setNamedItem(attr);
						}
					}
				}
			}
		}

		if (! element.isANoChildElement())
		{
			Iterator childElements = element.getChildren().iterator();
			while(childElements.hasNext())
			{
				XMLWrapperField xwf = (XMLWrapperField)childElements.next();
				if (xwf.getExpose())
				{
				    getChildElement(item,doc,xwf,main,aliases,withPrefix,limited);

				}
			}
		}else
		{
			Iterator childElements = element.getChildren().iterator();
			while(childElements.hasNext())
			{
				XMLWrapperField xwf = (XMLWrapperField)childElements.next();
				if (xwf.getExpose())
				{
				     main = addChild(item,xwf,main,doc);

				}
			}
		}

		return main;
	}


	/**
	 * Gets the value from the XFTItem and assigns it to a new attribute XML DOM Node.
	 * @param item
	 * @param doc
	 * @param xmlAttr
	 * @return Returns the new attribute XML DOM Node
	 */
	private Node getAttribute(XFTItem item, Document doc, XMLWrapperField xmlAttr, boolean withPrefix) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		Node attribute = doc.createAttribute(xmlAttr.getName(false));
		Object o = item.getProperty(xmlAttr.getId());
		if (o != null)
		{
			attribute.setNodeValue(o.toString());
			return attribute;
		}else{
		    if (xmlAttr.isRequired())
		    {
		        return attribute;
		    }else{
		        return null;
		    }
		}
	}

	/**
	 * @param item
	 * @param doc
	 * @param xmlField
	 * @param parent
	 * @param aliases
	 * @return Returns parent object after modification
	 * @throws org.nrg.xft.exception.ElementNotFoundException
	 */
	private Node getChildElement(XFTItem item, Document doc, XMLWrapperField xmlField, Node parent,Hashtable aliases,boolean withPrefix,boolean limited) throws org.nrg.xft.exception.ElementNotFoundException,XFTInitException,FieldNotFoundException
	{
		if (xmlField.isReference())
		{
			if (xmlField.isMultiple())
			{
			    if (item.isPreLoaded())
			    {
			        int counter = 0;
					ArrayList children =  item.getChildItems(xmlField);
					Iterator iter = children.iterator();
					int child_count = children.size();
					while (iter.hasNext())
					{
						Object o = iter.next();


						if (o.getClass().getName().equalsIgnoreCase("org.nrg.xft.XFTItem"))
						{
							try {
								XFTItem many1 = (XFTItem)o;
								long startTime = Calendar.getInstance().getTimeInMillis();
								if ((!limited) || (!many1.canBeRootWithBase()))
								{
									Node node =itemToNode(many1,doc,xmlField.getName(withPrefix),false,true,null,withPrefix,limited);
									if (! xmlField.getXMLType().getFullForeignType().equalsIgnoreCase(many1.getXSIType()))
									{
										Node attribute = doc.createAttribute("xsi:type");
										attribute.setNodeValue(many1.getGenericSchemaElement().getFullXMLName());
										node.getAttributes().setNamedItem(attribute);
									}
									if (xmlField.isInLineRepeaterElement())
									{
										CopyFields(node,parent);
									}else
									{
										parent.appendChild(node);
									}
								}

								if (many1.getXSIType().equals("xnat:subjectData"))
								{
									if(XFT.VERBOSE)System.out.println(counter + " of " + child_count + " Subjects (" +((float) (Calendar.getInstance().getTimeInMillis()-startTime)/1000) + "s)");
								}
							} catch (DOMException e) {
								log.error("", e);
							} catch (org.nrg.xft.exception.XFTInitException e) {
								log.error("", e);
							}
						}
						counter++;
					}
			    }else{
				    GenericWrapperElement foreign = (GenericWrapperElement)xmlField.getReferenceElement();
					int counter = 0;
					ArrayList children = item.getChildItemIds(xmlField,item.getUser());

					Iterator iter = children.iterator();
					int child_count = children.size();
					while (iter.hasNext())
					{
						ArrayList o = (ArrayList)iter.next();

						try {
	                        long startTime = Calendar.getInstance().getTimeInMillis();
	                        XFTItem many1 = XFTItem.SelectItemByIds(foreign, o.toArray(),item.getUser(),true,xmlField.getPreventLoop());
	                        if ((!limited) || (!many1.canBeRootWithBase()))
	                        {
	                        	Node node =itemToNode(many1,doc,xmlField.getName(withPrefix),false,true,null,withPrefix,limited);
	                        	if (! xmlField.getXMLType().getFullForeignType().equalsIgnoreCase(many1.getXSIType()))
	                        	{
	                        		Node attribute = doc.createAttribute("xsi:type");
	                        		attribute.setNodeValue(many1.getGenericSchemaElement().getFullXMLName());
	                        		node.getAttributes().setNamedItem(attribute);
	                        	}
	                        	if (xmlField.isInLineRepeaterElement())
	                        	{
	                        		CopyFields(node,parent);
	                        	}else
	                        	{
	                        		parent.appendChild(node);
	                        	}
	                        }

	                        if (many1.getXSIType().equals("xnat:subjectData"))
	                        {
	                        	if(XFT.VERBOSE)System.out.println(counter + " of " + child_count + " Subjects (" +((float) (Calendar.getInstance().getTimeInMillis()-startTime)/1000) + "s)");
	                        }

	                        many1.clear();

	                        counter++;
	                    } catch (DOMException e) {
	                        log.error("", e);
	                    } catch (Exception e) {
	                        log.error("", e);
	                    }
					}
			    }

				//CLEAR PROCESSED CHILDREN
				item.clearChildren(xmlField);
				return parent;
			}else
			{
				if (item.getProperty(xmlField.getId()) != null)
				{
					if (item.getProperty(xmlField.getId()) instanceof XFTItem)
					{
					    XFTItem child = (XFTItem)item.getProperty(xmlField.getId());
					    if ((!limited) || (!child.canBeRootWithBase()))
					    {
							try {
								if(XFTPseudonymManager.IsAnAlias(xmlField.getSQLName().toLowerCase(),parent.getNodeName()))
								{
									Node temp=itemToNode(child,doc,xmlField.getName(withPrefix),false,true,null,withPrefix,limited);
									parent =CopyFields(temp,parent);
								}else if((aliases.get(parent.getNodeName()) != null) && (XFTPseudonymManager.IsAnAlias(xmlField.getSQLName().toLowerCase(),(String)aliases.get(parent.getNodeName()))))
								{
									Node temp=itemToNode(child,doc,xmlField.getName(withPrefix),false,true,null,withPrefix,limited);
									parent =CopyFields(temp,parent);
								}else if (! xmlField.isChildXMLNode())
								{
									Node temp=itemToNode(child,doc,xmlField.getName(withPrefix),false,true,null,withPrefix,limited);
									parent =CopyFields(temp,parent);
								}else
								{
								    Node temp=itemToNode(child,doc,xmlField.getName(withPrefix),false,true,null,withPrefix,limited);


									if (! xmlField.getXMLType().getFullForeignType().equalsIgnoreCase(child.getXSIType()))
									{
										Node attribute = doc.createAttribute("xsi:type");
										attribute.setNodeValue(child.getGenericSchemaElement().getFullXMLName());
										temp.getAttributes().setNamedItem(attribute);
									}

									parent.appendChild(temp);
								}
							} catch (DOMException e) {
								log.error("", e);
							} catch (org.nrg.xft.exception.XFTInitException e) {
								log.error("", e);
							}
//
//							//CLEAR PROCESSED CHILDREN
//							item.clearChildren(xmlField);
							return parent;
					    }
					}
				}
				if (! xmlField.getWrapped().getMinOccurs().equalsIgnoreCase("0"))
				{
					parent.appendChild(doc.createElement(xmlField.getName(withPrefix)));
				}
				return parent;
			}
		}

		//NOT A REFERENCE
		Node main = doc.createElement(xmlField.getName(withPrefix));
		Iterator attributes = xmlField.getAttributes().iterator();
		while (attributes.hasNext())
		{
			XMLWrapperField x = (XMLWrapperField)attributes.next();
			if (x.getXMLType().getLocalType().equals("string"))
			{
				Node attr = getAttribute(item,doc,x,withPrefix);
				if (attr != null)
					main.getAttributes().setNamedItem(attr);
			}else{
			    if(item.getProperty(x.getId())!=null)
			    {
			        Node attr = getAttribute(item,doc,x,withPrefix);
					if (attr != null)
						main.getAttributes().setNamedItem(attr);
			    }
			}
		}

		if (xmlField.getChildren().size() > 0)
		{
			Iterator childElements = xmlField.getChildren().iterator();
			while(childElements.hasNext())
			{
				XMLWrapperField xwf = (XMLWrapperField)childElements.next();
				if (xwf.getExpose())
				{
					getChildElement(item,doc,xwf,main,aliases,withPrefix,limited);
				}
			}
		}else
		{
			main = addChild(item,xmlField,main,doc);
		}

		if ((parent != null) && (NodeHasValue(main)))
			parent.appendChild(main);

		return main;
	}

	/**
	 * Copies sub nodes from the 'from' to the 'to' node.
	 * @param from
	 * @param to
	 * @return Returns the 'to' Node after fields have been copied to it
	 */
	private static Node CopyFields(Node from, Node to)
	{
		while (from.getChildNodes().getLength() > 0)
		{
			Node child = from.getChildNodes().item(0);
			child = from.removeChild(child);
			to.appendChild(child);
		}

		while (from.getAttributes().getLength() > 0)
		{
			Node attr = from.getAttributes().item(0);
			attr = from.getAttributes().removeNamedItem(attr.getNodeName());
			to.getAttributes().setNamedItem(attr);
		}

		return to;
	}

	/**
	 * If node has child nodes or attributes then true.
	 * @param node
	 * @return Returns whether the node has child nodes or attributes
	 */
	private static boolean NodeHasValue(Node node)
	{
		if (node.getChildNodes().getLength() > 0)
		{
			return true;
		}else if (node.getAttributes().getLength() > 0)
		{
			return true;
		}else
		{
			return false;
		}
	}

	/**
	 * Assigns value to a text node in the main document.
	 * @param item
	 * @param field
	 * @param main
	 * @param doc
	 * @return Returns main node after appending the specified doc child node
	 */
	private Node addChild(XFTItem item,XMLWrapperField field, Node main, Document doc) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
//		if (field.hasBase())
//		{
//			if (item.findSubValue(field.getBaseElement(),field.getBaseCol()) != null)
//			{
//				String value = ValueParser(item.findSubValue(field.getBaseElement(),field.getBaseCol()),field);
//				if (! value.equalsIgnoreCase(""))
//				{
//					main.appendChild(doc.createTextNode(value));
//				}
//			}
//		}else if (field.getXMLType()==null){
	    if (field.getXMLType()==null){
		}else if((item.getProperty(field.getId()) != null))
		{
			String value = ValueParser(item.getProperty(field.getId()),field);
			if (! value.equalsIgnoreCase(""))
			{
				main.appendChild(doc.createTextNode(value));
			}
		}
		return main;
	}

	/**
	 * Formats data types to XML output string
	 * @param o
	 * @return Returns formatted data type
	 */
	public static String ValueParser(Object o,String type)
	{
        return ValueParser(o,type,null,null);
    }

	/**
	 * Formats data types to XML output string
	 * @param o
	 * @return Returns formatted data type
	 */
	@SuppressWarnings("deprecation")
    public static String ValueParser(Object o,String type,String appendRootPath,String relativizePath)
	{
	    if (o.getClass().getName().equalsIgnoreCase("[B"))
		{
			byte[] b = (byte[]) o;
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			try {
				baos.write(b);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return baos.toString();
		}
			if (type.equalsIgnoreCase("string"))
			{
				//return StringEscapeUtils.escapeXml(o.toString()); caused duplicate escaping, SAXWriter does parsing
				return o.toString();
			}else if (type.equalsIgnoreCase("boolean"))
			{
				if (o.toString().equalsIgnoreCase("true")|| o.toString().equalsIgnoreCase("1"))
				{
					return "1";
				}else
				{
					return "0";
				}
			}else if (type.equalsIgnoreCase("float"))
			{
				return o.toString();
			}else if (type.equalsIgnoreCase("anyURI"))
            {
                if (appendRootPath==null && relativizePath==null)
                    return o.toString();
                else if(relativizePath !=null)
                {
                    String uri = o.toString();
                    uri= uri.replace('\\', '/');
                    return uri.substring(uri.indexOf(relativizePath) + relativizePath.length());
                }
                else
                {
                    return FileUtils.AppendRootPath(appendRootPath,o.toString()).replace('\\', '/');
                }
			}else if (type.equalsIgnoreCase("double"))
			{
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
				return o.toString();
			}else if (type.equalsIgnoreCase("decimal"))
			{
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
				return o.toString();
			}else if (type.equalsIgnoreCase("integer"))
			{
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
				return o.toString();
			}else if (type.equalsIgnoreCase("nonPositiveInteger"))
			{
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
				return o.toString();
			}else if (type.equalsIgnoreCase("negativeInteger"))
			{
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
				return o.toString();
			}else if (type.equalsIgnoreCase("long"))
			{
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
				return o.toString();
			}else if (type.equalsIgnoreCase("int"))
			{
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
				return o.toString();
			}else if (type.equalsIgnoreCase("short"))
			{
				return o.toString();
			}else if (type.equalsIgnoreCase("byte"))
			{
				return o.toString();
			}else if (type.equalsIgnoreCase("nonNegativeInteger"))
			{
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
				return o.toString();
			}else if (type.equalsIgnoreCase("unsignedLong"))
			{
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
				return o.toString();
			}else if (type.equalsIgnoreCase("unsignedInt"))
			{
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
				return o.toString();
			}else if (type.equalsIgnoreCase("unsignedShort"))
			{
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
				return o.toString();
			}else if (type.equalsIgnoreCase("unsignedByte"))
			{
				return o.toString();
			}else if (type.equalsIgnoreCase("positiveInteger"))
			{
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
				return o.toString();
			}else if (type.equalsIgnoreCase("time"))
			{
				return o.toString();
			}else if (type.equalsIgnoreCase("date"))
			{
                if (o instanceof String)
                {
                    try {
                        java.util.Date d = DateUtils.parseDate((String)o);
                        o=d;
                    } catch (ParseException e) {
                        log.error("", e);
                    }
                }

                if (o instanceof java.util.Date)
                {
                    java.util.Date d = (java.util.Date)o;
                    StringBuffer sb = new StringBuffer();
                    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyy-MM-dd");
                    sb.append(formatter.format(d));
                    return sb.toString();
                }else if (o instanceof java.sql.Date)
                {
                    java.sql.Date d = (java.sql.Date)o;
                    StringBuffer sb = new StringBuffer();
                    sb.append(d.getYear());
                    sb.append("-");
                    sb.append(d.getMonth());
                    sb.append("-");
                    sb.append(d.getDate());
                    return sb.toString();
                }else if (o instanceof java.sql.Timestamp)
                {
                    java.util.Date d = (java.util.Date)o;
                    StringBuffer sb = new StringBuffer();
                    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyy-MM-dd");
                    sb.append(formatter.format(d));
                    return sb.toString();
                }
                return o.toString();
			}else if (type.equalsIgnoreCase("dateTime"))
			{
                if (o instanceof String)
                {
                    try {
                        java.util.Date d = DateUtils.parseDateTime((String)o);
                        o=d;
                    } catch (ParseException e) {
                        log.error("", e);
                    }
                }

				if (o.getClass().getName().equalsIgnoreCase("java.util.Date"))
				{
                    java.util.Date d = (java.util.Date)o;
                    StringBuffer sb = new StringBuffer();
                    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
                    sb.append(formatter.format(d));
                    return sb.toString();
				}else if (o.getClass().getName().equalsIgnoreCase("java.sql.Date"))
				{
					java.sql.Date d = (java.sql.Date)o;
					StringBuffer sb = new StringBuffer();
					sb.append(d.getYear());
					sb.append("-");
					sb.append(d.getMonth());
					sb.append("-");
					sb.append(d.getDate());
					sb.append("T");
					sb.append(d.getHours());
					sb.append(":");
					sb.append(d.getMinutes());
					sb.append(":");
					sb.append(d.getSeconds());
					return sb.toString();
				}else if (o.getClass().getName().equalsIgnoreCase("java.sql.Timestamp"))
				{
					java.util.Date d = (java.util.Date)o;
					StringBuffer sb = new StringBuffer();
					java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
					sb.append(formatter.format(d));
					return sb.toString();
				}
				return o.toString();
			}else
			{
				return o.toString();
			}
	}

	/**
	 * Formats data types to XML output string
	 * @param o
	 * @return Returns formatted data type
	 */
	public static String ValueParser(Object o,XMLWrapperField field)
	{
		if (o.getClass().getName().equalsIgnoreCase("[B"))
		{
			byte[] b = (byte[]) o;
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			try {
				baos.write(b);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return baos.toString();
		}
		if (field != null)
		{
			String type = field.getXMLType().getLocalType();
            return ValueParser(o,type);
				}else
				{
				return o.toString();
                    }
                }

    /**
     * Formats data types to XML output string
     * @param o
     * @return Returns formatted data type
     */
    public static String ValueParser(Object o,XMLWrapperField field,String appendRootPath,String relativizePath)
    {
        if (o.getClass().getName().equalsIgnoreCase("[B"))
                {
            byte[] b = (byte[]) o;
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    try {
                baos.write(b);
            } catch (IOException e) {
                e.printStackTrace();
                    }
            return baos.toString();
                }
        if (field != null)
				{
            String type = field.getXMLType().getLocalType();
            if (field.getWrapped().getRule()!=null && field.getWrapped().getRule().getBaseType() !=null)
            {
                if(field.getWrapped().getRule().getBaseType().equals("xs:anyURI")){
                    return ValueParser(o,"anyURI",appendRootPath,relativizePath);
                }
            }
            return ValueParser(o,type,appendRootPath,relativizePath);
		}else
		{
			return o.toString();
		}
	}

	/**
	 * Creates a XML DOM Document with a root element of type &#60;List&#62; and the items
	 * as a sub nodes.
	 * @param list
	 * @return Returns the created XML DOM Document
	 */
	public static Document XFTItemListToDOM(List list,boolean limited)
	{
		final XMLWriter writer = new XMLWriter();
		final Document doc = writer._builder.newDocument();
		try {
			final Element root = doc.createElement("List");
			doc.appendChild(root);
			for (final Object object : list) {
				final XFTItem item = (XFTItem) object;
				root.appendChild(writer.itemToNode(item, doc, item.getProperName(), false, false, null, false, limited));
			}
		} catch (XFTInitException e) {
			log.error("An error occurred accessing XFT", e);
		} catch (Exception e) {
			log.error("An unexpected error occurred", e);
		}

		return doc;
	}

	/**
	 * Creates a XML DOM Document with a root element of type &#60;List&#62; and the items
	 * as a sub nodes.
	 * @param list
	 * @return Returns the created XML DOM Document
	 */
	public static Document XFTItemListToDOM(List list)
	{
		return XFTItemListToDOM(list,false);
	}

	private DocumentBuilder _builder = null;
	private Document        doc      = null;
}

