/*
 * core: org.nrg.xdat.security.validators.RegExpValidatorTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.validators;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.apache.commons.lang3.StringUtils.repeat;

/**
 * Covers the password length ceiling added for CVE-2025-22228. The package-protected constructor gives the
 * "panic mode" defaults, so the complexity pattern is {@code ^.*$} and length is the only thing under test.
 */
public class RegExpValidatorTest {
    @Test
    public void passwordAtTheByteLimitIsAccepted() {
        assertThat(validator.isValid(repeat("a", 72), null)).isEmpty();
    }

    @Test
    public void passwordOneByteOverTheLimitIsRejected() {
        assertThat(validator.isValid(repeat("a", 73), null)).contains("72 characters or fewer");
    }

    @Test
    public void multiByteCharactersCountAsBytesNotCharacters() {
        // 25 three-byte characters is 75 bytes, so this is rejected despite being well under 72 characters.
        final String password = repeat("中", 25);
        assertThat(password.length()).isLessThan(72);
        assertThat(password.getBytes(StandardCharsets.UTF_8).length).isGreaterThan(72);
        assertThat(validator.isValid(password, null)).contains("72 characters or fewer");
    }

    @Test
    public void ordinaryPasswordIsUnaffected() {
        assertThat(validator.isValid("correct horse battery staple", null)).isEmpty();
    }

    private final RegExpValidator validator = new RegExpValidator();
}
