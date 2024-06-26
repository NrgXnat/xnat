/*
 * mail: org.nrg.mail.services.EmailRequestLogService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.mail.services;

import java.util.Date;

public interface EmailRequestLogService{

	public void logEmailRequest(String email, Date time) throws Exception;
	
	public boolean isEmailBlocked(String email);
	
	public void unblockEmail(String email);
	
	public void clearLogs();
}
