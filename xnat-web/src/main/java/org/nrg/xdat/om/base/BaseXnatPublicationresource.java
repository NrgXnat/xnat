/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatPublicationresource
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.om.base.auto.AutoXnatPublicationresource;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serial;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatPublicationresource extends AutoXnatPublicationresource {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatPublicationresource(ItemI item)
	{
		super(item);
	}

	public BaseXnatPublicationresource(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatPublicationresource(UserI user)
	 **/
	public BaseXnatPublicationresource()
	{}

	public BaseXnatPublicationresource(Hashtable properties, UserI user)
	{
		super(properties,user);
	}


    protected ArrayList files=null;
    protected ArrayList fileNames=null;
    /**
     * Returns ArrayList of java.io.File objects
     * @return
     */
    public ArrayList<File> getCorrespondingFiles(String rootPath) {
        if (files==null) {
            String fullPath = getFullPath(rootPath);
            if (fullPath.endsWith("\\")) {
                fullPath = fullPath.substring(0,fullPath.length() -1);
            }
            if (fullPath.endsWith("/")) {
                fullPath = fullPath.substring(0,fullPath.length() -1);
            }
            files = getAssociatedFiles(fullPath);
        }
        return files;
    }

    @Override
    public Map<File, CatEntryI> getCorrespondingFilesWithCatEntries(String rootPath) {
        if (files == null) {
            getCorrespondingFiles(rootPath);
        }
        // This is not a catalogued resource, so cat entries are all null
        Map<File, CatEntryI> filesMap = new HashMap<>();
        filesMap.keySet().addAll(files);
        return filesMap;
    }

    /**
     * Returns ArrayList of java.lang.String objects
     * @return
     */
    public ArrayList getCorrespondingFileNames(String rootPath) {
        if (fileNames==null) {
            fileNames = new ArrayList();
            File f = new File(org.nrg.xft.utils.FileUtils.AppendRootPath(rootPath,this.getUri()));
            if (!f.exists() && !getUri().endsWith(".gz"))
            {
                f = new java.io.File(org.nrg.xft.utils.FileUtils.AppendRootPath(rootPath,this.getUri()) + ".gz");
            }
            fileNames.add(f.getName());
        }
        return fileNames;
    }
    /**
     * Appends this path to the enclosed URI or path variables.
     * @param root
     */
    public void prependPathsWith(String root) {
        if (!FileUtils.IsAbsolutePath(this.getUri())) {
            try {
                    this.setUri(root + this.getUri());
            } catch (Exception e) {
                logger.error("",e);
            }
        }
    }

    /**
     * Relatives this path from the first occurrence of the indexOf string.
     * @param indexOf
     */
    public void relativizePaths(String indexOf, boolean caseSensitive) {
        String uri = this.getUri();
        if (uri.indexOf(indexOf)==-1) {
            if (!caseSensitive) {
                int index = uri.toLowerCase().indexOf(indexOf.toLowerCase());
                if (index!=-1) {
                    this.setUri(uri.substring(index));
                }
            }
        }else{
            this.setUri(uri.substring(uri.indexOf(indexOf)));
        }
    }


    /**
     * Appends this path to the enclosed URI or path variables.
     */
    public ArrayList<String> getUnresolvedPaths() {
        ArrayList<String> al = new ArrayList<String>();
        String p = getUri();
        p.replace('\\', '/');
        al.add(p);
        return al;
    }

    public String getFullPath(String rootPath) {

        String fullPath = StringUtils.replace(FileUtils.AppendRootPath(rootPath, this.getUri()), "\\", "/");
        while (fullPath.indexOf("//")!=-1) {
            fullPath =StringUtils.replace(fullPath,"//","/");
        }

        if(!fullPath.endsWith("/")) {
            fullPath+="/";
        }

        return fullPath;
    }


    /**
     * Gets the files associated with an image.
     * For an uri which is say like: /data/a.img
     * This method will return all files which match the pattern /data/a.*
     *
     * @return
     */


    public ArrayList<File> getAssociatedFiles(String rootPath, File tempDir) {
        String fullPath = getFullPath(rootPath);
        if (fullPath.endsWith(File.separator)) {
            fullPath = fullPath.substring(0,fullPath.length() -1);
        }
        return getAssociatedFiles(fullPath);
    }


    protected ArrayList<File> getAssociatedFiles(String fullPath) {
        ArrayList<File> associatedFiles = new ArrayList();
        if (!new File(fullPath).exists()) return associatedFiles;
        int lastSlash = fullPath.lastIndexOf("/");
        if (lastSlash==-1){
            lastSlash = fullPath.lastIndexOf("\\");
        }
        String path = "";
        String fileroot = fullPath;
        if (lastSlash != -1) {
            path = fullPath.substring(0, lastSlash);
            fileroot = fullPath.substring(lastSlash+1);
        }
        int indexOfDot = fileroot.lastIndexOf(".");
        if (indexOfDot != -1) {
            fileroot = fileroot.substring(0,indexOfDot);
        }
        final String  fileRoot = fileroot;
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.startsWith(fileRoot));
                }
            };
            String[] associatedFileNames = dir.list(filter);
            if (associatedFileNames == null) {
                return associatedFiles;
            }
            for (int i = 0; i < associatedFileNames.length; i++) {
                associatedFiles.add(new File(dir.getAbsolutePath() + File.separator + associatedFileNames[i]));
            }
        }
        return associatedFiles;
    }



    public String getLabel(){
        if (this.getTitle().length()>15)
        {
           return this.getTitle().substring(0,14);
        }else
            return this.getTitle();
    }
    
    public void moveTo(File newSessionDir, String existingSessionDir, String rootPath, @Nullable String currentProject,
                       @Nullable String destinationProject, UserI user, EventMetaI ci)  throws IOException, Exception {
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
    	
    	for(File f: this.getCorrespondingFiles(rootPath)){
    		FileUtils.MoveFile(f, new File(parentDir,f.getName()), true, true);
    	}
    	
    	this.setUri(newFile.getAbsolutePath());
    	SaveItemHelper.authorizedSave(this,user, true, false, ci);
    }
}
