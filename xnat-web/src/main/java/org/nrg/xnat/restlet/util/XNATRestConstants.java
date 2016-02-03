/*
 * org.nrg.xnat.restlet.util.XNATRestConstants
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.util;

/**
 * @author Timothy R. Olsen <olsent@wustl.edu>
 *
 */
public class XNATRestConstants {
	public static final String PULL_DATA_FROM_HEADERS = "pullDataFromHeaders";
	public static final String PULL_SESSION_DATA_FROM_HEADERS = "pullSessionDataFromHeaders";
	public static final String PULL_SCAN_DATA_FROM_HEADERS = "pullScanDataFromHeaders";
	public static final String TRIGGER_PIPELINES = "triggerPipelines";
    public static final String SUPRESS_EMAIL = "supressEmail";
	public static final String PREARCHIVE_TIMESTAMP = "yyyyMMdd_HHmmss";
	public static final String FIX_SCAN_TYPES = "fixScanTypes";
	public static final String ALLOW_DATA_DELETION = "allowDataDeletion";
	public static final String OVERWRITE = "overwrite";
	public static final String FILE = "file";
	public static final String IMPORT = "import";
	public static final String TRANSACTION_RECORD_ID="transaction";
	
	public static final String XML_PATH_REGEXP=".*:.*/.*";
	public static final String XML_PATH_REGEXP2=".*:.*\\..*";
}
