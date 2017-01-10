/*
 * core: org.nrg.xft.db.PoolDBUtilsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.db;

import static org.junit.Assert.*;

import org.junit.Test;

public class PoolDBUtilsTest {

	@Test
	public void testHackCheck() {
		assertFalse(PoolDBUtils.HackCheck("DELETE FROM"));

		assertFalse(PoolDBUtils.HackCheck("DELETE _ FROM ."));

		assertTrue(PoolDBUtils.HackCheck("'DELETE _ FROM .'"));
	}

}
