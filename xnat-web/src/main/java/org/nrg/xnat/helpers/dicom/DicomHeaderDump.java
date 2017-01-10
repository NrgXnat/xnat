/*
 * web: org.nrg.xnat.helpers.dicom.DicomHeaderDump
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.dicom;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringEscapeUtils;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.DicomObjectToStringParam;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.util.TagUtils;
import org.nrg.xft.XFTTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public final class DicomHeaderDump {
    // columns of the XFTTable
    private static final String[] columns = {
        "tag1",  // tag name, never empty.
        "tag2",  // for normal, non-sequence DICOM tags this is the empty string.
        "vr",   // DICOM Value Representation  
        "value", // Contents of the tag
        "desc"   // Description of the tag
    };

    private final Logger logger = LoggerFactory.getLogger(DicomHeaderDump.class);
    private final String file; // path to the DICOM file
    private final Map<Integer,Set<String>> fields;

    /**
     * @param file Path to the DICOM file
     */
    DicomHeaderDump(final String file, final Map<Integer,Set<String>> fields) {
        this.file = file;
        this.fields = ImmutableMap.copyOf(fields);
    }
    
    DicomHeaderDump(final String file) {
        this(file, Collections.<Integer,Set<String>>emptyMap());
    }

    /**
     * Read the header of the DICOM file ignoring the pixel data.
     * @param f 
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    DicomObject getHeader(File f) throws IOException, FileNotFoundException {
        final int stopTag;
        if (fields.isEmpty()) {
            stopTag = Tag.PixelData;
        } else {
            stopTag = 1 + Collections.max(fields.keySet());
        }
        final StopTagInputHandler stopHandler = new StopTagInputHandler(stopTag);

        IOException ioexception = null;
        final DicomInputStream dis = new DicomInputStream(f);
        try {
            dis.setHandler(stopHandler);
            return dis.readDicomObject();
        } catch (IOException e) {
            throw ioexception = e;
        } finally {
            try {
                dis.close();
            } catch (IOException e) {
                if (null != ioexception) {
                    logger.error("unable to close DicomInputStream", e);
                    throw ioexception;
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Convert a tag into a row of the XFTTable.
     * @param object Necessary so we can get to the description of the tag
     * @param element The current DICOM element
     * @param parentTag If non null, this is a nested DICOM tag. 
     * @param maxLen The maximum number of characters to read from the description and value 
     * @return The strings that comprise the row for the DICOM tag.
     */
    String[] makeRow(final DicomObject object, final DicomElement element, final String parentTag, final int maxLen) {
        final String tag = TagUtils.toString(element.tag());

        // If this element has nested tags it doesn't have a value and trying to 
        // extract one using dcm4che will result in an UnsupportedOperationException 
        // so check first.
        final String value = !element.hasDicomObjects() ? escapeHTML(element.getValueAsString(null, maxLen)) : "";

        final String vr = element.vr().toString();

        // This fixes the unfortunate tendency of DICOM tags to use good typographical but poor programming practices.
        final String desc = escapeHTML(object.nameOf(element.tag()));

        final List<String> strings = new ArrayList<>(parentTag == null ? Arrays.asList(tag, "", vr, value, desc) : Arrays.asList(parentTag, tag, vr, value, desc));
        return strings.toArray(new String[strings.size()]);
    }

    public static String escapeHTML(final String value) {
        return value == null ? null : StringEscapeUtils.escapeHtml4(value);
    }

    /**
     * Render the DICOM header to an XFTTable supporting one level of tag nesting. 
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    public XFTTable render() throws IOException,FileNotFoundException {
        XFTTable t = new XFTTable();
        t.initTable(columns);
        if (this.file == null) {
            return t;
        }

        DicomObject header = this.getHeader(new File(this.file));
        DicomObjectToStringParam formatParams = DicomObjectToStringParam.getDefaultParam();

        for (Iterator<DicomElement> it = header.iterator(); it.hasNext();) {
            DicomElement e = it.next();
            if (fields.isEmpty() || fields.containsKey(e.tag())) {
                if (e.hasDicomObjects()) {
                    for (int i = 0; i < e.countItems(); i++) {
                        DicomObject o = e.getDicomObject(i);
                        t.insertRow(makeRow(header, e, TagUtils.toString(e.tag()), formatParams.valueLength));
                        for (Iterator<DicomElement> it1 = o.iterator(); it1.hasNext();) {
                            DicomElement e1 = it1.next();
                            t.insertRow(makeRow(header, e1, TagUtils.toString(e.tag()), formatParams.valueLength));
                        }
                    }
                } else if (SiemensShadowHeader.isShadowHeader(header, e)) {
                    SiemensShadowHeader.addRows(t, header, e, fields.get(e.tag()));
                } else {
                    t.insertRow(makeRow(header, e, null, formatParams.valueLength));		
                }
            }
        }
        return t;
    }
}
