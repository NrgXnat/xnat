/*
 * DicomEdit: HttpClient
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.service.http;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HttpClient implements IHttpClient {
    private static Proxy  _proxy;
    private static String _proxyAuth;

    public static String getBase64Encoded(final PasswordAuthentication authentication) {
        return getBase64Encoded(authentication.getUserName(), authentication.getPassword());
    }

    public static String getBase64Encoded(final String username, final char[] password) {
        return getBase64Encoded(username, new String(password));
    }

    public static String getBase64Encoded(final String username, final String password) {
        return getBase64Encoded(username + ":" + password);
    }

    public static String getBase64Encoded(final String userInfo) {
        return StringUtils.isBlank(userInfo) ? "" : "Basic " + BASE_64.encodeToString(userInfo.getBytes());
    }

    public static void setProxy(final Proxy proxy) {
        _proxy = proxy;
    }

    public static void setProxyAuth(final String proxyAuth) {
        _proxyAuth = proxyAuth;
    }

    @Override
    public List<String> requestAsList(final URL url) throws IOException {
        final HttpURLConnection connection;
        final List<String> lines = new ArrayList<>();
        try {
            connection = (HttpURLConnection) (_proxy == null ? url.openConnection() : url.openConnection(_proxy));
        } catch (ClassCastException e) {
            throw new IOException("unable to make HTTP/HTTPS connection to URL " + url);
        }

        if (url.getUserInfo() != null) {
            connection.setRequestProperty(AUTHORIZATION_HEADER, getBase64Encoded(url.getUserInfo()));
        }

        if (StringUtils.isNotBlank(_proxyAuth)) {
            connection.setRequestProperty(PROXY_AUTHORIZATION_HEADER, _proxyAuth);
        }

        connection.connect();
        try {
            if (HttpURLConnection.HTTP_OK != connection.getResponseCode()) {
                throw new IOException("request failed: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            }

            final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            for (String line = reader.readLine(); null != line; line = reader.readLine()) {
                lines.add(line);
            }
            reader.close();
        } finally {
            connection.disconnect();
        }
        return lines;
    }

    @Override
    public String request(final URL url) throws IOException {
        return String.join( System.lineSeparator(), requestAsList(url));
    }

    private static final String AUTHORIZATION_HEADER       = "Authorization";
    private static final String PROXY_AUTHORIZATION_HEADER = "Proxy-Authorization";
    private static final Base64 BASE_64                    = new Base64(-1);
}
