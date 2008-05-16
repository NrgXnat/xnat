/**
 * $Id: AttrDefSetTest.java,v 1.2 2007/08/09 22:55:13 karchie Exp $
 * Copyright (c) 2007 Washington University
 */
package org.nrg.attr;

import java.util.Iterator;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author karchie
 *
 */
public class AttrDefSetTest {
  /**
   * Test method for {@link org.nrg.attr.AttrDefSet#add(org.nrg.attr.ExtAttrDef)}.
   */
  @Test
  public void testAddExtAttrDefOfSV() {
    final AttrDefSet<NativeAttr,Float> ads = new AttrDefSet<NativeAttr,Float>();
    Iterator<ExtAttrDef<NativeAttr,Float>> i = ads.iterator();
    assertFalse(i.hasNext());

    final ExtAttrDef<NativeAttr,Float> extA = 
      new ExtAttrDef.Text<NativeAttr,Float>("ext-A", NativeAttr.A);
    ads.add(extA);
    i = ads.iterator();
    assertEquals(extA, i.next());
    assertFalse(i.hasNext());
  }

  /**
   * Test method for {@link org.nrg.attr.AttrDefSet#add(java.lang.String)}.
   */
  @Test
  public void testAddString() {
    final AttrDefSet<NativeAttr,Float> ads = new AttrDefSet<NativeAttr,Float>();
    Iterator<ExtAttrDef<NativeAttr,Float>> i = ads.iterator();
    assertFalse(i.hasNext());

    final ExtAttrDef<NativeAttr,Float> empty = new ExtAttrDef.Empty<NativeAttr,Float>("empty");
    ads.add("empty");
    i = ads.iterator();
    assertEquals(empty, i.next());
    assertFalse(i.hasNext());
  }

  /**
   * Test method for {@link org.nrg.attr.AttrDefSet#add(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testAddStringString() {
    final AttrDefSet<NativeAttr,Float> ads = new AttrDefSet<NativeAttr,Float>();
    Iterator<ExtAttrDef<NativeAttr,Float>> i = ads.iterator();
    assertFalse(i.hasNext());

    final ExtAttrDef<NativeAttr,Float> foo = new ExtAttrDef.Constant<NativeAttr,Float>("foo", "bar");
    ads.add("foo", "bar");
    i = ads.iterator();
    assertEquals(foo, i.next());
    assertFalse(i.hasNext());
  }

  /**
   * Test method for {@link org.nrg.attr.AttrDefSet#add(java.lang.String, java.lang.Object)}.
   */
  @Test
  public void testAddStringS() {
    final AttrDefSet<NativeAttr,Float> ads = new AttrDefSet<NativeAttr,Float>();
    Iterator<ExtAttrDef<NativeAttr,Float>> i = ads.iterator();
    assertFalse(i.hasNext());

    final ExtAttrDef<NativeAttr,Float> extA = new ExtAttrDef.Text<NativeAttr,Float>("extA", NativeAttr.A);
    ads.add("extA", NativeAttr.A);
    i = ads.iterator();
    assertEquals(extA, i.next());
    assertFalse(i.hasNext());
  }

  /**
   * Test method for {@link org.nrg.attr.AttrDefSet#add(org.nrg.attr.ReadableAttrDefSet<S,V>[])}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testAddReadableAttrDefSetOfSVArray() {
    final AttrDefSet<NativeAttr,Float> ads = new AttrDefSet<NativeAttr,Float>();
    Iterator<ExtAttrDef<NativeAttr,Float>> i = ads.iterator();
    assertFalse(i.hasNext());
    
    ads.add(NativeAttr.frads);
    i = ads.iterator();
    assertEquals(NativeAttr.fextA, i.next());
    assertEquals(NativeAttr.fextC_BA, i.next());
    assertFalse(i.hasNext());
  }

  /**
   * Test method for {@link org.nrg.attr.AttrDefSet#remove(java.lang.String)}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testRemoveString() {
    final AttrDefSet<NativeAttr,Float> ads = new AttrDefSet<NativeAttr,Float>();
    Iterator<ExtAttrDef<NativeAttr,Float>> i = ads.iterator();
    assertFalse(i.hasNext());
    
    ads.add(NativeAttr.frads);
    
    i = ads.iterator();
    assertEquals(NativeAttr.fextA, i.next());
    assertEquals(NativeAttr.fextC_BA, i.next());
    assertFalse(i.hasNext());
    
    ads.remove("ext-A");
    i = ads.iterator();
    assertEquals(NativeAttr.fextC_BA, i.next());
    assertFalse(i.hasNext());
  }

  /**
   * Test method for {@link org.nrg.attr.AttrDefSet#remove(java.lang.Object)}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testRemoveS() {
    final AttrDefSet<NativeAttr,Float> ads = new AttrDefSet<NativeAttr,Float>();
    Iterator<ExtAttrDef<NativeAttr,Float>> i = ads.iterator();
    assertFalse(i.hasNext());
    
    ads.add(NativeAttr.frads);
    
    i = ads.iterator();
    assertEquals(NativeAttr.fextA, i.next());
    assertEquals(NativeAttr.fextC_BA, i.next());
    assertFalse(i.hasNext());
    
    ads.remove(NativeAttr.C);
    i = ads.iterator();
    assertEquals(NativeAttr.fextA, i.next());
    assertFalse(i.hasNext());
    
    ads.add(NativeAttr.fextC_BA);
    i = ads.iterator();
    assertEquals(NativeAttr.fextA, i.next());
    assertEquals(NativeAttr.fextC_BA, i.next());
    assertFalse(i.hasNext());

    ads.remove(NativeAttr.A);
    i = ads.iterator();
    assertFalse(i.hasNext());
  }

  /**
   * Test method for {@link org.nrg.attr.AttrDefSet#iterator()}.
   */
  @Test
  public void testIterator() {
    final AttrDefSet<NativeAttr,Float> ads = new AttrDefSet<NativeAttr,Float>();
    Iterator<ExtAttrDef<NativeAttr,Float>> i = ads.iterator();
    assertFalse(i.hasNext());

    ads.add("empty");
    i = ads.iterator();
    assertEquals(new ExtAttrDef.Empty<NativeAttr,Float>("empty"), i.next());
    assertFalse(i.hasNext());
  }

  /**
   * Test method for {@link org.nrg.attr.AttrDefSet#getExtAttrDef(java.lang.String)}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testGetExtAttrDef() {
    final AttrDefSet<NativeAttr,Float> ads = new AttrDefSet<NativeAttr,Float>();
    
    assertNull(ads.getExtAttrDef("ext-A"));
    
    ads.add(NativeAttr.frads);
    
    assertEquals(NativeAttr.fextA, ads.getExtAttrDef("ext-A"));
    assertEquals(NativeAttr.fextC_BA, ads.getExtAttrDef("ext-C"));
  }

  /**
   * Test method for {@link org.nrg.attr.AttrDefSet#getNativeAttrs()}.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testGetNativeAttrs() {
    final AttrDefSet<NativeAttr,Float> ads = new AttrDefSet<NativeAttr,Float>();
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
