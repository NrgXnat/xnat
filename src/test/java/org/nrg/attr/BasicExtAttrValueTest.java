/*
 * ExtAttr: org.nrg.attr.BasicExtAttrValueTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.attr;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

import org.junit.Test;

import org.nrg.attr.ExtAttrValue;

import com.google.common.collect.ImmutableMap;

public class BasicExtAttrValueTest {
  @Test
  public final void testHashCode() {
    ExtAttrValue val1 = new BasicExtAttrValue("foo", "value");
    ExtAttrValue val1a = new BasicExtAttrValue("foo", "value");
    assertTrue(val1.hashCode() == val1a.hashCode());
    
    final ExtAttrValue val2 = new BasicExtAttrValue("foo", "other value");
    assertFalse(val1.hashCode() == val2.hashCode());
    
    val1 = new BasicExtAttrValue("foo", "value",
        Collections.singletonMap("attr1", "foo"));
    val1a = new BasicExtAttrValue("foo", "value",
        Collections.singletonMap("attr1", "foo"));
    assertTrue(val1.hashCode() == val1a.hashCode());

    final ExtAttrValue val3 = new BasicExtAttrValue("foo", "value",
        Utils.zipmap(new String[]{"attr1", "attr2"},
            new String[]{"foo", "bar"}));
    assertFalse(val1.hashCode() == val3.hashCode());
    
    ExtAttrValue aoval1 = new BasicExtAttrValue("foo", null,
        Collections.singletonMap("attr1", "foo"));
    ExtAttrValue aoval1a = new BasicExtAttrValue("foo", null,
        Collections.singletonMap("attr1", "foo"));
        
    assertTrue(aoval1.hashCode() == aoval1a.hashCode());
    
    final ExtAttrValue aoval2 = new BasicExtAttrValue("foo", null);
    assertFalse(aoval1.hashCode() == aoval2.hashCode());
    
    final ExtAttrValue aoval3 = new BasicExtAttrValue("foo", null,
        Collections.singletonMap("attr1", "bar"));
    assertFalse(aoval1.hashCode() == aoval3.hashCode());
    
    final ExtAttrValue aoval4 = new BasicExtAttrValue("foo", null,
        Collections.singletonMap("attr2", "foo"));
    assertFalse(aoval1.hashCode() == aoval4.hashCode());
  }

  @Test
  public final void testEqualsObject() {
    ExtAttrValue val1 = new BasicExtAttrValue("foo", "value");
    ExtAttrValue val1a = new BasicExtAttrValue("foo", "value");
    assertEquals(val1, val1a);
    assertEquals(val1a, val1);
    
    final ExtAttrValue val2 = new BasicExtAttrValue("foo", "other value");
    assertFalse(val1.equals(val2));
    assertFalse(val2.equals(val1));
    
    val1 = new BasicExtAttrValue("foo", "value",
        Collections.singletonMap("attr1", "foo"));
    val1a = new BasicExtAttrValue("foo", "value",
        Collections.singletonMap("attr1", "foo"));
    assertEquals(val1, val1a);
    assertEquals(val1a, val1);

    final ExtAttrValue val3 = new BasicExtAttrValue("foo", "value",
        Utils.zipmap(new String[]{"attr1", "attr2"},
            new String[]{"foo", "bar"}));
    assertFalse(val1.equals(val3));
    assertFalse(val3.equals(val1));
    
    ExtAttrValue aoval1 = new BasicExtAttrValue("foo", null,
        Collections.singletonMap("attr1", "foo"));
    ExtAttrValue aoval1a = new BasicExtAttrValue("foo", null,
        Collections.singletonMap("attr1", "foo"));
    assertEquals(aoval1, aoval1a);
    assertEquals(aoval1a, aoval1);
    
    final ExtAttrValue aoval2 = new BasicExtAttrValue("foo", null);
    assertFalse(aoval1.equals(aoval2));
    assertFalse(aoval2.equals(aoval1));
    
    final ExtAttrValue aoval3 = new BasicExtAttrValue("foo", null,
        Collections.singletonMap("attr1", "bar"));
    assertFalse(aoval1.equals(aoval3));
    assertFalse(aoval3.equals(aoval1));
    
    final ExtAttrValue aoval4 = new BasicExtAttrValue("foo", null,
        Collections.singletonMap("attr2", "foo"));
    assertFalse(aoval1.equals(aoval4));
    assertFalse(aoval4.equals(aoval1));
  }

  @Test
  public final void testExtAttrValueString() {
    ExtAttrValue val = new BasicExtAttrValue("foo", "foo");
    assertEquals("foo", val.getText());
  }

  @Test
  public final void testGetAttrs() {
    final ExtAttrValue val1 = new BasicExtAttrValue("foo", null);
    assert(val1.getAttrs().isEmpty());
    
    final ExtAttrValue val2 = new BasicExtAttrValue("foo", null,
        Collections.singletonMap("foo", "bar"));
    assertEquals(1, val2.getAttrs().size());
    assertTrue(val2.getAttrs().containsKey("foo"));
    assertTrue(val2.getAttrs().containsValue("bar"));
    
    final ExtAttrValue val3 = new BasicExtAttrValue("foo", null,
        Utils.zipmap(new String[]{"bar", "baz"},
            new String[]{"foo", "bar"}));
    assertEquals(2, val3.getAttrs().size());
    assertEquals("foo", val3.getAttrs().get("bar"));
    assertEquals("bar", val3.getAttrs().get("baz"));
  }

  @Test
  public final void testToString() {
    assertEquals("<foo/>", new BasicExtAttrValue("foo", null).toString());
    
    assertEquals("<foo bar=\"baz\"/>", new BasicExtAttrValue("foo", null,
        Collections.singletonMap("bar", "baz")).toString());
    
    assertEquals("<foo bar=\"baz\" baz=\"bar\"/>",
        new BasicExtAttrValue("foo", null,
            Utils.zipmap(new String[]{"bar", "baz"}, new String[]{"baz", "bar"})
        ).toString()
    );
    
    assertEquals("<ack>thpthpppt</ack>",
        new BasicExtAttrValue("ack", "thpthpppt").toString());
    assertEquals("<ack baz=\"bar\">thpthpppt</ack>",
        new BasicExtAttrValue("ack", "thpthpppt",
            Collections.singletonMap("baz", "bar")).toString());

    assertEquals("<ack baz=\"bar\" boing=\"boing\">thpthpppt</ack>",
        new BasicExtAttrValue("ack", "thpthpppt",
            Utils.zipmap(new String[]{"baz", "boing"}, new String[]{"bar", "boing"})
        ).toString());
  }
  
  @Test
  public final void testAttributeMerge() {
      final ExtAttrValue a = new BasicExtAttrValue("var", "A");
      final ExtAttrValue b = new BasicExtAttrValue("var", "B",
              ImmutableMap.of("attr1", "foo", "attr2", "bar"));
      final ExtAttrValue c = new BasicExtAttrValue("var", "C",
              ImmutableMap.of("attr2", "baz", "attr3", "moo"));
      final ExtAttrValue merged = new BasicExtAttrValue(Arrays.asList(a, b, c));
      assertEquals("var", merged.getName());
      assertEquals("A,B,C", merged.getText());
      final Map<String,String> attrs = merged.getAttrs();
      assertEquals("foo", attrs.get("attr1"));
      assertEquals("moo", attrs.get("attr3"));
      final String attr2 = attrs.get("attr2");
      assertTrue(Pattern.matches("\\w\\w\\w,\\w\\w\\w", attr2));
      assertTrue(attr2.contains("bar"));
      assertTrue(attr2.contains("baz"));
  }
  
  @Test(expected=IllegalArgumentException.class)
  public final void testAttributeMergeFail() {
      final ExtAttrValue a = new BasicExtAttrValue("a", "A");
      final ExtAttrValue b = new BasicExtAttrValue("b", "B");
      new BasicExtAttrValue(Arrays.asList(a, b));
  }
}
