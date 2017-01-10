/*
 * web: org.nrg.xnat.turbine.modules.actions.SetArcSpecs
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.api.SubscriberType;
import org.nrg.notify.entities.Category;
import org.nrg.notify.entities.Channel;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.entities.Subscriber;
import org.nrg.notify.entities.Subscription;
import org.nrg.notify.exceptions.DuplicateSubscriberException;
import org.nrg.notify.services.NotificationService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.ArcArchivespecificationNotificationTypeI;
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.turbine.modules.actions.AdminAction;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

public class SetArcSpecs extends AdminAction {
    static Logger logger = Logger.getLogger(SetArcSpecs.class);

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocitySecureAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        PopulateItem populater = PopulateItem.Populate(data,"arc:ArchiveSpecification",true);
        XFTItem item = populater.getItem();
        item.setUser(TurbineUtils.getUser(data));
        
        ArcArchivespecification arcSpec = new ArcArchivespecification(item);
        ArcSpecManager.save(arcSpec, newEventInstance(data, EventUtils.CATEGORY.SIDE_ADMIN, "Modified archive specifications."));
        
        Channel channel = XDAT.getHtmlMailChannel();

        List<ArcArchivespecificationNotificationTypeI> types = arcSpec.getNotificationTypes_notificationType();
        for (ArcArchivespecificationNotificationTypeI type : types) {
            List<Subscriber> subscribers = getSubscribersFromAddresses(type.getEmailAddresses());
            Definition definition = getDefinitionForEvent(type.getNotificationType());

            Map<Subscriber, Subscription> subscriptions = getNotificationService().getSubscriptionService().getSubscriberMapOfSubscriptionsForDefinition(definition);

            for (Subscriber subscriber : subscribers) {
                // If we don't have a subscription for this notification...
                if (!subscriptions.containsKey(subscriber)) {
                    // Create one.
                    getNotificationService().subscribe(subscriber, SubscriberType.User, definition, channel);
                    // But if we do have a subscription for this notification...
                } else {
                    // Remove it from the map.
                    subscriptions.remove(subscriber);
                }
            }

            // If there are any left-over subscriptions...
            if (subscriptions.size() > 0) {
                // Those are no longer wanted (they weren't specified in the submitted list), so let's remove those subscriptions.
                for (Subscription subscription : subscriptions.values()) {
                    getNotificationService().getSubscriptionService().delete(subscription);
                }
            }
        }

        ArcSpecManager.Reset();
    }

    /**
     * @param event An event identifier.
     * @return The definition associated with the submitted event.
     */
    private Definition getDefinitionForEvent(String event) {
        Category category = getNotificationService().getCategoryService().getCategoryByScopeAndEvent(CategoryScope.Site, event);
        if (category == null) {
            category = getNotificationService().getCategoryService().newEntity();
            category.setScope(CategoryScope.Site);
            category.setEvent(event);
            getNotificationService().getCategoryService().create(category);
        }
        Definition definition;
        List<Definition> definitions = getNotificationService().getDefinitionService().getDefinitionsForCategory(category);
        if (definitions == null || definitions.size() == 0) {
            definition = getNotificationService().getDefinitionService().newEntity();
            definition.setCategory(category);
            getNotificationService().getDefinitionService().create(definition);
        } else {
            definition = definitions.get(0);
        }
        return definition;
    }

    /**
     * Takes a comma-separated list of "email addresses" (which actually may include {@link UserI#getEmail() email addresses},
     * {@link UserI#getLogin() XDAT user names}, and a combination of the two in the format:
     * <p/>
     * <code><i>username</i> &lt;<i>email</i>&gt;</code>
     * <p/>
     * So for example, you may have something like this:
     * <p/>
     * <code>user1 &lt;user1@@xnat.org&gt;, user2, user3@xnat.org</code>
     * <p/>
     * Note that if any of the users aren't found, this method currently will have no indication other than returning fewer
     * users than are specified in the <b>emailAddresses</b> parameter.
     *
     * @param addresses The comma-separated list of usernames, email addresses, and combined IDs.
     * @return A list of {@link Subscriber} objects representing those users.
     */
    private List<Subscriber> getSubscribersFromAddresses(String addresses) {
        List<Subscriber> subscribers = new ArrayList<Subscriber>();
        for (String address : addresses.split("[\\s]*,[\\s]*")) {
            String username = null;
            String email = null;
            Matcher userMatcher = PATTERN_USERNAME.matcher(address);
            if (userMatcher.matches()) {
                // Handle this as a username.
                username = address;
                try {
					UserI user = Users.getUser(username);
					if (user != null) {
					    email = user.getEmail();
					} else {
					    // TODO: Need to add users that aren't located to a list of error messages.
					    continue;
					}
				} catch (UserNotFoundException e) {
					logger.error("",e);
				} catch (UserInitException e) {
					logger.error("",e);
				}
            } else {
                Matcher emailMatcher = PATTERN_EMAIL.matcher(address);
                if (emailMatcher.matches()) {
                    // Handle this as an email.
                    List<? extends UserI> users = Users.getUsersByEmail(email = address);
                    if (users == null || users.size() == 0) {
                        // If we didn't find the user, do something.
                        // TODO: Need to add users that aren't located to a list of error messages.
                        continue;
                    } else {
                        username = users.get(0).getLogin();
                    }
                } else {
                    Matcher combinedMatcher = PATTERN_COMBINED.matcher(address);
                    if (combinedMatcher.matches()) {
                        // Handle this as a combined. username will match first capture, email second capture (0 capture in regex is full expression).
                        username = combinedMatcher.group(1);
                        email = combinedMatcher.group(2);
                    } else {
                        // TODO: Need to add users that aren't located to a list of error messages.
                    }
                }
            }

            Subscriber subscriber = getNotificationService().getSubscriberService().getSubscriberByName(username);
            if (subscriber == null) {
                try {
                    subscriber = getNotificationService().getSubscriberService().createSubscriber(username, email);
                } catch (DuplicateSubscriberException exception) {
                    // This shouldn't happen, since we just checked for the subscriber's existence.
                }
            }

            subscribers.add(subscriber);
        }

        return subscribers;
    }

    /**
     * Gets the notification service instance.
     *
     * @return The notification service instance.
     */
    private NotificationService getNotificationService() {
        if (_notificationService == null) {
            _notificationService = XDAT.getNotificationService();
        }
        return _notificationService;
    }

    private static final String EXPRESSION_USERNAME = "[a-zA-Z][a-zA-Z0-9_-]{3,15}";
    private static final String EXPRESSION_EMAIL = "[_A-Za-z0-9-]+(?:\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(?:\\.[A-Za-z0-9]+)*(?:\\.[A-Za-z]{2,})";
    private static final String EXPRESSION_COMBINED = "(" + EXPRESSION_USERNAME + ")[\\s]*<(" + EXPRESSION_EMAIL + ")>";
    private static final Pattern PATTERN_USERNAME = Pattern.compile(EXPRESSION_USERNAME);
    private static final Pattern PATTERN_EMAIL = Pattern.compile(EXPRESSION_EMAIL);
    private static final Pattern PATTERN_COMBINED = Pattern.compile(EXPRESSION_COMBINED);

    private NotificationService _notificationService;
}
