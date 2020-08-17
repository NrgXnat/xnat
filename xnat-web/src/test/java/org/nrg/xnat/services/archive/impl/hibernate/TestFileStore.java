package org.nrg.xnat.services.archive.impl.hibernate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnat.config.TestFileStoreConfig;
import org.nrg.xnat.entities.FileStoreInfo;
import org.nrg.xnat.preferences.FileStorePreferences;
import org.nrg.xnat.services.archive.FileStore;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@PowerMockIgnore({"org.apache.*", "java.*", "javax.*", "org.w3c.*", "com.sun.*", "org.xml.sax.*"})
@ContextConfiguration(classes = TestFileStoreConfig.class)
public class TestFileStore {
    @After
    public void cleanup() throws IOException {
        FileUtils.deleteDirectory(Paths.get(_preferences.getFileStorePath()).toFile());
    }

    @Test
    public void testBaseFileStoreOperations() {
        final String joined = HibernateFileStoreService.joinCoordinates(COORDS_1234);
        assertThat(joined).isEqualTo(JOINED_1234);
        final String joinedSha256 = HibernateFileStoreService.getHashedPath(joined);
        assertThat(joinedSha256).isEqualTo(JOINED_1234_SHA_256);
        final String[] splitSha256 = HibernateFileStoreService.splitHashedPath(joinedSha256);
        assertThat(splitSha256).containsExactly(SPLIT_1234_SHA_256);
        final Path fileStorePath     = HibernateFileStoreService.getFileStorePath(splitSha256);
        final Path easyFileStorePath = HibernateFileStoreService.getFileStorePath(joined);
        assertThat(fileStorePath).isEqualTo(easyFileStorePath).isEqualTo(SPLIT_1234_SHA_256_PATH);
    }

    @Test
    public void testFileStoreCreate() throws IOException, ResourceAlreadyExistsException {
        final URI           fullPath = Paths.get(_preferences.getFileStorePath(), SPLIT_1234_SHA_256).toUri();
        final Resource      dicom    = BasicXnatResourceLocator.getResource(DICOM_4_1_URI);
        final FileStoreInfo info     = _fileStore.create(new FileInputStream(dicom.getFile()), COORDS_1234);
        assertThat(info).isNotNull()
                        .hasFieldOrPropertyWithValue("checksum", DICOM_4_1_SHA_256)
                        .hasFieldOrPropertyWithValue("size", DICOM_4_1_SIZE)
                        .hasFieldOrPropertyWithValue("storeUri", fullPath)
                        .hasFieldOrPropertyWithValue("coordinates", JOINED_1234);
        assertThat(Paths.get(fullPath).toFile()).exists();
    }

    @Test
    public void testFileStoreUpdate() throws IOException, ResourceAlreadyExistsException, NotFoundException {
        final URI           fullPath = Paths.get(_preferences.getFileStorePath(), SPLIT_ABCD_SHA_256).toUri();
        final Resource      dicom41  = BasicXnatResourceLocator.getResource(DICOM_4_1_URI);
        final Resource      dicom42  = BasicXnatResourceLocator.getResource(DICOM_4_2_URI);
        final FileStoreInfo info41   = _fileStore.create(new FileInputStream(dicom41.getFile()), COORDS_ABCD);
        assertThat(info41).isNotNull()
                          .hasFieldOrPropertyWithValue("checksum", DICOM_4_1_SHA_256)
                          .hasFieldOrPropertyWithValue("size", DICOM_4_1_SIZE)
                          .hasFieldOrPropertyWithValue("storeUri", fullPath)
                          .hasFieldOrPropertyWithValue("coordinates", JOINED_ABCD);
        assertThat(Paths.get(fullPath).toFile()).exists();
        final FileStoreInfo info42 = _fileStore.update(new FileInputStream(dicom42.getFile()), COORDS_ABCD);
        assertThat(info42).isNotNull()
                          .hasFieldOrPropertyWithValue("checksum", DICOM_4_2_SHA_256)
                          .hasFieldOrPropertyWithValue("size", DICOM_4_2_SIZE)
                          .hasFieldOrPropertyWithValue("storeUri", fullPath)
                          .hasFieldOrPropertyWithValue("coordinates", JOINED_ABCD);
    }

