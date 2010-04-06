//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Aug 17, 2006
 *
 */
package org.nrg.xft.utils.zip;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Expand;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.srb.XNATDirectory;
import org.nrg.xnat.srb.XNATSrbFile;

import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.srb.SRBFile;
import edu.sdsc.grid.io.srb.SRBFileInputStream;


/**
 * @author timo
 *
 */
public class ZipUtils implements ZipI {
    final static boolean DEBUG = false;
    byte[] buf = new byte[FileUtils.LARGE_DOWNLOAD];
    ZipOutputStream out = null;
    int compression=ZipOutputStream.DEFLATED;
    boolean decompress = false;
        
    public void setOutputStream(OutputStream outStream) throws IOException
    {
        out = new ZipOutputStream(outStream);
        out.setMethod(ZipOutputStream.DEFLATED);
    }
    
    public void setOutputStream(OutputStream outStream, int compressionMethod) throws IOException
    {
        out = new ZipOutputStream(outStream);
        out.setMethod(compressionMethod);
        compression=compressionMethod;
    }
    
    public void setCompressionMethod(int compressionMethod){
        out.setMethod(compressionMethod);
        compression=compressionMethod;
    }
    
    /**
     * @param relativePath path name for zip file
     * @param absolutePath Absolute path used to load file.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void write(String relativePath,String absolutePath) throws FileNotFoundException,IOException
    {
        if (out== null)
        {
            throw new IOException("Undefined OutputStream");
        }
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
        if (out== null)
        {
            throw new IOException("Undefined OutputStream");
        }
        java.io.InputStream in = new java.io.FileInputStream(f);
        try {
            if (decompress && f.getName().toLowerCase().endsWith(".gz"))
            {
                in = new java.util.zip.GZIPInputStream(in);
                if (relativePath.toLowerCase().endsWith(".gz"))
                {
                    relativePath = relativePath.substring(0,relativePath.length()-3);
                }
            }
            
            if (decompress && f.getName().toLowerCase().endsWith(".gz") && compression==ZipOutputStream.STORED)
            {
                File temp = File.createTempFile("temp", "");
                FileOutputStream fos = new FileOutputStream(temp);
                int len;
                while ((len = in.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
                fos.close();
                fos = null;
                in.close();
                
                ZipEntry entry = new java.util.zip.ZipEntry(relativePath);
    
                if (compression==ZipOutputStream.STORED)
                {
                    CRC32 crc32 = new CRC32();
                    int n;
                    FileInputStream fileinputstream = new FileInputStream(temp);
                    byte [] rgb = new byte [1000];
                    while ((n = fileinputstream.read(rgb)) > -1)
                    {
                      crc32.update(rgb, 0, n);
                    }
    
                    fileinputstream.close();
                    fileinputstream = null;
                    
                    entry.setSize(temp.length());
                    entry.setCrc(crc32.getValue());
                }
                entry.setTime(f.lastModified());
                
                out.putNextEntry(entry);
                
                // Transfer bytes from the file to the ZIP file
                len=0;
                in = new java.io.FileInputStream(temp);
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                // Complete the entry
                out.closeEntry();
                FileUtils.DeleteFile(temp);
            }else{
                ZipEntry entry = new java.util.zip.ZipEntry(relativePath);
    
                if (compression==ZipOutputStream.STORED)
                {
                    CRC32 crc32 = new CRC32();
                    int n;
                    FileInputStream fileinputstream = new FileInputStream(f);
                    byte [] rgb = new byte [1000];
                    while ((n = fileinputstream.read(rgb)) > -1)
                    {
                      crc32.update(rgb, 0, n);
                    }
    
                    fileinputstream.close();
                    
                    entry.setSize(f.length());
                    entry.setCrc(crc32.getValue());
                }
                entry.setTime(f.lastModified());
                
                out.putNextEntry(entry);
                
                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
    
                // Complete the entry
                out.closeEntry();
            }
        } finally{
            try {
                in.close();
            } catch (Throwable e) {
            }
        }
    }
    
    public void write(String relativePath, InputStream is) throws IOException
    {
        if (compression==ZipOutputStream.STORED)
        {
            File temp = File.createTempFile("temp", "");
            FileOutputStream fos = new FileOutputStream(temp);
            int len;
            while ((len = is.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            fos.close();
            fos = null;
            is.close();
            
            write(relativePath,temp);
            
            FileUtils.DeleteFile(temp);
        }else{
            ZipEntry entry = new java.util.zip.ZipEntry(relativePath);        
            out.putNextEntry(entry);
            
            // Transfer bytes from the file to the ZIP file
            int len;
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            // Complete the entry
            out.closeEntry();
        }
    }
    
    public void write(String relativePath, SRBFile srb) throws IOException
    {
        byte[] tempBUF = new byte[FileUtils.LARGE_DOWNLOAD];
        if (compression==ZipOutputStream.STORED)
        {
            File temp = File.createTempFile("temp", "");
            temp.setLastModified(srb.lastModified());
            SRBFileInputStream is = new SRBFileInputStream(srb);
            
            FileOutputStream fos = new FileOutputStream(temp);
            int len;
            while ((len = is.read(tempBUF)) > 0) {
                fos.write(tempBUF, 0, len);
            }
            fos.close();
            fos = null;
            is.close();
            
            write(relativePath,temp);
            
            FileUtils.DeleteFile(temp);
        }else{
            long startTime = Calendar.getInstance().getTimeInMillis();
            ZipEntry entry = new java.util.zip.ZipEntry(relativePath);
            entry.setTime(srb.lastModified());
            entry.setSize(srb.length());
            out.putNextEntry(entry);
            
            if(DEBUG)System.out.print(srb.getName() + "," + srb.length() + "," + (Calendar.getInstance().getTimeInMillis()-startTime) + "ms");
            startTime = Calendar.getInstance().getTimeInMillis();
            
            SRBFileInputStream is = new SRBFileInputStream(srb);
            // Transfer bytes from the file to the ZIP file
            int len;
            
            if(DEBUG)System.out.print("," + (Calendar.getInstance().getTimeInMillis()-startTime) + "ms");
            startTime = Calendar.getInstance().getTimeInMillis();
            
            while ((len = is.read(tempBUF)) > 0) {
                if(DEBUG)System.out.print(",R:" + (Calendar.getInstance().getTimeInMillis()-startTime) + "ms");
                startTime = Calendar.getInstance().getTimeInMillis();
                out.write(tempBUF, 0, len);
                if(DEBUG)System.out.print(",W:" + (Calendar.getInstance().getTimeInMillis()-startTime) + "ms");
                startTime = Calendar.getInstance().getTimeInMillis();
                out.flush();
            }
            
            // Complete the entry
            out.closeEntry();
            
            if(DEBUG)System.out.println("," + (Calendar.getInstance().getTimeInMillis()-startTime) + "ms");
            startTime = Calendar.getInstance().getTimeInMillis();
        }
    }

    public void write(XNATDirectory dir) throws IOException{
        ArrayList files = dir.getFiles();
        ArrayList subDirectories = dir.getSubdirectories();
        
        String path = dir.getPath();
        for (int i = 0; i < files.size(); i++) {
            long startTime = Calendar.getInstance().getTimeInMillis();
            GeneralFile file = (GeneralFile)files.get(i);
            String relative = path + "/" + file.getName();
            if (file instanceof XNATSrbFile){
                if(relative.indexOf(((XNATSrbFile)file).getSession())!=-1)
                {
                    relative = relative.substring(relative.indexOf(((XNATSrbFile)file).getSession()));
                }
            }
            write(relative, (SRBFile)file);
        }
        
        for (int i=0;i<subDirectories.size();i++){
            XNATDirectory sub = (XNATDirectory)subDirectories.get(i);
            write(sub);
        }
    }
    
    /**
     * @throws IOException
     */
    public void close() throws IOException{
        if (out== null)
        {
            throw new IOException("Undefined OutputStream");
        }
        out.close();
    }
    
