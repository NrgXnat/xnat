package org.nrg.xnat.services.customfields.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.services.customfields.CustomFieldService;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
public class DefaultCustomFieldService implements CustomFieldService {
    private final ObjectMapper objectMapper;

    public DefaultCustomFieldService(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode getFields(final ItemI item) {
        return getFieldsForItem(item, null);
    }

    @Override
    public JsonNode getFields(ItemI item, List<String> keys) {
        return getFieldsForItem(item, keys);
    }

    @Override
    public JsonNode getFieldValue(final ItemI item, final String key) throws NotFoundException {
        final JsonNode fields = getFieldsForItem(item);
        if (!fields.has(key)) {
            throw new NotFoundException("Field " + key + " does not exist.");
        }
        return fields.get(key);
    }

    @Override
    public JsonNode setFields(final UserI user, final ItemI item, final JsonNode newFields) throws NrgServiceException, InsufficientPrivilegesException {
        throwForInvalidEditPermissions(user, item);

        final ObjectNode currFields = (ObjectNode) getFieldsForItem(item);
        final Iterator<String> iterator = newFields.fieldNames();
        while (iterator.hasNext()) {
            final String key = iterator.next();
            currFields.set(key, newFields.get(key));
        }

        saveItem(setFieldsForItem(item, currFields), user);
        return getFieldsForItem(item);
    }

    @Override
    public JsonNode removeField(final UserI user, final ItemI item, String key) throws NrgServiceException, InsufficientPrivilegesException {
        throwForInvalidEditPermissions(user, item);

        final ObjectNode currFields = (ObjectNode) getFieldsForItem(item);
        currFields.remove(key);
        saveItem(setFieldsForItem(item, currFields), user);

        return getFieldsForItem(item);
    }

    private void throwForInvalidEditPermissions(final UserI user, final ItemI item) throws InsufficientPrivilegesException {
        try {
            if (!Permissions.canEdit(user, item)) {
                throw new InsufficientPrivilegesException(user.getUsername(), null == item ? "null" : item.getXSIType());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new InsufficientPrivilegesException(user.getUsername(), null == item ? "null" : item.getXSIType());
        }
    }

    private JsonNode getFieldsForItem(final ItemI item) {
        return getFieldsForItem(item, null);
    }

    private JsonNode getFieldsForItem(final ItemI item, final List<String> keys) {
        JsonNode fields;
        if (item instanceof XnatExperimentdata) {
            fields = ((XnatExperimentdata) item).getCustomFields();
        } else if (item instanceof XnatSubjectdata) {
            fields = ((XnatSubjectdata) item).getCustomFields();
        } else if (item instanceof XnatProjectdata) {
            fields = ((XnatProjectdata) item).getCustomFields();
        } else if (item instanceof XnatImagescandata) {
            fields = ((XnatImagescandata) item).getCustomFields();
        } else {
            throw new UnsupportedOperationException("Custom fields are not supported for "
                    + (null == item ? "null" : item.getXSIType()));
        }

        if (null == fields) {
            fields = objectMapper.createObjectNode();
        }

        return (null == keys || keys.isEmpty()) ? fields : getSubsetOfFields(fields, keys);
    }

    private JsonNode getSubsetOfFields(final JsonNode fields, final List<String> keys) {
        final ObjectNode subset = objectMapper.createObjectNode();
        for (String key : keys) {
            if (fields.has(key)) {
                subset.set(key, fields.get(key));
            }
        }
        return subset;
    }

    private ItemI setFieldsForItem(final ItemI item, final JsonNode fields) {
        if (item instanceof XnatExperimentdata) {
            ((XnatExperimentdata) item).setCustomFields(fields);
        } else if (item instanceof XnatSubjectdata) {
            ((XnatSubjectdata) item).setCustomFields(fields);
        } else if (item instanceof XnatProjectdata) {
            ((XnatProjectdata) item).setCustomFields(fields);
        } else if (item instanceof XnatImagescandata) {
            ((XnatImagescandata) item).setCustomFields(fields);
        } else {
            throw new UnsupportedOperationException("Custom fields are not supported for "
                    + (null == item ? "null" : item.getXSIType()));
        }

        return item;
    }

    private void saveItem(final ItemI item, final UserI user) throws NrgServiceException {
        try {
            final EventDetails details = EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, "Modified Custom Fields");
            SaveItemHelper.authorizedSave(item, user, false, false, details);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new NrgServiceException("Unable to save item.", e);
        }

    }
}
