/*
 * org.nrg.xdat.om.base.BaseXnatAbstractresource
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.model.XnatAbstractresourceTagI;
import org.nrg.xdat.om.base.auto.AutoXnatAbstractresource;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.utils.CatalogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatAbstractresource extends AutoXnatAbstractresource {

	public BaseXnatAbstractresource(ItemI item)
	{
		super(item);
	}

	public BaseXnatAbstractresource(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatAbstractresource(UserI user)
	 **/
	public BaseXnatAbstractresource()
	{}

	public BaseXnatAbstractresource(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    /**
     * Returns ArrayList of java.io.File objects
     * @return
     */
    public abstract ArrayList<File> getCorrespondingFiles(String rootPath);


    /**
     * Returns ArrayList of java.lang.String objects
     * @return
     */
    public abstract ArrayList getCorrespondingFileNames(String rootPath);


    Long size = null;
    public long getSize(String rootPath){
        if (size ==null){
            calculate(rootPath);
        }
        return size;
    }
    
    public String getReadableFileStats() {
        return CatalogUtils.formatFileStats(getLabel(), getFileCount(), getFileSize());
    }

    public String getReadableFileSize() {
        Object fileSize = getFileSize();
        if (fileSize == null) {
            return "Empty";
        }
        return CatalogUtils.formatSize((Long) fileSize);
    }

    public void calculate(String rootPath){
    	long sizeI = 0;
        int countI = 0;
        for (File f : this.getCorrespondingFiles(rootPath)) {
            if (f.exists()){
                countI++;
                sizeI+=f.length();
            }
        }

        size = sizeI;
        count = countI;
    }

    Integer count = null;
    public Integer getCount(String rootPath){
        if (count ==null){
            calculate(rootPath);
        }
        return count;
    }

    /**
     * Prepends this path to the enclosed URI or path variables.
     * @param root
     */
    public abstract void prependPathsWith(String root);

    /**
     * Relatives this path from the first occurrence of the indexOf string.
     * @param indexOf
     */
    public abstract void relativizePaths(String indexOf, boolean caseSensitive);

    /**
     * Appends this path to the enclosed URI or path variables.
     * @param root
     */
    public abstract ArrayList<String> getUnresolvedPaths();

    public boolean isInRAWDirectory(){
        boolean hasRAW= false;
        for (String path : getUnresolvedPaths())
        {
            if (path.indexOf("RAW/")!=-1)
            {
                hasRAW=true;
                break;
            }
            if (path.indexOf("SCANS/")!=-1)
            {
                hasRAW=true;
                break;
            }
        }
        return hasRAW;
    }

    /**
     * Path to Files
     * @return
     */
    public String getFullPath(String rootPath){
        return "";
    }

    public String getContent(){
        return "";
    }

    public String getFormat(){
        return "";
    }

    public void deleteWithBackup(String rootPath, UserI user, EventMetaI c) throws Exception{
    	deleteFromFileSystem(rootPath);
    }

    public void deleteFromFileSystem(String rootPath){
        ArrayList<File> files = this.getCorrespondingFiles(rootPath);
        for(File f: files){
            try {
				FileUtils.MoveToCache(f);
				if(FileUtils.CountFiles(f.getParentFile(),true)==0){
					FileUtils.DeleteFile(f.getParentFile());
				}
		    } catch (FileNotFoundException e) {
		    	e.printStackTrace();
		    } catch (IOException e) {
		    	e.printStackTrace();
		    }
        }
    }
    
    public String getTagString(){
    	StringBuffer sb =new StringBuffer();
    	for(XnatAbstractresourceTagI tag:this.getTags_tag()){
    		if(sb.length()>0){
    			sb.append(",");
    		}
    		if(tag.getName()!=null){
    			sb.append(tag.getName()).append("=");
    		}
    		sb.append(tag.getTag());
    	}
    	
    	return sb.toString();
    }
    
    private String base_URI=null;
    public String getBaseURI(){return base_URI;}
    public void setBaseURI(String b){
    	if(b.startsWith("/REST") || b.startsWith("/data")){
    		this.base_URI=b;
    	}else{
    		this.base_URI="/data" +b;
    	}
    }
    
    
    public abstract void moveTo(File newSessionDir, String existingSessionDir,String rootPath,UserI user,EventMetaI ci) throws IOException,Exception;
}
