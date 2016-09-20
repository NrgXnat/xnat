/*
 * core: org.nrg.xft.utils.FieldMapping
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.utils;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class FieldMapping {
   private String elementName="";
   private Date createDate=null;
   private String title="";
   private String ID="";
   public ArrayList<String> fields = new ArrayList<String>();
   
   public FieldMapping(){
       createDate = Calendar.getInstance().getTime();
   }
   
   public FieldMapping(File f){
       this.populateFromFile(f);
   }
   
   public void populateFromFile(File f){
       Document dom =XMLUtils.GetDOM(f);
       Element e= dom.getDocumentElement();
       elementName = NodeUtils.GetAttributeValue(e, "data-type", "");
       title = NodeUtils.GetAttributeValue(e, "title", "");
       ID = NodeUtils.GetAttributeValue(e, "ID", "");
       String dateS= NodeUtils.GetAttributeValue(e, "create-date", "");
        try {
           createDate = DateUtils.parseDate(dateS);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        
        ArrayList<Node> nodes =NodeUtils.GetLevelNNodes(e, "field", 1);
        for(Node n : nodes){
            fields.add(NodeUtils.GetNodeText(n));
        }
   }
   
   public void saveToFile(File f){
       StringBuffer sb = new StringBuffer();
       sb.append("<FieldMapping data-type=\"").append(elementName).append("\"");
       sb.append(" create-date=\"").append(createDate).append("\"");
       sb.append(" title=\"").append(title).append("\"");
       sb.append(" ID=\"").append(ID).append("\"");
       sb.append(">");
       for(String field : fields){
           sb.append("\n\t<field>").append(field).append("</field>");
       }
       sb.append("\n</FieldMapping>");
       FileUtils.OutputToFile(sb.toString(), f.getAbsolutePath());
   }
   
   

/**
 * @return the elementName
 */
public String getElementName() {
    return elementName;
}

/**
 * @param elementName the elementName to set
 */
public void setElementName(String elementName) {
    this.elementName = elementName;
}

/**
 * @return the fields
 */
public ArrayList<String> getFields() {
    return fields;
}

/**
 * @param fields the fields to set
 */
public void setFields(ArrayList<String> fields) {
    this.fields = fields;
}

/**
 * @return the createDate
 */
public Date getCreateDate() {
    return createDate;
}

/**
 * @param createDate the createDate to set
 */
public void setCreateDate(Date createDate) {
    this.createDate = createDate;
}

/**
 * @return the title
 */
public String getTitle() {
    return title;
}

/**
 * @param title the title to set
 */
public void setTitle(String title) {
    this.title = title;
}

/**
 * @return the iD
 */
public String getID() {
    return ID;
}

/**
 * @param id the iD to set
 */
public void setID(String id) {
    ID = id;
}
   
   
}
