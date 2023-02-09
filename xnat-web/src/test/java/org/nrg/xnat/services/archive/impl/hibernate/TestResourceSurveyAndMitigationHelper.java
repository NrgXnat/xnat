package org.nrg.xnat.services.archive.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.dcm4che2.io.StopTagInputHandler;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatMrscandata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.entities.ResourceSurveyRequest;
import org.nrg.xnat.services.archive.RemoteFilesService;
import org.nrg.xnat.services.archive.ResourceMitigationReport;
import org.nrg.xnat.services.archive.ResourceSurveyReport;
import org.nrg.xnat.services.archive.ResourceSurveyRequestEntityService;
import org.nrg.xnat.utils.CatalogUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@PrepareForTest({ResourceMitigationHelper.class, XDAT.class, SaveItemHelper.class, CatalogUtils.class})
@ContextConfiguration(classes = TestResourceSurveyAndMitigationHelperConfig.class)
@Slf4j
public class TestResourceSurveyAndMitigationHelper {
    private static final String            RESOURCE_CATALOG_FILE = "scan_5_catalog.xml";
    private static final String            RESOURCE_CATALOG_URI  = "classpath:dicom/duplicates/" + RESOURCE_CATALOG_FILE;
    private static final String            SERIES_DESCRIPTION    = "3D SPGR VOLUME 1";
    private static final String            PROJECT_ID            = "Test";
    private static final String            SUBJECT_ID            = "XNAT_S00001";
    private static final String            EXPERIMENT_ID         = "XNAT_E00001";
    private static final String            SUBJECT_LABEL         = "Test_01";
    private static final String            EXPERIMENT_LABEL      = "Test_01_MR_02";
    private static final String            RESOURCE_LABEL        = "DICOM";
    private static final int               RESOURCE_ID           = 54;
    private static final int               SCAN_ID               = 5;
    private static final String            PROPER_FILENAME_LIST  = "classpath:dicom/duplicates/expectedNames.txt";
    private static final String            SOP_CLASS_UID         = "1.2.840.10008.5.1.4.1.1.4";
    private static final Condition<String> MATCHES_SOP_CLASS_UID = new Condition<>(key -> StringUtils.equals(SOP_CLASS_UID, key), "UID list doesn't contain SOP class UID " + SOP_CLASS_UID);

    @Mock
    private ResourceSurveyRequestEntityService _mockEntityService;

    @Mock
    private XnatResourcecatalog _mockResource;

    @Mock
    private ContextService _mockContextService;

    @Mock
    private SiteConfigPreferences _mockPreferences;

    @Mock
    private WrkWorkflowdata _mockWorkflow;

    @Mock
    private UserI _mockUser;

    private final Path     _resourcePath;
    private final Path     _cachePath;
    private final String[] _expectedFilenames;

    private SerializerService   _serializer;
    private DicomFileNamer      _dicomFileNamer;
    private StopTagInputHandler _stopTagInputHandler;

    public TestResourceSurveyAndMitigationHelper() throws IOException {
        final Path tempDirectory = Files.createTempDirectory("TestResourceSurveyAndMitigationHelper-");
        tempDirectory.toFile().deleteOnExit();
        _cachePath = tempDirectory.resolve("cache");
        _cachePath.toFile().mkdirs();

        final Path resourcePath = tempDirectory.resolve("resource");
        resourcePath.toFile().mkdirs();

        FileUtils.copyDirectory(BasicXnatResourceLocator.getResource(RESOURCE_CATALOG_URI).getFile().getParentFile(), resourcePath.toFile());
        _resourcePath      = resourcePath.resolve(RESOURCE_CATALOG_FILE);
        _expectedFilenames = Files.readAllLines(BasicXnatResourceLocator.getResource(PROPER_FILENAME_LIST).getFile().toPath()).toArray(new String[0]);
    }

    @Autowired
    public void setSerializer(final SerializerService serializer) {
        _serializer = serializer;
    }

    @Autowired
    public void setDicomFileNamer(final DicomFileNamer dicomFileNamer) {
        _dicomFileNamer = dicomFileNamer;
    }

    @Autowired
    public void setStopTagInputHandler(final StopTagInputHandler stopTagInputHandler) {
        _stopTagInputHandler = stopTagInputHandler;
    }

