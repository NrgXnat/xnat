/*
 * org.nrg.xdat.om.base.BaseXnatResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
package org.nrg.xdat.om.base;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXnatResource;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.StringUtils;
import org.nrg.xnat.srb.XNATDirectory;
import org.nrg.xnat.srb.XNATSrbSearch;

import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.srb.SRBFile;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatResource extends AutoXnatResource {

	public BaseXnatResource(ItemI item)
	{
		super(item);
	}

	public BaseXnatResource(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatResource(UserI user)
	 **/
	public BaseXnatResource()
	{}

	public BaseXnatResource(Hashtable properties, UserI user)
	{
		super(properties,user);
	}


    protected ArrayList files=null;
    protected ArrayList fileNames=null;
    /**
     * Returns ArrayList of java.io.File objects
     * @return
     */
    public ArrayList<File> getCorrespondingFiles(String rootPath)
    {
        if (files==null)
        {
            String fullPath = getFullPath(rootPath);
            if (fullPath.endsWith("\\")) {
                fullPath = fullPath.substring(0,fullPath.length() -1);
            }
            if (fullPath.endsWith("/")) {
                fullPath = fullPath.substring(0,fullPath.length() -1);
            }
            files = getAssociatedFilesOnLocalFileSystem(fullPath);
        }
        return files;
    }

    public ArrayList<File> getCorrespondingFiles()
    {
        if (files==null || files.size()==0)
        {
           if (getUri().startsWith("file:")) {
               try {
                   URI uri = new URI(getUri());
                   File file = new File(uri);
                   files = getAssociatedFilesOnLocalFileSystem(file.getAbsolutePath());
               }catch(Exception e) {

               }
           }
        }
        return files;
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
     * Prepends this path to the enclosed URI or path variables.
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

    /**
     * Relatives this path from the first occurrence of the indexOf string.
     * @param indexOf
     */
    public void relativizePaths(String indexOf, boolean caseSensitive){
        String uri = this.getUri();
        uri= uri.replace('\\', '/');
        if (uri.indexOf(indexOf)==-1){
            if (!caseSensitive){
                int index = uri.toLowerCase().indexOf(indexOf.toLowerCase());
                if (index!=-1){
                    this.setUri(uri.substring(index + 1));
                }
            }
        }else{
            this.setUri(uri.substring(uri.indexOf(indexOf) + 1));
        }
    }


    /**
     * Appends this path to the enclosed URI or path variables.
     * @param root
     */
    public ArrayList<String> getUnresolvedPaths(){
        ArrayList<String> al = new ArrayList<String>();
        String p = getUri();
        p.replace('\\', '/');
        al.add(p);
        return al;
    }

    public String getFullPath(String rootPath){

        String fullPath = StringUtils.ReplaceStr(FileUtils.AppendRootPath(rootPath,this.getUri()),"\\","/");
        while (fullPath.indexOf("//")!=-1)
        {
            fullPath =StringUtils.ReplaceStr(fullPath,"//","/");
        }

        if(!fullPath.endsWith("/"))
        {
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
        if (fullPath.startsWith("srb:"))
            return getAssociatedFilesFromRemoteFileSystem(fullPath,tempDir);
        else
            return getAssociatedFilesOnLocalFileSystem(fullPath);
    }

    protected ArrayList<File> getAssociatedFilesFromRemoteFileSystem(String fullPath,File tempDir) {
        ArrayList<File> associatedFiles = new ArrayList();
        int lastIndexOfSlash = fullPath.lastIndexOf(SRBFile.PATH_SEPARATOR);
        if (lastIndexOfSlash != -1) {
            String path = fullPath.substring(0,lastIndexOfSlash);
            try {
                URI uri = new URI(path);
                XNATDirectory srbDir = XNATSrbSearch.getFilesAssociatedWith(uri.getPath(),fullPath.substring(lastIndexOfSlash+1));
                srbDir.importFiles(tempDir);
                ArrayList srbAssociatedFiles = srbDir.getFiles();
                for (int i = 0; i < srbAssociatedFiles.size(); i++) {
                    associatedFiles.add(new File(tempDir.getAbsolutePath() + File.separator + ((GeneralFile)srbAssociatedFiles.get(i)).getName()));
                }
            }catch(Exception e) {
                logger.error("Couldnt get Files for " + fullPath,e);
            }

        }
        return associatedFiles;
    }

    protected File getFileOnLocalFileSystem(String fullPath) {
        File f = new File(fullPath);
        if (!f.exists()){
            if (!fullPath.endsWith(".gz")){
            	f= new File(fullPath + ".gz");
            	if (!f.exists()){
            		logger.debug("Unable to find file:" + fullPath);
            		return null;
            	}
            }else{
                return null;
            }
        }
        
        return f;
    }

    protected ArrayList<File> getAssociatedFilesOnLocalFileSystem(String fullPath) {
        ArrayList<File> associatedFiles = new ArrayList();
        if (!new File(fullPath).exists()){
            if (!fullPath.endsWith(".gz")){
                if (!new File(fullPath +".gz").exists())return associatedFiles;
            }else{
                return associatedFiles;
            }
        }
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
        int indexOfDot = (fileroot.toLowerCase().lastIndexOf(".ima")!=-1 ?fileroot.toLowerCase().lastIndexOf(".ima") : fileroot.toLowerCase().lastIndexOf(".img"));
        if (indexOfDot != -1) {
            fileroot = fileroot.substring(0,indexOfDot);
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
        }else{
            if (!new File(fullPath).exists()){
                if (!fullPath.endsWith(".gz")){
                    if (new File(fullPath +".gz").exists()){
                        associatedFiles.add(new File(fullPath +".gz"));
                    }
                }
            }else{
                associatedFiles.add(new File(fullPath));
            }
        }
        return associatedFiles;
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
    	
    	for(File f: this.getCorrespondingFiles(rootPath)){
    		FileUtils.MoveFile(f, new File(parentDir,f.getName()), true, true);
    	}
    	
    	this.setUri(newFile.getAbsolutePath());
    	SaveItemHelper.authorizedSave(this,user, true, false,ci);
    }

	@Override
	public String getUri() {
		if( super.getUri()!=null){
			return super.getUri().replace('\\', '/');
		}else{
			return null;
		}
	}
    
    
}
