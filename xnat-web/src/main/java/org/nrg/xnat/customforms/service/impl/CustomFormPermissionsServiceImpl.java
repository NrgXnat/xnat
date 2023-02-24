package org.nrg.xnat.customforms.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.customforms.exceptions.InsufficientPermissionsException;
import org.nrg.xnat.customforms.service.CustomFormPermissionsService;
import org.nrg.xnat.customforms.service.CustomVariableAppliesToService;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.entities.CustomVariableAppliesTo;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Slf4j
public class CustomFormPermissionsServiceImpl implements CustomFormPermissionsService {

    private final CustomVariableAppliesToService selectionService;

    @Autowired
    public CustomFormPermissionsServiceImpl(final CustomVariableAppliesToService selectionService) {
        this.selectionService = selectionService;
    }

    @Transactional
    public boolean isUserAuthorized(final UserI user, final List<CustomVariableFormAppliesTo> customVariableFormAppliesTos) throws InsufficientPermissionsException {
        if (isUserAdminOrDataManager(user)) {
            return true;
        }
        for (CustomVariableFormAppliesTo customVariableAppliesTo : customVariableFormAppliesTos) {
            isUserProjectOwner(user, customVariableAppliesTo);
        }
        return true;
    }

    public boolean isUserAuthorized(final UserI user, final CustomVariableFormAppliesTo customVariableFormAppliesTo) throws InsufficientPermissionsException {
        if (isUserAdminOrDataManager(user)) {
            return true;
        }
        isUserProjectOwner(user, customVariableFormAppliesTo);
        return true;
    }

    public boolean isUserProjectOwner(final UserI user, final CustomVariableFormAppliesTo customVariableFormAppliesTo) throws InsufficientPermissionsException {
        CustomVariableAppliesTo selection = customVariableFormAppliesTo.getCustomVariableAppliesTo();
        if (selection != null) {
            final String projectId = selection.getEntityId();
            if (selection.getScope().equals(Scope.Project) && projectId != null) {
                isUserProjectOwner(user, projectId);
            }
        }
        return true;
    }

    public boolean isUserProjectOwner(final UserI user, final String projectId) throws InsufficientPermissionsException {
        if (projectId == null) {
            return false;
        }
        if (!Permissions.isProjectOwner(user, projectId)) {
            throw new InsufficientPermissionsException("User is not an Admin/Form Data Manager or Owner of the Project");
        }
        return true;
    }

    public boolean isUserAdminOrDataManager(final UserI user) {
        return Roles.isSiteAdmin(user.getUsername()) || Roles.checkRole(user, CustomFormsConstants.DATAFORM_MANAGER_ROLE);
    }

}
