//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Nov 9, 2004
 */
package org.nrg.xft.references;

import org.nrg.xft.schema.XFTReferenceField;
import org.nrg.xft.schema.Wrappers.GenericWrapper.*;
import org.nrg.xft.utils.StringUtils;

import java.util.*;
/**
 * Class used to indentify a many-to-many reference within the schema.
 * 
 * @author Tim
 */
public class XFTManyToManyReference implements org.nrg.xft.references.XFTReferenceI{
	private GenericWrapperElement element1 = null;
	private GenericWrapperField field1 = null;
	private GenericWrapperElement element2 = null;
	private GenericWrapperField field2 = null;
	
	private String mapping_name = null;
	private boolean unique = false;
	
	private ArrayList mappingColumns = new ArrayList();//ArrayList of XFTMappingColumn
	
	/**
	 * Constructs a Reference object with the two related fields and generates a list 
	 * of the mapping columns between them.
	 * @param f1
	 * @param f2
	 * @throws org.nrg.xft.exception.XFTInitException
	 */
	public XFTManyToManyReference(GenericWrapperField f1,GenericWrapperField f2) throws org.nrg.xft.exception.XFTInitException
	{
		field1 = f1;
		field2 = f2;
		element1 = field1.getParentElement().getGenericXFTElement();
		element2 = field2.getParentElement().getGenericXFTElement();
		
		ArrayList element1KeysAL = element1.getAllPrimaryKeys();
		ArrayList element2KeysAL = element2.getAllPrimaryKeys();
		Iterator element1Keys = element1KeysAL.iterator();
		Iterator element2Keys = element2KeysAL.iterator();
		
		if (field1.getXMLSqlNameValue().equalsIgnoreCase(""))
		{
			while (element1Keys.hasNext())
			{
				GenericWrapperField key1 = (GenericWrapperField) element1Keys.next();
				XFTMappingColumn mappingColumn = new XFTMappingColumn();
				mappingColumn.setLocalSqlName(element1.getSQLName() + "_" + key1.getSQLName());
				mappingColumn.setXmlType(key1.getXMLType());
				mappingColumn.setForeignElement(element1);
				mappingColumn.setForeignKey(key1);
				mappingColumns.add(mappingColumn);
			}
		}else
		{
			while (element1Keys.hasNext())
			{
				GenericWrapperField key1 = (GenericWrapperField) element1Keys.next();
				XFTMappingColumn mappingColumn = new XFTMappingColumn();
				mappingColumn.setLocalSqlName(field1.getXMLSqlNameValue() + "_" + key1.getSQLName());
				mappingColumn.setXmlType(key1.getXMLType());
				mappingColumn.setForeignElement(element1);
				mappingColumn.setForeignKey(key1);
				mappingColumns.add(mappingColumn);
			}
		}
		
		if (field2.getXMLSqlNameValue().equalsIgnoreCase(""))
		{
			while (element2Keys.hasNext())
			{
				GenericWrapperField key2 = (GenericWrapperField) element2Keys.next();
				XFTMappingColumn mappingColumn = new XFTMappingColumn();
				mappingColumn.setLocalSqlName(element2.getSQLName() + "_" + key2.getSQLName());
				mappingColumn.setXmlType(key2.getXMLType());
				mappingColumn.setForeignElement(element2);
				mappingColumn.setForeignKey(key2);
				mappingColumns.add(mappingColumn);
			}
		}else
		{
			while (element2Keys.hasNext())
			{
				GenericWrapperField key2 = (GenericWrapperField) element2Keys.next();
				XFTMappingColumn mappingColumn = new XFTMappingColumn();
				mappingColumn.setLocalSqlName(field2.getXMLSqlNameValue() + "_" + key2.getSQLName());
				mappingColumn.setXmlType(key2.getXMLType());
				mappingColumn.setForeignElement(element2);
				mappingColumn.setForeignKey(key2);
				mappingColumns.add(mappingColumn);
			}
		}
	}
	
