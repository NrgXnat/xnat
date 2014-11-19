/*
 * org.nrg.xdat.display.ElementDisplay
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.display;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.velocity.app.Velocity;
import org.nrg.xdat.collections.DisplayFieldCollection;
import org.nrg.xdat.collections.DisplayFieldRefCollection.DuplicateDisplayFieldRefException;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.StringUtils;
/**
 * @author Tim
 *
 */
public class ElementDisplay extends DisplayFieldCollection {
	static Logger logger = Logger.getLogger(ElementDisplay.class);
	private String elementName = "";
	private String valueField = "";
	private String displayField = "";
	private String displayLabel = "";
	private String briefDescription = "";
	private String fullDescription = "";
	private Hashtable<String,DisplayVersion> versions = new Hashtable<String,DisplayVersion>();
	private Hashtable views = new Hashtable();
	private Hashtable arcs = new Hashtable();
	private Hashtable schemaLinks = new Hashtable();
	private Hashtable schemaLinksByAlias = new Hashtable();

	private Hashtable viewLinks = new Hashtable();
	
	private SchemaElement schemaElement = null;
	
	private String lightColor=null;
	private String darkColor = null;
	
	private Integer sortOrder = null;
	/**
	 * @return
	 */
	public String getElementName() {
		return elementName;
	}
	
	public SchemaElement getSchemaElement() throws XFTInitException,ElementNotFoundException
	{
		if (schemaElement == null)
		{
			schemaElement = SchemaElement.GetElement(elementName);
		}
		return schemaElement;
	}

	/**
	 * @return
	 */
	public Hashtable<String,DisplayVersion> getVersions() {
		return versions;
	}

	/**
	 * @param string
	 */
	public void setElementName(String string) {
		elementName = string;
	}

	/**
	 * @param hashtable
	 */
	public void setVersions(Hashtable<String,DisplayVersion> hashtable) {
		versions = hashtable;
	}

	public void addVersion(DisplayVersion dv)
	{
		dv.setParentElementDisplay(this);
		this.versions.put(dv.getVersionName(),dv);
	}
	
	public DisplayVersion getVersion(String version, String defaultName)
	{
		DisplayVersion match=getVersion(version);
		if ( match != null) 
		{
			return match;
		}else if ( (DisplayVersion)getVersions().get(defaultName) != null) 
		{
			return (DisplayVersion)getVersions().get(defaultName);
		}else{
			if (getVersions().size() > 0)
				return (DisplayVersion)getVersions().values().toArray()[0];
			else
				return null;
		}
	}
	
	public DisplayVersion getVersion(String version)
	{
	    if ( (DisplayVersion)getVersions().get(version) != null) 
		{
			return (DisplayVersion)getVersions().get(version);
		}else if ( version.equals("all")) 
		{
			DisplayVersion dv=new DisplayVersion();
			dv.setVersionName("all");
			for(Object ref:this.getSortedFields()){
				DisplayField field = (DisplayField)ref;
				if(field.isVisible() && !(field instanceof SQLQueryField)){
					try {
						DisplayFieldRef df = new DisplayFieldRef(dv);
						df.setVisible("true");
						df.setId(field.getId());
						df.setElementName(this.getElementName());
						if(field.getHeader()==null|| !field.getHeader().equals(""))
						{
							df.setHeader(field.getHeader());
						}
						
						if(field.getDataType()==null|| !field.getDataType().equals(""))
						{
							df.setType(field.getDataType());
						}
						dv.addDisplayField(df);
					} catch (DuplicateDisplayFieldRefException e) {
						logger.error(e);
					}
				}
			}

			this.addVersion(dv);
			
			return dv;
		}else
		{
		    return null;
		}
	}
	
	/**
	 * @return
	 */
	public Hashtable getViews() {
		return views;
	}

	/**
	 * @param hashtable
	 */
	public void setViews(Hashtable hashtable) {
		views = hashtable;
	}
	
	public void addView(SQLView sv)
	{
		views.put(sv.getName(),sv);
	}

	public ArrayList getSortedViews()
	{
		ArrayList temp = new ArrayList();
		temp.addAll(getViews().values());
		Collections.sort(temp,SQLView.SequenceComparator);
		return temp;
	}

	/**
	 * @return
	 */
	public Hashtable getArcs() {
		return arcs;
	}

