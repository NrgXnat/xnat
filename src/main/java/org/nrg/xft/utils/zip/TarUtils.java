//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Aug 22, 2006
 *
 */
package org.nrg.xft.utils.zip;

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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.tar.TarOutputStream;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
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
public class TarUtils implements ZipI {
    byte[] buf = new byte[FileUtils.LARGE_DOWNLOAD];
    TarOutputStream out = null;
    int _compressionMethod = ZipOutputStream.STORED;
    boolean decompress = false;
        
    public void setOutputStream(OutputStream outStream) throws IOException
    {
        //GZIPOutputStream gzip = new GZIPOutputStream(outStream);
        out = new TarOutputStream(outStream);

    }
    
    public void setOutputStream(OutputStream outStream, int compressionMethod) throws IOException
    {
        _compressionMethod=compressionMethod;
        if(compressionMethod == ZipOutputStream.DEFLATED)
        {
            GZIPOutputStream gzip = new GZIPOutputStream(outStream);
            out = new TarOutputStream(gzip);
        }else{
            out = new TarOutputStream(outStream);
        }

    }

    
    public ArrayList extract(InputStream is, String dir) throws IOException{
    	return extract(is,dir,true,null);
    }
    
    public ArrayList extract(InputStream is, String dir,boolean overwrite,EventMetaI ci) throws IOException{
        ArrayList extractedFiles = new ArrayList();
        if (_compressionMethod==ZipOutputStream.DEFLATED)
        {
            //f = unGzip(f,dir,deleteZip);
            is = new GZIPInputStream(is);
        }
    
        File dest = new File(dir);
        dest.mkdirs();
        
        TarInputStream tis = new TarInputStream(is);
        
        TarEntry te = tis.getNextEntry();
        
        while (te !=null){
            File destPath = new File(dest,te.getName());
            if (te.isDirectory())
            {
                destPath.mkdirs();
            }else
            {
                if(destPath.exists() && !overwrite){
                	throw new IOException("File already exists"+destPath.getCanonicalPath());
                }else{
                	FileUtils.MoveToHistory(destPath,EventUtils.getTimestamp(ci));
                }
                
            	destPath.getParentFile().mkdirs();
                //System.out.println("Writing: " + te.getName());
                FileOutputStream fout = new FileOutputStream(destPath); 

                tis.copyEntryContents(fout); 

                fout.close(); 
            } 
            extractedFiles.add(destPath);
            te = tis.getNextEntry(); 
        }
    
        tis.close();
        return extractedFiles;
    }

    
    public void extract(File f, String dir,boolean deleteZip) throws IOException{;
    
        InputStream is = new FileInputStream(f);
    	if (_compressionMethod==ZipOutputStream.DEFLATED)
    	{
    	    //f = unGzip(f,dir,deleteZip);
            is = new GZIPInputStream(is);
    	}
    
        File dest = new File(dir);
        dest.mkdirs();
        
        TarInputStream tis = new TarInputStream(is);
        
        TarEntry te = tis.getNextEntry();
        
        while (te !=null){
            File destPath = new File(dest.toString() + File.separatorChar + te.getName());
            if (te.isDirectory())
            {
                destPath.mkdirs();
            }else
            {
               // System.out.println("Writing: " + te.getName());
                FileOutputStream fout = new FileOutputStream(destPath); 

                tis.copyEntryContents(fout); 

                fout.close(); 
            } 
            te = tis.getNextEntry(); 
        }
    
        tis.close();
	    
	    f.deleteOnExit();
    }
    
