/*
 * web: org.nrg.xnat.helpers.uri.UriParserI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri;

interface UriParserI<T> {
	T readUri (String uri) throws java.util.MissingFormatArgumentException ;
}
