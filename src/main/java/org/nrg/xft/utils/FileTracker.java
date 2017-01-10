/*
 * core: org.nrg.xft.utils.FileTracker
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * @author timo
 *
 */
public class FileTracker {
    private ArrayList<String> ids = new ArrayList<String>();
    private ArrayList<String> relativePaths = new ArrayList<String>();
    private ArrayList<String> absolutePaths = new ArrayList<String>();
    private ArrayList<String> fileStatus = new ArrayList<String>();
    private ArrayList<File> files = new ArrayList<File>();
    private ArrayList<Long> sizes = new ArrayList<Long>();
    
    private ArrayList directories = new ArrayList();
    
    public final static String MISC="MISC";
    public final static String KNOWN="KNOWN";
    
    private Hashtable childTrackers= new Hashtable();
    
    /**
     * 
     */
    public FileTracker() {
        super();
    }

    public String addFile(String relativePath,java.io.File f,String status)
    {
        Long size = new Long(0);
        
            if (f.exists())
            {
                try {
                    size = new Long(f.length());
                } catch (Throwable e) {
                    size=null;
                }
            }
            return addFile(relativePath,f.getAbsolutePath(),status,size,f);
        
    }

    public String addFile(String relativePath,String absolutePath,String status, Long size,File f)
    {
        if (relativePath !=null && !relativePath.trim().equals(""))
        {
            if (absolutePath !=null && !absolutePath.trim().equals(""))
            {
	            if (relativePaths.contains(relativePath))
	            {
	                int previousindex= relativePaths.indexOf(relativePath);
	                return (String)ids.get(previousindex);
	            }else{
	                String _return = ""+ids.size();
	               ids.add(_return);
	               relativePaths.add(relativePath);
	               absolutePaths.add(absolutePath);
	               fileStatus.add(status);
	               sizes.add(size);
                   files.add(f);
	               
	               return _return;
	            }
            }
        }
        
        return null;
    }
    
    public int getIDIndex(String id){
        return ids.indexOf(id);
    }
    
    public int getRelativePathIndex(String relativePath){
        return relativePaths.indexOf(relativePath);
    }
    
    public int getAbsolutePathIndex(String absolutePath){
        return absolutePaths.indexOf(absolutePath);
    }
    
    public String[] getFileInfo(int index)
    {
        String[] _return = new String[4];
        _return[0]=(String)ids.get(index);
        _return[1]=(String)relativePaths.get(index);
        _return[2]=(String)absolutePaths.get(index);
        _return[3]=(String)fileStatus.get(index);
        return _return;
    }
    
    public String getID(int index)
    {
        return (String)this.ids.get(index);
    }
    
    public String getRelativePath(int index)
    {
        return (String)this.relativePaths.get(index);
    }
    
    public long getSize(int index)
    {
        return ((Long)this.sizes.get(index)).longValue();
    }
    
    public File getFile(int index)
    {
        return this.files.get(index);
    }
    
    public String getRelativePathByID(String id)
    {
        int index = ids.indexOf(id);
        if (index==-1)
        {
            return null;
        }
        return (String)this.relativePaths.get(index);
    }
    
    public File getFileByID(String id)
    {
        int index = ids.indexOf(id);
        if (index==-1)
        {
            return null;
        }
        return this.files.get(index);
    }
    
    public String getRelativePathByAbsolutePath(String path)
    {
        int index = absolutePaths.indexOf(path);
        if (index==-1)
        {
            return null;
        }
        return (String)this.relativePaths.get(index);
    }
    
    public String getAbsolutePath(int index)
    {
        return (String)this.absolutePaths.get(index);
    }
    
    public int indexOf(java.io.File f)
    {
        return getAbsolutePathIndex(f.getAbsolutePath());
    }
    
    public int getSize(){
        return ids.size();
    }
    /**
     * @return Returns the absolutePaths.
     */
    public ArrayList getAbsolutePaths() {
        return absolutePaths;
    }
    /**
     * @return Returns the ids.
     */
    public ArrayList<String> getIds() {
        return ids;
    }
    /**
     * @return Returns the relativePaths.
     */
    public ArrayList getRelativePaths() {
        return relativePaths;
    }
    
    public Hashtable createHash(){
        Hashtable hash = new Hashtable();
        for (int i=0;i<getSize();i++){
            hash.put(getRelativePath(i),getAbsolutePath(i));
        }
        return hash;
    }
    
