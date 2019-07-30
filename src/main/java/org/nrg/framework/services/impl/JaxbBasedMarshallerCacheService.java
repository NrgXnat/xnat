/*
 * framework: org.nrg.framework.services.impl.JaxbBasedMarshallerCacheService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services.impl;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.MarshallerCacheService;
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.utilities.Reflection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.util.xml.StaxUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.bind.Marshaller;
import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;

@Service
@Slf4j
public final class JaxbBasedMarshallerCacheService implements MarshallerCacheService {
    @Autowired
    public JaxbBasedMarshallerCacheService(final SerializerService serializer) throws ParserConfigurationException {
        _serializer = serializer;
        _documentBuilder = _serializer.getDocumentBuilder();
    }

    /**
     * Gets the list of packages that can be searched for classes that support
     * submitted XML root element values.
     *
     * @return An array
     */
    @Override
    public List<String> getMarshalablePackages() {
        return _packages;
    }

    /**
     * Sets the list of packages that can be searched for classes that support
     * submitted XML root element values.
     *
     * @param marshalablePackages The list of package names that can be searched.
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired(required = false)
    @Qualifier("marshalablePackages")
    public void setMarshalablePackages(final List<String> marshalablePackages) {
        _packages.addAll(marshalablePackages);
    }

    /**
     * Indicates whether the requested class is supported for marshalling
     * operations.
     *
     * @param clazz The class to test for marshalling support.
     *
     * @return <b>true</b> if the class is supported for marshalling,
     *         <b>false</b> if not.
     */
    @Override
    public boolean supports(Class<?> clazz) {
        log.debug("Asking if {} is supported", clazz.getName());
        return clazz.isAnnotationPresent(XmlRootElement.class);
    }

    /**
     * Marshals the submitted object ({@link #supports(Class) if supported} to
     * a string.
     *
     * @param object The object to be marshaled.
     *
     * @return The resulting XML.
     */
    @Override
    public String marshal(Object object) {
        StreamResult result = new StreamResult(new ByteArrayOutputStream());
        marshal(object, result);
        return result.getOutputStream().toString();
    }

    /**
     * Marshals the object to a {@link Document} object.
     */
    @Override
    public Document marshalToDocument(final Object object) {
        final DOMResult result = new DOMResult(getDocument());
        marshal(object, result);
        return (Document) result.getNode();
    }

    /**
     * Implements the base <b>marshall()</b> method. This accepts the object and
     * returns the results of the marshalling operation in the <b>result</b>
     * parameter.
     *
     * @param object The object to be marshalled.
     * @param result The results of the marshalling operation.
     */
    @Override
    public void marshal(Object object, Result result) {
        log.debug("Attempting to marshall object of type: {}", object.getClass().getName());

        JAXBContext context = getInstance(object.getClass());

        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(object, result);
        } catch (JAXBException exception) {
            throw convertJaxbException(exception);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.oxm.Unmarshaller#unmarshal(javax.xml.transform.Source
     * )
     */
    @Override
    public Object unmarshal(Source source) throws IOException, XmlMappingException {
        final String xmlElementName;
        final String xmlSource;

        try {
            if (source instanceof StreamSource || StaxUtils.isStaxSource(source)) {
                try (final InputStream stream = source instanceof StreamSource ? ((StreamSource) source).getInputStream() : new ByteArrayInputStream(StaxUtils.getXMLStreamReader(source).getText().getBytes())) {
                    final Document document = _serializer.parse(stream);
                    xmlElementName = document.getDocumentElement().getNodeName();
                    xmlSource = getStringFromDocument(document);
                }
            } else if (source instanceof DOMSource) {
                final Node     node     = ((DOMSource) source).getNode();
                final Document document = getDocument();
                document.adoptNode(node);
                document.appendChild(node);
                xmlElementName = document.getDocumentElement().getNodeName();
                xmlSource = getStringFromDocument(document);
            } else {
                log.warn("A transform source of unknown type \"{}\" was submitted, should use StreamSource, StaxSource, or DOMSource types.", source.getClass().getName());
                return null;
            }
        } catch (TransformerException exception) {
            throw new UncategorizedMappingException("There was a transformer exception.", exception);
        } catch (SAXException exception) {
            throw new UncategorizedMappingException("There was a SAX exception.", exception);
        }

        final JAXBContext context = getInstance(xmlElementName);
        if (context == null) {
            log.error("No JAXB context found for elements of type \"{}\". Please check your configuration.", xmlElementName);
            return null;
        }
        try (final InputStream inputStream = new ByteArrayInputStream(xmlSource.getBytes())) {
            return context.createUnmarshaller().unmarshal(new StreamSource(inputStream));
        } catch (JAXBException exception) {
            throw convertJaxbException(exception);
        }
    }

    /**
     * Gets the string from document.
     *
     * @param document the document
     *
     * @return the string from document
     *
     * @throws TransformerException the transformer exception
     */
    private String getStringFromDocument(Document document) throws TransformerException {
        DOMSource          domSource   = new DOMSource(document);
        Writer             writer      = new StringWriter();
        Result             result      = new StreamResult(writer);
        TransformerFactory factory     = TransformerFactory.newInstance();
        Transformer        transformer = factory.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }

    /**
     * Gets an instance of the {@link JAXBContext} class. This gets the
     * <b>JAXBContext</b> appropriate for the <b>clazz</b> parameter from the
     * internal cache.
     *
     * @param clazz The class for which the <b>JAXBContext</b> needs to be
     *              retrieved.
     *
     * @return The appropriate <b>JAXBContext</b> object.
     */
    private static JAXBContext getInstance(final Class<?> clazz) {
        final ContextDescriptor contextDescriptor = new ContextDescriptor(clazz.getName());

        if (cache.containsKey(contextDescriptor)) {
            return cache.get(contextDescriptor);
        }

        if (!clazz.isAnnotationPresent(XmlRootElement.class)) {
            throw new RuntimeException("The class specified is not declared with the XmlRootElement or XmlType annotation.");
        }

        final JAXBContext jaxbContext = newInstance(clazz);
        cacheContext(getXmlRootElementName(clazz), contextDescriptor, jaxbContext);

        return jaxbContext;
    }

    /**
     * Gets an instance of the {@link JAXBContext} class. This gets the
     * <b>JAXBContext</b> appropriate for the <b>clazz</b> parameter from the
     * internal cache.
     *
     * @param xmlElementName The XML element name for which the <b>JAXBContext</b> needs to
     *                       be retrieved.
     *
     * @return The appropriate <b>JAXBContext</b> object.
     */
    private JAXBContext getInstance(final String xmlElementName) {
        if (contextCache.containsKey(xmlElementName)) {
            return cache.get(contextCache.get(xmlElementName));
        }

        final Class<?> clazz = findClassForElement(xmlElementName);
        if (clazz == null) {
            log.warn("Couldn't find class for element \"{}\", please check configuration.", xmlElementName);
            return null;
        }

        final ContextDescriptor contextDescriptor = new ContextDescriptor(clazz.getName());
        final JAXBContext       jaxbContext       = newInstance(clazz);

        cacheContext(xmlElementName, contextDescriptor, jaxbContext);

        return jaxbContext;
    }

    /**
     * Creates a new instance of a <b>JAXBContext</b> object for the submitted
     * class.
     *
     * @param clazz The class for which a new <b>JAXBContext</b> needs to be
     *              created.
     *
     * @return The new <b>JAXBContext</b> object.
     */
    private static JAXBContext newInstance(final Class<?> clazz) {
        try {
            return JAXBContext.newInstance(clazz);
        } catch (JAXBException exception) {
            throw new RuntimeException("Exception occurred creating JAXB unmarshaller for context=" + clazz, exception);
        }
    }

    /**
     * This performs two caching operations for later lookups. The <b>clazz</b>
     * simple name is used to cache the context descriptor. The context
     * descriptor is used to cache the {@link JAXBContext}. This allows lookups
     * by both class name (generally for unmarshalling operations where you can
     * get the XML root element name)
     *
     * @param xmlElementName    the xml element name
     * @param contextDescriptor the context descriptor
     * @param jaxbContext       the jaxb context
     */
    private static void cacheContext(final String xmlElementName, final ContextDescriptor contextDescriptor, final JAXBContext jaxbContext) {
        contextCache.put(xmlElementName, contextDescriptor);
        cache.put(contextDescriptor, jaxbContext);
    }

    /**
     * Find class for element.
     *
     * @param xmlElementName the xml element name
     *
     * @return the class
     */
    private Class<?> findClassForElement(final String xmlElementName) {
        final Iterator<Class<?>> found = Iterables.filter(Iterables.transform(_packages, new Function<String, Class<?>>() {
            @Nullable
            @Override
            public Class<?> apply(@Nullable final String packageName) {
                return findClassForElementInPackage(xmlElementName, packageName);
            }
        }), Predicates.notNull()).iterator();
        return found.hasNext() ? found.next() : null;
    }

    private Class<?> findClassForElementInPackage(final String xmlElementName, final String packageName) {
        try {
            final Optional<Class<?>> found = Iterables.tryFind(Reflection.getClassesForPackage(packageName), new Predicate<Class<?>>() {
                @Override
                public boolean apply(final Class<?> clazz) {
                    return clazz.isAnnotationPresent(XmlRootElement.class) && StringUtils.equals(xmlElementName, resolve(clazz));
                }

                private String resolve(final Class<?> clazz) {
                    final String name = clazz.getAnnotation(XmlRootElement.class).name();
                    return StringUtils.equals(name, "##default") ? StringUtils.uncapitalize(clazz.getSimpleName()) : name;
                }
            });
            return found.orNull();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * <b>Note:</b> Taken from ClassPathScanningCandidateComponentProvider.
     * Resolve the specified base package into a pattern specification for the
     * package search path.
     * <p>
     * The default implementation resolves placeholders against system
     * properties, and converts a "."-based package path to a "/"-based resource
     * path.
     *
     * @param basePackage the base package as specified by the user
     *
     * @return the pattern specification to be used for package searching
     */
    @SuppressWarnings("unused")
    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    /**
     * Gets the xml root element name.
     *
     * @param clazz the clazz
     *
     * @return the xml root element name
     */
    private static String getXmlRootElementName(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(XmlRootElement.class)) {
            return null;
        }

        XmlRootElement annotation = clazz.getAnnotation(XmlRootElement.class);

        if (annotation.name().equals("##default")) {
            String simpleName = clazz.getSimpleName();
            return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
        } else {
            return annotation.name();
        }

    }

    /**
     * Convert the given <code>JAXBException</code> to a {@link XmlMappingException}
     * with an appropriate error message.
     *
     * @param exception <code>JAXBException</code> that occurred
     *
     * @return the corresponding {@link XmlMappingException}
     */
    private XmlMappingException convertJaxbException(JAXBException exception) {
        if (exception instanceof ValidationException) {
            return new ValidationFailureException("JAXB validation exception", exception);
        } else if (exception instanceof MarshalException) {
            return new MarshallingFailureException("JAXB marshalling exception", exception);
        } else if (exception instanceof UnmarshalException) {
            return new UnmarshallingFailureException("JAXB unmarshalling exception", exception);
        } else {
            return new UncategorizedMappingException("Unknown JAXB exception", exception);
        }
    }

    /**
     * Creating the JAXB Context ... javax.xml.bind.JAXBContext.newInstance() is
     * a very slow operation, you can improve your performance if you create a
     * cache of these instances, we do this by wrapping the call to newInstance
     * with the following code
     */
    private static class ContextDescriptor {

        /**
         * The class name.
         */
        private final String className;

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object o) {
            if (o instanceof ContextDescriptor) {
                final ContextDescriptor un = ((ContextDescriptor) o);
                return className.equals(un.className);
            }
            return false;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return (className).hashCode();
        }

        /**
         * Instantiates a new context descriptor.
         *
         * @param className the class name
         */
        ContextDescriptor(String className) {
            this.className = className.trim();
        }
    }

    private Document getDocument() {
        return _documentBuilder.newDocument();
    }

    private static final Map<ContextDescriptor, JAXBContext> cache        = new HashMap<>();
    private static final Map<String, ContextDescriptor>      contextCache = new HashMap<>();

    private final SerializerService _serializer;
    private final DocumentBuilder   _documentBuilder;

    private final List<String> _packages = new ArrayList<>();
}
