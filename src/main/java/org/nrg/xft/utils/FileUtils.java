/*
 * org.nrg.xft.utils.FileUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */



package org.nrg.xft.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTool;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;


public  class FileUtils
{
	static org.apache.log4j.Logger logger = Logger.getLogger(FileUtils.class);

	public static final int LARGE_DOWNLOAD=1000*1024;
	public static final int SMALL_DOWNLOAD=8*1024;
	
    public static String ReadFromFile(String path) throws IOException {
        return ReadFromFile(new File(path));
    }

    public static String ReadFromFile(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        try {
            FileChannel channel = stream.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            return Charset.defaultCharset().decode(buffer).toString();
        }
        finally {
            stream.close();
        }
    }

	public static void OutputToFile(String content, String filePath)
	{
		File _outFile;
		FileOutputStream _outFileStream;
		PrintWriter _outPrintWriter;

		_outFile = new File(filePath);

		try
		{
			_outFileStream = new FileOutputStream ( _outFile );
		}  // end try
		catch ( IOException except )
		{
			logger.error("FileUtils::OutputToFile",except);
			return;
		}  // end catch
		// Instantiate and chain the PrintWriter
		_outPrintWriter = new PrintWriter ( _outFileStream );

		_outPrintWriter.println(content);
		_outPrintWriter.flush();

		_outPrintWriter.close();

		try
		{
			_outFileStream.close();
		}
		catch ( IOException except )
		{
			logger.error("FileUtils::OutputToFile",except);
		}
	}
	public static void OutputToFile(String content, String filePath,boolean append)
	{
		File _outFile;
		FileOutputStream _outFileStream;
		PrintWriter _outPrintWriter;

		_outFile = new File(filePath);

		try
		{
			_outFileStream = new FileOutputStream ( _outFile,append);
		}  // end try
		catch ( IOException except )
		{
			logger.error("FileUtils::OutputToFile",except);
			return;
		}  // end catch
		// Instantiate and chain the PrintWriter
		_outPrintWriter = new PrintWriter ( _outFileStream );

		_outPrintWriter.println(content);
		_outPrintWriter.flush();

		_outPrintWriter.close();

		try
		{
			_outFileStream.close();
		}
		catch ( IOException except )
		{
			logger.error("FileUtils::OutputToFile",except);
			return;
		}
	}


    @SuppressWarnings("deprecation")
    public static String GetContents(File f)
    {
        try {
            FileInputStream in = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(in);
            StringBuffer sb = new StringBuffer();
            while (dis.available() !=0)
            {
                                    // Print file line to screen
                sb.append(dis.readLine()).append("\n");
            }

            dis.close();

            return sb.toString();
        } catch (Exception e) {
            logger.error("",e);
            return "";
        }
    }

	public static void OutputToSubFolder(String folder, String file, String content) throws XFTInitException
	{
		String local = XFTTool.GetSettingsLocation() + folder + File.separator;
		File f = new File(local);
		if (! f.exists())
		{
			f.mkdir();
		}

		OutputToFile(content,XFTTool.GetSettingsLocation() + folder + File.separator + file);
	}

	public static boolean SearchFolderForChild(File folder, String folderName)
	{
		boolean _return = false;

		if (folder.exists())
		{
			String[] children = folder.list();
			for (int i=0; i< children.length; i++)
			{
				if (children[i].indexOf(folderName) != -1)
				{
					return true;
				}
			}
		}else
		{
			return false;
		}

		return _return;
	}

	public static Properties GetPropertiesFromFile(File file) throws IOException
	{
		Properties props = new Properties();
		InputStream propsIn = new FileInputStream(file);
		props.load(propsIn);

		return props;
	}

    public static void WriteContents(File f,OutputStream out)
    {
        try {
            FileInputStream in = new FileInputStream(f);
            while (in.available() !=0)
            {
                                    // Print file line to stream
                out.write(in.read());
            }

            in.close();

            out.close();
        } catch (Exception e) {
            logger.error("",e);
        }
    }

