/*
 * org.nrg.xft.utils.ZipUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.utils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.nrg.xft.XFT;

/**
 * @author timo
 *
 */
public class ZipUtils {
    byte[] buf = new byte[FileUtils.SMALL_DOWNLOAD];
    ZipOutputStream out = null;

    public ZipUtils(OutputStream outStream)
    {
        out = new ZipOutputStream(outStream);
        out.setMethod(ZipOutputStream.DEFLATED);
    }

    /**
     * @param relativePath path name for zip file
     * @param absolutePath Absolute path used to load file.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void write(String relativePath,String absolutePath) throws FileNotFoundException,IOException
    {
        File f = new File(absolutePath);
        write(relativePath,f);
    }

    /**
     * @param relativePath path name for zip file
     * @param f
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void write(String relativePath, File f) throws FileNotFoundException,IOException
    {
        java.io.FileInputStream in = new java.io.FileInputStream(f);

        out.putNextEntry(new java.util.zip.ZipEntry(relativePath));

        // Transfer bytes from the file to the ZIP file
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        // Complete the entry
        out.closeEntry();
        in.close();
    }

    /**
     * @throws IOException
     */
    public void close() throws IOException{
        out.close();
    }

    /**
     * @param outStream
     * @param files
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void ZipToStream(OutputStream outStream,Hashtable files) throws FileNotFoundException,IOException
    {
        ZipUtils zip = new ZipUtils(outStream);
        Enumeration enumer = files.keys();
        while(enumer.hasMoreElements())
        {
            String key = (String)enumer.nextElement();
            File f = new File((String)files.get(key));
            if (!f.isDirectory())
            {
                zip.write(key,f);
            }
        }

        try {
            // Complete the ZIP file
            zip.close();
        } catch (java.io.IOException e) {
        }
    }

    public static void Unzip(File s) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(s));
        ZipInputStream zin = new ZipInputStream(in);
        ZipEntry e;

        while((e=zin.getNextEntry())!= null) {
          unzipFile(zin, e.getName());
          }
        zin.close();
        }

    public static void Unzip(String s) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(s));
        ZipInputStream zin = new ZipInputStream(in);
        ZipEntry e;

        while((e=zin.getNextEntry())!= null) {
          unzipFile(zin, e.getName());
          }
        zin.close();
        }

      public static void unzipFile(ZipInputStream zin, String s) throws IOException {
    	  if(XFT.VERBOSE)System.out.println("Extracting " + s);
        FileOutputStream out = new FileOutputStream(s);
        byte [] b = new byte[FileUtils.SMALL_DOWNLOAD];
        int len = 0;
        while ( (len=zin.read(b))!= -1 ) {
          out.write(b,0,len);
          }
        out.close();
        }

}
