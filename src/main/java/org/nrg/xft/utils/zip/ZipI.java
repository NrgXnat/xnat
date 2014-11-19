/*
 * org.nrg.xft.utils.zip.ZipI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/18/14 5:03 PM
 */


package org.nrg.xft.utils.zip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.nrg.xft.event.EventMetaI;
import org.nrg.xnat.srb.XNATDirectory;

/**
 * @author timo
 *
 */
public interface ZipI {
    public abstract void setOutputStream(OutputStream outStream) throws IOException;
    public abstract void setOutputStream(OutputStream outStream,int compressionMethod) throws IOException;
    public abstract void extract(File f, String dir,boolean deleteZip) throws IOException;
    public ArrayList extract(InputStream is, String dir,boolean overwrite,EventMetaI ci) throws IOException;
    public ArrayList extract(InputStream is, String dir) throws IOException;
    public List<String> getDuplicates();

    /**
     * @param relativePath path name for zip file
     * @param absolutePath Absolute path used to load file.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public abstract void write(String relativePath, String absolutePath) throws IOException;
    /**
     * @param dir    Directory to write into.
     * @throws IOException
     */
    public abstract void write(XNATDirectory dir)
            throws IOException;

    
    /**
     * @param relativePath Path name for zip file
     * @param f            File name for zip file
     * @throws IOException
     */
    public abstract void write(String relativePath, File f)
            throws IOException;


    /**
     * @param relativePath Path name for zip file
     * @param f            File name for zip file
     * @throws IOException
     */
    public abstract void write(String relativePath, InputStream f)
            throws IOException;
    
    /**
     * @param dir    Path for directory.
     * @throws IOException
     */
    public abstract void writeDirectory(File dir)
            throws IOException;

    /**
     * @throws IOException
     */
    public abstract void close() throws IOException;
    
    /**
     * @return Returns the _compressionMethod.
     */
    public boolean getDecompressFilesBeforeZipping();
    /**
     * @param method The _compressionMethod to set.
     */
    public void setDecompressFilesBeforeZipping(boolean method);
    
    /**
     * @return Returns the _compressionMethod.
     */
    public int getCompressionMethod();
    /**
     * @param method The _compressionMethod to set.
     */
    public void setCompressionMethod(int method);
}
