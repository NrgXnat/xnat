package org.nrg.xnat.services.uri;

import java.util.List;

import org.nrg.xnat.helpers.uri.ManageableXnatURIContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManageableURIContainerService{
	
		@Autowired
		private List<ManageableXnatURIContainer> _manageableURIs;
		
		public List<ManageableXnatURIContainer> getManageableURIs() {
			return this._manageableURIs;
		}
}