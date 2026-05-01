package org.nrg.xnat;

import org.nrg.dcm.Attributes;
import org.nrg.dcm.id.CompositeDicomObjectIdentifier.ExtractorType;
import org.nrg.dcm.id.XnatDefaultDicomObjectIdentifier;
import org.nrg.dcm.xnat.AbstractConditionalAttrDef;
import org.nrg.dcm.xnat.ConditionalAttrDef;

import java.util.Arrays;
import java.util.List;

public enum LabelType {
    // Note to devs: see XnatDefaultDicomObjectIdentifier and Xnat15DicomProjectIdentifier for parallel rule logic
    PROJECT("project", ExtractorType.PROJECT, new AbstractConditionalAttrDef.Rule[] {
            new AbstractConditionalAttrDef.ContainsAssignmentRule(Attributes.PatientComments, "Project"),
            new AbstractConditionalAttrDef.ContainsAssignmentRule(Attributes.StudyComments, "Project"),
            new AbstractConditionalAttrDef.ContainsAssignmentRule(Attributes.AdditionalPatientHistory, "Project"),
            new AbstractConditionalAttrDef.EqualsRule(Attributes.StudyDescription),
            new AbstractConditionalAttrDef.EqualsRule(Attributes.AccessionNumber)
    }),
    SUBJECT("subject_ID", ExtractorType.SUBJECT, new AbstractConditionalAttrDef.Rule[] {
            new AbstractConditionalAttrDef.ContainsAssignmentRule(Attributes.PatientComments, "Subject"),
            new AbstractConditionalAttrDef.ContainsAssignmentRule(Attributes.StudyComments, "Subject"),
            new AbstractConditionalAttrDef.ContainsAssignmentRule(Attributes.AdditionalPatientHistory, "Subject"),
            new AbstractConditionalAttrDef.EqualsRule(Attributes.PatientName)
    }),
    SESSION("label", ExtractorType.SESSION, new AbstractConditionalAttrDef.Rule[] {
            new AbstractConditionalAttrDef.ContainsAssignmentRule(Attributes.PatientComments, "Session"),
            new AbstractConditionalAttrDef.ContainsAssignmentRule(Attributes.StudyComments, "Session"),
            new AbstractConditionalAttrDef.ContainsAssignmentRule(Attributes.AdditionalPatientHistory, "Session"),
            new AbstractConditionalAttrDef.EqualsRule(Attributes.PatientID)
    });

    private final String label;
    private final ExtractorType et;
    private final AbstractConditionalAttrDef.Rule[] defaultRules;

    LabelType(String label, ExtractorType et, AbstractConditionalAttrDef.Rule[] defaultRules) {
        this.label = label;
        this.et = et;
        this.defaultRules = defaultRules;
    }

    private AbstractConditionalAttrDef.Rule[] getRules() {
        List<AbstractConditionalAttrDef.Rule> configRules = XnatDefaultDicomObjectIdentifier.getRulesFromConfig(et);
        if (configRules != null && !configRules.isEmpty()) {
            configRules.addAll(Arrays.asList(defaultRules));
            return configRules.toArray(new AbstractConditionalAttrDef.Rule[0]);
        } else {
            return defaultRules;
        }
    }

    public ConditionalAttrDef getAttrDef() {
        return new ConditionalAttrDef(label, getRules());
    }
}
