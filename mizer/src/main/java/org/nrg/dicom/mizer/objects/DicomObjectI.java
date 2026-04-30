/*
 * DicomEdit: DicomObjectI
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.objects;

import org.dcm4che2.data.DicomObject;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.VR;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.tags.Tag;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.Value;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Period;
import java.util.Iterator;
import java.util.Optional;

/**
 * <p> Interface for manipulating DICOM Objects. </p>
 *
 * <p> These are the methods DicomEdit uses to manipulate DICOM Objects. It provides a facade behind which specific
 * versions of Dicom libraries can be hidden, e.g. dcm4che2. This separation facilitates the migration to other libs. </p>
 *
 * <p> {@link TagPath} is the primary object used to address tags within DICOM objects. {@link TagPath} can
 * contain wild-card characters and/or private-creator IDs so TagPaths don't necessarily address unique elements. It is up to
 * the DicomObject to resolve these references in the context of the DicomObject's instance. </p>
 *
 * <p> Some methods are overloaded with int or int[] tag parameters and are primarily provided for convenience in initializing
 * and validating unit tests. </p>
 *
 * <p> DicomEdit manipulates all tag values as Strings regardless of the tag's value representation (VR). </p>
 */
public interface DicomObjectI {

    /**
     * Return the value(s) at the specified tagPath as a String.
     * <p>
     * The values of tags with multiplicity &gt; 1 are concatenated and separated with the DICOM value-separation character \.
     *
     * @param tagPath The {@link TagPath tag path} to retrieve.
     * @return String representation of value(s) at specified tagPath. null if the tag does not exist, empty string if tag exists but is empty.
     * @throws NumberFormatException if tagPath contains wildcard chars and does not resolve to a unique tag.
     */
    String getString(TagPath tagPath);

    /**
     * Return the value(s) at the specified tag as a String.
     * <p>
     * The values of tags with multiplicity &gt; 1 are concatenated and separated with the DICOM value-separation character \.
     * The value of a private tag which is missing a creator id is not a problem.
     *
     * @param tag The tag to retrieve.
     * @return String representation of value(s) at specified tag. null if the tag does not exist, empty string if tag exists but is empty.
     * @throws UnsupportedOperationException if tag is a sequence, VR = SQ.
     */
    String getString(int tag);

    /**
     * Return the value(s) at the specified tag array as a String.
     * <p>
     * The values of tags with multiplicity &gt; 1 are concatenated and separated with the DICOM value-separation character \.
     *
     * @param tagArray An array of tags to evaluate.
     * @return String representation of value(s) at specified tag. null if the tag does not exist, empty string if tag exists but is empty.
     */
    String getString(int... tagArray);

    /**
     * Return the value(s) at the specified tagPath as a String array.
     * <p>
     * The size of the string array matches the multiplicity of the element\.
     *
     * @param tagPath The {@link TagPath tag path} to retrieve.
     * @return String-array representation of value(s) at specified tagPath. null if the tag does not exist, empty string array if tag exists but is empty.
     * @throws NumberFormatException if tagPath contains wildcard chars and does not resolve to a unique tag.
     */
    String[] getStrings(TagPath tagPath);

    /**
     * Return the value(s) at the specified tag as a String array.
     * <p>
     * The size of the string array matches the multiplicity of the element\.
     *
     * @param tag The tag to retrieve.
     * @return String-array representation of value(s) at specified tag. null if the tag does not exist, empty string array if tag exists but is empty.
     */
    String[] getStrings(int tag);

    /**
     * Return the value(s) at the specified tag array as a String array.
     * <p>
     * The size of the string array matches the multiplicity of the element\.
     *
     * @param tag The tag to retrieve specified as array.
     * @return String-array representation of value(s) at specified tag. null if the tag does not exist, empty string array if tag exists but is empty.
     */
    String[] getStrings(int... tag);

    /**
     * Return the value at the specified tag as an array of bytes.
     *
     * @param tag The tag to retrieve
     * @return value of tag as byte array.
     */
    byte[] getBytes(int tag);

