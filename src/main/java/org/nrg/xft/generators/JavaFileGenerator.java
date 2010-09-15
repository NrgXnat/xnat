//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 9, 2005
 *
 */
package org.nrg.xft.generators;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;

import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xft.XFT;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.references.XFTRelationSpecification;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.StringUtils;

/**
 * @author Tim
 *
 */
public class JavaFileGenerator {
    private String prefix="Base";
    
    private static int item_counter = 0;
    
    public JavaFileGenerator(){}

    /**
     * @param e
     * @param location
     * @throws Exception
     */
    public void generateJavaFile(GenericWrapperElement e, String location) throws Exception
    {
        StringBuffer sb = new StringBuffer();
        StringBuffer sbI = new StringBuffer();
        
        String packageName = "org.nrg.xdat.om.base.auto";
        
        sb.append("/*\n * GENERATED FILE\n * Created on " + Calendar.getInstance().getTime() + "\n *\n */");
        sb.append("\npackage " + packageName + ";");
        //IMPORTS
        sb.append("\nimport org.nrg.xft.*;");
        sb.append("\nimport org.nrg.xft.security.UserI;");
        sb.append("\nimport org.nrg.xdat.om.*;");
        sb.append("\nimport org.nrg.xft.utils.ResourceFile;");
        sb.append("\nimport org.nrg.xft.exception.*;");
        sb.append("\n\nimport java.util.*;");
        sb.append("\n\n/**\n * @author XDAT\n *\n */");
        
        sbI.append("/*\n * GENERATED FILE\n * Created on " + Calendar.getInstance().getTime() + "\n *\n */");
        sbI.append("\npackage org.nrg.xdat.om;");
        //IMPORTS
        sbI.append("\nimport org.nrg.xft.*;");
        sbI.append("\nimport org.nrg.xft.security.UserI;");
        sbI.append("\nimport org.nrg.xdat.om.*;");
        sbI.append("\n\nimport java.util.*;");
        sbI.append("\n\n/**\n * @author XDAT\n *\n */");
        
        //CLASS
        String extensionName = "org.nrg.xdat.base.BaseElement";
        String interfaceExtensionName = null;
        if (e.isExtension())
        {
           GenericWrapperElement ext = GenericWrapperElement.GetElement(e.getExtensionType());
            extensionName = getSQLClassName(ext);
            interfaceExtensionName = extensionName + "I";
        }
        
        String interfaceName = getSQLClassName(e) +"I";
        sbI.append("\npublic interface ").append(interfaceName);
        if (interfaceExtensionName==null)
        {
            sbI.append(" {");
        }else{
            sbI.append(" extends " + interfaceExtensionName + " {");
        }
        sb.append("\n@SuppressWarnings({\"unchecked\",\"rawtypes\"})\npublic abstract class ").append(getClassName(e)).append(" extends " + extensionName + " implements " + interfaceName +"{");
        sb.append("\n\tpublic static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("+getClassName(e)+".class);");
        sb.append("\n\tpublic static String SCHEMA_ELEMENT_NAME=\"").append(e.getFullXMLName()).append("\";");
        //ADD CONSTRUCTORS
        sb.append("\n\n");
        sb.append("\tpublic ").append(getClassName(e)).append("(ItemI item)\n\t{\n\t\tsuper(item);\n\t}");
        
        sb.append("\n\n");
        sb.append("\tpublic ").append(getClassName(e)).append("(UserI user)\n\t{");
        sb.append("\n\t\tsuper(user);\n\t}");
        
        sb.append("\n");
        sb.append("\n\t/*");
        sb.append("\n\t * @deprecated Use ").append(getClassName(e)).append("(UserI user)");
        sb.append("\n\t **/");
        sb.append("\n\tpublic ").append(getClassName(e)).append("(){}");
        
        sb.append("\n\n");
        sb.append("\tpublic ").append(getClassName(e)).append("(Hashtable properties,UserI user)\n\t{");
        sb.append("\n\t\tsuper(properties,user);\n\t}");
        
        //ADD SCHEMA METHODS
        sb.append("\n\n");
        sb.append("\t").append("public String getSchemaElementName(){");
        sb.append("\n\t\t").append("return \"").append(e.getFullXMLName()).append("\";");
        sb.append("\n\t}");
        
        sbI.append("\n\n");
        sbI.append("\t").append("public String getSchemaElementName();");
        
        
        
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
                    String foreignClassName = getSQLClassName(foreign.getGenericXFTElement());
                    
                    if (f.isMultiple())
                    {
                        sb.append("\n\t private ArrayList<org.nrg.xdat.om.").append(foreignClassName).append("> _" + formatted + " =null;");
                        sb.append("\n");
                        sb.append("\n\t/**");
                        sb.append("\n\t * " + xmlPath);
                       	sb.append("\n\t * @return Returns an ArrayList of org.nrg.xdat.om.").append(foreignClassName).append("\n\t */");
                        sb.append("\n\t").append("public ArrayList<org.nrg.xdat.om.").append(foreignClassName).append("> get").append(formatted).append("() {");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t\t").append("if (_" + formatted + "==null){");
                        sb.append("\n\t\t\t\t_").append(formatted + "=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems(\"").append(xmlPath).append("\"));");
                        sb.append("\n\t\t\t\t").append("return _" + formatted +";");
                        sb.append("\n\t\t\t").append("}else {");
                        sb.append("\n\t\t\t\t").append("return _" + formatted +";");
                        sb.append("\n\t\t\t").append("}");
                        sb.append("\n\t\t").append("} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.").append(foreignClassName).append(">();}");
                        sb.append("\n\t}");
                        
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(ItemI v) throws Exception{");
                        sb.append("\n\t\t_").append(formatted + " =null;");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t\t").append("if (v instanceof XFTItem)");
                        sb.append("\n\t\t\t").append("{");
                        sb.append("\n\t\t\t\t").append("getItem().setChild(SCHEMA_ELEMENT_NAME + \"/").append(xmlPath).append("\",v,true);");
                        sb.append("\n\t\t\t").append("}else{");
                        sb.append("\n\t\t\t\t").append("getItem().setChild(SCHEMA_ELEMENT_NAME + \"/").append(xmlPath).append("\",v.getItem(),true);");
                        sb.append("\n\t\t\t").append("}");
                        sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);throw e1;}");
                        sb.append("\n\t}");

                        
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Removes the ").append(xmlPath).append(" of the given index.\n\t * @param index Index of child to remove.\n\t */");
                        sb.append("\n\t").append("public void remove").append(formatted).append("(int index) throws java.lang.IndexOutOfBoundsException {");
                        sb.append("\n\t\t_").append(formatted + " =null;");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t\t").append("getItem().removeChild(SCHEMA_ELEMENT_NAME + \"/").append(xmlPath).append("\",index);");
                        sb.append("\n\t\t").append("} catch (FieldNotFoundException e1) {logger.error(e1);}");
                        sb.append("\n\t}");
                        

                        sbI.append("\n");
                        sbI.append("\n\t/**");
                        sbI.append("\n\t * " + xmlPath);
                        sbI.append("\n\t * @return Returns an ArrayList of org.nrg.xdat.om.").append(foreignClassName).append("I\n\t */");
                        sbI.append("\n\t").append("public ArrayList<org.nrg.xdat.om.").append(foreignClassName).append("> get").append(formatted).append("();");
                        
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sbI.append("\n\t").append("public void set").append(formatted).append("(ItemI v) throws Exception;");
                    }else{
                        sb.append("\n\t private org.nrg.xdat.om." + foreignClassName +"I _" + formatted + " =null;");
                        sb.append("\n");
                        sb.append("\n\t/**");
                        sb.append("\n\t * " + xmlPath);
                       	sb.append("\n\t * @return org.nrg.xdat.om.").append(foreignClassName).append("I\n\t */");
                        sb.append("\n\t").append("public org.nrg.xdat.om." + foreignClassName +"I get").append(formatted).append("() {");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t\t").append("if (_" + formatted + "==null){");
                        sb.append("\n\t\t\t\t_").append(formatted + "=(("+ foreignClassName + "I)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty(\"").append(xmlPath).append("\")));");
                        sb.append("\n\t\t\t\t").append("return _" + formatted +";");
                        sb.append("\n\t\t\t").append("}else {");
                        sb.append("\n\t\t\t\t").append("return _" + formatted +";");
                        sb.append("\n\t\t\t").append("}");
                        sb.append("\n\t\t").append("} catch (Exception e1) {return null;}");
                        sb.append("\n\t}");
                        
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(ItemI v) throws Exception{");
                        sb.append("\n\t\t_").append(formatted + " =null;");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t\t").append("if (v instanceof XFTItem)");
                        sb.append("\n\t\t\t").append("{");
                        sb.append("\n\t\t\t\t").append("getItem().setChild(SCHEMA_ELEMENT_NAME + \"/").append(xmlPath).append("\",v,true);");
                        sb.append("\n\t\t\t").append("}else{");
                        sb.append("\n\t\t\t\t").append("getItem().setChild(SCHEMA_ELEMENT_NAME + \"/").append(xmlPath).append("\",v.getItem(),true);");
                        sb.append("\n\t\t\t").append("}");
                        sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);throw e1;}");
                        sb.append("\n\t}");

                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Removes the ").append(xmlPath).append(".\n\t * */");
                        sb.append("\n\t").append("public void remove").append(formatted).append("() {");
                        sb.append("\n\t\t_").append(formatted + " =null;");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t\t").append("getItem().removeChild(SCHEMA_ELEMENT_NAME + \"/").append(xmlPath).append("\",0);");
                        sb.append("\n\t\t").append("} catch (FieldNotFoundException e1) {logger.error(e1);}");
                        sb.append("\n\t\t").append("catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}");
                        sb.append("\n\t}");
                        
                        sbI.append("\n");
                        sbI.append("\n\t/**");
                        sbI.append("\n\t * " + xmlPath);
                        sbI.append("\n\t * @return org.nrg.xdat.om.").append(foreignClassName).append("I\n\t */");
                        sbI.append("\n\t").append("public org.nrg.xdat.om." + foreignClassName +"I get").append(formatted).append("();");
                        
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sbI.append("\n\t").append("public void set").append(formatted).append("(ItemI v) throws Exception;");

                        if(!f.getName().equalsIgnoreCase(e.getExtensionFieldName())){
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
                                    sb.append("\n\t\t").append("try{");
                                    sb.append("\n\t\t\t").append("if (_" + reformatted + "==null){");
                                    sb.append("\n\t\t\t\t_").append(reformatted + "=getIntegerProperty(\"").append(e.getXSIType() + "/" + temp).append("\");");
                                    sb.append("\n\t\t\t\t").append("return _" + reformatted +";");
                                    sb.append("\n\t\t\t").append("}else {");
                                    sb.append("\n\t\t\t\t").append("return _" + reformatted +";");
                                    sb.append("\n\t\t\t").append("}");
                                    sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);return null;}");
                                    sb.append("\n\t}");
                                    
                                    //STANDARD SET METHOD
                                    sb.append("\n\n");
                                    sb.append("\t/**\n\t * Sets the value for ").append(e.getXSIType() + "/" + temp).append(".\n\t * @param v Value to Set.\n\t */");
                                    sb.append("\n\t").append("public void set").append(reformatted).append("(Integer v) {");
                                    sb.append("\n\t\t").append("try{");
                                    sb.append("\n\t\t").append("setProperty(SCHEMA_ELEMENT_NAME + \"/").append(temp).append("\",v);");
                                    sb.append("\n\t\t_").append(reformatted +"=null;");
                                    sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);}");
                                    sb.append("\n\t}");
                                    

                                    //STANDARD GET METHOD
                                    sbI.append("\n\n");
                                    sbI.append("\t/**\n\t * @return Returns the ").append(e.getXSIType() + "/" + temp).append(".\n\t */");
                                    sbI.append("\n\t").append("public Integer get").append(reformatted).append("();");
                                    
                                    //STANDARD SET METHOD
                                    sbI.append("\n\n");
                                    sbI.append("\t/**\n\t * Sets the value for ").append(e.getXSIType() + "/" + temp).append(".\n\t * @param v Value to Set.\n\t */");
                                    sbI.append("\n\t").append("public void set").append(reformatted).append("(Integer v);");
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
                                    sb.append("\n\t\t").append("try{");
                                    sb.append("\n\t\t\t").append("if (_" + reformatted + "==null){");
                                    sb.append("\n\t\t\t\t_").append(reformatted + "=getStringProperty(\"").append(e.getXSIType() + "/" + temp).append("\");");
                                    sb.append("\n\t\t\t\t").append("return _" + reformatted +";");
                                    sb.append("\n\t\t\t").append("}else {");
                                    sb.append("\n\t\t\t\t").append("return _" + reformatted +";");
                                    sb.append("\n\t\t\t").append("}");
                                    sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);return null;}");
                                    sb.append("\n\t}");
                                    
                                    //STANDARD SET METHOD
                                    sb.append("\n\n");
                                    sb.append("\t/**\n\t * Sets the value for ").append(e.getXSIType() + "/" + temp).append(".\n\t * @param v Value to Set.\n\t */");
                                    sb.append("\n\t").append("public void set").append(reformatted).append("(String v){");
                                    sb.append("\n\t\t").append("try{");
                                    sb.append("\n\t\t").append("setProperty(SCHEMA_ELEMENT_NAME + \"/").append(e.getXSIType() + "/" + temp).append("\",v);");
                                    sb.append("\n\t\t_").append(reformatted +"=null;");
                                    sb.append("\n\t\t").append("} catch (Exception e1) {}");
                                    sb.append("\n\t}");
                                    

                                    //STANDARD GET METHOD
                                    sbI.append("\n\n");
                                    sbI.append("\t/**\n\t * @return Returns the ").append(e.getXSIType() + "/" + temp).append(".\n\t */");
                                    sbI.append("\n\t").append("public String get").append(reformatted).append("();");
                                    
                                    //STANDARD SET METHOD
                                    sbI.append("\n\n");
                                    sbI.append("\t/**\n\t * Sets the value for ").append(e.getXSIType() + "/" + temp).append(".\n\t * @param v Value to Set.\n\t */");
                                    sbI.append("\n\t").append("public void set").append(reformatted).append("(String v);");
                                }else{
                                    String reformatted = formatted + "_" +formatFieldName(temp);
                                    sb.append("\n\n\t").append("//FIELD");
                                    sb.append("\n\n\t").append("private Object _").append(reformatted);
                                    sb.append("=null;");
                                    
                                    //STANDARD GET METHOD
                                    sb.append("\n\n");
                                    sb.append("\t/**\n\t * @return Returns the ").append(e.getXSIType() + "/" + temp).append(".\n\t */");
                                    sb.append("\n\t").append("public Object get").append(reformatted).append("(){");
                                    sb.append("\n\t\t").append("try{");
                                    sb.append("\n\t\t\t").append("if (_" + reformatted + "==null){");
                                    sb.append("\n\t\t\t\t_").append(reformatted + "=getProperty(\"").append(e.getXSIType() + "/" + temp).append("\");");
                                    sb.append("\n\t\t\t\t").append("return _" + reformatted +";");
                                    sb.append("\n\t\t\t").append("}else {");
                                    sb.append("\n\t\t\t\t").append("return _" + reformatted +";");
                                    sb.append("\n\t\t\t").append("}");
                                    sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);return null;}");
                                    sb.append("\n\t}");
                                    
                                    //STANDARD SET METHOD
                                    sb.append("\n\n");
                                    sb.append("\t/**\n\t * Sets the value for ").append(e.getXSIType() + "/" + temp).append(".\n\t * @param v Value to Set.\n\t */");
                                    sb.append("\n\t").append("public void set").append(reformatted).append("(Object v){");
                                    sb.append("\n\t\t").append("try{");
                                    sb.append("\n\t\t").append("setProperty(SCHEMA_ELEMENT_NAME + \"/").append(e.getXSIType() + "/" + temp).append("\",v);");
                                    sb.append("\n\t\t_").append(reformatted +"=null;");
                                    sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);}");
                                    sb.append("\n\t}");
                                    

                                    //STANDARD GET METHOD
                                    sbI.append("\n\n");
                                    sbI.append("\t/**\n\t * @return Returns the ").append(e.getXSIType() + "/" + temp).append(".\n\t */");
                                    sbI.append("\n\t").append("public Object get").append(reformatted).append("();");
                                    
                                    //STANDARD SET METHOD
                                    sbI.append("\n\n");
                                    sbI.append("\t/**\n\t * Sets the value for ").append(e.getXSIType() + "/" + temp).append(".\n\t * @param v Value to Set.\n\t */");
                                    sbI.append("\n\t").append("public void set").append(reformatted).append("(Object v) ;");
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
                        sb.append("\n\n\t").append("//FIELD");
                        sb.append("\n\n\t").append("private Boolean _").append(formatted);
                        sb.append("=null;");
                        
