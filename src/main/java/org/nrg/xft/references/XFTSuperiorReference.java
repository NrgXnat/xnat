/*
 * core: org.nrg.xft.references.XFTSuperiorReference
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.references;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.InvalidReference;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.XMLType;
import org.nrg.xft.utils.XftStringUtils;

import java.util.ArrayList;
import java.util.Iterator;

public class XFTSuperiorReference implements org.nrg.xft.references.XFTReferenceI {
	/**
	 * value used for specifier field to say the relation was defined in the superior element (as a max occurs &gt; 1).
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
	 * @return Returns the superior element name
	 */
	public String getSuperiorElementName()
	{
		return superiorElement.getFullXMLName();
	}
	
	/**
	 * @return Returns the superior element's local name
	 */
	public String getSuperiorElementLocalName()
	{
		return superiorElement.getLocalXMLName();
	}
	
	/**
	 * @return Returns the subordinate element name
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
						XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(), XftStringUtils.SQLMaxCharsAbbr(subordinateField.getXMLSqlNameValue(), foreignKey.getSQLName()), superiorElement.getSQLName(), foreignKey.getSQLName(), foreignKey.getXMLType(), foreignKey, subordinateField, this);
						keyRelations.add(spec);
					}else
					{
						XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(), XftStringUtils.SQLMaxCharsAbbr(foreignKey.getSQLName()), superiorElement.getSQLName(), foreignKey.getSQLName(), foreignKey.getXMLType(), foreignKey, subordinateField, this);
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
						XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(), XftStringUtils.SQLMaxCharsAbbr(foreignKey.getSQLName()), superiorElement.getSQLName(), foreignKey.getSQLName(), foreignKey.getXMLType(), foreignKey, subordinateField, this);
						keyRelations.add(spec);
					}else{
						XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(), XftStringUtils.SQLMaxCharsAbbr(subordinateField.getSQLName(), foreignKey.getSQLName()), superiorElement.getSQLName(), foreignKey.getSQLName(), foreignKey.getXMLType(), foreignKey, subordinateField, this);
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
						XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(), XftStringUtils.SQLMaxCharsAbbr(superiorField.getXMLSqlNameValue(), foreignKey.getSQLName()), superiorElement.getSQLName(), foreignKey.getSQLName(), foreignKey.getXMLType(), foreignKey, subordinateField, this);
						keyRelations.add(spec);
					}else
					{
						String fkName = null;
					    if (superiorField.getName().equalsIgnoreCase(superiorField.getWrapped().getFullName()))
					    { 
					        fkName = XftStringUtils.SQLMaxCharsAbbr(superiorElement.getSQLName(), foreignKey.getSQLName());
							XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),fkName,superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
							keyRelations.add(spec);
					    }else
					    {
					        fkName = XftStringUtils.SQLMaxCharsAbbr(superiorField.getSQLName() + "_" + superiorElement.getSQLName(), foreignKey.getSQLName());
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
				        fkName = XftStringUtils.SQLMaxCharsAbbr(superiorElement.getSQLName(), foreignKey.getSQLName());
						XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),fkName,superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
						keyRelations.add(spec);
				    }else
				    {
				        fkName = XftStringUtils.SQLMaxCharsAbbr(superiorField.getSQLName() + "_" + superiorElement.getSQLName(), foreignKey.getSQLName());
						XFTRelationSpecification spec = new XFTRelationSpecification(subordinateElement.getSQLName(),fkName,superiorElement.getSQLName(),foreignKey.getSQLName(),foreignKey.getXMLType(),foreignKey,subordinateField,this);
						keyRelations.add(spec);
				    }
				}
			}
		}
	}
	/**
	 * @return Returns the subordinate element as a GenericWrapperElement object
	 */
	public GenericWrapperElement getSubordinateElement() {
		return subordinateElement;
	}

	/**
	 * @return Returns the subordinate field as a GenericWrapperField object
	 */
	public GenericWrapperField getSubordinateField() {
		return subordinateField;
	}

	/**
	 * @return Returns the superior element as a GenericWrapperElement object
	 */
	public GenericWrapperElement getSuperiorElement() {
		return superiorElement;
	}

	/**
	 * @return Returns the superior field as a GenericWrapperField object
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
	 * @return Returns element1 (the superior element) as a GenericWrapperElement
	 */
	public GenericWrapperElement getElement1() {
		return superiorElement;
	}

	/**
	 * @return Returns element2 (the subordinate element) as a GenericWrapperElement
	 */
	public GenericWrapperElement getElement2() {
		return subordinateElement;
	}

	/**
	 * @return Returns field1 (the superior field) as a GenericWrapperField
	 */
	public GenericWrapperField getField1() {
		return superiorField;
	}

	/**
	 * @return Returns field2 (the subordinate field) as a GenericWrapperField
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
	 * @return Returns a list of the XFTRelationSpecifications
	 */
	public ArrayList<XFTRelationSpecification> getKeyRelations() {
		return keyRelations;
	}
	
	/**
	 * @return Returns the SQL name String of the subordinate field
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
	 * @return Returns the SQL name String of the superior field
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
	    StringBuilder sb = new StringBuilder();
	    Iterator iter = this.getKeyRelations().iterator();
	    while (iter.hasNext())
	    {
	        XFTRelationSpecification spec = (XFTRelationSpecification)iter.next();
	        sb.append(spec.toString()).append("\n");
	    }
	    return sb.toString();
	}

	public int compareTo(XFTReferenceI arg0) {
		if(!(arg0 instanceof XFTSuperiorReference)){
			return 1;
		}
		
		XFTSuperiorReference ref=(XFTSuperiorReference)arg0;
		
		if(this.isManyToMany()==ref.isManyToMany()){
			return (isManyToMany())?-1:1;
		}
		
		int c=ref.getElement1().getXSIType().compareTo(this.getElement1().getXSIType());
		if(c!=0){
			return c;
		}
		
		c=ref.getElement2().getXSIType().compareTo(this.getElement2().getXSIType());
		if(c!=0){
			return c;
		}
		
		c=ref.getField1().getXMLPathString().compareTo(this.getField1().getXMLPathString());
		if(c!=0){
			return c;
		}
		
		return ref.getField2().getXMLPathString().compareTo(this.getField2().getXMLPathString());
	}
}

