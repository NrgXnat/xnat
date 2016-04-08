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

@Controller
@RequestMapping(value = "/spawner/elements", produces = "application/json")
public class ManageElementsController {

    @RequestMapping
    public ModelAndView getAvailableElements() {
        return new ModelAndView("spawner/elements", "elements", _service.getAvailableSpawnerElementIds());
    }

    @RequestMapping(value = "{elementId}", method = RequestMethod.GET)
    public ModelAndView getElement(@PathVariable final String elementId) {
        final SpawnerElement element = _service.retrieve(elementId);
        return new ModelAndView("spawner/element", element == null ? "error" : "element", element == null ? "The ID element " + elementId + " was not found in the system." : element);
    }

    @RequestMapping(value = "{elementId}", method = RequestMethod.PUT)
    public ModelAndView setElement(@PathVariable final String elementId, @RequestBody final SpawnerElement element) {
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
