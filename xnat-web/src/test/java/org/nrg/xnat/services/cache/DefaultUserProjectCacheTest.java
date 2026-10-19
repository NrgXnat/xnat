package org.nrg.xnat.services.cache;

import org.junit.Test;
import org.nrg.framework.jcache.JCacheHelper;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.event.XftItemEventI;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nrg.xnat.services.cache.DefaultUserProjectCache.CACHE_PROJECT_USER_ACCESS;
import static org.nrg.xdat.security.helpers.AccessLevel.Collaborator;
import static org.nrg.xdat.security.helpers.AccessLevel.Delete;
import static org.nrg.xdat.security.helpers.AccessLevel.Edit;
import static org.nrg.xdat.security.helpers.AccessLevel.Member;
import static org.nrg.xdat.security.helpers.AccessLevel.Owner;
import static org.nrg.xdat.security.helpers.AccessLevel.Read;

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

    // A non-member's cached Read on a public or protected project must not outlive the project being made private, and a
    // project deleted and created again with the same ID must not inherit the old project's levels.

    @Test
    public void aProjectUpdateDropsItsUsersAccessLevels() {
        final DefaultUserProjectCache cache = projectEventCache();

        cache.handleEventImpl(projectEvent(XftItemEventI.UPDATE));
        verify(cache).evict(CACHE_PROJECT_USER_ACCESS, "project");
    }

    @Test
    public void aProjectDeletionDropsItsUsersAccessLevels() {
        final DefaultUserProjectCache cache = projectEventCache();

        cache.handleEventImpl(projectEvent(XftItemEventI.DELETE));
        verify(cache).evict(CACHE_PROJECT_USER_ACCESS, "project");
    }

    private static DefaultUserProjectCache projectEventCache() {
        final NamedParameterJdbcTemplate template = mock(NamedParameterJdbcTemplate.class);
        final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.getDataSource()).thenReturn(mock(DataSource.class));
        when(template.getJdbcOperations()).thenReturn(jdbcTemplate);
        final DefaultUserProjectCache cache = spy(new DefaultUserProjectCache(mock(JCacheHelper.class), mock(GroupsAndPermissionsCache.class), mock(SerializerService.class), template, Collections.emptyList()));
        doReturn(null).when(cache).getCacheItem(eq(DefaultUserProjectCache.CACHE_PROJECTS), any(), any());
        doReturn(null).when(cache).evict(anyString(), anyString());
        return cache;
    }

    private static XftItemEventI projectEvent(final String action) {
        final XftItemEventI event = mock(XftItemEventI.class);
        when(event.getXsiType()).thenReturn(XnatProjectdata.SCHEMA_ELEMENT_NAME);
        when(event.getId()).thenReturn("project");
        when(event.getAction()).thenReturn(action);
        return event;
    }

    private static void assertMembershipChangeDropsEntry(final AccessLevel permission, final AccessLevel group) {
        final Map<String, ArrayList<AccessLevel>> levels = new HashMap<>();
        levels.put("user", new ArrayList<>(Arrays.asList(permission, group)));

        DefaultUserProjectCache.applyMembershipChange(levels, USER);
        assertThat(levels).doesNotContainKey("user");
    }
}
