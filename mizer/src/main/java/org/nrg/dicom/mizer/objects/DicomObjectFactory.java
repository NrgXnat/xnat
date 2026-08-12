package org.nrg.dicom.mizer.objects;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.TagUtils;
import org.dcm4che3.util.UIDUtils;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.tags.Tag;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.tags.TagPrivate;
import org.nrg.dicom.mizer.tags.TagPrivateCreator;
import org.nrg.dicom.mizer.tags.TagPublic;
import org.nrg.dicom.mizer.tags.TagSequence;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.visitors.AssignIfExistsDicomObjectVisitor;
import org.nrg.dicom.mizer.visitors.DeleteDicomObjectVisitor;
import org.nrg.dicom.mizer.visitors.DumpDicomTagVisitor;
import org.nrg.dicom.mizer.visitors.OrphanPvtCreatorIDExterminator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.zip.GZIPInputStream;

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


    public static DicomObjectI newInstance(final File file, boolean includeBulkData) throws MizerException {
        return new MizerDicomObject(file, includeBulkData);
    }

    /**
     * Create DicomObjectI representing the DICOM object in file, controlling how bulk data
     * (pixel data and friends) is handled.
     * <p>
     * Use {@link DicomInputStream.IncludeBulkData#URI URI} to keep bulk data on disk: values are
     * represented as {@link org.dcm4che3.data.BulkData} references into <b>file</b> rather than
     * being read onto the heap. This is the only mode that can load an object whose PixelData
     * value exceeds 2 GB, because {@code DicomInputStream.readValue()} reads into a {@code byte[]}
     * and throws "tag value too large" past {@link Integer#MAX_VALUE}.
     *
     * @param file            containing DICOM object.
     * @param includeBulkData how to handle bulk data elements.
     * @return {@link MizerDicomObject}.
     * @throws MizerException on error.
     */
    public static DicomObjectI newInstance(final File file, final DicomInputStream.IncludeBulkData includeBulkData) throws MizerException {
        return new MizerDicomObject(file, includeBulkData);
    }

    public static DicomObjectI newInstance(final File file, int stopTag) throws MizerException {
        return new MizerDicomObject(file, stopTag);
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

    public static DicomObjectI newInstance(final InputStream inputStream, int stopTag) throws MizerException {
        return new MizerDicomObject(inputStream, stopTag);
    }

    /**
     * Implementation of {@link DicomObjectI} as inner class.
     */
    public static class MizerDicomObject implements DicomObjectI {

        private Attributes dataset;
        private final DeleteDicomObjectVisitor deleteVisitor = new DeleteDicomObjectVisitor();

        /**
         * Files holding bulk data that this object's {@link org.dcm4che3.data.BulkData} values point
         * at, and that nothing else owns: dcm4che's spool files for gzipped sources, and the edited
         * pixel data written by pixel edit handlers. They must outlive every {@link #write} and be
         * deleted afterwards, which {@link #releaseScratchFiles()} does.
         */
        private final List<File> scratchFiles = new ArrayList<>();

        /**
         * Create an empty object.
         */
        public MizerDicomObject() {
            this.dataset = new Attributes();
        }

        public MizerDicomObject(Attributes dataset) {
            this.dataset = dataset;
        }


        /**
         * Create from the DICOM object in file.
         *
         * @param file DICOM object file.
         * @throws MizerException on error.
         */
        public MizerDicomObject(File file, boolean includeBulkData) throws MizerException {
            try (final InputStream fin = getInputStream(file)) {
                loadAttributes(fin, includeBulkData);
            } catch (IOException e) {
                throw new MizerException(e);
            }
        }

        public MizerDicomObject(File file) throws MizerException {
            this(file, true);
        }

        /**
         * Create from the DICOM object in file, controlling bulk data handling.
         * <p>
         * With {@link DicomInputStream.IncludeBulkData#URI URI}, the stream's URI is set to
         * <b>file</b> so that bulk data values become references into it, read on demand and never
         * copied. That is not possible for a gzipped source, where stream offsets bear no relation
         * to file offsets; dcm4che detects the {@code InflaterInputStream} and spools bulk data to
         * its own temporary files instead, which we register for cleanup.
         *
         * @param file            DICOM object file.
         * @param includeBulkData how to handle bulk data elements.
         * @throws MizerException on error.
         */
        public MizerDicomObject(File file, DicomInputStream.IncludeBulkData includeBulkData) throws MizerException {
            final boolean gzipped = file.getName().endsWith("gz");
            try (final InputStream fin = getInputStream(file);
                 final DicomInputStream dis = new DicomInputStream(fin)) {
                dis.setIncludeBulkData(includeBulkData);
                if (!gzipped) {
                    dis.setURI(file.toURI().toString());
                }
                final Attributes fmi = dis.readFileMetaInformation();
                dataset = dis.readDataset();
                if (fmi != null) {
                    dataset.addAll(fmi);
                }
                // Non-empty only for the gzipped case, where dcm4che had to spool.
                scratchFiles.addAll(dis.getBulkDataFiles());
            } catch (IOException e) {
                throw new MizerException(e);
            }
        }

        public MizerDicomObject(File file, int stopTag) throws MizerException {
            try (final InputStream fin = getInputStream(file)) {
                loadAttributes(fin, stopTag);
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
            this(inputStream, true);
        }

        public MizerDicomObject(final InputStream inputStream, boolean includeBulkData) throws MizerException {
            loadAttributes(inputStream, includeBulkData);
        }

        public MizerDicomObject(final InputStream inputStream, int stopTag) throws MizerException {
            loadAttributes(inputStream, stopTag);
        }

        private void loadAttributes(InputStream inputStream, int stopTag) throws MizerException {
            try (final DicomInputStream dis = new DicomInputStream(inputStream)) {
                dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
                Attributes fmi = dis.readFileMetaInformation();
                dataset = dis.readDataset(stopTag);
                if (fmi != null) {
                    dataset.addAll(fmi);
                }
            } catch (IOException e) {
                throw new MizerException(e);
            }
        }

        private void loadAttributes(InputStream inputStream, boolean includeBulkData) throws MizerException {
            try (final DicomInputStream dis = new DicomInputStream(inputStream)) {
                if (includeBulkData) {
                    dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.YES); // or NO / URI / DEFERRED
                } else {
                    dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO); // or NO / URI / DEFERRED
                }
                Attributes fmi = dis.readFileMetaInformation();
                dataset = dis.readDataset();
                if (fmi != null) {
                    dataset.addAll(fmi);
                }
            } catch (IOException e) {
                throw new MizerException(e);
            }
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
            if (!dataset.contains(tag)) {
                return null;
            }
            int[] tagArray = new int[]{tag};
            final String value = getString(tagArray);
            if (logger.isDebugEnabled()) {
                logger.debug("Got: {} = {}", TagPath.toString(tagArray), value);
            }
            return value == null ? "null" : value;
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
        private static final Set<VR> BYTES_VRS = new HashSet<>(Arrays.asList(VR.OB, VR.UN, VR.OW, VR.UC));
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
            if (tagArray.length == 0) {
                return new String[]{};
            }
            if (tagArray.length == 1) {
                int tag = tagArray[0];
                VR vr = dataset.getVR(tag);
                if (BYTES_VRS.contains(vr)) {
                    byte[] byteResult =getBytes(tag);
                    if (byteResult[byteResult.length-1]==0) {
                        byteResult = Arrays.copyOf(byteResult, byteResult.length-1);
                    }
                    return  new String[]{new String(byteResult,StandardCharsets.UTF_8)};
                }
                return dataset.getStrings(tagArray[0]);
            }
            Attributes current = Dcm4cheConvert.getNestedAttribute(dataset, tagArray);
            if (current == null) {
                return new String[]{};
            }
            VR vr = current.getVR(tagArray[tagArray.length - 1]);
            if (BYTES_VRS.contains(vr)) {
                return  new String[]{new String(Dcm4cheConvert.getNestedBytes(dataset, tagArray),StandardCharsets.UTF_8)};
            }
            return Dcm4cheConvert.getNestedStrings(dataset, tagArray);
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
            try {
                return dataset.getBytes(tag);
            } catch (IOException e) {
                return null;
            }
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
            dataset.setBytes(tag, vr, b);
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
            VR vr = dataset.getVR(tag);
            if (vr == null) {
                vr = ElementDictionary.getStandardElementDictionary().vrOf(tag);
                if (vr == null) {
                    vr = VR.UN; // Unknown
                }
            }
            return vr;
        }

        private VR vr(int[] tagPath) {
            Attributes current = dataset;
            for (int i = 0; i < tagPath.length - 1; i++) {
                current = current.getNestedDataset(tagPath[i]);
                if (current == null) {
                    break;
                }
            }

            VR vr = (current != null) ? current.getVR(tagPath[tagPath.length - 1]) : null;

            if (vr == null) {
                int lastTag = tagPath[tagPath.length - 1];
                vr = ElementDictionary.getStandardElementDictionary().vrOf(lastTag);
                if (vr == null) {
                    vr = VR.UN;
                }
            }

            return vr;
        }

        private VR vr(String vrString) {
            return switch (vrString) {
                case "AS" -> VR.AS;
                case "AT" -> VR.AT;
                case "CS" -> VR.CS;
                case "DA" -> VR.DA;
                case "DS" -> VR.DS;
                case "DT" -> VR.DT;
                case "FD" -> VR.FD;
                case "FL" -> VR.FL;
                case "IS" -> VR.IS;
                case "LO" -> VR.LO;
                case "LT" -> VR.LT;
                case "OB" -> VR.OB;
                case "OD" -> VR.OD;
                case "OL" -> VR.OL;
                case "OV" -> VR.OV;
                case "SV" -> VR.SV;
                case "UV" -> VR.UV;
                case "UC" -> VR.UC;
                case "OF" -> VR.OF;
                case "OW" -> VR.OW;
                case "PN" -> VR.PN;
                case "SH" -> VR.SH;
                case "SL" -> VR.SL;
                case "SQ" -> VR.SQ;
                case "SS" -> VR.SS;
                case "ST" -> VR.ST;
                case "TM" -> VR.TM;
                case "UI" -> VR.UI;
                case "UL" -> VR.UL;
                case "US" -> VR.US;
                case "UT" -> VR.UT;
                default -> VR.UN;
            };
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
            String ageString = dataset.getString(tag);
            if (StringUtils.isEmpty(ageString)) {
                return Optional.empty();
            }
            try {
                int length = Integer.parseInt(ageString.substring(0, 3));
                String units = ageString.substring(3, 4);
                return switch (units) {
                    case "Y" -> Optional.of(Period.ofYears(length));
                    case "M" -> Optional.of(Period.ofMonths(length));
                    case "W" -> Optional.of(Period.ofWeeks(length));
                    case "D" -> Optional.of(Period.ofDays(length));
                    default -> throw new IllegalArgumentException(String.format("Unknown age format in tag %d: '%s'", tag, ageString));
                };
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("Error parsing age in tag %d: '%s'", tag, ageString));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int resolvePrivateTag(int tag, String pvtCreator, boolean create) {
            return resolvePrivateTag(dataset, tag, pvtCreator, create);
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
            dataset.remove(tag);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void delete(int[] tags) {
            if (tags == null || tags.length < 1) return;

            if (tags.length == 1) {
                dataset.remove(tags[0]);
                return;
            }
            Dcm4cheConvert.removeNestedTag(dataset, tags);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void deleteAllTags() {
            dataset.clear();
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
         * <p>
         * This implementation only adds transfer syntax UID of Explicit VR Little Endian
         */
        @Override
        public void addMetaHeader() {
            dataset.setString(0x00020010, VR.UI, "1.2.840.10008.1.2.1");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<DicomElementI> iterator() {
            return new MizerDicomObject.DOIterator();
        }

        public static int resolvePrivateTag(Attributes attrs, int tag, String privateCreator, boolean create) {
            int group = TagUtils.groupNumber(tag);
            int elementInBlock = TagUtils.elementNumber(tag) & 0x00FF;
            if (!TagUtils.isPrivateGroup(tag)) {
                throw new IllegalArgumentException("Not a valid private tag: " + TagUtils.toString(tag));
            }
            // 1. Find existed Private Creator ID
            for (int creatorElementOffset = 0x0010; creatorElementOffset <= 0x00FF; creatorElementOffset++) {
                int creatorTag = (group << 16) | creatorElementOffset;
                if (attrs.contains(creatorTag)) {
                    String existingCreator = attrs.getString(creatorTag);
                    if (privateCreator.equals(existingCreator)) {
                        // Found
                        // XX (10-FF)，is creatorElementOffset low byte
                        int blockIdentifier = creatorElementOffset & 0x00FF;
                        return (group << 16) | (blockIdentifier << 8) | elementInBlock;
                    }
                }
            }

            // 2. If can't find and create is true，try to assign a new slot.
            if (create) {
                for (int creatorElementOffset = 0x0010; creatorElementOffset <= 0x00FF; creatorElementOffset++) {
                    int creatorTag = (group << 16) | creatorElementOffset;
                    if (!attrs.contains(creatorTag)) { // Find an unused slot
                        // assign it to pvtCreator
                        attrs.setString(creatorTag, VR.LO, privateCreator); // LO is the private creator ID VR

                        // XX (10-FF)，is creatorElementOffset's low byte
                        int blockIdentifier = creatorElementOffset & 0x00FF;
                        return (group << 16) | (blockIdentifier << 8) | elementInBlock;
                    }
                }
                // If no unused private creator's slot.
                System.err.println("Could not allocate new private creator ID slot in group " +
                        TagUtils.toHexString(group << 16) + " for creator: " + privateCreator);
                return -1;
            }
            return -1;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<int[]> resolveTagPath(TagPath tagPath) {
                if (!tagPath.isSingular()) {
                    throw new IllegalArgumentException("TagPath must be singular");
                }

                List<Integer> tagList = new ArrayList<>();
                Attributes current = this.dataset;

                for (Tag tagItem : tagPath.getTags()) {
                    int resolvedTag;

                    if (tagItem instanceof TagSequence) {
                        TagSequence ts = (TagSequence) tagItem;
                        Tag innerTag = ts.getTag();
                        int itemNumber = ts.getItemNumberAsInt();
                        if (TagUtils.isPrivateTag(innerTag.asInt())) {
                            TagPrivate tp = (TagPrivate) innerTag;
                            resolvedTag = resolvePrivateTag(current, tp.asInt(), tp.getPvtCreatorID(), false);
                        } else {
                            resolvedTag = innerTag.asInt();
                        }
                        Sequence seq = current.getSequence(resolvedTag);
                        if (seq == null || seq.size() <= itemNumber) {
                            return Optional.empty();
                        }
                        tagList.add(resolvedTag);
                        tagList.add(itemNumber);
                        current = seq.get(itemNumber); // drill down
                    } else if (tagItem instanceof TagPrivate) {
                        TagPrivate tp = (TagPrivate) tagItem;
                        resolvedTag = resolvePrivateTag(current, tagItem.asInt(), tp.getPvtCreatorID(), false);
                        if (!current.contains(resolvedTag)) {
                            return Optional.empty();
                        }
                        tagList.add(resolvedTag);
                    } else if (tagItem instanceof TagPrivateCreator) {
                        TagPrivateCreator tpc = (TagPrivateCreator) tagItem;
                        resolvedTag = resolvePrivateTag(current, tagItem.asInt(), tpc.getPvtCreatorID(), false);
                        if (resolvedTag == -1) {
                            return Optional.empty();
                        }
                        tagList.add(resolvedTag);
                    } else if (tagItem instanceof TagPublic) {
                        resolvedTag = tagItem.asInt();
                        if (!current.contains(resolvedTag)) {
                            return Optional.empty();
                        }
                        tagList.add(resolvedTag);
                    } else {
                        logger.debug("Unsupported tag type: {}", tagItem);
                        return Optional.empty();
                    }
                }

                return Optional.of(tagList.stream().mapToInt(Integer::intValue).toArray());
            }

        private int[] resolve(TagPath tagPath, boolean create) {
            List<Integer> tagArray = new ArrayList<>();
            Attributes currentAttrs = dataset;

            for (Tag tag : tagPath.getTags()) {
                if (tag instanceof TagSequence) {
                    TagSequence ts = (TagSequence) tag;
                    Tag nestedTag = ts.getTag();
                    int tagCode = nestedTag.asInt();
                    int itemIndex = ts.getItemNumberAsInt();
                    if (nestedTag instanceof TagPrivate) {
                        TagPrivate tagPrivate = (TagPrivate) nestedTag;
                        dataset.setString(tagPrivate.getPvtCreatorIDTag(), VR.LO, tagPrivate.getPvtCreatorID());
                    }
                    Sequence seq = currentAttrs.getSequence(tagCode);
                    if (seq == null) {
                        if (create) {
                            seq = currentAttrs.newSequence(tagCode, itemIndex + 1);
                        } else {
                            return null; // not found, and not allowed to create
                        }
                    }

                    // Expand the sequence if needed
                    while (seq.size() <= itemIndex) {
                        seq.add(new Attributes());
                    }

                    currentAttrs = seq.get(itemIndex);
                    tagArray.add(tagCode);
                    tagArray.add(itemIndex);
                } else if (tag instanceof TagPublic) {
                    int tagCode = tag.asInt();
                    tagArray.add(tagCode);
                } else if (tag instanceof TagPrivateCreator) {
                    TagPrivateCreator tpc = (TagPrivateCreator) tag;
                    String creator = tpc.getPvtCreatorID();
                    int baseTag = tag.asInt();
                    int privateTag = resolvePrivateTag(currentAttrs, baseTag, creator, false);
                    if (privateTag == -1) {
                        if (create) {
                            currentAttrs.setString(baseTag, VR.LO, creator);
                            privateTag = resolvePrivateTag(currentAttrs, baseTag, creator, true);
                        } else {
                            return null;
                        }
                    }
                    tagArray.add(privateTag);
                } else if (tag instanceof TagPrivate) {
                    TagPrivate tagPrivate = (TagPrivate) tag;
                    int baseTag = tag.asInt();
                    String creator = tagPrivate.getPvtCreatorID();
                    int resolvedTag = resolvePrivateTag(currentAttrs, baseTag, creator, true);
                    if (resolvedTag == -1) {
                        if (create) {
                            currentAttrs.setString(tagPrivate.getPvtCreatorIDTag(), VR.LO, creator);
                            resolvedTag = resolvePrivateTag(currentAttrs, baseTag, creator, true);
                        } else {
                            return null;
                        }
                    }
                    tagArray.add(resolvedTag);
                } else {
                    logger.warn("Unsupported Tag type: {}", tag.getClass());
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
                int[] tags = tagPath.getTagsAsArray();
                if (tags.length < 2) {
                    return Optional.empty();
                }
                int seqTag = tags[0];
                int itemIndex = tags[1];

                Sequence seq = dataset.getSequence(seqTag);
                if (seq != null && itemIndex >= 0 && itemIndex < seq.size()) {
                    Attributes itemAttrs = seq.get(itemIndex);
                    if (itemAttrs != null) {
                        return Optional.of(new MizerDicomObject(itemAttrs));
                    }
                }
            }
            return Optional.empty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DicomObjectI getItem(int[] tags) {
            Attributes nested = Dcm4cheConvert.getNestedAttribute(dataset, tags);
            if (nested != null) {
                return new MizerDicomObject(nested);
            } else {
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
            if (!dataset.contains(tag)) {
                return null;
            }
            return new MizerDicomElement(dataset, tag);
        }

        @Override
        public DicomElementI getElement(int[] tags) {
            Attributes nested = Dcm4cheConvert.getNestedAttribute(dataset, tags);
            return new MizerDicomElement(nested, tags[tags.length-1]);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int putCreatorIDString(int tag, String pvtCreatorID, String value) {
            int t = resolvePrivateTag(tag, pvtCreatorID, true);
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
         * putString to int tag for all VRs. dcm4che does not have a putString for all VRs. Revert to putBytes where needed.
         *
         * @param tag the tag to be written to
         * @param vr  the VR encoding to use
         * @param s   the string to be written.
         */
        public void putString(int tag, VR vr, String s) {
            putString(new int[]{tag}, vr, s);
        }

        /**
         * putString to int[] tags for all VRs. dcm4che does not have a putString for all VRs. Revert to putBytes where needed.
         *
         * @param tags the attribute's tag array
         * @param vr   the VR
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

                case "IS":
                case "LO":
                case "LT":
                case "PN":
                case "SH":
                case "ST":
                case "TM":
                case "UT":
                case "UI":
                case "UR":
                    setStrings(tags, vr, s);
                    break;

                case "OF":
                case "FL":
                case "OD":
                case "FD":
                case "OL":
                case "SL":
                case "UL":
                case "OS":
                case "SS":
                case "US":
                case "OV":
                case "SV":
                case "UV":
                    // Numeric VRs can have VM>1, but dcm4che doesn't handle setting \-separated
                    Dcm4cheConvert.setNestedString(dataset, tags, vr, s.split("\\\\"));
                    break;

                case "OB":
                case "OW":
                case "UC":
                case "UN":
                    // These VRs do not have a dcmche putString implementation since encoding their values in a string is
                    // fraught. Try something basic here.
                    putBytes(tags, vr, s.getBytes(StandardCharsets.UTF_8));
                    break;
                case "SQ":
                    // Create but do not write a value to sequence tags.
                    if (tags != null && tags.length > 0) {
                        dataset.newSequence(tags[tags.length - 1], 0);
                    }
                    break;
                default:
                    String msg = String.format("Unexpected vr = %s. String value = %s", vr, s);
                    logger.error(msg);
                    throw new RuntimeException(msg);
            }
        }

        public void putBytes(int[] tags, VR vr, byte[] value) {
            if (tags == null || tags.length == 0) return;
            if (tags.length == 1) {
                dataset.setBytes(tags[0], vr, value);
                return;
            }
            Dcm4cheConvert.setNestedBytes(dataset, tags, vr, value);
        }

        private void setStrings(int[] tags, VR vr, String s) {
            if (tags == null || tags.length == 0) return;
            if (tags.length == 1) {
                dataset.setString(tags[0], vr, s);
                return;
            }
            Dcm4cheConvert.setNestedString(dataset, tags, vr, s);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removeTag(int tag) {
            dataset.remove(tag);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removePrivateTag(int tag, String pvtCreatorID) {
            dataset.remove(resolvePrivateTag(tag, pvtCreatorID, false));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(OutputStream os) throws MizerException {
            try {
                String tsString = dataset.getString(org.dcm4che3.data.Tag.TransferSyntaxUID);
                if (tsString == null) {
                    tsString = "1.2.840.10008.1.2.1"; // Explicit VR Little Endian
                    dataset.setString(org.dcm4che3.data.Tag.TransferSyntaxUID, VR.UI, tsString);
                }
                try (DicomOutputStream out = new DicomOutputStream(os, UID.ExplicitVRLittleEndian)) {
                    String sopClassUID = dataset.getString(org.dcm4che3.data.Tag.SOPClassUID);
                    String sopInstanceUID = dataset.getString(org.dcm4che3.data.Tag.SOPInstanceUID);

                    if (sopClassUID == null) {
                        dataset.setString(org.dcm4che3.data.Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
                    }
                    if (sopInstanceUID == null) {
                        dataset.setString(org.dcm4che3.data.Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
                    }
                    Dcm4cheConvert.SplitAttributes split = Dcm4cheConvert.splitFmiAndDataset(dataset);
                    out.writeDataset(split.fmi, split.onlyDataset);
                    dataset.addAll(split.fmi);
                }
            }catch (IOException e) {
                throw new MizerException(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void read(InputStream is) throws MizerException {
            loadAttributes(is, true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void read(File f) throws MizerException {
            try (final InputStream fin = getInputStream(f); final DicomInputStream dis = new DicomInputStream(fin)) {
                dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.YES); // or NO / URI / DEFERRED
                Attributes fmi = dis.readFileMetaInformation();
                dataset = dis.readDataset();
                if (fmi != null) {
                    dataset.addAll(fmi);
                }
            } catch (IOException e) {
                throw new MizerException(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(int tag) {
            return dataset.contains(tag);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(int[] tagArray) {
            if (tagArray == null || tagArray.length == 0) {
                return false;
            }
            if (tagArray.length == 1) {
                return dataset.contains(tagArray[0]);
            }
            Attributes current =Dcm4cheConvert.getNestedAttribute(dataset, tagArray);
            if (current == null) {
                return false;
            }
            if (tagArray.length %2 ==0) {
                return true;
            }
            return current.contains(tagArray[tagArray.length - 1]);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(int tag, String pvtCreatorID) {
            return dataset.contains(resolvePrivateTag(tag, pvtCreatorID, true));
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
                return dataset.contains(tag.asInt());
            } else if (tag instanceof TagPrivate) {
                TagPrivate tagPrivate = (TagPrivate) tag;
                int tagInt = resolvePrivateTag(tagPrivate.asInt(), tagPrivate.getPvtCreatorID(), false);
                return dataset.contains(tagInt);
            } else if (tag instanceof TagSequence) {
                TagSequence tagSequence = (TagSequence) tag;
                List<Attributes> seq = dataset.getSequence(tagSequence.getTag().asInt());
                if (seq != null) {
                    int index = tagSequence.getItemNumberAsInt();
                    if (index >= 0 && index < seq.size()) {
                        return seq.get(index) != null;
                    }
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
            return dataset.getPrivateCreator(tag);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isSequenceElement(int tag) {
            if (!dataset.contains(tag)) {
                return false;
            }
            VR vr = dataset.getVR(tag);
            if (!VR.SQ.equals(vr)) {
                return false;
            }
            List<Attributes> seq = dataset.getSequence(tag);
            return seq != null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty() {
            return dataset.isEmpty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty(int tag) {
            if (!dataset.contains(tag)) {
                throw new IllegalArgumentException(String.format("tag is not present: 0x%08X", tag));
            }
            return !dataset.containsValue(tag);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty(int[] tagArray) {
            if (tagArray == null || tagArray.length == 0) {
                throw new IllegalArgumentException(String.format("tag is not present: 0x%08X", tagArray));
            }
            if (tagArray.length == 1) {
                return isEmpty(tagArray[0]);
            }
            Attributes attr = Dcm4cheConvert.getNestedAttribute(dataset, tagArray);
            if (attr == null) {
                throw new IllegalArgumentException(String.format("tag is not present: 0x%08X", tagArray));
            }
            return !attr.containsValue(tagArray[tagArray.length - 1]);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return dataset.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void deleteAllPrivateTags() {
            deleteAllPrivateTags(dataset);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return dataset.toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toCompleteString() {
            return dataset.toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dump(PrintStream ps) {
            DumpDicomTagVisitor visitor = new DumpDicomTagVisitor(this, ps);
            visitor.visit(this);
        }

        @Override
        public Attributes getAttributes() {
            return dataset;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void registerScratchFile(final File file) {
            scratchFiles.add(file);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void releaseScratchFiles() {
            for (final File file : scratchFiles) {
                if (file.exists() && !file.delete()) {
                    logger.warn("Unable to delete bulk data scratch file {}", file);
                }
            }
            scratchFiles.clear();
        }

        @Override
        public SpecificCharacterSet getSpecificCharacterSet() {
            return this.dataset.getSpecificCharacterSet();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            MizerDicomObject that = (MizerDicomObject) o;
            return Objects.equals(dataset, that.dataset);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(dataset);
        }

        private InputStream getInputStream(final File f) throws IOException {
            final InputStream fin = new BufferedInputStream(new FileInputStream(f));
            return f.getName().endsWith("gz") ? new GZIPInputStream(fin) : fin;
        }

        private List<Integer> getPrivateTagsInBlock(Attributes attrs, int privateCreatorTag) {
            List<Integer> privateTags = new ArrayList<>();
            if (!TagUtils.isPrivateCreator(privateCreatorTag)) {
                return privateTags;
            }
            int group = TagUtils.groupNumber(privateCreatorTag);
            int block = TagUtils.elementNumber(privateCreatorTag);

            for (int tag : attrs.tags()) {
                if(TagUtils.isPrivateTag(tag)) {
                    if (TagUtils.groupNumber(tag) == group && (TagUtils.elementNumber(TagUtils.creatorTagOf(tag)) == block)) {
                        privateTags.add(tag);
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
            return Tag.isPrivateCreatorDataTag(tag) && getPrivateTagsInBlock(this.dataset, tag).isEmpty();
        }

        private void deleteAllPrivateTags(Attributes attrs) {
            List<Integer> privateTags = new ArrayList<>();

            for (int tag : attrs.tags()) {
                if (TagUtils.isPrivateGroup(tag)) {
                    privateTags.add(tag);
                } else if (attrs.contains(tag) && attrs.getVR(tag).equals(org.dcm4che3.data.VR.SQ) && tag != org.dcm4che3.data.Tag.PixelData) {
                    Sequence seq = attrs.getSequence(tag);
                    if (seq != null) {
                        for (Attributes item : seq) {
                            deleteAllPrivateTags(item);
                        }
                    }
                }
            }
            for (int tag : privateTags) {
                attrs.remove(tag);
            }
        }

        private class DOIterator implements Iterator<DicomElementI> {
            private final int[] allTags = dataset.tags();
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < allTags.length;
            }

            @Override
            public DicomElementI next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                int tag = allTags[index++];
                return new MizerDicomElement(dataset, tag);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

    }

    private static class MizerDicomElement extends AbstractDicomElement implements DicomElementI {

        public MizerDicomElement(Attributes attrs, int tag) {
            super(attrs, tag);
        }
    }
}
