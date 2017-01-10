/*
 * core: org.nrg.xdat.bean.reader.XDATXMLReader
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.bean.reader;

import org.apache.log4j.Logger;
import org.nrg.xdat.bean.ClassMappingFactory;
import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.base.BaseElement.UnknownFieldException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Hashtable;


public class XDATXMLReader extends DefaultHandler {
    static org.apache.log4j.Logger logger = Logger.getLogger(XDATXMLReader.class);
    private BaseElement root = null;
    private SAXReaderObject current = null;
    private String tempValue = null;
    Hashtable uriToPrefixMapping = new Hashtable();
    Hashtable prefixToURIMapping = new Hashtable();
    String xsi = null;
    
    public BaseElement getItem()
    {
        return root;
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        this.uriToPrefixMapping.put(uri,prefix);
        this.prefixToURIMapping.put(prefix,uri);
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
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
    
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
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
                            logger.error("",e1);
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
                                        logger.error("",e1);
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
                    logger.error("",e);
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
                                logger.error(e1);
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
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String current_header = current.getHeader();
            if (tempValue!=null && !tempValue.equals("") && isValidText(tempValue))
            {
                BaseElement currentItem = current.getItem();
                try {
                    currentItem.setDataField(current_header,tempValue);
                } catch (BaseElement.UnknownFieldException e1) {
                    throw new SAXException("Invalid field '" + current_header +"'");
                } catch (IllegalArgumentException e1) {
                    throw new SAXException("Invalid value for field '" + current_header +"'");
                } catch (RuntimeException e){
                    logger.error(e);
                    throw new SAXException("Unknown Exception <" + current_header +">" + tempValue);
                }finally{
                    tempValue=null;
                }
            }
            
            if (current.getHeader() == "")
            {
                while ((!current.isRoot()) && current.getHeader()=="")
                {
                    current = current.getParent();
                }
                current.removeHeader();
            }else{
                current.removeHeader();
                if (current.getIsInlineRepeater() && current.getHeader() == "")
                {
                    while ((!current.isRoot()) && current.getHeader()=="")
                    {
                        current = current.getParent();
                    }
                    current.removeHeader();
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
    
    public class SAXReaderObject{
        BaseElement item = null;
        String header = "";
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
            return header;
        }
        
        public boolean isRoot(){return root;}
        
        public void addHeader(String s){
            if (header =="")
            {
                header += s;
            }else{
                header += "/" + s;
            }
        }
        
        public void removeHeader()
        {
            if(header.indexOf("/" )!=-1){
                header = header.substring(0,header.lastIndexOf("/" ));
            }else{
                header ="";
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
                if (FIELD_TYPE==null)
                {
                    return false;
                }else{
                    if (FIELD_TYPE.equals(BaseElement.field_LONG_DATA))
                    {
                        return true;
                    }else{
                        return false;
                    }
                }
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
    	return new java.io.ByteArrayInputStream(bs);
    }
    
    public BaseElement parse(java.io.File data) throws IOException, SAXException{
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            spf.setNamespaceAware(true);
            java.io.FileInputStream fi = new java.io.FileInputStream(data);
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();
            //parse the file and also register this class for call backs
            sp.parse(XDATXMLReader.removeNullUnicodeChars(fi), this);
            // sp.parse(fi, this);
            fi.close();
            
        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        
        return getItem();
    }

    public BaseElement parse(Reader data) throws IOException, SAXException{
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            spf.setNamespaceAware(true);
        
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();
            //parse the file and also register this class for call backs
            sp.parse(new org.xml.sax.InputSource(data), this);
            
        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        
        return getItem();
    }


    public BaseElement parse(org.xml.sax.InputSource data) throws IOException, SAXException{
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            spf.setNamespaceAware(true);
        
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();
            //parse the file and also register this class for call backs
            sp.parse(data, this);
            
        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        
        return getItem();
    }

    public BaseElement parse(InputStream data) throws IOException, SAXException{
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            spf.setNamespaceAware(true);
        
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();
            //parse the file and also register this class for call backs
            sp.parse(data, this);
            
            
        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        
        return getItem();
    }
    
    
}
