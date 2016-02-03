/*
 * org.nrg.xdat.om.base.BaseArcProject
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.apache.commons.lang.StringUtils;
import org.nrg.pipeline.PipelineRepositoryManager;
import org.nrg.xdat.model.ArcPipelinedataI;
import org.nrg.xdat.model.ArcProjectDescendantI;
import org.nrg.xdat.model.ArcProjectDescendantPipelineI;
import org.nrg.xdat.model.ArcProjectPipelineI;
import org.nrg.xdat.om.ArcProjectDescendant;
import org.nrg.xdat.om.ArcProjectDescendantPipeline;
import org.nrg.xdat.om.ArcProjectPipeline;
import org.nrg.xdat.om.base.auto.AutoArcProject;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.PipelineNotFoundException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseArcProject extends AutoArcProject {

	public BaseArcProject(ItemI item)
	{
		super(item);
	}

	public BaseArcProject(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseArcProject(UserI user)
	 **/
	public BaseArcProject()
	{}

	public BaseArcProject(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

	@Override
	public Integer getPrearchiveCode() {
		Integer i= super.getPrearchiveCode();
		if(i==null){
			return new Integer(0);
		}else{
			return i;
		}
	}

	@Override
	public Integer getQuarantineCode() {
		Integer i= super.getQuarantineCode();
		if(i==null){
			return new Integer(0);
		}else{
			return i;
		}
	}

		public List<ArcProjectDescendantPipelineI> getPipelinesForDescendant(String xsiType) {
			List<ArcProjectDescendantPipelineI>rtn = new ArrayList<ArcProjectDescendantPipelineI>();
			List<ArcProjectDescendant> descendants = getPipelines_descendants_descendant();
			if (xsiType == null || descendants == null) return rtn;
			for (int i = 0; i < descendants.size(); i++) {
            if (xsiType.equals(descendants.get(i).getXsitype()) || PipelineRepositoryManager.ALL_DATA_TYPES.equals(descendants.get(i).getXsitype())) {
            	List<ArcProjectDescendantPipelineI> pipelines = descendants.get(i).getPipeline();	
            	if (pipelines != null && pipelines.size() > 0)
            		rtn.addAll(pipelines);
				}
			}
			return rtn;
		}


		public ArrayList<ArcPipelinedataI> getPipelinesForDescendantLikeStepId(String xsiType, String pipelineStep) throws PipelineNotFoundException {
			ArrayList<ArcPipelinedataI> rtn = new ArrayList<ArcPipelinedataI>();
			List<ArcProjectDescendantPipelineI> descendantPipelines = getPipelinesForDescendant(xsiType);
			for (int i = 0; i < descendantPipelines.size(); i++) {
				if (descendantPipelines.get(i).getStepid()!=null && descendantPipelines.get(i).getStepid().startsWith(pipelineStep)) {
					rtn.add(((ArcProjectDescendantPipeline)descendantPipelines.get(i)).getPipelinedata());
				}
			}
			//if (rtn.size() == 0) throw new PipelineNotFoundException("A Pipeline identified by " + pipelineStep + " could not be found for " + xsiType + " for project " + getId());
			return rtn;
		}

		public ArrayList<ArcPipelinedataI> getPipelinesForDescendant(String xsiType, String pipelineStep, String match) throws PipelineNotFoundException {
			ArrayList<ArcPipelinedataI> rtn = new ArrayList<ArcPipelinedataI>();
			if (match.equalsIgnoreCase("EXACT")) {
				ArcPipelinedataI pipeline = getPipelineForDescendant(xsiType, pipelineStep);
				rtn.add(pipeline);
			}else if (match.equalsIgnoreCase("LIKE")) {
			  rtn = getPipelinesForDescendantLikeStepId(xsiType, pipelineStep);	
			}
			//if (rtn.size() == 0) throw new PipelineNotFoundException("A Pipeline identified by " + pipelineStep + " could not be found for " + xsiType + " for project " + getId());
			return rtn;
			
		}		

	public ArcPipelinedataI getPipelineForDescendant(String xsiType, String pipelineStep) throws PipelineNotFoundException {
		ArcPipelinedataI rtn = null;
		List<ArcProjectDescendantPipelineI> descendantPipelines = getPipelinesForDescendant(xsiType);
		for (int i = 0; i < descendantPipelines.size(); i++) {
			if (descendantPipelines.get(i).getStepid().equals(pipelineStep)) {
				rtn = ((ArcProjectDescendantPipeline)descendantPipelines.get(i)).getPipelinedata();
				break;
			}
		}
		if (rtn == null) throw new PipelineNotFoundException("A Pipeline identified by " + pipelineStep + " could not be found for " + xsiType + " for project " + getId());
		return rtn;
	}

	public ArcPipelinedataI getPipelineForDescendantByPath(String xsiType, String pipelinePath) throws PipelineNotFoundException {
		ArcPipelinedataI rtn = null;
		List<ArcProjectDescendantPipelineI> descendantPipelines = getPipelinesForDescendant(xsiType);
		for (int i = 0; i < descendantPipelines.size(); i++) {
			if (descendantPipelines.get(i).getLocation().equals(pipelinePath)) {
				rtn = ((ArcProjectDescendantPipeline)descendantPipelines.get(i)).getPipelinedata();
				break;
			}
		}
		if (rtn == null) throw new PipelineNotFoundException("A Pipeline identified by " + pipelinePath + " could not be found for " + xsiType + " for project " + getId());
		return rtn;
	}

	public ArcProjectDescendantPipeline getPipelineForDescendantEltByPath(String xsiType, String pipelinePath) throws PipelineNotFoundException {
		ArcProjectDescendantPipeline rtn = null;
		List<ArcProjectDescendantPipelineI> descendantPipelines = getPipelinesForDescendant(xsiType);
		for (int i = 0; i < descendantPipelines.size(); i++) {
			if (descendantPipelines.get(i).getLocation().equals(pipelinePath)) {
				rtn =((ArcProjectDescendantPipeline)descendantPipelines.get(i));
				break;
			}
		}
		if (rtn == null) throw new PipelineNotFoundException("A Pipeline identified by " + pipelinePath + " could not be found for " + xsiType + " for project " + getId());
		return rtn;
	}

	public int getPipelineForDescendantIndexByPath(String xsiType, String pipelinePath) throws PipelineNotFoundException {
		int rtn = -1;
		List<ArcProjectDescendantPipelineI> descendantPipelines = getPipelinesForDescendant(xsiType);
		for (int i = 0; i < descendantPipelines.size(); i++) {
			if (descendantPipelines.get(i).getLocation().equals(pipelinePath)) {
				rtn = i;
				break;
			}
		}
		if (rtn == -1) throw new PipelineNotFoundException("A Pipeline identified by " + pipelinePath + " could not be found for " + xsiType + " for project " + getId());
		return rtn;
	}


	public ArcPipelinedataI getPipeline(String pipelineStep) throws PipelineNotFoundException {
		ArcPipelinedataI rtn = null;
		List<ArcProjectPipelineI> pipelines =getPipelines_pipeline();
		for (int i = 0; i < pipelines.size(); i++) {
			if (pipelines.get(i).getStepid().equals(pipelineStep)) {
				rtn = ((ArcProjectPipeline)pipelines.get(i)).getPipelinedata();
				break;
			}
		}
		if (rtn == null) throw new PipelineNotFoundException("A Pipeline identified by " + pipelineStep + " could not be found for  project " + getId());
		return rtn;
	}
	
	/**
	 * Return root level pipeline with matching location and stepId (or null)
	 * @param location
	 * @param stepId
	 * @return
	 */
	public ArcProjectPipelineI getPipeline(String location, String stepId){
		for (ArcProjectPipelineI pipe:getPipelines_pipeline()) {
			if (StringUtils.equals(pipe.getLocation(),location) && StringUtils.equals(pipe.getStepid(),stepId)) {
				return pipe;
			}
		}
		
		return null;
	}
	
	/**
	 * Return descendant pipeline with matching xsiType, location and stepId (or null)
	 * @param xsiType
	 * @param location
	 * @param stepId
	 * @return
	 */
	public ArcProjectDescendantPipelineI getDescendantPipeline(String xsiType, String location, String stepId){
		for (final ArcProjectDescendantPipelineI pipe: getPipelinesForDescendant(xsiType)) {
			if (StringUtils.equals(pipe.getLocation(),location) && StringUtils.equals(pipe.getStepid(),stepId)) {
				return pipe;
			}
		}
		
		return null;
	}


	public ArcProjectPipeline getProjectPipeline(String pipelineStep) throws PipelineNotFoundException {
		ArcProjectPipeline rtn = null;
		List<ArcProjectPipelineI> pipelines =getPipelines_pipeline();
		for (int i = 0; i < pipelines.size(); i++) {
			if (pipelines.get(i).getStepid().equals(pipelineStep)) {
				return ((ArcProjectPipeline)pipelines.get(i));
			}
		}
		return null;
	}



	public ArcPipelinedataI getPipelineByPath(String pipelinePath) throws PipelineNotFoundException {
		ArcPipelinedataI rtn = null;
		List<ArcProjectPipelineI> pipelines =getPipelines_pipeline();
		for (int i = 0; i < pipelines.size(); i++) {
			if (pipelines.get(i).getLocation().equals(pipelinePath)) {
				rtn = ((ArcProjectPipeline)pipelines.get(i)).getPipelinedata();
				break;
			}
		}
		if (rtn == null) throw new PipelineNotFoundException("A Pipeline identified by " + pipelinePath + " could not be found for  project " + getId());
		return rtn;
	}

	public ArcProjectPipeline getPipelineEltByPath(String pipelinePath) throws PipelineNotFoundException {
		ArcProjectPipeline rtn = null;
		List<ArcProjectPipelineI> pipelines =getPipelines_pipeline();
		for (int i = 0; i < pipelines.size(); i++) {
			if (pipelines.get(i).getLocation().equals(pipelinePath)) {
				rtn = (ArcProjectPipeline)pipelines.get(i);
				break;
			}
		}
		if (rtn == null) throw new PipelineNotFoundException("A Pipeline identified by " + pipelinePath + " could not be found for  project " + getId());
		return rtn;
	}

	public int getPipelineIndexByPath(String pipelinePath) throws PipelineNotFoundException {
		int rtn = -1;
		List<ArcProjectPipelineI> pipelines =getPipelines_pipeline();
		for (int i = 0; i < pipelines.size(); i++) {
			if (pipelines.get(i).getLocation().equals(pipelinePath)) {
				rtn = i;
				break;
			}
		}
		if (rtn == -1) throw new PipelineNotFoundException("A Pipeline identified by " + pipelinePath + " could not be found for  project " + getId());
		return rtn;
	}


	public ArcProjectDescendant getDescendant(String xsiType) {
		ArcProjectDescendant rtn = null;
		List<ArcProjectDescendantI> descendants = getPipelines_descendants_descendant();
		if (xsiType == null || descendants == null) return rtn;
		for (int i = 0; i < descendants.size(); i++) {
			if (descendants.get(i).getXsitype().equals(xsiType)) {
				rtn = (ArcProjectDescendant)descendants.get(i);
				break;
			}
		}
		return rtn;
	}

	public void removeAllDescendantPipelines() {
		List<ArcProjectDescendantI> descendants = getPipelines_descendants_descendant();
		if (descendants.size() > 0) {
			int i = 0;
			Iterator iter = descendants.iterator();
			while (iter.hasNext()) {
				removePipelines_descendants_descendant(i);
				descendants = getPipelines_descendants_descendant();
				iter = descendants.iterator();
			}
		}
	}

	public void removeAllPipelines() {
		removeAllDescendantPipelines();
		removeAllProjectPipelines();
	}

	public void removeAllProjectPipelines() {
		List<ArcProjectPipelineI> projPipeline = getPipelines_pipeline();
		if (projPipeline.size() > 0) {
			int i = 0;
			Iterator iter = projPipeline.iterator();
			while (iter.hasNext()) {
				this.removePipelines_pipeline(i);
				projPipeline = getPipelines_pipeline();
				iter = projPipeline.iterator();
			}
		}
	}

}
