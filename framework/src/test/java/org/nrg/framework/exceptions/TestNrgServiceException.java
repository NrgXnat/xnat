/*
 * framework: org.nrg.framework.exceptions.TestNrgServiceException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.exceptions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

public class TestNrgServiceException {
    @Test
    public void testBasicException() {
        NrgServiceException exception = new NrgServiceException();
        assertThat(exception).isNotNull()
                             .hasFieldOrPropertyWithValue("serviceError", NrgServiceError.Default);
    }

    @Test
    public void testExceptionWithErrorCode() {
        NrgServiceException exception = new NrgServiceException(NrgServiceError.Unknown);
        assertThat(exception).isNotNull()
                             .hasFieldOrPropertyWithValue("serviceError", NrgServiceError.Unknown);
    }
}
