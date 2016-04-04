/*
 * org.nrg.xft.utils.XMLUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */



package org.nrg.xft.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public  class XMLUtils
{	
	private static final Logger logger = LoggerFactory.getLogger(XMLUtils.class);
	
	/*********************************************************
	 * Saves a DOM Document to the specified file.
	 * @param doc XML DOM Document
	 * @param _outputName File path and name
	 * @throws Exception
	 */
	
	public static void DOMToFile(Document doc, String _outputName) throws Exception
	{
        File f = new File(_outputName);
        if (!f.exists())
        {
            f.createNewFile();
        }
		FileWriter writer = new FileWriter(f);
		try {
		  OutputFormat format = new OutputFormat(doc);
		  format.setIndenting(true);
		  format.setIndent(4);
		  format.setLineWidth(0);

		  XMLSerializer output = new XMLSerializer(writer, format);
		  output.asDOMSerializer();
		  output.serialize(doc);
		}
		catch (IOException e) {
		  System.err.println(e);
		}
		logger.info("Created File:" + _outputName);
		writer.close();
	}
	
	public static void PrettyPrintDOM(File f)
	{
	    try {
            Document d = GetDOM(f);
            DOMToFile(d,f.getAbsolutePath());
        } catch (Exception e) {
            logger.error("",e);
        }
	}
	
	public static void PrettyPrintSAX(File f)
	{
	    try {
            Document d = GetDOM(f);
            DOMToFile(d,f.getAbsolutePath());
        } catch (Exception e) {
            logger.error("",e);
        }
	}

	/*********************************************************
	 * Translates the DOM Document to a basic string.
	 * @param doc XML DOM Document
	 */
	public static String DOMToString(Document doc)
	{
		StringWriter writer = new StringWriter();
		try {
			 OutputFormat format = new OutputFormat(doc);
			  format.setLineSeparator("\r\n");
			  format.setIndenting(true);
			  format.setOmitComments(true);
			  format.setLineWidth(0);
			  format.setOmitDocumentType(true);
			  format.setOmitXMLDeclaration(true);
			  XMLSerializer output = new XMLSerializer(writer, format);
			 
			  output.serialize(doc);
		}
		catch (IOException e) {
		  System.err.println(e);
		}
		
		return writer.toString();
	}


	/*********************************************************
	 * Translates the DOM Document to a basic string.
	 * @param doc XML DOM Document
	 */
	public static String DOMToBasicString(Document doc)
	{
		StringWriter writer = new StringWriter();
		try {
			 OutputFormat format = new OutputFormat(doc);
			  format.setLineSeparator("\r\n");
			  format.setIndenting(true);
			  format.setIndent(4);
			  format.setLineWidth(0);
			  
			  XMLSerializer output = new XMLSerializer(writer, format);
			  output.serialize(doc);
		}
		catch (IOException e) {
		  System.err.println(e);
		}
		
		return writer.toString();
	}
	/*********************************************************
	 * Translates the DOM Document to a basic string.
	 * @param doc XML DOM Document
	 */
	public static String DOMToHTML(Document doc)
	{
		StringWriter writer = new StringWriter();
		try {
		  OutputFormat format = new OutputFormat(doc);
		  format.setLineSeparator("<BR>");
		  format.setIndenting(true);
		  format.setLineWidth(0);
		  format.setOmitComments(true);
		  format.setOmitDocumentType(true);
		  format.setOmitXMLDeclaration(true);
		  XMLSerializer output = new XMLSerializer(writer, format);
		  output.serialize(doc);
		}
		catch (IOException e) {
		  System.err.println(e);
		}
		
		String s = writer.toString();
		s = StringUtils.ReplaceStr(s,"<BR>","*BR*");
		s =  StringUtils.ReplaceStr(StringUtils.ReplaceStr(StringUtils.ReplaceStr(s,"</","&lt;/"),"<","&lt;"),">","&gt;");
		return StringUtils.ReplaceStr(s,"*BR*","<BR>");
	}
	
	/**
	 * Translates the given DOM Document to a ByteArrayOutputStream
	 * @param doc
	 * @return
	 */
	public static ByteArrayOutputStream DOMToBAOS(Document doc)
	{
		ByteArrayOutputStream writer = new ByteArrayOutputStream();
		try {
		  OutputFormat format = new OutputFormat(doc);
		  format.setLineSeparator("\r\n");
		  format.setLineWidth(0);
		  format.setIndenting(true);
		  format.setOmitComments(true);
		  format.setOmitDocumentType(true);
		  format.setOmitXMLDeclaration(true);
		  XMLSerializer output = new XMLSerializer(writer, format);
		  output.serialize(doc);
		}
		catch (IOException e) {
		  System.err.println(e);
		}
		
		return writer;
	}
	
	
	/**********************************************************
	 * Transforms a File into a XML DOM Document using DocumentBuilderFactory.
	 * 
	 * @param file File to be translated.
	 * @return doc
	 */
	public static Document GetDOM(Resource file)
	{
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file.getInputStream());
			return doc;
		}catch(Exception ex)
		{
		    RuntimeException e1 = new RuntimeException("Unable to load DOM document:" + file + "\n" + ex.getMessage());
			throw e1;
		}
	}
	
	
	/**********************************************************
	 * Transforms a File into a XML DOM Document using DocumentBuilderFactory.
	 * 
	 * @param file File to be translated.
	 * @return doc
	 */
	public static Document GetDOM(File file)
	{
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			return doc;
		}catch(Exception ex)
		{
		    RuntimeException e1 = new RuntimeException("Unable to load DOM document:" + file + "\n" + ex.getMessage());
			throw e1;
		}
	}
	
	public static Document GetDOM(InputStream is)
	{
	    try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);
			return doc;
		}catch(Exception ex)
		{
		    RuntimeException e1 = new RuntimeException("Unable to load DOM document:\n" + ex.getMessage());
			throw e1;
		}
	}
	
	public static Document GetDOM(String stream)
	{
	    try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
            StringReader sr = new StringReader(stream);
            InputSource is = new InputSource(sr);
			Document doc = builder.parse(is);
			return doc;
		}catch(Exception ex)
		{
		    RuntimeException e1 = new RuntimeException("Unable to load DOM document:\n" + ex.getMessage());
			throw e1;
		}
	}
		
	public static boolean HasAttribute(Node node, String name)
	{
		boolean found = false;
		NamedNodeMap nnm = node.getAttributes();
		if (nnm != null)
		{
			Node temp = nnm.getNamedItem(name);
			if (temp != null)
			{
				return true;
			}
		}
		
		return found;
	}
	
}

