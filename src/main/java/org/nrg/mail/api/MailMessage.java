/*
 * org.nrg.mail.api.MailMessage
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.mail.api;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.nrg.mail.exceptions.InvalidMailAttachmentException;
import org.nrg.mail.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * This class provides a convenient encapsulation of mail message functionality that
 * can be used with NRG {@link MailService} implementations.
 */
@SuppressWarnings("unused")
public class MailMessage {
    public static final String PROP_FROM = "from";
    public static final String PROP_ON_BEHALF_OF = "onBehalfOf";
    public static final String PROP_TOS = "tos";
    public static final String PROP_CCS = "ccs";
    public static final String PROP_BCCS = "bccs";
    public static final String PROP_SUBJECT = "subject";
    public static final String PROP_HTML = "html";
    public static final String PROP_TEXT = "text";
    public static final String PROP_ATTACHMENTS = "attachments";

    public MailMessage() {
        _attachments = new HashMap<>();
    }

    /**
     * Creates a new mail message from the given parameters.
     * @param from           Who it's from.
     * @param onBehalfOf     Who it's on behalf of.
     * @param tos            Who it's to.
     * @param ccs            Who else it's to.
     * @param bccs           Who else it's to (but it's a secret!).
     * @param subject        The subject because yeah.
     * @param html           HTML body.
     * @param text           Text body.
     * @param attachments    Any attachments.
     * @throws InvalidMailAttachmentException If the attachment can't be located via file path, URI, or other standard means.
     */
    public MailMessage(String from, String onBehalfOf, List<String> tos, List<String> ccs, List<String> bccs, String subject, String html, String text, Map<String, Object> attachments) throws InvalidMailAttachmentException {
        _from = from;
        _onBehalfOf = onBehalfOf;
        _tos = tos;
        _ccs = ccs;
        _bccs = bccs;
        _subject = subject;
        _html = html;
        _text = text;

        _attachments = new HashMap<>();
        if (attachments != null) {
            _attachments = convertGenericAttachmentMap(attachments);
        }
    }

    @SuppressWarnings("unchecked")
    public MailMessage(Map<String, Object> properties) throws InvalidMailAttachmentException {
        _from = (String) properties.get(PROP_FROM);
        if (properties.containsKey(PROP_ON_BEHALF_OF)) {
            _onBehalfOf = (String) properties.get(PROP_ON_BEHALF_OF);
        }
        if (properties.containsKey(PROP_TOS)) {
            _tos = convertObjectToStringList(properties.get(PROP_TOS));
        }
        if (properties.containsKey(PROP_CCS)) {
            _ccs = convertObjectToStringList(properties.get(PROP_CCS));
        }
        if (properties.containsKey(PROP_BCCS)) {
            _bccs = convertObjectToStringList(properties.get(PROP_BCCS));
        }
        if (properties.containsKey(PROP_SUBJECT)) {
            _subject = (String) properties.get(PROP_SUBJECT);
        }
        if (properties.containsKey(PROP_HTML)) {
            _html = (String) properties.get(PROP_HTML);
        }
        if (properties.containsKey(PROP_TEXT)) {
            _text = (String) properties.get(PROP_TEXT);
        }
        _attachments = new HashMap<>();
        if (properties.containsKey(PROP_ATTACHMENTS)) {
            _attachments = convertGenericAttachmentMap((Map<String, Object>) properties.get(PROP_ATTACHMENTS));
        }
    }

    public String getFrom() {
        return _from;
    }

    public void setFrom(String from) {
        _from = from;
    }

    public String getOnBehalfOf() {
        return _onBehalfOf;
    }

    public void setOnBehalfOf(String onBehalfOf) {
        _onBehalfOf = onBehalfOf;
    }

    public List<String> getTos() {
        return _tos;
    }

    public void setTos(List<String> tos) {
        _tos = tos;
    }

    public void setTos(String[] tos) {
        setTos(Arrays.asList(tos));
    }

    public void addTo(String to) {
        _tos.add(to);
    }

    public List<String> getCcs() {
        return _ccs;
    }

    public void setCcs(List<String> ccs) {
        _ccs = ccs;
    }

    public void setCcs(String[] ccs) {
        setCcs(Arrays.asList(ccs));
    }

