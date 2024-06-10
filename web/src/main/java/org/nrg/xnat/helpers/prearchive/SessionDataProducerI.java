/*
 * web: org.nrg.xnat.helpers.prearchive.SessionDataProducerI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
