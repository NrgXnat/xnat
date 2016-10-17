/*
 * core: org.nrg.xft.meta.XFTMetaElement
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.meta;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.XFTElement;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperFactory;
public class XFTMetaElement {
	private Integer elementId = null;
	private String prefix = null;
	private String localXMLName = null;
	private String javaName = null;
	private String sqlName = null;
	private String code = null;
	private XFTElement element = null;
	
	/**
	 * Populates teh javaName, sqlName, code and full XML Name from the XFTElement.  If allowDBAccess
	 * is true, then the elements ID is selected from the db.
	 * @param e
	 */
	public XFTMetaElement(XFTElement e)
	{
		element = e;
		GenericWrapperElement gwe = (GenericWrapperElement)GenericWrapperFactory.GetInstance().wrapElement(e);
		localXMLName = gwe.getType().getLocalType();
		prefix = gwe.getType().getLocalPrefix();
		sqlName = gwe.getSQLName();
		
		if (gwe.getTypeCode() != "")
			code = gwe.getTypeCode();
		
			javaName = gwe.getJAVAName();
	}
	
	public Integer getElementId(boolean allowDBAccess)
	{
		if (XFTManager.GetElementTable() != null && allowDBAccess && elementId==null)
		{
			try {
				setElementId(XFTElementIDManager.GetInstance().getID(prefix + ":" + localXMLName,true));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return elementId;
	}
	
	public ItemI getExtensionXFTItem(boolean allowDBAccess) throws XFTInitException,ElementNotFoundException
	{
		XFTItem item = XFTItem.NewItem((GenericWrapperElement)GenericWrapperFactory.GetInstance().wrapElement(XFTManager.GetElementTable()),null);;
		if (getElementId(allowDBAccess) != null)
		{
			item.setFieldValue("xdat_xdat_meta_element_id",getElementId(allowDBAccess));
		}
		item.setFieldValue("element_name",prefix + ":" + localXMLName);
		return item;
	}
	
	/**
	 * @return Returns the element
	 */
	public XFTElement getElement() {
		return element;
	}

	/**
	 * @return Returns the element's prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @return Returns the element's java name
	 */
	public String getJavaName() {
		return javaName;
	}

	/**
	 * @return Returns the element's local xml name
	 */
	public String getLocalXMLName() {
		return localXMLName;
	}

	/**
	 * @return Returns the element's sql name
	 */
	public String getSqlName() {
		return sqlName;
	}

	/**
	 * @param element
	 */
	public void setElement(XFTElement element) {
		this.element = element;
	}

	/**
	 * @param integer
	 */
	public void setElementId(Integer integer) {
		elementId = integer;
	}

	/**
	 * @param string
	 */
	public void setPrefix(String string) {
		prefix = string;
	}

	/**
	 * @param string
	 */
	public void setJavaName(String string) {
		javaName = string;
	}

	/**
	 * @param string
	 */
	public void setLocalXMLName(String string) {
		localXMLName = string;
	}

	/**
	 * @param string
	 */
	public void setSqlName(String string) {
		sqlName = string;
	}

	/**
	 * @return Returns the element's code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param string
	 */
	public void setCode(String string) {
		code = string;
	}

}

