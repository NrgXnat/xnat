/*
 * org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/30/13 5:36 PM
 */


package org.nrg.xft.schema.Wrappers.GenericWrapper;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xft.XFT;
import org.nrg.xft.TypeConverter.TypeConverter;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidReference;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.references.XFTReferenceI;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.references.XFTRelationSpecification;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.TorqueField;
import org.nrg.xft.schema.XFTDataField;
import org.nrg.xft.schema.XFTElement;
import org.nrg.xft.schema.XFTField;
import org.nrg.xft.schema.XFTReferenceField;
import org.nrg.xft.schema.XFTRelation;
import org.nrg.xft.schema.XFTRule;
import org.nrg.xft.schema.XFTSqlField;
import org.nrg.xft.schema.XMLType;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLFieldData;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.SchemaFieldI;
import org.nrg.xft.schema.design.XFTElementWrapper;
import org.nrg.xft.schema.design.XFTFactoryI;
import org.nrg.xft.schema.design.XFTFieldWrapper;
import org.nrg.xft.schema.design.XFTNode;
import org.nrg.xft.utils.StringUtils;
@SuppressWarnings({"unchecked","rawtypes"})
public class GenericWrapperField extends XFTFieldWrapper implements SchemaFieldI {
	static org.apache.log4j.Logger logger = Logger.getLogger(GenericWrapperField.class);
	private TorqueField tf = null;
	private XFTSqlField sql = null;
	
	private ArrayList directFields = null;
	private ArrayList allFields = null;
	
	private XFTReferenceI xftRef = null;
	
	private Hashtable allPossibleXMLFieldNames = null;
	
	private static String DEFAULT_STRING_SIZE = "255";
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.schema.design.XFTFieldWrapper#getFactory()
	 */
	public XFTFactoryI getFactory() {
		return GenericWrapperFactory.GetInstance();
	}
	
	/**
	 * If the wrapped field has a TorqueField, it is returned.  Else, a new one is created and returned.
	 * @return
	 */
	public TorqueField getTorqueField()
	{
		if (tf == null)
		{
			if (wrapped.getWebAppField() != null)
			{
				tf = (TorqueField)wrapped.getWebAppField();
			}else
			{
				tf = new TorqueField();
			}
		}
		return tf;
	}

	/**
	 * Returns Hashtable of all possible root xml-field names.
	 * <BR>Value is the GenericWrapperField;
	 * @return
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public Hashtable getAllPossibleXMLFieldNames()throws ElementNotFoundException, XFTInitException 
	{
		if (this.allPossibleXMLFieldNames == null)
		{
			allPossibleXMLFieldNames = new Hashtable();
			Iterator dF = this.getDirectFieldsNoFilter().iterator();
			while (dF.hasNext())
			{
				GenericWrapperField f = (GenericWrapperField)dF.next();
				allPossibleXMLFieldNames.put(f.getName().toLowerCase(),f);
			}
		}
		return allPossibleXMLFieldNames;
	}
	
	/**
	 * @return
	 */
	public SchemaElementI getParentElement() throws ClassCastException
	{
		return (SchemaElementI)this.getParentE();
	}
	
	/**
	 * If the wrapped field has a Sql Field, it is returned.  Else, a new one is created and returned.
	 * @return
	 */
	public XFTSqlField getSqlField()
	{
		if (sql == null)
		{
			if (wrapped.getSqlField() != null)
			{
				sql = wrapped.getSqlField();
			}else
			{
				sql = new XFTSqlField();
			}
		}
		return sql;
	}
	
	/**
	 * If this field has a specified java name it is returned, Else an empty string is returned.
	 * @return
	 */
	public String getXMLJavaNameValue()
	{
		String _return = "";
		if (wrapped.getWebAppField() != null)
		{
			_return = wrapped.getWebAppField().getJavaName();
		}
		return _return;
	}
	
	/**
	 * If this field has a specified sql name it is returned, Else an empty string is returned.
	 * @return
	 */	
	public String getXMLSqlNameValue()
	{
		String _return = "";
		if (wrapped.getSqlField() != null)
		{
			_return = wrapped.getSqlField().getSqlName();
		}
		
		return _return;
	}
	
	/**
	 * Returns the xml name of the given field.
	 * @return
	 */
	public String getXMLName()
	{
		return wrapped.getName();
	}
	
	/**
	 * If this field has a specified sql name, it is returned (lower case).  Else, the full name of the field is returned.
	 * @return
	 */
	public String getSQLName()
	{
	    if (getWrapped().getFinalSqlName()==null)
	    {
			String temp = "";
			if (this.getXMLSqlNameValue() != "")
			{
				temp =  this.getXMLSqlNameValue().toLowerCase();
			}
			
			if (temp.equalsIgnoreCase(""))
				temp =  wrapped.getFullName().toLowerCase();
			
			if (temp != null)
		    {
		        if (temp.length()>63)
		        {
		            temp = temp.substring(0,63);
		        }
		    }
			this.getWrapped().setFinalSqlName(StringUtils.CleanForSQL(temp));
	    }
	    
	    return getWrapped().getFinalSqlName();
	}
	
	/**
	 * If this field has a specified java name, it is returned.  Else, the xml name is returned.
	 * @return
	 */
	public String getJAVAName()
	{
		if (this.getXMLJavaNameValue() != "")
		{
			return getXMLJavaNameValue();
		}

		return getName();
	}
	
