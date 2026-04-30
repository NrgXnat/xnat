/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatResourcecatalog
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.action.ServerException;
import org.nrg.framework.generics.GenericUtils;
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
import org.nrg.xnat.utils.CatalogUtils;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatResourcecatalog extends AutoXnatResourcecatalog {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatResourcecatalog(ItemI item)
	{
		super(item);
	}

	public BaseXnatResourcecatalog(UserI user)
	{
		super(user);
	}

	private Map<File, CatEntryI> filesMap = null;

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
     * @return A list for files for this catalog.
     */
    public ArrayList getCorrespondingFiles(final String rootPath)
    {
        if (files==null)
        {
			getCorrespondingFilesWithCatEntries(rootPath);
        	files = new ArrayList<>(filesMap.keySet());
        }
        return files;
    }

    @Override
	public Map<File, CatEntryI> getCorrespondingFilesWithCatEntries(String rootPath) {
    	if (filesMap == null) {
			filesMap = new HashMap<>();
			final CatalogUtils.CatalogData catalogData;
			try {
				catalogData = CatalogUtils.CatalogData.getOrCreate(rootPath, this, null);
				for (CatEntryI entry : catalogData.catBean.getEntries_entry()) {
					File temp = CatalogUtils.getFile(entry, catalogData.catPath, catalogData.project);
					if (temp != null) {
						filesMap.put(temp, entry);
					}
				}
			} catch (ServerException e) {
				logger.error("Unable to read catalog", e);
			}
		}
    	return filesMap;
	}

    public void clearFiles() {
        files = null;
    }
	

    /**
     * Returns ArrayList of java.lang.String objects
     * @return A list for file names for this catalog.
     */
    public ArrayList getCorrespondingFileNames(String rootPath) {
		if (fileNames == null) {
			fileNames = GenericUtils.convertToTypedList(getCorrespondingFiles(rootPath), File.class).stream().map(File::getName).collect(Collectors.toCollection(ArrayList::new));
		}
		return fileNames;
    }

    /**
     * Appends this path to the enclosed URI or path variables.
     * @param root The root path to add.
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

	@Deprecated
	public File getCatalogFile(String rootPath){
		return CatalogUtils.getCatalogFile(rootPath, this);
	}

	@Deprecated
	public CatCatalogBean getCatalog(String rootPath){
		return CatalogUtils.getCatalog(rootPath, this, null);
	}

	@Deprecated
	public CatCatalogBean getCatalog(String rootPath, String project){
		return CatalogUtils.getCatalog(rootPath, this, project);
	}

    public static void backupEntry(CatalogUtils.CatalogData catalogData, UserI user, EventMetaI c, String timestamp) throws IOException {
		for(CatEntryI entry: catalogData.catBean.getEntries_entry()){
			final File f = CatalogUtils.getFile(entry, catalogData.catPath, catalogData.project);
			if (f == null) {
				continue;
			}
			final File newFile=FileUtils.CopyToHistory(f,timestamp);
			entry.setUri(newFile.getAbsolutePath());
			((CatEntryBean)entry).setModifiedtime(EventUtils.getEventDate(c, false));
			if(user!=null){
				entry.setModifiedby(user.getUsername());
			}

			if(c!=null && c.getEventId()!=null){
				entry.setModifiedeventid(c.getEventId().intValue());
			}
		}
    }
    
    public void backupToHistory(String rootPath, String project, UserI user, EventMetaI c) throws Exception{
    	String timestamp = EventUtils.getTimestamp(c);
		CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate(rootPath, this, project);
		CatalogUtils.CatalogData historyCatalogData = new CatalogUtils.CatalogData(
				FileUtils.BuildHistoryFile(catalogData.catFile, timestamp),
				catalogData.project);
		backupEntry(catalogData, user, c, timestamp);
		CatalogUtils.writeCatalogToFile(historyCatalogData);
    }

	/**
	 * Deletes the folder for this resource catalog, saving a backup copy in the cache.
	 *
	 * @param rootPath  The root path of the archive file system.
	 * @param project   The project that contains the abstract resource.
	 * @param user      The user requesting the delete operation.
	 * @param c         The event data
	 * @param timestamp The timestamp for the delete operation. This allows multiple delete operations to be grouped in the same DELETED folder when backups are on.
	 *
	 * @throws Exception When an error occurs.
	 */
	public void deleteWithBackup(final String rootPath, final String project, final UserI user, final EventMetaI c, final String timestamp) throws Exception {
    	if(CatalogUtils.maintainFileHistory()){
    		backupToHistory(rootPath, project, user, c);
    	}
    	super.deleteWithBackup(rootPath, project, user, c, timestamp);
    }

    public void deleteFromFileSystem(String rootPath, String project) {
		super.deleteFromFileSystem(rootPath, project);
	}

	public void deleteFromFileSystem(final String rootPath, final String project, final String timestamp) {
    	super.deleteFromFileSystem(rootPath, project, timestamp);
    	
    	final File f = this.getCatalogFile(rootPath);	

    	if (f.exists()){
    		try {
    			FileUtils.MoveToCache(f, timestamp);
    			if(FileUtils.CountFiles(f.getParentFile(),true)==0){
    				FileUtils.DeleteFile(f.getParentFile());
    			}
    		} catch (IOException e) {
    			logger.error("",e);
    		}
    	}
    }
    
    public int entryCount =0;
    @Deprecated
    public boolean formalizeCatalog(CatCatalogI cat, String catPath, String project, UserI user, EventMetaI now){
    	return CatalogUtils.formalizeCatalog(cat, catPath, project, user, now);
    }

    @Deprecated
    public CatCatalogBean getCleanCatalog(String rootPath, String project, boolean includeFullPaths,UserI user, EventMetaI c){
    	return CatalogUtils.getCleanCatalog(project, rootPath, this, includeFullPaths,user,c);
    }
    
    @SuppressWarnings("unused")
	public void clearCountAndSize() {
    	count = null;
    	size = null;
    }

    Integer count = null;
    public Integer getCount(String rootPath){
        if (count ==null){
            long sizeI = 0;
            int countI = 0;
			for (final Object o : this.getCorrespondingFiles(rootPath)) {
				File f = (File) o;

				if (f != null && f.exists() && !f.getName().endsWith("catalog.xml")) {
					countI++;
					sizeI += f.length();
				}
			}

            size = sizeI;
            count = countI;
        }
        return count;
    }

    Long size = null;
    public long getSize(String rootPath){
        if (size ==null){
            long sizeI = 0;
            int countI = 0;
			for (final File f : GenericUtils.convertToTypedList(getCorrespondingFiles(rootPath), File.class)) {
				if (!f.getName().endsWith("catalog.xml")) {
					countI++;
					sizeI += f.length();
				}
			}

            size = sizeI;
            count = countI;
        }
        return size;
    }
    
    @SuppressWarnings("DuplicateThrows")
	public void moveTo(File newSessionDir, String existingSessionDir, String rootPath, @Nullable String currentProject,
					   @Nullable String destinationProject, UserI user, EventMetaI ci) throws IOException, SAXException, Exception {
    	final String uri = this.getUri();
    	final String relativePath;
		if (existingSessionDir != null && uri.startsWith(existingSessionDir)) {
			relativePath = uri.substring(existingSessionDir.length());
		} else if (FileUtils.IsAbsolutePath(uri)) {
			if (uri.contains("/")) {
				relativePath = uri.substring(uri.indexOf("/") + 1);
			} else if (uri.contains("\\")) {
				relativePath = uri.substring(uri.indexOf("\\") + 1);
			} else {
				relativePath = uri;
			}
		} else {
			relativePath = uri;
		}

    	File newFile = new File(newSessionDir,relativePath);
    	File parentDir=newFile.getParentFile();
		if (!parentDir.exists()) {
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

        if (base instanceof CatCatalogBean bean){
        	moveCatalogEntries(bean, catalog.getParent(), newFile.getParent(), currentProject,
					destinationProject);
        }
        
        try (FileWriter fw = new FileWriter(catalog)){
			base.toXML(fw, true);
		} catch (IOException e) {
			logger.error("",e);
		}
		
    	FileUtils.MoveFile(catalog, newFile, true, true);
    	
    	this.setUri(newFile.getAbsolutePath());
    	SaveItemHelper.authorizedSave(this,user, true, false,ci);
    }
    
    public void moveCatalogEntries(CatCatalogI cat, String existingRootPath, String newRootPath,
								   @Nullable String currentProject, @Nullable String destinationProject)
			throws IOException {

    	if (currentProject == null && cat instanceof CatCatalogBean bean) {
    		currentProject = CatalogUtils.getCatalogProject(bean);
		}

    	for (CatEntryI entry : cat.getEntries_entry()) {
			String uri = entry.getUri().replaceFirst("^file://","");
			String relativePath = uri;
    		File existingLocation = CatalogUtils.getFile(entry, existingRootPath, currentProject);
    		if (existingLocation == null || !existingLocation.exists()) {
    			logger.error("Unable to retrieve file corresponding to catalog entry URI=" + uri +
						" in catalog " + newRootPath + "/" + cat.getId());
    			continue;
			}

    		// Handle URLs or absolute paths
    		if (FileUtils.IsAbsolutePath(relativePath)) {
    			if (FileUtils.IsUrl(uri, true)) {
    				relativePath = entry.getId();
				} else {
					if (uri.startsWith(existingRootPath)) {
						relativePath = uri.substring(existingRootPath.length());
					} else {
						// "Fudge" a relative path
						if (uri.indexOf("/") > 0) {
							relativePath = uri.substring(uri.indexOf("/") + 1);
						} else if (uri.indexOf("\\") > 0) {
							relativePath = uri.substring(uri.indexOf("\\") + 1);
						}
					}
					entry.setUri(relativePath);
					entry.setId(relativePath);
				}
    		}

    		// Move the file locally
    		File newFile = new File(newRootPath, relativePath);
        	File parentDir = newFile.getParentFile();
        	if (!parentDir.exists()) {
        		parentDir.mkdirs();
        	}
          	FileUtils.MoveFile(existingLocation, newFile, true, true);
    	}
    	
    	for(CatCatalogI subset: cat.getSets_entryset()){
    		moveCatalogEntries(subset, existingRootPath, newRootPath, currentProject, destinationProject);
    	}

    	// reset project in catalog
		if (destinationProject != null && !destinationProject.equals(currentProject) && cat instanceof CatCatalogBean bean) {
			CatalogUtils.setCatalogProject(bean, destinationProject);
		}
    }
}
