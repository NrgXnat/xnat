/*
 * org.nrg.xdat.om.base.BaseXnatDicomseries
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.model.XnatDicomseriesImageI;
import org.nrg.xdat.om.XnatDicomseriesImage;
import org.nrg.xdat.om.base.auto.AutoXnatDicomseries;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatDicomseries extends AutoXnatDicomseries {

	public BaseXnatDicomseries(ItemI item)
	{
		super(item);
	}

	public BaseXnatDicomseries(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatDicomseries(UserI user)
	 **/
	public BaseXnatDicomseries()
	{}

	public BaseXnatDicomseries(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    protected ArrayList files=null;
    protected ArrayList fileNames=null;
    /**
     * Returns ArrayList of java.io.File objects
     * @return
     */
    public ArrayList getCorrespondingFiles(String rootPath)
    {
        if (files==null)
        {
            files = new ArrayList();

            for (int i=0;i<this.getImageset_image().size();i++){
                org.nrg.xdat.om.XnatDicomseriesImage image = (org.nrg.xdat.om.XnatDicomseriesImage)this.getImageset_image().get(i);
                File f = image.getFile(rootPath);
                files.add(f);
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
            for (int i=0;i<this.getImageset_image().size();i++){
                org.nrg.xdat.om.XnatDicomseriesImage image = (org.nrg.xdat.om.XnatDicomseriesImage)this.getImageset_image().get(i);
                File f = image.getFile(rootPath);
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
        for (int i=0;i<this.getImageset_image().size();i++){
            org.nrg.xdat.om.XnatDicomseriesImage image = (org.nrg.xdat.om.XnatDicomseriesImage)this.getImageset_image().get(i);
            image.prependPathsWith(root);
        }
    }


    /**
     * Relatives this path from the first occurrence of the indexOf string.
     * @param indexOf
     */
    public void relativizePaths(String indexOf, boolean caseSensitive){
        for (int i=0;i<this.getImageset_image().size();i++){
            org.nrg.xdat.om.XnatDicomseriesImage image = (org.nrg.xdat.om.XnatDicomseriesImage)this.getImageset_image().get(i);
            image.relativizePaths(indexOf,caseSensitive);
        }
    }

    /**
     * Appends this path to the enclosed URI or path variables.
     * @param root
     */
    public ArrayList<String> getUnresolvedPaths(){
        ArrayList<String> al = new ArrayList<String>();
        for (int i=0;i<this.getImageset_image().size();i++){
            org.nrg.xdat.om.XnatDicomseriesImage image = (org.nrg.xdat.om.XnatDicomseriesImage)this.getImageset_image().get(i);
            String p = image.getUri();
            p.replace('\\', '/');
            al.add(p);
        }
        return al;
    }

    public String getFullPath(String rootPath){
        String path = "";

        fileNames = new ArrayList();
        for (org.nrg.xdat.model.XnatDicomseriesImageI image:this.getImageset_image()){
            path = image.getUri();
            break;
        }

        String fullPath = StringUtils.ReplaceStr(FileUtils.AppendRootPath(rootPath,path),"\\","/");
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




    public String getLabel(){
        if (this.getDescription().length()>15)
        {
           return this.getDescription().substring(0,14);
        }else
            return this.getDescription();
    }
    
    public void moveTo(File newSessionDir,String existingSessionDir,String rootPath,UserI user,EventMetaI ci) throws IOException,Exception{
    	for(XnatDicomseriesImageI img : this.getImageset_image()){
    		((XnatDicomseriesImage)img).moveTo(newSessionDir, existingSessionDir, rootPath, user,ci);
    	}
    	SaveItemHelper.authorizedSave(this,user, true, false,ci);
    }
}
