package org.nrg.xdat.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class LegacySha256PasswordEncoder implements PasswordEncoder {
    private static final String               PASSWORD                = "password";
    private static final String               SALT                    = "salt";
    private static final Pattern              PASSWORD_SALT_EXTRACTOR = Pattern.compile("^(?<" + PASSWORD + ">[A-Fa-f0-9]{64})\\{(?<" + SALT + ">[A-Za-z0-9]{64})}$");
    private static final Pair<String, String> NO_PAIR                 = ImmutablePair.nullPair();

    @SuppressWarnings("deprecation")
    private final ShaPasswordEncoder shaPasswordEncoder = new ShaPasswordEncoder(256);

    @Override
    public String encode(final CharSequence rawPassword) {
        throw new UnsupportedOperationException("Encoding passwords with SHA-256 is no longer supported");
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        final Pair<String, String> split = extractCombinedPasswordAndSalt(encodedPassword);
        return !split.equals(NO_PAIR) && shaPasswordEncoder.isPasswordValid(split.getLeft(), rawPassword.toString(), split.getRight());
    }

    private Pair<String, String> extractCombinedPasswordAndSalt(final String encodedPassword) {
        final Matcher matcher = PASSWORD_SALT_EXTRACTOR.matcher(encodedPassword);
        return matcher.matches() ? Pair.of(matcher.group(PASSWORD), matcher.group(SALT)) : NO_PAIR;
    }
}