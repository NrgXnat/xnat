/*
 * core: org.nrg.xft.utils.FileUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.CSVUtils;
import org.nrg.xft.XFTTool;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;

@SuppressWarnings({"RedundantThrows", "DuplicateThrows"})
@Slf4j
public  class FileUtils {
    private static final String     TSDIR_SECONDS_FORMAT         = "yyyyMMdd_HHmmss";
    private static final DateFormat TSDIR_SECONDS_FORMATTER      = new SimpleDateFormat(TSDIR_SECONDS_FORMAT);
    private static final String     TSDIR_MILLISECONDS_FORMAT    = "yyyyMMdd_HHmmssSSS";
    private static final DateFormat TSDIR_MILLISECONDS_FORMATTER = new SimpleDateFormat(TSDIR_MILLISECONDS_FORMAT);

    public static final int LARGE_DOWNLOAD = 1000 * 1024;
    public static final int SMALL_DOWNLOAD = 8 * 1024;

    /**
     * Reads the contents of the specified file.
     *
     * @param path The path to the file to read
     *
     * @return The contents of the file
     *
     * @throws IOException When an error occurs reading the file.
     *
     * @deprecated Use methods from Apache Commons IO like <b>IOUtils.read()</b>, <b>IOUtils.readLines()</b>, or similar.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static String ReadFromFile(final String path) throws IOException {
        return ReadFromFile(new File(path));
    }

    /**
     * Reads the contents of the specified file.
     *
     * @param file The file to read
     *
     * @return The contents of the file
     *
     * @throws IOException When an error occurs reading the file.
     *
     * @deprecated Use methods from Apache Commons IO like <b>IOUtils.read()</b>, <b>IOUtils.readLines()</b>, or similar.
     */
    @Deprecated
    public static String ReadFromFile(final File file) throws IOException {
        try (final FileInputStream stream = new FileInputStream(file)) {
            final FileChannel      channel = stream.getChannel();
            final MappedByteBuffer buffer  = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            return Charset.defaultCharset().decode(buffer).toString();
        }
    }

    /**
     * Writes the content to the specified file.
     *
     * @param content  The content to be written
     * @param filePath The path to the file to be written
     *
     * @deprecated Use methods from Apache Commons IO like <b>IOUtils.write()</b>, <b>IOUtils.writeLines()</b>, or similar.
     */
    @Deprecated
	public static void OutputToFile(String content, String filePath)
	{
        File             _outFile;
        FileOutputStream _outFileStream;
        PrintWriter      _outPrintWriter;

        _outFile = new File(filePath);

        if (!_outFile.getParentFile().exists()) {
            _outFile.getParentFile().mkdirs();
        }

		try
		{
            _outFileStream = new FileOutputStream(_outFile);
        }  // end try
		catch ( IOException except )
		{
            log.error("FileUtils::OutputToFile", except);
            return;
        }  // end catch
        // Instantiate and chain the PrintWriter
        _outPrintWriter = new PrintWriter(_outFileStream);

        _outPrintWriter.println(content);
        _outPrintWriter.flush();

        _outPrintWriter.close();

		try
		{
            _outFileStream.close();
		}
		catch ( IOException except )
		{
            log.error("FileUtils::OutputToFile", except);
        }
    }

    /**
     * Writes the content to the specified file, overwriting the existing content if <b>append</b> is <b>false</b> and
     * appending to the existing content if <b>append</b> is <b>true</b>.
     *
     * @param content  The content to be written
     * @param filePath The path to the file to be written
     *
     * @deprecated Use methods from Apache Commons IO like <b>IOUtils.write()</b>, <b>IOUtils.writeLines()</b>, or similar.
     */
    @Deprecated
	public static void OutputToFile(String content, String filePath,boolean append)
	{
        File             _outFile;
        FileOutputStream _outFileStream;
        PrintWriter      _outPrintWriter;

        _outFile = new File(filePath);

		try
		{
            _outFileStream = new FileOutputStream(_outFile, append);
        }  // end try
		catch ( IOException except )
		{
            log.error("FileUtils::OutputToFile", except);
            return;
        }  // end catch
        // Instantiate and chain the PrintWriter
        _outPrintWriter = new PrintWriter(_outFileStream);

        _outPrintWriter.println(content);
        _outPrintWriter.flush();

        _outPrintWriter.close();

		try
		{
            _outFileStream.close();
		}
		catch ( IOException except )
		{
            log.error("FileUtils::OutputToFile", except);
        }
    }

    public static final FileFilter isDirectory = File::isDirectory;

    /**
     * Creates a formatted timestamp for the current time using the {@link #TSDIR_MILLISECONDS_FORMAT} specification.
     *
     * @return The formatted timestamp
     */
    public static String getMsTimestamp() {
        return getMsTimestamp(new Date());
    }

    /**
     * Creates a formatted timestamp for the specified date/time using the {@link #TSDIR_MILLISECONDS_FORMAT}
     * specification.
     *
     * @param date The date to be formatted.
     *
     * @return The formatted timestamp
     */
    public static String getMsTimestamp(final Date date) {
        return TSDIR_MILLISECONDS_FORMATTER.format(date);
    }

    @SuppressWarnings("deprecation")
    public static String GetContents(File f)
    {
        try {
            FileInputStream in  = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(in);
            StringBuilder   sb  = new StringBuilder();
            while (dis.available() !=0)
            {
                // Print file line to screen
                sb.append(dis.readLine()).append("\n");
            }

            dis.close();

            return sb.toString();
        } catch (Exception e) {
            log.error("", e);
            return "";
        }
    }

	public static void OutputToSubFolder(String folder, String file, String content) throws XFTInitException
	{
        String local = XFTTool.GetSettingsLocation() + folder + File.separator;
        File   f     = new File(local);
		if (! f.exists())
		{
            f.mkdir();
        }

        OutputToFile(content, XFTTool.GetSettingsLocation() + folder + File.separator + file);
    }

	public static boolean SearchFolderForChild(File folder, String folderName) {
		if (!folder.exists()) {
		    return false;
        }
        final String[] children = folder.list();
        return children != null && Arrays.stream(children).anyMatch(Pattern.compile(folderName).asPredicate());
    }

	public static Properties GetPropertiesFromFile(File file) throws IOException
	{
        Properties  props   = new Properties();
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
            log.error("", e);
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
            log.error("", e);
        }
    }

    public static List<Integer> getCharContent(File f)
    {
        try {
            FileInputStream in  = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(in);
            List<Integer>   sb  = new ArrayList<>();
            while (dis.available() !=0)
            {
                // Print file line to screen
                sb.add(dis.read());
            }

            dis.close();

            return sb;
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

	public static String RemoveRootPath(String path,String rootPath)
	{
	    if (rootPath==null || rootPath.equals(""))
	    {
            return path;
        } else {

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

	/**
	 * Prepend root path to local path if local isn't absolute or URL-like
	 *
	 * (Updated to recognize any URL-like path as absolute)
	 *
	 * @param root      root path to prepend
	 * @param local     local path
	 * @return full path
	 */
	public static String AppendRootPath(@Nullable String root, String local)
	{
		if (StringUtils.isEmpty(root)) {
			return local;
		} else {
			if (IsAbsolutePath(local)) {
				return local;
			}
			while (local.startsWith("\\") || local.startsWith("/")) {
				local = local.substring(1);
			}
			root = FileUtils.AppendSlash(root, "");
			return root + local;
		}
	}

	/**
	 * Append trailing slash to first argument. If empty, return null
	 * @param root string to which we append the slash
	 * @return root with string appended or null
	 */
	public static String AppendSlash(@Nullable String root)
	{
    	return AppendSlash(root, null);
	}

	/**
	 * Append trailing slash to first argument. If empty, return second argument
	 * @param root string to which we append the slash
	 * @param dflt string we return if root is empty
	 * @return root with string appended or dflt
	 */
	public static String AppendSlash(@Nullable String root, @Nullable String dflt)
	{
		if (StringUtils.isEmpty(root)) {
			return dflt;
		} else {
			if (!root.endsWith("/") && !root.endsWith("\\")){
				root += File.separator;
			}
			return root;
		}
    }

	/**
	 * Is path URL-like? (*://)
	 *
	 * @param path 	path to test
	 * @return T/F
	 */
	public static boolean IsUrl(String path)
	{
		return IsUrl(path, false);
	}

	/**
	 * Is path URL-like? (*://)
	 *
	 * @param path			the path
	 * @param remoteOnly	if true, will exclude file:// paths
	 * @return T/F
	 */
	public static boolean IsUrl(String path, boolean remoteOnly)
	{
		if (remoteOnly) {
			return (path.matches("^[A-Za-z0-9]+://.*") || ResourceUtils.isUrl(path)) && !path.startsWith("file://");
		} else {
			return path.matches("^[A-Za-z0-9]+://.*") || ResourceUtils.isUrl(path);
		}
	}

	/**
	 * Is path absolute or URL-like (*://)?
	 *
	 * Updated to recognize any URL-like path as absolute, not just http, https, file, srb protocols
	 *
	 * @param path	path to test
	 * @return true if absolute or URL-like, false if local
	 */
	public static boolean IsAbsolutePath(String path)
	{
		if (IsUrl(path)) {
			return true;
		}
		if (File.separator.equals("/")) {
			return path.startsWith("/");
		} else {
			return (path.contains(":\\") || path.contains(":/"));
		}
	}
	
    public static String RemoveAbsoluteCharacters(String p) {
        if (p.contains(":\\")) {
            p = p.substring(p.indexOf(":\\") + 2);
        }

        if (p.contains(":/")) {
            p = p.substring(p.indexOf(":/") + 2);
        }

        while (p.startsWith("/")) {
            p = p.substring(1);
        }

        while (p.startsWith("\\")) {
            p = p.substring(1);
        }

        return p;
    }

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
     * Reads the contents of the specified file and returns the contents in a list of strings.
     *
     * @param file The file to be read.
     *
     * @return A list of strings (one string for each line in the file)
     */
    public static List<String> FileLinesToArrayList(final File file) throws FileNotFoundException, IOException {
        // BOMInputStream detects byte-order marks in encoded text and removes them to return plain text.
        try (final InputStream input = new BOMInputStream(new FileInputStream(file))) {
            return IOUtils.readLines(input, Charset.defaultCharset());
        }
    }


    public static List<List<String>> CSVFileToArrayList(final File file) throws FileNotFoundException, IOException {
        return FileLinesToArrayList(file).stream().map(XftStringUtils::CommaDelimitedStringToArrayList).collect(Collectors.toList());
    }

    /**
     * Reads the contents of the specified file and returns the contents in a list of rows and columns.
     *
     * @param file The file to be read.
     *
     * @return A list rows and columns of strings (one string for each column in the file)
     */
    public static List<List<String>> csvFileToArrayListUsingApacheCommons(final File file) throws FileNotFoundException, IOException {
        try (final Reader in = new FileReader(file)) {
            return StreamSupport.stream(CSVUtils.DEFAULT_FORMAT.parse(in).spliterator(), false)
                    .map(CSVRecord::toList)
                    .collect(Collectors.toList());
        }
    }

    public static int CountFiles(File src, boolean stopAt1) {
        if (!src.isDirectory()) {
            return 1;
        }
        if (stopAt1) {
            return HasFiles(src) ? 1 : 0;
        }
        int count = 0;
        try (Stream<Path> stream = Files.walk(src.toPath())) {
            count += stream
                .filter(file -> !Files.isDirectory(file))
                .count();
        } catch (IOException e) {
            // Don't throw checked exceptions to match previous signature
            throw new RuntimeException(e);
        }
        return count;
    }

    public static boolean HasFiles(File src) {
        if (!src.isDirectory()) {
            return true;
        }
        try (final Stream<Path> stream = Files.walk(src.toPath())) {
            return stream.anyMatch(file -> !Files.isDirectory(file));
        } catch (IOException e) {
            // Don't throw checked exceptions to match previous signature
            throw new RuntimeException(e);
        }
    }

    /**
     * Return a List of duplicate Files
     *
     * @param src
     * @param dest
     * @param filter
     *
     * @return Returns a list of the files that match in the specified files/directories
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<File> CompareDirectories(File src, File dest, FileFilter filter) throws FileNotFoundException, IOException {
        final List<File> match = new ArrayList<>();
        final File[]     files = src.listFiles();
        if (files != null) {
            for (final File f : files) {
                final File dF = (new File(dest, f.getName()));
                if (f.isDirectory())
                {
                    match.addAll(CompareDirectories(f, dF, filter));
                } else if (filter == null || filter.accept(src)) {
                    if (dF.exists()) {
                        match.add(dF);
                    }
                }
            }
        }
        return match;
    }


    /**
     * Return the first duplicate File found
     *
     * @param source      The source to check for files.
     * @param destination The destination to check for files.
     * @param filter      The filter to apply when checking for files (may be null).
     *
     * @return Returns the first file that matches in the specified files/directories
     */
    public static File FindFirstMatch(final File source, final File destination, final FileFilter filter){
        final Path sourcePath = source.toPath();
        final Path destPath = destination.toPath();
        try (Stream<Path> entries = Files.walk(sourcePath)) {
            Path path = entries.filter(file -> !Files.isDirectory(file) &&
                    (filter == null || filter.accept(file.toFile())) &&
                    Files.exists(destPath.resolve(sourcePath.relativize(file))))
                    .findFirst().orElse(null);
            return path != null ? path.toFile() : null;
        } catch (IOException e) {
            // Don't throw checked exceptions to match previous signature
            throw new RuntimeException(e);
        }
    }

    public static void CopyDir(File src, File dest, boolean overwrite) throws FileNotFoundException, IOException {
        CopyDir(src, dest, overwrite, null);
    }

    public static void CopyDir(File src, File dest, boolean overwrite, FileFilter filter) throws FileNotFoundException, IOException {
        if (!overwrite) {
            final File match = FindFirstMatch(src, dest, filter);
            if (match != null) {
                throw new IOException(match.getName() + " already exists.");
            }
        }

        CopyDirImpl(src, dest, overwrite, filter);
    }

    private static void CopyDirImpl(File src, File dest, boolean overwrite, FileFilter filter) throws FileNotFoundException, IOException {
	    if (!dest.exists())
	    {
            dest.mkdirs();
        }

        final File[] files = src.listFiles();
        if (files != null) {
            for (final File file : files) {
                final File dChild = new File(dest, file.getName());
                if (filter == null || filter.accept(dChild)) {
                    if (file.isDirectory()) {
                        CopyDirImpl(file, dChild, overwrite, filter);
                    } else {
                        CopyFile(file, dChild, overwrite);
                    }
                }
            }
        }
    }

    public static void CopyFile(File src, File dest, boolean overwrite) throws FileNotFoundException, IOException {
        if (dest.exists() && !overwrite) {
            return;
        }

        org.apache.commons.io.FileUtils.copyFile(src, dest);
    }

    public static void MoveDir(File src, File dest, boolean overwrite) throws FileNotFoundException, IOException {
        MoveDir(src, dest, overwrite, null);
    }

    public static void MoveDir(File src, File dest, boolean overwrite, FileFilter filter) throws FileNotFoundException, IOException {
        MoveDir(src, dest, overwrite, filter, new DefaultFileHandler());
    }

    public static void MoveDir(File src, File dest, boolean overwrite, FileFilter filter, FileHandlerI handler) throws FileNotFoundException, IOException {
        if (!overwrite) {
            final File match = FindFirstMatch(src, dest, filter);
            if (match != null) {
                throw new IOException(match.getName() + " already exists.");
            }
        }

        MoveDirImpl(src, dest, overwrite, filter, handler);
    }

    private static void MoveDirImpl(final File src, final File dest, final boolean overwrite, final FileFilter filter, final FileHandlerI handler) throws FileNotFoundException, IOException {
        boolean moved = false;

        if (!dest.exists()) {
            try {
                moved = src.renameTo(dest);
            } catch (Throwable e) {
                log.error("", e);
            }
        }

        if (!moved) {
            if (!dest.exists()) {
                if (!dest.mkdirs() && !dest.exists()) {
                    log.error("Failed to create the destination folder {} when trying to move \"{}\" to \"{}\". This isn't an exception, so I don't know what went wrong, but probably more stuff will fail later.", dest.getAbsolutePath(), src.getAbsolutePath(), dest.getAbsolutePath());
                }
            }
            if (src != null && src.exists()) {
                final File[] files = src.listFiles();
                if (files != null) {
                    for (final File file : files) {
                        final File dChild = new File(dest, file.getName());
                        if (filter == null || filter.accept(dChild)) {
                            if (file.isDirectory()) {
                                MoveDirImpl(file, dChild, overwrite, filter, handler);
                            } else {
                                try {
                                    MoveFile(file, dChild, overwrite, handler);
                                } catch (FileNotFoundException e) {
                                    log.warn("", e);
                                }
                            }
                        }
                    }
                }

                final File[] recheck = src.listFiles();
                if (recheck == null || recheck.length == 0) {
                    src.delete();
                }
            }
        }
    }

    public static void MoveFile(File src, File dest, boolean overwrite, boolean deleteDIR, FileHandlerI handler) throws FileNotFoundException, IOException {
        boolean moved = false;

        if (!src.exists()) {
            throw new FileNotFoundException(src.getAbsolutePath());
        }

        if (!dest.exists()) {
            try {
                final File destDir = dest.getParentFile();
                if (!destDir.mkdirs() && !destDir.exists()) {
                    log.error("Failed to create the destination folder {} when trying to move \"{}\" to \"{}\". This isn't an exception, so I don't know what went wrong, but probably more stuff will fail later.", destDir.getAbsolutePath(), src.getAbsolutePath(), dest.getAbsolutePath());
                } else {
                    moved = src.renameTo(dest);
                }
            } catch (Throwable e) {
                log.error("", e);
            }
        } else if (!overwrite) {
            return;
        } else {
            if (handler != null) {
                handler.handle(dest);
            } else {
                dest.delete();
            }
        }

        if (!moved) {
            if (dest.exists()) {
                org.apache.commons.io.FileUtils.forceDelete(dest);
            }
            org.apache.commons.io.FileUtils.moveFile(src, dest);
        }

        if (deleteDIR) {
            CleanEmptyDirectories(src.getParentFile());
        }
    }

    public static void MoveFile(File src, File dest, boolean overwrite, boolean deleteDIR) throws FileNotFoundException, IOException {
        MoveFile(src, dest, overwrite, deleteDIR, null);
    }

    public static void MoveFile(File src, File dest, boolean overwrite, FileHandlerI handler) throws FileNotFoundException, IOException {
        MoveFile(src, dest, overwrite, false, handler);
    }

    public static void MoveFile(File src, File dest, boolean overwrite) throws FileNotFoundException, IOException {
        MoveFile(src, dest, overwrite, false, null);
    }

    public interface FileHandlerI {
        boolean handle(File f);
    }

    public static class DefaultFileHandler implements FileHandlerI {
        public boolean handle(File f) {
            return f.delete();
        }
    }

    public static void CleanEmptyDirectories(File level1) throws FileNotFoundException, IOException {
		if(!level1.isDirectory())return;
		if(FileUtils.HasFiles(level1))return;
        File level2 = null;
        File level3 = null;

        if (level1.getParentFile() != null) {
			if(!FileUtils.HasFiles(level1.getParentFile()))
                level2 = level1.getParentFile();
            }

        if (level2 != null && level2.getParentFile() != null) {
			if(!FileUtils.HasFiles(level2.getParentFile()))
                level3 = level2.getParentFile();
            }

        if (level3 != null && level3.getParentFile() != null) {
            if (!FileUtils.HasFiles(level3.getParentFile())) {
                FileUtils.DeleteFile(level3.getParentFile());
                return;
            }
        }

        if (level2 != null && level2.getParentFile() != null) {
            if (!FileUtils.HasFiles(level2.getParentFile())) {
                FileUtils.DeleteFile(level2.getParentFile());
                return;
            }
        }

        if (level1.getParentFile() != null) {
            if (!FileUtils.HasFiles(level1.getParentFile())) {
                FileUtils.DeleteFile(level1.getParentFile());
                return;
            }
        }

        if (!FileUtils.HasFiles(level1)) {
            FileUtils.DeleteFile(level1);
        }
    }

    /**
     * Populates list of files which do not match.
     *
     * @param src
     * @param dest
     *
     * @return Returns a list of String messages about mismatches between the specified files/directories
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<String> CompareFile(File src, File dest) throws FileNotFoundException, IOException {
        return CompareFile(src, dest, null);
    }

    /**
     * Populates list of files which do not match.
     *
     * @param src
     * @param dest
     *
     * @return Returns a list of String messages about mismatches between the specified files/directories
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<String> CompareFile(File src, File dest, List<String> al) throws FileNotFoundException, IOException {
        if (al == null) {
            al = new ArrayList<>();
        }
        if (src.exists() && dest.exists()) {

            if (src.isDirectory() && dest.isDirectory()) {
                final File[] files = src.listFiles();
                if (files != null) {
                    for (final File file : files) {
                        String file_name = file.getName();
                        String destPath  = dest.getAbsolutePath();
                        if (!destPath.endsWith(File.separator)) {
                            destPath += File.separator;
                        }
                        CompareFile(file, new File(destPath + file_name), al);
                    }
                }
            } else if (src.isDirectory()) {
                al.add(dest.getCanonicalPath() + ": Is a file");
            } else if (dest.isDirectory()) {
                al.add(dest.getCanonicalPath() + ": Is a directory");
            } else {
                if (src.length() != dest.length()) {
                    al.add(dest.getCanonicalPath() + ": Size mismatch " + src.length() + ":" + dest.length());
                }
            }
        } else {
            if (!src.exists() && !dest.exists())
            {

            } else if (!src.exists()) {
                al.add(dest.getAbsolutePath() + ": extra file");
            } else {
                al.add(dest.getAbsolutePath() + ": does not exist");
            }
        }

        return al;
    }

    public static void GUnzipFiles(final File file) throws FileNotFoundException, IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files != null) {
                for (final File child : files) {
                    GUnzipFiles(child);
                }
            }
        } else if (file.getName().endsWith(".gz")) {
            try (final InputStream in = new GZIPInputStream(new FileInputStream(file));
                 final FileOutputStream out = new FileOutputStream(new File(file.getParent(), FilenameUtils.removeExtension(file.getName())))) {
                IOUtils.copy(in, out);
            }
            file.delete();
        }
    }

    public static void GZIPFiles(final File file) throws java.io.FileNotFoundException, java.io.IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files != null) {
                for (final File child : files) {
                    GZIPFiles(child);
                }
            }
        } else if (!file.getName().endsWith(".gz")) {
            try (final FileInputStream in = new java.io.FileInputStream(file);
                 final GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(new File(file.getParent(), file.getName() + ".gz")))) {
                IOUtils.copy(in, out);
            }
            file.delete();
        }
    }

    public static void DeleteFile(final File file) {
        log.debug("Got request to delete file {}", file);
        if (!org.apache.commons.io.FileUtils.deleteQuietly(file)) {
            logFailedToDelete(file);
        }
    }

    public static String renameWTimestamp(final String name) {
        return renameWTimestamp(name, new Date());
    }

    public static String renameWTimestamp(final String name, final Date date) {
        return name + "_" + getMsTimestamp(date);
    }

    public static String getTimestamp(final Date date) {
        return TSDIR_SECONDS_FORMATTER.format(date);
    }

    public static String BuildRootHistoryPath() {
        final String cache = XDAT.getSiteConfigPreferences().getCachePath();
		return (StringUtils.isNotBlank(cache) ? StringUtils.appendIfMissing(cache, File.separator) : "/") + ".history/";
    }

    public static File BuildHistoryParentFile(File f) {
        return new File(AppendSlash(BuildRootHistoryPath()) + AppendSlash(RemoveAbsoluteCharacters(f.getParentFile().getAbsolutePath())));
    }

    public static File BuildHistoryFile(final File f, String timestamp) throws FileNotFoundException, IOException {
        return new File(BuildHistoryParentFile(f), (StringUtils.isBlank(timestamp) ? "" : AppendSlash(timestamp)) + f.getName());
    }

    public static File MoveToHistory(final File file, final String timestamp) throws FileNotFoundException, IOException {
        final File dest = BuildHistoryFile(file, timestamp);
		if(file.isDirectory()) {
            FileUtils.MoveDir(file, dest, true);
        } else {
            FileUtils.MoveFile(file, dest, true);
        }
        return dest;
    }

    public static File CopyToHistory(final File f, String timestamp) throws FileNotFoundException, IOException {
        final File dest = BuildHistoryFile(f, timestamp);

		if(f.isDirectory())
            FileUtils.CopyDir(f, dest, true);
		else
            FileUtils.CopyFile(f, dest, true);

        return dest;
    }

    public static void MoveToCache(final File file) throws FileNotFoundException, IOException {
        MoveToCache(file, null);
    }

    public static void MoveToCache(final File file, final String timestamp) throws FileNotFoundException, IOException {
        if (XDAT.getBoolSiteConfigurationProperty("backupDeletedToCache", false)) {
            final Path   cacheRoot   = Paths.get(StringUtils.defaultIfBlank(XDAT.getSiteConfigPreferences().getCachePath(), "/cache"), "DELETED");
            final String subpath     = StringUtils.stripStart(file.getAbsolutePath(), "/");
            final Path   destination = (StringUtils.isNotBlank(timestamp) ? cacheRoot.resolve(timestamp.replace("_", "/")) : getUniqueCacheFolder(cacheRoot, getMsTimestamp())).resolve(subpath);
            if (file.isDirectory()) {
                FileUtils.MoveDir(file, destination.toFile(), true);
            } else {
                FileUtils.MoveFile(file, destination.toFile(), true);
            }
        } else if (file.isDirectory()) {
            org.apache.commons.io.FileUtils.deleteDirectory(file);
        } else {
            file.delete();
        }
    }

    public static String RelativizePath(File parent, File child) {
        if (child.getAbsolutePath().startsWith(parent.getAbsolutePath())) {
            return child.getAbsolutePath().substring(parent.getAbsolutePath().length() + 1);
        }

        final StringBuilder path = new StringBuilder(child.getName());
        File temp = child;
        while (temp.getParentFile() != null) {
            if (temp.getParentFile().getName().equals(parent.getName())) {
                break;
            } else {
                temp = temp.getParentFile();
                path.insert(0, temp.getName() + "/");
            }
        }
        return path.toString();
    }

    public static void ValidateUriAgainstRoot(String s1, String root, String message) throws InvalidValueException {
        final URI rootU;
        try {
            rootU = new URI(root);
        } catch (URISyntaxException e) {
            log.error("The archive path is not a valid URI: " + root, e);
            return;
        }

        if (s1.contains("\\")) {
            s1 = s1.replace('\\', '/');
        }

        log.debug("Validating URI \"{}\" against root URI \"{}\"", s1, rootU);
        ValidateUriAgainstRoot(s1, rootU, message);

        if (FileUtils.IsAbsolutePath(s1)) {
            try {
                new URI(s1).normalize();
            } catch (URISyntaxException e) {
                log.error("Expected Path: " + s1);
                log.error("Current Path: " + root);
                log.error("", e);
                throw new InvalidValueException("Invalid URI: " + s1 + "\nCheck for occurrences of non-standard characters.");
            }
        }
    }

    public static void ValidateUriAgainstRoot(String s1, URI root, String message) throws InvalidValueException {
        URI u;
        if (FileUtils.IsAbsolutePath(s1)) {
            try {
                u = new URI(s1).normalize();
            } catch (URISyntaxException e) {
                log.error("", e);
                throw new InvalidValueException("Invalid URI: " + s1 + "\nCheck for occurrences of non-standard characters.");
            }

            final URI rootU = root.normalize();
            log.debug("Validating URI \"{}\" against root URI \"{}\"", u, rootU);
            final URI relativized = rootU.relativize(u);
            if (relativized.equals(u)) {
                log.error("Testing URI \"{}\" against normalized root URI \"{}\" gives relative path from root is \"{}\", apparently that's not good enough", u, rootU, relativized);
                throw new InvalidValueException(message);
            }
        }
    }

    public static void deleteQuietly(File src) {
        if (src.exists()) {
            org.apache.commons.io.FileUtils.deleteQuietly(src);
        }
    }

    public static void deleteDirQuietly(File s) {
        if (s.exists()) {
            try {
                org.apache.commons.io.FileUtils.deleteDirectory(s);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Creates a function that maps files from the root folder specified in <pre>origin</pre> to the root folder specified
     * in <pre>destination</pre>. Any files that aren't located under <pre>origin</pre> are not modified. This method
     * converts the incoming file parameters to paths then uses {@link #fileRootMapper(Path, Path)} to generate the
     * function.
     *
     * @param origin      The origin root folder.
     * @param destination The destination root folder.
     *
     * @return A function that can be used to map a stream of files from one root folder to another.
     * @see #fileRootMapper(Path, Path)
     */
    public static Function<File, File> fileRootMapper(final File origin, final File destination) {
        return fileRootMapper(origin.toPath(), destination.toPath());
    }

    /**
     * Creates a function that maps files from the root folder specified in <pre>origin</pre> to the root folder specified
     * in <pre>destination</pre>. Any files that aren't located under <pre>origin</pre> are not modified.
     *
     * @param origin      The origin root folder.
     * @param destination The destination root folder.
     *
     * @return A function that can be used to map a stream of files from one root folder to another.
     * @see #fileRootMapper(Path, Path)
     */
    public static Function<File, File> fileRootMapper(final Path origin, final Path destination) {
        return path -> path.toPath().startsWith(origin) ? destination.resolve(origin.relativize(path.toPath())).toFile() : path;
    }

    /**
     * Creates a function that maps files from the root folder specified in <pre>origin</pre> to the root folder specified
     * in <pre>destination</pre>. Any files that aren't located under <pre>origin</pre> are not modified. This method
     * converts the incoming file parameters to paths then uses {@link #fileRootMapper(Path, Path)} to generate the
     * function.
     *
     * @param origin      The origin root folder.
     * @param destination The destination root folder.
     *
     * @return A function that can be used to map a stream of files from one root folder to another.
     * @see #fileRootMapper(Path, Path)
     */
    public static Function<Path, Path> pathRootMapper(final File origin, final File destination) {
        return pathRootMapper(origin.toPath(), destination.toPath());
    }

    /**
     * Creates a function that maps files from the root folder specified in <pre>origin</pre> to the root folder specified
     * in <pre>destination</pre>. Any files that aren't located under <pre>origin</pre> are not modified.
     *
     * @param origin      The origin root folder.
     * @param destination The destination root folder.
     *
     * @return A function that can be used to map a stream of files from one root folder to another.
     * @see #fileRootMapper(Path, Path)
     */
    public static Function<Path, Path> pathRootMapper(final Path origin, final Path destination) {
        return path -> path.startsWith(origin) ? destination.resolve(origin.relativize(path)) : path;
    }

    private static void logFailedToDelete(final File f) {
        if (log.isDebugEnabled()) {
            final StringBuilder       buffer = new StringBuilder("Failed to delete: ").append(f.getAbsolutePath()).append("\n");
            final StackTraceElement[] layers = Thread.currentThread().getStackTrace();
            for (final StackTraceElement layer : layers) {
                final String present = layer.toString();
                // Filter out the getStackTrace call and call to this method...
                if (!present.contains("getStackTrace") && !present.contains("logFailedToDelete")) {
                    buffer.append("   at ").append(layer).append("\n");
                }
            }
            log.warn(buffer.toString());
        } else {
            log.warn("Failed to delete: {}", f.getAbsolutePath());
        }
    }

    private static Path getUniqueCacheFolder(final Path root, final String timestamp) {
        final String converted = timestamp.replace("_", "/");
        final Path defaultFolder = root.resolve(converted);
        if (!defaultFolder.toFile().exists()) {
            return defaultFolder;
        }
        final AtomicInteger index = new AtomicInteger(1);
        while (true) {
            final Path indexed = root.resolve(converted + "-" + index.getAndIncrement());
            if (!indexed.toFile().exists()) {
                return indexed;
            }
        }
    }
}
