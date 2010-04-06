//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Oct 11, 2004
 */
package org.nrg.xft.utils;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.nrg.xft.references.XFTPseudonymManager;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Tim
 */
public class NodeUtils {
	static org.apache.log4j.Logger logger = Logger.getLogger(NodeUtils.class);

	public static String GetAttributeValue(NamedNodeMap nnm, String name,String defaultValue)
	{
		if (nnm != null)
		{
			Node temp = nnm.getNamedItem(name);
			if (temp != null)
			{
				return temp.getNodeValue();
			}
		}
		
		return defaultValue;
	}

	public static String GetAttributeValue(Node node, String name,String defaultValue)
	{
		if (node == null)
		{
			return defaultValue;
		}
		return NodeUtils.GetAttributeValue(node.getAttributes(),name,defaultValue);
	}

	public static boolean GetBooleanAttributeValue(Node node, String name,boolean defaultValue)
	{
		if (node == null)
		{
			return defaultValue;
		}
		String s=  NodeUtils.GetAttributeValue(node.getAttributes(),name,"");
		if (s.equalsIgnoreCase("true"))
		{
			return true;
		}else if (s.equalsIgnoreCase("")){
			return defaultValue;
		}else{
			return false;
		}
	}

	public static String GetCleanedAttributeValue(NamedNodeMap nnm, String name,String defaultValue)
	{
		if (nnm != null)
		{
			Node temp = nnm.getNamedItem(name);
			if (temp != null)
			{
				String s =  temp.getNodeValue();
				if (s.indexOf(":") != -1)
				{
					return s.substring(s.indexOf(":")+1);
				}else
				{
					return s;
				}
			}
		}
		
		return defaultValue;
	}

	public static Node GetGroupNode(Node n)
	{
		Node group = null;
		if (n.getNodeName().indexOf("group") != -1)
		{
			return n;
		}else
		{
			for(int i=0;i<n.getChildNodes().getLength();i++)
			{
				Node child = GetGroupNode(n.getChildNodes().item(i));
				if (child != null)
				{
					return child;
				}
			}
		}
		return group;
	}
	
	public static boolean CheckXMLNodeName(String nodeName, String compareTo, Hashtable aliases)
	{
		if (nodeName.equalsIgnoreCase(compareTo))
		{
			return true;
		}else
		{
		    String s1 = compareTo;
		    String s2 = nodeName;
		    if (s1.indexOf(":")!=-1 || s2.indexOf(":")!=-1)
		    {
		        if (s1.indexOf(":")!=-1){
		            s1=s1.substring(s1.indexOf(":")+1);
		        }
		        if (s2.indexOf(":")!=-1){
		            s2=s2.substring(s2.indexOf(":")+1);
		        }
		        
		        if (s1.equalsIgnoreCase(s2)){
					return true;
		        }
		    }
		    
			if (! XFTPseudonymManager.HasPseudonyms(compareTo))
			{
				String temp = nodeName;
				if (temp.indexOf(":")!=-1)
				{
				    temp = temp.substring(temp.indexOf(":")+1);
				}
				ArrayList checked = new ArrayList();
				while (aliases.get(temp) != null)
				{
					temp = (String)aliases.get(temp);
					if (checked.contains(temp))
					{
						break;
					}else if (compareTo.equalsIgnoreCase(temp))
					{
						return true;
					}else
					{
					    s1 = compareTo;
					    s2 = temp;
					    if (s1.indexOf(":")!=-1 || s2.indexOf(":")!=-1)
					    {
					        if (s1.indexOf(":")!=-1){
					            s1=s1.substring(s1.indexOf(":")+1);
					        }
					        if (s2.indexOf(":")!=-1){
					            s2=s2.substring(s2.indexOf(":")+1);
					        }
					        
					        if (s1.equalsIgnoreCase(s2)){
								return true;
					        }
					    }
					}
					checked.add(temp);
					if (temp.indexOf(":")!=-1)
					{
					    temp = temp.substring(temp.indexOf(":")+1);
					}
				}
				return false;
			}else
			{
				ArrayList pseudonyms = XFTPseudonymManager.GetPseudonyms(compareTo);
				if (pseudonyms.contains(nodeName))
				{
					return true;
				}else
				{
					String temp = nodeName;
					if (temp.indexOf(":")!=-1)
					{
					    temp = temp.substring(temp.indexOf(":")+1);
					}
					
					if (pseudonyms.contains(temp))
					{
					    return true;
					}
					
					ArrayList checked = new ArrayList();
					while (aliases.get(temp) != null)
					{
						temp = (String)aliases.get(temp);
						if (checked.contains(temp))
						{
							break;
						}else if (compareTo.equalsIgnoreCase(temp))
						{
							return true;
						}else
						{
						    s1 = compareTo;
						    s2 = temp;
						    if (s1.indexOf(":")!=-1 || s2.indexOf(":")!=-1)
						    {
						        if (s1.indexOf(":")!=-1){
						            s1=s1.substring(s1.indexOf(":")+1);
						        }
						        if (s2.indexOf(":")!=-1){
						            s2=s2.substring(s2.indexOf(":")+1);
						        }
						        
						        if (s1.equalsIgnoreCase(s2)){
									return true;
						        }
						    }
						}
						
						checked.add(temp);
						if (temp.indexOf(":")!=-1)
						{
						    temp = temp.substring(temp.indexOf(":")+1);
						}
					}
					return false;
				}
			}
		}
	}
	
