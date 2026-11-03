package org.nrg.xnat.archive.services.impl;

import java.io.File;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatDcmentryBean;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xnat.archive.services.DirectArchiveSessionHibernateService;
import org.nrg.xnat.helpers.merge.MergeUtils;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.turbine.utils.XNATSessionPopulater;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xnat.utils.CatalogUtils.CatalogData;
import org.nrg.xnat.utils.WorkflowUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A direct-archive session whose build or archive fails moves to the prearchive, unless its directory is not its own
 * to move. A merge receives into the archived experiment's directory, so moving it took the experiment's files to the
 * prearchive and left the experiment with scans that list no files.
 */
public class DirectArchiveSessionServiceImplArchiveFailureTest {
    private static final long   SESSION_ID = 42L;
    private static final String PROJECT    = "PROJ";
    private static final String SESSION    = "SESSION_01";
    private static final String TIMESTAMP  = "20261103_120000";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private DirectArchiveSessionHibernateService hibernateService;
    private DirectArchiveSessionServiceImpl      service;
    private MockedStatic<PrearcUtils>            mockedPrearcUtils;
    private MockedStatic<PrearcDatabase>         mockedPrearcDatabase;
    private MockedStatic<BaseXnatProjectdata>    mockedProjects;
    private MockedStatic<BaseXnatExperimentdata> mockedExperiments;

    private File sessionDirectory;
    private File prearchiveDirectory;

    /** The session the move recorded in the prearchive. */
    private final AtomicReference<SessionData> recorded = new AtomicReference<>();

    private final Exception failure = new IllegalStateException("Unable to build the session");

    @Before
    public void setUp() throws Exception {
        hibernateService = mock(DirectArchiveSessionHibernateService.class);
        service          = new DirectArchiveSessionServiceImpl(hibernateService,
                                                               mock(JmsTemplate.class),
                                                               mock(XnatUserProvider.class),
                                                               mock(GroupsAndPermissionsCache.class),
                                                               mock(PermissionsServiceI.class));

        sessionDirectory    = temporaryFolder.newFolder("archive", PROJECT, "arc001", SESSION);
        prearchiveDirectory = new File(temporaryFolder.getRoot(), String.join(File.separator, "prearchive", PROJECT, TIMESTAMP, SESSION));
        Files.writeString(new File(sessionDirectory, "1.dcm").toPath(), "dicom");

        // Nothing is still landing and no archived experiment owns the directory unless a test says so.
        mockedPrearcUtils = Mockito.mockStatic(PrearcUtils.class);
        mockedPrearcUtils.when(() -> PrearcUtils.isSessionReceiving(any())).thenReturn(false);
        mockedPrearcUtils.when(() -> PrearcUtils.buildSession(any())).thenThrow(failure);
        mockedPrearcUtils.when(() -> PrearcUtils.getPrearcSessionDir(any(), eq(PROJECT), eq(TIMESTAMP), eq(SESSION), eq(true))).thenReturn(prearchiveDirectory);
        mockedExperiments = Mockito.mockStatic(BaseXnatExperimentdata.class);
        mockedExperiments.when(() -> BaseXnatExperimentdata.GetExptByProjectIdentifier(any(), any(), any(), anyBoolean())).thenReturn(null);
        final XnatProjectdata project = mock(XnatProjectdata.class);
        when(project.getRootArchivePath()).thenReturn(sessionDirectory.getParentFile().getParent());
        mockedProjects = Mockito.mockStatic(BaseXnatProjectdata.class);
        mockedProjects.when(() -> BaseXnatProjectdata.getProjectByIDorAlias(eq(PROJECT), any(), anyBoolean())).thenReturn(project);

        mockedPrearcDatabase = Mockito.mockStatic(PrearcDatabase.class);
        mockedPrearcDatabase.when(() -> PrearcDatabase.countOf(any(), any(), any())).thenReturn(0);
        mockedPrearcDatabase.when(() -> PrearcDatabase.addSession(any())).thenAnswer(invocation -> {
            recorded.set(invocation.getArgument(0));
            return null;
        });

        when(hibernateService.setStatusToBuildingAndReturn(SESSION_ID)).thenAnswer(invocation -> session(PrearcStatus.BUILDING));
        when(hibernateService.setStatusToArchivingAndReturn(SESSION_ID)).thenAnswer(invocation -> session(PrearcStatus.ARCHIVING));
    }

