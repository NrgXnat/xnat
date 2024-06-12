/*
 * framework: org.nrg.framework.logging.RESTfulAppender
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.logging;

// TODO: Rewrite with log4j2. This requires creating an @Plugin-annotated class.

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.converter.*;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("unused")
public class RESTfulAppender { // extends AppenderSkeleton {

    public RESTfulAppender() {
    }

    // @Override
    public void close() {
        //
    }

//    @Override
    public boolean requiresLayout() {
        return false;
    }

    public String getServiceAddress() {
        return _serviceAddress;
    }

    public void setServiceAddress(final String serviceAddress) {
        _serviceAddress = serviceAddress;
    }

//    @Override
//    protected void append(LoggingEvent event) {
//        try {
//            RemoteEvent remoteEvent = new RemoteEvent(convertEventToMap(event));
//            final String marshaledEvent = _serializer.writeValueAsString(remoteEvent);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    ResponseEntity<String> response = getRestTemplate().postForEntity(getServiceAddress(), marshaledEvent, String.class);
//                    System.out.println("Found response: " + response);
//                }
//
//            }).start();
//        } catch (IOException exception) {
//            throw new RuntimeException("There was an issue marshaling the submitted event: " + event, exception);
//        }
//    }
//
//    protected RestTemplate getRestTemplate() {
//        if (_template == null) {
//            _template = new RestTemplate(new AuthenticatedClientHttpRequestFactory("admin", "admin"));
//            _template.setMessageConverters(Arrays.asList(messageConverters));
//        }
//        return _template;
//    }
//
//    private Map<String, Object> convertEventToMap(final LoggingEvent event) {
//        Map<String, Object> eventMap = new HashMap<>();
//        eventMap.put("fqnOfLoggerClass", event.getFQNOfLoggerClass());
//        eventMap.put("level", event.getLevel().toString());
//        eventMap.put("locationInformation", event.getLocationInformation().toString());
//        eventMap.put("message", event.getMessage().toString());
//        try {
//            eventMap.put("properties", _serializer.writeValueAsString(event.getProperties()));
//        } catch (IOException ignored) {
//            // Just ignore if something goes wrong here.
//        }
//        eventMap.put("loggerName", event.getLoggerName());
//        eventMap.put("threadName", event.getThreadName());
//        final ThrowableInformation info = event.getThrowableInformation();
//        if (info != null) {
//            eventMap.put("throwableInformation", info.toString());
//        }
//        eventMap.put("timeStamp", Long.toString(event.getTimeStamp()));
//        return eventMap;
//    }

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private static final HttpMessageConverter<?>[] messageConverters = new HttpMessageConverter<?>[] {new FormHttpMessageConverter(), new StringHttpMessageConverter(), new ResourceHttpMessageConverter(), new ByteArrayHttpMessageConverter() };
    private static final ObjectMapper              _serializer       = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    private RestTemplate _template;
    private String _serviceAddress;
}
