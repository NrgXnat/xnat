//Copyright 2012 Radiologics, Inc.  All Rights Reserved
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
