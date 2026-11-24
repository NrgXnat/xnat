/*
 * web: org.nrg.xnat.archive.AnonymizationErrorSurfacingTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.config.entities.Configuration;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.merge.AnonUtils;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.restlet.data.Status;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What the uploader is shown when anonymization fails. {@code ServerException(Status, Throwable)} delegates to
 * {@code Exception(Throwable)}, whose message is {@code cause.toString()}, so wrapping a failure that is already a
 * {@link ServerException} prepends {@code "org.nrg.action.ServerException: "} to the text -- and the site-wide script
 * path used to do it twice, once on the way out of the error branch and again when its own
 * {@code catch (Throwable)} re-entered the handler. The reason has to arrive unprefixed: it is the text the uploader
 * is shown, and two copies of a class name push the part that names the failing script line out of view.
 *
 * <p>Re-entering the handler also ran the cleanup a second time. The tests through {@code applyScripts} count it.</p>
 */
public class AnonymizationErrorSurfacingTest {
    private static final String REASON = "line 11: Variable 'subject' is null.";

    private MockedStatic<XDAT>             mockedXDAT;
    private MockedStatic<DefaultAnonUtils> mockedAnonUtils;
    private AnonUtils                      anonUtils;
    private MizerService                   mizer;
    private WrittenFile                    written;

    @Before
    public void setUp() throws Exception {
        // ImporterHandlerA looks up importer packages in the Spring context when the class loads.
        mizer = mock(MizerService.class);
        when(mizer.createContext(any(), any(), any(), anyLong(), any(), anyBoolean(), anyBoolean())).thenReturn(mock(MizerContextWithScript.class));
        final ContextService contextService = mock(ContextService.class);
        doReturn(Collections.emptyMap()).when(contextService).getBeansOfType(any());
        doReturn(mizer).when(contextService).getBeanSafely(MizerService.class);
        mockedXDAT = Mockito.mockStatic(XDAT.class);
        mockedXDAT.when(XDAT::getContextService).thenReturn(contextService);

        final Configuration siteWide = mock(Configuration.class);
        when(siteWide.getStatus()).thenReturn(Configuration.ENABLED_STRING);
        anonUtils = mock(AnonUtils.class);
        mockedAnonUtils = Mockito.mockStatic(DefaultAnonUtils.class);
        mockedAnonUtils.when(DefaultAnonUtils::getService).thenReturn(anonUtils);
        mockedAnonUtils.when(DefaultAnonUtils::getCachedSitewideAnon).thenReturn(siteWide);

        written = new WrittenFile(Files.createTempFile("anon-error-surfacing", ".dcm").toFile());
    }

    @After
    public void tearDown() {
        mockedAnonUtils.closeOnDemand();
        mockedXDAT.closeOnDemand();
        written.refuseDelete = false;
        written.delete();
    }

    /** The trap the fix avoids, pinned so the shape of the fix is not simplified away. */
    @Test
    public void everyWrapOfAServerExceptionPrependsItsClassNameToTheMessage() {
        final ServerException real = new ServerException(Status.SERVER_ERROR_INTERNAL, REASON, new RuntimeException());
        assertEquals(REASON, real.getMessage());
        final ServerException once = new ServerException(Status.SERVER_ERROR_INTERNAL, real);
        assertEquals("org.nrg.action.ServerException: " + REASON, once.getMessage());
        final ServerException twice = new ServerException(Status.SERVER_ERROR_INTERNAL, once);
        assertEquals("org.nrg.action.ServerException: org.nrg.action.ServerException: " + REASON, twice.getMessage());
    }

    @Test
    public void aFailureThatIsAlreadyAServerExceptionIsThrownAsItIs() throws Exception {
        final ServerException real   = new ServerException(Status.SERVER_ERROR_INTERNAL, REASON, new RuntimeException());
        final ServerException thrown = assertThrows(ServerException.class,
                                                    () -> importer().handleAnonymizationError(new AtomicBoolean(false), null, written, real));

        assertSame("the handler should carry the exception, not rebuild it", real, thrown);
        assertEquals("the uploader's reason must arrive unprefixed", REASON, thrown.getMessage());
        assertFalse("the written file should have been cleaned up", written.exists());
    }

