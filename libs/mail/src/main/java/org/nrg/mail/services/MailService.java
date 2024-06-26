/*
 * mail: org.nrg.mail.services.MailService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.mail.services;

import org.nrg.framework.services.NrgService;
import org.nrg.mail.api.MailMessage;

import javax.mail.MessagingException;
import java.io.File;
import java.util.Map;

public interface MailService extends NrgService {

    /**
     * The prefix to be used for attachments that are intended as inline attachments
     * (e.g. formatted graphics) in emails.
     */
	String PREFIX_INLINE_ATTACHMENT = "cid:";

	/**
	 * Gets the prefix to add to the subject of emails.
	 * @return The prefix to add to the subject of emails.
	 */
	String getSubjectPrefix();

	/**
	 * Sets the prefix to add to the subject of emails.
	 * @param subjectPrefix    The prefix to add to the subject of emails.
	 */
	void setSubjectPrefix(final String subjectPrefix);

	/**
	 * Indicates whether a {@link #getSubjectPrefix() subject prefix} has been set.
	 * @return <b>true</b> if a non-blank subject prefix has been set.
	 */
	boolean hasSubjectPrefix();

    /**
     * Sends a {@link MailMessage mail message object}. This allows for more free-form composition of mail messages
     * without needing to deal with complicated method signatures.
     * @param message The {@link MailMessage mail message} object to send.
     * @throws javax.mail.MessagingException When a messaging error occurs.
     */
	void sendMessage(MailMessage message) throws MessagingException;

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
	 *
	 * @throws MessagingException When an error occurs attempting to send the message.
     */
	void sendMessage(MailMessage message, String username, String password) throws MessagingException;

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
     * @throws javax.mail.MessagingException When a message error occurs.
	 */
	void sendMessage(String from, String[] to, String[] ccs, String[] bccs, String subject, String body) throws MessagingException;

	/**
	 * Send a simple mail message. This supports multiple addresses on the to
	 * line.
	 * @param from
	 *            The address from which the email will be sent.
	 * @param to
	 *            A list of addresses to which to send the email.
	 * @param ccs A list of addresses to which to copy the email.
     * @param subject
	 *            The subject of the email.
	 * @param message
	 *            The body of the email.
	 *
	 * @see #sendMessage(String, String[], String[], String[], String, String)
	 * @see #sendMessage(String, String[], String, String)
	 * @see #sendMessage(String, String, String, String)
     * @throws javax.mail.MessagingException When a message error occurs.
	 */
	void sendMessage(String from, String[] to, String[] ccs, String subject, String message) throws MessagingException;

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
     * @throws javax.mail.MessagingException When a message error occurs.
	 */
	void sendMessage(String from, String[] to, String subject, String message) throws MessagingException;

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
     * @throws javax.mail.MessagingException When a message error occurs.
	 */
	void sendMessage(String from, String to, String subject, String message) throws MessagingException;

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
     *            the attachment body as a {@link java.io.File} object. Use the prefix
     *            {@link org.nrg.mail.services.MailService#PREFIX_INLINE_ATTACHMENT} to indicate inline
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
    void sendHtmlMessage(String from, String[] to, String[] ccs, String[] bccs, String subject, String html, String text, Map<String, File> attachments, Map<String, String> headers) throws MessagingException;

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
     *            the attachment body as a {@link java.io.File} object.
     *
     * @throws MessagingException
	 *             Thrown when an error occurs during message composition or
	 *             transmission.
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String)
	 */
	void sendHtmlMessage(String from, String[] to, String[] ccs, String[] bccs, String subject, String html, String text, Map<String, File> attachments) throws MessagingException;

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
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String, Map)
     * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String)
     * @see #sendHtmlMessage(String, String[], String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String)
	 */
	void sendHtmlMessage(String from, String[] to, String[] ccs, String[] bccs, String subject, String html, String text) throws MessagingException;

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
     * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String, Map)
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String)
	 */
	void sendHtmlMessage(String from, String[] to, String[] ccs, String[] bccs, String subject, String html) throws MessagingException;

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
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String)
	 */
	void sendHtmlMessage(String from, String[] to, String[] ccs, String subject, String html) throws MessagingException;

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
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String)
	 */
	void sendHtmlMessage(String from, String[] to, String subject, String html) throws MessagingException;

	/**
	 * Send an HTML-based mail message. This method expects the body parameter
	 * to be HTML-formatted already, i.e. it does NOT format plain text to HTML.
	 * This supports a single address for the to, cc, and bcc lines.
	 * @param from
	 *            The address from which the email will be sent.
	 * @param to
	 *            An address to which to send the email.
	 * @param ccs
	 *            An address to which to copy the email.
	 * @param bccs
	 *            An address to which to blind-copy the email.
	 * @param subject
	 *            The subject of the email.
	 * @param html
	 *            The body of the email in HTML format.
	 *
	 * @throws MessagingException
	 *             Thrown when an error occurs during message composition or
	 *             transmission.
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String)
	 */
	void sendHtmlMessage(String from, String to, String ccs, String bccs, String subject, String html) throws MessagingException;

	/**
	 * Send an HTML-based mail message. This method expects the body parameter
	 * to be HTML-formatted already, i.e. it does NOT format plain text to HTML.
	 * This supports a single address for the to, cc, and bcc lines.
	 * @param from
	 *            The address from which the email will be sent.
	 * @param to
	 *            An address to which to send the email.
	 * @param ccs
	 *            An address to which to copy the email.
	 * @param subject
	 *            The subject of the email.
	 * @param html
	 *            The body of the email in HTML format.
	 *
	 * @throws MessagingException
	 *             Thrown when an error occurs during message composition or
	 *             transmission.
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String)
	 */
	void sendHtmlMessage(String from, String to, String ccs, String subject, String html) throws MessagingException;

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
	 * @param message
	 *            The body of the email in HTML format.
	 *
	 * @throws MessagingException
	 *             Thrown when an error occurs during message composition or
	 *             transmission.
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String)
	 */
	void sendHtmlMessage(String from, String to, String subject, String message) throws MessagingException;

	void setSmtpEnabled(boolean enabled);
	boolean getSmtpEnabled();
}
