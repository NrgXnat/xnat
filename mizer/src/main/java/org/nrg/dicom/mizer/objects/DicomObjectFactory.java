package org.nrg.dicom.mizer.objects;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.*;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.tags.*;
import org.nrg.dicom.mizer.tags.Tag;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.visitors.AssignIfExistsDicomObjectVisitor;
import org.nrg.dicom.mizer.visitors.DeleteDicomObjectVisitor;
import org.nrg.dicom.mizer.visitors.DumpDicomTagVisitor;
import org.nrg.dicom.mizer.visitors.OrphanPvtCreatorIDExterminator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static org.dcm4che2.util.TagUtils.isPrivateDataElement;

/**
 * Instantiate concrete implementations of DicomObjectI.
 * <p>
 * This implementation returns instances of {@link MizerDicomObject}
 */
public class DicomObjectFactory {

    private static final Logger logger = LoggerFactory.getLogger(DicomObjectFactory.class);

    /**
     * Create an empty DicomObjectI.
     *
     * @return empty {@link MizerDicomObject}.
     */
    public static DicomObjectI newInstance() {
        return new MizerDicomObject();
    }

    /**
     * Create DicomObjectI representing DICOM object in file.
     *
     * @param file containing DICOM object.
     * @return {@link MizerDicomObject}.
     * @throws MizerException on error.
     */
    public static DicomObjectI newInstance(final File file) throws MizerException {
        return new MizerDicomObject(file);
    }

    /**
     * Create DicomObjectI representing DICOM object in {@link InputStream}.
     *
     * @param inputStream containing DICOM object.
     * @return {@link MizerDicomObject}.
     * @throws MizerException on error.
     */
    public static DicomObjectI newInstance(final InputStream inputStream) throws MizerException {
        return new MizerDicomObject(inputStream);
    }

    /**
     * Create DicomObjectI from the provided dcm4che2 dicom object and match file.
     * <p>
     * This causes the dcm4ch4 lib to leak, but hey, backwards compatability.
     * This is for backwards compatibility with anonymize package.
     * <p>
     * This implementation ignores matchFile. This is likely a vestige from DE4's use by another codebase (maybe DicomBrowser?)
     * that hasn't carried over into DE6.
     *
     * @param matchFile   TODO:  What is this? Ignored for now. Where might this be used?
     * @param dicomObject The DICOM object to be processed.
     */
    public static DicomObjectI newInstance(final File matchFile, final DicomObject dicomObject) {
        return new MizerDicomObject(dicomObject);
    }

    /**
     * Create DicomObjectI from the provided dcm4che2 dicom object.
     * <p>
     * This causes the dcm4ch4 lib to leak, but hey, backwards compatability.
     * This is for backwards compatibility with anonymize package.
     *
     * @param dicomObject The DICOM object to be processed.
     */
    public static DicomObjectI newInstance(final DicomObject dicomObject) {
        return new MizerDicomObject(dicomObject);
    }

    /**
     * Implementation of {@link DicomObjectI} as inner class backed by the third-party lib dcm4che2.
     */
    public static class MizerDicomObject implements DicomObjectI {

        private DicomObject dobj;
        private final DeleteDicomObjectVisitor deleteVisitor = new DeleteDicomObjectVisitor();

        /**
         * Create an empty object.
         */
        public MizerDicomObject() {
            this.dobj = new BasicDicomObject();
        }

        /**
         * Create from the DICOM object in file.
         *
         * @param file DICOM object file.
         * @throws MizerException on error.
         */
        public MizerDicomObject(File file) throws MizerException {
            try (final InputStream fin = getInputStream(file); final DicomInputStream dis = new DicomInputStream(fin)) {
                dobj = dis.readDicomObject();
            } catch (IOException e) {
                throw new MizerException(e);
            }
        }

        /**
         * Create from DICOM object in {@link InputStream}
         *
         * @param inputStream to DICOM object.
         * @throws MizerException on error.
         */
        public MizerDicomObject(final InputStream inputStream) throws MizerException {
            try (final DicomInputStream dis = new DicomInputStream(inputStream)) {
                dobj = dis.readDicomObject();
            } catch (IOException e) {
                throw new MizerException(e);
            }
        }

