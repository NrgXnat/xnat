//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 3, 2005
 *
 */
package org.nrg.xdat.display;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xdat.search.QueryOrganizer;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.identifier.Identifier;
import org.nrg.xft.search.QueryOrganizerI;
import org.nrg.xft.sequence.SequentialObject;
import org.nrg.xft.utils.StringUtils;
/**
 * @author Tim
 *
 */
public class DisplayField implements Identifier,SequentialObject {
    static Logger logger = Logger.getLogger(DisplayField.class);
	private String id = "";
	private String header = "";
	private boolean image = false;
	private boolean searchable = false;
	private String dataType = null;
	private boolean visible = true;

	private String description = null;
	
	private ArrayList elements = new ArrayList();
	private Hashtable content = new Hashtable();
	
	private HTMLLink htmlLink = null;
	private String sortBy = "";
	private String sortOrder = ("ASC").intern();
	
	private HTMLCell htmlCell = new HTMLCell();
	private HTMLImage htmlImage = new HTMLImage();
	
	private ElementDisplay parentDisplay = null;
	
	private int sortIndex = 0;
	
	private ArrayList possibleValues = null;
	
	public String generatedFor="";
	
	public DisplayField(ElementDisplay ed)
	{
		parentDisplay=ed;
	}
	/**
	 * @return
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}
	
	public String getIdentifier()
	{
		return getId();
	}


	/**
	 * @param string
	 */
	public void setHeader(String string) {
		header = StringUtils.intern(string);
	}

	/**
	 * @param string
	 */
	public void setId(String string) {
		id = StringUtils.intern(string);
	}

	/**
	 * @return
	 */
	public ArrayList getElements() {
		return elements;
	}

	/**
	 * @param list
	 */
	public void setElements(ArrayList list) {
		elements = list;
	}

	public void addDisplayFieldElement(DisplayFieldElement dfe)
	{
		elements.add(dfe);
	}
	/**
	 * @return
	 */
	public Hashtable getContent() {
		return content;
	}

    	
	/**
	 * @param qo
	 * @return
	 */
	public String getSQLContent(QueryOrganizerI qo) throws FieldNotFoundException {
	    String content = getSqlContent();
		   
	   Iterator dfes = getElements().iterator();
	   while (dfes.hasNext())
	   {
	       DisplayFieldElement dfe = (DisplayFieldElement)dfes.next();
	       String dfeAlias = null;
	       if (dfe.getSchemaElementName().equalsIgnoreCase(""))
	       {
	           String viewName = getParentDisplay().getElementName() + ".";
	           viewName += dfe.getViewName() + "." + dfe.getViewColumn();
	           if (((QueryOrganizer)qo).getFieldAlias(viewName) !=null)
	           {
	               dfeAlias = (String)((QueryOrganizer)qo).getFieldAlias(viewName);
	           }else{
		           dfeAlias = dfe.getViewName() + "_" + dfe.getViewColumn();
	           }
	       }else{
	           if (dfe.getXdatType() == null || dfe.getXdatType().equalsIgnoreCase("")){
                   dfeAlias = dfe.getSQLJoinedName(qo.getRootElement());
                   if (dfeAlias == null)
                   {
                       dfeAlias = qo.translateStandardizedPath(dfe.getStandardizedPath());
                   }
	           }else{
	               	try { 
	               	 String viewName = getParentDisplay().getElementName() + "." + dfe.getXdatType() + "_";
	  	           viewName += dfe.getSchemaElementName() + "." + dfe.getXdatType();
	               	    String temp = ((QueryOrganizer)qo).getFieldAlias(viewName);
	               	    if (temp==null)
	               	    {
	 		               dfeAlias = SchemaElement.GetElement(dfe.getSchemaElementName()).getSQLName() + "_COUNT";
	               	    }else{
	 		               dfeAlias = (String)((QueryOrganizer)qo).getFieldAlias(viewName);
	               	    }
	                } catch (XFTInitException e) {
	                    logger.error("",e);
	                    dfeAlias = "'ERROR'";
	                } catch (ElementNotFoundException e) {
	                    logger.error("",e);
	                    dfeAlias = "'ERROR'";
	                }
	           }
	       }
	       
	       if (content==null)
	       {
		       content= dfeAlias;
	       }else{
		       content= StringUtils.ReplaceStr(content,"@" + dfe.getName(),dfeAlias);
	       }
	   }
	   return content;
	}
	
