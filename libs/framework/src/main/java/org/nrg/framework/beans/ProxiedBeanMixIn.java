/*
 * framework: org.nrg.framework.beans.ProxiedBeanMixIn
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;

/**
 * A Jackson mix-in class to hide properties exposed on proxied bean objects.
 */
@SuppressWarnings("unused")
public abstract class ProxiedBeanMixIn {
    @JsonIgnore
    public abstract TargetSource getTargetSource();

    @JsonIgnore
    public abstract Class<?>[] getProxiedInterfaces();

    @JsonIgnore
    public abstract boolean isExposeProxy();

    @JsonIgnore
    public abstract boolean isPreFiltered();

    @JsonIgnore
    public abstract Advisor[] getAdvisors();

    @JsonIgnore
    public abstract boolean isProxyTargetClass();

    @JsonIgnore
    public abstract boolean isFrozen();

    @JsonIgnore
    public abstract Class<?> getTargetClass();
}
