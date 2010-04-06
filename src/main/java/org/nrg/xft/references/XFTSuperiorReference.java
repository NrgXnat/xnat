//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Nov 12, 2004
 */
package org.nrg.xft.references;
import java.util.ArrayList;
import java.util.Iterator;

import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.InvalidReference;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.XMLType;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.utils.StringUtils;
/**
 * Details a one-to-many reference 
 * 
 * @author Tim
 */
public class XFTSuperiorReference implements org.nrg.xft.references.XFTReferenceI {
	/**
	 * value used for specifier field to say the relation was defined in the superior element (as a max occurs > 1).
	 */
	public static final int SUPERIOR = 1;// value used for specifier field to say the relation was defined in the superior element (as a max occurs > 1).
	
	/**
	 * value used for specifier field to say the relation was defined in the subordinate element.
	 */
	public static final int SUBORDINATE = 2;// value used for specifier field to say the relation was defined in the subordinate element.
	 
	GenericWrapperElement superiorElement = null;
	GenericWrapperField superiorField = null;
	GenericWrapperElement subordinateElement = null;
	GenericWrapperField subordinateField = null;
	private int specifier= 0;
	ArrayList<XFTRelationSpecification> keyRelations = null;
		
	/**
	 * If the field isMultiple then this is initialized as a superior field, otherwise
	 * it is a subordinate field.
	 * @param field
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public XFTSuperiorReference(GenericWrapperField field) throws ElementNotFoundException,XFTInitException
	{
		if (! field.isReference())
		{
			throw new InvalidReference("Field needs to be a reference: " +field.getName());
		}
		if (field.isMultiple())
		{
			specifier = SUPERIOR;
			initializeWithSuperiorField(field);
		}else
		{
			specifier = SUBORDINATE;
			initializeWithSubOrdinateField(field);
		}
	}
	
	/**
	 * @return
	 */
	public String getSuperiorElementName()
	{
		return superiorElement.getFullXMLName();
	}
	
	/**
	 * @return
	 */
	public String getSuperiorElementLocalName()
	{
		return superiorElement.getLocalXMLName();
	}
	
	/**
	 * @return
	 */
	public String getSubordinateElementName()
	{
		return subordinateElement.getFullXMLName();
	}
	
