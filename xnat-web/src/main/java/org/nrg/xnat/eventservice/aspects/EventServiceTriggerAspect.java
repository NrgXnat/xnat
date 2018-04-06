package org.nrg.xnat.eventservice.aspects;


import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.*;
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

    @AfterReturning(pointcut = "@annotation(org.nrg.xft.utils.EventServiceTrigger) " +
            "&& args(item, user, ..)" +
            "&& execution(* save(..))")
    public void triggerOnItemSave(final JoinPoint joinPoint, ItemI item,UserI user) throws Throwable{
        try {
            String userLogin = user != null ? user.getLogin() : null;

            log.debug("triggerOnItemSave AfterReturning aspect called after " + joinPoint.getSignature().getName() + "." +
                    "  ItemI = " + (item != null ? item.getXSIType() : "null") +
                    "  UserI = " + userLogin);

            if(StringUtils.equals(item.getXSIType(),"arc:project")){
                XnatProjectdataI project = new XnatProjectdata(item);
                eventService.triggerEvent(new ProjectCreatedEvent(project, userLogin), project.getId());

            }else if(item instanceof XnatImagesessiondataI){
                XnatImagesessiondataI session = (XnatImagesessiondataI)item;
                eventService.triggerEvent(new SessionArchiveEvent(session, userLogin), session.getProject());

                // Fire scan archive events
                for (final XnatImagescandataI scan : session.getScans_scan()) {
                    eventService.triggerEvent(new ScanArchiveEvent(scan, userLogin), session.getProject());
                }

            }else if(StringUtils.equals(item.getXSIType(), "xnat:subjectData")){
                XnatSubjectdataI subject = new XnatSubjectdata(item);
                eventService.triggerEvent(new SubjectCreatedEvent((XnatSubjectdataI)item, userLogin), subject.getProject());

            }
        } catch (Throwable e){
            log.error("Exception processing triggerOnItemSave" + e.getMessage());
            throw e;
        }
    }


    @Around( value = "@annotation(org.nrg.xft.utils.EventServiceTrigger) " +
            "&& args(item, user, ..)" +
            "&& execution(* delete(..))")
    public void triggerOnItemDelete(final ProceedingJoinPoint joinPoint, ItemI item, UserI user) throws Throwable{
        try {


            String userLogin = user != null ? user.getLogin() : null;

            log.debug("triggerOnItemDelete Around aspect called after " + joinPoint.getSignature().getName() + "." +
                    "  ItemI = " + (item != null ? item.getXSIType() : "null") +
                    "  UserI = " + userLogin);

            if(StringUtils.equals(item.getXSIType(),"arc:project")){
                XnatProjectdataI project = new XnatProjectdata(item);
                eventService.triggerEvent(new ProjectDeletedEvent(project, userLogin), project.getId());

            }else if(item instanceof XnatImagesessiondataI){
                XnatImagesessiondataI session = (XnatImagesessiondataI)item;
                eventService.triggerEvent(new SessionDeletedEvent(session, userLogin), session.getProject());

            }else if(StringUtils.equals(item.getXSIType(), "xnat:subjectData")){
                XnatSubjectdataI subject = new XnatSubjectdata(item);
                eventService.triggerEvent(new SubjectDeletedEvent((XnatSubjectdataI)item, userLogin), subject.getProject());

            }
            joinPoint.proceed();
        } catch (Throwable e){
            log.error("Exception processing triggerOnItemSave" + e.getMessage());
            throw e;
        }
    }
}
