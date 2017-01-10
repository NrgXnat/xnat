/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_uploadCSV1
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.FieldMapping;

public class XDATScreen_uploadCSV1 extends SecureScreen {

    @Override
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
        FieldMapping fm = (FieldMapping)context.get("fm");
        String fm_id = (String)TurbineUtils.GetPassedParameter("fm_id", data);
        if (fm==null && fm_id!=null){
            File f = Users.getUserCacheFile(TurbineUtils.getUser(data),"csv/" + fm_id + ".xml");
            fm  = new FieldMapping(f);
        }
        String root = fm.getElementName();
        
        GenericWrapperElement gwe = GenericWrapperElement.GetElement(root);
        
        Hashtable<String,ArrayList<Object>> all = new Hashtable<String,ArrayList<Object>>();
        
        ArrayList<String> fields =ViewManager.GetFieldNames(gwe,ViewManager.ACTIVE,false,true);
        
        ArrayList<String> cleaned =new ArrayList<String>();
        ArrayList<String> required =new ArrayList<String>();
                
        Hashtable<String,ArrayList<String>> extendable = new Hashtable<String,ArrayList<String>>();
        
        for(String s: fields){
            s = root + "/" + GenericWrapperElement.GetCompactXMLPath(s);
            if ((! s.endsWith("/meta/last_modified")) &&
                    (! s.endsWith("/meta/status")) &&
                    (! s.endsWith("/meta/activation_date")) &&
                    (! s.endsWith("/meta/insert_date")) &&
                    (! s.endsWith("/meta/activation_user_xdat_user_id")) &&
                    (! s.endsWith("/meta/insert_user_xdat_user_id")) &&
                    (! s.endsWith("/meta/origin")) &&
                    (! s.endsWith("/meta/modified")) &&
                    (! s.endsWith("/meta/meta_data_id")) &&
                    (! s.endsWith("/meta/shareable")) &&
                    (! s.endsWith("/extension")) &&
                    (! s.equals(root +"/project")) &&
                    (! s.equals(root +"/ID")) &&
                    (! s.endsWith("_info")) &&
                    (! s.endsWith("/extension_item/element_name")) &&
                    (! s.endsWith("/extension_item/xdat_meta_element_id")) ){
                if (!cleaned.contains(s))
                    cleaned.add(s);
            }else if(s.endsWith("/extension")){
                String xmlPath = s.substring(0,s.length()-10);
                if (xmlPath.indexOf("/")>-1){
                    GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
                    if (f.isReference()){
                        if (extendable.get(xmlPath)==null)
                        {
                            extendable.put(xmlPath, new ArrayList<String>());
                            extendable.get(xmlPath).add(f.getReferenceElementName().getFullForeignType());
                        }
                        for(SchemaElementI se :f.getReferenceElement().getGenericXFTElement().getPossibleExtenders())
                        {
                            extendable.get(xmlPath).add(se.getFullXMLName());
                        }
                    }else{
                        if (!cleaned.contains(s))
                            cleaned.add(s);
                    }
                }
            }else if((s.equals(root +"/project"))){
                context.put("hasProject",true);
            }else if(s.equals(root +"/ID")){
                if (!required.contains(s))
                    required.add(s);
            }
        }

        ArrayList<String> toRemove = new ArrayList<String>();
        for(String key : extendable.keySet()){
            for(String value : cleaned){
                if (value.startsWith(key)){
                    toRemove.add(value);
                }
            }
        }
        
        for(String key : toRemove){
            cleaned.remove(key);
        }
        
        ArrayList<Object> temp = new ArrayList<Object>();
        temp.add(cleaned);
        temp.add(extendable);
        temp.add(required);
        all.put(root, temp);
                
        for(Map.Entry<String,ArrayList<String>> entry: extendable.entrySet()){
            for(String relation : entry.getValue()){
                root = relation;
                if (!all.containsKey(root)){
                    gwe = GenericWrapperElement.GetElement(root);
                    
                    
                    fields =ViewManager.GetFieldNames(gwe,ViewManager.ACTIVE,false,true);
                    
                    cleaned =new ArrayList<String>();
                    extendable = new Hashtable<String,ArrayList<String>>();
                    
                    for(String s: fields){
                        s = root + "/" + GenericWrapperElement.GetCompactXMLPath(s);
                        if ((! s.endsWith("/meta/last_modified")) &&
                                (! s.endsWith("/meta/status")) &&
                                (! s.endsWith("/meta/activation_date")) &&
                                (! s.endsWith("/meta/insert_date")) &&
                                (! s.endsWith("/meta/activation_user_xdat_user_id")) &&
                                (! s.endsWith("/meta/insert_user_xdat_user_id")) &&
                                (! s.endsWith("/meta/origin")) &&
                                (! s.endsWith("/meta/modified")) &&
                                (! s.endsWith("/meta/meta_data_id")) &&
                                (! s.endsWith("/meta/shareable")) &&
                                (! s.endsWith("/extension")) &&
                                (! s.endsWith("_info")) &&
                                (! s.endsWith("/extension_item/element_name")) &&
                                (! s.endsWith("/extension_item/xdat_meta_element_id")) ){
                            if (!cleaned.contains(s))
                                cleaned.add(s);
                        }else if(s.endsWith("/extension")){
                            String xmlPath = s.substring(0,s.length()-10);
                            if (xmlPath.indexOf("/")>-1){
                                GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
                                if (f.isReference()){
                                    if (extendable.get(xmlPath)==null)
                                    {
                                        extendable.put(xmlPath, new ArrayList<String>());
                                        extendable.get(xmlPath).add(f.getReferenceElementName().getFullForeignType());
                                    }
                                    for(SchemaElementI se :f.getReferenceElement().getGenericXFTElement().getPossibleExtenders())
                                    {
                                        extendable.get(xmlPath).add(se.getFullXMLName());
                                    }
                                }else{
                                    if (!cleaned.contains(s))
                                        cleaned.add(s);
                                }
                            }
                        }
                    }

                    toRemove = new ArrayList<String>();
                    for(String key : extendable.keySet()){
                        for(String value : cleaned){
                            if (value.startsWith(key)){
                                toRemove.add(value);
                            }
                        }
                    }
                    
                    for(String key : toRemove){
                        cleaned.remove(key);
                    }
                    
                    temp = new ArrayList<Object>();
                    temp.add(cleaned);
                    temp.add(extendable);
                    temp.add(new ArrayList());
                    all.put(root, temp);
                }
            }
            
        }
        
        context.put("allElements", all);
    }

    
    
}
