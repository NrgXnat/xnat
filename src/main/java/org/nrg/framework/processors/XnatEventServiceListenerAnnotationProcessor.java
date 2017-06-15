/*
 * framework: org.nrg.framework.processors.XnatEventServiceActionAnnotationProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.processors;

import org.kohsuke.MetaInfServices;
import org.nrg.framework.event.XnatEventServiceListener;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Processes the {@link XnatEventServiceListener} annotation and generates the properties file used by XNAT for action
 * class discovery.
 *
 * The basis for this code was adapted from <a href="http://kohsuke.org">Kohsuke Kawaguchi's</a> code for the
 * <a href="http://metainf-services.kohsuke.org">META-INF/services generator</a>. This does the same basic thing but
 * generates a file named "META-INF/xnat/{listener-name}-eventserviceslistener.properties", where the <i>listener-name</i> is taken from the
 * listener name defined in this annotation.
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("org.nrg.framework.event.XnatEventServiceListener")
public class XnatEventServiceListenerAnnotationProcessor extends NrgAbstractAnnotationProcessor<XnatEventServiceListener> {

    @Override
    protected Map<String, String> processAnnotation(final TypeElement element, final XnatEventServiceListener annotation) {
        final Map<String, String> properties = new LinkedHashMap<>();
        properties.put(XnatEventServiceListener.LISTENER_CLASS, element.getQualifiedName().toString());
        properties.put(XnatEventServiceListener.LISTENER_NAME, annotation.name());
        properties.put(XnatEventServiceListener.LISTENER_DISPLAY_NAME, annotation.displayName());
        properties.put(XnatEventServiceListener.LISTENER_DESC, annotation.description());
        properties.put(XnatEventServiceListener.LISTENER_EVENT, annotation.event());
        return properties;
    }


    @Override
    protected String getPropertiesName(final TypeElement element, final XnatEventServiceListener plugin) {
    	final String eventName = plugin.name();
        return String.format("META-INF/xnat/event/%s-xnateventservicelistener.properties", eventName.length() > 0 ? eventName : "ESAC" + Long.toString(new Date().getTime()));
    }
    
}
