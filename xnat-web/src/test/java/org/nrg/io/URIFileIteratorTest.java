/*
 * dicom-xnat-mx: org.nrg.io.URIFileIteratorTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.io;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class URIFileIteratorTest {
    @Test
    public void testURIFileIterator() throws URISyntaxException {
        final Iterable<URI> uris = Arrays.asList(new URI("file:///tmp/foo"), new URI("file:///tmp/bar"), new URI("file:///tmp/baz"));
        final Iterator<File> fi = new URIFileIterator(uris.iterator());
        assertEquals(new File("/tmp/foo"), fi.next());
        assertEquals(new File("/tmp/bar"), fi.next());
        assertEquals(new File("/tmp/baz"), fi.next());
        assertFalse(fi.hasNext());
    }
}