    /**
     * Set value of tag from byte array.
     *
     * @param tag tag under scrutiny.
     * @param vr  Value Representation to use
     * @param b   tag value as byte array.
     */
    void putBytes(int tag, String vr, byte[] b);

    /**
     * Get the VR as String of the specified tag.
     * Returns the existing VR if present, else the dictionary value if known, else UN.
     *
     * @param tag tag under scrutiny.
     * @return Value Representation of tag as String.
     */
    String getVR(int tag);

    /**
     * Get the VR as String of the tag specified by the tagPath.
     * Returns the existing VR if present, else the dictionary value if known, else UN.
     * It is an error if tagPath does not resolve to a single tag.
     *
     * @param tagPath tag under scrutiny as {@link TagPath}.
     * @return Value Representation of tag as String.
     * @throws IllegalArgumentException if tagPath does not resolve to a single tag.
     */
    String getVR(TagPath tagPath);

    /**
     * Convenience method to get tags of VR=AS (Age String) as {@link Period}
     *
     * @param tag tag under scrutiny.
     * @return {@link Period} representation of age string if tag is present and not empty.
     * @throws IllegalArgumentException if tag value is not a valid Age String.
     */
    Optional<Period> getAge(int tag);

    /**
     * Return the resolved private tag in the context of this dicom object, optionally create the tag if it does not exist.
     * <p>
     * Will create the associated private creator ID if the tag being created requires it.
     *
     * @param tag        tag to be resolved, in range of 0xggggnnee where nn is ignored.
     * @param pvtCreator value of private creator ID of the owner of this tag.
     * @param create     if true, create the pvtCreatorID with first available block if missing. Resolve, but do not create, the tag.
     * @return the resolved private tag with value 0xggggbbee where bb is the block value reserved by the private creator.
     * or -1 if there is no reserved block.
     */
    int resolvePrivateTag(int tag, String pvtCreator, boolean create);

    /**
     * resolve tagPath into integer tag array in the context of this DICOM object.
     *
     * @param tagPath the tagPath to resolve.
     * @return optional array of tags. Not present if tagPath is not present in the object.
     * @throws IllegalArgumentException if tagPath is not singular.
     */
    Optional<int[]> resolveTagPath(TagPath tagPath);

    /**
     * Set the tag at tagPath to the specified string.
     * TagPath must resolve to a unique tag.
     * The tag will be created if not present, else updated.
     * The VR used will be the existing VR, else the dictionary value, else UN.
     *
     * @param tagPath tag as {@link TagPath}
     * @param value   value to set as String
     * @throws IllegalArgumentException if tagPath does not resolve to a unique tag.
     */
    void assign(TagPath tagPath, String value);

    /**
     * Set the existing tag at tagPath to the specified string.
     * The tag will not be created if not present.
     * TagPath must resolve to a unique tag.
     * The VR used will be the existing VR, else the dictionary value, else UN.
     *
     * @param tagPath        tag as {@link TagPath}
     * @param assignedString {@link Value} value to assign.
     * @throws IllegalArgumentException if tagPath does not resolve to a unique tag.
     */
    void assignIfExists(TagPath tagPath, Value assignedString);

    /**
     * Set the tag to the specified value.
     * The tag will be created if not present, else updated.
     * The VR used will be the existing VR, else the dictionary value, else UN.
     *
     * @param tag   tag to assign
     * @param value {@link Value} value
     */
    void assign(int tag, Value value);

    /**
     * Set the tag to the specified value.
     * The tag will be created if not present, else updated.
     * The VR used will be the existing VR, else the dictionary value, else UN.
     *
     * @param tagArray      tag as int[]
     * @param assignedValue as {@link Value}
     */
    void assign(int[] tagArray, Value assignedValue);

    /**
     * Delete all tags matching tagPath.
     *
     * @param tagPath {@link TagPath}
     */
    void delete(TagPath tagPath);

    /**
     * Delete the specific tag.
     *
     * @param tag to delete.
     */
    void delete(int tag);

