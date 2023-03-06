package org.nrg.xnat.services.customfields;

import com.fasterxml.jackson.databind.JsonNode;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.List;

public interface CustomFieldService {
    JsonNode getFields(ItemI item);

    JsonNode getFields(ItemI item, List<String> keys);

    JsonNode getFieldValue(ItemI item, String key) throws NotFoundException;

    JsonNode setFields(UserI user, ItemI item, JsonNode fields) throws NrgServiceException, InsufficientPrivilegesException;

    JsonNode removeField(UserI user, ItemI item, String key) throws NrgServiceException, InsufficientPrivilegesException, NotFoundException;
}
