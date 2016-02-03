/*
 * org.nrg.xdat.om.base.BaseXnatResourcecatalog
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 10/14/13 5:42 PM
 */
package org.nrg.xdat.om.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xdat.model.CatCatalogI;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.om.base.auto.AutoXnatResourcecatalog;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.StringUtils;
import org.nrg.xnat.utils.CatalogUtils;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatResourcecatalog extends AutoXnatResourcecatalog {

	public BaseXnatResourcecatalog(ItemI item)
	{
		super(item);
	}

	public BaseXnatResourcecatalog(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatResourcecatalog(UserI user)
	 **/
	public BaseXnatResourcecatalog()
	{}

	public BaseXnatResourcecatalog(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    /**
     * Returns ArrayList of java.io.File objects
     * @return
     */
    public ArrayList getCorrespondingFiles(String rootPath)
    {
        if (files==null)
        {
        	files = new ArrayList<File>();
        	
        	final File catFile = this.getCatalogFile(rootPath);
			final String parentPath=catFile.getParent();
			final CatCatalogBean cat=CatalogUtils.getCatalog(rootPath, this);

            if (cat!=null){
                for(CatEntryI entry: cat.getEntries_entry()){
                    String entryPath = StringUtils.ReplaceStr(FileUtils.AppendRootPath(parentPath,entry.getUri()),"\\","/");
                    File temp=getFileOnLocalFileSystem(entryPath);
                    if(temp!=null)
                    	files.add(temp);
                }
            }

        }
        return files;
    }

    public void clearFiles() {
        files = null;
    }
	

    /**
     * Returns ArrayList of java.lang.String objects
     * @return
     */
    public ArrayList getCorrespondingFileNames(String rootPath)
    {
        if (fileNames==null)
        {
            fileNames = new ArrayList();
            ArrayList files = getCorrespondingFiles(rootPath);
            for (int i=0;i<files.size();i++){
                File f = (File)files.get(i);
                fileNames.add(f.getName());
            }
        }
        return fileNames;
    }

    /**
     * Appends this path to the enclosed URI or path variables.
     * @param root
     */
    public void prependPathsWith(String root){
        if (!FileUtils.IsAbsolutePath(this.getUri())){
            try {
                    this.setUri(root + this.getUri());
            } catch (Exception e) {
                logger.error("",e);
            }
        }
    }

    public File getCatalogFile(String rootPath){
        return CatalogUtils.getCatalogFile(rootPath, this);
    }

    public CatCatalogBean getCatalog(String rootPath){
        return CatalogUtils.getCatalog(rootPath, this);
    }
    
    public static void backupEntry(String parentPath,CatCatalogBean cat,UserI user, EventMetaI c, String timestamp) throws FileNotFoundException, IOException{
    	if (cat!=null){
            for(CatEntryI entry: cat.getEntries_entry()){
				final File f = new File(parentPath,entry.getUri());
            	final File newFile=FileUtils.CopyToHistory(f,timestamp);
				entry.setUri(newFile.getAbsolutePath());
				((CatEntryBean)entry).setModifiedtime(EventUtils.getEventDate(c, false));
				if(user!=null){
					entry.setModifiedby(user.getUsername());
				}
				
				if(c!=null && c.getEventId()!=null){
					entry.setModifiedeventid(Integer.valueOf(c.getEventId().intValue()));
				}
            }
        }
    }
    
    public void backupToHistory(String rootPath,UserI user, EventMetaI c) throws Exception{
    	final File f = this.getCatalogFile(rootPath);	
    	final String parentPath=f.getParentFile().getAbsolutePath();
    	final CatCatalogBean cat=CatalogUtils.getCatalog(rootPath,this);
    	
    	if(cat!=null){
    		String timestamp=EventUtils.getTimestamp(c);
    		backupEntry(parentPath, cat, user, c,timestamp);
			CatalogUtils.writeCatalogToFile(cat, FileUtils.BuildHistoryFile(f,timestamp));
    	}
    }
    


    public void deleteWithBackup(String rootPath, UserI user, EventMetaI c) throws Exception{
    	if(CatalogUtils.maintainFileHistory()){
    		backupToHistory(rootPath, user, c);
    	}
			
    	deleteFromFileSystem(rootPath);
    }

    public void deleteFromFileSystem(String rootPath){
    	super.deleteFromFileSystem(rootPath);
    	
    	final File f = this.getCatalogFile(rootPath);	

    	if (f.exists()){
    		try {
    			FileUtils.MoveToCache(f);
    			if(FileUtils.CountFiles(f.getParentFile(),true)==0){
    				FileUtils.DeleteFile(f.getParentFile());
    			}
    		} catch (FileNotFoundException e) {
    			logger.error("",e);
    		} catch (IOException e) {
    			logger.error("",e);
    		}
    	}
    }
    
    public int entryCount =0;
    public boolean formalizeCatalog(CatCatalogI cat, String catPath,UserI user, EventMetaI now){
    	return CatalogUtils.formalizeCatalog(cat,catPath,user,now);
    }
    
    public CatCatalogBean getCleanCatalog(String rootPath,boolean includeFullPaths,UserI user, EventMetaI c){
    	return CatalogUtils.getCleanCatalog(rootPath, this, includeFullPaths,user,c);
    }
    
    public void clearCountAndSize() {
    	count = null;
    	size = null;
    }

    Integer count = null;
    public Integer getCount(String rootPath){
        if (count ==null){
            long sizeI = 0;
            int countI = 0;
            Iterator files = this.getCorrespondingFiles(rootPath).iterator();
            while (files.hasNext()){
                File f = (File)files.next();
               
                if (f!=null && f.exists() && !f.getName().endsWith("catalog.xml")){
                    countI++;
                    sizeI+=f.length();
                }
            }

            size = new Long(sizeI);
            count = new Integer(countI);
        }
        return count;
    }

    Long size = null;
    public long getSize(String rootPath){
        if (size ==null){
            long sizeI = 0;
            int countI = 0;
            Iterator files = this.getCorrespondingFiles(rootPath).iterator();
            while (files.hasNext()){
                File f = (File)files.next();
                if (!f.getName().endsWith("catalog.xml")){
                    countI++;
                    sizeI+=f.length();
                }
            }

            size = new Long(sizeI);
            count = new Integer(countI);
        }
        return size.longValue();
    }
    
    public void moveTo(File newSessionDir,String existingSessionDir,String rootPath,UserI user,EventMetaI ci) throws IOException,Exception{
    	String uri = this.getUri();
    	
    	String relativePath=null;
    	if(existingSessionDir!=null && uri.startsWith(existingSessionDir)){
    		relativePath=uri.substring(existingSessionDir.length());
    	}else{
    		if(FileUtils.IsAbsolutePath(uri)){
    			if(uri.indexOf("/")>0){
    				relativePath=uri.substring(uri.indexOf("/")+1);
    			}else if(uri.indexOf("\\")>0){
    				relativePath=uri.substring(uri.indexOf("\\")+1);
    			}else{
    				relativePath=uri;
    			} 
    		}else{
    			relativePath=uri;
    		}
    	}
    	
    	File newFile = new File(newSessionDir,relativePath);
    	File parentDir=newFile.getParentFile();
    	if(!parentDir.exists())
    	{
    		parentDir.mkdirs();
    	}

    	File catalog =this.getCatalogFile(rootPath);
    	
    	InputStream fis = new FileInputStream(catalog);
        if (catalog.getName().endsWith(".gz"))
        {
            fis = new GZIPInputStream(fis);
        }
        XDATXMLReader reader = new XDATXMLReader();
        BaseElement base = reader.parse(fis);

        if (base instanceof CatCatalogBean){
        	moveCatalogEntries((CatCatalogBean)base,catalog.getParent(),newFile.getParent());
        }
        
        try {
			FileWriter fw = new FileWriter(catalog);
			base.toXML(fw, true);
			fw.close();
		} catch (IOException e) {
			logger.error("",e);
		}
		
    	FileUtils.MoveFile(catalog, newFile, true, true);
    	
    	this.setUri(newFile.getAbsolutePath());
    	SaveItemHelper.authorizedSave(this,user, true, false,ci);
    }
    
    public void moveCatalogEntries(CatCatalogI cat,String existingRootPath,String newRootPath) throws IOException{
    	for(CatEntryI entry: cat.getEntries_entry()){
    		File existingLocation=null;
    		String relativePath=null;
    		
    		String uri= entry.getUri();
    		if(FileUtils.IsAbsolutePath(entry.getUri())){
    			existingLocation=new File(entry.getUri());
    			if(entry.getUri().startsWith(existingRootPath)){
    	    		relativePath=uri.substring(existingRootPath.length());
    			}else{
    				if(FileUtils.IsAbsolutePath(entry.getUri())){
    	    			if(entry.getUri().indexOf("/")>0){
    	    				relativePath=uri.substring(entry.getUri().indexOf("/")+1);
    	    			}else if(uri.indexOf("\\")>0){
    	    				relativePath=uri.substring(entry.getUri().indexOf("\\")+1);
    	    			}else{
    	    				relativePath=uri;
    	    			} 
    	    		}else{
    	    			relativePath=uri;
    	    		}
    			}
    			((CatEntryBean)entry).setUri(relativePath);
    		}else{
    			existingLocation=new File(existingRootPath,uri);
    			relativePath=uri;
    		}
    		
    		
    		
    		File newFile = new File(newRootPath,relativePath);
        	File parentDir=newFile.getParentFile();
        	if(!parentDir.exists())
        	{
        		parentDir.mkdirs();
        	}
        	
        	if(!existingLocation.exists())
        		existingLocation=getFileOnLocalFileSystem(existingLocation.getAbsolutePath());
        	
        	if(existingLocation!=null && existingLocation.exists()){
            	FileUtils.MoveFile(existingLocation, newFile, true, true);
        	}
    	}
    	
    	for(CatCatalogI subset: cat.getSets_entryset()){
    		moveCatalogEntries(subset, existingRootPath, newRootPath);
    	}
    }
}