	/**
	 * @param hashtable
	 */
	public void setArcs(Hashtable hashtable) {
		arcs = hashtable;
	}

	public void addArc(Arc arc)
	{
		arcs.put(arc.getName(),arc);
	}
	
	/**
	 * @return
	 */
	public Hashtable getSchemaLinks() {
		return schemaLinks;
	}
	
	public void addSchemaLink(SchemaLink sl)
	{
		schemaLinks.put(sl.getElement(),sl);
		schemaLinksByAlias.put(sl.getAlias(),sl);
	}
	
	public String getSelectsForAllDisplayFields() throws Exception
	{
		SchemaElementI rootElement = SchemaElement.GetElement(this.getElementName());
		StringBuffer sb = new StringBuffer();
		Enumeration fields = this.getDisplayFieldHash().keys();
		int counter =0;
		while (fields.hasMoreElements())
		{
			if (counter++ != 0)
			{
				sb.append(" ,");
			}
			String fieldID = (String)fields.nextElement();
			DisplayField df = (DisplayField)this.getDisplayFieldHash().get(fieldID);
			String content = df.getSqlContent();
			if (content == null)
			{
				DisplayFieldElement dfe = (DisplayFieldElement)df.getElements().get(0);
				if (dfe.getViewName().equalsIgnoreCase(""))
				{
					//ELEMENT FIELD
					String elementName = StringUtils.GetRootElementName(dfe.getSchemaElementName());
					if (elementName.equalsIgnoreCase(rootElement.getFullXMLName()))
					{
						//SUB ELEMENT
						sb.append(rootElement.getSQLName() + "_" + dfe.getSQLJoinedName(rootElement)).append(" AS ").append(df.getId());
					}else
					{
						//LINKED ELEMENT
						SchemaLink sl = (SchemaLink)this.getSchemaLinksByAlias().get(elementName);
						if (sl == null)
						{
							throw new Exception("Unknown Link: " +rootElement.getFullXMLName() + "->" + elementName);
						}
						SchemaElementI foreign = SchemaElement.GetElement(sl.getElement());
						sb.append(foreign.getSQLName() + "_" + dfe.getSQLJoinedName(foreign)).append(" AS ").append(df.getId());
					}
				}else
				{
					//VIEW FIELD
					sb.append(dfe.getViewColumn()).append(" AS ").append(df.getId());
				}
			}else
			{
				//SPLICE FIELDS INTO CONTENT
				Iterator iter = df.getElements().iterator();
				while (iter.hasNext())
				{
					DisplayFieldElement dfe = (DisplayFieldElement)iter.next();
					if (dfe.getViewName().equalsIgnoreCase(""))
					{
						//ELEMENT FIELD
						String elementName = StringUtils.GetRootElementName(dfe.getSchemaElementName());
						if (elementName.equalsIgnoreCase(rootElement.getFullXMLName()))
						{
							//SUB ELEMENT
							content= StringUtils.ReplaceStr(content,"@"+dfe.getName(),rootElement.getSQLName() + "_" + dfe.getSQLJoinedName(rootElement));
						}else
						{
							//LINKED ELEMENT
							SchemaLink sl = (SchemaLink)this.getSchemaLinksByAlias().get(elementName);
							SchemaElementI foreign = SchemaElement.GetElement(sl.getElement());
							content= StringUtils.ReplaceStr(content,"@"+dfe.getName(),foreign.getSQLName() + "_" + dfe.getSQLJoinedName(foreign));
						}
					}else
					{
						//VIEW FIELD
						content= StringUtils.ReplaceStr(content,"@"+dfe.getName(),dfe.getViewColumn());
					}
				}
				sb.append(content).append(" AS ").append(df.getId());
			}
		}
		return sb.toString();
	}

	/**
	 * @return
	 */
	public Hashtable getSchemaLinksByAlias() {
		return schemaLinksByAlias;
	}

	/**
	 * @return
	 */
	public Hashtable getViewLinks() {
		return viewLinks;
	}
	
	public void addViewLink(ViewLink vl)
	{
		viewLinks.put(vl.getAlias(),vl);
	}

