/*
 * framework: org.nrg.framework.net.AuthenticatedClientHttpRequestFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.net;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
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
        Credentials credentials = new UsernamePasswordCredentials(_user, _password);
        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, credentials);

        HttpClientBuilder builder = HttpClients.custom();
        builder.setDefaultCredentialsProvider(provider);

        if (_proxy != null) {
            builder.setProxy(new HttpHost(_proxy.getHost(), _proxy.getPort(), _proxy.getScheme()));
        }

        return builder.build();
    }

    protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
        AuthCache authCache = new BasicAuthCache();

        BasicScheme basicAuth = new BasicScheme();
        authCache.put(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()), basicAuth);

        BasicHttpContext context = new BasicHttpContext();
        context.setAttribute("http.auth.auth-cache", authCache);
        return context;
    }

    private final String _user;
    private final String _password;
    private URI _proxy;
}