    public static void OutputToFile(InputStream in,File f)
    {
        try {
            FileOutputStream out = new FileOutputStream(f);
            while (in.available() !=0)
            {
                                    // Print file line to stream
                out.write(in.read());
            }

            in.close();

            out.close();
        } catch (Exception e) {
            logger.error("",e);
        }
    }

    public static List<Integer> getCharContent(File f)
    {
        try {
            FileInputStream in = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(in);
            List<Integer> sb = new ArrayList<Integer>();
            while (dis.available() !=0)
            {
                                    // Print file line to screen
                sb.add(dis.read());
            }

            dis.close();

            return sb;
        } catch (Exception e) {
            logger.error("",e);
            return null;
        }
    }

	public static String RemoveRootPath(String path,String rootPath)
	{
	    if (rootPath==null || rootPath.equals(""))
	    {
	        return path;
	    }else{

	        if (path.startsWith(rootPath))
	        {
	            path = path.substring(rootPath.length());

		        if (path.startsWith("\\") || path.startsWith("/"))
		        {
		            path = path.substring(1);
		        }
	        }

	        return path;
	    }
	}

//	public static String AppendRootPath(String path)
//	{
//        return AppendRootPath(XFT.GetArchiveRootPath(),path);
//	}

    public static String AppendRootPath(String root,String local)
    {
        if (root==null || root.equals(""))
        {
            return local;
        }else{
            if (IsAbsolutePath(local))
            {
                return local;
            }

            while (local.startsWith("\\") || local.startsWith("/"))
            {
                local = local.substring(1);
            }

            root=AppendSlash(root);

            return root + local;
        }
    }

    public static String AppendSlash(String root)
    {
        if (root==null || root.equals(""))
        {
            return null;
        }else{

            if (!root.endsWith("/") && !root.endsWith("\\")){
                root += File.separator;
            }

            return root;
        }
    }

	public static boolean IsAbsolutePath(String path)
	{
        if (path.startsWith("file:") || path.startsWith("http:") || path.startsWith("https:") || path.startsWith("srb:"))
        {
            return true;
        }
	    if (File.separator.equals("/"))
        {
            if (path.startsWith("/"))
            {
                return true;
            }
        }else{
            if (path.indexOf(":\\")!=-1)
            {
                return true;
            }else if (path.indexOf(":/")!=-1)
            {
                return true;
            }
        }

       return false;
	}
	
	public static String RemoveAbsoluteCharacters(String p){
	    if(p.indexOf(":\\")>-1){
		p=p.substring(p.indexOf(":\\")+2);
	    }
	    
	    if(p.indexOf(":/")>-1){
		p=p.substring(p.indexOf(":/")+2);
	    }
	    
	    while(p.startsWith("/")){
		p=p.substring(1);
	    }
	    
	    while(p.startsWith("\\")){
		p=p.substring(1);
	    }
	    
	    return p;
	}
//
//	public static String ArcFind(String sessionID) throws Exception
//	{
//	    if (XFT.GetArchiveRootPath().equals(""))
//	    {
//	        throw new Exception("Error: No root archive path found.");
//	    }else{
//	        File root = new File(XFT.GetArchiveRootPath());
//	        if (!root.exists())
//	        {
//		        throw new Exception("Error: Root archive directory not found.");
//	        }else{
//	            File[] archives = root.listFiles();
//
//	            for (int i=0;i<archives.length;i++)
//	            {
//	                File archive = archives[i];
//	                String[] sessions = archive.list();
//
//	                if (sessions != null)
//	                {
//	                    for (int k=0;k<sessions.length;k++)
//		                {
//		                    String session = sessions[k];
//		                    if (session.equalsIgnoreCase(sessionID))
//		                    {
//		                        return archive.getAbsolutePath();
//		                    }
//		                }
//	                }
//	            }
//	        }
//	    }
//
//	    throw new Exception("Error: Session not found.");
//	}
//
//	public static String ParseSessionIDFromPath(String archive_path) throws Exception
//	{
//	    String path = archive_path;
//	    if (XFT.GetArchiveRootPath().equals(""))
//	    {
//	        throw new Exception("Error: No root archive path found.");
//	    }else{
//	        if (path.startsWith(XFT.GetArchiveRootPath()))
//	        {
//	            path = path.substring(XFT.GetArchiveRootPath().length() + 1);
//	        }
//
//	        if (path.startsWith(File.separator))
//	        {
//	            path = path.substring(1);
//	        }
//
//	        if (path.indexOf(File.separator) == -1)
//	        {
//	           throw new Exception("Unable to parse session id from path: " + archive_path);
//	        }
//
//	        path = path.substring(path.indexOf(File.separator) + 1);
//
//	        if (path.indexOf(File.separator) == -1)
//	        {
//	           throw new Exception("Unable to parse session id from path: " + archive_path);
//	        }
//
//	        String sessionID = path.substring(0,path.indexOf(File.separator));
//
//	        if (sessionID == null || sessionID.equals(""))
//	        {
//		       throw new Exception("Unable to parse session id from path: " + archive_path);
//	        }
//
//	        return sessionID;
//	    }
//	}

