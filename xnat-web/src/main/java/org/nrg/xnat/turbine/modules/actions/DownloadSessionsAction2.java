/*
 * org.nrg.xnat.turbine.modules.actions.DownloadSessionsAction2
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/10/14 3:12 PM
 */
package org.nrg.xnat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xnat.utils.UserUtils;

import java.io.File;
import java.io.FileWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressWarnings("unused")
public class DownloadSessionsAction2 extends SecureAction {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocitySecureAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        String [] session_ids=((String[])TurbineUtils.GetPassedObjects("sessions",data));

        String [] requestScanTypes=((String[])TurbineUtils.GetPassedObjects("scan_type",data));
        String [] scanFormats=((String[])TurbineUtils.GetPassedObjects("scan_format",data));
        String [] recons=((String[])TurbineUtils.GetPassedObjects("recon",data));
        String [] assessors=((String[])TurbineUtils.GetPassedObjects("assessors",data));
        String [] resources=((String[])TurbineUtils.GetPassedObjects("resources",data));

		//BEGIN:IOWA customization: to allow project and subject included in path
        boolean projectIncludedInPath = "true".equalsIgnoreCase((String)TurbineUtils.GetPassedParameter("projectIncludedInPath", data));
        boolean subjectIncludedInPath = "true".equalsIgnoreCase((String)TurbineUtils.GetPassedParameter("subjectIncludedInPath",data));
        boolean simplified = "true".equalsIgnoreCase((String) TurbineUtils.GetPassedParameter("simplified", data));
        
        String extraParam = "";
        if(projectIncludedInPath){
            extraParam += "&projectIncludedInPath=true";
        }
        if(subjectIncludedInPath){
            extraParam += "&subjectIncludedInPath=true";
        }
        if(simplified){
            extraParam += "&structure=simplified";
        }
		//END:
        
        String server = TurbineUtils.GetFullServerPath();
        if (!server.endsWith("/")){
            server +="/";
        }
        
        List<String> l=new ArrayList<String>();
        CatCatalogBean cat = new CatCatalogBean();
        
        for(String session : session_ids){
            CatCatalogBean sessionCatalog = new CatCatalogBean();
            sessionCatalog.setId(session);

            // narrow down the range of scan types to only the ones relevant to this session
            if (requestScanTypes != null && requestScanTypes.length > 0) {
                String query = "SELECT id FROM xnat_imagescandata WHERE image_session_id = '" + session + "' AND " + getTypeClause(requestScanTypes);
                XFTTable table = XFTTable.Execute(query, TurbineUtils.getUser(data).getDBName(), TurbineUtils.getUser(data).getLogin());
                List<String> sessionScans = table.convertColumnToArrayList("id");
                if (sessionScans != null && sessionScans.size() > 0) {
                    CatCatalogBean scansCatalog = new CatCatalogBean();
                    scansCatalog.setId("RAW");
                    for (String scan : sessionScans) {
                        if (scanFormats != null && scanFormats.length > 0) {
                            for (String scanFormat : scanFormats) {
                                CatEntryBean entry = new CatEntryBean();
                                entry.setFormat("ZIP");
                                String uri = server + "data/experiments/" + session + "/scans/" + scan + "/resources/" + URLEncoder.encode(scanFormat, "UTF-8") + "/files?format=zip" + extraParam;
                                entry.setUri(uri);
                                l.add(uri);
                                scansCatalog.addEntries_entry(entry);
                            }
                        } else {
                            CatEntryBean entry = new CatEntryBean();
                            entry.setFormat("ZIP");
                            String uri = server + "data/experiments/" + session + "/scans/" + scan + "/files?format=zip" + extraParam;
                            entry.setUri(uri);
                            l.add(uri);
                            scansCatalog.addEntries_entry(entry);
                        }
                    }
                    sessionCatalog.addSets_entryset(scansCatalog);
                }
            }

            if (resources!=null && resources.length>0){
                final CatCatalogBean scansCatalog = new CatCatalogBean();
                scansCatalog.setId("RESOURCES");
                for(final String res : resources){
                    final CatEntryBean entry = new CatEntryBean();
                    entry.setFormat("ZIP");
                    String uri=server + "data/experiments/" + session + "/resources/" + URLEncoder.encode(res, "UTF-8") + "/files?format=zip" + extraParam;
                    entry.setUri(uri);
                    l.add(uri);
                    scansCatalog.addEntries_entry(entry);
                }
                sessionCatalog.addSets_entryset(scansCatalog);
            }
            
            if (recons!=null && recons.length>0){
                CatCatalogBean scansCatalog = new CatCatalogBean();
                scansCatalog.setId("RECONSTRUCTED");
                for(String scanType : recons){
                    CatEntryBean entry = new CatEntryBean();
                    entry.setFormat("ZIP");
                    String uri=server + "data/experiments/" + session + "/reconstructions/" + URLEncoder.encode(scanType, "UTF-8") + "/files?format=zip" + extraParam;
                    entry.setUri(uri);
                    l.add(uri);
                    scansCatalog.addEntries_entry(entry);
                }
                sessionCatalog.addSets_entryset(scansCatalog);
            }
            
            if (assessors!=null && assessors.length>0){
                CatCatalogBean scansCatalog = new CatCatalogBean();
                scansCatalog.setId("ASSESSORS");
                for(String scanType : assessors){
                    CatEntryBean entry = new CatEntryBean();
                    entry.setFormat("ZIP");
                    String uri=server + "data/experiments/" + session + "/assessors/" + URLEncoder.encode(scanType, "UTF-8") + "/files?format=zip" + extraParam;
                    entry.setUri(uri);
                    l.add(uri);
                    scansCatalog.addEntries_entry(entry);
                }
                sessionCatalog.addSets_entryset(scansCatalog);
            }
            
            cat.addSets_entryset(sessionCatalog);       
        }
        
        String id = Calendar.getInstance().getTimeInMillis() + "";
        File f = org.nrg.xdat.security.helpers.Users.getUserCacheFile(TurbineUtils.getUser(data),"catalogs/" + id + ".xml");

        boolean mkdirs = f.getParentFile().mkdirs();

        FileWriter fw = new FileWriter(f);
        cat.toXML(fw, true);
        fw.flush();
        fw.close();
        
        String catalogXML = server + "archive/catalogs/stored/" + id + ".xml";
        if (TurbineUtils.GetPassedParameter("download_option",data).equals("applet")){
            context.put("catalogXML", catalogXML);
            context.put("sessions", l);
            
            data.setScreenTemplate("DownloadApplet.vm");
        }else{
            data.setRedirectURI(catalogXML);
        }     
    }

    private String getTypeClause(final String[] requestScanTypes) {
        StringBuilder buffer = new StringBuilder();
        boolean foundNull = false;
        for (String item : requestScanTypes) {
            if (StringUtils.isBlank(item) || item.equalsIgnoreCase("NULL")) {
                foundNull = true;
                continue;
            }
            buffer.append("'").append(item).append("', ");
        }
        int length = buffer.length();
        if (length == 0 && !foundNull) {
            throw new RuntimeException("Bad state found: no scan types specified, not even NULL!");
        }
        if (length > 0) {
            buffer.delete(length - 2, length);
        }
        if (foundNull) {
            if (length > 0) {
                buffer.insert(0, "(type IN (");
                buffer.append(") OR type is NULL)");
            } else {
                buffer.append("type is NULL");
            }
        } else {
            buffer.insert(0, "type IN (");
            buffer.append(")");
        }
        return buffer.toString();
    }
}
