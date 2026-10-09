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
import org.nrg.dicom.mizer.objects.DicomElementI;
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
 * This implementation deletes every private tag that is not retained. Retained tags stay in place: their values
 * are not read or rewritten.
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

            // remove all other private tags.
            deletePrivateTagsExcept(retainTagPathList, dicomObject);

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
     * Delete every private tag that is not retained.
     * <p>
     * The retained tags are the tags with values (every tag except sequences with items, and except Pixel Data) that
     * match a tagPath in retainTagPathList. A private sequence that holds a retained tag is kept. Its other content is
     * deleted, and so are the items at the end of it that are left empty. Empty items before a retained one are kept,
     * so retained tags keep their item numbers.
     *
     * @param retainTagPathList tagPaths to match the tags to retain, including their creator IDs.
     * @param dicomObject       the object under scrutiny.
     */
    private void deletePrivateTagsExcept(List<TagPath> retainTagPathList, DicomObjectI dicomObject) {
        MatchingTagPathFilter filter = new MatchingTagPathFilter(retainTagPathList);
        Set<List<Integer>> retained = new HashSet<>();
        Set<List<Integer>> sequencesToKeep = new LinkedHashSet<>();
        for (TagPath tagPath : new ValueTagPathCollector().getAll(dicomObject)) {
            if (filter.allow(tagPath)) {
                List<Integer> path = asList(tagPath.getTagsAsArray());
                retained.add(path);
                sequencesToKeep.addAll(getSequencePaths(path));
            }
        }

        Set<List<Integer>> deleted = new HashSet<>();
        for (TagPath tagPath : new TagPathCollector().getAll(dicomObject)) {
            List<Integer> path = asList(tagPath.getTagsAsArray());
            if (!tagPath.isPrivate() || retained.contains(path) || sequencesToKeep.contains(path)
                    || getSequencePaths(path).stream().anyMatch(deleted::contains)) {
                continue;
            }
            removeTagPath(tagPath, dicomObject);
            deleted.add(path);
        }

        sequencesToKeep.stream()
                .filter(RetainPrivateTagsReplaceFunction::isPrivatePath)
                .forEach(path -> removeTrailingEmptyItems(path, dicomObject));
    }

    /**
     * The paths of the sequences that contain the tag at the specified path, outermost first.
     *
     * @param path the tag array of a tagPath.
     * @return the paths of the containing sequences, empty for a tag in the root object.
     */
    private static List<List<Integer>> getSequencePaths(List<Integer> path) {
        List<List<Integer>> sequencePaths = new ArrayList<>();
        for (int length = 1; length < path.size(); length += 2) {
            sequencePaths.add(new ArrayList<>(path.subList(0, length)));
        }
        return sequencePaths;
    }

    /**
     * A path is private if any of its tags is private. Every other element of the path is an item number.
     */
    private static boolean isPrivatePath(List<Integer> path) {
        for (int i = 0; i < path.size(); i += 2) {
            if (((path.get(i) >>> 16) & 1) == 1) {
                return true;
            }
        }
        return false;
    }

    private static List<Integer> asList(int[] tags) {
        return Arrays.stream(tags).boxed().collect(Collectors.toList());
    }

    /**
     * Collects the tagPaths of the tags with values: every tag except sequences with items, and except Pixel Data.
     * Values are not read.
     */
    private static class ValueTagPathCollector extends DicomObjectTagVisitor {
        private static final int PIXEL_DATA = 0x7FE00010;
        private final List<TagPath> tagPaths = new ArrayList<>();

        List<TagPath> getAll(DicomObjectI dicomObject) {
            visit(dicomObject);
            return tagPaths;
        }

        @Override
        public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
            if (dicomElement.tag() != PIXEL_DATA) {
                tagPaths.add(tagPath);
            }
        }
    }

    private void removeTrailingEmptyItems(List<Integer> sequencePath, DicomObjectI dicomObject) {
        int[] tags = sequencePath.stream().mapToInt(Integer::intValue).toArray();
        if (!dicomObject.contains(tags)) {
            return;
        }
        DicomElementI sequence = dicomObject.getElement(tags);
        for (int i = sequence.countItems() - 1; i >= 0 && sequence.getDicomObject(i).isEmpty(); i--) {
            sequence.removeItem(i);
        }
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

}