/*
 * org.nrg.xdat.om.base.BaseXnatAbstractprotocol
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.XdatSearchField;
import org.nrg.xdat.om.XnatAbstractprotocol;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatAbstractprotocol;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;
import java.util.Iterator;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatAbstractprotocol extends AutoXnatAbstractprotocol {

	public BaseXnatAbstractprotocol(ItemI item)
	{
		super(item);
	}

	public BaseXnatAbstractprotocol(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatAbstractprotocol(UserI user)
	 **/
	public BaseXnatAbstractprotocol()
	{}

	public BaseXnatAbstractprotocol(Hashtable properties, UserI user)
	{
		super(properties,user);
	}


	public String getProject()
	{
		try {
			return this.getStringProperty("xnat_projectdata_id");
		} catch (ElementNotFoundException e) {
			logger.error("",e);
		} catch (FieldNotFoundException e) {
			logger.error("",e);
		}
		return null;
	}

    public SchemaElement getDatatypeSchemaElement(){
        try {
            return SchemaElement.GetElement(this.getDataType());
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }

        return null;
    }

    public XdatStoredSearch getDefaultSearch(XnatProjectdata project){
        XdatStoredSearch xss=null;
        try {
            xss=project.getDefaultSearch(this.getDataType(),this.getId());

			xss.setId(this.getId());
			if (this.getName()!=null)
			    xss.setBriefDescription(this.getName());
			else{
			    xss.setBriefDescription(ElementSecurity.GetElementSecurity(this.getDataType()).getPluralDescription());
			}
			if (this.getDescription()!=null)
			    xss.setDescription(this.getDescription());

			xss.setSecure(true);
			xss.setAllowDiffColumns(false);
			xss.setTag(project.getId()); 
            
            UserI user = this.getUser();

            if (this.getDataType().equals("xnat:subjectData")){
                Iterator protocols2= project.getStudyprotocol().iterator();
                while(protocols2.hasNext()){
                    XnatAbstractprotocol protocol2 = (XnatAbstractprotocol)protocols2.next();
                    try {
                        GenericWrapperElement e = GenericWrapperElement.GetElement(protocol2.getDataType());
                        if (e.instanceOf("xnat:subjectAssessorData"))
                        {
                            XdatSearchField xsf = new XdatSearchField(user);
                            xsf.setElementName("xnat:subjectData");
                            xsf.setFieldId("SUB_EXPT_COUNT=" + protocol2.getDataType());

                            xsf.setHeader(ElementSecurity.GetPluralDescription(protocol2.getDataType()));
                            xsf.setType("integer");
                            xsf.setSequence(xss.getSearchField().size());
                            xsf.setValue(protocol2.getDataType());
                            xss.setSearchField(xsf);
                        }
                    } catch (XFTInitException e) {
                        logger.error("",e);
                    } catch (ElementNotFoundException e) {
                        logger.error("",e);
                    } catch (Exception e) {
                        logger.error("",e);
                    }
                }
            }

            if (this.getDataType().equals("xnat:mrSessionData")){
                Iterator protocols2= project.getStudyprotocol().iterator();
                while(protocols2.hasNext()){
                    XnatAbstractprotocol protocol2 = (XnatAbstractprotocol)protocols2.next();
                    try {
                        GenericWrapperElement e = GenericWrapperElement.GetElement(protocol2.getDataType());
                        if (e.instanceOf("xnat:mrAssessorData"))
                        {
                            XdatSearchField xsf = new XdatSearchField(user);
                            xsf.setElementName("xnat:mrSessionData");
                            xsf.setFieldId("MR_EXPT_COUNT=" + protocol2.getDataType());

                            xsf.setHeader(ElementSecurity.GetPluralDescription(protocol2.getDataType()));
                            xsf.setType("integer");
                            xsf.setSequence(xss.getSearchField().size());
                            xsf.setValue(protocol2.getDataType());
                            xss.setSearchField(xsf);
                        }
                    } catch (XFTInitException e) {
                        logger.error("",e);
                    } catch (ElementNotFoundException e) {
                        logger.error("",e);
                    } catch (Exception e) {
                        logger.error("",e);
                    }
                }
            }

            if (this.getDataType().equals("xnat:petSessionData")){
                Iterator protocols2= project.getStudyprotocol().iterator();
                while(protocols2.hasNext()){
                    XnatAbstractprotocol protocol2 = (XnatAbstractprotocol)protocols2.next();
                    try {
                        GenericWrapperElement e = GenericWrapperElement.GetElement(protocol2.getDataType());
                        if (e.instanceOf("xnat:petAssessorData"))
                        {
                            XdatSearchField xsf = new XdatSearchField(user);
                            xsf.setElementName("xnat:petSessionData");
                            xsf.setFieldId("PET_EXPT_COUNT=" + protocol2.getDataType());

                            xsf.setHeader(ElementSecurity.GetPluralDescription(protocol2.getDataType()));
                            xsf.setType("integer");
                            xsf.setSequence(xss.getSearchField().size());
                            xsf.setValue(protocol2.getDataType());
                            xss.setSearchField(xsf);
                        }
                    } catch (XFTInitException e) {
                        logger.error("",e);
                    } catch (ElementNotFoundException e) {
                        logger.error("",e);
                    } catch (Exception e) {
                        logger.error("",e);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("",e);
        }
        return xss;
    }
}