    public void extract(File f, String dir, boolean deleteZip) throws IOException{;
                
        final class Expander extends Expand {
            public Expander() {
     	    project = new Project();
    	    project.init();
    	    taskType = "unzip";
    	    taskName = "unzip";
    	    target = new Target();
    	}	
        }
        Expander expander = new Expander();
        expander.setSrc(f);
        expander.setDest(new File(dir));
        expander.execute();

        if (deleteZip)
            f.deleteOnExit();
    }
    
    public ArrayList extract(InputStream is, String destination) throws IOException{;
         ArrayList extractedFiles = new ArrayList();       
        //  Create a ZipInputStream to read the zip file
        BufferedOutputStream dest = null;
        ZipInputStream zis = new ZipInputStream( new BufferedInputStream( is ) );
    
        File df = new File(destination);
        if (!df.exists()){
            df.mkdirs();
        }
        // Loop over all of the entries in the zip file
        int count;
        byte data[] = new byte[ FileUtils.LARGE_DOWNLOAD ];
        ZipEntry entry;
        while( ( entry = zis.getNextEntry() ) != null )
        {
          if( !entry.isDirectory() )
          {            
            String destFN = destination + File.separator + entry.getName();
    
            File f = new File(destFN);
            f.getParentFile().mkdirs();
            // Write the file to the file system
            FileOutputStream fos = new FileOutputStream(f);
            dest = new BufferedOutputStream( fos, FileUtils.LARGE_DOWNLOAD );
            while( (count = zis.read( data, 0, FileUtils.LARGE_DOWNLOAD ) ) != -1 )
            {
              dest.write( data, 0, count );
            }
            dest.flush();
            dest.close();
            extractedFiles.add(new File(destFN));
          }else{
              df = new File(destination,entry.getName());
              if (!df.exists()){
                  df.mkdirs();
              }
              extractedFiles.add(df);
          }
        }
        zis.close();
        return extractedFiles;

    }