	public static File CreateTempFolder(String prefix,File directory) throws IOException
	{
		File tempFile = File.createTempFile(prefix, "", directory);
	    if (!tempFile.delete())
	        throw new IOException();
	    if (!tempFile.mkdir())
	        throw new IOException();
	    return tempFile;
	}

	/**
	 * @param f
	 * @return ArrayList of Strings (one string for each line in the file)
	 */
	public static List<String> FileLinesToArrayList(File f) throws FileNotFoundException, IOException{
	    List<String> al = new ArrayList<String>();

    	FileInputStream in = null;
        BufferedReader reader = null;
	    try
	    {
	    	in = new FileInputStream(f);
	        reader = new BufferedReader(new InputStreamReader(in));
	        String line = null;
	        while ((line = reader.readLine()) != null)
		{
				al.add(line);
			}
	    } finally {
	    	if (reader != null) {
	    		reader.close();
	    	}
	    	if (in != null) {
	    		in.close();
	    	}
		}

	    return al;
	}

    public static List<List<String>> CSVFileToArrayList(File f) throws FileNotFoundException, IOException{
        ArrayList<List<String>> all = new ArrayList<List<String>>();
        List<String> rows = FileLinesToArrayList(f);
        for (String row : rows)
        {
            all.add(StringUtils.CommaDelimitedStringToArrayList(row));
        }
        all.trimToSize();
        return all;
    }

	public static int CountFiles(File src,boolean stopAt1) {
		if(src.isDirectory()){
		    File[] files = src.listFiles();
		    int count =0;
		    
		    if(files==null){
		    	return count;
		    }	    
		    
		    for (int i=0;i<files.length;i++){
		        File f = files[i];
		        if(f.isDirectory()){
		            int sub_count=CountFiles(f,stopAt1);
		            if(sub_count>0 && stopAt1){
		        	return 1;
		            }else{
			            count+=sub_count;
		            }
		        }
		        else{ 
		            if(stopAt1){
		        	return 1;
		            }else{
			            count++;
		            }
		        }
		    }
		    
		    return count;
		}else{
			return 1;
		}
	}

	public static boolean HasFiles(File src) {
		if(src.isDirectory()){
		    File[] files = src.listFiles();
		    
		    if(files==null){
		    	return false;
		    }	    
		    
		    for (int i=0;i<files.length;i++){
		        File f = files[i];
		        if(f.isDirectory()){
		        	if(HasFiles(f)){
		        		return true;
		        	}
		        }
		        else return true;
		    }
		    
		    return false;
		}else{
			return true;
		}
	}

