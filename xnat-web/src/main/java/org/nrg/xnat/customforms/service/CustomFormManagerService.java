package org.nrg.xnat.customforms.service;

import com.fasterxml.jackson.databind.JsonNode;
import javassist.NotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.customforms.exceptions.CustomVariableNameClashException;
import org.nrg.xnat.customforms.exceptions.InsufficientPermissionsException;
import org.nrg.xnat.customforms.pojo.ComponentPojo;
import org.nrg.xnat.customforms.pojo.SubmissionPojo;
import org.nrg.xnat.customforms.pojo.UserOptionsPojo;
import org.nrg.xnat.customforms.pojo.XnatFormsIOEnv;
import org.nrg.xnat.customforms.pojo.formio.FormAppliesToPoJo;
import org.nrg.xnat.customforms.pojo.formio.PseudoConfiguration;
import org.nrg.xnat.customforms.pojo.formio.RowIdentifier;
import org.nrg.xnat.entities.CustomVariableForm;

import javax.annotation.Nullable;
import java.util.List;

public interface CustomFormManagerService {
    boolean enableForm(UserI user, String formAppliesToId) throws InsufficientPermissionsException;

    boolean disableForm(UserI user, String formAppliesId) throws Exception;

    boolean modifyDisplayOrder(UserI user, Integer zIndex, String formIdStr) throws Exception;

    String deleteForm(UserI user, String formAppliesId) throws Exception;

    boolean optProjectsIntoForm(UserI user, RowIdentifier rowIdentifier, List<String> projects) throws IllegalArgumentException;

    boolean promoteForm(UserI user, List<FormAppliesToPoJo> formAppliesToPoJos) throws NotFoundException, CustomVariableNameClashException;

    boolean promoteForm(UserI user, String formAppliesToId) throws NotFoundException, CustomVariableNameClashException;

    boolean optOutOfForm(UserI user, String formAppliesToId, List<String> projectIds) throws InsufficientPermissionsException, IllegalArgumentException;

    String save(SubmissionPojo data, final JsonNode formDefinition, final UserI user) throws InsufficientPermissionsException;
    String save(UserI user,
                UserOptionsPojo userOptionsPojo,
                List<ComponentPojo> entityIds,
                JsonNode proposed,
                RowIdentifier existingFormPrimaryKey
    ) throws CustomVariableNameClashException;

    List<PseudoConfiguration> getAllCustomForms(final String projectId);

    @Nullable
    String getCustomForm(UserI user, String xsiType, String id, String projectId,
                         String visitId, String subtype, boolean appendPreviousNextButtons) throws Exception;

    boolean checkCustomFormForData(RowIdentifier rowId) throws Exception;

    void createNew(CustomVariableForm form, UserI user,
                   UserOptionsPojo userOptions,
                   String entityId, String status);

    XnatFormsIOEnv getFormsEnvironment();
}