    @After
    public void tearDown() {
        mockedPrearcDatabase.closeOnDemand();
        mockedProjects.closeOnDemand();
        mockedExperiments.closeOnDemand();
        mockedPrearcUtils.closeOnDemand();
    }

    private SessionData session(final PrearcStatus status) {
        final SessionData session = new SessionData().setProject(PROJECT).setTag("1.2.3").setName(SESSION).setFolderName(SESSION)
                                                     .setTimestamp(TIMESTAMP).setUrl(sessionDirectory.getAbsolutePath())
                                                     .setStatus(status);
        session.setId(SESSION_ID);
        return session;
    }

    private void archivedExperimentOwnsTheDirectory() {
        mockedExperiments.when(() -> BaseXnatExperimentdata.GetExptByProjectIdentifier(eq(PROJECT), eq(SESSION), any(), anyBoolean()))
                         .thenReturn(mock(XnatExperimentdata.class));
    }

    private void assertLeftInErrorWhereItIs() throws Exception {
        assertThat(new File(sessionDirectory, "1.dcm")).isFile();
        assertThat(prearchiveDirectory).doesNotExist();
        mockedPrearcDatabase.verify(() -> PrearcDatabase.addSession(any()), never());
        verify(hibernateService).setStatusToError(SESSION_ID, failure);
        verify(hibernateService, never()).delete(anyLong());
    }

    @Test
    public void aSessionWhoseBuildFailsMovesToThePrearchive() throws Exception {
        service.build(SESSION_ID);

        assertThat(sessionDirectory).doesNotExist();
        assertThat(new File(prearchiveDirectory, "1.dcm")).isFile();
        assertThat(recorded.get().getUrl()).isEqualTo(prearchiveDirectory.getAbsolutePath());
        verify(hibernateService).delete(SESSION_ID);
    }

    @Test
    public void aMergeWhoseBuildFailsStaysInErrorWhereItIs() throws Exception {
        archivedExperimentOwnsTheDirectory();

        service.build(SESSION_ID);

        assertLeftInErrorWhereItIs();
    }

    @Test
    public void aMergeWhoseArchiveFailsStaysInErrorWhereItIs() throws Exception {
        // The same holds for an archive that failed after saving the experiment.
        archivedExperimentOwnsTheDirectory();

        try (final MockedConstruction<XNATSessionPopulater> ignored = Mockito.mockConstruction(XNATSessionPopulater.class,
                (populater, context) -> when(populater.populate()).thenThrow(failure))) {
            service.archive(SESSION_ID);
        }

        assertLeftInErrorWhereItIs();
    }

    @Test
    public void aSessionSharingItsDirectoryStaysInErrorWhereItIs() throws Exception {
        // Another session at the location is still receiving into, or has failed in, the same directory.
        when(hibernateService.hasOtherSessionAtLocation(sessionDirectory.getAbsolutePath(), SESSION_ID)).thenReturn(true);

        service.build(SESSION_ID);

        assertLeftInErrorWhereItIs();
    }

