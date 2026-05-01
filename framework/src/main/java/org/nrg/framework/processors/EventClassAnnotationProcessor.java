/*
 * framework: org.nrg.framework.processors.EventClassAnnotationProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.processors;

import org.kohsuke.MetaInfServices;
import org.nrg.framework.event.EventClass;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Processes the {@link EventClass} annotation and generates the plugin's properties file that used by XNAT for event
 * class discovery.  Currently this annotation is required only for classes that implement the AutomationEventImplementerI 
 * interfaces, as information from these classes are used to populate the automation service event handler setup
 * UI.  However, this annotation might be important for additional types of events in the future.
 * The basis for this code was adapted from <a href="http://kohsuke.org">Kohsuke Kawaguchi's</a> code for the
 * <a href="http://metainf-services.kohsuke.org">META-INF/services generator</a>. This does the same basic thing but
 * generates a file named "META-INF/xnat/{event-name}-event.properties", where the <i>event-name</i> is taken from the 
 * event name defined in this annotation.
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("org.nrg.framework.event.EventClass")
public class EventClassAnnotationProcessor extends NrgAbstractAnnotationProcessor<EventClass> {

    /* (non-Javadoc)
     * @see org.nrg.framework.processors.NrgAbstractAnnotationProcessor#processAnnotation(javax.lang.model.element.TypeElement, java.lang.annotation.Annotation)
     */
    @Override
    protected Map<String, String> processAnnotation(final TypeElement element, final EventClass plugin) {
        final Map<String, String> properties = new LinkedHashMap<>();
        properties.put(EventClass.EVENT_CLASS, element.getQualifiedName().toString());
        properties.put(EventClass.EVENT_NAME, plugin.name());
        properties.put(EventClass.EVENT_DESC, plugin.description());
        properties.put(EventClass.EVENT_DEFAULTIDS, Arrays.toString(plugin.defaultEventIds()));
        properties.put(EventClass.EVENT_INCLUDEFROMDATABASE, String.valueOf(plugin.includeValuesFromDatabase()));
        return properties;
    }

    /* (non-Javadoc)
     * @see org.nrg.framework.processors.NrgAbstractAnnotationProcessor#getPropertiesName(java.lang.annotation.Annotation)
     */
    @Override
    protected String getPropertiesName(final TypeElement element, final EventClass plugin) {
    	final String eventName = plugin.name();
        return String.format("META-INF/xnat/event/%s-event.properties", eventName.length() > 0 ? eventName : "EV" + Long.toString(new Date().getTime()));
    }
    
}
