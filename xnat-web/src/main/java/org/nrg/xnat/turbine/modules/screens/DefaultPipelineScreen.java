/*
 * web: org.nrg.xnat.turbine.modules.screens.DefaultPipelineScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.config.entities.Configuration;
import org.nrg.framework.constants.Scope;
import org.nrg.pipeline.PipelineRepositoryManager;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ParameterData.Values;
import org.nrg.pipeline.xmlbeans.ParametersDocument.Parameters;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.ArcPipelinedataI;
import org.nrg.xdat.model.ArcPipelineparameterdataI;
import org.nrg.xdat.model.ArcProjectDescendantPipelineI;
import org.nrg.xdat.model.PipePipelinedetailsParameterI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseWrkWorkflowdata;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.PipelineNotFoundException;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public abstract class DefaultPipelineScreen extends SecureReport {

    static Logger logger = Logger.getLogger(DefaultPipelineScreen.class);
    String message = null;
    ArrayList<WrkWorkflowdata> workflows;
    String project = null;

    protected HashMap<String, ArcPipelineparameterdata> projectParameters;
    String pipelinePath = null;

    public DefaultPipelineScreen() {
        workflows = Lists.newArrayList();
        projectParameters = Maps.newHashMap();
    }

    public abstract void finalProcessing(RunData data, Context context);

    protected void setHasDicomFiles(XnatImagesessiondata mr, String mprageScanType, Context context) {
        if (mr instanceof XnatMrsessiondata || mr instanceof XnatPetsessiondata) {
            boolean rtn = false;
            for (String type : mprageScanType.split(",")) {
                ArrayList<XnatImagescandata> scans = mr.getScansByType(type.trim());
                if (scans != null && scans.size() > 0) {
                    List<XnatAbstractresource> files = scans.get(0).getFile();
                    for (XnatAbstractresource absFile : files) {
                        if (absFile instanceof XnatDicomseries) {
                            rtn = true;
                        } else if (absFile instanceof XnatResourcecatalog) {
                            XnatResourcecatalog rsccat = (XnatResourcecatalog) absFile;
                            if (rsccat.getContent().endsWith("RAW")) {
                                if (rsccat.getFormat().equals("DICOM"))
                                    rtn = true;
                                break;
                            }
                        }

                    }
                }
            }
            context.put("isDicom", rtn ? "1" : "0");
        }
    }

    protected void setHasFreesurfer(XnatMrsessiondata mr, Context context) {
        String project = mr.getProject();
        int hasFreesurfer = 0;
        ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(project);
        final List<ArcProjectDescendantPipelineI> descPipelines = arcProject.getPipelinesForDescendant(XnatMrsessiondata.SCHEMA_ELEMENT_NAME);
        for (ArcProjectDescendantPipelineI aPipeline : descPipelines) {
            if (aPipeline.getLocation().endsWith(File.separator + "StdFreeSurferBuild.xml")) {
                hasFreesurfer = 1;
                break;
            }
        }
        context.put("freesurfer", hasFreesurfer);

    }


    protected void setWorkflows(RunData data, Context context) {
        String projectId = (String) context.get("project");
        try {
            org.nrg.xft.search.CriteriaCollection cc = new CriteriaCollection("AND");
            cc.addClause("wrk:workflowData.ID", item.getProperty("ID"));
            if (projectId != null) cc.addClause("wrk:workflowData.ExternalID", projectId);
            ItemCollection items = ItemSearch.GetItems(cc, TurbineUtils.getUser(data), false);
            //Sort by Launch Time
            ArrayList workitems = items.getItems("wrk:workflowData.launch_time", "DESC");
            for (Object item : workitems) {
                workflows.add(new WrkWorkflowdata((XFTItem) item));
            }
            context.put("workflows", workflows);
        } catch (Exception e) {
            logger.debug(e);
        }
    }

    public void preProcessing(RunData data, Context context) {
    }

    public void doBuildTemplate(RunData data, Context context) {
        // preserveVariables(data,context);
        logger.debug("BEGIN SECURE REPORT :" + this.getClass().getName());
        preProcessing(data, context);
        item = TurbineUtils.getDataItem(data);
        if (item == null) {
            try {
                item = TurbineUtils.GetItemBySearch(data, preLoad());
            } catch (IllegalAccessException e1) {
                logger.error(e1);
                data.setMessage(e1.getMessage());
                noItemError(data, context);
                return;
            } catch (Exception e1) {
                logger.error(e1);
                data.setMessage(e1.getMessage());
                data.setScreenTemplate("Error.vm");
                noItemError(data, context);
                return;
            }
        }
        if (item == null) {
            data.setMessage("Error: No item found.");
            noItemError(data, context);
        } else {
            try {
                if (XFT.VERBOSE) System.out.println("Creating report: " + getClass());
                context.put("item", item.getItem());
                if (XFT.VERBOSE)
                    System.out.println("Loaded item object (org.nrg.xft.ItemI) as context parameter 'item'.");
                context.put("element", SchemaElement.GetElement(item.getXSIType()));
                context.put("search_element", TurbineUtils.GetPassedParameter("search_element", data));
                context.put("search_field", TurbineUtils.GetPassedParameter("search_field", data));
                context.put("search_value", TurbineUtils.GetPassedParameter("search_value", data));
                project = (String) TurbineUtils.GetPassedParameter("project", data);
                pipelinePath = (String) context.get("pipelinePath");

                context.put("project", project);

                om = BaseElement.GetGeneratedItem(item);

                context.put("om", om);
                setWorkflows(data, context);
                setParameters(pipelinePath);
                if (message != null) data.setMessage(message);
                finalProcessing(data, context);
            } catch (Exception e) {
                data.setMessage(e.getMessage());
                logger.error("", e);
            }
        }
        logger.debug("END SECURE REPORT :" + this.getClass().getName());
    }


    protected boolean isAnyQueuedOrRunning(ArrayList<WrkWorkflowdata> workflows) {
        boolean rtn = false;
        try {
            for (WrkWorkflowdata wrkFlow : workflows) {
                if (wrkFlow.getStatus().toUpperCase().equals(BaseWrkWorkflowdata.QUEUED) || wrkFlow.getStatus().toUpperCase().equals(BaseWrkWorkflowdata.RUNNING)) {
                    rtn = true;
                    break;
                }
            }
        } catch (IndexOutOfBoundsException aoe) {
            logger.debug(aoe);
        }
        return rtn;
    }

    protected boolean hasBeenCompletedInThePast(String pipelinePath, ArrayList<WrkWorkflowdata> workflows) {
        boolean rtn = false;
        for (WrkWorkflowdata wrkFlow : workflows) {
            String matchPipelineName = wrkFlow.getPipelineName();
            if (matchPipelineName.equals(pipelinePath) || pipelinePath.contains(matchPipelineName)) {
                if (wrkFlow.getStatus().equalsIgnoreCase(BaseWrkWorkflowdata.COMPLETE)) {
                    rtn = true;
                    message = "This pipeline has been completed in the past. Relaunching the pipeline may result in loss of data. Are you sure you want to proceed?";
                    break;
                }
            }
        }
        return rtn;
    }

    protected ArcPipelineparameterdata getProjectPipelineSetting(String parameterName) throws Exception {
        return projectParameters.get(parameterName);
    }

    protected ArcPipelineparameterdata getParameter(ArcProject arcProject, String parameterName) throws PipelineNotFoundException {
        ArcPipelineparameterdata rtn = null;
        ArcPipelinedataI pipelineData = arcProject.getPipelineByPath(pipelinePath);
        for (ArcPipelineparameterdataI aParam : pipelineData.getParameters_parameter()) {
            if (aParam.getName().equals(parameterName)) {
                rtn = (ArcPipelineparameterdata) aParam;
                break;
            }
        }
        return rtn;
    }

    protected void setParameters(String pipeline) throws PipelineNotFoundException {
        ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(project);
        ArcPipelinedataI pipelineData;
        if (arcProject == null) { //Project pipeline hasnt been set
            PipePipelinedetails pipelineDetails = PipelineRepositoryManager.GetInstance().getPipeline(pipeline);
            for (PipePipelinedetailsParameterI paramI : pipelineDetails.getParameters_parameter()) {
                ArcPipelineparameterdata aParam = PipelineRepositoryManager.GetInstance().convertToArcPipelineParameter((PipePipelinedetailsParameter) paramI);
                projectParameters.put(aParam.getName(), aParam);
            }
        } else {
            if (om.getXSIType().equals(XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
                pipelineData = arcProject.getPipelineByPath(pipeline);
            } else {
                pipelineData = arcProject.getPipelineForDescendantByPath(om.getXSIType(), pipeline);
            }

            for (ArcPipelineparameterdataI aParamI : pipelineData.getParameters_parameter()) {
                projectParameters.put(aParamI.getName(), (ArcPipelineparameterdata) aParamI);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void setParameters(ArcPipelinedataI arcPipeline, Context context) throws Exception {
        List<ArcPipelineparameterdataI> pipelineParameters = arcPipeline.getParameters_parameter();

        Parameters parameters = Parameters.Factory.newInstance();
        ParameterData param;

        for (ArcPipelineparameterdataI pipelineParam : pipelineParameters) {
            String schemaLink = pipelineParam.getSchemalink();
            String paramCsv = pipelineParam.getCsvvalues();
            if (schemaLink == null && paramCsv == null) {
                // param has no default value
                // Make default value empty string
                paramCsv = "";
            }

            param = parameters.addNewParameter();
            param.setName(pipelineParam.getName());
            Values values = param.addNewValues();

            if (schemaLink != null) {
                Object o = om.getItem().getProperty(schemaLink, true);
                if (o != null) {
                    try {
                        ArrayList<? extends Class> matches = (ArrayList<? extends Class>) o;
                        if (matches.size() == 1) {
                            values.setUnique("" + matches.get(0));
                        } else {
                            for (Object match : matches) {
                                values.addList("" + match);
                            }
                        }
                    } catch (ClassCastException cce) {
                        values.setUnique("" + o);
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
        context.put("parameters", parameters);
    }

    public ArrayList<XnatImagescandata> getScansFromScantypeConfig(String toolName, String fileName) {

        Configuration config = XDAT.getConfigService().getConfig(toolName, fileName, Scope.Project, project);
        if (config != null && config.getContents() != null) {
            return ((XnatImagesessiondata) om).getScansByTypeCsv(config.getContents().trim());
        } else {
            return Lists.newArrayList();
        }
    }

    protected List<XnatImagesessiondata> getPreviousImagingSessions(final XnatImagesessiondata latestImagingSession) {
        if (latestImagingSession == null) {
            return null;
        }

        final String subjectId = latestImagingSession.getSubjectId();
        if (StringUtils.isBlank(subjectId)) {
            logger.error("Image session " + latestImagingSession.getId() + " has no subject identifier.");
            return null;
        }
        final String projectId = latestImagingSession.getSubjectId();
        if (StringUtils.isBlank(projectId)) {
            logger.error("Image session " + latestImagingSession.getId() + " has no project identifier.");
            return null;
        }
        final UserI userI = XDAT.getUserDetails();
        if (userI == null) {
            logger.error("Bad user object returned from XDAT.");
            return null;
        }
        final String login = userI.getLogin();

        final String query = "SELECT im.id FROM xnat_imagesessiondata im LEFT JOIN xnat_subjectAssessorData sad ON im.ID=sad.ID LEFT JOIN xnat_experimentData ed ON sad.ID=ed.ID WHERE subject_id='" + subjectId +"' and ed.project='" + projectId + "' and date < '" +  latestImagingSession.getDate() +"' ORDER BY date DESC ";

        final List<XnatImagesessiondata> previousSessions = Lists.newArrayList();
        XFTTable table;
        try {
            table = TableSearch.Execute(query, latestImagingSession.getDBName(), login);
        } catch (Exception e) {
            logger.error("There was a problem searching for previous imaging sessions.", e);
            return null;
        }

        if (table != null && table.size() > 0)            {
            table.resetRowCursor();

            while(table.hasMoreRows())    {
                final Object imageSessionId = table.nextRowHash().get("id");
                if (imageSessionId != null)      {
                    ItemI itemI = null;
                    try {
                        itemI = ItemSearch.GetItem("xnat:imageSessionData.ID", imageSessionId, userI, false);
                    } catch (Exception e) {
                        logger.error("Could not find image session " + imageSessionId, e);
                    }
                    if (itemI != null) {
                        previousSessions.add(new XnatImagesessiondata(itemI));
                    }
                }
            }
        }
        return previousSessions;
    }
}
