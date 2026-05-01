package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.tags.TagPath;

import java.util.List;

public class MatchingTagPathFilter implements TagPathFilter {
    List<TagPath> retainTagPathList;

    public MatchingTagPathFilter(List<TagPath> retainTagPathList) {
        this.retainTagPathList = retainTagPathList;
    }

    public boolean allow(TagPath tagPath) {
        return retainTagPathList.stream().anyMatch(rtp -> rtp.isMatch(tagPath));
    }

    public boolean deny(TagPath tagPath) {
        return retainTagPathList.stream().noneMatch(rtp -> rtp.isMatch(tagPath));
    }
}