	/**
	 * Return a List of duplicate Files
	 * 
	 * @param src
	 * @param dest
	 * @param filter
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<File> CompareDirectories(File src, File dest, FileFilter filter) throws FileNotFoundException,IOException{
	    final List<File> match=new ArrayList<File>();
		for (final File f: src.listFiles()){
			final File dF=(new File(dest,f.getName()));
	        if (f.isDirectory())
	        {
	        	match.addAll(CompareDirectories(f,dF,filter));
	        }else if(filter==null || filter.accept(src)){
	            if(dF.exists()){
	            	match.add(dF);
	            }
	        }
	    }
		return match;
	}


	/**
	 * Return a List of duplicate Files
	 * 
	 * @param src
	 * @param dest
	 * @param filter
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static File FindFirstMatch(File src, File dest, FileFilter filter) throws FileNotFoundException,IOException{
	    for (final File f: src.listFiles()){
	    	final File dF=(new File(dest,f.getName()));
	        if (f.isDirectory())
	        {
	        	final File match=FindFirstMatch(f,dF,filter);
	        	if(match!=null)return match;
	        }else if(filter==null || filter.accept(f)){
	            if(dF.exists()){
	            	return dF;
	            }
	        }
	    }
	    
	    return null;
	}
	
	public static void CopyDir(File src, File dest,boolean overwrite) throws FileNotFoundException,IOException{
		CopyDir(src,dest,overwrite,null);
	}
	
	public static void CopyDir(File src, File dest,boolean overwrite, FileFilter filter) throws FileNotFoundException,IOException{
		if(!overwrite){
			final File match=FindFirstMatch(src, dest, filter);
			if(match!=null){
				throw new IOException(match.getName() + " already exists.");
			}
		}
		
		CopyDirImpl(src,dest,overwrite,filter);
	}
	
	private static void CopyDirImpl(File src, File dest,boolean overwrite, FileFilter filter) throws FileNotFoundException,IOException{
	    if (!dest.exists())
	    {
		    dest.mkdirs();
	    }

	    for (final File f: src.listFiles()){
	    	final File dChild=new File(dest,f.getName());
	    	if(filter==null || filter.accept(dChild)){
	        if (f.isDirectory())
	        {
		        	CopyDirImpl(f,dChild,overwrite,filter);
	        }else{
		            CopyFile(f,dChild,overwrite);
		        }
	        }
	    }
	}

	public static void CopyFile(File src, File dest,boolean overwrite) throws FileNotFoundException,IOException{
        if (dest.exists() && !overwrite){
            return;
	        }
	
        org.apache.commons.io.FileUtils.copyFile(src, dest);
    }


	public static void MoveDir(File src, File dest,boolean overwrite) throws FileNotFoundException,IOException{
		MoveDir(src,dest,overwrite,null);
	}
	public static void MoveDir(File src, File dest,boolean overwrite, FileFilter filter) throws FileNotFoundException,IOException{
		MoveDir(src,dest,overwrite,filter,new DefaultOldFileHandler());
	}
	
	public static void MoveDir(File src, File dest,boolean overwrite, FileFilter filter,OldFileHandlerI handler) throws FileNotFoundException,IOException{
		if(!overwrite){
			final File match=FindFirstMatch(src, dest, filter);
			if(match!=null){
				throw new IOException(match.getName() + " already exists.");
			}
		}
		
		MoveDirImpl(src,dest,overwrite,filter,handler);
	}
	
	public static interface OldFileHandlerI{
		public boolean handle(File f);
	}
	
	public static class DefaultOldFileHandler implements OldFileHandlerI{
		public boolean handle(File f){
			return f.delete();
		}
	}
	
	private static void MoveDirImpl(File src, File dest,boolean overwrite, FileFilter filter,OldFileHandlerI handler) throws FileNotFoundException,IOException{
        boolean moved = false;

        if (!dest.exists()){
            try {
                moved = src.renameTo(dest);
            } catch (Throwable e) {
                logger.error("",e);
            }
        }

        if (!moved)
        {
            if (!dest.exists())
            {
                dest.mkdirs();
            }
            if (src!=null && src.exists()){
                for (final File f: src.listFiles()){
                	final File dChild=new File(dest,f.getName());
        	    	if(filter==null || filter.accept(dChild)){
                    if (f.isDirectory())
                    {
	                    	MoveDirImpl(f,dChild,overwrite,filter,handler);
                    }else{
                    	try{
	                          MoveFile(f,dChild,overwrite,handler);
                    	}catch(FileNotFoundException e){
                    		logger.warn("", e);
                    	}
                    }
                }
                }

                if(src.listFiles()==null || src.listFiles().length==0){
                src.delete();
            }
            }
        }
	}
	
	
	
	public static void MoveFile(File src, File dest,boolean overwrite,boolean deleteDIR,OldFileHandlerI handler)throws FileNotFoundException,IOException{
		boolean moved = false;

		if(!src.exists())throw new FileNotFoundException(src.getAbsolutePath()); 
		
            if (!dest.exists()){
                try {
                    moved = src.renameTo(dest);
                } catch (Throwable e) {
                    logger.error("",e);
                }
            }else if (!overwrite){
                return;
            }else{
            	if(handler!=null)
            		handler.handle(dest);
            	else
            		dest.delete();
            }
    
    	if (!moved) {
    	    if (dest.exists()) {
    		org.apache.commons.io.FileUtils.forceDelete(dest);
    	    }
    	    org.apache.commons.io.FileUtils.moveFile(src, dest);
    	}

	if(deleteDIR){
            	CleanEmptyDirectories(src.getParentFile());
            }
	}

	public static void MoveFile(File src, File dest,boolean overwrite,boolean deleteDIR) throws FileNotFoundException,IOException{
	    MoveFile(src,dest,overwrite,deleteDIR,null);
	}

	public static void MoveFile(File src, File dest,boolean overwrite,OldFileHandlerI handler) throws FileNotFoundException,IOException{
	    MoveFile(src,dest,overwrite,false,handler);
	}
	public static void MoveFile(File src, File dest,boolean overwrite) throws FileNotFoundException,IOException{
	    MoveFile(src,dest,overwrite,false,null);
	}

	public static void CleanEmptyDirectories(File level1) throws FileNotFoundException,IOException{
		if(!level1.isDirectory())return;
		if(FileUtils.HasFiles(level1))return;
		File level2=null;
		File level3=null;
		
		if(level1.getParentFile()!=null){
			if(!FileUtils.HasFiles(level1.getParentFile()))
				level2=level1.getParentFile();
		}
		
		if(level2!=null && level2.getParentFile()!=null){
			if(!FileUtils.HasFiles(level2.getParentFile()))
				level3=level2.getParentFile();
		}
		
		if(level3!=null && level3.getParentFile()!=null){
			if(!FileUtils.HasFiles(level3.getParentFile())){
            	FileUtils.DeleteFile(level3.getParentFile());
            	return;
			}
		}
		
		if(level2!=null && level2.getParentFile()!=null){
			if(!FileUtils.HasFiles(level2.getParentFile())){
            	FileUtils.DeleteFile(level2.getParentFile());
            	return;
			}
		}
		
		if(level1!=null && level1.getParentFile()!=null){
			if(!FileUtils.HasFiles(level1.getParentFile())){
            	FileUtils.DeleteFile(level1.getParentFile());
            	return;
			}
		}
		
		if(!FileUtils.HasFiles(level1)){
        	FileUtils.DeleteFile(level1);
        	return;
		}
	}


    /**
     * Populates list of files which do not match.
     * @param src
     * @param dest
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<String> CompareFile(File src, File dest) throws FileNotFoundException,IOException {
    	return CompareFile(src, dest, null);
    }

    /**
     * Populates list of files which do not match.
     * @param src
     * @param dest
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<String> CompareFile(File src, File dest, List<String> al) throws FileNotFoundException,IOException{
        if (al==null){
            al = new ArrayList<String>();
        }
        if (src.exists() && dest.exists()){

            if (src.isDirectory() && dest.isDirectory()){
                File[] files = src.listFiles();
                for (int i=0;i<files.length;i++){
                    File f = files[i];
                    String file_name = f.getName();
                    String destPath = dest.getAbsolutePath();
                    if (!destPath.endsWith(File.separator)){
                        destPath += File.separator;
                    }
                    CompareFile(f,new File(destPath + file_name),al);
                }
            }else if (src.isDirectory()){
                al.add(dest.getCanonicalPath() + ": Is a file");
            }else if (dest.isDirectory()){
                al.add(dest.getCanonicalPath() + ": Is a directory");
            }else{
                if (src.length()== dest.length()){

                }else{
                    al.add(dest.getCanonicalPath() + ": Size mismatch " + src.length() + ":" + dest.length());
                }
            }
        }else{
            if (!src.exists() && !dest.exists())
            {

            }else if (!src.exists()){
                al.add(dest.getAbsolutePath() + ": extra file");
            }else{
                al.add(dest.getAbsolutePath() + ": does not exist");
            }
        }

        return al;
    }

    public static void GUnzipFiles(File f) throws java.io.FileNotFoundException, java.io.IOException{
        if (f.exists()){
            if (f.isDirectory()){
                File[] files = f.listFiles();
                for (int i=0;i<files.length;i++){
                    File child = files[i];
                    GUnzipFiles(child);
                }
            }else{
                if (f.getName().endsWith(".gz")){
                    java.io.InputStream in = new java.io.FileInputStream(f);
                    in =new GZIPInputStream(in);
                    File outFile = new File(f.getParent() + File.separator + f.getName().substring(f.getName().length()-3));
                    FileOutputStream out = new FileOutputStream(outFile);
                    byte[] buf = new byte[FileUtils.SMALL_DOWNLOAD];

//                  Transfer bytes from the file to the ZIP file
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }

                    // Complete the entry
                    out.flush();
                    try {
                        out.close();
                        in.close();
                    } catch (IOException e) {
                    }

                    f.delete();
                }
            }
        }else{
            throw new java.io.FileNotFoundException(f.getAbsolutePath());
        }
    }

    public static void GZIPFiles(File f) throws java.io.FileNotFoundException, java.io.IOException{
        if (f.exists()){
            if (f.isDirectory()){
                File[] files = f.listFiles();
                for (int i=0;i<files.length;i++){
                    File child = files[i];
                    GZIPFiles(child);
                }
            }else{
                if (!f.getName().endsWith(".gz")){
                    java.io.FileInputStream in = new java.io.FileInputStream(f);
                    File outFile = new File(f.getParent() + File.separator + f.getName() + ".gz");
                    FileOutputStream fileOutputStream = new FileOutputStream(outFile);
                    GZIPOutputStream out = new GZIPOutputStream(fileOutputStream);
                    byte[] buf = new byte[FileUtils.SMALL_DOWNLOAD];

//                  Transfer bytes from the file to the ZIP file
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }

                    // Complete the entry
                    out.flush();
                    try {
                        out.close();
                        in.close();
                    } catch (IOException e) {
                    }

                    f.delete();
                }
            }
        }else{
            throw new java.io.FileNotFoundException(f.getAbsolutePath());
        }
    }

    public static void DeleteFile(File f)
    {
        if (f.isDirectory())
        {
            String[] files = f.list();
            for (int i=0;i<files.length;i++){
                File child = new File(f.getAbsolutePath() + File.separator + files[i]);
                DeleteFile(child);
            }
            if (!f.delete()){
                System.out.println("Failed to delete: " + f.getAbsolutePath());
            }
        }else{
            if (!f.delete()){
                System.out.println("Failed to delete: " + f.getAbsolutePath());
            }
        }
    }
    
	public static String renameWTimestamp(final String n,Date d){
		return n + "_" + getTimestamp(d);
	}
	
	public static String getTimestamp(Date d){
		final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
		return sdf.format(d);
	}

	public static String BuildRootHistoryPath(){
		String cache = XFT.GetArchiveRootPath();
		
		if(StringUtils.IsEmpty(cache))
		{
			return "/.history/";
		}else{
			return AppendSlash(cache)+".history/";
		}
	}
	
	public static File BuildHistoryParentFile(File f){
		return new File(AppendSlash(BuildRootHistoryPath())+AppendSlash(RemoveAbsoluteCharacters(f.getParentFile().getAbsolutePath())));
	}
    
    public static File BuildHistoryFile(final File f, String timestamp) throws FileNotFoundException, IOException{
		return new File(BuildHistoryParentFile(f),(StringUtils.IsEmpty(timestamp)?"":AppendSlash(timestamp))+ f.getName());
	}
    
    public static File MoveToHistory(final File f,String timestamp) throws FileNotFoundException, IOException{
		final File dest=BuildHistoryFile(f,timestamp);
				
		if(f.isDirectory())
			FileUtils.MoveDir(f, dest, true);
		else
			FileUtils.MoveFile(f, dest, true);
		
		return dest; 
    }
    
    public static File CopyToHistory(final File f,String timestamp) throws FileNotFoundException, IOException{
		final File dest=BuildHistoryFile(f,timestamp);
				
		if(f.isDirectory())
			FileUtils.CopyDir(f, dest, true);
		else
			FileUtils.CopyFile(f, dest, true);
		
		return dest; 
    }
    
    public static void MoveToCache(File f) throws FileNotFoundException, IOException{
    	if(XDAT.getBoolSiteConfigurationProperty("files.allow_move_to_cache", true)){
			String cache = XFT.GetCachePath();
			if(cache.equals(""))
			{
			    cache="/cache/";
			}
			cache=AppendSlash(cache)+"DELETED";
			
			cache=AppendSlash(cache)+getTimestamp(Calendar.getInstance().getTime());		
			
			String path=f.getAbsolutePath();
				
			String dest=AppendSlash(cache)+RemoveAbsoluteCharacters(path);
			
			if(f.isDirectory())
				FileUtils.MoveDir(f, new File(dest), true);
			else
				FileUtils.MoveFile(f, new File(dest), true);
    	}else{
    		if(f.isDirectory()){
    			org.apache.commons.io.FileUtils.deleteDirectory(f);
    		}else{
    			f.delete();
    		}
    	}
    }

    public static String RelativizePath(File parent, File child){
        if (child.getAbsolutePath().startsWith(parent.getAbsolutePath())){
            return child.getAbsolutePath().substring(parent.getAbsolutePath().length()+1);
        }
        String path=child.getName();

        File temp = child;

        while (temp.getParentFile()!=null){
            if (temp.getParentFile().getName().equals(parent.getName())){
                break;
            }else{
                temp =temp.getParentFile();
                path=temp.getName()+"/"+path;
            }
        }

        return path;
    }
    
    public static void ValidateUriAgainstRoot(String s1, String root, String message) throws InvalidValueException{
    	final URI rootU;
    	try {
    		rootU=new URI(root);
		} catch (URISyntaxException e) {
			logger.error("Archive Root Path is not a valid URI",e);
			return;
		}
		
		if(s1.contains("\\")){
			s1=s1.replace('\\', '/');
		}
		
		ValidateUriAgainstRoot(s1, rootU, message);
		
		if(FileUtils.IsAbsolutePath(s1)){
			try {
				new URI(s1).normalize();
			} catch (URISyntaxException e) {
				logger.error("Expected Path: "+s1);
				logger.error("Current Path: "+root);
				logger.error("",e);
				throw new InvalidValueException("Invalid URI: " + s1 + "\nCheck for occurrences of non-standard characters.");
			}
		}
    }
    
    public static void ValidateUriAgainstRoot(String s1, URI root, String message) throws InvalidValueException{
    	URI u;
		if(FileUtils.IsAbsolutePath(s1)){
			try {
				u=new URI(s1).normalize();
			} catch (URISyntaxException e) {
				logger.error("",e);
				throw new InvalidValueException("Invalid URI: " + s1 + "\nCheck for occurrences of non-standard characters.");
			}
			
			final URI rootU=root.normalize();
			if(u!=null && rootU.relativize(u).equals(u)){
				throw new InvalidValueException(message);
			}
		}
    }
	public static void deleteQuietly(File src){
		if(src.exists())org.apache.commons.io.FileUtils.deleteQuietly(src);
	}
	public static void deleteDirQuietly(File s){
		if(s.exists())
			try {
				org.apache.commons.io.FileUtils.deleteDirectory(s);
			} catch (IOException e) {
			}
	}
}

