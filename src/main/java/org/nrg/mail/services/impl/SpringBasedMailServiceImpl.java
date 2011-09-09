/**
 * SpringBasedMailServiceImpl
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.mail.services.impl;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.mail.api.MailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Provides relatively implementation-independent mail service to allow access
 * to Spring application context mail service without requiring context
 * initialization outside of Spring classes.
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>

 */
@Service
public class SpringBasedMailServiceImpl extends AbstractMailServiceImpl {

	@Override
	public void sendMessage(MailMessage message) throws MessagingException {
		if (StringUtils.isBlank(message.getHtml())
				&& StringUtils.isBlank(message.getOnBehalfOf())
				&& (message.getAttachments() == null || message
						.getAttachments().size() == 0)
				&& (message.getHeaders() == null || message.getHeaders().size() == 0)) {
			if (_log.isDebugEnabled()) {
				_log.debug("Sending email as a simple mail message");
			}
			_sender.send(message.asSimpleMailMessage());
		} else {
			if (_log.isDebugEnabled()) {
				_log.debug("Sending email as a MIME message");
			}
			sendMimeMessage(message.asMimeMessage(getMimeMessage()));
		}
	}

	/**
	 * Sets the {@link JavaMailSender} object on the service.
	 * 
	 * @param sender
	 *            The sender service instance.
	 */
	public void setJavaMailSender(JavaMailSender sender) {
		_sender = sender;
	}

	/**
	 * Gets a MimeMessage from the mail sender.
	 * 
	 * @return A usable MIME message object.
	 */
	protected MimeMessage getMimeMessage() {
		return _sender.createMimeMessage();
	}

	/**
	 * Sends a MIME message using the mail service. You should create this
	 * message by calling the {@link #getMimeMessage()} method.
	 * 
	 * @param message
	 *            The message to be sent.
	 */
	protected void sendMimeMessage(MimeMessage message) {
		_sender.send(message);
	}

	private static final Log _log = LogFactory.getLog(SpringBasedMailServiceImpl.class);

	@Inject
	private JavaMailSender _sender;
}
