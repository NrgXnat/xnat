/*
 * org.nrg.xdat.om.base.BaseXnatDatatypeprotocol
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.model.XnatFielddefinitiongroupFieldI;
import org.nrg.xdat.model.XnatFielddefinitiongroupI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.om.XdatSearchField;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatDatatypeprotocol;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatDatatypeprotocol extends AutoXnatDatatypeprotocol {

	public BaseXnatDatatypeprotocol(ItemI item)
	{
		super(item);
	}

	public BaseXnatDatatypeprotocol(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatDatatypeprotocol(UserI user)
	 **/
	public BaseXnatDatatypeprotocol()
	{}

	public BaseXnatDatatypeprotocol(Hashtable properties, UserI user)
	{
		super(properties,user);
	}


    public XdatStoredSearch getDefaultSearch(XnatProjectdataI project){
        XdatStoredSearch xss = super.getDefaultSearch((XnatProjectdata)project);

        for(XnatFielddefinitiongroupI group : this.getDefinitions_definition()){
            for(XnatFielddefinitiongroupFieldI field : group.getFields_field()){

                XdatSearchField xsf = new XdatSearchField(this.getUser());
                xsf.setElementName(this.getDataType());
                String fieldID=null;
                if (field.getType().equals("custom"))
                {
                    fieldID=this.getDatatypeSchemaElement().getSQLName().toUpperCase() + "_FIELD_MAP="+field.getName().toLowerCase();
                    
                }else{
                    try {
                        SchemaElement se=SchemaElement.GetElement(this.getDataType());
                        
                        try {
                            DisplayField df=se.getDisplayFieldForXMLPath(field.getXmlpath());
                            if (df!=null){
                                fieldID=df.getId();
                            }
                        } catch (Exception e) {
                            logger.error("",e);
                        }
                    } catch (XFTInitException e) {
                        logger.error("",e);
                    } catch (ElementNotFoundException e) {
                        logger.error("",e);
                    }
                }
                    
                if (fieldID!=null){
                    xsf.setFieldId(fieldID);

                    xsf.setHeader(field.getName());
                    xsf.setType(field.getDatatype());
                    xsf.setSequence(xss.getSearchField().size());
                    if (field.getType().equals("custom"))xsf.setValue(field.getName().toLowerCase());
                    try {
                        xss.setSearchField(xsf);
                    	System.out.println("LOADED " + field.getXmlpath());
                    } catch (Exception e) {
                        logger.error("",e);
                    	System.out.println("FAILED to load " + field.getXmlpath());
                    }
                }else{
                	System.out.println("FAILED to load " + field.getXmlpath());
                }
            }
        }
        
            

        return xss;
    }
}
