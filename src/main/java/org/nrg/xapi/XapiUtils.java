package org.nrg.xapi;

import org.springframework.http.HttpHeaders;

import javax.annotation.Nonnull;

import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;

public class XapiUtils {
    public static final String WWW_AUTHENTICATE_BASIC_HEADER = "Basic realm=\"%s\"";

    @Nonnull
    public static String getWwwAuthenticateBasicHeaderValue(final @Nonnull String realm) {
        return String.format(WWW_AUTHENTICATE_BASIC_HEADER, realm);
    }

    @Nonnull
    public static HttpHeaders getWwwAuthenticateBasicHeaders(final @Nonnull String realm) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(WWW_AUTHENTICATE, String.format(WWW_AUTHENTICATE_BASIC_HEADER, realm));
        return headers;
    }
}
