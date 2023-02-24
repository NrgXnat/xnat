package org.nrg.xnat.actions.subjectEdit.preSave;

import org.nrg.framework.utilities.Patterns;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.turbine.modules.actions.EditSubjectAction;

import java.util.Map;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.security.UserI;
import org.apache.commons.text.StringEscapeUtils;


public class SubjectDynamicVariablesPreSave implements EditSubjectAction.PreSaveAction {

    public void execute(UserI user, XnatSubjectdata src, Map<String,String> params, EventMetaI event) throws Exception {
        // @see org.nrg.xdat.turbine.utils.TurbineUtils - value has undergone StringEscapeUtils.escapeXml11(o)
        // As this string is a JSON, we will need to unescape it before it is saved
        if (null == params) {
            return;
        }
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


    private final static String CUSTOM_FIELD_NAME =  "xnat:subjectdata/custom_fields";

}
