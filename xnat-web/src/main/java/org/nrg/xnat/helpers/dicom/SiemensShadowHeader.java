/*
 * org.nrg.xnat.helpers.dicom.SiemensShadowHeader
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.dicom;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.util.TagUtils;
import org.nrg.xft.XFTTable;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Set;

public class SiemensShadowHeader {
    private static final String IMAGE_NUM_4 = "IMAGE NUM 4",
    SIEMENS_CSA_HEADER = "SIEMENS CSA HEADER",
    SPEC_NUM_4 = "SPEC NUM 4",
    SIEMENS_CSA_NON_IMAGE = "SIEMENS CSA NON-IMAGE",
    SV10_MAGIC = "SV10";

    private static final int INT32_SIZE = 4;

    private static String getStringZ(final byte[] bytes, final int offset, final int length) {
        for (int i = 0; i < length; i++) {
            if (0 == bytes[offset+i]) {
                return new String(bytes, offset, i);
            }
        }
        return new String(bytes, offset, length);
    }
    
    private static int getLEint(final ByteBuffer bb, final int offset) {
        return Integer.reverseBytes(bb.getInt(offset));
    }

    public static boolean isShadowHeader(final DicomObject o, final DicomElement e) {
        final int tag = e.tag();
        if (0x00290000 != (tag & 0xffff0000)) {
            return false;
        }
        final String version = o.getString(0x00291008);
        final String csa = o.getString(0x00290010);

        if (IMAGE_NUM_4.equals(version) && SIEMENS_CSA_HEADER.equals(csa)
                && (tag == 0x00291010 || tag == 0x00291020)) {
            return true;
        } else if (SPEC_NUM_4.equals(version) && SIEMENS_CSA_NON_IMAGE.equals(csa)
                && (tag == 0x00291210 || tag == 0x00291220
                        || tag == 0x00291110 || tag == 0x00291120)) {
            return true;
        } else {
            return false;
        }
    }

    public static XFTTable addRows(final XFTTable table, final DicomObject o,
            final DicomElement elem, Set<String> only) {
        final byte[] bytes = elem.getBytes();
        final ByteBuffer bb = ByteBuffer.wrap(bytes);
        if (!SV10_MAGIC.equals(new String(bytes, 0, 4))) {
            throw new IllegalArgumentException("not a Siemens shadow header: wrong magic");
        }
        final int tag = elem.tag();
        final String tagName = TagUtils.toString(tag);
        final String version = o.getString(0x00291008);
        final String csa = o.getString(0x00290010);
        final String desc;
        if (IMAGE_NUM_4.equals(version)) {
            if (SIEMENS_CSA_HEADER.equals(csa)) {
                if (tag == 0x00291010) {
                    desc = "Siemens CSA Image Instance Shadow Header";
                } else if (tag == 0x00291020) {
                    desc = "Siemens CSA Image Series Shadow Header";
                } else {
                    desc = "Siemens CSA Unknown Image Shadow Header";
                }
            } else {
                throw new IllegalArgumentException("invalid Siemens CSA HEADER identifier " + csa);
            }
        } else if (SPEC_NUM_4.equals(version)) {
            if (SIEMENS_CSA_NON_IMAGE.equals(csa)) {
                if (tag == 0x00291210) {
                    desc = "Siemens CSA Non-Image Instance Shadow Header";
                } else if (tag == 0x00291220) {
                    desc = "Siemens CSA Non-Image Series Shadow Header";
                } else if (tag == 0x00291110) {
                    desc = "Siemens CSA VB13 Instance Shadow Header";
                } else if (tag == 0x00291120) {
                    desc = "Siemens CSA VB13 Series Shadow Header";
                } else {
                    desc = "Siemens CSA Unknown Non-Image Shadow Header";
                }
            } else {
                throw new IllegalArgumentException("invalid Siemens CSA HEADER identifier " + csa);
            }
        } else {
            throw new IllegalArgumentException("invalid NUMARIS version " + version);
        }

        final int n = getLEint(bb, 8);
        int offset = 16;    // skip unknown int32 (= 77)
        for (int i = 0; i < n; i++) {
            final String fieldName = getStringZ(bytes, offset, 64); offset += 64;
            int vm = getLEint(bb, offset); offset += INT32_SIZE;
            final String vr = getStringZ(bytes, offset, 4); offset += 4;
            offset += INT32_SIZE;    // skip SyngoDT (int32)
            final int numItems = getLEint(bb, offset);  offset += INT32_SIZE;
            if (0 == vm) {
                vm = numItems; // can happen in spectroscopy or VB13 images
            }

            final String value;
            if (numItems > 1) {
                final Collection<String> values = Lists.newArrayListWithExpectedSize(numItems);
                offset += INT32_SIZE;   // skip unknown int32 (= 77)
                for (int j = 0; j < vm; j++) {
                    offset += 3 * INT32_SIZE;   // skip three int32s
                    final int fieldWidth = 4*(int)Math.ceil(getLEint(bb, offset)/4.0); offset += INT32_SIZE;
                    values.add(getStringZ(bytes, offset, fieldWidth).trim());
                    offset += fieldWidth;
                }
                value = DicomHeaderDump.escapeHTML(Joiner.on("\\").join(values));
            } else {
                value = "";
            }

            // skip the junk bytes at the end of the item
            if (numItems < 1) {
                offset += 4;
            } else if (numItems < vm) {
                offset += 16;
            } else {
                offset += 16 * (numItems - vm);
            }

            if (null == only || only.isEmpty() || only.contains(fieldName)) {
                table.insertRow(new String[]{tagName, fieldName, vr, value, desc});
            }
        }
        return table;
    }
}
