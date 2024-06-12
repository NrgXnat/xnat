/*
 * framework: org.nrg.framework.io.PresuffixFileMapperTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.io;

import com.google.common.base.Function;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class PresuffixFileMapperTest {
    /**
     * Test method for {@link PresuffixFileMapper#apply(File)}.
     */
    @Test
    public void testMap() {
        final Function<File, File> mapper = new PresuffixFileMapper("-mod");
        final File fooFile = mapper.apply(new File("foo"));
        assertNotNull(fooFile);
        assertEquals("foo-mod", fooFile.getName());
        final File barFile = mapper.apply(new File("bar/baz.dat"));
        assertNotNull(barFile);
        assertEquals("bar" + File.separator + "baz-mod.dat", barFile.getPath());
    }
}
