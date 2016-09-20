/*
 * core: org.nrg.xft.utils.XMLValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.utils;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xerces.parsers.DOMParser;
import org.nrg.xft.XFT;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public class XMLValidator {

    public void validateSchema(String XmlDocumentUrl) throws Exception {
        DOMParser domParser = new DOMParser();
        domParser
                .setFeature("http://xml.org/sax/features/validation", true);
        domParser.setFeature(
                "http://apache.org/xml/features/validation/schema", true);
        domParser.setFeature(
                        "http://apache.org/xml/features/validation/schema-full-checking",
                        true);
        domParser.setProperty(
                            "http://apache.org/xml/properties/schema/external-schemaLocation",
                            XFT.GetAllSchemaLocations(null));
        Validator handler = new Validator();
        domParser.setErrorHandler(handler);
        domParser.parse(XmlDocumentUrl);
        if (handler.validationError == true) {
            throw handler.saxParseException;
        } else{
            if (XFT.VERBOSE)
                System.out.println("XERCES - XML Document is valid");
        }

    }

    public ValidationHandler validateReader(Reader stream) throws Exception {
    	SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setValidating(true);
        spf.setFeature("http://apache.org/xml/features/validation/schema", true);

        //get a new instance of parser
        SAXParser sp = spf.newSAXParser();
        sp.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
        		XFT.GetAllSchemaLocations(null));

        ValidationHandler validator= new ValidationHandler();
        //parse the file and also register this class for call backs
        sp.parse(new org.xml.sax.InputSource(stream),validator);

        if (validator.assertValid()){
        	if(XFT.VERBOSE)System.out.println("done.");
        }else{
        	if(XFT.VERBOSE)System.out.println("FAILED\n");
            System.out.println("Parsing failed due to the following exception(s).");
            for (int i=0;i<validator.getErrors().size();i++){
                SAXParseException e = (SAXParseException)validator.getErrors().get(i);
                System.out.println(e.getMessage());
            }
        }
        
        return validator;

    }

    public ValidationHandler validateInputStream(InputStream stream) throws Exception {
    	SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setValidating(true);
        spf.setFeature("http://apache.org/xml/features/validation/schema", true);

        //get a new instance of parser
        SAXParser sp = spf.newSAXParser();
        sp.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
        		XFT.GetAllSchemaLocations(null));

        ValidationHandler validator= new ValidationHandler();
        //parse the file and also register this class for call backs
        sp.parse(stream,validator);

        if (validator.assertValid()){
        	if(XFT.VERBOSE)System.out.println("done.");
        }else{
        	if(XFT.VERBOSE)System.out.println("FAILED\n");
            System.out.println("Parsing failed due to the following exception(s).");
            for (int i=0;i<validator.getErrors().size();i++){
                SAXParseException e = (SAXParseException)validator.getErrors().get(i);
                System.out.println(e.getMessage());
            }
        }
        
        return validator;

    }

    public void validateString(String stream) throws Exception {
        StringReader sr = new StringReader(stream);
        InputSource is = new InputSource(sr);
        
        DOMParser domParser = new DOMParser();
                domParser
                        .setFeature("http://xml.org/sax/features/validation", true);
                domParser.setFeature(
                        "http://apache.org/xml/features/validation/schema", true);
                domParser.setFeature(
                                "http://apache.org/xml/features/validation/schema-full-checking",
                                true);
                domParser.setProperty(
                                    "http://apache.org/xml/properties/schema/external-schemaLocation",
                                    XFT.GetAllSchemaLocations(null));
                Validator handler = new Validator();
                domParser.setErrorHandler(handler);
                domParser.parse(is);
                if (handler.validationError == true) {
                    throw handler.saxParseException;
                } else{
                    if (XFT.VERBOSE)
                        System.out.println("XERCES - XML Document is valid");
                }

    }
    


    public static class ValidationHandler extends DefaultHandler{
        private ArrayList<SAXParseException> errors = new ArrayList<SAXParseException>();
        private boolean isValid = true;
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
         */
        @Override
        public void error(SAXParseException e) throws SAXException {
            errors.add(e);
            isValid = false;
        }

        public boolean assertValid(){
            return isValid;
        }

        public ArrayList<SAXParseException> getErrors(){
            return errors;
        }

        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
         */
        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            errors.add(e);
            isValid = false;
        }


    }

    public static void main(String args[]) {
        XMLValidator validator = new XMLValidator();
        String xmlUrl = null, schemaUrl = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-xml")) {
                if (i + 1 < args.length)
                    xmlUrl = args[i + 1];
            } else if (args[i].equalsIgnoreCase("-schema")) {
                if (i + 1 < args.length) {
                    schemaUrl = args[i + 1];
                } else {
                    System.out
                            .println("Usage: XMLValidator -xml <uri to xml file> [-schema <uri to schema>])");
                }
            }
        }
        if (xmlUrl == null) {
            System.out
                    .println("Usage: XMLValidator -xml <uri to xml file> -schema <uri to schema>)");
        } else
            try {
                validator.validateSchema(xmlUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}

class Validator extends DefaultHandler {
    public boolean validationError = false;

    public SAXParseException saxParseException = null;

    public void error(SAXParseException exception) throws SAXException {
        validationError = true;
        saxParseException = exception;
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        validationError = true;
        saxParseException = exception;
    }

    public void warning(SAXParseException exception) throws SAXException {
    }
}

