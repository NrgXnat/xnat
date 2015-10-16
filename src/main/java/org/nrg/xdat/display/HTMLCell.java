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

import org.apache.commons.lang.StringUtils;

/**
 * @author Tim
 */
public class HTMLCell {
    private Integer width = null;
    private Integer height = null;

    private String valign = null;
    private String align = null;
    private String serverLink = null;

    /**
     * @return The cell height.
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * @return The cell width.
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Sets the cell's height.
     *
     * @param height The height to set.
     */
    public void setHeight(final Integer height) {
        this.height = height;
    }

    /**
     * Sets the cell's height.
     *
     * @param height The height to set.
     */
    public void setHeight(final String height) {
        if (StringUtils.isNotBlank(height)) {
            setHeight(Integer.valueOf(height));
        }
    }

    /**
     * Sets the cell's width.
     *
     * @param width The width to set.
     */
    public void setWidth(final Integer width) {
        this.width = width;
    }

    /**
     * Sets the cell's width.
     *
     * @param width The width to set.
     */
    public void setWidth(final String width) {
        if (StringUtils.isNotBlank(width)) {
            setWidth(Integer.valueOf(width));
        }
    }

    /**
     * Gets the alignment setting for the cell.
     *
     * @return The alignment setting for the cell.
     */
    public String getAlign() {
        return align;
    }

    /**
     * Gets the server link for the cell.
     *
     * @return The server link for the cell.
     */
    @SuppressWarnings("unused")
    public String getServerLink() {
        return serverLink;
    }

    /**
     * Gets the vertical alignment setting for the cell.
     *
     * @return The vertical alignment setting for the cell.
     */
    public String getValign() {
        return valign;
    }

    /**
     * Sets the alignment setting for the cell.
     *
     * @param align    The alignment setting for the cell.
     */
    public void setAlign(final String align) {
        this.align = align;
    }

    /**
     * Sets the server link for the cell.
     *
     * @param serverLink    The server link for the cell.
     */
    public void setServerLink(final String serverLink) {
        this.serverLink = serverLink;
    }

    /**
     * Sets the vertical alignment setting for the cell.
     *
     * @param valign    The vertical alignment setting for the cell.
     */
    public void setValign(final String valign) {
        this.valign = valign;
    }

}

