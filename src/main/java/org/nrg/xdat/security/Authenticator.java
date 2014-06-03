// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.security;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.servlet.XDATServlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Properties;

public class Authenticator {

    public static final String DEFAULT_AUTHENTICATOR = "org.nrg.xdat.security.Authenticator";

    public synchronized static String RetrieveAuthenticatorClassName() {
        // MIGRATE: Went straight to inputstream instead of file.
        if (AUTH_CLASS == null) {
            InputStream inputs = XDATServlet.getConfigurationStream("authentication.properties");
            if (inputs != null) {
                try {
                    Properties properties = new Properties();
                    properties.load(inputs);
                    if (properties.containsKey(AUTH_CLASS_NAME)) {
                        AUTH_CLASS = properties.getProperty(AUTH_CLASS_NAME);
                    }
                } catch (IOException ignored) {
                    // Ignore, we'll handle later.
                }
            }
            if (AUTH_CLASS == null) {
                AUTH_CLASS = DEFAULT_AUTHENTICATOR;
            }
        }

        return AUTH_CLASS;
    }

    public static Authenticator CreateAuthenticator() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class authClass = Class.forName(RetrieveAuthenticatorClassName());
        return (Authenticator) authClass.newInstance();
    }

    public static XDATUser Authenticate(Credentials cred) throws Exception {
        return CreateAuthenticator().authenticate(cred);
    }

    public static boolean Authenticate(XDATUser u, Credentials cred) throws Exception {
        return CreateAuthenticator().authenticate(u, cred);
    }

    public XDATUser authenticate(Credentials cred) throws Exception {
        XDATUser user;
        try {
            user = new XDATUser(cred.username, cred.password);
        } catch (Exception e) {
            user = null;
        }
        if (user == null && AliasToken.isAliasFormat(cred.username)) {
            AliasToken token = getAliasTokenService().locateToken(cred.username);
            try {
                user = new XDATUser(token.getXdatUserId());
            } catch (Exception exception) {
                user = null;
            }
        }
        return user;
    }

    public boolean authenticate(XDATUser u, Credentials cred) throws Exception {
        return u.login(cred.password);
    }

    private AliasTokenService getAliasTokenService() {
        if (_aliasTokenService == null) {
            _aliasTokenService = XDAT.getContextService().getBean(AliasTokenService.class);
        }
        return _aliasTokenService;
    }

    private final static String AUTH_CLASS_NAME = "AUTHENTICATION_CLASS";
    private static String AUTH_CLASS = null;
    private AliasTokenService _aliasTokenService;

    public static class Credentials {
        String username = null;
        String password = null;

        public HashMap OTHER = new HashMap();

        public Credentials(String u, String p) {
            username = u;
            password = p;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }


    }
}
