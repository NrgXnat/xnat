/*
 * core: org.nrg.xdat.ajax.XMLSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.ajax;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXWriter;
import org.nrg.xft.security.UserI;
import org.springframework.http.MediaType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

@Slf4j
public class XMLSearch {
    public void execute(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final UserI user = XDAT.getUserDetails();
        if (user != null) {
            final InputSource input = getXmlPayload(request);
            if (input == null) {
                log.error("No XML specified for search");
                return;
            }

            response.setContentType("text/plain");
            response.setHeader("Cache-Control", "no-cache");

            final SAXReader reader = new SAXReader(user);
            try {
                final XFTItem        item          = reader.parse(input);
                final boolean        allowChildren = Boolean.parseBoolean(StringUtils.defaultIfBlank(request.getParameter("allowMultiples"), "true"));
                final ItemCollection items         = new XdatStoredSearch(item).getItemSearch(user).exec(allowChildren);
                final int            itemCount     = items.size();
                if (itemCount > 1 || itemCount == 0) {
                    response.getWriter().write("<matchingResults xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
                    for (final ItemI nextItem : items.getItems()) {
                        final XFTItem next = (XFTItem) nextItem;
                        Authorizer.getInstance().authorizeRead(next, user);
                        try (final ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                            response.getWriter().write("<matchingResult>");
                            try {
                                final SAXWriter writer = new SAXWriter(bytes, false);
                                writer.setWriteHiddenFields(true);
                                writer.write(next);
                            } catch (TransformerConfigurationException e) {
                                log.error("There was an error trying to configure the XML transformer", e);
                            } catch (TransformerFactoryConfigurationError e) {
                                log.error("There was an error trying to configure the XML transformer factory", e);
                            } catch (FieldNotFoundException e) {
                                log.error("Field not found {}: {}", e.FIELD, e.MESSAGE);
                            }
                            response.getWriter().write(bytes.toString());
                            response.getWriter().flush();
                            response.getWriter().write("</matchingResult>");
                        } catch (Exception ignored) {
                            // Swallow these errors cause why not?
                        }
                    }
                    response.getWriter().write("</matchingResults>");
                } else {
                    final XFTItem next = (XFTItem) items.first();
                    Authorizer.getInstance().authorizeRead(next, user);
                    try {
                        final SAXWriter writer = new SAXWriter(response.getOutputStream(), false);
                        writer.setWriteHiddenFields(true);
                        writer.write(next);
                    } catch (TransformerConfigurationException e) {
                        log.error("There was an error trying to configure the XML transformer", e);
                    } catch (TransformerFactoryConfigurationError e) {
                        log.error("There was an error trying to configure the XML transformer factory", e);
                    } catch (FieldNotFoundException e) {
                        log.error("Field not found {}: {}", e.FIELD, e.MESSAGE);
                    }
                }
            } catch (SAXException e) {
                log.error("Couldn't read the submitted input", e);
            } catch (XFTInitException e) {
                log.error("Error initializing XFT");
            } catch (ElementNotFoundException e) {
                log.error("Element not found: {}", e.ELEMENT);
            } catch (FieldNotFoundException e) {
                log.error("Field not found {}: {}", e.FIELD, e.MESSAGE);
            } catch (Exception e) {
                log.error("An unexpected error occurred", e);
            }
        }
    }

    @Nullable
    private InputSource getXmlPayload(final @Nonnull HttpServletRequest request) throws IOException {
        if (StringUtils.equalsAnyIgnoreCase(request.getMethod(), "POST", "PUT") && StringUtils.equalsAnyIgnoreCase(request.getContentType(), MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE)) {
            return new InputSource(request.getReader());
        }
        final String xml = request.getParameter("search");
        if (StringUtils.isNotBlank(xml)) {
            return new InputSource(new StringReader(xml));
        }
        return null;
    }
}
