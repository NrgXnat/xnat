/**
 * RestBasedMailServiceImpl
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.mail.services.impl;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.mail.api.MailMessage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.http.converter.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RestBasedMailServiceImpl extends AbstractMailServiceImpl {

    public RestBasedMailServiceImpl(String address) throws NrgServiceException {
        super();

        try {
            setRestMailServiceEndpoint(address);
        } catch (URISyntaxException exception) {
            throw new NrgServiceException(NrgServiceError.InvalidRestServiceParameters, "Invalid server address", exception);
        }
    }

    /**
     * Sets the REST mail service endpoint address. This should just be a plain
     * XNAT MailRestlet-hosted address, e.g.
     * https://central.xnat.org/data/services/mail/send.
     * @param address The address of the REST mail service, including protocol, server address,
     *                and method path.
     */
    public void setRestMailServiceEndpoint(String address) throws URISyntaxException {
        if (_log.isDebugEnabled()) {
            _log.debug("Setting the REST mail service endpoint to: " + address);
        }
        _address = new URI(address);
    }

    /**
     * Sets the server address for the desired proxy server. If not set, no proxy is used.
     * @param proxyHost    The address of the proxy server.
     */
    public void setProxyHost(String proxyHost) {
        _proxyHost = proxyHost;
    }

    /**
     * Sets the server port for the desired proxy server. If not set, but the {@link #setProxyHost(String) proxy host}
     * is specified, the default HTTP port 8888 is used.
     * @param proxyPort    The port of the proxy server.
     */
    public void setProxyPort(int proxyPort) {
        _proxyPort = proxyPort;
    }

    /**
     * Sets the username credential for accessing the REST mail service.
     * @param username The username credential.
     */
    public void setUsername(String username) {
        _username = username;
    }

    /**
     * Sets the password credential for accessing the REST mail service.
     * @param password The password credential.
     */
    public void setPassword(String password) {
        _password = password;
    }

    @Override
    public void sendMessage(MailMessage message) throws MessagingException {
        sendMessage(message, _username, _password);
    }

    @Override
    public void sendMessage(MailMessage message, String username, String password) throws MessagingException {
        assert !StringUtils.isBlank(message.getFrom()) : "You must specify a from address for your email.";
        assert message.getTos() != null && message.getTos().size() > 0 : "You must specify at least one address to which to send an email.";
        assert message.getSubject() != null : "You must specify a subject for your email.";

        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
        parameters.add("from", message.getFrom());
        for (String to : message.getTos()) {
            parameters.add("to", to);
        }
        List<String> ccs = message.getCcs();
        if (ccs != null && ccs.size() > 0) {
            for (String cc : ccs) {
                if (cc != null) {
                    parameters.add("cc", cc);
                }
            }
        }
        List<String> bccs = message.getBccs();
        if (bccs != null && bccs.size() > 0) {
            for (String bcc : bccs) {
                if (bcc != null) {
                    parameters.add("bcc", bcc);
                }
            }
        }
        parameters.add("subject", message.getSubject());

        String text = message.getText();
        if (!StringUtils.isBlank(text)) {
            parameters.add("text", text);
        }
        String html = message.getHtml();
        if (!StringUtils.isBlank(html)) {
            parameters.add("html", html);
        }
        Map<String, File> attachments = message.getAttachments();
        if (attachments != null) {
            for (String id : attachments.keySet()) {
                parameters.add(id, new FileSystemResource(attachments.get(id)));
            }
        }
        Map<String, List<String>> headers = message.getHeaders();
        if (headers != null) {
            for (String header : headers.keySet()) {
                List<String> values = headers.get(header);
                for (String value : values) {
                    parameters.add("header:" + header, value);
                }
            }
        }

        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);
        if (!StringUtils.isEmpty(_proxyHost)) {
            final HostConfiguration configuration = new HostConfiguration();
            configuration.setProxy(_proxyHost, _proxyPort);
            client.setHostConfiguration(configuration);
        }
        Credentials credentials = new UsernamePasswordCredentials(username, password);
        client.getState().setCredentials(new AuthScope(_address.getHost(), _address.getPort(), AuthScope.ANY_REALM), credentials);
        RestTemplate template = new RestTemplate(new CommonsClientHttpRequestFactory(client));
        template.setMessageConverters(Arrays.asList(messageConverters));
        ResponseEntity<String> response = template.postForEntity(_address, parameters, String.class);

        if (_log.isInfoEnabled()) {
            _log.info(String.format("Found the following response from %s: [%s] %s", _address, response.getStatusCode(), response.getBody()));
            if (_log.isDebugEnabled()) {
                for (Entry<String, List<String>> header : response.getHeaders().entrySet()) {
                    _log.info("Found header: " + header.getKey() + " with values: " + header.getValue());
                }
            }
        }

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new MessagingException(String.format("Got a non-HTTP OK response from the server: [%s] %s", response.getStatusCode(), response.getBody()));
        }
    }

    private static final Log _log = LogFactory.getLog(RestBasedMailServiceImpl.class);
    private final HttpMessageConverter<?>[] messageConverters = new HttpMessageConverter<?>[] { new FormHttpMessageConverter(), new StringHttpMessageConverter(), new ResourceHttpMessageConverter(), new ByteArrayHttpMessageConverter() };
    private URI _address;
    private String _username;
    private String _password;
    private String _proxyHost;
    private int _proxyPort = 8888;
}