	public static String GetSubNodeValue(Node node,String item, ArrayList layers,boolean isAttribute, Hashtable aliases)
	{
		String value = null;
		java.util.Iterator iter = layers.iterator();
		Node child = node;
		while (iter.hasNext())
		{
			String level = (String)iter.next();
			String nodeName = child.getNodeName();
			if (! CheckXMLNodeName(nodeName,level,aliases))
			{
				child = NodeUtils.GetLevel1Child(child,level);
			}
			if (child == null)
			{
				logger.debug("NodeUtils::GetSubNodeValue::NODE NOT FOUND:" + level);
				break;
			}
		}
		
		if (child != null)
		{
			if (isAttribute)
			{
				value = NodeUtils.GetAttributeValue(child,item,"");
				if (value.equalsIgnoreCase(""))
				{
				    if (item.indexOf(":")!=-1)
				    {
				        item = item.substring(item.indexOf(":")+1);
				        value = NodeUtils.GetAttributeValue(child,item,"");
				    }
				}
			}else
			{
				Node valueNode = NodeUtils.GetLevelNNode(child,item,1,new Hashtable());
//				Node valueNode = NodeUtils.GetLevel1Child(child,item);
				value = GetNodeText(valueNode);
			}
		}
		
		return value;
	}
	
	public static String GetNodeText(Node n)
	{
		String value = null;
		if (n != null)
		{
			Node text = n.getFirstChild();
			if (text != null)
			{
				value = text.getNodeValue();
			}
		}
		return value;
	}
	
	public static ArrayList<Node> GetLevelNNodes(Node node,String name,int level)
	{
		ArrayList<Node> al = new ArrayList<Node>();
		if (node.hasChildNodes())
		{
			for(int k =0; k<node.getChildNodes().getLength();k++)
			{ 
				Node child1 = node.getChildNodes().item(k);
				//LEVEL 1
				if (level <= 1)
				{
					if (child1.getNodeName().equalsIgnoreCase(name))
					{
						al.add(child1);
					}
				}else
				{
					al = GetLevelNNodes(child1,name,level-1);
					if (al.size() > 0)
					{
						break;
					}
				}
			}
		}
		return al;
	}
	