    public void addCc(String cc) {
        _ccs.add(cc);
    }

    public List<String> getBccs() {
        return _bccs;
    }

    public void setBccs(List<String> bccs) {
        _bccs = bccs;
    }

    public void setBccs(String[] bccs) {
        setBccs(Arrays.asList(bccs));
    }

    public void addBcc(String bcc) {
        _bccs.add(bcc);
    }

    public String getSubject() {
        return _subject;
    }

    public void setSubject(String subject) {
        _subject = subject;
    }

    public String getHtml() {
        return _html;
    }

    public void setHtml(String html) {
        _html = html;
    }

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
    }

    public Map<String, List<String>> getHeaders() {
        return _headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        _headers = headers;
    }

    public void addHeader(String header, String value) {
        List<String> values;
        if (_headers.containsKey(header)) {
            values = _headers.get(header);
        } else {
            values = new ArrayList<>();
            _headers.put(header, values);
        }
        values.add(value);
    }

    public Map<String, File> getAttachments() {
        return _attachments;
    }

    public void setAttachments(Map<String, File> attachments) {
        _attachments = attachments;
    }

    public void addAttachment(String id, File attachment) {
        _attachments.put(id, attachment);
    }

    public void addAttachment(String id, String attachment) {
        _attachments.put(id, new File(attachment));
    }
    
    public void addAttachment(String id, URI attachment) {
        _attachments.put(id, new File(attachment));
    }
    
    public SimpleMailMessage asSimpleMailMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
	message.setFrom(_from);
        message.setTo(_tos.toArray(new String[_tos.size()]));
        if (_ccs.size() > 0) {
            message.setCc(_ccs.toArray(new String[_ccs.size()]));
        }
        if (_bccs.size() > 0) {
            message.setBcc(_ccs.toArray(new String[_bccs.size()]));
        }
        message.setSubject(_subject);
        message.setText(_text);
        return message;
    }

    public MimeMessage asMimeMessage(MimeMessage message) throws MessagingException {
        boolean hasHtmlAndText = !StringUtils.isBlank(_html) && !StringUtils.isBlank(_text);
        boolean hasAttachments = _attachments != null && _attachments.size() > 0;

        MimeMessageHelper helper = new MimeMessageHelper(message, hasHtmlAndText || hasAttachments);

        if (!StringUtils.isBlank(_from)) {
            helper.setFrom(_from);
        }

        if (!StringUtils.isBlank(_onBehalfOf)) {
            helper.getMimeMessage().addHeader("Sender", _onBehalfOf);
        }

        helper.setTo(_tos.toArray(new String[_tos.size()]));
        helper.setCc(_ccs.toArray(new String[_ccs.size()]));
        helper.setBcc(_bccs.toArray(new String[_bccs.size()]));

        helper.setSubject(_subject);

        if (StringUtils.isBlank(_text)) {
            helper.setText(_html, true);
        } else if (StringUtils.isBlank(_html)) {
            helper.setText(_text, false);
        } else {
            helper.setText(_text, _html);
        }

        for (Map.Entry<String, List<String>> header : _headers.entrySet()) {
            String key = header.getKey();
            List<String> values = header.getValue();
            for (String value : values) {
                helper.getMimeMessage().addHeader(key, value);
            }
        }

        if (hasAttachments) {
            for (Map.Entry<String, File> attachment : _attachments.entrySet()) {
                helper.addAttachment(attachment.getKey(), attachment.getValue());
            }
        }

        return helper.getMimeMessage();
    }

    /**
     * Converts the mail message into an <a href="http://commons.apache.org/email">Apache
     * Commons Mail package</a> {@link HtmlEmail} object. This can be used to work with legacy
	 * code as well as with direct access to SMTP servers (as in fail-over mail messages
	 * for internal use). 
     * @return An Apache Commons Mail {@link HtmlEmail} object.
     * @throws EmailException Indicates an error when configuring the HtmlEmail object.
     */
    public HtmlEmail asHtmlEmail() throws EmailException {
    	HtmlEmail email = new HtmlEmail();

        if (!StringUtils.isBlank(_from)) {
            email.setFrom(_from);
        }
        if (_headers.size() > 0) {
            final Map<String, String> converted = new HashMap<>();
            for (final String key : _headers.keySet()) {
                converted.put(key, Joiner.on(", ").join(_headers.get(key)));
            }
            email.setHeaders(converted);
        }
        if (!StringUtils.isBlank(_onBehalfOf)) {
            email.addHeader("Sender", _onBehalfOf);
        }
        if (_tos.size() > 0) {
            email.setTo(convertToInternetAddresses(_tos));
        }
        if (_ccs.size() > 0) {
            email.setCc(convertToInternetAddresses(_ccs));
        }
        if (_bccs.size() > 0) {
            email.setBcc(convertToInternetAddresses(_bccs));
        }
        if (!StringUtils.isBlank(_subject)) {
            email.setSubject(_subject);
        }
        if (!StringUtils.isBlank(_html)) {
            email.setHtmlMsg(_html);
        }
        if (!StringUtils.isBlank(_text)) {
            email.setTextMsg(_text);
        }
        if (_attachments.size() > 0) {
    		for (Map.Entry<String, File> entry : _attachments.entrySet()) {
    			String key = entry.getKey();
    			FileSystemResource resource = new FileSystemResource(entry.getValue());
    			try {
					email.attach(resource.getURL(), key, "");
				} catch (IOException exception) {
					_log.error("Got an error retrieving the attachment: " + key, exception);
				}
    		}
    	}
    	
    	return email;
    }

    private Collection<InternetAddress> convertToInternetAddresses(final List<String> strings) {
        List<InternetAddress> addresses = new ArrayList<>();
        for (final String string : strings) {
            try {
                addresses.add(new InternetAddress(string));
            } catch (AddressException e) {
                _log.info("Address " + string + " is an");
            }
        }
        return addresses;
    }

    /**
     * This method converts a value of type <b>List<String></b>, <b>String[]</b>,
     * or <b>String</b> into a <b>List<String></b>. If the submitted object is not
     * of one of these types, this just calls the {@link Object#toString()} method
     * on the object, which may lead to unexpected results.
     * @param value The value to convert.
     * @return The value(s) in the value parameter converted into a <b>List<String></b>.
     */
    @SuppressWarnings("unchecked")
    private List<String> convertObjectToStringList(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof List) {
            return (List<String>) value;
        } else if (value instanceof String[]) {
            return Arrays.asList((String[]) value);
        } else if (value instanceof String) {
            return Collections.singletonList((String) value);
        }
        return Collections.singletonList(value.toString());
    }

    /**
     * Converts from a map of objects to a map of file objects. Effectively, this supports a map of File objects
     * along with String and URI objects, which are converted to File objects in a straightforward way. Any other
     * object type will result in an {@link InvalidMailAttachmentException} being thrown.
     * @param attachments The mixed bag of attachments represented by file, URI, and strings.
     * @return A normalized map of files.
     * @throws InvalidMailAttachmentException Thrown when a non-standard object is passed as a file.
     */
    private Map<String, File> convertGenericAttachmentMap(Map<String, Object> attachments) throws InvalidMailAttachmentException {
        Map<String, File> fileAttachments = new HashMap<>();
        for (Map.Entry<String, Object> entry : attachments.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (File.class.isAssignableFrom(value.getClass())) {
                fileAttachments.put(entry.getKey(), (File) value);
            } else if (String.class.isAssignableFrom(value.getClass())) {
                File file = new File((String) value);
                fileAttachments.put(entry.getKey(), file);
            } else if (URI.class.isAssignableFrom(value.getClass())) {
                File file = new File((URI) value);
                fileAttachments.put(entry.getKey(), file);
            } else {
                throw new InvalidMailAttachmentException("I don't know what this is: " + value);
            }
        }
        return fileAttachments;
    }

    private static final Logger _log = LoggerFactory.getLogger(MailMessage.class);

    private String _from;
    private String _onBehalfOf;
    private List<String> _tos = new ArrayList<>();
    private List<String> _ccs = new ArrayList<>();
    private List<String> _bccs = new ArrayList<>();
    private String _subject;
    private String _html;
    private String _text;
    private Map<String, List<String>> _headers = new HashMap<>();
    private Map<String, File> _attachments;
}
