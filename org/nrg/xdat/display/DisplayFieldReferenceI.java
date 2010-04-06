//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on May 11, 2005
 *
 */
package org.nrg.xdat.display;

import java.util.ArrayList;

import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;

/**
 * @author Tim
 *
 */
public interface DisplayFieldReferenceI {
    public ArrayList getSecondaryFields();
    
    public DisplayField getDisplayField()  throws DisplayFieldNotFoundException;
    public String getId();
    /**
     * @return
     */
    public String getHeader();

    /**
     * @return
     */
    public String getLightColor();
    public String getDarkColor();
    public Integer getHeaderCellWidth();
    public Integer getHeaderCellHeight();
    public String getHeaderCellAlign();
    public String getHeaderCellVAlign();
    public Integer getHTMLCellWidth();
    public Integer getHTMLCellHeight();
    public String getHTMLCellAlign();
    public String getHTMLCellVAlign();
    public String getElementName();
    public String getRowID();
    public String getSortBy();
    public String getType();
    public HTMLLink getHTMLLink();
    public boolean isImage();
    public boolean isHtmlContent();
    public Object getValue();
    
    public boolean isVisible() throws DisplayFieldNotFoundException;
    public void setValue(Object o);
    public String getElementSQLName() throws XFTInitException,ElementNotFoundException;
    
}
