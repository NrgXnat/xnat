package org.nrg.mail.services;

import java.util.Date;

public interface EmailRequestLogService{

	public void logEmailRequest(String email, Date time) throws Exception;
	
	public boolean isEmailBlocked(String email);
	
	public void clearLogs();
}
