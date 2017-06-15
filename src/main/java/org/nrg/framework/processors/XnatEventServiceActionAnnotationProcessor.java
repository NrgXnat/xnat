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
import org.nrg.framework.event.XnatEventServiceAction;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Processes the {@link XnatEventServiceAction} annotation and generates the properties file used by XNAT for action
 * class discovery.
 *
 * The basis for this code was adapted from <a href="http://kohsuke.org">Kohsuke Kawaguchi's</a> code for the
 * <a href="http://metainf-services.kohsuke.org">META-INF/services generator</a>. This does the same basic thing but
 * generates a file named "META-INF/xnat/{action-name}-eventservicesaction.properties", where the <i>action-name</i> is taken from the
 * action name defined in this annotation.
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("org.nrg.framework.event.XnatEventServiceAction")
public class XnatEventServiceActionAnnotationProcessor extends NrgAbstractAnnotationProcessor<XnatEventServiceAction> {

    @Override
    protected Map<String, String> processAnnotation(final TypeElement element, final XnatEventServiceAction annotation) {
        final Map<String, String> properties = new LinkedHashMap<>();
        properties.put(XnatEventServiceAction.ACTION_CLASS, element.getQualifiedName().toString());
        properties.put(XnatEventServiceAction.ACTION_NAME, annotation.name());
        properties.put(XnatEventServiceAction.ACTION_DISPLAY_NAME, annotation.displayName());
        properties.put(XnatEventServiceAction.ACTION_DESC, annotation.description());
        properties.put(XnatEventServiceAction.ACTION_EVENT_LIST, annotation.eventList());
        return properties;
    }


    @Override
    protected String getPropertiesName(final TypeElement element, final XnatEventServiceAction plugin) {
    	final String eventName = plugin.name();
        return String.format("META-INF/xnat/event/%s-xnateventserviceaction.properties", eventName.length() > 0 ? eventName : "ESAC" + Long.toString(new Date().getTime()));
    }
    
}
