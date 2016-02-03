/*
 * org.nrg.xnat.helpers.prearchive.SessionDataProducerI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.prearchive;

import java.io.IOException;
import java.util.Collection;
/**
 * Retrieve the session data from some permanent store
 * @author aditya
 *
 */
public interface SessionDataProducerI {
	Collection<SessionData> get() throws IOException;
}