	/**
	 * @param field
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	private void initializeWithSubOrdinateField(GenericWrapperField field) throws ElementNotFoundException,XFTInitException
	{
		subordinateField = field;		
		subordinateElement = subordinateField.getParentElement().getGenericXFTElement();
		XMLType superiorName = subordinateField.getReferenceElementName();
		try {
			superiorElement = GenericWrapperElement.GetElement(superiorName);
		} catch (ElementNotFoundException e) {
			throw new ElementNotFoundException(subordinateElement.getFullXMLName() + " -> " + superiorName);
		}
		
		//CHECK REF FIELDS
		boolean foundMatch = false;
		Iterator superiorRefs = superiorElement.getReferenceFields(true).iterator();
		while (superiorRefs.hasNext())
		{
			GenericWrapperField ref = (GenericWrapperField)superiorRefs.next();
			if (ref.getReferenceElementName().getFullForeignType().equalsIgnoreCase(subordinateElement.getFullXMLName()))
			{
				superiorField = ref;
				foundMatch = true;
				break;
			}
		}
		
		//ADD RELATIONS
		ArrayList superiorFields = superiorElement.getAllPrimaryKeys();
		keyRelations = new ArrayList<XFTRelationSpecification>();

		if (superiorFields.size() > 1)
		{
			Iterator foreignKeys = superiorFields.iterator();
			while (foreignKeys.hasNext())
			{
				GenericWrapperField foreignKey = (GenericWrapperField)foreignKeys.next();
				if (! subordinateField.getXMLSqlNameValue().equalsIgnoreCase(""))
				{
					XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),subordinateField.getXMLSqlNameValue()+ "_" + foreignKey.getSQLName(),superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
					keyRelations.add(spec);
				}else
				{
					XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),subordinateField.getSQLName() + "_" + foreignKey.getSQLName(),superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
					keyRelations.add(spec);
				}
			}
		}else{
			GenericWrapperField foreignKey = (GenericWrapperField)superiorFields.get(0);
			if (! subordinateField.getXMLSqlNameValue().equalsIgnoreCase(""))
			{
				XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),subordinateField.getXMLSqlNameValue(),superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
				keyRelations.add(spec);
			}else
			{
				
				if (subordinateField.getPrimaryKey().equalsIgnoreCase("true"))
				{
					XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),foreignKey.getSQLName(),superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
					keyRelations.add(spec);
				}else{
					XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),subordinateField.getSQLName() + "_" + foreignKey.getSQLName(),superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
					keyRelations.add(spec);
				}
			}
		}
	}
	
	/**
	 * @param f
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	private void initializeWithSuperiorField(GenericWrapperField f) throws ElementNotFoundException,XFTInitException
	{
		superiorField = f;
		superiorElement = superiorField.getParentElement().getGenericXFTElement();
		XMLType subordinateName = superiorField.getReferenceElementName();
		
		subordinateElement = GenericWrapperElement.GetElement(subordinateName);
		Iterator subordinateFields = subordinateElement.getAllFields(false,true).iterator();
		while (subordinateFields.hasNext())
		{
			GenericWrapperField field = (GenericWrapperField)subordinateFields.next();
			if (field.getXMLType().getLocalType().equalsIgnoreCase(superiorElement.getLocalXMLName()) || field.getBaseElement().equalsIgnoreCase(superiorElement.getXSIType()))
			{
				this.subordinateField = field;
				if (field.getBaseElement().equalsIgnoreCase(superiorElement.getXSIType())){
					if (field.getXMLSqlNameValue().equalsIgnoreCase("") && (! superiorField.getXMLSqlNameValue().equalsIgnoreCase(""))){
						field.getWrapped().getSqlField().setSqlName(superiorField.getXMLSqlNameValue());
					}
				}
				break;
			}
		}
		
		keyRelations = new ArrayList<XFTRelationSpecification>();

		ArrayList superiorKeysAL = superiorElement.getAllPrimaryKeys();
		
		if (subordinateField != null)
		{
			if (superiorKeysAL.size() > 1)
			{
				//multiple primary keys
				Iterator foreignKeys = superiorKeysAL.iterator();
				while (foreignKeys.hasNext())
				{
					GenericWrapperField foreignKey = (GenericWrapperField)foreignKeys.next();
					if (! subordinateField.getXMLSqlNameValue().equalsIgnoreCase(""))
					{
						XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),StringUtils.SQLMaxCharsAbbr(subordinateField.getXMLSqlNameValue(),foreignKey.getSQLName()),superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
						keyRelations.add(spec);
					}else
					{
						XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),StringUtils.SQLMaxCharsAbbr(foreignKey.getSQLName()),superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
						keyRelations.add(spec);
					}
				}
			}else
			{
				GenericWrapperField foreignKey = (GenericWrapperField)superiorKeysAL.get(0);
				if (! subordinateField.getXMLSqlNameValue().equalsIgnoreCase(""))
				{
					XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),subordinateField.getXMLSqlNameValue(),superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
					keyRelations.add(spec);
				}else
				{
					if (subordinateField.getName().equalsIgnoreCase(superiorElement.getFullXMLName()) || subordinateField.getPrimaryKey().equalsIgnoreCase("true"))
					{
						XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),StringUtils.SQLMaxCharsAbbr(foreignKey.getSQLName()),superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
						keyRelations.add(spec);
					}else{
						XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),StringUtils.SQLMaxCharsAbbr(subordinateField.getSQLName(),foreignKey.getSQLName()),superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
						keyRelations.add(spec);
					}
				}
			}
		}else
		{
			if (superiorKeysAL.size() > 1)
			{
				//multiple primary keys
				Iterator foreignKeys = superiorKeysAL.iterator();
				while (foreignKeys.hasNext())
				{
					GenericWrapperField foreignKey = (GenericWrapperField)foreignKeys.next();
					if (! superiorField.getXMLSqlNameValue().equalsIgnoreCase(""))
					{
						XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),StringUtils.SQLMaxCharsAbbr(superiorField.getXMLSqlNameValue(),foreignKey.getSQLName()),superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
						keyRelations.add(spec);
					}else
					{
						String fkName = null;
					    if (superiorField.getName().equalsIgnoreCase(superiorField.getWrapped().getFullName()))
					    { 
					        fkName = StringUtils.SQLMaxCharsAbbr(superiorElement.getSQLName(),foreignKey.getSQLName());
							XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),fkName,superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
							keyRelations.add(spec);
					    }else
					    {
					        fkName = StringUtils.SQLMaxCharsAbbr(superiorField.getSQLName() + "_" +superiorElement.getSQLName(),foreignKey.getSQLName());
							XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),fkName,superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
							keyRelations.add(spec);
					    }
					}
				}
			}else
			{
				GenericWrapperField foreignKey = (GenericWrapperField)superiorKeysAL.get(0);
				if (! superiorField.getXMLSqlNameValue().equalsIgnoreCase("") && (! superiorField.isCreatedChild()))
				{
					XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),superiorField.getXMLSqlNameValue(),superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
					keyRelations.add(spec);
				}else
				{
				    String fkName = null;
				    if (superiorField.getName().equalsIgnoreCase(superiorField.getWrapped().getFullName()))
				    { 
				        fkName = StringUtils.SQLMaxCharsAbbr(superiorElement.getSQLName(),foreignKey.getSQLName());
						XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),fkName,superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
						keyRelations.add(spec);
				    }else
				    {
				        fkName = StringUtils.SQLMaxCharsAbbr(superiorField.getSQLName() + "_" +superiorElement.getSQLName(),foreignKey.getSQLName());
						XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),fkName,superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
						keyRelations.add(spec);
				    }
				}
			}
		}
	}
	/**
	 * @return
	 */
	public GenericWrapperElement getSubordinateElement() {
		return subordinateElement;
	}

