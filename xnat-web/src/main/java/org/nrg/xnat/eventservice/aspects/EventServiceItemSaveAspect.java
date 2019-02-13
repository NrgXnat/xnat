package org.nrg.xnat.eventservice.aspects;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.model.XnatResourceI;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResource;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.ImageAssessorEvent;
import org.nrg.xnat.eventservice.events.ProjectEvent;
import org.nrg.xnat.eventservice.events.ResourceEvent;
import org.nrg.xnat.eventservice.events.ScanEvent;
import org.nrg.xnat.eventservice.events.SessionEvent;
import org.nrg.xnat.eventservice.events.SubjectEvent;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.eventservice.services.XnatObjectIntrospectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class EventServiceItemSaveAspect {

    private EventService eventService;
    private XnatObjectIntrospectionService xnatObjectIntrospectionService;

    @Autowired
    public EventServiceItemSaveAspect(EventService eventService, XnatObjectIntrospectionService xnatObjectIntrospectionService) {
        this.eventService = eventService;
        this.xnatObjectIntrospectionService = xnatObjectIntrospectionService;
    }

    @Around(value = "execution(* org.nrg.xft.utils.SaveItemHelper.save(..)) && @annotation(org.nrg.xft.utils.EventServiceTrigger) && args(item, user,..)")
    public Object processItemSaveTrigger(final ProceedingJoinPoint joinPoint, ItemI item, UserI user) throws Throwable {
        Object retVal = null;
        try {
            String userLogin = user == null ? null : user.getLogin();
            if (StringUtils.contains(item.getXSIType(), "xnat:user")) {
                retVal = joinPoint.proceed();

            } else if (StringUtils.equals(item.getXSIType(), "arc:project")) {
                log.debug("New Project Data Save" + " : xsiType:" + item.getXSIType());
                XnatProjectdataI project = item instanceof XnatProjectdataI ? (XnatProjectdataI) item : new XnatProjectdata(item);
                eventService.triggerEvent(new ProjectEvent(project, userLogin, ProjectEvent.Status.CREATED, project.getId()));

            } else if (StringUtils.equals(item.getXSIType(), "xnat:projectData")) {
                log.debug("Existing Project Data Save" + " : xsiType:" + item.getXSIType());
                XnatProjectdataI project = item instanceof XnatProjectdataI ? (XnatProjectdataI) item : new XnatProjectdata(item);

            } else if (StringUtils.equals(item.getXSIType(), "xnat:subjectData")) {
                XnatSubjectdataI subject = item instanceof XnatSubjectdataI ? (XnatSubjectdataI) item : new XnatSubjectdata(item);
                Boolean alreadyStored = xnatObjectIntrospectionService.storedInDatabase(subject);
                retVal = joinPoint.proceed();
                if (!alreadyStored && xnatObjectIntrospectionService.storedInDatabase(subject)) {
                    log.debug("New Subject Data Save" + " : xsiType:" + item.getXSIType());
                    eventService.triggerEvent(new SubjectEvent(subject, userLogin, SubjectEvent.Status.CREATED, subject.getProject()));
                } else {
                    log.debug("Existing Subject Data Save" + " : xsiType:" + item.getXSIType());
                    log.debug("SubjectEvent.Status.UPDATED detected - no-op");
                    //eventService.triggerEvent(new SubjectEvent(subject, userLogin, SubjectEvent.Status.UPDATED, subject.getProject()));

                }

            } else if (item instanceof XnatImagesessiondataI || StringUtils.containsIgnoreCase(item.getXSIType(), "SessionData")) {
                log.debug("Session Data Save" + " : xsiType:" + item.getXSIType());
                XnatImagesessiondataI session = item instanceof XnatImagesessiondataI ? (XnatImagesessiondataI) item : new XnatImagesessiondata(item);
                Boolean alreadyStored = xnatObjectIntrospectionService.storedInDatabase((XnatExperimentdata) session);

                if (!alreadyStored) {
                    log.debug("New Session Data Save" + " : xsiType:" + item.getXSIType());
                    retVal = joinPoint.proceed();
                    eventService.triggerEvent(new SessionEvent(session, userLogin, SessionEvent.Status.CREATED, session.getProject()));
                    List<XnatImagescandataI> scans = session.getScans_scan();
                    if (scans != null && !scans.isEmpty()) {
                        scans.forEach(sc -> eventService.triggerEvent(new ScanEvent(sc, userLogin, ScanEvent.Status.CREATED, session.getProject())));
                    }
                } else {
                    log.debug("Existing Session Data Save" + " : xsiType:" + item.getXSIType());
                    List<String> preScanIds = xnatObjectIntrospectionService.getScanIds((XnatExperimentdata) session);
                    retVal = joinPoint.proceed();
                    List<String> postScanIds = xnatObjectIntrospectionService.getScanIds((XnatExperimentdata) session);
                    postScanIds.removeAll(preScanIds);
                    if (!postScanIds.isEmpty()) {
                        List<XnatImagescandataI> newScans = session.getScans_scan().stream().filter(scn -> postScanIds.contains(scn.getId())).collect(Collectors.toList());
                        newScans.forEach(sc -> eventService.triggerEvent(new ScanEvent(sc, userLogin, ScanEvent.Status.CREATED, session.getProject())));
                    }
                    log.debug("SessionEvent.Status.UPDATED detected - no-op");
                    //eventService.triggerEvent(new SessionEvent(session, userLogin, SessionEvent.Status.UPDATED, session.getProject()));

                }

            } else if (item instanceof XnatResource || StringUtils.equals(item.getXSIType(), "xnat:resourceCatalog")) {
                log.debug("Resource Data Save" + " : xsiType:" + item.getXSIType());
                XnatResourceI resource = item instanceof XnatResourceI ? (XnatResourceI) item : new XnatResource(item);
                String project = (String) (item.getProperty("project"));
                if ((project == null || project.isEmpty()) && item.getParent() != null) {
                    project = (String) (item.getParent().getProperty("project"));
                    if (project == null && item.getParent() != null &&
                            !Strings.isNullOrEmpty(item.getParent().getXSIType()) && item.getParent().getXSIType().contentEquals("xnat:projectData") &&
                            item.getParent().getProperty("id") != null) {
                        project = (String) (item.getParent()).getProperty("id");
                    }
                }
                eventService.triggerEvent(new ResourceEvent((XnatResourcecatalogI) resource, userLogin, ResourceEvent.Status.CREATED, project));

            } else if (item instanceof XnatImagescandata) {
                log.debug("Image Scan Data Save : xsiType:" + item.getXSIType());
                XnatImagescandataI sc = (XnatImagescandata)item;
                String project = null;
                XnatImagesessiondata session = ((XnatImagescandata) sc).getImageSessionData();
                project = session == null ? null : session.getProject();
                List<String> scanIds = xnatObjectIntrospectionService.getScanIds(session);
                if(scanIds.contains(sc.getId())) {
                    log.debug("ScanEvent.Status.UPDATED - no-op");
                    //eventService.triggerEvent(new ScanEvent(sc, userLogin, ScanEvent.Status.UPDATED, project));
                } else {
                    eventService.triggerEvent(new ScanEvent(sc, userLogin, ScanEvent.Status.CREATED, project));
                }
            } else {
                retVal = null;
            }

        } catch (Throwable e) {
            log.error("Exception in EventServiceItemSaveAspect.processItemSaveTrigger() joinpoint.proceed(): " + joinPoint.toString()
                    + "item: " + item.toString()
                    + "user: " + user.toString()
                    + "\n" + e.getMessage());
        } finally {
            return retVal == null ? joinPoint.proceed() : retVal;
        }
    }

    @AfterReturning(pointcut = "@annotation(org.nrg.xft.utils.EventServiceTrigger) " +
            "&& args(item, user, ..)" +
            "&& execution(* org.nrg.xft.utils.SaveItemHelper.save(..))")
    public void triggerOnImageAssessorSave(final JoinPoint joinPoint, XnatImageassessordata item, UserI user) throws Throwable{
        try {
            String userLogin = user != null ? user.getLogin() : null;
            log.debug("triggerOnImageAssessorSave AfterReturning aspect called after " + joinPoint.getSignature().getName() + "." +
                    "  ItemI type = " + (item != null ? item.getClass().getSimpleName() : "null") +
                    "  ItemI xsiType = " + (item != null ? item.getXSIType() : "null") +
                    "  UserI = " + userLogin);
            eventService.triggerEvent(new ImageAssessorEvent(item, userLogin, ImageAssessorEvent.Status.CREATED, item.getProject()));

        } catch (Throwable e){
            log.error("Exception processing triggerOnImageAssessorSave" + e.getMessage());
            throw e;
        }
    }

    @AfterReturning(pointcut = "@annotation(org.nrg.xft.utils.EventServiceTrigger) " +
            "&& args(item, user, ..)" +
            "&& execution(* org.nrg.xft.utils.SaveItemHelper.delete(..))")
    public void triggerOnItemDelete(final JoinPoint joinPoint, ItemI item, UserI user) throws Throwable{
        try {

            String userLogin = user != null ? user.getLogin() : null;

            log.debug("triggerOnItemDelete AfterReturning aspect called after " + joinPoint.getSignature().getName() + "." +
                    "  ItemI type = " + (item != null ? item.getClass().getSimpleName() : "null") +
                    "  ItemI xsiType = " + (item != null ? item.getXSIType() : "null") +
                    "  UserI = " + userLogin);

            if(StringUtils.equals(item.getXSIType(),"xnat:projectData")){
                XnatProjectdataI project = new XnatProjectdata(item);
                eventService.triggerEvent(new ProjectEvent(project, userLogin, ProjectEvent.Status.DELETED, project.getId()));

            }else if(StringUtils.containsIgnoreCase(item.getXSIType(),"xnat:") &&
                    StringUtils.containsIgnoreCase(item.getXSIType(),"SessionData")){
                XnatImagesessiondataI session = new XnatImagesessiondata(item);
                eventService.triggerEvent(new SessionEvent(session, userLogin, SessionEvent.Status.DELETED, session.getProject()));

            }else if(StringUtils.equals(item.getXSIType(), "xnat:subjectData")){
                XnatSubjectdataI subject = new XnatSubjectdata(item);
                eventService.triggerEvent(new SubjectEvent(subject, userLogin, SubjectEvent.Status.DELETED, subject.getProject()));

            }

        } catch (Throwable e){
            log.error("Exception processing triggerOnItemDelete" + e.getMessage());
            throw e;
        }
    }


}
