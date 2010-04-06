//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Dec 7, 2005
 *
 */
package org.nrg.xft.collections;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class FileCollection extends ArrayList {
    private long fileSize =0;
    private int fileCount = 0;
    
    /**
     * 
     */
    public FileCollection() {
        super();
    }
    /**
     * 
     */
    public FileCollection(File f) {
        super();
        add(f);
    }
    
    /**
     * 
     */
    public FileCollection(ArrayList files) {
        super();
        addAll(files);
    }
    
    public void add(File f)
    {
        if (f.isDirectory()) {
            long size = 0;
            int count = 0;
            for (int i = 0; i < f.listFiles().length; i++) {
                FileCollection child = new FileCollection(f.listFiles()[0]);
                fileSize += child.fileSize;
                fileCount+= child.fileCount;
            }
        } else {
            fileSize +=f.length();
            fileCount++;
        }
        
        super.add(f);
    }
        
    public void addAll(ArrayList files)
    {
        for(int i=0;i<files.size();i++)
        {
            File f = (File)files.get(i);
            add(f);
        }
    }

    /**
     * @return Returns the fileCount.
     */
    public int getFileCount() {
        return fileCount;
    }
    /**
     * @return Returns the fileSize.
     */
    public long getFileSize() {
        return fileSize;
    }
}
