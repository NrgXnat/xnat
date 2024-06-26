/*
 * dicomtools: org.nrg.dicomtools.utilities.DicomUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.utilities;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.*;
import org.dcm4che2.io.DicomInputHandler;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.iod.module.macro.Code;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.RequiredAttributeUnsetException;
import org.nrg.dicomtools.exceptions.AttributeVRMismatchException;
import org.nrg.framework.exceptions.NrgRuntimeException;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Slf4j
public class DicomUtils {
    /**
     * The tag which contains the history of edit script application.
     * (0012,0064) - Deidentification Method Code Sequence is a sequence tag meaning that
     * it can contain multiple nested DICOM objects.
     */
    public final static int RecordTag = Tag.DeidentificationMethodCodeSequence;

    /**
     * Tests that a string contains only characters that might be in a DICOM header tag (XXXX,YYYY) or ID as defined in
     * the dcm4che2 Tag class. This means only the characters A-Z, a-z, 0-9, '(', ')', and ',' are allowed.
     */
    public final static Pattern VALID_DICOM_HEADER_OR_TAG_CHARS = Pattern.compile("^[\\p{Alnum}(),]+$");

    /**
     * A pattern that matches DICOM tags in the standard hex numeric format, e.g. "XXXX,YYYY". The header may have
     * parentheses or not, that is, it can be "XXXX,YYYY" or "(XXXX,YYYY)". One thing this doesn't check for is
     * unmatched parentheses, since this is nearly impossible to do with Java's lack of support for recursive
     * expressions. If this is important, create a matcher from the pattern, then check the groups "open" and "closed".
     * If they're both blank or both not blank, then you have balanced parentheses.
     */
    public static final Pattern DICOM_TAG = Pattern.compile("^(?<open>[(]?)(?<tag>(?<prefix>[\\p{Alnum}]{4}),(?<suffix>[\\p{Alnum}]{4}))(?<close>[)]?)$");

    /**
     * Converts a map with {@link DicomAttributeIndex} keys to one with string keys. This calls the {@link DicomAttributeIndex#getPath(DicomObject)} method
     * and uses the first integer in the returned array. That is converted to the attribute tag name using the {@link #getDicomAttribute(int)} method.
     */
    public static final Function<Map<DicomAttributeIndex, String>, Map<String, String>> MAP_BY_ATTRIBUTE_TO_STRING_FUNCTION = map -> map.keySet().stream().collect(Collectors.toMap(DicomUtils::getDicomAttribute, map::get));

    /**
     * This tries to convert a DICOM header ID&mdash;either a DICOM tag or attribute&mdash;into an integer value that
     * can be used with the dcm4che DicomObject classes various get methods. DICOM tags can be in the format "(xxxx,yyyy)" or
     * "xxxx,yyyy" (i.e. with or without bounding parentheses) or as a constant defined in the dcm4che2 Tag class. DICOM
     * attributes must be in the same form as they are represented as field names in the dcm4che Tag class.
     *
     * @param headerId The header ID in the form of a DICOM tag or attribute.
     *
     * @return The integer value representing the submitted DICOM tag or attribute.
     */
    public static int parseDicomHeaderId(final String headerId) {
        if (StringUtils.isBlank(headerId) || !VALID_DICOM_HEADER_OR_TAG_CHARS.matcher(headerId).matches()) {
            throw new IllegalArgumentException("Invalid DICOM tag: blank or invalid characters submitted. Submitted tag: " + headerId);
        }
        final Matcher matcher = DICOM_TAG.matcher(headerId);
        if (matcher.find()) {
            if (StringUtils.isBlank(matcher.group("open")) != StringUtils.isBlank(matcher.group("close"))) {
                throw new IllegalArgumentException("Invalid DICOM tag: must either have no or matching parentheses. Submitted tag: " + headerId);
            }
            final String tag = matcher.group("prefix") + matcher.group("suffix");
            try {
                return Integer.parseInt(tag, 16);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid DICOM tag: the specified header doesn't parse to an integer. Submitted tag: " + headerId);
            }
        }
        try {
            return Tag.forName(headerId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid DICOM tag: unknown tag name. Submitted tag: " + headerId);
        }
    }

    /**
     * Gets the DICOM attribute name&mdash;e.g. SeriesDescription, Modality, or StudyInstanceUID&mdash;for the indicated
     * tag.
     * Note that this requires the integer value for the tag. You can get the DICOM attribute for a DICOM tag by
     * calling
     * the {@link #getDicomAttribute(String)} version of this method.
     *
     * @param tag The DICOM tag for which you want to retrieve the DICOM attribute name.
     *
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
     * Gets the DICOM attribute name&mdash;e.g. SeriesDescription, Modality, or StudyInstanceUID&mdash;for the indicated
     * tag. This calls the {@link DicomAttributeIndex#getPath(DicomObject)} method to get the integer value of the tag.
     * If the attribute is a nested tag, this method uses the last item in the path. This may work properly, but you may
     * get unpredictable results.
     *
     * @param attribute The DICOM attribute for which you want to retrieve the name.
     *
     * @return The corresponding DICOM attribute name, if available.
     */
    public static String getDicomAttribute(final DicomAttributeIndex attribute) {
        final Integer[] path = attribute.getPath(null);
        if (ArrayUtils.isEmpty(path)) {
            log.info("Tried to get the path for a \"{}\" attribute but it was empty", attribute.getColumnName());
            return null;
        }
        final Integer lastTag = path[path.length - 1];
        if (path.length > 1) {
            log.warn("Got the path for a \"{}\" attribute but there were {} values. I'll use the last value, which is: {}", attribute.getColumnName(), path.length, lastTag);
        }
        return getDicomAttribute(lastTag);
    }

    /**
     * Get the DICOM attribute for a DICOM tag in the format "(xxxx,yyyy)" or "xxxx,yyyy" with or without bounding
     * parentheses. This is a convenience method that calls the {@link #parseDicomHeaderId(String)} method to get the
     * integer representation, then calls {@link #getDicomAttribute(int)} with that result.
     *
     * @param tag The DICOM tag in the format "(xxxx,yyyy)" or "xxxx,yyyy".
     *
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

    public static String getStringRequired(final DicomObject o, final int tag) throws RequiredAttributeUnsetException {
        final String v = o.getString(tag);
        if (null == v || "".equals(v)) {
            throw new RequiredAttributeUnsetException(o, tag);
        } else {
            return v;
        }
    }

    public static String getTransferSyntaxUID(final DicomObject o) {
        // Default TS UID is Implicit VR LE (PS 3.5, Section 10)
        return o.getString(Tag.TransferSyntaxUID, UID.ImplicitVRLittleEndian);
    }

    /**
     * Returns a stop-tag input handler with the stop tag set to the value for the submitted parameter plus one. In
     * comparison to the {@link #getStopTagInputHandler(int)} version, this method truncates any part of the long value
     * <i>above</i> the value of 2 bytes (0xFFFFFFFF).
     *
     * @param stopTag The last tag to be processed.
     *
     * @return A stop-tag input handler that indicates that the DICOM tags should processed up to the submitted tag.
     */
    public static StopTagInputHandler getStopTagInputHandler(final long stopTag) {
        // Scanning Sequence is the largest internally required tag:
        // > SOP Class UID and all of File metainformation Header
        final long truncated = 0xffffffffL & stopTag;
        if (0xffffffffL == truncated) {
            return null;
        }
        return getStopTagInputHandler((int) truncated);
    }

    /**
     * Returns a stop-tag input handler with the stop tag set to the value for the submitted parameter plus one. The
     * maximum stop-tag value is {@link Tag#ScanningSequence}. If you need to anonymize tag values above this, you
     * should call {@link #getMaxStopTagInputHandler()} or create the {@link StopTagInputHandler} directly.
     *
     * @param stopTag The last tag to be processed.
     *
     * @return A stop-tag input handler that indicates that the DICOM tags should processed up to the submitted tag.
     */
    public static StopTagInputHandler getStopTagInputHandler(final int stopTag) {
        return (stopTag > Tag.PixelData) ? MAX_STOP_TAG_INPUT_HANDLER : stopTag > 0 ? new StopTagInputHandler(stopTag + 1) : null;
    }

    /**
     * Returns the maximum useful stop-tag input handler, which processes all DICOM tags up to the pixel data.
     *
     * @return The maximum useful stop-tag input handler.
     */
    public static StopTagInputHandler getMaxStopTagInputHandler() {
        return MAX_STOP_TAG_INPUT_HANDLER;
    }

    /**
     * Reads a new DicomObject from the given InputStream
     *
     * @param in      InputStream from which the object will be read
     * @param handler determines whether next DICOM element should be read
     *
     * @return new DicomObject
     *
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final InputStream in, final DicomInputHandler handler) throws IOException {
        try (final DicomInputStream din = new DicomInputStream(new BufferedInputStream(in))) {
            if (null != handler) {
                din.setHandler(handler);
            }
            final DicomObject o = din.readDicomObject();
            if (o.contains(Tag.SOPClassUID)) {
                return o;
            } else {
                throw new IOException("no SOP class UID in prospective DICOM object");
            }
        } catch (Throwable t) {
            throw new IOException("unable to read as DICOM", t);
        }
    }

    /**
     * Reads a new DicomObject from the given InputStream
     *
     * @param in     InputStream from which the object will be read
     * @param maxTag last DICOM attribute to be included in the object
     *
     * @return new DicomObject
     *
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final InputStream in, final int maxTag) throws IOException {
        return read(in, getStopTagInputHandler(maxTag));
    }

    /**
     * Reads a complete new DicomObject from the given InputStream
     *
     * @param in InputStream from which the object will be read
     *
     * @return new DicomObject
     *
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final InputStream in) throws IOException {
        return read(in, null);
    }

    /**
     * Reads the named file into a new DicomObject
     *
     * @param file    File to be read
     * @param handler determines whether next DICOM element should be read
     *
     * @return new DicomObject
     *
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final File file, final DicomInputHandler handler) throws IOException {
        try (final InputStream fin = file.getName().endsWith(GZIP_SUFFIX) ? new GZIPInputStream(new FileInputStream(file)) : new FileInputStream(file)) {
            return read(fin, handler);
        }
    }

    /**
     * Reads the named file into a new DicomObject
     *
     * @param file   File to be read
     * @param maxTag last tag to be included in the object
     *
     * @return new DicomObject
     *
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final File file, final int maxTag) throws IOException {
        return read(file, getStopTagInputHandler(maxTag));
    }

    /**
     * Reads the complete named file into a new DicomObject
     *
     * @param file File to be read
     *
     * @return new DicomObject
     *
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final File file) throws IOException {
        return read(file, null);
    }

    /**
     * Reads a DicomObject from the named resource.
     *
     * @param uri     URI of the resource to be read
     * @param handler The handler for DICOM.
     *
     * @return The new DicomObject
     *
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final URI uri, final DicomInputHandler handler) throws IOException {
        if ("file".equals(uri.getScheme())) {
            return read(new File(uri), handler);
        } else if (uri.isAbsolute()) {
            final URL url = uri.toURL();
            try (final InputStream in = url.openStream()) {
                return read(in, handler);
            }
        } else {
            throw new IllegalArgumentException("URIs must be absolute");
        }
    }

    /**
     * Reads a DicomObject from the named resource.
     *
     * @param uri    URI of the resource to be read
     * @param maxTag last tag to be included in the object
     *
     * @return new DicomObject
     *
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final URI uri, final int maxTag) throws IOException {
        return read(uri, getStopTagInputHandler(maxTag));
    }

    /**
     * Reads a complete DicomObject from the named resource.
     *
     * @param uri URI of the resource to be read
     *
     * @return new DicomObject
     *
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final URI uri) throws IOException {
        return read(uri, null);
    }

    public static URI getQualifiedUri(final String address) throws URISyntaxException {
        return address.startsWith("/") ? new URI("file://" + address) : new URI(address);
    }

    public static StringBuilder stripTrailingChars(final StringBuilder sb, final char toStrip) {
        for (int i = sb.length() - 1; i >= 0 && toStrip == sb.charAt(i); i--) {
            sb.deleteCharAt(i);
        }
        return sb;
    }

    public static String stripTrailingChars(final String s, final char toStrip) {
        return stripTrailingChars(new StringBuilder(s), toStrip).toString();
    }

    private static StringBuffer join(final StringBuffer sb, final int[] array) {
        if (array.length > 0) {
            sb.append(array[0]);
            for (int i = 1; i < array.length; i++) {
                sb.append("\\");
                sb.append(array[i]);
            }
        }
        return sb;
    }

    private static String join(final int[] array) {
        return join(new StringBuffer(), array).toString();
    }

    private interface Converter {
        String convert(DicomObject o, DicomElement e) throws AttributeVRMismatchException;
    }

    private final static Map<VR, Converter> conversions =
            ImmutableMap.of(VR.SQ, (dicomObject, dicomElement) -> {
                                throw new AttributeVRMismatchException(dicomElement.tag(), dicomElement.vr());
                            },
                            VR.UN, (dicomObject, dicomElement) -> dicomElement.getString(dicomObject.getSpecificCharacterSet(), false),
                            VR.AT, (dicomObject, dicomElement) -> join(dicomElement.getInts(false)),
                            VR.OB, (dicomObject, dicomElement) -> VR.OB.toString(dicomElement.getBytes(), dicomObject.bigEndian(), dicomObject.getSpecificCharacterSet()));

    public static String getString(final DicomObject o, final int tag) throws AttributeVRMismatchException {
        final DicomElement de = o.get(tag);
        if (null == de) {
            return null;
        }
        final Converter converter = conversions.get(de.vr());
        if (null == converter) {
            // all other data types can be treated as simple strings, maybe with
            // multiple values separated by backslashes.  Join these.
            try {
                return String.join("\\", de.getStrings(o.getSpecificCharacterSet(), false));
            } catch (UnsupportedOperationException e) {
                throw new AttributeVRMismatchException(tag, de.vr());
            }
        } else {
            return converter.convert(o, de);
        }
    }


    private final static Pattern VALID_UID_PATTERN = Pattern.compile("(0|([1-9][0-9]*))(\\.(0|([1-9][0-9]*)))*");
    private final static int     UID_MIN_LEN       = 1;
    private final static int     UID_MAX_LEN       = 64;

    public static boolean isValidUID(final CharSequence uid) {
        if (null == uid) {
            return false;
        }
        final int len = uid.length();
        return !(len < UID_MIN_LEN || UID_MAX_LEN < len) && VALID_UID_PATTERN.matcher(uid).matches();
    }

    private static final TimeZone TIME_ZONE = Calendar.getInstance().getTimeZone();

    /**
     * Returns a new Date object that represents a date/time combination from the named
     * DA and TM attributes of the given DicomObject.  Assumes, but does not verify,
     * that attributes are of VR DA and TM, respectively.
     *
     * @param o       DicomObject from which date/time should be extracted
     * @param dateTag DA attribute
     * @param timeTag TM attribute
     *
     * @return combined Date object
     */
    public static Date getDateTime(final DicomObject o, final int dateTag, final int timeTag) {
        final Date date = o.getDate(dateTag);
        final Date time = o.getDate(timeTag);
        if (null == date) {
            return time;
        } else if (null == time) {
            return date;
        } else {
            if (TIME_ZONE.inDaylightTime(date)) {
                Calendar localTime = Calendar.getInstance(TIME_ZONE);
                localTime.setTime(date);

                Calendar fixTime = Calendar.getInstance();
                fixTime.setTime(time);
                localTime.set(Calendar.HOUR_OF_DAY, fixTime.get(Calendar.HOUR_OF_DAY));
                localTime.set(Calendar.MINUTE, fixTime.get(Calendar.MINUTE));
                return new Date(localTime.getTimeInMillis());
            } else {
                return new Date(date.getTime() + time.getTime() + TIME_ZONE.getOffset(date.getTime()));
            }
        }
    }

    private static final String               GZIP_SUFFIX                = ".gz";
    private static final StopTagInputHandler  MAX_STOP_TAG_INPUT_HANDLER = new StopTagInputHandler(Tag.PixelData);
    private static final Map<Integer, String> DICOM_TAGS                 = new HashMap<>();
}