	/**
	 * Returns the fields which are defined directly below this field.
	 * @return ArrayList of GenericWrapperFields
	 */
	public ArrayList getDirectFields()
	{
		if (this.directFields == null)
		{
			directFields = new ArrayList();
			Iterator iter = wrapped.getSortedFields().iterator();
			while (iter.hasNext())
			{
				XFTField xf = (XFTField)iter.next();
				directFields.add(getFactory().wrapField(xf));
				
			}
		}
		return directFields;
	}

	public ArrayList getDirectFieldsNoFilter() {
		ArrayList temp = new ArrayList();
		Iterator iter = wrapped.getSortedFields().iterator();
		while (iter.hasNext()) {
			XFTField xf = (XFTField) iter.next();
			temp.add(getFactory().wrapField(xf));
		}
		temp.trimToSize();
		return temp;
	}
	
	/**
	 * returns all fields from all levels.
	 * @return ArrayList of GenericWrapperFields
	 */
	public ArrayList getAllFields()
	{
		if (this.allFields == null)
		{
			allFields = new ArrayList();
			Iterator iter = wrapped.getSortedFields().iterator();
			while (iter.hasNext())
			{
				XFTField xf = (XFTField)iter.next();
				GenericWrapperField gwf = (GenericWrapperField)getFactory().wrapField(xf);
				
				allFields.addAll(gwf.getAllFields());
				if (IsLeafNode(xf))
					allFields.add(gwf);
			}
		}
		return allFields;
	}
	
	
	/**
	 * if the primary key for this field's sql field is set to 'true'
	 * @return
	 */
	public boolean isPrimaryKey()
	{
		if (wrapped.getSqlField() != null)
		{
			if (wrapped.getSqlField().getPrimaryKey().equalsIgnoreCase("true"))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * if the xml only property for this field is set to 'true'
	 * @return
	 */
	public boolean isXMLOnly()
	{
		if (wrapped.getXmlOnly().equalsIgnoreCase("true"))
		{
			return true;
		}
		
		return false;
	}

	/**
	 * If the size is not defined and the type is a string then '255' is returned.  If the size is greater than 256, then an empty string is returned.  Else the defined size is returned.
	 * @return
	 */
	public String getAdjustedSize()
	{
		if ((((XFTDataField)wrapped).getXMLType().getLocalType().equalsIgnoreCase("string")))
		{
			if (wrapped.getSize().equalsIgnoreCase(""))
			{
				return DEFAULT_STRING_SIZE;
			}
		}
//	TAKEN OUT TIM 11/17
//		if (wrapped.getRule() != null)
//		{
//			if (wrapped.getRule().getBaseType().equalsIgnoreCase("string"))
//			{
//				if (wrapped.getSize().equalsIgnoreCase(""))
//				{
//					return "50";
//				}
//			}
//		}
	
		if (wrapped.getSize() == "")
		{
			return "";
		}else
		{
			try{
				int size = Integer.valueOf(wrapped.getSize()).intValue();
				if (size > 256)
				{
					return "";
				}else
				{
					return wrapped.getSize();
				}
			}catch(Exception ex)
			{
				logger.error("'" + this.getParentElement().getFullXMLName() + "' -> '" + this.getName() + "'",ex);
				return wrapped.getSize();
			}
		}
	}
	
	/**
	 * Defined size
	 * @return
	 */
	public String getSize()
	{
		XFTRule xr = wrapped.getRule();
		String size = "";
		if (xr != null)
		{
			size = xr.getMaxLength();
			if (size.equalsIgnoreCase(""))
			{
				size = xr.getMaxInclusive();
			}
			if (size.equalsIgnoreCase(""))
			{
				size = xr.getLength();
			}
		}
		if (size.equalsIgnoreCase(""))
		{
			size = wrapped.getSize();
		}
		return size;
	}

	/**
	 * if the unique property for this field is set to 'true'
	 * @return
	 */
	public boolean isUnique()
	{
		if (wrapped.getUnique().equalsIgnoreCase("true"))
		{
			return true;
		}
		
		return false;
	}

	/**
	 * if the unique composite property for this field is set to 'true'
	 * @return
	 */
	public String getUniqueComposite()
	{
		return wrapped.getUniqueComposite();
	}
	
	/**
	 * Returns mapping arrays for this field to its relations.
	 * 0: Sql_Name
	 * 1: Foreign key GenericWrapperField
	 * 2: Foreign element GenericWrapperElement
	 * 3: Local field GenericWrapperField
	 * @return ArrayList of ArrayLists
	 */
	public ArrayList getLocalRefNames() throws org.nrg.xft.exception.ElementNotFoundException
	{
		ArrayList al = new ArrayList();
		try {
			if (isReference())
			{
				XFTReferenceI xftRef = XFTReferenceManager.FindReference(this);
				if (! xftRef.isManyToMany())
				{
					XFTSuperiorReference sup = (XFTSuperiorReference)xftRef;
					Iterator keys = sup.getKeyRelations().iterator();
					while (keys.hasNext())
					{
						XFTRelationSpecification spec = (XFTRelationSpecification)keys.next();

						ArrayList sub = new ArrayList();
						sub.add(spec.getLocalCol().toLowerCase());
						sub.add(spec.getForeignKey());
						sub.add(sup.getSuperiorElement());
						sub.add(this);
						sub.trimToSize();
						al.add(sub);
					}
				}
			}
			al.trimToSize();
		} catch (org.nrg.xft.exception.XFTInitException e) {
			logger.error("'" + this.getParentElement().getFullXMLName() + "' -> '" + this.getName() + "'",e);
		}
		return al;
	}
	
	/**
	 * Return the specified type.
	 * @return
	 */
	public XMLType getXMLType()
	{
		return wrapped.getXMLType();
	}
	
	/**
	 * Returns if this field is an xml attribute.
	 * @return
	 */
	public boolean isAttribute()
	{
		return wrapped.isAttribute();
	}
	
	/**
	 * TRUE if this item was created from a maxOccurs=unbounded reference and was not a root node.
	 * @return
	 */
	public boolean isCreatedChild()
	{
		if (isReference())
		{
			return ((XFTReferenceField)wrapped).isCreatedChild();
		}else{
			return false;
		}
	}
	
	/**
	 * Returns the type formatted by the specified type converter.
	 * @param tc
	 * @return
	 */
	public String getType(TypeConverter tc)
	{
		if (tc.getName().equalsIgnoreCase("SQL"))
		{
			String type = "";
			type=  tc.convert(((XFTDataField)wrapped).getXMLType().getFullLocalType());

			
			if (type.equalsIgnoreCase("VARCHAR") && (wrapped.getSize().length() > 2))
			{
				try{
					int size = Integer.valueOf(wrapped.getSize()).intValue();
					if (size > 256)
					{
						type = tc.convert("LONGVARCHAR");
					}else
					{
						type = "VARCHAR("+ wrapped.getSize()+ ")";
					}
				}catch(Exception ex)
				{
					
				}
			}else{
				if (type.equalsIgnoreCase("VARCHAR"))
				{
					if (wrapped.getSize() != "")
					{
						type = "VARCHAR("+ wrapped.getSize()+ ")";
					}else
					{
						type = "VARCHAR(" + DEFAULT_STRING_SIZE +")";
					}
				}
			}
			return type;
		}else
		{
			String type = "";
			type=  tc.convert(((XFTDataField)wrapped).getXMLType().getFullLocalType());

			if (type.equalsIgnoreCase("VARCHAR") && (wrapped.getSize().length() > 2))
			{
				try{
					int size = Integer.valueOf(wrapped.getSize()).intValue();
					if (size > 256)
					{
						type = tc.convert("LONGVARCHAR");
					}
				}catch(Exception ex)
				{
		
				}
			}
			return type;
		}
	}
	
	/**
	 * Returns true if this field is a primary key, is required, or has use=required.
	 * @return
	 */
	public String getRequired()
	{
		if (isPrimaryKey())
		{
			return "true";
		}
		if (wrapped.getUse().equalsIgnoreCase("required"))
		{
			return "true";
		}
		if (wrapped.getRequired().equalsIgnoreCase("true"))
		{
			return "true";
		}
		if (wrapped.getXMLType().getLocalType().equalsIgnoreCase("string"))
		{
		    if (getWrapped()==null)
		    {
		        return "false";
		    }else{
			    if ((!getWrapped().getRule().getLength().equals("")) || (!getWrapped().getRule().getMinLength().equals("")))
			    {
					return "true";
			    }else{
			        return "false";
			    }
		    }
		}
		if (!wrapped.getMinOccurs().equalsIgnoreCase("0") && !wrapped.getUse().equalsIgnoreCase("optional"))
		{
			return "true";
		}
		return "";
	}
	
	/**
	 * Returns true if this field is required or has use=required.
	 * @return
	 */
	public String getRequiredWOPk()
	{
		if (wrapped.getUse().equalsIgnoreCase("required"))
		{
			return "true";
		}
		if (wrapped.getRequired().equalsIgnoreCase("true"))
		{
			return "true";
		}
		if (!wrapped.getMinOccurs().equalsIgnoreCase("0") && !wrapped.getUse().equalsIgnoreCase("optional"))
			return "true";
		return "";
	}
	
	/**
	 * Returns true if this field is specified as required.
	 * @return
	 */
	
	public boolean isRequired()
	{
		if (getRequired().equalsIgnoreCase("true"))
		{
			return true;
		}else
		{
			return false;
		}
	}
	
	/**
	 * Returns true if this field is specified as auto-increment.
	 * @return
	 */
	public String getAutoIncrement()
	{
		return getSqlField().getAutoIncrement();
	}
	
	/**
	 * Returns the default value of the field's sql field.
	 * @return
	 */
	public String getDefaultValue()
	{
		return getSqlField().getDefaultValue();
	}
	
	/**
	 * Returns the primary key value of the field's sql field.
	 * @return
	 */
	public String getPrimaryKey()
	{
		return getSqlField().getPrimaryKey();
	}
	
	
	/**
	 * if wrapped field has a displayName then that is returned, else
	 * the wrapped field's name is returned.
	 * @return
	 */
	public String getName(boolean withPrefix)
	{
		if (wrapped.getDisplayName() != "")
		{
		    if (withPrefix)
		        return this.getPrefix() +":" + wrapped.getDisplayName();
		    else{
		        return wrapped.getDisplayName();
		    }
		}else
		{
		    if (withPrefix)
			    return this.getPrefix() +":" + wrapped.getName();
		    else{
			    return wrapped.getName();
		    }
		}
	}
	
	public String getPrefix()
	{
	    return this.getParentElement().getGenericXFTElement().getType().getLocalPrefix();
	}
	
	/**
	 * if wrapped field's expose equals 'false' then false is returned, else true is returned.
	 * @return
	 */
	public boolean getExpose()
	{
		if (wrapped.getExpose().equalsIgnoreCase("false"))
		{
			return false;
		}else
		{
			return true;
		}
	}
	
	/**
	 * wrapped field's relation's onDelete (default 'setnull')
	 * @return
	 */
	public String getOnDelete()
	{
		if (this.wrapped.getRelation() != null)
		{
			if (this.wrapped.getRelation().getOnDelete() != "")
			{
				return wrapped.getRelation().getOnDelete();
			}
		}
		return "set null";
	}
	
	/**
	 * wrapped field's relation's foreignKeyTable (default empty string)
	 * @return
	 */
	public String getForeignKeyTable()
	{
		if (this.wrapped.getRelation() != null)
		{
			if (this.wrapped.getRelation().getForeignKeyTable() != "")
			{
				return wrapped.getRelation().getForeignKeyTable();
			}
		}
		return "";
	}
	
	/**
	 * wrapped field's relation's foreignCol (default empty string)
	 * @return
	 */
	public String getForeignCol()
	{
		if (this.wrapped.getRelation() != null)
		{
			if (this.wrapped.getRelation().getForeignCol() != "")
			{
				return wrapped.getRelation().getForeignCol();
			}
		}
		return "";
	}
	
	/**
	 * if wrapped field has a relation
	 * @return
	 */
	public boolean hasRelation()
	{
		if (wrapped.getRelation() != null)
		{
			return true;
		}
	
		return false;
	}
	
	/**
	 * if the field is a XFTDataField with a defined XMLType or a field with no children 
	 * (including attributes).
	 * @param xf
	 * @return
	 */
	public static boolean IsLeafNode(XFTField xf)
	{
		ArrayList fields = xf.getSortedFields();
		if (xf.getClass().getName().equalsIgnoreCase("org.nrg.xft.schema.XFTDataField"))
		{
			XFTDataField data = (XFTDataField)xf;
			if (data.getXMLType() != null)
			{
				return true;
			}
		}
		if (fields.size() == 0)
		{
			return true;
		}else
		{
			return false;
//			boolean hasChild = false;
//			Iterator iter = fields.iterator();
//			while (iter.hasNext())
//			{
//				XFTField sub = (XFTField)iter.next();
//				if (!sub.isAttribute())
//				{
//					hasChild = true;
//					break;
//				}
//			}
//			return (! hasChild);
		}
	}
	
    public boolean isLeafNode(){
        if (this.getWrapped() instanceof XFTDataField){
            if (((XFTDataField)getWrapped()).getXMLType()!=null)
                return true;
        }
        
        if(this.getWrapped().getSortedFields().size()==0){
            return true;
        }else{
            return false;
        }
    }
    
	/**
	 * returns the GenericWrapperElement referenced by the getRefElement() method.
	 * @return
	 * @throws org.nrg.xft.exception.XFTInitException
	 * @throws ElementNotFoundException
	 */
	public SchemaElementI getReferenceElement() throws XFTInitException,ElementNotFoundException
	{
		try {
            if(this.isReference())
            {
            	XFTReferenceField ref = (XFTReferenceField)wrapped;
            	return getFactory().wrapElement(ref.getRefElement());
            }else
            {
            	throw new InvalidReference("Field must be a Reference Field.");
            }
        } catch (XFTInitException e) {
            throw new InvalidReference("Field must be a Reference Field.");
        }
	}
	
	/**
	 * If this field is a reference, then the field's XFTReferenceI is returned.
	 * @return
	 * @throws org.nrg.xft.exception.XFTInitException
	 * @throws ElementNotFoundException
	 */
	public XFTReferenceI getXFTReference() throws org.nrg.xft.exception.XFTInitException,ElementNotFoundException
	{
		if (xftRef == null)
		{
			if(this.isReference())
			{
				xftRef= org.nrg.xft.references.XFTReferenceManager.FindReference(this);
			}else
			{
				throw new InvalidReference("Field must be a Reference Field.");
			}
		}
		return xftRef;
	}
	
	/**
	 * returns the XMLType of the wrapped column.
	 * @return
	 */
	public XMLType getReferenceElementName()
	{
		if(this.isReference())
		{
			XFTReferenceField ref = (XFTReferenceField)wrapped;
			return ref.getXMLType();
		}else
		{
			return null;
		}
	}
    
    

	/**
	 * Generates XFTFields for all of the Referenced Element's primary key fields and returns them as
	 * wrapped Fields.
	 * @param p Parent Element
	 * @return ArrayList of GenericWrapperFields
	 * @throws Exception
	 * Wrapped Key fields of the referenced Element
	 */
	public ArrayList getReferenceKeys(XFTElement p) throws Exception,org.nrg.xft.exception.XFTInitException
	{
		if(this.isReference())
		{
			ArrayList al = new ArrayList();
			XFTReferenceField ref = (XFTReferenceField)wrapped;
			GenericWrapperElement foreign = null;
			foreign = (GenericWrapperElement)getFactory().wrapElement(ref.getRefElement());
			ArrayList allKeys = foreign.getAllPrimaryKeys();
			GenericWrapperField foreignField = (GenericWrapperField)foreign.getWrappedField(getName());
			
			if ((foreignField==null) || (foreignField.getXMLSqlNameValue().equalsIgnoreCase("")))
			{
				Iterator keys = allKeys.iterator();
				while (keys.hasNext())
				{
					GenericWrapperField key = (GenericWrapperField)keys.next();
					XFTDataField field = XFTDataField.GetEmptyField();
					field.setParent(p);
					XFTSqlField sql = field.getSqlField();
					
					if (this.getXMLSqlNameValue().equalsIgnoreCase(""))
					{
						field.setName(key.getSQLName());
						field.setFullName(key.getSQLName());
					}else
					{
						if (allKeys.size() > 1)
						{
							field.setName(getXMLSqlNameValue() + "_" + key.getSQLName());
							field.setFullName(getXMLSqlNameValue() + "_" + key.getSQLName());
						}else
						{
							field.setName(getXMLSqlNameValue());
							field.setFullName(getXMLSqlNameValue());
						}
					}
										
					sql.setAutoIncrement(this.getAutoIncrement());
					sql.setDefaultValue(getSqlField().getDefaultValue());
					sql.setIndex(getSqlField().getIndex());
					sql.setKey(getSqlField().getKey());
					sql.setPrimaryKey(getSqlField().getPrimaryKey());
					sql.setType(getSqlField().getType());

					field.setXMLType(((XFTDataField)key.getWrapped()).getXMLType());
					field.setRequired(this.getRequired());
					field.setSize(key.getAdjustedSize());

					field.setRelation(this.wrapped.getRelation());
					field.setSqlField(sql);
					field.setWebAppField(wrapped.getWebAppField());
					GenericWrapperField gwf = (GenericWrapperField)getFactory().wrapField(field);
					al.add(gwf);
				}
			}else
			{
				if (allKeys.size() > 1)
				{
					Iterator keys = allKeys.iterator();
					while (keys.hasNext())
					{
						GenericWrapperField key = (GenericWrapperField)keys.next();
						if (key.isReference())
						{
							al.addAll(key.getReferenceKeys(p));
						}else
						{
							XFTDataField field = XFTDataField.GetEmptyField();
							field.setParent(p);
							XFTSqlField sql = field.getSqlField();
							
							
							field.setName(getXMLSqlNameValue() + "_" + key.getSQLName());
							field.setFullName(getXMLSqlNameValue() + "_" + key.getSQLName());
												
							sql.setAutoIncrement(this.getAutoIncrement());
							sql.setDefaultValue(getSqlField().getDefaultValue());
							sql.setIndex(getSqlField().getIndex());
							sql.setKey(getSqlField().getKey());
							sql.setPrimaryKey(getSqlField().getPrimaryKey());
							sql.setType(getSqlField().getType());
		
							field.setXMLType(((XFTDataField)key.getWrapped()).getXMLType());
							field.setRequired(this.getRequired());
							field.setSize(key.getAdjustedSize());
		
							field.setRelation(this.wrapped.getRelation());
							field.setSqlField(sql);
							field.setWebAppField(wrapped.getWebAppField());
							GenericWrapperField gwf = (GenericWrapperField)getFactory().wrapField(field);
							al.add(gwf);
						}
					}
				}
				else
				{
					Iterator keys = allKeys.iterator();
					while (keys.hasNext())
					{
						GenericWrapperField key = (GenericWrapperField)keys.next();
						if (key.isReference())
						{
							al.addAll(key.getReferenceKeys(p));
						}else
						{
							XFTDataField field = XFTDataField.GetEmptyField();
							field.setParent(p);
							XFTSqlField sql = field.getSqlField();
							
							if (getXMLSqlNameValue().equalsIgnoreCase(""))
							{
								field.setName(key.getSQLName());
								field.setFullName(key.getSQLName());
							}
							else
							{
								field.setName(getXMLSqlNameValue());
								field.setFullName(getXMLSqlNameValue());
							}
												
							sql.setAutoIncrement(this.getAutoIncrement());
							sql.setDefaultValue(getSqlField().getDefaultValue());
							sql.setIndex(getSqlField().getIndex());
							sql.setKey(getSqlField().getKey());
							sql.setPrimaryKey(getSqlField().getPrimaryKey());
							sql.setType(getSqlField().getType());
		
							field.setXMLType(((XFTDataField)key.getWrapped()).getXMLType());
							field.setRequired(this.getRequired());
							field.setSize(key.getAdjustedSize());
		
							field.setRelation(this.wrapped.getRelation());
							field.setSqlField(sql);
							field.setWebAppField(wrapped.getWebAppField());
							
							GenericWrapperField gwf = (GenericWrapperField)getFactory().wrapField(field);
							al.add(gwf);
						}
					}
				}
			}
			return al;
		}else
		{
			throw new Exception("Field must be a Reference Field.");
		}
	}
	
	/**
	 * Gets lower-case version of base element property fo the wrapped field
	 * @return
	 */
	public String getBaseElement()
	{
		return wrapped.getBaseElement().toLowerCase();
	}
	
	/**
	 * Gets baseCol property of wrapped field
	 * @return
	 */
	public String getBaseCol()
	{
		return wrapped.getBaseCol();
	}
	
	/**
	 * if baseCol and baseElement are empty.
	 * @return
	 */
	public boolean hasBase()
	{
		if ((getBaseCol()!="") && (getBaseElement()!=""))
		{
			return true;
		}else
		{
			return false;
		}
	}

	
	/**
	 * ArrayList of String (possibleValue)
	 * @return
	 */
	public ArrayList getPossibleValues()
	{
	    ArrayList al = new ArrayList();
	    Iterator inner = getRules().iterator();
		while(inner.hasNext())
		{
		    String[] o = (String[])inner.next();
		    if (o[0].equalsIgnoreCase("comparison"))
		    {
		        al.add(o[1]);
		    }
		}
		al.trimToSize();
		return al;
	}
	
	/**
	 * Assigns rules based on this field's data type and its additional details.
	 * 0: rule type
	 * 1: rule size
	 * 2: value type
	 * @return ArrayList of String[]
	 */
	public ArrayList getRules()
	{
		ArrayList al = new ArrayList();
		XFTRule xr = wrapped.getRule();
		
		String prefix = this.getWrapped().getParentElement().getSchemaPrefix();
	
		String type = "";
	
		if (xr != null)
		{
			if (xr.getBaseType().equalsIgnoreCase(""))
			{
				if (getXMLType() != null)
					type = getXMLType().getFullLocalType();
			}else
			{
				type = xr.getBaseType();
			}
		}else
		{
			type = getXMLType().getFullLocalType();
		}
	
		if (getRequiredWOPk().equalsIgnoreCase("true"))
		{
			String [] rule1 = {"required","true",type};
			al.add(rule1);
		}		
		
		if (this.isReference())
		{
			String [] rule1 = {"foreign","true",type};
			al.add(rule1);
		}
		
		if (type.equalsIgnoreCase(prefix+":string"))
		{
			String size = getSize();
			if (size.equalsIgnoreCase(""))
			{
				size = DEFAULT_STRING_SIZE;
			}

			String [] rule1 = {"maxLength",size,type};
			al.add(rule1);
		
			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
		
			String mask = xr.getMask();
			if (! mask.equalsIgnoreCase(""))
			{
				String [] rule ={"mask",mask,type};
				al.add(rule);
			}
		
			if (xr.getPossibleValues().size() > 0)
			{
				Iterator iter = xr.getPossibleValues().iterator();
				while (iter.hasNext())
				{
					String [] rule ={"comparison",(String)iter.next(),type};
					al.add(rule);
				}
			}
			if (al.size() == 0)
			{
				String [] rule3 = {"required","false",type};
				al.add(rule3);
			}
		}else if (type.equalsIgnoreCase(prefix+":boolean"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":float"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":double"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":decimal"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":integer"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":gYear"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":nonPositiveInteger"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":negativeInteger"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":long"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":int"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":short"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":byte"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":nonNegativeInteger"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":unsignedLong"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":unsignedInt"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":unsignedShort"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":unsignedByte"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":positiveInteger"))
		{
			String size = getSize();
			if (! size.equalsIgnoreCase(""))
			{
				String [] rule1 = {"maxLength",size,type};
				al.add(rule1);
			}

			String min = xr.getMinInclusive();
			if (min.equalsIgnoreCase(""))
			{
				min = xr.getMinLength();
			}
			if (! min.equalsIgnoreCase(""))
			{
				String [] rule ={"minLength",min,type};
				al.add(rule);
			}
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":time"))
		{
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":date"))
		{
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":dateTime"))
		{
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":ID"))
		{
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else if (type.equalsIgnoreCase(prefix+":IDREF"))
		{
			if (al.size() == 0)
			{
				String [] rule1 = {"required","false",type};
				al.add(rule1);
			}
		}else
        {
            if (al.size() == 0)
            {
                String [] rule1 = {"required","false",type};
                al.add(rule1);
            }
        }
	
		return al;
	}
	
	
	/**
	 * Returns name and level of all xml data fields.
	 * @return ArrayList of XMLFieldData
	 */
	public ArrayList getXMLFields(int level,ArrayList layers, Hashtable uriToPrefixMapping)
	{
		ArrayList al = new ArrayList();
		
		if (getXMLType() == null)
		{
			Iterator fields = this.getDirectFieldsNoFilter().iterator();
			while (fields.hasNext())
			{
				GenericWrapperField field = ((GenericWrapperField)fields.next());
				if (! field.isXMLOnly())
				{
					ArrayList subLayers = new ArrayList();
					subLayers.addAll(layers);
					if (uriToPrefixMapping.get(getParentElement().getGenericXFTElement().getSchemaTargetNamespaceURI())==null)
					    subLayers.add(getName());
					else
					{
					    String prefix = (String)uriToPrefixMapping.get(getParentElement().getGenericXFTElement().getSchemaTargetNamespaceURI());
					    subLayers.add(prefix + ":" + getName());
					}
					al.addAll(field.getXMLFields(level+1,subLayers,uriToPrefixMapping));
				}
			}
		}else
		{
			if (this.isReference())
			{
				XMLFieldData data = new XMLFieldData();
				
				data.setSqlName(getSQLName());
				data.setLevels(new Integer(level));
				data.setAttribute(this.isAttribute());
				data.setReference(true);
				data.setXmlFieldName(this.getName());
				if (this.isMultiple())
					data.setMultiple(true);
				else
					data.setMultiple(false);
		
				ArrayList subLayers = new ArrayList();
				subLayers.addAll(layers);
				data.setLayers(subLayers);
				data.setXmlType(this.getXMLType());
				data.setChildXMLNode(this.isChildXMLNode());
				data.setRequired(this.getRequired());
				data.setField(this);
				al.add(data);
			}else if (isAttribute())
			{
				XMLFieldData data = new XMLFieldData();
				
				data.setSqlName(getSQLName());
				data.setLevels(new Integer(level));
				data.setAttribute(true);
				data.setReference(false);
				data.setXmlFieldName(this.getName());
				if (this.isMultiple())
					data.setMultiple(true);
				else
					data.setMultiple(false);

				ArrayList subLayers = new ArrayList();
				subLayers.addAll(layers);
				data.setLayers(subLayers);
				data.setXmlType(this.getXMLType());
				data.setChildXMLNode(this.isChildXMLNode());
				data.setRequired(this.getRequired());
				data.setField(this);
				al.add(data);
			}else
			{
				XMLFieldData data = new XMLFieldData();
				
				data.setSqlName(getSQLName());
				data.setLevels(new Integer(level));
				data.setAttribute(false);
				data.setReference(false);
				data.setXmlFieldName(this.getName());
				if (this.isMultiple())
					data.setMultiple(true);
				else
					data.setMultiple(false);
						
				data.setExtension(isExtension());				
				data.setChildXMLNode(isChildXMLNode());
												
				ArrayList subLayers = new ArrayList();
				subLayers.addAll(layers);
				data.setLayers(subLayers);
				data.setXmlType(this.getXMLType());
				data.setRequired(this.getRequired());
				data.setField(this);
				al.add(data);
					
					// add this element to layers
				subLayers = new ArrayList();
				subLayers.addAll(layers);
				if (uriToPrefixMapping.get(getParentElement().getGenericXFTElement().getSchemaTargetNamespaceURI())==null)
				    subLayers.add(getName());
				else
				{
				    String prefix = (String)uriToPrefixMapping.get(getParentElement().getGenericXFTElement().getSchemaTargetNamespaceURI());
				    subLayers.add(prefix + ":" + getName());
				}
				Iterator fields = this.getDirectFieldsNoFilter().iterator();
				while (fields.hasNext())
				{
					GenericWrapperField field = ((GenericWrapperField)fields.next());
					if ((! field.isXMLOnly()) && (field.isAttribute()))
					{
						al.addAll(field.getXMLFields(level+1,subLayers,uriToPrefixMapping));
					}
				}
			}
		}	
		
		return al;
	}
	
	/**
	 * if wrapped field isExtension()
	 * @return
	 */
	public boolean isExtension()
	{
		return getWrapped().isExtension();
	}

	/**
	 * if this field has a direct child (level 1) with the specified xml name, it is returned.
	 * @param name (XML name)
	 * @return GenericWrapperField
	 */
	public GenericWrapperField getDirectField(String name) throws FieldNotFoundException
	{
		Iterator iter = this.getDirectFieldsNoFilter().iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
			if (field.getName().equalsIgnoreCase(name)) {
				return field;
			}
		}
		throw new FieldNotFoundException(name);
	}
	
	/**
	 * if field's reference element will have its own distinct XML Node.
	 * @return
	 */
	public boolean isChildXMLNode()
	{
		if (this.isReference())
		{
			return ((XFTReferenceField)wrapped).isChildXMLNode();
		}else
		{
			return true;
		}
	}
	
	/**
	 * if field is an in-line-repeater-element.
	 * @return
	 */
	public boolean isInLineRepeaterElement()
	{
		if (this.isReference())
		{
			return ((XFTReferenceField)wrapped).isInLineRepeaterElement();
		}else
		{
			return true;
		}
	}
	
	/**
	 * gets XmlDisplay property of the wrapped field.
	 * @return
	 */
	public String getXMLDisplay()
	{
		return getWrapped().getXmlDisplay();
	}
	
	/**
	 * @param header
	 * @return ArrayList of Object[xmlPathName,GenericWrapperField]
	 */
	public ArrayList getAllFieldsWXMLPath(String header) throws XFTInitException,ElementNotFoundException
	{
		ArrayList al = new ArrayList();
		Iterator dF = this.getDirectFieldsNoFilter().iterator();
		while (dF.hasNext())
		{
			GenericWrapperField f = (GenericWrapperField)dF.next();
			if (f.getExpose())
			{
				if (f.isReference())
				{
					GenericWrapperElement ref = (GenericWrapperElement)f.getReferenceElement().getGenericXFTElement();
					al.addAll(ref.getAllFieldsWXMLPath(header +XFT.PATH_SEPERATOR + f.getName()));
				}else{
					al.addAll(f.getAllFieldsWXMLPath(header + XFT.PATH_SEPERATOR + f.getName()));
				}
			}
		}

		if (getXMLType() != null)
		{
			al.add(new Object[]{header,this});
		}
		
		al.trimToSize();
		return al;
	}
	
	/**
	 * @return
	 */
	public String getRelationType()
	{
		if (wrapped.getRelation() != null)
		{
			return wrapped.getRelation().getRelationType();
		}else
		{
			return XFTRelation.DEFAULT_RELATION_TYPE;
		}
	}
	
	/**
	 * @return
	 */
	public String getRelationName()
	{
		if (wrapped.getRelation() != null)
		{
			return wrapped.getRelation().getRelationName();
		}else
		{
			return null;
		}
	}
	
	public ArrayList getDuplicateRelationships()
	{
	    if (wrapped.getRelation() != null)
	    {
	        return wrapped.getRelation().getDuplicateRelationships();
	    }else{
	        return new ArrayList();
	    }
	}
	
	public boolean hasDuplicateRelationships()
	{
	    if (getDuplicateRelationships().size()>0)
	    {
	        return true;
	    }else{
	        return false;
	    }
	}
	
	/**
	 * @return
	 */
	public boolean getRelationUnique()
	{
		if (wrapped.getRelation() != null)
		{
			return wrapped.getRelation().isUnique();
		}else
		{
			return false;
		}
	}
	
	/**
	 * @return
	 */
	public String getRelationUniqueComposite()
	{
		if (wrapped.getRelation() != null)
		{
			return wrapped.getRelation().getUniqueComposite();
		}else
		{
			return "";
		}
	}
	
	public String toString()
	{
		return this.getName() + " -> " + this.getXMLType().getFullForeignType();
	}
	
	private String _finalId =null;
	public String getId()
	{
	    if (_finalId==null)
	    {
			if (this.isReference())
			{
				_finalId= (this.getSQLName() + "_" + this.getXMLType().getLocalType()).toLowerCase();
			}else{
			    _finalId= this.getSQLName().toLowerCase();
			}
	    }
	    
	    return _finalId;
	}
