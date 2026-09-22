/*
 * web: org.nrg.xnat.restlet.resources.prearchive.TestPrearcSessionListResourceProjects
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.prearchive;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.nrg.xdat.security.services.PermissionsServiceI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * XNAT-8806: the prearchive listing must show every session, assigned or unassigned, to a user with
 * all-data-access, while still hiding project sessions from plain collaborators.
 */
public class TestPrearcSessionListResourceProjects {
    private static final String USERNAME = "reviewer";

    private PermissionsServiceI permissions;

    @Before
    public void setUp() {
        permissions = Mockito.mock(PermissionsServiceI.class);
        Mockito.when(permissions.getUserReadableProjects(USERNAME)).thenReturn(Arrays.asList("alpha", "beta"));
        Mockito.when(permissions.getUserEditableProjects(USERNAME)).thenReturn(Collections.emptyList());
    }

    @Test
    public void readOnlyAllDataAccessUserListsEveryReadableProjectPlusUnassigned() {
        final List<String> projects = PrearcSessionListResource.resolveProjects(null, true, USERNAME, permissions);

        assertThat(projects).containsExactly("alpha", "beta", null);
    }

    @Test
    public void userWithoutAllDataAccessListsOnlyEditableProjects() {
        Mockito.when(permissions.getUserEditableProjects(USERNAME)).thenReturn(Collections.singletonList("alpha"));

        final List<String> projects = PrearcSessionListResource.resolveProjects(null, false, USERNAME, permissions);

        assertThat(projects).containsExactly("alpha");
    }

    @Test
    public void collaboratorWithNoEditableProjectsListsNothing() {
        final List<String> projects = PrearcSessionListResource.resolveProjects(null, false, USERNAME, permissions);

        assertThat(projects).isEmpty();
    }

    @Test
    public void explicitProjectRequestIsSplitOnCommas() {
        final List<String> projects = PrearcSessionListResource.resolveProjects("alpha, beta", false, USERNAME, permissions);

        assertThat(projects).containsExactly("alpha", "beta");
        Mockito.verifyNoInteractions(permissions);
    }

    @Test
    public void explicitProjectRequestStillIncludesUnassignedForAllDataAccess() {
        final List<String> projects = PrearcSessionListResource.resolveProjects("alpha", true, USERNAME, permissions);

        assertThat(projects).containsExactly("alpha", null);
    }
}
