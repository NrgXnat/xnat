/*
 * core: org.nrg.xdat.turbine.modules.screens.XMLScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.modules.screens.RawScreen;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Tim
 */
public class XMLScreen extends XDATRawScreen {
    /**
     * Set the content type to Xml. (see RawScreen)
     *
     * @param data Turbine information.
     * @return content type.
     */
    public String getContentType(final RunData data) {
        return "text/xml";
    }

    /**
     * {@inheritDoc}
     */
    protected final void doOutput(final RunData data) throws Exception {
        ItemI item = TurbineUtils.getDataItem(data);

        if (item == null) {
            item = TurbineUtils.GetItemBySearch(data);
        }

        if (item == null) {
            data.setMessage("No Item found for XML display.");
            data.setScreenTemplate("Index.vm");
        } else {
            final HttpServletResponse response = data.getResponse();
            response.setContentType("text/xml");
            writeToXml(item, response);
        }
    }
}

