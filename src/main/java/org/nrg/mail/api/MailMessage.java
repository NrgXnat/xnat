/**
 * MailMessage
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.mail.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.nrg.mail.services.MailService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * This class provides a convenient encapsulation of mail message functionality that
 * can be used with NRG {@link MailService} implementations.
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
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
    }

    public MailMessage(String from, String onBehalfOf, List<String> tos, List<String> ccs, List<String> bccs, String subject, String html, String text, Map<String, FileSystemResource> attachments) {
        _from = from;
        _onBehalfOf = onBehalfOf;
        _tos = tos;
        _ccs = ccs;
        _bccs = bccs;
        _subject = subject;
        _html = html;
        _text = text;
        _attachments = attachments;
    }

    @SuppressWarnings("unchecked")
    public MailMessage(Map<String, Object> properties) {
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
        if (properties.containsKey(PROP_ATTACHMENTS)) {
            _attachments = (Map<String, FileSystemResource>) properties.get(PROP_ATTACHMENTS);
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
            values = new ArrayList<String>();
            _headers.put(header, values);
        }
        values.add(value);
    }

    public Map<String, ? extends Resource> getAttachments() {
        return _attachments;
    }

    public void setAttachments(Map<String, FileSystemResource> attachments) {
        _attachments = attachments;
    }

    public void addAttachment(String id, FileSystemResource attachment) {
        _attachments.put(id, attachment);
    }

    public SimpleMailMessage asSimpleMailMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
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
            for (Map.Entry<String, ? extends Resource> attachment : _attachments.entrySet()) {
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

    	if (!StringUtils.isBlank(_from)) { email.setFrom(_from); }
    	if (_headers.size() > 0) { email.setHeaders(_headers); }
    	if (!StringUtils.isBlank(_onBehalfOf)) { email.addHeader("Sender", _onBehalfOf); }
    	if (_tos.size() > 0) { email.setTo(_tos); }
    	if (_ccs.size() > 0) { email.setCc(_ccs); }
    	if (_bccs.size() > 0) { email.setBcc(_bccs); }
    	if (!StringUtils.isBlank(_subject)) { email.setSubject(_subject); }
    	if (!StringUtils.isBlank(_html)) { email.setHtmlMsg(_html); }
    	if (!StringUtils.isBlank(_text)) { email.setTextMsg(_text); }
    	if (_attachments.size() > 0) {
    		for (Map.Entry<String, FileSystemResource> entry : _attachments.entrySet()) {
    			String key = entry.getKey();
    			FileSystemResource resource = entry.getValue();
    			try {
					email.attach(resource.getURL(), key, "");
				} catch (IOException exception) {
					_log.error("Got an error retrieving the attachment: " + key, exception);
				}
    		}
    	}
    	
    	return email;
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
            return Arrays.asList(new String[] {(String) value});
        }
        return Arrays.asList(new String[] {value.toString()});
    }

    private static final Log _log = LogFactory.getLog(MailMessage.class);

    private String _from;
    private String _onBehalfOf;
    private List<String> _tos = new ArrayList<String>();
    private List<String> _ccs = new ArrayList<String>();
    private List<String> _bccs = new ArrayList<String>();
    private String _subject;
    private String _html;
    private String _text;
    private Map<String, List<String>> _headers = new HashMap<String, List<String>>();
    private Map<String, FileSystemResource> _attachments = new HashMap<String, FileSystemResource>();
}
