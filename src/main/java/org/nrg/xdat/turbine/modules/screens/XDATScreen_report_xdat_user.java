/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_report_xdat_user
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.mail.services.EmailRequestLogService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;

/**
 * @author Tim
 */
@SuppressWarnings("unused")
@Slf4j
public class XDATScreen_report_xdat_user extends AdminReport {
    public void finalProcessing(final RunData data, final Context context) {
        try {
            final UserI user = Users.getUser(item.getStringProperty("login"));
            context.put("allRoles", Roles.getRoles());
            context.put("userObject", user);
            context.put("userObjectHelper", UserHelper.getUserHelperService(user));
            context.put("allGroups", Groups.getAllGroups());

            // Does the user have any failed login attempts?
            context.put("hasFailedLoginAttempts", Lists.newArrayList(Iterables.filter(XDAT.getXdatUserAuthService().getUsersByXdatUsername(user.getUsername()), new Predicate<XdatUserAuth>() {
                @Override
                public boolean apply(final XdatUserAuth auth) {
                    return auth.getFailedLoginAttempts() > 0;
                }
            })).size() > 0);

            // Has the user been blocked from requesting emails? (Resend Email Verification / Reset password)
            context.put("emailRequestsBlocked", XDAT.getContextService().getBean(EmailRequestLogService.class).isEmailBlocked(user.getEmail()));
        } catch (Exception e) {
            log.error("An unexpected error occurred", e);
        }
    }
}

