/*
 * web: org.nrg.xnat.test.AnonUtilsTestSupport
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.test;

import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;

import java.lang.reflect.Field;

/**
 * Shared helpers for tests that construct {@link DefaultAnonUtils}.
 */
public final class AnonUtilsTestSupport {
    private AnonUtilsTestSupport() {
    }

    /**
     * DefaultAnonUtils enforces a one-instance-per-JVM singleton through a static field; reset it so each
     * test class in the same JVM can construct its own instance.
     */
    public static void resetAnonUtilsSingleton() throws Exception {
        final Field instance = DefaultAnonUtils.class.getDeclaredField("_instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
}
