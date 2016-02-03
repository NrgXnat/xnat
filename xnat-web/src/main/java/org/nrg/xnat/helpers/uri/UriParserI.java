/*
 * org.nrg.xnat.helpers.uri.UriParserI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.uri;

interface UriParserI<T> {
	T readUri (String uri) throws java.util.MissingFormatArgumentException ;
}