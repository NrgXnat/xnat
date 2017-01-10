/*
 * core: org.nrg.xft.event.persist.PersistentWorkflowBuilderAbst
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.event.persist;

import java.util.Collection;
import java.util.List;

import org.nrg.xft.security.UserI;

public abstract class PersistentWorkflowBuilderAbst {
	
	public abstract PersistentWorkflowI getPersistentWorkflowI(UserI user);

	public abstract PersistentWorkflowI getWorkflowByEventId(final UserI user,final Integer id);
	
	public abstract Collection<? extends PersistentWorkflowI> getOpenWorkflows(final UserI user,final String ID);
	
	public abstract Collection<? extends PersistentWorkflowI> getWorkflows(final UserI user,final String ID);
	
	public abstract Collection<? extends PersistentWorkflowI> getWorkflows(final UserI user,final List<String> IDs);
	
	public abstract Collection<? extends PersistentWorkflowI> getWorkflowsByExternalId(final UserI user,final String ID);
}
