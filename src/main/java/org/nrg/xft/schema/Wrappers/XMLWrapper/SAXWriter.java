//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on May 31, 2006
 *
 */
package org.nrg.xft.schema.Wrappers.XMLWrapper;

import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTPseudonymManager;
import org.nrg.xft.references.XFTReferenceI;
import org.nrg.xft.references.XFTRelationSpecification;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.XFTElementWrapper;
import org.nrg.xft.schema.design.XFTFieldWrapper;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author timo
 *
 */
public class SAXWriter {
	static org.apache.log4j.Logger logger = Logger.getLogger(SAXWriter.class);
    boolean allowSchemaLocation= true;
    boolean _allowDBAccess=true;
    boolean allowClear = true;
    String location = "";
    boolean limited=false;
    TransformerHandler transformer = null;
    Object oldTransformer=null;
    String relativizePath = null;

    boolean writeHiddenFields = false;

    private String appendRootPath = null;

    Hashtable<String,String> aliases = new Hashtable<String,String>();

    public SAXWriter(OutputStream out,boolean allowDBAccess) throws TransformerConfigurationException,IllegalArgumentException,TransformerFactoryConfigurationError
    {
        this(new StreamResult(out),allowDBAccess);
    }

    public SAXWriter(Writer out,boolean allowDBAccess) throws TransformerConfigurationException,IllegalArgumentException,TransformerFactoryConfigurationError
    {
        this(new StreamResult(out),allowDBAccess);
    }

    /**
     *
     */
    public SAXWriter(StreamResult sr,boolean allowDBAccess) throws TransformerConfigurationException,IllegalArgumentException,TransformerFactoryConfigurationError {
//      Set the TransformerFactory system property.
//      Note: For more flexibility, load properties from a properties file.
         String key = "javax.xml.transform.TransformerFactory";
         String value = "org.apache.xalan.processor.TransformerFactoryImpl";
         Properties props = System.getProperties();
         if (props.containsKey(key)){
             if (!props.get(key).equals("org.apache.xalan.processor.TransformerFactoryImpl")){
                 oldTransformer= props.get(key);
                 props.put(key, value);
             }else{
                 oldTransformer= props.get(key);
             }
         }else{
             props.put(key, value);
         }
         System.setProperties(props);

        SAXTransformerFactory stf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

        TransformerHandler t = stf.newTransformerHandler();
        Transformer serializer = t.getTransformer();

        serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
        serializer.setOutputProperty(OutputKeys.METHOD,"xml");
        serializer.setOutputProperty(OutputKeys.INDENT,"yes");
        serializer.setOutputProperty("{ http://xml.apache.org/xslt }indent-amount", "4");

        t.setResult(sr);
        transformer=t;
        this.setAllowDBAccess(allowDBAccess);

        if (oldTransformer!=null){
            props = System.getProperties();
            if (props.containsKey(key)){
                if (!props.get(key).equals(oldTransformer)){
                    props.put(key, oldTransformer);
                }
            }
            System.setProperties(props);
        }else{
            props = System.getProperties();
            props.remove(key);
            System.setProperties(props);
        }
    }

    public void setIndenting(boolean b){
        if (b)
        {
            transformer.getTransformer().setOutputProperty(OutputKeys.INDENT,"yes");
        }else{
            transformer.getTransformer().setOutputProperty(OutputKeys.INDENT,"no");
        }
    }

    public void setMethod(String s){
        transformer.getTransformer().setOutputProperty(OutputKeys.METHOD,s);
    }

