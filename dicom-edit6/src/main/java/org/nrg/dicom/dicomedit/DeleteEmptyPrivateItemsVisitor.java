package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.Tag;
import org.nrg.dicom.mizer.tags.TagPath;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeleteEmptyPrivateItemsVisitor extends DicomObjectTagVisitor {

    private DicomObjectI rootDicomObject;

    @Override
    public void visit(DicomObjectI dicomObject) {
        this.rootDicomObject = dicomObject;
        super.visit(dicomObject);
    }

    @Override
    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
    }

    @Override
    public void visitSequenceTag(Tag tag, TagPath tpContext, DicomElementI dicomElement, DicomObjectI dicomObject) {
        List<Integer> itemIndexToDelete = new ArrayList<>();
        for (int i = 0; i < dicomElement.countItems(); i++) {
            DicomObjectI item = dicomElement.getDicomObject(i);
            if (mustDeleteItem(item) && tag.isPrivate()) {
                itemIndexToDelete.add(i);
            } else {
                DeleteEmptyPrivateItemsVisitor visitor = new DeleteEmptyPrivateItemsVisitor();
                visitor.visit(item);
            }
        }
        for (int index : itemIndexToDelete) {
            dicomElement.removeItem(index);
        }
        if (mustDeleteTag(rootDicomObject) && tag.isPrivate()) {
            dicomObject.delete(tag.asInt());
        }
    }

    private boolean mustDeleteItem(DicomObjectI dicomObject) {
//            return dicomObject.isEmpty() || onlyPvtTagsArePvtCreators(dicomObject) || ! allTagsArePublic(dicomObject);
        return dicomObject.isEmpty() || onlyPvtTagsArePvtCreators(dicomObject);
    }

    private boolean mustDeleteTag(DicomObjectI dicomObject) {
        return dicomObject.isEmpty() || onlyPvtTagsArePvtCreators(dicomObject);
    }

    /**
     * the only private tags are creator tags. There may be public tags present.
     *
     * @param dicomObject
     * @return true if the only private tags are creator tags.
     */
    private boolean onlyPvtTagsArePvtCreators(DicomObjectI dicomObject) {
        List<TagPath> allTags = new TagPathCompleteCollector().apply(dicomObject);
        List<TagPath> pvtTags = allTags.stream()
                .filter(tp -> tp.isPrivate() || tp.isPrivateCreatorID()).collect(Collectors.toList());
        List<TagPath> pvtCreatorTags = pvtTags.stream()
                .filter(TagPath::isPrivateCreatorID).collect(Collectors.toList());
        return !pvtTags.isEmpty() && pvtTags.size() == pvtCreatorTags.size();
    }

    private boolean allTagsArePublic(DicomObjectI dicomObject) {
        List<TagPath> allTags = new TagPathCompleteCollector().apply(dicomObject);
        List<TagPath> pvtTags = allTags.stream()
                .filter(tp -> tp.isPrivate() || tp.isPrivateCreatorID()).collect(Collectors.toList());
        return !allTags.isEmpty() && pvtTags.isEmpty();
    }
}
