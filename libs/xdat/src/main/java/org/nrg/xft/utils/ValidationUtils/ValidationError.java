package org.nrg.xft.utils.ValidationUtils;

import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.schema.design.XFTFieldWrapper;
import org.nrg.xft.utils.XftStringUtils;

@Value
@Builder
public class ValidationError {
    public static ValidationError fromArray(final Object[] array) {
        if (array.length < 4) {
            throw new RuntimeException("The result array must be of length 4");
        }
        final ValidationErrorBuilder builder = ValidationError.builder();
        if (array[0] != null) {
            builder.field((XFTFieldWrapper) array[0]);
        }
        if (array[1] != null) {
            builder.description(array[1].toString());
        }
        if (array[2] != null) {
            builder.xmlPath(array[2].toString());
        }
        if (array[3] != null) {
            builder.message(array[3].toString());
        }
        return builder.build();
    }

    public Object[] toArray() {
        return new Object[]{field, description, xmlPath, message};
    }

    /**
     * Tests whether the {@link #getField() field} {@link XFTFieldWrapper#getSQLName() name} for this error matches
     * the submitted field name <i>or</i> whether the {@link #getXmlPath() XML path} for this error matches the
     * submitted field name or the {@link XftStringUtils#StandardizeXMLPath(String) standardized version of the
     * submitted field name}.
     *
     * If you are matching multiple errors against a single field name, you should standardize the field name first then
     * call {@link #matches(String, String)} instead of this method to remove the cost of standardizing the field name
     * for each error in the result set.
     *
     * @param fieldName The field name to test against this error.
     *
     * @return Returns <b>true</b> if the field or XML path match, <b>false</b> otherwise.
     */
    public boolean matches(final String fieldName) {
        return matches(fieldName, XftStringUtils.StandardizeXMLPath(fieldName));
    }

    /**
     * Tests whether the {@link #getField() field} {@link XFTFieldWrapper#getSQLName() name} for this error matches
     * the submitted field name <i>or</i> whether the {@link #getXmlPath() XML path} for this error matches the
     * submitted or standardized field name.
     *
     * @param fieldName             The field name to test against this error.
     * @param standardizedFieldName The standardized field name to test against this error.
     *
     * @return Returns <b>true</b> if the field or XML path match, <b>false</b> otherwise.
     */
    public boolean matches(final String fieldName, final String standardizedFieldName) {
        return getField() != null && StringUtils.equalsIgnoreCase(getField().getSQLName(), standardizedFieldName) || StringUtils.equalsAnyIgnoreCase(getXmlPath(), standardizedFieldName, fieldName);
    }

    XFTFieldWrapper field;
    String          description;
    String          xmlPath;
    String          message;
}
