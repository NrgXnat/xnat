/*
 * DicomEdit: DumpVisitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.objects.DicomObjectVisitor;
import org.nrg.dicom.mizer.tags.TagPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * PrivateBLockTerminator visits every tag in a DICOM object and deletes private tags
 * specified by the list of private-creator-id tags. This class does not traverse sequences.
 * Private Blocks are specified by an integer tag, independent of the label associated with the block. This class
 * was implemented to handle broken DICOM in which private elements do not follow the rules, for example, if the
 * private-creator-id tag is missing or two blocks in the same group have the same private-creator id.
 *
 * See DupCreatorVisitor for a usage example.
 */
public class PrivateBlockTerminator extends DicomObjectVisitor {
    private List<TagPath> creatorIDsToDelete;
    private static final Logger logger = LoggerFactory.getLogger( PrivateBlockTerminator.class);

    public PrivateBlockTerminator() {
        this.creatorIDsToDelete = new ArrayList<>();
    }

    /**
     * deletePrivateGroups
     * Traverse the DICOM object, deleting all tags that are contained in specified private blocks.
     *
     * @param creatorIDtags list of tagPaths to private-creator-id tags defining blocks to be deleted.
     * @param dicomObject the object to traverse.
     */
    public void deletePrivateGroups( List<TagPath> creatorIDtags, DicomObjectI dicomObject) {
        this.creatorIDsToDelete = creatorIDtags;
        creatorIDsToDelete.stream().forEach( i -> logger.trace("Delete private block {}", i) );
        visit( dicomObject);
    }

    /**
     * isTagToDelete
     *
     * @param tagPath is a tag in the DICOM object.
     * @return true if tagPath is contained in the list of private blocks to delete (inclusive of private creator id tag).
     */
    protected boolean isTagToDelete( TagPath tagPath) {
        return creatorIDsToDelete.stream()
                .anyMatch( isPrivateCreatorIDTag( tagPath).or( isInPrivateBlock( tagPath)) );
    }

    /**
     * isPrivateCreatorIDTag
     * Predicate function. Lambda parameter is a private-creator-id tagPath.
     *
     * @param tagPath is a tag in a DICOM file.
     * @return true if tagPath is the same tag as the lambda parameter.
     */
    private static Predicate<TagPath> isPrivateCreatorIDTag( TagPath tagPath) {
        return i -> tagPath.equals( i);
    }

    /**
     * isInPrivateBlock
     * Predicate function. Lambda parameter is a private-creator-id tagPath.
     *
     * @param tagPath is a tag in a DICOM file.
     * @return true if tagPath is contained in the private block defined by the lambda parameter.
     */
    private Predicate<TagPath> isInPrivateBlock( TagPath tagPath) {
        return i -> tagPath.isInPrivateBlock( i);
    }

    /**
     * visitTag. Overrides DicomObjectVisitor.visitTag
     * Delete the tag if it is part of a specified private block.
     *
     * @param tagPath is the tag in the DICOM file being processed.
     * @param dicomElement is the tagPath as a DicomElementI
     * @param dicomObject is the DICOM object containing tagPath.
     */
    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        logger.trace( "Visit {}", dicomElement.tagString());
        if( isTagToDelete( tagPath) ) {
            logger.trace( "Delete {}", dicomElement.tagString());
            dicomObject.delete( dicomElement.tag());
        }
    }

}
