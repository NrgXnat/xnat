/*
 * framework: org.nrg.framework.net.AuthenticatedClientHttpRequestFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.net;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.net.URI;

public class AuthenticatedClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

    public AuthenticatedClientHttpRequestFactory(String user, String password) {
        _user = user;
        _password = password;
    }

    public void setProxy(URI proxy) {
        _proxy = proxy;
    }

    public HttpClient getHttpClient() {
        Credentials credentials = new UsernamePasswordCredentials(_user, _password.toCharArray());
        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(null, -1), credentials);

        HttpClientBuilder builder = HttpClients.custom();
        builder.setDefaultCredentialsProvider(provider);

        if (_proxy != null) {
            builder.setProxy(new HttpHost(_proxy.getScheme(), _proxy.getHost(), _proxy.getPort()));
        }

        return builder.build();
    }

    protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
        BasicAuthCache authCache = new BasicAuthCache();

        BasicScheme basicAuth = new BasicScheme();
        authCache.put(new HttpHost(uri.getScheme(), uri.getHost(), uri.getPort()), basicAuth);

        HttpClientContext context = HttpClientContext.create();
        context.setAuthCache(authCache);
        return context;
    }

    private final String _user;
    private final String _password;
    private URI _proxy;
}
