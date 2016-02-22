//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.event.persist;

import java.util.Date;

import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;

public interface PersistentWorkflowI {
	/**
	 * @return Returns the details.
	 */
	public abstract String getDetails();

	/**
	 * Sets the value for details.
	 * @param v Value to Set.
	 */
	public abstract void setDetails(String v);

	/**
	 * @return Returns the comments.
	 */
	public abstract String getComments();

	/**
	 * Sets the value for comments.
	 * @param v Value to Set.
	 */
	public abstract void setComments(String v);

	/**
	 * @return Returns the justification.
	 */
	public abstract String getJustification();

	/**
	 * Sets the value for justification.
	 * @param v Value to Set.
	 */
	public abstract void setJustification(String v);

	/**
	 * @return Returns the type.
	 */
	public abstract String getType();

	/**
	 * Sets the value for type.
	 * @param v Value to Set.
	 */
	public abstract void setType(EventUtils.TYPE v);

	/**
	 * @return Returns the type.
	 */
	public abstract String getCategory();

	/**
	 * Sets the value for type.
	 * @param v Value to Set.
	 */
	public abstract void setCategory(EventUtils.CATEGORY v);

	/**
	 * @return Returns the data_type.
	 */
	public abstract String getDataType();

	/**
	 * Sets the value for data_type.
	 * @param v Value to Set.
	 */
	public abstract void setDataType(String v);

	/**
	 * @return Returns the ID.
	 */
	public abstract String getId();

	/**
	 * Sets the value for ID.
	 * @param v Value to Set.
	 */
	public abstract void setId(String v);

	/**
	 * @return Returns the ExternalID.
	 */
	public abstract String getExternalid();

	/**
	 * Sets the value for ExternalID.
	 * @param v Value to Set.
	 */
	public abstract void setExternalid(String v);

	/**
	 * @return Returns the current_step_launch_time.
	 */
	public abstract Object getCurrentStepLaunchTime();

	/**
	 * Sets the value for current_step_launch_time.
	 * @param v Value to Set.
	 */
	public abstract void setCurrentStepLaunchTime(Object v);

	/**
	 * @return Returns the current_step_id.
	 */
	public abstract String getCurrentStepId();

	/**
	 * Sets the value for current_step_id.
	 * @param v Value to Set.
	 */
	public abstract void setCurrentStepId(String v);

	/**
	 * @return Returns the status.
	 */
	public abstract String getStatus();

	/**
	 * Sets the value for status.
	 * @param v Value to Set.
	 */
	public abstract void setStatus(String v);

	/**
	 * @return Returns the create_user.
	 */
	public abstract String getCreateUser();

	/**
	 * Sets the value for create_user.
	 * @param v Value to Set.
	 */
	public abstract void setCreateUser(String v);

	/**
	 * @return Returns the pipeline_name.
	 */
	public abstract String getPipelineName();

	/**
	 * Sets the value for pipeline_name.
	 * @param v Value to Set.
	 */
	public abstract void setPipelineName(String v);

	/**
	 * @return Returns the step_description.
	 */
	public abstract String getStepDescription();

	/**
	 * Sets the value for step_description.
	 * @param v Value to Set.
	 */
	public abstract void setStepDescription(String v);

	/**
	 * @return Returns the launch_time.
	 */
	public abstract Object getLaunchTime();
	/**
	 * @return Returns the launch_time.
	 */
	public abstract Date getLaunchTimeDate();
	/**
	 * @return Returns the launch_time.
	 */
	public abstract String getOnlyPipelineName();

	/**
	 * Sets the value for launch_time.
	 * @param v Value to Set.
	 */
	public abstract void setLaunchTime(Object v);

	/**
	 * @return Returns the percentageComplete.
	 */
	public abstract String getPercentagecomplete();

	/**
	 * Sets the value for percentageComplete.
	 * @param v Value to Set.
	 */
	public abstract void setPercentagecomplete(String v);

	public abstract String getUsername();
	
	public abstract Integer getWorkflowId();
	
	public abstract EventMetaI buildEvent();
	
	public abstract boolean save(UserI user, boolean overrideSecurity, boolean allowItemRemoval,EventMetaI c) throws Exception;

	public void postSave() throws Exception;
	
	public void postSave(boolean triggerEvent) throws Exception;
}
