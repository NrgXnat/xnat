/*
 * web: org.nrg.xnat.turbine.modules.screens.PrearchiveMatch
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XnatMrsessiondata;
import org.nrg.xdat.om.XnatPetsessiondata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.turbine.utils.ScanQualityUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class PrearchiveMatch extends SecureScreen {
    static org.apache.log4j.Logger logger = Logger.getLogger(PrearchiveMatch.class);

    final String[] mr_identifiers={"xnat:mrSessionData.ID","xnat:mrSessionData.label","xnat:mrSessionData.sharing.share.label","xnat:mrSessionData.dcmPatientId","xnat:mrSessionData.dcmPatientName"};
    final String[] pet_identifiers={"xnat:petSessionData.ID","xnat:petSessionData.label","xnat:petSessionData.sharing.share.label"};
    final String[] ct_identifiers={"xnat:ctSessionData.ID","xnat:ctSessionData.label","xnat:ctSessionData.sharing.share.label"};
    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        ArrayList allMatchers=new ArrayList();

        String project = (String)TurbineUtils.GetPassedParameter("project", data);
        context.put("project", project);
        
        String prearchive_path= ArcSpecManager.GetInstance().getPrearchivePathForProject(project);
        
        if (!prearchive_path.endsWith(File.separator)){
            prearchive_path += File.separator;
        }
        
        final UserI user =TurbineUtils.getUser(data);
        context.put("qualityLabels", ScanQualityUtils.getQualityLabels(project, user));

        File dir = new File(prearchive_path);
        
        if (dir.exists())
        {
            //PREARCHIVE ROOT
            File[] timestamps = dir.listFiles();
            for (int i=0;i<timestamps.length;i++){
                if (timestamps[i].isDirectory()){

                        //TIMESTAMP FOLDER
                        File[] sessions=timestamps[i].listFiles();
                        if (sessions.length==0){
                        }else{
                            for(int j=0;j<sessions.length;j++)
                            {
                                if (sessions[j].isDirectory())
                                {
                                    File session = sessions[j];
                                    
                                    try {
                                        File xml = new File(session.getAbsolutePath() + ".xml");
                                        File txt = new File(session.getAbsolutePath() + ".txt");
                                        if (xml.exists() && !txt.exists())
                                        {
                                            SAXReader reader = new SAXReader(TurbineUtils.getUser(data));
                                            XFTItem item = reader.parse(xml.getAbsolutePath());

                                            ItemCollection items = new ItemCollection();
                                            ArrayList sessionAL = new ArrayList();
                                            if (item.getXSIType().equalsIgnoreCase("xnat:mrSessionData")){
                                                final String[] identifiers=mr_identifiers;
                                            
                                                XnatMrsessiondata mr = new XnatMrsessiondata(item);
                                                sessionAL.add(mr);
                                                
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
                                                Date d = sdf.parse(timestamps[i].getName());
                                                java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("MM/dd/yyyy HH:mm:ss");
                                                sessionAL.add(formatter.format(d) + " : " + session.getName());
                                                sessionAL.add(timestamps[i].getName() + "/" + session.getName());

                                                ArrayList fields = new ArrayList();

                                                if (mr.getId()!=null)
                                                {
                                                    CriteriaCollection cc= new CriteriaCollection("OR");
                                                    
                                                    for(String xmlPath : identifiers){
                                                        cc.addClause(xmlPath, mr.getId());
                                                    }
                                                    
                                                    XnatMrsessiondata match=null;
                                                    
                                                    ArrayList<XnatMrsessiondata> matched= XnatMrsessiondata.getXnatMrsessiondatasByField(cc, null, false);
                                                    
                                                    if (matched.size() >0){
                                                        match=matched.get(0);
                                                    }
                                                    
                                                    ArrayList field = new ArrayList();
                                                    field.add("ID");
                                                    field.add(true);
                                                    
                                                    if (match!=null)
                                                    {
                                                        ArrayList matches = new ArrayList();
                                                        ArrayList secured = new ArrayList();
                                                        if (!items.contains(match, true))
                                                        {
                                                            if(Permissions.canRead(user, match) && Permissions.canEdit(user, match)){
                                                                ArrayList matchAL =new ArrayList();
                                                                matchAL.add(match);
                                                                matchAL.add(mr.getId());
                                                                matches.add(matchAL);
                                                                items.add(match);
                                                            }else{
                                                                secured.add(match);
                                                            }
                                                            
                                                            field.add(matches);
                                                            field.add(secured);
                                                            
                                                            fields.add(field);
                                                        }
                                                    }
                                                }
                                                
                                                if (mr.getDcmpatientid()!=null)
                                                {
                                                    CriteriaCollection cc= new CriteriaCollection("OR");
                                                
                                                    for(String xmlPath : identifiers){
                                                        cc.addClause(xmlPath, mr.getDcmpatientid());
                                                    }
                                                    
                                                    ArrayList matched= XnatMrsessiondata.getXnatMrsessiondatasByField(cc, user, false);
                                                    
                                                    ArrayList field = new ArrayList();
                                                    field.add("Patient Name");
                                                    field.add(true);

                                                    ArrayList matches = new ArrayList();
                                                    ArrayList secured = new ArrayList();
                                                    
                                                    Iterator matchedIter = matched.iterator();
                                                    while (matchedIter.hasNext()){
                                                        XnatMrsessiondata match = (XnatMrsessiondata)matchedIter.next();
                                                        if (match!=null)
                                                        {
                                                            if (!items.contains(match, true))
                                                            {
                                                                if(Permissions.canRead(user, match) && Permissions.canEdit(user, match)){
                                                                    ArrayList matchAL =new ArrayList();
                                                                    matchAL.add(match);
                                                                    matchAL.add(mr.getId());
                                                                    matches.add(matchAL);
                                                                    items.add(match);
                                                                }else{
                                                                    secured.add(match);
                                                                }
                                                            }
                                                        }
                                                    }
                                                    field.add(matches);
                                                    field.add(secured);
                                                    
                                                    fields.add(field);
                                                }
                                                
                                                if (mr.getDcmpatientname()!=null)
                                                {
                                                    CriteriaCollection cc= new CriteriaCollection("OR");
                                                    
                                                    for(String xmlPath : identifiers){
                                                        cc.addClause(xmlPath, mr.getDcmpatientname());
                                                    }
                                                    ArrayList matched= XnatMrsessiondata.getXnatMrsessiondatasByField(cc, user, false);
                                                    
                                                    ArrayList field = new ArrayList();
                                                    field.add("Patient Name");
                                                    field.add(true);

                                                    ArrayList matches = new ArrayList();
                                                    ArrayList secured = new ArrayList();
                                                    
                                                    Iterator matchedIter = matched.iterator();
                                                    while (matchedIter.hasNext()){
                                                        XnatMrsessiondata match = (XnatMrsessiondata)matchedIter.next();
                                                        if (match!=null)
                                                        {
                                                            if (!items.contains(match, true))
                                                            {
                                                                if(Permissions.canRead(user, match) && Permissions.canEdit(user, match)){
                                                                    ArrayList matchAL =new ArrayList();
                                                                    matchAL.add(match);
                                                                    matchAL.add(mr.getId());
                                                                    matches.add(matchAL);
                                                                    items.add(match);
                                                                }else{
                                                                    secured.add(match);
                                                                }
                                                            }
                                                        }
                                                    }
                                                    field.add(matches);
                                                    field.add(secured);
                                                    
                                                    fields.add(field);
                                                }

                                                sessionAL.add(items.size());
                                                sessionAL.add(fields);
                                                allMatchers.add(sessionAL);
                                            }else if (item.getXSIType().equalsIgnoreCase("xnat:petSessionData")){
                                                XnatPetsessiondata pet = new XnatPetsessiondata(item);
                                                sessionAL.add(pet);
                                                
                                                final String[] identifiers=pet_identifiers;
                                                
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
                                                Date d = sdf.parse(timestamps[i].getName());
                                                java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("MM/dd/yyyy HH:mm:ss");
                                                sessionAL.add(formatter.format(d) + " : " + session.getName());
                                                sessionAL.add(timestamps[i].getName() + "/" + session.getName());


                                                int containsMatch=0;
                                                ArrayList fields = new ArrayList();
                                                
                                                if (pet.getId()!=null)
                                                {
                                                    CriteriaCollection cc= new CriteriaCollection("OR");
                                                    
                                                    for(String xmlPath : identifiers){
                                                        cc.addClause(xmlPath, pet.getId());
                                                    }
                                                    
                                                    XnatPetsessiondata match=null;
                                                    
                                                    ArrayList<XnatPetsessiondata> matched= XnatPetsessiondata.getXnatPetsessiondatasByField(cc, null, false);
                                                    
                                                    if (matched.size() >0){
                                                        match=matched.get(0);
                                                    }
                                                    
                                                    ArrayList field = new ArrayList();
                                                    field.add("ID");
                                                    field.add(true);

                                                    ArrayList matches = new ArrayList();
                                                    ArrayList secured = new ArrayList();
                                                    if (match!=null)
                                                    {
                                                        if (!items.contains(match, true))
                                                        {
                                                            if(Permissions.canRead(user, match) && Permissions.canEdit(user, match)){
                                                                ArrayList matchAL =new ArrayList();
                                                                matchAL.add(match);
                                                                matchAL.add(pet.getId());
                                                                matches.add(matchAL);
                                                                items.add(match);
                                                            }else{
                                                                secured.add(match);
                                                            }
                                                            
                                                            field.add(matches);
                                                            field.add(secured);
                                                            
                                                            fields.add(field);
                                                        }
                                                    }
                                                }
                                                
                                                if (pet.getPatientid()!=null)
                                                {
                                                    CriteriaCollection cc= new CriteriaCollection("OR");
                                                    
                                                    for(String xmlPath : identifiers){
                                                        cc.addClause(xmlPath, pet.getPatientid());
                                                    }
                                                    ArrayList matched= XnatPetsessiondata.getXnatPetsessiondatasByField(cc, user, false);
                                                    
                                                    
                                                    ArrayList field = new ArrayList();
                                                    field.add("Patient ID");
                                                    field.add(true);

                                                    ArrayList matches = new ArrayList();
                                                    ArrayList secured = new ArrayList();
                                                    
                                                    Iterator matchedIter = matched.iterator();
                                                    while (matchedIter.hasNext()){
                                                        XnatPetsessiondata match = (XnatPetsessiondata)matchedIter.next();
                                                        if (match!=null)
                                                        {
                                                            if (!items.contains(match, true))
                                                            {
                                                                if(Permissions.canRead(user, match) && Permissions.canEdit(user, match)){
                                                                    ArrayList matchAL =new ArrayList();
                                                                    matchAL.add(match);
                                                                    matchAL.add(pet.getId());
                                                                    matches.add(matchAL);
                                                                    items.add(match);
                                                                }else{
                                                                    secured.add(match);
                                                                }
                                                            }
                                                            
                                                        }
                                                    }
                                                    field.add(matches);
                                                    field.add(secured);
                                                    
                                                    fields.add(field);
                                                }
                                                
                                                if (pet.getPatientname()!=null)
                                                {
                                                    CriteriaCollection cc= new CriteriaCollection("OR"); 
                                                    
                                                    for(String xmlPath : identifiers){
                                                        cc.addClause(xmlPath, pet.getPatientname());
                                                    }
                                                    ArrayList matched= XnatPetsessiondata.getXnatPetsessiondatasByField(cc, user, false);
                                                    
                                                    ArrayList field = new ArrayList();
                                                    field.add("Patient Name");
                                                    field.add(true);

                                                    ArrayList matches = new ArrayList();
                                                    ArrayList secured = new ArrayList();
                                                    
                                                    Iterator matchedIter = matched.iterator();
                                                    while (matchedIter.hasNext()){
                                                        XnatPetsessiondata match = (XnatPetsessiondata)matchedIter.next();
                                                        if (match!=null)
                                                        {
                                                            if (!items.contains(match, true))
                                                            {
                                                                if(Permissions.canRead(user, match) && Permissions.canEdit(user, match)){
                                                                    ArrayList matchAL =new ArrayList();
                                                                    matchAL.add(match);
                                                                    matchAL.add(pet.getId());
                                                                    matches.add(matchAL);
                                                                    items.add(match);
                                                                }else{
                                                                    secured.add(match);
                                                                }
                                                            }
                                                            
                                                        }
                                                    }
                                                    field.add(matches);
                                                    field.add(secured);
                                                    
                                                    fields.add(field);
                                                }
                                                sessionAL.add(items.size());
                                                sessionAL.add(fields);
                                                allMatchers.add(sessionAL);
                                            }
                                        }
                                    } catch (Throwable e) {
                                        logger.error("",e);
                                    }
                                }
                            }
                        }
                }
            }
            
        }
        context.put("sessions", allMatchers);
    }

}
