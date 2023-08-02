/*
 * framework: org.nrg.framework.utilities.Patterns
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import java.util.regex.Pattern;

/**
 * Contains re-usable regex patterns for matching strings.
 */
@SuppressWarnings("unused")
public class Patterns {
    public static final String ALPHANUMERIC_AND_DASHES = "A-Za-z0-9-_";

    public static final String EXPR_USERNAME            =  "^[a-zA-Z][\\w-'.]+$";
    public static final String EXPR_VALID_MAILBOX_CHARS = "A-Za-z0-9!#\\$%&'*+/=?^_`{|}~-";
    public static final String EXPR_EMAIL               = "(?<mailbox>[" + EXPR_VALID_MAILBOX_CHARS + "][." + EXPR_VALID_MAILBOX_CHARS + "]{0,63})@(?<domain>[A-Za-z0-9][A-Za-z0-9.-]*\\.[A-Za-z]{2,})";
    public static final String EXPR_COMBINED            = "(" + EXPR_USERNAME + ")[\\s]*<(" + EXPR_EMAIL + ")>";

    public static final Pattern EMAIL    = Pattern.compile(EXPR_EMAIL);
    public static final Pattern USERNAME = Pattern.compile(EXPR_USERNAME);
    public static final Pattern COMBINED = Pattern.compile(EXPR_COMBINED);
    public static final Pattern UUID     = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    public static final Pattern IP_PLAIN = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    public static final Pattern IP_MASK  = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}/\\d{1,2}\\b");

    public static final Pattern LIMIT_XSS_CHARS = Pattern.compile("^[^&<>\"/]+$");
}
