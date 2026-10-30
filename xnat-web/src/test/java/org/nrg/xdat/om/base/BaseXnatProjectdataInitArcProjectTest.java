package org.nrg.xdat.om.base;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

/**
 * Creating a project saves its arc project and tells the archive specification which project that was, instead of
 * resetting it: a reset made the next lookup load every project.
 */
public class BaseXnatProjectdataInitArcProjectTest {
    @Test
    public void creatingAProjectKeepsTheArchiveSpecification() throws Exception {
        final XnatProjectdata project = mock(XnatProjectdata.class, CALLS_REAL_METHODS);
        doReturn("P1").when(project).getId();
        final ArcProject arcProject = mock(ArcProject.class);

        try (final MockedStatic<ArcSpecManager> manager = Mockito.mockStatic(ArcSpecManager.class);
             final MockedStatic<SaveItemHelper> saves = Mockito.mockStatic(SaveItemHelper.class);
             final MockedStatic<XDAT> ignored = Mockito.mockStatic(XDAT.class)) {
            manager.when(ArcSpecManager::GetInstance).thenReturn(mock(ArcArchivespecification.class));

            project.initArcProjectImpl(arcProject, mock(UserI.class), mock(EventMetaI.class));

            saves.verify(() -> SaveItemHelper.authorizedSave(eq(arcProject), any(), eq(true), eq(false), any(EventMetaI.class)));
            manager.verify(() -> ArcSpecManager.arcProjectSaved("P1"));
            manager.verify(ArcSpecManager::Reset, never());
        }
    }
}