    /**
     * Delete the specific tag addressed as int[]
     *
     * @param tagArray tag under scrutiny.
     */
    void delete(int[] tagArray);

    /**
     * Delete all the tags in this object.
     */
    void deleteAllTags();

    /**
     * Populate this object with the DICOM dataset at {@link InputStream}
     *
     * @param inputStream stream to DICOM object.
     * @throws MizerException on error
     */
    void read(InputStream inputStream) throws MizerException;

    /**
     * Populate this object with the DICOM dataset at {@link File}
     *
     * @param file containing DICOM object.
     * @throws MizerException on error
     */
    void read(File file) throws MizerException;

    /**
     * Write this object to {@link OutputStream}
     * <p>
     * Insure group 2 attributes match object's. (SOPClassUID and SOPInstanceUID)
     * Use ExplicitVRLittleEndian if transfer syntax is not already set.
     *
     * @param outputStream stream to output destination.
     * @throws MizerException on error.
     */
    void write(OutputStream outputStream) throws MizerException;

    /**
     * True if tag is present.
     *
     * @param tag tag under scrutiny.
     * @return true if tag is present.
     */
    boolean contains(int tag);

    /**
     * True if tag is present at tagArray.
     * TagArray's length may be odd (addressing a tag) or it may be even (addressing an item).
     *
     * @param tagArray tag under scrutiny.
     * @return true if tag is present.
     */
    boolean contains(int[] tagArray);

    /**
     * True if tag is present and is a member of the named block.
     * This will ignore the pvt block portion of the tag and will find the tag in whatever block is assigned to the
     * provided creator id.
     *
     * @param tag          tag under scrutiny.
     * @param pvtCreatorID expected private-creator id of tag.
     * @return true if tag with pvtCreatorID is present.
     */
    boolean contains(int tag, String pvtCreatorID);

    /**
     * True if tag is present at {@link Tag}.
     *
     * @param tag {@link Tag} under scrutiny.
     * @return true if tag is present.
     */
    boolean contains(Tag tag);

    /**
     * True if tagPath is present at {@link TagPath}.
     *
     * @param tagPath {@link TagPath} under scrutiny.
     * @return true if tag is present.
     */
    boolean contains(TagPath tagPath);

    /**
     * Return the private-creator id of the supplied tag.
     *
     * @param tag under scrutiny.
     * @return the private-creator id of the block regardless if the block is populated, null if the block does not have a creator id.
     * @throws IllegalArgumentException exception if tag is not private or if it is a private creator id element.
     */
    String getPrivateCreator(int tag);

    /**
     * Return true if tag is present and VR=SQ, or if tag is present and VR=UN but tag contains item(s).
     *
     * @param tag tag under scrutiny.
     * @return true if tag is a sequence element.
     */
    boolean isSequenceElement(int tag);

    /**
     * Add metadata header.
     */
    void addMetaHeader();

    /**
     * Return iterator.
     *
     * @return iterator over all {@link DicomElementI}
     */
    Iterator<DicomElementI> iterator();

    /**
     * True if this object does not have any tags.
     *
     * @return true if this object does not have any tags.
     */
    boolean isEmpty();

    /**
     * True if tag is empty (tag must be present).
     *
     * @param tag tag under scrutiny.
     * @return true if tag is present but empty.
     * @throws IllegalArgumentException if tag is not present.
     */
    boolean isEmpty(int tag);

    /**
     * True if tag is present but empty (tag must be present).
     *
     * @param tagArray tag at tagArray under scrutiny.
     * @return true if tag is present but empty.
     * @throws IllegalArgumentException if tag is not present.
     */
    boolean isEmpty(int[] tagArray);

    /**
     * Return the number of tags, not counting the contents of sequence tags.
     *
     * @return the number of tags, not counting the contents of sequence tags.
     */
    int size();

