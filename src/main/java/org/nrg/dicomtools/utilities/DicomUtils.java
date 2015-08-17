package org.nrg.dicomtools.utilities;

import org.dcm4che2.data.Tag;
import org.nrg.framework.exceptions.NrgRuntimeException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DicomUtils {
    /**
     * This tries to convert a DICOM header ID&em;either a DICOM tag or attribute&em;into an integer value that can be
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
     * Gets the DICOM attribute name&em;e.g. SeriesDescription, Modality, or StudyInstanceUID&em;for the indicated tag.
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

    public static final Pattern DICOM_TAG = Pattern.compile("^[(]*(\\d{4}),(\\d{4})[)]*$");

    private static final Map<Integer, String> DICOM_TAGS = new HashMap<>();
}
