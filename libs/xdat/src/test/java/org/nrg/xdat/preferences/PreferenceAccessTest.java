package org.nrg.xdat.preferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xdat.configuration.mocks.MockUser;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PreferenceAccessTestConfig.class)
public class PreferenceAccessTest {
    private static final List<String> PUBLIC_PREFS        = Arrays.asList("publicOne", "publicTwo", "publicThree");
    private static final List<String> AUTHENTICATED_PREFS = Arrays.asList("authenticatedOne", "authenticatedTwo", "authenticatedThree");
    private static final List<String> ADMIN_PREFS         = Arrays.asList("adminOne", "adminTwo", "adminThree");

    private PreferenceAccess _access;
    private UserI            _admin;
    private UserI            _authenticated;
    private UserI            _guest;

    @Autowired
    public void setPreferenceAccess(final PreferenceAccess testPreferenceAccess) {
        _access = testPreferenceAccess;
    }

    @Before
    public void setup() {
        final MockUser admin = new MockUser();
        admin.setUsername("admin");
        admin.setAuthorities(Users.AUTHORITIES_ADMIN);
        _admin = admin;

        final MockUser authenticated = new MockUser();
        authenticated.setUsername("authenticated");
        authenticated.setAuthorities(Users.AUTHORITIES_USER);
        _authenticated = authenticated;

        final MockUser guest = new MockUser();
        guest.setUsername("guest");
        guest.setAuthorities(Users.AUTHORITIES_ANONYMOUS);
        _guest = guest;
    }

    @Test
    public void testSiteConfigAccess() {
        final List<Boolean> adminAccesses = Stream.concat(ADMIN_PREFS.stream(), Stream.concat(AUTHENTICATED_PREFS.stream(), PUBLIC_PREFS.stream())).map(pref -> _access.canRead(_admin, pref)).collect(Collectors.toList());
        assertThat(adminAccesses).isNotNull().isNotEmpty().hasSize(9).containsOnly(Boolean.TRUE);
        final List<Boolean> authenticatedAccesses = Stream.concat(ADMIN_PREFS.stream(), Stream.concat(AUTHENTICATED_PREFS.stream(), PUBLIC_PREFS.stream())).map(pref -> _access.canRead(_authenticated, pref)).collect(Collectors.toList());
        assertThat(authenticatedAccesses).isNotNull().isNotEmpty().hasSize(9).containsExactly(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
        final List<Boolean> guestAccesses = Stream.concat(ADMIN_PREFS.stream(), Stream.concat(AUTHENTICATED_PREFS.stream(), PUBLIC_PREFS.stream())).map(pref -> _access.canRead(_guest, pref)).collect(Collectors.toList());
        assertThat(guestAccesses).isNotNull().isNotEmpty().hasSize(9).containsExactly(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
    }
}