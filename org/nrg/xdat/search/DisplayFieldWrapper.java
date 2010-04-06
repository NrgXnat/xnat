//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on May 5, 2005
 *
 */
package org.nrg.xdat.search;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.DisplayFieldElement;
import org.nrg.xdat.display.DisplayFieldReferenceI;
import org.nrg.xdat.display.HTMLLink;
import org.nrg.xdat.display.HTMLLinkProperty;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.identifier.Identifier;
import org.nrg.xft.sequence.SequentialObject;
import org.nrg.xft.utils.StringUtils;

/**
 * @author Tim
 *
 */
public class DisplayFieldWrapper implements Identifier,SequentialObject,DisplayFieldReferenceI{
    private DisplayField df = null;
    private String id = null;
    private int sequence = 0;
    private String type = null;
    private String header = null;
    private Boolean visible = null;
    
    private ArrayList secondaryFields = new ArrayList();
    private ArrayList xmlPathNames = new ArrayList();
    private ArrayList viewCols = new ArrayList();
    
    /**
     * @param displayField
     */
    public DisplayFieldWrapper(DisplayField displayField)
    {
        df = displayField;
        id = df.getParentDisplay().getElementName() + "." + df.getId();
        
        Iterator iter = df.getElements().iterator();
        while(iter.hasNext())
        {
            DisplayFieldElement dfe = (DisplayFieldElement)iter.next();
            if ((! xmlPathNames.contains(dfe.getSchemaElementName())) && (! dfe.getSchemaElementName().equals("")))
                xmlPathNames.add(dfe.getSchemaElementName());
            else if(! dfe.getViewName().equals("")){
                viewCols.add(dfe.getViewName() + "." + dfe.getViewColumn());
            }
        }
        
        if (df.getHtmlLink()!=null)
        {
            HTMLLink link = df.getHtmlLink();
            Iterator props = link.getProperties().iterator();
            while (props.hasNext())
            {
                HTMLLinkProperty prop = (HTMLLinkProperty)props.next();
                Iterator values = prop.getInsertedValues().values().iterator();
                while (values.hasNext())
                {
                    String valueField = (String)values.next();
                    if (!valueField.equals("@WHERE")){
                        
                        DisplayField df2 = df.getParentDisplay().getDisplayField(valueField);
                        if (df2 ==null)
                        {
                            System.out.println("UNABLE TO FIND DISPLAY FIELD:"+df.getParentDisplay()+"." + valueField);
                        }else{
                            secondaryFields.add(df.getParentDisplay().getElementName() + "." + valueField);
                            iter = df2.getElements().iterator();
                            while(iter.hasNext())
                            {
                                DisplayFieldElement dfe = (DisplayFieldElement)iter.next();
                                if ((! xmlPathNames.contains(dfe.getSchemaElementName())) && (! dfe.getSchemaElementName().equals("")))
                                    xmlPathNames.add(dfe.getSchemaElementName());
                                else if(! dfe.getViewName().equals("")){
                                    viewCols.add(dfe.getViewName() + "." + dfe.getViewColumn());
                                }
                            }
                        }
                    }
                }
            }
            
            Enumeration values = link.getSecureProps().keys();
            while (values.hasMoreElements())
            {
                String valueField = (String)values.nextElement();
                secondaryFields.add(df.getParentDisplay().getElementName() + "." + valueField);
                
                DisplayField df2 = df.getParentDisplay().getDisplayField(valueField);
                iter = df2.getElements().iterator();
                while(iter.hasNext())
                {
                    DisplayFieldElement dfe = (DisplayFieldElement)iter.next();
                    if ((! xmlPathNames.contains(dfe.getSchemaElementName())) && (! dfe.getSchemaElementName().equals("")))
                        xmlPathNames.add(dfe.getSchemaElementName());
                    else if(! dfe.getViewName().equals("")){
                        viewCols.add(dfe.getViewName() + "." + dfe.getViewColumn());
                    }
                }
            }
        }
    }
    
