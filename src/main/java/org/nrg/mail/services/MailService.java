/**
 * MailService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 23, 2011
 */
package org.nrg.mail.services;

import org.nrg.mail.api.MailMessage;
import org.springframework.core.io.FileSystemResource;

import javax.mail.MessagingException;
import java.util.Map;

/**
 * Provides implementation-independent mail service. This implements a large array of methods to send messages
 * to allow for easier key-value specification of parameters for REST-style invocations.
 *
 * @author rherrick
 */
public interface MailService extends NrgService {
    public static String SERVICE_NAME = "MailService"; 
    
    /**
     * The prefix to be used for attachments that are intended as inline attachments
     * (e.g. formatted graphics) in emails.
     */
    public final static String PREFIX_INLINE_ATTACHMENT = "cid:";

    /**
     * Sends a {@link MailMessage mail message object}. This allows for more free-form composition of mail messages
     * without needing to deal with complicated method signatures.
     * @param message The {@link MailMessage mail message} object to send.
     * @throws javax.mail.MessagingException When a messaging error occurs.
     */
    public abstract void sendMessage(MailMessage message) throws MessagingException;

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
     * @throws javax.mail.MessagingException When a message error occurs.
	 */
	public abstract void sendMessage(String from, String[] to, String[] cc, String[] bcc, String subject, String body) throws MessagingException;

	/**
	 * Send a simple mail message. This supports multiple addresses on the to
	 * line.
	 * @param from
	 *            The address from which the email will be sent.
	 * @param to
	 *            A list of addresses to which to send the email.
	 * @param cc  A list of addresses to which to copy the email.
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
	public abstract void sendMessage(String from, String[] to, String[]cc, String subject, String message) throws MessagingException;

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
	public abstract void sendMessage(String from, String[] to, String subject, String message) throws MessagingException;

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
	public abstract void sendMessage(String from, String to, String subject, String message) throws MessagingException;

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
    void sendHtmlMessage(String from, String[] to, String[] cc, String[] bcc, String subject, String html, String text, Map<String, FileSystemResource> attachments, Map<String, String> headers) throws MessagingException;

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
	public abstract void sendHtmlMessage(String from, String[] to, String[] cc, String[] bcc, String subject, String html, String text, Map<String, FileSystemResource> attachments) throws MessagingException;

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
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String, Map)
     * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String)
     * @see #sendHtmlMessage(String, String[], String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String)
	 */
	public abstract void sendHtmlMessage(String from, String[] to, String[] cc, String[] bcc, String subject, String html, String text) throws MessagingException;

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
     * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String, Map)
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String)
	 */
	public abstract void sendHtmlMessage(String from, String[] to, String[] cc, String[] bcc, String subject, String html) throws MessagingException;

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
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String)
	 */
	public abstract void sendHtmlMessage(String from, String[] to, String[] cc, String subject, String html) throws MessagingException;

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
	public abstract void sendHtmlMessage(String from, String[] to, String subject, String html) throws MessagingException;

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
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String)
	 */
	public abstract void sendHtmlMessage(String from, String to, String cc, String bcc, String subject, String html) throws MessagingException;

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
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String[], String, String)
	 * @see #sendHtmlMessage(String, String[], String, String)
	 * @see #sendHtmlMessage(String, String, String, String, String, String)
	 * @see #sendHtmlMessage(String, String, String, String)
	 */
	public abstract void sendHtmlMessage(String from, String to, String cc, String subject, String html) throws MessagingException;

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
	public abstract void sendHtmlMessage(String from, String to, String subject, String message) throws MessagingException;
}
