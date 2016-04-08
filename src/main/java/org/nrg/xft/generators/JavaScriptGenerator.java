/*
 * org.nrg.xft.generators.JavaScriptGenerator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.generators;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.nrg.xft.XFT;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTReferenceI;
import org.nrg.xft.references.XFTRelationSpecification;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperElement;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperFactory;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.XFTFieldWrapper;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.XftStringUtils;

public class JavaScriptGenerator {
    private enum TYPE {data,single_reference,multi_reference,inline_repeater,LONG_DATA,NO_CHILD}
    public static boolean VERSION5=true;
    private String header ="\t";
    /**
     * @param e
     * @param location
     * @throws Exception
     */
    public void generateJSFile(GenericWrapperElement e, String location) throws Exception
    {
        
        StringBuffer sb = new StringBuffer();
                
        sb.append("/*\n * GENERATED FILE\n * Created on " + Calendar.getInstance().getTime() + "\n *\n */");
                //IMPORTS
        sb.append("\n\n/**\n * @author XDAT\n *\n */");
        
        //CLASS
        String sqlName = e.getFormattedName();
        sb.append("\n");
        sb.append("\n").append("function " + sqlName +"(){");
        sb.append("\n").append("this.xsiType=\"" + e.getXSIType() + "\";");

        sb.append("\n\n");
        sb.append("\t").append("this.getSchemaElementName=function(){");
        sb.append("\n\t\t").append("return \"").append(e.getLocalXMLName()).append("\";");
        sb.append("\n\t}");
        sb.append("\n\n");
        sb.append("\t").append("this.getFullSchemaElementName=function(){");
        sb.append("\n\t\t").append("return \"").append(e.getFullXMLName()).append("\";");
        sb.append("\n\t}");

        XMLWrapperElement xE = (XMLWrapperElement)XMLWrapperFactory.GetInstance().convertElement(e);
        
//      CLASS
        if (e.isExtension())
        {
           GenericWrapperElement ext = GenericWrapperElement.GetElement(e.getExtensionType());
           if (!ext.getXSIType().equals(e.getXSIType()))
            sb.append("\n").append("this.extension=dynamicJSLoad('" + ext.getFormattedName() + "','generated/" + ext.getFormattedName() + ".js');");
        }

        ArrayList<String> xmlPaths = new ArrayList<String>();
        ArrayList<TYPE> types = new ArrayList<TYPE>();
        ArrayList<String> methods = new ArrayList<String>();
        ArrayList<String> getmethods = new ArrayList<String>();
        ArrayList<String> foreignElements = new ArrayList<String>();
        
        StringBuffer realtimeGet = new StringBuffer();
        StringBuffer realtimeSet = new StringBuffer();
        
        Iterator fields = e.getAllFields(true,true).iterator();
        while (fields.hasNext())
        {
            GenericWrapperField f= (GenericWrapperField)fields.next();
            if (f.isReference())
            {
                String xmlPath = f.getXMLPathString();
                String formatted = formatFieldName(xmlPath);
                SchemaElementI foreign = f.getReferenceElement();
                
                if (foreign.getGenericXFTElement().getAddin().equals(""))
                {
                    
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
                        getmethods.add("get" + formatted);
                        foreignElements.add(foreign.getGenericXFTElement().getSchemaTargetNamespaceURI() + ":" + foreign.getGenericXFTElement().getLocalXMLName());
                        
                        sb.append("\n\tthis." + formatted + " =new Array();");
                        sb.append("\n");
                        sb.append("\n\tfunction ").append("get").append(formatted).append("() {");
                        sb.append("\n\t\treturn this." + formatted + ";");
                        sb.append("\n\t}");
                        sb.append("\n\tthis.").append("get").append(formatted).append("=").append("get").append(formatted).append(";"); 
                        
                        sb.append("\n\n");
                        sb.append("\n\t").append("function add").append(formatted).append("(v){");
                        sb.append("\n\t\tthis." + formatted + ".push(v);");
                        sb.append("\n\t}");                   
                        sb.append("\n\tthis.").append("add").append(formatted).append("=").append("add").append(formatted).append(";"); 

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
                            getmethods.add("get" + formatted);
                            foreignElements.add(foreign.getGenericXFTElement().getSchemaTargetNamespaceURI() + ":" + foreign.getGenericXFTElement().getLocalXMLName());
                            sb.append("\n\tthis." + formatted + " =null;");
                            sb.append("\n\t").append("function get").append(formatted).append("() {");
                            sb.append("\n\t\treturn this." + formatted +";");
                            sb.append("\n\t}");
                            sb.append("\n\tthis.get").append(formatted).append("=get").append(formatted).append(";");
                            
                            sb.append("\n\n");
                            sb.append("\n\t").append("function set").append(formatted).append("(v){");
                            sb.append("\n\t\tthis.").append(formatted + " =v;");
                            sb.append("\n\t}");
                            sb.append("\n\tthis.set").append(formatted).append("=set").append(formatted).append(";");
                            
                        
                            XFTSuperiorReference ref = (XFTSuperiorReference)f.getXFTReference();
                            Iterator iter=ref.getKeyRelations().iterator();
                            while (iter.hasNext())
                            {
                                XFTRelationSpecification spec = (XFTRelationSpecification) iter.next();
                                String temp = spec.getLocalCol();
                                
                                String reformatted = formatted + "_" +formatFieldName(temp);
                                sb.append("\n\n\t").append("this.").append(reformatted);
                                sb.append("=null;");
                                
                                //STANDARD GET METHOD
                                sb.append("\n\n");
                                 sb.append("\n\t").append("function get").append(reformatted).append("(){");
                                sb.append("\n\t\treturn this." + reformatted +";");
                                sb.append("\n\t}");
                                sb.append("\n\tthis.get").append(reformatted).append("=get").append(reformatted).append(";");
                                
                                //STANDARD SET METHOD
                                sb.append("\n\n");
                                sb.append("\n\t").append("function set").append(reformatted).append("(v){");
                                sb.append("\n\t\tthis.").append(reformatted +"=v;");
                                sb.append("\n\t}");
                                sb.append("\n\tthis.set").append(reformatted).append("=set").append(reformatted).append(";");
                                
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
                    getmethods.add("get" +formatted);
                    foreignElements.add("");
                                    
                    sb.append("\n\n\t").append("this.").append(formatted);
                    sb.append("=null;");
                    
//                      STANDARD GET METHOD
                    sb.append("\n\n");
                    sb.append("\n\t").append("function get").append(formatted).append("() {");
                    sb.append("\n\t\treturn this." + formatted +";");
                    sb.append("\n\t}");
                    sb.append("\n\tthis.get").append(formatted).append("=get").append(formatted).append(";");

                    //STANDARD SET METHOD
                    sb.append("\n\n");
                    sb.append("\n\t").append("function set").append(formatted).append("(v){");
                    sb.append("\n\t\tthis.").append(formatted +"=v;");
                    sb.append("\n\t}");                
                    sb.append("\n\tthis.set").append(formatted).append("=set").append(formatted).append(";");
                    
                    if (type.equals("boolean")){
                        sb.append("\n\n");
                        sb.append("\n\t").append("this.is").append(formatted).append("=function(defaultValue) {");
                        sb.append("\n\t\tif(this." + formatted +"==null)return defaultValue;");
                        sb.append("\n\t\tif(this." + formatted +"==\"1\" || this." + formatted +"==true)return true;");
                        sb.append("\n\t\treturn false;");
                        sb.append("\n\t}");
                    }
                }
            }
        }
        
        ArrayList<XFTFieldWrapper> addins = e.getAddIns();
        if (addins.size()>0){
            for(XFTFieldWrapper xwf:addins){
                GenericWrapperField gwf = (GenericWrapperField)xwf;
                String xmlPath = gwf.getXMLPathString();
                String formatted = formatFieldName(xmlPath);
                
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
                                    sb.append("\n\n\t").append("this.").append(spec.getLocalCol());
                                    sb.append("_fk=null;");
                                    
                                    //STANDARD GET METHOD
                                    sb.append("\n\n");
                                    sb.append("\n\t").append("this.get").append(spec.getLocalCol()).append("=function() {");
                                    sb.append("\n\t\treturn this." + spec.getLocalCol() +"_fk;");
                                    sb.append("\n\t}");
              
                                    //STANDARD SET METHOD
                                    sb.append("\n\n");
                                    sb.append("\n\t").append("this.set").append(spec.getLocalCol()).append("=function(v){");
                                    sb.append("\n\t\tthis.").append(spec.getLocalCol() +"_fk=v;");
                                    sb.append("\n\t}");
                                }
                            }
                        }
                    }
                }else{
                    sb.append("\n\n\t").append("this.").append(formatted);
                    sb.append("=null;");
                    
                    //STANDARD GET METHOD
                    sb.append("\n\n");
                    sb.append("\n\t").append("function get").append(formatted).append("() {");
                    sb.append("\n\t\treturn this." + formatted +";");
                    sb.append("\n\t}");
                    sb.append("\n\tthis.get").append(formatted).append("=get").append(formatted).append(";");

                    //STANDARD SET METHOD
                    sb.append("\n\n");
                    sb.append("\n\t").append("function set").append(formatted).append("(v){");
                    sb.append("\n\t\tthis.").append(formatted +"=v;");
                    sb.append("\n\t}");
                    sb.append("\n\tthis.set").append(formatted).append("=set").append(formatted).append(";");
                }
            }
        }
        
        if (!e.containsStatedKey()){
            GenericWrapperField f = (GenericWrapperField)e.getDefaultKey();

        }

        //GET PROPERTY
        sb.append("\n\n");
        sb.append("\n\t").append("this.getProperty=function(xmlPath){");

        sb.append("\n\t\t\t").append("if(xmlPath.startsWith(this.getFullSchemaElementName())){");
        sb.append("\n\t\t\t\t").append("xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);");
        sb.append("\n\t\t\t").append("}");

        fields = e.getAllFields(true,true).iterator();
        while (fields.hasNext())
        {
            GenericWrapperField f= (GenericWrapperField)fields.next();
            String xmlPath = f.getXMLPathString();
            String formatted = formatFieldName(xmlPath);
            
            sb.append("\n\t\t\t").append("if(xmlPath==\"" + xmlPath + "\"){");
            sb.append("\n\t\t\t\t").append("return this." + formatted + " ;");
            sb.append("\n\t\t\t").append("} else ");
            
            if (f.isReference())
            {
                SchemaElementI foreign = f.getReferenceElement();
                if (foreign.getGenericXFTElement().getAddin().equals(""))
                {                    
                    sb.append("\n\t\t\t").append("if(xmlPath.startsWith(\"" + xmlPath + "\")){");
                    sb.append("\n\t\t\t\t").append("xmlPath=xmlPath.substring(" + (xmlPath.length()) + ");");
                    sb.append("\n\t\t\t\t").append("if(xmlPath==\"\")return this." + formatted + " ;");
                    sb.append("\n\t\t\t\t").append("if(xmlPath.startsWith(\"[\")){");

                    sb.append("\n\t\t\t\t\t").append("if (xmlPath.indexOf(\"/\")>-1){");
                    sb.append("\n\t\t\t\t\t\t").append("var optionString=xmlPath.substring(0,xmlPath.indexOf(\"/\"));");
                    sb.append("\n\t\t\t\t\t\t").append("xmlPath=xmlPath.substring(xmlPath.indexOf(\"/\")+1);");
                    sb.append("\n\t\t\t\t\t").append("}else{");
                    sb.append("\n\t\t\t\t\t\t").append("var optionString=xmlPath;");
                    sb.append("\n\t\t\t\t\t\t").append("xmlPath=\"\";");
                    sb.append("\n\t\t\t\t\t").append("}");

                    sb.append("\n\t\t\t\t\t").append("");
                    sb.append("\n\t\t\t\t\t").append("var options = loadOptions(optionString);//omUtils.js");
                    
                    sb.append("\n\t\t\t\t").append("}else{xmlPath=xmlPath.substring(1);}");
                    if (f.isMultiple()){

                        sb.append("\n\t\t\t\t").append("var index=0;");
                        sb.append("\n\t\t\t\t").append("if(options){");
                        sb.append("\n\t\t\t\t\t").append("if(options.index)index=options.index;");
                        sb.append("\n\t\t\t\t").append("}");

                        sb.append("\n\n\t\t\t").append("var whereArray;");
                        sb.append("\n\t\t\t\t").append("if (options.where){");
                        sb.append("\n\n\t\t\t\t").append("whereArray=new Array();");
                        sb.append("\n\n\t\t\t\t").append("for(var whereCount=0;whereCount<this." + formatted + ".length;whereCount++){");
                        sb.append("\n\n\t\t\t\t\t").append("var tempValue=this." + formatted + "[whereCount].getProperty(options.where.field);");
                        sb.append("\n\n\t\t\t\t\t").append("if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){");
                        sb.append("\n\n\t\t\t\t\t\t").append("whereArray.push(this." + formatted + "[whereCount]);");
                        sb.append("\n\n\t\t\t\t\t").append("}");
                        sb.append("\n\n\t\t\t\t").append("}");
                        sb.append("\n\t\t\t\t").append("}else{");
                        sb.append("\n\n\t\t\t\t").append("whereArray=this." + formatted + ";");
                        sb.append("\n\t\t\t\t").append("}");
                        

                        sb.append("\n\n\t\t\t").append("var typeArray;");
                        sb.append("\n\t\t\t\t").append("if (options.xsiType){");
                        sb.append("\n\n\t\t\t\t").append("typeArray=new Array();");
                        sb.append("\n\n\t\t\t\t").append("for(var typeCount=0;typeCount<whereArray.length;typeCount++){");
                        sb.append("\n\n\t\t\t\t\t").append("if(whereArray[typeCount].getFullSchemaElementName()==options.xsiType){");
                        sb.append("\n\n\t\t\t\t\t\t").append("typeArray.push(whereArray[typeCount]);");
                        sb.append("\n\n\t\t\t\t\t").append("}");
                        sb.append("\n\n\t\t\t\t").append("}");
                        sb.append("\n\t\t\t\t").append("}else{");
                        sb.append("\n\n\t\t\t\t").append("typeArray=whereArray;");
                        sb.append("\n\t\t\t\t").append("}");

                        sb.append("\n\t\t\t\t").append("if (typeArray.length>index){");
                        sb.append("\n\t\t\t\t\t").append("return typeArray[index].getProperty(xmlPath);");
                        sb.append("\n\t\t\t\t").append("}else{");
                        sb.append("\n\t\t\t\t\t").append("return null;");
                        sb.append("\n\t\t\t\t").append("}");
                        
                    }else{
                        sb.append("\n\t\t\t\t").append("if(this." + formatted + "!=undefined)return this." + formatted + ".getProperty(xmlPath);");
                        sb.append("\n\t\t\t\t").append("else return null;");
                    }
                    sb.append("\n\t\t\t").append("} else ");
                }
            }
        }
        
        if (addins.size()>0){
            for(XFTFieldWrapper xwf:addins){
                GenericWrapperField gwf = (GenericWrapperField)xwf;
                String xmlPath = gwf.getXMLPathString();
                String formatted = formatFieldName(xmlPath);
                
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
                                    
                                    sb.append("\n\t\t\t").append("if(xmlPath==\"" + spec.getLocalCol() + "\"){");
                                    sb.append("\n\t\t\t\t").append("return this." + spec.getLocalCol() + "_fk ;");
                                    sb.append("\n\t\t\t").append("} else ");
                                }
                            }
                        }
                    }
                }else{
                    sb.append("\n\t\t\t").append("if(xmlPath==\"" + xmlPath + "\"){");
                    sb.append("\n\t\t\t\t").append("return this." + formatted + " ;");
                    sb.append("\n\t\t\t").append("} else ");
                }
            }
        }
        
        sb.append("\n\t\t\t").append("{");
        if (e.isExtension())
            sb.append("\n\t\t\t\t").append("return this.extension.getProperty(xmlPath);");
        else
            sb.append("\n\t\t\t\t").append("return null;");
        sb.append("\n\t\t\t").append("}");
        
        sb.append("\n\t").append("}");
        


        //SET PROPERTY
        sb.append("\n\n");
        sb.append("\n\t").append("this.setProperty=function(xmlPath,value){");

        sb.append("\n\t\t\t").append("if(xmlPath.startsWith(this.getFullSchemaElementName())){");
        sb.append("\n\t\t\t\t").append("xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);");
        sb.append("\n\t\t\t").append("}");

        fields = e.getAllFields(true,true).iterator();
        while (fields.hasNext())
        {
            GenericWrapperField f= (GenericWrapperField)fields.next();
            String xmlPath = f.getXMLPathString();
            String formatted = formatFieldName(xmlPath);
            
            sb.append("\n\t\t\t").append("if(xmlPath==\"" + xmlPath + "\"){");
            sb.append("\n\t\t\t\t").append("this." + formatted + "=value;");
            sb.append("\n\t\t\t").append("} else ");
            
            if (f.isReference())
            {
                SchemaElementI foreign = f.getReferenceElement();
                if (foreign.getGenericXFTElement().getAddin().equals(""))
                {                    
                    sb.append("\n\t\t\t").append("if(xmlPath.startsWith(\"" + xmlPath + "\")){");
                    sb.append("\n\t\t\t\t").append("xmlPath=xmlPath.substring(" + (xmlPath.length()) + ");");
                    sb.append("\n\t\t\t\t").append("if(xmlPath==\"\")return this." + formatted + " ;");
                    sb.append("\n\t\t\t\t").append("if(xmlPath.startsWith(\"[\")){");

                    sb.append("\n\t\t\t\t\t").append("if (xmlPath.indexOf(\"/\")>-1){");
                    sb.append("\n\t\t\t\t\t\t").append("var optionString=xmlPath.substring(0,xmlPath.indexOf(\"/\"));");
                    sb.append("\n\t\t\t\t\t\t").append("xmlPath=xmlPath.substring(xmlPath.indexOf(\"/\")+1);");
                    sb.append("\n\t\t\t\t\t").append("}else{");
                    sb.append("\n\t\t\t\t\t\t").append("var optionString=xmlPath;");
                    sb.append("\n\t\t\t\t\t\t").append("xmlPath=\"\";");
                    sb.append("\n\t\t\t\t\t").append("}");

                    sb.append("\n\t\t\t\t\t").append("");
                    sb.append("\n\t\t\t\t\t").append("var options = loadOptions(optionString);//omUtils.js");
                    
                    sb.append("\n\t\t\t\t").append("}else{xmlPath=xmlPath.substring(1);}");
                    if (f.isMultiple()){

                        sb.append("\n\t\t\t\t").append("var index=0;");
                        sb.append("\n\t\t\t\t").append("if(options){");
                        sb.append("\n\t\t\t\t\t").append("if(options.index)index=options.index;");
                        sb.append("\n\t\t\t\t").append("}");

                        sb.append("\n\n\t\t\t").append("var whereArray;");
                        sb.append("\n\t\t\t\t").append("if (options && options.where){");
                        sb.append("\n\n\t\t\t\t").append("whereArray=new Array();");
                        sb.append("\n\n\t\t\t\t").append("for(var whereCount=0;whereCount<this." + formatted + ".length;whereCount++){");
                        sb.append("\n\n\t\t\t\t\t").append("var tempValue=this." + formatted + "[whereCount].getProperty(options.where.field);");
                        sb.append("\n\n\t\t\t\t\t").append("if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){");
                        sb.append("\n\n\t\t\t\t\t\t").append("whereArray.push(this." + formatted + "[whereCount]);");
                        sb.append("\n\n\t\t\t\t\t").append("}");
                        sb.append("\n\n\t\t\t\t").append("}");
                        sb.append("\n\t\t\t\t").append("}else{");
                        sb.append("\n\n\t\t\t\t").append("whereArray=this." + formatted + ";");
                        sb.append("\n\t\t\t\t").append("}");
                        

                        sb.append("\n\n\t\t\t").append("var typeArray;");
                        sb.append("\n\t\t\t\t").append("if (options && options.xsiType){");
                        sb.append("\n\n\t\t\t\t").append("typeArray=new Array();");
                        sb.append("\n\n\t\t\t\t").append("for(var typeCount=0;typeCount<whereArray.length;typeCount++){");
                        sb.append("\n\n\t\t\t\t\t").append("if(whereArray[typeCount].getFullSchemaElementName()==options.xsiType){");
                        sb.append("\n\n\t\t\t\t\t\t").append("typeArray.push(whereArray[typeCount]);");
                        sb.append("\n\n\t\t\t\t\t").append("}");
                        sb.append("\n\n\t\t\t\t").append("}");
                        sb.append("\n\t\t\t\t").append("}else{");
                        sb.append("\n\n\t\t\t\t").append("typeArray=whereArray;");
                        sb.append("\n\t\t\t\t").append("}");
                        
                        
                        sb.append("\n\t\t\t\t").append("if (typeArray.length>index){");
                        sb.append("\n\t\t\t\t\t").append("typeArray[index].setProperty(xmlPath,value);");
                        sb.append("\n\t\t\t\t").append("}else{");
                        sb.append("\n\t\t\t\t\t").append("var newChild;");
                        sb.append("\n\t\t\t\t\t").append("if(options && options.xsiType){");
                        sb.append("\n\t\t\t\t\t\t").append("newChild= instanciateObject(options.xsiType);//omUtils.js");
                        sb.append("\n\t\t\t\t\t").append("}else{");
                        sb.append("\n\t\t\t\t\t\t").append("newChild= instanciateObject(\"" + f.getReferenceElementName().getFullForeignType() + "\");//omUtils.js");
                        sb.append("\n\t\t\t\t\t").append("}");
                        sb.append("\n\t\t\t\t\t").append("this.add" + formatted + "(newChild);");
                        sb.append("\n\t\t\t\t\t").append("if(options && options.where)newChild.setProperty(options.where.field,options.where.value);");
                        sb.append("\n\t\t\t\t\t").append("newChild.setProperty(xmlPath,value);");
                        sb.append("\n\t\t\t\t").append("}");
                        
                    }else{
                        sb.append("\n\t\t\t\t").append("if(this." + formatted + "!=undefined){");
                        sb.append("\n\t\t\t\t\t").append("this." + formatted + ".setProperty(xmlPath,value);");
                        sb.append("\n\t\t\t\t").append("}else{");
                        sb.append("\n\t\t\t\t\t\t").append("if(options && options.xsiType){");
                        sb.append("\n\t\t\t\t\t\t\t").append("this." + formatted + "= instanciateObject(options.xsiType);//omUtils.js");
                        sb.append("\n\t\t\t\t\t\t").append("}else{");
                        sb.append("\n\t\t\t\t\t\t\t").append("this." + formatted + "= instanciateObject(\"" + f.getReferenceElementName().getFullForeignType() + "\");//omUtils.js");
                        sb.append("\n\t\t\t\t\t\t").append("}");
                        sb.append("\n\t\t\t\t\t\t").append("if(options && options.where)this." + formatted + ".setProperty(options.where.field,options.where.value);");
                        sb.append("\n\t\t\t\t\t\t").append("this." + formatted + ".setProperty(xmlPath,value);");
                        sb.append("\n\t\t\t\t").append("}");
                        
                    }
                    sb.append("\n\t\t\t").append("} else ");
                }
                
            }
        }
        
        if (addins.size()>0){
            for(XFTFieldWrapper xwf:addins){
                GenericWrapperField gwf = (GenericWrapperField)xwf;
                String xmlPath = gwf.getXMLPathString();
                String formatted = formatFieldName(xmlPath);
                
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

                                    
                                    sb.append("\n\t\t\t").append("if(xmlPath==\"" + spec.getLocalCol() + "\"){");
                                    sb.append("\n\t\t\t\t").append("this." + spec.getLocalCol() + "_fk=value;");
                                    sb.append("\n\t\t\t").append("} else ");
                                }
                            }
                        }
                    }
                }else{
                    sb.append("\n\t\t\t").append("if(xmlPath==\"" + xmlPath + "\"){");
                    sb.append("\n\t\t\t\t").append("this." + formatted + "=value;");
                    sb.append("\n\t\t\t").append("} else ");
                }
            }
        }
        sb.append("\n\t\t\t").append("{");
        if (e.isExtension())
            sb.append("\n\t\t\t\t").append("return this.extension.setProperty(xmlPath,value);");
        else
            sb.append("\n\t\t\t\t").append("return null;");
        sb.append("\n\t\t\t").append("}");
        
        sb.append("\n\t").append("}");

        
