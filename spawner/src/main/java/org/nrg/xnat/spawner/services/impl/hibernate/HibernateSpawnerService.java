/*
 * spawner: org.nrg.xnat.spawner.services.impl.hibernate.HibernateSpawnerService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.services.impl.hibernate;

import com.fasterxml.jackson.dataformat.yaml.JacksonYAMLParseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.framework.utilities.NameUtils;
import org.nrg.framework.utilities.TreeNode;
import org.nrg.xnat.spawner.components.SpawnerWorker;
import org.nrg.xnat.spawner.entities.SpawnerElement;
import org.nrg.xnat.spawner.exceptions.CircularReferenceException;
import org.nrg.xnat.spawner.exceptions.InvalidElementIdException;
import org.nrg.xnat.spawner.preferences.SpawnerPreferences;
import org.nrg.xnat.spawner.repositories.SpawnerElementRepository;
import org.nrg.xnat.spawner.services.SpawnerResourceLocator;
import org.nrg.xnat.spawner.services.SpawnerService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

@Service
@Transactional
@Slf4j
public class HibernateSpawnerService extends AbstractHibernateEntityService<SpawnerElement, SpawnerElementRepository> implements SpawnerService {
    @Autowired
    public HibernateSpawnerService(final SpawnerWorker worker, final SpawnerPreferences preferences, final List<SpawnerResourceLocator> resourceLocators) {
        _worker = worker.spawnerService(this);
        _preferences = preferences;
        _resourceLocators = resourceLocators;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(final boolean forcePurge) throws BeansException {
        try {
            if (forcePurge || _preferences.getPurgeAndRefreshOnStartup()) {
                if (forcePurge) {
                    log.debug("Called with parameter indicating spawner namespaces should be purged and reloaded.");
                } else {
                    log.debug("Spawner preferences indicates spawner namespaces should be purged and reloaded.");
                }
                getNamespaces().forEach(this::deleteNamespace);
            }

            final List<SpawnerElement> elements = getSpawnerElementDefinitions();
            if (!elements.isEmpty()) {
                log.debug("Preparing to process {} spawner element definitions to look for new spawner elements.", elements.size());
                elements.stream().filter(element -> !getNamespacedElementIds(element.getNamespace()).contains(element.getElementId())).forEach(this::create);
                for (final SpawnerElement element : elements) {
                    if (!getNamespacedElementIds(element.getNamespace()).contains(element.getElementId())) {
                        create(element);
                    }
                }
            }
        } catch (InvalidElementIdException e) {
            throw new BeanInitializationException("An error occurred loading the spawner element definitions.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpawnerElement newEntity(final Object... parameters) {
        // If there are two parameters, they're using the constructor with a YAML string as the second parameter.
        if (parameters.length == 2) {
            if (parameters[0] instanceof String && parameters[1] instanceof String) {
                _worker.validateElement((String) parameters[0], (String) parameters[1]);
            } else {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The parameters passed into this method should be String, String, you submitted " + parameters[0].getClass().getName() + ", " + parameters[1].getClass().getName() + ".");
            }
        }
        return super.newEntity(parameters);
    }

    @Override
    public SpawnerElement create(final SpawnerElement element) {
        log.debug("Creating a new spawner element with element ID {}", element.getElementId());
        return super.create(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final SpawnerElement entity) {
        _worker.validateElement(entity.getElementId(), entity.getYaml());
        final String restrictedTo = _worker.extractRestrictedTo(entity.getYaml());
        if (restrictedTo != null) {
            entity.setRestricted(restrictedTo);
        }
        super.update(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getNamespaces() {
        final List<String> namespaces = getDao().getNamespaces();
        log.debug("Fetching available namespaces for the system, found {} total.", namespaces.size());
        return namespaces;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object[]> getNamespacesAndResrictions() {
        final List<Object[]> namespaces = getDao().getNamespacesAndResrictions();
        log.debug("Fetching available namespaces and restrictions for the system, found {} total.", namespaces.size());
        return namespaces;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshNamespace(final String namespace) throws InvalidElementIdException {
        final List<SpawnerElement> definitions = getSpawnerElementDefinitions(namespace);
        for (final SpawnerElement element : definitions) {
            final SpawnerElement retrieved = retrieve(namespace, element.getElementId());
            if (retrieved == null) {
                create(element);
            } else {
                retrieved.updateFrom(element);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void purgeAndRefreshNamespace(final String namespace) throws InvalidElementIdException {
        deleteNamespace(namespace);
        refreshNamespace(namespace);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNamespace(final String namespace) {
        log.debug("Purging namespace {}", namespace);
        getDao().getNamespaceElements(namespace).forEach(this::delete);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getDefaultElementIds() {
        return getNamespacedElementIds(SpawnerElement.DEFAULT_NAMESPACE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getNamespacedElementIds(final String namespace) {
        final List<String> elements = getDao().getNamespaceElementIds(namespace);
        log.debug("Fetching available spawner element IDs for {}, found {} total.", StringUtils.isBlank(namespace) ? "the default namespace" : "namespace " + namespace, elements.size());
        return elements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SpawnerElement> getDefaultElements() {
        return getNamespacedElements(SpawnerElement.DEFAULT_NAMESPACE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SpawnerElement> getNamespacedElements(final String namespace) {
        final List<SpawnerElement> elements = getDao().getNamespaceElements(namespace);
        log.debug("Fetching available spawner elements for {}, found {} total.", StringUtils.isBlank(namespace) ? "the default namespace" : "namespace " + namespace, elements.size());
        return elements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpawnerElement retrieve(final String elementId) {
        return retrieve(SpawnerElement.DEFAULT_NAMESPACE, elementId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpawnerElement retrieve(final String namespace, final String elementId) {
        log.debug("Retrieving {}", getFormattedElementId(namespace, elementId));
        return getDao().getElement(namespace, elementId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve(final String elementId) throws InvalidElementIdException, CircularReferenceException {
        return resolve(SpawnerElement.DEFAULT_NAMESPACE, elementId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve(final String namespace, final String elementId) throws CircularReferenceException, InvalidElementIdException {
        log.debug("Fetching {}", getFormattedElementId(namespace, elementId));
        final SpawnerElement root = retrieve(namespace, elementId);
        if (root == null) {
            return null;
        }
        final TreeNode<SpawnerElement> tree = _worker.resolve(root);
        log.debug("Found a total of {} elements referenced from {}", tree.size(), getFormattedElementId(namespace, elementId));
        return _worker.compose(tree);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpawnerElement parse(final String yaml) throws InvalidElementIdException, IOException {
        return _worker.parse(yaml);
    }

    private List<SpawnerElement> getSpawnerElementDefinitions() throws InvalidElementIdException {
        final List<SpawnerElement> elements = new ArrayList<>();
        try {
            for (final SpawnerResourceLocator locator : _resourceLocators) {
                for (final Resource resource : locator.getResources()) {
                    elements.addAll(importElementsFromResource(resource));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred trying to locate XNAT spawner element definitions.", e);
        }
        return elements;
    }

    private List<SpawnerElement> getSpawnerElementDefinitions(final String namespace) throws InvalidElementIdException {
        final List<SpawnerElement> elements               = new ArrayList<>();
        final String               definitionResourceName = getResourceNameFromNamespace(namespace);
        try {
            for (final SpawnerResourceLocator locator : _resourceLocators) {
                for (final Resource resource : locator.getResources()) {
                    if (StringUtils.equals(definitionResourceName, resource.getFilename())) {
                        elements.addAll(importElementsFromResource(resource));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred trying to locate XNAT spawner element definitions.", e);
        }

        return elements;
    }

    private List<SpawnerElement> importElementsFromResource(final Resource resource) throws IOException, InvalidElementIdException {
        final String               namespace = getNamespaceFromResourceName(getResourcePath(resource));
        final List<SpawnerElement> elements  = new ArrayList<>();
        try (final InputStream input = resource.getInputStream(); final BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String        line;
            StringBuilder incoming = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                final Matcher newElement = REGEX_NEW_ELEMENT.matcher(line);
                if (newElement.find()) {
                    if (incoming.length() > 0) {
                        addParsedElement(resource, namespace, elements, incoming);
                    }
                    incoming = new StringBuilder(line).append(System.lineSeparator());
                } else {
                    final Matcher comment = REGEX_COMMENT.matcher(line);
                    if (comment.matches()) {
                        log.debug("Ignoring line from incoming YAML because it's a comment: {}", line);
                    } else {
                        incoming.append(line).append(System.lineSeparator());
                    }
                }
            }
            if (incoming.length() > 0) {
                addParsedElement(resource, namespace, elements, incoming);
            }
        }
        return elements;
    }

    private String getResourcePath(final Resource resource) {
        if (resource instanceof UrlResource) {
            try {
                return resource.getURI().toString();
            } catch (IOException e) {
                log.warn("Got an error trying to get the URI of a UrlResource, using toString: {}", resource);
                return resource.toString();
            }
        }
        if (resource instanceof FileSystemResource) {
            return ((FileSystemResource) resource).getPath();
        }
        if (resource instanceof ClassPathResource) {
            return ((ClassPathResource) resource).getPath();
        }
        if (resource instanceof InputStreamResource) {
            try {
                return resource.getURI().toString();
            } catch (IOException e) {
                log.warn("Got an error trying to get the URI of an InputStreamResource, using toString: {}", resource);
                return resource.toString();
            }
        }
        log.info("No standard way to return a path from a resource of type {}, using toString: {}", resource.getClass(), resource);
        return resource.toString();
    }

    private void addParsedElement(final Resource resource, final String namespace, final List<SpawnerElement> elements, final StringBuilder incoming) throws InvalidElementIdException, IOException {
        try {
            final SpawnerElement element = parse(incoming.toString());
            if (StringUtils.isBlank(element.getElementId())) {
                log.warn("Ignoring possibly valid YAML in the resource located at {}: no element ID was specified before encountering indented text:{}{}", resource.getURI(), System.lineSeparator(), element.getYaml());
            } else {
                element.setNamespace(namespace);
                elements.add(element);
            }
        } catch (JacksonYAMLParseException e) {
            log.error("An error occurred trying to parse the YAML from the resource located at {} in namespace {}: {}", resource.getURI(), namespace, incoming, e);
        }
    }

    /**
     * Converts from a resource name, usually a package name or path delimited by the '/' or '.' characters between each
     * token and words within each token delimited by the '-' character, to a namespace, with tokens separated by the ':'
     * character and words within each token delimited by camel caps.
     *
     * @param resource The resource name, path, or package.
     *
     * @return The namespace converted from the resource name.
     */
    private String getNamespaceFromResourceName(final String resource) {
        final Matcher matcher = REGEX_RESOURCE_NAMESPACE.matcher(resource);
        return matcher.find() ? StringUtils.join(NameUtils.convertResourceNamesToBeanIds(Arrays.asList(matcher.group("nselement").split("[/.]"))), ":") : null;
    }

    /**
     * Converts from a namespace, with tokens separated by the ':' character and words within each token delimited by
     * camel caps, to a resource name, usually a package name or path delimited by the '/' character between each token
     * and words within each token delimited by the '-' character.
     *
     * @param namespace The namespace.
     *
     * @return The resource name converted from the namespace.
     */
    private String getResourceNameFromNamespace(final String namespace) {
        return StringUtils.join(NameUtils.convertBeanIdsToResourceNames(Arrays.asList(namespace.split(":"))), "/") + "-elements.yaml";
    }

    private String getFormattedElementId(final String namespace, final String elementId) {
        return (StringUtils.isBlank(namespace) ? SpawnerElement.DEFAULT_NAMESPACE : namespace + " namespace") + " element " + elementId;
    }

    private final SpawnerWorker                _worker;
    private final SpawnerPreferences           _preferences;
    private final List<SpawnerResourceLocator> _resourceLocators;
}
