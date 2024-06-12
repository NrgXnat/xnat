/*
 * framework: org.nrg.framework.io.RenameFileMapperTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.io;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class RenameFileMapperTest {
    /**
     * Test method for {@link RenameFileMapper#apply(File)}.
     */
    @Test
    public void testMap() {
        final File fooBarFile = new RenameFileMapper("{0}").apply(new File("foo/bar"));
        assertNotNull(fooBarFile);
        assertEquals("foo" + File.separator + "bar", fooBarFile.getPath());

        final File bazYakFile = new RenameFileMapper("a{0}b").apply(new File("baz/yak.bar"));
        assertNotNull(bazYakFile);
        assertEquals("baz" + File.separator + "ayak.barb", bazYakFile.getPath());
    }
}
