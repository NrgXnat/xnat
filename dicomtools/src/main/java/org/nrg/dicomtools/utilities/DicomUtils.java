/*
 * dicomtools: org.nrg.dicomtools.utilities.DicomUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.utilities;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.util.TagUtils;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.RequiredAttributeUnsetException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicomtools.exceptions.AttributeVRMismatchException;
import org.nrg.framework.exceptions.NrgRuntimeException;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
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
     * the dcm4che Tag class. This means only the characters A-Z, a-z, 0-9, '(', ')', and ',' are allowed.
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
     * Converts a map with {@link DicomAttributeIndex} keys to one with string keys. This calls the {@link DicomAttributeIndex#getPath(DicomObjectI)} method
     * and uses the first integer in the returned array. That is converted to the attribute tag name using the {@link #getDicomAttribute(int)} method.
     */
    public static final Function<Map<DicomAttributeIndex, String>, Map<String, String>> MAP_BY_ATTRIBUTE_TO_STRING_FUNCTION = map -> map.keySet().stream().collect(Collectors.toMap(DicomUtils::getDicomAttribute, map::get));

    /**
     * This tries to convert a DICOM header ID&mdash;either a DICOM tag or attribute&mdash;into an integer value that
     * can be used with dcm4che's Attributes class's various get methods. DICOM tags can be in the format "(xxxx,yyyy)" or
     * "xxxx,yyyy" (i.e. with or without bounding parentheses) or as a constant defined in the dcm4che Tag class. DICOM
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
                return Integer.parseUnsignedInt(tag, 16);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid DICOM tag: the specified header doesn't parse to an integer. Submitted tag: " + headerId);
            }
        }
        try {
            return TagUtils.forName(headerId);
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
                Field[] fields = Tag.class.getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType() != int.class) {
                        continue;
                    }
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
     * tag. This calls the {@link DicomAttributeIndex#getPath(DicomObjectI)} method to get the integer value of the tag.
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

    public static String getAttributeName(DicomObjectI doi, int tag) {
        return getAttributeName(doi.getAttributes(), tag);
    }

    public static String getAttributeName(final Attributes attrs, int tag) {
        ElementDictionary dict = ElementDictionary.getStandardElementDictionary();
        String name = dict.keywordOf(tag);
        if (name != null && !name.isEmpty()) {
            return name;
        }

        if (TagUtils.isPrivateTag(tag)) {
            int creatorTag = TagUtils.creatorTagOf(tag);
            String creatorID = attrs.getString(creatorTag);
            if (creatorID != null) {
                name = ElementDictionary.keywordOf(tag, creatorID);
                if (name != null && !name.isEmpty()) {
                    return name;
                }
                return String.format("PrivateTag_%08X_%s", tag, creatorID);
            }
            return String.format("PrivateTag_%08X", tag);
        }
        return String.format("Tag_%08X", tag);
    }

    /**
     * Retrieves codes indicating the deidentification methods from DICOM data contained in the submitted file. The
     * codes are taken from the DICOM tag indicated by the {@link #RecordTag} value. This method reads the DICOM header
     * values from the file into a DICOM object then calls the {@link #getCodes(DicomObjectI)} method to extract the
     * appropriate values.
     *
     * @param file The file from which DICOM data should be retrieved.
     *
     * @return The codes found in the DICOM header field specified by the {@link #RecordTag} value.
     *
     * @throws IOException If an error occurs reading the submitted file.
     */
    public static List<Code> getCodes(final File file) throws IOException {
        return getCodes(read(file, RecordTag + 1));
    }

    /**
     * Retrieves codes indicating the deidentification methods from the submitted DICOM data. The codes are taken from
     * the DICOM tag indicated by the {@link #RecordTag} value.
     *
     * @param dicomObject The DICOM object from which data should be retrieved.
     *
     * @return The codes found in the DICOM header field specified by the {@link #RecordTag} value.
     */
    public static List<Code> getCodes(final Attributes attrs) {
        return Arrays.stream(attrs.tags())
                .filter(tag -> VR.SQ.equals(attrs.getVR(tag))).boxed()
                .flatMap(tag -> attrs.getSequence(tag).stream())
                .filter(item -> item.containsValue(Tag.CodeValue))
                .filter(item -> item.containsValue(Tag.CodingSchemeDesignator))
                .map(Code::new)
                .collect(Collectors.toList());
    }

    public static List<Code> getCodes(final DicomObjectI dicomObject) {
        return getCodes(dicomObject.getAttributes());
    }

    public static String getStringRequired(final DicomObjectI doi, final int tag) throws RequiredAttributeUnsetException {
        final String value = doi.getString(tag);
        if (StringUtils.isBlank(value)) {
            throw new RequiredAttributeUnsetException(doi, tag);
        } else {
            return value;
        }
    }

    public static String getStringRequired(final Attributes attributes, final int tag) throws RequiredAttributeUnsetException {
        final String value = attributes.getString(tag);
        if (StringUtils.isBlank(value)) {
            throw new RequiredAttributeUnsetException(attributes, tag);
        }
        return value;
    }

    public static String getTransferSyntaxUID(final DicomObjectI doi) {
        // Default TS UID is Implicit VR LE (PS 3.5, Section 10)
        String tuid =doi.getString(Tag.TransferSyntaxUID);
        if (tuid ==null) {
            tuid =UID.ImplicitVRLittleEndian;
        }
        return tuid;
    }

    public static String getTransferSyntaxUID(final Attributes attributes) {
        return attributes.getString(Tag.TransferSyntaxUID, UID.ExplicitVRLittleEndian);
    }

    /**
     * Reads a new DicomObject, including File Metainformation Header, from the given InputStream
     *
     * Deprecated because dcm4che5 separates FMI from dataset; this has some annoying consequences
     * (notably decoupling TS from the dataset, though it could be important for decoding pixel data)
     * but is how dcm4che5 does things.
     *
     * @param in     InputStream from which the object will be read
     * @param maxTag last DICOM attribute to be included in the object
     *
     * @return new DicomObject
     *
     * @throws IOException When an error occurs reading or writing data.
     */
    @Deprecated
    public static Attributes read(final InputStream in, final int maxTag) throws IOException {
        try (final InputStream buffered = in instanceof BufferedInputStream ? in : new BufferedInputStream(in);
             final DicomInputStream din = new DicomInputStream(buffered)) {
            din.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            final Attributes fmi = din.readFileMetaInformation();
            final Attributes dataset = din.readDataset(maxTag + 1);
            if (fmi != null) {
                dataset.addAll(fmi);
            }
            return dataset;
        }
    }

    /**
     * Reads a complete new DicomObject from the given InputStream
     *
     * See deprecation note for read(InputStream, int)
     *
     * @param in InputStream from which the object will be read
     *
     * @return new DicomObject
     *
     * @throws IOException When an error occurs reading or writing data.
     */
    @Deprecated
    public static Attributes read(final InputStream in) throws IOException {
        try (final InputStream buffered = in instanceof BufferedInputStream ? in : new BufferedInputStream(in);
             final DicomInputStream din = new DicomInputStream(buffered)) {
            din.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            final Attributes fmi = din.readFileMetaInformation();
            final Attributes dataset = din.readDataset();
            if (fmi != null) {
                dataset.addAll(fmi);
            }
            return dataset;
        }
    }

    /**
     * Reads the named file into a new DicomObject
     *
     * @param file    File to be read
//     * @param handler determines whether next DICOM element should be read
     *
     * @return new DicomObject
     *
     * @throws IOException When an error occurs reading or writing data.
     */
    public static Attributes read(final File file, final int stopTag) throws IOException {
        try (final InputStream fin = file.getName().endsWith(GZIP_SUFFIX) ? new GZIPInputStream(Files.newInputStream(file.toPath())) :
                new FileInputStream(file)) {
            return read(fin, stopTag);
        }
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
    public static Attributes read(final File file) throws IOException {
        try (final InputStream fin = file.getName().endsWith(GZIP_SUFFIX) ? new GZIPInputStream(Files.newInputStream(file.toPath())) :
                Files.newInputStream(file.toPath())) {
            return read(fin);
        }
    }

    /**
     * Reads a DicomObject from the named resource.
     *
     * @param uri     URI of the resource to be read
//     * @param handler The handler for DICOM.
     *
     * @return The new DicomObject
     *
     * @throws IOException When an error occurs reading or writing data.
     */
    public static Attributes read(final URI uri, final int stopTag) throws IOException {
        if ("file".equals(uri.getScheme())) {
            return read(new File(uri), stopTag);
        } else if (uri.isAbsolute()) {
            final URL url = uri.toURL();
            try (final InputStream in = url.openStream()) {
                return read(in, stopTag);
            }
        } else {
            throw new IllegalArgumentException("URIs must be absolute");
        }
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
    public static Attributes read(final URI uri) throws IOException {
        return read(new File(uri));
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

    public static boolean isBigEndian(final DicomObjectI doi) {
        return isBigEndian(doi.getAttributes());
    }

    public static boolean isBigEndian(final Attributes attributes) {
        return UID.ExplicitVRBigEndian.equals(attributes.getString(Tag.TransferSyntaxUID));
    }

    public static String getString(final DicomObjectI doi, final int tag) throws AttributeVRMismatchException {
        return getString(doi.getAttributes(), tag);
    }

    public static String getString(final Attributes attributes, final int tag) throws AttributeVRMismatchException {
        final String[] values;
        final VR vr = attributes.getVR(tag);
        if (VR.UN.equals(vr)) {
            // VR UN gets special rendering for backwards compatibility
            try {
                final byte[] byteVals = attributes.getBytes(tag);
                if (null == byteVals) {
                    return null;
                }
                values = new String[byteVals.length];
                for (int i = 0; i < byteVals.length; i++) {
                    values[i] = Byte.toString(byteVals[i]);
                }
            } catch (IOException e) {
                log.error("error reading UN attribute {}", TagUtils.toString(tag), e);
                return null;
            }
        } else if (VR.SQ.equals(vr)) {
            throw new AttributeVRMismatchException(tag, attributes.getVR(tag).name());
        } else {
            values = attributes.getStrings(tag);
        }
        return combineValues(values);
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
    public static Date getDateTime(final DicomObjectI o, final int dateTag, final int timeTag) {
        final Date date = o.getAttributes().getDate(dateTag);
        final Date time = o.getAttributes().getDate(timeTag);
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

    public static TransferCapability.Role parseRole(String roleStr) {
        if (roleStr == null) return null;
        switch (roleStr.trim().toUpperCase()) {
            case "SCU":
                return TransferCapability.Role.SCU;
            case "SCP":
                return TransferCapability.Role.SCP;
            default:
                throw new IllegalArgumentException("Unknown Role: " + roleStr);
        }
    }

    private static final String               GZIP_SUFFIX                = ".gz";
    private static final Map<Integer, String> DICOM_TAGS                 = new HashMap<>();

    public static final String MULTI_VALUE_SEPARATOR = "\\";

    public static String combineValues(final String[] values) {
        return null == values ? null : String.join(MULTI_VALUE_SEPARATOR, values);
    }
}