	/**
	 * Summary of field content
	 * @return
	 */
	public String getSummary()
	{
	    String content = getSqlContent();
		   
	   Iterator dfes = getElements().iterator();
	   while (dfes.hasNext())
	   {
	       DisplayFieldElement dfe = (DisplayFieldElement)dfes.next();
	       String dfeAlias = null;
	       if (dfe.getSchemaElementName().equalsIgnoreCase(""))
	       {
	           String viewName = getParentDisplay().getElementName() + ".";
	           viewName += dfe.getViewName() + "." + dfe.getViewColumn();
	           dfeAlias = dfe.getViewName() + "." + dfe.getViewColumn();
	       }else{
	           if (dfe.getXdatType() == null || dfe.getXdatType().equalsIgnoreCase("")){
		          dfeAlias = dfe.getSchemaElementName();
	           }else{
	               	try {
		               dfeAlias = SchemaElement.GetElement(dfe.getSchemaElementName()).getSQLName() + "_COUNT";
	                } catch (XFTInitException e) {
	                    logger.error("",e);
	                    dfeAlias = "'ERROR'";
	                } catch (ElementNotFoundException e) {
	                    logger.error("",e);
	                    dfeAlias = "'ERROR'";
	                }
	           }
	       }
	       
	       if (content==null)
	       {
		       content= dfeAlias;
	       }else{
		       content= StringUtils.ReplaceStr(content,"@" + dfe.getName(),dfeAlias);
	       }
	   }
	   return content;
	}

	/**
	 * @param hashtable
	 */
	public void setContent(Hashtable hashtable) {
		content = hashtable;
	}
	
	public String getSqlContent()
	{
		return (String)content.get("sql");
	}
	

	/**
	 * @return
	 */
	public boolean isImage() {
		return image;
	}

	/**
	 * @param b
	 */
	public void setImage(boolean b) {
		image = b;
	}
	
	public void setImage(String s)
	{
		if (s.equalsIgnoreCase("true"))
		{
			image = true;
		}else
		{
			image = false;
		}
	}

	/**
	 * @return
	 */
	public HTMLLink getHtmlLink() {
		return htmlLink;
	}

	/**
	 * @param link
	 */
	public void setHtmlLink(HTMLLink link) {
		htmlLink = link;
	}

	/**
	 * @return
	 */
	public String getSortBy() {
		if (sortBy.equalsIgnoreCase(""))
		{
			return getId();
		}else
		{
			return sortBy;
		}
	}

	/**
	 * @return
	 */
	public String getSortOrder() {
		return sortOrder;
	}

	/**
	 * @param string
	 */
	public void setSortBy(String string) {
		sortBy = StringUtils.intern(string);
	}

	/**
	 * @param string
	 */
	public void setSortOrder(String string) {
		sortOrder = StringUtils.intern(string);
	}

	/**
	 * @return
	 */
	public HTMLCell getHtmlCell() {
		return htmlCell;
	}

	/**
	 * @return
	 */
	public HTMLImage getHtmlImage() {
		return htmlImage;
	}

	/**
	 * @param cell
	 */
	public void setHtmlCell(HTMLCell cell) {
		htmlCell = cell;
	}

	/**
	 * @param image
	 */
	public void setHtmlImage(HTMLImage image) {
		htmlImage = image;
	}

	/**
	 * @return
	 */
	public ElementDisplay getParentDisplay() {
		return parentDisplay;
	}
	
