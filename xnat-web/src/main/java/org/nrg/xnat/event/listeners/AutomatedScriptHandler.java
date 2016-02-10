package org.nrg.xnat.event.listeners;

import com.google.common.collect.Lists;

import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import org.apache.commons.lang.StringUtils;
import org.nrg.automation.entities.Script;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.WorkflowStatusEvent;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.WorkflowUtils;
import org.nrg.xnat.services.messaging.automation.AutomatedScriptRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static reactor.bus.selector.Selectors.$;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * The Class AutomatedScriptHandler.
 */
@Service
@SuppressWarnings("unused")
public class AutomatedScriptHandler extends WorkflowStatusEventHandlerAbst implements Consumer<Event<WorkflowStatusEvent>> {
    
    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(AutomatedScriptHandler.class);
	
	/**
	 * Instantiates a new automated script handler.
	 *
	 * @param eventBus the event bus
	 */
	@Inject public AutomatedScriptHandler( EventBus eventBus ){
	//public AutomatedScriptHandler( EventBus eventBus ){
		eventBus.on($(WorkflowStatusEvent.class.getName() + "." + PersistentWorkflowUtils.COMPLETE), this);
	}
	
	/* (non-Javadoc)
	 * @see reactor.fn.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(Event<WorkflowStatusEvent> event) {
		
		final WorkflowStatusEvent wfsEvent = event.getData();
		if (wfsEvent.getWorkflow() instanceof WrkWorkflowdata) {
			handleEvent(wfsEvent);
		}
		
	}

    /* (non-Javadoc)
     * @see org.nrg.xnat.event.listeners.WorkflowStatusEventHandlerAbst#handleEvent(org.nrg.xft.event.WorkflowStatusEvent)
     */
    @Override
    public void handleEvent(WorkflowStatusEvent wfsEvent) {
    	PersistentWorkflowI wrk = wfsEvent.getWorkflow();
    	if (!(wrk instanceof WrkWorkflowdata)) {
    		return;
    	}
        final WrkWorkflowdata wrkflow = (WrkWorkflowdata)wrk;
        final UserI user = (wrkflow.getUser()!=null) ? wrkflow.getUser() : wrkflow.getInsertUser();
        final String pipelineName = wrkflow.getPipelineName().replaceAll("\\*OPEN\\*", "(").replaceAll("\\*CLOSE\\*", ")");
				
        if (StringUtils.equals(PersistentWorkflowUtils.COMPLETE, wrkflow.getStatus()) && !StringUtils.equals(wrkflow.getExternalid(), PersistentWorkflowUtils.ADMIN_EXTERNAL_ID)) {
            //check to see if this has been handled before
            for (Script script : getScripts(wrkflow.getExternalid(), pipelineName)) {
                try {
                    final String action = "Executed script " + script.getScriptId();
                    final String justification = wrkflow.getJustification();
                    final String comment = "Executed script " + script.getScriptId() + " triggered by event " + pipelineName;
                    PersistentWorkflowI scriptWrk = PersistentWorkflowUtils.buildOpenWorkflow(user, wrkflow.getDataType(), wrkflow.getId(), wrkflow.getExternalid(), EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.PROCESS, action, StringUtils.isNotBlank(justification) ? justification : "Automated execution: " + comment, comment));
                    assert scriptWrk != null;
                    scriptWrk.setStatus(PersistentWorkflowUtils.QUEUED);
                    WorkflowUtils.save(scriptWrk, scriptWrk.buildEvent());
                    AutomatedScriptRequest request = new AutomatedScriptRequest(wrkflow.getWrkWorkflowdataId().toString(), user, script.getScriptId(), pipelineName, scriptWrk.getWorkflowId().toString(), wrk.getDataType(), wrk.getId(), wrk.getExternalid());
                    XDAT.sendJmsRequest(request);
                } catch (Exception e1) {
                    logger.error("", e1);
                }
            }
        }
    }

    /**
     * Gets the scripts.
     *
     * @param projectId the project id
     * @param event the event
     * @return the scripts
     */
    private List<Script> getScripts(final String projectId, String event) {
    	final ScriptRunnerService _service = XDAT.getContextService().getBean(ScriptRunnerService.class);
    	
        final List<Script> scripts = Lists.newArrayList();

        //project level scripts
        if (StringUtils.isNotBlank(projectId)) {
            Script script = _service.getScript(Scope.Project, projectId, event);
            if (script != null) {
                scripts.add(script);
            }
        }

        //site level scripts
        Script script = _service.getScript(Scope.Site, null, event);
        if (script != null) {
            scripts.add(script);
        }

        return scripts;
    }

}
