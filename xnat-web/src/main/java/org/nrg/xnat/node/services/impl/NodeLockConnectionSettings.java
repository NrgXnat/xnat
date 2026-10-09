/*
 * web: org.nrg.xnat.node.services.impl.NodeLockConnectionSettings
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.node.services.impl;

import lombok.ToString;
import lombok.Value;

/**
 * How {@link PostgresNodeLockService} opens its dedicated lock connection. These are the application data source's
 * driver, URL and credentials: the lock connection is opened directly through the configured driver class rather
 * than borrowed from the connection pool, so drivers that do their own authentication (for example IAM token
 * drivers) work the same way for the lock connection as for the pool.
 */
@Value
public class NodeLockConnectionSettings {
    String driverClassName;
    String url;
    String username;
    @ToString.Exclude
    String password;
}
