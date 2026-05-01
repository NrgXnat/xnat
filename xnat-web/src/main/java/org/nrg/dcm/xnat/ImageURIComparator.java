/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ImageURIComparator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.nrg.attr.ExtAttrValue;
import org.nrg.dicomtools.utilities.DicomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Comparator;

/**
 * Orders Files by DICOM Instance Number.
 * Files without an instance number always compare larger than Files with an instance number.
 * Files with equal instance number are ordered lexicographically by pathname.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class ImageURIComparator implements Comparator<URI> {
    private static final String SORTING_ATTR = "instanceNumber";
    private final Multimap<URI, ? extends ExtAttrValue> values;

    public ImageURIComparator(final Multimap<URI, ? extends ExtAttrValue> values) {
        this.values = Multimaps.unmodifiableMultimap(values);
    }

    /**
     * {@inheritDoc}
     */
    public int compare(final URI uri1, final URI uri2) {
        final Integer i1 = getOrdinalValue(uri1);
        final Integer i2 = getOrdinalValue(uri2);

        if (null == i1) {
            if (null == i2) {
                return uri1.toString().compareTo(uri2.toString());    // compare pathnames lexicographically
            } else {
                return 1;
            }
        } else if (null == i2) {
            return -1;
        }
        if (i1.equals(i2)) {
            return uri1.toString().compareTo(uri2.toString());    // might be distinct files; compare pathnames lexicographically
        } else {
            return i1.compareTo(i2);
        }
    }

    private Integer getOrdinalValue(final URI uri) {
        try {
            final URI    qualified      = DicomUtils.getQualifiedUri(uri.toString());
            final File   file           = new File(qualified);
            final String attributeValue = getAttributeValue(file, SORTING_ATTR);

            return attributeValue != null ? Integer.valueOf(attributeValue) : null;
        } catch (NumberFormatException e) {
            _log.warn("An invalid value was found for an attribute value", e);
            return null;
        } catch (URISyntaxException e) {
            _log.warn("There was an issue creating a file URI from the URI " + uri.toString(), e);
            return null;
        }
    }

    private String getAttributeValue(final File f, final String attr) {
        final Collection<? extends ExtAttrValue> fileValues = values.get(f.toURI());
        if (null == fileValues) {
            return null;
        }
        for (final ExtAttrValue eav : fileValues) {
            if (attr.equals(eav.getName())) {
                return eav.getText();
            }
        }
        return null;
    }

    private static final Logger _log = LoggerFactory.getLogger(ImageURIComparator.class);
}