	public String getPrimarySchemaField()
	{
	    DisplayFieldElement dfe = ((DisplayFieldElement)this.getElements().get(0));
	    
	    if (dfe.getSchemaElementName().equalsIgnoreCase(""))
	    {
		    return "VIEW_" + this.getParentDisplay().getElementName() + "." + dfe.getViewName() + "." + dfe.getViewColumn();
	    }else{
		    return dfe.getSchemaElementName();
	    }
	}
	
	/**
	 * @return ArrayList of Object[String path, SchemaFieldI sf]
	 */
	public ArrayList getSchemaFields()
	{
	    ArrayList al = new ArrayList();
	    Iterator iter = this.getElements().iterator();
	    while (iter.hasNext())
	    {
	        DisplayFieldElement dfe = (DisplayFieldElement)iter.next();
            Object[] o = new Object[2];
	        if (dfe.getSchemaElementName().equalsIgnoreCase(""))
		    {
                o[0]="VIEW_" + this.getParentDisplay().getElementName() + "." + dfe.getViewName() + "." + dfe.getViewColumn();
                o[1]=null;
			    al.add(o);
		    }else{
		        if (dfe.getXdatType().equalsIgnoreCase("COUNT"))
		        {
                    o[0]="VIEW_" + this.getParentDisplay().getElementName() + ".COUNT_" + dfe.getSchemaElementName() +".count";
                    o[1]=null;
                    al.add(o);
		        }else{
                    o[0]=dfe.getSchemaElementName();
                    try {
                        o[1]=dfe.getSchemaField();
                    } catch (XFTInitException e) {
                        logger.error("",e);
                        o[1]=null;
                   } catch (ElementNotFoundException e) {
                        logger.error("",e);
                        o[1]=null;
                    } catch (Exception e) {
                        logger.error("",e);
                        o[1]=null;
                    }
				    al.add(o);
		        }
		    }
	    }
        
	    return al;
	}

	/**
	 * @return
	 */
	public String getDataType() {
	    if (dataType==null)
	    {
	        dataType= deriveType();
	    }
        return dataType;
	}

	/**
	 * @return
	 */
	public boolean isSearchable() {
		return searchable;
	}

	/**
	 * @param string
	 */
	public void setDataType(String string) {
		dataType = string;
	}

	/**
	 * @param b
	 */
	public void setSearchable(boolean b) {
		searchable = b;
	}

	public void setSearchable(String s)
	{
		if (s.equalsIgnoreCase("true"))
		{
			searchable = true;
		}else
		{
			searchable = false;
		}
	}
	
