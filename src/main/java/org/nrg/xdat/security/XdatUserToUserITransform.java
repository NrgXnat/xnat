/*
 * core: org.nrg.xdat.security.XdatUserToUserITransform
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import com.google.common.base.Function;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * Transforms instances of the {@link XdatUser} class to instances of the {@link UserI} interface. Can be used with
 * various Guava transform methods.
 */
public class XdatUserToUserITransform implements Function<XdatUser, UserI> {
    /**
     * Gets the default instance of this transform.
     *
     * @return The default instance.
     */
    public static XdatUserToUserITransform getInstance() {
        return _instance;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public UserI apply(@Nullable final XdatUser user) {
        if (user == null) {
            return null;
        }
        try {
            return new XDATUser(user.getItem());
        } catch (UserInitException e) {
            _log.error("", e);
            return null;
        }
    }

    private XdatUserToUserITransform() {
        _log.debug("Creating the default instance of this transform.");
    }

    private static final Logger                   _log      = LoggerFactory.getLogger(XdatUserToUserITransform.class);
    private static final XdatUserToUserITransform _instance = new XdatUserToUserITransform();
}
