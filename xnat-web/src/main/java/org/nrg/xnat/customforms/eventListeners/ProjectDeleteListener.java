package org.nrg.xnat.customforms.eventListeners;


import lombok.extern.slf4j.Slf4j;
import org.hibernate.NonUniqueObjectException;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xnat.customforms.service.CustomVariableAppliesToService;
import org.nrg.xnat.customforms.service.CustomVariableFormAppliesToService;
import org.nrg.xnat.customforms.service.CustomVariableFormService;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.entities.CustomVariableAppliesTo;
import org.nrg.xnat.entities.CustomVariableForm;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import org.nrg.xdat.om.XnatProjectdata;


import java.util.List;

import static reactor.bus.selector.Selectors.type;

@Slf4j
@Component
public class ProjectDeleteListener implements Consumer<Event<XftItemEvent>> {
    private final XnatUserProvider primaryAdminUserProvider;
    private final CustomVariableAppliesToService selectionService;
    private final CustomVariableFormService formService;
    private final CustomVariableFormAppliesToService customVariableFormAppliesToService;

    @Autowired
    public ProjectDeleteListener(final XnatUserProvider primaryAdminUserProvider,
                                 final CustomVariableAppliesToService selectionService,
                                 final CustomVariableFormService formService,
                                 final CustomVariableFormAppliesToService customVariableFormAppliesToService,
                                 final EventBus eventBus) {
        this.primaryAdminUserProvider = primaryAdminUserProvider;
        this.formService = formService;
        this.selectionService = selectionService;
        this.customVariableFormAppliesToService = customVariableFormAppliesToService;
        eventBus.on(type(XftItemEvent.class), this);
    }

    @Override
    public void accept(Event<XftItemEvent> busEvent) {
        final XftItemEvent xftItemEvent = busEvent.getData();
        if (xftItemEvent == null) {
            return;
        }

        final String xsiType = xftItemEvent.getXsiType();
        final String action = xftItemEvent.getAction();
        final String actionProperty = (String) xftItemEvent.getProperties().get(XftItemEventI.OPERATION);
        if (!(xsiType.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME) && action.equals(XftItemEvent.DELETE))) {
            return;
        }
        final String projectIdStr = xftItemEvent.getId();
        if (projectIdStr == null) {
            return;
        }

        //Project is being deleted; Handle the custom forms associated with the project
        //If there is a form exclusive to the project - for any datatype - delete it
        //If there are other projects sharing the form; remove this project from the association
        //If the project has opted out of a form, remove the optout association
        //Confirm that the Custom Form API which returns the project list, does not refer to this (deleted) project
        synchronized (this){
            List<CustomVariableAppliesTo> siteWideAppliesTo = selectionService.findAllByScopeEntityIdDataType(Scope.Site, null, null);
            List<CustomVariableAppliesTo> projectSpecificAppliesTos = selectionService.findAllByScopeEntityIdDataType(Scope.Project, projectIdStr, null);
            if (projectSpecificAppliesTos != null && !projectSpecificAppliesTos.isEmpty()) {
                for (CustomVariableAppliesTo p: projectSpecificAppliesTos) {
                    List<CustomVariableFormAppliesTo> customVariableFormAppliesTos = p.getCustomVariableFormAppliesTos();
                    for (CustomVariableFormAppliesTo formAppliesTo : customVariableFormAppliesTos) {
                        CustomVariableForm form = formAppliesTo.getCustomVariableForm();
                        long formId = form.getId();
                        //Are there other projects using this form?
                        boolean othersAreUsingTheForm  = false;
                        List<CustomVariableFormAppliesTo> potentiallyOthers = customVariableFormAppliesToService.findByFormId(formId);
                        if (potentiallyOthers != null) {
                            for (CustomVariableFormAppliesTo pp: potentiallyOthers) {
                                CustomVariableAppliesTo ppAppliesTo = pp.getCustomVariableAppliesTo();
                                if (ppAppliesTo.getScope().equals(Scope.Site) || (ppAppliesTo.getScope().equals(Scope.Project) && !ppAppliesTo.getEntityId().equals(projectIdStr))) {
                                    othersAreUsingTheForm = true;
                                    break;
                                }
                            }
                            //If othersAreUsingTheForm the form should not be deleted; only the association should be deleted
                            delete(formAppliesTo, othersAreUsingTheForm);
                        }
                    }
                }
            }
        }
    }

    private void delete(CustomVariableFormAppliesTo single, final boolean othersAreUsingTheForm) {
        final long formId =  single.getCustomVariableForm().getId();
        final long appliesToId = single.getCustomVariableAppliesTo().getId();
        try {
            customVariableFormAppliesToService.delete(single);
        } catch (NonUniqueObjectException e) {
            formService.evict(single.getCustomVariableForm());
            selectionService.evict(single.getCustomVariableAppliesTo());
            customVariableFormAppliesToService.delete(single);
        }
        selectionService.delete(appliesToId);
        if (!othersAreUsingTheForm) {
            formService.delete(formId);
        }
    }
}
