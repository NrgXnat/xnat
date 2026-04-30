/*
 * DicomEdit: org.nrg.dcm.io.TransferCapabilityExtractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.io;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.TransferCapability;
import org.nrg.dicomtools.utilities.DicomUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@SuppressWarnings("unused")
public final class TransferCapabilityExtractor {
    private TransferCapabilityExtractor() {}

    public static TransferCapability[] getTransferCapabilities(final Iterator<File> files, final String role) {
        final SetMultimap<String,String> tcElements = LinkedHashMultimap.create();
        while (files.hasNext()) {
            final File f = files.next();
            try {
                final Attributes o = DicomUtils.read(f, Tag.SOPClassUID);
                tcElements.put(o.getString(Tag.SOPClassUID), DicomUtils.getTransferSyntaxUID(o));
            } catch (IOException ignored) {}
        }

        return getTransferCapabilities(tcElements, role);
    }

    public static TransferCapability[] getTransferCapabilities(final Iterable<File> files, final String role) {
        return getTransferCapabilities(files.iterator(), role);
    }

    private static final TransferCapability[] EMPTY_TC_ARRAY = new TransferCapability[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final Predicate<String> notNull = Predicates.notNull();

    /**
     * Generates an array of TransferCapability from a SetMultimap of SOP to transfer syntax.
     * @param sopToTS Map of SOP class UID (String) to Collection of Transfer Syntax (String)
     * @param role "SCP" or "SCU"
     * @return array of TransferCapabilities
     */
    public static TransferCapability[] getTransferCapabilities(final SetMultimap<String,String> sopToTS, final String role) {
        final List<TransferCapability> tcs = Lists.newArrayListWithCapacity(sopToTS.size());
        for (final String sop : sopToTS.keySet()) {
            final Set<String> tsuids = Sets.filter(sopToTS.get(sop), notNull);
            if (!tsuids.isEmpty()) {
                TransferCapability tc = new TransferCapability();
                tc.setSopClass(sop);
                tc.setRole(DicomUtils.parseRole(role));
                tc.setTransferSyntaxes(tsuids.toArray(new String[0]));
                tcs.add(tc);
            }
        }
        return tcs.toArray(EMPTY_TC_ARRAY);
    }
}
