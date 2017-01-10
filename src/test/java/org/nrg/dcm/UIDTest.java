/*
 * dicomtools: org.nrg.dcm.UIDTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import junit.framework.TestCase;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class UIDTest extends TestCase {

  /**
   * Test method for {@link org.nrg.dcm.UID#hashCode()}.
   */
  public void testHashCode() throws UID.InvalidUIDException {
    final UID uid1 = new UID("1.2.3.4.5");
    assertEquals(uid1.hashCode(), uid1.hashCode());
    assertEquals(new UID("1.2.3.4.5").hashCode(), uid1.hashCode());
  }

  /**
   * Test method for {@link org.nrg.dcm.UID#UID(java.lang.String)}.
   */
  public void testUID() throws UID.InvalidUIDException {
    new UID("0.1");
    new UID("1.2.3.4.5");
    new UID("0.1.2.3.4");
    new UID("10.1.2.0.3");
    new UID("100.1.2.0.3");
    try {
      UID bad = new UID(null);
      fail("accepted invalid UID " + bad);
    } catch (UID.InvalidUIDException pass) {}
    try {
      UID bad = new UID("");
      fail("accepted invalid UID " + bad);
    } catch (UID.InvalidUIDException pass) {}
    try {
      UID bad = new UID(".");
      fail("accepted invalid UID " + bad);
    } catch (UID.InvalidUIDException pass) {}
    try {
      UID bad = new UID("1");   // must have both org root and suffix
      fail("accepted invalid UID " + bad);
    } catch (UID.InvalidUIDException pass) {}
    try {
      UID bad = new UID("1.");
      fail("accepted invalid UID " + bad);
    } catch (UID.InvalidUIDException pass) {}
    try {
      UID bad = new UID(".0");
      fail("accepted invalid UID " + bad);
    } catch (UID.InvalidUIDException pass) {}
    try {
      UID bad = new UID(".1");
      fail("accepted invalid UID " + bad);
    } catch (UID.InvalidUIDException pass) {}
    try {
      UID bad = new UID("01.2.3.4.5");
      fail("accepted invalid UID " + bad);
    } catch (UID.InvalidUIDException pass) {}
    try {
      UID bad = new UID("1.02.3.4.5");
      fail("accepted invalid UID " + bad);
    } catch (UID.InvalidUIDException pass) {}
    try {
      UID bad = new UID("1.2.3 ");
      fail("accepted invalid UID " + bad);
    } catch (UID.InvalidUIDException pass) {}
    try {
      UID bad = new UID(" 1.2.3");
      fail("accepted invalid UID " + bad);
    } catch (UID.InvalidUIDException pass) {}
    try {
      UID bad = new UID("a.b.c.d");
      fail("accepted invalid UID " + bad);
    } catch (UID.InvalidUIDException pass) {}
  }

  /**
   * Test method for {@link org.nrg.dcm.UID#equals(java.lang.Object)}.
   */
  public void testEqualsObject() throws UID.InvalidUIDException {
    final UID uid1a = new UID("1.2.3.4");
    final UID uid1b = new UID("1.2.3.4");
    final UID uid2a = new UID("2.3.4.5");
    final UID uid2b = new UID("2.3.4.5");
    
    assertEquals(uid1a, uid1a);
    assertFalse(uid1a.equals(null));
    assertFalse(uid1a.equals("1.2.3.4"));
    assertEquals(uid1a, uid1b);
    assertFalse(uid1a.equals(uid2a));
    assertFalse(uid1a.equals(uid2b));
    
    assertEquals(uid1b, uid1b);
    assertFalse(uid1b.equals("1.2.3.4"));
    assertEquals(uid1b, uid1a);
    assertFalse(uid1b.equals(uid2a));
    assertFalse(uid1b.equals(uid2b));
    
    assertEquals(uid2a, uid2a);
    assertFalse(uid2a.equals("2.3.4.5"));
    assertEquals(uid2a, uid2b);
    assertEquals(uid2b, uid2a);
    assertFalse(uid2a.equals(uid1a));
    assertFalse(uid2a.equals(uid1b));
  }

  /**
   * Test method for {@link org.nrg.dcm.UID#compareTo(org.nrg.dcm.UID)}.
   */
  public void testCompareTo() throws UID.InvalidUIDException {
    final UID uid1 = new UID("0.1");
    final UID uid2 = new UID("0.2");
    assertTrue(uid1.compareTo(uid2) < 0);
    assertTrue(uid2.compareTo(uid1) > 0);
    assertEquals(0, uid1.compareTo(uid1));
  }
}
