/*
 * DicomDB: org.nrg.dcm.ChainedDicomAttributeIndex
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import java.util.Arrays;
import java.util.List;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class ChainedDicomAttributeIndex extends AbstractDicomAttributeIndex
        implements DicomAttributeIndex {
    private static final Integer[] NO_PATH = {};
    private final        Logger    logger  = LoggerFactory.getLogger(ChainedDicomAttributeIndex.class);
    private final String          id;
    private final List<Integer[]> paths;

    public ChainedDicomAttributeIndex(final String id, final Iterable<Integer[]> paths) {
        this.id = id;
        this.paths = Lists.newArrayList(paths);
    }

    public ChainedDicomAttributeIndex(final String id, final Integer[]... paths) {
        this(id, Arrays.asList(paths));
    }

    @Override
    public String getAttributeName(final DicomObject dicomObject) {
        return id;
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.DicomAttributeIndex#getColumnName()
     */
    public String getColumnName() {
        return id;
    }

    public DicomElement getElement(final DicomObject o) {
        for (final Integer[] path : paths) {
            final DicomElement e = getElement(o, path);
            if (null != e) {
                try {
                    logger.trace("Getting {} value from {}: {}", id, pathToString(o, path), Arrays.toString(e.getStrings(o.getSpecificCharacterSet(), false)));
                } catch (Throwable t) {
                    logger.warn("Unable to extract value", t);
                }
                return e;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.DicomAttributeIndex#getPath(org.dcm4che2.data.DicomObject)
     */
    public Integer[] getPath(final DicomObject context) {
        for (final Integer[] path : paths) {
            final DicomElement e = getElement(context, path);
            if (null != e) {
                return path;
            }
        }
        return NO_PATH;
    }
}
