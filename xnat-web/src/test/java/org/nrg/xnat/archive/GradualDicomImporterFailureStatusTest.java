package org.nrg.xnat.archive;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.nrg.dicom.dicomedit.DE6ScriptParser;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.helpers.prearchive.SessionException;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Failures the uploader causes are a 400 even when they surface as the server's own exceptions: another session already
 * at the destination folder, and an inline script that can't be parsed or run. Everything else stays a 500.
 */
public class GradualDicomImporterFailureStatusTest {
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

    @Test
    public void anotherSessionAtTheDestinationIsTheUploadersFault() {
        // As PostgreSQL reports it, wrapped or not, and as the prearchive's own duplicate check does for no project.
        assertTrue(GradualDicomImporter.isDestinationTaken(new SQLException("duplicate key value violates unique constraint \"prearchive_study\"", "23505")));
        assertTrue(GradualDicomImporter.isDestinationTaken(new Exception(new SQLException("duplicate key", "23505"))));
        assertTrue(GradualDicomImporter.isDestinationTaken(new SQLIntegrityConstraintViolationException("duplicate key")));
        assertTrue(GradualDicomImporter.isDestinationTaken(new SessionException(SessionException.Error.AlreadyExists, "Trying to add an existing session")));
    }

    @Test
    public void otherFailuresToCreateTheSessionAreTheServers() {
        assertFalse(GradualDicomImporter.isDestinationTaken(new SQLException("connection refused", "08001")));
        assertFalse(GradualDicomImporter.isDestinationTaken(new SessionException(SessionException.Error.DatabaseError, "Multiple records")));
        assertFalse(GradualDicomImporter.isDestinationTaken(new IllegalStateException("no database")));
    }

    @Test
    public void aScriptThatCannotBeParsedIsTheUploadersFault() {
        final MizerException parseFailure = assertThrows(MizerException.class,
                                                         () -> new DE6ScriptParser().parse(Collections.singletonList("(0010,0010 := ((( not a script")));
        assertTrue(GradualDicomImporter.isScriptFailure(parseFailure));
    }

    @Test
    public void aScriptFailingToReadOrWriteTheInstanceIsTheServers() {
        assertFalse(GradualDicomImporter.isScriptFailure(new MizerException(new IOException("No space left on device"))));
        assertFalse(GradualDicomImporter.isScriptFailure(new MizerException(new MizerException(new IOException("Stream closed")))));
    }
}
