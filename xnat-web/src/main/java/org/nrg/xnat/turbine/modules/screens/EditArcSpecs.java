/*
 * org.nrg.xnat.turbine.modules.screens.EditArcSpecs
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import java.util.List;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.mail.api.NotificationType;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.entities.Category;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.entities.Subscription;
import org.nrg.notify.services.NotificationService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.ArcArchivespecificationNotificationTypeI;
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.om.ArcArchivespecificationNotificationType;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.modules.screens.AdminScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

public class EditArcSpecs extends AdminScreen {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocitySecureScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        ArcArchivespecification arcSpec = ArcSpecManager.GetInstance();
        if (arcSpec==null){
            arcSpec = ArcSpecManager.initialize((UserI) TurbineUtils.getUser(data));
        }

        checkNotifications(arcSpec);

        context.put("idLabelMap", NotificationType.getIdLabelMap());
        context.put("arc", arcSpec);
    }

    /**
     * Checks whether site-side notifications exist and initializes them if not.
     *
     * @param arcSpec The archive specification object.
     * @throws Exception
     */
    private void checkNotifications(ArcArchivespecification arcSpec) throws Exception {
        // Check whether any notification types already exist and clear them if so.
        clearArcSpecNotifications(arcSpec);

        for (NotificationType type : NotificationType.values()) {
            Definition definition = retrieveSiteEventDefinition(type);
            List<Subscription> subscriptions = getNotificationService().getSubscriptionService().getSubscriptionsForDefinition(definition);

            ArcArchivespecificationNotificationTypeI typeObj = new ArcArchivespecificationNotificationType();
            typeObj.setNotificationType(type.id());
            if (subscriptions == null || subscriptions.size() == 0) {
                typeObj.setEmailAddresses(getSiteAdminAccount(arcSpec) + " <" + arcSpec.getSiteAdminEmail() + ">");
            } else {
                StringBuilder buffer = new StringBuilder();
                boolean isFirst = true;
                for (Subscription subscription : subscriptions) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        buffer.append(", ");
}
                    buffer.append(subscription.getSubscriber());
                }
                typeObj.setEmailAddresses(buffer.toString());
            }
            arcSpec.addNotificationTypes_notificationType(typeObj);
        }
    }

    /**
     * @param arcSpec
     */
    private void clearArcSpecNotifications(ArcArchivespecification arcSpec) {
        List<ArcArchivespecificationNotificationTypeI> notificationTypes;
        while ((notificationTypes = arcSpec.getNotificationTypes_notificationType()) != null && notificationTypes.size() > 0) {
            arcSpec.removeNotificationTypes_notificationType(0);
        }
    }

    /**
     * @param type The type for which the definition and its associated category should be created or retrieved.
     * @return The existing or newly created definition.
     */
    private Definition retrieveSiteEventDefinition(NotificationType type) {
        Category category = getNotificationService().getCategoryService().getCategoryByScopeAndEvent(CategoryScope.Site, type.toString());
        if (category == null) {
            category = initializeSiteEventCategory(type.toString());
        }
        List<Definition> definitions = getNotificationService().getDefinitionService().getDefinitionsForCategory(category);
        Definition definition;
        if (definitions == null || definitions.size() == 0) {
            definition = initializeSiteEventDefinition(category);
        } else {
            definition = definitions.get(0);
        }
        return definition;
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

    /**
     * @param event The event for which a category should be created.
     * @return The newly created category object.
     */
    private Category initializeSiteEventCategory(String event) {
        Category category = getNotificationService().getCategoryService().newEntity();
        category.setScope(CategoryScope.Site);
        category.setEvent(event);
        getNotificationService().getCategoryService().create(category);
        return category;
    }


    /**
     * @param category The category for which a definition should be created.
     * @return The newly created definition object.
     */
    private Definition initializeSiteEventDefinition(Category category) {
        Definition definition = getNotificationService().getDefinitionService().newEntity();
        definition.setCategory(category);
        getNotificationService().getDefinitionService().create(definition);
        return definition;
    }

    /**
     * @param arcSpec
     * @return
     */
    private String getSiteAdminAccount(ArcArchivespecification arcSpec) {
        List<UserI> users = Users.getUsersByEmail(arcSpec.getSiteAdminEmail());
        if (users == null || users.size() == 0) {
            throw new RuntimeException("Can't find anything for the site admin email account! It must be associated with a user account: " + arcSpec.getSiteAdminEmail());
        }
        return users.get(0).getLogin();
    }

    private NotificationService _notificationService;
}
