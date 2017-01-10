/*
 * web: org.nrg.xnat.turbine.modules.screens.ArcGet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatReconstructedimagedataI;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatReconstructedimagedata;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.srb.XNATDirectory;

/**
 * @author timo
 *
 */
public class ArcGet extends
org.apache.turbine.modules.screens.RawScreen
{
    static org.apache.log4j.Logger logger = Logger.getLogger(ArcGet.class);
    
   /**
	* Set the content type to Xml. (see RawScreen)
	*
	* @param data Turbine information.
	* @return content type.
	*/
	public String getContentType(RunData data)
	{
		return "application/zip";
	};

	/**
	* Overrides & finalizes doOutput in RawScreen to serve the output stream
created in buildPDF.
	*
	* @param data RunData
	* @exception Exception, any old generic exception.
	*/
    @SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
	protected final void doOutput(RunData data) 
	{
            String username = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("username",data));
            String password = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("password",data));
            String raw = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("raw",data));
            String processed = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("proc",data));
            String quality = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("quality",data));
            String unzip = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("unzip",data));
            if (unzip==null){
                unzip="false";
            }
            try {
                UserI user = TurbineUtils.getUser(data);
                if(user==null){
                    if (username != null && password !=null)
                    {
                        user = Authenticator.Authenticate(new Authenticator.Credentials(username,password));
                        data.getSession().invalidate();
                    }
                }
                if (user != null)
                {
                	
                		SecureAction.isCsrfTokenOk(data.getRequest(),false);
                	
                        String id = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("id",data));

                        HttpServletResponse response = data.getResponse();
                        response.setContentType(getContentType(data));
                        
                        java.util.Date today = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
                        String fileName=id + "_" + (today.getMonth() + 1) + "_" + today.getDate() + "_" + (today.getYear() + 1900) + "_" + today.getHours() + "_" + today.getMinutes() + "_" + today.getSeconds() + ".zip";
                		TurbineUtils.setContentDisposition(response, fileName, false);
                		
                        ZipI zip = new ZipUtils();
                        if (unzip.equalsIgnoreCase("true"))
                            zip.setDecompressFilesBeforeZipping(true);
                        int compressionMethod = ZipOutputStream.STORED;
                        zip.setOutputStream(response.getOutputStream(),compressionMethod);

                        File temp = File.createTempFile("arc-get.log.","");
                        FileWriter fw = new FileWriter(temp);
                        
                        ArrayList<XnatImagesessiondata> al = XnatImagesessiondata.getXnatImagesessiondatasByField("xnat:imageSessionData.ID",id,user,true);
                        if (al.size()== 0)
                        {
                        	al = XnatImagesessiondata.getXnatImagesessiondatasByField("xnat:imageSessionData/label",id,user,true);
                        	
                        	if (al.size()== 0)
                            {
                                fw.write("INVALID SESSION ID '" + id +"'.");

                                fw.flush();
                                fw.close();
                                
                                zip.write("README.txt",temp);
                                
                                zip.close();
                                temp.delete();
                                return;
                            }
                        }
                        
                        XnatImagesessiondata mr = (XnatImagesessiondata)al.get(0);
                       
                		fw.write(mr.getId() +" Archive Download Summary \n");
                		int successful = 0;
                		int failed = 0;
                		try {
                		    boolean hasOptions= true;
                		    if (raw == null && processed==null && (quality==null || quality.equals("ALL"))){
                		        hasOptions = false;
                		    }
                                if (mr.hasSRBData()){
                                    mr.loadSRBFiles();
                                    
                                    if (!hasOptions){
                                        XNATDirectory srbDIR = mr.getSRBDirectory();

                                        zip.write(srbDIR);
                                    }else{
                                        if (raw == null){
                                            //files = mr.getFileTracker().createHash(mr.getId());
                                        }else{
                                            if (raw.trim().equalsIgnoreCase("ALL")){
                                                Hashtable fileGroups = mr.getFileGroups();
                                                for (Enumeration e = fileGroups.keys(); e.hasMoreElements();) {
                                                    String key = (String)e.nextElement();
                                                    if (key.toLowerCase().contains("scan")){
                                                        XNATDirectory filesA = (XNATDirectory)fileGroups.get(key);
                                                        zip.write(filesA);
                                                    }
                                                }
                                            }else{
                                                Hashtable fileGroups = mr.getFileGroups();
                                                raw = raw.trim();
                                                ArrayList rawTypes = XftStringUtils.CommaDelimitedStringToArrayList(raw);
                                                Iterator iter= rawTypes.iterator();
                                                while (iter.hasNext())
                                                {
                                                    String rType = (String)iter.next();
                                                    ArrayList<XnatImagescandata> scans= mr.getScansByType(rType);
                                                    if (scans!=null && scans.size()>0)
                                                    {
                                                        for(XnatImagescandata scan: scans){
                                                            String parsedScanID= StringUtils.replace(StringUtils.replace(scan.getId(), "-", ""), "*", "AST");
                                                            XNATDirectory filesA = (XNATDirectory)fileGroups.get("scan" +parsedScanID);
                                                            zip.write(filesA);

                                                            fw.write("Including " +filesA.getSize() + " Raw Files for " + scan.getId() +" (" + rType + ").\n");
                                                        }
                                                    }else{
                                                        fw.write("No " +rType + " Raw Scans Found.\n");
                                                    }
                                                }
                                            }
                                        }
                                        
                                        if (null == processed){
                                            //files = mr.getFileTracker().createHash(mr.getId());
                                        } else {
                                            if (processed.trim().equalsIgnoreCase("ALL")){
                                        	Hashtable fileGroups = mr.getFileGroups();
                                        	for (final Map.Entry e : (Collection<Map.Entry>)fileGroups.entrySet()) {
                                        	    final String key = (String)e.getKey();
                                        	    if (key.toLowerCase().indexOf("recon")!=-1){
                                        		XNATDirectory filesA = (XNATDirectory)e.getValue();
                                        		zip.write(filesA);
                                        	    }
                                        	}
                                            } else {
                                        	Hashtable fileGroups = mr.getFileGroups();
                                        	processed = processed.trim();
                                        	for (final String rType : XftStringUtils.CommaDelimitedStringToArrayList(processed)) {
                                        	    Collection<XnatReconstructedimagedata> scans= mr.getReconstructionsByType(rType);
                                        	    if (scans.isEmpty()) {
                                        		fw.write("No " +rType + " Processed Images Found.\n");
                                        	    } else {
                                        		for (final XnatReconstructedimagedata scan : scans) {
                                        		    final String parsedScanID= StringUtils.replace(StringUtils.replace(scan.getId(),"-",""),"*","AST");
                                        		    final XNATDirectory filesA = (XNATDirectory)fileGroups.get("recon" +parsedScanID);
                                        		    zip.write(filesA);
                                        		    fw.write("Including " +filesA.getSize() + " Processed Files for " + scan.getId() +" (" + rType + ").\n");                                                	    
                                        		}
                                        	    }
                                        	}
                                            }
                                        }
                                    }

//                                    File tempFile = File.createTempFile("srbTransfer", "");
//                                    tempFile.delete();
//                                    File tempSession = new File(tempFile.getAbsolutePath() + File.separator + mr.getId());
//                                    tempSession.mkdirs();
                                    
//                                    Iterator enumer = files.iterator();
//                                    while(enumer.hasNext())
//                                    {
//                                        XNATDirectory key = (XNATDirectory)enumer.next();
//                                        try {
//                                            zip.write(key);
//                                            successful++;
//                                        } catch (Throwable e1) {
//                                            logger.error("",e1);
//                                        }
//                                    }
//                                    

                                    
                                    
                                    fw.write("\nFiles loaded successfully.\n");
                                    if (failed>0)
                                    {
                                        fw.write(failed +" file(s) failed.\n");
                                    }
                                    fw.flush();
                                    fw.close();
                                    
                                    zip.write("README.txt",temp);
                                    
                                    // Complete the ZIP file
                                    zip.close();
//                                    FileUtils.DeleteFile(tempSession);
                                }else{
                                    String archive = mr.getArchivePath();
                                    File archiveF = new File(archive);
                                    if (archiveF.exists())
                                    {
                                        mr.loadLocalFiles();
                                        Hashtable files = new Hashtable();
                                        if (!hasOptions){
                                            files = mr.getFileTracker().createHash(mr.getArchiveDirectoryName());
                                        }else{
                                            ArrayList images = new ArrayList();
                                            if (raw == null && (quality !=null && !quality.equalsIgnoreCase("ALL"))){
                                                raw="ALL";
                                            }
                                            
                                            if (raw == null){
                                                //files = mr.getFileTracker().createHash(mr.getId());
                                            }else{
                                                if (raw.trim().equalsIgnoreCase("ALL")){
                                                    Hashtable fileGroups = mr.getFileGroups();
                                                    Collection<XnatImagescandataI> scans= mr.getSortedScans();
                                                    if (scans!=null && scans.size()>0)
                                                    {
                                                        for(XnatImagescandataI scan: scans){
                                                            boolean include = true;
                                                            if (quality != null){
                                                                include = false;
                                                                quality = quality.trim();
                                                                if (quality.indexOf("ALL")!=-1)
                                                                {
                                                                    include=true;
                                                                }else{
                                                                    ArrayList qualities = XftStringUtils.CommaDelimitedStringToArrayList(quality);
                                                                    if (scan.getQuality()!=null){
                                                                        if (qualities.contains(scan.getQuality()))
                                                                        {
                                                                            include=true;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            
                                                            if (include){
                                                                String parsedScanID= StringUtils.replace(StringUtils.replace(scan.getId(),"-",""),"*","AST");
                                                                ArrayList filesA = (ArrayList)fileGroups.get("scan" +parsedScanID);
                                                                images.addAll(filesA);
                                                                fw.write("Including " +filesA.size() + " Raw Files for scan " + scan.getId() +" (" + scan.getQuality() + ").\n");
                                                            }
                                                        }
                                                    }
                                                }else{
                                                    Hashtable fileGroups = mr.getFileGroups();
                                                    raw = raw.trim();
                                                    for (final String rType : XftStringUtils.CommaDelimitedStringToArrayList(raw)) {
                                                        ArrayList<XnatImagescandata> scans= mr.getScansByType(rType);
                                                        if (scans!=null && scans.size()>0)
                                                        {
                                                            for(XnatImagescandata scan: scans){
                                                                
                                                                boolean include = true;
                                                                if (quality != null){
                                                                    include = false;
                                                                    quality = quality.trim();
                                                                    if (quality.indexOf("ALL")!=-1)
                                                                    {
                                                                        include=true;
                                                                    }else{
                                                                        ArrayList qualities = XftStringUtils.CommaDelimitedStringToArrayList(quality);
                                                                        if (scan.getQuality()!=null){
                                                                            if (qualities.contains(scan.getQuality()))
                                                                            {
                                                                                include=true;
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                
                                                                if (include){
                                                                    String parsedScanID= StringUtils.replace(StringUtils.replace(scan.getId(),"-",""),"*","AST");
                                                                    ArrayList filesA = (ArrayList)fileGroups.get("scan" +parsedScanID);
                                                                    images.addAll(filesA);
                                                                    fw.write("Including " +filesA.size() + " Raw Files for " + scan.getId() +" (" + rType + ":" + scan.getQuality() + ").\n");
                                                                }
                                                            }
                                                        }else{
                                                            XnatImagescandata scan =mr.getScanById(rType);
                                                            if (scan==null)
                                                                fw.write("No " +rType + " Raw Scans Found.\n");
                                                            else{
                                                                String parsedScanID= StringUtils.replace(StringUtils.replace(scan.getId(),"-",""),"*","AST");
                                                                ArrayList filesA = (ArrayList)fileGroups.get("scan" +parsedScanID);
                                                                images.addAll(filesA);
                                                                fw.write("Including " +filesA.size() + " Raw Files for " + scan.getId() +" (" + rType + ":" + scan.getQuality() + ").\n");
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            if (processed == null){
                                                //files = mr.getFileTracker().createHash(mr.getId());
                                            }else{
                                                if (processed.trim().equalsIgnoreCase("ALL")){
                                                    Hashtable fileGroups = mr.getFileGroups();
                                                    for (Map.Entry e : (Collection<Map.Entry>)fileGroups.entrySet()) {
                                                        String key = (String)e.getKey();
                                                        if (key.toLowerCase().indexOf("recon")!=-1) {
                                                            images.addAll((Collection)e.getValue());
                                                        }
                                                    }
                                                }else{
                                                    Hashtable fileGroups = mr.getFileGroups();
                                                    processed = processed.trim();
                                                    for (final String rType : XftStringUtils.CommaDelimitedStringToArrayList(processed)) {
                                                        Collection<XnatReconstructedimagedata> scans= mr.getReconstructionsByType(rType);
                                                        if (scans.isEmpty())
                                                        {
                                                            for (final XnatReconstructedimagedata scan : scans) {
                                                                String parsedScanID= StringUtils.replace(StringUtils.replace(scan.getId(),"-",""),"*","AST");
                                                                Collection filesA = (Collection)fileGroups.get("recon" +parsedScanID);
                                                                images.addAll(filesA);    
                                                                fw.write("Including " +filesA.size() + " Processed Files for " + scan.getId() +" (" + rType + ").\n");
                                                            }
                                                        }else{
                                                            XnatReconstructedimagedataI scan =mr.getReconstructionByID(rType);
                                                            if (scan==null)
                                                                fw.write("No " +rType + " Processed Images Found.\n");
                                                            else{
                                                                String parsedScanID= StringUtils.replace(StringUtils.replace(scan.getId(),"-",""),"*","AST");
                                                                ArrayList filesA = (ArrayList)fileGroups.get("recon" +parsedScanID);
                                                                images.addAll(filesA);
                                                                fw.write("Including " +filesA.size() + " Processed Files for " + scan.getId() +" (" + rType + ").\n");
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            files = mr.getFileTracker().createPartialHashByIDs(images,mr.getArchiveDirectoryName());
                                        }
                                        
                                        
                                        //ZipUtils.ZipToStream(response.getOutputStream(),files);
                                        Enumeration enumer = files.keys();
                                        while(enumer.hasMoreElements())
                                        {
                                              String key = (String)enumer.nextElement();
                                              try {
                                                File f = new File((String)files.get(key));
                                                  if (!f.isDirectory())
                                                  {
                                                      zip.write(key,f);
                                                  }
                                                  successful++;
                                            } catch (FileNotFoundException e) {
                                                failed++;
                                                fw.write("cannot access " +key + ": " + e.getMessage() +"\n");
                                            } catch (IOException e) {
                                                failed++;
                                                fw.write("cannot access " +key + ": " + e.getMessage() +"\n");
                                            } catch (RuntimeException e) {
                                                failed++;
                                                fw.write("cannot access " +key + ": " + e.getMessage() +"\n");
                                            }
                                        }
                                        
                                        fw.write("\n" + successful +" file(s) loaded successfully.\n");
                                        if (failed>0)
                                        {
                                            fw.write(failed +" file(s) failed.\n");
                                        }
                                        fw.flush();
                                        fw.close();
                                        
                                        zip.write("README.txt",temp);
                                        
                                        // Complete the ZIP file
                                        zip.close();
                                    }
                                }
                        } catch (IOException e) {
                            logger.error("",e);
                        }
                		FileUtils.DeleteFile(temp);
                }
            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            } catch (DBPoolException e) {
                logger.error("",e);
            } catch (SQLException e) {
                logger.error("",e);
            } catch (FieldNotFoundException e) {
                logger.error("",e);
            } catch (FailedLoginException e) {
                logger.error("",e);
            } catch (Exception e) {
                logger.error("",e);
            }
	}
}
