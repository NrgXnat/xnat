package org.nrg.xnat.customforms.eventListeners;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.customforms.events.CustomFormEvent;
import org.nrg.xnat.customforms.service.CustomVariableFormService;
import org.nrg.xnat.customforms.service.FormDisplayFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import static reactor.bus.selector.Selectors.type;

/**
 * Accepts an Update and Delete event of a form
 */

@Slf4j
@Component
public class CustomFormEventListener implements Consumer<Event<CustomFormEvent>> {

    private final CustomVariableFormService formService;
    private final FormDisplayFieldService formDisplayFieldService;

    @Autowired
    public CustomFormEventListener(final CustomVariableFormService formService,
                                   final FormDisplayFieldService formDisplayFieldService,
                                   final EventBus eventBus) {
        this.formService = formService;
        this.formDisplayFieldService = formDisplayFieldService;
        eventBus.on(type(CustomFormEvent.class), this);
    }

    /**
     * Listens and acts upon a CustomForm CREATE, UPDATE and DELETE events
     * @param busEvent
     */

    @Override
    public void accept(Event<CustomFormEvent> busEvent) {
        final CustomFormEvent customFormEvent = busEvent.getData();
        if (customFormEvent == null) {
            return;
        }

        final String xsiType = customFormEvent.getXsiType();
        final String formUUIDAsString = customFormEvent.getUuid();
        final String action = customFormEvent.getAction();
        if (action.equals(CustomFormEvent.UPDATE) || action.equals(CustomFormEvent.CREATE)) {
            formDisplayFieldService.reloadDisplayFieldsForForm(xsiType, formUUIDAsString, action.equals(CustomFormEvent.UPDATE)?true:false);
        }else if (action.equals(CustomFormEvent.DELETE)) {
            formDisplayFieldService.removeDisplayFieldsForForm(xsiType, formUUIDAsString);
        }
    }
}
