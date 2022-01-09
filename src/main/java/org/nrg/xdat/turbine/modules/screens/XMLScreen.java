/*
 * core: org.nrg.xdat.turbine.modules.screens.XMLScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.om.XdatUserLogin;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.restlet.data.Status;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Tim
 */
@Slf4j
public class XMLScreen extends XDATRawScreen {
    private static final String USER_PASSWORD_PROPERTY       = "primary_password";
    private static final String USER_SALT_PROPERTY           = "salt";
    private static final String LOGIN_SESSION_ID_PROPERTY    = "session_id";
    private static final String LOGIN_USER_PASSWORD_PROPERTY = "user/primary_password";
    private static final String LOGIN_USER_SALT_PROPERTY     = "user/salt";

    /**
     * Set the content type to Xml. (see RawScreen)
     *
     * @param data Turbine information.
     *
     * @return content type.
     */
    public String getContentType(final RunData data) {
        return "text/xml";
    }

    /**
     * {@inheritDoc}
     */
    protected final void doOutput(final RunData data) throws Exception {
        try {
            final ItemI item = ObjectUtils.getIfNull(TurbineUtils.getDataItem(data), () -> getItemBySearch(data));
            if (item == null) {
                data.setMessage("No Item found for XML display.");
                data.setScreenTemplate("Index.vm");
                return;
            }

            final UserI user = XDAT.getUserDetails();
            if (!TurbineUtils.isAccessibleItem(user, item) || user == null || user.isGuest()) {
                TurbineUtils.denyAccess(data);
                return;
            }

            final HttpServletResponse response = data.getResponse();
            response.setContentType("text/xml");

            writeToXml(Roles.isSiteAdmin(user) || isUserRelated(user, item) ? item : scrubItem(item), response);
        } catch (IllegalAccessException e) {
            final HttpServletResponse response = data.getResponse();
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN.getCode());
        }
    }

    private static boolean isUserRelated(UserI user, final ItemI item) throws ElementNotFoundException, XFTInitException, FieldNotFoundException {
        if (!(item instanceof XFTItem)) {
            return false;
        }
        if (((XFTItem) item).instanceOf(XdatUser.SCHEMA_ELEMENT_NAME)) {
            return StringUtils.equalsIgnoreCase(user.getUsername(), item.getStringProperty("login"));
        }
        if (((XFTItem) item).instanceOf(XdatUserLogin.SCHEMA_ELEMENT_NAME)) {
            return Objects.equals(((XdatUser) user).getXdatUserId(), ((XFTItem) item).getIntegerProperty("user_xdat_user_id"));
        }
        return false;
    }

    /**
     * Removes or clears properties that may compromise security, e.g. session IDs, IP addresses, passwords
     * and salts, and so on.
     *
     * @param item The item to be scrubbed.
     *
     * @return The item with sensitive properties scrubbed.
     */
    private static ItemI scrubItem(final ItemI item) {
        switch (item.getXSIType()) {
            case XdatUser.SCHEMA_ELEMENT_NAME:
                clearProperties(item, USER_PASSWORD_PROPERTY, USER_SALT_PROPERTY);
                break;

            case XdatUserLogin.SCHEMA_ELEMENT_NAME:
                clearProperties(item, LOGIN_SESSION_ID_PROPERTY, LOGIN_USER_PASSWORD_PROPERTY, LOGIN_USER_SALT_PROPERTY);
                break;

            default:
                log.debug("Got an item of type {}, nothing to do to scrub this object specifically", item.getXSIType());
        }
        return item;
    }

    private static void clearProperties(final ItemI item, final String... properties) {
        Arrays.stream(properties).filter(property -> hasPropertyValue(item, property)).forEach(property -> {
            try {
                item.setProperty(property, null);
            } catch (Exception e) {
                log.error("An error occurred trying to unset the property {} on an item of type {}", property, item.getXSIType());
            }
        });
    }

    private static boolean hasPropertyValue(final ItemI item, final String property) {
        try {
            return Objects.nonNull(item.getProperty(property));
        } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
            return false;
        }
    }

    private static ItemI getItemBySearch(final RunData data) {
        try {
            return TurbineUtils.GetItemBySearch(data);
        } catch (Exception e) {
            log.warn("An error occurred trying to retrieve an item by search", e);
            return null;
        }
    }
}
