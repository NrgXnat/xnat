/*
 * framework: org.nrg.framework.scope.SimpleEntityResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.scope;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SimpleEntityResolver implements EntityResolver<Wired> {

    public SimpleEntityResolver() throws IOException {
        final Site site = new ObjectMapper().readValue(SITE_MAP, Site.class);
        _registry.put(site.getEntityId(), site);
        for (final Project project : site.getProjects()) {
            project.setParentEntityId(site.getEntityId());
            _registry.put(project.getEntityId(), project);
            _results.put(project.getEntityId(), project.isWired() ? project.getEntityId() : site.getEntityId());
            for (final Subject subject : project.getSubjects()) {
                subject.setParentEntityId(project.getEntityId());
                _registry.put(subject.getEntityId(), subject);
                _results.put(subject.getEntityId(), subject.isWired() ? subject.getEntityId() : project.isWired() ? project.getEntityId() : site.getEntityId());
            }
        }
    }

    public Wired getWired(final EntityId entityId) {
        if (!_registry.containsKey(entityId)) {
            throw new RuntimeException("No entity found for ID " + entityId);
        }
        return _registry.get(entityId);
    }

    @Override
    public Wired resolve(final EntityId entityId, Object... parameters) {
        final List<EntityId> hierarchy = getHierarchy(entityId);
        for (final EntityId candidate : hierarchy) {
            final Wired wired = getWired(candidate);
            if (wired.isWired()) {
                return wired;
            }
        }
        return null;
    }

    @Override
    public List<EntityId> getHierarchy(final EntityId entityId) {
        EntityId current = new EntityId(entityId.getScope(), entityId.getEntityId());

        final List<EntityId> hierarchy = new ArrayList<>();
        switch (entityId.getScope()) {
            case Subject:
                hierarchy.add(current);
                current = getWired(current).getParentEntityId();

            case Project:
                hierarchy.add(current);
                current = getWired(current).getParentEntityId();

            case Site:
                hierarchy.add(current);
                return hierarchy;

            default:
                throw new RuntimeException("Unknown scope " + entityId.getScope());
        }
    }

    public boolean checkResults(final EntityId start, final EntityId finish) {
        return _results.get(start).equals(finish);
    }

    private static final String SITE_MAP = "{\n" +
            "    \"wired\": true,\n" +
            "    \"projects\": [\n" +
            "        {\n" +
            "            \"id\": \"project1\",\n" +
            "            \"wired\": true,\n" +
            "            \"subjects\": [\n" +
            "                { \"id\": \"p1s1\", \"wired\": false },\n" +
            "                { \"id\": \"p1s2\", \"wired\": true },\n" +
            "                { \"id\": \"p1s3\", \"wired\": false },\n" +
            "                { \"id\": \"p1s4\", \"wired\": true },\n" +
            "                { \"id\": \"p1s5\", \"wired\": false },\n" +
            "                { \"id\": \"p1s6\", \"wired\": true }\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\": \"project2\",\n" +
            "            \"wired\": false,\n" +
            "            \"subjects\": [\n" +
            "                { \"id\": \"p2s1\", \"wired\": true },\n" +
            "                { \"id\": \"p2s2\", \"wired\": false },\n" +
            "                { \"id\": \"p2s3\", \"wired\": true },\n" +
            "                { \"id\": \"p2s4\", \"wired\": false },\n" +
            "                { \"id\": \"p2s5\", \"wired\": true },\n" +
            "                { \"id\": \"p2s6\", \"wired\": false }\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    private final Map<EntityId, Wired> _registry = new HashMap<>();
    private final Map<EntityId, EntityId> _results  = new HashMap<>();
}
