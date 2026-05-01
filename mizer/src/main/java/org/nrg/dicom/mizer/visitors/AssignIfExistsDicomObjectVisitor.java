package org.nrg.dicom.mizer.visitors;

import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.objects.DicomObjectVisitor;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assign tags matching tagPathToAssign
 */
public class AssignIfExistsDicomObjectVisitor extends DicomObjectVisitor {
    private final TagPath tagPathToAssign;
    private final Value value;
    private static final Logger logger = LoggerFactory.getLogger(DicomObjectFactory.class);

    public AssignIfExistsDicomObjectVisitor( TagPath tagPathToAssign, Value value) {
        this.tagPathToAssign = tagPathToAssign;
        this.value = value;
    }

    @Override
    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        logger.debug("Testing {} against {} with regex {}", tagPath, tagPathToAssign, tagPathToAssign.getRegex());
        if( tagPathToAssign.isMatch(tagPath)) {
            logger.debug("Assign to existing tagPath = {}", tagPath);
            dicomObject.assign( dicomElement.tag(), value);
        }
    }

}
