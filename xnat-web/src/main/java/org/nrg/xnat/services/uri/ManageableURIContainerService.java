package org.nrg.xnat.services.uri;

import java.util.ArrayList;
import java.util.List;

import org.nrg.xnat.helpers.uri.ManageableXnatURIContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManageableURIContainerService{
	
		@Autowired(required=false)
		private List<ManageableXnatURIContainer> _manageableURIs = new ArrayList<>();
		
		public List<ManageableXnatURIContainer> getManageableURIs() {
			return this._manageableURIs;
		}
}