//	
//	public String getDotSyntaxName()
//	{
//		XFTNode p = wrapped.getParent();
//		if (p ==null || p.getClass().getName().equalsIgnoreCase(XFTElement.class.getName()))
//		{
//			return getName();
//		}else{
//			GenericWrapperField f = (GenericWrapperField)GenericWrapperFactory.GetInstance().wrapField((XFTField)p);
//			return f.getDotSyntaxName() + XFT.PATH_SEPERATOR + getName();
//		}
//	}
		
	public String getXMLPathString(String elementName)
	{
	    return elementName + XFT.PATH_SEPERATOR + getXPATH();
	}
	
	public String getXMLPathString()
	{
	    return getXPATH();
	}
	
	public String getXPATH()
	{
		XFTNode p = wrapped.getParent();
		if (p ==null || p.getClass().getName().equalsIgnoreCase(XFTElement.class.getName()))
		{
			return getName();
		}else{
			GenericWrapperField f = (GenericWrapperField)GenericWrapperFactory.GetInstance().wrapField((XFTField)p);
			if (this.isAttribute())
			{
				return f.getXPATH() + "/" +  getName();
			}else{
				return f.getXPATH() + "/" +  getName();
			}
		}
	}
	
	public Object parseValue(Object o)
	{
	    return this.getXMLType().parseValue(o);
	}
	
	public boolean isFilter()
	{
	    return this.wrapped.isFilter();
	}
	
	public GenericWrapperField getGenericXFTField(){
	    return this;
	}
    
	public boolean isBooleanField()
	{
		if (isReference())
		{
			return false;
		}else{
			if (getXMLType().getLocalType().equalsIgnoreCase("boolean"))
			{
				return true;
			}else{
				return false;
			}
		}
	}


	/**
	 * @return
	 */
	protected XFTElementWrapper getParentE() throws ClassCastException
	{
		try {
            return GenericWrapperElement.GetElement(this.getWrapped().getParentElement().getType());
        } catch (XFTInitException e) {
            logger.error("",e);
            return null;
        } catch (ElementNotFoundException e) {
            logger.error("",e);
            return null;
        }
	}
	
	public Object formatValue(Object o) throws InvalidValueException
	{
	    try {
            return DBAction.ValueParser(o,this,false);
        } catch (XFTInitException e) {
            logger.error("",e);
            return o;
        }
	}
	

	
	public boolean compareValues(Object o1, Object o2)
	{
	    if (o1 == null && o2 == null)
	    {
	        return true;
	    }else if (o1 == null || o2 == null)
	    {
            if (o1==null){
                if (o2.equals("NULL"))
                    return true;
                else
                    return false;
            }
            if (o2==null){
                if (o1.equals("NULL"))
                    return true;
                else
                    return false;
            }
	        return false;
	    }else{
	        String s1;
			try {
				s1 = formatValue(o1).toString();
			} catch (InvalidValueException e) {
				s1=o1.toString();
			}
	        String s2;
			try {
				s2 = formatValue(o2).toString();
			} catch (InvalidValueException e) {
				s2=o2.toString();
			}
	        if (s1.equals(s2))
	        {
	            return true;
	        }else{
	            return false;
	        }
	    }
	}
    
    public boolean getPreventLoop(){
        return wrapped.getPreventLoop();
    }
	
	public boolean isOnlyRoot(){
		return wrapped.isOnlyRoot();
	}
    
    public boolean isPossibleLoop(){
        return wrapped.isPossibleLoop();
    }
}

