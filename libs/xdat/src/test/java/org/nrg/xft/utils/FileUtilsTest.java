/*
 * core: org.nrg.xft.utils.FileUtilsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FileUtilsTest {
    private static final String     CSV_PATH               = "classpath:org/nrg/xft/utils/import.csv";
    private static final String     BOM_UTF_8              = "\uFEFF";
    private static final String     ACCESSION_NUMBER       = "Accession Number";
    private static final String     UTF_8_ACCESSION_NUMBER = BOM_UTF_8 + ACCESSION_NUMBER;
    private static final Path       ORIGIN_PATH            = Paths.get("/this/is/the/origin");
    private static final Path       DESTINATION_PATH       = Paths.get("/this/is/the/destination");
    private static final Path       UNRELATED_PATH         = Paths.get("/this/one/is/unrelated");
    private static final List<Path> STARTING_PATHS         = Arrays.asList(ORIGIN_PATH.resolve("path/one"), ORIGIN_PATH.resolve("path/two"), ORIGIN_PATH.resolve("path/three"), ORIGIN_PATH.resolve("path/four"), ORIGIN_PATH.resolve("path/five"), UNRELATED_PATH.resolve("path/six"));
    private static final List<Path> ENDING_PATHS           = Arrays.asList(DESTINATION_PATH.resolve("path/one"), DESTINATION_PATH.resolve("path/two"), DESTINATION_PATH.resolve("path/three"), DESTINATION_PATH.resolve("path/four"), DESTINATION_PATH.resolve("path/five"), UNRELATED_PATH.resolve("path/six"));
    private static final List<File> STARTING_FILES         = STARTING_PATHS.stream().map(Path::toFile).collect(Collectors.toList());
    private static final List<File> ENDING_FILES           = ENDING_PATHS.stream().map(Path::toFile).collect(Collectors.toList());

    final File src  = new File("./FileUtilsSrcTest");
    final File dest = new File("./FileUtilsDestTest");

    @After
    public void tearDown() throws Exception {
        deleteDirNoException(src);
        deleteDirNoException(dest);
    }

    public void deleteDirNoException(File s) throws Exception {
        if (s.exists()) {
            org.apache.commons.io.FileUtils.deleteDirectory(s);
        }
    }

    private static final String[] c = new String[]{"AA", "BB", "CC", "DD", "EE", "FF", "GG", "HH"};
    private static final String[] f = new String[]{"TEST1.txt", "TEST2.txt", "TEST3.txt", "TEST4.txt", "TEST5.txt", "TEST6.txt", "TEST7.txt", "TEST8.txt"};

    public File createFile(File dir, String name, String content) throws IOException {
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File f = new File(dir, name);
        org.apache.commons.io.FileUtils.writeStringToFile(new File(dir, name), content, Charset.defaultCharset());
        return f;
    }

    public String get(File dir, String name) throws IOException {
        return org.apache.commons.io.FileUtils.readFileToString(new File(dir, name), Charset.defaultCharset());
    }

    @Test
    public void testCSVFileToArrayList() throws IOException {
        final Resource csv = BasicXnatResourceLocator.getResource(CSV_PATH);
        if (!csv.exists()) {
            fail("Couldn't load the import CSV");
        }

        final File               file         = csv.getFile();
        final List<List<String>> untranslated = getUntranslatedCsvFileToList(file);
        final List<List<String>> translated   = FileUtils.CSVFileToArrayList(file);
        assertThat(untranslated).isNotNull().isNotEmpty().hasSize(3).allMatch(row -> row != null && !row.isEmpty());
        assertThat(translated).isNotNull().isNotEmpty().hasSize(3).allMatch(row -> row != null && !row.isEmpty());

        final String untranslatedHeader = untranslated.get(0).get(0);
        assertThat(untranslatedHeader).isNotBlank().isNotEqualTo(ACCESSION_NUMBER).isEqualTo(UTF_8_ACCESSION_NUMBER);
        final String translatedHeader = translated.get(0).get(0);
        assertThat(translatedHeader).isNotBlank().isNotEqualTo(UTF_8_ACCESSION_NUMBER).isEqualTo(ACCESSION_NUMBER);
    }

    @Test
    public void testMoveSuccessT() throws Exception {
        final File file1 = createFile(src, f[0], c[0]);

        FileUtils.MoveDir(src, dest, true);

        Assert.assertFalse((file1.exists()));

        Assert.assertEquals(c[0], get(dest, f[0]));
    }

    @Test
    public void testCopySuccessT() throws Exception {
        final File file1 = createFile(src, f[0], c[0]);

        FileUtils.CopyDir(src, dest, true);

        Assert.assertTrue((file1.exists()));
        Assert.assertEquals(get(src, f[0]), get(dest, f[0]));
    }

    @Test
    public void testMoveSuccessF() throws Exception {
        final File file1 = createFile(src, f[0], c[0]);

        FileUtils.MoveDir(src, dest, false);

        Assert.assertFalse((file1.exists()));

        Assert.assertEquals(c[0], get(dest, f[0]));
    }

    @Test
    public void testCopySuccessF() throws Exception {
        final File file1 = createFile(src, f[0], c[0]);

        FileUtils.CopyDir(src, dest, false);

        Assert.assertTrue((file1.exists()));
        Assert.assertEquals(get(src, f[0]), get(dest, f[0]));
    }


    @Test
    public void testCopyOverwriteF() throws Exception {
        createFile(src, f[0], c[0]);
        createFile(src, "level1/" + f[1], c[1]);

        createFile(dest, "level1/" + f[1], c[2]);

        try {
            FileUtils.CopyDir(src, dest, false);

            fail("Should have thrown an exception");
        } catch (IOException e) {
            assertEquals(e.getMessage(), "TEST2.txt already exists.");
        }

        Assert.assertEquals(get(src, f[0]), c[0]);
        Assert.assertEquals(get(src, "level1/" + f[1]), c[1]);
        Assert.assertEquals(get(dest, "level1/" + f[1]), c[2]);
    }


    @Test
    public void testCopyOverwriteT() throws Exception {
        createFile(src, f[0], c[0]);
        createFile(src, "level1/" + f[1], c[1]);

        createFile(dest, "level1/" + f[1], c[2]);

        FileUtils.CopyDir(src, dest, true);

        Assert.assertEquals(get(dest, "level1/" + f[1]), c[1]);
    }


    @Test
    public void testMoveOverwriteF() throws Exception {
        createFile(src, f[0], c[0]);
        createFile(src, "level1/" + f[1], c[1]);

        createFile(dest, "level1/" + f[1], c[2]);

        try {
            FileUtils.MoveDir(src, dest, false);

            fail("Should have thrown an exception");
        } catch (IOException e) {
            assertEquals(e.getMessage(), "TEST2.txt already exists.");
        }

        Assert.assertEquals(get(src, f[0]), c[0]);
        Assert.assertEquals(get(src, "level1/" + f[1]), c[1]);
        Assert.assertEquals(get(dest, "level1/" + f[1]), c[2]);
    }


    @Test
    public void testMoveOverwriteT() throws Exception {
        createFile(src, f[0], c[0]);
        createFile(src, "level1/" + f[1], c[1]);

        createFile(dest, "level1/" + f[1], c[2]);

        FileUtils.MoveDir(src, dest, true);

        Assert.assertEquals(get(dest, "level1/" + f[1]), c[1]);
    }

    @Test
    public void testRootMappers() {
        final Function<Path, Path> mapPathsFromPathToPath = FileUtils.pathRootMapper(ORIGIN_PATH, DESTINATION_PATH);
        final Function<Path, Path> mapPathsFromFileToFile = FileUtils.pathRootMapper(ORIGIN_PATH.toFile(), DESTINATION_PATH.toFile());
        final Function<File, File> mapFilesFromPathToPath = FileUtils.fileRootMapper(ORIGIN_PATH, DESTINATION_PATH);
        final Function<File, File> mapFilesFromFileToFile = FileUtils.fileRootMapper(ORIGIN_PATH.toFile(), DESTINATION_PATH.toFile());

        final List<Path> pathsFromPathToPath = STARTING_PATHS.stream().map(mapPathsFromPathToPath).collect(Collectors.toList());
        final List<Path> pathsFromFileToFile = STARTING_PATHS.stream().map(mapPathsFromFileToFile).collect(Collectors.toList());
        final List<File> filesFromPathToPath = STARTING_FILES.stream().map(mapFilesFromPathToPath).collect(Collectors.toList());
        final List<File> filesFromFileToFile = STARTING_FILES.stream().map(mapFilesFromFileToFile).collect(Collectors.toList());

        assertThat(pathsFromPathToPath).isNotNull().isNotEmpty().hasSize(ENDING_PATHS.size()).containsExactlyElementsOf(ENDING_PATHS);
        assertThat(pathsFromFileToFile).isNotNull().isNotEmpty().hasSize(ENDING_PATHS.size()).containsExactlyElementsOf(ENDING_PATHS);
        assertThat(filesFromPathToPath).isNotNull().isNotEmpty().hasSize(ENDING_FILES.size()).containsExactlyElementsOf(ENDING_FILES);
        assertThat(filesFromFileToFile).isNotNull().isNotEmpty().hasSize(ENDING_FILES.size()).containsExactlyElementsOf(ENDING_FILES);
    }

    private static List<List<String>> getUntranslatedCsvFileToList(final File file) throws IOException {
        try (final InputStream input = Files.newInputStream(file.toPath())) {
            return IOUtils.readLines(input, Charset.defaultCharset()).stream().map(XftStringUtils::CommaDelimitedStringToArrayList).collect(Collectors.toList());
        }
    }
}
