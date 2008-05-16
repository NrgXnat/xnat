package org.nrg.attr;

import java.util.List;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.nrg.attr.ExtAttrValue;

public class ExtAttrValueTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public final void testHashCode() {
    ExtAttrValue val1 = new ExtAttrValue("foo", "value");
    ExtAttrValue val1a = new ExtAttrValue("foo", "value");
    assertTrue(val1.hashCode() == val1a.hashCode());
    
    ExtAttrValue val2 = new ExtAttrValue("foo", "other value");
    assertFalse(val1.hashCode() == val2.hashCode());
    
    val1.addAttr("attr1", "foo");
    val1a.addAttr("attr1", "foo");
    assertTrue(val1.hashCode() == val1a.hashCode());

    val1a.addAttr("attr2", "bar");
    assertFalse(val1.hashCode() == val1a.hashCode());
    
    ExtAttrValue val3 = new ExtAttrValue("foo");
    val3.addAttr("attr1", "foo");
    
    ExtAttrValue val4 = new ExtAttrValue("foo");
    val4.addAttr("attr1", "foo");
    
    assertTrue(val3.hashCode() == val4.hashCode());
    
    ExtAttrValue val5 = new ExtAttrValue("foo");
    assertFalse(val3.hashCode() == val5.hashCode());
    
    val5.addAttr("attr1", "bar");
    assertFalse(val3.hashCode() == val5.hashCode());
    
    ExtAttrValue val6 = new ExtAttrValue("foo");
    val6.addAttr("attr2", "foo");
    assertFalse(val3.hashCode() == val6.hashCode());
  }

  @Test
  public final void testEqualsObject() {
    ExtAttrValue val1 = new ExtAttrValue("foo", "value");
    ExtAttrValue val1a = new ExtAttrValue("foo", "value");
    assertTrue(val1.equals(val1a));
    assertTrue(val1a.equals(val1));
    
    ExtAttrValue val2 = new ExtAttrValue("foo", "other value");
    assertFalse(val1.equals(val2));
    assertFalse(val2.equals(val1));
    
    val1.addAttr("attr1", "foo");
    val1a.addAttr("attr1", "foo");
    assertTrue(val1.equals(val1a));
    assertTrue(val1a.equals(val1));

    val1a.addAttr("attr2", "bar");
    assertFalse(val1.equals(val1a));
    assertFalse(val1a.equals(val1));
    
    ExtAttrValue val3 = new ExtAttrValue("foo");
    val3.addAttr("attr1", "foo");
    
    ExtAttrValue val4 = new ExtAttrValue("foo");
    val4.addAttr("attr1", "foo");
    
    assertTrue(val3.equals(val4));
    assertTrue(val4.equals(val3));
    
    ExtAttrValue val5 = new ExtAttrValue("foo");
    assertFalse(val3.equals(val5));
    assertFalse(val5.equals(val3));
    
    val5.addAttr("attr1", "bar");
    assertFalse(val3.equals(val5));
    assertFalse(val5.equals(val3));
    
    ExtAttrValue val6 = new ExtAttrValue("foo");
    val6.addAttr("attr2", "foo");
    assertFalse(val3.equals(val6));
    assertFalse(val6.equals(val3));
  }

  @Test
  public final void testExtAttrValueString() {
    ExtAttrValue val = new ExtAttrValue("foo", "foo");
    assertEquals("foo", val.getText());
  }

  @Test
  public final void testExtAttrValue() {
    ExtAttrValue val = new ExtAttrValue("foo");
    assertNull(val.getText());
  }

  @Test
  public final void testAddAttr() {
    ExtAttrValue val = new ExtAttrValue("foo");
    List<String> names = val.getAttrNames();
    assertNotNull(names);
    assertEquals(0, names.size());
    val.addAttr("foo", "bar");
    assertEquals(0, names.size());
    names = val.getAttrNames();
    assertEquals(1, names.size());
  }

  @Test
  public final void testGetText() {
    testExtAttrValueString();
  }

  @Test
  public final void testGetAttrNames() {
    ExtAttrValue val = new ExtAttrValue("foo");
    List<String> names = val.getAttrNames();
    assertNotNull(names);
    assertEquals(0, names.size());
    val.addAttr("foo", "bar");
    assertEquals(0, names.size());
    names = val.getAttrNames();
    assertEquals(1, names.size());
    assertEquals("bar", val.getAttrValue("foo"));
    val.addAttr("bar", "baz");
    assertEquals(1, names.size());
    names = val.getAttrNames();
    assertEquals(2, names.size());
    assertEquals("bar", val.getAttrValue("foo"));
    assertEquals("baz", val.getAttrValue("bar"));
  }

  @Test
  public final void testGetAttrValue() {
    testGetAttrNames();
  }
  
  @Test
  public final void testToString() {
    final ExtAttrValue foo = new ExtAttrValue("foo");
    assertEquals("<foo/>", foo.toString());
    foo.addAttr("bar", "baz");
    assertEquals("<foo bar=\"baz\"/>", foo.toString());
    foo.addAttr("baz", "bar");
    assertEquals("<foo bar=\"baz\" baz=\"bar\"/>", foo.toString());
    
    final ExtAttrValue ack = new ExtAttrValue("ack", "thpthpppt");
    assertEquals("<ack>thpthpppt</ack>", ack.toString());
    ack.addAttr("baz", "bar");
    assertEquals("<ack baz=\"bar\">thpthpppt</ack>", ack.toString());
    ack.addAttr("boing", "boing");
    assertEquals("<ack baz=\"bar\" boing=\"boing\">thpthpppt</ack>", ack.toString());
    ack.addAttr("bar", "baz");
    assertEquals("<ack baz=\"bar\" boing=\"boing\" bar=\"baz\">thpthpppt</ack>", ack.toString());
    
    assertEquals("<foo bar=\"baz\" baz=\"bar\"/>", foo.toString());
  }

}
