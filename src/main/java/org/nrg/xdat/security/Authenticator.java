// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xft.XFT;
import org.nrg.xft.security.UserI;

public class Authenticator {
    public synchronized static String RetrieveAuthenticatorClassName() {
        if (AUTH_CLASS == null) {
            File AUTH_PROPS = new File(XFT.GetConfDir(), "authentication.properties");
            if (!AUTH_PROPS.exists()) {
                System.out.println("No authentication.properties file found in conf directory. Skipping enhanced authentication method.");
                AUTH_CLASS = "org.nrg.xdat.security.Authenticator";
            } else {
                try {
                    InputStream inputs = new FileInputStream(AUTH_PROPS);
                    Properties properties = new Properties();
                    properties.load(inputs);

                    if (properties.containsKey(AUTH_CLASS_NAME)) {
                        AUTH_CLASS = properties.getProperty(AUTH_CLASS_NAME);
                    } else {
                        AUTH_CLASS = "org.nrg.xdat.security.Authenticator";
                    }
                } catch (IOException e) {
                    AUTH_CLASS = "org.nrg.xdat.security.Authenticator";
                }
            }
        }

        return AUTH_CLASS;
    }

    public static Authenticator CreateAuthenticator() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class authClass = Class.forName(RetrieveAuthenticatorClassName());
        return (Authenticator) authClass.newInstance();
    }

    public static UserI Authenticate(Credentials cred) throws Exception {
        return CreateAuthenticator().authenticate(cred);
    }

    public static boolean Authenticate(UserI u, Credentials cred) throws Exception {
        return CreateAuthenticator().authenticate(u, cred);
    }

    public UserI authenticate(Credentials cred) throws Exception {
    	UserI user;
        try {
            user= Users.getUser(cred.username);
            if(!authenticate(user,cred)){
            	user=null;
            }
        } catch (Exception e) {
            user = null;
        }
        if (user == null && AliasToken.isAliasFormat(cred.username)) {
            AliasToken token = getAliasTokenService().locateToken(cred.username);
            try {
                user = Users.getUser(token.getXdatUserId());
            } catch (Exception exception) {
                user = null;
            }
        }
        return user;
    }

    public boolean authenticate(UserI u, Credentials cred) throws Exception {
    	return Users.authenticate(u,cred);
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
