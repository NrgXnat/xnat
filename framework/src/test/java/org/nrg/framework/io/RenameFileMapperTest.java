/*
 * framework: org.nrg.framework.io.RenameFileMapperTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.io;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
        assertThat(fooBarFile).isNotNull().hasParent("foo").hasName("bar");

        final File bazYakFile = new RenameFileMapper("a{0}b").apply(new File("baz/yak.bar"));
        assertThat(bazYakFile).isNotNull().hasParent("baz").hasName("ayak.barb");
    }
}