	/**
	 * Constructs a Reference object with field and its referenced element and generates a list 
	 * of the mapping columns between them.
	 * @param f1
	 * @param foreignElement
	 * @throws org.nrg.xft.exception.XFTInitException
	 */
	public XFTManyToManyReference(GenericWrapperField f1,GenericWrapperElement foreignElement) throws org.nrg.xft.exception.XFTInitException
	{
		field1 = f1;
		element1 = field1.getParentElement().getGenericXFTElement();
		element2 = foreignElement;
		
		XFTReferenceField ref = XFTReferenceField.GetEmptyRef();
		ref.setName(element2.getFullXMLName());
		ref.setFullName(element2.getFullXMLName());
		field2 = (GenericWrapperField)GenericWrapperFactory.GetInstance().wrapField(ref);
	
		ArrayList element1KeysAL = element1.getAllPrimaryKeys();
		ArrayList element2KeysAL = element2.getAllPrimaryKeys();
		Iterator element1Keys = element1KeysAL.iterator();
		Iterator element2Keys = element2KeysAL.iterator();
	
		if (field1.getXMLSqlNameValue().equalsIgnoreCase(""))
		{
			while (element1Keys.hasNext())
			{
				GenericWrapperField key1 = (GenericWrapperField) element1Keys.next();
				XFTMappingColumn mappingColumn = new XFTMappingColumn();
				mappingColumn.setLocalSqlName(element1.getSQLName() + "_" + key1.getSQLName());
				mappingColumn.setXmlType(key1.getXMLType());
				mappingColumn.setForeignElement(element1);
				mappingColumn.setForeignKey(key1);
				mappingColumns.add(mappingColumn);
			}
		}else
		{
			while (element1Keys.hasNext())
			{
				GenericWrapperField key1 = (GenericWrapperField) element1Keys.next();
				XFTMappingColumn mappingColumn = new XFTMappingColumn();
				mappingColumn.setLocalSqlName(field1.getXMLSqlNameValue() + "_" + key1.getSQLName());
				mappingColumn.setXmlType(key1.getXMLType());
				mappingColumn.setForeignElement(element1);
				mappingColumn.setForeignKey(key1);
				mappingColumns.add(mappingColumn);
			}
		}
	
		if (field2 == null || field2.getXMLSqlNameValue().equalsIgnoreCase(""))
		{
			while (element2Keys.hasNext())
			{
				GenericWrapperField key2 = (GenericWrapperField) element2Keys.next();
				XFTMappingColumn mappingColumn = new XFTMappingColumn();
				mappingColumn.setLocalSqlName(element2.getSQLName() + "_" + key2.getSQLName());
				mappingColumn.setXmlType(key2.getXMLType());
				mappingColumn.setForeignElement(element2);
				mappingColumn.setForeignKey(key2);
				mappingColumns.add(mappingColumn);
			}
		}else
		{
			while (element2Keys.hasNext())
			{
				GenericWrapperField key2 = (GenericWrapperField) element2Keys.next();
				XFTMappingColumn mappingColumn = new XFTMappingColumn();
				mappingColumn.setLocalSqlName(field2.getXMLSqlNameValue() + "_" + key2.getSQLName());
				mappingColumn.setXmlType(key2.getXMLType());
				mappingColumn.setForeignElement(element2);
				mappingColumn.setForeignKey(key2);
				mappingColumns.add(mappingColumn);
			}
		}
	}
	
	/**
	 * Returns the name of the mapping table (the two elements' sql names in alphabetical order).
	 * @return
	 */
	public String getMappingTable()
	{
	    if (this.getMapping_name() == null)
	    {
			String f1 = StringUtils.RegCharsAbbr(field1.getSQLName());
			String e1 = element1.getSQLName();
			String f2 = "";
			String e2 = element2.getSQLName();
			if (field2 != null)
			{
				f2 = StringUtils.RegCharsAbbr(field2.getSQLName());
			}
			if (f1.compareTo(e2) >= 0)
			{
				return f1 + "_" + e1 + "_"+ f2 + "_" + e2;
			}else
			{
				return f2 + "_" + e2 + "_"+ f1 + "_" + e1;
			}
	    }else{
	        return getMapping_name();
	    }
	}

	/**
	 * @return
	 */
	public GenericWrapperElement getElement1() {
		return element1;
	}

	/**
	 * @return
	 */
	public GenericWrapperElement getElement2() {
		return element2;
	}

	/**
	 * @return
	 */
	public GenericWrapperField getField1() {
		return field1;
	}

	/**
	 * @return
	 */
	public GenericWrapperField getField2() {
		return field2;
	}

	/**
	 * @param element
	 */
	public void setElement1(GenericWrapperElement element) {
		element1 = element;
	}

	/**
	 * @param element
	 */
	public void setElement2(GenericWrapperElement element) {
		element2 = element;
	}

	/**
	 * @param field
	 */
	public void setField1(GenericWrapperField field) {
		field1 = field;
	}
	
	/**
	 * @param field
	 */
	public void setField2(GenericWrapperField field) {
		field2 = field;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.references.XFTReferenceI#isManyToMany()
	 */
	public boolean isManyToMany()
	{
		return true;
	}

	/**
	 * ArrayList of XFTMappingColumn
	 * @return
	 */
	public ArrayList getMappingColumns() {
		return mappingColumns;
	}
	
	/**
	 * ArrayList of XFTMappingColumns for this element.
	 * @param element
	 * @return
	 */
	public ArrayList<XFTMappingColumn> getMappingColumnsForElement(GenericWrapperElement element)
	{
		ArrayList<XFTMappingColumn> al = new ArrayList<XFTMappingColumn>();
		Iterator iter = getMappingColumns().iterator();
		while (iter.hasNext())
		{
			XFTMappingColumn c = (XFTMappingColumn)iter.next();
			if (element.instanceOf(c.getForeignElement().getFullXMLName()))
			{
				al.add(c);
			}
		}
		al.trimToSize();
		return al;
	}

	public GenericWrapperElement getOppositeElement(GenericWrapperElement gwe)
	{
		if (gwe.instanceOf(getElement1().getFullXMLName()))
		{
			return getElement2();
		}else{
			return getElement1();
		}
	}
    /**
     * @return Returns the mapping_name.
     */
    private String getMapping_name() {
        return mapping_name;
    }
    /**
     * @param mapping_name The mapping_name to set.
     */
    public void setMapping_name(String mapping_name) {
        this.mapping_name = mapping_name;
    }
    
    public String getHistoryTableName()
    {
    	String mappingName=getMappingTable();
    	if (mappingName.length()<55)
    	{
    	    mappingName +="_history";
    	}else{
    	    if (mappingName.length()>60)
    		{
    		   mappingName = StringUtils.FirstNChars(mappingName,60) + "_h";
    		}else{
     		   mappingName += "_h";
    		}
    	}
    	return mappingName;
    }

    /**
     * @return Returns the uniqueComposite.
     */
    public boolean isUnique() {
        return unique;
    }
    /**
     * @param uniqueComposite The uniqueComposite to set.
     */
    public void setUnique(boolean unique) {
        this.unique = unique;
    }
}