    @Test
    @Ignore("Changes to workflow updates have made this difficult to get working, revisit later.")
    public void testSurveyReport() throws Exception {
        final ResourceSurveyRequest request      = getSurveyRequest();
        final ResourceSurveyHelper  surveyHelper = new ResourceSurveyHelper(_mockEntityService, request, _serializer, _dicomFileNamer, _stopTagInputHandler);
        final ResourceSurveyReport  report       = surveyHelper.call();

        assertThat(report).isNotNull();
        assertThat(report.getUids()).isNotNull().isNotEmpty().hasSize(1).hasKeySatisfying(MATCHES_SOP_CLASS_UID);
        assertThat(report.getUids().get(SOP_CLASS_UID)).isNotNull().isNotEmpty().hasSize(60);
        assertThat(report.getBadFiles()).isNotNull().isEmpty();
        assertThat(report.getDuplicates()).isNotNull().isNotEmpty().hasSize(1).hasKeySatisfying(MATCHES_SOP_CLASS_UID);
        assertThat(report.getDuplicates().get(SOP_CLASS_UID)).isNotNull().isNotEmpty().hasSize(5);
        assertThat(report.getMismatchedFiles()).isNotNull().isNotEmpty().hasSize(26);

        request.setSurveyReport(report);

        // CatalogUtils leverages these methods
        PowerMockito.mockStatic(XDAT.class);
        PowerMockito.when(XDAT.getSiteConfigurationProperty("checksums")).thenReturn("false");
        PowerMockito.when(XDAT.getBoolSiteConfigurationProperty("maintainFileHistory", false)).thenReturn(true);
        PowerMockito.when(XDAT.getSerializerService()).thenReturn(_serializer);
        PowerMockito.when(XDAT.getContextService()).thenReturn(_mockContextService);
        Mockito.when(_mockContextService.getBeanSafely(RemoteFilesService.class)).thenReturn(null);
        PowerMockito.when(XDAT.getSiteConfigPreferences()).thenReturn(_mockPreferences);
        Mockito.when(_mockPreferences.getCachePath()).thenReturn(_cachePath.toAbsolutePath().toString());
        PowerMockito.mockStatic(SaveItemHelper.class);
        PowerMockito.when(SaveItemHelper.authorizedSave(eq(_mockResource), eq(_mockUser), anyBoolean(), anyBoolean(), any(EventMetaI.class))).thenReturn(true);
        PowerMockito.mockStatic(CatalogUtils.class);
        // PowerMockito.doNothing().when(CatalogUtils.class, "updateModificationEvent", any(CatEntryBean.class), any(EventMetaI.class));

        // Gross private method mock bc XFT :(
        final ResourceMitigationHelper mitigationHelper = PowerMockito.spy(new ResourceMitigationHelper(request, _mockWorkflow, _mockUser, _mockPreferences));
        PowerMockito.doReturn(_mockResource).when(mitigationHelper, "getCatalogResource");
        Mockito.when(_mockResource.getUri()).thenReturn(_resourcePath.toString());

        // This shouldn't matter too much: only gets called when there's an error.
        Mockito.when(_mockEntityService.get(anyLong())).thenReturn(request);

        // Ok, back to business...
        final ResourceMitigationReport mitigationReport = mitigationHelper.call();

        assertThat(mitigationReport).isNotNull();
        assertThat(mitigationReport.getRemovedFiles()).isNotNull().isNotEmpty().hasSize(36);
        assertThat(mitigationReport.getMovedFiles()).isNotNull().isNotEmpty().hasSize(31);

        CatalogUtils.CatalogData catalogData = new CatalogUtils.CatalogData(_resourcePath.toFile(), null);
        Assert.assertThat(catalogData.catBean.getEntries_entry(), hasSize(60));
        Assert.assertThat(catalogData.catBean.getEntries_entry().stream().map(CatEntryI::getUri).collect(Collectors.toList()), containsInAnyOrder(_expectedFilenames));
        Assert.assertThat(catalogData.catBean.getEntries_entry().stream().map(CatEntryI::getId).collect(Collectors.toList()), containsInAnyOrder(_expectedFilenames));
    }

    private ResourceSurveyRequest getSurveyRequest() {
        final ResourceSurveyRequest request = ResourceSurveyRequest.builder()
                                                                   .resourceId(RESOURCE_ID)
                                                                   .resourceUri(_resourcePath.toString())
                                                                   .rsnStatus(ResourceSurveyRequest.Status.CREATED)
                                                                   .projectId(PROJECT_ID)
                                                                   .subjectId(SUBJECT_ID)
                                                                   .subjectLabel(SUBJECT_LABEL)
                                                                   .experimentId(EXPERIMENT_ID)
                                                                   .experimentLabel(EXPERIMENT_LABEL)
                                                                   .scanId(SCAN_ID)
                                                                   .scanDescription(SERIES_DESCRIPTION)
                                                                   .scanLabel(SERIES_DESCRIPTION)
                                                                   .resourceLabel(RESOURCE_LABEL)
                                                                   .xsiType(XnatMrscandata.SCHEMA_ELEMENT_NAME)
                                                                   .build();
        request.setId(RandomUtils.nextLong(1, 1000));
        return request;
    }
}
