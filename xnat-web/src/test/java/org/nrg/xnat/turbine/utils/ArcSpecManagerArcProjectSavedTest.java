package org.nrg.xnat.turbine.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.om.base.auto.AutoArcArchivespecification;
import org.nrg.xft.db.PoolDBUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Saving a project's arc settings keeps the cached specification and drops only that project. Nothing that holds the
 * manager's lock writes {@code archive_specification.xml}: writing it loads every project.
 */
public class ArcSpecManagerArcProjectSavedTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private MockedStatic<PoolDBUtils>                 mockedDb;
    private MockedStatic<AutoArcArchivespecification> mockedSpecs;
    private ArcSpecManager.DeferredRun                savedRewrites;
    private ArcSpecManager.DeferredRun                rewrites;
    private File                                      cachePath;

    @Before
    public void setUp() throws Exception {
        cachePath = folder.newFolder("cache");
        mockedDb = Mockito.mockStatic(PoolDBUtils.class);
        mockedDb.when(() -> PoolDBUtils.ReturnStatisticQuery(anyString(), eq("last_modified"), any(), any())).thenReturn(null);
        // Each load returns a new instance, so a reload shows up as a different object.
        mockedSpecs = Mockito.mockStatic(AutoArcArchivespecification.class);
        mockedSpecs.when(() -> AutoArcArchivespecification.getAllArcArchivespecifications(any(), anyBoolean())).thenAnswer(invocation -> {
            final ArcArchivespecification spec = mock(ArcArchivespecification.class);
            when(spec.getGlobalCachePath()).thenReturn(cachePath.getAbsolutePath() + "/");
            return new ArrayList<>(Collections.singletonList(spec));
        });
        savedRewrites = ArcSpecManager.xmlRewrites;
        rewrites = mock(ArcSpecManager.DeferredRun.class);
        ArcSpecManager.xmlRewrites = rewrites;
        // The once-a-minute check ran a moment ago, so these lookups don't run it.
        ArcSpecManager.lastChecked = System.currentTimeMillis();
        ArcSpecManager.Reset();
    }

    @After
    public void tearDown() {
        ArcSpecManager.Reset();
        ArcSpecManager.lastModified = null;
        ArcSpecManager.xmlRewrites = savedRewrites;
        mockedSpecs.closeOnDemand();
        mockedDb.closeOnDemand();
    }

    @Test
    public void savingAnArcProjectKeepsTheSpecificationAndDropsOnlyThatProject() {
        final ArcArchivespecification loaded = ArcSpecManager.GetInstance(false);
        clearInvocations(rewrites);

        ArcSpecManager.arcProjectSaved("P1");

        verify(loaded).evictArcProject("P1");
        verify(rewrites).request();
        assertThat(ArcSpecManager.GetInstance(false)).isSameAs(loaded);
    }

    @Test
    public void aReloadLeavesTheXmlToTheBackgroundRewrite() {
        ArcSpecManager.GetInstance(false);

        assertThat(new File(cachePath, "archive_specification.xml")).doesNotExist();
        verify(rewrites).request();
    }
}