//      CREATE PROJECT

//        fields = e.getAllFields(true,true).iterator();
//        while (fields.hasNext())
//        {
//            GenericWrapperField f= (GenericWrapperField)fields.next();
//            String xmlPath = f.getXMLPathString();
//            String formatted = formatFieldName(xmlPath);
//
//            if (f.isReference()){
//                sb.append("\n\n");
//                sb.append("\n\t").append("this." + formatted +"_CREATE=function(){");
//                sb.append("\n\t\t").append("if(window.classMapping==undefined){");
//                sb.append("\n\t\t\t").append("dynamicJSLoad(\"ClassMapping\",\"generated/ClassMapping.js\");");
//                sb.append("\n\t\t\t").append("window.classMapping=new ClassMapping();");
//                sb.append("\n\t\t").append("}");
//                sb.append("\n\t\t").append("var fn = window.classMapping.newInstance;");
//                sb.append("\n\t\t").append("return fn(\"" + f.getReferenceElementName().getFullForeignType() + "\");");
//                sb.append("\n\t").append("}");
//            }
//            
//        }
        //SET DATA FIELD
//            sb.append("\n\n");
//            sb.append("\n\t").append("this.getProperty=function(xmlPath){");
//
//            if (xmlPaths.size()>0)
//            {
//                int count = 0;
//                for (int i=0; i<xmlPaths.size();i++)
//                {
//                    String xmlPath = xmlPaths.get(i);
//                    TYPE type = types.get(i);
//                    String method = getmethods.get(i);
//                    
//                    if (type.equals(TYPE.data) || type.equals(TYPE.LONG_DATA)){
//                        if (count==0){
//                            sb.append("\n\t\t").append("if (xmlPath==\"").append(xmlPath).append("\"){");
//                        }else{
//                            sb.append("\n\t\t").append("}else if (xmlPath==\"").append(xmlPath).append("\"){");
//                        }
//                        sb.append("\n\t\t\treturn this.").append(method).append("();");
//                        count++;
//                    }
//                }
//                
//                if (count>0)
//                {
//                    sb.append("\n\t\t").append("}");
//                    sb.append("\n\t\t").append("else{");
//                    if (e.isExtension())
//                     sb.append("\n\t\t\t").append("return this.extension.getProperty(xmlPath);");
//
//                    sb.append("\n\t\t").append("}");
//                }else{
//                    if (e.isExtension())
//                        sb.append("\n\t\t\t").append("return this.extension.getProperty(xmlPath);");
//
//                }
//            }else{
//                if (e.isExtension())
//                    sb.append("\n\t\t").append("return this.extension.getProperty(xmlPath);");
//            }
//            sb.append("\n\t}");

