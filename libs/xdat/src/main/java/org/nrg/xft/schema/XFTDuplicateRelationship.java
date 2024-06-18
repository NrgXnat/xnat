/*
 * core: org.nrg.xft.schema.XFTDuplicateRelationship
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema;

import org.nrg.xft.utils.NodeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * @author Tim
 *
 */
public class XFTDuplicateRelationship {
    private String elementName = null;
    private String id = null;
    private String foreignField = null;
    private String localField = null;
    private Hashtable fieldMappings = new Hashtable();
    
    public XFTDuplicateRelationship(Node node)
    {
        elementName = NodeUtils.GetAttributeValue(node,"elementName","");
        id = NodeUtils.GetAttributeValue(node,"id","");
        foreignField = NodeUtils.GetAttributeValue(node,"foreignField","");
        localField = NodeUtils.GetAttributeValue(node,"localField","");
        
        Iterator iter = NodeUtils.GetLevelNNodes(node,"xdat:FieldMapping",1).iterator();
		while(iter.hasNext())
		{
		    Node child1 = (Node)iter.next();
		    String l = NodeUtils.GetAttributeValue(child1,"localField","");
		    String f = NodeUtils.GetAttributeValue(child1,"foreignField","");
		    fieldMappings.put(l,f);
		}
    }
    /**
     * @return Returns the elementName.
     */
    public String getElementName() {
        return elementName;
    }
    /**
     * @param elementName The elementName to set.
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }
    
    /**
     * @param localField Dot-syntax
     * @param foreignField Dot-syntax
     */
    public void addFieldMapping(String localField,String foreignField)
    {
        fieldMappings.put(localField,foreignField);
    }
    
    /**
     * @return Returns the fieldMappings.
     */
    public Hashtable getFieldMappings() {
        return fieldMappings;
    }
	
	/**
	 * @param doc
	 * @return Returns the XML representation of the document, represented in a Node object
	 */
	public Node toXML(Document doc)
	{
		Node main = doc.createElement("duplicateRelationship");
		
		if (getElementName() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"elementName",this.getElementName()));
		
		if (getId() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"id",this.getId()));
		
		Enumeration enumer = fieldMappings.keys();
		while (enumer.hasMoreElements())
		{
			Node props = doc.createElement("fieldMapping");
		    String s = (String)enumer.nextElement();
		    props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"localField",s));
		    props.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"foreignField",(String)fieldMappings.get(s)));

			main.appendChild(props);
		}
		return main;
	}
    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }
    /**
     * @param id The id to set.
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return Returns the foreignField.
     */
    public String getForeignField() {
        return foreignField;
    }
    /**
     * @param foreignField The foreignField to set.
     */
    public void setForeignField(String foreignField) {
        this.foreignField = foreignField;
    }
    /**
     * @return Returns the localField.
     */
    public String getLocalField() {
        return localField;
    }
    /**
     * @param localField The localField to set.
     */
    public void setLocalField(String localField) {
        this.localField = localField;
    }
}