    @Test
    public void anOverwriteMergeUpdatesTheArchivedEntryUnderTheMergesEvent() throws Exception {
        // Without an event, updating the entry threw a NullPointerException, so every overwrite merge failed.
        final XnatResourcecatalog incomingResource = mock(XnatResourcecatalog.class);
        final XnatResourcecatalog archivedResource = mock(XnatResourcecatalog.class);
        final XnatImagescandataI  incomingScan     = mock(XnatImagescandataI.class);
        final XnatImagescandataI  archivedScan     = mock(XnatImagescandataI.class);
        final List<XnatAbstractresourceI> archivedResources = List.of(archivedResource);
        when(incomingScan.getFile()).thenReturn(List.of(incomingResource));
        when(archivedScan.getFile()).thenReturn(archivedResources);

        final CatDcmentryBean incomingEntry = new CatDcmentryBean();
        incomingEntry.setUid("1.2.3.4");
        incomingEntry.setUri("1.dcm");
        final CatCatalogBean incomingCatalog = new CatCatalogBean();
        incomingCatalog.addEntries_entry(incomingEntry);
        final CatCatalogBean  archivedCatalog = new CatCatalogBean();
        final CatDcmentryBean archivedEntry   = new CatDcmentryBean();
        final CatalogData     incomingData    = catalogData(incomingCatalog);
        final CatalogData     archivedData    = catalogData(archivedCatalog);
        final EventMetaI      event           = mock(EventMetaI.class);

        try (final MockedStatic<MergeUtils> mergeUtils = Mockito.mockStatic(MergeUtils.class);
             final MockedStatic<CatalogData> catalogData = Mockito.mockStatic(CatalogData.class);
             final MockedStatic<CatalogUtils> catalogUtils = Mockito.mockStatic(CatalogUtils.class)) {
            mergeUtils.when(() -> MergeUtils.getMatchingResource(incomingResource, archivedResources)).thenReturn(archivedResource);
            catalogData.when(() -> CatalogData.getOrCreate(any(String.class), eq(incomingResource), eq(PROJECT))).thenReturn(incomingData);
            catalogData.when(() -> CatalogData.getOrCreate(any(String.class), eq(archivedResource), eq(PROJECT))).thenReturn(archivedData);
            catalogUtils.when(() -> CatalogUtils.getDCMEntryByUID(archivedCatalog, "1.2.3.4")).thenReturn(archivedEntry);

            service.mergeScanCatalogs(incomingScan, archivedScan, PROJECT, sessionDirectory.getPath(), true, event);

            catalogUtils.verify(() -> CatalogUtils.addOrUpdateEntry(eq(archivedData), eq(archivedEntry), eq("1.dcm"), eq("1.dcm"),
                                                                    any(File.class), isNull(), eq(event)));
            catalogUtils.verify(() -> CatalogUtils.writeCatalogToFile(archivedData));
        }
    }

    @Test
    public void aMergeThatFailsFailsItsWorkflow() throws Exception {
        // archive() fails only a workflow it created itself. A merge workflow left open refused every later prearchive
        // archive into the experiment.
        final XnatImagesessiondata archived = mock(XnatImagesessiondata.class);
        final XnatImagesessiondata incoming = mock(XnatImagesessiondata.class);
        when(incoming.getLabel()).thenReturn(SESSION);
        when(archived.getId()).thenReturn("XNAT_E00001");
        when(archived.getScans_scan()).thenThrow(failure);
        mockedExperiments.when(() -> BaseXnatExperimentdata.GetExptByProjectIdentifier(eq(PROJECT), eq(SESSION), any(), anyBoolean()))
                         .thenReturn(archived);
        final PersistentWorkflowI workflow = mock(PersistentWorkflowI.class);
        when(workflow.getPipelineName()).thenReturn(EventUtils.MERGE);
        when(workflow.getJustification()).thenReturn("Direct-to-archive upload");
        when(workflow.getLaunchTimeDate()).thenReturn(new Date());

        try (final MockedStatic<PersistentWorkflowUtils> workflows = Mockito.mockStatic(PersistentWorkflowUtils.class);
             final MockedStatic<WorkflowUtils> workflowUtils = Mockito.mockStatic(WorkflowUtils.class)) {
            workflows.when(() -> PersistentWorkflowUtils.getOpenWorkflows(any(), eq("XNAT_E00001"))).thenAnswer(invocation -> List.of(workflow));

            assertThatThrownBy(() -> service.archiveMerge(SESSION_ID, session(PrearcStatus.ARCHIVING), incoming, null,
                                                          sessionDirectory.getPath(), PrearcUtils.DELETE)).isSameAs(failure);

            workflowUtils.verify(() -> WorkflowUtils.fail(eq(workflow), any()));
        }
    }

    private CatalogData catalogData(final CatCatalogBean catalog) {
        final CatalogData data = mock(CatalogData.class);
        data.catBean = catalog;
        data.catFile = new File(sessionDirectory, "scans/1/resources/DICOM/scan_1_catalog.xml");
        return data;
    }
}
