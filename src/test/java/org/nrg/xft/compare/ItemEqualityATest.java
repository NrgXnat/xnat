/*
 * core: org.nrg.xft.compare.ItemEqualityATest
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * Copyright 2010 Washington University
 */
package org.nrg.xft.compare;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;

/**
 * @author timo
 *
 */
public class ItemEqualityATest {
	ItemEqualityI checker=new FakeItemEqualityA();
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.nrg.xft.compare.ItemEqualityA#isEqualTo(org.nrg.xft.XFTItem, org.nrg.xft.XFTItem)}.
	 */
	@Test
	public void testIsEqualTo() throws Exception{
		XFTItem newI = mock(XFTItem.class);
		XFTItem oldI = mock(XFTItem.class);
		
		when(newI.getXSIType()).thenReturn("xnat:mrSessionData");
		when(oldI.getXSIType()).thenReturn("xnat:subjectData");
		
		if(checker.isEqualTo(newI, oldI)){
			fail("Invalid comparison types.");
		}
	}

	/**
	 * Test method for {@link org.nrg.xft.compare.ItemEqualityA#isEqualTo(org.nrg.xft.XFTItem, org.nrg.xft.XFTItem)}.
	 */
	@Test
	public void testEmptyProperties1() throws Exception{
		XFTItem newI = mock(XFTItem.class);
		XFTItem oldI = mock(XFTItem.class);
		
		when(newI.getXSIType()).thenReturn("xnat:mrSessionData");
		when(oldI.getXSIType()).thenReturn("xnat:mrSessionData");

		when(newI.hasProperties()).thenReturn(true);
		when(oldI.hasProperties()).thenReturn(false);
		
		
		if(checker.isEqualTo(newI, oldI)){
			fail("One type has no properties set.");
		}
	}

	/**
	 * Test method for {@link org.nrg.xft.compare.ItemEqualityA#isEqualTo(org.nrg.xft.XFTItem, org.nrg.xft.XFTItem)}.
	 */
	@Test
	public void testEmptyProperties2() throws Exception{
		XFTItem newI = mock(XFTItem.class);
		XFTItem oldI = mock(XFTItem.class);
		
		when(newI.getXSIType()).thenReturn("xnat:mrSessionData");
		when(oldI.getXSIType()).thenReturn("xnat:mrSessionData");

		when(oldI.hasProperties()).thenReturn(true);
		when(newI.hasProperties()).thenReturn(false);
		
		
		if(checker.isEqualTo(newI, oldI)){
			fail("One type has no properties set.");
		}
	}

	/**
	 * Test method for {@link org.nrg.xft.compare.ItemEqualityA#isEqualTo(org.nrg.xft.XFTItem, org.nrg.xft.XFTItem)}.
	 */
	@Test
	public void testTrue() throws Exception{
		XFTItem newI = mock(XFTItem.class);
		XFTItem oldI = mock(XFTItem.class);
		
		when(newI.getXSIType()).thenReturn("xnat:mrSessionData");
		when(oldI.getXSIType()).thenReturn("xnat:mrSessionData");

		when(oldI.hasProperties()).thenReturn(true);
		when(newI.hasProperties()).thenReturn(true);
		
		if(!checker.isEqualTo(newI, oldI)){
			fail("Expected comparison to pass.");
		}
	}


	static class FakeItemEqualityA extends ItemEqualityA {

		/* (non-Javadoc)
		 * @see org.nrg.xft.compare.ItemEqualityA#doCheck(org.nrg.xft.XFTItem, org.nrg.xft.XFTItem)
		 */
		@Override
		public boolean doCheck(XFTItem newI, XFTItem oldI) throws XFTInitException, ElementNotFoundException, FieldNotFoundException, Exception {
			return true;
		}
		
	}
}