	public static Node GetLevelNNode(Node node,String name,int level,Hashtable aliases)
	{
		Node found = null;
		if (node.hasChildNodes())
		{
			for(int k =0; k<node.getChildNodes().getLength();k++)
			{ 
				Node child1 = node.getChildNodes().item(k);
				//LEVEL 1
				if (level <= 1)
				{
					if (child1.getNodeName().equalsIgnoreCase(name))
					{
						found = child1;
						break;
					}
					String temp = name;
					if (temp.indexOf(":")!=-1)
					{
					    temp = temp.substring(temp.indexOf(":")+1);
					}
					ArrayList checked = new ArrayList();
					while (aliases.get(temp) != null)
					{
						temp = (String)aliases.get(temp);
						if (checked.contains(temp))
						{
							break;
						}else if (child1.getNodeName().equalsIgnoreCase(temp))
						{
							found = child1;
							break;
						}else
						{
						    String s1 = node.getNodeName();
						    String s2 = temp;
						    if (s1.indexOf(":")!=-1 || s2.indexOf(":")!=-1)
						    {
						        if (s1.indexOf(":")!=-1){
						            s1=s1.substring(s1.indexOf(":")+1);
						        }
						        if (s2.indexOf(":")!=-1){
						            s2=s2.substring(s2.indexOf(":")+1);
						        }
						        
						        if (s1.equalsIgnoreCase(s2)){
						            found = node;
									break;
						        }
						    }
						}
						checked.add(temp);
						if (temp.indexOf(":")!=-1)
						{
						    temp = temp.substring(temp.indexOf(":")+1);
						}
					}
					if (found==null)
					{
						java.util.Iterator pseudos = XFTPseudonymManager.GetPseudonyms(name).iterator();
						while (pseudos.hasNext())
						{
							String alias = (String)pseudos.next();
							if (child1.getNodeName().equalsIgnoreCase(alias))
							{
								found = child1;
								break;
							}
						}
					}
					if (found != null)
					{
						break;
					}
				}else
				{
					found = GetLevelNNode(child1,name,level-1,aliases);
					if (found != null)
					{
						break;
					}
				}
			}
		}
		
		//Check for field with same name as element
		if (found == null)
		{
			if (node.getNodeName().equalsIgnoreCase(name))
			{
				found = node;
			}else{
				String temp = name;
				if (temp.indexOf(":")!=-1)
				{
				    temp = temp.substring(temp.indexOf(":")+1);
				}
				ArrayList checked = new ArrayList();
				while (aliases.get(temp) != null)
				{
					temp = (String)aliases.get(temp);
					if (checked.contains(temp))
					{
						break;
					}else if (node.getNodeName().equalsIgnoreCase(temp))
					{
						found = node;
						break;
					}else
					{
					    String s1 = node.getNodeName();
					    String s2 = temp;
					    if (s1.indexOf(":")!=-1 || s2.indexOf(":")!=-1)
					    {
					        if (s1.indexOf(":")!=-1){
					            s1=s1.substring(s1.indexOf(":")+1);
					        }
					        if (s2.indexOf(":")!=-1){
					            s2=s2.substring(s2.indexOf(":")+1);
					        }
					        
					        if (s1.equalsIgnoreCase(s2)){
					            found = node;
								break;
					        }
					    }
					}
					checked.add(temp);
					if (temp.indexOf(":")!=-1)
					{
					    temp = temp.substring(temp.indexOf(":")+1);
					}
				}
				if (found == null)
				{
					java.util.Iterator pseudos = XFTPseudonymManager.GetPseudonyms(name).iterator();
					while (pseudos.hasNext())
					{
						String alias = (String)pseudos.next();
						if (node.getNodeName().equalsIgnoreCase(alias))
						{
							found = node;
							break;
						}
					}
				}
			}
		}
		return found;
	}
	
	public static Object GetLevelNAttributeValue(Node node,String name,int level)
	{
		Object found = null;
		if (node.hasChildNodes())
		{
			for(int k =0; k<node.getChildNodes().getLength();k++)
			{ 
				Node child1 = node.getChildNodes().item(k);
				//LEVEL 1
				if (level <= 1)
				{
					found = NodeUtils.GetAttributeValue(node,name,null);
					if (found != null)
					{
						break;
					}
				}else if (level == 2)
				{
					found = NodeUtils.GetAttributeValue(child1,name,null);
					if (found != null)
					{
						break;
					}
				}else
				{
					found = GetLevelNAttributeValue(child1,name,level-1);
					if (found != null)
					{
						break;
					}
				}
			}
		}
		return found;
	}
	
	public static Node GetLevel1Child(Node node,String name)
	{
		Node found = null;
		if (node.hasChildNodes())
		{
			for(int k =0; k<node.getChildNodes().getLength();k++)
			{ 
				//LEVEL 1
				if (node.getChildNodes().item(k).getNodeName().equalsIgnoreCase(name))
				{
					found = node.getChildNodes().item(k);
					break;
				}else{
				    if (name.indexOf(":")!=-1)
				    {
				        if (node.getChildNodes().item(k).getNodeName().equalsIgnoreCase(name.substring(name.indexOf(":")+1)))
						{
							found = node.getChildNodes().item(k);
							break;
						}
				    }
				}
			}
		}
	
		return found;
	}

