/**
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

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.nrg.mail.api.MailMessage;
import org.nrg.mail.services.MailService;
import org.nrg.notify.entities.Notification;
import org.nrg.notify.entities.Subscription;
import org.nrg.notify.exceptions.ChannelRendererProcessingException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public class NrgMailChannelRenderer implements ChannelRenderer {
    public static final String DEFAULT_FORMAT = "text/html";

    /**
     * Renders the submitted notification to the submitted subscription.
     * @throws ChannelRendererProcessingException 
     * @see ChannelRenderer#render(Subscription, Notification)
     */
    @Override
    public void render(Subscription subscription, Notification notification) throws ChannelRendererProcessingException {
        render(subscription, notification, DEFAULT_FORMAT);
    }

    /**
     * Renders the submitted notification to the submitted subscription.
     * @throws ChannelRendererProcessingException 
     * @see ChannelRenderer#render(Subscription, Notification)
     */
    @Override
    public void render(Subscription subscription, Notification notification, String format) throws ChannelRendererProcessingException {
        try {
            // TODO: For now, this is only supporting JSON. This should actually branch on notification.getParameterFormat();
            Map<String, Object> parameters = new ObjectMapper().readValue(notification.getParameters(), new TypeReference<HashMap<String, Object>>() {});
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
            throw new ChannelRendererProcessingException("An error occurred sending the mail message", exception);
        }
    }

    /**
     * @param mailService Sets the mailService property.
     */
    public void setMailService(MailService mailService) {
        _mailService = mailService;
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
    public String getFromAddress() {
        return _fromAddress;
    }

    /**
     * @param onBehalfOf Sets the onBehalfOf property.
     */
    public void setOnBehalfOf(String onBehalfOf) {
        _onBehalfOf = onBehalfOf;
    }

    /**
     * @return Returns the onBehalfOf property.
     */
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
    public String getSubjectPrefix() {
        return _subjectPrefix;
    }

    /**
     * @param parameters
     * @return
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

    @Autowired(required = false)
    private MailService _mailService;

    private String _fromAddress;
    private String _onBehalfOf;
    private String _subjectPrefix;
}
