/*
 * web: org.nrg.xnat.helpers.prearchive.TestPrearcUtilsAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.nrg.xft.utils.predicates.ProjectAccessPredicate.UNASSIGNED;

/**
 * XNAT-8806: prearchive access checks. Editing a session still requires edit access on its project, but reading one
 * is also open to all-data-access users, and a refused read or edit is always reported as a permission problem.
 */
public class TestPrearcUtilsAccess {
    private static final String  PROJECT   = "alpha";
    private static final boolean READ_ONLY = true;
    private static final boolean EDIT      = false;
    private static final boolean UNASSIGNED_ALLOWED = true;
    private static final boolean UNASSIGNED_DENIED  = false;

    private MockedStatic<Groups>     groups;
    private MockedStatic<UserHelper> userHelper;
    private UserI                    user;
    private UserHelperServiceI       helperService;

    @Before
    public void setUp() {
        user          = Mockito.mock(UserI.class);
        helperService = Mockito.mock(UserHelperServiceI.class);
        Mockito.when(user.getUsername()).thenReturn("reviewer");
        groups     = Mockito.mockStatic(Groups.class);
        userHelper = Mockito.mockStatic(UserHelper.class);
        userHelper.when(() -> UserHelper.getUserHelperService(user)).thenReturn(helperService);
    }

    @After
    public void tearDown() {
        userHelper.close();
        groups.close();
    }

    @Test
    public void readOnlyAllDataAccessUserMayReadProjectSession() throws Exception {
        givenAllDataAccess();

        assertThatCode(() -> PrearcUtils.checkPrearcAccess(user, PROJECT, UNASSIGNED_DENIED, READ_ONLY)).doesNotThrowAnyException();
    }

    @Test
    public void readOnlyAllDataAccessUserMayNotEditProjectSession() throws Exception {
        givenAllDataAccess();

        assertThatThrownBy(() -> PrearcUtils.checkPrearcAccess(user, PROJECT, UNASSIGNED_DENIED, EDIT)).isInstanceOf(InvalidPermissionException.class);
    }

    @Test
    public void readOnlyAllDataAccessUserMayReadUnassignedSession() throws Exception {
        givenAllDataAccess();

        assertThatCode(() -> PrearcUtils.checkPrearcAccess(user, UNASSIGNED, UNASSIGNED_DENIED, READ_ONLY)).doesNotThrowAnyException();
    }

    @Test
    public void readOnlyAllDataAccessUserMayNotEditUnassignedSession() throws Exception {
        givenAllDataAccess();

        assertThatThrownBy(() -> PrearcUtils.checkPrearcAccess(user, null, UNASSIGNED_DENIED, EDIT)).isInstanceOf(InvalidPermissionException.class);
    }

    @Test
    public void collaboratorMayNotReadProjectSession() throws Exception {
        givenNoSpecialAccess();

        assertThatThrownBy(() -> PrearcUtils.checkPrearcAccess(user, PROJECT, UNASSIGNED_DENIED, READ_ONLY)).isInstanceOf(InvalidPermissionException.class);
    }

    @Test
    public void plainUserRefusedUnassignedGetsPermissionErrorNotServerError() throws Exception {
        givenNoSpecialAccess();

        assertThatThrownBy(() -> PrearcUtils.checkPrearcAccess(user, UNASSIGNED, UNASSIGNED_DENIED, READ_ONLY)).isInstanceOf(InvalidPermissionException.class);
    }

    @Test
    public void projectEditorMayReadAndEditProjectSession() throws Exception {
        givenNoSpecialAccess();
        Mockito.when(helperService.hasEditAccessToSessionDataByTag(PROJECT)).thenReturn(true);

        assertThatCode(() -> PrearcUtils.checkPrearcAccess(user, PROJECT, UNASSIGNED_DENIED, READ_ONLY)).doesNotThrowAnyException();
        assertThatCode(() -> PrearcUtils.checkPrearcAccess(user, PROJECT, UNASSIGNED_DENIED, EDIT)).doesNotThrowAnyException();
    }

    @Test
    public void allDataAdminMayEditUnassignedSession() {
        groups.when(() -> Groups.hasAllDataAdmin(user)).thenReturn(true);
        groups.when(() -> Groups.hasAllDataAccess(user)).thenReturn(true);

        assertThatCode(() -> PrearcUtils.checkPrearcAccess(user, UNASSIGNED, UNASSIGNED_DENIED, EDIT)).doesNotThrowAnyException();
    }

    @Test
    public void allowUnassignedFlagStillBypassesUnassignedCheck() throws Exception {
        givenNoSpecialAccess();

        assertThatCode(() -> PrearcUtils.checkPrearcAccess(user, UNASSIGNED, UNASSIGNED_ALLOWED, EDIT)).doesNotThrowAnyException();
    }

    @Test
    public void nullUserBypassesChecks() {
        assertThatCode(() -> PrearcUtils.checkPrearcAccess(null, PROJECT, UNASSIGNED_DENIED, EDIT)).doesNotThrowAnyException();
        assertThatCode(() -> PrearcUtils.checkPrearcAccess(null, UNASSIGNED, UNASSIGNED_DENIED, EDIT)).doesNotThrowAnyException();
    }

    @Test
    public void readOnlyAllDataAccessUserCannotModifyProjectOrUnassignedSessions() throws Exception {
        givenAllDataAccess();

        assertThat(PrearcUtils.canModifyPrearchive(user, PROJECT)).isFalse();
        assertThat(PrearcUtils.canModifyPrearchive(user, UNASSIGNED)).isFalse();
    }

    @Test
    public void projectEditorCanModifyProjectSessions() throws Exception {
        givenNoSpecialAccess();
        Mockito.when(helperService.hasEditAccessToSessionDataByTag(PROJECT)).thenReturn(true);

        assertThat(PrearcUtils.canModifyPrearchive(user, PROJECT)).isTrue();
    }

    @Test
    public void allDataAdminCanModifyUnassignedSessions() {
        groups.when(() -> Groups.hasAllDataAdmin(user)).thenReturn(true);
        groups.when(() -> Groups.hasAllDataAccess(user)).thenReturn(true);

        assertThat(PrearcUtils.canModifyPrearchive(user, null)).isTrue();
    }

    @Test
    public void accessLookupFailureMeansCannotModify() throws Exception {
        givenNoSpecialAccess();
        Mockito.when(helperService.hasEditAccessToSessionDataByTag(PROJECT)).thenThrow(new IllegalStateException("no db"));

        assertThat(PrearcUtils.canModifyPrearchive(user, PROJECT)).isFalse();
    }

    private void givenAllDataAccess() throws Exception {
        groups.when(() -> Groups.hasAllDataAdmin(user)).thenReturn(false);
        groups.when(() -> Groups.hasAllDataAccess(user)).thenReturn(true);
        Mockito.when(helperService.hasEditAccessToSessionDataByTag(PROJECT)).thenReturn(false);
    }

    private void givenNoSpecialAccess() throws Exception {
        groups.when(() -> Groups.hasAllDataAdmin(user)).thenReturn(false);
        groups.when(() -> Groups.hasAllDataAccess(user)).thenReturn(false);
        Mockito.when(helperService.hasEditAccessToSessionDataByTag(PROJECT)).thenReturn(false);
    }
}
