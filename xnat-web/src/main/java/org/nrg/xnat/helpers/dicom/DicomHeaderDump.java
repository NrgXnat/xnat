/*
 * org.nrg.xnat.helpers.dicom.DicomHeaderDump
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.dicom;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringEscapeUtils;
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
     * @param o Necessary so we can get to the description of the tag
     * @param e The current DICOM element
     * @param parentTag If non null, this is a nested DICOM tag. 
     * @param maxLen The maximum number of characters to read from the description and value 
     * @return
     */
    String[] makeRow(DicomObject o, DicomElement e, String parentTag , int maxLen) {
        String tag = TagUtils.toString(e.tag());
        String value = "";

        // If this element has nested tags it doesn't have a value and trying to 
        // extract one using dcm4che will result in an UnsupportedOperationException 
        // so check first.
        if (!e.hasDicomObjects()) {
            value = escapeHTML(e.getValueAsString(null, maxLen));
        }
        else {
            value = "";
        }


        String vr = e.vr().toString();
        String desc = o.nameOf(e.tag());
        List<String> l = new ArrayList<String>();
        if (parentTag == null) {
            String[] _s = {tag,"",vr,value,desc};
            l.addAll(Arrays.asList(_s));
        }
        else {
            String[] _s = {parentTag, tag, vr, value, desc};
            l.addAll(Arrays.asList(_s));
        }
        String[] row = l.toArray(new String[l.size()]);
        return row;
    }

    public static String escapeHTML(String o){
        return (o==null)?null: StringEscapeUtils.escapeHtml(o);
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