    /**
     * Huh? Nuke if possible.
     * Not clear if this is supposed to return object that contains this tag or, more likely,
     * an object that contains the contents of this tag. The tag must be a seq tag.
     * The existing implementation always returns null, so this is probably dead code.
     * TODO: Nuke if possible.
     *
     * @param tag
     * @return
     */
    DicomObjectI getDicomObject(Tag tag);

    /**
     * Get an optional {@link DicomObjectI} referencing the contents of tagPath.
     * Note the item object references the original objects tags so changes to the item are made to the original object.
     *
     * @param tagPath tag under scrutiny.
     * @return {@link DicomObjectI} with contents of tagPath.
     */
    Optional<DicomObjectI> getItem(TagPath tagPath);

    /**
     * Get a {@link DicomObjectI} referencing the contents of tagPath, null if it doesn't exist.
     * Note the item object references the original objects tags so changes to the item are made to the original object.
     *
     * @param tagArray tag under scrutiny.
     * @return {@link DicomObjectI} with contents of tagPath or null.
     */
    DicomObjectI getItem(int[] tagArray);

    /**
     * Get contents of tag as {@link DicomElementI} or null if not present.
     *
     * @param tag tag under scrutiny.
     * @return contents of tag as {@link DicomElementI} or null if not present.
     */
    DicomElementI get(int tag);

    /**
     * Get contents of tag as {@link DicomElementI} or null if not present.
     *
     * @param tag tag under scrutiny.
     * @return contents of tag as {@link DicomElementI} or null if not present.
     */
    DicomElementI get(Tag tag);

    /**
     * Get contents of tag as {@link DicomElementI} or null if not present.
     *
     * @param tag tag under scrutiny.
     * @return contents of tag as {@link DicomElementI} or null if not present.
     */
    DicomElementI getElement(int tag);

    DicomElementI getElement(int[] tagPath);

    /**
     * Put the string value to the specified tag.
     * The tag will be created if not present or updated. The VR will be that found in the dictionary, otherwise UN.
     * Tag values that are private or private-creator-ids are treated as regular tags.
     * A sequence tag will be created but the value is ignored.
     * String values can be anything so may be in violation of allowed characters in specific VRs.
     *
     * @param tag   tag to create or update.
     * @param value value to set.
     */
    void putString(int tag, String value);

    /**
     * Put the string value to the specified tag using the given VR.
     * The tag will be updated, or created if not present. The specified VR is used regardless of the tag's VR
     * in the dictionary or current VR in the object.
     *
     * @param tag      to create or update.
     * @param vr        the VR  to use.
     * @param value    value to set.
     */
    void putString(int tag, VR vr, String value);
    /**
     * Put the string value to the specified tag using the given VR.
     * The tag will be updated, or created if not present. The specified VR is used regardless of the tag's VR
     * in the dictionary or current VR in the object.
     *
     * @param tag      to create or update.
     * @param vrString the VR encoding to use.
     * @param value    value to set.
     */
    void putString(int tag, String vrString, String value);

    /**
     * Put the string value to the specified tag and item(s).
     * The tag will be updated, or created if not present. The VR will be that found in the dictionary, otherwise UN.
     * It is an error if tags does not specify a tag, i.e. tags.length must be odd.
     *
     * @param tagArray tag to create or update.
     * @param value    value to set.
     * @throws IllegalArgumentException if tags.length is even. (tags points to an item and not a tag within an item).
     */
    void putString(int[] tagArray, String value);

    /**
     * Put the string value to the specified tag and item(s).
     * The tag will be updated, or created if not present.
     * The specified VR is used regardless of the tag's VR in the dictionary or current VR in the object.
     * It is an error if tags does not specify a tag, i.e. tags.length must be odd.
     *
     * @param tagArray tag to create or update.
     * @param vrString the VR encoding to use.
     * @param value    value to set.
     * @throws IllegalArgumentException if tags.length is even. (tags points to an item and not a tag within an item).
     */
    void putString(int[] tagArray, String vrString, String value);

