package org.nrg.xnat;

import org.nrg.dcm.NamedAttributes;
import org.nrg.dcm.id.CompositeDicomObjectIdentifier.ExtractorType;
import org.nrg.dcm.id.XnatDefaultDicomObjectIdentifier;
import org.nrg.dcm.xnat.AbstractConditionalAttrDef;
import org.nrg.dcm.xnat.ConditionalAttrDef;

import java.util.Arrays;
import java.util.List;

public enum LabelType {
    // Note to devs: see XnatDefaultDicomObjectIdentifier and Xnat15DicomProjectIdentifier for parallel rule logic
    PROJECT("project", ExtractorType.PROJECT, new AbstractConditionalAttrDef.Rule[] {
            new AbstractConditionalAttrDef.ContainsAssignmentRule(NamedAttributes.PatientComments, "Project"),
            new AbstractConditionalAttrDef.ContainsAssignmentRule(NamedAttributes.StudyComments, "Project"),
            new AbstractConditionalAttrDef.ContainsAssignmentRule(NamedAttributes.AdditionalPatientHistory, "Project"),
            new AbstractConditionalAttrDef.EqualsRule(NamedAttributes.StudyDescription),
            new AbstractConditionalAttrDef.EqualsRule(NamedAttributes.AccessionNumber)
    }),
    SUBJECT("subject_ID", ExtractorType.SUBJECT, new AbstractConditionalAttrDef.Rule[] {
            new AbstractConditionalAttrDef.ContainsAssignmentRule(NamedAttributes.PatientComments, "Subject"),
            new AbstractConditionalAttrDef.ContainsAssignmentRule(NamedAttributes.StudyComments, "Subject"),
            new AbstractConditionalAttrDef.ContainsAssignmentRule(NamedAttributes.AdditionalPatientHistory, "Subject"),
            new AbstractConditionalAttrDef.EqualsRule(NamedAttributes.PatientName)
    }),
    SESSION("label", ExtractorType.SESSION, new AbstractConditionalAttrDef.Rule[] {
            new AbstractConditionalAttrDef.ContainsAssignmentRule(NamedAttributes.PatientComments, "Session"),
            new AbstractConditionalAttrDef.ContainsAssignmentRule(NamedAttributes.StudyComments, "Session"),
            new AbstractConditionalAttrDef.ContainsAssignmentRule(NamedAttributes.AdditionalPatientHistory, "Session"),
            new AbstractConditionalAttrDef.EqualsRule(NamedAttributes.PatientID)
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
