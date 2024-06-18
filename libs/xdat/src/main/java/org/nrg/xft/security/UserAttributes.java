/*
 * core: org.nrg.xft.security.UserAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.security;

/**
 * Reference for attributes on {@link UserI} interface.
 */
public enum UserAttributes {
    id,
    username,
    login,
    guest,
    firstname,
    lastname,
    email,
    dBName,
    password,
    enabled,
    verified,
    @Deprecated
    salt,
    active,
    lastModified,
    authorization
}
