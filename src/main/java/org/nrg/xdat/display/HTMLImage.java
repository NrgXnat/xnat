/*
 * org.nrg.xdat.display.HTMLImage
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
public class HTMLImage {
	private Integer width = null;
	private Integer height = null;
	/**
	 * @return The image height.
	 */
	public Integer getHeight() {
		return height;
	}

	/**
	 * @return The image width.
	 */
	public Integer getWidth() {
		return width;
	}

	/**
	 * @param integer    The height to set for the image.
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
	 * @param integer    The width to set for the image.
	 */
	public void setWidth(Integer integer) {
		width = integer;
	}
	
	public void setWidth(String s)
	{
		if (! s.equalsIgnoreCase(""))
			width = Integer.valueOf(s);
	}
}

