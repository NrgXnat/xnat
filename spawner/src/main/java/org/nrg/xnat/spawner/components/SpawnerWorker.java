/*
 * spawner: org.nrg.xnat.spawner.components.SpawnerWorker
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.utilities.TreeNode;
import org.nrg.xnat.spawner.entities.SpawnerElement;
import org.nrg.xnat.spawner.exceptions.CircularReferenceException;
import org.nrg.xnat.spawner.exceptions.InvalidElementIdException;
import org.nrg.xnat.spawner.services.SpawnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SpawnerWorker {
    @Autowired
    public SpawnerWorker(final SerializerService serializer) {
        _serializer = serializer;
    }

    public SpawnerWorker spawnerService(final SpawnerService service) {
        _service = service;
        return this;
    }

    public TreeNode<SpawnerElement> resolve(final SpawnerElement element) throws InvalidElementIdException, CircularReferenceException {
        final String elementId = element.getElementId();
        log.debug("Resolving element {}", elementId);
        final TreeNode<SpawnerElement> treeNode = new TreeNode<>(element);
        validate(treeNode);

        final List<List<String>> references = extractReferencedIds(element.getNamespace(), element.getYaml());
        log.debug("Found {} references in the element {}", references.size(), elementId);
        for (final List<String> reference : references) {
            final String namespace   = StringUtils.isNotBlank(reference.getFirst()) ? reference.getFirst() : element.getNamespace();
            final String referenceId = reference.get(1);
            log.debug("Processing new element {}:{}", namespace, referenceId);
            final SpawnerElement child = _service.retrieve(namespace, referenceId);
            if (child == null) {
                throw new InvalidElementIdException(namespace, referenceId);
            }
            treeNode.addChild(resolve(child));
        }
        return treeNode;
    }

    public String compose(final TreeNode<SpawnerElement> treeNode) {
        final String yaml = treeNode.getData().getYaml();
        return new StringSubstitutor(treeNode.getChildren().stream()
                                             .collect(Collectors.toMap(child -> child.getData().getElementId(),
                                                                       child -> getIndent(child.getData().getElementId(), yaml, compose(child)))),
                                     "${", "}").replace(yaml);
    }

    public SpawnerElement parse(final String yaml) throws IOException, InvalidElementIdException {
        final SpawnerElement element = new SpawnerElement();
        if (StringUtils.isBlank(yaml)) {
            return element;
        }
        final JsonNode jsonNode = validateYaml(yaml);
        element.setYaml(yaml);
        final Matcher elementIdCandidate = SpawnerService.REGEX_NEW_ELEMENT.matcher(yaml.trim().split("\\r?\\n")[0]);
        if (elementIdCandidate.find()) {
            element.setElementId(elementIdCandidate.group(1));
        }
        final JsonNode label = jsonNode.findValue("label");
        element.setLabel(label != null ? label.asText(element.getElementId()) : element.getElementId());
        final JsonNode description = jsonNode.findValue("description");
        element.setDescription(description != null ? description.asText("") : "");
        final String restrictedTo = extractRestrictedTo(jsonNode);
        if (null != restrictedTo) {
            element.setRestricted(restrictedTo);
        }
        return element;
    }

    public String extractRestrictedTo(final String yaml)  {
        try {
            return extractRestrictedTo(validateYaml(yaml));
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An unknown error occurred processing the YAML :\n" + yaml, e);
        }
    }

    public String extractRestrictedTo(final JsonNode yamlNode) {
        String restricted = null;
        final JsonNode metaNode = yamlNode.findValue("meta");
        if (metaNode != null) {
            final JsonNode restrictedNode = metaNode.findValue("restricted");
            if (restrictedNode != null) {
               restricted = restrictedNode.asText();
            }
        }
        return restricted;
    }

    @SuppressWarnings("UnusedReturnValue")
    public String validateElement(final String elementId, final String yaml) {
        try {
            validateYaml(yaml);
            return yaml;
        } catch (JsonProcessingException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "An error was found in the YAML for element ID " + elementId + ". The found YAML is:\n" + yaml, e);
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An unknown error occurred processing the YAML for element ID " + elementId + ". The found YAML is:\n" + yaml, e);
        }
    }

    private JsonNode validateYaml(final String yaml) throws IOException {
        return _serializer.deserializeYaml(yaml);
    }

    private void validate(final TreeNode<SpawnerElement> treeNode) throws CircularReferenceException {
        final List<TreeNode<SpawnerElement>> ancestry = treeNode.getAncestry();
        if (ancestry.contains(treeNode)) {
            Collections.reverse(ancestry);
            final StringBuilder message   = new StringBuilder();
            final String        elementId = treeNode.getData().getElementId();
            message.append("The element ").append(elementId).append(" is its own ancestor, which indicates a circular reference: ");
            for (final TreeNode<SpawnerElement> node : ancestry) {
                final String nodeId = node.getData().getElementId();
                if (elementId.equals(nodeId)) {
                    message.append("*").append(nodeId).append("*").append("->");
                } else {
                    message.append(nodeId).append("->");
                }
            }
            message.append("*").append(elementId).append("*");
            throw new CircularReferenceException(message.toString());
        }
    }

    private List<List<String>> extractReferencedIds(final String defaultNamespace, final String yaml) {
        final List<List<String>> matches = new ArrayList<>();
        final Matcher            matcher = ELEMENT_REFERENCE.matcher(yaml);
        while (matcher.find()) {
            final String ns      = matcher.group("namespace");
            final String element = matcher.group("element");
            matches.add(Arrays.asList(StringUtils.isNotBlank(ns) ? ns : defaultNamespace, element));
        }
        return matches;
    }

    private String getIndent(final String elementId, final String yaml, final String childText) {
        final Matcher matcher = Pattern.compile("^(\\s+)[$][{]" + elementId + "[}]", Pattern.MULTILINE).matcher(yaml);
        if (matcher.find()) {
            final String group = matcher.group(1);
            return childText.replaceAll("(?m)^", group).trim();
        }
        return childText;
    }

    private static final Pattern ELEMENT_REFERENCE = Pattern.compile("[$][{](?<namespace>[0-9a-zA-Z_]+(?::))?(?<element>[0-9a-zA-Z_-]+)[}]");

    private       SpawnerService    _service;
    private final SerializerService _serializer;
}
