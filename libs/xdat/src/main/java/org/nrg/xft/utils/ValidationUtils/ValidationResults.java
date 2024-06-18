/*
 * core: org.nrg.xft.utils.ValidationUtils.ValidationResults
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils.ValidationUtils;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.XFTFieldWrapper;
import org.nrg.xft.utils.XftStringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ValidationResults implements ValidationResultsI {
    public void addResults(final ValidationResults added) {
        errors.addAll(added.getErrors());
    }

    /**
     * Indicates whether the validated item is valid, i.e. validated without errors.
     *
     * @return Returns <b>true</b> if the item is valid, i.e. there are no errors.
     */
    @Override
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * Gets validation errors as a list of object arrays, where the array members are:
     *
     * <ul>
     *     <li>0: a {@link XFTFieldWrapper} instance for the field that failed validation</li>
     *     <li>1: a string containing a description or brief message describing the validation failure</li>
     *     <li>2: a string containing the XML path of the field that failed validation</li>
     *     <li>3: a string containing a detailed description of the failed validation</li>
     * </ul>
     *
     * @return Returns a list of the results
     *
     * @deprecated Call the {@link #getErrors()} method instead.
     */
    @Deprecated
    public List<Object[]> getResults() {
        return getErrors().stream().map(ValidationError::toArray).collect(Collectors.toList());
    }

    /**
     * Gets the list of validation errors.
     *
     * @return Returns a list of the errors from the validation operation.
     */
    public List<ValidationError> getErrors() {
        return errors;
    }

    /**
     * Sets the validation errors as a list of object arrays. See {@link #getResults()} for a description of the various
     * items in the object array.
     *
     * @param list The list of results to set.
     *
     * @deprecated Call the {@link #setErrors(List)} method instead.
     */
    @Deprecated
    public void setResults(final List<Object[]> list) {
        setErrors(list.stream().map(ValidationError::fromArray).collect(Collectors.toList()));
    }

    /**
     * Sets the list of validation errors for this result.
     *
     * @param incoming The list of errors to set.
     */
    public void setErrors(final List<ValidationError> incoming) {
        errors.clear();
        errors.addAll(incoming);
    }

    /**
     * Adds error message to collection of results.
     *
     * @param field        The field with the error.
     * @param briefMessage A short display message.
     * @param xmlPath      The XML path of the field.
     * @param element      The element with the error result.
     *
     * @deprecated Call the {@link #addError(XFTFieldWrapper, String, String, GenericWrapperElement)} instead.
     */
    @Deprecated
    public void addResult(final XFTFieldWrapper field, final String briefMessage, final String xmlPath, final GenericWrapperElement element) {
        addError(field, briefMessage, xmlPath, element);
    }

    /**
     * Adds error message to collection of results.
     *
     * @param field        The field with the error.
     * @param briefMessage A short display message.
     * @param xmlPath      The XML path of the field.
     * @param fullMessage  The full message.
     *
     * @deprecated Call the {@link #addError(XFTFieldWrapper, String, String, String)} instead.
     */
    @Deprecated
    public void addResult(final XFTFieldWrapper field, final String briefMessage, final String xmlPath, final String fullMessage) {
        addError(field, briefMessage, xmlPath, fullMessage);
    }

    /**
     * Adds error message to collection of results.
     *
     * @param field       The field with the error.
     * @param description A short display message.
     * @param xmlPath     The XML path of the field.
     * @param element     The element with the error result.
     */
    public void addError(final XFTFieldWrapper field, final String description, final String xmlPath, final GenericWrapperElement element) {
        final String message = element != null && field != null ? "The content of element '" + element.getFullXMLName() + "' is not complete. '{\"" + element.getSchemaTargetNamespaceURI() + "\":" + field.getXPATH() + "}' " + description : xmlPath + " " + description;
        addError(field, description, xmlPath, message);
    }

    /**
     * Adds error message to collection of results.
     *
     * @param field       The field with the error.
     * @param description A short display message.
     * @param xmlPath     The XML path of the field.
     * @param message     The full message.
     */
    public void addError(final XFTFieldWrapper field, final String description, final String xmlPath, final String message) {
        errors.add(ValidationError.builder().field(field).description(description).xmlPath(xmlPath).message(message).build());
    }

    /**
     * Adds error message to collection of results.
     *
     * @param error The validation error to be added.
     */
    public void addError(final ValidationError error) {
        errors.add(error);
    }

    /**
     * Removes validation results for the indicated field name.
     *
     * @param fieldName The name of the field to check for.
     *
     * @return The validation result that was removed.
     *
     * @deprecated Call the {@link #removeError(String)} method instead.
     */
    @Deprecated
    public Object[] removeResult(final String fieldName) {
        final ValidationError error = removeError(fieldName);
        return error != null ? error.toArray() : null;
    }

    /**
     * Removes validation results for the indicated field name.
     *
     * @param fieldName The name of the field to check for.
     *
     * @return The validation result that was removed.
     */
    public ValidationError removeError(final String fieldName) {
        final int index = getFieldIndex(fieldName);
        return index > -1 ? getErrors().remove(index) : null;
    }

    /**
     * Basic iterator for the results collection.
     *
     * @return Returns the Iterator for the results collection
     */
    public Iterator<Object[]> getResultsIterator() {
        return errors.stream().map(ValidationError::toArray).collect(Collectors.toList()).iterator();
    }

    /**
     * Indicates whether there is a message for this field (sql_name) in the results.
     *
     * @param fieldName The name of the field to check for.
     *
     * @return Returns true if there's a validation failure for the indicated field.
     */
    public boolean hasField(final String fieldName) {
        final int index = getFieldIndex(fieldName);
        return index > -1;
    }

    /**
     * If there is a message for this field (sql_name) in the results, its message
     * is returned.
     *
     * @param fieldName The name of the field to get.
     *
     * @return Returns the String message
     */
    public String getField(final String fieldName) {
        final int index = getFieldIndex(fieldName);
        return index > -1 ? getErrors().get(index).getDescription() : null;
    }

    /**
     * Outputs the results collection as an Unordered List with HTML Tags.
     *
     * @return Returns a String containing the unordered list HTML
     */
    public String toHTML() {
        return "<ul>\n" + errors.stream().map(error -> "    <li>" + error.getXmlPath() + " : " + StringUtils.replace(error.getDescription(), "'bad'", "") + "</li>").collect(Collectors.joining("\n")) + "\n</ul>\n";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "\nValidationResults\n\nIsValid:" + isValid() + "\n" + errors.stream().map(error -> error.getField() != null ? error.getXmlPath() + " : " + error.getDescription() : error.getDescription()).collect(Collectors.joining("\n")) + "\n";
    }

    public String toFullString() {
        return errors.stream().map(ValidationError::getMessage).collect(Collectors.joining("\n")) + "\n";
    }

    /**
     * Returns the {@link GenericWrapperField} object for the specified XML path.
     *
     * @param xmlPath The XML path for the field to be retrieved.
     *
     * @return The {@link GenericWrapperField} object for the specified XML path if it exists.
     *
     * @throws XFTInitException         When an error occurs initializing or accessing XFT.
     * @throws ElementNotFoundException When the specified element doesn't exist.
     * @throws FieldNotFoundException   When the specified field doesn't exist.
     * @deprecated Call the {@link GenericWrapperElement#GetFieldForXMLPath(String)} method instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static GenericWrapperField GetField(final String xmlPath) throws XFTInitException, FieldNotFoundException, ElementNotFoundException {
        return GenericWrapperElement.GetFieldForXMLPath(xmlPath);
    }

    private int getFieldIndex(final String fieldName) {
        final String standardized = XftStringUtils.StandardizeXMLPath(fieldName);
        for (int index = 0; index < errors.size(); index++) {
            final ValidationError error = errors.get(index);
            if ((error.getField() != null && StringUtils.equalsIgnoreCase(error.getField().getSQLName(), standardized)) || StringUtils.equalsAnyIgnoreCase(error.getXmlPath(), standardized, fieldName)) {
                return index;
            }
        }
        return -1;
    }

    private final List<ValidationError> errors = new ArrayList<>();
}