//                      STANDARD GET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sb.append("\n\t").append("public Boolean get").append(formatted).append("() {");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t\t").append("if (_" + formatted + "==null){");
                        sb.append("\n\t\t\t\t_").append(formatted + "=getBooleanProperty(\"").append(xmlPath).append("\");");
                        sb.append("\n\t\t\t\t").append("return _" + formatted +";");
                        sb.append("\n\t\t\t").append("}else {");
                        sb.append("\n\t\t\t\t").append("return _" + formatted +";");
                        sb.append("\n\t\t\t").append("}");
                        sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);return null;}");
                        sb.append("\n\t}");

                        //STANDARD SET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(Object v){");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t").append("setBooleanProperty(SCHEMA_ELEMENT_NAME + \"/").append(xmlPath).append("\",v);");
                        sb.append("\n\t\t_").append(formatted +"=null;");
                        sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);}");
                        sb.append("\n\t}");
                        

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
                        sb.append("\n\n\t").append("//FIELD");
                        sb.append("\n\n\t").append("private Integer _").append(formatted);
                        sb.append("=null;");
                        
                        //STANDARD GET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sb.append("\n\t").append("public Integer get").append(formatted).append("() {");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t\t").append("if (_" + formatted + "==null){");
                        sb.append("\n\t\t\t\t_").append(formatted + "=getIntegerProperty(\"").append(xmlPath).append("\");");
                        sb.append("\n\t\t\t\t").append("return _" + formatted +";");
                        sb.append("\n\t\t\t").append("}else {");
                        sb.append("\n\t\t\t\t").append("return _" + formatted +";");
                        sb.append("\n\t\t\t").append("}");
                        sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);return null;}");
                        sb.append("\n\t}");

                        //STANDARD SET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(Integer v){");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t").append("setProperty(SCHEMA_ELEMENT_NAME + \"/").append(xmlPath).append("\",v);");
                        sb.append("\n\t\t_").append(formatted +"=null;");
                        sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);}");
                        sb.append("\n\t}");

                        
                        //STANDARD GET METHOD
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sbI.append("\n\t").append("public Integer get").append(formatted).append("();");

                        //STANDARD SET METHOD
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sbI.append("\n\t").append("public void set").append(formatted).append("(Integer v);");
                    }else if(type.equalsIgnoreCase("double") || type.equalsIgnoreCase("float")){
                        sb.append("\n\n\t").append("//FIELD");
                        sb.append("\n\n\t").append("private Double _").append(formatted);
                        sb.append("=null;");
                        
                        //STANDARD GET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sb.append("\n\t").append("public Double get").append(formatted).append("() {");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t\t").append("if (_" + formatted + "==null){");
                        sb.append("\n\t\t\t\t_").append(formatted + "=getDoubleProperty(\"").append(xmlPath).append("\");");
                        sb.append("\n\t\t\t\t").append("return _" + formatted +";");
                        sb.append("\n\t\t\t").append("}else {");
                        sb.append("\n\t\t\t\t").append("return _" + formatted +";");
                        sb.append("\n\t\t\t").append("}");
                        sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);return null;}");
                        sb.append("\n\t}");
                        
                        //STANDARD SET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(Double v){");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t").append("setProperty(SCHEMA_ELEMENT_NAME + \"/").append(xmlPath).append("\",v);");
                        sb.append("\n\t\t_").append(formatted +"=null;");
                        sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);}");
                        sb.append("\n\t}");
                        

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
                        sb.append("\n\n\t").append("//FIELD");
                        sb.append("\n\n\t").append("private String _").append(formatted);
                        sb.append("=null;");
                        
                        //STANDARD GET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sb.append("\n\t").append("public String get").append(formatted).append("(){");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t\t").append("if (_" + formatted + "==null){");
                        sb.append("\n\t\t\t\t_").append(formatted + "=getStringProperty(\"").append(xmlPath).append("\");");
                        sb.append("\n\t\t\t\t").append("return _" + formatted +";");
                        sb.append("\n\t\t\t").append("}else {");
                        sb.append("\n\t\t\t\t").append("return _" + formatted +";");
                        sb.append("\n\t\t\t").append("}");
                        sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);return null;}");
                        sb.append("\n\t}");

                        //STANDARD SET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(String v){");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t").append("setProperty(SCHEMA_ELEMENT_NAME + \"/").append(xmlPath).append("\",v);");
                        sb.append("\n\t\t_").append(formatted +"=null;");
                        sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);}");
                        sb.append("\n\t}");
                        

                        //STANDARD GET METHOD
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sbI.append("\n\t").append("public String get").append(formatted).append("();");

                        //STANDARD SET METHOD
                        sbI.append("\n\n");
                        sbI.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sbI.append("\n\t").append("public void set").append(formatted).append("(String v);");
                    }else{
                        sb.append("\n\n\t").append("//FIELD");
                        sb.append("\n\n\t").append("private Object _").append(formatted);
                        sb.append("=null;");
                        
                        //STANDARD GET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
                        sb.append("\n\t").append("public Object get").append(formatted).append("(){");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t\t").append("if (_" + formatted + "==null){");
                        sb.append("\n\t\t\t\t_").append(formatted + "=getProperty(\"").append(xmlPath).append("\");");
                        sb.append("\n\t\t\t\t").append("return _" + formatted +";");
                        sb.append("\n\t\t\t").append("}else {");
                        sb.append("\n\t\t\t\t").append("return _" + formatted +";");
                        sb.append("\n\t\t\t").append("}");
                        sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);return null;}");
                        sb.append("\n\t}");
                        
                        //STANDARD SET METHOD
                        sb.append("\n\n");
                        sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
                        sb.append("\n\t").append("public void set").append(formatted).append("(Object v){");
                        sb.append("\n\t\t").append("try{");
                        sb.append("\n\t\t").append("setProperty(SCHEMA_ELEMENT_NAME + \"/").append(xmlPath).append("\",v);");
                        sb.append("\n\t\t_").append(formatted +"=null;");
                        sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);}");
                        sb.append("\n\t}");
                        

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
            sb.append("\n\n\t").append("//FIELD");
            sb.append("\n\n\t").append("private Integer _").append(formatted);
            sb.append("=null;");
            
            //STANDARD GET METHOD
            sb.append("\n\n");
            sb.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
            sb.append("\n\t").append("public Integer get").append(formatted).append("() {");
            sb.append("\n\t\t").append("try{");
            sb.append("\n\t\t\t").append("if (_" + formatted + "==null){");
            sb.append("\n\t\t\t\t_").append(formatted + "=getIntegerProperty(\"").append(xmlPath).append("\");");
            sb.append("\n\t\t\t\t").append("return _" + formatted +";");
            sb.append("\n\t\t\t").append("}else {");
            sb.append("\n\t\t\t\t").append("return _" + formatted +";");
            sb.append("\n\t\t\t").append("}");
            sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);return null;}");
            sb.append("\n\t}");

            //STANDARD SET METHOD
            sb.append("\n\n");
            sb.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
            sb.append("\n\t").append("public void set").append(formatted).append("(Integer v){");
            sb.append("\n\t\t").append("try{");
            sb.append("\n\t\t").append("setProperty(SCHEMA_ELEMENT_NAME + \"/").append(xmlPath).append("\",v);");
            sb.append("\n\t\t_").append(formatted +"=null;");
            sb.append("\n\t\t").append("} catch (Exception e1) {logger.error(e1);}");
            sb.append("\n\t}");

            
            //STANDARD GET METHOD
            sbI.append("\n\n");
            sbI.append("\t/**\n\t * @return Returns the ").append(xmlPath).append(".\n\t */");
            sbI.append("\n\t").append("public Integer get").append(formatted).append("();");

            //STANDARD SET METHOD
            sbI.append("\n\n");
            sbI.append("\t/**\n\t * Sets the value for ").append(xmlPath).append(".\n\t * @param v Value to Set.\n\t */");
            sbI.append("\n\t").append("public void set").append(formatted).append("(Integer v);");
        }
        
