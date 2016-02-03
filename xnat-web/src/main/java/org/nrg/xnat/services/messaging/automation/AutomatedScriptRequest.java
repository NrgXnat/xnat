package org.nrg.xnat.services.messaging.automation;

import org.nrg.xft.security.UserI;

import java.io.Serializable;

public class AutomatedScriptRequest implements Serializable {
	private static final long serialVersionUID = 6828367474920729056L;
	
	private final String _srcWorkflowId;
	private final UserI  _user;
	private final String _scriptId;
	private final String _event;
	private final String _scriptWorkflowId;
	private final String _externalId;
	private final String _dataType;
	private final String _dataId;
	
	public AutomatedScriptRequest(final String srcWorkflowId, final UserI user, final String scriptId, final String event, final String scriptWorkflow, final String dataType, final String dataId, final String externalId) {
		_srcWorkflowId = srcWorkflowId;
		_user = user;
		_scriptId = scriptId;
		_event = event;
		_scriptWorkflowId = scriptWorkflow;
		_dataType = dataType;
		_dataId = dataId;
		_externalId = externalId;
	}

	public String getSrcWorkflowId() {
		return _srcWorkflowId;
	}

	public UserI getUser() {
		return _user;
	}

	public String getScriptId() {
		return _scriptId;
	}

	public String getEvent() {
		return _event;
	}

	public String getScriptWorkflowId() {
		return _scriptWorkflowId;
	}

	public String getExternalId() {
		return _externalId;
	}

	public String getDataType() {
		return _dataType;
	}

	public String getDataId() {
		return _dataId;
	}
}
