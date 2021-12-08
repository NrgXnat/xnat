/*
 * core: org.nrg.xdat.turbine.modules.screens.XMLScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.restlet.data.Status;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Tim
 */
@Slf4j
public class XMLScreen extends XDATRawScreen {
    /**
     * Set the content type to Xml. (see RawScreen)
     *
     * @param data Turbine information.
     *
     * @return content type.
     */
    public String getContentType(final RunData data) {
        return "text/xml";
    }

    /**
     * {@inheritDoc}
     */
    protected final void doOutput(final RunData data) throws Exception {
        try {
            final ItemI item = ObjectUtils.getIfNull(TurbineUtils.getDataItem(data), () -> getItemBySearch(data));
            if (item == null) {
                data.setMessage("No Item found for XML display.");
                data.setScreenTemplate("Index.vm");
                return;
            }
            if (!TurbineUtils.isAccessibleItem(XDAT.getUserDetails(), item)) {
                TurbineUtils.denyAccess(data);
                return;
            }
            if (item instanceof XFTItem && ((XFTItem) item).instanceOf("xdat:user")) {
                item.setProperty("primary_password", "");
                item.setProperty("salt", "");
            }
            final HttpServletResponse response = data.getResponse();
            response.setContentType("text/xml");
            writeToXml(item, response);
        } catch (IllegalAccessException e) {
            final HttpServletResponse response = data.getResponse();
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN.getCode());
        }
    }

    private static ItemI getItemBySearch(final RunData data) {
        try {
            return TurbineUtils.GetItemBySearch(data);
        } catch (Exception e) {
            log.warn("An error occurred trying to retrieve an item by search", e);
            return null;
        }
    }
}
