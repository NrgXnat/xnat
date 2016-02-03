/*
 * org.nrg.xnat.helpers.prearchive.FileSystemSessionData
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.prearchive;

public final class FileSystemSessionData extends SessionDataDelegate implements SessionDataProducerI, SessionDataModifierI{
	public FileSystemSessionData (String basePath) {
		super(new FileSystemSessionTrawler(basePath), new FileSystemSessionDataModifier(basePath));
	}	
}
