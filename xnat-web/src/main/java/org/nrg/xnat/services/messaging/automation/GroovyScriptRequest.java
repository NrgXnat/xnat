package org.nrg.xnat.services.messaging.automation;

import java.io.Serializable;

import org.nrg.xft.security.UserI;

public class GroovyScriptRequest implements Serializable {
	private static final long serialVersionUID = 6828367474920729056L;
	
	private final String srcWorkflowId;
	private final UserI user;
	private final String scriptId;
	private final String scriptWorkflowId;
	private final String datatype;
	private final String externalid;
	private final String dataid;
	
	public GroovyScriptRequest(String workflow, UserI user, String scriptId, String scriptWorkflow, String datatype, String dataid, String externalid){
		this.srcWorkflowId=workflow;
		this.user=user;
		this.scriptId= scriptId;
		this.scriptWorkflowId=scriptWorkflow;
		this.datatype=datatype;
		this.dataid=dataid;
		this.externalid=externalid;
	}

	public String getSrcWorkflowId() {
		return srcWorkflowId;
	}

	public UserI getUser() {
		return user;
	}

	public String getScriptId() {
		return scriptId;
	}

	public String getScriptWorkflowId() {
		return scriptWorkflowId;
	}

	public String getExternalId() {
		return externalid;
	}

	public String getDataType() {
		return datatype;
	}

	public String getDataId() {
		return dataid;
	}
}
