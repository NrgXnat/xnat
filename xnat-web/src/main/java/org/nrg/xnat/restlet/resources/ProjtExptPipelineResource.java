/*
 * web: org.nrg.xnat.restlet.resources.ProjtExptPipelineResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.pipeline.XnatPipelineLauncher;
import org.nrg.pipeline.utils.PipelineFileUtils;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ParameterData.Values;
import org.nrg.pipeline.xmlbeans.ParametersDocument;
import org.nrg.pipeline.xmlbeans.ParametersDocument.Parameters;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.ArcPipelinedataI;
import org.nrg.xdat.model.ArcPipelineparameterdataI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.ValidationException;
import org.nrg.xnat.restlet.actions.FixScanTypes;
import org.nrg.xnat.restlet.actions.PullSessionDataFromHeaders;
import org.nrg.xnat.restlet.actions.TriggerPipelines;
import org.nrg.xnat.restlet.util.XNATRestConstants;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.xml.sax.SAXException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProjtExptPipelineResource extends SecureResource {
	XnatProjectdata proj=null;
	XnatExperimentdata expt=null;
    String step = null;

	public ProjtExptPipelineResource(Context context, Request request, Response response) {
		super(context, request, response);

		String pID = (String) getParameter(request,"PROJECT_ID");
		if (pID != null) {
			final UserI user = getUser();

			proj = XnatProjectdata.getXnatProjectdatasById(pID, user, false);
			step = (String) getParameter(request,"STEP_ID");
			if (step != null) {
				String exptID = (String) getParameter(request,"EXPT_ID");
				if (exptID != null) {
					expt = XnatExperimentdata.getXnatExperimentdatasById(
							exptID, user, false);

					if (expt == null) {
						expt = XnatExperimentdata.GetExptByProjectIdentifier(
								proj.getId(), exptID, user, false);
					}
				}
				this.getVariants().add(new Variant(MediaType.TEXT_XML));

			} else {
				response.setStatus(Status.CLIENT_ERROR_GONE);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_GONE);
		}
	}


    public Representation represent(Variant variant) {
		if(proj!=null && step!=null){
			ArcPipelinedata arcPipeline = null;
			ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(proj.getId());
			//arcProject.setItem(arcProject.getCurrentDBVersion());
			try {
				if (expt == null) { // Look for Project level pipeline
					arcPipeline = (ArcPipelinedata)arcProject.getPipeline(step);
				}else { //Look for experiment level pipeline
					arcPipeline = (ArcPipelinedata)arcProject.getPipelineForDescendant(expt.getXSIType(), step);
				}
				MediaType mt = overrideVariant(variant);
				if (mt.equals(MediaType.TEXT_XML)) {
					return representItem(arcPipeline.getItem(), mt, null,false, true);
				}else {
					return null;
				}
			}catch(Exception e) {
				e.printStackTrace();
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		}else {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		return null;
	}

	@Override
	public boolean allowPost() {
		return true;
	}


	@Override
	public void handlePost() {
		if(proj!=null && step!=null && expt != null){
			try {
				final UserI user = getUser();
				if(step.equals(XNATRestConstants.TRIGGER_PIPELINES)){
					if(Permissions.canEdit(user, expt)){

						PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, expt.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TRIGGER_PIPELINES));
						EventMetaI c=wrk.buildEvent();

						try {
							FixScanTypes fst=new FixScanTypes(expt, user, proj, true, c);
							fst.call();

							TriggerPipelines tp = new TriggerPipelines(expt, this.isQueryVariableTrue(XNATRestConstants.SUPRESS_EMAIL), user);
							tp.call();
							PersistentWorkflowUtils.complete(wrk,c);
						} catch (Exception e) {
							WorkflowUtils.fail(wrk, c);
							throw e;
						}
					}
				}else if(step.equals(XNATRestConstants.PULL_DATA_FROM_HEADERS) && expt instanceof XnatImagesessiondata){
					if(Permissions.canEdit(user, expt)){
						try {
							PersistentWorkflowI wrk=PersistentWorkflowUtils.buildOpenWorkflow(user, expt.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.DICOM_PULL));
							EventMetaI c=wrk.buildEvent();
							try {
								PullSessionDataFromHeaders pull=new PullSessionDataFromHeaders((XnatImagesessiondata)expt, user, this.isQueryVariableTrue("allowDataDeletion"), this.isQueryVariableTrue("overwrite"), false, c);
								pull.call();
								WorkflowUtils.complete(wrk, c);
							} catch (Exception e) {
								WorkflowUtils.fail(wrk, c);
								throw e;
							}
						} catch (SAXException e){
							logger.error("",e);
							this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,e.getMessage());
						} catch (ValidationException e){
							logger.error("",e);
							this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,e.getMessage());
						} catch (Exception e) {
							logger.error("",e);
							this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
							return;
						}
					}else{
						getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
					}
				}else if(step.equals(XNATRestConstants.FIX_SCAN_TYPES) && expt instanceof XnatImagesessiondata){
					if(Permissions.canEdit(user, expt)){

						PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, expt.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TRIGGER_PIPELINES));
						EventMetaI c=wrk.buildEvent();
						PersistentWorkflowUtils.save(wrk,c);

						try {
							FixScanTypes fst=new FixScanTypes(expt, user, proj, true, c);
							fst.call();
							WorkflowUtils.complete(wrk, c);
						} catch (Exception e) {
							WorkflowUtils.fail(wrk, c);
							throw e;
						}
					}else{
						getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
					}
				}else{
					ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(proj.getId());
					//arcProject.setItem(arcProject.getCurrentDBVersion());

                    // Grab any parameters that may have been passed in...
                    // as form params
                    Map<String,String> bodyParams = getBodyVariableMap();

                    // as query string params
                    Map<String,String> queryParams = getQueryVariableMap();

                    // as an XML file
                    Map<String,String> xmlDocumentParams = new HashMap<String,String>();
                    String XMLbody = getRequest().getEntity().getText();
                    if (StringUtils.isNotBlank(XMLbody)) {
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
							boolean success = launch(arcPipeline);
                            } else {
                                launch(arcPipeline,pipelineParams);
                            }
						}
					}catch(Exception e) {
						e.printStackTrace();
                        logger.error("Pipeline step " + step + " for project " + proj.getId() + " does not exist");
						//getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
					}
				}
			} catch (Exception e) {
				logger.error(e);
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			}
		}else {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

    private void launch(ArcPipelinedataI arcPipeline, Map<String,String> paramsMap) throws Exception {
		final UserI user = getUser();

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
        xnatPipelineLauncher.launch();
    }

	private boolean launch(ArcPipelinedataI arcPipeline) throws Exception {
		final UserI user = getUser();

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
