/*
 * web: org.nrg.xnat.turbine.modules.actions.ModifySubjectAssessorDataAllowDataDeletion
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.log4j.Logger;

public class ModifySubjectAssessorDataAllowDataDeletion extends ModifySubjectAssessorData{
	static Logger logger = Logger.getLogger(ModifySubjectAssessorDataAllowDataDeletion.class);

    public boolean allowDataDeletion(){
        return true;
    }
}