	public static boolean MapTypesForQuotes(String type)
	{
		if (type != null)
		{
			if (type.equalsIgnoreCase("CHAR")){
				return true;
			}else if(type.equalsIgnoreCase("VARCHAR"))
			{
				return true;
			}else if(type.equalsIgnoreCase("STRING"))
			{
				return true;
			}else if(type.equalsIgnoreCase("DATE"))
			{
				return true;
			}else if(type.equalsIgnoreCase("TIMESTAMP"))
			{
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	public boolean needsSQLQuotes()
	{
		return MapTypesForQuotes(getDataType());
	}

    // empty quote comparisons do not work for dates and timestamps
    public static boolean MapTypesForEmptyQuotes(String type)
    {
        if (type != null)
        {
            if (type.equalsIgnoreCase("CHAR")){
                return true;
            }else if(type.equalsIgnoreCase("VARCHAR"))
            {
                return true;
            }else if(type.equalsIgnoreCase("STRING"))
            {
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    public boolean needsSQLEmptyQuotes()
    {
        return MapTypesForEmptyQuotes(getDataType());
    }
	
	private String deriveType()
	{
		if (this.elements.size() == 1)
		{
			DisplayFieldElement dfe = (DisplayFieldElement)elements.get(0);
			try {
				return dfe.getFieldType();
			} catch (Exception e) {
			}
			return "UNKNOWN";
		}else{
			return "UNKNOWN";
		}
	}

	/**
	 * @return
	 */
	public boolean isVisible() {
		return visible;
	}
	/**
	 * @param b
	 */
	public void setVisible(boolean b) {
		visible = b;
	}

	public void setVisible(String s)
	{
		if (s.equalsIgnoreCase("true"))
		{
			visible = true;
		}else{
			visible = false;
		}
	}
	
    /**
     * @return Returns the sortIndex.
     */
    public int getSortIndex() {
        return sortIndex;
    }
    /**
     * @param sortIndex The sortIndex to set.
     */
    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }
    
    
    
    /* (non-Javadoc)
     * @see org.nrg.xft.sequence.SequentialObject#getSequence()
     */
    public int getSequence() {
        return sortIndex;
    }
    /* (non-Javadoc)
     * @see org.nrg.xft.sequence.SequentialObject#setSequence(int)
     */
    public void setSequence(int sequence) {
        this.sortIndex = sequence;
    }
    public boolean hasEnumeration(String login)
    {
        if (this.content.size()==0)
        {
            if (getEnumeration(login).size()>0)
            {
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    public boolean hasEnumeration()
    {
        if (this.content.size()==0)
        {
            if (getEnumeration(null).size()>0)
            {
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    
    public ArrayList getEnumeration(String login)
    {
        if (this.possibleValues== null)
        {
            possibleValues = new ArrayList();
            
            if (this.elements.size() == 1)
    		{
    			DisplayFieldElement dfe = (DisplayFieldElement)elements.get(0);
    			try {
                    SchemaField sf = dfe.getSchemaField();
                    possibleValues.addAll(sf.getPossibleValues(login).values());
                } catch (XFTInitException e) {
                    logger.error("",e);
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                } catch (Exception e) {
                    logger.error("",e);
                }
    		}
        }
        return possibleValues;
    }
    
    public ArrayList getEnumeration()
    {
        return getEnumeration(null);
    }
    
    public String getLightColor()
    {
        return this.getParentDisplay().getLightColor();
    }
    
    public String getDarkColor()
    {
        return this.getParentDisplay().getDarkColor();
    }
    
    public String toString()
    {
        return this.getParentDisplay().getElementName() + ":"+this.getId();
    }
    

	private boolean htmlContent = false;
    /**
     * @return Returns the htmlContent.
     */
    public boolean isHtmlContent() {
        return htmlContent;
    }
    /**
     * @param htmlContent The htmlContent to set.
     */
    public void setHtmlContent(boolean htmlContent) {
        this.htmlContent = htmlContent;
    }
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
    

	
	public static DisplayField getDisplayFieldForUnknownPath(String s) throws XFTInitException,ElementNotFoundException,DisplayFieldNotFoundException,Exception{
		if(s.indexOf(".")>-1){
	        String keyElement = s.substring(0,s.indexOf("."));
	        String keyField = s.substring(s.indexOf(".")+1);

	        SchemaElement se = SchemaElement.GetElement(keyElement);
            return se.getDisplayField(keyField);
    	}else{
	        SchemaElement se = SchemaElement.GetElement(StringUtils.GetRootElementName(s));
    		return se.getDisplayFieldForXMLPath(s);
    	}
	}
	
	public static DisplayField getDisplayFieldForDFIdOrXPath(String s) throws XFTInitException,ElementNotFoundException,DisplayFieldNotFoundException,Exception{
		final String elementName1 = StringUtils.GetRootElementName(s);
        final String field = StringUtils.GetFieldText(s);

        final SchemaElement element = SchemaElement.GetElement(elementName1);
        
        try{
        	return element.getDisplayField(field);
        }catch(DisplayFieldNotFoundException e){
    		try {
				return element.getDisplayFieldForXMLPath(s);
			} catch (Exception e1) {
				logger.error("",e1);
				throw e;
			}
        }
    	
	}
}