	public String getDescription()
	{
	    if (this.getBriefDescription()!=null && ! (this.getBriefDescription().equals("")))
	    {
	        return getBriefDescription();
	    }
		String s= getVersion("listing","default").getBriefDescription();
		if (s!=null && ! (s.equals("")))
	    {
	        return s;
	    }
		return this.getElementName();
	}
	/**
	 * @return
	 */
	public String getDisplayField() {
		return displayField;
	}

	/**
	 * @return
	 */
	public String getDisplayLabel() {
		return displayLabel;
	}

	/**
	 * @return
	 */
	public String getValueField() {
		return valueField;
	}

	/**
	 * @param string
	 */
	public void setDisplayField(String string) {
		displayField = string;
	}

	/**
	 * @param string
	 */
	public void setDisplayLabel(String string) {
		displayLabel = string;
	}

	/**
	 * @param string
	 */
	public void setValueField(String string) {
		valueField = string;
	}
	
	public int getSequence()
	{
	    if (sortOrder==null)
	    {
	        try {
                ElementSecurity es = ElementSecurity.GetElementSecurity(this.getElementName());
                if (es!=null)
                {
                    sortOrder = es.getSequence();
                }else{
                    sortOrder = new Integer(0);
                }
            } catch (Exception e) {
                sortOrder = new Integer(0);
            }
	    }
	    return sortOrder.intValue();
	}

	/**
	 * ArrayList of DisplayFields
	 * @return
	 */
	public Collection getSearchableFields()
	{
	    ArrayList al = new ArrayList();
	    
	    Iterator fields = this.getSortedFields().iterator();
	    while(fields.hasNext())
	    {
	        DisplayField df = (DisplayField)fields.next();
	        if (df.isSearchable())
	        {
	            al.add(df);
	        }
	    }
	    
	    if (al.size()==0)
	    {
	        return this.getDisplayFieldHash().values();
	    }
	    
	    return al;
	}
	


	/**
	 * ArrayList of ArrayLists of DisplayFields
	 * @return
	 */
	public Collection getSearchableFields(int cols)
	{
	    ArrayList al = new ArrayList();
	    
	    Collection searchableFields = getSearchableFields();
	    
	    int rows = searchableFields.size() / cols;
	    rows++;
	    	    
	    for (int i=0;i<rows;i++)
	    {
	        int startIndex = cols * i;
	        int endIndex = startIndex + cols;
	        
            ArrayList row = new ArrayList();
	        while (startIndex < endIndex)
	        {
		        if (startIndex <searchableFields.size())
		        {
		            row.add(searchableFields.toArray()[startIndex]);
		        }
		        
		        startIndex++;
	        }
	        
	        al.add(row);
	    }
	    
	    return al;
	}
    
    public String getLightColor()
    {
        if (lightColor ==null)
        {
            lightColor = "";
            Iterator iter = this.getVersions().values().iterator();
            while (iter.hasNext())
            {
                DisplayVersion dv = (DisplayVersion)iter.next();
                if (dv.getLightColor()!=null && !dv.getLightColor().equalsIgnoreCase(""))
                {
                    lightColor = dv.getLightColor();
                    break;
                }
            }
        }
        return lightColor;
    }
    
    public String getDarkColor()
    {
        if (darkColor ==null)
        {
            darkColor = "";
            Iterator iter = this.getVersions().values().iterator();
            while (iter.hasNext())
            {
                DisplayVersion dv = (DisplayVersion)iter.next();
                if (dv.getDarkColor()!=null && !dv.getDarkColor().equalsIgnoreCase(""))
                {
                    darkColor = dv.getDarkColor();
                    break;
                }
            }
        }
        return darkColor;
    }
    

