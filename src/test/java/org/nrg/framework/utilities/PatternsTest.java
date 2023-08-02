package org.nrg.framework.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class PatternsTest {
    @Test
    public void testUsernamePattern() {
        assertThat(VALID_USERNAMES.stream().allMatch(username -> Patterns.USERNAME.matcher(username).matches())).isTrue();
        assertThat(INVALID_USERNAMES.stream().noneMatch(username -> Patterns.USERNAME.matcher(username).matches())).isTrue();
    }

    @Test
    public void testEmailPattern() {
        assertThat(VALID_EMAIL_ADDRESSES.stream().allMatch(email -> Patterns.EMAIL.matcher(email).matches())).isTrue();
        assertThat(INVALID_EMAIL_ADDRESSES.stream().noneMatch(email -> Patterns.EMAIL.matcher(email).matches())).isTrue();
    }

    private static final List<String> VALID_USERNAMES   = Arrays.asList("foo", "harmitage", "fooBar", "foo1", "foo12345", "foo_bar", "foo'bar", "foo.bar", "foo-bar", "foo0-9_'.-", "abcdefghijklmnopqrstuvwxy");
    private static final List<String> INVALID_USERNAMES = Arrays.asList("1foo", "_bar", "'bar", ".bar", "-bar", "foo$", "foo#", "xnatselenium@gmail.com", "firstname.lastname@med.uni-goettingen.de");

    // Many examples here taken from https://en.wikipedia.org/wiki/Email_address#Examples
    private static final List<String> VALID_EMAIL_ADDRESSES   = Arrays.asList("xnatselenium@gmail.com", "firstname.lastname@med.uni-goettingen.de", "harmitage@miskatonic.edu", "A-Za-z0-9!#$%&'*+/=?^_`{|}~-@aol.com", "simple@example.com", "very.common@example.com", "disposable.style.email.with+symbol@example.com", "other.email-with-hyphen@example.com", "fully-qualified-domain@example.com", "user.name+tag+sorting@example.com", "x@example.com", "example-indeed@strange-example.com", "example@s.example");
    private static final List<String> INVALID_EMAIL_ADDRESSES = Arrays.asList(".foo@gmail.com", "Abc.example.com", "A@b@c@example.com", "a\"b(c)d,e:f;g<h>i[j\\k]l@example.com", "1234567890123456789012345678901234567890123456789012345678901234+x@example.com", "i_like_underscore@but_its_not_allow_in_this_part.example.com");
}
