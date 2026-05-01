/*
 * DicomEdit: DumpVisitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.visitors;

import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.objects.PrivateBlock;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Locate all private tags that are missing a private creator ID.
 */
public class OrphanPvtTagLocator {
    private PvtBlockLocator pvtBlockLocator = new PvtBlockLocator();
    private PvtTagLocator pvtTagLocator = new PvtTagLocator();

    public List<List<Integer>> apply( DicomObjectI dicomObject) {
        return getOrphanPvtTags(dicomObject);
    }

    public List<List<Integer>> getOrphanPvtTags( DicomObjectI dicomObject) {
        List<PrivateBlock> privateBlocks = pvtBlockLocator.getPvtBlockList( dicomObject);
        // List<int[]> is more natural here but List<int[]>::contains does not compare int[] contents.
        List<List<Integer>> pvtTagArrays = pvtTagLocator.getPvtTagList( dicomObject, false);

         return pvtTagArrays.stream()
                .filter( ta -> hasNoPvtCreatorID( ta, privateBlocks))
                .collect( Collectors.toList());
    }

    public boolean hasNoPvtCreatorID( List<Integer> tags, List<PrivateBlock> privateBlocks) {
        return privateBlocks.stream().noneMatch( pb -> pb.isPvtCreatorForTag( tags));
    }

}