    /**
     * @return Returns the briefDescription.
     */
    public String getBriefDescription() {
        return briefDescription;
    }
    /**
     * @param briefDescription The briefDescription to set.
     */
    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }
    /**
     * @return Returns the fullDescription.
     */
    public String getFullDescription() {
        return fullDescription;
    }
    /**
     * @param fullDescription The fullDescription to set.
     */
    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }
    
    public String toString()
    {
        return this.getElementName();
    }
    
    public String getCustomSearchVM()
    {
        try {
            String templateName = "/screens/" + this.getSchemaElement().getFormattedName() + "_search.vm";

            if (Velocity.templateExists(templateName))
            {
                return templateName;
            }else
            {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    public String getCustomSearchVM(boolean brief)
    {
    	if(brief){
            try {
                String templateName = "/screens/" + this.getSchemaElement().getFormattedName() + "_search_brief.vm";

                if (Velocity.templateExists(templateName))
                {
                    return templateName;
                }else
                {
                	templateName = "/screens/" + this.getSchemaElement().getFormattedName() + "_search.vm";

                    if (Velocity.templateExists(templateName))
                    {
                        return templateName;
                    }else
                    {
                        return null;
                    }
                }
            } catch (Exception e) {
                return null;
            }
    	}else{
    		return getCustomSearchVM();
    	}
    }
    
    public String getProperName()
    {
        try {
            return this.getSchemaElement().getGenericXFTElement().getProperName();
        } catch (Exception e) {
            return "";
        }
    }
    
    public String getSQLName()
    {
        try {
            return this.getSchemaElement().getSQLName();
        } catch (Exception e) {
            return "";
        }
    }
    
    public String getPrefix()
    {
        try {
            return this.getSchemaElement().getGenericXFTElement().getSchemaTargetNamespacePrefix();
        } catch (Exception e) {
            return "";
        }
    }
	
	public final static Comparator SequenceComparator = new Comparator() {
  	  public int compare(Object mr1, Object mr2) throws ClassCastException {
  		  try{
  			int value1 = ((ElementDisplay)mr1).getSequence();
  			int value2 = ((ElementDisplay)mr2).getSequence();

  			if (value1 > value2)
  			  {
  				  return 1;
  			  }else if(value1 < value2)
  			  {
  				  return -1;
  			  }else
  			  {
  				  return 0;
  			  }
  		  }catch(Exception ex)
  		  {
  			  throw new ClassCastException("Error Comparing Sequence");
  		  }
  	  }
  	};
  	
  	public ElementSecurity getElementSecurity() throws Exception{
  	    return ElementSecurity.GetElementSecurity(this.getElementName());
  	}
  	
  	public String getVersionsXML(){

		StringBuffer sb = new StringBuffer();
		sb.append("<DisplayVersions ");
		sb.append("element_name=\"").append(this.getElementName()).append("\">");
		sb.append("\n<versions>");
		for(Entry<String,DisplayVersion> entry:this.getVersions().entrySet()){
			sb.append("<version name=\"").append(entry.getKey()).append("\"");
			sb.append(" orderBy=\"").append(entry.getValue().getDefaultOrderBy()).append("\"");
			sb.append(" darkColor=\"").append(entry.getValue().getDarkColor()).append("\"");
			sb.append(" lightColor=\"").append(entry.getValue().getLightColor()).append("\"");
			sb.append(" defaultSortOrder=\"").append(entry.getValue().getDefaultSortOrder()).append("\"");
			sb.append(">\n<fields>");
			for(DisplayFieldReferenceI field:entry.getValue().getAllFields()){
				sb.append("<field");
				sb.append(" id=\"").append(field.getId()).append("\"");
				if(field.getElementName()==null|| !field.getElementName().equals(""))
				{
					sb.append(" element_name=\"").append(field.getElementName()).append("\"");
				}else{
					sb.append(" element_name=\"").append(elementName).append("\"");
				}
				
				if(field.getHeader()==null|| !field.getHeader().equals(""))
				{
					sb.append(" header=\"").append(field.getHeader()).append("\"");
				}
				
				if(field.getType()==null|| !field.getType().equals(""))
				{
					sb.append(" type=\"").append(field.getType()).append("\"");
				}
				
				if(field.getValue()==null|| !field.getValue().equals(""))
				{
					sb.append(" value=\"").append(field.getValue()).append("\"");
				}
				
				try {
					if(field.isVisible())
					{
						sb.append(" visible=\"true\"");
					}
				} catch (DisplayFieldNotFoundException e) {
					e.printStackTrace();
				}
				
				sb.append("/>");
			}
			sb.append("</fields>");
			
			sb.append("</version>");
		}
		
		//all
		sb.append("<version");
		sb.append(" name=\"all\"");
		sb.append("<fields>");
		for(Object ref:this.getSortedFields()){
			DisplayField field = (DisplayField)ref;
			if(field.isVisible() && !(field instanceof SQLQueryField)){
				sb.append("<field");
				sb.append(" id\":\"").append(field.getId()).append("\"");
				sb.append(" element_name=\"").append(elementName).append("\"");
				
				if(field.getHeader()==null|| !field.getHeader().equals(""))
				{
					sb.append(" header=\"").append(field.getHeader()).append("\"");
				}
				
				if(field.getDataType()==null|| !field.getDataType().equals(""))
				{
					sb.append(" type=\"").append(field.getDataType()).append("\"");
				}
				
				sb.append(" visible=\"true\"");
				
				sb.append("/>");
			}
		}
		sb.append("</fields>");
		
		sb.append("</version>");
		
		sb.append("</versions>");
		sb.append("</DisplayVersions>");
		
		return sb.toString();
  	}
  	
  	public String getVersionsJSON(){

		StringBuffer sb = new StringBuffer();
		sb.append("{\"DisplayVersions\":{");
		sb.append("\"element_name\":\"").append(this.getElementName()).append("\"");
		sb.append(",\"versions\":[");
		int c=0;
		for(Entry<String,DisplayVersion> entry:this.getVersions().entrySet()){
			if(c++>0)sb.append(",");
			sb.append("{");
			sb.append("\"name\":\"").append(entry.getKey()).append("\"");
			sb.append(",\"orderBy\":\"").append(entry.getValue().getDefaultOrderBy()).append("\"");
			sb.append(",\"darkColor\":\"").append(entry.getValue().getDarkColor()).append("\"");
			sb.append(",\"lightColor\":\"").append(entry.getValue().getLightColor()).append("\"");
			sb.append(",\"defaultSortOrder\":\"").append(entry.getValue().getDefaultSortOrder()).append("\"");
			sb.append(",\"fields\":[");
			int i=0;
			for(DisplayFieldReferenceI field:entry.getValue().getAllFields()){
				if(i++>0)sb.append(",");
				sb.append("{");
				sb.append("\"id\":\"").append(field.getId()).append("\"");
				if(field.getElementName()==null|| !field.getElementName().equals(""))
				{
					sb.append(",\"element_name\":\"").append(field.getElementName()).append("\"");
				}else{
					sb.append(",\"element_name\":\"").append(elementName).append("\"");
				}
				
				if(field.getHeader()==null|| !field.getHeader().equals(""))
				{
					sb.append(",\"header\":\"").append(field.getHeader()).append("\"");
				}
				
				if(field.getType()==null|| !field.getType().equals(""))
				{
					sb.append(",\"type\":\"").append(field.getType()).append("\"");
				}
				
				if(field.getValue()==null|| !field.getValue().equals(""))
				{
					sb.append(",\"value\":\"").append(field.getValue()).append("\"");
				}
				
				try {
					if(field.isVisible())
					{
						sb.append(",\"visible\":\"true\"");
					}
				} catch (DisplayFieldNotFoundException e) {
					e.printStackTrace();
				}
				
				sb.append("}");
			}
			sb.append("]");
			
			sb.append("}");
		}
		
		//all
		if(c++>0)sb.append(",");
		sb.append("{");
		sb.append("\"name\":\"all\"");
		sb.append(",\"fields\":[");
		int i=0;
		for(Object ref:this.getSortedFields()){
			DisplayField field = (DisplayField)ref;
			if(field.isVisible() && !(field instanceof SQLQueryField)){

				if(i++>0)sb.append(",");
				sb.append("{");
				sb.append("\"id\":\"").append(field.getId()).append("\"");
				sb.append(",\"element_name\":\"").append(elementName).append("\"");
				
				if(field.getHeader()==null|| !field.getHeader().equals(""))
				{
					sb.append(",\"header\":\"").append(field.getHeader()).append("\"");
				}
				
				if(field.getDataType()==null|| !field.getDataType().equals(""))
				{
					sb.append(",\"type\":\"").append(field.getDataType()).append("\"");
				}
				
				sb.append(",\"visible\":\"true\"");
				
				sb.append("}");
			}
		}
		sb.append("]");
		
		sb.append("}");
		
		sb.append("]");
		sb.append("}}");
		
		return sb.toString();
  	}
  	
  	public DisplayField getProjectIdentifierField(){
  		for(DisplayField df:this.getSortedFields()){
  			if(df.getId().toUpperCase().endsWith("PROJECT_IDENTIFIER"))
  				return df;
  		}
  			
  		return null;
  	}
}

