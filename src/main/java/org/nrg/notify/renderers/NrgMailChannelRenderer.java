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

import javax.inject.Inject;

import org.codehaus.jackson.map.ObjectMapper;
import org.nrg.mail.services.MailService;
import org.nrg.notify.entities.Notification;
import org.nrg.notify.entities.Subscription;
import org.nrg.notify.exceptions.ChannelRendererProcessingException;

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
        subscription.getDefinition();
        subscription.getSubscriber();
        try {
            // TODO: For now, this is only supporting JSON. This should actually branch on notification.getParameterFormat();
            Map<String,Object> parameters = new ObjectMapper().readValue(notification.getParameters(), HashMap.class);
            // _mailService.
        } catch (IOException exception) {
            throw new ChannelRendererProcessingException("An error occurred processing the notification parameters in format: " + notification.getParameterFormat(), exception);
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

    private MailService _mailService;
}
