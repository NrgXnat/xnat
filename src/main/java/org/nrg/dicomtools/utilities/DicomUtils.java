/*
 * dicomtools: org.nrg.dicomtools.utilities.DicomUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.utilities;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.iod.module.macro.Code;
import org.nrg.framework.exceptions.NrgRuntimeException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DicomUtils {
    /**
     * The tag which contains the history of edit script application.
     * (0012,0064) - Deidentification Method Code Sequence is a sequence tag meaning that
     * it can contain multiple nested DICOM objects.
     */
    public final static int RecordTag = Tag.DeidentificationMethodCodeSequence;

    /**
     * This tries to convert a DICOM header ID&mdash;either a DICOM tag or attribute&mdash;into an integer value that can be
     * used with the dcm4che DicomObject classes various get methods. DICOM tags can be in the format "(xxxx,yyyy)" or
     * "xxxx,yyyy" (i.e. with or without bounding parentheses). DICOM attributes must be in the same form as they are
     * represented as field names in the dcm4che Tag class.
     * @param headerId    The header ID in the form of a DICOM tag or attribute.
     * @return The integer value representing the submitted DICOM tag or attribute.
     */
    public static int parseDicomHeaderId(final String headerId) {
        final Matcher matcher = DICOM_TAG.matcher(headerId);
        if (matcher.matches()) {
            final String tag = matcher.group(1) + matcher.group(2);
            return Integer.parseInt(tag, 16);
        } else {
            return Tag.forName(headerId);
        }
    }

    /**
     * Gets the DICOM attribute name&mdash;e.g. SeriesDescription, Modality, or StudyInstanceUID&mdash;for the indicated tag.
     * Note that this requires the integer value for the tag. You can get the DICOM attribute for a DICOM tag by calling
     * the {@link #getDicomAttribute(String)} version of this method.
     * @param tag    The DICOM tag for which you want to retrieve the DICOM attribute name.
     * @return The corresponding DICOM attribute name, if available.
     */
    public static String getDicomAttribute(final int tag) {
        if (DICOM_TAGS.isEmpty()) {
            synchronized (DICOM_TAGS) {
                for (Field field : Tag.class.getDeclaredFields()) {
                    try {
                        DICOM_TAGS.put(field.getInt(null), field.getName());
                    } catch (IllegalAccessException ignored) {
                        // We'll just ignore this: fields that we can't access are irrelevant.
                    }
                }
            }
        }
        if (!DICOM_TAGS.containsKey(tag)) {
            return null;
        }
        return DICOM_TAGS.get(tag);
    }

    /**
     * Get the DICOM attribute for a DICOM tag in the format "(xxxx,yyyy)" or "xxxx,yyyy" with or without bounding
     * parentheses. This is a convenience method that calls the {@link #parseDicomHeaderId(String)} method to get the
     * integer representation, then calls {@link #getDicomAttribute(int)} with that result.
     * @param tag    The DICOM tag in the format "(xxxx,yyyy)" or "xxxx,yyyy".
     * @return The corresponding DICOM attribute name, if available.
     */
    public static String getDicomAttribute(final String tag) {
        final Matcher matcher = DICOM_TAG.matcher(tag);
        if (!matcher.matches()) {
            throw new NrgRuntimeException("The tag for this method must be in the form \"(xxxx,yyyy)\" or \"xxxx,yyyy\". " + tag + " is an invalid value.");
        }
        return getDicomAttribute(parseDicomHeaderId(tag));
    }

    /**
     * Retrieves codes indicating the deidentification methods from DICOM data contained in the submitted file. The
     * codes are taken from the DICOM tag indicated by the {@link #RecordTag} value. This method reads the DICOM header
     * values from the file into a DICOM object then calls the {@link #getCodes(DicomObject)} method to extract the
     * appropriate values.
     *
     * @param file The file from which DICOM data should be retrieved.
     *
     * @return The codes found in the DICOM header field specified by the {@link #RecordTag} value.
     *
     * @throws IOException If an error occurs reading the submitted file.
     */
    public static Code[] getCodes(final File file) throws IOException {
        try (DicomInputStream input = new DicomInputStream(file)) {
            input.setHandler(new StopTagInputHandler(RecordTag + 1));
            DicomObject dicomObject = input.readDicomObject();
            return getCodes(dicomObject);
        }
    }

    /**
     * Retrieves codes indicating the deidentification methods from the submitted DICOM data. The codes are taken from
     * the DICOM tag indicated by the {@link #RecordTag} value.
     *
     * @param dicomObject The DICOM object from which data should be retrieved.
     *
     * @return The codes found in the DICOM header field specified by the {@link #RecordTag} value.
     */
    public static Code[] getCodes(final DicomObject dicomObject) {
        final DicomElement record = dicomObject.get(RecordTag);
        return Code.toCodes(record);
    }

    public static final Pattern DICOM_TAG = Pattern.compile("^[(]*([\\dA-Fa-f]{4}),([\\dA-Fa-f]{4})[)]*$");

    private static final Map<Integer, String> DICOM_TAGS = new HashMap<>();
}
