package org.nrg.pipeline;

import java.util.List;
import java.util.Map;

/**
 * @author Mohana Ramaratnam
 *
 */
public class PipelineLaunchReport {
	String pipelineName;
	String project;
	int successes, failures;
	
	List<PipelineLaunchStatus> experimentLaunchStatus;
	Map<String,String> params;
	/**
	 * @return the pipelineName
	 */
	public String getPipelineName() {
		return pipelineName;
	}
	/**
	 * @param pipelineName the pipelineName to set
	 */
	public void setPipelineName(String pipelineName) {
		this.pipelineName = pipelineName;
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
	 * @return the successCount
	 */
	public int getSuccesses() {
		return successes;
	}
	/**
	 * @param successCount the successCount to set
	 */
	public void setSuccesses(int successCount) {
		this.successes = successCount;
	}
	/**
	 * @return the failureCount
	 */
	public int getFailures() {
		return failures;
	}
	/**
	 * @param failureCount the failureCount to set
	 */
	public void setFailures(int failureCount) {
		this.failures = failureCount;
	}
	/**
	 * @return the experiments
	 */
	public List<PipelineLaunchStatus> getExperimentLaunchStatuses() {
		return experimentLaunchStatus;
	}
	/**
	 * @param experiments the experiments to set
	 */
	public void setExperimentLaunchStatuses(List<PipelineLaunchStatus> experiments) {
		experimentLaunchStatus = experiments;
	}
	/**
	 * @return the launchParameters
	 */
	public Map<String, String> getParams() {
		return params;
	}
	/**
	 * @param launchParameters the launchParameters to set
	 */
	public void setParams(Map<String, String> launchParameters) {
		this.params = launchParameters;
	}

	
}
