package org.nrg.xnat.services.cache;

import org.junit.Test;
import org.nrg.xdat.security.helpers.AccessLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nrg.xdat.security.helpers.AccessLevel.Member;
import static org.nrg.xdat.security.helpers.AccessLevel.Owner;

public class DefaultUserProjectCacheTest {
    private static final List<String> USER = Collections.singletonList("user");

    @Test
    public void promotingAMemberAndThenRemovingThemLeavesNoAccess() {
        final Map<String, ArrayList<AccessLevel>> levels = new HashMap<>();

        DefaultUserProjectCache.applyMembershipChange(levels, USER, true, Member, Collections.emptyList());
        // Promotion is one event: added to the owners, with the members group reported as removed.
        DefaultUserProjectCache.applyMembershipChange(levels, USER, true, Owner, Collections.singletonList(Member));
        assertThat(levels.get("user")).containsExactly(Owner);

        DefaultUserProjectCache.applyMembershipChange(levels, USER, false, Owner, Collections.emptyList());
        assertThat(levels.get("user")).isEmpty();
    }

    @Test
    public void addingAndRemovingAMemberLeavesNoAccess() {
        final Map<String, ArrayList<AccessLevel>> levels = new HashMap<>();

        DefaultUserProjectCache.applyMembershipChange(levels, USER, true, Member, Collections.emptyList());
        assertThat(levels.get("user")).containsExactly(Member);

        DefaultUserProjectCache.applyMembershipChange(levels, USER, false, Member, Collections.emptyList());
        assertThat(levels.get("user")).isEmpty();
    }

    @Test
    public void aRemovedGroupTheUserWasNotInChangesNothingElse() {
        final Map<String, ArrayList<AccessLevel>> levels = new HashMap<>();

        DefaultUserProjectCache.applyMembershipChange(levels, USER, true, Owner, Collections.singletonList(Member));
        assertThat(levels.get("user")).containsExactly(Owner);
    }
}
