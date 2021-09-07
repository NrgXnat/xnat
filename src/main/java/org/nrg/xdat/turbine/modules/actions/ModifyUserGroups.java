/*
 * core: org.nrg.xdat.turbine.modules.actions.ModifyUserGroups
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.actions;

import org.apache.commons.lang.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ModifyUserGroups extends SecureAction {

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        UserI newUser = Users.createUser(TurbineUtils.GetDataParameterHash(data));
        UserI oldUser = Users.getUser(newUser.getLogin());
        UserI authenticatedUser = XDAT.getUserDetails();

        if (Roles.isSiteAdmin(authenticatedUser)) {
            PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, authenticatedUser, Users.getUserDataType(), oldUser.getID().toString(), PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified user settings"));
            EventMetaI ci = wrk.buildEvent();

            try {
                Map<String, UserGroupI> oldGroups = Groups.getGroupsForUser(oldUser);
                List<String> newGroups = Groups.getGroupIdsForUser(newUser);

                //remove old groups no longer needed
                for (UserGroupI uGroup : oldGroups.values()) {
                    if (!Groups.isMember(newUser, uGroup.getId())) {
                        Groups.removeUserFromGroup(oldUser, authenticatedUser, uGroup.getId(), ci);
                    }
                }

                if (newUser.isEnabled() && !oldUser.isEnabled()) {
                    //Enable the UserAuth as well
                    List<XdatUserAuth> auths = XDAT.getXdatUserAuthService().getAllWithDisabled();
                    for(XdatUserAuth auth : auths){
                        if(StringUtils.equals(newUser.getUsername(),auth.getXdatUsername()) && StringUtils.equals(auth.getAuthMethod(),XdatUserAuthService.LOCALDB)){
                            auth.setEnabled(true);
                            auth.setPasswordUpdated(new java.util.Date());
                            XDAT.getXdatUserAuthService().update(auth);
                        }
                    }
                }

                try {
                    Users.save(newUser, authenticatedUser, false, ci);

                    for (String group_id : newGroups) {
                        if (!Groups.isMember(oldUser, group_id)) {
                            Groups.addUserToGroup(group_id, newUser, authenticatedUser, ci);
                        }
                    }

                    PersistentWorkflowUtils.complete(wrk, ci);
                } catch (InvalidPermissionException e) {
                    PersistentWorkflowUtils.fail(wrk, ci);
                    if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
                        String body = XDAT.getNotificationsPreferences().getEmailMessageUnauthorizedDataAttempt();
                        String type = "attempted. User attempted to modify a user account other than thier own. This typically requires tampering with the HTTP form submission process.";
                        body = body.replaceAll("TYPE", type);
                        body = body.replaceAll("USER_DETAILS", "");
                        notifyAdmin(authenticatedUser, data, 403, "Possible Authorization Bypass event", body);
                    }
                    return;
                } catch (Exception e) {
                    PersistentWorkflowUtils.fail(wrk, ci);
                    logger.error("Error Storing User", e);
                    return;
                }

            } catch (Exception e) {
                PersistentWorkflowUtils.fail(wrk, ci);
                logger.error("Error Storing User", e);
            }

            if (!newUser.isEnabled() && oldUser.isEnabled()) {
                //When a user is disabled, deactivate all their AliasTokens
                try {
                    XDAT.getContextService().getBean(AliasTokenService.class).deactivateAllTokensForUser(newUser.getLogin());
                } catch (Exception e) {
                    logger.error("", e);
                }
            } else if (newUser.isEnabled() && !oldUser.isEnabled()) {
                //When a user is enabled, notify the administrator
                try {
                    AdminUtils.sendNewUserEmailMessage(oldUser.getUsername(), oldUser.getEmail());
                } catch (Exception e) {
                    logger.error("", e);
                }
            }

            redirectToReportScreen(DisplayItemAction.GetReportScreen(Users.getUserDataType()), (ItemI) Users.getUser(oldUser.getLogin()), data);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ModifyUserGroups.class);
}
