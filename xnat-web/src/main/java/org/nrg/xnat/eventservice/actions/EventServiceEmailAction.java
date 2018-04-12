package org.nrg.xnat.eventservice.actions;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.nrg.mail.api.MailMessage;
import org.nrg.mail.services.MailService;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.ActionAttributeConfiguration;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.eventservice.services.SubscriptionDeliveryEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.ACTION_COMPLETE;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.ACTION_FAILED;

@Service
public class EventServiceEmailAction extends SingleActionProvider {
    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private static final String FROM_KEY    = "from";
    private static final String TO_KEY      = "to";
    private static final String CC_KEY      = "cc";
    private static final String BCC_KEY     = "bcc";
    private static final String SUBJECT_KEY = "subject";
    private static final String BODY_KEY    = "body";

    private final String displayName = "Email Action";
    private final String description = "Project owners and site administrators can send an email in response to events.";
    private Map<String, ActionAttributeConfiguration> attributes;
    private Boolean enabled = true;

    private MailService mailService;
    private SubscriptionDeliveryEntityService subscriptionDeliveryEntityService;

    @Autowired
    public EventServiceEmailAction(MailService mailService, SubscriptionDeliveryEntityService subscriptionDeliveryEntityService) {
        this.mailService = mailService;
        this.subscriptionDeliveryEntityService = subscriptionDeliveryEntityService;
    }


    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    @Override
    public Map<String, ActionAttributeConfiguration> getAttributes() {
        Map<String, ActionAttributeConfiguration> attributeConfigurationMap = new HashMap<>();
        attributeConfigurationMap.put(FROM_KEY,
                ActionAttributeConfiguration.builder()
                                            .description("Email originator.")
                                            .type("string")
                                            .defaultValue("")
                                            .required(true)
                                            .build());

        attributeConfigurationMap.put(TO_KEY,
                ActionAttributeConfiguration.builder()
                                            .description("Comma separated list of email recipients.")
                                            .type("string")
                                            .defaultValue("")
                                            .required(true)
                                            .build());

        attributeConfigurationMap.put(CC_KEY,
                ActionAttributeConfiguration.builder()
                                            .description("Comma separated list of email recipients.")
                                            .type("string")
                                            .defaultValue("")
                                            .required(false)
                                            .build());

        attributeConfigurationMap.put(BCC_KEY,
                ActionAttributeConfiguration.builder()
                                            .description("Comma separated list of email recipients.")
                                            .type("string")
                                            .defaultValue("")
                                            .required(false)
                                            .build());

        attributeConfigurationMap.put(SUBJECT_KEY,
                ActionAttributeConfiguration.builder()
                                            .description("Email message subject.")
                                            .type("string")
                                            .defaultValue("")
                                            .required(false)
                                            .build());

        attributeConfigurationMap.put(BODY_KEY,
                ActionAttributeConfiguration.builder()
                                            .description("Textual body of email.")
                                            .type("string")
                                            .defaultValue("")
                                            .required(true)
                                            .build());

        return attributeConfigurationMap;
    }

    @Override
    public void processEvent(EventServiceEvent event, SubscriptionEntity subscription, UserI user, final Long deliveryId) {

        log.debug("Attempting to send email with EventServiceEmailAction.");
        final Map<String,String> inputValues = subscription.getAttributes() != null ? subscription.getAttributes() : Maps.newHashMap();

        MailMessage mailMessage = new MailMessage();

        String from = inputValues.get(FROM_KEY);
        if(Strings.isNullOrEmpty(from)){
            failWithMessage(subscription,deliveryId, "Action missing \"from\" email attribute.");
            log.debug("Action missing \"from\" email attribute.");
            return;
        }else {
            mailMessage.setFrom(from);
        }

        List<String> toList = Splitter.on(CharMatcher.anyOf(";:, "))
                                      .trimResults().omitEmptyStrings()
                                      .splitToList(inputValues.get(TO_KEY) != null ? inputValues.get(TO_KEY) : "");
        if (toList == null || toList.isEmpty()) {
            failWithMessage(subscription, deliveryId, "Action missing email recipient as \"to\" attribute.");
            log.debug("Action missing email recipient as \"to\" attribute.");
            return;
        }
        mailMessage.setTos(toList);

        if(!Strings.isNullOrEmpty(inputValues.get(CC_KEY))) {
            List<String> ccList = Splitter.on(CharMatcher.anyOf(";:, "))
                                          .trimResults().omitEmptyStrings().splitToList(inputValues.get(CC_KEY));
            mailMessage.setCcs(ccList);
        }

        if(!Strings.isNullOrEmpty(inputValues.get(BCC_KEY))) {
            List<String> bccList = Splitter.on(CharMatcher.anyOf(";:, "))
                                           .trimResults().omitEmptyStrings().splitToList(inputValues.get(BCC_KEY));
            mailMessage.setBccs(bccList);
        }

        if(Strings.isNullOrEmpty(inputValues.get(SUBJECT_KEY))) {
            String subject = inputValues.get(SUBJECT_KEY);
            mailMessage.setSubject(subject);
        }

        String body = inputValues.get(BODY_KEY);
        if(Strings.isNullOrEmpty(body)){
            failWithMessage(subscription,deliveryId, "Action missing \"body\" email attribute.");
            log.debug("Action missing \"body\" email attribute.");
            return;
        }
        mailMessage.setText(body);

        try {
            mailService.sendMessage(mailMessage);
            subscriptionDeliveryEntityService.addStatus(deliveryId, ACTION_COMPLETE, new Date(), "Email action completed successfully.");
        } catch (MessagingException e) {
            log.error("Email service failed to send message. \n" + e.getMessage());
            failWithMessage(subscription,deliveryId,"Email service failed to send message. \nCheck configuration");
        }

        log.error("EventServiceLoggingAction called for RegKey " + subscription.getListenerRegistrationKey());

    }

    void failWithMessage(SubscriptionEntity subscription, Long deliveryId, String message){
        subscriptionDeliveryEntityService.addStatus(deliveryId, ACTION_FAILED, new Date(), "Email action failed: " + message);
    }

}
