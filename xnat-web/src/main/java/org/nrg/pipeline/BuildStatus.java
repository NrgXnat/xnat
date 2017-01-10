/*
 * web: org.nrg.pipeline.BuildStatus
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.pipeline;

import org.nrg.xdat.om.base.BaseWrkWorkflowdata;

public class BuildStatus {
    
    public BuildStatus(String workFlowStatus, boolean disabled) {
        this.workFlowStatus = workFlowStatus;
        this.disabled = disabled;
    }
    
    String workFlowStatus;
    boolean disabled;
    
    public boolean canBuild() {
        boolean rtn =true;
        if (disabled)
            rtn = false;
        else if  ( workFlowStatus != null && (workFlowStatus.toUpperCase().equals(BaseWrkWorkflowdata.RUNNING) || workFlowStatus.toUpperCase().equals(BaseWrkWorkflowdata.QUEUED)))
            rtn = false;
        return rtn;
    }
    
    /**
     * @return Returns the disabled.
     */
    public boolean isDisabled() {
        return disabled;
    }
    /**
     * @param disabled The disabled to set.
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    /**
     * @return Returns the workFlowStatus.
     */
    public String getWorkFlowStatus() {
        return workFlowStatus;
    }
    /**
     * @param workFlowStatus The workFlowStatus to set.
     */
    public void setWorkFlowStatus(String workFlowStatus) {
        this.workFlowStatus = workFlowStatus;
    }
}
