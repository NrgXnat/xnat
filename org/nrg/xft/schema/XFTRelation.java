//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 22, 2004
 */
package org.nrg.xft.schema;

import java.util.ArrayList;
import java.util.Iterator;

import org.nrg.xft.utils.NodeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
/**
 * This class details additional information about an XFTReferenceField, usually maps
 * to a &#60;xft:relation&#62; tag.
 * 
 * @author Tim
 */
public class XFTRelation {
    public static String DEFAULT_RELATION_TYPE = "single";
	private String foreignKeyName = "";
	private String foreignKeyTable = "";
	private String foreignCol = "";
	private String onDelete = "";
	private String onUpdate = "";
	private String relationType = "";
	private ArrayList duplicateRelationships = new ArrayList();
	private boolean unique=false;
	private String uniqueComposite="";
	
	private String relationName = null;
	
	/**
	 * @return Returns the unique.
	 */
	public boolean isUnique() {
		return unique;
	}
	/**
	 * @param unique The unique to set.
	 */
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	/**
	 * @return Returns the uniqueComposite.
	 */
	public String getUniqueComposite() {
		return uniqueComposite;
	}
	/**
	 * @param uniqueComposite The uniqueComposite to set.
	 */
	public void setUniqueComposite(String uniqueComposite) {
		this.uniqueComposite = uniqueComposite;
	}
	
	public XFTRelation(){}
	
	/**
	 * Sets the values of the XFTRelation based on the attributes of the supplied node.
	 * @param node
	 */
	public XFTRelation(Node node)
	{
		foreignKeyName = NodeUtils.GetAttributeValue(node,"foreignKeyName","");
		foreignKeyTable = NodeUtils.GetAttributeValue(node,"foreignKeyTable","");
		foreignCol = NodeUtils.GetAttributeValue(node,"foreignCol","");
		onDelete = NodeUtils.GetAttributeValue(node,"onDelete","");
		onUpdate = NodeUtils.GetAttributeValue(node,"onUpdate","");
		relationType = NodeUtils.GetAttributeValue(node,"relationType","");
		unique = NodeUtils.GetBooleanAttributeValue(node,"unique",false);
		uniqueComposite = NodeUtils.GetAttributeValue(node,"uniqueComposite","");
		relationName = NodeUtils.GetAttributeValue(node,"relationName",null);
		
		Iterator iter = NodeUtils.GetLevelNNodes(node,"xdat:duplicateRelationship",1).iterator();
		while(iter.hasNext())
		{
		    Node child1 = (Node)iter.next();
		    XFTDuplicateRelationship dup = new XFTDuplicateRelationship(child1);
		    duplicateRelationships.add(dup);
		}
	}
	
	public XFTRelation clone(XFTElement e)
	{
		XFTRelation clone = new XFTRelation();
		clone.setForeignCol(this.getForeignCol());
		clone.setForeignKeyName(this.getForeignKeyName());
		clone.setForeignKeyTable(this.getForeignKeyTable());
		clone.setOnDelete(this.getOnDelete());
		clone.setOnUpdate(this.getOnUpdate());
		clone.setRelationType(this.getRelationType());
		clone.setUnique(this.isUnique());
		clone.setUniqueComposite(this.getUniqueComposite());
		return clone;
	}
	
	/**
	 * @return
	 */
	public String getForeignCol() {
		return foreignCol;
	}

	/**
	 * @return
	 */
	public String getForeignKeyName() {
		return foreignKeyName;
	}

	/**
	 * @return
	 */
	public String getForeignKeyTable() {
		return foreignKeyTable;
	}

	/**
	 * @return
	 */
	public String getOnDelete() {
		return onDelete;
	}

	/**
	 * @return
	 */
	public String getOnUpdate() {
		return onUpdate;
	}

	/**
	 * @return
	 */
	public String getRelationType() {
		if (relationType == null || relationType.equalsIgnoreCase(""))
		{
		    return DEFAULT_RELATION_TYPE;
		}else{
		    return relationType;
		}		
	}

	/**
	 * @param string
	 */
	public void setForeignCol(String string) {
		foreignCol = string;
	}

	/**
	 * @param string
	 */
	public void setForeignKeyName(String string) {
		foreignKeyName = string;
	}

	/**
	 * @param string
	 */
	public void setForeignKeyTable(String string) {
		foreignKeyTable = string;
	}

	/**
	 * @param string
	 */
	public void setOnDelete(String string) {
		onDelete = string;
	}

	/**
	 * @param string
	 */
	public void setOnUpdate(String string) {
		onUpdate = string;
	}

	/**
	 * @param string
	 */
	public void setRelationType(String string) {
		relationType = string;
	}
	


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append("\nXDATRelation\n");
		sb.append("foreignKeyName:").append(this.getForeignKeyName()).append("\n");
		sb.append("ForeignKeyTable").append(this.getForeignKeyTable()).append("\n");
		sb.append("ForeignCol:").append(this.getForeignCol()).append("\n");
		sb.append("OnDelete:").append(this.getOnDelete()).append("\n");
		sb.append("OnUpdate:").append(this.getOnUpdate()).append("\n");
		sb.append("RelationType:").append(this.getRelationType()).append("\n");
		sb.append("DuplicateRelationships:").append(this.getDuplicateRelationships()).append("\n");

		return sb.toString();
	}

	/**
	 * @param header
	 * @return
	 */
	public String toString(String header)
	{
		java.lang.StringBuffer sb = new StringBuffer();
		sb.append("\n").append(header).append("XDATRelation\n");
		if (getForeignKeyName() != "")
			sb.append(header).append("foreignKeyName:").append(this.getForeignKeyName()).append("\n");
		if (getForeignKeyTable() != "")
			sb.append(header).append("ForeignKeyTable").append(this.getForeignKeyTable()).append("\n");
		if (getForeignCol() != "")
			sb.append(header).append("ForeignCol:").append(this.getForeignCol()).append("\n");
		if (getOnDelete() != "")
			sb.append(header).append("OnDelete:").append(this.getOnDelete()).append("\n");
		if (getOnUpdate() != "")
			sb.append(header).append("OnUpdate:").append(this.getOnUpdate()).append("\n");
		if (getRelationType() != "")
			sb.append(header).append("RelationType:").append(this.getRelationType()).append("\n");
		return sb.toString();
	}

	
	public Node toXML(Document doc)
	{
		Node main = doc.createElement("relation");
		
		if (getForeignKeyName() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"foreignKeyName",this.getForeignKeyName()));
		if (getForeignKeyTable() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"ForeignKeyTable",this.getForeignKeyTable()));
		if (getForeignCol() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"ForeignCol",this.getForeignCol()));
		if (getOnDelete() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"OnDelete",this.getOnDelete()));
		if (getOnUpdate() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"OnUpdate",this.getOnUpdate()));
		if (getRelationType() != "")
			main.getAttributes().setNamedItem(NodeUtils.CreateAttributeNode(doc,"RelationType",this.getRelationType()));
		if (getDuplicateRelationships().size()==0)
		{
		    Iterator iter = getDuplicateRelationships().iterator();
		    while (iter.hasNext())
		    {
		        XFTDuplicateRelationship dup = (XFTDuplicateRelationship)iter.next();
				main.appendChild(dup.toXML(doc));
		    }
		}
		return main;
	}
    /**
     * @return Returns the duplicateRelationships.
     */
    public ArrayList getDuplicateRelationships() {
        return duplicateRelationships;
    }
    /**
     * @return Returns the relationName.
     */
    public String getRelationName() {
        return relationName;
    }
    /**
     * @param relationName The relationName to set.
     */
    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }
}

