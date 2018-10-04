package org.nrg.xnat.services.velocity.context;

import java.util.List;

import org.nrg.xnat.velocity.context.PostAddProjectContextPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostAddProjectContextPopulatorService{
	
	@Autowired
	private List<PostAddProjectContextPopulator> contextPopulators;
	
	public List<PostAddProjectContextPopulator> getContextPopulators() { 
		return this.contextPopulators; 
	}
}