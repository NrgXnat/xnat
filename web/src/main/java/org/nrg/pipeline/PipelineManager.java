/*
 * web: org.nrg.pipeline.PipelineManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.pipeline;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;

import org.apache.log4j.Logger;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.ArcProjectDescendantPipeline;
import org.nrg.xdat.om.ArcProjectPipeline;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.base.BaseWrkWorkflowdata;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperElement;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperFactory;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.xml.sax.InputSource;

import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;

public class PipelineManager {
   
   public static final String PIPELINE_STEP_CTXT_VAR = "pipeline_step";
   
   private static Logger logger = Logger.getLogger(PipelineManager.class);
   
   public static String getPipelineStepCtxtVar() {
       return PIPELINE_STEP_CTXT_VAR;
   }
   
   public static ArrayList getWorkFlowsOrderByLaunchTimeDesc(String id, String dataType, org.nrg.xft.security.UserI user) {
       ArrayList workflows = new ArrayList();
       org.nrg.xft.search.CriteriaCollection cc = new CriteriaCollection("AND");
       cc.addClause("wrk:workflowData.ID",id);
       cc.addClause("wrk:workflowData.data_type",dataType);
       //Sort by Launch Time
       try {
           org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(cc,user,false);
           ArrayList workitems = items.getItems("wrk:workflowData.launch_time","DESC");
           Iterator iter = workitems.iterator();
           while (iter.hasNext())
           {
               WrkWorkflowdata vrc = new WrkWorkflowdata((XFTItem)iter.next());
               workflows.add(vrc);
           }
       }catch(Exception e) {
           logger.debug("",e);
       }
      logger.info("Workflows by Ordered by Launch Time " + workflows.size());
       return workflows;
   }
   
   
   public static List<ArcProjectDescendantPipelineI> getPipelinesForProjectDescendant(String projectId, String descendantXsiType, boolean sort) {
       List rtn = new ArrayList();
       ArcProjectI arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(projectId);
       if (arcProject != null) {
         List<ArcProjectDescendantI> descendants = arcProject.getPipelines_descendants_descendant();
         for (ArcProjectDescendantI desc: descendants) {
             if (desc.getXsitype().equals(descendantXsiType)) {
                 List<ArcProjectDescendantPipelineI> pipelines = desc.getPipeline();
                 if (sort) {
	                 Hashtable pipelineHash = new Hashtable();
	                 for (ArcProjectDescendantPipelineI pipe: pipelines) {
	                	 pipelineHash.put(pipe.getStepid(), pipe);
	                 }
	                 Vector v = new Vector(pipelineHash.keySet());
	                 Collections.sort(v);
	                 Iterator it = v.iterator();
	                 while (it.hasNext()) {
	                	 rtn.add(pipelineHash.get(it.next()));
	                 }
                 }else 
                	 rtn = pipelines;
                 break;
             }
         }
       }
       return rtn;
   }
   
   public ArcProjectDescendantPipeline getPipelineForProjectDescendantByPath(String projectId, String descendantXsiType, String pipelineFullPath) {
       ArcProjectI arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(projectId);
       ArcProjectDescendantPipeline rtn = null;
       if (arcProject != null) {
         List<ArcProjectDescendantI> descendants = arcProject.getPipelines_descendants_descendant();
         for (ArcProjectDescendantI desc: descendants) {
             if (desc.getXsitype().equals(descendantXsiType)) {
                 List<ArcProjectDescendantPipelineI> pipelines = desc.getPipeline();
	                 for (ArcProjectDescendantPipelineI pipe: pipelines) {
	                	 if (PipelineManager.getFullPath(pipe).equalsIgnoreCase(pipelineFullPath)) {
	                		 rtn = (ArcProjectDescendantPipeline)pipe; break;
	                	 }
	                 }
                 break;
             }
        }
       }
       return rtn;
   }
   
   public String  getPipelineStepForProjectDescendantByPath(String projectId, String descendantXsiType, String pipelineFullPath) {
	   ArcProjectDescendantPipeline pipeline = getPipelineForProjectDescendantByPath(projectId, descendantXsiType, pipelineFullPath);
	   if (pipeline != null)
	    return pipeline.getStepid();
	   else 
		  return "";  
   }   
   
   public static ArrayList<ArcProjectDescendantPipelineI> getDependentPipelinesForProjectDescendant(String projectId, String descendantXsiType, boolean sort) {
       ArrayList rtn = new ArrayList();
       ArcProjectI arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(projectId);
       if (arcProject != null) {
         List<ArcProjectDescendantI> descendants = arcProject.getPipelines_descendants_descendant();
         for (ArcProjectDescendantI desc: descendants) {
             if (desc.getXsitype().equals(descendantXsiType)) {
                 List<ArcProjectDescendantPipelineI> pipelines = desc.getPipeline();
                 ArrayList<ArcProjectDescendantPipeline> dependent = new ArrayList<ArcProjectDescendantPipeline>();
                 for (ArcProjectDescendantPipelineI pipe: pipelines) {
                	 if (pipe.getDependent()) 
                		 dependent.add((ArcProjectDescendantPipeline)pipe);
                 }
                 if (sort) {
	                 Hashtable pipelineHash = new Hashtable();
	                 for (ArcProjectDescendantPipeline pipe: dependent) {
	                	 pipelineHash.put(pipe.getStepid(), pipe);
	                 }
	                 Vector v = new Vector(pipelineHash.keySet());
	                 Collections.sort(v);
	                 Iterator it = v.iterator();
	                 while (it.hasNext()) {
	                	 rtn.add(pipelineHash.get(it.next()));
	                 }
                 }else 
                	 rtn = dependent;
                 break;
             }
         }
       }
       return rtn;
   }


   
   public static ArrayList<ArcProjectDescendantPipelineI> getIndependentPipelinesForProjectDescendant(String projectId, String descendantXsiType, boolean sort) {
       ArrayList rtn = new ArrayList();
       ArcProjectI arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(projectId);
       if (arcProject != null) {
         List<ArcProjectDescendantI> descendants = arcProject.getPipelines_descendants_descendant();
         for (ArcProjectDescendantI desc: descendants) {
             if (desc.getXsitype().equals(descendantXsiType)) {
                 List<ArcProjectDescendantPipelineI> pipelines = desc.getPipeline();
                 ArrayList<ArcProjectDescendantPipeline> independent = new ArrayList<ArcProjectDescendantPipeline>();
                 for (ArcProjectDescendantPipelineI pipe: pipelines) {
                	 if (!pipe.getDependent()) 
                		 independent.add((ArcProjectDescendantPipeline)pipe);
                 }
                 if (sort) {
	                 Hashtable pipelineHash = new Hashtable();
	                 for (ArcProjectDescendantPipeline pipe: independent) {
	                	 pipelineHash.put(pipe.getStepid(), pipe);
	                 }
	                 Vector v = new Vector(pipelineHash.keySet());
	                 Collections.sort(v);
	                 Iterator it = v.iterator();
	                 while (it.hasNext()) {
	                	 rtn.add(pipelineHash.get(it.next()));
	                 }
                 }else 
                	 rtn = independent;
                 break;
             }
         }
       }
       return rtn;
   }
   
   
   public static ArcProjectDescendantPipelineI getPipelineForProjectDescendant(String projectId, String stepId, String descendantXsiType) {
       List<ArcProjectDescendantPipelineI> pipelines = getPipelinesForProjectDescendant(projectId, descendantXsiType, false);
       ArcProjectDescendantPipelineI rtn = null;
       if (pipelines != null) {
           for (ArcProjectDescendantPipelineI pipe:pipelines) {
               if (pipe.getStepid().equals(stepId)) {
                   rtn = pipe; break;
               }
           }
       }
       return rtn;
   }

   public static ArcProjectDescendantPipelineI getPipelineForProjectDescendantBySequence(String projectId, int stepNo, String descendantXsiType) {
       List<ArcProjectDescendantPipelineI> pipelines = getPipelinesForProjectDescendant(projectId, descendantXsiType, true);
       ArcProjectDescendantPipelineI rtn = null;
       if (pipelines != null) {
           try {
               rtn = pipelines.get(stepNo);
           }catch(IndexOutOfBoundsException e){}
       }
       return rtn;
   }

   public static String getPathToPipelineForProject(String projectId, String stepId, String descendantXsiType) {
       String rtn = "";
       ArcProjectDescendantPipelineI pipeline = getPipelineForProjectDescendant(projectId, stepId, descendantXsiType);
       if (pipeline != null) {
    	   rtn = getFullPath(pipeline);
       }
       return rtn;
   }
   
   public static String getFullPath(ArcProjectDescendantPipelineI pipeline) {
   	String rtn = null;
   	if (pipeline != null) {
	   	 if (pipeline.getLocation() != null) {
	            rtn = pipeline.getLocation();
	            if (!rtn.endsWith(File.separator)) 
	                rtn += File.separator; 
	        }
	        rtn += pipeline.getName();
	        if (!rtn.endsWith(".xml")) rtn += ".xml";
   	}
        return rtn;
   }

   public static String getFullPath(ArcProjectPipelineI pipeline) {
	   	String rtn = null;
	   	if (pipeline != null) {
		   	 if (pipeline.getLocation() != null) {
		            rtn = pipeline.getLocation();
		            if (!rtn.endsWith(File.separator)) 
		                rtn += File.separator; 
		        }
		        rtn += pipeline.getName();
		        if (!rtn.endsWith(".xml")) rtn += ".xml";
	   	}
	        return rtn;
	   }

   
   public static String getPipelineNameForProject(String projectId, String stepId) {
       String rtn = "";
       ArcProjectPipelineI pipeline = getPipelineForProject(projectId, stepId);
       if (pipeline != null) {
           rtn += pipeline.getName();
       }
       return rtn;
   }
   
   public static String getPipelineNameForProject(String projectId, String stepId, String descendantXsiType) {
       String rtn = "";
       ArcProjectDescendantPipelineI pipeline = getPipelineForProjectDescendant(projectId, stepId, descendantXsiType);
       if (pipeline != null) {
           rtn += pipeline.getName();
       }
       return rtn;
   }

   public static LinkedHashMap getBatchParametersForDescendantPipeline(String projectId, String stepId, String descendantXsiType) {
       LinkedHashMap rtn = new LinkedHashMap();
       try {
           List params = getParametersForDescendantPipeline(projectId, stepId, descendantXsiType);
           if (params != null) {
               for (int i = 0; i < params.size(); i++) {
                   ArcPipelineparameterdataI aParam = ((ArcPipelineparameterdataI)params.get(i));
                   if (aParam.getBatchparam()) {
                       if (aParam.getCsvvalues() != null) {
                           String csvValues = aParam.getCsvvalues().trim();
                           ArrayList<String> values = new ArrayList<String>(Arrays.asList(csvValues.split(",")));
                           rtn.put(aParam,values);
                       }else {
                           ArrayList<String> values = new ArrayList(); values.add("");
                           rtn.put(aParam,values);
                       }
                   }
               }
           }
       }catch(Exception e){}
       return rtn;
   }
   
   public static ArcPipelineparameterdataI getDescendantParameterByName(String projectId, String step, String descendantXsiType, String nameToMatch, boolean ignoreCase) throws Exception{
       List  params = getParametersForDescendantPipeline(projectId,step, descendantXsiType);
       ArcPipelineparameterdataI rtn = null;
       if (params == null) return null;
       for (int i = 0; i < params.size(); i++) {
           ArcPipelineparameterdataI aParam = (ArcPipelineparameterdataI)params.get(i);
           if ((ignoreCase?aParam.getName().equalsIgnoreCase(nameToMatch):aParam.getName().equals(nameToMatch))) {
               rtn = aParam;
               break;
           }
       }
       return rtn;
   } 

  
   
   public static LinkedHashMap getResolvedParametersForDescendantPipeline(String stepId, String projectId, ItemI item) {
       //System.out.println("Came here " + stepId + "  " +  projectId);
       LinkedHashMap parametersHash = new LinkedHashMap();
       try {
           String descendantXsiType = item.getXSIType();
           List parameters = getParametersForDescendantPipeline(projectId,stepId, descendantXsiType);
           SAXSource ss = new SAXSource(new  InputSource(new ByteArrayInputStream(item.toXML_BOS(TurbineUtils.GetFullServerPath() + "/schemas").toByteArray())));
           DocumentInfo docInfo = new StaticQueryContext(new Configuration()).buildDocument(ss);
    
           if (parameters == null) {
               return parametersHash;
           }
           for (int i = 0; i < parameters.size(); i++) {
               ArcPipelineparameterdataI aParameter = ((ArcPipelineparameterdataI)parameters.get(i));
               if (aParameter.getCsvvalues() != null) {
                   String csvValues = aParameter.getCsvvalues().trim();
                   ArrayList<String> values = new ArrayList<String>(Arrays.asList(csvValues.split(",")));
                   parametersHash.put(aParameter,values);
                   //System.out.println("Am inserting " + aParameter + " " + values);
               }else if (aParameter.getSchemalink() != null){
                    ArrayList values = resolveXPath(aParameter.getSchemalink(), item, docInfo);
                    if (values != null) parametersHash.put(aParameter,values);
                   //Saxon seems to have problem evaluating recursively. 
                   // SXXP0003 Premature end of file
                   // System.out.println("Am inserting " + aParameter + " " + values);
               }else {
                   ArrayList<String> values = new ArrayList(); values.add("");
                   parametersHash.put(aParameter,values);
               }
           }
       }catch(Exception e) {
           logger.debug(e);
       }
       //System.out.println("Returning size is " + parametersHash.size());
       return parametersHash;
   }
   
   public static List getParametersForDescendantPipeline(String projectId, String stepId, String descendantXsiType) throws Exception{
       List rtn = null;
       ArcProjectDescendantPipelineI arcProject = getPipelineForProjectDescendant(projectId, stepId, descendantXsiType);
       if (arcProject == null)  return rtn;
       rtn = arcProject.getParameters_parameter();
       return rtn;
   } 

   public static boolean isDependentDescendantPipeline(String projectId, String stepId, String descendantXsiType) {
       List<ArcProjectDescendantPipelineI> pipelines = getPipelinesForProjectDescendant(projectId, descendantXsiType, true);
       return isDependentDescendantPipeline(pipelines, stepId);
   }

   /*
    * Returns if the current step can be launched by checking if the 
    * step is dependent on all previous steps or not. 
    * If it's dependent then the workflowstatus of all previous dependent steps is  checked.
    * If any one of the previous have failed, returns false. 
    */
   
   public static BuildStatus canDescendantPipelineBeLaunched(String projectId, String stepId, ItemI item, UserI user) {
       BuildStatus rtn = new BuildStatus(null,false);
       String descendantXsiType = item.getXSIType();
       try {
           String descendantId = item.getStringProperty("ID");
           if (descendantId != null) {
               List<ArcProjectDescendantPipelineI> pipelines = getPipelinesForProjectDescendant(projectId, descendantXsiType, true);
               rtn = canDescendantPipelineBeLaunched(pipelines,descendantId,descendantXsiType, stepId, user);
           }
       }catch(FieldNotFoundException fne) {
           
       }catch(ElementNotFoundException fne) {
           
       }catch(XFTInitException fne) {
           
       }
       return rtn;
   }

   
   /* PROJECT BASED PIPELINE QUERIES */
   
    public static List<ArcProjectPipelineI> getPipelinesForProject(String projectId, boolean sort) {
        List rtn = new ArrayList();
        ArcProjectI arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(projectId);
        if (arcProject != null) {
          List<ArcProjectPipelineI> pipelines = arcProject.getPipelines_pipeline();
          if (sort) {
	          Hashtable pipelineHash = new Hashtable();
	          for (ArcProjectPipelineI pipe: pipelines) {
	        	  pipelineHash.put(pipe.getStepid(), pipe);
	          }
	          Vector v = new Vector(pipelineHash.keySet());
	          Collections.sort(v);
	          Iterator it = v.iterator();
	          while (it.hasNext()) {
	        	  rtn.add(pipelineHash.get(it.next()));
	          }
          }else 
        	  rtn = pipelines;
        }
        return rtn;
    }
    
    public static ArrayList<ArcProjectPipelineI> getDependentPipelinesForProject(String projectId, boolean sort) {
        ArrayList rtn = new ArrayList();
        ArcProjectI arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(projectId);
        if (arcProject != null) {
          List<ArcProjectPipelineI> pipelines = arcProject.getPipelines_pipeline();
          ArrayList<ArcProjectPipeline> dependent = new ArrayList<ArcProjectPipeline>();
          for (ArcProjectPipelineI pipe: pipelines) {
        	  if (pipe.getDependent()) dependent.add((ArcProjectPipeline)pipe);
          }
          if (sort) {
	          Hashtable pipelineHash = new Hashtable();
	          for (ArcProjectPipeline pipe: dependent) {
	        	  pipelineHash.put(pipe.getStepid(), pipe);
	          }
	          Vector v = new Vector(pipelineHash.keySet());
	          Collections.sort(v);
	          Iterator it = v.iterator();
	          while (it.hasNext()) {
	        	  rtn.add(pipelineHash.get(it.next()));
	          }
          }else 
        	  rtn = dependent;
        }
        return rtn;
    }
    
    public static ArrayList<ArcProjectPipelineI> getIndependentPipelinesForProject(String projectId, boolean sort) {
        ArrayList rtn = new ArrayList();
        ArcProjectI arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(projectId);
        if (arcProject != null) {
          List<ArcProjectPipelineI> pipelines = arcProject.getPipelines_pipeline();
          ArrayList<ArcProjectPipeline> independent = new ArrayList<ArcProjectPipeline>();
          for (ArcProjectPipelineI pipe: pipelines) {
        	  if (!pipe.getDependent()) independent.add((ArcProjectPipeline)pipe);
          }
          if (sort) {
	          Hashtable pipelineHash = new Hashtable();
	          for (ArcProjectPipeline pipe: independent) {
	        	  pipelineHash.put(pipe.getStepid(), pipe);
	          }
	          Vector v = new Vector(pipelineHash.keySet());
	          Collections.sort(v);
	          Iterator it = v.iterator();
	          while (it.hasNext()) {
	        	  rtn.add(pipelineHash.get(it.next()));
	          }
          }else 
        	  rtn = independent;
        }
        return rtn;
    }
    
    public static ArcProjectPipelineI getPipelineForProject(String projectId, String stepId) {
        List<ArcProjectPipelineI> pipelines = getPipelinesForProject(projectId,false);
        ArcProjectPipelineI rtn = null;
        if (pipelines != null) {
            for (ArcProjectPipelineI pipe:pipelines) {
                if (pipe.getStepid().equals(stepId)) {
                    rtn = pipe; break;
                }
            }
        }
        return rtn;
    }
    
    public ArcProjectPipelineI getPipelineForProjectByPath(String projectId,  String pipelineFullPath) {
        List<ArcProjectPipelineI> pipelines = getPipelinesForProject(projectId,false);
        ArcProjectPipelineI rtn = null;
         for (ArcProjectPipelineI pipe: pipelines) {
        	 if (PipelineManager.getFullPath(pipe).equalsIgnoreCase(pipelineFullPath)) {
        		 rtn = pipe; break;
        	 }
         }
        return rtn;
    }
    
    public String  getPipelineStepForProjectByPath(String projectId,  String pipelineFullPath) {
 	   ArcProjectPipelineI pipeline = getPipelineForProjectByPath(projectId,  pipelineFullPath);
 	   if (pipeline != null)
 	    return pipeline.getStepid();
 	   else 
 		  return "";  
    }   

    public static ArcProjectPipelineI getPipelineForProjectBySequence(String projectId, int stepNo) {
        List<ArcProjectPipelineI> pipelines = getPipelinesForProject(projectId, false);
        ArcProjectPipelineI rtn = null;
        if (pipelines != null) {
            try {
                rtn = pipelines.get(stepNo);
            }catch(IndexOutOfBoundsException e){}
        }
        return rtn;
    }
    
    public static String getPathToPipelineForProject(String projectId, String stepId) {
        String rtn = "";
        ArcProjectPipelineI pipeline = getPipelineForProject(projectId, stepId);
        if (pipeline != null) {
            if (pipeline.getLocation() != null) {
                rtn = pipeline.getLocation();
                if (!rtn.endsWith(File.separator)) 
                    rtn = pipeline.getLocation() + File.separator; 
            }
            rtn += pipeline.getName();
        }
        if (!rtn.endsWith(".xml")) rtn += ".xml";
        return rtn;
    }
    
    public static  ArrayList getWorkFlowStatus(String id, String data_type, org.nrg.xft.security.UserI user) {
        org.nrg.xft.search.CriteriaCollection cc = new CriteriaCollection("AND");
        cc.addClause("wrk:workflowData.ID",id);
        cc.addClause("wrk:workflowData.data_type",data_type);
        return WrkWorkflowdata.getWrkWorkflowdatasByField(cc,user,false);

    }
    
    public static String getWorkFlowStatus(String id, String data_type, String pipelinePath, org.nrg.xft.security.UserI user) {
        String rtn = null;
        org.nrg.xft.search.CriteriaCollection cc = new CriteriaCollection("AND");
        cc.addClause("wrk:workflowData.ID",id);
        cc.addClause("wrk:workflowData.data_type",data_type);
        cc.addClause("wrk:workflowData.pipeline_name",pipelinePath);
        ArrayList rtns = WrkWorkflowdata.getWrkWorkflowdatasByField(cc,user,false,"wrk:workflowData.launch_time","desc");
        if (rtns != null && rtns.size() > 0) {
                rtn = ((WrkWorkflowdata)rtns.get(0)).getStatus();
        }
        return rtn;
    }
    
  
    
    public static LinkedHashMap getBatchParametersForPipeline(String projectId, String stepId) {
        LinkedHashMap rtn = new LinkedHashMap();
        try {
            List params = getParametersForPipeline(projectId, stepId);
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    ArcPipelineparameterdataI aParam = ((ArcPipelineparameterdataI)params.get(i));
                    if (aParam.getBatchparam()) {
                        if (aParam.getCsvvalues() != null) {
                            String csvValues = aParam.getCsvvalues().trim();
                            ArrayList<String> values = new ArrayList<String>(Arrays.asList(csvValues.split(",")));
                            rtn.put(aParam,values);
                        }else {
                            ArrayList<String> values = new ArrayList(); values.add("");
                            rtn.put(aParam,values);
                        }
                    }
                }
            }
        }catch(Exception e){}
        return rtn;
    }
    
    public static ArrayList resolveXPath(String xpathStmt, ItemI item, DocumentInfo docInfo) throws Exception {
        final List<String> rtn = new ArrayList<>();
        XPathEvaluator xpe = new XPathEvaluator();
        XMLWrapperElement element = (XMLWrapperElement)XFTMetaManager.GetWrappedElementByName(XMLWrapperFactory.GetInstance(),item.getXSIType());
        if (element.getSchemaTargetNamespacePrefix() != null && element.getSchemaTargetNamespacePrefix().equals("") && element.getSchemaTargetNamespaceURI() != null) {
            ((IndependentContext)xpe.getStaticContext()).declareNamespace("", element.getSchemaTargetNamespaceURI());
        }else if (element.getSchemaTargetNamespacePrefix() != null && !element.getSchemaTargetNamespacePrefix().equals("") && element.getSchemaTargetNamespaceURI() != null) {
            ((IndependentContext)xpe.getStaticContext()).declareNamespace(element.getSchemaTargetNamespacePrefix(), element.getSchemaTargetNamespaceURI());
        }
        XPathExpression xExpr =  xpe.createExpression(xpathStmt);
        SequenceIterator rtns = xExpr.rawIterator(docInfo);
        Item xpathObj = rtns.next();
        while (xpathObj != null) {
            xpathObj = rtns.current();
            rtn.add(xpathObj.getStringValue());
            xpathObj = rtns.next();
        }
        return (ArrayList) rtn;
    }
    
    public static ArrayList resolveXPath(String xpathStmt, SAXSource ss,XMLWrapperElement element ) throws Exception {
        final List<String> rtn = new ArrayList<>();
        XPathEvaluator xpe = new XPathEvaluator();
        if (element.getSchemaTargetNamespacePrefix() != null && element.getSchemaTargetNamespacePrefix().equals("") && element.getSchemaTargetNamespaceURI() != null) {
            ((IndependentContext)xpe.getStaticContext()).declareNamespace("", element.getSchemaTargetNamespaceURI());
        }else if (element.getSchemaTargetNamespacePrefix() != null && !element.getSchemaTargetNamespacePrefix().equals("") && element.getSchemaTargetNamespaceURI() != null) {
            ((IndependentContext)xpe.getStaticContext()).declareNamespace(element.getSchemaTargetNamespacePrefix(), element.getSchemaTargetNamespaceURI());
        }
        XPathExpression xExpr =  xpe.createExpression(xpathStmt);
        SequenceIterator rtns = xExpr.rawIterator(ss);
        Item xpathObj = rtns.next();
        while (xpathObj != null) {
            xpathObj = rtns.current();
            rtn.add(xpathObj.getStringValue());
            xpathObj = rtns.next();
        }
        return (ArrayList) rtn;
    }


    public static ArcPipelineparameterdataI getParameterByName(LinkedHashMap parametersHash, String name, boolean ignorecase) {
        Iterator paramIter = parametersHash.keySet().iterator();
        ArcPipelineparameterdataI rtn = null;
        while (paramIter.hasNext()) {
            ArcPipelineparameterdataI param = (ArcPipelineparameterdataI)paramIter.next();
            if ((ignorecase?param.getName().equalsIgnoreCase(name):param.getName().equals(name))) {
                rtn = param;
                break;
            }
        }
        return rtn;
    }

    public static ArcPipelineparameterdataI getParameterByName(String projectId, String step,String nameToMatch, boolean ignoreCase) throws Exception{
        List  params = getParametersForPipeline(projectId,step);
        ArcPipelineparameterdataI rtn = null;
        if (params == null) return null;
        for (int i = 0; i < params.size(); i++) {
            ArcPipelineparameterdataI aParam = (ArcPipelineparameterdataI)params.get(i);
            if ((ignoreCase?aParam.getName().equalsIgnoreCase(nameToMatch):aParam.getName().equals(nameToMatch))) {
                rtn = aParam;
                break;
            }
        }
        return rtn;
    } 

   
    
    public static LinkedHashMap getResolvedParametersForPipeline(String stepId, String projectId, ItemI item) throws Exception {
        //System.out.println("Came here " + stepId + "  " +  projectId);
        LinkedHashMap parametersHash = new LinkedHashMap();
        try {
            List parameters = getParametersForPipeline(projectId,stepId);
            SAXSource ss = new SAXSource(new  InputSource(new ByteArrayInputStream(item.toXML_BOS(TurbineUtils.GetFullServerPath() + "/schemas").toByteArray())));
            DocumentInfo docInfo = new StaticQueryContext(new Configuration()).buildDocument(ss);
    
            if (parameters == null) {
                return parametersHash;
            }
            for (int i = 0; i < parameters.size(); i++) {
                ArcPipelineparameterdataI aParameter = ((ArcPipelineparameterdataI)parameters.get(i));
                if (aParameter.getCsvvalues() != null) {
                    String csvValues = aParameter.getCsvvalues().trim();
                    ArrayList<String> values = new ArrayList<String>(Arrays.asList(csvValues.split(",")));
                    parametersHash.put(aParameter,values);
                    //System.out.println("Am inserting " + aParameter + " " + values);
                }else if (aParameter.getSchemalink() != null){
                     ArrayList values = resolveXPath(aParameter.getSchemalink(), item, docInfo);
                     if (values != null) parametersHash.put(aParameter,values);
                    //Saxon seems to have problem evaluating recursively. 
                    // SXXP0003 Premature end of file
                    // System.out.println("Am inserting " + aParameter + " " + values);
                }else {
                    ArrayList<String> values = new ArrayList(); values.add("");
                    parametersHash.put(aParameter,values);
                }
            }
        }catch(Exception e) {
            logger.debug(e);
        }
        return parametersHash;
    }
    
    public static List getParametersForPipeline(String projectId, String stepId) throws Exception{
        List rtn = null;
        ArcProjectPipelineI arcProject = getPipelineForProject(projectId, stepId);
        if (arcProject == null)  return rtn;
        rtn = arcProject.getParameters_parameter();
        return rtn;
    } 
    

    private static boolean isDependentPipeline(List<ArcProjectPipelineI> pipelines, String stepId) {
        boolean rtn = true;
        if (pipelines != null) {
            for (ArcProjectPipelineI pipe:pipelines) {
                if (pipe.getStepid().equals(stepId)) {
                    if (pipe.getDependent() != null) rtn = pipe.getDependent();
                    break; 
                }
            }
        }
        return rtn;
    }
    
    private static boolean isDependentDescendantPipeline(List<ArcProjectDescendantPipelineI> pipelines, String stepId) {
        boolean rtn = true;
        if (pipelines != null) {
            for (ArcProjectDescendantPipelineI pipe:pipelines) {
                if (pipe.getStepid().equals(stepId)) {
                    if (pipe.getDependent() != null) rtn = pipe.getDependent();
                    break; 
                }
            }
        }
        return rtn;
    }
    
    public static boolean isDependentPipeline(String projectId, String stepId) {
        List<ArcProjectPipelineI> pipelines = getPipelinesForProject(projectId, false);
        return isDependentPipeline(pipelines, stepId);
    }
    
    /*
     * Returns if the current step can be launched by checking if the 
     * step is dependent on all previous steps or not. 
     * If it's dependent then the workflowstatus of all previous dependent steps is  checked.
     * If any one of the previous have failed, returns false. 
     */
    
    public static BuildStatus canPipelineBeLaunched(String projectId, String stepId,  UserI user) {
        List<ArcProjectPipelineI> pipelines = getPipelinesForProject(projectId, false);
        return canPipelineBeLaunched(pipelines,projectId,org.nrg.xdat.om.XnatProjectdata.SCHEMA_ELEMENT_NAME,stepId, user);
    }
    
    
    private static BuildStatus canPipelineBeLaunched(List<ArcProjectPipelineI> pipelines, String id, String xsiType, String stepId, UserI user) {
        BuildStatus rtn = new BuildStatus(null,false);
        boolean checkDependency = false;
        if (pipelines != null) {
                for (ArcProjectPipelineI pipe:pipelines) {
                    if (pipe.getStepid().equals(stepId)) {
                        String status = org.nrg.xdat.om.WrkWorkflowdata.GetLatestWorkFlowStatus(id, xsiType, null, pipe.getName(), user);
                        rtn.setWorkFlowStatus(status);
                        if (status.equalsIgnoreCase(BaseWrkWorkflowdata.QUEUED) || status.equalsIgnoreCase(BaseWrkWorkflowdata.RUNNING)) rtn.setDisabled(true);
                        if (pipe.getDependent()) checkDependency = true; 
                        break; 
                   }
              }
              if (checkDependency) {
            	  for (ArcProjectPipelineI pipe:pipelines) {
            		  if (pipe.getStepid().equals(stepId)) break;
            		  else {
                          String status = org.nrg.xdat.om.WrkWorkflowdata.GetLatestWorkFlowStatus(id, xsiType, null, pipe.getName(), user);
                          if (!status.equalsIgnoreCase(BaseWrkWorkflowdata.COMPLETE)) {
                        	  rtn.setDisabled(true);
                        	  rtn.setWorkFlowStatus("previous pipeline is not complete");
                          }
            		  }
            	  }
              }
        }
        return rtn;
    }
    
    private static BuildStatus canDescendantPipelineBeLaunched(List<ArcProjectDescendantPipelineI> pipelines, String id, String xsiType, String stepId, UserI user) {
        BuildStatus rtn = new BuildStatus(null,false);
        boolean checkDependency = false;
        if (pipelines != null) {
                for (ArcProjectDescendantPipelineI pipe:pipelines) {
                    if (pipe.getStepid().equals(stepId)) {
                        String status = org.nrg.xdat.om.WrkWorkflowdata.GetLatestWorkFlowStatus(id, xsiType, null, pipe.getName(), user);
                        rtn.setWorkFlowStatus(status);
                        if (status.equalsIgnoreCase(BaseWrkWorkflowdata.QUEUED) || status.equalsIgnoreCase(BaseWrkWorkflowdata.RUNNING)) rtn.setDisabled(true);
                        if (pipe.getDependent()) checkDependency = true; 
                        break; 
                   }
              }
              if (checkDependency) {
            	  for (ArcProjectDescendantPipelineI pipe:pipelines) {
            		  if (pipe.getStepid().equals(stepId)) break;
            		  else {
                          String status = org.nrg.xdat.om.WrkWorkflowdata.GetLatestWorkFlowStatus(id, xsiType, null, pipe.getName(), user);
                          if (!status.equalsIgnoreCase(BaseWrkWorkflowdata.COMPLETE)) {
                        	  rtn.setDisabled(true);
                        	  rtn.setWorkFlowStatus("previous pipeline is not complete" );
                          }
            		  }
            	  }
              }
        }  
        return rtn;
    }
    
    public static int indexOfFirstLaunchablePipeline(ArrayList<ArcProjectDescendantPipelineI> pipelines, ItemI item, UserI user) {
        int rtn = 0;
        try {
            String xsiType = item.getXSIType();
            String id = (String)item.getProperty("ID");
            if (pipelines != null) {
                for (int i = 0 ; i < pipelines.size(); i++) {
                    ArcProjectDescendantPipelineI pipe  = pipelines.get(i);
                    if (!pipe.getDependent()) {
                         rtn = i; break;
                    }else if (pipe.getDependent() && !org.nrg.xdat.om.WrkWorkflowdata.GetLatestWorkFlowStatus(id, xsiType, null, pipe.getName(), user).equalsIgnoreCase(WrkWorkflowdata.COMPLETE)) {
                        rtn = i; break;
                    }
                }
            }
        }catch(Exception e) {
            logger.debug(e);
        }
        return rtn;
    }
    
}
