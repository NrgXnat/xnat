package org.nrg.xnat.services.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.nrg.framework.jcache.JCacheHelper;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.event.XftItemEventI;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.cache.Cache;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nrg.xdat.security.helpers.AccessLevel.Collaborator;
import static org.nrg.xdat.security.helpers.AccessLevel.Delete;
import static org.nrg.xdat.security.helpers.AccessLevel.Edit;
import static org.nrg.xdat.security.helpers.AccessLevel.Member;
import static org.nrg.xdat.security.helpers.AccessLevel.Owner;
import static org.nrg.xdat.security.helpers.AccessLevel.Read;
import static org.nrg.xdat.security.helpers.Groups.OPERATION_REMOVE_USERS;
import static org.nrg.xnat.services.cache.DefaultUserProjectCache.CACHE_PROJECT_USER_ACCESS;

public class DefaultUserProjectCacheTest {
    private static final List<String> USER = Collections.singletonList("user");

    // Entries built by hasAccess() or the extractor hold the levels Permissions.getAllUserProjectAccess() returns: the
    // permission-derived level as well as the group's.

    @Test
    public void removingAMemberDropsTheirEntry() {
        assertMembershipChangeDropsEntry(Edit, Member);
    }

    @Test
    public void removingAnOwnerDropsTheirEntry() {
        assertMembershipChangeDropsEntry(Delete, Owner);
    }

    @Test
    public void removingACollaboratorDropsTheirEntry() {
        assertMembershipChangeDropsEntry(Read, Collaborator);
    }

    @Test
    public void addingAUserWithNoEntryLeavesNoEntry() {
        final Map<String, ArrayList<AccessLevel>> levels = new HashMap<>();

        DefaultUserProjectCache.applyMembershipChange(levels, USER);
        assertThat(levels).isEmpty();
    }

    @Test
    public void otherUsersKeepTheirEntries() {
        final Map<String, ArrayList<AccessLevel>> levels = new HashMap<>();
        levels.put("user", new ArrayList<>(Arrays.asList(Edit, Member)));
        levels.put("other", new ArrayList<>(Arrays.asList(Delete, Owner)));

        DefaultUserProjectCache.applyMembershipChange(levels, USER);
        assertThat(levels).containsOnlyKeys("other");
        assertThat(levels.get("other")).containsExactly(Delete, Owner);
    }

    // A non-member's cached Read on a public or protected project must not outlive the project being made private. A
    // member's levels come from their group, and other updates change no one's, so those stay cached.

    @Test
    public void aChangeOfAccessibilityDropsOnlyNonMembersLevels() throws IOException {
        final SerializerService serializer = mock(SerializerService.class);
        final DefaultUserProjectCache cache = projectEventCache(serializer);
        final Map<String, ArrayList<AccessLevel>> levels = new HashMap<>();
        levels.put("member", new ArrayList<>(Arrays.asList(Edit, Member)));
        levels.put("nonMember", new ArrayList<>(Collections.singletonList(Read)));
        @SuppressWarnings("unchecked") final Cache<String, String> accessLevels = mock(Cache.class);
        when(accessLevels.get("project")).thenReturn("cached");
        doReturn(accessLevels).when(cache).getCache(CACHE_PROJECT_USER_ACCESS, String.class, String.class);
        doReturn(levels).when(serializer).deserializeJson(eq("cached"), any(TypeReference.class));
        when(serializer.toJson(any())).thenReturn("written");
        doNothing().when(cache).cacheObject(anyString(), anyString(), any());

        cache.handleEventImpl(projectEvent(XftItemEventI.UPDATE, Collections.singletonMap("accessibility", "private")));
        assertThat(levels).containsOnlyKeys("member");
        verify(cache).cacheObject(CACHE_PROJECT_USER_ACCESS, "project", "written");
        verify(cache, never()).evict(CACHE_PROJECT_USER_ACCESS, "project");
    }

    @Test
    public void anUpdateThatKeepsTheAccessibilityKeepsTheAccessLevels() {
        final DefaultUserProjectCache cache = projectEventCache();

        cache.handleEventImpl(projectEvent(XftItemEventI.UPDATE, Collections.emptyMap()));
        verify(cache, never()).evict(CACHE_PROJECT_USER_ACCESS, "project");
    }

