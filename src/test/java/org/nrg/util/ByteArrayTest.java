/*
 * DicomDB: org.nrg.util.ByteArrayTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class ByteArrayTest {

    /**
     * Test method for {@link org.nrg.util.ByteArray#ByteArray(byte[])}.
     */
    @Test
    public void testByteArrayByteArray() {
        final byte[] bytes = new byte[]{1, 2, 3};
        final ByteArray b = new ByteArray(bytes);
        assertSame(bytes, b.getBytes());
    }

    /**
     * Test method for {@link org.nrg.util.ByteArray#ByteArray(byte[], int, int)}.
     */
    @Test
    public void testByteArrayByteArrayIntInt() {
        final byte[] bytes = new byte[]{1, 2, 3, 4, 5};
        assertTrue(Arrays.equals(bytes, new ByteArray(bytes, 0, 5).getBytes()));
        assertTrue(Arrays.equals(new byte[]{2, 3, 4, 5},
                new ByteArray(bytes, 1, 4).getBytes()));
        assertTrue(Arrays.equals(new byte[]{3, 4},
                new ByteArray(bytes, 2, 2).getBytes()));
    }

    /**
     * Test method for {@link org.nrg.util.ByteArray#split(org.nrg.util.ByteArray)}.
     */
    @Test
    public void testSplit() {
        final byte[] b0 = new byte[]{1, 2, 3, 4, 5, 2, 3, 4, 5, 6, 3, 4, 5, 6, 7};
        final ByteArray ba0 = new ByteArray(b0);
        final Iterator<ByteArray> ba0i = ba0.split(new ByteArray(new byte[]{2, 3})).iterator();
        assertTrue(ba0i.hasNext());
        assertTrue(Arrays.equals(new byte[]{1}, ba0i.next().getBytes()));
        assertTrue(Arrays.equals(new byte[]{4, 5}, ba0i.next().getBytes()));
    }
    
    @Test
    public void testSplitBy() {
    	final byte[] b0 = new byte[]{1, 2, 3, 4, 5, 2, 3, 4, 5, 6, 3, 4, 5, 6, 7};
    	final ByteArray ba0 = new ByteArray(b0);
    	final Iterator<ByteArray> ba0i = ba0.splitBy(3).iterator();
    	assertTrue(ba0i.hasNext());
    	assertTrue(Arrays.equals(new byte[]{1, 2, 3}, ba0i.next().getBytes()));
    	assertTrue(Arrays.equals(new byte[]{4, 5, 2}, ba0i.next().getBytes()));
    	assertTrue(Arrays.equals(new byte[]{3, 4, 5}, ba0i.next().getBytes()));
    	assertTrue(Arrays.equals(new byte[]{6, 3, 4}, ba0i.next().getBytes()));
    	assertTrue(Arrays.equals(new byte[]{5, 6, 7}, ba0i.next().getBytes()));
    	assertFalse(ba0i.hasNext());
    	
    	final Iterator<ByteArray> ba0i2 = ba0.splitBy(6).iterator();
    	assertTrue(Arrays.equals(new byte[]{1, 2, 3, 4, 5, 2}, ba0i2.next().getBytes()));
    	assertTrue(Arrays.equals(new byte[]{3, 4, 5, 6, 3, 4}, ba0i2.next().getBytes()));
    	assertTrue(Arrays.equals(new byte[]{5, 6, 7}, ba0i2.next().getBytes()));
    	assertFalse(ba0i.hasNext());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSplitByInvalidArgument() {
    	final byte[] b0 = new byte[]{1, 2, 3};
    	final ByteArray ba0 = new ByteArray(b0);
    	ba0.splitBy(0);
    }
}
