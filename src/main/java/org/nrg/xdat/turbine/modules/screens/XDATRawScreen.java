/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATRawScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.modules.screens.RawScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXWriter;
import org.xml.sax.SAXException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;
import java.io.IOException;

public abstract class XDATRawScreen extends RawScreen{
    protected void writeToXml(final ItemI item, final HttpServletResponse response) throws IOException, TransformerConfigurationException, FieldNotFoundException, SAXException {
        try (final ServletOutputStream out = response.getOutputStream()) {
            final SAXWriter writer = new SAXWriter(out, true);
            writer.setAllowSchemaLocation(true);
            writer.setLocation(TurbineUtils.GetFullServerPath() + "/" + "schemas/");
            writer.write(item.getItem());
        }
    }
}
