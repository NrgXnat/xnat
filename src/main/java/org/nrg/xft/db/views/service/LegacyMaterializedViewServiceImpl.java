package org.nrg.xft.db.views.service;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.nrg.xft.db.MaterializedViewI;
import org.nrg.xft.db.views.LegacyMaterializedViewImpl;
import org.nrg.xft.security.UserI;

public class LegacyMaterializedViewServiceImpl implements MaterializedViewServiceI {
	static org.apache.log4j.Logger logger = Logger.getLogger(LegacyMaterializedViewServiceImpl.class);

	@Override
	public void deleteViewsByUser(UserI user) throws Exception {
		MaterializedViewManager manager=MaterializedViewManager.getMaterializedViewManager();
		for(MaterializedViewI view: manager.getViewsByUser(user,this)){
			view.delete();
		}
		
	}

	@Override
	public MaterializedViewI getViewByTablename(String tablename, UserI user) throws Exception{
		MaterializedViewManager manager=MaterializedViewManager.getMaterializedViewManager();
		return manager.getViewByTablename(tablename,user,this);
	}

	@Override
	public MaterializedViewI getViewBySearchID(String search_id, UserI user) throws Exception{
		MaterializedViewManager manager=MaterializedViewManager.getMaterializedViewManager();
		return manager.getViewBySearchID(search_id,user,this);
	}

	@Override
	public MaterializedViewI createView(UserI user) {
		return new LegacyMaterializedViewImpl(user);
	}

	@Override
	public MaterializedViewI populateView(Hashtable t, UserI u) {
		return new LegacyMaterializedViewImpl(t, u);
	}
	
	
}
