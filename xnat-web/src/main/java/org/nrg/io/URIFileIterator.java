/*
 * dicom-xnat-mx: org.nrg.io.URIFileIterator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.io;

import java.io.File;
import java.net.URI;
import java.util.Iterator;

/**
 * Iterator<File> wrapper for an Iterator<URI>.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class URIFileIterator implements Iterator<File> {
    private final Iterator<URI> urii;
    
    public URIFileIterator(final Iterator<URI> urii) {
        this.urii = urii;
    }
    
    /*
     * (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() { return urii.hasNext(); }
    
    /*
     * (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public File next() { return new File(urii.next()); }
    
    /*
     * (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() { urii.remove(); }
}
