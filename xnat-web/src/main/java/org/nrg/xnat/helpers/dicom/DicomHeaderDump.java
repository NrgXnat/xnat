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
import org.apache.commons.text.StringEscapeUtils;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.util.TagUtils;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.xft.XFTTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public DicomHeaderDump(final String file, final Map<Integer,Set<String>> fields) {
        this.file = file;
        this.fields = ImmutableMap.copyOf(fields);
    }
    
    @SuppressWarnings("unused")
    public DicomHeaderDump(final String file) {
        this(file, Collections.<Integer,Set<String>>emptyMap());
    }

    /**
     * Read the header of the DICOM file ignoring the pixel data.
     * @param file The DICOM file to read.
     * @return A DICOM object containing the file's headers.
     * @throws IOException When an error occurs reading the file.
     * @throws FileNotFoundException When the specified file isn't found.
     */
    DicomObjectI getHeader(File file) throws MizerException {
        if (fields.isEmpty()) {
            // Read it all. The reader already excludes bulk data, so the old PixelData stop tag never
            // skipped the pixels -- it truncated everything sorting after them.
            return DicomObjectFactory.newInstance(file, false);
        }
        // Tags are unsigned: a signed max would rank a group >= 0x8000 as the smallest value.
        final int maxTag = fields.keySet().stream().max(Integer::compareUnsigned).orElse(0);
        // Guards the public constructor, which takes any map: +1 would wrap to 0 and stop the read dead.
        final int stopTag = AttributeTags.NOT_RESOLVED == maxTag ? -1 : maxTag + 1;
        return DicomObjectFactory.newInstance(file, stopTag);
    }
    
    /**
     *  If this element has nested tags it doesn't have a value and trying to 
        extract one using dcm4che will result in an UnsupportedOperationException 
     * @param element
     * @param length
     * @return
     */
    private String getValueAsString(final DicomElementI element, final int length) {
        try {
            return !element.hasItems() ? escapeHTML(element.getValueAsString()) : "";
        }catch(UnsupportedOperationException usex) {
            return "UnsupportedBinarySequence";
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
    String[] makeRow(final DicomObjectI object, final DicomElementI element, final String parentTag, final int maxLen) {
        final String tag = TagUtils.toString(element.tag());

        final String value = this.getValueAsString(element,maxLen);
       
        final String vr = element.getVRAsString();

        // This fixes the unfortunate tendency of DICOM tags to use good typographical but poor programming practices.
        final String desc = escapeHTML(ElementDictionary.keywordOf(element.tag(), null));

        final List<String> strings = new ArrayList<>(parentTag == null ? Arrays.asList(tag, "", vr, value, desc) : Arrays.asList(parentTag, tag, vr, value, desc));
        return strings.toArray(new String[0]);
    }

    public static String escapeHTML(final String value) {
        return value == null ? null : StringEscapeUtils.escapeHtml4(value);
    }

    /**
     * Render the DICOM header to an XFTTable supporting one level of tag nesting.
     *
     * @return The DICOM header values rendered into an {@link XFTTable} object.
     *
     * @throws IOException When an error occurs reading the file.
     * @throws FileNotFoundException When the specified file isn't found.
     */
    public XFTTable render() throws IOException, FileNotFoundException, MizerException {
        XFTTable t = new XFTTable();
        t.initTable(columns);
        if (this.file == null) {
            return t;
        }

        DicomObjectI header = this.getHeader(new File(this.file));
        int maxLen = 255;

        for (Iterator<DicomElementI> it = header.iterator(); it.hasNext();) {
            DicomElementI e = it.next();
            try{
                write( t, header, maxLen, e);
            }catch(Exception ex){
                logger.error("Error reading dicom tag,"+ e.tag(),ex);
            }
        }
        return t;
    }
    public void write(XFTTable t,DicomObjectI header,int maxLen,DicomElementI e){
        // dcm4che3 - header is already DicomObjectI, no need to wrap
        DicomElementI dei = header.getElement(e.tag());
        if (fields.isEmpty() || fields.containsKey(e.tag())) {
            if (e.hasItems()) {
                for (int i = 0; i < e.countItems(); i++) {
                    DicomObjectI o = e.getDicomObject(i);
                    t.insertRow(makeRow(header, e, TagUtils.toString(e.tag()), maxLen));
                    for (Iterator<DicomElementI> it1 = o.iterator(); it1.hasNext();) {
                        DicomElementI e1 = it1.next();
                        t.insertRow(makeRow(header, e1, TagUtils.toString(e.tag()), maxLen));
                    }
                }
            } else if (SiemensShadowHeader.isShadowHeader(header, dei)) {
                SiemensShadowHeader.addRows(t, header, dei, fields.get(e.tag()));
            } else {
                t.insertRow(makeRow(header, e, null, maxLen));		
            }
        }
    }
}
