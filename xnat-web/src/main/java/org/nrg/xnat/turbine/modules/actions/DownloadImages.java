/*
 * web: org.nrg.xnat.turbine.modules.actions.DownloadImages
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import edu.sdsc.grid.io.GeneralFile;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatCatalogMetafieldBean;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.bean.CatEntryMetafieldBean;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXWriter;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.zip.TarUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.srb.XNATDirectory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.zip.ZipOutputStream;

/**
 * @author timo
 *
 */
public class DownloadImages extends SecureAction {
	static Logger logger = Logger.getLogger(DownloadImages.class);
    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @SuppressWarnings("deprecation")
    public void doPerform(RunData data, Context context) throws Exception {
        System.out.println("BEGIN DownloadImages.java");
        UserI user = TurbineUtils.getUser(data);
        long startTime = Calendar.getInstance().getTimeInMillis();
        XnatImagesessiondata mr = (XnatImagesessiondata)data.getSession().getAttribute("download_session");
        if (mr==null){

            ItemI o = TurbineUtils.getDataItem(data);
            
            if (o==null)o = TurbineUtils.GetItemBySearch(data,Boolean.TRUE);
            if (o!=null)mr = (XnatImagesessiondata)BaseElement.GetGeneratedItem(o);
        }else{
            data.getSession().removeAttribute("download_session");
        }
        
        try {
            java.util.Date today = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
            String fileName=mr.getArchiveDirectoryName() + "_" + (today.getMonth() + 1) + "_" + today.getDate() + "_" + (today.getYear() + 1900) + "_" + today.getHours() + "_" + today.getMinutes() + "_" + today.getSeconds() + ".zip";
            String contentType = "application/zip";
            
            boolean tar = false;
            int COMPRESSION = ZipUtils.DEFAULT_COMPRESSION;
            boolean cat = false;
            
            if (TurbineUtils.HasPassedParameter("download_type",data))
            {
                if (TurbineUtils.GetPassedParameter("download_type",data).equals("xar"))
                {
                    contentType = "application/xar";
                    fileName=mr.getArchiveDirectoryName() + "_" + (today.getMonth() + 1) + "_" + today.getDate() + "_" + (today.getYear() + 1900) + "_" + today.getHours() + "_" + today.getMinutes() + "_" + today.getSeconds() + ".xar";
                }else if (TurbineUtils.GetPassedParameter("download_type",data).equals("tar"))
                {
                    tar= true;
                    COMPRESSION=ZipOutputStream.STORED;
                    contentType = "application/tar";
                    fileName=mr.getArchiveDirectoryName() + "_" + (today.getMonth() + 1) + "_" + today.getDate() + "_" + (today.getYear() + 1900) + "_" + today.getHours() + "_" + today.getMinutes() + "_" + today.getSeconds() + ".tar";
                }else if (TurbineUtils.GetPassedParameter("download_type",data).equals("tar.gz"))
                {
                    tar= true;
                    COMPRESSION=ZipOutputStream.DEFLATED;
                    contentType = "application/tar.gz";
                    fileName=mr.getArchiveDirectoryName() + "_" + (today.getMonth() + 1) + "_" + today.getDate() + "_" + (today.getYear() + 1900) + "_" + today.getHours() + "_" + today.getMinutes() + "_" + today.getSeconds() + ".tar.gz";
                }else if (TurbineUtils.GetPassedParameter("download_type",data).equals("xcat"))
                {
                    cat= true;
                    contentType = "application/xcat";
                    fileName=mr.getArchiveDirectoryName() + "_" + (today.getMonth() + 1) + "_" + today.getDate() + "_" + (today.getYear() + 1900) + "_" + today.getHours() + "_" + today.getMinutes() + "_" + today.getSeconds() + ".xcat";
                }
            }
            
            HttpServletResponse response= data.getResponse();
    		response.setContentType(contentType);
			TurbineUtils.setContentDisposition(response, fileName, false);
            
            if(cat){
                final String server = TurbineUtils.GetFullServerPath();
                
                final String url = server + "/app/template/GetFile.vm/search_element/" + mr.getXSIType() + "/search_field/" + mr.getXSIType() + ".ID/search_value/" + mr.getId();
                
                mr.loadLocalFiles();

                XnatProjectdata project = mr.getPrimaryProject(false);
                
                CatCatalogBean catalog = new CatCatalogBean();
                catalog.setId(mr.getId());

                CatCatalogMetafieldBean catalogmeta = new CatCatalogMetafieldBean();
                catalogmeta.setName("SESSION_ID");
                catalogmeta.setMetafield(mr.getId());
                catalog.addMetafields_metafield(catalogmeta);
                
                catalogmeta = new CatCatalogMetafieldBean();
                catalogmeta.setName("SUBJECT_ID");
                catalogmeta.setMetafield(mr.getSubjectId());
                catalog.addMetafields_metafield(catalogmeta);
                
                catalogmeta = new CatCatalogMetafieldBean();
                catalogmeta.setName("PROJECT_ID");
                catalogmeta.setMetafield(mr.getProject());
                catalog.addMetafields_metafield(catalogmeta);
                
                Hashtable<String,Object> fileMap = new Hashtable<String,Object>();
                
                Hashtable fileGroups = mr.getFileGroups();
                
                String uri = server + "/archive/cache/";

                for (Enumeration e = fileGroups.keys(); e.hasMoreElements();) {
                    String key = (String)e.nextElement();
                    if (TurbineUtils.HasPassedParameter(key,data)){
                        ArrayList groupFiles = (ArrayList)fileGroups.get(key);
                        int counter=0;
                        for(Iterator iter=groupFiles.iterator();iter.hasNext();){
                            Object o = iter.next();
                            if (o instanceof String){

                                String id = (String)o;
                                
                                int index = mr.getFileTracker().getIDIndex(id);
                                String relativePath = mr.getFileTracker().getRelativePath(index);
                                File f = mr.getFileTracker().getFile(index);
                                String identifier = "/file/" + id;
                                CatEntryBean entry = new CatEntryBean();
                                
                                String relative = f.getAbsolutePath();
                                
                                Object file_id = cacheFileLink(url + identifier, relative, mr.getDBName(), user.getLogin());
                                
                                entry.setUri(uri + file_id);
                                
                                fileMap.put(identifier, f);
                                
                                String path = f.getAbsolutePath();
                                if (path.indexOf(File.separator + project.getId())!=-1){
                                    path = path.substring(path.indexOf(File.separator + project.getId()) + 1);
                                }else{
                                    if (path.indexOf(File.separator + mr.getArchiveDirectoryName())!=-1){
                                        path = path.substring(path.indexOf(File.separator + mr.getArchiveDirectoryName()) + 1);
                                    }
                                }
                                
                                entry.setCachepath(path);
                                entry.setName(f.getName());
                                
                                CatEntryMetafieldBean meta = new CatEntryMetafieldBean();
                                meta.setMetafield(path);
                                meta.setName("RELATIVE_PATH");
                                entry.addMetafields_metafield(meta);
                                
                                meta = new CatEntryMetafieldBean();
                                meta.setMetafield(key);
                                meta.setName("GROUP");
                                entry.addMetafields_metafield(meta);
                                
                                
                                meta = new CatEntryMetafieldBean();
                                meta.setMetafield(new Long(f.length()).toString());
                                meta.setName("SIZE");
                                entry.addMetafields_metafield(meta);

                                catalog.addEntries_entry(entry);
                            }else{
                                XNATDirectory dir = (XNATDirectory)o;
                                
                                
                                for (Map.Entry<String,GeneralFile> entryF: dir.getRelativeFiles().entrySet()) {
                                    
                                    String relative = entryF.getKey();
                                    
                                    if(relative.indexOf(mr.getArchiveDirectoryName())!=-1)
                                    {
                                        relative = relative.substring(relative.indexOf(mr.getArchiveDirectoryName()));
                                    }
                                        
                                    String identifier = "/file/" + counter++;
                                    CatEntryBean entry = new CatEntryBean();
                                    entry.setUri(url + identifier);
                                    
                                    fileMap.put(identifier, entryF.getValue());
                                    
                                    entry.setCachepath(relative);
                                    entry.setName(entryF.getValue().getName());
                                    
                                    CatEntryMetafieldBean meta = new CatEntryMetafieldBean();
                                    meta.setMetafield(relative);
                                    meta.setName("RELATIVE_PATH");
                                    entry.addMetafields_metafield(meta);
                                    
                                    meta = new CatEntryMetafieldBean();
                                    meta.setMetafield(key);
                                    meta.setName("GROUP");
                                    entry.addMetafields_metafield(meta);
                                    
                                    meta = new CatEntryMetafieldBean();
                                    meta.setMetafield(new Long(entryF.getValue().length()).toString());
                                    meta.setName("SIZE");
                                    entry.addMetafields_metafield(meta);

                                    catalog.addEntries_entry(entry);
                                }
                            }
                            
                        }
                    }
                }
                

                Enumeration enumer = data.getParameters().keys();
                while (enumer.hasMoreElements())
                {
                    String key = (String)enumer.nextElement();
                    String id = null;
                    if (key.startsWith("file_"))
                    {
                        id = key.substring(5);
                    }else if (key.startsWith("dir_file_"))
                    {
                        id = key.substring(9);
                    }
                    
                    if(id!=null){
                        int index = mr.getFileTracker().getIDIndex(id);
                        String relativePath = mr.getFileTracker().getRelativePath(index);
                        File f = mr.getFileTracker().getFile(index);
                        String identifier = "/file/" + id;
                        CatEntryBean entry = new CatEntryBean();
                        entry.setUri(url + identifier);
                        
                        fileMap.put(identifier, f);
                        
                        String path = f.getAbsolutePath();
                        if (path.indexOf(File.separator + project.getId())!=-1){
                            path = path.substring(path.indexOf(File.separator + project.getId()) + 1);
                        }else{
                            if (path.indexOf(File.separator + mr.getArchiveDirectoryName())!=-1){
                                path = path.substring(path.indexOf(File.separator + mr.getArchiveDirectoryName()) + 1);
                            }
                        }
                        
                        entry.setCachepath(path);
                        entry.setName(f.getName());
                        
                        CatEntryMetafieldBean meta = new CatEntryMetafieldBean();
                        meta.setMetafield(path);
                        meta.setName("RELATIVE_PATH");
                        entry.addMetafields_metafield(meta);
                        
                        meta = new CatEntryMetafieldBean();
                        meta.setMetafield(key);
                        meta.setName("GROUP");
                        entry.addMetafields_metafield(meta);
                        
                        
                        meta = new CatEntryMetafieldBean();
                        meta.setMetafield(new Long(f.length()).toString());
                        meta.setName("SIZE");
                        entry.addMetafields_metafield(meta);

                        catalog.addEntries_entry(entry);
                    }
                }
                
                final String identifier = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_element",data)) + ":"+ ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_field",data)) + ":"+ ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data));
                
                data.getSession().setAttribute(identifier, fileMap);
                
                ServletOutputStream out = response.getOutputStream();
                OutputStreamWriter sw = new OutputStreamWriter(out);
                catalog.toXML(sw, false);
            
                sw.flush();
                sw.close();
            }else{
                if (mr.hasSRBData()){
                    long startTime1 = Calendar.getInstance().getTimeInMillis();
                    mr.loadSRBFiles();

                    System.out.println("Finished loading SRB Files: " + (System.currentTimeMillis()-startTime1) + "ms");
                    startTime1 = Calendar.getInstance().getTimeInMillis();
                    
                    ArrayList<XNATDirectory> al = new ArrayList<XNATDirectory>();
                    Hashtable fileGroups = mr.getFileGroups();
                    for (Enumeration e = fileGroups.keys(); e.hasMoreElements();) {
                        String key = (String)e.nextElement();
                        if (TurbineUtils.HasPassedParameter(key,data)){
                            XNATDirectory files = (XNATDirectory)fileGroups.get(key);
                            al.add(files);
                        }
                    }

                    System.out.println("Finished file groups: " + (System.currentTimeMillis()-startTime1) + "ms");
                    startTime1 = Calendar.getInstance().getTimeInMillis();
                                               
                    ZipI zip = null;
                    OutputStream outStream = response.getOutputStream();
                    if (tar)
                    {
                        zip = new TarUtils();
                    }else{
                        zip = new ZipUtils();
                    }
                    
                    zip.setOutputStream(outStream,COMPRESSION);
                                    
                    for(XNATDirectory sub : al){
                        try {
                            zip.write(sub);
                            System.out.println("Loaded ("+ sub.getPath() +"): " + (System.currentTimeMillis()-startTime1) + "ms");
                            startTime1 = Calendar.getInstance().getTimeInMillis();
                        } catch (Throwable e) {
                            logger.error("",e);
                        }
                    }
                    
                                          
                    // Complete the ZIP file
                    zip.close();
                    
                }else{
                    ArrayList al = new ArrayList();
                    mr.loadLocalFiles();

                    CatCatalogBean catalog = new CatCatalogBean();
                    
                    Hashtable fileGroups = mr.getFileGroups();
                    for (Enumeration e = fileGroups.keys(); e.hasMoreElements();) {
                        String key = (String)e.nextElement();
                        if (TurbineUtils.HasPassedParameter(key,data)){
                            ArrayList files = (ArrayList)fileGroups.get(key);
                            for(Iterator iter=files.iterator();iter.hasNext();){
                                
                                al.add(iter.next());
                            }
                        }
                    }
                    
                    
                    Enumeration enumer = data.getParameters().keys();
                    while (enumer.hasMoreElements())
                    {
                        String key = (String)enumer.nextElement();
                        if (key.startsWith("file_"))
                        {
                            String imgName = key.substring(5);
                            al.add(imgName);
                        }else if (key.startsWith("dir_file_"))
                        {
                            String imgName = key.substring(9);
                            al.add(imgName);
                        }
                    }
                    
                    Hashtable mappings = mr.getFileTracker().createPartialHashByIDs(al,mr.getArchiveDirectoryName());
                    ZipI zip = null;
                    OutputStream outStream = response.getOutputStream();
                    if (tar)
                    {
                        zip = new TarUtils();
                    }else{
                        zip = new ZipUtils();
                    }
                    
                    zip.setOutputStream(outStream,COMPRESSION);
                    
                    if (contentType=="application/xar"){
                        File f = Users.getUserCacheFile(TurbineUtils.getUser(data),"xar_sessions/" + mr.getId() + ".xml");
                        f.getParentFile().mkdirs();
                        FileOutputStream fos = new FileOutputStream(f);
                        SAXWriter writer = new SAXWriter(fos,true);
                        writer.setAllowSchemaLocation(true);
                        writer.setLocation(TurbineUtils.GetRelativeServerPath(data) + "/" + "schemas/");
                        
                        writer.setRelativizePath(mr.getArchiveDirectoryName()+"/");
                        
                        writer.write(mr.getItem());
                        
                        zip.write(mr.getId() + ".xml", f);
                        
                        FileUtils.DeleteFile(f);
                    }
                    
                    enumer = mappings.keys();
                    while(enumer.hasMoreElements())
                    {
                          String key = (String)enumer.nextElement();
                          File f = new File((String)mappings.get(key));
                          if (!f.isDirectory())
                          {
                              zip.write(key,f);
                          }
                    }
                          
                    // Complete the ZIP file
                    zip.close();
                }
            }
            
        } catch (Exception e) {
        	logger.error("",e);
            data.setMessage(e.getMessage());
        } 
        System.out.println("END DownloadImages.java " + (System.currentTimeMillis()-startTime) + "ms");
		    
    }

    public static boolean ARCHIVE_PATH_CHECKED = false;

    public static Boolean CreatedArchivePathCache(String dbName, String login) throws Exception {
        if (!ARCHIVE_PATH_CHECKED) {
            String query = "SELECT relname FROM pg_catalog.pg_class WHERE  relname=LOWER('xs_archive_path_cache');";
            String exists = (String) PoolDBUtils.ReturnStatisticQuery(query, "relname", dbName, login);

            if (exists != null) {
                ARCHIVE_PATH_CHECKED = true;
            } else {
                query = "CREATE TABLE xs_archive_path_cache" +
                        "\n(" +
                        "\n  id serial," +
                        "\n  create_date timestamp DEFAULT now()," +
                        "\n  username VARCHAR(255)," +
                        "\n  url text," +
                        "\n  _token VARCHAR(255)," +
                        "\n  absolute_path text" +
                        "\n) " +
                        "\nWITH OIDS;";

                PoolDBUtils.ExecuteNonSelectQuery(query, dbName, login);

                ARCHIVE_PATH_CHECKED = true;
            }
        }
        return true;
    }

    public static Object cacheFileLink(String url, String absolutePath, String dbName, String login) throws Exception {
        CreatedArchivePathCache(dbName, login);
        Object o = RandomStringUtils.randomAlphanumeric(64);
        Object exists = PoolDBUtils.ReturnStatisticQuery("SELECT id FROM xs_archive_path_cache WHERE _token='" + o + "';", "id", dbName, login);
        while (exists != null) {
            o = RandomStringUtils.randomAlphanumeric(64);
            exists = PoolDBUtils.ReturnStatisticQuery("SELECT id FROM xs_archive_path_cache WHERE _token='" + o + "';", "id", dbName, login);
        }
        String query = "INSERT INTO xs_archive_path_cache (username,url,_token,absolute_path) VALUES ('" + login + "'," + DBAction.ValueParser(url, "string", true) + ",'" + o + "'," + DBAction.ValueParser(absolutePath, "string", true) + ");";
        PoolDBUtils.ExecuteNonSelectQuery(query, dbName, login);
        return o;
    }


}