    /**
     * @param al ArrayList of Strings (Absolute Paths)
     * @return Returns Hashtable mapping relative paths to absolute paths for the specified files
     */
    public Hashtable createPartialHashByAbsolutePaths(ArrayList al){
        Hashtable hash = new Hashtable();
        for (int i=0;i<al.size();i++){
            String absolutePath = (String)al.get(i);
            String relativePath = getRelativePathByAbsolutePath(absolutePath);
            if (relativePath==null)
            {
                String copy = absolutePath.replace('\\','/');
                if (copy.indexOf(":/")!=-1)
                {
                    copy = copy.substring(copy.indexOf(":/")+2);
                }
                while (copy.startsWith("/"))
                {
                    copy = copy.substring(1);
                }
                relativePath= copy;
            }
            hash.put(relativePath,absolutePath);
        }
        return hash;
    }
    
    /**
     * @param al ArrayList of Strings (Absolute Paths)
     * @return Returns Hashtable mapping relative paths to absolute paths for the specified files
     */
    public Hashtable createPartialHashByIDs(ArrayList al){
        Hashtable hash = new Hashtable();
        for (int i=0;i<al.size();i++){
            String id = (String)al.get(i);
            int index = getIDIndex(id);
            if (index!=-1)
                hash.put(getRelativePath(index),getAbsolutePath(index));
        }
        return hash;
    }
    
    /**
     * @param al ArrayList of Strings (File IDs)
     * @return Returns Hashtable mapping relative paths to File objects
     */
    public Hashtable<String,File> createPartialFileHashByIDs(ArrayList al,String rootFolder){
        String lowerCase = rootFolder.toLowerCase();
        Hashtable<String,File> hash = new Hashtable<String,File>();
        for (int i=0;i<al.size();i++){
            String id = (String)al.get(i);
            int index = getIDIndex(id);
            String relativePath = getRelativePath(index);
            if (FileUtils.IsAbsolutePath(relativePath))
            {
                String copy = relativePath.replace('\\','/').toLowerCase();
                
                if (copy.indexOf(lowerCase+"/")!=-1)
                {
                    copy = relativePath.substring(copy.indexOf(lowerCase+"/"));
                    while (copy.startsWith("/"))
                    {
                        copy = copy.substring(1);
                    }
                    
                    relativePath= copy;
                }else if (copy.indexOf(lowerCase)!=-1)
                {
                    copy = relativePath.substring(copy.indexOf(lowerCase));
                    while (copy.startsWith("/"))
                    {
                        copy = copy.substring(1);
                    }
                    
                    relativePath= rootFolder + "/" + copy;
                }else{
                    if (copy.indexOf(":/")!=-1)
                    {
                        relativePath = relativePath.substring(copy.indexOf(":/")+2);
                    }
                    while (relativePath.startsWith("/"))
                    {
                        relativePath = relativePath.substring(1);
                    }
                    
                    relativePath= rootFolder + "/" + relativePath;
                }
            }
            hash.put(relativePath,files.get(index));
        }
        return hash;
    }
    
    /**
     * @param al ArrayList of Strings (File IDs)
     * @return Returns Hashtable mapping relative paths to absolute paths
     */
    public Hashtable createPartialHashByIDs(ArrayList al,String rootFolder){
        String lowerCase = rootFolder.toLowerCase();
        Hashtable hash = new Hashtable();
        for (int i=0;i<al.size();i++){
            String id = (String)al.get(i);
            int index = getIDIndex(id);
            String relativePath = getRelativePath(index);
            if (FileUtils.IsAbsolutePath(relativePath))
            {
                String copy = relativePath.replace('\\','/').toLowerCase();
                
                if (copy.indexOf(lowerCase+"/")!=-1)
                {
                    copy = relativePath.substring(copy.indexOf(lowerCase+"/"));
                    while (copy.startsWith("/"))
                    {
                        copy = copy.substring(1);
                    }
                    
                    relativePath= copy;
                }else if (copy.indexOf(lowerCase)!=-1)
                {
                    copy = relativePath.substring(copy.indexOf(lowerCase));
                    while (copy.startsWith("/"))
                    {
                        copy = copy.substring(1);
                    }
                    
                    relativePath= rootFolder + "/" + copy;
                }else{
                    if (copy.indexOf(":/")!=-1)
                    {
                        relativePath = relativePath.substring(copy.indexOf(":/")+2);
                    }
                    while (relativePath.startsWith("/"))
                    {
                        relativePath = relativePath.substring(1);
                    }
                    
                    relativePath= rootFolder + "/" + relativePath;
                }
            }
            hash.put(relativePath,getAbsolutePath(index));
        }
        return hash;
    }
    
