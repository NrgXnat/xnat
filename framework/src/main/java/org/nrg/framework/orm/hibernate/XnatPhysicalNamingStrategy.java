/*
 * framework: org.nrg.framework.orm.hibernate.XnatPhysicalNamingStrategy
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate;

/**
 * No-arg {@link PrefixedPhysicalNamingStrategy} with XNAT's standard <b>xhbm</b> table prefix, so the strategy can
 * be configured through the standard <code>hibernate.physical_naming_strategy</code> property. XNAT configures the
 * naming strategies via Hibernate properties rather than the {@code LocalSessionFactoryBean} setters because
 * Spring's {@code orm.hibernate5} support is compiled against Hibernate 5 method signatures
 * ({@code void setImplicitNamingStrategy(...)}) that became fluent in Hibernate 6, which triggers
 * {@code NoSuchMethodError} at session-factory build time.
 */
public class XnatPhysicalNamingStrategy extends PrefixedPhysicalNamingStrategy {
    public XnatPhysicalNamingStrategy() {
        super("xhbm");
    }
}
