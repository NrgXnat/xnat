/*
 * org.nrg.xnat.helpers.prearchive.XMLSetAttributeApp
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.prearchive;

import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XMLSetAttributeApp {

	/**
	 * @param args
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(new File("/tmp/test.xml"));
		Element e = doc.getDocumentElement();
		e.normalize();
		e.setAttribute("proj", "newProject");
		  // use specific Xerces class to write DOM-data to a file:
	    XMLSerializer serializer = new XMLSerializer();
	    serializer.setOutputCharStream(
	      new java.io.FileWriter("/tmp/test.xml"));
	    serializer.serialize(doc);
	}
}
