/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.RetainPrivateTagsReplaceFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.dicomedit.*;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationRuntimeException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.*;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Retain private tags matching the provided white-listed tags, remove all other private tags.
 * <p>
 * This implementation records the private-tag values that need to be retained, deletes all private tags,
 * then restores the retained values.
 */
public class RetainPrivateTagsReplaceFunction extends AbstractScriptFunction {

    private static final Logger logger = LoggerFactory.getLogger(RetainPrivateTagsReplaceFunction.class);

    public RetainPrivateTagsReplaceFunction() {
        super("retainPrivateTags", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    /**
     * The RetainPrivateTags script function.
     *
     * @param values      The list of arguments to the function. It is an error if any value does not resolve to a private tagPath.
     * @param dicomObject The DICOM object to be modified in place.
     * @return VOID.
     * @throws ScriptEvaluationException if any exception occurs.
     */
    @Override
    public Value apply(final List<Value> values, final DicomObjectI dicomObject) throws ScriptEvaluationException {

        try {
            // extract the list of private-tagPaths-to-retain from the function's arguments.
            List<TagPath> tagPathArguments = getTagPathArguments(values);

            // troll the DICOM object for all tags to retain.
            List<TagPath> retainTagPathList = getTagPathsToRetain(tagPathArguments, dicomObject);

            // troll the DICOM for the values of the retained tags.
            Map<TagPath, String> tagPathValueMap = new TagPathValueCollector().getMatching(retainTagPathList, dicomObject);

            // remove all private tags.
            deleteAllPrivateTags(dicomObject);

            // restore the retained values.
            restoreTags(tagPathValueMap, dicomObject);

        } catch (Exception e) {
            throw new ScriptEvaluationException("Error in retainPrivateTags: " + e.getMessage());
        }
        return AbstractMizerValue.VOID;
    }

    /**
     * Filter function arguments for private tagPaths.
     *
     * @param values List of Values from arguments.
     * @return List of private tagPaths.
     * @throws ScriptEvaluationRuntimeException if any argument is not a valid private tagPath.
     */
    private List<TagPath> getTagPathArguments(List<Value> values) {
        List<TagPath> tagPathsToRetain = getTagPaths(values);
        Optional<TagPath> tagPathNotPrivate = tagPathsToRetain.stream().filter(tp -> !tp.isPrivate()).findFirst();

        if (tagPathNotPrivate.isPresent()) {
            throw new ScriptEvaluationRuntimeException("TagPath argument in retainPrivateTags is not private: " + tagPathNotPrivate.get());
        }

        return tagPathsToRetain;
    }

    /**
     * getTagPathsToRetain
     *
     * @param tagPathArguments The list of tagPaths specifying the tags to retain.
     * @param dicomObject      the DICOM object under scrutiny.
     * @return list of TagPath specifying every tagPath that must be retained.
     */
    private List<TagPath> getTagPathsToRetain(List<TagPath> tagPathArguments, DicomObjectI dicomObject) {
        // Add the tagPath of the children each specified tagPath.
        // This enables arguments to implicitly specify sequences.
        List<TagPath> children = tagPathArguments.stream()
                .map(this::mapChildren)
                .flatMap(List::stream).collect(Collectors.toList());
        tagPathArguments.addAll(children);

        // troll the dicom for all tags matching the tagPath arguments.
        List<TagPath> retainTagPathList = new TagPathCollector().getMatching(tagPathArguments, dicomObject).stream()
                .filter(TagPath::isPrivate)
                .collect(Collectors.toList());

        // Include the necessary creator IDs
        Set<TagPath> pvcs = retainTagPathList.stream()
                .map(this::mapCreatorIDPaths)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        retainTagPathList.addAll(pvcs);

        return retainTagPathList;
    }

    /**
     * Map a tagPath to a list of tagPaths to all its creator IDs.
     *
     * @param tagPath
     * @return
     */
    public Set<TagPath> mapCreatorIDPaths(TagPath tagPath) {
        TagPath tp = new TagPath(tagPath);
        if (logger.isTraceEnabled()) {
            logger.trace("Map creator IDs {}", tagPath);
        }
        Set<TagPath> pcIdPaths = new HashSet<>();
        while (!tp.isEmpty()) {
            TagPath pcidPath = getPrivateCreatorIDPath(tp);
            if (pcidPath != null) {
                pcIdPaths.add(pcidPath);
            }
            tp = tp.getParentTagPath();
        }
        return pcIdPaths;
    }

    /**
     * getPrivateCreatorIDPath
     *
     * @param tagPath The tagPath under scrutiny.
     * @return the tagPath to the creator ID of the specified tagPath. Null if none found.
     */
    private TagPath getPrivateCreatorIDPath(TagPath tagPath) {
        Tag lastTag = tagPath.getLastTag();
        if (logger.isTraceEnabled()) {
            logger.trace("get creator ID {}", tagPath);
        }
        if (lastTag instanceof TagSequence) {
            lastTag = ((TagSequence) lastTag).getTag();
        }
        if (lastTag.isPrivate() && !lastTag.isPrivateCreatorID()) {
            TagPrivate t = (TagPrivate) lastTag;
            return new TagPath(tagPath.getParentTagPath())
                    .addTag(new TagPrivateCreator(t.getGroup(), t.getPvtCreatorID(), Integer.toHexString(t.getPrivateBlock())));
        }
        return null;
    }

    /**
     * Map a tagPath into a list of tagPath where the list contains the tagPath wildcard for all content.
     * <p>
     * This enables a tagPath argument to implicitly specify sequence tags.
     * For example, (2001,{pc}12) becomes (2001,{pc}12)/*
     * If the tag is not a sequence, there will be no tags for the wildcard to match, so no harm in adding it.
     * If the tag is a sequence, then the added tagPath will match the sequences content.
     *
     * @param tagPath TagPath to map.
     * @return a list of tagPath where the list contains the tagPath wildcard for all content.
     */
    public List<TagPath> mapChildren(TagPath tagPath) {
        List<TagPath> children = new ArrayList<>();
        Tag lastTag = tagPath.getLastTag();
        if (!(lastTag instanceof TagSequenceWildcard)) {
            TagPath childTagPath = new TagPath(tagPath).addTag(new TagSequenceWildcard("*"));
            children.add(childTagPath);
        }
        return children;
    }

    /**
     * deleteAllPrivateTags, including empty private items and blocks.
     *
     * @param dicomObject the object under scrutiny.
     */
    private void deleteAllPrivateTags(DicomObjectI dicomObject) {
        List<TagPath> pvtTagPaths = new TagPathCollector().getAll(dicomObject).stream()
                .filter(TagPath::isPrivate)
                .collect(Collectors.toList());

        pvtTagPaths.forEach(tagPath -> removeTagPath(tagPath, dicomObject));

        new DeleteEmptyPrivateItemsVisitor().visit(dicomObject);
        new DeleteEmptyPrivateBlocksVisitor().visit(dicomObject);
    }

    /**
     * Remove the tagPath from the Dicom object.
     * <p>
     * Private TagPaths do not need to be resolved against the Dicom object. TagPaths are created with the block they
     * have in the data. There is no need to look them up by creator ID and that can fail in some data.
     *
     * @param tagPath     tagPath to be removed.
     * @param dicomObject remove tagPath from this object.
     */
    private void removeTagPath(TagPath tagPath, DicomObjectI dicomObject) {
        if (dicomObject.contains(tagPath.getTagsAsArray())) {
            dicomObject.delete(tagPath.getTagsAsArray());
        } else {
            logger.warn("Can not resolve attribute for deletion: " + tagPath);
        }
    }

    /**
     * restoreTags
     *
     * @param tagPathValueMap Map of the tagPaths and values to be restored.
     * @param dicomObject     The object under scrutiny.
     */
    private void restoreTags(Map<TagPath, String> tagPathValueMap, DicomObjectI dicomObject) {
        tagPathValueMap.entrySet()
                .forEach(e -> dicomObject.putString(e.getKey().getTagsAsArray(), e.getKey().getLastTag().getVR(),e.getValue()));
    }

}