    @Test(expected = NotFoundException.class)
    public void testFileStoreDelete() throws IOException, ResourceAlreadyExistsException, NotFoundException {
        final URI           fullPath = Paths.get(_preferences.getFileStorePath(), SPLIT_DCBA_SHA_256).toUri();
        final Resource      dicom41  = BasicXnatResourceLocator.getResource(DICOM_4_1_URI);
        final Resource      dicom42  = BasicXnatResourceLocator.getResource(DICOM_4_2_URI);
        final FileStoreInfo info41   = _fileStore.create(new FileInputStream(dicom41.getFile()), COORDS_DCBA);
        final long          info41Id = info41.getId();
        assertThat(info41).isNotNull()
                          .hasFieldOrPropertyWithValue("checksum", DICOM_4_1_SHA_256)
                          .hasFieldOrPropertyWithValue("size", DICOM_4_1_SIZE)
                          .hasFieldOrPropertyWithValue("storeUri", fullPath)
                          .hasFieldOrPropertyWithValue("coordinates", JOINED_DCBA);
        assertThat(Paths.get(fullPath).toFile()).exists();
        final FileStoreInfo info42;
        try {
            info42 = _fileStore.update(new FileInputStream(dicom42.getFile()), COORDS_DCBA);
        } catch (NotFoundException e) {
            fail("Got not found exception for coordinates \"" + JOINED_DCBA + "\" but this should exist.");
            return;
        }
        final long          info42Id = info41.getId();
        assertThat(info42).isNotNull()
                          .hasFieldOrPropertyWithValue("id", info41Id)
                          .hasFieldOrPropertyWithValue("checksum", DICOM_4_2_SHA_256)
                          .hasFieldOrPropertyWithValue("size", DICOM_4_2_SIZE)
                          .hasFieldOrPropertyWithValue("storeUri", fullPath)
                          .hasFieldOrPropertyWithValue("coordinates", JOINED_DCBA);
        final Path outputFile = Files.createTempFile(Paths.get(_preferences.getFileStorePath()), "testRetrieve-", ".dcm");
        try {
            try (final InputStream input = _fileStore.open(COORDS_DCBA);
                 final OutputStream output = new FileOutputStream(outputFile.toFile())) {
                IOUtils.copy(input, output);
                assertThat(outputFile.toFile()).exists().hasSize(DICOM_4_2_SIZE);
            }
        } catch (NotFoundException e) {
            fail("Got not found exception for coordinates \"" + JOINED_DCBA + "\" but this should exist.");
            return;
        }
        try {
            _fileStore.delete(COORDS_DCBA);
        } catch (NotFoundException e) {
            fail("Got not found exception for coordinates \"" + JOINED_DCBA + "\" but this should exist.");
            return;
        }
        _fileStore.getFileStoreInfo(info42Id);
    }

    private static final String[] COORDS_1234             = {"one", "two", "three", "four"};
    private static final String   JOINED_1234             = StringUtils.join(COORDS_1234, "/");
    private static final String   JOINED_1234_SHA_256     = "F484118D335B431419FE274323802A311080EE08A1B1405E83251F0CB19B2B9F";
    private static final String[] SPLIT_1234_SHA_256      = {"F484118D", "335B4314", "19FE2743", "23802A31", "1080EE08", "A1B1405E", "83251F0C", "B19B2B9F"};
    private static final Path     SPLIT_1234_SHA_256_PATH = Paths.get("F484118D", "335B4314", "19FE2743", "23802A31", "1080EE08", "A1B1405E", "83251F0C", "B19B2B9F");
    private static final String[] COORDS_ABCD             = {"A", "B", "C", "D"};
    private static final String   JOINED_ABCD             = StringUtils.join(COORDS_ABCD, "/");
    private static final String[] SPLIT_ABCD_SHA_256      = {"137AE21D", "93E40B8C", "26F1A284", "61A9A6C6", "789420F7", "36991601", "F7935AE7", "646DC69B"};
    private static final String[] COORDS_DCBA             = {"D", "C", "B", "A"};
    private static final String   JOINED_DCBA             = StringUtils.join(COORDS_DCBA, "/");
    private static final String[] SPLIT_DCBA_SHA_256      = {"4F3857DE", "478F00C3", "545AC6B5", "9E03A163", "34D3F2BB", "62CB09E1", "3A9DC70C", "D0C3677C"};
    private static final String   DICOM_4_1_URI           = "classpath:dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm";
    private static final String   DICOM_4_1_SHA_256       = "31ACB2E88909B60309B0167AD0B44F00628A9240D05115ABEC6037035BA48F31";
    private static final long     DICOM_4_1_SIZE          = 191904;
    private static final String   DICOM_4_2_URI           = "classpath:dicom/1.MR.head_DHead.4.2.20061214.091206.156000.0918517980.dcm";
    private static final String   DICOM_4_2_SHA_256       = "D853B36A608CC333C8667410FEF6B12F648FDB0467EF788E526206D9C82009B6";
    private static final long     DICOM_4_2_SIZE          = 191864;

    @Autowired
    private FileStorePreferences _preferences;

    @Autowired
    private FileStore _fileStore;
}
