/*
 * web: org.nrg.xnat.utils.TestCatalogUtilsReadOnlyCatalog
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import java.io.File;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.nrg.action.ServerException;
import org.nrg.xdat.model.XnatResourcecatalogI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * XNAT-8806: read-only callers must be able to locate and read a catalog without creating one or unzipping it in
 * place, which {@code getOrCreateCatalogFile} does.
 */
public class TestCatalogUtilsReadOnlyCatalog {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File                 root;
    private XnatResourcecatalogI resource;

    @Before
    public void setUp() throws Exception {
        root     = temporaryFolder.newFolder("session");
        resource = Mockito.mock(XnatResourcecatalogI.class);
        Mockito.when(resource.getUri()).thenReturn("SCANS/1/DICOM/scan_1_catalog.xml");
    }

    @Test
    public void findsAnExistingCatalogFile() throws Exception {
        final File catalog = catalogFile("scan_1_catalog.xml");

        assertThat(CatalogUtils.findCatalogFile(root.getAbsolutePath(), resource)).isEqualTo(catalog);
    }

    @Test
    public void findsAGzippedCatalogWithoutUnzippingIt() throws Exception {
        final File gz = catalogFile("scan_1_catalog.xml.gz");

        assertThat(CatalogUtils.findCatalogFile(root.getAbsolutePath(), resource)).isEqualTo(gz);
        assertThat(gz).exists();
        assertThat(new File(gz.getParentFile(), "scan_1_catalog.xml")).doesNotExist();
    }

    @Test
    public void reportsAMissingCatalogWithoutCreatingOne() {
        assertThat(CatalogUtils.findCatalogFile(root.getAbsolutePath(), resource)).isNull();
        assertThat(new File(root, "SCANS/1/DICOM")).doesNotExist();
    }

    @Test
    public void readOnlyAccessToAMissingCatalogIsAnErrorNotACreation() {
        assertThatThrownBy(() -> CatalogUtils.CatalogData.getExistingAndClean(root.getAbsolutePath(), resource, false, "PROJ"))
                .isInstanceOf(ServerException.class);
        assertThat(new File(root, "SCANS/1/DICOM")).doesNotExist();
    }

    private File catalogFile(final String name) throws Exception {
        final File dir = new File(root, "SCANS/1/DICOM");
        assertThat(dir.mkdirs()).isTrue();
        final File file = new File(dir, name);
        Files.writeString(file.toPath(), "");
        return file;
    }
}
