/*
 * core: org.nrg.xdat.display.HTMLLink
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
	private ArrayList<HTMLLinkProperty> properties = new ArrayList<>();
	
	private Hashtable secureProps = new Hashtable();
	/**
	 * @return ArrayList of HTMLLinkProperty
	 */
	public ArrayList<HTMLLinkProperty> getProperties() {
		return properties;
	}

	/**
	 * @param list    The properties to set for the link.
	 */
	public void setProperties(ArrayList<HTMLLinkProperty> list) {
		properties = list;
	}
	
	public void addProperty(HTMLLinkProperty prop)
	{
		properties.add(prop);
	}

	/**
	 * @return The secure link.
	 */
	public String getSecureLinkTo() {
		return secureLinkTo;
	}

	/**
	 * @return The secure properties.
	 */
	public Hashtable getSecureProps() {
		return secureProps;
	}

	/**
	 * @param string    The secure link to set.
	 */
	public void setSecureLinkTo(String string) {
		secureLinkTo = string;
	}

	/**
	 * @param hashtable    The secure properties to set.
	 */
	@SuppressWarnings("unused")
	public void setSecureProps(Hashtable hashtable) {
		secureProps = hashtable;
	}
	
	public boolean isSecure()
	{
		return !(this.getSecureLinkTo() == null || this.getSecureLinkTo().equalsIgnoreCase(""));
	}

}

