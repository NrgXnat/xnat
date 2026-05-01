package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.Tag;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.tags.TagPrivate;
import org.nrg.dicom.mizer.tags.TagSequence;

import java.util.Optional;

public class MissingPrivateCreatorIDPrognosticator implements DicomTagPrognosticator {

    /**
     * prognosticate
     * Inspect the tagPath. Search up the tagPath for the first private tag with a creator id. Assume this "enclosing"
     * private sequence's creator ID for the "enclosed" private tags.
     *
     * This recursively moves up the tagPath and returns optional null if an enclosing private tag is not found.
     *
     * @param path the tagPath
     * @param dobj the DICOM object under scrutiny (ignored in this implementation).
     * @return optional String value of creator id.
     */
    @Override
    public Optional<String> prognosticate( TagPath path, DicomObjectI dobj) {
        if( ! path.isEmpty()) {
            Tag tag = path.getLastTag();
            if (tag.isPrivate()) {
                if (TagSequence.class.isInstance(tag)) {
                    tag = ((TagSequence) tag).getTag();
                }
                TagPrivate tp = (TagPrivate) tag;
                String id = tp.getPvtCreatorID();
                if (id == null || "NULL".equals(id)) {
                    return prognosticate(path.getParentTagPath(), dobj);
                } else {
                    return Optional.of(id);
                }
            }
        }
        return Optional.empty();
    }
}
