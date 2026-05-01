/*
 * framework: org.nrg.framework.io.PresuffixFileMapperTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.io;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
        assertThat(fooFile).isNotNull().hasName("foo-mod");

        final File barFile = mapper.apply(new File("bar/baz.dat"));
        assertThat(barFile).isNotNull().hasParent("bar").hasName("baz-mod.dat");
    }
}
