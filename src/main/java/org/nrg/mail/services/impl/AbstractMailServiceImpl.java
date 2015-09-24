/**
 * AbstractMailServiceImpl
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 * <p/>
 * Released under the Simplified BSD License
 * <p/>
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.mail.services.impl;

import java.io.File;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.mail.api.MailMessage;
import org.nrg.mail.services.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

abstract public class AbstractMailServiceImpl implements MailService {

    /**
     * Gets the prefix to add to the subject of emails.
     * @return The prefix to add to the subject of emails.
     */
    @Override
    public String getSubjectPrefix() {
        return _subjectPrefix;
    }

    /**
     * Sets the prefix to add to the subject of emails.
     * @param subjectPrefix    The prefix to add to the subject of emails.
     */
    @Override
    @Value("${mailserver.prefix}")
    public void setSubjectPrefix(final String subjectPrefix) {
        _hasSubjectPrefix = StringUtils.isNotBlank(subjectPrefix);
        _subjectPrefix = subjectPrefix;
    }

    /**
     * Indicates whether a {@link #getSubjectPrefix() subject prefix} has been set.
     * @return <b>true</b> if a non-blank subject prefix has been set.
     */
    @Override
    public boolean hasSubjectPrefix() {
        return _hasSubjectPrefix;
    }

    /**
     * Sends a {@link MailMessage}. The XDAT mail message class abstracts the plain-text, HTML,
     * attachment, and other specialized logic away and leaves it to the implementation of this
     * method to make the proper decisions about how the message should actually be dispatched.
     * @param message The mail message object to send.
     */
    public abstract void sendMessage(MailMessage message) throws MessagingException;

    /**
     * Sends a {@link MailMessage}. The XDAT mail message class abstracts the plain-text, HTML,
     * attachment, and other specialized logic away and leaves it to the implementation of this
     * method to make the proper decisions about how the message should actually be dispatched.
     * This includes a username and password to validate against the mail service being used.
     * This method may be unimplemented in cases where the username and password aren't used
     * or where the username and password are intended to be cached and used in a singleton.
     *
     * @param message     The mail message object to send.
     * @param username    The username to use to validate against the mail service.
     * @param password    The password to use to validate against the mail service.
     */
    public abstract void sendMessage(MailMessage message, String username, String password) throws MessagingException;

    /**
     * Protected constructor.
     */
    protected AbstractMailServiceImpl() {
    }

    /**
     * Send a simple mail message. This supports multiple addresses on the to,
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
     * @param body
     *            The body of the email.
     *
     * @see #sendMessage(String, String[], String[], String, String)
     * @see #sendMessage(String, String[], String, String)
     * @see #sendMessage(String, String, String, String)
     */
    @Override
    public void sendMessage(String from, String[] to, String[] ccs, String[] bccs, String subject, String body) throws MessagingException {
        Assert.notNull(to, "To address array must not be null");
        Assert.notNull(from, "From address must not be null");

        if (_log.isDebugEnabled()) {
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
     * Send a simple mail message. This supports multiple addresses on the to
     * line.
     * @param from
     *            The address from which the email will be sent.
     * @param to
     *            A list of addresses to which to send the email.
     * @param ccs
     *            A list of addresses to which to cc the email.
     * @param subject
     *            The subject of the email.
     * @param message
     *            The body of the email.
     *
     * @see #sendMessage(String, String[], String[], String[], String, String)
     * @see #sendMessage(String, String[], String, String)
     * @see #sendMessage(String, String, String, String)
     */
    @Override
    public void sendMessage(String from, String[] to, String[] ccs, String subject, String message) throws MessagingException {
        sendMessage(from, to, ccs, null, subject, message);
    }

    /**
     * Send a simple mail message. This supports multiple addresses on the to
     * line.
     * @param from
     *            The address from which the email will be sent.
     * @param to
     *            A list of addresses to which to send the email.
     * @param subject
     *            The subject of the email.
     * @param message
     *            The body of the email.
     *
     * @see #sendMessage(String, String[], String[], String[], String, String)
     * @see #sendMessage(String, String[], String[], String, String)
     * @see #sendMessage(String, String, String, String)
     */
    @Override
    public void sendMessage(String from, String[] to, String subject, String message) throws MessagingException {
        sendMessage(from, to, null, null, subject, message);
    }

    /**
     * Send a simple mail message. This supports a single address on the to
     * line.
     * @param from
     *            The address from which the email will be sent.
     * @param to
     *            An address to which to send the email.
     * @param subject
     *            The subject of the email.
     * @param message
     *            The body of the email.
     *
     * @see #sendMessage(String, String[], String[], String[], String, String)
     * @see #sendMessage(String, String[], String[], String, String)
     * @see #sendMessage(String, String[], String, String)
     */
    @Override
    public void sendMessage(String from, String to, String subject, String message) throws MessagingException {
        sendMessage(from, new String[]{to}, null, null, subject, message);
    }

    /**
     * Send an HTML-based mail message. This method takes both an HTML-formatted
     * and plain-text message to support mail clients that don't support or
     * accept HTML-formatted emails. This supports multiple addresses on the to,
     * cc, and bcc lines.
     *
     * This method also accepts a list of attachments.
     *
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
     * @param attachments
     *            A map of attachments, with the attachment name as a string and
     *            the attachment body as a {@link File} object. Use the prefix
     *            {@link MailService#PREFIX_INLINE_ATTACHMENT} to indicate inline
     *            attachments.
     *
     * @param headers Additional headers to be added to the message.
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
    public void sendHtmlMessage(String from, String[] to, String[] ccs, String[] bccs, String subject, String html, String text, Map<String, File> attachments, Map<String, String> headers) throws MessagingException {
        Assert.notNull(to, "To address array must not be null");
        Assert.notNull(from, "From address must not be null");

        if (_log.isDebugEnabled()) {
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

    private static final Log _log = LogFactory.getLog(AbstractMailServiceImpl.class);

    private boolean _hasSubjectPrefix;
    private String _subjectPrefix;
}
