package org.nrg.xnat.actions.imageAssessorEdit.preSave;

import org.apache.commons.text.StringEscapeUtils;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.modules.actions.ModifyImageAssessorData;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;

import java.util.Map;

public class ImageAssessorDynamicVariablesPreSave implements ModifyImageAssessorData.PreSaveAction {

    public void execute(UserI user, XnatImageassessordata src, Map<String,String> params, PersistentWorkflowI wrk) throws Exception {
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
