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
        final String schemaLocations = XFT.GetAllSchemaLocations(null);
        log.debug("Validating schema from URL {} with schema locations: {}", documentUrl, schemaLocations);
        final ValidationHandler handler = XDAT.getSerializerService().validateSchema(documentUrl, schemaLocations);
        if (!handler.assertValid()) {
            log.error("{} errors occurred while processing the XML document loaded from URI {}", handler.getErrors().size(), documentUrl);
            throw handler.getErrors().getFirst();
        }
    }

    @SuppressWarnings("unused")
    public ValidationHandler validateReader(final Reader reader) throws Exception {
        final String schemaLocations = XFT.GetAllSchemaLocations(null);
        log.debug("Validating reader with schema locations: {}", schemaLocations);
        return XDAT.getSerializerService().validateReader(reader, schemaLocations);
    }

    public ValidationHandler validateInputStream(final InputStream inputStream) throws Exception {
        final String schemaLocations = XFT.GetAllSchemaLocations(null);
        log.debug("Validating input stream with schema locations: {}", schemaLocations);
        return XDAT.getSerializerService().validateInputStream(inputStream, schemaLocations);
    }

    public void validateString(final String xml) throws Exception {
        final String schemaLocations = XFT.GetAllSchemaLocations(null);
        log.debug("Validating XML string with schema locations: {}", schemaLocations);
        final ValidationHandler handler = XDAT.getSerializerService().validateString(xml, schemaLocations);
        if (!handler.assertValid()) {
            log.error("{} errors occurred while processing the XML document:\n{}", handler.getErrors().size(), xml);
            throw handler.getErrors().getFirst();
        }
    }
}
