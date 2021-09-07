/*
 * web: org.nrg.xnat.turbine.modules.actions.SetArcSpecs
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.jetbrains.annotations.NotNull;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.api.SubscriberType;
import org.nrg.notify.entities.*;
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
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@SuppressWarnings("unused")
public class SetArcSpecs extends AdminAction {
    /**
     * Sets the arc specification attributes based on submitted data.
     *
     * @param data    The run-data for the request.
     * @param context The context for the request.
     */
    @Override
    public void doPerform(final RunData data, final Context context) throws Exception {
        final PopulateItem populater = PopulateItem.Populate(data, "arc:ArchiveSpecification", true);
        final XFTItem      item      = populater.getItem();
        item.setUser(XDAT.getUserDetails());

        final ArcArchivespecification arcSpec = new ArcArchivespecification(item);
        ArcSpecManager.save(arcSpec, newEventInstance(data, EventUtils.CATEGORY.SIDE_ADMIN, "Modified archive specifications."));

        final Channel channel = XDAT.getHtmlMailChannel();

        for (final ArcArchivespecificationNotificationTypeI type : arcSpec.getNotificationTypes_notificationType()) {
            final Definition                    definition    = getDefinitionForEvent(type.getNotificationType());
            final Map<Subscriber, Subscription> subscriptions = getNotificationService().getSubscriptionService().getSubscriberMapOfSubscriptionsForDefinition(definition);

            for (final Subscriber subscriber : getSubscribersFromAddresses(type.getEmailAddresses())) {
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
            if (!subscriptions.isEmpty()) {
                // Those are no longer wanted (they weren't specified in the submitted list), so let's remove those subscriptions.
                for (final Subscription subscription : subscriptions.values()) {
                    getNotificationService().getSubscriptionService().delete(subscription);
                }
            }
        }

        ArcSpecManager.Reset();
    }

    /**
     * Gets the site-wide notification definition for the specified event. If the definition already exists, this method
     * returns that. Otherwise, it creates a new definition for the event and returns that.
     *
     * @param event An event identifier.
     *
     * @return The definition associated with the submitted event.
     */
    private Definition getDefinitionForEvent(String event) {
        final Category         category    = getOrCreateCategory(event);
        final List<Definition> definitions = getNotificationService().getDefinitionService().getDefinitionsForCategory(category);
        if (!CollectionUtils.isEmpty(definitions)) {
            return definitions.get(0);
        }
        final Definition definition = getNotificationService().getDefinitionService().newEntity();
        definition.setCategory(category);
        getNotificationService().getDefinitionService().create(definition);
        return definition;
    }

    /**
     * Gets the site-wide category for the specified event. If the category already exists for the event, this method
     * returns that. Otherwise, it creates a new category for the event and returns that.
     *
     * @param event The event for which you want a category.
     *
     * @return The existing category if possible, otherwise a newly created category.
     */
    @NotNull
    private Category getOrCreateCategory(final String event) {
        Category category = getNotificationService().getCategoryService().getCategoryByScopeAndEvent(CategoryScope.Site, event);
        if (category == null) {
            category = getNotificationService().getCategoryService().newEntity();
            category.setScope(CategoryScope.Site);
            category.setEvent(event);
            getNotificationService().getCategoryService().create(category);
        }
        return category;
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
     *
     * @return A list of {@link Subscriber} objects representing those users.
     */
    private List<Subscriber> getSubscribersFromAddresses(String addresses) {
        final List<Subscriber> subscribers = new ArrayList<>();
        final List<String>     errors      = new ArrayList<>();
        for (String address : addresses.split("[\\s]*,[\\s]*")) {
            final String username;
            final String email;
            if (Users.isValidUsername(address)) {
                // Handle this as a username.
                username = address;
                try {
                    final UserI user = Users.getUser(username);
                    email = user.getEmail();
                } catch (UserNotFoundException e) {
                    errors.add(address);
                    log.error("User {} not found", username, e);
                    continue;
                } catch (UserInitException e) {
                    errors.add(address);
                    log.error("An error occurred trying to retrieve user {}", username, e);
                    continue;
                }
            } else if (Users.isValidEmail(address)) {
                // Handle this as an email.
                final List<? extends UserI> users = Users.getUsersByEmail(email = address);
                if (users == null || users.size() == 0) {
                    // If we didn't find the user, do something.
                    // TODO: Need to add users that aren't located to a list of error messages.
                    continue;
                }
                username = users.get(0).getLogin();
            } else if (Users.isValidUsernameAndEmail(address)) {
                final Pair<String, String> usernameAndEmail = Users.extractUsernameAndEmail(address);
                username = usernameAndEmail.getKey();
                email = usernameAndEmail.getValue();
            } else {
                // TODO: Need to add users that aren't located to a list of error messages.
                username = email = null;
            }

            if (username != null && email != null) {
                subscribers.add(getSubscriber(username, email));
            }
        }

        if (!errors.isEmpty()) {
            log.error("For some reason I couldn't resolve {} addresses: {}", errors.size(), String.join(", ", errors));
        }
        return subscribers;
    }

    private Subscriber getSubscriber(final String username, final String email) {
        return Optional.ofNullable(getNotificationService().getSubscriberService().getSubscriberByName(username)).orElseGet(() -> createSubscriber(username, email));
    }

    private Subscriber createSubscriber(final String username, final String email) {
        try {
            return getNotificationService().getSubscriberService().createSubscriber(username, email);
        } catch (DuplicateSubscriberException e) {
            // This shouldn't happen, since we just checked for the subscriber's existence.
            throw new RuntimeException("A totally unanticipated error occurred", e);
        }
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

    private NotificationService _notificationService;
}
