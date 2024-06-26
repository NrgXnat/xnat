/*
 * core: org.nrg.xdat.display.MappingColumn
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.display;

import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.utils.XftStringUtils;

/**
 * @author Tim
 */
public class MappingColumn {
    private String rootElement = "";
    private String fieldElementXMLPath = "";
    private String fieldElementFullSQL = null;
    private String mapsTo = "";

    /**
     * @return The field element XML path.
     */
    public String getFieldElementXMLPath() {
        return fieldElementXMLPath;
    }

    /**
     * @return The field element SQL.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     */
    @SuppressWarnings("unused")
    public String getFieldElementFullSQL() throws XFTInitException, ElementNotFoundException {
        if (fieldElementFullSQL == null) {
            String rootElement = XftStringUtils.GetRootElementName(fieldElementXMLPath);
            GenericWrapperElement root = GenericWrapperElement.GetElement(rootElement);

            fieldElementFullSQL = ViewManager.GetViewColumnName(root, fieldElementXMLPath, ViewManager.DEFAULT_LEVEL, false, true);
        }
        return fieldElementFullSQL;
    }

    /**
     * @return The maps-to setting.
     */
    public String getMapsTo() {
        return mapsTo;
    }

    /**
     * @return The root element name.
     */
    public String getRootElement() {
        return rootElement;
    }

    /**
     * @param string The field element XML path to set.
     */
    public void setFieldElementXMLPath(String string) {
        fieldElementXMLPath = string;
    }

    /**
     * @param string The maps-to setting to set.
     */
    public void setMapsTo(String string) {
        mapsTo = string;
    }

    /**
     * @param string The root element name to set.
     */
    public void setRootElement(String string) {
        rootElement = string;
    }

}

