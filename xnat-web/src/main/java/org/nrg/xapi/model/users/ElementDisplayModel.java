package org.nrg.xapi.model.users;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.om.XdatElementSecurityListingAction;
import org.nrg.xdat.schema.SchemaElement;

import java.util.HashMap;
import java.util.Map;

import static lombok.AccessLevel.PUBLIC;

@Getter(PUBLIC)
@Accessors(prefix = "_")
@Slf4j
@JsonPropertyOrder({"elementName", "properName", "singular", "plural", "lbg", "dbg", "xsDescription", "allowBriefVersion", "allowDetailedVersion", "isExperiment", "isSubjectAssessor", "isImageAssessor", "isImageSession", "isImageScan", "actions"})
public class ElementDisplayModel {
    public ElementDisplayModel(final ElementDisplay elementDisplay) throws Exception {
        final SchemaElement schemaElement = elementDisplay.getSchemaElement();

        _elementName = elementDisplay.getElementName();
        _singular = schemaElement.getElementSecurity().getSingularDescription();
        _plural = schemaElement.getElementSecurity().getPluralDescription();
        _lbg = elementDisplay.getLightColor();
        _dbg = elementDisplay.getDarkColor();
        _xsDescription = schemaElement.getGenericXFTElement().getWrapped().getXsDescription();
        _properName = elementDisplay.getProperName();
        _supportsBrief = elementDisplay.getVersions().containsKey("brief");
        _supportsDetailed = elementDisplay.getVersions().containsKey("detailed");
        _isExperiment = schemaElement.instanceOf("xnat:experimentData");
        _isSubjectAssessor = schemaElement.instanceOf("xnat:subjectAssessorData");
        _isImageAssessor = schemaElement.instanceOf("xnat:imageAssessorData");
        _isImageSession = schemaElement.instanceOf("xnat:imageSessionData");
        _isImageScan = schemaElement.instanceOf("xnat:imageScanData");

        _actions = new HashMap<>();
        for (final XdatElementSecurityListingAction action : elementDisplay.getElementSecurity().getListingActions()) {
            _actions.put(action.getElementActionName(), action.getDisplayName());
        }
    }

    private final String              _elementName;
    private final String              _properName;
    private final String              _singular;
    private final String              _plural;
    private final String              _lbg;
    private final String              _dbg;
    private final String              _xsDescription;
    private final boolean             _supportsBrief;
    private final boolean             _supportsDetailed;
    private final boolean             _isExperiment;
    private final boolean             _isSubjectAssessor;
    private final boolean             _isImageAssessor;
    private final boolean             _isImageSession;
    private final boolean             _isImageScan;
    private final Map<String, String> _actions;
}
