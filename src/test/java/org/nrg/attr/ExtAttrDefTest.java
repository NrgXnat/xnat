/**
 * $Id: ExtAttrDefTest.java,v 1.1 2007/08/10 18:48:15 karchie Exp $
 * Copyright (c) 2007 Washington University
 */
package org.nrg.attr;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

import org.junit.Test;

/**
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 *
 */
public class ExtAttrDefTest {
  private final static String A_VALUE = "a-value";
  private final static String B_VALUE = "b-value";
  private final static String C_VALUE = "c-value";
  private final static Map<NativeAttr,String> values = new HashMap<NativeAttr,String>();
  static {
    values.put(NativeAttr.A, A_VALUE);
    values.put(NativeAttr.B, B_VALUE);
    values.put(NativeAttr.C, C_VALUE);
  }


  /**
   * Test method for {@link org.nrg.attr.ExtAttrDef.AttributesOnly#convert(java.util.Map)}.
   */
  @Test
  public final void testAttributesOnlyConvert() {
    final ExtAttrDef<NativeAttr,String> ao = new ExtAttrDef.AttributesOnly<NativeAttr,String>("ao",
	new String[]{"A"}, new NativeAttr[]{NativeAttr.A});
    final ExtAttrValue val;
    try {
      val = ao.convert(values);
    } catch (ConversionFailureException e) {
      fail(e.getMessage());
      return;
    }
    assertEquals(A_VALUE, val.getAttrValue("A"));
    assertNull(val.getAttrValue("B"));
  }

  /**
   * Test method for {@link org.nrg.attr.ExtAttrDef.TextWithAttributes#convert(java.util.Map)}.
   */
  @Test
  public final void testTextWithAttributesConvert() {
    final ExtAttrDef<NativeAttr,String> twa =
      new ExtAttrDef.TextWithAttributes<NativeAttr,String>("A", NativeAttr.A,
	  new String[]{"B", "C"}, new NativeAttr[]{NativeAttr.B, NativeAttr.C});
    final ExtAttrValue val;
    try {
      val = twa.convert(values);
    } catch (ConversionFailureException e) {
      fail(e.getMessage());
      return;
    }
    assertEquals(A_VALUE, val.getText());
    assertEquals(B_VALUE, val.getAttrValue("B"));
    assertEquals(C_VALUE, val.getAttrValue("C"));
    assertNull(val.getAttrValue("D"));
  }


  /**
   * Test method for {@link org.nrg.attr.ExtAttrDef.Labeled#convert(java.util.Map)}.
   */
  @Test
  public final void testLabeledConvert() {
    final ExtAttrDef<NativeAttr,String> twa =
      new ExtAttrDef.TextWithAttributes<NativeAttr,String>("attributes", NativeAttr.A,
	  new String[]{"B", "C"}, new NativeAttr[]{NativeAttr.B, NativeAttr.C});
    final ExtAttrDef<NativeAttr,String> labeled = new ExtAttrDef.Labeled<NativeAttr,String>(twa, "main", "A");
    final ExtAttrValue val;
    try {
      val = labeled.convert(values);
    } catch (ConversionFailureException e) {
      fail(e.getMessage());
      return;
    }
    assertEquals(A_VALUE, val.getText());
    assertEquals("A", val.getAttrValue("main"));
    assertEquals(B_VALUE, val.getAttrValue("B"));
    assertEquals(C_VALUE, val.getAttrValue("C"));
    assertNull(val.getAttrValue("D"));
  }


  /**
   * Arguably there should be convertText() tests, but the convert() tests usually cover that code 
   * Test method for {@link org.nrg.attr.ExtAttrDef#convertText(java.util.Map)}.
   */

  /**
   * Test method for {@link org.nrg.attr.ExtAttrDef#getName()}.
   */
  @Test
  public final void testGetName() {
    final String name = "myAttr";
    final ExtAttrDef<NativeAttr,String> ead = new ExtAttrDef.Abstract<NativeAttr,String>(name, new HashSet<NativeAttr>()) {
      public String convertText(final Map<NativeAttr,String> vals) { throw new UnsupportedOperationException("convertText"); }
    };
    assertEquals(name, ead.getName());
  }

  /**
   * Test method for {@link org.nrg.attr.ExtAttrDef#getAttrs()}.
   */
  @Test
  public final void testGetAttrs() {
    final ExtAttrDef<NativeAttr,String> twa =
      new ExtAttrDef.TextWithAttributes<NativeAttr,String>("A", NativeAttr.A,
	  new String[]{"B", "C"}, new NativeAttr[]{NativeAttr.B, NativeAttr.C});
    final Collection<NativeAttr> attrs = twa.getAttrs();
    assertEquals(3, attrs.size());
    assertTrue(attrs.contains(NativeAttr.A));
    assertTrue(attrs.contains(NativeAttr.B));
    assertTrue(attrs.contains(NativeAttr.C));
    assertFalse(attrs.contains(NativeAttr.D));
  }
}
