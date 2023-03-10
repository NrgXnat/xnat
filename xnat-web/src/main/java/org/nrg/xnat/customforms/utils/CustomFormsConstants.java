/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * @author: Mohana Ramaratnam (mohana@radiologics.com)
 * @since: 07-03-2021
 */

package org.nrg.xnat.customforms.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomFormsConstants {

    public static final String ADMIN_ROLE = "Administrator";
    public static final String FORM_MANAGER_ROLE = "CustomFormManager";
    public static final String PROTOCOL_UNAWARE = "PROTOCOL_UNAWARE";
    public static final String PROTOCOL_PLUGIN_AWARE = "PROTOCOL_AWARE";
    public static final String DELIMITER = ":";
    public static final String ENABLED_STATUS_STRING = "enabled";
    public static final String DISABLED_STATUS_STRING = "disabled";
    public static final String OPTED_OUT_STATUS_STRING = "optedout";
    public static final String DELETED = "deleted";
    public static final String PROTOCOLS_PLUGIN_IDENTIFIER = "protocols";
    public static final Pattern LIMIT_JSON_XSS_CHARS = Pattern.compile("^[^&<>]+$");
    public static final String CUSTOM_FORM_DATATYPE_FOR_WRKFLOW = "custom_form";
    public static final int NO_DATA_AVAILABLE_FOR_CUSTOM_VARIABLE = -100;
    public static final int EMPTY_FORM_DATA_FOR_CUSTOM_VARIABLE = -200;

    public final static String COMPONENTS_KEY = "components";
    public final static String TITLE_KEY = "title";
    public final static String LABEL_KEY = "label";
    public final static String SETTINGS_KEY = "settings";
    public final static String DISPLAY_KEY = "display";
    public final static String COMPONENTS_KEY_FIELD = "key";
    public final static String COMPONENTS_TYPE_FIELD = "type";
    public final static String COMPONENTS_INPUT_FIELD = "input";
    public final static String COMPONENT_CONTENT_TYPE = "content";
    public final static String COMPONENT_PANEL_TYPE = "panel";
    public final static String COMPONENTS_ROWS_TYPE = "rows";
    public final static String COMPONENTS_COLUMNS_TYPE = "columns";
    public final static String CONTAINER_KEY = "container";
    public final static String DOT_SEPARATOR = ".";
    public static final String IS_SITEWIDE_YES = "YES";
    public static final String IS_SITEWIDE_NO = "NO";
    public static final String DEFAULT_XNAT_TYPE = "string";

    public static final Set<String> IS_SITEWIDE_VALUES = Stream.of(IS_SITEWIDE_YES, IS_SITEWIDE_NO)
            .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));

    public static final Set<String> NON_SEARCHABLE_FORMIO_TYPES = Stream.of("tree", "editgrid", "datagrid", "datamap", "address")
            .collect(Collectors.toCollection(HashSet::new));

    public static final Map<String, String> FORMIO_TYPE_TO_XNAT_TYPE = Stream.of(new String[][] {
            { "xnatNumber", "float" },
            { "Number", "float" },
            { "day", "date" },
            { "datetime", "timestamp" },
            { "time", "time" },
            { "xnatdate", "date" }
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
}
