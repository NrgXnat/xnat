package org.nrg.pipeline;

/**
 * @author Mohana Ramaratnam
 *
 */
public class PipelineLaunchStatus {


	private String status;
	private String project;
	private String exptId;
	private String exptLabel;
	private String workflowId;
	

	
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the project
	 */
	public String getProject() {
		return project;
	}
	/**
	 * @param project the project to set
	 */
	public void setProject(String project) {
		this.project = project;
	}
	/**
	 * @return the exptId
	 */
	public String getExptId() {
		return exptId;
	}
	/**
	 * @param exptId the exptId to set
	 */
	public void setExptId(String exptId) {
		this.exptId = exptId;
	}
	/**
	 * @return the workflowId
	 */
	public String getWorkflowId() {
		return workflowId;
	}
	/**
	 * @param workflowId the workflowId to set
	 */
	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	/**
	 * @return the exptLabel
	 */
	public String getExptLabel() {
		return exptLabel;
	}

	/**
	 * @param exptLabel the exptLabel to set
	 */
	public void setExptLabel(String exptLabel) {
		this.exptLabel = exptLabel;
	}
    

}
