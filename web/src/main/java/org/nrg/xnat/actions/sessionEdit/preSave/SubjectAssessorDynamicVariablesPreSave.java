package org.nrg.xnat.actions.sessionEdit.preSave;

import org.nrg.framework.utilities.Patterns;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xdat.om.XnatSubjectassessordata;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.turbine.modules.actions.ModifySubjectAssessorData;

import java.util.Map;

import org.nrg.xft.security.UserI;
import org.apache.commons.text.StringEscapeUtils;


public class SubjectAssessorDynamicVariablesPreSave implements ModifySubjectAssessorData.PreSaveAction {

    public void execute(UserI user, XnatSubjectassessordata src, Map<String,String> params, PersistentWorkflowI wrk) throws Exception {
        // @see org.nrg.xdat.turbine.utils.TurbineUtils - value has undergone StringEscapeUtils.escapeXml11(o)
        // As this string is a JSON, we will need to unescape it before it is saved
        if (null == params) {
            return;
        }
        final String CUSTOM_FIELD_NAME =  src.getXSIType().toLowerCase() + "/custom_fields";
        String escapedValueOfCustomFields = params.get(CUSTOM_FIELD_NAME);
        if (null != escapedValueOfCustomFields) {
            String unescapedValueOfCustomFields = StringEscapeUtils.unescapeXml(escapedValueOfCustomFields);
            //final DataFormatException exception = new DataFormatException();
            //exception.validateBlankAndRegex("custom_fields", unescapedValueOfCustomFields, CustomFormsConstants.LIMIT_JSON_XSS_CHARS);
            //if (exception.hasDataFormatErrors()) {
            //    throw exception;
           // }
            params.put(CUSTOM_FIELD_NAME, unescapedValueOfCustomFields);
            src.getItem().setProperty("custom_fields", unescapedValueOfCustomFields);
        }
    }

}