    public File unGzip(File f, String dir,boolean deleteZip) throws IOException{
        if (!dir.endsWith(File.separator)){
            dir += dir + File.separator;
        }
        String dest = dir + "upload.tar";
        File destF = new File(dest);
        FileInputStream fis = new FileInputStream(f);
        GZIPInputStream gis = new GZIPInputStream(fis);
        
        BufferedOutputStream bos = null;
        
        FileOutputStream out = new FileOutputStream(destF);

        bos = new BufferedOutputStream(out);

        byte[] buff = new byte[FileUtils.LARGE_DOWNLOAD];
        int bytesRead;
        
        System.out.println("Uploading file...");
        int loaded = 0;
        long currentStep = 0;
        while(-1 != (bytesRead = gis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
            bos.flush();
        }
        
        out.close();
        fis.close();
        
        if (deleteZip)
            f.deleteOnExit();
        
        return destF;
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
    public void write(String relativePath, InputStream in) throws FileNotFoundException,IOException
    {
        if (out== null)
        {
            throw new IOException("Undefined OutputStream");
        }        
        TarEntry tarAdd = new TarEntry(relativePath);
        tarAdd.setModTime(Calendar.getInstance().getTimeInMillis());
        tarAdd.setMode(TarEntry.LF_NORMAL);
        out.putNextEntry(tarAdd);
        // Write file to archive
        while (true) {
            int nRead = in.read(buf, 0, buf.length);
            if (nRead <= 0)
                break;
            out.write(buf, 0, nRead);
        }
        in.close();             
        out.closeEntry();
    }
    
    /**
     * @param relativePath path name for zip file
     * @param f
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void write(String relativePath, File file) throws FileNotFoundException,IOException
    {
        if (out== null)
        {
            throw new IOException("Undefined OutputStream");
        }        
        
        TarEntry tarAdd = new TarEntry(file);
		tarAdd.setModTime(file.lastModified());
		tarAdd.setMode(TarEntry.LF_NORMAL);
		tarAdd.setName(relativePath.replace('\\', '/'));
		out.putNextEntry(tarAdd);
		// Write file to archive
		FileInputStream in = new FileInputStream(file);
		while (true) {
			int nRead = in.read(buf, 0, buf.length);
			if (nRead <= 0)
				break;
			out.write(buf, 0, nRead);
		}
		in.close();				
		out.closeEntry();
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
    
    public void write(String relativePath, SRBFile srb) throws IOException
    {
        if (out== null)
        {
            throw new IOException("Undefined OutputStream");
        }        
        TarEntry tarAdd = new TarEntry(srb.getName());
        tarAdd.setModTime(srb.lastModified());
        tarAdd.setMode(TarEntry.LF_NORMAL);
        tarAdd.setName(relativePath.replace('\\', '/'));
        tarAdd.setSize(srb.length());
        out.putNextEntry(tarAdd);
        // Write file to archive
        SRBFileInputStream in = new SRBFileInputStream(srb);
        while (true) {
            int nRead = in.read(buf, 0, buf.length);
            if (nRead <= 0)
                break;
            out.write(buf, 0, nRead);
            
        }
        in.close();             
        out.closeEntry();
    }

    private static boolean DEBUG=true;
    
    public void write(XNATDirectory dir) throws IOException{
        ArrayList files = dir.getFiles();
        ArrayList subDirectories = dir.getSubdirectories();
        
        String path = dir.getPath();
        for (int i = 0; i < files.size(); i++) {
            long startTime = Calendar.getInstance().getTimeInMillis();
            GeneralFile file = (GeneralFile)files.get(i);
            String relativePath = path + "/" + file.getName();
            if (file instanceof XNATSrbFile){
                if(relativePath.indexOf(((XNATSrbFile)file).getSession())!=-1)
                {
                    relativePath = relativePath.substring(relativePath.indexOf(((XNATSrbFile)file).getSession()));
                }
            }
            TarEntry tarAdd = new TarEntry(file.getName());
            tarAdd.setModTime(file.lastModified());
            tarAdd.setMode(TarEntry.LF_NORMAL);
            tarAdd.setName(relativePath.replace('\\', '/'));
            tarAdd.setSize(file.length());
            out.putNextEntry(tarAdd);
            // Write file to archive
            if(DEBUG)System.out.print(file.getName() + "," + file.length() + "," + (Calendar.getInstance().getTimeInMillis()-startTime));
            SRBFileInputStream in = new SRBFileInputStream((SRBFile)file);
            if(DEBUG)System.out.print("," + (Calendar.getInstance().getTimeInMillis()-startTime));
            while (true) {
                int nRead = in.read(buf, 0, buf.length);
                if (nRead <= 0)
                    break;
                out.write(buf, 0, nRead);
                
            }
            in.close();             
            out.closeEntry();
            if(DEBUG)System.out.println("," + (Calendar.getInstance().getTimeInMillis()-startTime));
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

    /**
     * @return Returns the _compressionMethod.
     */
    public int getCompressionMethod() {
        return _compressionMethod;
    }
    /**
     * @param method The _compressionMethod to set.
     */
    public void setCompressionMethod(int method) {
        _compressionMethod = method;
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
