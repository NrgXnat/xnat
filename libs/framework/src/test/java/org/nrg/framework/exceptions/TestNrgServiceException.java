/*
 * framework: org.nrg.framework.exceptions.TestNrgServiceException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.exceptions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestNrgServiceException {
	@Test
	public void testBasicException() {
		NrgServiceException exception = new NrgServiceException();
		assertEquals(NrgServiceError.Default, exception.getServiceError());
	}

	@Test
	public void testExceptionWithErrorCode() {
	    NrgServiceException exception = new NrgServiceException(NrgServiceError.Unknown);
	    assertEquals(NrgServiceError.Unknown, exception.getServiceError());
	}
}
