/*
 * core: org.nrg.xdat.om.XdatCriteria
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.om;

import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
import org.nrg.xdat.om.base.BaseXdatCriteria;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.search.ElementCriteria;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.search.SQLClause;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatCriteria extends BaseXdatCriteria {

	public XdatCriteria(ItemI item)
	{
		super(item);
	}

	public XdatCriteria(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatCriteria(UserI user)
	 **/
	public XdatCriteria()
	{}

	public XdatCriteria(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

	
	public void populateCriteria(SQLClause c){
		if (c instanceof ElementCriteria)
        {
            ElementCriteria ec = (ElementCriteria)c;
            this.setSchemaField(ec.getXMLPath());
            this.setComparisonType(ec.getComparison_type());
            this.setValue(ec.getValue().toString());
            this.setOverrideValueFormatting(ec.overrideFormatting());
        }else if(c instanceof SearchCriteria){
            SearchCriteria ec = (SearchCriteria)c;
            this.setSchemaField(ec.getXMLPath());
            this.setComparisonType(ec.getComparison_type());
            this.setValue(ec.getValue().toString());
            this.setOverrideValueFormatting(ec.overrideFormatting());
        }else{
            DisplayCriteria dc = (DisplayCriteria)c;
            this.setSchemaField(dc.getElementName() + "." + dc.getField());
            this.setComparisonType(dc.getComparisonType());
            this.setValue(dc.getValue().toString());
            this.setOverrideValueFormatting(dc.isOverrideDataFormatting());
        }
	}
	
	public SQLClause buildDisplaySearchCriteria() throws Exception{
		String comparison_type = getComparisonType();
        if (comparison_type==null)
            comparison_type="=";
        String value = getValue();
        Boolean overrideValueFormatting = getOverrideValueFormatting();
        if (overrideValueFormatting==null)
            overrideValueFormatting=Boolean.FALSE;

        if(comparison_type.trim().startsWith("IS") 
        		&& overrideValueFormatting.equals(Boolean.FALSE)
        		&& !comparison_type.trim().contains("FROM") ){
        	overrideValueFormatting=Boolean.TRUE;
        	if(comparison_type.trim().equals("IS")){
        		if(value.trim().equals("NOT NULL")){
        			value=" NOT NULL";
        		}else{
        			value=" NULL";
        		}
        		comparison_type=" IS ";
        	}else if(comparison_type.trim().equals("IS NOT") || comparison_type.trim().equals("IS NOT NULL")){
        		comparison_type=" IS NOT ";
        		value = " NULL";
        	}else if(comparison_type.trim().equals("IS NULL")){
        		comparison_type=" IS ";
        		value = " NULL";
        	}
        }
        final String schema_field = getSchemaField();
        
		//the ordering here was questionable.  What we really need is different type of Criteria objects ElementCriteria vs DisplayCriteria.
        // instead we only have one type XdatCriteriaSet which needs to be mapped to one of those types.  This should be adjusted in the proposed search refactoring.
        // In the meantime, we can move this into separate methods to pinpoint which Criteria type should be tried first.

        try {
            final String rootElement = XftStringUtils.GetRootElementName(schema_field);
            String fieldName = XftStringUtils.GetFieldText(schema_field);
            final SchemaElement se = SchemaElement.GetElement(rootElement);
            final DisplayCriteria dc = new DisplayCriteria();
            if(fieldName.contains("=")){
    	    	dc.setWhere_value(fieldName.substring(fieldName.indexOf("=")+1));
    	    	fieldName=fieldName.substring(0,fieldName.indexOf("="));
    	    }
            se.getDisplayField(fieldName);
            dc.setSearchFieldByDisplayField(rootElement,fieldName);
            dc.setComparisonType(comparison_type);
            dc.setValue(value,true);
            dc.setOverrideDataFormatting(overrideValueFormatting);
            return dc;
        } catch (DisplayFieldNotFoundException e1) {
        	try {
                final ElementCriteria ec = new ElementCriteria();
                ec.setOverrideFormatting(overrideValueFormatting);
                ec.setFieldWXMLPath(schema_field);
                ec.setComparison_type(comparison_type);
                ec.setValue(value);
                return ec;
            } catch (FieldNotFoundException e) {
                logger.error("",e1);
            }
        }
        
        return null;
	}
	
	public SQLClause buildItemSearchCriteria() throws Exception{
		String comparison_type = getComparisonType();
        if (comparison_type==null)
            comparison_type="=";
        String value = getValue();
        Boolean overrideValueFormatting = getOverrideValueFormatting();
        if (overrideValueFormatting==null)
            overrideValueFormatting=Boolean.FALSE;

        if(comparison_type.trim().startsWith("IS") 
        		&& overrideValueFormatting.equals(Boolean.FALSE)
        		&& !comparison_type.trim().contains("FROM") ){
        	overrideValueFormatting=Boolean.TRUE;
        	if(comparison_type.trim().equals("IS")){
        		if(value.trim().equals("NOT NULL")){
        			value=" NOT NULL";
        		}else{
        			value=" NULL";
        		}
        		comparison_type=" IS ";
        	}else if(comparison_type.trim().equals("IS NOT") || comparison_type.trim().equals("IS NOT NULL")){
        		comparison_type=" IS NOT ";
        		value = " NULL";
        	}else if(comparison_type.trim().equals("IS NULL")){
        		comparison_type=" IS ";
        		value = " NULL";
        	}
        }
        final String schema_field = getSchemaField();
        
		//the ordering here was questionable.  What we really need is different type of Criteria objects ElementCriteria vs DisplayCriteria.
        // instead we only have one type XdatCriteriaSet which needs to be mapped to one of those types.  This should be adjusted in the proposed search refactoring.
        // In the meantime, we can move this into separate methods to pinpoint which Criteria type should be tried first.
        try {
            final ElementCriteria ec = new ElementCriteria();
            ec.setOverrideFormatting(overrideValueFormatting);
            ec.setFieldWXMLPath(schema_field);
            ec.setComparison_type(comparison_type);
            ec.setValue(value);
            return ec;
        } catch (FieldNotFoundException e) {
            //NOT AN STANDARD FIELD... CHECK DISPLAY FIELDS
            final String rootElement = XftStringUtils.GetRootElementName(schema_field);
            String fieldName = XftStringUtils.GetFieldText(schema_field);
            final SchemaElement se = SchemaElement.GetElement(rootElement);
            try {
                final DisplayCriteria dc = new DisplayCriteria();
                if(fieldName.contains("=")){
        	    	dc.setWhere_value(fieldName.substring(fieldName.indexOf("=")+1));
        	    	fieldName=fieldName.substring(0,fieldName.indexOf("="));
        	    }
                se.getDisplayField(fieldName);
                dc.setSearchFieldByDisplayField(rootElement,fieldName);
                dc.setComparisonType(comparison_type);
                dc.setValue(value,true);
                dc.setOverrideDataFormatting(overrideValueFormatting);
                return dc;
            } catch (DisplayFieldNotFoundException e1) {
                logger.error("",e1);
            }

        }
        
        return null;
	}

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof XdatCriteria) {
            XdatCriteria other = (XdatCriteria) anObject;
            return StringUtils.equals(this.getComparisonType(), other.getComparisonType()) &&
                   StringUtils.equals(this.getCustomSearch(), other.getCustomSearch()) &&
                   StringUtils.equals(this.getSchemaElementName(), other.getSchemaElementName()) &&
                   StringUtils.equals(this.getSchemaField(), other.getSchemaField()) &&
                   StringUtils.equals(this.getValue(), other.getValue());
        }
        return false;
    }
}