    /**
     * Put the string values to the specified tag for Value Multiplicity greater than 1.
     * The tag will be created if not present or updated. The VR will be that found in the dictionary, otherwise UN.
     * Tag values that are private or private-creator-ids are treated as regular tags.
     * A sequence tag will be created but the value is ignored.
     * String values can be anything so may be in violation of allowed characters in specific VRs.
     *
     * @param tag    tag to create or update.
     * @param values values to set.
     */
    void putStrings(int tag, String[] values);

    /**
     * Put the string values to the specified tag with VM > 1, using the given VR.
     * The tag will be updated, or created if not present. The specified VR is used regardless of the tag's VR
     * in the dictionary or current VR in the object.
     *
     * @param tag      to create or update.
     * @param vrString the VR encoding to use.
     * @param values   value to set.
     */
    void putStrings(int tag, String vrString, String[] values);

    /**
     * Put the string values to the specified tag with VM > 1 and item(s).
     * The tag will be updated, or created if not present. The VR will be that found in the dictionary, otherwise UN.
     * It is an error if tags does not specify a tag, i.e. tags.length must be odd.
     *
     * @param tagArray tag to create or update.
     * @param values   value to set.
     * @throws IllegalArgumentException if tags.length is even. (tags points to an item and not a tag within an item).
     */
    void putStrings(int[] tagArray, String[] values);

    /**
     * Put the string value to the specified tag with VM > 1 and item(s).
     * The tag will be updated, or created if not present.
     * The specified VR is used regardless of the tag's VR in the dictionary or current VR in the object.
     * It is an error if tags does not specify a tag, i.e. tags.length must be odd.
     *
     * @param tagArray tag to create or update.
     * @param vrString the VR encoding to use.
     * @param values   value to set.
     * @throws IllegalArgumentException if tags.length is even. (tags points to an item and not a tag within an item).
     */
    void putStrings(int[] tagArray, String vrString, String[] values);

    /**
     * Add the provided string value to the private tag. Use this if you want to let dcm4che manage the assignment
     * and resolution of private blocks.
     * <p>
     * For a tag of form 0xggggbbee, putString ignores bb and "does the right thing", creating the private creator
     * ID element if needed and then puts the tag in the right block.
     * <p>
     * Convenient for creating test DICOM objects.
     *
     * @param tag          The tag to write to.
     * @param pvtCreatorID The ID of the private tag creator.
     * @param value        The value to write to the tag.
     * @return The ID of the resolved tag.
     */
    int putCreatorIDString(int tag, String pvtCreatorID, String value);

    /**
     * Remove the tag, it is not an error to remove a tag that is not present.
     *
     * @param tag tag to be removed
     */
    void removeTag(int tag);

    /**
     * Remove tag with the specified private-creator id.
     * Does not remove private-creator-id tag if removal of this tag leaves the block empty.
     *
     * @param tag          tag to be removed.
     * @param pvtCreatorID private-creator id of tag's block.
     */
    void removePrivateTag(int tag, String pvtCreatorID);

    /**
     * Remove every private tag, including private-creator ids, and private tags without private-creator ids.
     */
    void deleteAllPrivateTags();

    /**
     * Remove empty private blocks.
     */
    void deleteEmptyPrivateBlocks();

    /**
     * True if tag is a private-data creator tag and no private-data tags for this block are present. The private-data-creator tag itself may be present.
     * Does not descend into sequences.
     *
     * @param tag tag under scrutiny, should be a private-creator-id tag.
     * @return true if no private-data tags are in this block.
     */
    boolean isEmptyPrivateBlock(int tag);

    /**
     * Get content as a single String.
     *
     * @return content as a String.
     */
    String toCompleteString();

    /**
     * Write a human-readable version to the {@link PrintStream}
     *
     * @param printStream
     */
    void dump(PrintStream printStream);

    /**
     * poke a hole for DicomEdit v4.
     *
     * @return Dcm4che2 instance of a Dicom Object.
     */
    DicomObject getDcm4che2Object();

    Attributes getAttributes();

    SpecificCharacterSet getSpecificCharacterSet();
}
