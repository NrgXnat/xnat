/**
 * Copyright (c) 2007,2010 Washington University
 */
package org.nrg.attr;

import java.util.Iterator;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public class MutableAttrDefsTest {
    /**
     * Test method for {@link org.nrg.attr.MutableAttrDefs#add(org.nrg.attr.ExtAttrDefString)}.
     */
    @Test
    public void testAddExtAttrDefOfSV() {
        final MutableAttrDefs<NativeAttr> ads = new MutableAttrDefs<NativeAttr>();
        Iterator<ExtAttrDef<NativeAttr>> i = ads.iterator();
        assertFalse(i.hasNext());

        final ExtAttrDef<NativeAttr> extA = new SingleValueTextAttr<NativeAttr>("ext-A", NativeAttr.A);
        ads.add(extA);
        i = ads.iterator();
        assertEquals(extA, i.next());
        assertFalse(i.hasNext());
    }

    /**
     * Test method for {@link org.nrg.attr.MutableAttrDefs#add(java.lang.String)}.
     */
    @Test
    public void testAddString() {
        final MutableAttrDefs<NativeAttr> ads = new MutableAttrDefs<NativeAttr>();
        Iterator<ExtAttrDef<NativeAttr>> i = ads.iterator();
        assertFalse(i.hasNext());

        final ExtAttrDef<NativeAttr> empty = new ConstantAttrDef<NativeAttr>("empty", null);
        ads.add("empty");
        i = ads.iterator();
        assertEquals(empty, i.next());
        assertFalse(i.hasNext());
    }

    /**
     * Test method for {@link org.nrg.attr.MutableAttrDefs#add(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testAddStringString() {
        final MutableAttrDefs<NativeAttr> ads = new MutableAttrDefs<NativeAttr>();
        Iterator<ExtAttrDef<NativeAttr>> i = ads.iterator();
        assertFalse(i.hasNext());

        final ExtAttrDef<NativeAttr> foo = new ConstantAttrDef<NativeAttr>("foo", "bar");
        ads.add("foo", "bar");
        i = ads.iterator();
        assertEquals(foo, i.next());
        assertFalse(i.hasNext());
    }

    /**
     * Test method for {@link org.nrg.attr.MutableAttrDefs#add(java.lang.String, java.lang.Object)}.
     */
    @Test
    public void testAddStringS() {
        final MutableAttrDefs<NativeAttr> ads = new MutableAttrDefs<NativeAttr>();
        Iterator<ExtAttrDef<NativeAttr>> i = ads.iterator();
        assertFalse(i.hasNext());

        final ExtAttrDef<NativeAttr> extA = new SingleValueTextAttr<NativeAttr>("extA", NativeAttr.A);
        ads.add("extA", NativeAttr.A);
        i = ads.iterator();
        assertEquals(extA, i.next());
        assertFalse(i.hasNext());
    }

    /**
     * Test method for {@link org.nrg.attr.MutableAttrDefs#add(org.nrg.attr.ReadableAttrDefSet<S,V>[])}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAddReadableAttrDefSetOfSVArray() {
        final MutableAttrDefs<NativeAttr> ads = new MutableAttrDefs<NativeAttr>();
        Iterator<ExtAttrDef<NativeAttr>> i = ads.iterator();
        assertFalse(i.hasNext());

        ads.add(NativeAttr.frads);
        i = ads.iterator();
        assertEquals(NativeAttr.fextA, i.next());
        assertEquals(NativeAttr.fextC_BA, i.next());
        assertFalse(i.hasNext());
    }


    /**
     * Test method for {@link org.nrg.attr.MutableAttrDefs#iterator()}.
     */
    @Test
    public void testIterator() {
        final MutableAttrDefs<NativeAttr> ads = new MutableAttrDefs<NativeAttr>();
        Iterator<ExtAttrDef<NativeAttr>> i = ads.iterator();
        assertFalse(i.hasNext());

        ads.add("empty");
        i = ads.iterator();
        assertEquals(new ConstantAttrDef<NativeAttr>("empty", null), i.next());
        assertFalse(i.hasNext());
    }

    /**
     * Test method for {@link org.nrg.attr.MutableAttrDefs#getNativeAttrs()}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetNativeAttrs() {
        final MutableAttrDefs<NativeAttr> ads = new MutableAttrDefs<NativeAttr>();
        Iterator<NativeAttr> i = ads.getNativeAttrs().iterator();
        assertFalse(i.hasNext());

        ads.add(NativeAttr.frads);

        i = ads.getNativeAttrs().iterator();
        assertEquals(NativeAttr.A, i.next());
        assertEquals(NativeAttr.B, i.next());
        assertEquals(NativeAttr.C, i.next());
        assertFalse(i.hasNext());
    }
}
