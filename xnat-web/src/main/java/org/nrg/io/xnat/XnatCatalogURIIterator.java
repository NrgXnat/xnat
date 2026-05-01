/*
 * dicom-xnat-mx: org.nrg.io.xnat.XnatCatalogURIIterator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.io.xnat;

import org.nrg.dicomtools.utilities.DicomUtils;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xdat.model.CatEntryI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

/**
 * Iterator over the URIs enumerated in an XNAT catalog file
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class XnatCatalogURIIterator implements Iterator<URI> {
    private final Logger logger = LoggerFactory.getLogger(XnatCatalogURIIterator.class);
    private final Iterator<CatEntryI> entriesi;

    /**
     * Creates an iterator over the URIs enumerated in the given catalog object
     *
     * @param catbean catalog object
     */
    public XnatCatalogURIIterator(final CatCatalogBean catbean) {
        this.entriesi = catbean.getEntries_entry().iterator();
    }

    /**
     * Creates an iterator over the URIs enumerated in the given catalog file
     *
     * @param catalogFile file containing an XNAT catalog
     * @throws SAXException When an error occurs reading XML input..
     * @throws IOException  When an error occurs reading or writing data.
     */
    public XnatCatalogURIIterator(final File catalogFile) throws IOException, SAXException {
        this((CatCatalogBean) new XDATXMLReader().parse(catalogFile));
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return entriesi.hasNext();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public URI next() {
        final CatEntryI entryb = entriesi.next();
        try {
            return DicomUtils.getQualifiedUri(entryb.getUri());
        } catch (URISyntaxException e) {
            logger.error("catalog entry " + entryb + " is not a valid URI", e);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
