/*
 * web: org.nrg.xnat.restlet.resources.prearchive.TestPrearcSessionResourceReadMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.prearchive;

import org.junit.Test;
import org.restlet.data.Method;

import static org.assertj.core.api.Assertions.assertThat;

/** XNAT-8806: only GET and HEAD take the read check; every other method keeps the edit check. */
public class TestPrearcSessionResourceReadMethod {
    @Test
    public void getAndHeadAreReads() {
        assertThat(PrearcSessionResourceA.isReadMethod(Method.GET)).isTrue();
        assertThat(PrearcSessionResourceA.isReadMethod(Method.HEAD)).isTrue();
    }

    @Test
    public void everyOtherMethodKeepsTheEditCheck() {
        assertThat(PrearcSessionResourceA.isReadMethod(Method.POST)).isFalse();
        assertThat(PrearcSessionResourceA.isReadMethod(Method.PUT)).isFalse();
        assertThat(PrearcSessionResourceA.isReadMethod(Method.DELETE)).isFalse();
        assertThat(PrearcSessionResourceA.isReadMethod(Method.OPTIONS)).isFalse();
        assertThat(PrearcSessionResourceA.isReadMethod(null)).isFalse();
    }
}
