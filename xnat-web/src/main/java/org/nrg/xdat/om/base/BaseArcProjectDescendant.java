/*
 * org.nrg.xdat.om.base.BaseArcProjectDescendant
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.model.ArcProjectDescendantPipelineI;
import org.nrg.xdat.om.ArcProjectDescendantPipeline;
import org.nrg.xdat.om.base.auto.AutoArcProjectDescendant;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.PipelineNotFoundException;

import java.util.Hashtable;
import java.util.List;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseArcProjectDescendant extends AutoArcProjectDescendant {

	public BaseArcProjectDescendant(ItemI item)
	{
		super(item);
	}

	public BaseArcProjectDescendant(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseArcProjectDescendant(UserI user)
	 **/
	public BaseArcProjectDescendant()
	{}

	public BaseArcProjectDescendant(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

	
	public int getPipelineIndex(String pipelinePath) throws PipelineNotFoundException {
		int i = -1;
		List<ArcProjectDescendantPipelineI> pipelines = getPipeline();
		for (int j = 0; j <pipelines.size(); j++) {
			if (pipelines.get(j).getLocation().equals(pipelinePath)) {
				i = j;
				break;
			}
		}
		if (i == -1) throw new PipelineNotFoundException("Couldnt find pipelines located at " + pipelinePath + " for " + getXsitype());
		return i;
	}

	public ArcProjectDescendantPipeline getPipeline(String pipelinePath) throws PipelineNotFoundException {
		ArcProjectDescendantPipeline rtn = null;
		List<ArcProjectDescendantPipelineI> pipelines = getPipeline();
		for (int j = 0; j <pipelines.size(); j++) {
			if (pipelines.get(j).getLocation().equals(pipelinePath)) {
				rtn = (ArcProjectDescendantPipeline)pipelines.get(j);
				break;
			}
		}
		if (rtn == null) throw new PipelineNotFoundException("Couldnt find pipelines located at " + pipelinePath + " for " + getXsitype());
		return rtn;
	}

}
