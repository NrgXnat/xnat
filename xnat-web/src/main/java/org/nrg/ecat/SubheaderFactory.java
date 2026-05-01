/*
 * ecat4xnat: org.nrg.ecat.SubheaderFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat;

interface SubheaderFactory {
	public Header create(final int fileType);
}
