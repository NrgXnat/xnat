/*
 * org.nrg.xnat.turbine.modules.actions.ModifySubjectAssessorDataAllowDataDeletion
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.actions;

import org.apache.log4j.Logger;

public class ModifySubjectAssessorDataAllowDataDeletion extends ModifySubjectAssessorData{
	static Logger logger = Logger.getLogger(ModifySubjectAssessorDataAllowDataDeletion.class);

    public boolean allowDataDeletion(){
        return true;
    }
}
