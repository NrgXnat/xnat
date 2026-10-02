package org.nrg.xnat.services.cache;

import org.junit.Test;
import org.nrg.xdat.security.helpers.AccessLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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

    private static void assertMembershipChangeDropsEntry(final AccessLevel permission, final AccessLevel group) {
        final Map<String, ArrayList<AccessLevel>> levels = new HashMap<>();
        levels.put("user", new ArrayList<>(Arrays.asList(permission, group)));

        DefaultUserProjectCache.applyMembershipChange(levels, USER);
        assertThat(levels).doesNotContainKey("user");
    }
}
