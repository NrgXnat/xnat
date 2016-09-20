/*
 * core: org.nrg.xft.generators.JavaBeanGenerator
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.generators;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTRelationSpecification;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperElement;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperFactory;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.XftStringUtils;


public class JavaBeanGenerator {
    public static final String INTERFACE_PACKAGE = "org.nrg.xdat.model";
    private String project = "";
    private enum TYPE {data,single_reference,multi_reference,inline_repeater,LONG_DATA,NO_CHILD}
    public static boolean VERSION5=true;
    private String header ="\t";
    
    public static void SetVersion5(final boolean ve){
    	VERSION5=ve;
    }
    
    /**
     * @param e
     * @param location
     * @throws Exception
     */
    public void generateJavaBeanFile(GenericWrapperElement e, String location) throws Exception
    {
        
        StringBuffer sb = new StringBuffer();
        
        String packageName = project + ".bean";
        
        sb.append("/*\n * GENERATED FILE\n * Created on ").append(Calendar.getInstance().getTime()).append("\n *\n */");
        sb.append("\npackage ").append(packageName).append(";");
        //IMPORTS
        sb.append("\nimport org.apache.log4j.Logger;");
        sb.append("\nimport org.nrg.xdat.bean.base.BaseElement;");
        sb.append("\n\nimport java.util.*;");
        sb.append("\n\n/**\n * @author XDAT\n *\n */");
        
        //CLASS
        String extensionName = "BaseElement";
        if (e.isExtension())
        {
           GenericWrapperElement ext = GenericWrapperElement.GetElement(e.getExtensionType());
           if (!ext.getXSIType().equals(e.getXSIType()))
            extensionName = getFormattedBean(ext);
        }
        

        sb.append("/*\n ******************************** \n * DO NOT MODIFY THIS FILE \n *\n ********************************/");
        
        sb.append("\n@SuppressWarnings({\"unchecked\",\"rawtypes\"})");
        
        sb.append("\npublic class ").append(getFormattedBean(e)).append(" extends ").append(extensionName).append(" implements java.io.Serializable, ").append(INTERFACE_PACKAGE).append(".").append(getFormattedInterface(e)).append(" {");
        sb.append("\n\tpublic static final Logger logger = Logger.getLogger(").append(getFormattedBean(e)).append(".class);");
        sb.append("\n\tpublic static final String SCHEMA_ELEMENT_NAME=\"").append(e.getFullXMLName()).append("\";");
        //ADD CONSTRUCTORS


        
        //ADD SCHEMA METHODS
        sb.append("\n\n");
        sb.append("\t").append("public String getSchemaElementName(){");
        sb.append("\n\t\t").append("return \"").append(e.getLocalXMLName()).append("\";");
        sb.append("\n\t}");
        sb.append("\n\n");
        sb.append("\t").append("public String getFullSchemaElementName(){");
        sb.append("\n\t\t").append("return \"").append(e.getFullXMLName()).append("\";");
        sb.append("\n\t}");

        ArrayList<String> xmlPaths = new ArrayList<String>();
        ArrayList<TYPE> types = new ArrayList<TYPE>();
        ArrayList<String> methods = new ArrayList<String>();
        ArrayList<String> gets = new ArrayList<String>();
        ArrayList<String> foreignElements = new ArrayList<String>();
        
        Iterator fields = e.getAllFields(true,true).iterator();
        while (fields.hasNext())
        {
            GenericWrapperField f= (GenericWrapperField)fields.next();
            if (f.isReference())
            {
                String xmlPath = f.getXMLPathString();
                String formatted = formatFieldName(xmlPath);
                if (formatted.equalsIgnoreCase("USER"))
                {
                    formatted = "userProperty";
                }
                SchemaElementI foreign = f.getReferenceElement();
                
                if (foreign.getGenericXFTElement().getAddin().equals(""))
                {
                    final String foreignClassName = getFormattedBean(foreign.getGenericXFTElement());
                    final String foreignInterface=getFormattedInterface(foreign.getGenericXFTElement());
                    
                    if (f.isMultiple())
                    {
                        xmlPaths.add(f.getXMLPathString());
                        if (f.isInLineRepeaterElement())
                        {
                            types.add(TYPE.inline_repeater);
                        }else if(foreign.getGenericXFTElement().isANoChildElement()){
                            types.add(TYPE.NO_CHILD);
                        }else{
                            types.add(TYPE.multi_reference);
                        }
                        methods.add("add" +formatted);
                        gets.add("get" +formatted);
                        foreignElements.add(foreign.getGenericXFTElement().getSchemaTargetNamespaceURI() + ":" + foreign.getGenericXFTElement().getLocalXMLName());
                        
                        sb.append("\n\t private List");
                        if (VERSION5)sb.append("<"+ project + ".bean.").append(foreignClassName).append(">");
                        sb.append(" _" + formatted + " =new ArrayList");
                        if (VERSION5)sb.append("<"+ project + ".bean.").append(foreignClassName).append(">");
                        sb.append("();");
                        sb.append("\n");
                        sb.append("\n\t/**");
                        sb.append("\n\t * " + xmlPath);
                         sb.append("\n\t * @return Returns an List of "+ project + ".bean.").append(foreignClassName).append("\n\t */");
                        sb.append("\n\t").append("public ");
                        if (VERSION5)
                        	sb.append("<A extends " +INTERFACE_PACKAGE +".").append(foreignInterface).append("> List<A>");
                        else
                        	sb.append ("List");
                        sb.append(" get").append(formatted).append("() {");
                        sb.append("\n\t\treturn (List<A>) _" + formatted + ";");
                        sb.append("\n\t}");
                        
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(ArrayList");
                        if (VERSION5)sb.append("<"+ project + ".bean.").append(foreignClassName).append(">");
                        sb.append(" v){");
                        sb.append("\n\t\t_" + formatted + "=v;");
                        sb.append("\n\t}");
                        
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Adds the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void add").append(formatted).append("("+ project + ".bean.").append(foreignClassName).append(" v){");
                        sb.append("\n\t\t_" + formatted + ".add(v);");
                        sb.append("\n\t}");
                        
                        sb.append("\n");
                        sb.append("\n\t/**");
                        sb.append("\n\t * " + xmlPath);
                        sb.append("\n\t * Adds org.nrg.xdat.model.").append(foreignInterface).append("\n\t */");
                        sb.append("\n\t").append("public <A extends ").append(INTERFACE_PACKAGE +".").append(foreignInterface).append("> void add").append(formatted).append("(A item) throws Exception{"); 
                        sb.append("\n\t_" + formatted).append(".add").append("(("+project + ".bean.").append(foreignClassName+")item);"); 
                        sb.append("\n\t}");
                        
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Adds the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void add").append(formatted).append("(Object v){");
                        sb.append("\n\t\tif (v instanceof "+ project + ".bean.").append(foreignClassName).append(")");
                        sb.append("\n\t\t\t_" + formatted + ".add(("+ project + ".bean.").append(foreignClassName).append(")v);");
                        sb.append("\n\t\telse");
                        sb.append("\n\t\t\tthrow new IllegalArgumentException(\"Must be a valid "+ project + ".bean.").append(foreignClassName).append("\");");
                        sb.append("\n\t}");
                       

                    }else{
                        if(!f.getName().equalsIgnoreCase(e.getExtensionFieldName())){
                            xmlPaths.add(f.getXMLPathString());
                            if (f.isInLineRepeaterElement())
                            {
                                types.add(TYPE.inline_repeater);
                            }else if(foreign.getGenericXFTElement().isANoChildElement()){
                                types.add(TYPE.NO_CHILD);
                            }else{
                                types.add(TYPE.single_reference);
                            }
                            methods.add("set" +formatted);
                            gets.add("get" +formatted);
                            foreignElements.add(foreign.getGenericXFTElement().getSchemaTargetNamespaceURI() + ":" + foreign.getGenericXFTElement().getLocalXMLName());
                            sb.append("\n\t private "+ project + ".bean." + foreignClassName +" _" + formatted + " =null;");
                            sb.append("\n");
                            sb.append("\n\t/**");
                            sb.append("\n\t * " + xmlPath);
                             sb.append("\n\t * @return "+ project + ".bean.").append(foreignClassName).append("\n\t */");
                            sb.append("\n\t").append("public "+ project + ".bean." + foreignClassName +" get").append(formatted).append("() {");
                            sb.append("\n\t\treturn _" + formatted +";");
                            sb.append("\n\t}");
                            
                            sb.append("\n\n");
                            sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                            sb.append("\n\t").append("public void set").append(formatted).append("("+ project + ".bean." + foreignClassName +" v){");
                            sb.append("\n\t\t_").append(formatted + " =v;");
                            sb.append("\n\t}");
                            
                            sb.append("\n\n");
                            sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                            sb.append("\n\t").append("public void set").append(formatted).append("(Object v) {");
                            sb.append("\n\t\tif (v instanceof "+ project + ".bean.").append(foreignClassName).append(")");
                            sb.append("\n\t\t\t_").append(formatted + " =("+ project + ".bean.").append(foreignClassName).append(")v;");
                            sb.append("\n\t\telse");
                            sb.append("\n\t\t\tthrow new IllegalArgumentException(\"Must be a valid "+ project + ".bean.").append(foreignClassName).append("\");");
                            sb.append("\n\t}");

                            sb.append("\n");
                            sb.append("\n\t/**");
                            sb.append("\n\t * " + xmlPath);
                            sb.append("\n\t * @return ").append(INTERFACE_PACKAGE +".").append(foreignInterface).append("\n\t */");
                            sb.append("\n\t").append("public <A extends ").append(INTERFACE_PACKAGE +".").append(foreignInterface).append("> void set").append(formatted).append("(A item) throws Exception{");
                            sb.append("\n\t").append("set").append(formatted).append("(("+ project + ".bean." + foreignClassName +")item);");
                            sb.append("\n\t}");

                        
                            XFTSuperiorReference ref = (XFTSuperiorReference)f.getXFTReference();
                            Iterator iter=ref.getKeyRelations().iterator();
                            while (iter.hasNext())
                            {
                                XFTRelationSpecification spec = (XFTRelationSpecification) iter.next();
                                String temp = spec.getLocalCol();

                                String type = spec.getSchemaType().getLocalType();
                                if (type==null){
                                    type = "";
                                }
                                
                                else if (type.equalsIgnoreCase("integer"))
                                {
                                    String reformatted = formatted + "FK";
                                    sb.append("\n\n\t").append("//FIELD");
                                    sb.append("\n\n\t").append("private Integer _").append(reformatted);
                                    sb.append("=null;");
                                    
                                    //STANDARD GET METHOD
                                    sb.append("\n\n");
                                    sb.append("\t/**\n\t * @return Returns the ").append(e.getXSIType() + "/" + temp).append(".\n\t */");
                                    sb.append("\n\t").append("public Integer get").append(reformatted).append("(){");
                                    sb.append("\n\t\treturn _" + reformatted +";");
                                    sb.append("\n\t}");
                                    
                                    //STANDARD SET METHOD
                                    sb.append("\n\n");
                                    sb.append("\t/**\n\t * Sets the value for ").append(e.getXSIType() + "/" + temp).append(".\n\t * @param v Value to Set.\n\t */");
                                    sb.append("\n\t").append("public void set").append(reformatted).append("(Integer v) {");
                                    sb.append("\n\t\t_").append(reformatted +"=v;");
                                    sb.append("\n\t}");
                                }else if (type.equalsIgnoreCase("string"))
                                {
                                    String reformatted = formatted + "_" +formatFieldName(temp);
                                    sb.append("\n\n\t").append("//FIELD");
                                    sb.append("\n\n\t").append("private String _").append(reformatted);
                                    sb.append("=null;");
                                    
                                    //STANDARD GET METHOD
                                    sb.append("\n\n");
                                    sb.append("\t/**\n\t * @return Returns the ").append(e.getXSIType() + "/" + temp).append(".\n\t */");
                                    sb.append("\n\t").append("public String get").append(reformatted).append("(){");
                                    sb.append("\n\t\treturn _" + reformatted +";");
                                    sb.append("\n\t}");
                                    
                                    //STANDARD SET METHOD
                                    sb.append("\n\n");
                                    sb.append("\t/**\n\t * Sets the value for ").append(e.getXSIType() + "/" + temp).append(".\n\t * @param v Value to Set.\n\t */");
                                    sb.append("\n\t").append("public void set").append(reformatted).append("(String v){");
                                    sb.append("\n\t\t_").append(reformatted +"=v;");
                                    sb.append("\n\t}");
                                }else if (type.equalsIgnoreCase("date") || type.equalsIgnoreCase("dateTime") ||type.equalsIgnoreCase("timestamp"))
                                {
                                    String reformatted = formatted + "_" +formatFieldName(temp);
                                    sb.append("\n\n\t").append("//FIELD");
                                    sb.append("\n\n\t").append("private Date _").append(reformatted);
                                    sb.append("=null;");
                                    
                                    //STANDARD GET METHOD
                                    sb.append("\n\n");
                                    sb.append("\t/**\n\t * @return Returns the ").append(e.getXSIType() + "/" + temp).append(".\n\t */");
                                    sb.append("\n\t").append("public Date get").append(reformatted).append("(){");
                                    sb.append("\n\t\treturn _" + reformatted +";");
                                    sb.append("\n\t}");
                                    
                                    //STANDARD SET METHOD
                                    sb.append("\n\n");
                                    sb.append("\t/**\n\t * Sets the value for ").append(e.getXSIType() + "/" + temp).append(".\n\t * @param v Value to Set.\n\t */");
                                    sb.append("\n\t").append("public void set").append(reformatted).append("(Date v){");
                                    sb.append("\n\t\t_").append(reformatted +"=v;");
                                    sb.append("\n\t}");
                                }else{
                                    String reformatted = formatted + "_" +formatFieldName(temp);
                                    sb.append("\n\n\t").append("//FIELD");
                                    sb.append("\n\n\t").append("private Object _").append(reformatted);
                                    sb.append("=null;");
                                    
                                    //STANDARD GET METHOD
                                    sb.append("\n\n");
                                    sb.append("\t/**\n\t * @return Returns the ").append(e.getXSIType() + "/" + temp).append(".\n\t */");
                                    sb.append("\n\t").append("public Object get").append(reformatted).append("(){");
                                    sb.append("\n\t\treturn _" + reformatted +";");
                                    sb.append("\n\t}");
                                    
                                    //STANDARD SET METHOD
                                    sb.append("\n\n");
                                    sb.append("\t/**\n\t * Sets the value for ").append(e.getXSIType() + "/" + temp).append(".\n\t * @param v Value to Set.\n\t */");
                                    sb.append("\n\t").append("public void set").append(reformatted).append("(Object v){");
                                    sb.append("\n\t\t_").append(reformatted +"=v;");
                                    sb.append("\n\t}");
                                }
                            }
                        }
                    }
                }
            }else{
                String type = f.getXMLType().getLocalType();
                if (type != null)
                {
                    String xmlPath = f.getXMLPathString();
                    String formatted = formatFieldName(xmlPath);
                    if (formatted.equalsIgnoreCase("USER"))
                    {
                        formatted = "userProperty";
                    }

                    xmlPaths.add(xmlPath);
                    if (f.getXMLType().getLocalType().equals("string"))
                    {
                        if (f.getSize().equals("")){
                            types.add(TYPE.data);
                        }else{
                            int size = Integer.valueOf(f.getSize()).intValue();
                            if (size > 256)
                            {
                                types.add(TYPE.LONG_DATA);
                            }else{
                                types.add(TYPE.data);
                            }
                        }
                    }else{
                        types.add(TYPE.data);
                    }
                    methods.add("set" +formatted);
                    gets.add("get" +formatted);
                    foreignElements.add("");
                                        
                    if (type.equalsIgnoreCase("boolean"))
                    {
                        sb.append("\n\n\t").append("//FIELD");
                        sb.append("\n\n\t").append("private Boolean _").append(formatted);
                        sb.append("=null;");
                        
//                      STANDARD GET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sb.append("\n\t").append("public Boolean get").append(formatted).append("() {");
                        sb.append("\n\t\treturn _" + formatted +";");
                        sb.append("\n\t}");

//                        //STANDARD SET METHOD
//                        sb.append("\n\n");
//                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
//                        sb.append("\n\t").append("public void set").append(formatted).append("(Boolean v){");
//                        sb.append("\n\t\t_").append(formatted +"=v;");
//                        sb.append("\n\t}");

                        //STANDARD SET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(Object v){");
                        sb.append("\n\t\tif(v instanceof Boolean){");
                        sb.append("\n\t\t\t_").append(formatted +"=(Boolean)v;");
                        sb.append("\n\t\t}else if(v instanceof String){");
                        sb.append("\n\t\t\t_").append(formatted +"=formatBoolean((String)v);");
                        sb.append("\n\t\t}else if(v!=null){");
                        sb.append("\n\t\t\tthrow new IllegalArgumentException();");
                        sb.append("\n\t\t}");
                        sb.append("\n\t}");
                        


//                        //XML SET METHOD
//                        sb.append("\n\n");
//                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
//                        sb.append("\n\t").append("public void set").append(formatted).append("(String v) {");
//                        sb.append("\n\t\t_").append(formatted +"=formatBoolean(v);");
//                        sb.append("\n\t}");
                     }else if (type.equalsIgnoreCase("integer"))
                    {
                        sb.append("\n\n\t").append("//FIELD");
                        sb.append("\n\n\t").append("private Integer _").append(formatted);
                        sb.append("=null;");
                        
//                      STANDARD GET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sb.append("\n\t").append("public Integer get").append(formatted).append("(){");
                        sb.append("\n\t\treturn _" + formatted +";");
                        sb.append("\n\t}");
                        
                        //STANDARD SET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(e.getXSIType() + "/" + xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(Integer v) {");
                        sb.append("\n\t\t_").append(formatted +"=v;");
                        sb.append("\n\t}");

                        
                        //STANDARD SET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(e.getXSIType() + "/" + xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(String v)  {");
                        sb.append("\n\t\t_").append(formatted +"=formatInteger(v);");
                        sb.append("\n\t}");
                     }else if(type.equalsIgnoreCase("double") || type.equalsIgnoreCase("float")){
                        sb.append("\n\n\t").append("//FIELD");
                        sb.append("\n\n\t").append("private Double _").append(formatted);
                        sb.append("=null;");
                        
                        //STANDARD GET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sb.append("\n\t").append("public Double get").append(formatted).append("() {");
                        sb.append("\n\t\treturn _" + formatted +";");
                        sb.append("\n\t}");
                        
                        //STANDARD SET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(Double v){");
                        sb.append("\n\t\t_").append(formatted +"=v;");
                        sb.append("\n\t}");
                        
                        //STANDARD SET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(String v)  {");
                        sb.append("\n\t\t_").append(formatted +"=formatDouble(v);");
                        sb.append("\n\t}");
                    }else if (type.equalsIgnoreCase("string"))
                    {
                        sb.append("\n\n\t").append("//FIELD");
                        sb.append("\n\n\t").append("private String _").append(formatted);
                        sb.append("=null;");
                        
                        //STANDARD GET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sb.append("\n\t").append("public String get").append(formatted).append("(){");
                        sb.append("\n\t\treturn _" + formatted +";");
                        sb.append("\n\t}");

                        //STANDARD SET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(String v){");
                        sb.append("\n\t\t_").append(formatted +"=v;");
                        sb.append("\n\t}");
                    }else if (type.equalsIgnoreCase("date") || type.equalsIgnoreCase("dateTime"))
                    {
                        sb.append("\n\n\t").append("//FIELD");
                        sb.append("\n\n\t").append("private Date _").append(formatted);
                        sb.append("=null;");
                        
                        //STANDARD GET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sb.append("\n\t").append("public Date get").append(formatted).append("(){");
                        sb.append("\n\t\treturn _" + formatted +";");
                        sb.append("\n\t}");

                        //STANDARD SET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(Date v){");
                        sb.append("\n\t\t_").append(formatted +"=v;");
                        sb.append("\n\t}");
                        
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(Object v){");
                        sb.append("\n\t\tthrow new IllegalArgumentException();");
                        sb.append("\n\t}");
                        
                        if(type.equalsIgnoreCase("date")){
                            sb.append("\n\n");
                            sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                            sb.append("\n\t").append("public void set").append(formatted).append("(String v)  {");
                            sb.append("\n\t\t_").append(formatted +"=formatDate(v);");
                            sb.append("\n\t}");
                        }else if(type.equalsIgnoreCase("dateTime")){
                            sb.append("\n\n");
                            sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                            sb.append("\n\t").append("public void set").append(formatted).append("(String v)  {");
                            sb.append("\n\t\t_").append(formatted +"=formatDateTime(v);");
                            sb.append("\n\t}");
                        }else if(type.equalsIgnoreCase("time")){
                            sb.append("\n\n");
                            sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                            sb.append("\n\t").append("public void set").append(formatted).append("(String v)  {");
                            sb.append("\n\t\t_").append(formatted +"=formatTime(v);");
                            sb.append("\n\t}");
                        }else if(type.equalsIgnoreCase("timestamp")){
                            sb.append("\n\n");
                            sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                            sb.append("\n\t").append("public void set").append(formatted).append("(String v)  {");
                            sb.append("\n\t\t_").append(formatted +"=formatDateTime(v);");
                            sb.append("\n\t}");
                        }
                    }else{
                        sb.append("\n\n\t").append("//FIELD");
                        sb.append("\n\n\t").append("private Object _").append(formatted);
                        sb.append("=null;");
                        
                        //STANDARD GET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sb.append("\n\t").append("public Object get").append(formatted).append("(){");
                        sb.append("\n\t\treturn _" + formatted +";");
                        sb.append("\n\t}");
                        
                        //STANDARD SET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(Object v){");
                        sb.append("\n\t\t_").append(formatted +"=v;");
                        sb.append("\n\t}");
                    }
                }
                }
        }
        
        if (!e.containsStatedKey()){
            GenericWrapperField f = (GenericWrapperField)e.getDefaultKey();
            String xmlPath = f.getXMLPathString();
            String formatted = formatFieldName(xmlPath);
            if (formatted.equalsIgnoreCase("USER"))
            {
                formatted = "userProperty";
            }
            sb.append("\n\n\t").append("//FIELD");
            sb.append("\n\n\t").append("private Integer _").append(formatted);
            sb.append("=null;");
            
            //STANDARD GET METHOD
            sb.append("\n\n");
            sb.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
            sb.append("\n\t").append("public Integer get").append(formatted).append("() {");
            sb.append("\n\t\treturn _" + formatted +";");
            sb.append("\n\t}");

            //STANDARD SET METHOD
            sb.append("\n\n");
            sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
            sb.append("\n\t").append("public void set").append(formatted).append("(Integer v){");
            sb.append("\n\t\t_").append(formatted +"=v;");
            sb.append("\n\t}");

        }

        //SET DATA FIELD
            sb.append("\n\n");
            sb.append("\t/**\n\t * Sets the value for a field via the XMLPATH.\n\t * @param v Value to Set.\n\t */");
            sb.append("\n\t").append("public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{");

            if (xmlPaths.size()>0)
            {
                int count = 0;
                for (int i=0; i<xmlPaths.size();i++)
                {
                    String xmlPath = xmlPaths.get(i);
                    TYPE type = types.get(i);
                    String method = methods.get(i);
                    
                    if (type.equals(TYPE.data) || type.equals(TYPE.LONG_DATA)){
                        if (count==0){
                            sb.append("\n\t\t").append("if (xmlPath.equals(\"").append(xmlPath).append("\")){");
                        }else{
                            sb.append("\n\t\t").append("}else if (xmlPath.equals(\"").append(xmlPath).append("\")){");
                        }
                        sb.append("\n\t\t\t").append(method).append("(v);");
                        count++;
                    }
                }
                
                if (count>0)
                {
                    sb.append("\n\t\t").append("}");
                    sb.append("\n\t\t").append("else{");
                     sb.append("\n\t\t\t").append("super.setDataField(xmlPath,v);");

                    sb.append("\n\t\t").append("}");
                }else{
                    sb.append("\n\t\t\t").append("super.setDataField(xmlPath,v);");

                }
            }else{
                sb.append("\n\t\t").append("super.setDataField(xmlPath,v);");
            }
            sb.append("\n\t}");


            
            
            //SET REFERENCE FIELD
            sb.append("\n\n");
            sb.append("\t/**\n\t * Sets the value for a field via the XMLPATH.\n\t * @param v Value to Set.\n\t */");
            sb.append("\n\t").append("public void setReferenceField(String xmlPath,BaseElement v) throws BaseElement.UnknownFieldException{");

            if (xmlPaths.size()>0)
            {
                int count = 0;
                for (int i=0; i<xmlPaths.size();i++)
                {
                    String xmlPath = xmlPaths.get(i);
                    TYPE type = types.get(i);
                    String method = methods.get(i);
                    
                    if (!type.equals(TYPE.data) && !type.equals(TYPE.LONG_DATA)){
                        if (count==0){
                            sb.append("\n\t\t").append("if (xmlPath.equals(\"").append(xmlPath).append("\")){");
                        }else{
                            sb.append("\n\t\t").append("}else if (xmlPath.equals(\"").append(xmlPath).append("\")){");
                        }
                        sb.append("\n\t\t\t").append(method).append("(v);");
                        count++;
                    }
                }
                
                if (count>0)
                {
                    sb.append("\n\t\t").append("}");
                    sb.append("\n\t\t").append("else{");
                    sb.append("\n\t\t\t").append("super.setReferenceField(xmlPath,v);");

                    sb.append("\n\t\t").append("}");
                }else{
                    sb.append("\n\t\t\t").append("super.setReferenceField(xmlPath,v);");

                }
            }else{
                sb.append("\n\t\t").append("super.setReferenceField(xmlPath,v);");
            }
            sb.append("\n\t}");
        



            //SET DATA FIELD
                sb.append("\n\n");
                sb.append("\t/**\n\t * Gets the value for a field via the XMLPATH.\n\t * @param v Value to Set.\n\t */");
                sb.append("\n\t").append("public Object getDataFieldValue(String xmlPath) throws BaseElement.UnknownFieldException{");

                if (xmlPaths.size()>0)
                {
                    int count = 0;
                    for (int i=0; i<xmlPaths.size();i++)
                    {
                        String xmlPath = xmlPaths.get(i);
                        TYPE type = types.get(i);
                        String method = gets.get(i);
                        
                        if (type.equals(TYPE.data) || type.equals(TYPE.LONG_DATA)){
                            if (count==0){
                                sb.append("\n\t\t").append("if (xmlPath.equals(\"").append(xmlPath).append("\")){");
                            }else{
                                sb.append("\n\t\t").append("}else if (xmlPath.equals(\"").append(xmlPath).append("\")){");
                            }
                            sb.append("\n\t\t\treturn ").append(method).append("();");
                            count++;
                        }
                    }
                    
                    if (count>0)
                    {
                        sb.append("\n\t\t").append("}");
                        sb.append("\n\t\t").append("else{");
                         sb.append("\n\t\t\t").append("return super.getDataFieldValue(xmlPath);");

                        sb.append("\n\t\t").append("}");
                    }else{
                        sb.append("\n\t\t\t").append("return super.getDataFieldValue(xmlPath);");

                    }
                }else{
                    sb.append("\n\t\t").append("return super.getDataFieldValue(xmlPath);");
                }
                sb.append("\n\t}");
                
            //SET REFERENCE FIELD
            sb.append("\n\n");
            sb.append("\t/**\n\t * Gets the value for a field via the XMLPATH.\n\t * @param v Value to Set.\n\t */");
            sb.append("\n\t").append("public Object getReferenceField(String xmlPath) throws BaseElement.UnknownFieldException{");

            if (xmlPaths.size()>0)
            {
                int count = 0;
                for (int i=0; i<xmlPaths.size();i++)
                {
                    String xmlPath = xmlPaths.get(i);
                    TYPE type = types.get(i);
                    String method = gets.get(i);
                    
                    if (!type.equals(TYPE.data) && !type.equals(TYPE.LONG_DATA)){
                        if (count==0){
                            sb.append("\n\t\t").append("if (xmlPath.equals(\"").append(xmlPath).append("\")){");
                        }else{
                            sb.append("\n\t\t").append("}else if (xmlPath.equals(\"").append(xmlPath).append("\")){");
                        }
                        sb.append("\n\t\t\treturn ").append(method).append("();");
                        count++;
                    }
                }
                
                if (count>0)
                {
                    sb.append("\n\t\t").append("}");
                    sb.append("\n\t\t").append("else{");
                    sb.append("\n\t\t\t").append("return super.getReferenceField(xmlPath);");

                    sb.append("\n\t\t").append("}");
                }else{
                    sb.append("\n\t\t\t").append("return super.getReferenceField(xmlPath);");

                }
            }else{
                sb.append("\n\t\t").append("return super.getReferenceField(xmlPath);");
            }
            sb.append("\n\t}");

            
            //GET REFERENCE FIELD ELEMENT NAME
            sb.append("\n\n");
            sb.append("\t/**\n\t * Gets the value for a field via the XMLPATH.\n\t * @param v Value to Set.\n\t */");
            sb.append("\n\t").append("public String getReferenceFieldName(String xmlPath) throws BaseElement.UnknownFieldException{");

            if (xmlPaths.size()>0)
            {
                int count = 0;
                for (int i=0; i<xmlPaths.size();i++)
                {
                    String xmlPath = xmlPaths.get(i);
                    TYPE type = types.get(i);
                    String method = methods.get(i);
                    String foreign = foreignElements.get(i);
                    
                    if (!type.equals(TYPE.data) && !type.equals(TYPE.LONG_DATA)){
                        if (count==0){
                            sb.append("\n\t\t").append("if (xmlPath.equals(\"").append(xmlPath).append("\")){");
                        }else{
                            sb.append("\n\t\t").append("}else if (xmlPath.equals(\"").append(xmlPath).append("\")){");
                        }
                        sb.append("\n\t\t\treturn \"" + foreign + "\";");
                        count++;
                    }
                }
                
                if (count>0)
                {
                    sb.append("\n\t\t").append("}");
                    sb.append("\n\t\t").append("else{");
                    sb.append("\n\t\t\t").append("return super.getReferenceFieldName(xmlPath);");

                    sb.append("\n\t\t").append("}");
                }else{
                    sb.append("\n\t\t\t").append("return super.getReferenceFieldName(xmlPath);");

                }
            }else{
                sb.append("\n\t\t").append("return super.getReferenceFieldName(xmlPath);");
            }
            sb.append("\n\t}");
            
            
            //GET FIELD TYPE
            sb.append("\n\n");
            sb.append("\t/**\n\t * Returns whether or not this is a reference field\n\t */");
            sb.append("\n\t").append("public String getFieldType(String xmlPath) throws BaseElement.UnknownFieldException{");

            if (xmlPaths.size()>0)
            {
                int count = 0;
                for (int i=0; i<xmlPaths.size();i++)
                {
                    String xmlPath = xmlPaths.get(i);
                    TYPE type = types.get(i);
                    String method = methods.get(i);

                    if (i==0){
                        sb.append("\n\t\t").append("if (xmlPath.equals(\"").append(xmlPath).append("\")){");
                    }else{
                        sb.append("\n\t\t").append("}else if (xmlPath.equals(\"").append(xmlPath).append("\")){");
                    }
                    if (type.equals(TYPE.data)){
                        sb.append("\n\t\t\treturn BaseElement.field_data;");
                    }else if (type.equals(TYPE.multi_reference)){
                        sb.append("\n\t\t\treturn BaseElement.field_multi_reference;");
                    }else if (type.equals(TYPE.single_reference)){
                        sb.append("\n\t\t\treturn BaseElement.field_single_reference;");
                    }else if (type.equals(TYPE.inline_repeater)){
                        sb.append("\n\t\t\treturn BaseElement.field_inline_repeater;");
                    }else if (type.equals(TYPE.LONG_DATA)){
                        sb.append("\n\t\t\treturn BaseElement.field_LONG_DATA;");
                    }else if (type.equals(TYPE.NO_CHILD)){
                        sb.append("\n\t\t\treturn BaseElement.field_NO_CHILD;");
                    }
                }
                
                    sb.append("\n\t\t").append("}");
                    sb.append("\n\t\t").append("else{");
                        sb.append("\n\t\t\t").append("return super.getFieldType(xmlPath);");
                    sb.append("\n\t\t").append("}");
            }else{
                sb.append("\n\t\t").append("return super.getFieldType(xmlPath);");
            }
            sb.append("\n\t}");
        
            

            
            //GET All FIELDs
            sb.append("\n\n");
            sb.append("\t/**\n\t * Returns arraylist of all fields\n\t */");
            sb.append("\n\t").append("public ArrayList getAllFields() {");
            sb.append("\n\t\t").append("ArrayList all_fields=new ArrayList();");

            if (xmlPaths.size()>0)
            {
                int count = 0;
                for (int i=0; i<xmlPaths.size();i++)
                {
                    String xmlPath = xmlPaths.get(i);
                    TYPE type = types.get(i);
                    String method = methods.get(i);

                    if (i==0){
                        sb.append("\n\t\t").append("all_fields.add(\"").append(xmlPath).append("\");");
                    }else{
                        sb.append("\n\t\t").append("all_fields.add(\"").append(xmlPath).append("\");");
                    }
                   
                }
                
                sb.append("\n\t\t").append("all_fields.addAll(super.getAllFields());");
                sb.append("\n\t\t").append("return all_fields;");
               
            }else{
                sb.append("\n\t\t").append("return super.getAllFields();");
            }
            sb.append("\n\t}");
        
        //WRITE XML
        sb.append("\n\n");
        sb.append("\n\t").append("public String toString(){");
        sb.append("\n\t\t").append("java.io.StringWriter sw = new java.io.StringWriter();");
        sb.append("\n\t\t").append("try{this.toXML(sw,true);}catch(java.io.IOException e){}");
        sb.append("\n\t\t").append("return sw.toString();");
        sb.append("\n\t").append("}");

        
        XMLWrapperElement xE = (XMLWrapperElement)XMLWrapperFactory.GetInstance().convertElement(e);
        
        //WRITE XML - WRITER
        sb.append("\n\n");
        sb.append("\n\t").append("public void toXML(java.io.Writer writer,boolean prettyPrint) throws java.io.IOException{");
        sb.append("\n\t\t").append("writer.write(\"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>\");");
        sb.append("\n\t\t").append("writer.write(\"\\n<").append(getRootName(xE)).append("\");");
        sb.append("\n\t\t").append("TreeMap map = new TreeMap();");
        sb.append("\n\t\t").append("map.putAll(getXMLAtts());");
        Map map = this.addXMLNSAttributes();
        Iterator iter = map.keySet().iterator();
        while(iter.hasNext()){
            String key = (String)iter.next();
            sb.append("\n\t\t").append("map.put(\"" + key +"\",\"" + map.get(key) +"\");");
        }
        
        sb.append("\n\t\t").append("java.util.Iterator iter =map.keySet().iterator();");
        sb.append("\n\t\t").append("while(iter.hasNext()){");
        sb.append("\n\t\t\t").append("String key = (String)iter.next();");
        sb.append("\n\t\t\twriter.write(\" \" + key + \"=\\\"\" + map.get(key) + \"\\\"\");");
        sb.append("\n\t\t").append("}");
        
        sb.append("\n\t\t").append("int header = 0;");
        sb.append("\n\t\t").append("if (prettyPrint)header++;");
        sb.append("\n\t\t").append("writer.write(\">\");");

        sb.append("\n\t\t").append("addXMLBody(writer,header);");

        sb.append("\n\t\t").append("if (prettyPrint)header--;");
        sb.append("\n\t\t").append("writer.write(\"\\n</").append(getRootName(xE)).append(">\");");
        sb.append("\n\t").append("}");
        

        //WRITE XML Attributes - WRITER
        sb.append("\n\n");
        sb.append("\n\t").append("protected void addXMLAtts(java.io.Writer writer) throws java.io.IOException{");
        Object[] attributesArray = xE.getAttributes().toArray();
        sb.append("\n\t\t").append("TreeMap map = this.getXMLAtts();");

        sb.append("\n\t\t").append("java.util.Iterator iter =map.keySet().iterator();");
        sb.append("\n\t\t").append("while(iter.hasNext()){");
        sb.append("\n\t\t\t").append("String key = (String)iter.next();");
        sb.append("\n\t\t\twriter.write(\" \" + key + \"=\\\"\" + map.get(key) + \"\\\"\");");
        sb.append("\n\t\t").append("}");
       
        sb.append("\n\t").append("}");
        

        //WRITE XML Attributes - WRITER
        sb.append("\n\n");
        sb.append("\n\t").append("protected TreeMap getXMLAtts() {");
        attributesArray = xE.getAttributes().toArray();
        sb.append("\n\t\t").append("TreeMap map = super.getXMLAtts();");
        for (int i=0;i<attributesArray.length;i++){
            XMLWrapperField attField = (XMLWrapperField)attributesArray[i];
            if (attField.isReference())
            {
//                    XFTItem ref = (XFTItem)child.getProperty(attField.getId());
//                    if (ref != null)
//                    {
//                        if((!limited) || (!ref.canBeRootWithBase()))
//                        {
//                            addAttributes(ref,atts);
//                        }
//                    }   
            }else
            {
                String fieldID = attField.getId();
                String xmlPath = attField.getXMLPathString();
                String formatted = formatFieldName(xmlPath);
                sb.append("\n\t\tif (_").append(formatted).append("!=null)");
                sb.append("\n\t\t\tmap.put(\"").append(attField.getName(false)).append("\",ValueParser(_").append(formatted).append(",\"" + attField.getXMLType().getLocalType() + "\"));");
                if (attField.isRequired()){
                    sb.append("\n\t\telse map.put(\"").append(attField.getName(false)).append("\",\"\");//REQUIRED FIELD\n");
                }else{
                    sb.append("\n\t\t//NOT REQUIRED FIELD\n");
                }
            }
        }
        sb.append("\n\t\t").append("return map;");
        sb.append("\n\t").append("}");
        
        //WRITE XML Body tags - WRITER
        sb.append("\n\n");
        sb.append("\n\t").append("protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{");

        sb.append("\n\t\t").append("super.addXMLBody(writer,header);");
        int iterCounter =0;
        Iterator childElements = xE.getChildren().iterator();
        while(childElements.hasNext())
        {
            XMLWrapperField xmlField = (XMLWrapperField)childElements.next();
            iterCounter =writeFieldToXMLCode(e, xmlField, iterCounter, sb);
        }
        
        if (e.isANoChildElement())
            sb.append("\n\t").append("return false;");
        else
            sb.append("\n\t").append("return true;");
        
        sb.append("\n\t").append("}");
        


        

        //WRITE HAS XML CONTENT
        sb.append("\n\n");
        sb.append("\n\t").append("protected boolean hasXMLBodyContent(){");

        boolean returned =false;
        childElements = xE.getChildren().iterator();
        while(childElements.hasNext() && !returned)
        {
            XMLWrapperField xmlField = (XMLWrapperField)childElements.next();
            if (xmlField.getExpose())
            {
                String xmlPath = xmlField.getXMLPathString();
                String formatted = formatFieldName(xmlPath);
                if (formatted.equalsIgnoreCase("USER"))
                {
                    formatted = "userProperty";
                }
                if (xmlField.isReference())
                {
                    try {
                        XMLWrapperElement foreign = (XMLWrapperElement)xmlField.getReferenceElement();
                        String foreignClassName = getFormattedBean(foreign.getGenericXFTElement());
                        if (xmlField.isMultiple())
                        { 
                            sb.append("\n\t\t").append("if(_" + formatted + ".size()>0) return true;");
                        }else
                        {
                            if (!e.getExtensionFieldName().equalsIgnoreCase(xmlField.getName())){
                                sb.append("\n\t\t").append("if (_" + formatted + "!=null){");
                                sb.append("\n\t\t\t").append("if (_" + formatted + ".hasXMLBodyContent()) return true;");
                                sb.append("\n\t\t").append("}");
                                if (! xmlField.getWrapped().getMinOccurs().equalsIgnoreCase("0")){
                                    sb.append("\n\t\t").append("return true;//REQUIRED " + xmlField.getXMLPathString());
                                    returned=true;
                                }else{
                                    sb.append("\n\t\t").append("//NOT REQUIRED\n");
                                }
                            }
                        }
                    } catch (XFTInitException e1) {
                    } catch (ElementNotFoundException e1) {
                    }
                }else{
                    //NOT A REFERENCE
                                        
                    ArrayList attAL = xmlField.getAttributes();
                    if (attAL.size() > 0)
                    {   
                        Iterator attributes = xmlField.getAttributes().iterator();
                        while (attributes.hasNext())
                        {
                            XMLWrapperField attField = (XMLWrapperField)attributes.next();
                            String fieldID = attField.getId();
                            String xmlPathATT = attField.getXMLPathString();
                            String formattedATT = formatFieldName(xmlPathATT);
                            sb.append("\n\t\tif (_").append(formattedATT).append("!=null)");
                            sb.append("\n\t\t\treturn true;");
                            if (attField.isRequired()){
                                sb.append("\n\t\t").append("return true;//REQUIRED " + attField.getXMLPathString());
                                returned=true;
                                break;
                            }
                        }
                    }
                        
                    if (!returned)
                    {
                        if (xmlField.getChildren().size() > 0)
                        {          
                            for (Map.Entry<String,String> childVariable : this.getSubVariables(e, xmlField,true).entrySet()){
                                if (childVariable.getValue().equals("MULTI"))
                                {
                                    sb.append("\n\t\t\t").append("if(" + childVariable.getKey() + ".size()>0)return true;");
                                }else{
                                    sb.append("\n\t\t\t").append("if(" + childVariable.getKey() + "!=null) return true;");
                                }
                            }
                        }else
                        {

                            if (xmlField.getXMLType()==null){
                            }else
                            {
                                sb.append("\n\t\t").append("if (_" + formatted + "!=null) return true;");
                                if (xmlField.isRequired())
                                {
                                    sb.append("\n\t\t").append("return true;//REQUIRED " + xmlField.getXMLPathString());
                                    returned=true;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!returned){
            sb.append("\n\t\t").append("if(super.hasXMLBodyContent())return true;");

            sb.append("\n\t\t").append("return false;");
        }
        sb.append("\n\t").append("}");
        

        
        
        sb.append("\n");
        sb.append("}");
        
        if (!location.endsWith(File.separator))
        {
            location += File.separator;
        }
        
        File dir = new File(location);
        if (!dir.exists())
        {
            dir.mkdir();
        }
        
        String dirStucture = packageName;
        String finalLocation = location + File.separator + StringUtils.replace(dirStucture, ".", File.separator);
        while (dirStucture.indexOf(".")!=-1)
        {
            String folder = dirStucture.substring(0,dirStucture.indexOf("."));
            dirStucture = dirStucture.substring(dirStucture.indexOf(".")+1);
            
            location = location + folder + File.separator;
            dir = new File(location);
            if (!dir.exists())
            {
                dir.mkdir();
            }
        }
        
        location = location + dirStucture + File.separator;
        dir = new File(location);
        if (!dir.exists())
        {
            dir.mkdir();
        }

        if (XFT.VERBOSE)
             System.out.println("Generating File " + location +getFormattedBean(e) +".java...");
        FileUtils.OutputToFile(sb.toString(),location +getFormattedBean(e) + ".java");
        
    }
    
    public Hashtable<String,String> getSubVariables(GenericWrapperElement e,XMLWrapperField xmlField, boolean isRoot)
    {
        Hashtable<String,String> subMethods = new Hashtable<String,String>();

        if (xmlField.getExpose())
        {
            String xmlPath = xmlField.getXMLPathString();
            String formatted = formatFieldName(xmlPath);
            if (formatted.equalsIgnoreCase("USER"))
            {
                formatted = "userProperty";
            }
            if (xmlField.isReference())
            {
                if (xmlField.isMultiple())
                {
                    subMethods.put("_" + formatted,"MULTI");
                }else
                {
                    if (!e.getExtensionFieldName().equalsIgnoreCase(xmlField.getName())){
                        subMethods.put("_" + formatted,"SINGLE");
                    }
                }
            }else{
                //NOT A REFERENCE
                                    
                ArrayList attAL = xmlField.getAttributes();
                if (attAL.size() > 0)
                {   
                    Iterator attributes = xmlField.getAttributes().iterator();
                    while (attributes.hasNext())
                    {
                        XMLWrapperField attField = (XMLWrapperField)attributes.next();
                        String fieldID = attField.getId();
                        String xmlPathATT = attField.getXMLPathString();
                        String formattedATT = formatFieldName(xmlPathATT);
                        if (isRoot)
                            subMethods.put("_" + formattedATT,"ATT");
                        else
                            subMethods.put("_" + formattedATT,"SINGLE");
                    }
                }
                    
                if (xmlField.getChildren().size() > 0)
                {          
                    Iterator childElements2 = xmlField.getChildren().iterator();
                    while(childElements2.hasNext())
                    {
                        XMLWrapperField xwf = (XMLWrapperField)childElements2.next();
                        if (xwf.getExpose())
                        {
                           subMethods.putAll(getSubVariables(e, xwf,false));
                        }
                    }
                }else
                {
                    if (xmlField.getXMLType()==null){
                    }else
                    {
                        subMethods.put("_" + formatted,"SINGLE");
                    }
                }
            }
        }
        
        return subMethods;
    }

        
    private int writeFieldToXMLCode(GenericWrapperElement e,XMLWrapperField xmlField,int iterCounter, StringBuffer sb)
    {
        if (xmlField.getExpose())
        {
            String xmlPath = xmlField.getXMLPathString();
            String formatted = formatFieldName(xmlPath);
            if (formatted.equalsIgnoreCase("USER"))
            {
                formatted = "userProperty";
            }
            if (xmlField.isReference())
            {
                sb.append("\n\t\t").append("//REFERENCE FROM " + e.getXMLName() + " -> " + xmlField.getXMLPathString());
                try {
                    XMLWrapperElement foreign = (XMLWrapperElement)xmlField.getReferenceElement();
                    String foreignClassName = getFormattedBean(foreign.getGenericXFTElement());
                    if (xmlField.isMultiple())
                    { 
                        if (xmlField.isInLineRepeaterElement())
                        {
                            sb.append("\n\t\t").append("//IN-LINE REPEATER");
                            
                            sb.append("\n\t\t").append("java.util.Iterator iter" + iterCounter + "=_" + formatted + ".iterator();");
                            sb.append("\n\t\t").append("while(iter" + iterCounter + ".hasNext()){");
                            sb.append("\n\t\t\t").append(project + ".bean.").append(foreignClassName).append(" child = ("+ project + ".bean.").append(foreignClassName).append(")iter" + iterCounter++ + ".next();");
                            sb.append("\n\t\t\t").append("child.addXMLBody(writer,header);");
                            sb.append("\n\t\t").append("}");
                        }else{
                            sb.append("\n\t\t").append("java.util.Iterator iter" + iterCounter + "=_" + formatted + ".iterator();");
                            sb.append("\n\t\t").append("while(iter" + iterCounter + ".hasNext()){");
                            sb.append("\n\t\t\t").append(project + ".bean.").append(foreignClassName).append(" child = ("+ project + ".bean.").append(foreignClassName).append(")iter" + iterCounter++ + ".next();");
                            sb.append("\n\t\t\t").append("writer.write(\"\\n\" + createHeader(header++) + \"<" + xmlField.getName(true) + "\");");
                            sb.append("\n\t\t\t").append("child.addXMLAtts(writer);");
                            sb.append("\n\t\t\t").append("if(!child.getFullSchemaElementName().equals(\"" + xmlField.getXMLType().getFullForeignType() + "\")){");
                            sb.append("\n\t\t\t\t").append("writer.write(\" xsi:type=\\\"\" + child.getFullSchemaElementName() + \"\\\"\");");
                            sb.append("\n\t\t\t").append("}");
                            sb.append("\n\t\t\t").append("if (child.hasXMLBodyContent()){");
                            sb.append("\n\t\t\t\t").append("writer.write(\">\");");
                            sb.append("\n\t\t\t\t").append("boolean return" + iterCounter + " =child.addXMLBody(writer,header);");
                            sb.append("\n\t\t\t\t").append("if(return" + iterCounter + "){");
                            sb.append("\n\t\t\t\t\t").append("writer.write(\"\\n\" + createHeader(--header) + \"</" + xmlField.getName(true) + ">\");");
                            sb.append("\n\t\t\t\t").append("}else{");
                            sb.append("\n\t\t\t\t\t").append("writer.write(\"</" + xmlField.getName(true) + ">\");");
                            sb.append("\n\t\t\t\t\t").append("header--;");
                            sb.append("\n\t\t\t\t").append("}");
                            sb.append("\n\t\t\t").append("}else {writer.write(\"/>\");header--;}");
                            
                                
                            sb.append("\n\t\t").append("}");
                        }
                    }else
                    {
                        if (!e.getExtensionFieldName().equalsIgnoreCase(xmlField.getName())){
                            sb.append("\n\t\t").append("//DATA-FIELD FROM " + e.getXMLName() + " -> " + xmlField.getXMLPathString());
                            sb.append("\n\t\t").append("if (_" + formatted + "!=null){");

                            boolean isNewElement= true;
                            if (! xmlField.isChildXMLNode())
                            {
                                isNewElement= false;
                            }

                            if (! isNewElement){
                                sb.append("\n\t\t").append("//NOT A NEW ELEMENT");
                                sb.append("\n\t\t\t").append("_" + formatted + ".addXMLBody(writer,header);");
                            }else{
                                sb.append("\n\t\t").append("//NEW ELEMENT");
                                sb.append("\n\t\t\t").append("writer.write(\"\\n\" + createHeader(header++) + \"<" + xmlField.getName(true) + "\");");
                                sb.append("\n\t\t\t").append("_" + formatted + ".addXMLAtts(writer);");
                                sb.append("\n\t\t\t").append("if(!_" + formatted + ".getFullSchemaElementName().equals(\"" + xmlField.getXMLType().getFullForeignType() + "\")){");
                                sb.append("\n\t\t\t\t").append("writer.write(\" xsi:type=\\\"\" + _" + formatted + ".getFullSchemaElementName() + \"\\\"\");");
                                sb.append("\n\t\t\t").append("}");

                                sb.append("\n\t\t\t").append("if (_" + formatted + ".hasXMLBodyContent()){");
                                sb.append("\n\t\t\t\t").append("writer.write(\">\");");
                                sb.append("\n\t\t\t\t").append("boolean return" + iterCounter + " =_" + formatted + ".addXMLBody(writer,header);");
                                sb.append("\n\t\t\t\t").append("if(return" + iterCounter + "){");
                                sb.append("\n\t\t\t\t\t").append("writer.write(\"\\n\" + createHeader(--header) + \"</" + xmlField.getName(true) + ">\");");
                                sb.append("\n\t\t\t\t").append("}else{");
                                sb.append("\n\t\t\t\t\t").append("writer.write(\"</" + xmlField.getName(true) + ">\");");
                                sb.append("\n\t\t\t\t\t").append("header--;");
                                sb.append("\n\t\t\t\t").append("}");
                                sb.append("\n\t\t\t").append("}else {writer.write(\"/>\");header--;}");
                            }
                            sb.append("\n\t\t").append("}");
                            if (! xmlField.getWrapped().getMinOccurs().equalsIgnoreCase("0")){
                                sb.append("\n\t\t").append("else{");
                                sb.append("\n\t\t\t").append("writer.write(\"\\n\" + createHeader(header) + \"<" + xmlField.getName(true) + "/>\");//REQUIRED");
                                sb.append("\n\t\t").append("}");
                            }else{
                                sb.append("\n\t\t").append("//NOT REQUIRED\n");
                            }
                        }
                    }
                } catch (XFTInitException e1) {
                } catch (ElementNotFoundException e1) {
                }
            }else{
                //NOT A REFERENCE
                ArrayList attAL = xmlField.getAttributes();
                if (attAL.size() > 0)
                {   
                    sb.append("\n\t\tTreeMap " + formatted + "ATTMap = new TreeMap();");
                    sb.append("\n\t\tString " + formatted + "ATT = new String();");
                    Iterator attributes = xmlField.getAttributes().iterator();
                    while (attributes.hasNext())
                    {
                        XMLWrapperField attField = (XMLWrapperField)attributes.next();
                        String fieldID = attField.getId();
                        String xmlPathATT = attField.getXMLPathString();
                        String formattedATT = formatFieldName(xmlPathATT);
                        sb.append("\n\t\tif (_").append(formattedATT).append("!=null)");
                        sb.append("\n\t\t\t" + formatted + "ATTMap.put(\"").append(attField.getName(false)).append("\",ValueParser(_").append(formattedATT).append(",\"" + attField.getXMLType().getLocalType() + "\"));");
                        if (attField.isRequired()){
                            sb.append("\n\t\telse " +formatted + "ATTMap.put(\"").append(attField.getName(false)).append("\",ValueParser(_").append(formattedATT).append(",\"" + attField.getXMLType().getLocalType() + "\"));//REQUIRED FIELD\n");
                        }
                    }
                    sb.append("\n\t\t").append("java.util.Iterator iter" + iterCounter + " =" + formatted + "ATTMap.keySet().iterator();");
                    sb.append("\n\t\t").append("while(iter" + iterCounter + ".hasNext()){");
                    sb.append("\n\t\t\t").append("String key = (String)iter" + iterCounter++ + ".next();");
                    sb.append("\n\t\t\t" + formatted + "ATT +=\" \" + key + \"=\\\"\" + " + formatted + "ATTMap.get(key) + \"\\\"\";");
                    sb.append("\n\t\t").append("}");
                }
                    
                if (xmlField.getChildren().size() > 0)
                {          
                    sb.append("\n\t\t\t").append("int child"+iterCounter + "=0;");
                    sb.append("\n\t\t\t").append("int att"+iterCounter + "=0;");
                    for (Map.Entry<String,String> childVariable : this.getSubVariables(e, xmlField,true).entrySet()){
                        if (childVariable.getValue().equals("MULTI"))
                        {
                            sb.append("\n\t\t\t").append("child"+iterCounter + "+=" + childVariable.getKey() + ".size();");
                        }else if(childVariable.getValue().equals("ATT")){
                            sb.append("\n\t\t\t").append("if(" + childVariable.getKey() + "!=null)");
                            sb.append("\n\t\t\t").append("att"+iterCounter + "++;");
                        }else{
                            sb.append("\n\t\t\t").append("if(" + childVariable.getKey() + "!=null)");
                            sb.append("\n\t\t\t").append("child"+iterCounter + "++;");
                        }
                    }

                    sb.append("\n\t\t\t").append("if(child"+iterCounter + ">0 || att"+iterCounter + ">0){");

                    int tempCounter = iterCounter;
                    iterCounter++;
                    StringBuffer childCode = new StringBuffer();
                    Iterator childElements2 = xmlField.getChildren().iterator();
                    while(childElements2.hasNext())
                    {
                        XMLWrapperField xwf = (XMLWrapperField)childElements2.next();
                        if (xwf.getExpose())
                        {
                           iterCounter =  writeFieldToXMLCode(e, xwf, iterCounter, childCode);
                        }
                    }
                    
                    sb.append("\n\t\t\t\t").append("writer.write(\"\\n\" + createHeader(header++) + \"<" + xmlField.getName(true) + "\");");
                    if (attAL.size() > 0)
                    {   
                        sb.append("\n\t\t\t\t").append("writer.write(" + formatted + "ATT);");
                    }
                    sb.append("\n\t\t\t").append("if(child"+tempCounter + "==0){");
                    sb.append("\n\t\t\t\t").append("writer.write(\"/>\");");
                    sb.append("\n\t\t\t").append("}else{");

                    sb.append("\n\t\t\t\t").append("writer.write(\">\");");
                    
                    sb.append(childCode);
                    
                    sb.append("\n\t\t\t\t").append("writer.write(\"\\n\" + createHeader(--header) + \"</" + xmlField.getName(true) + ">\");");

                    sb.append("\n\t\t\t").append("}");
                    sb.append("\n\t\t\t").append("}\n");
                }else
                {

                    if (xmlField.getXMLType()==null){
                        sb.append("\n\t\t").append("if(!" + formatted + "ATT.equals(\"\")){");
                        sb.append("\n\t\t\t").append("writer.write(\"\\n\" + createHeader(header) + \"<" + xmlField.getName(true) + "\");");
                        if (attAL.size() > 0)
                        {   
                            sb.append("\n\t\t\t").append("writer.write(" + formatted + "ATT);");
                        }
                        sb.append("\n\t\t\t").append("writer.write(\"/>\");");
                        sb.append("\n\t\t").append("}\n");
                    }else
                    {
                        sb.append("\n\t\t").append("if (_" + formatted + "!=null){");
                        if (!e.isANoChildElement()){
                            sb.append("\n\t\t\t").append("writer.write(\"\\n\" + createHeader(header++) + \"<" + xmlField.getName(true) + "\");");
                            if (attAL.size() > 0)
                            {   
                                sb.append("\n\t\t\t").append("writer.write(" + formatted + "ATT);");
                            }
                            sb.append("\n\t\t\t").append("writer.write(\">\");");
                        }
                        sb.append("\n\t\t\t").append("writer.write(ValueParser(_").append(formatted).append(",\"" + xmlField.getXMLType().getLocalType() + "\"));");
                        if (!e.isANoChildElement())sb.append("\n\t\t\t").append("writer.write(\"</" + xmlField.getName(true) + ">\");");
                        sb.append("\n\t\t\t").append("header--;");
                        sb.append("\n\t\t").append("}");
                        if (xmlField.isRequired())
                        {
                            sb.append("\n\t\t").append("else{");
                            sb.append("\n\t\t\t").append("writer.write(\"\\n\" + createHeader(header++) + \"<" + xmlField.getName(true) + "\");");
                            if (attAL.size() > 0)
                            {   
                                sb.append("\n\t\t\t").append("writer.write(" + formatted + "ATT);");
                            }
                            sb.append("\n\t\t\t").append("writer.write(\"/>\");");
                            sb.append("\n\t\t\t").append("header--;");
                            sb.append("\n\t\t").append("}\n");
                        }else if (attAL.size() > 0){
                            sb.append("\n\t\t").append("else if(!" + formatted + "ATT.equals(\"\")){");
                            sb.append("\n\t\t\t").append("writer.write(\"\\n\" + createHeader(header++) + \"<" + xmlField.getName(true) + "\");");
                            sb.append("\n\t\t\t").append("writer.write(" + formatted + "ATT);");
                            sb.append("\n\t\t\t").append("writer.write(\"/>\");");
                            sb.append("\n\t\t\t").append("header--;");
                            sb.append("\n\t\t").append("}\n");
                        }
                    }
                }
            }
        }
        
        return iterCounter;
    }
    
    public TreeMap addXMLNSAttributes(){
//      ADD SCHEMA SPECIFICATION ATTRIBUTES
        TreeMap map = new TreeMap();
        Enumeration enumer = XFTMetaManager.getPrefixEnum();
        while(enumer.hasMoreElements())
        {
            String prefix = (String)enumer.nextElement();
            String uri = XFTMetaManager.TranslatePrefixToURI(prefix);
            map.put("xmlns:"+prefix,uri);
        }
        map.put("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
        
        return map;
    }
    
    public String getRootName(XMLWrapperElement element)
    {
        String rootName=null;
        String alias = element.getProperName();
        if ((alias != null) && (!alias.equalsIgnoreCase("")))
        {
            if(alias.indexOf(":")!=-1)
            {
                rootName=alias;
            
            }else{
                rootName = element.getSchemaTargetNamespacePrefix() +":" + alias;
            }
        }else
        {
            rootName = element.getFullXMLName();
        }
        
        return rootName;
    }

    
    public static String getFormattedBean(GenericWrapperElement e)
    {
        return XftStringUtils.FormatStringToClassName(e.getFormattedName()) + "Bean";
    }
    
    public static String getFormattedInterface(GenericWrapperElement e)
    {
        return XftStringUtils.FormatStringToClassName(e.getFormattedName()) + "I";
    }
    /**
     * @param s    The file name.
     * @return The formatted field name.
     */
    private String formatFieldName(String s)
    {
        return XftStringUtils.FormatStringToMethodSignature(s);
    }


    /**
     * @return the project
     */
    public String getProject() {
        return project;
    }

    public void generateBaseFile(String location){
        StringBuffer sb = new StringBuffer();
        sb.append("/*\n * GENERATED FILE\n * Created on " + Calendar.getInstance().getTime() + "\n *\n */");

        String packageName = project + ".bean";
        sb.append("\npackage " + packageName + ";");
        sb.append(getBaseTemplate());
        
        File dir = new File(location);
        if (!dir.exists())
        {
            dir.mkdir();
        }
        
        String dirStucture = packageName;
        String finalLocation = location + File.separator + StringUtils.replace(dirStucture, ".", File.separator);
        while (dirStucture.indexOf(".")!=-1)
        {
            String folder = dirStucture.substring(0,dirStucture.indexOf("."));
            dirStucture = dirStucture.substring(dirStucture.indexOf(".")+1);
            
            location = location + folder + File.separator;
            dir = new File(location);
            if (!dir.exists())
            {
                dir.mkdir();
            }
        }
        
        location = location + dirStucture + File.separator;
        dir = new File(location);
        if (!dir.exists())
        {
            dir.mkdir();
        }

        if (XFT.VERBOSE)
             System.out.println("Generating File " + location +"BaseElement.java...");
        FileUtils.OutputToFile(sb.toString(),location +"BaseElement.java");
    }

    /**
     * @param project the project to set
     */
    public void setProject(String project) {
        this.project = project;
    }
    

    public static void GenerateJavaFiles(String name, String javalocation, boolean skipXDAT, String packag, String propsLocation) throws Exception
    {

        JavaBeanGenerator generator = new JavaBeanGenerator();
        generator.setProject(packag);
        List<String> al = XFTMetaManager.GetElementNames();
                
        generator.generateJavaFiles(al, name, javalocation, packag, propsLocation);
    }
    
    public void generateJavaFiles(List<String> elementList, String name, String javalocation, String rootPackage, String propsLocation) throws Exception
    {
        Hashtable<String,String> elements =new Hashtable<String,String>();
    	for (String s: elementList)
        {
            GenericWrapperElement e = GenericWrapperElement.GetElement(s);
            if (e.getAddin().equalsIgnoreCase(""))
            {
                elements.put(e.getSchemaTargetNamespaceURI() + ":" + e.getLocalXMLName(), rootPackage + ".bean." + this.getFormattedBean(e));
                if (!e.getProperName().equals(e.getFullXMLName()))
                {
                    elements.put(e.getSchemaTargetNamespaceURI() + ":" + e.getProperName(), rootPackage + ".bean." + this.getFormattedBean(e));
                }
                this.generateJavaBeanFile(e,javalocation);
                this.generateJavaInterface(e,javalocation);
            }
        }
                
        StringBuilder sb          = new StringBuilder();
        String        packageName = rootPackage + ".bean";
        for (Map.Entry<String, String> entry : elements.entrySet()) {
            sb.append("").append(entry.getKey().replace(":", "\\:")).append("=").append(entry.getValue()).append("\n");
        }
          
        outputToFile(propsLocation,sb.toString(),name+"-bean-definition.properties",packageName);
    }
    
    public static void outputToFile(String location,String contents, String fileName, String packageName){
        File dir = new File(location);
        if (!dir.exists())
        {
            dir.mkdir();
        }
        
        String dirStucture = packageName;
        String finalLocation = location + File.separator + StringUtils.replace(dirStucture, ".", File.separator);
        while (dirStucture.indexOf(".")!=-1)
        {
            String folder = dirStucture.substring(0,dirStucture.indexOf("."));
            dirStucture = dirStucture.substring(dirStucture.indexOf(".")+1);
            
            location = location + folder + File.separator;
            dir = new File(location);
            if (!dir.exists())
            {
                dir.mkdir();
            }
        }
        
        location = location + dirStucture + File.separator;
        dir = new File(location);
        if (!dir.exists())
        {
            dir.mkdir();
        }

        if (XFT.VERBOSE)
             System.out.println("Generating File " + location +fileName);
        FileUtils.OutputToFile(contents,location +fileName);
    }
    
    public String getBaseTemplate(){
        StringBuffer sb =  new StringBuffer();

        sb.append("\n").append("import java.text.DateFormat;");
        sb.append("\n").append("import java.text.ParseException;");
        sb.append("\n").append("import java.text.SimpleDateFormat;");
        sb.append("\n").append("import java.util.*;");
        sb.append("\n\n\n").append("public abstract class BaseElement{");
        sb.append("\n").append("    public final static String field_data=\"DATA\";");
        sb.append("\n").append("public final static String field_single_reference=\"SINGLE\";");
        sb.append("\n").append("public final static String field_multi_reference=\"MULTI\";");
        sb.append("\n").append("public final static String field_inline_repeater=\"INLINE\";");
        sb.append("\n").append("public final static String field_LONG_DATA=\"LONG_DATA\";");
        sb.append("\n").append("public final static String field_NO_CHILD=\"NO_CHILD\";");
        sb.append("\n").append("    public Date formatDate(String s) {");
        sb.append("\n").append("        try {");
        sb.append("\n").append("            return parseDate(s);");
        sb.append("\n").append("        } catch (ParseException e) {");
        sb.append("\n").append("            throw new IllegalArgumentException(e);");
        sb.append("\n").append("        }");
        sb.append("\n").append("    }");
        sb.append("\n").append("    public Date formatDateTime(String s) {");
        sb.append("\n").append("        try {");
        sb.append("\n").append("            return parseDateTime(s);");
        sb.append("\n").append("        } catch (ParseException e) {");
        sb.append("\n").append("            throw new IllegalArgumentException(e);");
        sb.append("\n").append("        }");
        sb.append("\n").append("    }");
        sb.append("\n").append("    public Date formatTime(String s) {");
        sb.append("\n").append("        try {");
        sb.append("\n").append("            return parseTime(s);");
        sb.append("\n").append("        } catch (ParseException e) {");
        sb.append("\n").append("            throw new IllegalArgumentException(e);");
        sb.append("\n").append("        }");
        sb.append("\n").append("    }");
        sb.append("\n").append("    public Double formatDouble(String s) {");
        sb.append("\n").append("       try {");
        sb.append("\n").append("            return Double.valueOf(s);");
        sb.append("\n").append("        } catch (NumberFormatException e) {");
        sb.append("\n").append("            throw new IllegalArgumentException(e);");
        sb.append("\n").append("        }");
        sb.append("\n").append("    }");
        sb.append("\n").append("    public Integer formatInteger(String s) {");
        sb.append("\n").append("        try {");
        sb.append("\n").append("            return Integer.valueOf(s);");
        sb.append("\n").append("        } catch (NumberFormatException e) {");
        sb.append("\n").append("            throw new IllegalArgumentException(e);");
        sb.append("\n").append("        }");
        sb.append("\n").append("    }");
        sb.append("\n").append("    ");
        sb.append("\n").append("    public Boolean formatBoolean(String s) {");
        sb.append("\n").append("        if (s.equals(\"0\") || s.equalsIgnoreCase(\"false\")|| s.equalsIgnoreCase(\"f\"))");
        sb.append("\n").append("        {");
        sb.append("\n").append("            return Boolean.FALSE;");
        sb.append("\n").append("        }else if (s.equals(\"1\") || s.equalsIgnoreCase(\"true\")|| s.equalsIgnoreCase(\"t\"))");
        sb.append("\n").append("       {");
        sb.append("\n").append("            return Boolean.TRUE;");
        sb.append("\n").append("        }");
        sb.append("\n").append("        throw new IllegalArgumentException(\"Unable to translate '\" + s + \"' to a boolean value.\");");
        sb.append("\n").append("    }");
        sb.append("\n").append("    public class UnknownFieldException extends Exception{");
        sb.append("\n").append("        public UnknownFieldException(String s)");
        sb.append("\n").append("        {");
        sb.append("\n").append("            super(s);");
        sb.append("\n").append("        }");
        sb.append("\n").append("        public UnknownFieldException(Exception s)");
        sb.append("\n").append("        {");
        sb.append("\n").append("            super(s);");
        sb.append("\n").append("        }");
        sb.append("\n").append("        public UnknownFieldException(String s,Exception e)");
        sb.append("\n").append("        {");
        sb.append("\n").append("            super(s,e);");
        sb.append("\n").append("       }");
        sb.append("\n").append("    }");
        sb.append("\n").append("    public static Date parseDate(String s) throws ParseException");
        sb.append("\n").append("    {");
        sb.append("\n").append("        if (s.indexOf(\"'\")!= -1)");
        sb.append("\n").append("        {");
        sb.append("\n").append("            s = ReplaceStr(s,\"'\",\"\");");
                sb.append("\n").append("        }");
        sb.append("\n").append("        if (s==null)");
        sb.append("\n").append("        {");
        sb.append("\n").append("            return null;");
        sb.append("\n").append("        }else{");
        sb.append("\n").append("            try {");
        sb.append("\n").append("                return DateFormat.getInstance().parse(s);");
        sb.append("\n").append("            } catch (ParseException e) {");
        sb.append("\n").append("                SimpleDateFormat sdf = new SimpleDateFormat(\"yyyy-MM-dd\", Locale.US);");
        sb.append("\n").append("                try {");
        sb.append("\n").append("                    return sdf.parse(s);");
        sb.append("\n").append("                } catch (ParseException e1) {");
        sb.append("\n").append("                    sdf = new SimpleDateFormat(\"EEE MMM dd HH:mm:ss z yyyy\", Locale.US);");
        sb.append("\n").append("                    try {");
        sb.append("\n").append("                        return sdf.parse(s);");
        sb.append("\n").append("                    } catch (ParseException e2) {");
        sb.append("\n").append("                        sdf = new SimpleDateFormat(\"MM/dd/yyyy\", Locale.US);");
        sb.append("\n").append("                        try {");
        sb.append("\n").append("                            return sdf.parse(s);");
        sb.append("\n").append("                        } catch (ParseException e3) {");
        sb.append("\n").append("                            sdf = new SimpleDateFormat(\"MM.dd.yyyy\", Locale.US);");
        sb.append("\n").append("                            return sdf.parse(s);");
        sb.append("\n").append("                        }");
        sb.append("\n").append("                    }");
        sb.append("\n").append("                }");
        sb.append("\n").append("           }");
        sb.append("\n").append("        }");
        sb.append("\n").append("    }");
        sb.append("\n").append("    public static String ReplaceStr(String _base, String _old, String _new)");
        sb.append("\n").append("    {");
        sb.append("\n").append("        if (_base.indexOf(_old)==-1)");
        sb.append("\n").append("        {");
        sb.append("\n").append("            return _base;");
        sb.append("\n").append("        }else{");
        sb.append("\n").append("            StringBuffer sb = new StringBuffer();");
        sb.append("\n").append("                while(_base.indexOf(_old) != -1)");
        sb.append("\n").append("                {");
        sb.append("\n").append("  ");
        sb.append("\n").append("                    String pre = _base.substring(0,_base.indexOf(_old));");
        sb.append("\n").append("                    String post;");
        sb.append("\n").append("                    try {");
        sb.append("\n").append("                        post = _base.substring(_base.indexOf(_old) + _old.length());");
        sb.append("\n").append("                    } catch (RuntimeException e) {");
        sb.append("\n").append("                        post = \"\";");
        sb.append("\n").append("                    }");
        sb.append("\n").append("    ");
        sb.append("\n").append("                    sb.append(pre).append(_new);");
        sb.append("\n").append("                    _base = post;");
        sb.append("\n").append("                }");
        sb.append("\n").append("                sb.append(_base);");
        sb.append("\n").append("  ");
        sb.append("\n").append("            return sb.toString();");
        sb.append("\n").append("        }");
        sb.append("\n").append("   }");
        sb.append("\n").append("    public static Date parseDateTime(String s) throws ParseException");
        sb.append("\n").append("    {");
        sb.append("\n").append("        if (s.indexOf(\"'\")!= -1)");
                sb.append("\n").append("        {");
        sb.append("\n").append("            s = ReplaceStr(s,\"'\",\"\");");
                sb.append("\n").append("        }");
        sb.append("\n").append("        if (s==null)");
        sb.append("\n").append("        {");
        sb.append("\n").append("            return null;");
        sb.append("\n").append("        }else{");
        sb.append("\n").append("            try {");
        sb.append("\n").append("                return DateFormat.getInstance().parse(s);");
        sb.append("\n").append("            } catch (ParseException e) {");
        sb.append("\n").append("                SimpleDateFormat sdf = new SimpleDateFormat(\"EEE MMM dd HH:mm:ss z yyyy\", Locale.US);");
        sb.append("\n").append("                try {");
        sb.append("\n").append("                    return sdf.parse(s);");
        sb.append("\n").append("                } catch (ParseException e1) {");
        sb.append("\n").append("                    sdf = new SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\", Locale.US);");
        sb.append("\n").append("                    try {");
        sb.append("\n").append("                        return sdf.parse(s);");
        sb.append("\n").append("                    } catch (ParseException e2) {");
        sb.append("\n").append("                        sdf = new SimpleDateFormat(\"EEE MMM dd HH:mm:ss\", Locale.US);");
        sb.append("\n").append("                        try {");
        sb.append("\n").append("                            return sdf.parse(s);");
        sb.append("\n").append("                        } catch (ParseException e6) {");
        sb.append("\n").append("                            sdf = new SimpleDateFormat(\"EEE MMM dd HH:mm:ss.S\", Locale.US);");
        sb.append("\n").append("                            try {");
        sb.append("\n").append("                                return sdf.parse(s);");
        sb.append("\n").append("                            } catch (ParseException e3) {");
        sb.append("\n").append("                                sdf = new SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss z\", Locale.US);");
        sb.append("\n").append("                                try {");
        sb.append("\n").append("                                    return sdf.parse(s);");
        sb.append("\n").append("                                } catch (ParseException e4) {");
        sb.append("\n").append("                                    sdf = new SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss.S\", Locale.US);");
        sb.append("\n").append("                                    try {");
        sb.append("\n").append("                                        return sdf.parse(s);");
        sb.append("\n").append("                                    } catch (ParseException e5) {");
        sb.append("\n").append("                                        sdf = new SimpleDateFormat(\"EEE MMM dd HH:mm:ss z\", Locale.US);");
        sb.append("\n").append("                                        try {");
        sb.append("\n").append("                                            return sdf.parse(s);");
        sb.append("\n").append("                                        } catch (ParseException e7) {");
        sb.append("\n").append("                                            sdf = new SimpleDateFormat(\"yyyy-MM-dd'T'HH:mm:ss\", Locale.US);");
        sb.append("\n").append("                                            try {");
        sb.append("\n").append("                                                return sdf.parse(s);");
        sb.append("\n").append("                                            } catch (ParseException e8) {");
        sb.append("\n").append("                                                sdf = new SimpleDateFormat(\"MM/dd/yyyy\", Locale.US);");
        sb.append("\n").append("                                                try {");
        sb.append("\n").append("                                                    return sdf.parse(s);");
        sb.append("\n").append("                                                } catch (ParseException e9) {");
        sb.append("\n").append("                                                    sdf = new SimpleDateFormat(\"MM/dd/yyyy\", Locale.US);");
        sb.append("\n").append("                                                    try {");
        sb.append("\n").append("                                                        return sdf.parse(s);");
        sb.append("\n").append("                                                    } catch (ParseException e10) {");
        sb.append("\n").append("                                                        sdf = new SimpleDateFormat(\"yyyy-MM-dd\", Locale.US);");
        sb.append("\n").append("                                                        return sdf.parse(s);");
        sb.append("\n").append("                                                    }");
        sb.append("\n").append("                                                }");
        sb.append("\n").append("                                            }");
        sb.append("\n").append("                                        }");
        sb.append("\n").append("                                    }");
        sb.append("\n").append("                                }");
        sb.append("\n").append("                            }");
        sb.append("\n").append("                            ");
        sb.append("\n").append("                        }");
        sb.append("\n").append("                        ");
        sb.append("\n").append("                    }");
        sb.append("\n").append("                }");
        sb.append("\n").append("            }");
        sb.append("\n").append("        }");
        sb.append("\n").append("    }");
        sb.append("\n").append("");
        sb.append("\n").append("    public static Date parseTime(String s) throws ParseException");
        sb.append("\n").append("    {");
        sb.append("\n").append("        if (s.indexOf(\"'\")!= -1)");
                sb.append("\n").append("        {");
        sb.append("\n").append("            s = ReplaceStr(s,\"'\",\"\");");
                sb.append("\n").append("        }");
        sb.append("\n").append("        if (s==null)");
        sb.append("\n").append("        {");
        sb.append("\n").append("           return null;");
        sb.append("\n").append("        }else{");
        sb.append("\n").append("            try {");
        sb.append("\n").append("                return DateFormat.getInstance().parse(s);");
        sb.append("\n").append("            } catch (ParseException e) {");
        sb.append("\n").append("                SimpleDateFormat sdf = new SimpleDateFormat(\"HH:mm:ss\", Locale.US);");
        sb.append("\n").append("                try {");
        sb.append("\n").append("                    return sdf.parse(s);");
        sb.append("\n").append("                } catch (ParseException e1) {");
        sb.append("\n").append("                    sdf = new SimpleDateFormat(\"HH:mm:ss z\", Locale.US);");
        sb.append("\n").append("                    return sdf.parse(s);");
        sb.append("\n").append("                }");
        sb.append("\n").append("            }");
        sb.append("\n").append("        }");
        sb.append("\n").append("    }");
        sb.append("\n").append("    public void setDataField(String xmlPath,String s) throws UnknownFieldException");
        sb.append("\n").append("    {");
        sb.append("\n").append("        throw new UnknownFieldException(xmlPath);");
        sb.append("\n").append("    }");
        sb.append("\n").append("    public void setReferenceField(String xmlPath,BaseElement s) throws UnknownFieldException");
        sb.append("\n").append("    {");
        sb.append("\n").append("        throw new UnknownFieldException(xmlPath);");
        sb.append("\n").append("    }");
        sb.append("\n").append("    public String getFieldType(String xmlPath) throws UnknownFieldException");
        sb.append("\n").append("    {");
        sb.append("\n").append("        throw new UnknownFieldException(xmlPath);");
        sb.append("\n").append("    }");
        sb.append("\n").append("}");

        
        return sb.toString();
    }
    
    /**
     * @param e
     * @param location
     * @throws Exception
     */
    public void generateJavaInterface(GenericWrapperElement e, String location) throws Exception
    {
        StringBuffer sbI = new StringBuffer();
        
        String packageName = INTERFACE_PACKAGE;
        
        sbI.append("/*\n * GENERATED FILE\n * Created on " + Calendar.getInstance().getTime() + "\n *\n */");
        sbI.append("\npackage " + INTERFACE_PACKAGE + ";");
        //IMPORTS
        sbI.append("\n\nimport java.util.List;");
        sbI.append("\n\n/**\n * @author XDAT\n *\n */");
        
        //INTERFACE
        String interfaceExtensionName = null;
        if (e.isExtension())
        {
           GenericWrapperElement ext = GenericWrapperElement.GetElement(e.getExtensionType());
           interfaceExtensionName = getFormattedInterface(ext);
}
        
        String interfaceName = getFormattedInterface(e);
        sbI.append("\npublic interface ").append(interfaceName);
        if (interfaceExtensionName==null)
        {
            sbI.append(" {");
        }else{
            sbI.append(" extends " + interfaceExtensionName + " {");
        }
                
        sbI.append("\n\n");
        sbI.append("\t").append("public String getXSIType();");
        sbI.append("\n\n");
        sbI.append("\t").append("public void toXML(java.io.Writer writer) throws java.lang.Exception;");
        
        
        
        Iterator fields = e.getAllFields(true,true).iterator();
        while (fields.hasNext())
        {
            GenericWrapperField f= (GenericWrapperField)fields.next();
            if (f.isReference())
            {
                String xmlPath = f.getXMLPathString();
                String formatted = formatFieldName(xmlPath);
                if (formatted.equalsIgnoreCase("USER"))
                {
                    formatted = "userProperty";
                }
                SchemaElementI foreign = f.getReferenceElement();
                
                if (foreign.getGenericXFTElement().getAddin().equals(""))
                {
                    String foreignClassName = getFormattedInterface(foreign.getGenericXFTElement());
                    
                    if (f.isMultiple())
                    {               
                        sbI.append("\n");
                        sbI.append("\n\t/**");
                        sbI.append("\n\t * " + xmlPath);
                        sbI.append("\n\t * @return Returns an List of org.nrg.xdat.model.").append(foreignClassName).append("\n\t */");
                        sbI.append("\n\t").append("public <A extends ").append(INTERFACE_PACKAGE +".").append(foreignClassName).append("> List<A> get").append(formatted).append("();");                        

                        sbI.append("\n");
                        sbI.append("\n\t/**");
                        sbI.append("\n\t * " + xmlPath);
                        sbI.append("\n\t * @return Returns an List of org.nrg.xdat.model.").append(foreignClassName).append("\n\t */");
                        sbI.append("\n\t").append("public <A extends ").append(INTERFACE_PACKAGE +".").append(foreignClassName).append("> void add").append(formatted).append("(A item) throws Exception;"); 
                    }else{
                        
                        if(!f.getName().equalsIgnoreCase(e.getExtensionFieldName())){
                        	sbI.append("\n");
                            sbI.append("\n\t/**");
                            sbI.append("\n\t * " + xmlPath);
                            sbI.append("\n\t * @return ").append(INTERFACE_PACKAGE +".").append(foreignClassName).append("\n\t */");
                            sbI.append("\n\t").append("public ").append(INTERFACE_PACKAGE +".").append(foreignClassName).append(" get").append(formatted).append("();");
                            
                        	sbI.append("\n");
                            sbI.append("\n\t/**");
                            sbI.append("\n\t * " + xmlPath);
                            sbI.append("\n\t * @return ").append(INTERFACE_PACKAGE +".").append(foreignClassName).append("\n\t */");
                            sbI.append("\n\t").append("public <A extends ").append(INTERFACE_PACKAGE +".").append(foreignClassName).append("> void set").append(formatted).append("(A item) throws Exception;");
                            
                            XFTSuperiorReference ref = (XFTSuperiorReference)f.getXFTReference();
                            Iterator iter=ref.getKeyRelations().iterator();
                            while (iter.hasNext())
                            {
                                XFTRelationSpecification spec = (XFTRelationSpecification) iter.next();
                                String temp = spec.getLocalCol();

                                String type = spec.getSchemaType().getLocalType();
                                if (type==null){
                                    type = "";
                                }
                                
                                else if (type.equalsIgnoreCase("integer"))
                                {
                                    String reformatted = formatted + "FK";                                    

                                    //STANDARD GET METHOD
                                    sbI.append("\n\n");
                                    sbI.append("\t/**\n\t * @return Returns the ").append(e.getXSIType() + "/" + temp).append(".\n\t */");
                                    sbI.append("\n\t").append("public Integer get").append(reformatted).append("();");
                                    
                                }else if (type.equalsIgnoreCase("string"))
                                {
                                    String reformatted = formatted + "_" +formatFieldName(temp);                                    

                                    //STANDARD GET METHOD
                                    sbI.append("\n\n");
                                    sbI.append("\t/**\n\t * @return Returns the ").append(e.getXSIType() + "/" + temp).append(".\n\t */");
                                    sbI.append("\n\t").append("public String get").append(reformatted).append("();");
                                    
                                }else{
                                    String reformatted = formatted + "_" +formatFieldName(temp);                                    

                                    //STANDARD GET METHOD
                                    sbI.append("\n\n");
                                    sbI.append("\t/**\n\t * @return Returns the ").append(e.getXSIType() + "/" + temp).append(".\n\t */");
                                    sbI.append("\n\t").append("public Object get").append(reformatted).append("();");
                                    
                                }
                            }
                        }
                    }
                }
            }else{
                String type = f.getXMLType().getLocalType();
                if (type != null)
                {
                    String xmlPath = f.getXMLPathString();
                    String formatted = formatFieldName(xmlPath);
                    if (formatted.equalsIgnoreCase("USER"))
                    {
                        formatted = "userProperty";
                    }
                                        
                    if (type.equalsIgnoreCase("boolean"))
                    {                    
//                      STANDARD GET METHOD
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sbI.append("\n\t").append("public Boolean get").append(formatted).append("();");


                        //STANDARD SET METHOD
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sbI.append("\n\t").append("public void set").append(formatted).append("(Object v);");
                    }else if (type.equalsIgnoreCase("integer"))
                    {                        
                        //STANDARD GET METHOD
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sbI.append("\n\t").append("public Integer get").append(formatted).append("();");

                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * Sets the value for ").append(e.getXSIType() + "/" + xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sbI.append("\n\t").append("public void set").append(formatted).append("(Integer v) ;");

                   }else if(type.equalsIgnoreCase("double") || type.equalsIgnoreCase("float")){   
                        //STANDARD GET METHOD
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sbI.append("\n\t").append("public Double get").append(formatted).append("();");
                        
                        //STANDARD SET METHOD
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sbI.append("\n\t").append("public void set").append(formatted).append("(Double v);");
                        
                    }else if (type.equalsIgnoreCase("string"))
                    {
                        //STANDARD GET METHOD
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sbI.append("\n\t").append("public String get").append(formatted).append("();");

                        //STANDARD SET METHOD
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sbI.append("\n\t").append("public void set").append(formatted).append("(String v);");

                    }else if (type.equalsIgnoreCase("date") || type.equalsIgnoreCase("dateTime")){                        
                        //STANDARD GET METHOD
                    	sbI.append("\n\n");
                    	sbI.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sbI.append("\n\t").append("public Object get").append(formatted).append("();");

                        //STANDARD SET METHOD
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sbI.append("\n\t").append("public void set").append(formatted).append("(Object v);");

                    }else{
                        //STANDARD GET METHOD
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sbI.append("\n\t").append("public Object get").append(formatted).append("();");
                        
                        //STANDARD SET METHOD
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sbI.append("\n\t").append("public void set").append(formatted).append("(Object v);");
                        
                    }
                }
            }
        }
        
        if (!e.containsStatedKey()){
            GenericWrapperField f = (GenericWrapperField)e.getDefaultKey();
            String xmlPath = f.getXMLPathString();
            String formatted = formatFieldName(xmlPath);
            if (formatted.equalsIgnoreCase("USER"))
            {
                formatted = "userProperty";
            }
            
            //STANDARD GET METHOD
            sbI.append("\n\n");
            sbI.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
            sbI.append("\n\t").append("public Integer get").append(formatted).append("();");
        }
        sbI.append("\n");
        sbI.append("}");
        
        if (!location.endsWith(File.separator))
        {
            location += File.separator;
        }
        
        File dir = new File(location);
        if (!dir.exists())
        {
            dir.mkdir();
        }
        
        String dirStucture = packageName;
        while (dirStucture.indexOf(".")!=-1)
        {
            String folder = dirStucture.substring(0,dirStucture.indexOf("."));
            dirStucture = dirStucture.substring(dirStucture.indexOf(".")+1);
            
            location = location + folder + File.separator;
            dir = new File(location);
            if (!dir.exists())
            {
                dir.mkdir();
            }
        }
        
        location = location + dirStucture + File.separator;
        dir = new File(location);
        if (!dir.exists())
        {
            dir.mkdir();
        }

        if (XFT.VERBOSE)
             System.out.println("Generating File " + location +getFormattedInterface(e) +".java...");
        FileUtils.OutputToFile(sbI.toString(),location +getFormattedInterface(e) + ".java");
    }
}
