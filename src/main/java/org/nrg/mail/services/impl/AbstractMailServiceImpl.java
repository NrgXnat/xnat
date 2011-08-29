/**
 * AbstractMailServiceImpl
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.mail.services.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.mail.api.MailMessage;
import org.nrg.mail.services.MailService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.Assert;

import javax.mail.MessagingException;
import java.util.Map;

abstract public class AbstractMailServiceImpl implements MailService {

    /**
     * Sends a {@link MailMessage}. The XDAT mail message class abstracts the plain-text, HTML,
     * attachment, and other specialized logic away and leaves it to the implementation of this
     * method to make the proper decisions about how the message should actually be dispatched.
     * @param message The mail message object to send.
     */
    public abstract void sendMessage(MailMessage message) throws MessagingException;

    /**
     * Public constructor. Generally, you should NOT call this method! Instead
     * call the static initializer method {@link #getInstance()}. The public
     * constructor is provided to allow Spring to create the class and auto-wire
     * the mail sender.
     * @throws NrgServiceException Thrown when service is already initialized.
     */
    public AbstractMailServiceImpl() throws NrgServiceException {
        if (_instance != null) {
            throw new NrgServiceException(NrgServiceError.AlreadyInitialized, "The mail service instance is already initialized. Use the static MailService.getInstance() method to get an instance of this class.");
        }
        _log.info("Initializing mail service static singleton.");
        setInstance(this);
    }

    /**
     * Returns an existing instance of the service class. The underlying
     * singleton should be initialized by the context, e.g. Spring Framework.
     *
     * @return An instance of the mail service.
     */
    public static MailService getInstance() {
        if (_instance == null) {
            throw new RuntimeException("The mail service is not yet initialized. Use the static MailService.getInstance() method to get an instance of this class.");
        }
        _log.debug("Returning static mail service singleton.");
        return _instance;
    }

    /**
     * Send a simple mail message. This supports multiple addresses on the to,
     * cc, and bcc lines.
     * @param from
     *            The address from which the email will be sent.
     * @param to
     *            A list of addresses to which to send the email.
     * @param cc
     *            A list of addresses to which to copy the email.
     * @param bcc
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
    public void sendMessage(String from, String[] to, String[] cc, String[] bcc, String subject, String body) throws MessagingException {
        Assert.notNull(to, "To address array must not be null");
        Assert.notNull(from, "From address must not be null");

        if (_log.isDebugEnabled()) {
            StringBuilder tos = new StringBuilder();
            if (to != null && to.length > 0) {
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
        if (cc != null)
            message.setCcs(cc);
        if (bcc != null)
            message.setBccs(bcc);
        message.setFrom(from);
        message.setSubject(subject);
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
        sendMessage(from, to, ccs, new String[] {}, subject, message);
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
        sendMessage(from, to, new String[] {}, new String[] {}, subject, message);
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
        sendMessage(from, new String[] { to }, new String[] {}, new String[] {}, subject, message);
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
     * @param cc
     *            A list of addresses to which to copy the email.
     * @param bcc
     *            A list of addresses to which to blind-copy the email.
     * @param subject
     *            The subject of the email.
     * @param html
     *            The body of the email in HTML format.
     * @param text
     *            The body of the email in plain-text format.
     * @param attachments
     *            A map of attachments, with the attachment name as a string and
     *            the attachment body as a {@link java.io.File} object. Use the prefix
     *            {@link org.nrg.xdat.mail.services.MailService#PREFIX_INLINE_ATTACHMENT} to indicate inline
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
    public void sendHtmlMessage(String from, String[] to, String[] cc, String[] bcc, String subject, String html, String text, Map<String, FileSystemResource> attachments, Map<String, String> headers) throws MessagingException {
        Assert.notNull(to, "To address array must not be null");
        Assert.notNull(from, "From address must not be null");

        if (_log.isDebugEnabled()) {
            StringBuilder tos = new StringBuilder();
            if (to != null && to.length > 0) {
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
        if (cc != null && cc.length > 0)
            message.setCcs(cc);
        if (bcc != null && bcc.length > 0)
            message.setBccs(bcc);
        message.setSubject(subject);
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
     * @param cc
     *            A list of addresses to which to copy the email.
     * @param bcc
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
    public void sendHtmlMessage(String from, String[] to, String[] cc, String[] bcc, String subject, String html, String text, Map<String, FileSystemResource> attachments) throws MessagingException {
        sendHtmlMessage(from, to, cc, bcc, subject, html, text, attachments, null);
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
     * @param cc
     *            A list of addresses to which to copy the email.
     * @param bcc
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
    public void sendHtmlMessage(String from, String[] to, String[] cc, String[] bcc, String subject, String html, String text) throws MessagingException {
        sendHtmlMessage(from, to, cc, bcc, subject, html, text, null);
    }

    /**
     * Send an HTML-based mail message. This method expects the body parameter
     * to be HTML-formatted already, i.e. it does NOT format plain text to HTML.
     * This supports multiple addresses on the to, cc, and bcc lines.
     * @param from
     *            The address from which the email will be sent.
     * @param to
     *            A list of addresses to which to send the email.
     * @param cc
     *            A list of addresses to which to copy the email.
     * @param bcc
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
    public void sendHtmlMessage(String from, String[] to, String[] cc, String[] bcc, String subject, String html) throws MessagingException {
        sendHtmlMessage(from, to, cc, bcc, subject, html, null, null);
    }

    /**
     * Send an HTML-based mail message. This method expects the body parameter
     * to be HTML-formatted already, i.e. it does NOT format plain text to HTML.
     * This supports multiple addresses on the to line.
     * @param from
     *            The address from which the email will be sent.
     * @param to
     *            A list of addresses to which to send the email.
     * @param cc
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
    public void sendHtmlMessage(String from, String[] to, String[] cc, String subject, String html) throws MessagingException {
        sendHtmlMessage(from, to, cc, new String[] {}, subject, html, null, null);
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
        sendHtmlMessage(from, to, new String[] {}, new String[] {}, subject, html, null, null);
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
        sendHtmlMessage(from, new String[] { to }, new String[] { cc }, new String[] { bcc }, subject, html, null, null);
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
        sendHtmlMessage(from, new String[] { to }, new String[] { cc }, new String[] {}, subject, html, null, null);
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
        sendHtmlMessage(from, new String[] { to }, new String[] {}, new String[] {}, subject, html, null, null);
    }

    /**
     * Sets the instance of the service class.
     * @param instance The mail service to set.
     */
    protected static void setInstance(MailService instance) {
        if (_instance != null) {
            throw new RuntimeException("The mail service is already initialized with an instance of type " + _instance.getClass().getName() + ". Use the static MailService.getInstance() method to get the existing service instance.");
        }
        _log.debug("Returning static mail service singleton.");
        _instance = instance;
    }

   private static final Log _log = LogFactory.getLog(AbstractMailServiceImpl.class);
   private static MailService _instance;
}

