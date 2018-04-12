package org.nrg.xnat.eventservice.aspects;


import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.om.XnatImagesessiondata;
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

            if(!(StringUtils.equals(item.getXSIType(), "xnat:subjectData") || StringUtils.containsIgnoreCase(item.getXSIType(),"SessionData") || StringUtils.equals(item.getXSIType(), "xnat:projectData") ))
                return;

            log.debug("triggerOnItemSave AfterReturning aspect called after " + joinPoint.getSignature().getName() + "." +
                    "  ItemI type = " + (item != null ? item.getClass().getSimpleName() : "null") +
                    "  ItemI xsiType = " + (item != null ? item.getXSIType() : "null") +
                    "  UserI = " + userLogin);
            log.debug("\n\n" + item.getItem().toString() + "\n\n");

        } catch (Throwable e){
            log.error("Exception processing triggerOnItemSave" + e.getMessage());
            throw e;
        }
    }


    @AfterReturning(pointcut = "@annotation(org.nrg.xft.utils.EventServiceTrigger) " +
            "&& args(item, user, ..)" +
            "&& execution(* save(..))")
    public void triggerOnProjectSave(final JoinPoint joinPoint, XnatProjectdata item,UserI user) throws Throwable{
        try {
            String userLogin = user != null ? user.getLogin() : null;

            log.debug("triggerOnProjectSave AfterReturning aspect called after " + joinPoint.getSignature().getName() + "." +
                    "  ItemI type = " + (item != null ? item.getClass().getSimpleName() : "null") +
                    "  ItemI xsiType = " + (item != null ? item.getXSIType() : "null") +
                    "  UserI = " + userLogin);
            eventService.triggerEvent(new ProjectCreatedEvent(item, userLogin), item.getId());
        } catch (Throwable e){
            log.error("Exception processing triggerOnProjectSave" + e.getMessage());
            throw e;
        }
    }

    @AfterReturning(pointcut = "@annotation(org.nrg.xft.utils.EventServiceTrigger) " +
            "&& args(item, user, ..)" +
            "&& execution(* save(..))")
    public void triggerOnSubjectSave(final JoinPoint joinPoint, XnatSubjectdata item,UserI user) throws Throwable{
        try {
            String userLogin = user != null ? user.getLogin() : null;

            log.debug("triggerOnSubjectSave AfterReturning aspect called after " + joinPoint.getSignature().getName() + "." +
                    "  ItemI type = " + (item != null ? item.getClass().getSimpleName() : "null") +
                    "  ItemI xsiType = " + (item != null ? item.getXSIType() : "null") +
                    "  UserI = " + userLogin);
            eventService.triggerEvent(new SubjectCreatedEvent(item, userLogin), item.getProject());
        } catch (Throwable e){
            log.error("Exception processing triggerOnSubjectSave" + e.getMessage());
            throw e;
        }
    }

    @AfterReturning(pointcut = "@annotation(org.nrg.xft.utils.EventServiceTrigger) " +
            "&& args(item, user, ..)" +
            "&& execution(* save(..))")
    public void triggerOnSessionSave(final JoinPoint joinPoint, XnatImagesessiondata item, UserI user) throws Throwable{
        try {
            String userLogin = user != null ? user.getLogin() : null;

            log.debug("triggerOnSessionSave AfterReturning aspect called after " + joinPoint.getSignature().getName() + "." +
                    "  ItemI type = " + (item != null ? item.getClass().getSimpleName() : "null") +
                    "  ItemI xsiType = " + (item != null ? item.getXSIType() : "null") +
                    "  UserI = " + userLogin);
            eventService.triggerEvent(new SessionArchiveEvent(item, userLogin), item.getProject());
            // Fire scan archive events
            for (final XnatImagescandataI scan : item.getScans_scan()) {
                eventService.triggerEvent(new ScanArchiveEvent(scan, userLogin), item.getProject());
            }
        } catch (Throwable e){
            log.error("Exception processing triggerOnSessionSave" + e.getMessage());
            throw e;
        }
    }

    @AfterReturning(pointcut = "@annotation(org.nrg.xft.utils.EventServiceTrigger) " +
            "&& args(item, user, ..)" +
            "&& execution(* save(..))")
    public void triggerOnUserSave(final JoinPoint joinPoint, XdatUser item, UserI user) throws Throwable{
        try {
            String userLogin = user != null ? user.getLogin() : null;

            log.debug("triggerOnUserSave AfterReturning aspect called after " + joinPoint.getSignature().getName() + "." +
                    "  ItemI type = " + (item != null ? item.getClass().getSimpleName() : "null") +
                    "  ItemI xsiType = " + (item != null ? item.getXSIType() : "null") +
                    "  UserI = " + userLogin);
            //eventService.triggerEvent(new SessionArchiveEvent(item, userLogin), item.getId());

        } catch (Throwable e){
            log.error("Exception processing triggerOnUserSave" + e.getMessage());
            throw e;
        }
    }



    @AfterReturning(pointcut = "@annotation(org.nrg.xft.utils.EventServiceTrigger) " +
            "&& args(item, user, ..)" +
            "&& execution(* delete(..))")
    public void triggerOnItemDelete(final JoinPoint joinPoint, ItemI item, UserI user) throws Throwable{
        try {


            String userLogin = user != null ? user.getLogin() : null;

            log.debug("triggerOnItemDelete AfterReturning aspect called after " + joinPoint.getSignature().getName() + "." +
                    "  ItemI type = " + (item != null ? item.getClass().getSimpleName() : "null") +
                    "  ItemI xsiType = " + (item != null ? item.getXSIType() : "null") +
                    "  UserI = " + userLogin);

            if(StringUtils.equals(item.getXSIType(),"xnat:projectData")){
                XnatProjectdataI project = new XnatProjectdata(item);
                eventService.triggerEvent(new ProjectDeletedEvent(project, userLogin), project.getId());

            }else if(StringUtils.containsIgnoreCase(item.getXSIType(),"xnat:") &&
                    StringUtils.containsIgnoreCase(item.getXSIType(),"SessionData")){
                XnatImagesessiondataI session = new XnatImagesessiondata(item);
                eventService.triggerEvent(new SessionDeletedEvent(session, userLogin), session.getProject());

            }else if(StringUtils.equals(item.getXSIType(), "xnat:subjectData")){
                XnatSubjectdataI subject = new XnatSubjectdata(item);
                eventService.triggerEvent(new SubjectDeletedEvent(subject, userLogin), subject.getProject());

            }

        } catch (Throwable e){
            log.error("Exception processing triggerOnItemDelete" + e.getMessage());
            throw e;
        }
    }
}
