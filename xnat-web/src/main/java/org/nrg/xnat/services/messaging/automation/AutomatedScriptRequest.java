package org.nrg.xnat.services.messaging.automation;

import org.json.JSONObject;
import org.nrg.xft.security.UserI;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

public class AutomatedScriptRequest implements Serializable {
	
	private static final long serialVersionUID = -5425712284737600869L;
	
	private final String _srcEventId;
	private final String _srcEventClass;
	private final UserI  _user;
	private final String _scriptId;
	private final String _event;
	private final String _scriptWorkflowId;
	private final String _externalId;
	private final String _dataType;
	private final String _dataId;
	private final Map<String,Object> _argumentMap = Maps.newHashMap();
	
	public AutomatedScriptRequest(final String srcEventId, final String srcEventClass, final UserI user, final String scriptId, final String event, final String scriptWorkflow, final String dataType, final String dataId, final String externalId) {
		_srcEventId = srcEventId;
		_srcEventClass = srcEventClass;
		_user = user;
		_scriptId = scriptId;
		_event = event;
		_scriptWorkflowId = scriptWorkflow;
		_dataType = dataType;
		_dataId = dataId;
		_externalId = externalId;
	}
	
	public AutomatedScriptRequest(final String srcEventId, final String srcEventClass, final UserI user, final String scriptId, final String event, final String scriptWorkflow, final String dataType, final String dataId, final String externalId, Map<String,Object> argumentMap) {
		this(srcEventId, srcEventClass, user, scriptId, event, scriptWorkflow, dataType, dataId, externalId);
		_argumentMap.putAll(argumentMap);
	}	

	public String getSrcEventId() {
		return _srcEventId;
	}
	
	public String getSrcEventClass() {
		return _srcEventClass;
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

	public Map<String,Object> getArgumentMap() {
		return _argumentMap;
	}

	public String getArgumentJson() {
		return new JSONObject(_argumentMap).toString();
	}
}
