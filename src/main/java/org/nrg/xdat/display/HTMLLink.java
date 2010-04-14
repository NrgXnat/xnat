//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 4, 2005
 *
 */
package org.nrg.xdat.display;
import java.util.ArrayList;
import java.util.Hashtable;
/**
 * @author Tim
 *
 */
public class HTMLLink {
	private String secureLinkTo = null;
	private ArrayList<HTMLLinkProperty> properties = new ArrayList<HTMLLinkProperty>();
	
	private Hashtable secureProps = new Hashtable();
	/**
	 * @return ArrayList of HTMLLinkProperty
	 */
	public ArrayList<HTMLLinkProperty> getProperties() {
		return properties;
	}

	/**
	 * @param list
	 */
	public void setProperties(ArrayList<HTMLLinkProperty> list) {
		properties = list;
	}
	
	public void addProperty(HTMLLinkProperty prop)
	{
		properties.add(prop);
	}

	/**
	 * @return
	 */
	public String getSecureLinkTo() {
		return secureLinkTo;
	}

	/**
	 * @return
	 */
	public Hashtable getSecureProps() {
		return secureProps;
	}

	/**
	 * @param string
	 */
	public void setSecureLinkTo(String string) {
		secureLinkTo = string;
	}

	/**
	 * @param hashtable
	 */
	public void setSecureProps(Hashtable hashtable) {
		secureProps = hashtable;
	}
	
	public boolean isSecure()
	{
		if (this.getSecureLinkTo() == null || this.getSecureLinkTo().equalsIgnoreCase(""))
		{
			return false;
		}else{
			return true;
		}
	}

}

