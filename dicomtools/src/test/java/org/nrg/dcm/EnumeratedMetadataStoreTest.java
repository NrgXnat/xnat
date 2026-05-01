/*
 * DicomDB: org.nrg.dcm.EnumeratedMetadataStoreTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.assertj.core.data.MapEntry;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.net.TransferCapability;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.attr.ConversionFailureException;
import org.nrg.dicomtools.utilities.DicomUtils;
import org.nrg.util.FileURIOpener;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class EnumeratedMetadataStoreTest {
    private static final String[] fileNames = {
            "1.MR.head_DHead.4.3.20061214.091206.156000.0506717986.dcm.gz",
            "1.MR.head_DHead.4.11.20061214.091206.156000.8007818018.dcm.gz",
            "1.MR.head_DHead.4.12.20061214.091206.156000.6027118028.dcm.gz",
            "1.MR.head_DHead.5.130.20061214.091206.156000.3643619204.dcm.gz",
            "1.MR.head_DHead.5.131.20061214.091206.156000.9657019208.dcm.gz"
    };

    private static final String[] fileNamesSeries5 = {
            "1.MR.head_DHead.5.130.20061214.091206.156000.3643619204.dcm.gz",
            "1.MR.head_DHead.5.131.20061214.091206.156000.9657019208.dcm.gz"
    };
    private static final String[] otherFileNames = {
            "1.MR.head_DHead.6.130.20061214.091206.156000.8530419915.dcm.gz",
            "1.MR.head_DHead.6.140.20061214.091206.156000.9129119951.dcm.gz",
    };

    private static final DicomAttributeIndex SERIES_NUMBER = new FixedDicomAttributeIndex(Tag.SeriesNumber);
    private static final DicomAttributeIndex INSTANCE_NUMBER = new FixedDicomAttributeIndex(Tag.InstanceNumber);
    private static final DicomAttributeIndex STUDY_INSTANCE_UID = new FixedDicomAttributeIndex(Tag.StudyInstanceUID);
    private static final DicomAttributeIndex SERIES_INSTANCE_UID = new FixedDicomAttributeIndex(Tag.SeriesInstanceUID);

    private static final Collection<DicomAttributeIndex> TAGS =
            ImmutableList.of(SERIES_NUMBER, INSTANCE_NUMBER, STUDY_INSTANCE_UID, SERIES_INSTANCE_UID);

    private Project project;
    private File tempDir;
    private List<URI> resources, otherResources;

    private static URI toURI(final File folder, final String file) throws URISyntaxException {
        return DicomUtils.getQualifiedUri(Paths.get(folder.getAbsolutePath(), file).toString());
    }

    private static URI toURI(final File f) {
        try {
            return DicomUtils.getQualifiedUri(f.getPath());
        } catch (URISyntaxException e) {
            return f.toURI();
        }
    }

    private URI toURI(final String name) throws URISyntaxException {
        return toURI(name, "dicom");
    }

    private URI toURI(final String name, String subdir) throws URISyntaxException {
        final URL url = getClass().getClassLoader().getResource(subdir + "/" + name);
        assertNotNull(url);
        return url.toURI();
    }

    @Before
    public void setUp() throws IOException, URISyntaxException {
        final File temp = File.createTempFile("EnumeratedMetadataStore", "test").getCanonicalFile();
        temp.delete();
        temp.mkdirs();
        if (!temp.isDirectory()) {
            throw new IOException("Unable to create temporary directory " + temp);
        }

        assert null == project;
        project = new Project();
        project.setBaseDir(temp);

        final Copy copy = new Copy();
        copy.setProject(project);
        copy.setTodir(temp);
        for (final String name : fileNames) {
            copy.setFile(new File(toURI(name)));
            copy.execute();
        }

        resources = Arrays.stream(fileNames).map(name -> {
            try {
                return toURI(temp, name);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        otherResources = Arrays.stream(otherFileNames).map(name -> {
            try {
                return toURI(name);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        assert null == tempDir;
        tempDir = temp;
    }

    @After
    public void tearDown() {
        final Delete delete = new Delete();
        delete.setProject(project);
        delete.setDir(tempDir);
        delete.execute();
        tempDir.delete();
        tempDir = null;
        project = null;
    }

    @Test
    public void testAddFileDicomObject() throws IOException, SQLException, URISyntaxException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());

        final URI uri = toURI(otherFileNames[0]);
        final DicomObject o = DicomUtils.read(new File(uri), Math.max(Tag.StudyInstanceUID, Tag.SeriesInstanceUID));
        store.add(uri, o);
        assertEquals(1, store.getSize());
        assertTrue(store.getResources().contains(uri));
    }

    @Test
    public void testAddIterableOfFileProgressMonitorI()
            throws IOException, SQLException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());

        store.add(otherResources);         // TODO: actually test the progress monitor
        assertEquals(otherFileNames.length, store.getSize());
    }

    @Test
    public void testAddIterableOfFile()
            throws IOException, SQLException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());

        store.add(otherResources);         // TODO: actually test the progress monitor
        assertEquals(otherFileNames.length, store.getSize());
    }


    @Test(expected = SQLException.class)
    public void testClose() throws IOException, SQLException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());

        store.close();
        store.getSize();
    }

    @Test
    public void testRemoveMapOfIntegerString() throws IOException, SQLException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());
        store.add(Collections.singletonList(toURI(tempDir)));
        assertEquals(fileNames.length, store.getSize());

        store.add(otherResources);
        assertEquals(fileNames.length + otherFileNames.length, store.getSize());

        store.remove(Collections.singletonMap(SERIES_NUMBER, "4"));
        assertEquals(otherFileNames.length + fileNamesSeries5.length, store.getSize());
    }

    @Test
    public void testRemoveIterableOfFile() throws IOException, SQLException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());
        store.add(Collections.singletonList(toURI(tempDir)));
        assertEquals(fileNames.length, store.getSize());

        store.add(otherResources);
        assertEquals(fileNames.length + otherFileNames.length, store.getSize());

        store.remove(resources);
        assertEquals(otherFileNames.length, store.getSize());
    }

    @Test
    public void testGetDataFiles() throws IOException, SQLException, URISyntaxException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());
        store.add(Collections.singletonList(new URI(tempDir.getPath())));
        assertEquals(fileNames.length, store.getSize());
        final Set<URI> files = store.getResources();
        assertEquals(fileNames.length, files.size());
        for (final String name : fileNames) {
            assertTrue(files.contains(toURI(tempDir, name)));
        }
    }

    @Test
    public void testGetFilesForValues() throws IOException, SQLException, URISyntaxException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());
        store.add(Collections.singletonList(toURI(tempDir)));
        assertEquals(fileNames.length, store.getSize());
        final Map<DicomAttributeIndex, ConversionFailureException> failures = Maps.newLinkedHashMap();
        final Set<URI> matches = store.getResourcesForValues(Collections.singletonMap(SERIES_NUMBER, "5"), failures);
        assertTrue(failures.isEmpty());
        assertEquals(fileNamesSeries5.length, matches.size());
        for (final String name : fileNamesSeries5) {
            assertTrue(matches.contains(toURI(tempDir, name)));
        }
    }

    @Test
    public void testGetSize() throws IOException, SQLException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());
        store.add(Collections.singletonList(toURI(tempDir)));
        assertEquals(fileNames.length, store.getSize());
    }

    @Test
    public void testGetTransferCapabilitiesStringMapOfIntegerString()
            throws IOException, SQLException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());
        store.add(Collections.singletonList(toURI(tempDir)));
        assertEquals(fileNames.length, store.getSize());
        final Map<DicomAttributeIndex, String> nullConstraints = Collections.emptyMap();
        final TransferCapability tcs[] = store.getTransferCapabilities("SCP", nullConstraints);
        assertEquals(1, tcs.length);
    }

    @Test
    public void testGetTransferCapabilitiesStringIterableOfFile()
            throws IOException, SQLException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());
        store.add(Collections.singletonList(toURI(tempDir)));
        assertEquals(fileNames.length, store.getSize());
        final TransferCapability tcs[] = store.getTransferCapabilities("SCP", store.getResources());
        assertEquals(1, tcs.length);
    }

    @Test
    public void testGetUniqueCombinations()
            throws IOException, SQLException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());
        store.add(Collections.singletonList(toURI(tempDir)));
        assertEquals(fileNames.length, store.getSize());
        final Map<DicomAttributeIndex, ConversionFailureException> failures = Maps.newLinkedHashMap();
        final Set<Map<DicomAttributeIndex, String>> combs = store.getUniqueCombinations(Arrays.asList(SERIES_NUMBER, INSTANCE_NUMBER), failures);
        assertTrue(failures.isEmpty());
        assertEquals(fileNames.length, combs.size());

        final Set<Map<DicomAttributeIndex, String>> combs2 = store.getUniqueCombinations(Collections.singletonList(SERIES_NUMBER), failures);
        assertTrue(failures.isEmpty());
        assertEquals(2, combs2.size());
    }

    @Test
    public void testGetUniqueCombinationsGivenValues()
            throws IOException, SQLException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());
        store.add(Collections.singletonList(toURI(tempDir)));
        assertEquals(fileNames.length, store.getSize());
        final Map<DicomAttributeIndex, ConversionFailureException> failures = Maps.newLinkedHashMap();
        final Set<Map<DicomAttributeIndex, String>> combs = store.getUniqueCombinationsGivenValues(
                Collections.singletonMap(SERIES_NUMBER, "5"),
                Collections.singletonList(INSTANCE_NUMBER), failures);
        assertTrue(failures.isEmpty());
        assertEquals(fileNamesSeries5.length, combs.size());
    }

    @Test
    public void testGetUniqueValuesInt()
            throws IOException, SQLException, ConversionFailureException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());
        store.add(Collections.singletonList(toURI(tempDir)));
        assertEquals(fileNames.length, store.getSize());
        final Set<String> values = store.getUniqueValues(SERIES_NUMBER);
        assertEquals(2, values.size());
        assertTrue(values.contains("4"));
        assertTrue(values.contains("5"));
        assertFalse(values.contains("6"));
    }

    @Test
    public void testGetUniqueValuesCollectionOfIntegerMapOfIntegerConversionFailureException()
            throws IOException, SQLException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());
        store.add(Collections.singletonList(toURI(tempDir)));
        assertEquals(fileNames.length, store.getSize());
        final Map<DicomAttributeIndex, ConversionFailureException> failures = Maps.newLinkedHashMap();
        final SetMultimap<DicomAttributeIndex, String> values = store.getUniqueValues(TAGS, failures);
        assertTrue(failures.isEmpty());
        assertEquals(TAGS.size(), values.keySet().size());
        assertEquals(1, values.get(STUDY_INSTANCE_UID).size());
        assertEquals(2, values.get(SERIES_INSTANCE_UID).size());
        assertEquals(fileNames.length, values.get(INSTANCE_NUMBER).size());
    }

    @Test
    public void testGetValuesForFilesMatching() throws IOException, SQLException, URISyntaxException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        assertEquals(0, store.getSize());
        store.add(Collections.singletonList(toURI(tempDir)));
        assertEquals(fileNames.length, store.getSize());
        final Map<URI, Map<DicomAttributeIndex, String>> values = store.getValuesForResourcesMatching(Arrays.asList(SERIES_INSTANCE_UID, INSTANCE_NUMBER), Collections.singletonMap(SERIES_NUMBER, "5"));
        assertEquals(fileNamesSeries5.length, values.size());
        for (final String name : fileNamesSeries5) {
            assertTrue(values.containsKey(toURI(tempDir, name)));
        }
        final Set<String> seriesUIDs = new LinkedHashSet<>();
        final Set<String> instanceNumbers = new LinkedHashSet<>();
        for (final Map.Entry<URI, Map<DicomAttributeIndex, String>> me : values.entrySet()) {
            seriesUIDs.add(me.getValue().get(SERIES_INSTANCE_UID));
            instanceNumbers.add(me.getValue().get(INSTANCE_NUMBER));
        }
        assertEquals(1, seriesUIDs.size());
        assertEquals(fileNamesSeries5.length, instanceNumbers.size());
    }

    @Test
    public void testLongValueTruncation()
            throws IOException, SQLException, URISyntaxException, ConversionFailureException {

        // See dicom-xnat DicomAttributes#chain
        final DicomAttributeIndex rescaleSlope = new ChainedDicomAttributeIndex("RescaleSlope",
                new Integer[]{Tag.RescaleSlope},
                new Integer[]{Tag.SharedFunctionalGroupsSequence, null, Tag.PixelValueTransformationSequence, null, Tag.RescaleSlope},
                new Integer[]{Tag.PerFrameFunctionalGroupsSequence, null, Tag.PixelValueTransformationSequence, null, Tag.RescaleSlope});

        Map<String, DicomAttributeIndex> fileAttrMap = new HashMap<>();
        fileAttrMap.put("testtoenhanceddynamicpet.dcm", rescaleSlope);
        fileAttrMap.put("1.MR.head_DHead.4.23.20061214.091206.156000.8215318065.comment.dcm",
                new FixedDicomAttributeIndex(Tag.PatientComments));

        for (Map.Entry<String, DicomAttributeIndex> entry : fileAttrMap.entrySet()) {
            testLongValueStore(entry.getKey(), entry.getValue());
        }
    }

    private void testLongValueStore(String fileName, DicomAttributeIndex dicomAttribute)
            throws IOException, SQLException, URISyntaxException, ConversionFailureException {
        int h2ColumnSize = 1024;
        char ellipses = '\u2026';

        // copy our long-value file into tempDir
        FileUtils.cleanDirectory(tempDir);
        final Copy copy = new Copy();
        copy.setProject(project);
        copy.setTodir(tempDir);
        copy.setFile(new File(toURI(fileName, "longValues")));
        copy.execute();

        // create store
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(Collections.singletonList(dicomAttribute),
                FileURIOpener.getInstance());
        assertEquals(0, store.getSize());
        store.add(Collections.singletonList(toURI(tempDir)));
        assertEquals(1, store.getSize());
        Set<String> values = store.getUniqueValues(dicomAttribute);
        assertNotNull(values);
        assertEquals(1, values.size());
        String value = values.stream().findFirst().orElse(null);
        assertNotNull(value);
        assertEquals(h2ColumnSize, value.length());
        assertEquals(ellipses, value.charAt(h2ColumnSize - 1));
    }
}
