package org.nrg.xnat.services.archive;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatMrsessiondata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.ResourceData;
import org.nrg.xnat.config.TestConfig;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.impl.ExptURI;
import org.nrg.xnat.helpers.uri.archive.impl.ResourcesExptURI;
import org.nrg.xnat.services.archive.impl.legacy.DefaultCatalogService;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Fail.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@PowerMockIgnore({"org.apache.*", "java.*", "javax.*", "org.w3c.*", "com.sun.*", "org.xml.sax.*"})
@PrepareForTest({UriParserUtils.class})
@ContextConfiguration(classes = {TestConfig.class})
public class TestCatalogService {
    @Autowired private PermissionsServiceI mockPermissionsService;
    @Autowired private CatalogService catalogService;
    @Autowired private DefaultCatalogService catalogServiceNoRemote;
    @Autowired private RemoteFilesService remoteFilesService;

    private UserI mockUser;
    private UserI unpermittedUser;

    private XnatMrsessiondata session;
    private String mockSesUri;
    private String mockCatResUri;
    private String writableArchivePath = "/tmp/test_catalog_service";
    private String sesArchivePath = "/some/fake/path";
    private XnatResourcecatalog catRes;
    private ExptURI mockSesUriObj;
    private ResourcesExptURI mockCatResUriObj;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        catalogServiceNoRemote.setRemoteFilesService(null);

        Files.createDirectories(Paths.get(writableArchivePath));

        mockUser = Mockito.mock(UserI.class);
        when(mockUser.getLogin()).thenReturn("mockUser");

        unpermittedUser = Mockito.mock(UserI.class);
        when(unpermittedUser.getLogin()).thenReturn("unpermittedUser");

        // Permissions
        when(mockPermissionsService.can(eq(unpermittedUser), any(ItemI.class), anyString()))
                .thenReturn(Boolean.FALSE);

        when(mockPermissionsService.can(eq(mockUser), any(ItemI.class), anyString()))
                .thenReturn(Boolean.TRUE);

        //Mock objects
        session = Mockito.mock(XnatMrsessiondata.class);
        Mockito.when(session.getId()).thenReturn("LOCAL_E00001");

        catRes = Mockito.mock(XnatResourcecatalog.class);
        Mockito.when(catRes.getLabel()).thenReturn("LABEL");

