//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Oct 25, 2004
 */
package org.nrg.xft.schema.Wrappers.XMLWrapper;

//import java.util.ArrayList;
//import java.util.Hashtable;
//import java.util.Iterator;
//
//import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

/**
 * Class used to translate an XML DOM document to an XFTItem.
 * 
 * @author Tim
 */
public class XMLReader {
	static org.apache.log4j.Logger logger = Logger.getLogger(XMLReader.class);
//	
//	/**
//	 * Translates the given XML DOM to an XFTItem with its populated sub-items.
//	 * @param doc
//	 * @return
//	 * @throws org.nrg.xft.exception.XFTInitException
//	 * @throws ElementNotFoundException
//	 * @throws FieldNotFoundException
//	 */
//	public static XFTItem TranslateDomToItem(Document doc,UserI user) throws org.nrg.xft.exception.XFTInitException,ElementNotFoundException,FieldNotFoundException
//	{
//		XFTItem item = XFTItem.NewEmptyItem(user);
//		
//		Element root = doc.getDocumentElement();
//		String xmlns = null;
//		
//		Hashtable uriToPrefixMapping = new Hashtable();
//		NamedNodeMap attributes = root.getAttributes();
//		for (int i = 0;i<attributes.getLength();i++)
//		{
//		    Node attribute = attributes.item(i);
//		    if (attribute.getNodeName().equalsIgnoreCase("xmlns"))
//		    {
//		        xmlns = attribute.getNodeValue();
//		    }else{
//		        if (attribute.getNodeName().indexOf("xmlns") != -1)
//		        {
//		            String prefix = attribute.getNodeName().substring(attribute.getNodeName().indexOf(":") + 1);
//		            String value = attribute.getNodeValue();
//		            uriToPrefixMapping.put(value,prefix);
//		        }
//		    }
//		}
//		
//		GenericWrapperElement element = GenericWrapperElement.GetElement(root.getNodeName(),xmlns);
//		
//		if (xmlns==null)
//		    xmlns = "";
//		if (element != null)
//		{
//			Hashtable elementTypes = new Hashtable();
//			Hashtable expectedNames = new Hashtable();
//			if (! root.getNodeName().equalsIgnoreCase(element.getLocalXMLName()))
//			{
//			    String prefix = (String)uriToPrefixMapping.get(element.getSchemaTargetNamespaceURI());
//			    if (prefix == null)
//			    {
//					elementTypes.put(root.getNodeName(),element.getType().getLocalType());
//					expectedNames.put(element.getType().getLocalType(),root.getNodeName());
//			    }else{
//					elementTypes.put(root.getNodeName(),prefix + ":" + element.getType().getLocalType());
//					expectedNames.put(prefix + ":" + element.getType().getLocalType(),root.getNodeName());
//					
//			        String nodeName = root.getNodeName();
//			        if (nodeName.indexOf(":")!=-1)
//			        {
//			            nodeName = nodeName.substring(nodeName.indexOf(":")+1);
//			        }
//			        elementTypes.put(nodeName,element.getType().getLocalType());
//					expectedNames.put(element.getType().getLocalType(),nodeName);
//			    }
//			}
//			item = TranslateNodeToItem(root,element,elementTypes,expectedNames,uriToPrefixMapping,xmlns,user);
//		}
//		
//		return item;
//	}
//	
////	private static XFTItem TranslateXPATHNodeToItem(Node node, GenericWrapperElement element,Hashtable uriToPrefixMapping, String targetURI, UserI user) throws org.nrg.xft.exception.XFTInitException,ElementNotFoundException,FieldNotFoundException
////	{
////	    if (NodeUtils.HasAttribute(node,"xsi:type"))
////		{
////		    String prefix = (String)uriToPrefixMapping.get(element.getSchemaTargetNamespaceURI());
////			String xsiType = NodeUtils.GetAttributeValue(node,"xsi:type","");
////			element = GenericWrapperElement.GetElement(xsiType);
////			node.getAttributes().removeNamedItem("xsi:type");
////		}
////		XFTItem item = XFTItem.NewItem(element,user);
////		LOADED_ITEMS.add(item);
////		
////		String uri = element.getSchemaTargetNamespaceURI();
////		String prefix = null;
////		if (uri.equalsIgnoreCase(targetURI))
////		{
////		   prefix = null; 
////		}else{
////		    prefix = (String)uriToPrefixMapping.get(uri);
////		}
////		
////		Iterator fields = element.getAllFields().iterator();
////		while (fields.hasNext())
////		{
////		    GenericWrapperField field = (GenericWrapperField) fields.next();
////		    if (field.isReference())
////		    {
////		        GenericWrapperElement sub = (GenericWrapperElement)field.getReferenceElement();
////		        if (field.isChildXMLNode())
////				{
////				    NodeList nl = null;
////				    try {
////				        String xpath = field.getXPATH();
////                        nl = XPathAPI.selectNodeList(node,xpath);
////                    } catch (TransformerException e1) {
////                        logger.error("",e1);
////                    }
////					
////                    if (nl !=null && nl.getLength()>0)
////                    {
////                        for(int i = 0;i<nl.getLength();i++)
////                        {
////                            Node tempNode = nl.item(i);
////							ItemI temp = TranslateXPATHNodeToItem(tempNode,sub,uriToPrefixMapping,targetURI,user);
////							if (temp != null)
////							{
////								item.setChild(field,temp);
////							}else
////							{
////								try {
////									if (sub.getAddin().equalsIgnoreCase(""))
////										throw new FieldNotFoundException((String)field.getXPATH());
////								} catch (FieldNotFoundException e) {
////									if (field.isRequired())
////									{
////										logger.error("ERROR: Missing REQUIRED element '" + e.FIELD + "' as child of '" + element.getFullXMLName() +"'");
////										throw e;
////									}else{
////										logger.debug("WARNING: Missing element '" + e.FIELD + "' as child of '" + element.getFullXMLName() +"'");
////									}
////								}
////							}
////                        }
////                    }
////				}else{
////					Iterator subItems = FindRootLevelMultipleElements(sub,node,uriToPrefixMapping,targetURI,user).iterator();
////					int counter = 0;
////					while (subItems.hasNext())
////					{
////						ItemI temp = (ItemI)subItems.next();
////						item.setChild(field,temp);
////					}
////				}
////		    }else{
////		        String xPath = field.getXPATH();
////		        try {
////                    Node child = XPathAPI.selectSingleNode(node,xPath);
////                    Object v = null;
////                    if (child.getNodeType()== Node.ATTRIBUTE_NODE)
////                    {
////                        v = child.getNodeValue();
////                    }else{
////                        v = NodeUtils.GetNodeText(child);
////                    }
////					if (v != null)
////					{
////						if (! v.toString().equalsIgnoreCase(""))
////						{
////							item.setFieldValue(field.getSQLName(),v);
////						}
////					}
////					else
////					{
////						logger.debug("WARNING: Missing field(" + field.getXPATH() + ") in xml element(" + element.getFullXMLName() + ")");	
////					}
////                } catch (TransformerException e) {
////                    logger.error("",e);
////                }
////		    }
////		}
////		return item;
////	}
//
//	/**
//	 * Uses the XMLFieldData from the getXMLFields of GenericWrapperField to 
//	 * translate the XML DOM Node to an XFTItem.
//	 * @param node
//	 * @param element
//	 * @param elementTypes
//	 * @param expectedNames
//	 * @return
//	 * @throws org.nrg.xft.exception.XFTInitException
//	 * @throws ElementNotFoundException
//	 * @throws FieldNotFoundException
//	 */
//	private static XFTItem TranslateNodeToItem(Node node,GenericWrapperElement element,Hashtable elementTypes,Hashtable expectedNames,Hashtable uriToPrefixMapping, String targetURI, UserI user) throws org.nrg.xft.exception.XFTInitException,ElementNotFoundException,FieldNotFoundException
//	{
//		if (NodeUtils.HasAttribute(node,"xsi:type"))
//		{
//		    String prefix = (String)uriToPrefixMapping.get(element.getSchemaTargetNamespaceURI());
//			String xsiType = NodeUtils.GetAttributeValue(node,"xsi:type","");
////			if (prefix ==null)
////			{
//				elementTypes.put(element.getLocalXMLName(),xsiType);
//				expectedNames.put(xsiType,element.getLocalXMLName());
////			}else{
////				elementTypes.put(element.getLocalXMLName(),xsiType);
////				expectedNames.put(xsiType,element.getLocalXMLName());
////			}
//			element = GenericWrapperElement.GetElement(xsiType);
//			node.getAttributes().removeNamedItem("xsi:type");
//		}
//		XFTItem item = XFTItem.NewItem(element,user);
//		
//		String uri = element.getSchemaTargetNamespaceURI();
//		String prefix = null;
//		if (uri.equalsIgnoreCase(targetURI))
//		{
//		   prefix = null; 
//		}else{
//		    prefix = (String)uriToPrefixMapping.get(uri);
//		}
//
//		Iterator fields = element.getXMLFields(uriToPrefixMapping).iterator();
//		while (fields.hasNext())
//		{
//			Hashtable subElementTypes = (Hashtable)elementTypes.clone();
//			Hashtable subExpectedNames = (Hashtable)expectedNames.clone();
//			XMLFieldData field = (XMLFieldData)fields.next();
//logger.debug("Loading field(" + field.getXmlFieldName(prefix) + "-" + field.getXmlType() + ") from xml element(" + element.getFullXMLName() + ")");
//			if (field.isReference())
//			{
//				if (!(field.getXmlFieldName(null)).equals(field.getXmlType().getLocalType()))
//				{
//				    //FIELD NAME is different then referenced element type
//					if (!field.getXmlFieldName(null).equals(element.getLocalXMLName()))
//					{
//					   String subPrefix = null;
//					   if (uri.equals(field.getXmlType().getForeignXMLNS()))
//					   {
//					       subPrefix = prefix;
//					   }else{
//					       if (targetURI.equalsIgnoreCase(field.getXmlType().getForeignXMLNS()))
//					       {
//					           subPrefix = null;
//					       }else{
//					           subPrefix = (String)uriToPrefixMapping.get(field.getXmlType().getForeignXMLNS());
//					       }
//					   }
//					   
////					   if (subPrefix==null && prefix ==null)
////				        {
//					       subElementTypes.put(field.getXmlFieldName(null),(field.getXmlType().getLocalType()));
//							if (subExpectedNames.get(field.getXmlType().getLocalType()) == null)
//								subExpectedNames.put(field.getXmlType().getLocalType(),field.getXmlFieldName(null));
////				        }else if (subPrefix==null)
////				        {
////				            subElementTypes.put(field.getXmlFieldName(prefix),(field.getXmlType().getLocalType()));
////							if (subExpectedNames.get(field.getXmlType().getLocalType()) == null)
////								subExpectedNames.put(field.getXmlType().getLocalType(),field.getXmlFieldName(prefix));
////				        }else if (prefix==null){
////				            subElementTypes.put(field.getXmlFieldName(prefix),subPrefix + ":" + (field.getXmlType().getLocalType()));
////							if (subExpectedNames.get(field.getXmlType().getLocalType()) == null)
////								subExpectedNames.put(field.getXmlType().getLocalType(),field.getXmlFieldName(prefix));
////				        }else{
////				            subElementTypes.put(field.getXmlFieldName(prefix),subPrefix + ":" + (field.getXmlType().getLocalType()));
////							if (subExpectedNames.get(field.getXmlType().getLocalType()) == null)
////								subExpectedNames.put(field.getXmlType().getLocalType(),field.getXmlFieldName(prefix));
////				        }
//					   
////					   if (prefix==null)
////					    {
////							subElementTypes.put(field.getXmlFieldName(prefix),(field.getXmlType().getLocalType()));
////							if (subExpectedNames.get(field.getXmlType().getLocalType()) == null)
////								subExpectedNames.put(field.getXmlType().getLocalType(),field.getXmlFieldName(prefix));
////					    }else{
////					        subElementTypes.put(field.getXmlFieldName(prefix),prefix + ":" + (field.getXmlType().getLocalType()));
////							if (subExpectedNames.get(prefix + ":" + field.getXmlType().getLocalType()) == null)
////								subExpectedNames.put(prefix + ":" + field.getXmlType().getLocalType(),field.getXmlFieldName(prefix));
////					    }
//					}
//				}
//			}
//			if (field.isReference())
//			{
//				//IS REFERENCE
//				if (field.isMultiple())
//				{
//					//IS MULTIPLE
//					//GenericWrapperElement sub = (GenericWrapperElement)XFTManager.GetInstance().getWrappedElementByName(GenericWrapperFactory.GetInstance(),((XMLType)field.getXmlType()).getFullLocalType(),null);
//					GenericWrapperElement sub = GenericWrapperElement.GetElement((XMLType)field.getXmlType());
//					if (field.isChildXMLNode())
//					{
//					    NodeList nl = null;
//					    try {
//					        String xpath = field.getField().getXPATH();
//                            nl = XPathAPI.selectNodeList(node,xpath);
//                        } catch (TransformerException e1) {
//                            logger.error("",e1);
//                        }
//						
//                        if (nl !=null && nl.getLength()>0)
//                        {
//	                        for(int i = 0;i<nl.getLength();i++)
//	                        {
//	                            Node tempNode = nl.item(i);
//								ItemI temp = TranslateNodeToItem(tempNode,sub,subElementTypes,subExpectedNames,uriToPrefixMapping,targetURI,user);
//								if (temp != null)
//								{
//									item.setChild(field.getField(),temp,false);
//								}else
//								{
//									try {
//										if (sub.getAddin().equalsIgnoreCase(""))
//											throw new FieldNotFoundException((String)field.getXmlFieldName(prefix));
//									} catch (FieldNotFoundException e) {
//										if (field.isRequired())
//										{
//											logger.error("ERROR: Missing REQUIRED element '" + e.FIELD + "' as child of '" + element.getFullXMLName() +"'");
//											throw e;
//										}else{
//											logger.debug("WARNING: Missing element '" + e.FIELD + "' as child of '" + element.getFullXMLName() +"'");
//										}
//									}
//								}
//	                        }
//                        }else{
//                            // OLD CODE ... SWITCHED TO XPATH (ABOVE)
////    						Iterator nodeIter = NodeUtils.GetLevelNNodes(node,(String)field.getXmlFieldName(prefix),field.getLevels().intValue()).iterator();
////    						int counter = 0;
////    						while (nodeIter.hasNext())
////    						{
////    							Node tempNode = (Node)nodeIter.next();
////    							ItemI temp = TranslateNodeToItem(tempNode,sub,subElementTypes,subExpectedNames,uriToPrefixMapping,targetURI,user);
////    							if (temp != null)
////    							{
////    								item.setChild(field.getField(),temp,false);
////    							}else
////    							{
////    								try {
////    									if (sub.getAddin().equalsIgnoreCase(""))
////    										throw new FieldNotFoundException((String)field.getXmlFieldName(prefix));
////    								} catch (FieldNotFoundException e) {
////    									if (field.isRequired())
////    									{
////    										logger.error("ERROR: Missing REQUIRED element '" + e.FIELD + "' as child of '" + element.getFullXMLName() +"'");
////    										throw e;
////    									}else{
////    										logger.debug("WARNING: Missing element '" + e.FIELD + "' as child of '" + element.getFullXMLName() +"'");
////    									}
////    								}
////    							}
////    						}
//                        }
//					}else{
//						Iterator subItems = FindRootLevelMultipleElements(sub,node,uriToPrefixMapping,targetURI,user).iterator();
//						int counter = 0;
//						while (subItems.hasNext())
//						{
//							ItemI temp = (ItemI)subItems.next();
//							item.setChild(field.getField(),temp,false);
//						}
//					}
//				}else{
//					//IS NOT MULTIPLE
//					//GenericWrapperElement sub = (GenericWrapperElement)XFTManager.GetInstance().getWrappedElementByName(GenericWrapperFactory.GetInstance(),((XMLType)field.getXmlType()).getFullLocalType(),null);
//					GenericWrapperElement sub = GenericWrapperElement.GetElement((XMLType)field.getXmlType());
//					if ((! field.isChildXMLNode()) || field.isAttribute())
//					{
////					    if (uriToPrefixMapping.size()==0)
////					    {
//							subElementTypes.put(element.getType().getLocalType(),sub.getType().getLocalType());
//							subExpectedNames.put(sub.getType().getLocalType(),element.getType().getLocalType());
////					    }else{
////					        String subPrefix = (String)uriToPrefixMapping.get(sub.getSchemaTargetNamespaceURI());
////					        if (subPrefix==null && prefix ==null)
////					        {
////								subElementTypes.put(element.getType().getLocalType(),sub.getType().getLocalType());
////								subExpectedNames.put(sub.getType().getLocalType(),element.getType().getLocalType());
////					        }else if (subPrefix==null)
////					        {
////								subElementTypes.put(element.getType().getLocalType(),sub.getType().getLocalType());
////								subExpectedNames.put(sub.getType().getLocalType(),prefix + ":" + element.getType().getLocalType());
////					        }else if (prefix==null){
////								subElementTypes.put(element.getType().getLocalType(),subPrefix + ":" + sub.getType().getLocalType());
////								subExpectedNames.put(sub.getType().getLocalType(),element.getType().getLocalType());
////					        }else{
////								subElementTypes.put(element.getType().getLocalType(),subPrefix + ":" + sub.getType().getLocalType());
////								subExpectedNames.put(sub.getType().getLocalType(),prefix + ":" + element.getType().getLocalType());
////					        }
////					    }
//						ItemI temp = TranslateNodeToItem(node,sub,subElementTypes,subExpectedNames,uriToPrefixMapping,targetURI,user);
//						if (temp != null)
//						{
//							item.setChild(field.getField(),temp,false);
//						}else
//						{
//							try {
//								if (sub.getAddin().equalsIgnoreCase(""))
//									throw new FieldNotFoundException((String)field.getXmlFieldName(prefix));
//							} catch (FieldNotFoundException e) {
//								if (field.isRequired())
//								{
//									logger.error("ERROR: Missing REQUIRED element '" + e.FIELD + "' as child of '" + element.getFullXMLName() +"'");
//									throw e;
//								}else{
//									logger.debug("WARNING: Missing element '" + e.FIELD + "' as child of '" + element.getFullXMLName() +"'");
//								}
//							}
//						}
//					}else
//					{
//					    Node tempNode=null;
//                        try {
//                            tempNode = XPathAPI.selectSingleNode(node,field.getField().getXPATH());
//                        } catch (TransformerException e1) {
//                            logger.error("",e1);
//                        }
//                        if (tempNode ==null)
//						    tempNode = NodeUtils.GetLevelNNode(node,(String)field.getXmlFieldName(prefix),field.getLevels().intValue(),subExpectedNames);
//						if (tempNode != null)
//						{
//						    // OLD CODE
//							ItemI temp = TranslateNodeToItem(tempNode,sub,subElementTypes,subExpectedNames,uriToPrefixMapping,targetURI,user);
//							if (temp != null)
//							{
//								item.setChild(field.getField(),temp,false);
//							}else
//							{
//								if (field.isRequired())
//								{
//									try {
//										if (sub.getAddin().equalsIgnoreCase(""))
//											throw new FieldNotFoundException((String)field.getXmlFieldName(prefix));
//									} catch (FieldNotFoundException e) {
//										if (field.isRequired())
//										{
//											logger.error("ERROR: Missing REQUIRED element '" + e.FIELD + "' as child of '" + element.getFullXMLName() +"'");
//											throw e;
//										}else{
//											logger.debug("WARNING: Missing element '" + e.FIELD + "' as child of '" + element.getFullXMLName() +"'");
//										}
//									}
//								}
//							}
//						}else
//						{
//							try {
//								if (sub.getAddin().equalsIgnoreCase(""))
//									throw new FieldNotFoundException((String)field.getXmlFieldName(prefix));
//							} catch (FieldNotFoundException e) {
//								if (field.isRequired())
//								{
//									logger.error("ERROR: Missing REQUIRED element '" + e.FIELD + "' as child of '" + element.getFullXMLName() +"'");
//									throw e;
//								}else{
//									logger.debug("WARNING: Missing element '" + e.FIELD + "' as child of '" + element.getFullXMLName() +"'");
//								}
//							}
//						}
//					}
//				}
//			}else
//			{
//				if (field.isExtension())
//				{
//					String s = NodeUtils.GetNodeText(node);
//				
//					if (s != null)
//					{
//						if (! s.equalsIgnoreCase(""))
//						{
//							item.setFieldValue(field.getSqlName(),s);
//						}
//					}
//					else
//					{
//						logger.debug("WARNING: Missing field(" + field.getXmlFieldName(prefix) + "-" + field.getXmlType() + ") in xml element(" + element.getFullXMLName() + ")");
//					}
//				}else{
//					String s = NodeUtils.GetSubNodeValue(node,(String)field.getXmlFieldName(prefix),field.getLayers(),field.isAttribute(),subElementTypes);
//									
//					if (s != null)
//					{
//						if (! s.equalsIgnoreCase(""))
//						{
//							item.setFieldValue(field.getSqlName(),s);
//						}
//					}
//					else
//					{ 
//					    try {
//					        String xpath = field.getField().getXPATH();
//                            Node n = XPathAPI.selectSingleNode(node,xpath);
//                            
//                            if (n != null)
//                            {
//                                if (n.getNodeType()== Node.ATTRIBUTE_NODE)
//                                {
//                                    s = n.getNodeValue();
//                                }else{
//                                    s = NodeUtils.GetNodeText(n);
//                                }
//                            }else{
//                                
//                            }
//                        } catch (TransformerException e) {
//                            logger.error("",e);
//                        }
//                        if (s != null)
//    					{
//    						if (! s.equalsIgnoreCase(""))
//    						{
//    							item.setFieldValue(field.getSqlName(),s);
//    						}
//    					}
//    					else
//    					{ 
//    						logger.debug("WARNING: Missing field(" + field.getXmlFieldName(prefix) + "-" + field.getXmlType() + ") in xml element(" + element.getFullXMLName() + ")");
//    					}
//					}
//				}
//			}
//		}
//		return item;
//	}
//	
//	/**
//	 * @param element
//	 * @param node
//	 * @return
//	 * @throws org.nrg.xft.exception.XFTInitException
//	 * @throws ElementNotFoundException
//	 * @throws FieldNotFoundException
//	 */
//	private static ArrayList FindRootLevelMultipleElements(GenericWrapperElement element,Node node, Hashtable uriToPrefixMapping,String targetURI, UserI user) throws org.nrg.xft.exception.XFTInitException,ElementNotFoundException,FieldNotFoundException
//	{
//		ArrayList items = new ArrayList();
//		
//		ArrayList level1Names = new ArrayList();
//
//		String uri = element.getSchemaTargetNamespaceURI();
//		String prefix = (String)uriToPrefixMapping.get(uri);
//		
//		ArrayList xmlFields = element.getXMLFields(uriToPrefixMapping);
//		Iterator fields = xmlFields.iterator();
//		while (fields.hasNext())
//		{
//			XMLFieldData field = (XMLFieldData)fields.next();
//			if (field.getLevels().intValue()==1)
//			{
//				level1Names.add(field.getXmlFieldName(prefix));
//			}
//		}
//		
//		String firstChild = (String)level1Names.get(0);
//		Node child = NodeUtils.GetLevel1Child(node,firstChild);
//		
//		while (child != null)
//		{
//			ArrayList foundNames = new ArrayList();
//			Node created = node.getOwnerDocument().createElement(element.getLocalXMLName());
//			while(child != null && (! foundNames.contains(child.getNodeName())))
//			{
//				foundNames.add(child.getNodeName());
//				Node temp = child.cloneNode(true);
//				created.appendChild(temp);
//				child = NodeUtils.GetNextNode(child);
//				
//				if (child != null)
//				{
//					if (! level1Names.contains(child.getNodeName()))
//					{
//						child = null;
//					}
//				}
//			}
//			
//			ItemI item = TranslateNodeToItem(created,element,new Hashtable(),new Hashtable(),uriToPrefixMapping,targetURI,user);
//			items.add(item);
//		}
//		
//		items.trimToSize();
//		return items;
//	}
}

