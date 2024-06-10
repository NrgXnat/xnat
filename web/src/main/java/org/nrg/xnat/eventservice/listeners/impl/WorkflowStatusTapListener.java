package org.nrg.xnat.eventservice.listeners.impl;

//@Slf4j
//@Service
//@SuppressWarnings("unused")
//public class WorkflowStatusTapListener implements Consumer<Event<WorkflowStatusEvent>> {
//
//    private final EventService eventService;
//    private final ObjectMapper mapper;
//
//    @Autowired
//    public WorkflowStatusTapListener(final EventBus eventBus, final EventService eventService, final ObjectMapper mapper) {
//        eventBus.on(type(WorkflowStatusEvent.class), this);
//        this.eventService = eventService;
//        this.mapper = mapper;
//    }
//
//    //*
//    // Translate workflow status events into Event Service events for workflow events containing appropriate labels
//    //*
//    @Override
//    public void accept(Event<WorkflowStatusEvent> event) {
//
//        if (eventService != null && eventService.getPrefs() != null && !eventService.getPrefs().getEnabled()){
//            return;
//        }
//
//        WorkflowStatusEvent wfsEvent = event.getData();
//        if (wfsEvent.getWorkflow() instanceof WrkWorkflowdata) {
//            try {
//                final String project = wfsEvent.getExternalId();
//                if(!Strings.isNullOrEmpty(project) && project.contentEquals("ADMIN")){
//                    return;
//                }
//
//                eventService.triggerEvent(
//                        new WorkflowStatusChangeEvent(
//                                wfsEvent.getWorkflow(),
//                                Users.getUsername(wfsEvent.getUserId()),
//                                WorkflowStatusChangeEvent.Status.CHANGED,
//                                project,
//                                "wrk:workflowData"));
//            } catch (Throwable e) {
//                log.error("Exception thrown when trying to catch/trigger WorkFlowStatus event for Event Service.  {}", e.getMessage());
//            }
//        }
//    }
//}