/*
 * core: org.nrg.xft.utils.XMLUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.utilities.PropertiesUtils;
import org.nrg.xdat.XDAT;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import java.io.*;

@Slf4j
public  class XMLUtils {	
	/**
	 * Saves an XNAT Document object to the specified file.
	 * @param document XML DOM Document
	 * @param outputName File path and name
	 * @throws Exception When an error occurs
	 */
	public static void DOMToFile(final Document document, final String outputName) throws Exception {
		final File outputFile = new File(outputName);
		if (!outputFile.exists()) {
			//noinspection ResultOfMethodCallIgnored
			outputFile.createNewFile();
		}
		try (final FileWriter writer = new FileWriter(outputFile)) {
			XDAT.getSerializerService().toXml(document, writer);
			log.info("Created file: {}", outputName);
		} catch (IOException e) {
			log.error("An error occurred writing to the output file {}", outputName, e);
		}
	}
	
	public static void PrettyPrintDOM(final File file) {
	    try {
            DOMToFile(GetDOM(file), file.getAbsolutePath());
        } catch (Exception e) {
            log.error("", e);
        }
	}
	
	@SuppressWarnings("unused")
	public static void PrettyPrintSAX(final File file) {
	    try {
            DOMToFile(GetDOM(file), file.getAbsolutePath());
        } catch (Exception e) {
            log.error("", e);
        }
	}

	/**
	 * Translates the DOM Document to a basic string.
	 * @param document XML DOM Document
	 */
	public static String DOMToString(final Document document) {
		return DOMToCustomString(document, OutputKeys.OMIT_XML_DECLARATION, "yes");
	}

	public static String DOMToCustomString(final Document document, final String... properties) {
		try {
			return XDAT.getSerializerService().toXml(document, PropertiesUtils.of(properties));
		} catch (TransformerException e) {
		  log.error("An error occurred trying to transform an XML document to a string", e);
		  return null;
		}
	}


	/**
	 * Translates the DOM Document to a basic string.
	 * @param document XML DOM Document
	 */
	@SuppressWarnings("unused")
	public static String DOMToBasicString(final Document document) {
		try {
			return XDAT.getSerializerService().toXml(document);
		}
		catch (TransformerException e) {
			log.error("An error occurred trying to transform an XML document to a string", e);
			return null;
		}
	}
	/**
	 * Translates the DOM Document to a basic string.
	 * @param document XML DOM Document
	 */
	public static String DOMToHTML(final Document document) {
		return DOMToCustomString(document, OutputKeys.OMIT_XML_DECLARATION, "yes", OutputKeys.METHOD, "html");
	}
	
	/**
	 * Translates the given DOM Document to a ByteArrayOutputStream
	 * @param document XML DOM Document
	 * @return Returns the ByteArrayOutputStream translation of the DOM element
	 */
	public static ByteArrayOutputStream DOMToBAOS(final Document document) {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			output.write(StringUtils.defaultIfBlank(DOMToString(document), "").getBytes());
		} catch (IOException e) {
			log.error("An error occurred trying to transform an XML document to a byte-array output stream", e);
		}
		return output;
	}
	
	
	/**
	 * Transforms a File into a XML DOM Document.
	 * 
	 * @param resource File to be translated.
	 * @return doc
	 */
	public static Document GetDOM(final Resource resource) {
		try {
			return XDAT.getSerializerService().parse(resource);
		} catch (Exception ex) {
			throw new RuntimeException("Unable to load DOM document from resource: " + resource, ex);
		}
	}
	
	/**
	 * Transforms a File into a XML DOM Document.
	 * 
	 * @param file File to be translated.
	 * @return doc
	 */
	public static Document GetDOM(final File file) {
		try (final InputStream input = new FileInputStream(file)){
			return XDAT.getSerializerService().parse(input);
		} catch (Exception e) {
			throw new RuntimeException("Unable to load DOM document from file: " + file, e);
		}
	}

	public static Document GetDOM(final InputStream input) {
		try {
			return XDAT.getSerializerService().parse(input);
		} catch (Exception e) {
			throw new RuntimeException("Unable to load DOM document", e);
		}
	}

	public static Document GetDOM(final String stream) {
		try {
			return XDAT.getSerializerService().parse(stream);
		} catch (Exception e) {
			throw new RuntimeException("Unable to load DOM document:", e);
		}
	}
		
	public static boolean HasAttribute(final Node node, final String name) {
		final NamedNodeMap nodeMap = node.getAttributes();
		return nodeMap != null && nodeMap.getNamedItem(name) != null;
	}
}

