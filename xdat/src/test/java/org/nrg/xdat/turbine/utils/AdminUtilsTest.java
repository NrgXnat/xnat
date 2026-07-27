/*
 * core: org.nrg.xdat.turbine.utils.AdminUtilsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.utils;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.nrg.mail.api.NotificationType;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Tests the recipient resolution for the "Copy Administrator On Notifications" setting: when enabled, the copy
 * must go to the email address of the primary admin user ({@link Users#getAdminUser()}), not the site admin email.
 */
public class AdminUtilsTest {
    private static final String SITE_ADMIN_EMAIL    = "siteadmin@example.test";
    private static final String PRIMARY_ADMIN_EMAIL = "primaryadmin@example.test";

    @Test
    public void sendsNothingWhenCopyAdminDisabled() {
        assertNull(resolveRecipient(false, primaryAdmin(PRIMARY_ADMIN_EMAIL), SITE_ADMIN_EMAIL));
    }

    @Test
    public void copiesPrimaryAdminEmailNotSiteAdminEmail() {
        // Default installations subscribe the site admin email to every notification type, so the primary
        // admin user must still receive a copy when their email differs from the subscriber list.
        assertEquals(PRIMARY_ADMIN_EMAIL, resolveRecipient(true, primaryAdmin(PRIMARY_ADMIN_EMAIL), SITE_ADMIN_EMAIL));
    }

    @Test
    public void skipsCopyWhenPrimaryAdminAlreadySubscribed() {
        assertNull(resolveRecipient(true, primaryAdmin(PRIMARY_ADMIN_EMAIL), SITE_ADMIN_EMAIL, PRIMARY_ADMIN_EMAIL));
    }

    @Test
    public void copiesWhenSubscriberMerelyContainsPrimaryAdminEmailAsSubstring() {
        // A subscriber like siteadmin@example.test must not suppress the copy to admin@example.test
        // just because the latter is a substring of the former.
        assertEquals(PRIMARY_ADMIN_EMAIL, resolveRecipient(true, primaryAdmin(PRIMARY_ADMIN_EMAIL), "x" + PRIMARY_ADMIN_EMAIL));
    }

    @Test
    public void skipsCopyWhenPrimaryAdminSubscribedWithDifferentCase() {
        assertNull(resolveRecipient(true, primaryAdmin(PRIMARY_ADMIN_EMAIL), PRIMARY_ADMIN_EMAIL.toUpperCase()));
    }

    @Test
    public void fallsBackToSiteAdminEmailWhenPrimaryAdminUnavailable() {
        assertEquals(SITE_ADMIN_EMAIL, resolveRecipient(true, () -> null, PRIMARY_ADMIN_EMAIL));
    }

    @Test
    public void fallsBackToSiteAdminEmailWhenPrimaryAdminEmailBlank() {
        assertEquals(SITE_ADMIN_EMAIL, resolveRecipient(true, primaryAdmin(" "), PRIMARY_ADMIN_EMAIL));
    }

    @Test
    public void skipsFallbackWhenSiteAdminEmailAlreadySubscribed() {
        assertNull(resolveRecipient(true, () -> null, SITE_ADMIN_EMAIL));
    }

    @Test
    public void fallsBackToSiteAdminEmailWhenPrimaryAdminLookupFails() {
        // A misconfigured primaryAdminUsername makes XnatUserProvider.get() throw: notification
        // sending must survive that and fall back to the site admin email.
        assertEquals(SITE_ADMIN_EMAIL, resolveRecipient(true, () -> {
            throw new RuntimeException("User with name aaf-user could not be found.");
        }, PRIMARY_ADMIN_EMAIL));
    }

    private static Supplier<UserI> primaryAdmin(final String email) {
        final UserI user = mock(UserI.class);
        when(user.getEmail()).thenReturn(email);
        return () -> user;
    }

    private static String resolveRecipient(final boolean copyEnabled, final Supplier<UserI> primaryAdmin, final String... subscribers) {
        try (final MockedStatic<XDAT> xdat = mockStatic(XDAT.class);
             final MockedStatic<Users> users = mockStatic(Users.class)) {
            final NotificationsPreferences notifications = mock(NotificationsPreferences.class);
            when(notifications.getCopyAdminOnNotifications()).thenReturn(copyEnabled);
            final SiteConfigPreferences siteConfig = mock(SiteConfigPreferences.class);
            when(siteConfig.getAdminEmail()).thenReturn(SITE_ADMIN_EMAIL);

            xdat.when(XDAT::getNotificationsPreferences).thenReturn(notifications);
            xdat.when(XDAT::getSiteConfigPreferences).thenReturn(siteConfig);
            xdat.when(() -> XDAT.getSubscriberEmails(NotificationType.Error)).thenReturn(new HashSet<>(Arrays.asList(subscribers)));
            users.when(Users::getAdminUser).thenAnswer(invocation -> primaryAdmin.get());

            return AdminUtils.getAdminNotificationCopyRecipient(NotificationType.Error);
        }
    }
}