	public static Node GetLevel4Child(Node node,String name)
	{
		Node found = null;
		if (node.hasChildNodes())
		{
			for(int k =0; k<node.getChildNodes().getLength();k++)
			{ 
				//LEVEL 1
				Node child1 = node.getChildNodes().item(k);
				if (child1.hasChildNodes())
				{
					for(int j =0; j<child1.getChildNodes().getLength();j++)
					{ 
						//LEVEL 2
						Node child2 = child1.getChildNodes().item(j);
						if (child2.hasChildNodes())
						{
							for(int p =0; p<child2.getChildNodes().getLength();p++)
							{ 
								//LEVEL3
								Node child3 = child2.getChildNodes().item(p);
								if (child3.hasChildNodes())
								{
									for(int l =0; l<child3.getChildNodes().getLength();l++)
									{ 
										if (child3.getChildNodes().item(l).getNodeName().equalsIgnoreCase(name))
										{
											found = child3.getChildNodes().item(l);
											break;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		return found;
	}

	public static Node GetLevel3Child(Node node,String name)
	{
		Node found = null;
		if (node.hasChildNodes())
		{
			for(int k =0; k<node.getChildNodes().getLength();k++)
			{ 
				//LEVEL 1
				Node child1 = node.getChildNodes().item(k);
				for(int j =0; j<child1.getChildNodes().getLength();j++)
				{ 
					//LEVEL 2
					Node child2 = child1.getChildNodes().item(j);
					for(int p =0; p<child2.getChildNodes().getLength();p++)
					{ 
						//LEVEL3
						if (child2.getChildNodes().item(p).getNodeName().equalsIgnoreCase(name))
						{
							found = child2.getChildNodes().item(p);
							break;
						}
					}
				}
			}
		}
		
		return found;
	}

	public static Node GetLevel2Child(Node node,String name)
	{
		Node found = null;
		if (node.hasChildNodes())
		{
			for(int k =0; k<node.getChildNodes().getLength();k++)
			{ 
				//LEVEL 1
				Node child1 = node.getChildNodes().item(k);
				for(int j =0; j<child1.getChildNodes().getLength();j++)
				{ 
                    Node child2=child1.getChildNodes().item(j);
					//LEVEL 2
					if (child2.getNodeName().equalsIgnoreCase(name))
					{
						found = child1.getChildNodes().item(j);
						break;
					}
				}
			}
		}
	
		return found;
	}

	public static boolean NodeHasName(Node n)
	{
		if (n.getAttributes() != null)
			return NodeHasName(n.getAttributes());
		else
			return false;
	}

	public static String GetNodeName(Node n)
	{
		if (NodeHasName(n))
		{
			return n.getAttributes().getNamedItem("name").getNodeValue();
		}else
		{
			return "";
		}
	}

	public static boolean NodeHasName(NamedNodeMap nnm)
	{
		if (nnm.getNamedItem("name") != null)
		{
			return true;
		}else
		{
			return false;
		}
	}

	public static boolean HasAttribute(Node node, String name)
	{
		boolean found = false;
		NamedNodeMap nnm = node.getAttributes();
		if (nnm != null)
		{
			Node temp = nnm.getNamedItem(name);
			if (temp != null)
			{
				return true;
			}
		}
		
		return found;
	}
	
	public static Node GetNextNode(Node node)
	{
		Node sibling1 = node.getNextSibling();
		return sibling1.getNextSibling();
	}
	
	public static Node CreateAttributeNode(org.w3c.dom.Document doc, String name, String value)
	{
		Node attr = doc.createAttribute(name);
		attr.setNodeValue(value);
		return attr;
	}
	
	public static Node CreateTextNode(Document doc,String name, String value)
	{
		Node child = doc.createElement(name);
		Node text = doc.createTextNode(value);
		child.appendChild(text);
		return child;
	}
	
	public static Node CreateEmptyNode(Document doc,String name)
	{
		Node child = doc.createElement(name);
		return child;
	}

    @SuppressWarnings("unused")
    private static boolean HasComplexExtension(Node complexType,String prefix)
    {    	
    	Node extension = GetLevel2Child(complexType,prefix +":extension");
    	if (extension==null)
    	{
    	    return false;
    	}else{
    	    return ExtensionHasAddOns(extension);
    	}
    	
    }
    
    public static boolean ExtensionHasAddOns(Node extension)
    {
        boolean _return = false;
        NodeList children = extension.getChildNodes();
    	for (int i=0;i<children.getLength();i++)
    	{
    		Node child = children.item(i);
    		if (child.getNodeName().indexOf("sequence") != -1 || child.getNodeName().indexOf("attribute") != -1)
    		{
    			_return = true;
    			break;
    		}
    	}
    	return _return;
    }

    /**
     * If Node has a child named complexType
     * @param node
     * @return
     */
    public static boolean NodeHasComplexContent(Node node, String prefix)
    {
    	boolean _return = false;
    	
    	NodeList children = node.getChildNodes();
    	for (int i=0;i<children.getLength();i++)
    	{
    		Node child = children.item(i);
    		if (child.getNodeName().indexOf("complexType") != -1)
    		{
    		    Node extension = GetLevel2Child(child,prefix +":extension");
    		    if (extension == null)
    		    {
    		        _return = true;
        			break;
    		    }else{    			
    		        if (NodeUtils.ExtensionHasAddOns(extension))
    		        {
    		            return true;
    		        }else{
    		            return false;
    		        }
    		    }
    		}
    	}
    	
    	return _return;
    }
}

