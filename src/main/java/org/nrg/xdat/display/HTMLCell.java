//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 5, 2005
 *
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

