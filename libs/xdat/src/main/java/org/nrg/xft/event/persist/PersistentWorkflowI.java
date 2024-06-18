/*
 * core: org.nrg.xft.event.persist.PersistentWorkflowI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.event.persist;

import java.util.Date;

import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;

public interface PersistentWorkflowI {
	/**
	 * @return Returns the details.
	 */
	String getDetails();

	/**
	 * Sets the value for details.
	 * @param v Value to Set.
	 */
	void setDetails(String v);

	/**
	 * @return Returns the comments.
	 */
	String getComments();

	/**
	 * Sets the value for comments.
	 * @param v Value to Set.
	 */
	void setComments(String v);

	/**
	 * @return Returns the justification.
	 */
	String getJustification();

	/**
	 * Sets the value for justification.
	 * @param v Value to Set.
	 */
	void setJustification(String v);

	/**
	 * @return Returns the type.
	 */
	String getType();

	/**
	 * Sets the value for type.
	 * @param v Value to Set.
	 */
	void setType(EventUtils.TYPE v);

	/**
	 * @return Returns the type.
	 */
	String getCategory();

	/**
	 * Sets the value for type.
	 * @param v Value to Set.
	 */
	void setCategory(EventUtils.CATEGORY v);

	/**
	 * @return Returns the data_type.
	 */
	String getDataType();

	/**
	 * Sets the value for data_type.
	 * @param v Value to Set.
	 */
	void setDataType(String v);

	/**
	 * @return Returns the ID.
	 */
	String getId();

	/**
	 * Sets the value for ID.
	 * @param v Value to Set.
	 */
	void setId(String v);

	/**
	 * @return Returns the scan ID.
	 */
	String getScanId();

	/**
	 * Sets the value for scan ID.
	 * @param v Value to Set.
	 */
	void setScanId(String v);

	/**
	 * @return Returns the ExternalID.
	 */
	String getExternalid();

	/**
	 * Sets the value for ExternalID.
	 * @param v Value to Set.
	 */
	void setExternalid(String v);

	/**
	 * @return Returns the current_step_launch_time.
	 */
	Object getCurrentStepLaunchTime();

	/**
	 * @return Returns the current_step_launch_time.
	 */
	Date getCurrentStepLaunchTimeDate();

	/**
	 * Sets the value for current_step_launch_time.
	 * @param v Value to Set.
	 */
	void setCurrentStepLaunchTime(Object v);

	/**
	 * @return Returns the current_step_id.
	 */
	String getCurrentStepId();

	/**
	 * Sets the value for current_step_id.
	 * @param v Value to Set.
	 */
	void setCurrentStepId(String v);

	/**
	 * @return Returns the next_step_id.
	 */
	String getNextStepId();

	/**
	 * Sets the value for next_step_id.
	 * @param v Value to Set.
	 */
	void setNextStepId(String v);

	/**
	 * @return Returns the status.
	 */
	String getStatus();

	/**
	 * Sets the value for status.
	 * @param v Value to Set.
	 */
	void setStatus(String v);

	/**
	 * @return Returns the create_user.
	 */
	String getCreateUser();

	/**
	 * Sets the value for create_user.
	 * @param v Value to Set.
	 */
	void setCreateUser(String v);

	/**
	 * @return Returns the pipeline_name.
	 */
	String getPipelineName();

	/**
	 * Sets the value for pipeline_name.
	 * @param v Value to Set.
	 */
	void setPipelineName(String v);

	/**
	 * @return Returns the step_description.
	 */
	String getStepDescription();

	/**
	 * Sets the value for step_description.
	 * @param v Value to Set.
	 */
	void setStepDescription(String v);

	/**
	 * @return Returns the launch_time.
	 */
	Object getLaunchTime();
	/**
	 * @return Returns the launch_time.
	 */
	Date getLaunchTimeDate();
	/**
	 * @return Returns the launch_time.
	 */
	String getOnlyPipelineName();

	/**
	 * Sets the value for launch_time.
	 * @param v Value to Set.
	 */
	void setLaunchTime(Object v);

	/**
	 * @return Returns the percentageComplete.
	 */
	String getPercentagecomplete();

	/**
	 * set the src
	 * @param src the src string
	 */
	void setSrc(String src);

	/**
	 *
	 * @return the src
	 */
	String getSrc();

	/**
	 * set the jobId
	 * @param jobId the src string
	 */
	void setJobid(String jobId);

	/**
	 *
	 * @return the jobId
	 */
	String getJobid();

	/**
	 * Sets the value for percentageComplete.
	 * @param v Value to Set.
	 */
	void setPercentagecomplete(String v);

	String getUsername();

	Integer getUserId();
	
	Integer getWorkflowId();
	
	EventMetaI buildEvent();
	
	boolean save(UserI user, boolean overrideSecurity, boolean allowItemRemoval,EventMetaI c) throws Exception;

	void postSave() throws Exception;
	
	void postSave(boolean triggerEvent) throws Exception;
}
