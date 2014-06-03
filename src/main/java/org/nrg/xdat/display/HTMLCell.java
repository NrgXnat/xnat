/*
 * org.nrg.xdat.display.HTMLCell
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.display;

/**
 * @author Tim
 *
 */
public class HTMLCell {
	private Integer width = null;
	private Integer height = null;
	
	private String valign = null;
	private String align = null;
	private String serverLink = null;
	/**
	 * @return
	 */
	public Integer getHeight() {
		return height;
	}

	/**
	 * @return
	 */
	public Integer getWidth() {
		return width;
	}

	/**
	 * @param integer
	 */
	public void setHeight(Integer integer) {
		height = integer;
	}
	
	public void setHeight(String s)
	{
		if (! s.equalsIgnoreCase(""))
			height = Integer.valueOf(s);
	}

	/**
	 * @param integer
	 */
	public void setWidth(Integer integer) {
		width = integer;
	}
	
	public void setWidth(String s)
	{
		if (! s.equalsIgnoreCase(""))
			width = Integer.valueOf(s);
	}

	/**
	 * @return
	 */
	public String getAlign() {
		return align;
	}

	/**
	 * @return
	 */
	public String getServerLink() {
		return serverLink;
	}

	/**
	 * @return
	 */
	public String getValign() {
		return valign;
	}

	/**
	 * @param string
	 */
	public void setAlign(String string) {
		align = string;
	}

	/**
	 * @param string
	 */
	public void setServerLink(String string) {
		serverLink = string;
	}

	/**
	 * @param string
	 */
	public void setValign(String string) {
		valign = string;
	}

}

