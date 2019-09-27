package org.nrg.pipeline;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.pipeline.utils.PipelineFileUtils;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ParameterData.Values;
import org.nrg.pipeline.xmlbeans.ParametersDocument;
import org.nrg.pipeline.xmlbeans.ParametersDocument.Parameters;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.ArcPipelinedataI;
import org.nrg.xdat.model.ArcPipelineparameterdataI;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

/**
 * @author Mohana Ramaratnam
 *
 */
public class PipelineLaunchHandler {
	
	XnatProjectdata proj=null;
	XnatExperimentdata expt=null;
    String step = null;
    Logger logger = Logger.getLogger(PipelineLaunchHandler.class);

	
	public PipelineLaunchHandler(XnatProjectdata proj, XnatExperimentdata expt,String step) {
		this.proj = proj;
		this.expt = expt;
		this.step = step;
	}
	

	public boolean handleLaunch(Map<String,String> bodyParams, Map<String,String> queryParams,  Map<String,String> xmlDocumentParams, String XMLbody, final UserI user) {
		boolean launchSuccess = false;
		try {
		ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(proj.getId());
        if (null != xmlDocumentParams && null != XMLbody  && StringUtils.isNotBlank(XMLbody)) {
            ParametersDocument doc = ParametersDocument.Factory.parse(XMLbody);
            for (ParameterData param : doc.getParameters().getParameterArray()) {
                Values values = param.getValues();
                if (values.isSetUnique()) {
                    xmlDocumentParams.put(param.getName(), values.getUnique());
                } else {
                    String listCSV = "[" + StringUtils.join(values.getListArray(), ",") + "]";
                    xmlDocumentParams.put(param.getName(), listCSV);
                }
            }
        }

        // Find the "match" query param if it exists
        String match;
        if (queryParams.containsKey("match")) {
            match = queryParams.get("match");
            queryParams.remove("match");
        } else {
            match = "EXACT";
        }

        // LEGACY MODE
        // Assume we want to use legacy mode
        // If we have passed in "legacy" as a query param, if "legacy=true" we are in legacy mode, else not
        // Else, if we have ANY params in the form body, xml document, or query params we are not in legacy mode
        boolean legacy = true;
        if (queryParams.containsKey("legacy")) {
            legacy = queryParams.get("legacy").equalsIgnoreCase("true");
            queryParams.remove("legacy");
        } else if (!bodyParams.keySet().isEmpty() || !xmlDocumentParams.keySet().isEmpty() || !queryParams.keySet().isEmpty()) {
            legacy = false;
        }

        // Put all params from all sources into one map.
        Map<String,String> pipelineParams = new HashMap<String, String>();
        pipelineParams.putAll(queryParams);
        pipelineParams.putAll(bodyParams);
        pipelineParams.putAll(xmlDocumentParams);

		try {
			ArrayList<ArcPipelinedataI> arcPipelines = arcProject.getPipelinesForDescendant(expt.getXSIType(), step, match);
            for (ArcPipelinedataI arcPipeline : arcPipelines) {

                logger.info("Launching pipeline at step " + arcPipeline.getLocation() + File.separator + arcPipeline.getName());
                if (legacy) {
                	launchSuccess = launch(arcPipeline, user);
                } else {
                	launchSuccess = launch(arcPipeline,pipelineParams, user);
                }
			}
		}catch(Exception e) {
			e.printStackTrace();
            logger.error("Pipeline step " + step + " for project " + proj.getId() + " does not exist " + e.getLocalizedMessage());
			//getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		}catch(XmlException x) {
			x.printStackTrace();
            logger.error("Unable to parse the uploaded XML for Pipeline step " + step + " for project " + proj.getId() + " Experiment " + expt.getId());
		}
		return launchSuccess;
	}

	   private boolean launch(ArcPipelinedataI arcPipeline, Map<String,String> paramsMap, final UserI user) throws Exception {

			XnatPipelineLauncher xnatPipelineLauncher = new XnatPipelineLauncher(user);
	        xnatPipelineLauncher.setSupressNotification(true);

	        List<String> hasParams = new ArrayList<String>();
	        xnatPipelineLauncher.setParameter("useremail", user.getEmail());
	        hasParams.add("useremail");
	        xnatPipelineLauncher.setParameter("userfullname", XnatPipelineLauncher.getUserName(user));
	        hasParams.add("userfullname");
	        xnatPipelineLauncher.setParameter("adminemail", XDAT.getSiteConfigPreferences().getAdminEmail());
	        hasParams.add("adminemail");
	        xnatPipelineLauncher.setParameter("mailhost", XDAT.getNotificationsPreferences().getSmtpServer().getHostname());
	        hasParams.add("mailhost");
	        xnatPipelineLauncher.setParameter("xnatserver", TurbineUtils.GetSystemName());
	        hasParams.add("xnatserver");


	        xnatPipelineLauncher.setPipelineName(arcPipeline.getLocation());
	        xnatPipelineLauncher.setId(expt.getId());
	        hasParams.add("id");
	        xnatPipelineLauncher.setLabel(expt.getLabel());
	        hasParams.add("label");
	        xnatPipelineLauncher.setExternalId(expt.getProject());
	        hasParams.add("project");
	        xnatPipelineLauncher.setDataType(expt.getXSIType());
	        hasParams.add("dataType");

	        String buildDir = PipelineFileUtils.getBuildDir(expt.getProject(), true);
	        buildDir += "restlaunch";
	        xnatPipelineLauncher.setBuildDir(buildDir);
	        xnatPipelineLauncher.setNeedsBuildDir(false);

	        Parameters parameters = Parameters.Factory.newInstance();
	        ParameterData param;

	        // Set all the parameters we were fed
	        //    (unless we already got them from the context)
	        for (String paramName : paramsMap.keySet()) {
	            if (hasParams.contains(paramName)) {
	                continue;
	            }
	            param = parameters.addNewParameter();
	            param.setName(paramName);
	            Values values = param.addNewValues();

	            String paramVal = paramsMap.get(paramName);
	            if (paramVal == null) {
	                values.setUnique("");
	                hasParams.add(paramName);
	                continue;
	            }

	            if (paramVal.length() > 2 && paramVal.startsWith("[") && paramVal.endsWith("]")) {
	                String[] paramArray = StringUtils.substringBetween(paramVal,"[","]").split(",");
	                values.setListArray(paramArray);
	            } else {
	                values.setUnique(""+paramVal);
	            }
	            hasParams.add(paramName);
	        }

	        // Get all the input parameters the pipeline wants.
	        // If they haven't been set yet, use their default values.
	        XFTItem itemOfExpectedXsiType = expt.getItem();
	        List<ArcPipelineparameterdataI> pipelineParameters = arcPipeline.getParameters_parameter();
	        for (ArcPipelineparameterdataI pipelineParam : pipelineParameters) {
	            if (hasParams.contains(pipelineParam.getName())) {
	                continue;
	            }

	            String schemaLink = pipelineParam.getSchemalink();
	            String paramCsv = pipelineParam.getCsvvalues();
	            if (schemaLink == null && paramCsv == null) {
	                // param has no default value, and we were not given a value, so skip it
	                continue;
	            }

	            param = parameters.addNewParameter();
	            param.setName(pipelineParam.getName());
	            Values values = param.addNewValues();

	            if (schemaLink != null) {
	                Object o = itemOfExpectedXsiType.getProperty(schemaLink, true);
	                if (o != null ) {
	                    try {
	                        ArrayList<? extends Class> matches = (ArrayList<? extends Class>) o;
	                        if (matches.size() == 1) {
	                            values.setUnique(""+matches.get(0));
	                        }else {
	                            for (Object match : matches) {
	                                values.addList(""+match);
	                            }
	                        }
	                    } catch(ClassCastException  cce) {
	                        values.setUnique(""+o);
	                    }
	                }
	            } else {
	                String[] paramArray = paramCsv.split(",");
	                if (paramArray.length == 1) {
	                    values.setUnique(paramArray[0]);
	                } else {
	                    values.setListArray(paramArray);
	                }
	            }
	        }

	        Date date = new Date();
	        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
	        String dateSuffix = formatter.format(date);

	        String paramFileName = expt.getLabel() + "_" + arcPipeline.getName() + "_params_" + dateSuffix + ".xml";
	        String paramFilePath = saveParameters(buildDir + File.separator + expt.getLabel(),paramFileName,parameters);
	        xnatPipelineLauncher.setParameterFile(paramFilePath);
	        // return xnatPipelineLauncher.launch();
	        return xnatPipelineLauncher.launch();
	    }

		private boolean launch(ArcPipelinedataI arcPipeline, final UserI user) throws Exception {
			XnatPipelineLauncher xnatPipelineLauncher = new XnatPipelineLauncher(user);
			xnatPipelineLauncher.setSupressNotification(true);
	        xnatPipelineLauncher.setParameter("useremail", user.getEmail());
		    xnatPipelineLauncher.setParameter("userfullname", XnatPipelineLauncher.getUserName(user));
		    xnatPipelineLauncher.setParameter("adminemail", XDAT.getSiteConfigPreferences().getAdminEmail());
		    xnatPipelineLauncher.setParameter("mailhost", XDAT.getNotificationsPreferences().getSmtpServer().getHostname());
		    xnatPipelineLauncher.setParameter("xnatserver", TurbineUtils.GetSystemName());


		    xnatPipelineLauncher.setPipelineName(arcPipeline.getLocation());
			xnatPipelineLauncher.setId(expt.getId());
			xnatPipelineLauncher.setLabel(expt.getLabel());
			xnatPipelineLauncher.setExternalId(expt.getProject());
			xnatPipelineLauncher.setDataType(expt.getXSIType());

	    	Date date = new Date();
	    	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		    String dateSuffix = formatter.format(date);

			String buildDir = PipelineFileUtils.getBuildDir(expt.getProject(), true);
			buildDir +=   "archive_trigger" + File.separator +  dateSuffix    ;
			xnatPipelineLauncher.setBuildDir(buildDir);
			xnatPipelineLauncher.setNeedsBuildDir(false);

			Parameters parameters = Parameters.Factory.newInstance();
			ParameterData param = parameters.addNewParameter();
	    	param.setName("xnat_id");
	    	param.addNewValues().setUnique(expt.getId());

	    	if (expt instanceof XnatImagesessiondata) {
	    		String path = ((XnatImagesessiondata)expt).getArchivePath();
	    		if (path.endsWith(File.separator)) path = path.substring(0, path.length()-1);
		   		param = parameters.addNewParameter();
		    	param.setName("archivedir");
		    	param.addNewValues().setUnique(path);
	    	}


	    	param = parameters.addNewParameter();
	    	param.setName("sessionId");
	    	param.addNewValues().setUnique(expt.getLabel());

	    	param = parameters.addNewParameter();
	    	param.setName("project");
	    	param.addNewValues().setUnique(expt.getProject());

	    	XFTItem itemOfExpectedXsiType = expt.getItem();

			List<ArcPipelineparameterdataI> pipelineParameters = arcPipeline.getParameters_parameter();
	        for (ArcPipelineparameterdataI pipelineParam : pipelineParameters) {

	            String schemaLink = pipelineParam.getSchemalink();
	            String paramCsv = pipelineParam.getCsvvalues();
	            if (schemaLink == null && paramCsv == null) {
	                // param has no default value, and we were not given a value, so skip it
	                continue;
	            }

	            param = parameters.addNewParameter();
	            param.setName(pipelineParam.getName());
	            Values values = param.addNewValues();

	    		if (schemaLink != null) {
	    			Object o = itemOfExpectedXsiType.getProperty(schemaLink, true);
	    			if (o != null ) {
		    			try {
	                        ArrayList<? extends Class> matches = (ArrayList<? extends Class>) o;
		        				if (matches.size() == 1) {
			        		    	values.setUnique(""+matches.get(0));
			        			}else {
	                            for (Object match : matches) {
	                                values.addList(""+match);
			        			}
		        			}
		    			}catch(ClassCastException  cce) {
	        		    	values.setUnique(""+o);
		    			}
	    			}
	    		}else {
	                String[] paramArray = paramCsv.split(",");
	                if (paramArray.length == 1) {
	                    values.setUnique(paramArray[0]);
	                } else {
	                    values.setListArray(paramArray);
		    			}
	    		}
	    	}
			String paramFileName = expt.getLabel() + "_" + arcPipeline.getName() + "_params_" + dateSuffix + ".xml";
			String paramFilePath = saveParameters(buildDir+File.separator + expt.getLabel(),paramFileName,parameters);
		    xnatPipelineLauncher.setParameterFile(paramFilePath);
		    return xnatPipelineLauncher.launch();
		}

		protected String saveParameters(String rootpath, String fileName, Parameters parameters) throws Exception{
	        File dir = new File(rootpath);
	        if (!dir.exists()) dir.mkdirs();
	        File paramFile = new File(rootpath + File.separator + fileName);
	        ParametersDocument paramDoc = ParametersDocument.Factory.newInstance();
	        paramDoc.addNewParameters().set(parameters);
	        paramDoc.save(paramFile,new XmlOptions().setSavePrettyPrint().setSaveAggressiveNamespaces());
	        return paramFile.getAbsolutePath();
	    }
		

}