        /**
         * Create from dcm4che2 DicomObject.
         *
         * @param dicomObject dcm4che2 DICOM object.
         */
        public MizerDicomObject(final DicomObject dicomObject) {
            this.dobj = dicomObject;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getString(TagPath tagPath) {
            return getString(resolve(tagPath, false));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getString(int tag) {
            int[] tagArray = new int[]{tag};
            final String value = getString(tagArray);
            if (logger.isDebugEnabled()) {
                logger.debug("Got: {} = {}", TagPath.toString(tagArray), value);
            }
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getString(final int... tagArray) {
            return joinMultipleValues(getStrings(tagArray));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String[] getStrings(TagPath tagPath) {
            return getStrings(resolve(tagPath, false));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String[] getStrings(int tag) {
            int[] tagArray = new int[]{tag};
            final String[] value = getStrings(tagArray);
            if (logger.isDebugEnabled()) {
                logger.debug("Got: {} = {}", TagPath.toString(tagArray), value);
            }
            return value;
        }

        /**
         * {@inheritDoc}
         * All implementations of getString and getStrings ultimately rely on this method.
         */
        @Override
        public String[] getStrings(final int... tagArray) {
            // tags with VR == UN do not have an implementation of getStrings(). Are there other VRs that do this?
            // Is there a better way to detect tags with multiplicity > 1?
            if (logger.isTraceEnabled()) {
                logger.trace("Fetching: {}", TagPath.toString(tagArray));
            }
            if (isVR_UnAndPresentAndEmpty(tagArray)) {
                return new String[]{};
            }
            String[] strings;
            try {
                strings = dobj.getStrings(tagArray);
            } catch (UnsupportedOperationException e) {
                strings = new String[1];
                strings[0] = dobj.getString(tagArray);
            }
            return strings;
        }

        /**
         * Join string array into a single string using the DICOM-standard-separator backslash.
         *
         * @param strings array to join
         * @return concatenated strings joined with DICOM-standard backslash
         */
        protected String joinMultipleValues(String[] strings) {
            return (strings != null) ? String.join("\\", strings) : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public byte[] getBytes(int tag) {
            return dobj.getBytes(tag);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void putBytes(int tag, String vrString, byte[] b) {
            VR vr = VR.UN;
            switch (vrString) {
                case "OB":
                    vr = VR.OB;
                    break;
                case "OW":
                    vr = VR.OW;
                    break;
                default:
                    logger.debug("setting bytes with VR UN.");
                    break;
            }
            dobj.putBytes(tag, vr, b);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getVR(int tag) {
            return vr(tag).toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getVR(TagPath tagPath) {
            if (tagPath.isSingular()) {
                return vr(tagPath.getTagsAsArray()).toString();
            } else {
                throw new IllegalArgumentException("tagPath is not singular");
            }
        }

        private VR vr(int tag) {
            // get existing VR if present
            VR vr = (dobj.get(tag) != null) ? dobj.get(tag).vr() : null;
            if (vr == null) {
                // look up known value. UN if not found.
                vr = (dobj.vrOf(tag) != null) ? dobj.vrOf(tag) : VR.UN;
            }
            return vr;
        }

        private VR vr(int[] tag) {
            // get existing VR if present
            VR vr = (dobj.get(tag) != null) ? dobj.get(tag).vr() : null;
            if (vr == null) {
                // look up known value. UN if not found.
                vr = (dobj.vrOf(tag[tag.length - 1]) != null) ? dobj.vrOf(tag[tag.length - 1]) : VR.UN;
            }
            return vr;
        }

        private VR vr(String vrString) {
            switch (vrString) {
                case "UN_SIEMENS":
                    return VR.UN_SIEMENS;
                case "AS":
                    return VR.AS;
                case "AT":
                    return VR.AT;
                case "CS":
                    return VR.CS;
                case "DA":
                    return VR.DA;
                case "DS":
                    return VR.DS;
                case "DT":
                    return VR.DT;
                case "FD":
                    return VR.FD;
                case "FL":
                    return VR.FL;
                case "IS":
                    return VR.IS;
                case "LO":
                    return VR.LO;
                case "LT":
                    return VR.LT;
                case "OB":
                    return VR.OB;
                case "OF":
                    return VR.OF;
                case "OW":
                    return VR.OW;
                case "PN":
                    return VR.PN;
                case "SH":
                    return VR.SH;
                case "SL":
                    return VR.SL;
                case "SQ":
                    return VR.SQ;
                case "SS":
                    return VR.SS;
                case "ST":
                    return VR.ST;
                case "TM":
                    return VR.TM;
                case "UI":
                    return VR.UI;
                case "UL":
                    return VR.UL;
                case "UN":
                    return VR.UN;
                case "US":
                    return VR.US;
                case "UT":
                    return VR.UT;
            }
            return VR.UN;
        }

        private boolean isVR_UnAndPresentAndEmpty(int[] tags) {
            return "UN".equals(this.vr(tags).toString())
                    && this.contains(tags)
                    && this.isEmpty(tags);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<Period> getAge(int tag) {
            String ageString = dobj.getString(tag);
            if (StringUtils.isEmpty(ageString)) {
                return Optional.empty();
            }
            try {
                int length = Integer.parseInt(ageString.substring(0, 3));
                String units = ageString.substring(3, 4);
                switch (units) {
                    case "Y":
                        return Optional.of(Period.ofYears(length));
                    case "M":
                        return Optional.of(Period.ofMonths(length));
                    case "W":
                        return Optional.of(Period.ofWeeks(length));
                    case "D":
                        return Optional.of(Period.ofDays(length));
                    default:
                        throw new IllegalArgumentException(String.format("Unknown age format in tag %d: '%s'", tag, ageString));
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("Error parsing age in tag %d: '%s'", tag, ageString));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int resolvePrivateTag(int tag, String pvtCreator, boolean create) {
            return dobj.resolveTag(tag, pvtCreator, create);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void assign(TagPath tagPath, String value) {
            if (tagPath.isSingular()) {
                if (value == null) {
                    logger.debug("Failed to assign null value to tag: {}", tagPath);
                    return;
                }
                int[] ia = this.resolve(tagPath, true);
                putString(ia, value);
            } else {
                throw new UnsupportedOperationException("Assigning to a tagpath that maps to multiple attributes in not supported. TagPath: " + tagPath);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void assignIfExists(TagPath tagPath, Value value) {
            DicomObjectVisitor visitor = new AssignIfExistsDicomObjectVisitor(tagPath, value);
            visitor.visit(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void assign(int tag, Value value) {
            putString(tag, value.asString());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void assign(int[] tagPath, Value value) {
            putString(tagPath, value.asString());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void delete(int tag) {
            dobj.remove(tag);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void delete(int[] tags) {
            dobj.remove(tags);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void deleteAllTags() {
            dobj.clear();
        }

        /**
         * {@inheritDoc}
         * <p>
         * One would think one could use tagPathToDelete.isSingular() to avoid visiting all tags, but a singular
         * TagPath may not be singular in the context of some bad-data. In particular, private data with two blocks with
         * the same creator ID in a single group.
         */
        @Override
        public void delete(final TagPath tagPathToDelete) {
            deleteVisitor.setTagPathToDelete(tagPathToDelete);
            deleteVisitor.visit(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void deleteEmptyPrivateBlocks() {
            DicomObjectVisitor visitor = new OrphanPvtCreatorIDExterminator();
            visitor.visit(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DicomObject getDcm4che2Object() {
            return dobj;
        }

        /**
         * {@inheritDoc}
         * <p>
         * This implementation only adds transfer syntax UID of Explicit VR Little Endian
         */
        @Override
        public void addMetaHeader() {
            dobj.putString(0x00020010, VR.UI, "1.2.840.10008.1.2.1");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<DicomElementI> iterator() {
            return new MizerDicomObject.DOIterator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<int[]> resolveTagPath(TagPath tagPath) {
            if (!tagPath.isSingular()) {
                throw new IllegalArgumentException("TagPath must be singular");
            }
            List<Integer> tagArray = new ArrayList<>();
            DicomObject item = dobj;
            boolean create = false;

            for (Tag t : tagPath.getTags()) {
                if (t instanceof TagSequence) {
                    TagSequence ts = (TagSequence) t;
                    int itemNumber = ts.getItemNumberAsInt();
                    Tag tag = ts.getTag();
                    int theTag = t.asInt();

                    if (tag instanceof TagPrivate) {
                        TagPrivate tagPrivate = (TagPrivate) tag;
                        theTag = item.resolveTag(theTag, tagPrivate.getPvtCreatorID(), create);
                        if (theTag == -1) {
                            return Optional.empty();
                        }
                    }
                    DicomElement de = item.get(theTag);
                    if (de == null) {
                        return Optional.empty();
                    }
                    tagArray.add(theTag);
                    if (hasItem(de, itemNumber)) {
                        tagArray.add(itemNumber);
                        item = de.getDicomObject(itemNumber);
                    } else {
                        return Optional.empty();
                    }
                } else if (t instanceof TagPublic) {
                    if (!item.contains(t.asInt())) {
                        return Optional.empty();
                    }
                    tagArray.add(t.asInt());
                } else if (t instanceof TagPrivateCreator) {
                    TagPrivateCreator tpc = (TagPrivateCreator) t;
                    int resolvedTag = item.resolveTag(t.asInt(), tpc.getPvtCreatorID(), create);
                    if (resolvedTag == -1) {
                        return Optional.empty();
                    }
                    tagArray.add(resolvedTag);
                } else if (t instanceof TagPrivate) {
                    TagPrivate tagPrivate = (TagPrivate) t;
                    int resolvedTag = item.resolveTag(t.asInt(), tagPrivate.getPvtCreatorID(), create);
                    if (resolvedTag == -1) {
                        return Optional.empty();
                    }
                    if (!item.contains(resolvedTag)) {
                        return Optional.empty();
                    }
                    tagArray.add(resolvedTag);
                } else {
                    logger.debug("Error resolving tag of unimplemented type: {}", t);
                }
            }
            return Optional.of(TagPath.copyListToArray(tagArray));
        }

        private boolean hasItem(DicomElement de, int itemNumber) {
            return itemNumber < de.countItems();
        }

        private int[] resolve(TagPath tagPath, boolean create) {
            List<Integer> tagArray = new ArrayList<>();
            DicomObject tmpdobj = dobj;

            for (Tag t : tagPath.getTags()) {
                if (t instanceof TagSequence) {
                    TagSequence ts = (TagSequence) t;
                    Tag tag = ts.getTag();
                    if (tag instanceof TagPrivate) {
                        TagPrivate tagPrivate = (TagPrivate) tag;
                        int resolvedTag = tmpdobj.resolveTag(tag.asInt(), tagPrivate.getPvtCreatorID(), create);
                        if (resolvedTag == -1) {
                            DicomElement de = tmpdobj.putSequence(tag.asInt());
                            tmpdobj.putString(tagPrivate.getPvtCreatorIDTag(), VR.LO, tagPrivate.getPvtCreatorID());
                            tmpdobj = de.getDicomObject();
                            resolvedTag = tagPrivate.getPvtCreatorIDTag();
                        }
                        tagArray.add(resolvedTag);
                        tagArray.add(ts.getItemNumberAsInt());
                    } else if (tag instanceof TagPublic) {
                        DicomElement de = tmpdobj.get(tag.asInt());
                        if (de == null) {
                            de = tmpdobj.putSequence(tag.asInt());
                            de.addDicomObject(new BasicDicomObject());
                        }
                        tmpdobj = de.getDicomObject();
                        tagArray.add(tag.asInt());
                        tagArray.add(ts.getItemNumberAsInt());
                    }
                } else if (t instanceof TagPublic) {
                    tagArray.add(t.asInt());
                } else if (t instanceof TagPrivateCreator) {
                    TagPrivateCreator tpc = (TagPrivateCreator) t;
                    int resolvedTag = tmpdobj.resolveTag(t.asInt(), tpc.getPvtCreatorID(), false);
                    tagArray.add(resolvedTag);
                } else if (t instanceof TagPrivate) {
                    TagPrivate tagPrivate = (TagPrivate) t;
                    int resolvedTag = tmpdobj.resolveTag(t.asInt(), tagPrivate.getPvtCreatorID(), true);
                    if (resolvedTag == -1) {
                        tmpdobj.putString(tagPrivate.getPvtCreatorIDTag(), VR.LO, tagPrivate.getPvtCreatorID());
                        resolvedTag = tagPrivate.getPvtCreatorIDTag();
                    }
                    tagArray.add(resolvedTag);
                } else {
                    logger.debug("Error resolving tag of unimplemented type: {}", t);
                }
            }
            return TagPath.copyListToArray(tagArray);
        }

        /**
         * {@inheritDoc}
         * TODO this is dead code. The implementation always returns null.
         */
        @Override
        public DicomObjectI getDicomObject(Tag tag) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<DicomObjectI> getItem(TagPath tagPath) {
            if (tagPath.isSingular() && tagPath.isSequence()) {
                DicomObject nestedDicomObject = dobj.getNestedDicomObject(tagPath.getTagsAsArray());
                return (nestedDicomObject != null) ? Optional.of(new MizerDicomObject(nestedDicomObject)) : Optional.empty();
            }
            return Optional.empty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DicomObjectI getItem(int[] tags) {
            try {
                DicomObject nestedDicomObject = dobj.getNestedDicomObject(tags);
                return (nestedDicomObject != null) ? new MizerDicomObject(nestedDicomObject) : null;
            } catch (Exception e) {
                logger.error("Error finding item {}", tags);
                return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DicomElementI get(int tag) {
            return getElement(tag);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DicomElementI get(Tag tag) {
            return getElement(tag.asInt());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DicomElementI getElement(int tag) {
            DicomElement element = dobj.get(tag);
            return (element != null) ? new MizerDicomElement(element) : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int putCreatorIDString(int tag, String pvtCreatorID, String value) {
            int t = dobj.resolveTag(tag, pvtCreatorID, true);
            VR vr = vr(t);
            putString(t, vr, value);
            return t;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void putString(int tag, String value) {
            putString(tag, vr(tag), value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void putString(int tag, String vrString, String value) {
            VR vr = vr(vrString);
            if (VR.UN.equals(vr)) {
                vr = vr(tag);
            }
            putString(tag, vr, value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void putString(int[] tags, String value) {
            putString(tags, vr(tags), value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void putString(int[] tags, String vrString, String value) {
            VR vr = vr(vrString);
            if (VR.UN.equals(vr)) {
                vr = vr(tags);
            }
            putString(tags, vr, value);
        }

        /**
         * {@inheritDoc}
         * Warning: this implementation does not use VM > 1. Instead, it joins String array into a single string with
         * substrings joined with backslash and puts the single string.
         */
        @Override
        public void putStrings(int[] tags, String[] s) {
            putString(tags, joinMultipleValues(s));
        }

        /**
         * {@inheritDoc}
         * Warning: this implementation does not use VM > 1. Instead, it joins String array into a single string with
         * substrings joined with backslash and puts the single string.
         */
        @Override
        public void putStrings(int[] tags, String vr, String[] s) {
            putString(tags, vr, joinMultipleValues(s));
        }

        /**
         * {@inheritDoc}
         * Warning: this implementation does not use VM > 1. Instead, it joins String array into a single string with
         * substrings joined with backslash and puts the single string.
         */
        @Override
        public void putStrings(int tag, String[] s) {
            putString(tag, vr(tag), joinMultipleValues(s));
        }

        /**
         * {@inheritDoc}
         * Warning: this implementation does not use VM > 1. Instead, it joins String array into a single string with
         * substrings joined with backslash and puts the single string.
         */
        @Override
        public void putStrings(int tag, String vrString, String[] s) {
            putString(tag, vrString, joinMultipleValues(s));
        }

        /**
         * putString to int tag for all VRs.
         * dcm4che2 does not have a putString for all VRs. Revert to putBytes where needed.
         *
         * @param tag the tag to be written to
         * @param vr  the VR encoding to use
         * @param s   the string to be written.
         */
        private void putString(int tag, VR vr, String s) {
            putString(new int[]{tag}, vr, s);
        }

        /**
         * putString to int[] tags for all VRs.
         * dcm4che2 does not have a putString for all VRs. Revert to putBytes where needed.
         *
         * @param tags the attribute's tag array
         * @param vr   the dcm4che2 VR
         * @param s    the attribute's value.
         */
        private void putString(int[] tags, VR vr, String s) {
            // VR is not an enum.
            switch (vr.toString()) {
                case "AE":
                case "AS":
                case "AT":
                case "CS":
                case "DA":
                case "DS":
                case "DT":
                case "FD":
                case "FL":
                case "IS":
                case "LO":
                case "LT":
                case "PN":
                case "SH":
                case "SL":
                case "SS":
                case "ST":
                case "TM":
                case "UL":
                case "US":
                case "UT":
                case "UI":
                case "UR":
                    dobj.putString(tags, vr, s);
                    break;
                case "OF":
                    // OF can be a stream of floats. There is not an established way to encode multiple floats in
                    // a string, so we assume the string contains a single float. Throws NumberFormatException if
                    // string is not a valid float.
                    dobj.putFloat(tags, vr, Float.parseFloat(s));
                    break;
                case "OB":
                case "OW":
                case "UN":
                    // These VRs do not have a dcmche putString implementation since encoding their values in a string is
                    // fraught. Try something basic here.
                    // OD, OL, OV, SV, UC, UV are unknown by dcm4che2
                    dobj.putBytes(tags, vr, s.getBytes(StandardCharsets.UTF_8));
                    break;
                case "SQ":
                    // Create but do not write a value to sequence tags.
                    if (tags != null && tags.length > 0) {
                        dobj.putSequence(tags[tags.length - 1]);
                    }
                    break;
                default:
                    String msg = String.format("Unexpected vr = %s. String value = %s", vr, s);
                    logger.error(msg);
                    throw new RuntimeException(msg);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removeTag(int tag) {
            dobj.remove(tag);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removePrivateTag(int tag, String pvtCreatorID) {
            dobj.remove(dobj.resolveTag(tag, pvtCreatorID));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(OutputStream os) throws MizerException {
            try (final DicomOutputStream out = new DicomOutputStream(os)) {
                String tsString = dobj.getString(0x00020010);
                if (tsString == null) {
                    dobj.putString(0x00020010, VR.UI, "1.2.840.10008.1.2.1"); // ExplicitVRLittleEndian
                }
                String sopClassUID = dobj.getString(0x00080016);
                dobj.putString(0x00020002, VR.UI, sopClassUID);
                String sopInstanceUID = dobj.getString(0x00080018);
                dobj.putString(0x00020003, VR.UI, sopInstanceUID);
                out.writeDicomFile(dobj);
            } catch (IOException e) {
                throw new MizerException(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void read(InputStream is) throws MizerException {
            try (final DicomInputStream dis = new DicomInputStream(is)) {
                dobj = dis.readDicomObject();
            } catch (IOException e) {
                throw new MizerException(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void read(File f) throws MizerException {
            try (final InputStream fin = getInputStream(f); final DicomInputStream dis = new DicomInputStream(fin)) {
                dobj = dis.readDicomObject();
            } catch (IOException e) {
                throw new MizerException(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(int tag) {
            return dobj.contains(tag);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(int[] tagArray) {
            if (isEven(tagArray.length)) {
                return dobj.getNestedDicomObject(tagArray) != null;
            } else {
                return dobj.get(tagArray) != null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(int tag, String pvtCreatorID) {
            return dobj.contains(dobj.resolveTag(tag, pvtCreatorID));
        }

        private boolean isEven(int i) {
            return (i | 1) > i;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(Tag tag) {
            if (tag instanceof TagPublic) {
                return dobj.contains(tag.asInt());
            } else if (tag instanceof TagPrivate) {
                TagPrivate tagPrivate = (TagPrivate) tag;
                int tagInt = dobj.resolveTag(tagPrivate.asInt(), tagPrivate.getPvtCreatorID(), false);
                return dobj.contains(tagInt);
            } else if (tag instanceof TagSequence) {
                TagSequence tagSequence = (TagSequence) tag;
                DicomElement dicomElement = dobj.get(tagSequence.getTag().asInt());
                if (dicomElement != null) {
                    return (dicomElement.getDicomObject(tagSequence.getItemNumberAsInt()) != null);
                }
                return false;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(TagPath tagPath) {
            return contains(tagPath.getTagsAsArray());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getPrivateCreator(int tag) {
            return dobj.getPrivateCreator(tag);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isSequenceElement(int tag) {
            DicomElement element = dobj.get(tag);
            return element != null && element.hasItems();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty() {
            return dobj.isEmpty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty(int tag) {
            DicomElement element = dobj.get(tag);
            if (element == null) {
                throw new IllegalArgumentException(String.format("tag is not present: 0x%08X", tag));
            }
            return element.isEmpty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty(int[] tagArray) {
            DicomElement element = dobj.get(tagArray);
            if (element == null) {
                throw new IllegalArgumentException(String.format("tag is not present: %s", asStringOfHex(tagArray)));
            }
            return element.isEmpty();
        }

        private String asStringOfHex(int[] tagArray) {
            return Arrays.stream(tagArray).boxed().map(Integer::toHexString).collect(Collectors.joining("/"));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return dobj.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void deleteAllPrivateTags() {
            deleteAllPrivateTags(dobj);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return dobj.toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toCompleteString() {
            StringBuffer sb = new StringBuffer();
            DicomObjectToStringParam dp = DicomObjectToStringParam.getDefaultParam();
            DicomObjectToStringParam params = new DicomObjectToStringParam(dp.name, dp.valueLength, dp.numItems, 128, Integer.MAX_VALUE, dp.indent, dp.lineSeparator);
            dobj.toStringBuffer(sb, params);
            return sb.toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dump(PrintStream ps) {
            DumpDicomTagVisitor visitor = new DumpDicomTagVisitor(this, ps);
            visitor.visit(this);
        }

        private InputStream getInputStream(final File f) throws IOException {
            final InputStream fin = new BufferedInputStream(new FileInputStream(f));
            return f.getName().endsWith("gz") ? new GZIPInputStream(fin) : fin;
        }

        /**
         * listPrivateBlock
         * Return a list of all tags in the specified private block.
         * Does not descend into sequences.
         *
         * @param dicomObject object under scrutiny.
         * @param tag         tag specifying private block. Can be any tag in the private block including private-creator-id tag. Returned list is empty if tag is not Private.
         * @return list of private tags in block, excluding private-creator-id tag. List is empty if there are no private tags in block or if specified tag is not a private tag.
         */
        private List<Integer> listPrivateBlock(DicomObject dicomObject, int tag) {
            List<Integer> privateTags = new ArrayList<>();
            int privateCreatorID;
            try {
                privateCreatorID = Tag.getPrivateCreatorIDTag(tag);
            } catch (IllegalArgumentException e) {
                String msg = "Tag is not private: " + Integer.toHexString(tag);
                logger.warn(msg, e);
                return privateTags;
            }
            int group = Tag.getGroup(privateCreatorID);
            int block = Tag.getPrivateCreatorBlock(privateCreatorID);

            for (Iterator<DicomElement> it = dicomObject.datasetIterator(); it.hasNext(); ) {
                DicomElement de = it.next();
                if (Tag.isPrivateDataTag(de.tag()) && !Tag.isPrivateCreatorDataTag(de.tag())) {
                    if (group == Tag.getGroup(de.tag()) && block == Tag.getPrivateBlock(de.tag())) {
                        privateTags.add(de.tag());
                    }
                }
            }
            return privateTags;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmptyPrivateBlock(int tag) {
            return Tag.isPrivateCreatorDataTag(tag) && listPrivateBlock(this.dobj, tag).isEmpty();
        }

        private void deleteAllPrivateTags(org.dcm4che2.data.DicomObject dicomObject) {

            for (Iterator<DicomElement> it = dicomObject.datasetIterator(); it.hasNext(); ) {
                DicomElement de = it.next();
                if (isPrivateDataElement(de.tag())) {
                    dicomObject.remove(de.tag());
                } else if (de.hasItems() && (de.tag() != 0x7FE00010)) {
                    for (int i = 0; i < de.countItems(); i++) {
                        deleteAllPrivateTags(de.getDicomObject(i));
                    }
                }

            }
        }

        private class DOIterator implements Iterator<DicomElementI> {

            Iterator<DicomElement> iterator = dobj.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public DicomElementI next() {
                return new MizerDicomElement(iterator.next());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

    }

    private static class MizerDicomElement implements DicomElementI {

        private final DicomElement element;

        public MizerDicomElement(final DicomElement element) {
            this.element = element;
        }

        @Override
        public int tag() {
            return element.tag();
        }

        public String tagString() {
            return Integer.toHexString(element.tag());
        }

        @Override
        public boolean hasItems() {
            // dcm4che2 element.hasItems() would be better named canHaveItems().
            // return element.hasItems();
            return element.countItems() > 0;
        }

        @Override
        public int countItems() {
            return element.countItems();
        }

        @Override
        public DicomObjectI getDicomObject(int i) {
            return new MizerDicomObject(element.getDicomObject(i));
        }

        @Override
        public void removeItem(int i) {
            element.removeDicomObject(i);
        }

        @Override
        public boolean isUID() {
            return element.vr() == VR.UI;
        }

        @Override
        public String getVRAsString() {
            return element.vr().toString();
        }

    }
}
