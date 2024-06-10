package org.nrg.xnat.actions.preArchive;

import org.apache.commons.text.StringEscapeUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.PrearcSessionArchiver;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;

import java.util.Map;

public class XnatImageSessionDataDynamicVariablesPreArchive implements PrearcSessionArchiver.PreArchiveAction {

   public  Boolean execute(UserI user, XnatImagesessiondata src, Map<String, Object> params, XnatImagesessiondata existing) throws ServerException, ClientException {
        // @see org.nrg.xdat.turbine.utils.TurbineUtils - value has undergone StringEscapeUtils.escapeXml11(o)
        // As this string is a JSON, we will need to unescape it before it is saved
        if (null == params) {
            return false;
        }
        //No lowercase here as this is not a form submission
        final String CUSTOM_FIELD_NAME =  src.getXSIType() + "/custom_fields";
        String escapedValueOfCustomFields = (String)params.get(CUSTOM_FIELD_NAME);
        if (null != escapedValueOfCustomFields) {
            String unescapedValueOfCustomFields = StringEscapeUtils.unescapeXml(escapedValueOfCustomFields);
            final DataFormatException exception = new DataFormatException();
            exception.validateBlankAndRegex("custom_fields", unescapedValueOfCustomFields, CustomFormsConstants.LIMIT_JSON_XSS_CHARS);
            if (exception.hasDataFormatErrors()) {
                throw new ServerException(exception.getMessage(), exception);
            }
            params.put(CUSTOM_FIELD_NAME, unescapedValueOfCustomFields);
            try {
                src.getItem().setProperty("custom_fields", unescapedValueOfCustomFields);
            }catch(Exception e) {
                return false;
            }
        }
        return true;
    }
}
