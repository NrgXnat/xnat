// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.email;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;
import org.nrg.mail.services.impl.SpringBasedMailServiceImpl;
import org.nrg.xft.cl.ExtensibleClassLoader;

/**
 * @deprecated As of version 1.5.3, use the appropriate methods from the {@link SpringBasedMailServiceImpl} class. 
 */
@Deprecated
public class EmailUtils {
	static Logger logger = Logger.getLogger(EmailUtils.class);
	
	/**
	 * @deprecated As of version 1.5.3, use the appropriate methods from the {@link SpringBasedMailServiceImpl} class. 
	 */
	@Deprecated
    public static void sendEmail(List<InternetAddress> to, List<InternetAddress> cc, List<InternetAddress> bcc,String from, String subject, String message) throws EmailException{
		try {
			Class<?> c=ExtensibleClassLoader.GetClass(ExtensibleClassLoader.EMAIL_IMPL);
			EmailerI emailer=(EmailerI)c.newInstance();
			
			emailer.setTo(to);
			emailer.setCc(cc);
			emailer.setBcc(bcc);
			emailer.setFrom(from);
			emailer.setSubject(subject);
			emailer.setMsg(message);
			
			emailer.send();
		} catch (InstantiationException e) {
			logger.error(e);
			throw new EmailException("Unable to send email do to a configuration problem.");
		} catch (IllegalAccessException e) {
			logger.error(e);
			throw new EmailException("Unable to send email do to a configuration problem.");
		} catch (ClassNotFoundException e) {
			logger.error(e);
			throw new EmailException("Unable to send email do to a configuration problem.");
		}
    }
    
	/**
	 * @deprecated As of version 1.5.3, use the appropriate methods from the {@link SpringBasedMailServiceImpl} class. 
	 */
	@Deprecated
    public static EmailerI getEmailer()throws Exception{
    	try {
			Class<?> c=ExtensibleClassLoader.GetClass(ExtensibleClassLoader.EMAIL_IMPL);
			return (EmailerI)c.newInstance();
		} catch (InstantiationException e) {
			logger.error(e);
			throw new EmailException("Unable to send email do to a configuration problem.");
		} catch (IllegalAccessException e) {
			logger.error(e);
			throw new EmailException("Unable to send email do to a configuration problem.");
		} catch (ClassNotFoundException e) {
			logger.error(e);
			throw new EmailException("Unable to send email do to a configuration problem.");
		}
    }
    
	/**
	 * @deprecated As of version 1.5.3, use the appropriate methods from the {@link SpringBasedMailServiceImpl} class. 
	 */
	@Deprecated
    public static void sendEmail(List<InternetAddress> to, String from, String subject, String message) throws EmailException{
    	sendEmail(to, new ArrayList<InternetAddress>(), new ArrayList<InternetAddress>(),from, subject, message);
    }
    
	/**
	 * @deprecated As of version 1.5.3, use the appropriate methods from the {@link SpringBasedMailServiceImpl} class. 
	 */
	@Deprecated
    public static void sendEmail(String to,String from, String subject, String message) throws EmailException,AddressException{
    	sendEmail(Arrays.asList(new InternetAddress[]{new InternetAddress(to)}),from, subject, message);
    }
}
