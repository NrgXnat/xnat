/*
 * framework: org.nrg.framework.processors.XnatTaskExecutionResolverAnnotationProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.processors;

import org.kohsuke.MetaInfServices;
import org.nrg.framework.task.XnatTask;
import org.nrg.framework.task.XnatTaskExecutionResolver;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Processes the {@link XnatTask} annotation.  This annotation notates a class that might require node-specific handling
 * (e.g. that task should execute only on a single node in a multi-node environment). 
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("org.nrg.framework.task.XnatTaskExecutionResolver")
public class XnatTaskExecutionResolverAnnotationProcessor extends NrgAbstractAnnotationProcessor<XnatTaskExecutionResolver> {

    /* (non-Javadoc)
     * @see org.nrg.framework.processors.NrgAbstractAnnotationProcessor#processAnnotation(javax.lang.model.element.TypeElement, java.lang.annotation.Annotation)
     */
    @Override
    protected Map<String, String> processAnnotation(final TypeElement element, final XnatTaskExecutionResolver resolver) {
        final Map<String, String> properties = new LinkedHashMap<>();
        properties.put(XnatTaskExecutionResolver.CLASS, element.getQualifiedName().toString());
        properties.put(XnatTaskExecutionResolver.RESOLVER_ID, resolver.resolverId());
        properties.put(XnatTaskExecutionResolver.DESCRIPTION, resolver.description());
        properties.put("javaClass", element.getQualifiedName().toString());
        return properties;
    }

    /* (non-Javadoc)
     * @see org.nrg.framework.processors.NrgAbstractAnnotationProcessor#getPropertiesName(java.lang.annotation.Annotation)
     */
    @Override
    protected String getPropertiesName(final TypeElement element, final XnatTaskExecutionResolver resolver) {
    	final String resolverId = resolver.resolverId();
        return String.format("META-INF/xnat/task/%s-xnat-task-execution-resolver.properties", resolverId);
    }
    
}
