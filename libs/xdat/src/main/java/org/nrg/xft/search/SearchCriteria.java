/*
 * core: org.nrg.xft.search.SearchCriteria
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.search;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.nrg.xdat.exceptions.InvalidSearchException;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.security.PermissionCriteriaI;
import org.nrg.xft.XFT;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xft.utils.XftStringUtils;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SearchCriteria implements SQLClause {
    private static final Supplier<InvalidSearchException> NO_VALUE_SET_EXCEPTION_SUPPLIER = () -> new InvalidSearchException("You must specify a value before getting the SQL for this search, but value is currently null");

    private String              elementName        = "";
    private String              xmlPath            = "";
    private String              field_name         = "";
    private Object              value              = "";
    private String              comparison_type    = "=";
    private GenericWrapperField field              = null;
    private String              cleanedType        = "";
    private boolean             overrideFormatting = false;

    /**
     * @return Returns the overrideFormatting.
     */
    public boolean overrideFormatting() {
        return overrideFormatting;
    }

    /**
     * @param overrideFormatting The overrideFormatting to set.
     */
    public void setOverrideFormatting(boolean overrideFormatting) {
        this.overrideFormatting = overrideFormatting;
    }

    public String getSQLClause() throws Exception {
        final boolean comparisonContainsLike = StringUtils.containsIgnoreCase(getComparison_type().trim(), "like");
        if(comparisonContainsLike){
            //Necessary to support PreparedStatement execution
            return String.join(""," (", "LOWER(",getField_name(),") ", getComparison_type(), " LOWER(", (overrideFormatting ? (String) getValue() : valueToDB()), ") ", ")");
        }else{
            return String.join(""," (" ,getField_name(), getComparison_type(), (overrideFormatting ? (String) getValue() : valueToDB()), ")");
        }
    }

    public String getSQLClause(QueryOrganizerI qo) throws Exception {
        if (qo == null) {
            return getSQLClause();
        }
        final String  translatedXMLPath      = qo.translateXMLPath(getXMLPath()).trim();
        final String  comparisonType         = getComparison_type().trim();
        final boolean comparisonContainsLike = StringUtils.containsIgnoreCase(comparisonType, "like");
        if (overrideFormatting) {
            final String value = Optional.ofNullable(getValue()).orElseThrow(NO_VALUE_SET_EXCEPTION_SUPPLIER).toString().trim();
            if (comparisonContainsLike) {
                return " (" + Stream.of(" LOWER(",translatedXMLPath,") ", comparisonType, " LOWER(",value,") ").map(String::trim).collect(Collectors.joining(" ")) + ")";
            }
            if (StringUtils.equalsIgnoreCase(comparisonType, "IS NULL") ||
                StringUtils.equalsIgnoreCase(comparisonType, "IS") && StringUtils.equalsIgnoreCase(value, "NULL")) {
                return StringUtils.equalsIgnoreCase("string", getCleanedType())
                       ? " (" + translatedXMLPath + " IS NULL OR " + translatedXMLPath + "='')"
                       : " (" + translatedXMLPath + " IS NULL) ";
            }
            if (StringUtils.equalsIgnoreCase(comparisonType, "IS NOT NULL") ||
                StringUtils.equalsIgnoreCase(comparisonType, "IS NOT") && StringUtils.equalsIgnoreCase(value, "NULL") ||
                StringUtils.equalsIgnoreCase(comparisonType, "IS") && StringUtils.equalsIgnoreCase(value, "NOT NULL")) {
                return StringUtils.equalsIgnoreCase("string", getCleanedType())
                       ? " NOT(" + translatedXMLPath + " IS NULL OR " + translatedXMLPath + "='')"
                       : " NOT(" + translatedXMLPath + " IS NULL) ";
            }
            return " (" + String.join(" ", translatedXMLPath, comparisonType, value) + ")";
        }
        final String value = StringUtils.trim(valueToDB());
        if (comparisonContainsLike) {
            final String comparisonValue = StringUtils.lowerCase("'%" + StringUtils.stripEnd(StringUtils.stripStart(StringUtils.replace(valueToDB(), "*", "%"), "'%"), "'%") + "%'");
            return " (LOWER(" + translatedXMLPath + ") " + comparisonType + " LOWER(" + comparisonValue + "))";
        }
        if (StringUtils.isBlank(value) && comparisonType.equals("=")) {
            return translatedXMLPath + " IS NULL OR " + translatedXMLPath + " " + comparisonType + "''";
        }
        if (StringUtils.equals(value, "*")) {
            return " (" + translatedXMLPath + " IS NOT NULL)";
        }
        return " (" + translatedXMLPath + " " + comparisonType.trim() + " " + value + ")";
    }

    public SearchCriteria() {
    }

    /**
     * Populates the field, field_name, cleanedType, and value properties.
     *
     * @param f The search field to set.
     * @param v The search value to set.
     */
    public SearchCriteria(GenericWrapperField f, Object v) throws Exception {
        field   = f;
        xmlPath = f.getXMLPathString(f.getParentElement().getFullXMLName());
        setValue(v);
        field_name  = f.getSQLName();
        cleanedType = f.getXMLType().getLocalType();
    }

    /**
     * cleaned data type (i.e. string, integer, etc.) This is used to define the format of the
     * value in the SQL WHERE clause (i.e. if it needs quotes).
     *
     * @return The cleaned type.
     */
    public String getCleanedType() {
        return cleanedType;
    }

    /**
     * =,&#60;,&#60;=,&#62;,&#62;=
     *
     * @return The comparison type.
     */
    public String getComparison_type() {
        return comparison_type;
    }

    /**
     * corresponding GenericWrapperField (can be null)
     *
     * @return The field.
     */
    public GenericWrapperField getField() {
        return field;
    }

    /**
     * exact sql name to use in the where clause.
     *
     * @return The field name.
     */
    public String getField_name() {
        return field_name;
    }

    /**
     * value to search for
     *
     * @return The value to search for.
     */
    public Object getValue() {
        return value;
    }

    /**
     * cleaned data type (i.e. string, integer, etc.) This is used to define the format of the
     * value in the SQL WHERE clause (i.e. if it needs quotes).
     *
     * @param string The cleaned type to set.
     */
    public void setCleanedType(String string) {
        cleanedType = string;
    }

    /**
     * =,&#60;,&#60;=,&#62;,&#62;=
     *
     * @param string The comparison type to set.
     */
    public void setComparison_type(String string) {
        comparison_type = string;
    }

    /**
     * corresponding GenericWrapperField (can be null)
     *
     * @param field The field to set.
     */
    public void setField(GenericWrapperField field) {
        this.field = field;
    }

    /**
     * exact sql name to use in the where clause.
     *
     * @param string The field name to set.
     */
    public void setField_name(String string) {
        field_name = string;
    }

    /**
     * value to search for.
     *
     * @param value The value to set for searching.
     *
     * @throws Exception When an error occurs.
     */
    public void setValue(final Object value) throws Exception {
        final String temp;
        if (value == null) {
            this.value = null;
        } else {
            if(value instanceof Date){
                //Timestamp type must be hardcoded for PrepraredStatement execution
                this.value = new Timestamp(((Date)value).getTime());
            }else{
                temp = value instanceof String ? (String) value : value.toString();
                hackCheck(temp);
                this.value = temp.contains("'") ? XftStringUtils.CleanForSQLValue(temp) : temp;
            }
        }
    }

    /**
     * Parse the value using the cleanedType to output the correct format for SQL/
     *
     * @return The SQL-formatted value.
     */
    public String valueToDB() {
        if (getValue() == null) {
            return null;
        }
        try {
            return field != null && StringUtils.equalsIgnoreCase(field.getWrapped().getRule().getBaseType(), "xs:anyURI")
                   ? DBAction.ValueParser(getValue(), "anyURI", true)
                   : DBAction.ValueParser(getValue(), getCleanedType(), true);
        } catch (InvalidValueException e) {
            log.error("An invalid value was specified", e);
            return null;
        }
    }

    /**
     * Uses an XMLName Dot-Syntax to specify what field within an element is to be searched on.
     * (i.e. MrSession.Experiment.ExptDate)
     *
     * @param field The field to set for searching.
     *
     * @throws XFTInitException         When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When a specified field isn't found on the object.
     */
    public void setFieldWXMLPath(String field) throws ElementNotFoundException, XFTInitException, FieldNotFoundException {
        xmlPath = XftStringUtils.StandardizeXMLPath(field);
        String                rootElement = XftStringUtils.GetRootElementName(field);
        GenericWrapperElement root        = GenericWrapperElement.GetElement(rootElement);
        setElementName(root.getFullXMLName());
        field = root.getFullXMLName() + xmlPath.substring(xmlPath.indexOf(XFT.PATH_SEPARATOR));
        GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(field);

        String temp = ViewManager.GetViewColumnName(root, field);

        setField_name(temp);
        assert f != null;
        if (f.isReference()) {
            GenericWrapperElement gwe = ((GenericWrapperElement) f.getReferenceElement());
            for (final GenericWrapperField gwf : gwe.getAllPrimaryKeys()) {
                setCleanedType(gwf.getXMLType().getLocalType());
                break;
            }
        } else {
            setCleanedType(f.getXMLType().getLocalType());
        }

    }

    public String getXMLPath() {
        if (xmlPath.equalsIgnoreCase("")) {
            xmlPath = getElementName() + XFT.PATH_SEPARATOR + getField_name();
        }
        return xmlPath;
    }

    public int numClauses() {
        return 1;
    }

    /**
     * @return Returns the elementName.
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * @param elementName The elementName to set.
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    @Override
    public String toString() {
        try {
            return getSQLClause();
        } catch (Exception e) {
            return "error";
        }
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof SearchCriteria)) {
            return false;
        }

        final SearchCriteria that = (SearchCriteria) other;

        return new EqualsBuilder()
                .append(overrideFormatting, that.overrideFormatting)
                .append(getElementName(), that.getElementName())
                .append(xmlPath, that.xmlPath)
                .append(getField_name(), that.getField_name())
                .append(getValue(), that.getValue())
                .append(getComparison_type(), that.getComparison_type())
                .append(getField(), that.getField())
                .append(getCleanedType(), that.getCleanedType())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getElementName())
                .append(xmlPath)
                .append(getField_name())
                .append(getValue())
                .append(getComparison_type())
                .append(getField())
                .append(getCleanedType())
                .append(overrideFormatting)
                .toHashCode();
    }

    @SuppressWarnings("rawtypes")
    public ArrayList getSchemaFields() {
        ArrayList al = new ArrayList();
        Object[]  o  = new Object[2];
        o[0] = getXMLPath();
        o[1] = getField();
        //noinspection unchecked
        al.add(o);
        return al;
    }

    @SuppressWarnings("RedundantThrows")
    public ArrayList<DisplayCriteria> getSubQueries() throws Exception {
        return new ArrayList<>();
    }

    public static SearchCriteria buildCriteria(PermissionCriteriaI c) {
        final SearchCriteria newC = new SearchCriteria();
        try {
            newC.setFieldWXMLPath(c.getField());
            newC.setValue(c.getFieldValue());
        } catch (Exception e) {
            log.error("", e);
        }

        return newC;
    }

    /*****************************************
     * Used to parse out parameters for Prepared Statement execution
     *
     * @param tracker
     * @return
     * @throws InvalidValueException
     */
    public SQLClause templatizeQuery(ValueTracker tracker) throws InvalidValueException {
        SearchCriteria copy = new SearchCriteria();
        copy.comparison_type=(this.comparison_type);
        copy.cleanedType=(this.cleanedType);
        copy.elementName=(elementName);
        copy.field=(field);
        copy.field_name=(field_name);
        copy.overrideFormatting=(true);
        copy.xmlPath=this.xmlPath;

        if(StringUtils.equals(getCleanedType(),"dateTime") && value instanceof String){
            try {
                value = DateUtils.parseDateTime((String)value);
            } catch (ParseException e) {
                log.error("");
            }
        }

        copy.value=tracker.trackValue(value,DBAction.TypeParser(value,this.getCleanedType(),false));

        return copy;
    }
}

