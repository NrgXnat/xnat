package org.nrg.xnat.services.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.nrg.framework.jcache.JCacheHelper;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xnat.services.cache.extractors.DataExtractor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jms.core.JmsTemplate;

import javax.cache.Cache;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nrg.xdat.services.cache.GroupsAndPermissionsCache.ACTIONS;
import static org.nrg.xnat.services.cache.DefaultGroupsAndPermissionsCache.CACHE_ACTIONS;
import static org.nrg.xnat.services.cache.DefaultGroupsAndPermissionsCache.CACHE_GROUPS;

/**
 * A user's cache rebuild can go on for as long as events for that user keep arriving. These tests hold the first
 * rebuild an event starts and check what the event's handling has already done by then.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class DefaultGroupsAndPermissionsCacheRebuildOrderTest {
    private static final List<String> MEMBERS = Arrays.asList("alice", "bob", "carol");

    private final ExecutorService            handler        = Executors.newSingleThreadExecutor();
    private final NamedParameterJdbcTemplate template       = mock(NamedParameterJdbcTemplate.class);
    private final Set<String>                evictedActions = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean              countsReset    = new AtomicBoolean();
    private final AtomicReference<String>    heldUser       = new AtomicReference<>();
    private final CountDownLatch             held           = new CountDownLatch(1);
    private final CountDownLatch             released       = new CountDownLatch(1);

    private DefaultGroupsAndPermissionsCache cache;

    @Before
    public void setUp() {
        final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.getDataSource()).thenReturn(mock(DataSource.class));
        when(template.getJdbcOperations()).thenReturn(jdbcTemplate);
        cache = spy(new DefaultGroupsAndPermissionsCache(mock(JCacheHelper.class), mock(UserManagementServiceI.class), template, Collections.emptyList(), mock(JmsTemplate.class)));

        final UserGroupI group = mock(UserGroupI.class);
        when(group.getUsernames()).thenReturn(MEMBERS);
        final Cache<String, UserGroupI> groups = mock(Cache.class);
        when(groups.get("group")).thenReturn(group);
        doReturn(Collections.singletonMap(CACHE_GROUPS, mock(DataExtractor.class))).when(cache).getExtractors();
        doReturn(groups).when(cache).getCache(eq(CACHE_GROUPS), any(), any());

        doNothing().when(cache).clearUserCache(anyString());
        doReturn(null).when(cache).evict(anyString(), anyString());
        doAnswer(invocation -> evictedActions.add(invocation.getArgument(1) + ":" + invocation.getArgument(2))).when(cache).evictCacheMapPartition(eq(CACHE_ACTIONS), anyString(), anyString());
        doReturn(false).when(cache).isCacheEmpty(anyString(), any());
        doReturn(Collections.emptyList()).when(cache).getCacheList(anyString(), any(), any());
        doReturn(Collections.emptyMap()).when(cache).getBrowseableElementDisplays(anyString());
        doReturn(Collections.emptyList()).when(cache).getGroupsForProject(anyString());
        doAnswer(invocation -> {
            if (heldUser.compareAndSet(null, invocation.getArgument(0))) {
                held.countDown();
                released.await();
            }
            return Collections.emptyList();
        }).when(cache).getActionElementDisplays(anyString(), anyString());

        when(template.queryForList(anyString(), any(SqlParameterSource.class), eq(String.class))).thenReturn(Collections.singletonList("owner"));
        when(template.queryForObject(anyString(), any(SqlParameterSource.class), eq(Long.class))).thenAnswer(invocation -> {
            countsReset.set(true);
            return 1L;
        });
    }

    @After
    public void tearDown() {
        released.countDown();
        handler.shutdownNow();
    }

    @Test(timeout = 30_000)
    public void aGroupChangeClearsEveryMembersActionsBeforeRebuildingAny() throws Exception {
        final Future<Boolean> handling = handle(event(XdatUsergroup.SCHEMA_ELEMENT_NAME, "group"));
        assertThat(held.await(10, TimeUnit.SECONDS)).as("a rebuild started").isTrue();

        for (final String member : MEMBERS) {
            for (final String action : ACTIONS) {
                assertThat(evictedActions).as("%s's %s actions while %s's rebuild runs", member, action, heldUser.get()).contains(action + ":" + member);
            }
        }
        released.countDown();
        handling.get(10, TimeUnit.SECONDS);
    }

    @Test(timeout = 30_000)
    public void aProjectCreationFinishesItsOtherUpdatesBeforeRebuildingTheOwners() throws Exception {
        final Future<Boolean> handling = handle(event(XnatProjectdata.SCHEMA_ELEMENT_NAME, "project"));
        assertThat(held.await(10, TimeUnit.SECONDS)).as("a rebuild started").isTrue();

        assertThat(heldUser.get()).isEqualTo("owner");
        assertThat(countsReset).as("project count reset while the owner's rebuild runs").isTrue();
        released.countDown();
        handling.get(10, TimeUnit.SECONDS);
    }

    @Test(timeout = 30_000)
    public void aRebuildThatFailsEvictsWhatItCachedAndTheOthersStillRebuild() throws Exception {
        released.countDown();
        doThrow(new IllegalStateException("connection pool exhausted")).when(cache).getBrowseableElementDisplays("alice");

        handle(event(XdatUsergroup.SCHEMA_ELEMENT_NAME, "group")).get(10, TimeUnit.SECONDS);

        final InOrder order = inOrder(cache);
        order.verify(cache).getBrowseableElementDisplays("alice");
        order.verify(cache).clearUserCache("alice");
        for (final String member : MEMBERS) {
            verify(cache).getBrowseableElementDisplays(member);
        }
    }

    private Future<Boolean> handle(final XftItemEventI event) {
        return handler.submit(() -> cache.handleEventImpl(event));
    }

    private static XftItemEventI event(final String xsiType, final String id) {
        final XftItemEventI event = mock(XftItemEventI.class);
        when(event.getXsiType()).thenReturn(xsiType);
        when(event.getId()).thenReturn(id);
        when(event.getAction()).thenReturn(XftItemEventI.CREATE);
        when(event.getProperties()).thenReturn(Collections.emptyMap());
        return event;
    }
}
