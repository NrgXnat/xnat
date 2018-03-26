/*
 * core: org.nrg.xdat.turbine.modules.screens.AdminEditScreenA
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

import java.util.Collections;
import java.util.List;

@Slf4j
public abstract class AdminEditScreenA extends EditScreenA {
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isAuthorized(final RunData data) throws Exception {
        boolean authorized = super.isAuthorized(data);
        if (authorized) {
            final UserI user = XDAT.getUserDetails();
            if (!Roles.isSiteAdmin(user)) {
                authorized = false;
                data.setMessage("Unauthorized access.  Please login to gain access to this page.");
                logAccess(data, "Unauthorized access (prevented).");
                log.error("Unauthorized Access to an Admin Screen (prevented).");
                AdminUtils.sendAdminEmail(user, "Unauthorized Admin Access Attempt", "Unauthorized Access to an Admin Screen (" + data.getScreen() + ") prevented.");


            }
        }

        return authorized;
    }

    protected List<String> getAllDefinedFieldsForSecurityElement() {
        final ItemI item = getEditItem();
        if (item == null) {
            return Collections.emptyList();
        }

        try {
            final String elementName = item.getStringProperty("xdat:element_security.element_name");
            if (StringUtils.isNotBlank(elementName)) {
                final SchemaElement schemaElement = SchemaElement.GetElement(elementName);
                return schemaElement.getAllDefinedFields();
            }
        } catch (XFTInitException e) {
            log.error("An error occurred trying to access XFT when trying to get the element_name property from this ItemI object:\n{}", item, e);
        } catch (ElementNotFoundException e) {
            log.error("Couldn't find the element of type {}: {}", e.ELEMENT, e);
        } catch (FieldNotFoundException e) {
            log.error("Couldn't find the field named {} for type {}: {}", e.FIELD, item.getXSIType(), e.MESSAGE);
        }

        return Collections.emptyList();
    }
}
