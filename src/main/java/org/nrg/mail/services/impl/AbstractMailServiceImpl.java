/*
 * org.nrg.mail.services.impl.AbstractMailServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.mail.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.nrg.mail.api.MailMessage;
import org.nrg.mail.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.mail.MessagingException;
import java.io.File;
import java.util.Map;

abstract public class AbstractMailServiceImpl implements MailService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSubjectPrefix() {
        return _subjectPrefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSubjectPrefix(final String subjectPrefix) {
        _hasSubjectPrefix = StringUtils.isNotBlank(subjectPrefix);
        _subjectPrefix = subjectPrefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSubjectPrefix() {
        return _hasSubjectPrefix;
    }

    /**
     * {@inheritDoc}
     */
    public abstract void sendMessage(MailMessage message) throws MessagingException;

    /**
     * {@inheritDoc}
     */
    public abstract void sendMessage(MailMessage message, String username, String password) throws MessagingException;

    /**
     * Protected constructor.
     */
    protected AbstractMailServiceImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(String from, String[] to, String[] ccs, String[] bccs, String subject, String body) throws MessagingException {
        Assert.notNull(to, "To address array must not be null");
        Assert.notNull(from, "From address must not be null");

        if (_log.isDebugEnabled()) {
            logMailData(from, to, subject);
        }

        MailMessage message = new MailMessage();
        message.setTos(to);
        if (ccs != null) {
            message.setCcs(ccs);
        }
        if (bccs != null) {
            message.setBccs(bccs);
        }
        message.setFrom(from);
        message.setSubject(prefixSubject(subject));

        message.setText(body);

        sendMessage(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(String from, String[] to, String[] ccs, String subject, String message) throws MessagingException {
        sendMessage(from, to, ccs, null, subject, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(String from, String[] to, String subject, String message) throws MessagingException {
        sendMessage(from, to, null, null, subject, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(String from, String to, String subject, String message) throws MessagingException {
        sendMessage(from, new String[]{to}, null, null, subject, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendHtmlMessage(String from, String[] to, String[] ccs, String[] bccs, String subject, String html, String text, Map<String, File> attachments, Map<String, String> headers) throws MessagingException {
        Assert.notNull(to, "To address array must not be null");
        Assert.notNull(from, "From address must not be null");

        if (_log.isDebugEnabled()) {
            logMailData(from, to, subject);
        }

        MailMessage message = new MailMessage();
        message.setFrom(from);
        message.setTos(to);
        if (ccs != null && ccs.length > 0) {
            message.setCcs(ccs);
        }
        if (bccs != null && bccs.length > 0) {
            message.setBccs(bccs);
        }
        message.setSubject(prefixSubject(subject));
        if (!StringUtils.isBlank(text)) {
            message.setText(text);
        }
        if (!StringUtils.isBlank(html)) {
            message.setHtml(html);
        }
        if (attachments != null) {
            message.setAttachments(attachments);
        }
        sendMessage(message);
    }

    /**
     * Send an HTML-based mail message. This method takes both an HTML-formatted
     * and plain-text message to support mail clients that don't support or
     * accept HTML-formatted emails. This supports multiple addresses on the to,
     * cc, and bcc lines. It also supports a map of attachments.
     * @param from
     *            The address from which the email will be sent.
     * @param to
     *            A list of addresses to which to send the email.
     * @param ccs
     *            A list of addresses to which to copy the email.
     * @param bccs
     *            A list of addresses to which to blind-copy the email.
     * @param subject
     *            The subject of the email.
     * @param html
     *            The body of the email in HTML format.
     * @param text
     *            The body of the email in plain-text format.
     * @param attachments Items to be attached to the email.
     *
     * @throws MessagingException
     *             Thrown when an error occurs during message composition or
     *             transmission.
     * @see #sendHtmlMessage(String, String[], String[], String[], String, String)
     * @see #sendHtmlMessage(String, String[], String[], String, String)
     * @see #sendHtmlMessage(String, String[], String, String)
     * @see #sendHtmlMessage(String, String, String, String, String, String)
     * @see #sendHtmlMessage(String, String, String, String, String, String)
     * @see #sendHtmlMessage(String, String, String, String)
     */
    @Override
    public void sendHtmlMessage(String from, String[] to, String[] ccs, String[] bccs, String subject, String html, String text, Map<String, File> attachments) throws MessagingException {
        sendHtmlMessage(from, to, ccs, bccs, subject, html, text, attachments, null);
    }

    /**
     * Send an HTML-based mail message. This method takes both an HTML-formatted
     * and plain-text message to support mail clients that don't support or
     * accept HTML-formatted emails. This supports multiple addresses on the to,
     * cc, and bcc lines.
     * @param from
     *            The address from which the email will be sent.
     * @param to
     *            A list of addresses to which to send the email.
     * @param ccs
     *            A list of addresses to which to copy the email.
     * @param bccs
     *            A list of addresses to which to blind-copy the email.
     * @param subject
     *            The subject of the email.
     * @param html
     *            The body of the email in HTML format.
     * @param text
     *            The body of the email in plain-text format.
     *
     * @throws MessagingException
     *             Thrown when an error occurs during message composition or
     *             transmission.
     * @see #sendHtmlMessage(String, String[], String[], String[], String,
     *      String)
     * @see #sendHtmlMessage(String, String[], String[], String, String)
     * @see #sendHtmlMessage(String, String[], String, String)
     * @see #sendHtmlMessage(String, String, String, String, String, String)
     * @see #sendHtmlMessage(String, String, String, String, String, String)
     * @see #sendHtmlMessage(String, String, String, String)
     */
    @Override
    public void sendHtmlMessage(String from, String[] to, String[] ccs, String[] bccs, String subject, String html, String text) throws MessagingException {
        sendHtmlMessage(from, to, ccs, bccs, subject, html, text, null);
    }

    /**
     * Send an HTML-based mail message. This method expects the body parameter
     * to be HTML-formatted already, i.e. it does NOT format plain text to HTML.
     * This supports multiple addresses on the to, cc, and bcc lines.
     * @param from
     *            The address from which the email will be sent.
     * @param to
     *            A list of addresses to which to send the email.
     * @param ccs
     *            A list of addresses to which to copy the email.
     * @param bccs
     *            A list of addresses to which to blind-copy the email.
     * @param subject
     *            The subject of the email.
     * @param html
     *            The body of the email in HTML format.
     *
     * @throws MessagingException
     *             Thrown when an error occurs during message composition or
     *             transmission.
     * @see #sendHtmlMessage(String, String[], String[], String[], String,
     *      String, String)
     * @see #sendHtmlMessage(String, String[], String[], String, String)
     * @see #sendHtmlMessage(String, String[], String, String)
     * @see #sendHtmlMessage(String, String, String, String, String, String)
     * @see #sendHtmlMessage(String, String, String, String, String, String)
     * @see #sendHtmlMessage(String, String, String, String)
     */
    @Override
    public void sendHtmlMessage(String from, String[] to, String[] ccs, String[] bccs, String subject, String html) throws MessagingException {
        sendHtmlMessage(from, to, ccs, bccs, subject, html, null, null);
    }

    /**
     * Send an HTML-based mail message. This method expects the body parameter
     * to be HTML-formatted already, i.e. it does NOT format plain text to HTML.
     * This supports multiple addresses on the to line.
     * @param from
     *            The address from which the email will be sent.
     * @param to
     *            A list of addresses to which to send the email.
     * @param ccs
     *            A list of addresses to which to copy the email.
     * @param subject
     *            The subject of the email.
     * @param html
     *            The body of the email in HTML format.
     *
     * @throws MessagingException
     *             Thrown when an error occurs during message composition or
     *             transmission.
     * @see #sendHtmlMessage(String, String[], String[], String[], String,
     *      String, String)
     * @see #sendHtmlMessage(String, String[], String[], String[], String,
     *      String)
     * @see #sendHtmlMessage(String, String[], String, String)
     * @see #sendHtmlMessage(String, String, String, String, String, String)
     * @see #sendHtmlMessage(String, String, String, String, String, String)
     * @see #sendHtmlMessage(String, String, String, String)
     */
    @Override
    public void sendHtmlMessage(String from, String[] to, String[] ccs, String subject, String html) throws MessagingException {
        sendHtmlMessage(from, to, ccs, null, subject, html, null, null);
    }

    /**
     * Send an HTML-based mail message. This method expects the body parameter
     * to be HTML-formatted already, i.e. it does NOT format plain text to HTML.
     * This supports multiple addresses on the to line.
     * @param from
     *            The address from which the email will be sent.
     * @param to
     *            A list of addresses to which to send the email.
     * @param subject
     *            The subject of the email.
     * @param html
     *            The body of the email in HTML format.
     *
     * @throws MessagingException
     *             Thrown when an error occurs during message composition or
     *             transmission.
     * @see #sendHtmlMessage(String, String[], String[], String[], String,
     *      String, String)
     * @see #sendHtmlMessage(String, String[], String[], String[], String,
     *      String)
     * @see #sendHtmlMessage(String, String[], String[], String, String)
     * @see #sendHtmlMessage(String, String[], String, String)
     * @see #sendHtmlMessage(String, String, String, String, String, String)
     * @see #sendHtmlMessage(String, String, String, String, String, String)
     * @see #sendHtmlMessage(String, String, String, String)
     */
    @Override
    public void sendHtmlMessage(String from, String[] to, String subject, String html) throws MessagingException {
        sendHtmlMessage(from, to, null, null, subject, html, null, null);
    }

    /**
     * Send an HTML-based mail message. This method expects the body parameter
     * to be HTML-formatted already, i.e. it does NOT format plain text to HTML.
     * This supports a single address for the to, cc, and bcc lines.
     * @param from
     *            The address from which the email will be sent.
     * @param to
     *            An address to which to send the email.
     * @param cc
     *            An address to which to copy the email.
     * @param bcc
     *            An address to which to blind-copy the email.
     * @param subject
     *            The subject of the email.
     * @param html
     *            The body of the email in HTML format.
     *
     * @throws MessagingException
     *             Thrown when an error occurs during message composition or
     *             transmission.
     * @see #sendHtmlMessage(String, String[], String[], String[], String,
     *      String, String)
     * @see #sendHtmlMessage(String, String[], String[], String[], String,
     *      String)
     * @see #sendHtmlMessage(String, String[], String[], String, String)
     * @see #sendHtmlMessage(String, String[], String, String)
     * @see #sendHtmlMessage(String, String, String, String, String)
     * @see #sendHtmlMessage(String, String, String, String)
     */
    @Override
    public void sendHtmlMessage(String from, String to, String cc, String bcc, String subject, String html) throws MessagingException {
        String[] ccs = cc == null ? null : new String[]{cc};
        String[] bccs = bcc == null ? null : new String[]{bcc};
        sendHtmlMessage(from, new String[]{to}, ccs, bccs, subject, html, null, null);
    }

    /**
     * Send an HTML-based mail message. This method expects the body parameter
     * to be HTML-formatted already, i.e. it does NOT format plain text to HTML.
     * This supports a single address for the to, cc, and bcc lines.
     * @param from
     *            The address from which the email will be sent.
     * @param to
     *            An address to which to send the email.
     * @param cc
     *            An address to which to copy the email.
     * @param subject
     *            The subject of the email.
     * @param html
     *            The body of the email in HTML format.
     *
     * @throws MessagingException
     *             Thrown when an error occurs during message composition or
     *             transmission.
     * @see #sendHtmlMessage(String, String[], String[], String[], String,
     *      String, String)
     * @see #sendHtmlMessage(String, String[], String[], String[], String,
     *      String)
     * @see #sendHtmlMessage(String, String[], String[], String, String)
     * @see #sendHtmlMessage(String, String[], String, String)
     * @see #sendHtmlMessage(String, String, String, String, String, String)
     * @see #sendHtmlMessage(String, String, String, String)
     */
    @Override
    public void sendHtmlMessage(String from, String to, String cc, String subject, String html) throws MessagingException {
        String[] ccs = cc == null ? null : new String[]{cc};
        sendHtmlMessage(from, new String[]{to}, ccs, null, subject, html, null, null);
    }

    /**
     * Send an HTML-based mail message. This method expects the body parameter
     * to be HTML-formatted already, i.e. it does NOT format plain text to HTML.
     * This supports a single address on the to line.
     * @param from
     *            The address from which the email will be sent.
     * @param to
     *            An address to which to send the email.
     * @param subject
     *            The subject of the email.
     * @param html
     *            The body of the email in HTML format.
     *
     * @throws MessagingException
     *             Thrown when an error occurs during message composition or
     *             transmission.
     * @see #sendHtmlMessage(String, String[], String[], String[], String,
     *      String, String)
     * @see #sendHtmlMessage(String, String[], String[], String[], String,
     *      String)
     * @see #sendHtmlMessage(String, String[], String[], String, String)
     * @see #sendHtmlMessage(String, String[], String, String)
     * @see #sendHtmlMessage(String, String, String, String, String, String)
     * @see #sendHtmlMessage(String, String, String, String, String)
     */
    @Override
    public void sendHtmlMessage(String from, String to, String subject, String html) throws MessagingException {
        sendHtmlMessage(from, new String[]{to}, null, null, subject, html, null, null);
    }

    /**
     * Adds the system prefix if not already present.
     * @param subject    The subject.
     * @return The subject prefixed with the system prefix if not already present.
     */
    protected String prefixSubject(final String subject) {
        return hasSubjectPrefix() && !subject.startsWith(getSubjectPrefix()) ? getSubjectPrefix() + ":" + subject : subject;
    }

    private void logMailData(final String from, final String[] to, final String subject) {
        StringBuilder tos = new StringBuilder();
        if (to.length > 0) {
            boolean started = false;
            for (String address : to) {
                if (started) {
                    tos.append(", ");
                } else {
                    started = true;
                }
                tos.append(address);
            }
        }
        _log.debug(String.format("Sending mail message: FROM[%s] TO [%S], SUBJECT[%S]", from, tos.toString(), subject));
    }

    private static final Logger _log = LoggerFactory.getLogger(AbstractMailServiceImpl.class);

    private boolean _hasSubjectPrefix;
    private String _subjectPrefix;
}
