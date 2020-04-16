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
import org.nrg.xnat.eventservice.events.SubjectAssessorEvent;
import org.nrg.xnat.eventservice.events.SubjectEvent;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.eventservice.services.EventServiceComponentManager;
import org.nrg.xnat.eventservice.services.XnatObjectIntrospectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

        if (eventService != null && eventService.getPrefs() != null && !eventService.getPrefs().getTriggerCoreEvents()){
           return joinPoint.proceed();
       }


       StopWatch sw = new StopWatch();
        sw.start();
        ProceedingReturn proceedingReturn = null;
        try {
            if (isItemA(item, XnatType.USER)) {
                //retVal = joinPoint.proceed();

            } else if (isItemA(item, XnatType.USER_GROUP)){
                //retVal = joinPoint.proceed();

            } else if (isItemA(item, XnatType.WORKFLOW)){
                //retVal = joinPoint.proceed();

            }else if (isItemA(item, XnatType.NEW_PROJECT)) {
                log.debug("New Project Data Save" + " : xsiType:" + item.getXSIType());
                XnatProjectdataI project = item instanceof XnatProjectdataI ? (XnatProjectdataI) item : new XnatProjectdata(item);
                triggerProjectCreate(project, user);
                if(log.isDebugEnabled() && sw.isRunning()) {
                    sw.stop();
                    log.debug("Event detection took " + sw.getTotalTimeMillis() + " milliseconds.");
                }
            } else if (isItemA(item, XnatType.PROJECT)) {
                log.debug("Existing Project Data Save" + " : xsiType:" + item.getXSIType());
                log.debug("ProjectEvent.Status.UPDATED detected - no-op");
                //XnatProjectdataI project = item instanceof XnatProjectdataI ? (XnatProjectdataI) item : new XnatProjectdata(item);
                if(log.isDebugEnabled() && sw.isRunning()) {
                    sw.stop();
                    log.debug("Event detection took " + sw.getTotalTimeMillis() + " milliseconds.");
                }
            } else if (isItemA(item, XnatType.SUBJECT)) {
                XnatSubjectdataI subject = item instanceof XnatSubjectdataI ? (XnatSubjectdataI) item : new XnatSubjectdata(item);
                Boolean alreadyStored = xnatObjectIntrospectionService.storedInDatabase(subject);
                if (!alreadyStored) {
                    // New subject save
                    log.debug("New Subject Data Save" + " : xsiType:" + item.getXSIType());
                    // ** Proceed with save operation ** //
                    proceedingReturn = proceedAndCaptureException(joinPoint);
                    if(proceedingReturn != null && proceedingReturn.throwable == null) {
                        triggerSubjectCreate(subject, user);
                    }
                } else if (alreadyStored && (item instanceof XnatSubjectdataI) && (subject.getExperiments_experiment() == null || subject.getExperiments_experiment().isEmpty())){
                    // This is an existing subject being edited
                    log.debug("SubjectEvent.Status.UPDATED detected - no-op");
                } else {
                    log.debug("Existing Subject Data Save" + " : xsiType:" + item.getXSIType());
                    final List<String> preSubjectAssessorIds = xnatObjectIntrospectionService.getStoredSubjectAssessorIds(subject);
                    final List<XnatSubjectassessordataI> currentSubjectAssessors = subject.getExperiments_experiment();
                    final List<String> currentSubjectAssessorIds = currentSubjectAssessors.stream()
                                                                        .map(XnatSubjectassessordataI::getId).collect(Collectors.toList());
                    final List<String> currentSessionIds = currentSubjectAssessors.stream()
                                                                        .filter(as -> as instanceof XnatImagesessiondataI)
                                                                        .map(XnatSubjectassessordataI::getId).collect(Collectors.toList());
                    final List<String> removedSubjectAssessorIds = preSubjectAssessorIds.stream()
                                                                        .filter(id -> !currentSubjectAssessorIds.contains(id)).collect(Collectors.toList());
                    final List<String> removedSessionIds = (removedSubjectAssessorIds == null || removedSubjectAssessorIds.isEmpty())
                                                        ? Arrays.asList()
                                                        : currentSessionIds.stream()
                                                                            .filter(sid -> removedSubjectAssessorIds.contains(sid)).collect(Collectors.toList());
                    final List<String> removeNonImageAssesorIds = (removedSubjectAssessorIds == null || removedSubjectAssessorIds.isEmpty())
                                                        ? Arrays.asList()
                                                        : removedSubjectAssessorIds.stream()
                                                                           .filter(asid -> !removedSessionIds.contains(asid)).collect(Collectors.toList());

                    final List<String> addedSubjectAssessorIds = currentSubjectAssessorIds.stream()
                                                                        .filter(id -> !preSubjectAssessorIds.contains(id)).collect(Collectors.toList());

                    // ** If there are potentially deleted assessors, we need to hold on to them so they can be used in the Delete event trigger ** //
                    List<XnatSubjectassessordataI> deletedAssessors = new ArrayList<>();
                    removedSubjectAssessorIds.forEach(assessorId -> deletedAssessors.add(XnatSubjectassessordata.getXnatSubjectassessordatasById(assessorId, user, false)));

                    // ** Proceed with save operation ** //
                    proceedingReturn = proceedAndCaptureException(joinPoint);
                    if(proceedingReturn != null && proceedingReturn.throwable == null) {

                        // All Subject Assessors
                        triggerSubjectAssessorCreate(currentSubjectAssessors.stream()
                                                                            .filter(as -> addedSubjectAssessorIds.contains(as.getId()))
                                                                            .collect(Collectors.toList()), user);
                        // Imaging Subject Assessors (Sessions)
                        triggerSessionsCreate(currentSubjectAssessors.stream()
                                                                     .filter(as -> addedSubjectAssessorIds.contains(as.getId()))
                                                                     .filter(as -> as instanceof XnatImagesessiondataI)
                                                                     .map(as -> (XnatImagesessiondataI) as)
                                                                     .collect(Collectors.toList()), user);



                        // ** If an XML was uploaded without delete permissions, the assessors that we detect as deleted may not have actually been deleted ** //
                        // ** Now that the save has been completed, check the db to see if those assessors are still there                                  ** //
                        final List<String> updatedSubjectAssessorIds = xnatObjectIntrospectionService.getStoredSubjectAssessorIds(subject);
                        final List<String> updatedRemovedSubjectAssessorIds = removedSubjectAssessorIds.stream().filter(id -> !updatedSubjectAssessorIds.contains(id)).collect(Collectors.toList());
                        final List<String> updatedRemovedSessionIds = removedSessionIds.stream().filter(id -> !updatedSubjectAssessorIds.contains(id)).collect(Collectors.toList());


                        // ** Now trigger events with the corrected added/removed ids ** //
                        triggerSubjectAssessorDelete(deletedAssessors.stream().filter(as -> updatedRemovedSubjectAssessorIds.contains(as.getId())).collect(Collectors.toList()), user);
                        triggerSessionDelete(deletedAssessors.stream().filter(as -> updatedRemovedSessionIds.contains(as.getId())).collect(Collectors.toList()), user);


                        log.debug("SubjectEvent.Status.UPDATED detected - no-op");
                        //eventService.triggerEvent(new SubjectEvent(subject, userLogin, SubjectEvent.Status.UPDATED, subject.getProject()));
                    }
                }
                if(log.isDebugEnabled() && sw.isRunning()) {
                    sw.stop();
                    log.debug("Event detection took " + sw.getTotalTimeMillis() + " milliseconds.");
                }
            } else if (isItemA(item, XnatType.SUBJECT_ASSESSOR)) {
                XnatSubjectassessordataI subjectAssessor = item instanceof XnatSubjectassessordataI ? (XnatSubjectassessordataI) item : new XnatSubjectassessordata(item);
                Boolean alreadyStored = xnatObjectIntrospectionService.storedInDatabase(subjectAssessor);
                if (!alreadyStored){
                    triggerSubjectAssessorCreate(subjectAssessor, user);
                }

                if (isItemA(item, XnatType.SESSION)) {
                    XnatImagesessiondataI session = item instanceof XnatImagesessiondataI ? (XnatImagesessiondataI) item : new XnatImagesessiondata(item);
                    if (!alreadyStored) {
                        log.debug("New Session Data Save" + " : xsiType:" + item.getXSIType());

                        // ** Proceed with save operation ** //
                        proceedingReturn = proceedAndCaptureException(joinPoint);
                        if(proceedingReturn != null && proceedingReturn.throwable == null) {
                            triggerSessionCreate(session, user);
                        }
                    } else {
                        log.debug("Existing Session Data Save" + " : xsiType:" + item.getXSIType());
                        List<String> preScanIds = xnatObjectIntrospectionService.getStoredScanIds((XnatExperimentdata) session);

                        // ** Proceed with save operation ** //
                        proceedingReturn = proceedAndCaptureException(joinPoint);
                        if(proceedingReturn != null && proceedingReturn.throwable == null) {
                            List<String> postScanIds = xnatObjectIntrospectionService.getStoredScanIds((XnatExperimentdata) session);
                            postScanIds.removeAll(preScanIds);
                            if (!postScanIds.isEmpty()) {
                                List<XnatImagescandataI> newScans = session.getScans_scan().stream().filter(scn -> postScanIds.contains(scn.getId())).collect(Collectors.toList());
                                triggerScansCreate(newScans, session.getProject(), user);
                            }
                            log.debug("SessionEvent.Status.UPDATED detected - no-op");
                            //eventService.triggerEvent(new SessionEvent(session, userLogin, SessionEvent.Status.UPDATED, session.getProject()));
                        }
                    }
                }
                if(log.isDebugEnabled() && sw.isRunning()) {
                    sw.stop();
                    log.debug("Event detection took " + sw.getTotalTimeMillis() + " milliseconds.");
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
                XnatImagescandataI sc = (XnatImagescandata)item;
                String project = null;
                XnatImagesessiondata session = ((XnatImagescandata) sc).getImageSessionData();
                project = session == null ? null : session.getProject();
                List<String> scanIds = xnatObjectIntrospectionService.getStoredScanIds(session);
                if(scanIds.contains(sc.getId())) {
                    log.debug("ScanEvent.Status.UPDATED - no-op");
                    //eventService.triggerEvent(new ScanEvent(sc, userLogin, ScanEvent.Status.UPDATED, project));
                } else {
                    log.debug("Image Scan Data Save : xsiType:" + item.getXSIType());
                    triggerScanCreate(sc, project, user);
                }
                if(log.isDebugEnabled() && sw.isRunning()) {
                    sw.stop();
                    log.debug("Event detection took " + sw.getTotalTimeMillis() + " milliseconds.");
                }
            } else if (isItemA(item, XnatType.IMAGE_ASSESSOR)) {
                XnatImageassessordataI assessor = new XnatImageassessordata(item);
                if(!Strings.isNullOrEmpty(assessor.getImagesessionId())){
                    if(!xnatObjectIntrospectionService.storedInDatabase(assessor)) {
                        log.debug("Image Assessor Data Save" + " : xsiType:" + item.getXSIType());
                        triggerImageAssessorCreate(assessor, user);
                    } else {
                        log.debug("Image Assessor Data Update" + " : xsiType:" + item.getXSIType());
                        triggerImageAssessorUpdate(assessor, user);
                    }
                }
                if(log.isDebugEnabled() && sw.isRunning()) {
                    sw.stop();
                    log.debug("Event detection took " + sw.getTotalTimeMillis() + " milliseconds.");
                }
            }

        } catch (Throwable e) {
            log.error("Exception in EventServiceItemSaveAspect.processItemSaveTrigger() joinpoint.proceed(): " + joinPoint.toString()
//                    + "item: " + item.toString()
//                    + "user: " + user.toString()
                    + "\n" + e.getStackTrace());
        } finally {
            if (proceedingReturn == null) {
                proceedingReturn = proceedAndCaptureException(joinPoint);
            }
            if (proceedingReturn.throwable == null){
                return proceedingReturn.retValue;
            }else {
                throw proceedingReturn.throwable;
            }
        }
    }

    @AfterReturning(pointcut = "@annotation(org.nrg.xft.utils.EventServiceTrigger) " +
            "&& args(item, user, ..)" +
            "&& execution(* org.nrg.xft.utils.SaveItemHelper.delete(..))")
    public void triggerOnItemDelete(final JoinPoint joinPoint, ItemI item, UserI user) throws Throwable{

        if (eventService != null && eventService.getPrefs() != null && !eventService.getPrefs().getTriggerCoreEvents()){
            return;
        }

        try {

            String userLogin = user != null ? user.getLogin() : null;

            if(isItemA(item, XnatType.PROJECT)){
                triggerProjectDelete(new XnatProjectdata(item), user);

            }else if(isItemA(item, XnatType.SUBJECT_ASSESSOR)){
                XnatSubjectassessordataI subjectAssessor = new XnatSubjectassessordata(item);
                triggerSubjectAssessorDelete(subjectAssessor, user);

                if (isItemA(item, XnatType.SESSION)) {
                    XnatImagesessiondataI session = new XnatImagesessiondata(item);
                    triggerSessionDelete(session, user);
                }

            }else if(isItemA(item, XnatType.SUBJECT)){
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
            for (XnatSubjectassessordataI sa : subject.getExperiments_experiment()) {
                triggerSubjectAssessorCreate(sa, user);
                if (sa instanceof XnatImageassessordataI) {
                    triggerSessionCreate((XnatImagesessiondataI) sa, user);
                }
            }
        }

    }
    private void triggerSubjectDelete(XnatSubjectdataI subject, UserI user){
        eventService.triggerEvent(new SubjectEvent(subject, user.getLogin(), SubjectEvent.Status.DELETED, subject.getProject()));
    }

    //** Subject Assessor Triggers **//
    private void triggerSubjectAssessorCreate(List<XnatSubjectassessordataI> subjectAssessors, UserI user) {
        if(subjectAssessors != null && !subjectAssessors.isEmpty()){
            subjectAssessors.forEach(as -> triggerSubjectAssessorCreate(as, user));
        }
    }
    private void triggerSubjectAssessorCreate(XnatSubjectassessordataI subjectAssessor, UserI user){
        eventService.triggerEvent(new SubjectAssessorEvent(subjectAssessor, user.getLogin(), SubjectAssessorEvent.Status.CREATED, subjectAssessor.getProject()));
    }
    private void triggerSubjectAssessorDelete(List<XnatSubjectassessordataI> subjectAssessors, UserI user){
        if(subjectAssessors != null) {
            subjectAssessors.forEach(subjectAssessor ->
                triggerSubjectAssessorDelete(subjectAssessor, user)
            );
        }
    }
    private void triggerSubjectAssessorDelete(XnatSubjectassessordataI subjectAssessor, UserI user){
        eventService.triggerEvent(new SubjectAssessorEvent(subjectAssessor, user.getLogin(), SubjectAssessorEvent.Status.DELETED, subjectAssessor.getProject()));
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
    private void triggerSessionDelete(List<XnatSubjectassessordataI> sessions, UserI user){
        if(sessions != null) {
            sessions.forEach(session ->
                    triggerSessionDelete((XnatImagesessiondataI) session, user)
            );
        }
    }    private void triggerSessionDelete(XnatImagesessiondataI session, UserI user){
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

    private void triggerImageAssessorUpdate(XnatImageassessordataI imageAssessor, UserI user){
        eventService.triggerEvent(new ImageAssessorEvent(imageAssessor, user.getLogin(), ImageAssessorEvent.Status.UPDATED, imageAssessor.getProject()));
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
        SUBJECT_ASSESSOR
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
                if (item instanceof XnatImagesessiondataI) {
                    return true;
                }
                // Attempt xsiType lookup
                List<String> sessionXsiTypes = componentManager.getXsiTypes(XnatImagesessiondataI.class);
                if (sessionXsiTypes != null && !sessionXsiTypes.isEmpty() &&  xsiTypeContainsSimilar(sessionXsiTypes, item.getXSIType())){
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
                List<String> imageAssessorXsiTypes = componentManager.getXsiTypes(XnatImageassessordataI.class);
                if (!Strings.isNullOrEmpty(item.getXSIType()) && imageAssessorXsiTypes != null
                        && !imageAssessorXsiTypes.isEmpty() && imageAssessorXsiTypes.contains(item.getXSIType())) {
                    return true;
                }
                // if that fails, compare to static string
                else if(StringUtils.containsIgnoreCase(item.getXSIType(), "ImageAssessorData")) {
                    return true;
                }
                return false;
            case SUBJECT_ASSESSOR:
                if (item instanceof XnatSubjectassessordataI){
                    return true;
                }
                List<String> subjectAssessorXsiTypes = componentManager.getXsiTypes(XnatSubjectassessordataI.class);
                if (!Strings.isNullOrEmpty(item.getXSIType()) && subjectAssessorXsiTypes != null
                        && !subjectAssessorXsiTypes.isEmpty() && xsiTypeContainsSimilar(subjectAssessorXsiTypes, item.getXSIType())){
                    return true;
                }
                return false;

            default:
                log.error("No detection implementation for type: " + type.name());
                return false;
        }
    }

    // ** Some xsiTypes are partially truncated when returned by item.getXsiType()
    // ** xnat_assessor:someotherstring might be reported as xnat_a:someotherstring
    // ** if there are no exact matches, we check for matches that are close
    private Boolean xsiTypeContainsSimilar(List<String> xsiTypes, String itemXsiType){
        if (xsiTypes.contains(xsiTypes)){
            return true;
        }
        if (itemXsiType.contains(":")){
            String[] parts = itemXsiType.split(":",2);
            if(parts.length == 2) {
                String ns = parts[0];
                String label = parts[1];
                if (ns.length() > 3 && label.length() > 0) {
                    Optional<String> first = xsiTypes.stream().filter(listEntry -> StringUtils.startsWith(listEntry, ns) && StringUtils.endsWith(listEntry, label)).findFirst();
                    return first.isPresent();
                }
            }
        }
        return false;
    }

    private ProceedingReturn proceedAndCaptureException(final ProceedingJoinPoint joinPoint){
        Throwable throwable = null;
        Object retValue = null;
        try{
            retValue = joinPoint.proceed();
        } catch (Throwable e){
            throwable = e;
        }
        return new ProceedingReturn(throwable, retValue);
    }

    public class ProceedingReturn{
        public ProceedingReturn(Throwable throwable, Object retValue) {
            this.throwable = throwable;
            this.retValue = retValue;
        }

        public Throwable throwable;
        public Object retValue;
    }

}
