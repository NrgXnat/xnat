/*
 * framework: org.nrg.framework.processors.XnatEventServiceEventAnnotationProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.processors;

import org.kohsuke.MetaInfServices;
import org.nrg.framework.event.XnatEventServiceEvent;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Processes the {@link XnatEventServiceEvent} annotation and generates the properties file used by XNAT for event
 * class discovery.
 *
 * The basis for this code was adapted from <a href="http://kohsuke.org">Kohsuke Kawaguchi's</a> code for the
 * <a href="http://metainf-services.kohsuke.org">META-INF/services generator</a>. This does the same basic thing but
 * generates a file named "META-INF/xnat/{event-name}-eventservicesevent.properties", where the <i>event-name</i> is taken from the
 * event name defined in this annotation.
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("org.nrg.framework.event.XnatEventServiceEvent")
public class XnatEventServiceEventAnnotationProcessor extends NrgAbstractAnnotationProcessor<XnatEventServiceEvent> {

    @Override
    protected Map<String, String> processAnnotation(final TypeElement element, final XnatEventServiceEvent annotation) {
        final Map<String, String> properties = new LinkedHashMap<>();
        properties.put(XnatEventServiceEvent.EVENT_CLASS, element.getQualifiedName().toString());
        properties.put(XnatEventServiceEvent.EVENT_NAME, annotation.name());
        properties.put(XnatEventServiceEvent.EVENT_DESC, annotation.description());
        return properties;
    }


    @Override
    protected String getPropertiesName(final TypeElement element, final XnatEventServiceEvent plugin) {
    	final String eventName = plugin.name();
        return String.format("META-INF/xnat/event/%s-xnateventserviceevent.properties", eventName.length() > 0 ? eventName : "ESEV" + Long.toString(new Date().getTime()));
    }
    
}
