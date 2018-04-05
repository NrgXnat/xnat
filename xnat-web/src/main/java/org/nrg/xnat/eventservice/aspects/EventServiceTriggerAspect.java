package org.nrg.xnat.eventservice.aspects;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.ProjectCreatedEvent;
import org.nrg.xnat.eventservice.events.ScanArchiveEvent;
import org.nrg.xnat.eventservice.events.SessionArchiveEvent;
import org.nrg.xnat.eventservice.events.SubjectCreatedEvent;
import org.nrg.xnat.eventservice.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class EventServiceTriggerAspect {
    private static final Logger log = LoggerFactory.getLogger(EventServiceTriggerAspect.class);

    private EventService eventService;

    @Autowired
    public EventServiceTriggerAspect(EventService eventService) {
        this.eventService = eventService;
    }

    @AfterReturning(pointcut = "@annotation(org.nrg.xft.utils.EventServiceSaveTrigger) && args(item, user, ..)")
    public void triggerOnItemSave(final JoinPoint joinPoint, ItemI item,UserI user) throws Throwable{
        try {
            log.debug("triggerOnItemSave AfterReturning aspect called after " + joinPoint.getSignature().getName() + ".");
            log.debug("ItemI = " + (item != null ? item.getXSIType() : "null"));

            String userLogin = user != null ? user.getLogin() : null;
            log.debug("UserI = " + userLogin);

            if(item instanceof XnatProjectdataI){
                XnatProjectdataI project = (XnatProjectdataI)item;
                eventService.triggerEvent(new ProjectCreatedEvent(project, userLogin), project.getId());

            }else if(item instanceof XnatImagesessiondataI){
                XnatImagesessiondataI session = (XnatImagesessiondataI)item;
                eventService.triggerEvent(new SessionArchiveEvent(session, userLogin), session.getProject());

                // Fire scan archive events
                for (final XnatImagescandataI scan : session.getScans_scan()) {
                    eventService.triggerEvent(new ScanArchiveEvent(scan, userLogin), session.getProject());
                }

            }else if(item instanceof XnatSubjectdataI){
                XnatSubjectdataI subject = (XnatSubjectdataI)item;
                eventService.triggerEvent(new SubjectCreatedEvent((XnatSubjectdataI)item, userLogin), subject.getProject());

            }
        } catch (Throwable e){
            log.error("Exception processing triggerOnItemSave" + e.getMessage());
            throw e;
        }
    }

}
