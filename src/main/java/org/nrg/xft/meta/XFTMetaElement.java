/*
 * org.nrg.xft.meta.XFTMetaElement
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
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
	 * @param allowDBAccess
	 */
	public XFTMetaElement(XFTElement e, boolean allowDBAccess)
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
	 * @return
	 */
	public XFTElement getElement() {
		return element;
	}

	/**
	 * @return
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @return
	 */
	public String getJavaName() {
		return javaName;
	}

	/**
	 * @return
	 */
	public String getLocalXMLName() {
		return localXMLName;
	}

	/**
	 * @return
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
	 * @return
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

