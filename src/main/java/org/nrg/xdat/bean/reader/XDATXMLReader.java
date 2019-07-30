/*
 * core: org.nrg.xdat.bean.reader.XDATXMLReader
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.bean.reader;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.ClassMappingFactory;
import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.base.BaseElement.UnknownFieldException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class XDATXMLReader extends DefaultHandler {
    private final SerializerService   _serializer;
    private final Map<String, String> prefixToURIMapping = new HashMap<>();
    private       BaseElement         root               = null;
    private       SAXReaderObject     current            = null;
    private       String              tempValue          = null;

    public XDATXMLReader() {
        _serializer = XDAT.getContextService().getBean(SerializerService.class);
    }

    public BaseElement getItem()
    {
        return root;
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        this.prefixToURIMapping.put(prefix,uri);
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (length > 0) {
                String temp = (new String(ch, start, length));
                if (temp.length()!=0 && isValidText(temp)){
                    if (tempValue != null){
                        if (current.insertNewLine())
                        {
                            tempValue +="\n" + temp;
                        }else{
                            tempValue +=temp;
                        }
                    }else{
                        tempValue=temp;
                    }
                }
            }
    }
    
    public BaseElement getBaseElement(String uri, String localName) throws SAXException{
        return getBaseElement(uri + ":" + localName);
    }

    public BaseElement getBaseElement(String name) throws SAXException{
        String className=null;;
		try {
			className = (String)ClassMappingFactory.getInstance().getElements().get(name);
		} catch (Exception e1) {
            throw new SAXException("Unknown class: " + className,e1);
		}
		
        if (className ==null)
        {
            throw new SAXException("Unknown type= " + name);
        }else{
            try {
                Class c = Class.forName(className);
                
                return (BaseElement) c.newInstance();
            } catch (ClassNotFoundException e) {
                throw new SAXException("Unknown class: " + className,e);
            } catch (InstantiationException e) {
                throw new SAXException("Unable to instantiate class: " + className,e);
            } catch (IllegalAccessException e) {
                throw new SAXException("Illegal access of class: " + className,e);
            }
            
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,Attributes attributes) throws SAXException {
        tempValue = null;
        if (root ==null)
        {
            BaseElement item = getBaseElement(uri,localName);                    
            if (attributes != null)
            {
                for (int i=0;i<attributes.getLength();i++)
                {
                    String local = attributes.getLocalName(i);
                    String value= attributes.getValue(i);

                    if (! value.equalsIgnoreCase(""))
                    {
                        try {
                            item.setDataField(local,value);
                        } catch (BaseElement.UnknownFieldException e1) {
                            log.error("", e1);
                        } catch (IllegalArgumentException e1) {
                            throw new SAXException("Invalid value for attribute '" + local +"'");
                        }
                    }
                }
            }
            
            current = new SAXReaderObject(item,null);
            root = item;
        }else{
            current.addHeader(localName);
            String current_header = current.getHeader();
            BaseElement currentItem = current.getItem();
            String TYPE = null;
            try {
                TYPE = currentItem.getFieldType(current_header);
            } catch (UnknownFieldException e) {
                
            }
            if (TYPE!=null && (TYPE.equals(BaseElement.field_inline_repeater) || TYPE.equals(BaseElement.field_multi_reference) || TYPE.equals(BaseElement.field_single_reference) || TYPE.equals(BaseElement.field_NO_CHILD)))
            {
                String foreignElement = null;
                if (attributes != null)
                {
                    for (int i=0;i<attributes.getLength();i++)
                    {
                        if (attributes.getURI(i).equalsIgnoreCase("http://www.w3.org/2001/XMLSchema-instance") && attributes.getLocalName(i).equalsIgnoreCase("type"))
                        {
                            foreignElement=attributes.getValue(i);
                            int index = foreignElement.indexOf(":");
                            if (index !=-1)
                            {
                                foreignElement = this.prefixToURIMapping.get(foreignElement.substring(0,index)) + foreignElement.substring(index);
                            }
                            break;
                        }
                    }
                }
                try {
                    if (foreignElement==null)
                    {
                        foreignElement= currentItem.getReferenceFieldName(current_header);
                    }
                    
                    BaseElement item = getBaseElement(foreignElement);  
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
                                        item.setDataField(local,value);
                                    } catch (BaseElement.UnknownFieldException e1) {
                                        log.error("", e1);
                                    } catch (IllegalArgumentException e1) {
                                        throw new SAXException("Invalid value for attribute '" + local +"'");
                                    }
                                }
                            }
                        }
                    }
                    try {
                        current.getItem().setReferenceField(current_header,item);
                        current = new SAXReaderObject(item,current,TYPE);
                        if (TYPE.equals(BaseElement.field_inline_repeater) || TYPE.equals(BaseElement.field_NO_CHILD))
                        {
                            current.setIsInlineRepeater(true);
                            boolean match = false;
                            
                            try {
                                if (item.getFieldType(localName)!=null)
                                {
                                    current.addHeader(localName);
                                    match = true;
                                }
                            } catch (Throwable e) {
                            }
                            
                            if (!match)
                            {
                                if (item.getFieldType(item.getSchemaElementName())!=null){
                                    current.addHeader(item.getSchemaElementName());
                                    match = true;
                                }
                            }
                            
                            if (!match){
                                throw new SAXException("Invalid XML '" + item.getSchemaElementName() + ":" + current_header + "'");
                            }
                        }
                    } catch (BaseElement.UnknownFieldException e2) {
                        throw new SAXException("Invalid XML '" + item.getSchemaElementName() + ":" + current_header + "'");
                    } catch (Exception e2) {
                        throw new SAXException(e2.getMessage());
                    }
                } catch (UnknownFieldException e) {
                    log.error("", e);
                    throw new SAXException("INVALID XML STRUCTURE:");
                }
            }else{
                current.setFIELD_TYPE(TYPE);
                if (attributes != null)
                {
                    for (int i=0;i<attributes.getLength();i++)
                    {
                        String local = attributes.getLocalName(i);
                        String value= attributes.getValue(i);

                        if (! value.equalsIgnoreCase(""))
                        {
                            try {
                                currentItem.setDataField(current_header + "/" + local,value);
                            } catch (BaseElement.UnknownFieldException e1) {
                                throw new SAXException("Unknown field '" + current_header + "/" + local +"'");
                            } catch (IllegalArgumentException e1) {
                                throw new SAXException("Invalid value for attribute '" + local +"'");
                            }
                        }
                    }
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
            s = RemoveChar(s.trim(),'\n');
            s = RemoveChar(s,'\t');
            
            if (s==null || s.equals(""))
            {
                return false;
            }
        }
        
        return true;
    }
    
    public static String RemoveChar(String _base, char _old)
    {
        while (_base.indexOf(_old) !=-1)
        {
            int index =_base.indexOf(_old);
            if (index==0)
            {
                _base = _base.substring(1);
            }else if (index== (_base.length()-1)) {
                _base = _base.substring(0,index);
            }else{
                String pre = _base.substring(0,index);
                _base = pre + _base.substring(index+1);
            }
        }
        
        return _base;
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        final String currentHeader = current.getHeader();
        if (tempValue != null && !tempValue.equals("") && isValidText(tempValue)) {
            final BaseElement currentItem = current.getItem();
            try {
                currentItem.setDataField(currentHeader, tempValue);
            } catch (BaseElement.UnknownFieldException e) {
                throw new SAXException("Invalid field '" + currentHeader + "'");
            } catch (IllegalArgumentException e) {
                throw new SAXException("Invalid value for field '" + currentHeader + "'");
            } catch (RuntimeException e) {
                throw new SAXException("Unknown Exception <" + currentHeader + ">" + tempValue);
            } finally {
                tempValue = null;
            }
        }

        if (StringUtils.isBlank(current.getHeader())) {
            while ((!current.isRoot()) && StringUtils.isBlank(current.getHeader())) {
                current = current.getParent();
            }
            current.removeHeader();
        } else {
            current.removeHeader();
            if (current.getIsInlineRepeater() && StringUtils.isBlank(current.getHeader())) {
                while ((!current.isRoot()) && StringUtils.isBlank(current.getHeader())) {
                    current = current.getParent();
                }
                current.removeHeader();
            }
        }
    }

    @Override
    public void startDocument() {
    }

    @Override
    public void endDocument() {
    }
    
    public static class SAXReaderObject{
        BaseElement item = null;
        StringBuilder header = new StringBuilder();
        SAXReaderObject parent = null;
        boolean root = false;
        boolean isInlineRepeater = false;
        String FIELD_TYPE=null;
        
        
        public SAXReaderObject(BaseElement i, SAXReaderObject p,String type)
        {
            item = i;
            parent=p;
            FIELD_TYPE=type;
        }
        
        public SAXReaderObject(BaseElement i,String type)
        {
            item = i;
            parent=null;
            root = true;
            FIELD_TYPE=type;
        }
        
        public String getHeader(){
            return header.toString();
        }
        
        public boolean isRoot(){return root;}
        
        public void addHeader(final String s){
            if (StringUtils.isBlank(header)) {
                header.append(s);
            }else{
                header.append("/").append(s);
            }
        }
        
        public void removeHeader()
        {
            if(header.toString().contains("/")){
                header = new StringBuilder(StringUtils.substringBeforeLast(header.toString(), "/"));
            }else{
                header = new StringBuilder();
            }
        }
        
        public BaseElement getItem(){
            return item;
        }
        
        public SAXReaderObject getParent(){
            return parent;
        }
        
        public boolean getIsInlineRepeater()
        {
            return this.isInlineRepeater;
        }
        
        public void setIsInlineRepeater(boolean b)
        {
            this.isInlineRepeater=b;
        }
        
        public boolean insertNewLine(){
            try {
                return FIELD_TYPE != null && FIELD_TYPE.equals(BaseElement.field_LONG_DATA);
            } catch (RuntimeException e) {
                return false;
            }
        }

        /**
         * @return the fIELD_TYPE
         */
        public String getFIELD_TYPE() {
            return FIELD_TYPE;
        }

        /**
         * @param field_type the fIELD_TYPE to set
         */
        public void setFIELD_TYPE(String field_type) {
            FIELD_TYPE = field_type;
        }
    }
    
    /**
     * Convert null unicode characters into spaces. The given InputStream is iterated and 
     * mark set to the beginning afterwards.
     * @param i
     * @return InputStream with null characters removed
     * @throws IOException
     */
    public static InputStream removeNullUnicodeChars (InputStream i) throws IOException {
    	byte [] bs = new byte[i.available()];
    	i.read(bs);
    	for (int j = 0; j < bs.length ; j++) {
    		if (bs[j] == Byte.decode("0x00")) {
    			bs[j] =' ';
    		}
    	}
    	return new ByteArrayInputStream(bs);
    }

    public BaseElement parse(final File data) throws IOException, SAXException {
        try (final FileInputStream inputStream = new FileInputStream(data)) {
            //parse the file and also register this class for call backs
            _serializer.parse(XDATXMLReader.removeNullUnicodeChars(inputStream), this);
        } catch (ParserConfigurationException e) {
            log.error("An error occurred creating the SAX parser", e);
        }
        return getItem();
    }

    public BaseElement parse(final Reader data) throws IOException, SAXException {
        try {
            //parse the file and also register this class for call backs
            _serializer.parse(new InputSource(data), this);
        } catch (ParserConfigurationException e) {
            log.error("An error occurred creating the SAX parser", e);
        }
        return getItem();
    }

    public BaseElement parse(final InputSource data) throws IOException, SAXException {
        try {
            //parse the file and also register this class for call backs
            _serializer.parse(data, this);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        return getItem();
    }

    public BaseElement parse(final InputStream data) throws IOException, SAXException {
        try {
            //parse the file and also register this class for call backs
            _serializer.parse(data, this);
        } catch (ParserConfigurationException e) {
            log.error("An error occurred creating the SAX parser", e);
        }
        return getItem();
    }
}