        mockSesUriObj = Mockito.mock(ExptURI.class);
        mockCatResUriObj = Mockito.mock(ResourcesExptURI.class);
        mockSesUri = "/archive/experiments/" + session.getId();
        mockCatResUri = mockSesUri + "/resources/" + catRes.getLabel();
        PowerMockito.mockStatic(UriParserUtils.class);
        PowerMockito.when(UriParserUtils.parseURI(mockSesUri)).thenReturn(mockSesUriObj);
        PowerMockito.when(UriParserUtils.parseURI(mockCatResUri)).thenReturn(mockCatResUriObj);
        Mockito.when(mockSesUriObj.getSecurityItem()).thenReturn(session);
        Mockito.when(mockCatResUriObj.getSecurityItem()).thenReturn(session);
        Mockito.when(mockSesUriObj.getResources(anyBoolean()))
                .thenReturn(Collections.singletonList((XnatAbstractresourceI) catRes));
        Mockito.when(mockCatResUriObj.getResourceFilePath()).thenReturn("");
        Mockito.when(mockCatResUriObj.getXnatResource()).thenReturn(catRes);
    }

    @Test
    public void testPullNoRemoteService() throws Exception {
        exceptionRule.expect(ServerException.class);
        exceptionRule.expectMessage("No remote filesystems configured for this site; all catalogs must be local");
        catalogServiceNoRemote.pullResourceCatalogsToDestination(unpermittedUser, "junk", sesArchivePath, null);
    }

    @Test
    public void testPullPermsException() throws Exception {
        exceptionRule.expect(ClientException.class);
        exceptionRule.expectMessage("The user " + unpermittedUser.getLogin() +
                " does not have permission to read the resource " + mockSesUri + " for item " +
                session.getId());
        catalogService.pullResourceCatalogsToDestination(unpermittedUser, mockSesUri, sesArchivePath, writableArchivePath);
    }

    @Test
    public void testPullPermsSuccess() {
        try {
            catalogService.pullResourceCatalogsToDestination(mockUser, mockSesUri, sesArchivePath,null);
        } catch (Exception e) {
            fail("Expecting no exceptions but caught " + ExceptionUtils.getStackTrace(e));
        }
    }

    @Test
    public void testHasRemoteFilesNoRemoteService() throws Exception {
        assertThat(catalogServiceNoRemote.hasRemoteFiles(mockUser, "anything"), is(false));
    }

    @Test
    public void testHasRemoteFiles() throws Exception {
        Mockito.when(remoteFilesService.catalogHasRemoteFiles(catRes)).thenReturn(true);
        assertThat(catalogService.hasRemoteFiles(mockUser, mockCatResUri), is(true));
        assertThat(catalogService.hasRemoteFiles(mockUser, mockSesUri), is(true));

        Mockito.when(remoteFilesService.catalogHasRemoteFiles(catRes)).thenReturn(false);
        assertThat(catalogService.hasRemoteFiles(mockUser, mockCatResUri), is(false));
        assertThat(catalogService.hasRemoteFiles(mockUser, mockSesUri), is(false));
    }

    @Test
    public void testGetResourceDataFromUriInvalid() throws Exception {
        String uriString = "bad";
        exceptionRule.expect(ClientException.class);
        exceptionRule.expectMessage("Invalid URI: " + uriString);
        catalogService.getResourceDataFromUri(uriString);
    }

    @Test
    public void testGetResourceDataFromUriNoItem() throws Exception {
        ExptURI mockBadExptUri = Mockito.mock(ExptURI.class);
        String uriString = "/archive/experiments/badid";
        PowerMockito.when(UriParserUtils.parseURI(uriString)).thenReturn(mockBadExptUri);
        Mockito.when(mockBadExptUri.getSecurityItem()).thenReturn(null);
        exceptionRule.expect(ClientException.class);
        exceptionRule.expectMessage("Cannot locate archivable item securing " + uriString);
        catalogService.getResourceDataFromUri(uriString);
    }

    @Test
    public void testGetResourceDataFromUriFilePath() throws Exception {
        ResourcesExptURI mockResUriObj = Mockito.mock(ResourcesExptURI.class);
        String uriString = "/archive/experiments/id/resources/label/label_catalog.xml";
        PowerMockito.when(UriParserUtils.parseURI(uriString)).thenReturn(mockResUriObj);
        Mockito.when(mockResUriObj.getSecurityItem()).thenReturn(session);
        Mockito.when(mockResUriObj.getResourceFilePath()).thenReturn("label_catalog.xml");
        exceptionRule.expect(ClientException.class);
        exceptionRule.expectMessage("Resource URI: " + uriString +
                " is a file; you should provide the path to a resource (leave off the *_catalog.xml).");
        catalogService.getResourceDataFromUri(uriString);
    }

    @Test
    public void testGetResourceDataFromUriItem() throws Exception {
        ResourceData resourceData = catalogService.getResourceDataFromUri(mockSesUri);
        assertThat(resourceData.getItem(), is((ArchivableItem) session));
        assertThat(resourceData.getUri(), is((URIManager.DataURIA) mockSesUriObj));
        assertThat(resourceData.getXnatUri(), is((URIManager.ArchiveItemURI) mockSesUriObj));
        assertThat(resourceData.getCatalogResource(), is(nullValue()));

        exceptionRule.expect(ClientException.class);
        exceptionRule.expectMessage("Resource URI: " + mockSesUri +
                        " doesn't refer to a resource.");
        catalogService.getResourceDataFromUri(mockSesUri, true);
    }

    @Test
    public void testGetResourceDataFromUriCatalog() throws Exception {
        ResourceData resourceData = catalogService.getResourceDataFromUri(mockCatResUri);
        assertThat(resourceData.getItem(), is((ArchivableItem) session));
        assertThat(resourceData.getUri(), is((URIManager.DataURIA) mockCatResUriObj));
        assertThat(resourceData.getXnatUri(), is((URIManager.ArchiveItemURI) mockCatResUriObj));
        assertThat(resourceData.getCatalogResource(), is(catRes));

        ResourceData resourceData2 = catalogService.getResourceDataFromUri(mockCatResUri, true);
        assertEquals(resourceData, resourceData2);
    }

    @Test
    public void testGetResourceDataFromUriResource() throws Exception {
        ResourcesExptURI mockResUriObj = Mockito.mock(ResourcesExptURI.class);
        String uriString = "/archive/experiments/id/resources/label";
        PowerMockito.when(UriParserUtils.parseURI(uriString)).thenReturn(mockResUriObj);
        Mockito.when(mockResUriObj.getSecurityItem()).thenReturn(session);
        Mockito.when(mockResUriObj.getResourceFilePath()).thenReturn("");
        XnatAbstractresourceI abstRes = Mockito.mock(XnatAbstractresourceI.class);
        Mockito.when(mockResUriObj.getXnatResource()).thenReturn(abstRes);

        ResourceData resourceData = catalogService.getResourceDataFromUri(uriString);
        assertThat(resourceData.getItem(), is((ArchivableItem) session));
        assertThat(resourceData.getUri(), is((URIManager.DataURIA) mockResUriObj));
        assertThat(resourceData.getXnatUri(), is((URIManager.ArchiveItemURI) mockResUriObj));
        assertThat(resourceData.getCatalogResource(), is(nullValue()));

        exceptionRule.expect(ClientException.class);
        exceptionRule.expectMessage("Resource URI: " + uriString +
                " doesn't refer to a catalog.");
        catalogService.getResourceDataFromUri(uriString, true);
    }

}
