/*
 * core: org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema.Wrappers.XMLWrapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.security.UserI;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author timo
 *
 */
@Slf4j
public class SAXReader extends DefaultHandler2 {
    private final SerializerService   _serializer;
    private final UserI               user;
    private final Map<String, String> uriToPrefixMapping = new HashMap<>();
    private       XFTItem             root      = null;
    private       XFTItem             template  =null;
    private       SAXReaderObject     current   = null;
    private       String              tempValue = null;
    boolean stopRecording = false;
    
    String stopAtPath= null;

    private ArrayList errors = new ArrayList();
    private boolean isValid = true;

    /**
     * 
     */
    public SAXReader(final UserI u) {
        user=u;
        _serializer = XDAT.getSerializerService();
    }
    
    public XFTItem getItem()
    {
        return root;
    }
    
    public XFTItem getTemplate() {
		return template;
	}

	public void setTemplate(XFTItem template) {
		this.template = template;
	}

	public XFTItem parse(final File data) throws IOException, SAXException{
        try (final FileReader reader = new FileReader(data)) {
            _serializer.parse(new InputSource(reader), this, "http://xml.org/sax/properties/lexical-handler", this);
        } catch (ParserConfigurationException e) {
            log.error("An error occurred creating the SAX parser", e);
        }
        return getItem();
    }

    /**
     * Stops loading document after specified tag is reached.  Allows for the partial loading of an item.
     * @param data
     * @param stopAtXMLPath
     * @return Returns the XFTItem
     * @throws IOException
     * @throws SAXException
     */
    public XFTItem parse(final File data, final String stopAtXMLPath) throws IOException, SAXException{
        try (final FileReader reader = new FileReader(data)) {
            this.stopAtPath= stopAtXMLPath;
            _serializer.parse(new InputSource(reader), this, "http://xml.org/sax/properties/lexical-handler", this);
        } catch (ParserConfigurationException e) {
            log.error("An error occurred creating the SAX parser", e);
        }
        return getItem();
    }

    public XFTItem parse(final Reader reader) throws IOException, SAXException{
        try {
            _serializer.parse(new InputSource(reader), this, "http://xml.org/sax/properties/lexical-handler", this);
        } catch (ParserConfigurationException e) {
            log.error("An error occurred creating the SAX parser", e);
        }
        return getItem();
    }

    public XFTItem parse(final InputSource data) throws IOException, SAXException{
        try {
            _serializer.parse(data, this, "http://xml.org/sax/properties/lexical-handler", this);
        } catch (ParserConfigurationException e) {
            log.error("An error occurred creating the SAX parser", e);
        }
        return getItem();
    }

    public XFTItem parse(final InputStream data) throws IOException, SAXException{
        try {
            _serializer.parse(data, this, "http://xml.org/sax/properties/lexical-handler", this);
        } catch (ParserConfigurationException e) {
            log.error("An error occurred creating the SAX parser", e);
        }
        return getItem();
    }