    /**
     * The control, without which the test above would pass on a handler that wrapped nothing at all: a failure that
     * is not an {@link org.nrg.action.ActionException} still has to become one, and that is where the prefix is
     * legitimate, because there is no other message to show.
     */
    @Test
    public void aFailureThatIsNotAnActionExceptionIsStillWrapped() throws Exception {
        final RuntimeException raw    = new RuntimeException("no such column");
        final ServerException  thrown = assertThrows(ServerException.class,
                                                     () -> importer().handleAnonymizationError(new AtomicBoolean(false), null, written, raw));

        assertSame(raw, thrown.getCause());
        assertEquals(Status.SERVER_ERROR_INTERNAL, thrown.getStatus());
        assertEquals("java.lang.RuntimeException: no such column", thrown.getMessage());
        assertFalse("the written file should have been cleaned up", written.exists());
    }

    @Test
    public void aSiteWideScriptThatFailsIsA500WithItsReasonAndOneCleanup() throws Exception {
        when(anonUtils.isSiteWideScriptEnabled()).thenReturn(true);

        final ServerException thrown = assertThrows(ServerException.class, () -> applyScripts(importer()));

        assertEquals("the uploader's reason must arrive unprefixed", REASON, thrown.getMessage());
        assertEquals("the failure should be cleaned up once", 1, written.deletes);
        assertFalse(written.exists());
    }

    /** A cleanup that fails is reported as it is, not retried by the site-wide branch's own catch. */
    @Test
    public void aSiteWideFailureWhoseCleanupFailsIsCleanedUpOnce() throws Exception {
        when(anonUtils.isSiteWideScriptEnabled()).thenReturn(true);
        written.refuseDelete = true;

        final ServerException thrown = assertThrows(ServerException.class, () -> applyScripts(importer()));

        assertTrue(thrown.getCause() instanceof SecurityException);
        assertEquals("the failed cleanup should not be attempted again", 1, written.deletes);
    }

    @Test
    public void anInlineScriptThatFailsIsA400WithItsReasonAndOneCleanup() throws Exception {
        final ClientException thrown = assertThrows(ClientException.class, () -> applyScripts(importer(INLINE_SCRIPT)));

        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, thrown.getStatus());
        assertEquals("The supplied inline anonymization script could not be applied: " + REASON, thrown.getMessage());
        assertEquals("the failure should be cleaned up once", 1, written.deletes);
        assertFalse(written.exists());
    }

    /** A cleanup that fails is reported as it is, not retried by the inline branch's {@code catch (Throwable)}. */
    @Test
    public void anInlineFailureWhoseCleanupFailsIsCleanedUpOnce() throws Exception {
        written.refuseDelete = true;

        final ServerException thrown = assertThrows(ServerException.class, () -> applyScripts(importer(INLINE_SCRIPT)));

        assertTrue(thrown.getCause() instanceof SecurityException);
        assertEquals("the failed cleanup should not be attempted again", 1, written.deletes);
    }

    private static final String INLINE_SCRIPT = "(0010,0010) := subject";

    /**
     * Runs the scripts for a session this import did not create, so the cleanup is deleting the written file. The
     * script fails on this data, as {@code id.das} line 11 does when routing resolves no subject.
     */
    private void applyScripts(final GradualDicomImporter importer) throws Exception {
        importer.applyScripts(context -> new AnonymizationResultError(null, REASON), mock(SessionData.class), new AtomicBoolean(false), written);
    }

    private GradualDicomImporter importer() throws ServerException {
        return importer(null);
    }

    private GradualDicomImporter importer(final String inlineScript) throws ServerException {
        final Map<String, Object> parameters = new HashMap<>();
        if (inlineScript != null) {
            parameters.put(GradualDicomImporter.ANON_SCRIPT_PARAM, inlineScript);
        }
        // The status producer names its channel after the user, even when it is given one.
        return new GradualDicomImporter(null, mock(UserI.class), null, parameters);
    }

    /** The written file, counting the cleanup's attempts to delete it. */
    private static final class WrittenFile extends File {
        private int     deletes;
        private boolean refuseDelete;

        private WrittenFile(final File file) {
            super(file.getPath());
        }

        @Override
        public boolean delete() {
            deletes++;
            if (refuseDelete) {
                throw new SecurityException("delete refused");
            }
            return super.delete();
        }
    }
}
