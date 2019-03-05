package org.nrg.xnat.eventservice.aspects;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XdatUsergroupI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectassessordata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.ImageAssessorEvent;
import org.nrg.xnat.eventservice.events.ProjectEvent;
import org.nrg.xnat.eventservice.events.ScanEvent;
import org.nrg.xnat.eventservice.events.SessionEvent;
import org.nrg.xnat.eventservice.events.SubjectEvent;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.eventservice.services.EventServiceComponentManager;
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
    private EventServiceComponentManager componentManager;

    @Autowired
    public EventServiceItemSaveAspect(EventService eventService, XnatObjectIntrospectionService xnatObjectIntrospectionService,
                                      EventServiceComponentManager componentManager) {
        this.eventService = eventService;
        this.xnatObjectIntrospectionService = xnatObjectIntrospectionService;
        this.componentManager = componentManager;
    }

   @Around(value = "execution(* org.nrg.xft.utils.SaveItemHelper.save(..)) && @annotation(org.nrg.xft.utils.EventServiceTrigger) && args(item, user,..)")
    public Object processItemSaveTrigger(final ProceedingJoinPoint joinPoint, ItemI item, UserI user) throws Throwable {
        Object retVal = null;
        try {
            if (isItemA(item, XnatType.USER)) {
                retVal = joinPoint.proceed();

            } else if (isItemA(item, XnatType.USER_GROUP)){
                retVal = joinPoint.proceed();

            } else if (isItemA(item, XnatType.WORKFLOW)){
                retVal = joinPoint.proceed();

            }else if (isItemA(item, XnatType.NEW_PROJECT)) {
                log.debug("New Project Data Save" + " : xsiType:" + item.getXSIType());
                XnatProjectdataI project = item instanceof XnatProjectdataI ? (XnatProjectdataI) item : new XnatProjectdata(item);
                triggerProjectCreate(project, user);
            } else if (isItemA(item, XnatType.PROJECT)) {
                log.debug("Existing Project Data Save" + " : xsiType:" + item.getXSIType());
                log.debug("ProjectEvent.Status.UPDATED detected - no-op");
                //XnatProjectdataI project = item instanceof XnatProjectdataI ? (XnatProjectdataI) item : new XnatProjectdata(item);

            } else if (isItemA(item, XnatType.SUBJECT)) {
                XnatSubjectdataI subject = item instanceof XnatSubjectdataI ? (XnatSubjectdataI) item : new XnatSubjectdata(item);
                Boolean alreadyStored = xnatObjectIntrospectionService.storedInDatabase(subject);
                if (!alreadyStored) {
                    // New subject save
                    log.debug("New Subject Data Save" + " : xsiType:" + item.getXSIType());
                    retVal = joinPoint.proceed();
                    triggerSubjectCreate(subject, user);
                } else if (alreadyStored && (item instanceof XnatSubjectdataI) && (subject.getExperiments_experiment() == null || subject.getExperiments_experiment().isEmpty())){
                    // This is an existing subject being edited
                    log.debug("SubjectEvent.Status.UPDATED detected - no-op");
                    retVal = joinPoint.proceed();
                } else {
                    List<String> preImageSessionIds = xnatObjectIntrospectionService.getStoredImageSessionIds(subject);
                    List<XnatSubjectassessordataI> currentSessions =
                            subject.getExperiments_experiment()
                                    .stream().filter(experiment -> experiment instanceof XnatImagesessiondataI).collect(Collectors.toList());
                    List<String> currentSessionIds = currentSessions.stream().map(XnatSubjectassessordataI::getId).collect(Collectors.toList());
                    List<String> removedSessionIds = preImageSessionIds.stream().filter(id -> !currentSessionIds.contains(id)).collect(Collectors.toList());
                    List<String> addedSessionIds = currentSessionIds.stream().filter(id -> !preImageSessionIds.contains(id)).collect(Collectors.toList());
                    log.debug("Existing Subject Data Save" + " : xsiType:" + item.getXSIType());
                    triggerSessionDelete(removedSessionIds, user);
                    triggerSessionsCreate(currentSessions.stream()
                                                         .filter(s -> addedSessionIds.contains(s.getId()))
                                                         .filter(s -> s instanceof XnatImagesessiondataI)
                                                         .map(s -> (XnatImagesessiondataI)s)
                                                         .collect(Collectors.toList()), user);
                    
                    log.debug("SubjectEvent.Status.UPDATED detected - no-op");
                    retVal = joinPoint.proceed();
                    //eventService.triggerEvent(new SubjectEvent(subject, userLogin, SubjectEvent.Status.UPDATED, subject.getProject()));
                }

            } else if (isItemA(item, XnatType.SESSION)) {
                log.debug("Session Data Save" + " : xsiType:" + item.getXSIType());
                XnatImagesessiondataI session = item instanceof XnatImagesessiondataI ? (XnatImagesessiondataI) item : new XnatImagesessiondata(item);
                Boolean alreadyStored = xnatObjectIntrospectionService.storedInDatabase((XnatExperimentdata) session);

                if (!alreadyStored) {
                    log.debug("New Session Data Save" + " : xsiType:" + item.getXSIType());
                    retVal = joinPoint.proceed();
                    triggerSessionCreate(session, user);
                } else {
                    log.debug("Existing Session Data Save" + " : xsiType:" + item.getXSIType());
                    List<String> preScanIds = xnatObjectIntrospectionService.getStoredScanIds((XnatExperimentdata) session);
                    retVal = joinPoint.proceed();
                    List<String> postScanIds = xnatObjectIntrospectionService.getStoredScanIds((XnatExperimentdata) session);
                    postScanIds.removeAll(preScanIds);
                    if (!postScanIds.isEmpty()) {
                        List<XnatImagescandataI> newScans = session.getScans_scan().stream().filter(scn -> postScanIds.contains(scn.getId())).collect(Collectors.toList());
                        triggerScansCreate(newScans, session.getProject(), user);
                    }
                    log.debug("SessionEvent.Status.UPDATED detected - no-op");
                    //eventService.triggerEvent(new SessionEvent(session, userLogin, SessionEvent.Status.UPDATED, session.getProject()));

                }

//            } else if (item instanceof XnatResource || StringUtils.equals(item.getXSIType(), "xnat:resourceCatalog")) {
//                log.debug("Resource Data Save" + " : xsiType:" + item.getXSIType());
//                XnatResourceI resource = item instanceof XnatResourceI ? (XnatResourceI) item : new XnatResource(item);
//                String project = (String) (item.getProperty("project"));
//                if ((project == null || project.isEmpty()) && item.getParent() != null) {
//                    project = (String) (item.getParent().getProperty("project"));
//                    if (project == null && item.getParent() != null &&
//                            !Strings.isNullOrEmpty(item.getParent().getXSIType()) && item.getParent().getXSIType().contentEquals("xnat:projectData") &&
//                            item.getParent().getProperty("id") != null) {
//                        project = (String) (item.getParent()).getProperty("id");
//                    }
//                }
//                eventService.triggerEvent(new ResourceEvent((XnatResourcecatalogI) resource, userLogin, ResourceEvent.Status.CREATED, project));
//
            } else if (isItemA(item, XnatType.SCAN)) {
                log.debug("Image Scan Data Save : xsiType:" + item.getXSIType());
                XnatImagescandataI sc = (XnatImagescandata)item;
                String project = null;
                XnatImagesessiondata session = ((XnatImagescandata) sc).getImageSessionData();
                project = session == null ? null : session.getProject();
                List<String> scanIds = xnatObjectIntrospectionService.getStoredScanIds(session);
                if(scanIds.contains(sc.getId())) {
                    log.debug("ScanEvent.Status.UPDATED - no-op");
                    //eventService.triggerEvent(new ScanEvent(sc, userLogin, ScanEvent.Status.UPDATED, project));
                } else {
                    triggerScanCreate(sc, project, user);
                }
            } else if (isItemA(item, XnatType.IMAGE_ASSESSOR)) {
                XnatImageassessordataI assessor = new XnatImageassessordata(item);
                if(!Strings.isNullOrEmpty(assessor.getImagesessionId())){
                    if(!xnatObjectIntrospectionService.storedInDatabase(assessor)) {
                        log.debug("Image Assessor Data Save" + " : xsiType:" + item.getXSIType());
                        triggerImageAssessorCreate(assessor, user);
                    } else {
                        log.debug("Image Assessor Data Update - no-op");
                    }
                }
            } else if (isItemA(item, XnatType.NON_IMAGE__SUBJECT_ASSESSOR)) {
                log.debug("Subject Assessor Data Save : xsiType: "  + item.getXSIType());
                triggerSubjectAssessorCreate(new XnatSubjectassessordata(item), user);
            }else {
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
            "&& execution(* org.nrg.xft.utils.SaveItemHelper.delete(..))")
    public void triggerOnItemDelete(final JoinPoint joinPoint, ItemI item, UserI user) throws Throwable{
        try {

            String userLogin = user != null ? user.getLogin() : null;

            log.debug("triggerOnItemDelete AfterReturning aspect called after " + joinPoint.getSignature().getName() + "." +
                    "  ItemI type = " + (item != null ? item.getClass().getSimpleName() : "null") +
                    "  ItemI xsiType = " + (item != null ? item.getXSIType() : "null") +
                    "  UserI = " + userLogin);

            if(StringUtils.equals(item.getXSIType(),"xnat:projectData")){
                triggerProjectDelete(new XnatProjectdata(item), user);

            }else if(StringUtils.containsIgnoreCase(item.getXSIType(),"xnat:") &&
                    StringUtils.containsIgnoreCase(item.getXSIType(),"SessionData")){
                XnatImagesessiondataI session = new XnatImagesessiondata(item);
                triggerSessionDelete(session, user);

            }else if(StringUtils.equals(item.getXSIType(), "xnat:subjectData")){
                XnatSubjectdataI subject = new XnatSubjectdata(item);
                triggerSubjectDelete(subject, user);

            }

        } catch (Throwable e){
            log.error("Exception processing triggerOnItemDelete" + e.getMessage());
            throw e;
        }
    }

    //** Project Triggers **//
    private void triggerProjectCreate(XnatProjectdataI project, UserI user){
        eventService.triggerEvent(new ProjectEvent(project, user.getLogin(), ProjectEvent.Status.CREATED, project.getId()));
    }
    private void triggerProjectDelete(XnatProjectdataI project, UserI user){
        eventService.triggerEvent(new ProjectEvent(project, user.getLogin(), ProjectEvent.Status.DELETED, project.getId()));
    }

    //** Subject Triggers **//
    private void triggerSubjectCreate(XnatSubjectdataI subject, UserI user){
        eventService.triggerEvent(new SubjectEvent(subject, user.getLogin(), SubjectEvent.Status.CREATED, subject.getProject()));
        if (subject.getExperiments_experiment() != null) {
            subject.getExperiments_experiment().stream()
                   .filter(s -> s instanceof XnatImagesessiondataI)
                   .map(s -> (XnatImagesessiondataI) s)
                   .forEach(s -> triggerSessionCreate(s, user));
        }
        if (subject.getResources_resource() != null) {
            subject.getResources_resource().stream()
                   .forEach(r ->triggerResourceCreate(r, user));
        }

    }
    private void triggerSubjectDelete(XnatSubjectdataI subject, UserI user){
        eventService.triggerEvent(new SubjectEvent(subject, user.getLogin(), SubjectEvent.Status.DELETED, subject.getProject()));
    }

    //** Session Triggers **//
    private void triggerSessionsCreate(List<XnatImagesessiondataI> sessions, UserI user){
        if (sessions != null && !sessions.isEmpty()){
            sessions.forEach(session -> triggerSessionCreate(session, user));
        }
    }
    private void triggerSessionCreate(XnatImagesessiondataI session, UserI user){
        eventService.triggerEvent(new SessionEvent(session, user.getLogin(), SessionEvent.Status.CREATED, session.getProject()));
        List<XnatImagescandataI> scans = session.getScans_scan();
        if (scans != null && !scans.isEmpty()) {
            scans.forEach(sc -> triggerScanCreate(sc, session.getProject(), user));
        }
        List<XnatImageassessordataI> assessors = session.getAssessors_assessor();
        if (assessors != null && !assessors.isEmpty()){
            assessors.forEach(as -> triggerImageAssessorCreate(as, user));
        }
        List<XnatAbstractresourceI> resources = session.getResources_resource();
        if(resources != null && !resources.isEmpty()){
            resources.forEach(r -> triggerResourceCreate(r, user));
        }

    }
    private void triggerSessionDelete(List<String> sessionIds, UserI user){
        if(sessionIds != null && !sessionIds.isEmpty()) {
            sessionIds.forEach(sessionId -> triggerSessionDelete(sessionId, user));
        }
    }
    private void triggerSessionDelete(String sessionId, UserI user){
        try {
            XnatImagesessiondataI session = XnatImagesessiondata.getXnatImagesessiondatasById(sessionId, user, false);
            triggerSessionDelete(session, user);
        } catch (Throwable e){
            log.error("Failed to load session for \"Session Delete\" event trigger");
        }
    }
    private void triggerSessionDelete(XnatImagesessiondataI session, UserI user){
        eventService.triggerEvent(new SessionEvent(session, user.getLogin(), SessionEvent.Status.DELETED, session.getProject()));
    }

    //** Scan Triggers **//
    private void triggerScansCreate(List<XnatImagescandataI> scans, String projectId, UserI user){
        if (scans != null && !scans.isEmpty()){
            scans.forEach(scan -> triggerScanCreate(scan, projectId, user));
        }
    }

    private void triggerScanCreate(XnatImagescandataI scan, String projectId, UserI user){
        eventService.triggerEvent(new ScanEvent(scan, user.getLogin(), ScanEvent.Status.CREATED, projectId));
    }

    //** Image Assessor Triggers **//
    private void triggerImageAssessorCreate(XnatImageassessordataI imageAssessor, UserI user){
        eventService.triggerEvent(new ImageAssessorEvent(imageAssessor, user.getLogin(), ImageAssessorEvent.Status.CREATED, imageAssessor.getProject()));

    }

    //** Non-Image Subject Assessor Triggers **//
    private void triggerSubjectAssessorCreate(XnatSubjectassessordataI subjectAssessor, UserI user){
        log.debug("NO-OP");
        //TODO
    }

    //** Resource Triggers **//
    private void triggerResourceCreate(XnatAbstractresourceI resource, UserI user){
        log.debug("NO-OP");
        //TODO
    }


    private enum XnatType {
        USER,
        USER_GROUP,
        WORKFLOW,
        NEW_PROJECT,
        PROJECT,
        SUBJECT,
        SESSION,
        SCAN,
        IMAGE_ASSESSOR,
        NON_IMAGE__SUBJECT_ASSESSOR
    }

    private Boolean isItemA(ItemI item, XnatType type){
        switch (type) {
            case USER:
                return StringUtils.contains(item.getXSIType(), "xnat:user") || StringUtils.contains(item.getXSIType(), "xdat:user_login");
            case USER_GROUP:
                return (item instanceof XdatUsergroupI || StringUtils.contains(item.getXSIType(), "xdat:userGroup"));
            case WORKFLOW:
                return StringUtils.contains(item.getXSIType(), "wrk:workflowData");
            case NEW_PROJECT:
                return StringUtils.equals(item.getXSIType(), "arc:project");
            case PROJECT:
                return StringUtils.equals(item.getXSIType(), "xnat:projectData");
            case SUBJECT:
                return (StringUtils.equals(item.getXSIType(), "xnat:subjectData") || item instanceof  XnatSubjectdataI);
            case SESSION:
                if (item instanceof XnatImagesessiondataI)
                    return true;
                // Attempt xsiType lookup
                List<String> sessionXsiTypes = componentManager.getXsiTypes(XnatImagesessiondataI.class);
                if (sessionXsiTypes != null && !sessionXsiTypes.isEmpty() && sessionXsiTypes.contains(item.getXSIType())){
                    return true;
                }
                // if that fails, compare to static string
                else if(StringUtils.containsIgnoreCase(item.getXSIType(), "SessionData")){
                    return true;
                }
                return false;
            case SCAN:
                return (item instanceof XnatImagescandata);
            case IMAGE_ASSESSOR:
                if (item instanceof XnatImageassessordataI)
                    return true;
                // Attempt xsiType lookup
                List<String> imageAssessorXsiTypes = componentManager.getXsiTypes(XnatImagesessiondataI.class);
                if (imageAssessorXsiTypes != null && !imageAssessorXsiTypes.isEmpty() && imageAssessorXsiTypes.contains(item.getXSIType())) {
                    return true;
                }
                // if that fails, compare to static string
                else if(StringUtils.containsIgnoreCase(item.getXSIType(), "ImageAssessorData")) {
                    return true;
                }
                return false;
            case NON_IMAGE__SUBJECT_ASSESSOR:
                return false;
            default:
                log.error("No detection implementation for type: " + type.name());
                return false;
        }
    }
}
