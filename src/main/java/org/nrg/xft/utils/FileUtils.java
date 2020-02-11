/*
 * core: org.nrg.xft.utils.FileUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFTTool;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@SuppressWarnings({"RedundantThrows", "DuplicateThrows"})
public  class FileUtils
{
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static final int LARGE_DOWNLOAD = 1000 * 1024;
    public static final int SMALL_DOWNLOAD = 8 * 1024;

    public static String ReadFromFile(String path) throws IOException {
        return ReadFromFile(new File(path));
    }

    public static String ReadFromFile(File file) throws IOException {
        try (final FileInputStream stream = new FileInputStream(file)) {
            FileChannel      channel = stream.getChannel();
            MappedByteBuffer buffer  = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            return Charset.defaultCharset().decode(buffer).toString();
        }
    }

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
            logger.error("FileUtils::OutputToFile", except);
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
            logger.error("FileUtils::OutputToFile", except);
        }
    }
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
            logger.error("FileUtils::OutputToFile", except);
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
            logger.error("FileUtils::OutputToFile", except);
        }
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
            logger.error("", e);
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
        return children != null && Iterables.any(Arrays.asList(children), Predicates.containsPattern(folderName));
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
            logger.error("", e);
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
            logger.error("", e);
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
            logger.error("", e);
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

    public static String AppendRootPath(String root,String local)
    {
        if (root==null || root.equals(""))
        {
            return local;
        } else {
            if (IsAbsolutePath(local))
            {
                return local;
            }

            while (local.startsWith("\\") || local.startsWith("/"))
            {
                local = local.substring(1);
            }

            root = AppendSlash(root);

            return root + local;
        }
    }

    public static String AppendSlash(String root)
    {
        if (root==null || root.equals(""))
        {
            return null;
        } else {

            if (!root.endsWith("/") && !root.endsWith("\\")) {
                root += File.separator;
            }

            return root;
        }
    }

    public static boolean IsAbsolutePath(final String path) {
        if (StringUtils.startsWithAny(path, "file:", "http:", "https:", "srb:")) {
            return true;
        }
        if (File.separator.equals("/")) {
            return path.startsWith("/");
        }
        return path.contains(":\\") || path.contains(":/");
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
     * @param file
     *
     * @return ArrayList of Strings (one string for each line in the file)
     */
    public static List<String> FileLinesToArrayList(final File file) throws FileNotFoundException, IOException {
        return IOUtils.readLines(new FileInputStream(file), Charset.defaultCharset());
    }

    public static List<List<String>> CSVFileToArrayList(File f) throws FileNotFoundException, IOException {
        ArrayList<List<String>> all  = new ArrayList<>();
        List<String>            rows = FileLinesToArrayList(f);
        for (String row : rows)
        {
            all.add(XftStringUtils.CommaDelimitedStringToArrayList(row));
        }
        all.trimToSize();
        return all;
    }

    public static int CountFiles(File src, boolean stopAt1) {
        if (src.isDirectory()) {
            File[] files = src.listFiles();
            int    count = 0;

            if (files == null) {
                return count;
            }

            for (final File file : files) {
                if (file.isDirectory()) {
                    int sub_count = CountFiles(file, stopAt1);
                    if (sub_count > 0 && stopAt1) {
                        return 1;
                    } else {
                        count += sub_count;
                    }
                } else {
                    if (stopAt1) {
                        return 1;
                    } else {
                        count++;
                    }
                }
            }

            return count;
        } else {
            return 1;
        }
    }

    public static boolean HasFiles(File src) {
        if (src.isDirectory()) {
            File[] files = src.listFiles();

            if (files == null) {
                return false;
            }

            for (final File file : files) {
                if (file.isDirectory()) {
                    if (HasFiles(file)) {
                        return true;
                    }
                } else return true;
            }

            return false;
        } else {
            return true;
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
    public static File FindFirstMatch(final File source, final File destination, final FileFilter filter) {
        if (source == null) {
            return null;
        }
        final File[] files = source.listFiles();
        if (files == null) {
            return null;
        }
        for (final File file : files) {
            final File target = (new File(destination, file.getName()));
            if (file.isDirectory()) {
                final File match = FindFirstMatch(file, target, filter);
                if (match != null) {
                    return match;
                }
            } else if (filter == null || filter.accept(file)) {
                if (target.exists()) {
                    return target;
                }
            }
        }

        return null;
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
                if (file.isDirectory())
                {
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
                logger.error("", e);
            }
        }

        if (!moved) {
            if (!dest.exists()) {
                if (!dest.mkdirs() && !dest.exists()) {
                    logger.error("Failed to create the destination folder {} when trying to move \"{}\" to \"{}\". This isn't an exception, so I don't know what went wrong, but probably more stuff will fail later.", dest.getAbsolutePath(), src.getAbsolutePath(), dest.getAbsolutePath());
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
                                    logger.warn("", e);
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
                    logger.error("Failed to create the destination folder {} when trying to move \"{}\" to \"{}\". This isn't an exception, so I don't know what went wrong, but probably more stuff will fail later.", destDir.getAbsolutePath(), src.getAbsolutePath(), dest.getAbsolutePath());
                } else {
                    moved = src.renameTo(dest);
                }
            } catch (Throwable e) {
                logger.error("", e);
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
        logger.debug("Got request to delete file {}", file);
        if (!org.apache.commons.io.FileUtils.deleteQuietly(file)) {
            logFailedToDelete(file);
        }
    }

    public static String renameWTimestamp(final String n, Date d) {
        return n + "_" + getTimestamp(d);
    }

    public static String getTimestamp(Date d) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        return sdf.format(d);
    }

    public static String BuildRootHistoryPath() {
        String cache = XDAT.getSiteConfigPreferences().getCachePath();

		if(StringUtils.isBlank(cache))
		{
            return "/.history/";
        } else {
            return AppendSlash(cache) + ".history/";
        }
    }

    public static File BuildHistoryParentFile(File f) {
        return new File(AppendSlash(BuildRootHistoryPath()) + AppendSlash(RemoveAbsoluteCharacters(f.getParentFile().getAbsolutePath())));
    }

    public static File BuildHistoryFile(final File f, String timestamp) throws FileNotFoundException, IOException {
        return new File(BuildHistoryParentFile(f), (StringUtils.isBlank(timestamp) ? "" : AppendSlash(timestamp)) + f.getName());
    }

    public static File MoveToHistory(final File f, String timestamp) throws FileNotFoundException, IOException {
        final File dest = BuildHistoryFile(f, timestamp);

		if(f.isDirectory())
            FileUtils.MoveDir(f, dest, true);
		else
            FileUtils.MoveFile(f, dest, true);

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

    public static void MoveToCache(File f) throws FileNotFoundException, IOException {
        if (XDAT.getBoolSiteConfigurationProperty("files.allow_move_to_cache", true)) {
            String cache = XDAT.getSiteConfigPreferences().getCachePath();
			if(cache.equals(""))
			{
                cache = "/cache/";
            }
            cache = AppendSlash(cache) + "DELETED";

            cache = AppendSlash(cache) + getTimestamp(Calendar.getInstance().getTime());

            String path = f.getAbsolutePath();

            String dest = AppendSlash(cache) + RemoveAbsoluteCharacters(path);

			if(f.isDirectory())
                FileUtils.MoveDir(f, new File(dest), true);
			else
                FileUtils.MoveFile(f, new File(dest), true);
        } else {
            if (f.isDirectory()) {
                org.apache.commons.io.FileUtils.deleteDirectory(f);
            } else {
                f.delete();
            }
        }
    }

    public static String RelativizePath(File parent, File child) {
        if (child.getAbsolutePath().startsWith(parent.getAbsolutePath())) {
            return child.getAbsolutePath().substring(parent.getAbsolutePath().length() + 1);
        }
        String path = child.getName();

        File temp = child;

        while (temp.getParentFile() != null) {
            if (temp.getParentFile().getName().equals(parent.getName())) {
                break;
            } else {
                temp = temp.getParentFile();
                path = temp.getName() + "/" + path;
            }
        }

        return path;
    }

    public static void ValidateUriAgainstRoot(String s1, String root, String message) throws InvalidValueException {
        final URI rootU;
        try {
            rootU = new URI(root);
        } catch (URISyntaxException e) {
            logger.error("The archive path is not a valid URI: " + root, e);
            return;
        }

        if (s1.contains("\\")) {
            s1 = s1.replace('\\', '/');
        }

        logger.debug("Validating URI \"{}\" against root URI \"{}\"", s1, rootU);
        ValidateUriAgainstRoot(s1, rootU, message);

        if (FileUtils.IsAbsolutePath(s1)) {
            try {
                new URI(s1).normalize();
            } catch (URISyntaxException e) {
                logger.error("Expected Path: " + s1);
                logger.error("Current Path: " + root);
                logger.error("", e);
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
                logger.error("", e);
                throw new InvalidValueException("Invalid URI: " + s1 + "\nCheck for occurrences of non-standard characters.");
            }

            final URI rootU = root.normalize();
            logger.debug("Validating URI \"{}\" against root URI \"{}\"", u, rootU);
            if (rootU.relativize(u).equals(u)) {
                logger.error("The relative path from root is \"{}\", apparently that's not good enough", rootU.relativize(u));
                throw new InvalidValueException(message);
            }
        }
    }
    public static void deleteQuietly(File src) {
		if(src.exists())org.apache.commons.io.FileUtils.deleteQuietly(src);
    }
    public static void deleteDirQuietly(File s) {
		if(s.exists())
            try {
                org.apache.commons.io.FileUtils.deleteDirectory(s);
            } catch (IOException ignored) {
            }
        }

    private static void logFailedToDelete(final File f) {
        if (logger.isDebugEnabled()) {
            final StringBuilder       buffer = new StringBuilder("Failed to delete: ").append(f.getAbsolutePath()).append("\n");
            final StackTraceElement[] layers = Thread.currentThread().getStackTrace();
            for (final StackTraceElement layer : layers) {
                final String present = layer.toString();
                // Filter out the getStackTrace call and call to this method...
                if (!present.contains("getStackTrace") && !present.contains("logFailedToDelete")) {
                    buffer.append("   at ").append(layer).append("\n");
                }
            }
            logger.warn(buffer.toString());
        } else {
            logger.warn("Failed to delete: {}", f.getAbsolutePath());
        }
    }
}

