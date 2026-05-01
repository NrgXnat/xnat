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
import org.nrg.dicom.mizer.tags.Tag;
import org.nrg.dicom.mizer.tags.TagPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DupPrivateBlockTerminator visits every tag and finds private blocks that are different blocks in the same
 * group with same creator-id value. For example,
 * (0019,0010) "FujiFILM TM"
 * (0019,00E1) "FujiFILM TM"
 * Once identified, the higher-valued block(s) are deleted.
 *
 * Sequences are handled by recursively instantiating this class on sequence items.
 */
public class DupPrivateBlockTerminator extends DicomObjectVisitor {
    /**
     * Map of integer private-creator-id tags to the values in those tags.
     */
    private final Map<TagPath, String> creatorIDMap;
    private final PrivateBlockTerminator terminator;
    private final static Logger logger = LoggerFactory.getLogger( DupPrivateBlockTerminator.class);

    public DupPrivateBlockTerminator() {
        this.creatorIDMap = new HashMap<>();
        this.terminator = new PrivateBlockTerminator();
    }

    /**
     * Override this visitor to do the following:
     *
     * 1. visit all tags and identify the private blocks.
     * 2. identify "duplicate" private blocks (different blocks in the same group but with the same ID value).
     * 3. For each of the duplicate blocks, select the subset to be deleted. see getBlocksToDelete().
     * 4. delete the specified private blocks. Deletion is delegated to PrivateBlockTerminator.
     *
     * @param dicomObject
     */
    @Override
    public void visit( DicomObjectI dicomObject) {
        // collect all private creator ids in creatorIDMap
        creatorIDMap.clear();
        super.visit(dicomObject);
        // reorganize creatorIDMap by creator id. The same creator ID can be in different groups, and erroneously, in the same group.
        Map<String, List<TagPath>> repeatedCreatorIDs = getRepeatedCreatorIDs();
        // We only care about the same creator ID in the same group.
        Map<String, List<TagPath>> dupedGroups= getDupedGroups( repeatedCreatorIDs);
        for( String id: dupedGroups.keySet()) {
            List<TagPath> blocksToDelete = getBlocksToDelete( dupedGroups.get(id));
            blocksToDelete.forEach( t -> logger.trace( "Delete private block " + t));
            terminator.deletePrivateGroups( blocksToDelete, dicomObject);
        }
    }

    /**
     * getBlocksToDelete winnows down the list of private blocks to just the larger-valued tags.
     *
     * @param dupTags is the list of all private-creator ID tags in a group with duplicate ID values.
     * @return the winnowed list to those to be deleted
     */
    protected List<TagPath> getBlocksToDelete( List<TagPath> dupTags) {
        List<TagPath> groupsToDelete;
        if( dupTags.size() > 1) {
            List<TagPath> sorted = dupTags.stream().sorted().collect(Collectors.toList());
            groupsToDelete =  sorted.subList(1, sorted.size());
        }
        else {
            groupsToDelete = new ArrayList<>();
        }
        return groupsToDelete;
    }

    /**
     * getRepeatedCreatorIDs
     * This "flips" creatorIDMap so the Map is indexed by private creator id and each index points to
     * the list of tagPaths that define that creator id.
     *
     * @return
     */
    private Map<String, List<TagPath>> getRepeatedCreatorIDs() {
        Map<String,List<TagPath>> map = new HashMap<>();
        for( String value: creatorIDMap.values()) {
            List<TagPath> keys = getKeysWithValue( creatorIDMap, value);
            if( keys.size() > 1) {
                map.put( value, keys);
            }
        }
        return map;
    }

    private <K, V> Stream<K> keys(Map<K, V> map, V value) {
        return map
                .entrySet()
                .stream()
                .filter(entry -> value.equals(entry.getValue()))
                .map(Map.Entry::getKey);
    }

    private List<TagPath> getKeysWithValue( Map<TagPath, String> map, String value) {
        return keys( map, value).collect(Collectors.toList());
    }

    /**
     * getDupedGroups
     * Map the provided map which is indexed by creator id to one that is mapped by creator-id and group.
     * Then filter the new map down to only those entries where a creator-id--group index maps to multiple tags.
     * These are the troublemakers.
     *
     * @param creatorIDMap
     * @return
     */
    private Map<String, List<TagPath>> getDupedGroups( Map<String, List<TagPath>> creatorIDMap) {
        Map<String, List<TagPath>> map = new HashMap<>();
        creatorIDMap.keySet().forEach(
                id ->{ creatorIDMap.get(id).forEach(
                        tp -> {
                            String key = String.format("%s_0x%04X", id, Tag.getGroup( tp.getLastTag().asInt()));
                            if( map.containsKey( key)) {
                                map.get( key).add( tp);
                            } else {
                                List<TagPath> tpList = new ArrayList<>();
                                tpList.add( tp);
                                map.put( key, tpList);
                            }
                        }
                );}
        );
        return map.entrySet().stream().filter( e -> e.getValue().size() > 1).collect(Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Collect all private creator ids in creatorIDMap.
     *
     * @param tagPath
     * @param dicomElement
     * @param dicomObject
     */
    @Override
    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        logger.trace( "{}", dicomElement.tagString());
        if( tagPath.isPrivateCreatorID() ) {
            logger.trace( " is private creator ID.");
            creatorIDMap.put( tagPath, dicomObject.getString( dicomElement.tag()));
        }
    }

}
