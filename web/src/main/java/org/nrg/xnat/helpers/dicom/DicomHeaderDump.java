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
    DicomObject getHeader(File file) throws IOException, FileNotFoundException {
        final int stopTag;
        if (fields.isEmpty()) {
            stopTag = Tag.PixelData;
        } else {
            stopTag = 1 + Collections.max(fields.keySet());
        }
        final StopTagInputHandler stopHandler = new StopTagInputHandler(stopTag);

        IOException ioexception = null;
        final DicomInputStream dis = new DicomInputStream(file);
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
     *  If this element has nested tags it doesn't have a value and trying to 
        extract one using dcm4che will result in an UnsupportedOperationException 
     * @param e 
     * @param length 
     * @return
     */
    private String getValueAsString(final DicomElement e, final int length) {
        try {
            return !e.hasDicomObjects() ? escapeHTML(e.getValueAsString(null, length)) : "";
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
    String[] makeRow(final DicomObject object, final DicomElement element, final String parentTag, final int maxLen) {
        final String tag = TagUtils.toString(element.tag());

        final String value = this.getValueAsString(element,maxLen);
       
        final String vr = element.vr().toString();

        // This fixes the unfortunate tendency of DICOM tags to use good typographical but poor programming practices.
        final String desc = escapeHTML(object.nameOf(element.tag()));

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
    public XFTTable render() throws IOException,FileNotFoundException {
        XFTTable t = new XFTTable();
        t.initTable(columns);
        if (this.file == null) {
            return t;
        }

        DicomObject header = this.getHeader(new File(this.file));
        //DicomObjectToStringParam formatParams = DicomObjectToStringParam.getDefaultParam();
        DicomObjectToStringParam DEFAULT_PARAM = DicomObjectToStringParam.getDefaultParam();
        DicomObjectToStringParam formatParams = new DicomObjectToStringParam(
        		DEFAULT_PARAM.name, 			// name
        		255,							// valueLength;
        		DEFAULT_PARAM.numItems,			// numItems;
        		DEFAULT_PARAM.lineLength,		// lineLength;
        		DEFAULT_PARAM.numLines, 		// numLines;
        		DEFAULT_PARAM.indent,			// indent
        		DEFAULT_PARAM.lineSeparator);	// line separator

        for (Iterator<DicomElement> it = header.iterator(); it.hasNext();) {
            DicomElement e = it.next();
            try{
                write( t, header, formatParams, e);
            }catch(Exception ex){
                logger.error("Error reading dicom tag,"+ e.tag(),ex);
            }
        }
        return t;
    }
    public void write(XFTTable t,DicomObject header,DicomObjectToStringParam formatParams,DicomElement e){
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
}
