//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Nov 29, 2004
 *
 */
package org.nrg.xft.schema;
import java.io.File;
import java.util.Hashtable;

import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.utils.NodeUtils;
import org.nrg.xft.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The purpose of this class is to organize and maintain
 * access to every XML root node in every XML schema.
 * 
 * <BR><BR>When a new schema is loaded, the first thing it does 
 * is create a NodeWrapper for each root node in 
 * the schema.  The Node Wrapper is created using the static
 * AddNode method.  It uses the XFTSchema's definedPrefix and
 * the node's name to create an XMLType for the node.  It maintains
 * a reference to the XFTSchema and stores the name of the
 * corresponding XFTElement (if one was created).  This NodeWrapper
 * is then added to the ALL_NODES hashtable which maintains a list of
 * all loaded nodes with their XMLType.FullXMLType as the key value.<BR><BR>
 * 
 * When a schema is first loaded, the nodeWrappers are created before 
 * the XFTElements are created.  Thus, initially all NodeWrappers are initially
 * created with null values in the element parameter.  After the XFTElements are
 * loaded the NodeWrappers are overwritten by NodeWrappers with the correct
 * element name specified.
 * 
 * @author Tim
 *
 */
public class NodeWrapper {
	
	/**
	 * static Hashtable which maintains a collection of all of the loaded NodeWrappers.  It uses
	 * the elements full local xml type (local prefix + type) as the key.
	 */
	public final static Hashtable ALL_NODES = new Hashtable();
	
	/**
	 * Adds a new NodeWrapper to the ALL_NODES collection of loaded nodes
	 * using the input variables.  The schema's definedPrefix is used with the
	 * name variable to create the node's XMLType.
	 * 
	 * @param name name of root level node
	 * @param element name of the created element
	 * @param s
	 */
	public static void AddNode(String name,String element,XFTSchema s)
	{
		NodeWrapper nw = new NodeWrapper();
		XMLType t = new XMLType(name,s);
		nw.setName(t);
		nw.setSchema(s);
		nw.setCorrespondingElement(element);
		ALL_NODES.put(t.getFullLocalType(),nw);
	}
	
	
	private NodeWrapper()
	{}
		
	/**
	 * Finds a NodeWrapper based on its XMLType full name (schema defined prefix + ":" + node name)
	 * @param name full XMLType to find
	 * @return full NodeWrapper (or null, if not found)
	 */
	public static NodeWrapper FindNode(String name)
	{
		return (NodeWrapper)ALL_NODES.get(name);
	}

	/**
	 * Finds the specific XML DOM node based on an item's node name.  The schema is used, if 
	 * the node does not already contain the schema's defined prefix.
	 * 
	 * @param name name of node to search for
	 * @param s xftSchema which the node belongs to
	 * @return XML DOM node org.w3c.dom.Node
	 */
	public static Node FindNode(String name,XFTSchema s)
	{
		if (name.indexOf(":") != -1)
		{
		    String pre = XMLType.GetPrefix(name);
		    if (pre.equalsIgnoreCase(s.getTargetNamespacePrefix()))
		    {
		        if((NodeWrapper)ALL_NODES.get(name) != null)
				{
					return ((Node)((NodeWrapper)ALL_NODES.get(name)).getNode());
				}else
				{
				    
					return null;
				}
		    }else{
		        String post = XMLType.CleanType(name);
			    
			    String uri = s.getURIForPrefix(pre);
			    if (uri==null)
			    {
			        //NO CHANGE
			    }else{
				    String newPre = XFTMetaManager.TranslateURIToPrefix(uri);
				    if (newPre==null)
				    {
				        //NO CHANGE
				    }else if (newPre.equalsIgnoreCase(""))
				    {
				        name = post;
				    }else{
				        name = newPre + ":" + post;
				    }
			    }
			    
			    if((NodeWrapper)ALL_NODES.get(name) != null)
				{
					return ((Node)((NodeWrapper)ALL_NODES.get(name)).getNode());
				}else
				{
				    
					return null;
				}
		    }
		    
			
		}else
		{
		    if (!s.getTargetNamespacePrefix().equalsIgnoreCase(""))
			   name = s.getTargetNamespacePrefix()+":"+name;
			if((NodeWrapper)ALL_NODES.get(name) != null)
			{
				return ((Node)((NodeWrapper)ALL_NODES.get(name)).getNode());
			}else
			{
				return null;
			}
		}
	}
	private String correspondingElement = null;
	private XMLType name = null;
	private XFTSchema s = null;
	/**
	 * Returns the name of the corresponding XFTElement
	 * @return name of corresponding XFTElement (or null)
	 */
	public String getCorrespondingElement() {
		return correspondingElement;
	}

	/**
	 * Returns the XMLType of the wrapped node
	 * @return XMLType of the node
	 */
	public XMLType getName() {
		return name;
	}

	/**
	 * Returns the XML DOM Node.<BR><BR>
	 * 
	 * Using the XFTSchema, this method loads the schema DOM from its file and finds the selected Node to return.
	 * @return XML DOM node org.w3c.dom.Node
	 */
	public Node getNode() {
		if (s != null)
		{
			File f = new File(s.getDataModel().getFileLocation() + s.getDataModel().getFileName());
			Document doc;
			if(f.exists()){
				doc = XMLUtils.GetDOM(f);
			}else{
				String path=s.getDataModel().getFileLocation();
				if(path.endsWith(File.separator)){
					path=path.substring(0,path.length()-1);
				}
				doc = XMLUtils.GetDOM(getClass().getClassLoader().getResourceAsStream(path));
			}
			
			Element root = doc.getDocumentElement();
			NodeList elements = root.getChildNodes();
		
			//Load empty NodeWrappers
			for(int i =0; i<elements.getLength();i++)
			{
				Node element = elements.item(i);
				if (NodeUtils.NodeHasName(element))
				{
					if (NodeUtils.GetNodeName(element).equalsIgnoreCase(this.getName().getLocalType()))
					{
						return element;	
					}
				}
			}
			return null;
		}else
		{
			return null;
		}
	}

	/**
	 * Returns the NodeWrapper's XFTSchema
	 * @return XFTSchema for this node
	 */
	public XFTSchema getSchema() {
		return s;
	}

	/**
	 * Assisgn a new corresponding XFTElement name to this NodeWrapper
	 * @param element XFTElement xmlName
	 */
	public void setCorrespondingElement(String element) {
		correspondingElement = element;
	}

	/**
	 * Assign a XMLType to this NodeWrapper
	 * @param type
	 */
	public void setName(XMLType type) {
		name = type;
	}


	/**
	 * Assign a XFTSchema to this Schema.<BR><BR>
	 * 
	 * This schema will be used to assign a definedPrefix to the XMLType and for finding the actual Node in its DOM document.
	 * @param schema
	 */
	public void setSchema(XFTSchema schema) {
		s = schema;
	}
}