//      ADD ITEM LOADERS
        sb.append("\n\n\tpublic static ArrayList<org.nrg.xdat.om.").append(getSQLClassName(e)).append("> getAll"+ getSQLClassName(e) +"s(org.nrg.xft.security.UserI user,boolean preLoad)");
        sb.append("\n\t{");
        sb.append("\n\t\tArrayList<org.nrg.xdat.om.").append(getSQLClassName(e)).append("> al = new ArrayList<org.nrg.xdat.om.").append(getSQLClassName(e)).append(">();\n");
        sb.append("\n\t\ttry{");
        sb.append("\n\t\t\torg.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);");
        sb.append("\n\t\t\tal = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());");
        sb.append("\n\t\t} catch (Exception e) {");
        sb.append("\n\t\t\tlogger.error(\"\",e);");
        sb.append("\n\t\t}\n");
        sb.append("\n\t\tal.trimToSize();");
        sb.append("\n\t\treturn al;");
        sb.append("\n\t}");
        
        sb.append("\n\n\tpublic static ArrayList<org.nrg.xdat.om.").append(getSQLClassName(e)).append("> get"+ getSQLClassName(e) +"sByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)");
        sb.append("\n\t{");
        sb.append("\n\t\tArrayList<org.nrg.xdat.om.").append(getSQLClassName(e)).append("> al = new ArrayList<org.nrg.xdat.om.").append(getSQLClassName(e)).append(">();");
        sb.append("\n\t\ttry {");
        sb.append("\n\t\t\torg.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);");
        sb.append("\n\t\t\tal = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());");
        sb.append("\n\t\t} catch (Exception e) {");
        sb.append("\n\t\t\tlogger.error(\"\",e);");
        sb.append("\n\t\t}\n");
        sb.append("\n\t\tal.trimToSize();");
        sb.append("\n\t\treturn al;");
        sb.append("\n\t}");
        
        sb.append("\n\n\tpublic static ArrayList<org.nrg.xdat.om.").append(getSQLClassName(e)).append("> get"+ getSQLClassName(e) +"sByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)");
        sb.append("\n\t{");
        sb.append("\n\t\tArrayList<org.nrg.xdat.om.").append(getSQLClassName(e)).append("> al = new ArrayList<org.nrg.xdat.om.").append(getSQLClassName(e)).append(">();");
        sb.append("\n\t\ttry {");
        sb.append("\n\t\t\torg.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);");
        sb.append("\n\t\t\tal = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());");
        sb.append("\n\t\t} catch (Exception e) {");
        sb.append("\n\t\t\tlogger.error(\"\",e);");
        sb.append("\n\t\t}\n");
        sb.append("\n\t\tal.trimToSize();");
        sb.append("\n\t\treturn al;");
        sb.append("\n\t}");
        
        ArrayList keys = e.getAllPrimaryKeys();
        if (keys.size()>1){
        }else{
            GenericWrapperField f = (GenericWrapperField)keys.get(0);
            String xmlPath = f.getXMLPathString();
            String formatted = formatFieldName(xmlPath);
            sb.append("\n\n\tpublic static " + getSQLClassName(e) +" get"+ getSQLClassName(e) +"sBy" + formatted +"(Object value, org.nrg.xft.security.UserI user,boolean preLoad)");
            sb.append("\n\t{");
            sb.append("\n\t\ttry {");
            sb.append("\n\t\t\torg.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(\"" + f.getXMLPathString(e.getXSIType()) + "\",value,user,preLoad);");
            sb.append("\n\t\t\tItemI match = items.getFirst();");
            sb.append("\n\t\t\tif (match!=null)");
            sb.append("\n\t\t\t\treturn ("+ getSQLClassName(e) +") org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);");
            sb.append("\n\t\t\telse");
            sb.append("\n\t\t\t\t return null;");
            sb.append("\n\t\t} catch (Exception e) {");
            sb.append("\n\t\t\tlogger.error(\"\",e);");
            sb.append("\n\t\t}\n");
            sb.append("\n\t\treturn null;");
            sb.append("\n\t}");
        }
        
        if (e.hasUniques()){
            ArrayList uniques = e.getUniqueFields();
            if (uniques.size() >0){
                Iterator iter = uniques.iterator();
                while (iter.hasNext()){
                    GenericWrapperField f = (GenericWrapperField)iter.next();
                    String xmlPath = f.getXMLPathString();
                    String formatted = formatFieldName(xmlPath);
                    sb.append("\n\n\tpublic static " + getSQLClassName(e) + " get"+ getSQLClassName(e) +"sBy" + formatted +"(Object value, org.nrg.xft.security.UserI user,boolean preLoad)");
                    sb.append("\n\t{");
                    sb.append("\n\t\ttry {");
                    sb.append("\n\t\t\torg.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(\"" + f.getXMLPathString(e.getXSIType()) + "\",value,user,preLoad);");
                    sb.append("\n\t\t\tItemI match = items.getFirst();");
                    sb.append("\n\t\t\tif (match!=null)");
                    sb.append("\n\t\t\t\treturn ("+ getSQLClassName(e) +") org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);");
                    sb.append("\n\t\t\telse");
                    sb.append("\n\t\t\t\t return null;");
                    sb.append("\n\t\t} catch (Exception e) {");
                    sb.append("\n\t\t\tlogger.error(\"\",e);");
                    sb.append("\n\t\t}\n");
                    sb.append("\n\t\treturn null;");
                    sb.append("\n\t}");
                }
            }
            
            Hashtable uniqueComposites = e.getUniqueCompositeFields();
        }
        
        sb.append("\n\n\tpublic static ArrayList wrapItems(ArrayList items)");
        sb.append("\n\t{");
        sb.append("\n\t\tArrayList al = new ArrayList();");
        sb.append("\n\t\tal = org.nrg.xdat.base.BaseElement.WrapItems(items);");
        sb.append("\n\t\tal.trimToSize();");
        sb.append("\n\t\treturn al;");
        sb.append("\n\t}");
        sb.append("\n\n\tpublic static ArrayList wrapItems(org.nrg.xft.collections.ItemCollection items)");
        sb.append("\n\t{");
        sb.append("\n\t\treturn wrapItems(items.getItems());");
        sb.append("\n\t}");
        
        String tables = e.getPrimaryElements();
        if (tables.indexOf("xnat:mrAssessorData")!=-1)
        {
            //MR ASSESSOR
            sb.append("\n\n\tpublic org.w3c.dom.Document toJoinedXML() throws Exception");
            sb.append("\n\t{");
            sb.append("\n\t\tArrayList al = new ArrayList();");
            sb.append("\n\t\tal.add(this.getItem());");
            
            sb.append("\n\t\tXFTItem mr = org.nrg.xft.search.ItemSearch.GetItem(\"xnat:mrSessionData.ID\",getItem().getProperty(\""+ e.getFullXMLName() + ".imageSession_ID\"),getItem().getUser(),false);");
            sb.append("\n\t\tal.add(mr);");
            sb.append("\n\t\tal.add(org.nrg.xft.search.ItemSearch.GetItem(\"xnat:subjectData.ID\",mr.getProperty(\"xnat:mrSessionData.subject_ID\"),getItem().getUser(),false));");
            sb.append("\n\t\tal.trimToSize();");
            sb.append("\n\t\treturn org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter.ItemListToDOM(al);");
            sb.append("\n\t}");
        }else if (tables.indexOf("xnat:subjectAssessorData")!=-1)
        {
            //SUBJECT ASSESSOR
            sb.append("\n\n\tpublic org.w3c.dom.Document toJoinedXML() throws Exception");
            sb.append("\n\t{");
            sb.append("\n\t\tArrayList al = new ArrayList();");
            sb.append("\n\t\tal.add(this.getItem());");
            
            sb.append("\n\t\tal.add(org.nrg.xft.search.ItemSearch.GetItem(\"xnat:subjectData.ID\",this.getItem().getProperty(\"xnat:mrSessionData.subject_ID\"),getItem().getUser(),false));");
            sb.append("\n\t\tal.trimToSize();");
            sb.append("\n\t\treturn org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter.ItemListToDOM(al);");
            sb.append("\n\t}");
        }else{
            //UNKNOWN
            
        }
               
        sb.append("\n\t").append("public ArrayList<ResourceFile> getFileResources(String rootPath, boolean preventLoop){");
        sb.append("\n").append("ArrayList<ResourceFile> _return = new ArrayList<ResourceFile>();");
        sb.append("\n\t").append(" boolean localLoop = preventLoop;");
        
        if (e.instanceOf("xnat:abstractResource") && e.isAbstract()){
            sb.append("\n\t").append("//ABSTRACT");
        }else if (e.instanceOf("xnat:abstractResource") && e.isExtension()){
            sb.append("\n\t").append("              int counter=0;");
            sb.append("\n\t").append("              for(java.io.File f: this.getCorrespondingFiles(rootPath)){");
            sb.append("\n\t").append("                 ResourceFile rf = new ResourceFile(f);");
            sb.append("\n\t").append("                 rf.setXpath(\"file/\" + counter +\"\");");
            sb.append("\n\t").append("                 rf.setXdatPath((counter++) +\"\");");
            sb.append("\n\t").append("                 rf.setSize(f.length());");
            sb.append("\n\t").append("                 rf.setAbsolutePath(f.getAbsolutePath());");
            sb.append("\n\t").append("                 _return.add(rf);");
            sb.append("\n\t").append("              }");
        }else{
            for(GenericWrapperField gwf: e.getReferenceFields(true)){
                if (gwf.isPossibleLoop()){
                    sb.append("\n\t").append("    //POSSIBLE NEVER ENDING LOOP");
                    sb.append("\n\t").append("    if (!preventLoop){");
                }
                
                sb.append("\n\t").append("        localLoop = preventLoop;");
                if (gwf.getPreventLoop()){
                    sb.append("\n\t").append("        //POSSIBLE NEVER ENDING LOOP IN SUB-FIELD");
                    sb.append("\n\t").append("        localLoop = true;");
                }
                sb.append("\n\t").append("");
                
                String xmlPath = gwf.getXMLPathString();
                String formatted = formatFieldName(xmlPath);
                if (formatted.equalsIgnoreCase("USER"))
                {
                    formatted = "userProperty";
                }
                SchemaElementI foreign = gwf.getReferenceElement();
                String foreignClassName = getSQLClassName(foreign.getGenericXFTElement());
                
    
                    if (foreign.getGenericXFTElement().getAddin().equals(""))
                    {
                        if (foreign.getGenericXFTElement().instanceOf("xnat:abstractResource")){
                            sb.append("\n\t").append("        //" + xmlPath);
                            if (gwf.isMultiple())
                                sb.append("\n\t").append("        for(XnatAbstractresource child" + formatted + " : this.get" +formatted + "()){");
                            else
                                sb.append("\n\t").append("        XnatAbstractresource child" + formatted + " = (" + foreignClassName + ")this.get" +formatted + "();");
                            sb.append("\n\t").append("            if (child" + formatted + "!=null){");
                            sb.append("\n\t").append("              int counter" + formatted + "=0;");
                            sb.append("\n\t").append("              for(java.io.File f: child" + formatted + ".getCorrespondingFiles(rootPath)){");
                            sb.append("\n\t").append("                 ResourceFile rf = new ResourceFile(f);");
                            sb.append("\n\t").append("                 rf.setXpath(\"" + xmlPath+ "[xnat_abstractresource_id=\" + child" + formatted + ".getXnatAbstractresourceId() + \"]/file/\" + counter" + formatted + " +\"\");");
                            sb.append("\n\t").append("                 rf.setXdatPath(\"" + xmlPath+ "/\" + child" + formatted + ".getXnatAbstractresourceId() + \"/\" + counter" + formatted + "++);");
                            sb.append("\n\t").append("                 rf.setSize(f.length());");
                            sb.append("\n\t").append("                 rf.setAbsolutePath(f.getAbsolutePath());");
                            sb.append("\n\t").append("                 _return.add(rf);");
                            sb.append("\n\t").append("              }");
                            sb.append("\n\t").append("            }");
                            if (gwf.isMultiple())
                                sb.append("\n\t").append("        }");
                            sb.append("\n\t").append("");
                        }else{
                            sb.append("\n\t").append("        //" + xmlPath);
                            if (gwf.isMultiple())
                                sb.append("\n\t").append("        for(" + foreignClassName + " child" + formatted + " : this.get" +formatted + "()){");
                            else
                                sb.append("\n\t").append("        " + foreignClassName + " child" + formatted + " = (" + foreignClassName + ")this.get" +formatted + "();");
                            sb.append("\n\t").append("            if (child" + formatted + "!=null){");
                            sb.append("\n\t").append("              for(ResourceFile rf: child" + formatted + ".getFileResources(rootPath, localLoop)) {");
                            sb.append("\n\t").append("                 rf.setXpath(\"" + xmlPath+ "[\" + child" + formatted + ".getItem().getPKString() + \"]/\" + rf.getXpath());");
                            sb.append("\n\t").append("                 rf.setXdatPath(\"" + xmlPath+ "/\" + child" + formatted + ".getItem().getPKString() + \"/\" + rf.getXpath());");
                            sb.append("\n\t").append("                 _return.add(rf);");
                            sb.append("\n\t").append("              }");
                            sb.append("\n\t").append("            }");
                            if (gwf.isMultiple())
                                sb.append("\n\t").append("        }");
                            sb.append("\n\t").append("");
                        }
                    }
                
                
                if (gwf.isPossibleLoop())
                    sb.append("\n\t").append("    }");
            }
        }
        
        sb.append("\n\t").append("return _return;");
        sb.append("\n").append("}");
        
        
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
        String finalLocation = location + File.separator + StringUtils.ReplaceStr(dirStucture,".",File.separator);
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
	        System.out.println("Generating File " + location +getClassName(e) +".java...");
        FileUtils.OutputToFile(sb.toString(),location +getClassName(e) + ".java");
        

        

        
        //GENERATED BASE CLASS
        if (prefix != null && !prefix.equalsIgnoreCase(""))
        {
            location = location.substring(0,location.lastIndexOf(File.separator));
            location = location.substring(0,location.lastIndexOf(File.separator)) + File.separator;
            
            File file = new File(location + getBaseClassName(e)+".java");
            if (! file.exists())
            {
                sb = new StringBuffer();
                sb.append("/*\n * GENERATED FILE\n * Created on " + Calendar.getInstance().getTime() + "\n *\n */");
                sb.append("\npackage org.nrg.xdat.om.base;");
                sb.append("\nimport org.nrg.xdat.om.base.auto.*;");
                sb.append("\nimport org.nrg.xft.*;");
                sb.append("\nimport org.nrg.xft.security.UserI;");
                sb.append("\n\nimport java.util.*;");
              
                //CLASS COMMENTS
                sb.append("\n\n/**\n * @author XDAT\n *\n */");
                
                //CLASS

                sb.append("\n@SuppressWarnings({\"unchecked\",\"rawtypes\"})");
                
                sb.append("\npublic abstract class ").append(getBaseClassName(e)).append(" extends ").append(getClassName(e)).append(" {");
                //ADD CONSTRUCTORS
                sb.append("\n\n");
                sb.append("\tpublic ").append(getBaseClassName(e)).append("(ItemI item)\n\t{\n\t\tsuper(item);\n\t}");
                
                sb.append("\n\n");
                sb.append("\tpublic ").append(getBaseClassName(e)).append("(UserI user)\n\t{");
                sb.append("\n\t\tsuper(user);\n\t}");

                sb.append("\n");
                sb.append("\n\t/*");
                sb.append("\n\t * @deprecated Use ").append(getBaseClassName(e)).append("(UserI user)");
                sb.append("\n\t **/");
                sb.append("\n\tpublic ").append(getBaseClassName(e)).append("()\n\t{}");
                
                sb.append("\n\n");
                sb.append("\tpublic ").append(getBaseClassName(e)).append("(Hashtable properties, UserI user)\n\t{");
                sb.append("\n\t\tsuper(properties,user);\n\t}");
                sb.append("\n\n}");

                if (XFT.VERBOSE)
        	        System.out.println("Generating File " + location +getBaseClassName(e) +".java...");
                FileUtils.OutputToFile(sb.toString(),location + getBaseClassName(e)+".java");
            }
            
            //CREATE OM CLASS
            location = location.substring(0,location.lastIndexOf(File.separator));
            location = location.substring(0,location.lastIndexOf(File.separator)) + File.separator;
            
            file = new File(location + getSQLClassName(e)+".java");
            if (! file.exists())
            {
                sb = new StringBuffer();
                sb.append("/*\n * GENERATED FILE\n * Created on " + Calendar.getInstance().getTime() + "\n *\n */");
                sb.append("\npackage org.nrg.xdat.om;");
                sb.append("\nimport org.nrg.xft.*;");
                sb.append("\nimport org.nrg.xdat.om.base.*;");
                sb.append("\nimport org.nrg.xft.security.UserI;");
                sb.append("\n\nimport java.util.*;");
              
                //CLASS COMMENTS
                sb.append("\n\n/**\n * @author XDAT\n *\n */");
                

                sb.append("\n@SuppressWarnings({\"unchecked\",\"rawtypes\"})");
                
                if (e.isAbstract()){
                    //CLASS
                    sb.append("\npublic abstract class ").append(getSQLClassName(e)).append(" extends ").append(getBaseClassName(e)).append(" {");
                }else{
                    //CLASS
                    sb.append("\npublic class ").append(getSQLClassName(e)).append(" extends ").append(getBaseClassName(e)).append(" {");
                }
                //ADD CONSTRUCTORS
                sb.append("\n\n");
                sb.append("\tpublic ").append(getSQLClassName(e)).append("(ItemI item)\n\t{\n\t\tsuper(item);\n\t}");
                
                sb.append("\n\n");
                sb.append("\tpublic ").append(getSQLClassName(e)).append("(UserI user)\n\t{");
                sb.append("\n\t\tsuper(user);\n\t}");

                sb.append("\n");
                sb.append("\n\t/*");
                sb.append("\n\t * @deprecated Use ").append(getBaseClassName(e)).append("(UserI user)");
                sb.append("\n\t **/");
                sb.append("\n\tpublic ").append(getSQLClassName(e)).append("()\n\t{}");
                
                sb.append("\n\n");
                sb.append("\tpublic ").append(getSQLClassName(e)).append("(Hashtable properties, UserI user)\n\t{");
                sb.append("\n\t\tsuper(properties,user);\n\t}");
                sb.append("\n\n}");

                if (XFT.VERBOSE)
        	        System.out.println("Generating File " + location +getSQLClassName(e) +".java...");
                FileUtils.OutputToFile(sb.toString(),location + getSQLClassName(e)+".java");
            }
            

            //INTERFACE
                sbI.append("\n}");

                if (XFT.VERBOSE)
        	        System.out.println("Generating File " + location +getSQLClassName(e) +"I.java...");
                FileUtils.OutputToFile(sbI.toString(),location + getSQLClassName(e)+"I.java");
        }
    }
    
    public String getSQLClassName(GenericWrapperElement e)
    {
        return StringUtils.FormatStringToClassName(e.getFormattedName());
    }
    
    public String getBaseClassName(GenericWrapperElement e)
    {
        return "Base" +StringUtils.FormatStringToClassName(e.getFormattedName());
    }
    
    /**
     * @param e
     * @return
     */
    public String getClassName(GenericWrapperElement e)
    {
        return StringUtils.FormatStringToClassName("Auto_" + e.getFormattedName());
    }
    /**
     * @param e
     * @return
     */
    private String formatFieldName(String s)
    {
        return StringUtils.FormatStringToMethodSignature(s);
    }

    /**
     * @return Returns the prefix.
     */
    public String getPrefix() {
        return prefix;
    }
    /**
     * @param prefix The prefix to set.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public static void GenerateJavaFiles(String javalocation, String templateLocation, boolean skipXDAT,boolean generateDisplayDocs) throws Exception
    {

		JavaFileGenerator generator = new JavaFileGenerator();
		
        ArrayList al = XFTMetaManager.GetElementNames();
		Iterator iter = al.iterator();
		if (generateDisplayDocs)
		{
			while (iter.hasNext())
			{
			    String s = (String)iter.next();
			    GenericWrapperElement e = GenericWrapperElement.GetElement(s);
			    if (e.getAddin().equalsIgnoreCase(""))
			    {
			        if ((!skipXDAT) || (!e.getSchemaTargetNamespacePrefix().equalsIgnoreCase("xdat")))
			        {
						generator.generateJavaFile(e,javalocation);
						if (! e.getProperName().equals(e.getFullXMLName()))
						{
						    generator.generateDisplayFile(e);
						}
			        }
			    }
			}
			
			DisplayManager.clean();
		}
				
		//SECOND PASS
		iter = al.iterator();
		while (iter.hasNext())
		{
		    String s = (String)iter.next();
		    GenericWrapperElement e = GenericWrapperElement.GetElement(s);
		    if (e.getAddin().equalsIgnoreCase(""))
		    {
		        if ((!skipXDAT) || (!e.getSchemaTargetNamespacePrefix().equalsIgnoreCase("xdat")))
		        {
					generator.generateJavaFile(e,javalocation);
					if (! e.getProperName().equals(e.getFullXMLName()))
					{
					    generator.generateJavaReportFile(e,javalocation);
					    generator.generateVMReportFile(e,templateLocation);
					    generator.generateJavaEditFile(e,javalocation);
					    generator.generateVMEditFile(e,templateLocation);
					    generator.generateVMSearchFile(e,templateLocation);
					}
		        }
		    }
		}
    }

    

    public void generateJavaEditFile(GenericWrapperElement e, String location) throws Exception
    {
        if (!location.endsWith(File.separator))
        {
            location += File.separator;
        }
        
        File dir = new File(location);
        if (!dir.exists())
        {
            dir.mkdir();
        }
        
        String dirStucture = "org.nrg.xdat.turbine.modules.screens";
        String finalLocation = location + File.separator + StringUtils.ReplaceStr(dirStucture,".",File.separator);
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
        
        File file = new File(location +"XDATScreen_edit_"+ e.getFormattedName() +".java");

            // GENERATE JAVA REPORT
            StringBuffer sb = new StringBuffer();
            
            sb.append("/*\n * GENERATED FILE\n * Created on " + Calendar.getInstance().getTime() + "\n *\n */");
            sb.append("\npackage org.nrg.xdat.turbine.modules.screens;");
            //IMPORTS
            sb.append("\nimport org.apache.turbine.util.RunData;");
            sb.append("\nimport org.apache.velocity.context.Context;");
            sb.append("\nimport org.nrg.xdat.turbine.utils.TurbineUtils;");
            sb.append("\nimport org.nrg.xft.ItemI;");
            sb.append("\nimport org.nrg.xft.XFTItem;");
            
            //CLASS COMMENTS
            sb.append("\n\n/**\n * @author XDAT\n *\n */");

            String tables = e.getPrimaryElements();
            //CLASS
            sb.append("\npublic class XDATScreen_edit_").append(e.getFormattedName());

            if (tables.indexOf("xnat:subjectAssessorData")!=-1){
                sb.append(" extends org.nrg.xnat.turbine.modules.screens.EditSubjectAssessorScreen {");
            }else if (tables.indexOf("xnat:imageAssessorData")!=-1){
                sb.append(" extends org.nrg.xnat.turbine.modules.screens.EditImageAssessorScreen {");
            }else{
                sb.append(" extends org.nrg.xdat.turbine.modules.screens.EditScreenA {");
            }
            sb.append("\n\tstatic org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_").append(e.getFormattedName()).append(".class);");
            sb.append("\n\t/* (non-Javadoc)");
            sb.append("\n\t * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()");
            sb.append("\n\t */");
            sb.append("\n\tpublic String getElementName() {");
            sb.append("\n\t    return \"").append(e.getFullXMLName()).append("\";");
            sb.append("\n\t}");
            sb.append("\n\t");
 
            sb.append("\n\tpublic ItemI getEmptyItem(RunData data) throws Exception");
            sb.append("\n\t{");
            sb.append("\n\t	return super.getEmptyItem(data);");
            sb.append("\n\t}");
            
            sb.append("\n\t/* (non-Javadoc)");
            sb.append("\n\t * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)");
            sb.append("\n\t */");
            sb.append("\n\tpublic void finalProcessing(RunData data, Context context) {");
            if (tables.indexOf("xnat:subjectAssessorData")!=-1){
                sb.append("\n\t\tsuper.finalProcessing(data,context);");
            }
            sb.append("\n\t}");
            sb.append("}");
            
            if (XFT.VERBOSE)
    	        System.out.println("Generating File " + location +"XDATScreen_edit_"+ e.getFormattedName() +".java...");
            FileUtils.OutputToFile(sb.toString(),location +"XDATScreen_edit_"+ e.getFormattedName() +".java");
       
    }

    public void generateJavaReportFile(GenericWrapperElement e, String location) throws Exception
    {
        if (!location.endsWith(File.separator))
        {
            location += File.separator;
        }
        
        File dir = new File(location);
        if (!dir.exists())
        {
            dir.mkdir();
        }
        
        String dirStucture = "org.nrg.xdat.turbine.modules.screens";
        String finalLocation = location + File.separator + StringUtils.ReplaceStr(dirStucture,".",File.separator);
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
        
        File file = new File(location +"XDATScreen_report_"+ e.getFormattedName() +".java");

            // GENERATE JAVA REPORT
            StringBuffer sb = new StringBuffer();
            
            sb.append("/*\n * GENERATED FILE\n * Created on " + Calendar.getInstance().getTime() + "\n *\n */");
            sb.append("\npackage org.nrg.xdat.turbine.modules.screens;");
            //IMPORTS
            sb.append("\nimport org.apache.turbine.util.RunData;");
            sb.append("\nimport org.apache.velocity.context.Context;");
            sb.append("\nimport org.nrg.xdat.turbine.modules.screens.SecureReport;");
            
            //CLASS COMMENTS
            sb.append("\n\n/**\n * @author XDAT\n *\n */");
            
            //CLASS
            sb.append("\npublic class XDATScreen_report_").append(e.getFormattedName()).append(" extends SecureReport {");
            sb.append("\n\tpublic static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_report_").append(e.getFormattedName()).append(".class);");
            
            sb.append("\n\t/* (non-Javadoc)");
            sb.append("\n\t * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)");
            sb.append("\n\t */");
            sb.append("\n\tpublic void finalProcessing(RunData data, Context context) {");
            
            String tables = e.getPrimaryElements();
            if (tables.indexOf("xnat:mrAssessorData")!=-1)
            {
                //MR ASSESSOR
                sb.append("\n\t\ttry{");
                sb.append("\n\t\t\torg.nrg.xdat.om." + getSQLClassName(e) +" om = new org.nrg.xdat.om." + getSQLClassName(e) +"(item);");

                sb.append("\n\t\t\torg.nrg.xdat.om.XnatMrsessiondata mr = om.getMrSessionData();");
                sb.append("\n\t\t\tcontext.put(\"om\",om);");
                sb.append("\n\t\t\tSystem.out.println(\"Loaded om object (org.nrg.xdat.om." + getSQLClassName(e) +") as context parameter 'om'.\");");
                sb.append("\n\t\t\tcontext.put(\"mr\",mr);");
                sb.append("\n\t\t\tSystem.out.println(\"Loaded mr session object (org.nrg.xdat.om.XnatMrsessiondata) as context parameter 'mr'.\");");
                sb.append("\n\t\t\tcontext.put(\"subject\",mr.getSubjectData());");
                sb.append("\n\t\t\tSystem.out.println(\"Loaded subject object (org.nrg.xdat.om.XnatSubjectdata) as context parameter 'subject'.\");");
                sb.append("\n\t\t} catch(Exception e){}");
            }else if (tables.indexOf("xnat:subjectAssessorData")!=-1)
            {
                //SUBJECT ASSESSOR
                sb.append("\n\t\ttry{");
                sb.append("\n\t\t\torg.nrg.xdat.om." + getSQLClassName(e) +" om = new org.nrg.xdat.om." + getSQLClassName(e) +"(item);");
                sb.append("\n\t\t\tcontext.put(\"om\",om);");
                sb.append("\n\t\t\tSystem.out.println(\"Loaded om object (org.nrg.xdat.om." + getSQLClassName(e) +") as context parameter 'om'.\");");
                sb.append("\n\t\t\tcontext.put(\"subject\",om.getSubjectData());");
                sb.append("\n\t\t\tSystem.out.println(\"Loaded subject object (org.nrg.xdat.om.XnatSubjectdata) as context parameter 'subject'.\");");
                sb.append("\n\t\t} catch(Exception e){}");
            }else{
                //UNKNOWN
                
            }
            
            sb.append("\n\t}");
            sb.append("}");
            
            if (XFT.VERBOSE)
    	        System.out.println("Generating File " + location +"XDATScreen_report_"+ e.getFormattedName() +".java...");
            FileUtils.OutputToFile(sb.toString(),location +"XDATScreen_report_"+ e.getFormattedName() +".java");
       
    }
    


    public void generateVMReportFile(GenericWrapperElement e, String location) throws Exception
    {
        item_counter = 0;
        if (!location.endsWith(File.separator))
        {
            location += File.separator;
        }
        
        File dir = new File(location);
        if (!dir.exists())
        {
            dir.mkdir();
        }
        
        File file = new File(location +"XDATScreen_report_"+ e.getFormattedName() +".vm");

        // GENERATE VM REPORT
        String template = getReportTemplate();

        String properName = XFTReferenceManager.GetProperName(e.getFullXMLName());
        if (properName==null)
        {
            properName = e.getFullXMLName();
        }
        
        template = StringUtils.ReplaceStr(template,"@PAGE_TITLE@",properName + " Details");
        
        String header = "\n\t\t\t\t\t\t";
        StringBuffer sb = new StringBuffer();
        sb.append(header).append("<TABLE>");

        GenericWrapperElement ext = e;
        while (ext.isExtension())
        {
            try {
                ext = ext.getExtensionField().getReferenceElement().getGenericXFTElement();
                sb.append(this.getLocalFieldsReport(ext,header + "\t",e.getFullXMLName()));
            } catch (Exception e1) {
            }
        }


        sb.append(this.getLocalFieldsReport(e,header + "\t",e.getFullXMLName()));
        
        sb.append(header).append("</TABLE>");
        

        template = StringUtils.ReplaceStr(template,"@STATIC@",sb.toString());
        
        
        //BUILD CUSTOM CONTENT
        sb = new StringBuffer();
        sb.append(getChildFieldsReport(e,new ArrayList(),"\n\t\t\t",e.getFullXMLName(),true));
        template = StringUtils.ReplaceStr(template,"@CONTENT@",sb.toString());

        String tables = e.getPrimaryElements();
        if (tables.indexOf("xnat:mrAssessorData")!=-1)
        {
            template +="<BR>#parse(\"/screens/ReportProjectSpecificFields.vm\")";
        }else if (tables.indexOf("xnat:subjectAssessorData")!=-1)
        {
            template +="<BR>#parse(\"/screens/ReportProjectSpecificFields.vm\")";
        }else{
            //UNKNOWN
            
        }
        
        if (XFT.VERBOSE)
	        System.out.println("Generating File " + location +"XDATScreen_report_"+ e.getFormattedName() +".vm...");
        FileUtils.OutputToFile(template,location +"XDATScreen_report_"+ e.getFormattedName() +".vm");

    }
    


    public void generateVMSearchFile(GenericWrapperElement e, String location) throws Exception
    {
        item_counter = 0;
        if (!location.endsWith(File.separator))
        {
            location += File.separator;
        }
        
        File dir = new File(location);
        if (!dir.exists())
        {
            dir.mkdir();
        }
        
        File file = new File(location+ e.getFormattedName() +"_search.vm");

           // GENERATE VM REPORT

           String properName = XFTReferenceManager.GetProperName(e.getFullXMLName());
           if (properName==null)
           {
               properName = e.getFullXMLName();
           }
            
           String header = "\n";
           StringBuffer sb = new StringBuffer();
           sb.append(header).append("<TABLE VALIGN=\"top\" style=\"border-right:1px solid #6D99B6;border-left:1px solid #6D99B6;border-top:1px solid #6D99B6;border-bottom:1px solid #6D99B6;\">");

           SchemaElement se = new SchemaElement(e);
           
           ElementDisplay ed = se.getDisplay();
           
           if (ed != null)
           {
               Iterator iter = ed.getSearchableFields(4).iterator();
               while (iter.hasNext())
               {
                   sb.append(header).append("\t").append("<TR>");
                   ArrayList dfs = (ArrayList)iter.next();
                   Iterator iter2 = dfs.iterator();
                   while (iter2.hasNext())
                   {
                       DisplayField df = (DisplayField)iter2.next();
                       sb.append(header).append("\t\t").append("<TD>").append(df.getHeader()).append("</TD>");
                       sb.append(header).append("\t\t").append("<TD>");
                       sb.append(header).append("\t\t\t").append("#xdatSearchField($schemaElement $schemaElement.getDisplayField(\"").append(df.getId()).append("\"))");
                       sb.append(header).append("\t\t").append("</TD>");
                       sb.append(header).append("\t\t").append("");
                   }
                   sb.append(header).append("\t").append("</TR>");
               }
           }
            
            sb.append(header).append("</TABLE>");
            
         

            if (XFT.VERBOSE)
    	        System.out.println("Generating File " + location+ e.getFormattedName() +"_search.vm...");
            FileUtils.OutputToFile(sb.toString(),location + e.getFormattedName() +"_search.vm");

    }

    public void generateVMEditFile(GenericWrapperElement e, String location) throws Exception
    {
        item_counter = 0;
        if (!location.endsWith(File.separator))
        {
            location += File.separator;
        }
        
        File dir = new File(location);
        if (!dir.exists())
        {
            dir.mkdir();
        }
        
        File file = new File(location +"XDATScreen_edit_"+ e.getFormattedName() +".vm");

        // GENERATE VM REPORT
        String template = getEditTemplate();

        String properName = XFTReferenceManager.GetProperName(e.getFullXMLName());
        if (properName==null)
        {
            properName = e.getFullXMLName();
        }
        
        template = StringUtils.ReplaceStr(template,"@PAGE_TITLE@",properName + " Details");
        
        ArrayList<String> ignoreXMLPaths= new ArrayList<String>();
        
        StringBuffer validateForm =new StringBuffer();
        
        String header = "\n\t\t\t\t\t\t";
        StringBuffer sb = new StringBuffer();
        sb.append(header).append("<TABLE>");

        String tables = e.getPrimaryElements();
        if (tables.indexOf("xnat:subjectAssessorData")!=-1){
            template = StringUtils.ReplaceStr(template, "ModifyItem", "ModifySubjectAssessorData");
            
            ignoreXMLPaths.add("xnat:experimentData/project");
            ignoreXMLPaths.add("xnat:experimentData/ID");
            ignoreXMLPaths.add("xnat:subjectAssessorData/subject_ID");
            ignoreXMLPaths.add("xnat:experimentData/sharing/share");

            sb.append(header).append("\t<TR><TD colspan='2'>");
            sb.append(header).append("\t\t#parse(\"/screens/xnat_edit_subjectAssessorData.vm\")");
            sb.append(header).append("\t</TD></TR>");
            
            sb.append(header).append("\t<tr>");
            sb.append(header).append("\t\t<TD colspan=\"2\">#parse(\"/screens/EditProjectSpecificFields.vm\")</TD>");
            sb.append(header).append("\t</tr>");   
            
            validateForm.append("\n<script type=\"text/javascript\">");
            validateForm.append("\nfunction validateForm()");
            validateForm.append("\n{");
            validateForm.append("\n   //INSERT CUSTOM CONTENT HERE");
            validateForm.append("\n");
            validateForm.append("\n   validateSubjectAssessorForm();");
            validateForm.append("\n   return false;");
            validateForm.append("\n}");
            validateForm.append("\n</script>");
            
        }else if(tables.indexOf("xnat:experimentData")!=-1){
            ignoreXMLPaths.add("xnat:experimentData/project");
            ignoreXMLPaths.add("xnat:experimentData/ID");
            ignoreXMLPaths.add("xnat:experimentData/sharing/share");
            
            sb.append(header).append("\t<TR><TD colspan='2'>");
            sb.append(header).append("\t\t<hr>");
            sb.append(header).append("\t\t#parse(\"/screens/xnat_edit_experimentData.vm\")");
            sb.append(header).append("\t\t<HR>");
            sb.append("</TD></TR>");
            sb.append(header).append("\t<tr>");
            sb.append(header).append("\t\t<th align=\"left\">" + properName +" ID</th>");
            sb.append(header).append("\t\t<TD align=\"left\">#xdatStringBox(\"" + e.getFullXMLName() + ".ID\" $item \"\" $vr)</TD>");
            sb.append(header).append("\t</tr>");
            
            sb.append(header).append("\t<tr>");
            sb.append(header).append("\t\t<TD colspan=\"2\">#parse(\"/screens/EditProjectSpecificFields.vm\")</TD>");
            sb.append(header).append("\t</tr>");      
            
            
            
            validateForm.append("\n<script type=\"text/javascript\">");
            validateForm.append("\nfunction validateForm()");
            validateForm.append("\n{");
            validateForm.append("\n   //INSERT CUSTOM CONTENT HERE");
            validateForm.append("\n");
            validateForm.append("\n   validateExperimentForm();");
            validateForm.append("\n   return false;");
            validateForm.append("\n}");
            validateForm.append("\n</script>");
        }else{
            validateForm.append("\n<script type=\"text/javascript\">");
            validateForm.append("\nfunction validateForm()");
            validateForm.append("\n{");
            validateForm.append("\n   //INSERT CUSTOM CONTENT HERE");
            validateForm.append("\n}");
            validateForm.append("\n</script>");
        }
        
        GenericWrapperElement ext = e;
        while (ext.isExtension())
        {
            try {
                ext = ext.getExtensionField().getReferenceElement().getGenericXFTElement();
                sb.append(this.getLocalFieldsEdit(ext,header + "\t",e.getFullXMLName(),ignoreXMLPaths));
            } catch (Exception e1) {
            }
        }


        sb.append(this.getLocalFieldsEdit(e,header + "\t",e.getFullXMLName(),ignoreXMLPaths));
        
        sb.append(header).append("</TABLE>");
        

        template = StringUtils.ReplaceStr(template,"@STATIC@",sb.toString());
        template = StringUtils.ReplaceStr(template,"@VALIDATE_FORM@",validateForm.toString());
        
        //BUILD CUSTOM CONTENT
        sb = new StringBuffer();
        sb.append(getChildFieldsEdit(e,new ArrayList(),"\n\t\t\t",e.getFullXMLName(),true,ignoreXMLPaths));
        template = StringUtils.ReplaceStr(template,"@CONTENT@",sb.toString());

        if (XFT.VERBOSE)
	        System.out.println("Generating File " + location +"XDATScreen_edit_"+ e.getFormattedName() +".vm...");
        FileUtils.OutputToFile(template,location +"XDATScreen_edit_"+ e.getFormattedName() +".vm");

    }
    


    public void generateDisplayFile(GenericWrapperElement e) throws Exception
    {
        item_counter = 0;
        
        String location =e.getSchema().getDataModel().getFileLocation();

        if (!location.endsWith(File.separator))
        {
            location += File.separator;
        }
        
        location += "display" + File.separator;
        
        File dir = new File(location);
        if (!dir.exists())
        {
            dir.mkdir();
        }
        
        File file = new File(location+ e.getFormattedName() +"_display.xml");
        File file2 = new File(location+ e.getName() +"_display.xml");

        if (!file.exists() && !file2.exists())
        {
            String header = "\n";
            StringBuffer sb = new StringBuffer();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            sb.append(header).append("<Displays xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"../../xdat/display.xsd\" schema-element=\"");
            sb.append(e.getFullXMLName()).append("\" full-description=\"").append(e.getProperName()).append("\" brief-description=\"").append(e.getProperName()).append("\">");

            String tables = e.getPrimaryElements();
            if (tables.indexOf("xnat:mrAssessorData")!=-1)
            {
                //MR ASSESSOR
                sb.append(header).append("\t<Arc name=\"ASSESSOR\">");
                sb.append(header).append("\t\t<CommonField id=\"EXPT_ID\" local-field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t<CommonField id=\"ASSESSED_EXPT_ID\" local-field=\"SESSION_ID\"/>");
                sb.append(header).append("\t</Arc>");
                
                sb.append(header).append("\t<Arc name=\"PARTICIPANT_EXPERIMENT\">");
                sb.append(header).append("\t\t<CommonField id=\"PART_ID\" local-field=\"SUBJECT_ID\"/>");
                sb.append(header).append("\t\t<CommonField id=\"DATE\" local-field=\"MR_DATE\"/>");
                sb.append(header).append("\t\t<CommonField id=\"EXPT_ID\" local-field=\"EXPT_ID\"/>");
                sb.append(header).append("\t</Arc>");
                
                sb.append(header).append("\t<DisplayField id=\"SESSION_ID\" header=\"Session\" visible=\"true\" searchable=\"true\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".imageSession_ID\"/>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"none\"/>");
                sb.append(header).append("\t\t\t<Property name=\"ONCLICK\" value=\"return rpt('@Field1','xnat:mrSessionData','xnat:mrSessionData.ID');\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"SESSION_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"SUBJECT_ID\" header=\"Subject\" visible=\"true\" searchable=\"true\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"xnat:mrSessionData.subject_ID\"/>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"none\"/>");
                sb.append(header).append("\t\t\t<Property name=\"ONCLICK\" value=\"return rpt('@Field1','xnat:subjectData','xnat:subjectData.ID');\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"SUBJECT_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"MR_DATE\" header=\"MR Date\" visible=\"true\" searchable=\"true\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"xnat:mrSessionData.date\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"EXPT_ID\" header=\"ID\" visible=\"false\" searchable=\"true\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".ID\"/>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"none\"/>");
                sb.append(header).append("\t\t\t<Property name=\"ONCLICK\" value=\"return rpt('@Field1','").append(e.getFullXMLName()).append("','").append(e.getFullXMLName()).append(".ID');\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"RPT\" header=\"ID\" visible=\"true\" image=\"true\">");
                sb.append(header).append("\t\t<Content type=\"sql\">'/@WEBAPP/images/r.gif'::text</Content>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"none\"/>");
                sb.append(header).append("\t\t\t<Property name=\"ONCLICK\" value=\"return rpt('@Field1','").append(e.getFullXMLName()).append("','").append(e.getFullXMLName()).append(".ID');\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"DATE\" header=\"Date\" visible=\"true\" searchable=\"true\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".date\"/>");
                sb.append(header).append("\t</DisplayField>");                

                sb.append(header).append("\t<DisplayField id=\"AGE\" header=\"Age\" visible=\"true\" searchable=\"true\" data-type=\"integer\">");
                sb.append(header).append("\t<DisplayFieldElement name=\"Field1\" schema-element=\"xnat:mrSessionData.date\"/>");
                sb.append(header).append("\t<DisplayFieldElement name=\"Field2\" schema-element=\"xnat:demographicData.dob\"/>");
                sb.append(header).append("\t<DisplayFieldElement name=\"Field3\" schema-element=\"xnat:demographicData.yob\"/>");
                sb.append(header).append("\t<Content type=\"sql\">CAST(COALESCE(FLOOR(CAST((CAST(((@Field1) - (@Field2))AS FLOAT4)/365) AS numeric)),FLOOR((EXTRACT(YEAR FROM @Field1)) - (@Field3))) AS numeric)</Content>");
                sb.append(header).append("\t</DisplayField>");
                

                sb.append(header).append("\t<DisplayField header=\"Projects\" id=\"PROJECTS\" data-type=\"string\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" viewName=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECTS\" viewColumn=\"PROJECTS\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField header=\"Label\" id=\"LABEL\" data-type=\"string\">");
                sb.append(header).append("\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".ID\"/>");
                sb.append(header).append("\t<DisplayFieldElement name=\"Field2\" schema-element=\"").append(e.getFullXMLName()).append(".label\"/>");
                sb.append(header).append("\t<Content type=\"sql\">COALESCE(@Field2, @Field1)</Content>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<SecureLink elementName=\"").append(e.getFullXMLName()).append("\">");
                sb.append(header).append("\t\t\t\t<securityMappingValue displayFieldId=\"PROJECTS\" schemaElementMap=\"").append(e.getFullXMLName()).append("/sharing/share/project\"/>");
                sb.append(header).append("\t\t\t\t<securityMappingValue displayFieldId=\"PROJECT\" schemaElementMap=\"").append(e.getFullXMLName()).append("/project\"/>");
                sb.append(header).append("\t\t\t</SecureLink>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"@WEBAPPapp/action/DisplayItemAction/search_value/@Field1/search_element/").append(e.getFullXMLName()).append("/search_field/").append(e.getFullXMLName()).append(".ID\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t\t<Property name=\"ONCLICK\" value=\"return rpt('@Field1','").append(e.getFullXMLName()).append("','").append(e.getFullXMLName()).append(".ID');\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t\t<Property name=\"TITLE\" value=\"Inserted: @Field1 (@Field2)\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"INSERT_DATE\"/>");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field2\" field=\"INSERT_USER\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"PROJECT\" header=\"Project\" visible=\"true\" searchable=\"true\" data-type=\"string\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".project\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECT_IDENTIFIER\" header=\"").append(e.getFormattedName().toUpperCase()).append(" ID\" visible=\"true\" searchable=\"false\" data-type=\"string\" xsi:type=\"SubQueryField\">");
                sb.append(header).append("\t\t<Content type=\"sql\">").append(e.getFormattedName().toLowerCase()).append("_project_id</Content>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<SecureLink elementName=\"").append(e.getFullXMLName()).append("\">");
                sb.append(header).append("\t\t\t\t<securityMappingValue displayFieldId=\"PROJECTS\" schemaElementMap=\"").append(e.getFullXMLName()).append("/sharing/share/project\"/>");
                sb.append(header).append("\t\t\t\t<securityMappingValue displayFieldId=\"PROJECT\" schemaElementMap=\"").append(e.getFullXMLName()).append("/project\"/>");
                sb.append(header).append("\t\t\t</SecureLink>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"@WEBAPPapp/action/DisplayItemAction/search_value/@Field1/search_element/").append(e.getFullXMLName()).append("/search_field/").append(e.getFullXMLName()).append(".ID/project/@Field2\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field2\" field=\"@WHERE\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t\t<SubQuery>SELECT DISTINCT COALESCE(label,sharing_share_xnat_experimentda_id) AS ").append(e.getFormattedName().toLowerCase()).append("_project_id,sharing_share_xnat_experimentda_id FROM (	SELECT sharing_share_xnat_experimentda_id,label FROM xnat_experimentdata_share WHERE project='@WHERE'	UNION 	SELECT id,label FROM xnat_experimentData WHERE project='@WHERE' )SEARCH</SubQuery>");
                sb.append(header).append("\t\t<MappingColumns>");
                sb.append(header).append("\t\t\t<MappingColumn schemaField=\"").append(e.getFullXMLName()).append(".ID\" queryField=\"sharing_share_xnat_experimentda_id\"/>");
                sb.append(header).append("\t\t</MappingColumns>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"").append(e.getFormattedName().toUpperCase()).append("_FIELD_MAP\" header=\"Field\" visible=\"true\" searchable=\"false\" data-type=\"string\" xsi:type=\"SubQueryField\">");
                sb.append(header).append("\t\t<Content type=\"sql\">field</Content>");
                sb.append(header).append("\t\t<SubQuery>SELECT DISTINCT ON ( e.ID) e.ID AS expt_id,field FROM xnat_experimentData_field ef JOIN (SELECT ID,extension,element_name FROM xnat_experimentData e JOIN xdat_meta_element xme ON e.extension=xme.xdat_meta_element_id WHERE xme.element_name='").append(e.getFullXMLName()).append("') e on ef.fields_field_xnat_experimentdat_id=e.id WHERE name='@WHERE'</SubQuery>");
                sb.append(header).append("\t\t<MappingColumns>");
                sb.append(header).append("\t\t\t<MappingColumn schemaField=\"").append(e.getFullXMLName()).append(".ID\" queryField=\"expt_id\"/>");
                sb.append(header).append("\t\t</MappingColumns>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"INSERT_DATE\" header=\"Inserted\" visible=\"true\" searchable=\"true\" data-type=\"string\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".meta.insert_date\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"INSERT_USER\" header=\"Creator\" visible=\"true\" searchable=\"true\" data-type=\"string\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".meta.insert_user.login\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                ArrayList localFields = new ArrayList();
                Iterator fields = e.getAllFields(true,true).iterator();
                while (fields.hasNext())
                {
                    GenericWrapperField f= (GenericWrapperField)fields.next();
                    if (!f.isReference())
                    {
                        String id = f.getSQLName().toUpperCase();
                        localFields.add(id);
                        sb.append(header).append("\t<DisplayField id=\"").append(id).append("\" header=\"").append(f.getName()).append("\" visible=\"true\" searchable=\"true\">");
                        sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(f.getXMLPathString(e.getFullXMLName())).append("\"/>");
                        sb.append(header).append("\t</DisplayField>");
                    }
                }
                
                sb.append(header).append("\t<DisplayVersion versionName=\"listing\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"RPT\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"LABEL\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"LABEL\"  element_name=\"xnat:mrSessionData\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"SUBJECT_LABEL\" element_name=\"xnat:subjectData\"/>");
                
                Iterator iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                
                sb.append(header).append("\t</DisplayVersion>");
                
                sb.append(header).append("\t<DisplayVersion versionName=\"listing_csv\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"LABEL\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"LABEL\"  element_name=\"xnat:mrSessionData\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"SUBJECT_LABEL\" element_name=\"xnat:subjectData\"/>");
                iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                sb.append(header).append("\t</DisplayVersion>");
                
                //full
                sb.append(header).append("\t<DisplayVersion versionName=\"full\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"LABEL\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"LABEL\"  element_name=\"xnat:mrSessionData\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"SUBJECT_LABEL\" element_name=\"xnat:subjectData\"/>");
                iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                sb.append(header).append("\t</DisplayVersion>");

                //DETAILED
                sb.append(header).append("\t<DisplayVersion versionName=\"detailed\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                sb.append(header).append("\t</DisplayVersion>");
                
                //project bundle
                sb.append(header).append("\t<DisplayVersion versionName=\"project_bundle\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECT_IDENTIFIER\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"MR_PROJECT_IDENTIFIER\" element_name=\"xnat:mrSessionData\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"SUB_PROJECT_IDENTIFIER\" element_name=\"xnat:subjectData\"/>");

                iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                sb.append(header).append("\t</DisplayVersion>");
                

                sb.append(header).append("\t<ViewLink alias=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECTS\">");
                sb.append(header).append("\t\t<Mapping TableName=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECTS\">");
                sb.append(header).append("\t\t\t<MappingColumn rootElement=\"").append(e.getFullXMLName()).append("\" fieldElement=\"").append(e.getFullXMLName()).append(".ID\" mapsTo=\"id\"/>");
                sb.append(header).append("\t\t</Mapping>");
                sb.append(header).append("\t</ViewLink>");
                
                sb.append(header).append("\t<SQLView name=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECTS\" sql=\"SELECT id, '&lt;' || expt.project || '&gt;' || xs_a_concat(', ' || shared.project) AS projects FROM xnat_experimentData expt LEFT JOIN xnat_experimentData_share shared ON expt.id=shared.sharing_share_xnat_experimentda_id LEFT JOIN xdat_meta_element xme ON expt.extension = xme.xdat_meta_element_id WHERE element_name='").append(e.getFullXMLName()).append("' GROUP BY expt.id,expt.project\"/>");
                
                

                sb.append(header).append("</Displays>");
                
                if (XFT.VERBOSE)
        	        System.out.println("Generating File " + location+ e.getFormattedName() +"_display.xml");
                FileUtils.OutputToFile(sb.toString(),location+ e.getFormattedName() +"_display.xml");
            }else if (tables.indexOf("xnat:subjectAssessorData")!=-1)
            {
                //SUBJECT ASSESSOR
                sb.append(header).append("\t<Arc name=\"PARTICIPANT_EXPERIMENT\">");
                sb.append(header).append("\t\t<CommonField id=\"PART_ID\" local-field=\"SUBJECT_ID\"/>");
                sb.append(header).append("\t\t<CommonField id=\"DATE\" local-field=\"DATE\"/>");
                sb.append(header).append("\t\t<CommonField id=\"EXPT_ID\" local-field=\"EXPT_ID\"/>");
                sb.append(header).append("\t</Arc>");
                
                sb.append(header).append("\t<DisplayField id=\"SUBJECT_ID\" header=\"Subject\" visible=\"true\" searchable=\"true\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".subject_ID\"/>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"none\"/>");
                sb.append(header).append("\t\t\t<Property name=\"ONCLICK\" value=\"return rpt('@Field1','xnat:subjectData','xnat:subjectData.ID');\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"SUBJECT_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"EXPT_ID\" header=\"ID\" visible=\"true\" searchable=\"true\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".ID\"/>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"none\"/>");
                sb.append(header).append("\t\t\t<Property name=\"ONCLICK\" value=\"return rpt('@Field1','").append(e.getFullXMLName()).append("','").append(e.getFullXMLName()).append(".ID');\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"RPT\" header=\"ID\" visible=\"true\" image=\"true\">");
                sb.append(header).append("\t\t<Content type=\"sql\">'/@WEBAPP/images/r.gif'::text</Content>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"none\"/>");
                sb.append(header).append("\t\t\t<Property name=\"ONCLICK\" value=\"return rpt('@Field1','").append(e.getFullXMLName()).append("','").append(e.getFullXMLName()).append(".ID');\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"DATE\" header=\"Date\" visible=\"true\" searchable=\"true\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".date\"/>");
                sb.append(header).append("\t</DisplayField>");
                                
                sb.append(header).append("\t<DisplayField id=\"AGE\" header=\"Age\" visible=\"true\" searchable=\"true\" data-type=\"integer\">");
                sb.append(header).append("\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".date\"/>");
                sb.append(header).append("\t<DisplayFieldElement name=\"Field2\" schema-element=\"xnat:demographicData.dob\"/>");
                sb.append(header).append("\t<DisplayFieldElement name=\"Field3\" schema-element=\"xnat:demographicData.yob\"/>");
                sb.append(header).append("\t<Content type=\"sql\">CAST(COALESCE(FLOOR(CAST((CAST(((@Field1) - (@Field2))AS FLOAT4)/365) AS numeric)),FLOOR((EXTRACT(YEAR FROM @Field1)) - (@Field3))) AS numeric)</Content>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField header=\"Projects\" id=\"PROJECTS\" data-type=\"string\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" viewName=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECTS\" viewColumn=\"PROJECTS\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField header=\"Label\" id=\"LABEL\" data-type=\"string\">");
                sb.append(header).append("\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".ID\"/>");
                sb.append(header).append("\t<DisplayFieldElement name=\"Field2\" schema-element=\"").append(e.getFullXMLName()).append(".label\"/>");
                sb.append(header).append("\t<Content type=\"sql\">COALESCE(@Field2, @Field1)</Content>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<SecureLink elementName=\"").append(e.getFullXMLName()).append("\">");
                sb.append(header).append("\t\t\t\t<securityMappingValue displayFieldId=\"PROJECTS\" schemaElementMap=\"").append(e.getFullXMLName()).append("/sharing/share/project\"/>");
                sb.append(header).append("\t\t\t\t<securityMappingValue displayFieldId=\"PROJECT\" schemaElementMap=\"").append(e.getFullXMLName()).append("/project\"/>");
                sb.append(header).append("\t\t\t</SecureLink>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"@WEBAPPapp/action/DisplayItemAction/search_value/@Field1/search_element/").append(e.getFullXMLName()).append("/search_field/").append(e.getFullXMLName()).append(".ID\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t\t<Property name=\"ONCLICK\" value=\"return rpt('@Field1','").append(e.getFullXMLName()).append("','").append(e.getFullXMLName()).append(".ID');\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t\t<Property name=\"TITLE\" value=\"Inserted: @Field1 (@Field2)\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"INSERT_DATE\"/>");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field2\" field=\"INSERT_USER\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"PROJECT\" header=\"Project\" visible=\"true\" searchable=\"true\" data-type=\"string\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".project\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECT_IDENTIFIER\" header=\"").append(e.getFormattedName().toUpperCase()).append(" ID\" visible=\"true\" searchable=\"false\" data-type=\"string\" xsi:type=\"SubQueryField\">");
                sb.append(header).append("\t\t<Content type=\"sql\">").append(e.getFormattedName().toLowerCase()).append("_project_id</Content>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<SecureLink elementName=\"").append(e.getFullXMLName()).append("\">");
                sb.append(header).append("\t\t\t\t<securityMappingValue displayFieldId=\"PROJECTS\" schemaElementMap=\"").append(e.getFullXMLName()).append("/sharing/share/project\"/>");
                sb.append(header).append("\t\t\t\t<securityMappingValue displayFieldId=\"PROJECT\" schemaElementMap=\"").append(e.getFullXMLName()).append("/project\"/>");
                sb.append(header).append("\t\t\t</SecureLink>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"@WEBAPPapp/action/DisplayItemAction/search_value/@Field1/search_element/").append(e.getFullXMLName()).append("/search_field/").append(e.getFullXMLName()).append(".ID/project/@Field2\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field2\" field=\"@WHERE\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t\t<SubQuery>SELECT DISTINCT COALESCE(label,sharing_share_xnat_experimentda_id) AS ").append(e.getFormattedName().toLowerCase()).append("_project_id,sharing_share_xnat_experimentda_id FROM (	SELECT sharing_share_xnat_experimentda_id,label FROM xnat_experimentdata_share WHERE project='@WHERE'	UNION 	SELECT id,label FROM xnat_experimentData WHERE project='@WHERE' )SEARCH</SubQuery>");
                sb.append(header).append("\t\t<MappingColumns>");
                sb.append(header).append("\t\t\t<MappingColumn schemaField=\"").append(e.getFullXMLName()).append(".ID\" queryField=\"sharing_share_xnat_experimentda_id\"/>");
                sb.append(header).append("\t\t</MappingColumns>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"INSERT_DATE\" header=\"Inserted\" visible=\"true\" searchable=\"true\" data-type=\"string\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".meta.insert_date\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"INSERT_USER\" header=\"Creator\" visible=\"true\" searchable=\"true\" data-type=\"string\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".meta.insert_user.login\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"").append(e.getFormattedName().toUpperCase()).append("_FIELD_MAP\" header=\"Field\" visible=\"true\" searchable=\"false\" data-type=\"string\" xsi:type=\"SubQueryField\">");
                sb.append(header).append("\t\t<Content type=\"sql\">field</Content>");
                sb.append(header).append("\t\t<SubQuery>SELECT DISTINCT ON ( e.ID) e.ID AS expt_id,field FROM xnat_experimentData_field ef JOIN (SELECT ID,extension,element_name FROM xnat_experimentData e JOIN xdat_meta_element xme ON e.extension=xme.xdat_meta_element_id WHERE xme.element_name='").append(e.getFullXMLName()).append("') e on ef.fields_field_xnat_experimentdat_id=e.id WHERE name='@WHERE'</SubQuery>");
                sb.append(header).append("\t\t<MappingColumns>");
                sb.append(header).append("\t\t\t<MappingColumn schemaField=\"").append(e.getFullXMLName()).append(".ID\" queryField=\"expt_id\"/>");
                sb.append(header).append("\t\t</MappingColumns>");
                sb.append(header).append("\t</DisplayField>");
                
                ArrayList localFields = new ArrayList();
                Iterator fields = e.getAllFields(true,true).iterator();
                while (fields.hasNext())
                {
                    GenericWrapperField f= (GenericWrapperField)fields.next();
                    if (!f.isReference())
                    {
                        String id = f.getSQLName().toUpperCase();
                        localFields.add(id);
                        sb.append(header).append("\t<DisplayField id=\"").append(id).append("\" header=\"").append(f.getName()).append("\" visible=\"true\" searchable=\"true\">");
                        sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(f.getXMLPathString(e.getFullXMLName())).append("\"/>");
                        sb.append(header).append("\t</DisplayField>");
                    }
                }
                
                sb.append(header).append("\t<DisplayVersion versionName=\"listing\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"RPT\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"LABEL\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"SUBJECT_LABEL\" element_name=\"xnat:subjectData\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"DATE\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"GENDER\" element_name=\"xnat:subjectData\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"AGE\"/>");
                
                Iterator iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                
                sb.append(header).append("\t</DisplayVersion>");
                
                sb.append(header).append("\t<DisplayVersion versionName=\"listing_csv\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"LABEL\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"SUBJECT_LABEL\" element_name=\"xnat:subjectData\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"DATE\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"AGE\"/>");
                iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                sb.append(header).append("\t</DisplayVersion>");
                
                sb.append(header).append("\t<DisplayVersion versionName=\"full\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"SUBJECT_LABEL\" element_name=\"xnat:subjectData\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"DATE\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"AGE\"/>");
                iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                sb.append(header).append("\t</DisplayVersion>");

                //detailed
                sb.append(header).append("\t<DisplayVersion versionName=\"detailed\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                sb.append(header).append("\t</DisplayVersion>");

                
                sb.append(header).append("\t<DisplayVersion versionName=\"project_bundle\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECT_IDENTIFIER\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"SUB_PROJECT_IDENTIFIER\" element_name=\"xnat:subjectData\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"DATE\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"AGE\"/>");
                iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                sb.append(header).append("\t</DisplayVersion>");
                

                sb.append(header).append("\t<ViewLink alias=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECTS\">");
                sb.append(header).append("\t\t<Mapping TableName=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECTS\">");
                sb.append(header).append("\t\t\t<MappingColumn rootElement=\"").append(e.getFullXMLName()).append("\" fieldElement=\"").append(e.getFullXMLName()).append(".ID\" mapsTo=\"id\"/>");
                sb.append(header).append("\t\t</Mapping>");
                sb.append(header).append("\t</ViewLink>");
                
                sb.append(header).append("\t<SQLView name=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECTS\" sql=\"SELECT id, '&lt;' || expt.project || '&gt;' || xs_a_concat(', ' || shared.project) AS projects FROM xnat_experimentData expt LEFT JOIN xnat_experimentData_share shared ON expt.id=shared.sharing_share_xnat_experimentda_id LEFT JOIN xdat_meta_element xme ON expt.extension = xme.xdat_meta_element_id WHERE element_name='").append(e.getFullXMLName()).append("' GROUP BY expt.id,expt.project\"/>");
                
                sb.append(header).append("</Displays>");
                
                if (XFT.VERBOSE)
        	        System.out.println("Generating File " + location+ e.getFormattedName() +"_display.xml");
                FileUtils.OutputToFile(sb.toString(),location+ e.getFormattedName() +"_display.xml");
            }else if (tables.indexOf("xnat:experimentData")!=-1)
            {                
                sb.append(header).append("\t<DisplayField id=\"EXPT_ID\" header=\"ID\" visible=\"true\" searchable=\"true\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".ID\"/>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"none\"/>");
                sb.append(header).append("\t\t\t<Property name=\"ONCLICK\" value=\"return rpt('@Field1','").append(e.getFullXMLName()).append("','").append(e.getFullXMLName()).append(".ID');\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"RPT\" header=\"ID\" visible=\"true\" image=\"true\">");
                sb.append(header).append("\t\t<Content type=\"sql\">'/@WEBAPP/images/r.gif'::text</Content>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"none\"/>");
                sb.append(header).append("\t\t\t<Property name=\"ONCLICK\" value=\"return rpt('@Field1','").append(e.getFullXMLName()).append("','").append(e.getFullXMLName()).append(".ID');\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"DATE\" header=\"Date\" visible=\"true\" searchable=\"true\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".date\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField header=\"Projects\" id=\"PROJECTS\" data-type=\"string\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" viewName=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECTS\" viewColumn=\"PROJECTS\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField header=\"Label\" id=\"LABEL\" data-type=\"string\">");
                sb.append(header).append("\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".ID\"/>");
                sb.append(header).append("\t<DisplayFieldElement name=\"Field2\" schema-element=\"").append(e.getFullXMLName()).append(".label\"/>");
                sb.append(header).append("\t<Content type=\"sql\">COALESCE(@Field2, @Field1)</Content>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<SecureLink elementName=\"").append(e.getFullXMLName()).append("\">");
                sb.append(header).append("\t\t\t\t<securityMappingValue displayFieldId=\"PROJECTS\" schemaElementMap=\"").append(e.getFullXMLName()).append("/sharing/share/project\"/>");
                sb.append(header).append("\t\t\t\t<securityMappingValue displayFieldId=\"PROJECT\" schemaElementMap=\"").append(e.getFullXMLName()).append("/project\"/>");
                sb.append(header).append("\t\t\t</SecureLink>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"@WEBAPPapp/action/DisplayItemAction/search_value/@Field1/search_element/").append(e.getFullXMLName()).append("/search_field/").append(e.getFullXMLName()).append(".ID\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t\t<Property name=\"ONCLICK\" value=\"return rpt('@Field1','").append(e.getFullXMLName()).append("','").append(e.getFullXMLName()).append(".ID');\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t\t<Property name=\"TITLE\" value=\"Inserted: @Field1 (@Field2)\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"INSERT_DATE\"/>");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field2\" field=\"INSERT_USER\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"PROJECT\" header=\"Project\" visible=\"true\" searchable=\"true\" data-type=\"string\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".project\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECT_IDENTIFIER\" header=\"").append(e.getFormattedName().toUpperCase()).append(" ID\" visible=\"true\" searchable=\"false\" data-type=\"string\" xsi:type=\"SubQueryField\">");
                sb.append(header).append("\t\t<Content type=\"sql\">").append(e.getFormattedName().toLowerCase()).append("_project_id</Content>");
                sb.append(header).append("\t\t<HTML-Link>");
                sb.append(header).append("\t\t\t<SecureLink elementName=\"").append(e.getFullXMLName()).append("\">");
                sb.append(header).append("\t\t\t\t<securityMappingValue displayFieldId=\"PROJECTS\" schemaElementMap=\"").append(e.getFullXMLName()).append("/sharing/share/project\"/>");
                sb.append(header).append("\t\t\t\t<securityMappingValue displayFieldId=\"PROJECT\" schemaElementMap=\"").append(e.getFullXMLName()).append("/project\"/>");
                sb.append(header).append("\t\t\t</SecureLink>");
                sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"@WEBAPPapp/action/DisplayItemAction/search_value/@Field1/search_element/").append(e.getFullXMLName()).append("/search_field/").append(e.getFullXMLName()).append(".ID/project/@Field2\">");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t\t\t<InsertValue id=\"Field2\" field=\"@WHERE\"/>");
                sb.append(header).append("\t\t\t</Property>");
                sb.append(header).append("\t\t</HTML-Link>");
                sb.append(header).append("\t\t<SubQuery>SELECT DISTINCT COALESCE(label,sharing_share_xnat_experimentda_id) AS ").append(e.getFormattedName().toLowerCase()).append("_project_id,sharing_share_xnat_experimentda_id FROM (	SELECT sharing_share_xnat_experimentda_id,label FROM xnat_experimentdata_share WHERE project='@WHERE'	UNION 	SELECT id,label FROM xnat_experimentData WHERE project='@WHERE' )SEARCH</SubQuery>");
                sb.append(header).append("\t\t<MappingColumns>");
                sb.append(header).append("\t\t\t<MappingColumn schemaField=\"").append(e.getFullXMLName()).append(".ID\" queryField=\"sharing_share_xnat_experimentda_id\"/>");
                sb.append(header).append("\t\t</MappingColumns>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"INSERT_DATE\" header=\"Inserted\" visible=\"true\" searchable=\"true\" data-type=\"string\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".meta.insert_date\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"INSERT_USER\" header=\"Creator\" visible=\"true\" searchable=\"true\" data-type=\"string\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".meta.insert_user.login\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"").append(e.getFormattedName().toUpperCase()).append("_FIELD_MAP\" header=\"Field\" visible=\"true\" searchable=\"false\" data-type=\"string\" xsi:type=\"SubQueryField\">");
                sb.append(header).append("\t\t<Content type=\"sql\">field</Content>");
                sb.append(header).append("\t\t<SubQuery>SELECT DISTINCT ON ( e.ID) e.ID AS expt_id,field FROM xnat_experimentData_field ef JOIN (SELECT ID,extension,element_name FROM xnat_experimentData e JOIN xdat_meta_element xme ON e.extension=xme.xdat_meta_element_id WHERE xme.element_name='").append(e.getFullXMLName()).append("') e on ef.fields_field_xnat_experimentdat_id=e.id WHERE name='@WHERE'</SubQuery>");
                sb.append(header).append("\t\t<MappingColumns>");
                sb.append(header).append("\t\t\t<MappingColumn schemaField=\"").append(e.getFullXMLName()).append(".ID\" queryField=\"expt_id\"/>");
                sb.append(header).append("\t\t</MappingColumns>");
                sb.append(header).append("\t</DisplayField>");
                
                ArrayList localFields = new ArrayList();
                Iterator fields = e.getAllFields(true,true).iterator();
                while (fields.hasNext())
                {
                    GenericWrapperField f= (GenericWrapperField)fields.next();
                    if (!f.isReference())
                    {
                        String id = f.getSQLName().toUpperCase();
                        localFields.add(id);
                        sb.append(header).append("\t<DisplayField id=\"").append(id).append("\" header=\"").append(f.getName()).append("\" visible=\"true\" searchable=\"true\">");
                        sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(f.getXMLPathString(e.getFullXMLName())).append("\"/>");
                        sb.append(header).append("\t</DisplayField>");
                    }
                }
                
                sb.append(header).append("\t<DisplayVersion versionName=\"listing\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"RPT\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"LABEL\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"DATE\"/>");
                
                Iterator iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                
                sb.append(header).append("\t</DisplayVersion>");
                
                sb.append(header).append("\t<DisplayVersion versionName=\"listing_csv\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"LABEL\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"DATE\"/>");
                iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                sb.append(header).append("\t</DisplayVersion>");
                
                sb.append(header).append("\t<DisplayVersion versionName=\"full\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"DATE\"/>");
                iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                sb.append(header).append("\t</DisplayVersion>");


                //detailed
                sb.append(header).append("\t<DisplayVersion versionName=\"detailed\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"EXPT_ID\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"DATE\"/>");
                iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                sb.append(header).append("\t</DisplayVersion>");

                
                sb.append(header).append("\t<DisplayVersion versionName=\"project_bundle\" default-order-by=\"DATE\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECT_IDENTIFIER\"/>");
                sb.append(header).append("\t\t<DisplayFieldRef id=\"DATE\"/>");
                iter = localFields.iterator();
                while (iter.hasNext())
                {
                    String id = (String)iter.next();
                    sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                }
                sb.append(header).append("\t</DisplayVersion>");
                

                sb.append(header).append("\t<ViewLink alias=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECTS\">");
                sb.append(header).append("\t\t<Mapping TableName=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECTS\">");
                sb.append(header).append("\t\t\t<MappingColumn rootElement=\"").append(e.getFullXMLName()).append("\" fieldElement=\"").append(e.getFullXMLName()).append(".ID\" mapsTo=\"id\"/>");
                sb.append(header).append("\t\t</Mapping>");
                sb.append(header).append("\t</ViewLink>");
                
                sb.append(header).append("\t<SQLView name=\"").append(e.getFormattedName().toUpperCase()).append("_PROJECTS\" sql=\"SELECT id, '&lt;' || expt.project || '&gt;' || xs_a_concat(', ' || shared.project) AS projects FROM xnat_experimentData expt LEFT JOIN xnat_experimentData_share shared ON expt.id=shared.sharing_share_xnat_experimentda_id LEFT JOIN xdat_meta_element xme ON expt.extension = xme.xdat_meta_element_id WHERE element_name='").append(e.getFullXMLName()).append("' GROUP BY expt.id,expt.project\"/>");
                
                sb.append(header).append("</Displays>");
                
                if (XFT.VERBOSE)
                    System.out.println("Generating File " + location+ e.getFormattedName() +"_display.xml");
                FileUtils.OutputToFile(sb.toString(),location+ e.getFormattedName() +"_display.xml");
            }else{
                //UNKNOWN
                ArrayList localFields = new ArrayList();
                
                GenericWrapperElement ext = e;
                while (ext.isExtension())
                {
                    try {
                        ext = ext.getExtensionField().getReferenceElement().getGenericXFTElement();
                        
                        Iterator fields = ext.getAllFields(true,true).iterator();
                        while (fields.hasNext())
                        {
                            GenericWrapperField f= (GenericWrapperField)fields.next();
                            if (!f.isReference())
                            {
                                if (f.isPrimaryKey())
                                {
                                    String id = f.getSQLName().toUpperCase();
                                    localFields.add(id);
                                    sb.append(header).append("\t<DisplayField id=\"").append(id).append("\" header=\"").append(f.getName()).append("\" visible=\"true\" searchable=\"true\">");
                                    sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(f.getXMLPathString(e.getFullXMLName())).append("\"/>");
                                    sb.append(header).append("\t\t<HTML-Link>");
                                    sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"none\"/>");
                                    sb.append(header).append("\t\t\t<Property name=\"ONCLICK\" value=\"return rpt('@Field1','").append(e.getFullXMLName()).append("','").append(f.getXMLPathString(e.getFullXMLName()).replace('/', '.')).append("');\">");
                                    sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"").append(id).append("\"/>");
                                    sb.append(header).append("\t\t\t</Property>");
                                    sb.append(header).append("\t\t</HTML-Link>");
                                    sb.append(header).append("\t</DisplayField>");
                                }else{
                                    String id = f.getSQLName().toUpperCase();
                                    localFields.add(id);
                                    sb.append(header).append("\t<DisplayField id=\"").append(id).append("\" header=\"").append(f.getName()).append("\" visible=\"true\" searchable=\"true\">");
                                    sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(f.getXMLPathString(e.getFullXMLName())).append("\"/>");
                                    sb.append(header).append("\t</DisplayField>");
                                }
                            }
                        }
                    } catch (Exception e1) {
                    }
                }
                
                sb.append(header).append("\t<DisplayField id=\"INSERT_DATE\" header=\"Inserted\" visible=\"true\" searchable=\"true\" data-type=\"string\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".meta.insert_date\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                sb.append(header).append("\t<DisplayField id=\"INSERT_USER\" header=\"Creator\" visible=\"true\" searchable=\"true\" data-type=\"string\">");
                sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(e.getFullXMLName()).append(".meta.insert_user.login\"/>");
                sb.append(header).append("\t</DisplayField>");
                
                Iterator fields = e.getAllFields(true,true).iterator();
                while (fields.hasNext())
                {
                    GenericWrapperField f= (GenericWrapperField)fields.next();
                    if (!f.isReference())
                    {
                        if (f.isPrimaryKey())
                        {
                            String id = f.getSQLName().toUpperCase();
                            localFields.add(id);
                            sb.append(header).append("\t<DisplayField id=\"").append(id).append("\" header=\"").append(f.getName()).append("\" visible=\"true\" searchable=\"true\">");
                            sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(f.getXMLPathString(e.getFullXMLName())).append("\"/>");
                            sb.append(header).append("\t\t<HTML-Link>");
                            sb.append(header).append("\t\t\t<Property name=\"HREF\" value=\"none\"/>");
                            sb.append(header).append("\t\t\t<Property name=\"ONCLICK\" value=\"return rpt('@Field1','").append(e.getFullXMLName()).append("','").append(f.getXMLPathString(e.getFullXMLName()).replace('/', '.')).append("');\">");
                            sb.append(header).append("\t\t\t\t<InsertValue id=\"Field1\" field=\"").append(id).append("\"/>");
                            sb.append(header).append("\t\t\t</Property>");
                            sb.append(header).append("\t\t</HTML-Link>");
                            sb.append(header).append("\t</DisplayField>");
                        }else{
                            String id = f.getSQLName().toUpperCase();
                            localFields.add(id);
                            sb.append(header).append("\t<DisplayField id=\"").append(id).append("\" header=\"").append(f.getName()).append("\" visible=\"true\" searchable=\"true\">");
                            sb.append(header).append("\t\t<DisplayFieldElement name=\"Field1\" schema-element=\"").append(f.getXMLPathString(e.getFullXMLName())).append("\"/>");
                            sb.append(header).append("\t</DisplayField>");
                        }
                    }
                }
                
                if (localFields.size()>0)
                {
                    sb.append(header).append("\t<DisplayVersion versionName=\"listing\" default-order-by=\"").append(localFields.get(0)).append("\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                    
                    Iterator iter = localFields.iterator();
                    while (iter.hasNext())
                    {
                        String id = (String)iter.next();
                        sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                    }
                    
                    sb.append(header).append("\t</DisplayVersion>");
                    
                    sb.append(header).append("\t<DisplayVersion versionName=\"full\" default-order-by=\"").append(localFields.get(0)).append("\" default-sort-order=\"DESC\" brief-description=\"").append(e.getProperName()).append("\" dark-color=\"9999CC\" light-color=\"CCCCFF\">");
                    
                    iter = localFields.iterator();
                    while (iter.hasNext())
                    {
                        String id = (String)iter.next();
                        sb.append(header).append("\t\t<DisplayFieldRef id=\"").append(id).append("\"/>");
                    }
                    
                    sb.append(header).append("\t</DisplayVersion>");

                    sb.append(header).append("</Displays>");
                    
                    if (XFT.VERBOSE)
            	        System.out.println("Generating File " + location+ e.getFormattedName() +"_display.xml");
                    FileUtils.OutputToFile(sb.toString(),location+ e.getFormattedName() +"_display.xml");
                }
            }
            
        }else{
            if (file.exists())
            {
                System.out.println("\n\nFile already exists: " + file.getAbsolutePath());
            }else if(file2.exists())
            {
                System.out.println("\n\nFile already exists: " + file2.getAbsolutePath());
            }
        }
    }
    
    private String getEditTemplate()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 3.2//EN\">");
        sb.append("\n$page.setTitle(\"XDAT\")");
        sb.append("\n$page.setLinkColor($ui.alink)");
        sb.append("\n$page.setVlinkColor($ui.vlink)");
        sb.append("\n#set($months = [\"January\", \"February\", \"March\", \"April\", \"May\", \"June\", \"July\", \"August\", \"September\", \"October\", \"November\", \"December\"])");
        sb.append("\n#set($days = [ 1..31 ])");
        sb.append("\n#set($years = [ 2010..1900])");
        sb.append("\n#if ($data.message)");
        sb.append("\n<font color=\"red\" size=\"3\">$data.message</font>");
        sb.append("\n#end");
        sb.append("\n<p>");
        sb.append("\n<form ID=\"form1\" name=\"form1\" method=\"post\" action=\"$link.setAction(\"ModifyItem\")\">");
        sb.append("\n<input type=\"hidden\" name=\"project\" value=\"$!{project}\" >");
        sb.append("\n#if($vr)");
        sb.append("\n	<font color=\"red\">Invalid parameters:<BR>$vr.toHTML()</font>");
        sb.append("\n<HR>");
        sb.append("\n#end");
        sb.append("\n");
        
        sb.append("\n<TABLE width=\"100%\">");
        sb.append("\n\t<TR>");
        sb.append("\n\t\t<TD>");
        sb.append("\n\t\t\t<table width=\"100%\">");
        sb.append("\n\t\t\t\t<TR>");
        sb.append("\n\t\t\t\t\t<TD align=\"left\" valign=\"middle\">");
        sb.append("\n\t\t\t\t\t\t<DIV class=\"edit_title\">@PAGE_TITLE@</DIV>");
        sb.append("\n\t\t\t\t\t</TD>");
        sb.append("\n\t\t\t\t</TR>");
        sb.append("\n\t\t\t</TABLE>");
        sb.append("\n\t\t</TD>");
        sb.append("\n\t</TR>");
        sb.append("\n\t<TR>");
        sb.append("\n\t\t<TD>");
        sb.append("\n\t\t\t<TABLE width=\"100%\">");
        sb.append("\n\t\t\t\t<TR>");
        sb.append("\n\t\t\t\t\t<TD valign=\"top\">");
        sb.append("@STATIC@");
        sb.append("\n\t\t\t\t\t</TD>");
        sb.append("\n\t\t\t\t</TR>");
        sb.append("\n\t\t\t</TABLE>");
        sb.append("\n\t\t</TD>");
        sb.append("\n\t</TR>");
        sb.append("\n\t<TR>");
        sb.append("\n\t\t<TD>");
        sb.append("@CONTENT@");
        sb.append("\n\t\t</TD>");
        sb.append("\n\t</TR>");
        sb.append("\n\t<TR>");
        sb.append("\n\t\t<TD>");
        sb.append("\n\t\t#xdatEditProps($item $edit_screen)");
        sb.append("\n\t\t<TR><TD COLSPAN=2 ALIGN=left><input type=\"button\" ONCLICK=\"validateForm();\"  name=\"eventSubmit_doInsert\" value=\"Submit\"/></TD></TR>");
        sb.append("\n\t\t</TD>");
        sb.append("\n\t</TR>");
        
        sb.append("\n</TABLE>");
        sb.append("\n</form>");
        sb.append("\n@VALIDATE_FORM@");
        
        return sb.toString();
    }
    
    private String getReportTemplate()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 3.2//EN\">");
        sb.append("\n$page.setTitle(\"@PAGE_TITLE@\")");
        sb.append("\n$page.setLinkColor($ui.alink)");
        sb.append("\n$page.setVlinkColor($ui.vlink)");
        sb.append("\n#if ($data.getParameters().getString(\"popup\"))");
        sb.append("\n\t#set ($popup = $data.getParameters().getString(\"popup\") )");
        sb.append("\n\t#set ($popup = \"false\")");
        sb.append("\n#end");
        sb.append("\n<TABLE width=\"100%\">");
        sb.append("\n\t<TR>");
        sb.append("\n\t\t<TD>");
        sb.append("\n\t\t\t<table width=\"100%\">");
        sb.append("\n\t\t\t\t<TR>");
        sb.append("\n\t\t\t\t\t<TD align=\"left\" valign=\"middle\">");
        sb.append("\n\t\t\t\t\t\t<font face=\"$ui.sansSerifFonts\" size=\"3\"><b>@PAGE_TITLE@</b></font>");
        sb.append("\n\t\t\t\t\t</TD>");
        sb.append("\n\t\t\t\t</TR>");
        sb.append("\n\t\t\t</TABLE>");
        sb.append("\n\t\t</TD>");
        sb.append("\n\t</TR>");
        sb.append("\n\t<TR>");
        sb.append("\n\t\t<TD>");
        sb.append("\n\t\t\t<TABLE width=\"100%\">");
        sb.append("\n\t\t\t\t<TR>");
        sb.append("\n\t\t\t\t\t<TD valign=\"top\">");
        sb.append("@STATIC@");
        sb.append("\n\t\t\t\t\t</TD>");
        sb.append("\n\t\t\t\t\t<TD valign=\"top\" align=\"right\">");
        sb.append("\n\t\t\t\t\t\t#elementActionsBox($element $search_field $search_value $data.getSession().getAttribute(\"user\") $item)");
        sb.append("\n\t\t\t\t\t</TD>");
        sb.append("\n\t\t\t\t</TR>");
        sb.append("\n\t\t\t</TABLE>");
        sb.append("\n\t\t</TD>");
        sb.append("\n\t</TR>");
        sb.append("\n\t<TR>");
        sb.append("\n\t\t<TD>");
        sb.append("@CONTENT@");
        sb.append("\n\t\t</TD>");
        sb.append("\n\t</TR>");
        sb.append("\n</TABLE>");
        return sb.toString();
    }
    
    private boolean hasMultipleChildren(GenericWrapperElement e, ArrayList parents)
    {
        Iterator fields = e.getAllFields(true,true).iterator();
        while (fields.hasNext())
        {
            GenericWrapperField f= (GenericWrapperField)fields.next();
            if (!parents.contains(f.getXMLType().getFullForeignType()))
            {
                if (f.isReference())
                {
                    if (f.isMultiple())
                    {
                        return true;
                    }
                }
            }
        }
        
        if (e.isExtension())
        {
            try {
                GenericWrapperElement ext = e.getExtensionField().getReferenceElement().getGenericXFTElement();
                boolean temp =  hasMultipleChildren(ext,parents);
                if (temp)
                {
                    return true;
                }
            } catch (Exception e1) {
            }
        }
        
        fields = e.getAllFields(true,true).iterator();
        while (fields.hasNext())
        {
            GenericWrapperField f= (GenericWrapperField)fields.next();
            if (f.isReference())
            {
                if (!parents.contains(f.getXMLType().getFullForeignType()))
                {
	                if (!e.getExtensionFieldName().equals(f.getName()))
	                {
	                    try {
	                        GenericWrapperElement ext = f.getReferenceElement().getGenericXFTElement();
	
	                        if (ext.getGenericXFTElement().getAddin().equals(""))
	                        {
		                        boolean temp =  hasMultipleChildren(ext,parents);
		                        if (temp)
		                        {
		                            return true;
		                        }
	                        }
	                    } catch (Exception e1) {
	                    }
	                }
                }
            }
        }
        
        return false;
    }
    
    /**
     * 
     * @param e
     * @param xmlPathHeader
     * @return ArrayList of ArrayLists(String xmlPath, GenericWrapperField, String (hidden || visible))
     */
    private ArrayList getSingleFields(GenericWrapperElement e,String xmlPathHeader,ArrayList parents)
    {
        ArrayList al = new ArrayList();
        
        //GET EXTENDED FIELDS FIRST
        if (e.isExtension())
        {
            try {
                GenericWrapperElement ext = e.getExtensionField().getReferenceElement().getGenericXFTElement();
                al.addAll(getSingleFields(ext,xmlPathHeader,parents));
            } catch (Exception e1) {
            }
        }
        
        //ADD LOCAL FIELDS
        Iterator fields = e.getAllFields(true,true).iterator();
        while (fields.hasNext())
        {
            GenericWrapperField f= (GenericWrapperField)fields.next();
            if (f.isReference())
            {
            }else{
                String type = f.getXMLType().getLocalType();
                if (type != null)
                {
                    ArrayList sub = new ArrayList();
                    sub.add(f.getXMLPathString(xmlPathHeader));
                    sub.add(f);
                    sub.add("visible");
                    al.add(sub);
                }
            }
        }
        
        //GET SINGLE REFERENCE FIELDS
        fields = e.getAllFields(true,true).iterator();
        while (fields.hasNext())
        {
            GenericWrapperField f= (GenericWrapperField)fields.next();
            if (f.isReference())
            {
                if (!parents.contains(f.getXMLType().getFullForeignType()))
                {
	                if (!e.getExtensionFieldName().equals(f.getName()))
	                {
	                    try {
	                        GenericWrapperElement ext = f.getReferenceElement().getGenericXFTElement();
	
	                        if (ext.getGenericXFTElement().getAddin().equals(""))
	                        {
	                            al.addAll(getSingleFields(ext,f.getXMLPathString(xmlPathHeader),parents));
	                        }
	                    } catch (Exception e1) {
	                    }
	                }
                }
            }
        }
        
        try {
            Iterator addons = e.getAddIns().iterator();
            while(addons.hasNext())
            {
                GenericWrapperField f = (GenericWrapperField)addons.next();
                if (!f.isReference())
                {
                    ArrayList sub = new ArrayList();
                    sub.add(f.getXMLPathString(xmlPathHeader));
                    sub.add(f);
                    sub.add("hidden");
                    al.add(sub);
                }
            }
            
        } catch (Exception e1) {
        }
        
        al.trimToSize();
        return al;
    }
    
    private StringBuffer getLocalFieldsEdit(GenericWrapperElement e, String header, String xmlPathHeader,ArrayList<String> ignoreXMLPaths)
    {
        StringBuffer sb = new StringBuffer();
        ArrayList localFields = new ArrayList();
        Iterator fields = e.getAllFields(true,true).iterator();
        while (fields.hasNext())
        {
            GenericWrapperField f= (GenericWrapperField)fields.next();
            if (!f.isReference())
            {
                if (!ignoreXMLPaths.contains(f.getXMLPathString(e.getFullXMLName()))){
                    String type = f.getXMLType().getLocalType();
                    if (type != null)
                    {
                        sb.append(header).append("<TR>");
                        sb.append("<TD>").append(f.getXMLPathString()).append("</TD>");
                        sb.append("<TD>").append(getLocalFieldEdit(f,f.getXMLPathString(xmlPathHeader))).append("</TD></TR>");
                    }
                }
            }
        }
        
        try {
            Iterator addons = e.getAddIns().iterator();
            while(addons.hasNext())
            {
                GenericWrapperField f = (GenericWrapperField)addons.next();
                if (!f.isReference())
                {
                    sb.append(header).append("\t\t\t\t\t\t#xdatHiddenBox(\"").append(f.getXMLPathString(xmlPathHeader)).append("\" $item \"\")");
                }
            }
            
        } catch (Exception e1) {
        }
        
        return sb;
    }
    
    private String getLocalFieldEdit(GenericWrapperField f, String xmlPath)
    {
        String type = f.getXMLType().getLocalType();
        if (type != null)
        {
            if (type.equalsIgnoreCase("boolean"))
            {
                return "#xdatBooleanRadio(\"" + xmlPath + "\" $item false $vr)";
            }else if (type.equalsIgnoreCase("date")){
                return "#xdatDateBox(\"" + xmlPath + "\" $item $vr $years)";
            }else if (type.equalsIgnoreCase("string")){
                return "#xdatStringBox(\"" + xmlPath + "\" $item \"\" $vr)";
            }else{
                return "#xdatTextBox(\"" + xmlPath + "\" $item \"\" $vr)";
            }
        }
        return "";
    }
    
    private StringBuffer getChildFieldsEdit(GenericWrapperElement e, ArrayList parents, String header, String xmlPathHeader, boolean isRoot,ArrayList<String> ignoreXMLPaths)
    {
        StringBuffer sb =new StringBuffer();
        ArrayList localParents = (ArrayList)parents.clone();
        
        if (!localParents.contains(e.getFullXMLName()))
        {
            localParents.add(e.getFullXMLName());
            item_counter = item_counter+1;
            sb.append("\n").append("<!-- BEGIN ").append(xmlPathHeader).append(" -->");
            sb.append(header).append("<TABLE>");
            if (!isRoot)
                sb.append(header).append("\t<TR><TH align=\"left\"><BR><font face=\"$ui.sansSerifFonts\" size=\"2\">").append(xmlPathHeader).append("</font></TH></TR>");
            else{
                sb.append(header).append("\t<TR><TH align=\"left\"><font face=\"$ui.sansSerifFonts\" size=\"2\">Related Items</font></TH></TR>");
            }

            if (!isRoot)
            {
                sb.append(header).append("\t<TR>");
                sb.append(header).append("\t\t<TD align=\"left\" valign=\"top\">");
                sb.append(header).append("\t\t\t<TABLE>");
                GenericWrapperElement ext = e;
                while (ext.isExtension())
                {
                    try {
                        ext = ext.getExtensionField().getReferenceElement().getGenericXFTElement();
                        sb.append(this.getLocalFieldsEdit(ext,header + "\t\t\t\t",xmlPathHeader,ignoreXMLPaths));
                    } catch (Exception e1) {
                    }
                }
                
                sb.append(this.getLocalFieldsEdit(e,header + "\t\t\t\t",xmlPathHeader,ignoreXMLPaths));
                sb.append(header).append("\t\t\t</TABLE>");
                sb.append(header).append("\t\t</TD>");
                sb.append(header).append("\t</TR>");
            }

            GenericWrapperElement ext = e;
            while (ext.isExtension())
            {
                try {
                    ext = ext.getExtensionField().getReferenceElement().getGenericXFTElement();
                    sb.append(getReferenceFieldsEdit(ext,header,xmlPathHeader,localParents,ignoreXMLPaths));
                } catch (Exception e1) {
                }
            }
            
            sb.append(getReferenceFieldsEdit(e,header, xmlPathHeader,localParents,ignoreXMLPaths));
            
            

            sb.append(header).append("</TABLE>");
            sb.append("\n").append("<!-- END ").append(xmlPathHeader).append(" -->");
        }
        
        return sb;
    }
    
   private StringBuffer getReferenceFieldsEdit(GenericWrapperElement e,String header,String xmlPathHeader, ArrayList localParents,ArrayList<String> ignoreXMLPaths)
    {
        StringBuffer sb = new StringBuffer();
//      FOREACH REFERENCE
        Iterator fields = e.getAllFields(true,true).iterator();
        while (fields.hasNext())
        {
            GenericWrapperField f= (GenericWrapperField)fields.next();
            if (f.isReference())
            {
                if (!ignoreXMLPaths.contains(f.getXMLPathString(e.getFullXMLName())))
                if (!e.getExtensionFieldName().equals(f.getName()))
                {
                    try {
                        SchemaElementI foreign = f.getReferenceElement();

                        if (!localParents.contains(foreign.getFullXMLName()))
                        {
                            if (foreign.getGenericXFTElement().getAddin().equals(""))
                            {
                                if (f.isMultiple())
                                {
                                    if (hasMultipleChildren(foreign.getGenericXFTElement(),localParents))
                                    {
                                        sb.append(header).append("\t<TR>");
                                        sb.append(header).append("\t\t<TD align=\"left\" valign=\"top\">");          
                                        
                                        sb.append(header).append("\t\t\t<TABLE><TR><TD>");
                                        String childNameCounter = foreign.getSQLName()+"_" + item_counter + "_COUNTER";
                                        
                                        sb.append(header).append("\t\t\t\t#foreach($").append(childNameCounter).append(" in [0..5])");
                                        sb.append(getChildFieldsEdit(foreign.getGenericXFTElement(),localParents,header + "\t\t\t\t\t",f.getXMLPathString(xmlPathHeader) + "[$" + childNameCounter +"]",false,ignoreXMLPaths));
                                        sb.append(header).append("\t\t\t\t#end");
                                        sb.append(header).append("\t\t\t</TD></TR></TABLE>");
                                                        
                                        sb.append(header).append("\t\t</TD>");
                                        sb.append(header).append("\t</TR>");
                                    }else{
                                        String childNameCounter = foreign.getSQLName()+"_" + item_counter + "_COUNTER";
                                        ArrayList sfs = this.getSingleFields(foreign.getGenericXFTElement(),f.getXMLPathString(xmlPathHeader) + "[$" + childNameCounter +"]",localParents);
                                        Iterator singleFields = sfs.iterator();

                                        sb.append("\n").append("<!-- BEGIN ").append(f.getXMLPathString(xmlPathHeader)).append(" -->");
                                        sb.append(header).append("\t<TR><TH align=\"left\"><BR><font face=\"$ui.sansSerifFonts\" size=\"2\">").append(f.getXMLPathString(xmlPathHeader)).append("</font></TH></TR>");
                                        sb.append(header).append("\t<TR>");
                                        sb.append(header).append("\t\t<TD align=\"left\" valign=\"top\">");     
                                        sb.append(header).append("\t\t\t<TABLE>");
                                        
                                        //CREATE ELEMENT HEADER ROW
                                        StringBuffer elementHeaderRow = new StringBuffer();
                                        elementHeaderRow.append(header).append("\t\t\t\t<TR>");
                                        String lastElement = "";
                                        int hasDifferentElements = 0;
                                        while (singleFields.hasNext())
                                        {
                                            ArrayList singleField = (ArrayList)singleFields.next();
                                            String hidden = (String)singleField.get(2);
                                            if (hidden.equals("visible"))
                                            {
                                                GenericWrapperField sf = (GenericWrapperField)singleField.get(1);
                                                String elementName = sf.getParentElement().getFullXMLName();
                                                
                                                elementHeaderRow.append(header).append("\t\t\t\t\t<TD>").append(elementName).append("</TD>");
                                                
                                                if (!elementName.equals(lastElement))
                                                {
                                                    lastElement = elementName;
                                                    hasDifferentElements++;
                                                }
                                            }
                                        }
                                        elementHeaderRow.append(header).append("\t\t\t\t</TR>");
                                        
                                        if (hasDifferentElements>1)
                                        {
                                            sb.append(elementHeaderRow);
                                        }
                                        
                                        
                                        //CREATE FIELD HEADER ROW
                                        singleFields = sfs.iterator();
                                        sb.append(header).append("\t\t\t\t<TR>");
                                        while (singleFields.hasNext())
                                        {
                                            ArrayList singleField = (ArrayList)singleFields.next();
                                            String hidden = (String)singleField.get(2);
                                            if (hidden.equals("visible"))
                                            {
                                                GenericWrapperField sf = (GenericWrapperField)singleField.get(1);
                                                sb.append(header).append("\t\t\t\t\t<TD>").append(sf.getXMLPathString()).append("</TD>");
                                            }
                                        }
                                        sb.append(header).append("\t\t\t\t</TR>");
                                        

                                        //CREATE FIELD VALUE ROW
                                        singleFields = sfs.iterator();
                                        
                                        sb.append(header).append("\t\t\t\t#foreach($").append(childNameCounter).append(" in [0..5])");
                                        sb.append(header).append("\t\t\t\t\t<TR>");
                                        while (singleFields.hasNext())
                                        {
                                            ArrayList singleField = (ArrayList)singleFields.next();
                                            String hidden = (String)singleField.get(2);
                                            if (hidden.equals("visible"))
                                            {
                                                GenericWrapperField sf = (GenericWrapperField)singleField.get(1);
                                                String sfxml = (String)singleField.get(0);
                                                sb.append(header).append("\t\t\t\t\t\t<TD>").append(this.getLocalFieldEdit(f,sfxml)).append("</TD>");
                                            }else{
                                                GenericWrapperField sf = (GenericWrapperField)singleField.get(1);
                                                String sfxml = (String)singleField.get(0);
                                                sb.append(header).append("\t\t\t\t\t\t#xdatHiddenBox(\"").append(sfxml).append("\" $item \"\")");
                                            }
                                        }
                                        sb.append(header).append("\t\t\t\t\t</TR>");
                                        sb.append(header).append("\t\t\t\t#end");
                                        sb.append(header).append("\t\t\t</TABLE>");
                                        sb.append(header).append("\t\t</TD>");
                                        sb.append(header).append("\t</TR>");
                                        sb.append("\n").append("<!-- END ").append(f.getXMLPathString(xmlPathHeader)).append(" -->");
                                    }

                                }else{
                                    sb.append(header).append("\t<TR>");
                                    sb.append(header).append("\t\t<TD align=\"left\" valign=\"top\">");          
                                    
                                    String childName = foreign.getSQLName() + item_counter;
                                    sb.append(getChildFieldsEdit(foreign.getGenericXFTElement(),localParents,header + "\t\t\t",f.getXMLPathString(xmlPathHeader),false,ignoreXMLPaths));
                                    sb.append(header).append("\t\t</TD>");
                                    sb.append(header).append("\t</TR>");
                                }
                            }
                        }
                    } catch (Exception e1) {
                    }
                }
            }
        }
        return sb;
    }
    
    private StringBuffer getLocalFieldsReport(GenericWrapperElement e, String header, String xmlPathHeader)
    {
        StringBuffer sb = new StringBuffer();
        ArrayList localFields = new ArrayList();
        Iterator fields = e.getAllFields(true,true).iterator();
        while (fields.hasNext())
        {
            GenericWrapperField f= (GenericWrapperField)fields.next();
            if (!f.isReference())
            {
                String type = f.getXMLType().getLocalType();
                if (type != null)
                {
                    if (type.equalsIgnoreCase("boolean"))
                    {
                        sb.append(header).append("<TR>");
                        sb.append("<TD>").append(f.getXMLPathString()).append("</TD>");
                        sb.append("<TD>$!item.getBooleanProperty(\"").append(f.getXMLPathString(xmlPathHeader)).append("\")</TD></TR>");
                    }else if (type.equalsIgnoreCase("integer"))
                    {
                        sb.append(header).append("<TR>");
                        sb.append("<TD>").append(f.getXMLPathString()).append("</TD>");
                        sb.append("<TD>$!item.getIntegerProperty(\"").append(f.getXMLPathString(xmlPathHeader)).append("\")</TD></TR>");
                    }else if(type.equalsIgnoreCase("double") || type.equalsIgnoreCase("float")){

                        sb.append(header).append("<TR>");
                        sb.append("<TD>").append(f.getXMLPathString()).append("</TD>");
                        sb.append("<TD>$!item.getDoubleProperty(\"").append(f.getXMLPathString(xmlPathHeader)).append("\")</TD></TR>");
                    }else if (type.equalsIgnoreCase("string"))
                    {
                        sb.append(header).append("<TR>");
                        sb.append("<TD>").append(f.getXMLPathString()).append("</TD>");
                        sb.append("<TD>$!item.getStringProperty(\"").append(f.getXMLPathString(xmlPathHeader)).append("\")</TD></TR>");
                    }else{
                        sb.append(header).append("<TR>");
                        sb.append("<TD>").append(f.getXMLPathString()).append("</TD>");
                        sb.append("<TD>$!item.getProperty(\"").append(f.getXMLPathString(xmlPathHeader)).append("\")</TD></TR>");
                    }
                }
            }
        }
        return sb;
    }
    
    private StringBuffer getChildFieldsReport(GenericWrapperElement e, ArrayList parents, String header, String xmlPathHeader, boolean isRoot)
    {
        StringBuffer sb =new StringBuffer();
        ArrayList localParents = (ArrayList)parents.clone();
        
        if (!localParents.contains(e.getFullXMLName()))
        {
            localParents.add(e.getFullXMLName());
            item_counter = item_counter+1;

            sb.append("\n").append("<!-- BEGIN ").append(xmlPathHeader).append(" -->");
            sb.append(header).append("<TABLE>");
            if (!isRoot)
                sb.append(header).append("\t<TR><TH align=\"left\"><BR><font face=\"$ui.sansSerifFonts\" size=\"2\">").append(xmlPathHeader).append("</font></TH></TR>");
            else{
                sb.append(header).append("\t<TR><TH align=\"left\"><font face=\"$ui.sansSerifFonts\" size=\"2\">Related Items</font></TH></TR>");
            }
            sb.append(header).append("\t<TR>");
            sb.append(header).append("\t\t<TD align=\"left\" valign=\"top\">");
            sb.append(header).append("\t\t\t<TABLE>");
            
            if (!isRoot)
            {
                GenericWrapperElement ext = e;
                while (ext.isExtension())
                {
                    try {
                        ext = ext.getExtensionField().getReferenceElement().getGenericXFTElement();
                        sb.append(this.getLocalFieldsReport(ext,header + "\t\t\t\t",xmlPathHeader));
                    } catch (Exception e1) {
                    }
                }
                
                sb.append(this.getLocalFieldsReport(e,header + "\t\t\t\t",xmlPathHeader));
            }

            
            sb.append(header).append("\t\t\t</TABLE>");
            sb.append(header).append("\t\t</TD>");
            sb.append(header).append("\t</TR>");
            
//            if (!isRoot)
//            {
                GenericWrapperElement ext = e;
	            while (ext.isExtension())
	            {
	                try {
	                    ext = ext.getExtensionField().getReferenceElement().getGenericXFTElement();
	                    sb.append(getReferenceFieldsReport(ext,header,xmlPathHeader,localParents));
	                } catch (Exception e1) {
	                }
	            }
//            }
            
            sb.append(getReferenceFieldsReport(e,header, xmlPathHeader,localParents));
            

            sb.append(header).append("</TABLE>");
            sb.append("\n").append("<!-- END ").append(xmlPathHeader).append(" -->");
        }
        
        return sb;
    }
    
   private StringBuffer getReferenceFieldsReport(GenericWrapperElement e,String header,String xmlPathHeader, ArrayList localParents)
    {
        StringBuffer sb = new StringBuffer();
//      FOREACH REFERENCE
        Iterator fields = e.getAllFields(true,true).iterator();
        while (fields.hasNext())
        {
            GenericWrapperField f= (GenericWrapperField)fields.next();
            if (f.isReference())
            {
                if (!e.getExtensionFieldName().equals(f.getName()))
                {
                    try {
                        SchemaElementI foreign = f.getReferenceElement();

                        if (!localParents.contains(foreign.getFullXMLName()))
                        {
                            if (foreign.getGenericXFTElement().getAddin().equals(""))
                            {
                                if (f.isMultiple())
                                {
                                    sb.append(header).append("\t<TR>");
                                    sb.append(header).append("\t\t<TD align=\"left\" valign=\"top\">");          
                                    
                                    sb.append(header).append("\t\t\t<TABLE><TR><TD>");
                                    String childNameCounter = foreign.getSQLName()+"_" + item_counter + "_COUNTER";
                                    String hasRows = foreign.getSQLName()+"_" + item_counter + "_NUM_ROWS";
                                    sb.append(header).append("\t\t\t\t#set($").append(hasRows).append("=$item.getChildItems(\"").append(f.getXMLPathString(xmlPathHeader)).append("\").size() - 1)");
                                    sb.append(header).append("\t\t\t\t#if($").append(hasRows).append(">=0)");
                                    
                                    sb.append(header).append("\t\t\t\t\t#foreach($").append(childNameCounter).append(" in [0..$").append(hasRows).append("])");
                                    sb.append(getChildFieldsReport(foreign.getGenericXFTElement(),localParents,header + "\t\t\t\t\t\t",f.getXMLPathString(xmlPathHeader) + "[$" + childNameCounter +"]",false));
                                    sb.append(header).append("\t\t\t\t\t#end");
                                    sb.append(header).append("\t\t\t\t#end");
                                    sb.append(header).append("\t\t\t</TD></TR></TABLE>");
                                                    
                                    sb.append(header).append("\t\t</TD>");
                                    sb.append(header).append("\t</TR>");
                                }else{
                                    sb.append(header).append("\t<TR>");
                                    sb.append(header).append("\t\t<TD align=\"left\" valign=\"top\">");          
                                    
                                    String childName = foreign.getSQLName() + item_counter;
                                    sb.append(getChildFieldsReport(foreign.getGenericXFTElement(),localParents,header + "\t\t\t",f.getXMLPathString(xmlPathHeader),false));
                                    sb.append(header).append("\t\t</TD>");
                                    sb.append(header).append("\t</TR>");
                                }
                            }
                        }
                    } catch (Exception e1) {
                    }
                }
            }
        }
        return sb;
    }
}