    public XFTItem parse(final String filePath) throws IOException, SAXException{
        try {
            _serializer.parse(filePath, this, "http://xml.org/sax/properties/lexical-handler", this);
        } catch (ParserConfigurationException e) {
            log.error("An error occurred creating the SAX parser", e);
        }
        return getItem();
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        this.uriToPrefixMapping.put(uri,prefix);
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (!stopRecording)
        {
            if (length > 0) {
                String temp = (new String(ch, start, length));
                if (temp.length()!=0 && isValidText(temp)){
                    if (tempValue != null){
                        tempValue +=temp;
                    }else{
                        tempValue=temp;
                    }
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName,Attributes attributes) throws SAXException {
        if (!stopRecording)
        {
            tempValue = null;
            if (root ==null)
            {
                try {
                    if(template==null){
                        GenericWrapperElement element = GenericWrapperElement.GetElement(localName,uri);
                    	root=XFTItem.NewItem(element,user);
                    }else{
                    	root=template;
                    }
                    root.setUser(user);
                    if (attributes != null)
                    {
                        for (int i=0;i<attributes.getLength();i++)
                        {
                            String local = attributes.getLocalName(i);
                            String value= attributes.getValue(i);

                            if (! value.equalsIgnoreCase(""))
                            {
                                try {
                                    root.setProperty(local,value,false);
                                } catch (FieldNotFoundException e1) {
                                } catch (InvalidValueException e1) {
                                    throw new SAXException("Invalid value for attribute '" + local +"'");
                                }
                            }
                        }
                    }
                    
                    current = new SAXReaderObject(root);
                } catch (XFTInitException e) {
                } catch (ElementNotFoundException e) {
                    throw new SAXException("Invalid Element '" + uri + ":" + localName + "'");
                }
            }else{
                try {
                    current.addHeader(localName);
                    String current_header = current.getHeader();
                    XFTItem currentItem = current.getItem();
                    GenericWrapperElement e = currentItem.getGenericSchemaElement();
                    GenericWrapperField f=null;
                    try {
                        f = GenericWrapperElement.GetFieldForXMLPath(e.getXSIType() + XFT.PATH_SEPARATOR + current_header);
                    } catch (FieldNotFoundException e3) {
                        log.error("", e3);
//                      NOT A REFERENCE
                        if (attributes != null)
                        {
                            for (int i=0;i<attributes.getLength();i++)
                            {
                                String local = attributes.getLocalName(i);
                                String value= attributes.getValue(i);

                                if (! value.equalsIgnoreCase(""))
                                {
                                    try {
                                        currentItem.setProperty(current_header + XFT.PATH_SEPARATOR + local, value, false);
                                    } catch (FieldNotFoundException e1) {
                                        throw new SAXException("Invalid attribute '" + local +"' of '" + currentItem.getXSIType() + "/" + current_header + "'");
                                    } catch (InvalidValueException e1) {
                                        throw new SAXException("Invalid value for attribute '" + local +"' of '" + currentItem.getXSIType() + "/" + current_header + "'");
                                    }
                                }
                            }
                        }
                    }
                    if (f==null)
                    {
                        // NOT A REFERENCE
                        if (attributes != null)
                        {
                            for (int i=0;i<attributes.getLength();i++)
                            {
                                String local = attributes.getLocalName(i);
                                String value= attributes.getValue(i);

                                if (! value.equalsIgnoreCase(""))
                                {
                                    try {
                                        currentItem.setProperty(current_header + XFT.PATH_SEPARATOR + local, value, false);
                                    } catch (FieldNotFoundException e1) {
                                        throw new SAXException("Invalid attribute '" + local +"' of '" + currentItem.getXSIType() + "/" + current_header + "'");
                                    } catch (InvalidValueException e1) {
                                        throw new SAXException("Invalid value for attribute '" + local +"' of '" + currentItem.getXSIType() + "/" + current_header + "'");
                                    }
                                }
                            }
                        }
                    }else if (f.isReference())
                    {
                        try {
                            String foreignElement = null;
                            String localURI = uri;
                            
                            if (attributes != null)
                            {
                                for (int i=0;i<attributes.getLength();i++)
                                {
                                    if (attributes.getURI(i).equalsIgnoreCase("http://www.w3.org/2001/XMLSchema-instance") && attributes.getLocalName(i).equalsIgnoreCase("type"))
                                    {
                                        foreignElement=attributes.getValue(i);
                                        if (foreignElement.contains(":")){
                                            String prefix = foreignElement.substring(0,foreignElement.indexOf(":"));
                                            foreignElement = foreignElement.substring(foreignElement.indexOf(":")+1);
                                            
                                            if (uriToPrefixMapping.containsValue(prefix)){
                                                Iterator iter = uriToPrefixMapping.entrySet().iterator();
                                                while(iter.hasNext()){
                                                    Map.Entry entry=(Map.Entry)iter.next();
                                                    if (entry.getValue().equals(prefix))
                                                    {
                                                        localURI=(String)entry.getKey();
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                            
                            if (foreignElement==null)
                            {
                                foreignElement= f.getReferenceElementName().getFullForeignType();
                            }
                            
                            GenericWrapperElement element = GenericWrapperElement.GetElement(foreignElement,localURI);
                            if (element==null){
                                throw new SAXException("Unknown data-type " + localURI + ":" + foreignElement);
                            }
                            XFTItem item = XFTItem.NewItem(element,user);
                            item.setUser(user);
                            if (attributes != null)
                            {
                                for (int i=0;i<attributes.getLength();i++)
                                {
                                    if (!(attributes.getURI(i).equalsIgnoreCase("http://www.w3.org/2001/XMLSchema-instance") && attributes.getLocalName(i).equalsIgnoreCase("type")))
                                    {
                                        String local = attributes.getLocalName(i);
                                        String value= attributes.getValue(i);

                                        if (! value.equalsIgnoreCase(""))
                                        {
                                            try {
                                                item.setProperty(local,value,false);
                                            } catch (FieldNotFoundException e1) {
                                                throw new SAXException("Invalid attribute '" + local +"' of '" + currentItem.getXSIType() + "/" + current_header + "'");
                                            } catch (InvalidValueException e1) {
                                                throw new SAXException("Invalid value for attribute '" + local +"' of '" + currentItem.getXSIType() + "/" + current_header + "'");
                                            }
                                        }
                                    }
                                }
                            }
                            try {
                                current.getItem().setProperty(current_header,item,false);
                                current = new SAXReaderObject(item,current);
                                if (f.isInLineRepeaterElement() || element.isANoChildElement())
                                {
                                    current.setIsInlineRepeater(true);
                                    if (item.hasLocalField(localName))
                                    {
                                        current.addHeader(localName);
                                    }else if (item.hasLocalField(element.getName())){
                                        current.addHeader(element.getName());
                                    }else{
                                        throw new SAXException("Invalid XML '" + item.getXSIType() + ":" + current_header + "'");
                                    }
                                }
                            } catch (FieldNotFoundException e2) {
                                throw new SAXException("Invalid XML '" + item.getXSIType() + ":" + current_header + "'");
                            } catch (Exception e2) {
                                throw new SAXException(e2.getMessage());
                            }
                        } catch (XFTInitException ex) {
                        } catch (ElementNotFoundException ex) {
                            throw new SAXException("Invalid Element '" + uri + ":" + localName + "'");
                        }
                    }else{
                        current.setF(f);
                        if (attributes != null)
                        {
                            for (int i=0;i<attributes.getLength();i++)
                            {
                                String local = attributes.getLocalName(i);
                                String value= attributes.getValue(i);

                                if (! value.equalsIgnoreCase(""))
                                {
                                    try {
                                        currentItem.setProperty(current_header + XFT.PATH_SEPARATOR + local, value, false);
                                    } catch (FieldNotFoundException e1) {
                                        throw new SAXException("Invalid attribute '" + local +"' of '" + currentItem.getXSIType() + "/" + current_header + "'");
                                    } catch (InvalidValueException e1) {
                                        throw new SAXException("Invalid value for attribute '" + local +"' of '" + currentItem.getXSIType() + "/" + current_header + "'");
                                    }
                                }
                            }
                        }
                    }
                } catch (ElementNotFoundException e) {
                } catch (XFTInitException e1) {
                }
            }
        }
    }
    
    private boolean isValidText(String s)
    {
        if (s ==null)
        {
           return false;
        }else{
            s = StringUtils.remove(s.trim(), '\n');
            s = StringUtils.remove(s, '\t');
            
            if (StringUtils.isBlank(s))
            {
                return false;
            }
        }
        
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!stopRecording)
        {
            String current_header = current.getHeader();
            if (tempValue!=null && !tempValue.equals("") && isValidText(tempValue))
            {
                XFTItem currentItem = current.getItem();
                try {
                    if (current_header.equals("")){
                        if (currentItem.hasLocalField(localName))
                        {
                            current_header=localName;
                        }else if (currentItem.hasLocalField(currentItem.getGenericSchemaElement().getName())){
                            current_header=currentItem.getGenericSchemaElement().getName();
                        }
                    }
                    currentItem.setProperty(current_header,tempValue,false);
                } catch (XFTInitException e) {
                } catch (ElementNotFoundException e) {
                } catch (FieldNotFoundException e1) {
                    throw new SAXException("Invalid field '" + current_header +"'");
                } catch (InvalidValueException e1) {
                    throw new SAXException("Invalid value for field '" + current_header +"'");
                } catch (RuntimeException e){
                    log.error("Unknown exception processing <{}>: {}", current_header, tempValue, e);
                    throw new SAXException("Unknown Exception <" + current_header +">" + tempValue);
                }finally{
                    tempValue=null;
                }
            }
            
            if (StringUtils.isBlank(current.getHeader()))
            {
                while ((!current.isRoot()) && StringUtils.isBlank(current.getHeader()))
                {
                    current = current.getParent();
                }
                current.removeHeader();
            }else{
                current.removeHeader();
                if (current.getIsInlineRepeater() && StringUtils.isBlank(current.getHeader()))
                {
                    while ((!current.isRoot()) && StringUtils.isBlank(current.getHeader()))
                    {
                        current = current.getParent();
                    }
                    current.removeHeader();
                }
            }
            
            if (stopAtPath!=null){
                if (current_header.equals(stopAtPath)){
                    stopRecording=true;
                }
            }
        }
    }
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
    }
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
    }
    
    public String toString(){
        return current.toString();
    }
    
    public class SAXReaderObject{
        XFTItem item = null;
        String header = "";
        SAXReaderObject parent = null;
        boolean root = false;
        boolean isInlineRepeater = false;
        GenericWrapperField f = null;
        
        
        public SAXReaderObject(XFTItem i, SAXReaderObject p)
        {
            item = i;
            parent=p;
        }
        
        public SAXReaderObject(XFTItem i)
        {
            item = i;
            parent=null;
            root = true;
        }
        
        public String getHeader(){
            return header;
        }
        
        public boolean isRoot(){return root;}
        
        public void addHeader(String s){
            if (StringUtils.isBlank(header))
            {
                header += s;
            }else{
                header += org.nrg.xft.XFT.PATH_SEPARATOR + s;
            }
        }
        
        public void removeHeader()
        {
            if(header.indexOf(org.nrg.xft.XFT.PATH_SEPARATOR) != -1){
                header = header.substring(0,header.lastIndexOf(org.nrg.xft.XFT.PATH_SEPARATOR));
            }else{
                header ="";
            }
        }
        
        public XFTItem getItem(){
            return item;
        }
        
        public SAXReaderObject getParent(){
            return parent;
        }
        
        public String toString(){
            return this.getItem().getXSIType() + org.nrg.xft.XFT.PATH_SEPARATOR + header + " (" + item.getXSIType() + ")";
        }
        
        public boolean getIsInlineRepeater()
        {
            return this.isInlineRepeater;
        }
        
        public void setIsInlineRepeater(boolean b)
        {
            this.isInlineRepeater=b;
        }
        /**
         * @return Returns the f.
         */
        public GenericWrapperField getF() {
            return f;
        }
        /**
         * @param f The f to set.
         */
        public void setF(GenericWrapperField f) {
            this.f = f;
        }
    }
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

    /* (non-Javadoc)
     * @see org.xml.sax.ext.DefaultHandler2#comment(char[], int, int)
     */
    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
        if (!stopRecording)
        {
            if (length > 0) {
                String temp = (new String(ch, start, length));
                if (temp.length()!=0 && isValidText(temp)){
                    int index = temp.indexOf("hidden_fields[");
                    if (index>-1){ 
                        temp = temp.substring(index+14,temp.indexOf("]",index));
                        if (temp.length()!=0 && isValidText(temp)){
                            String[] array = temp.split(",");
                            for(int hiddenCounter=0;hiddenCounter<array.length;hiddenCounter++){
                                String[] token=array[hiddenCounter].split("=");
                                token[1]=token[1].substring(1,token[1].length()-1);
                                try {
                                	if(!token[1].equals("null"))
                                		this.current.getItem().setProperty(token[0],token[1]);
                                } catch (XFTInitException e) {
                                    log.error("", e);
                                } catch (ElementNotFoundException e) {
                                    log.error("", e);
                                } catch (FieldNotFoundException e) {
                                    log.error("", e);
                                } catch (InvalidValueException e) {
                                    log.error("", e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    
}