    /**
     * @param al ArrayList of Strings (Absolute Paths)
     * @return Returns a Hashtable mapping relative paths to absolute paths
     */
    public Hashtable createPartialHashByAbsolutePaths(ArrayList al, String rootFolder){
        String lowerCase = rootFolder.toLowerCase();
        Hashtable hash = new Hashtable();
        for (int i=0;i<al.size();i++){
            String absolutePath = (String)al.get(i);
            int index = getAbsolutePathIndex(absolutePath);
            String relativePath = getRelativePath(index);
            if (FileUtils.IsAbsolutePath(relativePath))
            {
                String copy = relativePath.replace('\\','/').toLowerCase();
                
                if (copy.indexOf(lowerCase+"/")!=-1)
                {
                    copy = relativePath.substring(copy.indexOf(lowerCase+"/"));
                    while (copy.startsWith("/"))
                    {
                        copy = copy.substring(1);
                    }
                    
                    relativePath= copy;
                }else if (copy.indexOf(lowerCase)!=-1)
                {
                    copy = relativePath.substring(copy.indexOf(lowerCase));
                    while (copy.startsWith("/"))
                    {
                        copy = copy.substring(1);
                    }
                    
                    relativePath= rootFolder + "/" + copy;
                }else{
                    if (copy.indexOf(":/")!=-1)
                    {
                        relativePath = relativePath.substring(copy.indexOf(":/")+2);
                    }
                    while (relativePath.startsWith("/"))
                    {
                        relativePath = relativePath.substring(1);
                    }
                    
                    relativePath= rootFolder + "/" + relativePath;
                }
            }
            hash.put(relativePath,absolutePath);
        }
        return hash;
    }
    
    public Hashtable createHash(String rootFolder){
        String lowerCase = rootFolder.toLowerCase();
        Hashtable hash = new Hashtable();
        for (int i=0;i<getSize();i++){
            String relativePath = getRelativePath(i);
            if (FileUtils.IsAbsolutePath(relativePath))
            {
                String copy = relativePath.replace('\\','/').toLowerCase();
                
                if (copy.indexOf(lowerCase+"/")!=-1)
                {
                    copy = relativePath.substring(copy.indexOf(lowerCase+"/"));
                    while (copy.startsWith("/"))
                    {
                        copy = copy.substring(1);
                    }
                    
                    relativePath= copy;
                }else if (copy.indexOf(lowerCase)!=-1)
                {
                    copy = relativePath.substring(copy.indexOf(lowerCase));
                    while (copy.startsWith("/"))
                    {
                        copy = copy.substring(1);
                    }
                    
                    relativePath= rootFolder + "/" + copy;
                }else{
                    if (copy.indexOf(":/")!=-1)
                    {
                        relativePath = relativePath.substring(copy.indexOf(":/")+2);
                    }
                    while (relativePath.startsWith("/"))
                    {
                        relativePath = relativePath.substring(1);
                    }
                    
                    relativePath= rootFolder + "/" + relativePath;
                }
            }
            hash.put(relativePath,getAbsolutePath(i));
        }
        return hash;
    }
    
    public void addChildFileTracker(String id, FileTracker ft)
    {
        this.childTrackers.put(id.toLowerCase(),ft);
    }
    
    public FileTracker getChildFileTracker(String id)
    {
        return (FileTracker)this.childTrackers.get(id.toLowerCase());
    }
    
    public void addDirectory(String dir)
    {
        int replaceindex = -1;
        for(int i=0;i<directories.size();i++){
            String stored = (String)directories.get(i);
            if (stored.equals(dir))
            {
                return;
            }else{
                if (stored.startsWith(dir)){
                    replaceindex=i;
                    break;
                }else if(dir.startsWith(stored)){
                    return;
                }
            }
        }
        
        if (replaceindex==-1)
        {
            directories.add(dir);
        }else{
            directories.remove(replaceindex);
            directories.add(dir);
        }
    }
    
    public ArrayList getDirectories(){
        return this.directories;
    }
    
    public void syncToFS(){
        String root = null;
        for (int i=0; i<absolutePaths.size();i++)
        {
            String path = absolutePaths.get(i);
            if (path.startsWith("srb:"))
            {
                
            }
        }
        
    }
}
