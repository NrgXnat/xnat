/*
 * core: org.nrg.xft.references.XFTRelationSpecification
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.references;

import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.XMLType;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;

public class XFTRelationSpecification {
	private String localTable=null;
	private String localCol=null;
	private String foreignTable = null;
	private String foreignCol = null;
	private XMLType schemaType = null;
	private GenericWrapperField localKey=null;
	
	private GenericWrapperField foreignKey = null;
	
	private String fieldSize = null;
	private XFTSuperiorReference parent = null;
	
	/**
	 * Constructs the XFTRelationSpecification using the assigned values.
	 * @param lTable
	 * @param lCol
	 * @param fTable
	 * @param fCol
	 * @param type
	 * @param fkey
	 * @param lKey
	 */
	public XFTRelationSpecification(String lTable,String lCol,String fTable,String fCol,XMLType type,GenericWrapperField fkey,GenericWrapperField lKey,XFTSuperiorReference p)
	{
		localTable = lTable;
		localCol = lCol;
		foreignTable = fTable;
		foreignCol = fCol;
		schemaType = type;
		foreignKey = fkey;
		localKey = lKey;
		parent=p;
	}
	/**
	 * @return Returns the foreign column
	 */
	public String getForeignCol() {
		return foreignCol;
	}

	/**
	 * @return Returns the foreign table
	 */
	public String getForeignTable() {
		return foreignTable;
	}

	/**
	 * @return Returns the local column
	 */
	public String getLocalCol() {
	    if (localCol != null)
	    {
	        if (localCol.length()>63)
	        {
	            localCol = localCol.substring(0,63);
	        }
	    }
		return localCol;
	}

	/**
	 * @return Returns the local table
	 */
	public String getLocalTable() {
		return localTable;
	}

	/**
	 * @return Returns the schema type
	 */
	public XMLType getSchemaType() {
		return schemaType;
	}

	/**
	 * @return Returns the foreign key
	 */
	public GenericWrapperField getForeignKey() {
		return foreignKey;
	}

	/**
	 * @param field
	 */
	public void setForeignKey(GenericWrapperField field) {
		foreignKey = field;
	}

	/**
	 * @return Returns the local key
	 */
	public GenericWrapperField getLocalKey() {
		return localKey;
	}

	/**
	 * @param field
	 */
	public void setLocalKey(GenericWrapperField field) {
		localKey = field;
	}

	public String toString(){
	    StringBuffer sb = new StringBuffer();
	    sb.append(this.getLocalTable() + "." +this.getLocalCol()).append("=").append(this.getForeignTable() + "." + foreignCol);
	    sb.append("  ").append(this.schemaType.toString());
	    return sb.toString();
	}
	
	public String getLocalXMLPath() throws FieldNotFoundException
	{
	    if (localKey != null)
	    {
	        if (localKey.isReference())
            {
                return localCol;
            }else{
                return localKey.getXMLPathString();
            }
	    }else{
	        try {
                GenericWrapperElement e = parent.getSubordinateElement();
                GenericWrapperField f = e.getFieldBySQLName(this.getLocalCol());
                if (f.isReference())
                {
                    return localCol;
                }else{
                    return f.getXMLPathString();
                }
            } catch (ElementNotFoundException e) {
                return null;
            } catch (XFTInitException e) {
                return null;
            }
	    }
	}
}

