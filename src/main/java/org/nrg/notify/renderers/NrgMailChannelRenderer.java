/*
 * NrgMailChannelRenderer
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 30, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.renderers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.nrg.mail.api.MailMessage;
import org.nrg.mail.exceptions.InvalidMailAttachmentException;
import org.nrg.mail.services.MailService;
import org.nrg.notify.entities.Notification;
import org.nrg.notify.entities.Subscription;
import org.nrg.notify.exceptions.ChannelRendererProcessingException;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("unused")
public class NrgMailChannelRenderer implements ChannelRenderer {
    public static final String DEFAULT_FORMAT = "text/html";

    @Autowired
    public NrgMailChannelRenderer(final MailService mailService) {
        _mailService = mailService;
    }

    /**
     * Renders the submitted notification to the submitted subscription.
     * @throws ChannelRendererProcessingException An error occurred while rendering the notification.
     * @see ChannelRenderer#render(Subscription, Notification)
     */
    @Override
    public void render(Subscription subscription, Notification notification) throws ChannelRendererProcessingException {
        render(subscription, notification, DEFAULT_FORMAT);
    }

    /**
     * Renders the submitted notification to the submitted subscription.
     * @throws ChannelRendererProcessingException An error occurred while rendering the notification.
     * @see ChannelRenderer#render(Subscription, Notification)
     */
    @Override
    public void render(Subscription subscription, Notification notification, String format) throws ChannelRendererProcessingException {
        try {
            // TODO: For now, this is only supporting JSON. This should actually branch on notification.getParameterFormat();
            Map<String, Object> parameters = MAPPER.readValue(notification.getParameters(), TYPEREF_HASHMAP_STRING_OBJECT);
            parameters.put(MailMessage.PROP_FROM, _fromAddress);
            parameters.put(MailMessage.PROP_SUBJECT, formatSubject((String) parameters.get(MailMessage.PROP_SUBJECT)));
            parameters.put(MailMessage.PROP_TOS, subscription.getSubscriber().getEmailList());
            if (!StringUtils.isBlank(_onBehalfOf)) {
                parameters.put(MailMessage.PROP_ON_BEHALF_OF, _onBehalfOf);
            }

            _mailService.sendMessage(new MailMessage(parameters));
        } catch (IOException exception) {
            throw new ChannelRendererProcessingException("An error occurred processing the notification parameters in format: " + notification.getParameterFormat(), exception);
        } catch (MessagingException exception) {
            throw new ChannelRendererProcessingException("An error occurred sending the mail message.", exception);
        } catch (InvalidMailAttachmentException exception) {
            throw new ChannelRendererProcessingException("The mail message was associated with an invalid attachment.", exception);
        }
    }

    /**
     * @return Returns the mailService property.
     */
    public MailService getMailService() {
        return _mailService;
    }

    /**
     * @param fromAddress Sets the fromAddress property.
     */
    public void setFromAddress(String fromAddress) {
        _fromAddress = fromAddress;
    }

    /**
     * @return Returns the fromAddress property.
     */
    @SuppressWarnings("unused")
    public String getFromAddress() {
        return _fromAddress;
    }

    /**
     * @param onBehalfOf Sets the onBehalfOf property.
     */
    @SuppressWarnings("unused")
    public void setOnBehalfOf(String onBehalfOf) {
        _onBehalfOf = onBehalfOf;
    }

    /**
     * @return Returns the onBehalfOf property.
     */
    @SuppressWarnings("unused")
    public String getOnBehalfOf() {
        return _onBehalfOf;
    }

    /**
     * @param subjectPrefix Sets the subjectPrefix property.
     */
    public void setSubjectPrefix(String subjectPrefix) {
        _subjectPrefix = subjectPrefix;
    }

    /**
     * @return Returns the subjectPrefix property.
     */
    @SuppressWarnings("unused")
    public String getSubjectPrefix() {
        return _subjectPrefix;
    }

    /**
     * Formats the subject with the subject prefix if set, to default value otherwise.
     * @param subject    The notification subject to be formatted.
     * @return The formatted notification subject.
     */
    private String formatSubject(String subject) {
        if (StringUtils.isBlank(subject)) {
            if (StringUtils.isBlank(_subjectPrefix)) {
                subject = "NRG Notification";
            } else {
                subject = "[" + _subjectPrefix + "]";
            }
        } else {
            if (StringUtils.isBlank(_subjectPrefix)) {
                subject = "[NRG] " + subject;
            } else {
                subject = "[" + _subjectPrefix + "] " + subject;
            }
        }
        return subject;
    }

    private static final TypeReference<HashMap<String, Object>> TYPEREF_HASHMAP_STRING_OBJECT = new TypeReference<HashMap<String, Object>>() {};
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final MailService _mailService;

    private String _fromAddress;
    private String _onBehalfOf;
    private String _subjectPrefix;
}