//        //SET DATA FIELD
//            sb.append("\n\n");
//            sb.append("\n\t").append("this.setDataField=function(xmlPath,v){");
//
//            if (xmlPaths.size()>0)
//            {
//                int count = 0;
//                for (int i=0; i<xmlPaths.size();i++)
//                {
//                    String xmlPath = xmlPaths.get(i);
//                    TYPE type = types.get(i);
//                    String method = methods.get(i);
//                    
//                    if (type.equals(TYPE.data) || type.equals(TYPE.LONG_DATA)){
//                        if (count==0){
//                            sb.append("\n\t\t").append("if (xmlPath==\"").append(xmlPath).append("\"){");
//                        }else{
//                            sb.append("\n\t\t").append("}else if (xmlPath==\"").append(xmlPath).append("\"){");
//                        }
//                        sb.append("\n\t\t\tthis.").append(method).append("(v);");
//                        count++;
//                    }
//                }
//                
//                if (count>0)
//                {
//                    sb.append("\n\t\t").append("}");
//                    sb.append("\n\t\t").append("else{");
//                    if (e.isExtension())
//                     sb.append("\n\t\t\t").append("this.extension.setDataField(xmlPath,v);");
//
//                    sb.append("\n\t\t").append("}");
//                }else{
//                    if (e.isExtension())
//                        sb.append("\n\t\t\t").append("this.extension.setDataField(xmlPath,v);");
//
//                }
//            }else{
//                if (e.isExtension())
//                    sb.append("\n\t\t").append("this.extension.setDataField(xmlPath,v);");
//            }
//            sb.append("\n\t}");
//        
//            
            //SET REFERENCE FIELD
            sb.append("\n\n");
            sb.append("\t/**\n\t * Sets the value for a field via the XMLPATH.\n\t * @param v Value to Set.\n\t */");
            sb.append("\n\t").append("this.setReferenceField=function(xmlPath,v) {");

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
                            sb.append("\n\t\t").append("if (xmlPath==\"").append(xmlPath).append("\"){");
                        }else{
                            sb.append("\n\t\t").append("}else if (xmlPath==\"").append(xmlPath).append("\"){");
                        }
                        sb.append("\n\t\t\tthis.").append(method).append("(v);");
                        count++;
                    }
                }
                
                if (count>0)
                {
                    sb.append("\n\t\t").append("}");
                    sb.append("\n\t\t").append("else{");
                    if (e.isExtension())
                        sb.append("\n\t\t\t").append("this.extension.setReferenceField(xmlPath,v);");

                    sb.append("\n\t\t").append("}");
                }else{
                    if (e.isExtension())
                        sb.append("\n\t\t\t").append("this.extension.setReferenceField(xmlPath,v);");

                }
            }else{
                if (e.isExtension())
                    sb.append("\n\t\t").append("this.extension.setReferenceField(xmlPath,v);");
            }
            sb.append("\n\t}");
        
            

            
            //GET REFERENCE FIELD ELEMENT NAME
            sb.append("\n\n");
            sb.append("\t/**\n\t * Gets the value for a field via the XMLPATH.\n\t * @param v Value to Set.\n\t */");
            sb.append("\n\t").append("this.getReferenceFieldName=function(xmlPath) {");

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
                            sb.append("\n\t\t").append("if (xmlPath==\"").append(xmlPath).append("\"){");
                        }else{
                            sb.append("\n\t\t").append("}else if (xmlPath==\"").append(xmlPath).append("\"){");
                        }
                        sb.append("\n\t\t\treturn \"" + foreign + "\";");
                        count++;
                    }
                }
                
                if (count>0)
                {
                    sb.append("\n\t\t").append("}");
                    sb.append("\n\t\t").append("else{");
                    if (e.isExtension())
                        sb.append("\n\t\t\t").append("return this.extension.getReferenceFieldName(xmlPath);");

                    sb.append("\n\t\t").append("}");
                }else{
                    if (e.isExtension())
                        sb.append("\n\t\t\t").append("return this.extension.getReferenceFieldName(xmlPath);");

                }
            }else{
                if (e.isExtension())
                    sb.append("\n\t\t").append("return this.extension.getReferenceFieldName(xmlPath);");
            }
            sb.append("\n\t}");
            
            
            //GET FIELD TYPE
            sb.append("\n\n");
            sb.append("\t/**\n\t * Returns whether or not this is a reference field\n\t */");
            sb.append("\n\t").append("this.getFieldType=function(xmlPath){");

            if (xmlPaths.size()>0)
            {
                int count = 0;
                for (int i=0; i<xmlPaths.size();i++)
                {
                    String xmlPath = xmlPaths.get(i);
                    TYPE type = types.get(i);
                    String method = methods.get(i);

                    if (i==0){
                        sb.append("\n\t\t").append("if (xmlPath==\"").append(xmlPath).append("\"){");
                    }else{
                        sb.append("\n\t\t").append("}else if (xmlPath==\"").append(xmlPath).append("\"){");
                    }
                    if (type.equals(TYPE.data)){
                        sb.append("\n\t\t\treturn \"field_data\";");
                    }else if (type.equals(TYPE.multi_reference)){
                        sb.append("\n\t\t\treturn \"field_multi_reference\";");
                    }else if (type.equals(TYPE.single_reference)){
                        sb.append("\n\t\t\treturn \"field_single_reference\";");
                    }else if (type.equals(TYPE.inline_repeater)){
                        sb.append("\n\t\t\treturn \"field_inline_repeater\";");
                    }else if (type.equals(TYPE.LONG_DATA)){
                        sb.append("\n\t\t\treturn \"field_LONG_DATA\";");
                    }else if (type.equals(TYPE.NO_CHILD)){
                        sb.append("\n\t\t\treturn \"field_NO_CHILD\";");
                    }
                }
                
                    sb.append("\n\t\t").append("}");
                    sb.append("\n\t\t").append("else{");
                    if (e.isExtension())
                        sb.append("\n\t\t\t").append("return this.extension.getFieldType(xmlPath);");
                    sb.append("\n\t\t").append("}");
            }else{
                if (e.isExtension())
                    sb.append("\n\t\t").append("return this.extension.getFieldType(xmlPath);");
            }
            sb.append("\n\t}");
        
        
        //WRITE XML - WRITER
        sb.append("\n\n");
        sb.append("\n\t").append("this.toXML=function(xmlTxt,preventComments){");
        sb.append("\n\t\t").append("xmlTxt+=\"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>\";");
        sb.append("\n\t\t").append("xmlTxt+=\"\\n<").append(getRootName(xE)).append("\";");
        sb.append("\n\t\t").append("xmlTxt+=this.getXMLAtts();");
        Map map = this.addXMLNSAttributes();
        Iterator iter = map.keySet().iterator();
        while(iter.hasNext()){
            String key = (String)iter.next();
            sb.append("\n\t\t").append("xmlTxt+=\" " + key +"=\\\"" + map.get(key) +"\\\"\";");
        }
        
        sb.append("\n\t\t").append("xmlTxt+=\">\";");

        sb.append("\n\t\t").append("xmlTxt+=this.getXMLBody(preventComments)");

        sb.append("\n\t\t").append("xmlTxt+=\"\\n</").append(getRootName(xE)).append(">\";");
        sb.append("\n\t\t").append("return xmlTxt;");
        
        sb.append("\n\t").append("}");
        


        sb.append("\n\n");
        sb.append("\n\t").append("this.getXMLComments=function(preventComments){");
        sb.append("\n\t\t").append("var str =\"\";");
        sb.append("\n\t\t").append("if((preventComments==undefined || !preventComments) && this.hasXMLComments()){");
        
        if (addins.size()>0){
            sb.append("\n\t\t").append("str += \"<!--hidden_fields[\";");
            sb.append("\n\t\t").append("var hiddenCount = 0;");
            for(XFTFieldWrapper xwf:addins){
                GenericWrapperField gwf = (GenericWrapperField)xwf;
                String xmlPath = gwf.getXMLPathString();
                String formatted = formatFieldName(xmlPath);
                
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
                                    sb.append("\n\t\t\t").append("if(this." + spec.getLocalCol() + "_fk!=null){");
                                    sb.append("\n\t\t\t\t").append("if(hiddenCount++>0)str+=\",\";");
                                    sb.append("\n\t\t\t\t").append("str+=\"" + spec.getLocalCol() + "=\\\"\" + this." + spec.getLocalCol() + "_fk + \"\\\"\";");
                                    sb.append("\n\t\t\t").append("}");
                                }
                            }
                        }
                    }
                }else{
                    sb.append("\n\t\t\t").append("if(this." + formatted + "!=null){");
                    sb.append("\n\t\t\t\t").append("if(hiddenCount++>0)str+=\",\";");
                    sb.append("\n\t\t\t\t").append("str+=\"" + xmlPath + "=\\\"\" + this." + formatted + " + \"\\\"\";");
                    sb.append("\n\t\t\t").append("}");
                }
            }
            sb.append("\n\t\t").append("str +=\"]-->\";");
        }
        sb.append("\n\t\t").append("}");
        sb.append("\n\t\t").append("return str;");
        sb.append("\n\t").append("}");
        
        
        //WRITE XML Attributes - WRITER
        sb.append("\n\n");
        sb.append("\n\t").append("this.getXMLAtts=function(){");
        Object[]  attributesArray = xE.getAttributes().toArray();
        if (e.isExtension())
            sb.append("\n\t\t").append("var attTxt = this.extension.getXMLAtts();");
        else
            sb.append("\n\t\t").append("var attTxt = \"\";");
        for (int i=0;i<attributesArray.length;i++){
            XMLWrapperField attField = (XMLWrapperField)attributesArray[i];
            if (attField.isReference())
            {
            }else
            {
                String fieldID = attField.getId();
                String xmlPath = attField.getXMLPathString();
                String formatted = formatFieldName(xmlPath);
                sb.append("\n\t\tif (this.").append(formatted).append("!=null)");
                sb.append("\n\t\t\tattTxt+=\" ").append(attField.getName(false)).append("=\\\"\" +this.").append(formatted).append(" +\"\\\"\";");
                if (attField.isRequired()){
                    sb.append("\n\t\telse attTxt+=\" ").append(attField.getName(false)).append("=\\\"\\\"\";//REQUIRED FIELD\n");
                }else{
                    sb.append("\n\t\t//NOT REQUIRED FIELD\n");
                }
            }
        }
        sb.append("\n\t\t").append("return attTxt;");
        sb.append("\n\t").append("}");
        
        //WRITE XML Body tags - WRITER
        sb.append("\n\n");
        sb.append("\n\t").append("this.getXMLBody=function(preventComments){");
        sb.append("\n\t\t").append("var xmlTxt=this.getXMLComments(preventComments);");
        if (e.isExtension())
            sb.append("\n\t\t").append("xmlTxt+=this.extension.getXMLBody(preventComments);");
        int iterCounter =0;
        Iterator childElements = xE.getChildren().iterator();
        while(childElements.hasNext())
        {
            XMLWrapperField xmlField = (XMLWrapperField)childElements.next();
            iterCounter =writeFieldToXMLCode(e, xmlField, iterCounter, sb);
        }
        
