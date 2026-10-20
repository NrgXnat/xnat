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
import org.nrg.action.ServerException;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.restlet.data.Status;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * What the uploader is shown when anonymization fails. {@code ServerException(Status, Throwable)} delegates to
 * {@code Exception(Throwable)}, whose message is {@code cause.toString()}, so wrapping a failure that is already a
 * {@link ServerException} prepends {@code "org.nrg.action.ServerException: "} to the text -- and the site-wide script
 * path used to do it twice, once on the way out of the error branch and again when its own
 * {@code catch (Throwable)} re-entered the handler. The reason has to arrive unprefixed: it is the text the uploader
 * is shown, and two copies of a class name push the part that names the failing script line out of view.
 */
public class AnonymizationErrorSurfacingTest {
    private static final String REASON = "line 11: Variable 'subject' is null.";

    private MockedStatic<XDAT> mockedXDAT;

    @Before
    public void setUp() {
        // ImporterHandlerA looks up importer packages in the Spring context when the class loads.
        final ContextService contextService = mock(ContextService.class);
        doReturn(Collections.emptyMap()).when(contextService).getBeansOfType(any());
        mockedXDAT = Mockito.mockStatic(XDAT.class);
        mockedXDAT.when(XDAT::getContextService).thenReturn(contextService);
    }

    @After
    public void tearDown() {
        mockedXDAT.closeOnDemand();
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
        final ServerException real  = new ServerException(Status.SERVER_ERROR_INTERNAL, REASON, new RuntimeException());
        final Throwable       thrown = handleAnonymizationError(real);

        assertSame("the handler should carry the exception, not rebuild it", real, thrown);
        assertEquals("the uploader's reason must arrive unprefixed", REASON, thrown.getMessage());
    }

    /**
     * The control, without which the test above would pass on a handler that wrapped nothing at all: a failure that
     * is not an {@link org.nrg.action.ActionException} still has to become one, and that is where the prefix is
     * legitimate, because there is no other message to show.
     */
    @Test
    public void aFailureThatIsNotAnActionExceptionIsStillWrapped() throws Exception {
        final RuntimeException raw    = new RuntimeException("no such column");
        final Throwable        thrown = handleAnonymizationError(raw);

        assertFalse("a raw failure has to be given a status", raw == thrown);
        assertTrue(thrown instanceof ServerException);
        assertSame(raw, thrown.getCause());
        assertEquals(Status.SERVER_ERROR_INTERNAL, ((ServerException) thrown).getStatus());
        assertEquals("java.lang.RuntimeException: no such column", thrown.getMessage());
    }

    /**
     * Drives the private 4-argument {@code handleAnonymizationError}. With {@code isNew} false it deletes the written
     * file and throws, touching no instance state, so it runs against an uninitialized instance and needs no
     * database or Spring context. Asserts the cleanup happened too, since that is the handler's other job.
     *
     * @return the exception the handler threw
     */
    private Throwable handleAnonymizationError(final Throwable cause) throws Exception {
        final File written = Files.createTempFile("anon-error-surfacing", ".dcm").toFile();
        assertTrue(written.exists());

        final Method handler = GradualDicomImporter.class.getDeclaredMethod(
                "handleAnonymizationError", AtomicBoolean.class, SessionData.class, File.class, Throwable.class);
        handler.setAccessible(true);

        try {
            handler.invoke(Mockito.mock(GradualDicomImporter.class), new AtomicBoolean(false), null, written, cause);
            throw new AssertionError("the handler is required to throw");
        } catch (final InvocationTargetException e) {
            assertFalse("the written file should have been cleaned up", written.exists());
            return e.getCause();
        } finally {
            written.delete();
        }
    }
}
