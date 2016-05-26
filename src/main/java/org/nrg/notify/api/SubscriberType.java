/**
 * SubscriberType
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.api;

public enum SubscriberType {
    User,
    NonUser;

    public static SubscriberType Default = User;
}