    @Test
    public void aProjectDeletionDropsItsUsersAccessLevels() {
        final DefaultUserProjectCache cache = projectEventCache();

        cache.handleEventImpl(projectEvent(XftItemEventI.DELETE, Collections.emptyMap()));
        verify(cache).evict(CACHE_PROJECT_USER_ACCESS, "project");
    }

    // A user removed from a project's custom group keeps no cached levels from it, and a group change with nothing
    // cached for the project loads nothing just to drop it.

    @Test
    public void aCustomGroupChangeDropsItsUsersEntries() throws IOException {
        final SerializerService serializer = mock(SerializerService.class);
        final DefaultUserProjectCache cache = groupEventCache(serializer);
        final Map<String, ArrayList<AccessLevel>> levels = new HashMap<>();
        levels.put("user", new ArrayList<>(Collections.singletonList(Read)));
        levels.put("other", new ArrayList<>(Arrays.asList(Edit, Member)));
        @SuppressWarnings("unchecked") final Cache<String, String> accessLevels = mock(Cache.class);
        when(accessLevels.get("project")).thenReturn("cached");
        doReturn(accessLevels).when(cache).getCache(CACHE_PROJECT_USER_ACCESS, String.class, String.class);
        doReturn(levels).when(serializer).deserializeJson(eq("cached"), any(TypeReference.class));
        when(serializer.toJson(any())).thenReturn("written");

        try (final MockedStatic<Groups> groups = Mockito.mockStatic(Groups.class, Mockito.CALLS_REAL_METHODS)) {
            final UserGroupI custom = mock(UserGroupI.class);
            when(custom.getTag()).thenReturn("project");
            groups.when(() -> Groups.getGroup("project_reviewers")).thenReturn(custom);

            final XftItemEventI event = groupEvent("project_reviewers", OPERATION_REMOVE_USERS, "user");
            assertThat(cache.matches(event)).as("the cache subscribes to a custom group's membership changes").isTrue();
            cache.handleEventImpl(event);
        }
        assertThat(levels).containsOnlyKeys("other");
        verify(cache).cacheObject(CACHE_PROJECT_USER_ACCESS, "project", "written");
    }

    @Test
    public void aGroupChangeWithNothingCachedLoadsNothing() {
        final DefaultUserProjectCache cache = groupEventCache(mock(SerializerService.class));
        @SuppressWarnings("unchecked") final Cache<String, String> accessLevels = mock(Cache.class);
        doReturn(accessLevels).when(cache).getCache(CACHE_PROJECT_USER_ACCESS, String.class, String.class);

        cache.handleEventImpl(groupEvent("project_member", OPERATION_REMOVE_USERS, "user"));
        verify(cache, never()).getCacheItem(eq(CACHE_PROJECT_USER_ACCESS), any(), any());
        verify(cache, never()).cacheObject(eq(CACHE_PROJECT_USER_ACCESS), anyString(), any());
    }

    // A check builds a user's levels without the project's lock, and builds them again if they were dropped meanwhile.

    @Test
    public void levelsDroppedWhileTheyWereBuiltAreBuiltAgain() throws Exception {
        final DefaultUserProjectCache cache = buildCache();
        @SuppressWarnings("unchecked") final Cache<String, String> nothingCached = mock(Cache.class);
        doReturn(nothingCached).when(cache).getCache(CACHE_PROJECT_USER_ACCESS, String.class, String.class);

        final AtomicInteger builds = new AtomicInteger();
        final List<AccessLevel> levels = cache.buildUserAccessLevels("project", "user", () -> {
            if (builds.incrementAndGet() == 1) {
                // The user is removed from the project while their first levels are built.
                cache.handleEventImpl(groupEvent("project_member", OPERATION_REMOVE_USERS, "user"));
                return Arrays.asList(Edit, Member);
            }
            return Collections.emptyList();
        });
        assertThat(builds).hasValue(2);
        assertThat(levels).isEmpty();
    }