    /**
     * @return Returns the viewCols.
     */
    public ArrayList getViewCols() {
        return viewCols;
    }
    /**
     * @param viewCols The viewCols to set.
     */
    public void setViewCols(ArrayList viewCols) {
        this.viewCols = viewCols;
    }
    /**
     * @return Returns the xmlPathNames.
     */
    public ArrayList getXmlPathNames() {
        return xmlPathNames;
    }
    /**
     * @return Returns the secondaryFields.
     */
    public ArrayList getSecondaryFields() {
        return secondaryFields;
    }
    /**
     * @return Returns the df.
     */
    public DisplayField getDf() {
        return df;
    }
    /**
     * @param df The df to set.
     */
    public void setDf(DisplayField df) {
        this.df = df;
    }
    
    public String getId()
    {
        return id;
    }
    /**
     * @param id The id to set.
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return Returns the sequence.
     */
    public int getSequence() {
        return sequence;
    }
    /**
     * @param sequence The sequence to set.
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getLightColor()
    {
        return this.getDf().getLightColor();
    }
    
    public String getDarkColor()
    {
        return this.getDf().getDarkColor();
    }
    
    public Integer getHeaderCellWidth()
    {
        return null;
    }
    
    public Integer getHeaderCellHeight()
    {
        return null;
    }
    
    public String getHeaderCellAlign()
    {
        return null;
    }
    
    public String getHeaderCellVAlign()
    {
        return null;
    }
    
    public String getElementName(){
        return this.getDf().getParentDisplay().getElementName();
    }
    
    public DisplayField getDisplayField()
    {
        return this.getDf();
    }

    /**
     * @return
     */
    public String getHeader(){
        if (header==null || header.equals(""))
            return this.getDisplayField().getHeader();
        else
            return header;
    }
    
    public void setHeader(String s){
        header =s;
    }
    
    public String getRowID(){
        String alias = this.getDisplayField().getId();
        if (this.getValue()!=null)
            alias = df.getId() +"_" + DisplaySearch.cleanColumnName(this.getValue().toString());
        return alias;
    }
    public String getElementSQLName() throws XFTInitException,ElementNotFoundException{
        return this.getDisplayField().getParentDisplay().getSchemaElement().getSQLName();
    }
    public String toString()
    {
        return getId();
    }
    
    public Integer getHTMLCellWidth()
    {
        return getDisplayField().getHtmlCell().getWidth();
    }
    public Integer getHTMLCellHeight()
    {
        return getDisplayField().getHtmlCell().getHeight();
    }
    public String getHTMLCellAlign()
    {
        return getDisplayField().getHtmlCell().getAlign();
    }
    public String getHTMLCellVAlign()
    {
        return getDisplayField().getHtmlCell().getValign();
    }
    
    public String getSortBy()
    {
        return getDisplayField().getSortBy();
    }
    public HTMLLink getHTMLLink()
    {
        return getDisplayField().getHtmlLink();
    }
    public boolean isImage()
    {
        return getDisplayField().isImage();
    }
    public boolean isHtmlContent()
    {
        return getDisplayField().isHtmlContent();
    }
    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }
    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }
    

    
    private Object value=null;
    public Object getValue(){
        return value;
    }
    
    public void setValue(Object v){
        value=v;
        if(value!=null)
        	id = df.getParentDisplay().getElementName() + "." + df.getId() + "." + value;
        else
        	id = df.getParentDisplay().getElementName() + "." + df.getId();
    }
    
    public void setVisible(String v){
    	if(v!=null){
        	if(v.equalsIgnoreCase("true")){
        		visible=true;
        	}else{
        		visible=false;
        	}
    	}
    }
    
    public void setVisible(Boolean v){
    	visible=v;
    }
    public boolean isVisible() throws DisplayFieldNotFoundException{
    	if(visible==null){
    		return this.getDisplayField().isVisible();
    	}else{
    		return visible;
    	}
    }
}
