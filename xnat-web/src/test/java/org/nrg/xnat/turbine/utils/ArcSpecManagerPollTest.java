package org.nrg.xnat.turbine.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.om.base.auto.AutoArcArchivespecification;
import org.nrg.xft.db.PoolDBUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

/**
 * Creating a project no longer resets the cached specification, so the once-a-minute check of its last-modified time
 * is what reloads it after a change the per-project refresh can't see, such as one to an arc project's paths, and on
 * the nodes that made no change at all. The check has to be armed by every load, not only by the loads that happen
 * to run it.
 */
public class ArcSpecManagerPollTest {
    private MockedStatic<PoolDBUtils>                 mockedDb;
    private MockedStatic<AutoArcArchivespecification> mockedSpecs;

    /** The last-modified time of the specification in the "database". */
    private Date specModified = new Date(1000);

    private long                       savedInterval;
    private ArcSpecManager.DeferredRun savedRewrites;

    @Before
    public void setUp() {
        mockedDb = Mockito.mockStatic(PoolDBUtils.class);
        mockedDb.when(() -> PoolDBUtils.ReturnStatisticQuery(anyString(), eq("last_modified"), any(), any())).thenAnswer(invocation -> specModified);
        // Each load returns a new instance, so a reload shows up as a different object.
        mockedSpecs = Mockito.mockStatic(AutoArcArchivespecification.class);
        mockedSpecs.when(() -> AutoArcArchivespecification.getAllArcArchivespecifications(any(), anyBoolean()))
                   .thenAnswer(invocation -> new ArrayList<>(Collections.singletonList(mock(ArcArchivespecification.class))));
        savedInterval = ArcSpecManager.lastCheckedInterval;
        ArcSpecManager.lastCheckedInterval = 60000L;
        // A reload asks for the XML rewrite, which would otherwise run on the real background thread.
        savedRewrites = ArcSpecManager.xmlRewrites;
        ArcSpecManager.xmlRewrites = mock(ArcSpecManager.DeferredRun.class);
        ArcSpecManager.Reset();
    }

    @After
    public void tearDown() {
        ArcSpecManager.Reset();
        ArcSpecManager.lastModified = null;
        ArcSpecManager.lastCheckedInterval = savedInterval;
        ArcSpecManager.xmlRewrites = savedRewrites;
        mockedSpecs.closeOnDemand();
        mockedDb.closeOnDemand();
    }

    @Test
    public void aLoadThatDidNotRunTheCheckStillArmsIt() {
        // The check ran a moment ago, so this load doesn't run it.
        ArcSpecManager.lastChecked = System.currentTimeMillis();
        final ArcArchivespecification loaded = ArcSpecManager.GetInstance(false);

        specModified = new Date(2000);
        ArcSpecManager.lastChecked = 0L; // a minute has passed

        assertThat(ArcSpecManager.GetInstance(false)).as("a change after the load must reload it at the next check").isNotSameAs(loaded);
    }

    @Test
    public void aTimeSetBackStillReloads() {
        ArcSpecManager.lastChecked = System.currentTimeMillis();
        specModified = new Date(2000);
        final ArcArchivespecification loaded = ArcSpecManager.GetInstance(false);

        // Earlier than the time last read, as after a restore: still a change.
        specModified = new Date(1500);
        ArcSpecManager.lastChecked = 0L;

        assertThat(ArcSpecManager.GetInstance(false)).isNotSameAs(loaded);
    }

    @Test
    public void anUnchangedSpecificationIsKept() {
        ArcSpecManager.lastChecked = System.currentTimeMillis();
        final ArcArchivespecification loaded = ArcSpecManager.GetInstance(false);

        ArcSpecManager.lastChecked = 0L;

        assertThat(ArcSpecManager.GetInstance(false)).isSameAs(loaded);
    }

    @Test
    public void betweenChecksTheCachedSpecificationIsReturned() {
        ArcSpecManager.lastChecked = System.currentTimeMillis();
        final ArcArchivespecification loaded = ArcSpecManager.GetInstance(false);

        specModified = new Date(2000);

        assertThat(ArcSpecManager.GetInstance(false)).isSameAs(loaded);
    }
}
