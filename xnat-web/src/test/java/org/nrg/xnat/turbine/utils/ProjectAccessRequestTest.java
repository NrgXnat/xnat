/*
 * web: org.nrg.xnat.turbine.utils.ProjectAccessRequestTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.utils;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.nrg.xft.security.UserI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

/**
 * Unit tests for {@link ProjectAccessRequest#RequestPARByGUID(String, UserI)}. A guid that isn't a UUID returns null
 * without reaching {@link ProjectAccessRequest#RequestPAR}, which runs the query. {@code RequestPAR} also returns null
 * when the query fails, so the test checks that it is never called rather than only checking the result.
 */
public class ProjectAccessRequestTest {
    @Test
    public void requestByGuidReturnsNullForNonUuidWithoutQuerying() {
        try (final MockedStatic<ProjectAccessRequest> par = mockStatic(ProjectAccessRequest.class, CALLS_REAL_METHODS)) {
            final UserI user = mock(UserI.class);

            assertThat(ProjectAccessRequest.RequestPARByGUID(null, user)).isNull();
            assertThat(ProjectAccessRequest.RequestPARByGUID("", user)).isNull();
            assertThat(ProjectAccessRequest.RequestPARByGUID("not-a-guid", user)).isNull();
            assertThat(ProjectAccessRequest.RequestPARByGUID("3f2504e0-4f89-41d3-9a0c-0305e82c3301x", user)).isNull();
            assertThat(ProjectAccessRequest.RequestPARByGUID("3f2504e0-4f89-41d3-9a0c-0305e82c3301'", user)).isNull();

            par.verify(() -> ProjectAccessRequest.RequestPAR(any(), any()), never());
        }
    }

    @Test
    public void requestByGuidReturnsNullWhenLookupFailsWithoutUser() {
        // There is no database here, so the lookup fails. XnatAuthenticationFilter passes a null user, and the
        // failure must still come back as null rather than an exception.
        assertThat(ProjectAccessRequest.RequestPARByGUID("3f2504e0-4f89-41d3-9a0c-0305e82c3301", null)).isNull();
    }
}
