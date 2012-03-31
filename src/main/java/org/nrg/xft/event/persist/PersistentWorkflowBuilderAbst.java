package org.nrg.xft.event.persist;

import java.util.Collection;
import java.util.List;

import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.security.UserI;

public abstract class PersistentWorkflowBuilderAbst {
	
	public abstract PersistentWorkflowI getPersistentWorkflowI(UserI user);

	public abstract PersistentWorkflowI getWorkflowByEventId(final XDATUser user,final Integer id);
	
	public abstract Collection<? extends PersistentWorkflowI> getOpenWorkflows(final XDATUser user,final String ID);
	
	public abstract Collection<? extends PersistentWorkflowI> getWorkflows(final XDATUser user,final String ID);
	
	public abstract Collection<? extends PersistentWorkflowI> getWorkflows(final XDATUser user,final List<String> IDs);
	
	public abstract Collection<? extends PersistentWorkflowI> getWorkflowsByExternalId(final XDATUser user,final String ID);
}