//        if (e.isANoChildElement())
//            sb.append("\n\t").append("return false;");
//        else
//            sb.append("\n\t").append("return true;");
        sb.append("\n\t\t").append("return xmlTxt;");
        sb.append("\n\t").append("}");
        


        //WRITE HAS XML CONTENT
        sb.append("\n\n");
        sb.append("\n\t").append("this.hasXMLComments=function(){");
        if (addins.size()>0){
            for(XFTFieldWrapper xwf:addins){
                GenericWrapperField gwf = (GenericWrapperField)xwf;
                String xmlPath = gwf.getXMLPathString();
                String formatted = formatFieldName(xmlPath);
                
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
                                    sb.append("\n\t\t\t").append("if (this." + spec.getLocalCol() + "_fk!=null) return true;");                                        
                                }
                            }
                        }
                    }
                }else{
                    sb.append("\n\t\t\t").append("if (this." + formatted + "!=null) return true;");
                }
            }
            sb.append("\n\t\t\t").append("return false;");
        }
        sb.append("\n\t").append("}");

        //WRITE HAS XML CONTENT
        sb.append("\n\n");
        sb.append("\n\t").append("this.hasXMLBodyContent=function(){");

        boolean returned =false;
        childElements = xE.getChildren().iterator();
        while(childElements.hasNext() && !returned)
        {
            XMLWrapperField xmlField = (XMLWrapperField)childElements.next();
            if (xmlField.getExpose())
            {
                String xmlPath = xmlField.getXMLPathString();
                String formatted = formatFieldName(xmlPath);
                if (xmlField.isReference())
                {
                    try {
                        XMLWrapperElement foreign = (XMLWrapperElement)xmlField.getReferenceElement();
                        if (xmlField.isMultiple())
                        { 
                            sb.append("\n\t\t").append("if(this." + formatted + ".length>0) return true;");
                        }else
                        {
                            if (!e.getExtensionFieldName().equalsIgnoreCase(xmlField.getName())){
                                sb.append("\n\t\t").append("if (this." + formatted + "!=null){");
                                sb.append("\n\t\t\t").append("if (this." + formatted + ".hasXMLBodyContent()) return true;");
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
                            sb.append("\n\t\tif (this.").append(formattedATT).append("!=null)");
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
                                    sb.append("\n\t\t\t").append("if(this." + childVariable.getKey() + ".length>0)return true;");
                                }else{
                                    sb.append("\n\t\t\t").append("if(this." + childVariable.getKey() + "!=null) return true;");
                                }
                            }
                        }else
                        {

                            if (xmlField.getXMLType()==null){
                            }else
                            {
                                sb.append("\n\t\t").append("if (this." + formatted + "!=null) return true;");
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
            sb.append("\n\t\t").append("if(this.hasXMLComments())return true;");
            
            if (e.isExtension())
                sb.append("\n\t\t").append("if(this.extension.hasXMLBodyContent())return true;");

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
        
        File finalLocation = new File(location + File.separator + "generated" + File.separator);
        finalLocation.mkdirs();


        if (XFT.VERBOSE)
             System.out.println("Generating File " + finalLocation.getAbsolutePath() + File.separator +sqlName +".js...");
        FileUtils.OutputToFile(sb.toString(),finalLocation.getAbsolutePath() + File.separator +sqlName + ".js");
        
    }
    
    public Hashtable<String,String> getSubVariables(GenericWrapperElement e,XMLWrapperField xmlField, boolean isRoot)
    {
        Hashtable<String,String> subMethods = new Hashtable<String,String>();

        if (xmlField.getExpose())
        {
            String xmlPath = xmlField.getXMLPathString();
            String formatted = formatFieldName(xmlPath);
            if (xmlField.isReference())
            {
                if (xmlField.isMultiple())
                {
                    subMethods.put(formatted,"MULTI");
                }else
                {
                    if (!e.getExtensionFieldName().equalsIgnoreCase(xmlField.getName())){
                        subMethods.put(formatted,"SINGLE");
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
                            subMethods.put(formattedATT,"ATT");
                        else
                            subMethods.put(formattedATT,"SINGLE");
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
                        subMethods.put(formatted,"SINGLE");
                    }
                }
            }
        }
        
        return subMethods;
    }

        
    public int writeFieldToXMLCode(GenericWrapperElement e,XMLWrapperField xmlField,int iterCounter, StringBuffer sb)
    {
        if (xmlField.getExpose())
        {
            String xmlPath = xmlField.getXMLPathString();
            String formatted = formatFieldName(xmlPath);
            if (xmlField.isReference())
            {
                try {
                    XMLWrapperElement foreign = (XMLWrapperElement)xmlField.getReferenceElement();
                    if (xmlField.isMultiple())
                    { 
                        String formattedCount = formatted + "COUNT";
                        if (xmlField.isInLineRepeaterElement())
                        {
                            
                            sb.append("\n\t\t").append("for(var " + formattedCount + "=0;" + formattedCount + "<this." + formatted + ".length;" + formattedCount + "++){");
                            sb.append("\n\t\t\t").append("xmlTxt+=this." + formatted + "[" + formattedCount + "].getXMLBody(preventComments);");
                            sb.append("\n\t\t").append("}");
                        }else{
                            sb.append("\n\t\t").append("for(var " + formattedCount + "=0;" + formattedCount + "<this." + formatted + ".length;" + formattedCount + "++){");
                            sb.append("\n\t\t\t").append("xmlTxt +=\"\\n<" + xmlField.getName(true) + "\";");
                            sb.append("\n\t\t\t").append("xmlTxt +=this." + formatted + "[" + formattedCount + "].getXMLAtts();");
                            sb.append("\n\t\t\t").append("if(this." + formatted + "[" + formattedCount + "].xsiType!=\"" + xmlField.getXMLType().getFullForeignType() + "\"){");
                            sb.append("\n\t\t\t\t").append("xmlTxt+=\" xsi:type=\\\"\" + this." + formatted + "[" + formattedCount + "].xsiType + \"\\\"\";");
                            sb.append("\n\t\t\t").append("}");
                            sb.append("\n\t\t\t").append("if (this." + formatted + "[" + formattedCount + "].hasXMLBodyContent()){");
                            sb.append("\n\t\t\t\t").append("xmlTxt+=\">\";");
                            sb.append("\n\t\t\t\t").append("xmlTxt+=this." + formatted + "[" + formattedCount + "].getXMLBody(preventComments);");

                            sb.append("\n\t\t\t\t\t").append("xmlTxt+=\"</" + xmlField.getName(true) + ">\";");

                            sb.append("\n\t\t\t").append("}else {xmlTxt+=\"/>\";}");
                            
                                
                            sb.append("\n\t\t").append("}");
                        }
                    }else
                    {
                        if (!e.getExtensionFieldName().equalsIgnoreCase(xmlField.getName())){
                            sb.append("\n\t\t").append("if (this." + formatted + "!=null){");

                            boolean isNewElement= true;
                            if (! xmlField.isChildXMLNode())
                            {
                                isNewElement= false;
                            }

                            if (! isNewElement){
                                sb.append("\n\t\t\t").append("xmlTxt+=this." + formatted + ".getXMLBody(writer,header);");
                            }else{
                                sb.append("\n\t\t\t").append("xmlTxt+=\"\\n<" + xmlField.getName(true) + "\";");
                                sb.append("\n\t\t\t").append("xmlTxt+=this." + formatted + ".getXMLAtts();");
                                sb.append("\n\t\t\t").append("if(this." + formatted + ".xsiType!=\"" + xmlField.getXMLType().getFullForeignType() + "\"){");
                                sb.append("\n\t\t\t\t").append("xmlTxt+=\" xsi:type=\\\"\" + this." + formatted + ".xsiType + \"\\\"\";");
                                sb.append("\n\t\t\t").append("}");

                                sb.append("\n\t\t\t").append("if (this." + formatted + ".hasXMLBodyContent()){");
                                sb.append("\n\t\t\t\t").append("xmlTxt+=\">\";");
                                sb.append("\n\t\t\t\t").append("xmlTxt+=this." + formatted + ".getXMLBody(preventComments);");
                                sb.append("\n\t\t\t\t").append("xmlTxt+=\"</" + xmlField.getName(true) + ">\";");
                                sb.append("\n\t\t\t").append("}else {xmlTxt+=\"/>\";}");
                            }
                            sb.append("\n\t\t").append("}");
                            if (! xmlField.getWrapped().getMinOccurs().equalsIgnoreCase("0")){
                                sb.append("\n\t\t").append("else{");
                                sb.append("\n\t\t\t").append("xmlTxt+=\"\\n<" + xmlField.getName(true) + "/>\";//REQUIRED");
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
                    sb.append("\n\t\tvar " + formatted + "ATT = \"\"");
                    Iterator attributes = xmlField.getAttributes().iterator();
                    while (attributes.hasNext())
                    {
                        XMLWrapperField attField = (XMLWrapperField)attributes.next();
                        String fieldID = attField.getId();
                        String xmlPathATT = attField.getXMLPathString();
                        String formattedATT = formatFieldName(xmlPathATT);
                        sb.append("\n\t\tif (this.").append(formattedATT).append("!=null)");

                        if(attField.getXMLType().getLocalType().equals("string"))
                        	sb.append("\n\t\t\t" + formatted + "ATT+=\" ").append(attField.getName(false)).append("=\\\"\" + this.").append(formattedATT).append(".replace(/>/g,\"&gt;\").replace(/</g,\"&lt;\") + \"\\\"\";");
                        else
                        	sb.append("\n\t\t\t" + formatted + "ATT+=\" ").append(attField.getName(false)).append("=\\\"\" + this.").append(formattedATT).append(" + \"\\\"\";");
                        
                        if (attField.isRequired()){
                            sb.append("\n\t\telse " + formatted + "ATT+=\" ").append(attField.getName(false)).append("=\"+ this.").append(formattedATT).append(" +\"\\\"\";//REQUIRED FIELD\n");
                            
                        }
                    }
                }
                    
                if (xmlField.getChildren().size() > 0)
                {          
                    sb.append("\n\t\t\t").append("var child"+iterCounter + "=0;");
                    sb.append("\n\t\t\t").append("var att"+iterCounter + "=0;");
                    for (Map.Entry<String,String> childVariable : this.getSubVariables(e, xmlField,true).entrySet()){
                        if (childVariable.getValue().equals("MULTI"))
                        {
                            sb.append("\n\t\t\t").append("child"+iterCounter + "+=this." + childVariable.getKey() + ".length;");
                        }else if(childVariable.getValue().equals("ATT")){
                            sb.append("\n\t\t\t").append("if(this." + childVariable.getKey() + "!=null)");
                            sb.append("\n\t\t\t").append("att"+iterCounter + "++;");
                        }else{
                            sb.append("\n\t\t\t").append("if(this." + childVariable.getKey() + "!=null)");
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
                    
                    sb.append("\n\t\t\t\t").append("xmlTxt+=\"\\n<" + xmlField.getName(true) + "\";");
                    if (attAL.size() > 0)
                    {   
                        sb.append("\n\t\t\t\t").append("xmlTxt+=" + formatted + "ATT;");
                    }
                    sb.append("\n\t\t\t").append("if(child"+tempCounter + "==0){");
                    sb.append("\n\t\t\t\t").append("xmlTxt+=\"/>\";");
                    sb.append("\n\t\t\t").append("}else{");

                    sb.append("\n\t\t\t\t").append("xmlTxt+=\">\";");
                    
                    sb.append(childCode);
                    
                    sb.append("\n\t\t\t\t").append("xmlTxt+=\"\\n</" + xmlField.getName(true) + ">\";");

                    sb.append("\n\t\t\t").append("}");
                    sb.append("\n\t\t\t").append("}\n");
                }else
                {

                    if (xmlField.getXMLType()==null){
                        sb.append("\n\t\t").append("if(" + formatted + "ATT!=\"\"){");
                        sb.append("\n\t\t\t").append("xmlTxt+=\"\\n<" + xmlField.getName(true) + "\";");
                        if (attAL.size() > 0)
                        {   
                            sb.append("\n\t\t\t").append("xmlTxt+=" + formatted + "ATT;");
                        }
                        sb.append("\n\t\t\t").append("xmlTxt+=\"/>\";");
                        sb.append("\n\t\t").append("}\n");
                    }else
                    {
                        sb.append("\n\t\t").append("if (this." + formatted + "!=null){");
                        if (!e.isANoChildElement()){
                            sb.append("\n\t\t\t").append("xmlTxt+=\"\\n<" + xmlField.getName(true) + "\";");
                            if (attAL.size() > 0)
                            {   
                                sb.append("\n\t\t\t").append("xmlTxt+=" + formatted + "ATT;");
                            }
                            sb.append("\n\t\t\t").append("xmlTxt+=\">\";");
                        }
                        if(xmlField.getXMLType().getLocalType().equals("string"))
                        	sb.append("\n\t\t\t").append("xmlTxt+=this.").append(formatted).append(".replace(/>/g,\"&gt;\").replace(/</g,\"&lt;\");");
                        else
                        	sb.append("\n\t\t\t").append("xmlTxt+=this.").append(formatted).append(";");
                        
                        if (!e.isANoChildElement())sb.append("\n\t\t\t").append("xmlTxt+=\"</" + xmlField.getName(true) + ">\";");
                        sb.append("\n\t\t").append("}");
                        if (xmlField.isRequired())
                        {
                            sb.append("\n\t\t").append("else{");
                            sb.append("\n\t\t\t").append("xmlTxt+=\"\\n<" + xmlField.getName(true) + "\";");
                            if (attAL.size() > 0)
                            {   
                                sb.append("\n\t\t\t").append("xmlTxt+=" + formatted + "ATT;");
                            }
                            sb.append("\n\t\t\t").append("xmlTxt+=\"/>\";");
                            sb.append("\n\t\t").append("}\n");
                        }else if (attAL.size() > 0){
                            sb.append("\n\t\t").append("else if(" + formatted + "ATT!=\"\"){");
                            sb.append("\n\t\t\t").append("xmlTxt+=\"\\n<" + xmlField.getName(true) + "\";");
                            sb.append("\n\t\t\t").append("xmlTxt+=" + formatted + "ATT;");
                            sb.append("\n\t\t\t").append("xmlTxt+=\"/>\";");
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
    /**
     * @param e
     * @return
     */
    private String formatFieldName(String s)
    {
        return XftStringUtils.FormatStringToMethodSignature(s);
    }
    

    public static void GenerateJSFiles(String javalocation, boolean skipXDAT) throws Exception
    {

        JavaScriptGenerator generator = new JavaScriptGenerator();
        ArrayList al = XFTMetaManager.GetElementNames();
        Iterator iter = al.iterator();

        StringBuffer includes = new StringBuffer();
        includes.append("\n<script type=\"text/javascript\" src=\"$content.getURI(\"scripts/generated/ClassMapping.js\")\"></script>");
        includes.append("\n<script type=\"text/javascript\" src=\"$content.getURI(\"scripts/generated/SAXEventHandler.js\")\"></script>");
        includes.append("\n<script type=\"text/javascript\" src=\"$content.getURI(\"scripts/generated/xmlsax.js\")\"></script>");
        
        Hashtable<String,String> elements =new Hashtable<String,String>();
        
        //SECOND PASS
        iter = al.iterator();
        while (iter.hasNext())
        {
            String s = (String)iter.next();
            GenericWrapperElement e = GenericWrapperElement.GetElement(s);
            if (e.getAddin().equalsIgnoreCase(""))
            {
                includes.append("\n<script type=\"text/javascript\" src=\"$content.getURI(\"scripts/generated/" + e.getFormattedName() + ".js\")\"></script>");
                elements.put(e.getSchemaTargetNamespaceURI() + ":" + e.getLocalXMLName(), e.getFormattedName());
                elements.put(e.getFullXMLName(), e.getFormattedName());
                if (!e.getProperName().equals(e.getFullXMLName()))
                {
                    elements.put(e.getSchemaTargetNamespaceURI() + ":" + e.getProperName(), e.getFormattedName());
                    elements.put(e.getSchemaTargetNamespacePrefix() + ":" + e.getProperName(), e.getFormattedName());
                }
                if ((!skipXDAT) || (!e.getSchemaTargetNamespacePrefix().equalsIgnoreCase("xdat")))
                {
                    generator.generateJSFile(e,javalocation);
                }
            }
        }
        
        StringBuffer sb = new StringBuffer();
        
        sb.append("\n\nfunction ClassMapping(){");
//        sb.append("\n\t").append("this.alias = new Array();");
//        sb.append("\n\t").append("this.elements = new Array();");
//        for (Map.Entry<String, String> entry : elements.entrySet())
//        {
//            sb.append("\n\t").append("this.alias.push(\"" + entry.getKey() + "\");");
//            sb.append("\n\t").append("this.elements.push(\"" + entry.getValue() + "\");");
//        }
        
        sb.append("\n\t").append("this.newInstance=function(name){");
        for (Map.Entry<String, String> entry : elements.entrySet())
        {
            sb.append("\n\t\t").append("if(name==\"" + entry.getKey() + "\"){");
            sb.append("\n\t\t\t").append("if(window." + entry.getValue() + "==undefined)dynamicJSLoad('" + entry.getValue() + "','generated/" + entry.getValue() + ".js');");
            sb.append("\n\t\t\t").append("return new " + entry.getValue() + "();");
            sb.append("\n\t\t").append("}");
        }
        sb.append("\n\t").append("}");
        
//        sb.append("\n\t").append("this.loadSpecification=function(name){");
//        sb.append("\n\t").append("    var e=document.createElement(\"script\");");
//        sb.append("\n\t").append("    e.src=serverRoot+\"scripts/generated/\" + name + \".js\";");
//        sb.append("\n\t").append("    e.type=\"text/javascript\";");
//        sb.append("\n\t").append("    document.getElementByTagName(\"head\")[0].appendChild(e);");
//        sb.append("\n\t").append("}");
        
        sb.append("\n}");
        
        outputToFile(javalocation,sb.toString(),"generated/ClassMapping.js");
        //outputToFile(javalocation,includes.toString(),"generated/AllIncludes.vm");
        
    }
    
    public static void outputToFile(String location,String contents, String fileName){
        
        File f = new File(location,fileName);
        
        f.getParentFile().mkdirs();
        
        if (XFT.VERBOSE)
             System.out.println("Generating File " + f.getAbsolutePath());
        FileUtils.OutputToFile(contents,f.getAbsolutePath());
    }
}
