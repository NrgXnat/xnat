/*
 * web: org.nrg.xnat.helpers.prearchive.FileSystemSessionData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

public final class FileSystemSessionData extends SessionDataDelegate implements SessionDataProducerI, SessionDataModifierI{
	public FileSystemSessionData (String basePath) {
		super(new FileSystemSessionTrawler(basePath), new FileSystemSessionDataModifier(basePath));
	}	
}
