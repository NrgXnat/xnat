/*
 * mail: org.nrg.mail.services.impl.SpringBasedMailServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.mail.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.nrg.mail.api.MailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service("mailService")
public class SpringBasedMailServiceImpl extends AbstractMailServiceImpl {
    @Autowired
    public SpringBasedMailServiceImpl(final JavaMailSender sender) {
        _sender = sender;
    }

    @Override
    public void sendMessage(MailMessage message) throws MessagingException {
        if (_enabled) {
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
    }

    @Override
    public void sendMessage(MailMessage message, String username, String password) throws MessagingException {
        sendMessage(message);
    }

    public void setSmtpEnabled(boolean enabled) {
        _enabled = enabled;
    }

    public boolean getSmtpEnabled() {
        return _enabled;
    }

    public JavaMailSender getJavaMailSender() {
        return _sender;
    }

    /**
     * Sends a MIME message using the mail service. You should create this
     * message by calling the {@link #getMimeMessage()} method.
     *
     * @param message The message to be sent.
     */
    protected void sendMimeMessage(MimeMessage message) {
        _sender.send(message);
    }

    /**
     * Gets a MimeMessage from the mail sender.
     *
     * @return A usable MIME message object.
     */
    protected MimeMessage getMimeMessage() {
        return _sender.createMimeMessage();
    }

    private static final Logger _log = LoggerFactory.getLogger(SpringBasedMailServiceImpl.class);

    private final JavaMailSender _sender;

    private boolean _enabled = true;
}
