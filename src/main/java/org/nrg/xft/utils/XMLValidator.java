/*
 * core: org.nrg.xft.utils.XMLValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.services.impl.ValidationHandler;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;

import java.io.InputStream;
import java.io.Reader;

@Slf4j
public class XMLValidator {
    public void validateSchema(final String documentUrl) throws Exception {
        final ValidationHandler handler = XDAT.getSerializerService().validateSchema(documentUrl, XFT.GetAllSchemaLocations(null));
        if (!handler.assertValid()) {
            log.error("{} errors occurred while processing the XML document loaded from URI {}", handler.getErrors().size(), documentUrl);
            throw handler.getErrors().get(0);
        }
    }

    @SuppressWarnings("unused")
    public ValidationHandler validateReader(final Reader reader) throws Exception {
        return XDAT.getSerializerService().validateReader(reader, XFT.GetAllSchemaLocations(null));
    }

    public ValidationHandler validateInputStream(final InputStream inputStream) throws Exception {
        return XDAT.getSerializerService().validateInputStream(inputStream, XFT.GetAllSchemaLocations(null));
    }

    public void validateString(final String xml) throws Exception {
        final ValidationHandler handler = XDAT.getSerializerService().validateString(xml, XFT.GetAllSchemaLocations(null));
        if (!handler.assertValid()) {
            log.error("{} errors occurred while processing the XML document:\n{}", handler.getErrors().size(), xml);
            throw handler.getErrors().get(0);
        }
    }
}
