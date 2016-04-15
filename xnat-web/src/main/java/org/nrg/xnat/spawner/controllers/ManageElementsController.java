package org.nrg.xnat.spawner.controllers;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xnat.spawner.entities.SpawnerElement;
import org.nrg.xnat.spawner.exceptions.InvalidElementIdException;
import org.nrg.xnat.spawner.services.SpawnerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("SpringMVCViewInspection")
@Controller
@RequestMapping(value = "/spawner/manage", produces = "application/json")
public class ManageElementsController {

    @RequestMapping
    public ModelAndView getNamespaces() {
        final List<String> namespaces = _service.getNamespaces();
        return new ModelAndView("spawner/elements", "namespaces", namespaces);
    }

    @RequestMapping("elements")
    public ModelAndView getDefaultElements() {
        return getNamespacedElements(SpawnerElement.DEFAULT_NAMESPACE);
    }

    @RequestMapping("elements/{namespace}")
    public ModelAndView getNamespacedElements(@PathVariable("namespace") final String namespace) {
        final Map<String, Object> models = new HashMap<>();
        models.put("namespace", namespace);
        models.put("namespaces", _service.getNamespaces());
        models.put("elements", _service.getDefaultElements());
        return new ModelAndView("spawner/elements", models);
    }

    @RequestMapping(value = "element/{elementId}", method = RequestMethod.GET)
    public ModelAndView getDefaultElement(@PathVariable final String elementId) {
        return getNamespacedElement(SpawnerElement.DEFAULT_NAMESPACE, elementId);
    }

    @RequestMapping(value = "element/{namespace}/{elementId}", method = RequestMethod.GET)
    public ModelAndView getNamespacedElement(@PathVariable("namespace") final String namespace, @PathVariable final String elementId) {
        final SpawnerElement element = _service.retrieve(namespace, elementId);
        return new ModelAndView("spawner/element", element == null ? "error" : "elementId", element == null ? "The ID element " + elementId + " was not found in the system." : elementId);
    }

    @RequestMapping(value = "element/{elementId}", method = RequestMethod.PUT)
    public ModelAndView setDefaultElement(@PathVariable final String elementId, @RequestBody final SpawnerElement element) {
        return setNamespacedElement(SpawnerElement.DEFAULT_NAMESPACE, elementId, element);
    }

    @RequestMapping(value = "element/{namespace}/{elementId}", method = RequestMethod.PUT)
    public ModelAndView setNamespacedElement(@PathVariable final String namespace, @PathVariable final String elementId, @RequestBody final SpawnerElement element) {
        if (element == null) {
            return new ModelAndView("spawner/element", "error", "No valid spawner element was found in your submitted data.");
        }
        final SpawnerElement existing       = _service.retrieve(elementId);
        final boolean        isModElementId = !StringUtils.equals(existing.getElementId(), element.getElementId());
        final boolean        isModYaml      = !StringUtils.equals(existing.getYaml(), element.getYaml());
        if (isModElementId || isModYaml) {
            if (isModElementId) {
                try {
                    existing.setElementId(element.getElementId());
                } catch (InvalidElementIdException e) {
                    return new ModelAndView("spawner/element", "error", "The element ID " + element.getElementId() + " in your submitted data is invalid. Check for duplicated or invalid values.");
                }
            }
            if (isModYaml) {
                existing.setYaml(element.getYaml());
            }
            _service.update(existing);
            return new ModelAndView("spawner/element", "element", element);
        }
        return new ModelAndView("spawner/element", "error", "The submitted spawner element wasn't modified, not updated.");
    }

    @Inject
    private SpawnerService _service;
}
