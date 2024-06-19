/*
 * core: org.nrg.xft.XFTTableSortTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * 
 */
package org.nrg.xft;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import com.google.common.base.Joiner;
import org.junit.Test;

/**
 * @author timo
 *
 */
public class XFTTableSortTest {
	public static final String[] COLUMNS={"String","float","int","date"};
		
	private XFTTable buildRandomTable(int numRows){
		final Random random = new Random();
		final XFTTable t = new XFTTable();
		t.initTable(COLUMNS);

		for(int i=0;i<numRows;i++){
			Object[] row=new Object[COLUMNS.length];
			row[0]=new Long(random.nextLong()).toString();
			row[1]=new Float(random.nextFloat());
			row[2]=new Integer(random.nextInt());
			row[3]=new Date(random.nextLong());
			t.rows().add(row);
		}
		
		return t;
	}
	
	/**
	 * Test method for {@link org.nrg.xft.XFTTable#sort(java.util.List)}.
	 */
	@Test
	public void testSortList() {
		//This test has a 1/numRows chance of failing due to chance, which is why a large numRows is used here.
		final XFTTable t = buildRandomTable(1000000);
		
		final Object[] o1=t.rows().get(0);
		
		t.sort(Arrays.asList(COLUMNS));
		
		final Object[] o2=t.rows().get(0);
		
		if(o1==o2){
			fail("Objects should not be equal:" + System.lineSeparator() + System.lineSeparator() + " * o1: [" + Joiner.on(", ").join(o1) + "]" + System.lineSeparator() + " * o2: [" + Joiner.on(", ").join(o2) + "]");
		}
	}
	
	/**
	 * Test method for {@link org.nrg.xft.XFTTable#sort(java.util.List)}.
	 */
	@Test
	public void testSortListNull() {
		
		final XFTTable t = buildRandomTable(100);
		
		final Object[] o1=t.rows().get(0);
		
		try {
			t.sort(null);
			fail("Should throw NullPointerException");
		} catch (NullPointerException e) {}
		
	}
	
	/**
	 * Test method for {@link org.nrg.xft.XFTTable#sort(java.util.List)}.
	 */
	@Test
	public void testSortListEmpty() {
		
		final XFTTable t = buildRandomTable(100);
		
		final Object[] o1=t.rows().get(0);
		
		t.sort(new ArrayList<String>());
		
		final Object[] o2=t.rows().get(0);
		
		if(o1!=o2){
			fail("Objects should be equal.");
		}
	}

	/**
	 * Test method for {@link org.nrg.xft.XFTTable#reverse()}.
	 */
	@Test
	public void testReverse() {
		final XFTTable t = buildRandomTable(100);
		
		final Object[] o1=t.rows().get(0);

		t.reverse();
		
		final Object[] o2=t.rows().get(0);
		
		if(o1==o2){
			fail("Objects should not be equal.");
		}
		
	}

}