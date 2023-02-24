package org.nrg.xnat.customforms.service;


import org.nrg.xft.security.UserI;
import org.nrg.xnat.customforms.exceptions.InsufficientPermissionsException;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;

import java.util.List;

public interface CustomFormPermissionsService {

    boolean isUserAuthorized(final UserI user, final CustomVariableFormAppliesTo customVariableFormAppliesTo) throws InsufficientPermissionsException;

    boolean isUserProjectOwner(final UserI user, final String projectId) throws InsufficientPermissionsException;

    boolean isUserProjectOwner(final UserI user, final CustomVariableFormAppliesTo customVariableFormAppliesTo) throws InsufficientPermissionsException;

    boolean isUserAdminOrDataManager(final UserI user);
}
