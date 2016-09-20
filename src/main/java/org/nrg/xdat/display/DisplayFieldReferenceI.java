/*
 * core: org.nrg.xdat.display.DisplayFieldReferenceI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.display;

import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;

import java.util.List;

/**
 * @author Tim
 */
public interface DisplayFieldReferenceI {
    List<String> getSecondaryFields();

    DisplayField getDisplayField() throws DisplayFieldNotFoundException;

    String getId();

    /**
     * Gets the reference header.
     *
     * @return The reference header.
     */
    String getHeader();

    String getLightColor();

    String getDarkColor();

    Integer getHeaderCellWidth();

    Integer getHeaderCellHeight();

    String getHeaderCellAlign();

    String getHeaderCellVAlign();

    Integer getHTMLCellWidth();

    Integer getHTMLCellHeight();

    String getHTMLCellAlign();

    String getHTMLCellVAlign();

    String getElementName();

    String getRowID();

    String getSortBy();

    String getType();

    HTMLLink getHTMLLink();

    boolean isImage();

    boolean isHtmlContent();

    Object getValue();

    boolean isVisible() throws DisplayFieldNotFoundException;

    void setValue(Object o);

    String getElementSQLName() throws XFTInitException, ElementNotFoundException;
}
