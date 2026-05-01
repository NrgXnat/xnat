package org.nrg.dicom.mizer.service;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by davidmaffitt on 4/13/17.
 */
public class VersionStringTest {

    @Test
    public void stringConstructorTest1() {
        VersionString vs = new VersionString("4.0.10");
        assertNotNull( vs);
    }

    @Test
    public void stringConstructorTest2() {
        VersionString vs = new VersionString("4.0");
        assertNotNull( vs);
    }

    @Test
    public void stringConstructorTest3() {
        VersionString vs = new VersionString("4");
        assertNotNull( vs);
    }

    @Test
    public void stringConstructorTest4() {
        VersionString vs = new VersionString("4.");
        assertNotNull( vs);
    }

    @Test
    public void stringConstructorTest5() {
        VersionString vs = new VersionString(".1");
        assertNotNull( vs);
    }

}