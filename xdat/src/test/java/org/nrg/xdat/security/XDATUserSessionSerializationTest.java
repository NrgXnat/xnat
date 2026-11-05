package org.nrg.xdat.security;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTPseudonymManager;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.XFTManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Characterizes what happens today when the session's copy of the user is serialized and read back, as a session store
 * shared between nodes would do. Serialization doesn't throw, but what comes back isn't the user that went in.
 *
 * <p>These tests assert today's behaviour, including the parts that are wrong, so that a change to it is noticed. The
 * planned change to store a reference to the user in the session, rather than the {@link XDATUser} itself, is expected
 * to change these assertions; update them then rather than deleting them.</p>
 */
public class XDATUserSessionSerializationTest {
    private static final String USERNAME      = "jdoe";
    private static final int    USER_ID       = 42;
    private static final String LAST_NAME     = "O'Brien (Jr.)";
    private static final String PASSWORD_HASH = "{bcrypt}$2a$10$abcdefghijklmnopqrstuv";

    private XDATUser user;

    @Before
    public void createUser() throws Exception {
        // XFT looks up optional Spring beans through the static ContextService and loads schemas without them. A test
        // earlier in the same JVM can leave its application context registered, and beans missing from that context
        // then throw instead of being skipped.
        ContextService.getInstance().setApplicationContext(null);
        loadSchema();

        final XFTItem item = XFTItem.NewItem("xdat:user", null);
        item.setProperty("xdat:user.login", USERNAME);
        item.setProperty("xdat:user.firstname", "Jane");
        item.setProperty("xdat:user.lastname", LAST_NAME);
        item.setProperty("xdat:user.email", "jdoe@example.org");
        item.setProperty("xdat:user.primary_password", PASSWORD_HASH);
        item.setProperty("xdat:user.xdat_user_id", USER_ID);
        item.setProperty("xdat:user.enabled", true);
        item.setProperty("xdat:user.verified", true);

        user = new XDATUser(item, false);
        user.setAuthorization(new XdatUserAuth(USERNAME, "ldap", "ldap1"));
    }

    @After
    public void unloadSchema() {
        XFTManager.clean();
        XFTMetaManager.clean();
        XFTReferenceManager.clean();
        XFTPseudonymManager.clean();
        GenericWrapperElement.ClearElementCache();
        ViewManager.FIELD_MAPS.clear();
        ViewManager.FIELD_NAMES.clear();
    }

    @Test
    public void readBackKeepsTheIdentityAndAuthorities() throws Exception {
        final SecurityContext restored = roundTrip(contextFor(user));

        final UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) restored.getAuthentication();
        assertEquals(Collections.singletonList("ROLE_USER"), token.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

        final XDATUser principal = (XDATUser) token.getPrincipal();
        assertNotSame(user, principal);
        assertEquals(user, principal);
        assertEquals(USERNAME, principal.getUsername());
        assertEquals(Integer.valueOf(USER_ID), principal.getID());
        assertEquals("Jane", principal.getFirstname());
        assertEquals("jdoe@example.org", principal.getEmail());
        assertTrue(principal.isEnabled());
    }

    @Test
    public void readBackDropsTheAuthMethodSoTheAccountLooksLocked() throws Exception {
        assertNotNull(user.getAuthorization());
        assertTrue(user.isAccountNonLocked());

        final XDATUser principal = principalOf(roundTrip(contextFor(user)));

        // Only the XFT item is written: XDATUser's own fields, including the auth method, are left at their defaults.
        assertNull(principal.getAuthorization());
        assertFalse(principal.isAccountNonLocked());
    }

    @Test
    public void readBackCorruptsParentheses() throws Exception {
        assertEquals(LAST_NAME, user.getLastname());

        // XFT's flat-string format escapes parentheses when it writes them and doesn't unescape them when it reads.
        assertEquals("O'Brien *OPEN*Jr.*CLOSE*", principalOf(roundTrip(contextFor(user))).getLastname());
    }

    @Test
    public void principalAndUserHelperShareOneUserOnlyWhenWrittenTogether() throws Exception {
        // The session holds the user twice: as the security context's principal and inside the "userHelper" attribute.
        final SecurityContext       context = contextFor(user);
        final XDATUserHelperService helper  = new XDATUserHelperService(user);
        assertSame(principalOf(context), helper.getUser());

        // A store that writes the whole session in one stream, as Tomcat's file-backed managers do, keeps one instance.
        final Object[] together = (Object[]) deserialize(serialize(new Object[]{context, helper}));
        assertSame(principalOf((SecurityContext) together[0]), ((XDATUserHelperService) together[1]).getUser());

        // A store that writes each attribute separately, as Spring Session and Redisson do, gives two independent copies,
        // so a change made through one is no longer seen through the other.
        final XDATUser fromContext = principalOf(roundTrip(context));
        final XDATUser fromHelper  = (XDATUser) ((XDATUserHelperService) deserialize(serialize(helper))).getUser();
        assertNotSame(fromContext, fromHelper);
        fromContext.setAuthorization(new XdatUserAuth(USERNAME, "localdb", "localdb"));
        assertNull(fromHelper.getAuthorization());
    }

    @Test
    public void readBackWithoutTheSchemaSilentlyReturnsAUserWithNoUsername() throws Exception {
        final byte[] bytes = serialize(contextFor(user));

        // A node reading the session before XFT has loaded its schema, as Tomcat's StandardManager does at startup.
        unloadSchema();
        final XDATUser principal = principalOf(deserialize(bytes));

        assertNull(principal.getUsername());
        assertNull(principal.getID());
    }

    private static void loadSchema() throws Exception {
        final File schema = new File(XDATUserSessionSerializationTest.class.getClassLoader().getResource("schemas/security/security.xsd").toURI());
        XFT.init(schema.getParentFile().getParentFile().getParent());
    }

    private static SecurityContext contextFor(final XDATUser user) {
        final List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        return new SecurityContextImpl(new UsernamePasswordAuthenticationToken(user, null, authorities));
    }

    private static XDATUser principalOf(final Object context) {
        return (XDATUser) ((SecurityContext) context).getAuthentication().getPrincipal();
    }

    private static SecurityContext roundTrip(final SecurityContext context) throws IOException, ClassNotFoundException {
        return (SecurityContext) deserialize(serialize(context));
    }

    private static byte[] serialize(final Object object) throws IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (final ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(object);
        }
        return bytes.toByteArray();
    }

    private static Object deserialize(final byte[] bytes) throws IOException, ClassNotFoundException {
        try (final ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return input.readObject();
        }
    }
}