    @Test(timeout = 30_000)
    public void checksOnOneProjectDoNotWaitForEachOthersBuilds() throws Exception {
        final DefaultUserProjectCache cache = buildCache();
        final CountDownLatch building = new CountDownLatch(1);
        final CountDownLatch release  = new CountDownLatch(1);
        final ExecutorService other   = Executors.newSingleThreadExecutor();
        try {
            final Future<List<AccessLevel>> slow = other.submit(() -> cache.buildUserAccessLevels("project", "slow", () -> {
                building.countDown();
                Uninterruptibles.awaitUninterruptibly(release);
                return Collections.singletonList(Read);
            }));
            building.await();
            assertThat(cache.buildUserAccessLevels("project", "fast", () -> Collections.singletonList(Read))).containsExactly(Read);
            release.countDown();
            assertThat(slow.get(10, TimeUnit.SECONDS)).containsExactly(Read);
        } finally {
            release.countDown();
            other.shutdownNow();
        }
    }

    /** A cache with nothing cached for the project, whose writes go nowhere. */
    private static DefaultUserProjectCache buildCache() throws IOException {
        final SerializerService serializer = mock(SerializerService.class);
        when(serializer.toJson(any())).thenReturn("written");
        final DefaultUserProjectCache cache = groupEventCache(serializer);
        doReturn(null).when(cache).getCacheItem(eq(CACHE_PROJECT_USER_ACCESS), any(), any());
        return cache;
    }

    /** A cache that knows the project, as after its creation event, and has it in the project cache. */
    private static DefaultUserProjectCache groupEventCache(final SerializerService serializer) {
        final DefaultUserProjectCache cache = projectEventCache(serializer);
        cache.handleEventImpl(projectEvent(XftItemEventI.CREATE, Collections.emptyMap()));
        doReturn(mock(XnatProjectdata.class)).when(cache).getCacheItem(eq(DefaultUserProjectCache.CACHE_PROJECTS), any(), any());
        doNothing().when(cache).cacheObject(anyString(), anyString(), any());
        return cache;
    }

    private static XftItemEventI groupEvent(final String groupId, final String operation, final String user) {
        final XftItemEventI event = mock(XftItemEventI.class);
        when(event.getXsiType()).thenReturn(XdatUsergroup.SCHEMA_ELEMENT_NAME);
        when(event.getId()).thenReturn(groupId);
        when(event.getAction()).thenReturn(XftItemEventI.UPDATE);
        final Map<String, Object> properties = new HashMap<>();
        properties.put(XftItemEventI.OPERATION, operation);
        properties.put(Groups.USERS, Collections.singleton(user));
        doReturn(properties).when(event).getProperties();
        return event;
    }

    private static DefaultUserProjectCache projectEventCache() {
        return projectEventCache(mock(SerializerService.class));
    }

    private static DefaultUserProjectCache projectEventCache(final SerializerService serializer) {
        final NamedParameterJdbcTemplate template = mock(NamedParameterJdbcTemplate.class);
        final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.getDataSource()).thenReturn(mock(DataSource.class));
        when(template.getJdbcOperations()).thenReturn(jdbcTemplate);
        final DefaultUserProjectCache cache = spy(new DefaultUserProjectCache(mock(JCacheHelper.class), mock(GroupsAndPermissionsCache.class), serializer, template, Collections.emptyList()));
        doReturn(null).when(cache).getCacheItem(eq(DefaultUserProjectCache.CACHE_PROJECTS), any(), any());
        doReturn(null).when(cache).evict(anyString(), anyString());
        return cache;
    }

    private static XftItemEventI projectEvent(final String action, final Map<String, ?> properties) {
        final XftItemEventI event = mock(XftItemEventI.class);
        when(event.getXsiType()).thenReturn(XnatProjectdata.SCHEMA_ELEMENT_NAME);
        when(event.getId()).thenReturn("project");
        when(event.getAction()).thenReturn(action);
        doReturn(properties).when(event).getProperties();
        return event;
    }

    private static void assertMembershipChangeDropsEntry(final AccessLevel permission, final AccessLevel group) {
        final Map<String, ArrayList<AccessLevel>> levels = new HashMap<>();
        levels.put("user", new ArrayList<>(Arrays.asList(permission, group)));

        DefaultUserProjectCache.applyMembershipChange(levels, USER);
        assertThat(levels).doesNotContainKey("user");
    }
}