	/**
	 * @return
	 */
	public GenericWrapperField getSubordinateField() {
		return subordinateField;
	}

	/**
	 * @return
	 */
	public GenericWrapperElement getSuperiorElement() {
		return superiorElement;
	}

	/**
	 * @return
	 */
	public GenericWrapperField getSuperiorField() {
		return superiorField;
	}

	/**
	 * @param element
	 */
	public void setSubordinateElement(GenericWrapperElement element) {
		subordinateElement = element;
	}

	/**
	 * @param field
	 */
	public void setSubordinateField(GenericWrapperField field) {
		subordinateField = field;
	}

	/**
	 * @param element
	 */
	public void setSuperiorElement(GenericWrapperElement element) {
		superiorElement = element;
	}

	/**
	 * @param field
	 */
	public void setSuperiorField(GenericWrapperField field) {
		superiorField = field;
	}

	/**
	 * @return
	 */
	public GenericWrapperElement getElement1() {
		return superiorElement;
	}

	/**
	 * @return
	 */
	public GenericWrapperElement getElement2() {
		return subordinateElement;
	}

	/**
	 * @return
	 */
	public GenericWrapperField getField1() {
		return superiorField;
	}

	/**
	 * @return
	 */
	public GenericWrapperField getField2() {
		return subordinateField;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.references.XFTReferenceI#isManyToMany()
	 */
	public boolean isManyToMany()
	{
		return false;
	}
	
	/**
	 * ArrayList of XFTRelationSpecification
	 * @return
	 */
	public ArrayList<XFTRelationSpecification> getKeyRelations() {
		return keyRelations;
	}
	
	/**
	 * @return
	 */
	public String getSubordinateFieldSQLName()
	{
		if (this.getSubordinateField() == null)
		{
			return null;
		}else
		{
			return this.getSubordinateField().getSQLName();
		}
	}
	
	/**
	 * @return
	 */
	public String getSuperiorFieldSQLName()
	{
		if (this.getSuperiorField() == null)
		{
			return null;
		}else
		{
			return this.getSuperiorField().getSQLName();
		}
	}

	public String toString()
	{
	    StringBuffer sb = new StringBuffer();
	    Iterator iter = this.getKeyRelations().iterator();
	    while (iter.hasNext())
	    {
	        XFTRelationSpecification spec = (XFTRelationSpecification)iter.next();
	        sb.append(spec.toString()).append("\n");
	    }
	    return sb.toString();
	}
}