    public static void Unzip(File f) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(f));
        String s = f.getAbsolutePath();
        int index = s.lastIndexOf('/');
        if (index == -1){
            index = s.lastIndexOf('\\');
        }
        if (index == -1)
        {
            throw new IOException("Unknown Zip File.");
        }
        
        String dir = s.substring(0,index);
        
        final class Expander extends Expand {
            public Expander() {
     	    project = new Project();
    	    project.init();
    	    taskType = "unzip";
    	    taskName = "unzip";
    	    target = new Target();
    	}	
        }
        Expander expander = new Expander();
        expander.setSrc(new File(s));
        expander.setDest(new File(dir));
        expander.execute();
        
    }
    
    public void writeDirectory(File dir) throws FileNotFoundException, IOException
    {
        writeDirectory("", dir);
    }
    
    private void writeDirectory(String parentPath, File dir) throws FileNotFoundException, IOException
    {
        String dirName = dir.getName() + "/";
        for(int i=0;i<dir.listFiles().length;i++)
        {
            File child = dir.listFiles()[i];
            if (child.isDirectory())
            {
                writeDirectory(parentPath + dirName,child);
            }else{
                write(parentPath + dirName + child.getName(),child);
            }
        }
        
    }

    /**
     * @return Returns the _compressionMethod.
     */
    public int getCompressionMethod() {
        return this.compression;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.utils.zip.ZipI#getDecompressFilesBeforeZipping()
     */
    public boolean getDecompressFilesBeforeZipping() {
        return decompress;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.utils.zip.ZipI#setDecompressFilesBeforeZipping(boolean)
     */
    public void setDecompressFilesBeforeZipping(boolean method) {
        decompress=method;
    }
      
}
