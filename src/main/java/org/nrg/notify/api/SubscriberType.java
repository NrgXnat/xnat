/*
 * org.nrg.notify.api.SubscriberType
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.api;

public enum SubscriberType {
    User,
    NonUser;

    public static SubscriberType Default = User;
}