    public void setEncoding(String s){
        transformer.getTransformer().setOutputProperty(OutputKeys.ENCODING,s);

    }


    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }


    private Hashtable<String,XFTElementWrapper> elements=new Hashtable<String,XFTElementWrapper>();
    public XMLWrapperElement getElement(String name){
        try {
            if (! elements.containsKey(name))
            {
                elements.put(name,XFTMetaManager.GetWrappedElementByName(XMLWrapperFactory.GetInstance(),name));
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }

        return (XMLWrapperElement)elements.get(name);
    }

    public void write(XFTItem item,boolean _allowClear) throws FieldNotFoundException, SAXException{
        this.allowClear=_allowClear;
        write(item);
    }

    public void write(XFTItem item) throws FieldNotFoundException, SAXException{
           transformer.startDocument();
            XMLWrapperElement element = getElement(item.getXSIType());

            AttributesImpl atts = new AttributesImpl();
            //GET ROOT ITEM ATTRIBUTES
            addAttributes(item,atts);

            //ADD XMLNS ROOT ATTRIBUTES
            addXMLNSAttributes(atts,element);

            //CREATE ELEMENT
            String rootName = getRootName(element,item);
            transformer.startElement("","",rootName,atts);

            writeHiddenFields(item);

            writeChildData(item,rootName);

            transformer.endElement("","",rootName);


            transformer.endDocument();
    }

    private void writeHiddenFields(XFTItem item) throws SAXException{
        if (this.writeHiddenFields){
            try {
                ArrayList<XFTFieldWrapper> addins = item.getGenericSchemaElement().getAddIns();
                if (addins.size()>0){
                    StringBuffer sb = new StringBuffer();
                    sb.append("hidden_fields[");
                    int index =0;
                    for(XFTFieldWrapper xwf:addins){
                        GenericWrapperField gwf = (GenericWrapperField)xwf;
                        if (gwf.isReference()){
                            if (!gwf.isMultiple())
                            {
                                XFTReferenceI ref = gwf.getXFTReference();
                                if (! ref.isManyToMany())
                                {
                                    XFTSuperiorReference sup = (XFTSuperiorReference)ref;
                                    Iterator specs = sup.getKeyRelations().iterator();
                                    while (specs.hasNext())
                                    {
                                        XFTRelationSpecification spec = (XFTRelationSpecification)specs.next();
                                        if (spec.getLocalCol()!=null && spec.getLocalCol()!="")
                                        {

                                            if (index++>0)sb.append(",");
                                            sb.append(spec.getLocalCol()).append("=\"");
                                            try {
                                                sb.append(item.getProperty(spec.getLocalCol()));
                                            } catch (FieldNotFoundException e) {
                                                logger.error("",e);
                                            }
                                            sb.append("\"");
                                        }
                                    }
                                }
                            }
                        }else{
                            if (index++>0)sb.append(",");
                            sb.append(gwf.getXMLPathString()).append("=\"");
                            try {
                                sb.append(item.getProperty(gwf.getXMLPathString()));
                            } catch (FieldNotFoundException e) {
                                logger.error("",e);
                            }
                            sb.append("\"");
                        }
                    }
                    sb.append("]");
                    insertComments(sb.toString());
                }
            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            }
        }
    }

    private void insertComments(String str) throws SAXException{
        transformer.comment(str.toCharArray(),0,str.length());
    }

    public String getRootName(XMLWrapperElement element, XFTItem item)
    {
		String rootName=null;
		try {
            String alias = item.getProperName();
            if ((alias != null) && (!alias.equalsIgnoreCase("")))
            {
                if(alias.indexOf(":")!=-1)
                {
                    if (allowSchemaLocation)
                    {
                        rootName=alias;
            			aliases.put(rootName,element.getName());
                    }else{
                        rootName = alias.substring(alias.indexOf(":")+1);
                        aliases.put(rootName,element.getName());
                    }

                }else{
                    if (allowSchemaLocation)
                    {
                        rootName = element.getSchemaTargetNamespacePrefix() +":" + alias;
                        aliases.put(rootName,element.getName());
                    }else{
                        rootName=alias;
            			aliases.put(rootName,element.getName());
                    }
                }
            }else
            {
                if (allowSchemaLocation)
                {
                    rootName = element.getFullXMLName();
            		aliases.put(rootName,element.getName());
                }else{
                    rootName = element.getName();
            		aliases.put(rootName,element.getName());
                }
            }
        } catch (ElementNotFoundException e) {
        }

		return rootName;
    }


    public void addAttributes(XFTItem child,AttributesImpl atts) throws SAXException,FieldNotFoundException
    {
        try {
            XMLWrapperElement element = getElement(child.getXSIType());

			addNoChildAttributes(child,element.getChildren(),atts);

            Object[] attributesArray = element.getAttributes().toArray();
            for (int i=0;i<attributesArray.length;i++){
                XMLWrapperField attField = (XMLWrapperField)attributesArray[i];
            	if (attField.isReference())
            	{
            			XFTItem ref = (XFTItem)child.getProperty(attField.getId());
            			if (ref != null)
            			{
            			    if((!limited) || (!ref.canBeRootWithBase()))
            			    {
            			        addAttributes(ref,atts);
            			    }
            			}
            	}else
            	{
            		Object o = child.getProperty(attField.getId());
            		if (o != null && !o.toString().equals(""))
            		{
            		    atts.addAttribute("","",attField.getDisplayName(false),"CDATA",XMLWriter.ValueParser(o,attField,appendRootPath,relativizePath));
            		}else{
            		    if (attField.isRequired())
            		    {
            		        atts.addAttribute("","",attField.getDisplayName(false),"CDATA","");
            		    }else{

            		    }
            		}
            	}
            }

        } catch (XFTInitException e) {
        } catch (ElementNotFoundException e) {
        }
    }

    public void addNoChildAttributes(XFTItem child, ArrayList children, AttributesImpl atts) throws SAXException,FieldNotFoundException
    {
        try {
            Iterator childElements = children.iterator();
            while(childElements.hasNext())
            {
            	XMLWrapperField xwf = (XMLWrapperField)childElements.next();
            	if (xwf.getExpose() && xwf.isReference())
            	{
            	    if ( xwf.getName().equals(child.getGenericSchemaElement().getExtensionFieldName()))
            	    {
            	        Iterator iter = child.getChildItems(xwf).iterator();
            	        while (iter.hasNext())
            	        {
            	            XFTItem subChild = (XFTItem)iter.next();
            	            addAttributes(subChild,atts);
            	        }
            	    }
            	}
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }
    }

    public void addXMLNSAttributes(AttributesImpl atts,XMLWrapperElement element){
//      ADD SCHEMA SPECIFICATION ATTRIBUTES
		if (allowSchemaLocation)
		{
			Enumeration enumer = XFTMetaManager.getPrefixEnum();
			while(enumer.hasMoreElements())
			{
			    String prefix = (String)enumer.nextElement();
			    String uri = XFTMetaManager.TranslatePrefixToURI(prefix);
			    atts.addAttribute("","","xmlns:"+prefix,"CDATA",uri);
			}
		    atts.addAttribute("","","xmlns:xsi","CDATA","http://www.w3.org/2001/XMLSchema-instance");
		    atts.addAttribute("","","xsi:schemaLocation","CDATA",XFT.GetAllSchemaLocations(location));

		}
    }

    public void writeChildData(XFTItem child,String parentNodeName) throws SAXException,FieldNotFoundException
    {
        XMLWrapperElement element = getElement(child.getXSIType());

        Iterator childElements = element.getChildren().iterator();
        while(childElements.hasNext())
        {
        	XMLWrapperField xmlField = (XMLWrapperField)childElements.next();
        	if (xmlField.getExpose())
        	{
        	    writeChildField(child,xmlField,parentNodeName,element.isANoChildElement());
        	}
        }

    }


    public void writeChildField(XFTItem item, XMLWrapperField xmlField, String parentNodeName, boolean noChildParent) throws SAXException,FieldNotFoundException
    {
        if (xmlField.isReference())
		{
			if (xmlField.isMultiple())
			{
			    if (item.isPreLoaded() || (!this.isAllowDBAccess()))
			    {
			        int counter = 0;
					try {
                        ArrayList children =  item.getCurrentChildItems(xmlField,item.getUser());
                        Iterator iter = children.iterator();
                        int child_count = children.size();
                        while (iter.hasNext())
                        {
                        	Object o = iter.next();

                        	if (o instanceof org.nrg.xft.XFTItem)
                        	{
                        			XFTItem many1 = (XFTItem)o;
                        			long startTime = Calendar.getInstance().getTimeInMillis();
                        			if ((!limited) || (!many1.canBeRootWithBase()))
                        			{
                        			    AttributesImpl atts = new AttributesImpl();
                        			    addAttributes(many1,atts);
                        			    if (! xmlField.getXMLType().getFullForeignType().equalsIgnoreCase(many1.getXSIType()))
                        				{
                        			        atts.addAttribute("","","xsi:type","CDATA",many1.getXSIType());
                        				}

                        			    if (xmlField.isInLineRepeaterElement())
                        				{
                                            writeHiddenFields(many1);
                                            writeChildData(many1,parentNodeName);
                        				}else
                        				{
                        					this.transformer.startElement("","",xmlField.getDisplayName(allowSchemaLocation),atts);
                                            writeHiddenFields(many1);
                        			        writeChildData(many1,xmlField.getDisplayName(allowSchemaLocation));
                        					this.transformer.endElement("","",xmlField.getDisplayName(allowSchemaLocation));
                        				}
                        			}

                        			if (many1.getXSIType().equals("xnat:subjectData"))
                        			{
                        				if(XFT.VERBOSE) System.out.println(counter + " of " + child_count + " Subjects (" +((float) (Calendar.getInstance().getTimeInMillis()-startTime)/1000) + "s)");
                        			}
                                    if(allowClear)many1.clear();
                        	}
                        	counter++;
                        }
                    } catch (XFTInitException e) {
                        logger.error("",e);
                    } catch (ElementNotFoundException e) {
                        logger.error("",e);
                    }
			    }else{
				    try {
                        XMLWrapperElement foreign = (XMLWrapperElement)xmlField.getReferenceElement();
                        int counter = 0;
                        ArrayList children = item.getChildItemIds(xmlField,item.getUser());

                        Iterator iter = children.iterator();
                        int child_count = children.size();
                        while (iter.hasNext())
                        {
                        	ArrayList o = (ArrayList)iter.next();

                        	try {
                                long startTime = Calendar.getInstance().getTimeInMillis();
                                XFTItem many1 = XFTItem.SelectItemByIds(foreign, o.toArray(),item.getUser(),true,xmlField.getPreventLoop());
                                if ((!limited) || (!many1.canBeRootWithBase()))
                                {
                                    AttributesImpl atts = new AttributesImpl();
                        		    addAttributes(many1,atts);
                        		    if (! xmlField.getXMLType().getFullForeignType().equalsIgnoreCase(many1.getXSIType()))
                        			{
                        		        atts.addAttribute("","","xsi:type","CDATA",many1.getXSIType());
                        			}

                        		    if (xmlField.isInLineRepeaterElement())
                        			{
                                        writeHiddenFields(many1);
                                        writeChildData(many1,parentNodeName);
                        			}else
                        			{
                        				this.transformer.startElement("","",xmlField.getDisplayName(allowSchemaLocation),atts);
                                        writeHiddenFields(many1);
                        		        writeChildData(many1,xmlField.getDisplayName(allowSchemaLocation));
                        				this.transformer.endElement("","",xmlField.getDisplayName(allowSchemaLocation));
                        			}
                                }

                                if (many1.getXSIType().equals("xnat:subjectData"))
                                {
                                	if(XFT.VERBOSE)System.out.println(counter + " of " + child_count + " Subjects (" +((float) (Calendar.getInstance().getTimeInMillis()-startTime)/1000) + "s)");
                                }

                                if(allowClear)many1.clear();

                                counter++;
                            } catch (DOMException e) {
                                logger.error("",e);
                            } catch (IllegalAccessException e) {
                            } catch (Exception e) {
                                logger.error("",e);
                            }
                        }
                    } catch (XFTInitException e) {
                        logger.error("",e);
                    } catch (ElementNotFoundException e) {
                        logger.error("",e);
                    }
			    }

				//CLEAR PROCESSED CHILDREN
                if(allowClear)item.clearChildren(xmlField);
			}else
			{
				try {
                    if (item.getProperty(xmlField.getId()) != null)
                    {
                    	if (item.getProperty(xmlField.getId()) instanceof XFTItem)
                    	{
                    	    XFTItem child = (XFTItem)item.getProperty(xmlField.getId());
                    	    if ((!limited) || (!child.canBeRootWithBase()))
                    	    {
                    			    boolean isNewElement= true;
                    			    if(XFTPseudonymManager.IsAnAlias(xmlField.getSQLName().toLowerCase(),parentNodeName))
                    				{
                    			        isNewElement= false;
                    				}else if((aliases.get(parentNodeName) != null) && (XFTPseudonymManager.IsAnAlias(xmlField.getSQLName().toLowerCase(),(String)aliases.get(parentNodeName))))
                    				{
                    			        isNewElement= false;
                    				}else if (! xmlField.isChildXMLNode())
                    				{
                    			        isNewElement= false;
                    				}

                    				AttributesImpl atts = new AttributesImpl();
                    			    addAttributes(child,atts);
                    			    if (! xmlField.getXMLType().getFullForeignType().equalsIgnoreCase(child.getXSIType()))
                    				{
                    			        atts.addAttribute("","","xsi:type","CDATA",child.getXSIType());
                    				}

                    			    if (! isNewElement)
                    				{
                                        writeHiddenFields(child);
                                        writeChildData(child,parentNodeName);
                    				}else
                    				{
                    					this.transformer.startElement("","",xmlField.getDisplayName(allowSchemaLocation),atts);
                                        writeHiddenFields(child);
                    			        writeChildData(child,xmlField.getDisplayName(allowSchemaLocation));
                    					this.transformer.endElement("","",xmlField.getDisplayName(allowSchemaLocation));
                    				}
//
//							//CLEAR PROCESSED CHILDREN
//							item.clearChildren(xmlField);
                    	    }
                    	}
                    }else if ((! xmlField.getWrapped().getMinOccurs().equalsIgnoreCase("0")) && (!item.getGenericSchemaElement().getExtensionFieldName().equals(xmlField.getName())))
    				{

    				    this.transformer.startElement("","",xmlField.getDisplayName(allowSchemaLocation),new AttributesImpl());
    					this.transformer.endElement("","",xmlField.getDisplayName(allowSchemaLocation));
    				}
                } catch (XFTInitException e) {
                    logger.error("",e);
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                }
			}
		}else{
			//NOT A REFERENCE

			AttributesImpl nextAtts=new AttributesImpl();
			Iterator attributes = xmlField.getAttributes().iterator();
			while (attributes.hasNext())
			{
				XMLWrapperField x = (XMLWrapperField)attributes.next();
				if (x.getXMLType().getLocalType().equals("string"))
				{
				    try {
                        Object o = item.getProperty(x.getId());
                        if (o != null && !o.toString().equals(""))
                        {
                            nextAtts.addAttribute("","",x.getDisplayName(false),"CDATA",XMLWriter.ValueParser(o,x,appendRootPath,relativizePath));
                        }else{
                            if (x.isRequired())
                            {
                        	    nextAtts.addAttribute("","",x.getDisplayName(false),"CDATA","");
                            }else{
                            }
                        }
                    } catch (XFTInitException e) {
                        logger.error("",e);
                    } catch (ElementNotFoundException e) {
                        logger.error("",e);
                    }
				}else{
				    try {
                        if(item.getProperty(x.getId())!=null)
                        {
                            Object o = item.getProperty(x.getId());
                        	if (o != null && !o.toString().equals(""))
                        	{
                        	    nextAtts.addAttribute("","",x.getDisplayName(false),"CDATA",XMLWriter.ValueParser(o,x,appendRootPath,relativizePath));
                        	}else{
                        	    if (x.isRequired())
                        	    {
                        		    nextAtts.addAttribute("","",x.getDisplayName(false),"CDATA","");
                        	    }else{
                        	    }
                        	}
                        }else{
                            if (x.isRequired()){
                        	    nextAtts.addAttribute("","",x.getDisplayName(false),"CDATA","");
                            }
                        }
                    } catch (XFTInitException e) {
                        logger.error("",e);
                    } catch (ElementNotFoundException e) {
                        logger.error("",e);
                    }
				}
			}

			if (xmlField.getChildren().size() > 0)
			{
			    addNoChildAttributes(item,xmlField.getChildren(),nextAtts);

			    boolean hasContent=false;
			    if (nextAtts.getLength()>0)
			    {
			        hasContent=true;
			    }else{
			        hasContent=item.hasXMLContent(xmlField,_allowDBAccess);
			    }

			    if (hasContent)
			    {
			        transformer.startElement("","",xmlField.getDisplayName(allowSchemaLocation),nextAtts);

					Iterator childElements2 = xmlField.getChildren().iterator();
					while(childElements2.hasNext())
					{
						XMLWrapperField xwf = (XMLWrapperField)childElements2.next();
						if (xwf.getExpose())
						{
						    writeChildField(item,xwf,xmlField.getDisplayName(allowSchemaLocation),false);
						}
					}
					transformer.endElement("","",xmlField.getDisplayName(allowSchemaLocation));

			    }
			}else
			{
			    try {
                    boolean needsEmptyTag = false;

                    if (nextAtts.getLength()>0){
                        needsEmptyTag=true;
                    }

                    if (xmlField.getXMLType()==null){
                    }else if((item.getProperty(xmlField.getId()) != null))
                    {

                    	String value = XMLWriter.ValueParser(item.getProperty(xmlField.getId()),xmlField,appendRootPath,relativizePath);
                    	if (! (value == null || value.equalsIgnoreCase("")))
                    	{
                    	    if (!noChildParent)transformer.startElement("","",xmlField.getDisplayName(allowSchemaLocation),nextAtts);
                    	    transformer.characters(value.toCharArray(),0,value.length());
                    	    if (!noChildParent)transformer.endElement("","",xmlField.getDisplayName(allowSchemaLocation));
                    	    needsEmptyTag=false;
                    	}
                    }else if (xmlField.isRequired()){
                        needsEmptyTag=true;
                    }


                    if (needsEmptyTag)
                    {
                        transformer.startElement("","",xmlField.getDisplayName(allowSchemaLocation),nextAtts);
                        transformer.endElement("","",xmlField.getDisplayName(allowSchemaLocation));
                    }
                }catch (XFTInitException e) {
                    logger.error("",e);
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                }
			}

		}
    }

    /**
     * @return Returns the allowSchemaLocation.
     */
    public boolean isAllowSchemaLocation() {
        return allowSchemaLocation;
    }
    /**
     * @param allowSchemaLocation The allowSchemaLocation to set.
     */
    public void setAllowSchemaLocation(boolean _allowSchemaLocation) {
        this.allowSchemaLocation = _allowSchemaLocation;
    }
    /**
     * @return Returns the limited.
     */
    public boolean isLimited() {
        return limited;
    }
    /**
     * @param limited The limited to set.
     */
    public void setLimited(boolean limited) {
        this.limited = limited;
    }
    /**
     * @return Returns the location.
     */
    public String getLocation() {
        return location;
    }
    /**
     * @param location The location to set.
     */
    public void setLocation(String location) {
        this.location = location;
    }
    /**
     * @return Returns the transformer.
     */
    public TransformerHandler getTransformer() {
        return transformer;
    }
    /**
     * @param transformer The transformer to set.
     */
    public void setTransformer(TransformerHandler transformer) {
        this.transformer = transformer;
    }

    /**
     * @return the _allowDBAccess
     */
    public boolean isAllowDBAccess() {
        return _allowDBAccess;
    }

    /**
     * @param access the _allowDBAccess to set
     */
    public void setAllowDBAccess(boolean access) {
        _allowDBAccess = access;
    }

    /**
     * @return the appendRootPath
     */
    public String getAppendRootPath() {
        return appendRootPath;
    }

    /**
     * @param appendRootPath the appendRootPath to set
     */
    public void setAppendRootPath(String appendRootPath) {
        this.appendRootPath = appendRootPath;
    }

    /**
     * @return the relativizePath
     */
    public String getRelativizePath() {
        return relativizePath;
    }

    /**
     * @param relativizePath the relativizePath to set
     */
    public void setRelativizePath(String relativizePath) {
        this.relativizePath = relativizePath;
    }

    /**
     * @return the writeHiddenFields
     */
    public boolean isWriteHiddenFields() {
        return writeHiddenFields;
    }

    /**
     * @param writeHiddenFields the writeHiddenFields to set
     */
    public void setWriteHiddenFields(boolean writeHiddenFields) {
        this.writeHiddenFields = writeHiddenFields;
    }


}
