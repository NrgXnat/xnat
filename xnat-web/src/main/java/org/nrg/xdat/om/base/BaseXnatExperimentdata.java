/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatExperimentdata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.nrg.action.ClientException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.WrkWorkflowdataI;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatExperimentdataFieldI;
import org.nrg.xdat.model.XnatExperimentdataShareI;
import org.nrg.xdat.model.XnatFielddefinitiongroupI;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatAbstractprotocol;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatDatatypeprotocol;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatExperimentdataShare;
import org.nrg.xdat.om.XnatFielddefinitiongroup;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatPvisitdata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.om.XnatSubjectassessordata;
import org.nrg.xdat.om.base.auto.AutoXnatExperimentdata;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.SecurityValues;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.shared.OmUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.identifier.IDGeneratorFactory;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.archive.CurrentArcIdentifier;
import org.nrg.xnat.archive.CurrentArcIdentifierI;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.data.Status;

import javax.annotation.Nullable;
import java.io.File;
import java.io.Serial;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatExperimentdata extends AutoXnatExperimentdata implements ArchivableItem, MoveableI {

    @Serial
    private static final long serialVersionUID = -1237275273363081417L;
  public static final int ONE_MINUTE = 60000;

  public BaseXnatExperimentdata(ItemI item)
	{
		super(item);
	}

	public BaseXnatExperimentdata(UserI user)
	{
		super(user);
	}

	public BaseXnatExperimentdata()
	{}

	public BaseXnatExperimentdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    public String getArchiveDirectoryName(){
        return StringUtils.defaultIfBlank(getLabel(), getId());
    }

    @SuppressWarnings("unused")
    public String getFreeFormDate(String dateParam){
		try{
			Date now = Calendar.getInstance().getTime();
			DateFormat dateFormat = new SimpleDateFormat(dateParam);
            return dateFormat.format(now);
		} catch (Exception e1) {logger.error(e1);return null;}
	}

    Hashtable fieldsByName = null;
    public Hashtable getFieldsByName(){
        if (fieldsByName == null){
            fieldsByName=new Hashtable();
            for(final XnatExperimentdataFieldI field: this.getFields_field()){
                fieldsByName.put(field.getName(), field);
            }
        }

        return fieldsByName;
    }

    @SuppressWarnings("unused")
    public Object getFieldByName(final String s){
        XnatExperimentdataFieldI field = (XnatExperimentdataFieldI)getFieldsByName().get(s);
        if (field!=null){
            return field.getField();
        }else{
            return null;
        }
    }

    public String getIdentifier(String project){
        return getIdentifier(project,false);
    }

    public String getIdentifier(final String project,final boolean returnNULL){
        if (project!=null){
        	if (this.getProject().equals(project)){
        		if (this.getLabel()!=null){
        			return this.getLabel();
        		}
        	}

        	for (final XnatExperimentdataShareI pp : this.getSharing_share())
            {
            	if (pp.getProject().equals(project))
                {
                    if (pp.getLabel()!=null){
                        return pp.getLabel();
                    }
                }
            }
        }

        if (returnNULL){
            return null;
        }else{
            return getId();
        }
    }

    public XnatProjectdataI getProject(final String projectID, final boolean preLoad)
    {
        XnatExperimentdataShare ep = null;
        for (final XnatExperimentdataShareI pp : this.getSharing_share())
        {
        	if (pp.getProject().equals(projectID))
            {
                ep=(XnatExperimentdataShare)pp;
                break;
            }
        }

        try {
            if (ep!=null){
                return XnatProjectdata.getXnatProjectdatasById(ep.getProject(), this.getUser(), preLoad);
            }else if (this.getProject().equals(projectID)){
                return XnatProjectdata.getXnatProjectdatasById(this.getProject(), this.getUser(), preLoad);
            }
        } catch (RuntimeException e) {
            logger.error("",e);
        }

        return null;
    }

    public XnatProjectdata getPrimaryProject(boolean preLoad){
        if (this.getProject()!=null){
            return XnatProjectdata.getXnatProjectdatasById(getProject(), this.getUser(), preLoad);
        }else{
            return (XnatProjectdata)getFirstProject();
        }
    }


    public XnatProjectdataI getFirstProject() {
        List<XnatExperimentdataShareI> shares = getSharing_share();
        if (!shares.isEmpty()){
            for (XnatExperimentdataShareI ep : shares) {
                if (ep != null) {
        try {
                        // We'll return the first non-null project, i.e. the first shared project this user can access.
                        XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(ep.getProject(), this.getUser(), false);
                        if (project != null) {
                            return project;
            }
        } catch (RuntimeException e) {
            logger.error("",e);
        }
                }
            }
        }

        return null;
    }



    public String getIdentifiers(){
        Hashtable ids = new Hashtable();

        if (this.getProject()!=null){
        	if (this.getLabel()!=null){
        		ids.put(this.getLabel(), this.getProject());
        	}else{
        		ids.put(this.getId(), this.getProject());
        	}
        }

        for (final XnatExperimentdataShareI pp : this.getSharing_share())
        {

            if (pp.getLabel()!=null){
                if (ids.containsKey(pp.getLabel()))
                {
                    ids.put(pp.getLabel(), ids.get(pp.getLabel()) + "," + pp.getProject());
                }else{
                    ids.put(pp.getLabel(), pp.getProject());
                }
            }else{
                if (ids.containsKey(this.getId()))
                {
                    ids.put(this.getId(), ids.get(this.getId()) + "," + pp.getProject());
                }else{
                    ids.put(this.getId(), pp.getProject());
                }
            }
        }

        StringBuilder identifiers = new StringBuilder();

        Enumeration keys = ids.keys();
        int counter=0;
        while (keys.hasMoreElements()){
            String key =(String) keys.nextElement();
            if (counter++>0) identifiers.append(", ");
            identifiers.append(key).append(" (").append(ids.get(key)).append(")");
        }

        return identifiers.toString();
    }




    public String name = null;
    public String description = null;
    public String secondaryID = null;
    private boolean initd = false;

    public void loadProjectDetails(){
        if (!initd)
        {
            initd=true;
            Object[] row=this.loadProjectDetails(this.getProject());
            if (row!=null)
            {
                name = (String)row[0];
                description = (String)row[1];
                secondaryID = (String)row[2];
            }
        }
    }

    public Object[] loadProjectDetails(String s){
    	try{
	    	XFTTable table = XFTTable.Execute("SELECT name,description,secondary_ID FROM xnat_projectData WHERE ID ='" + s + "';", this.getDBName(), null);

	        if (table.size()>0)
	        {
	            return table.rows().getFirst();
	        }
    	} catch (SQLException | DBPoolException e) {
            logger.error("",e);
        }

        return null;
    }

    public XnatProjectdata getProjectData(){
        return XnatProjectdata.getXnatProjectdatasById(this.getProject(), this.getUser(), false);
    }


    /**
     * @return the description
     */
    public String getDescription() {
        loadProjectDetails();
        return description;
    }

    /**
     * @return the name
     */
    public String getProjectName() {
        loadProjectDetails();
        return name;
    }

    /**
     * @return the secondaryID
     */
    public String getProjectSecondaryID() {
        loadProjectDetails();
        return secondaryID;
    }



    /**
     * @return the secondaryID
     */
    public String getProjectDisplayID() {
        loadProjectDetails();
        if (secondaryID!=null){
            return secondaryID;
        }else{
           return getProject();
        }
    }

    public Hashtable<XnatProjectdataI,String> getProjectDatas(){
        Hashtable<XnatProjectdataI,String> hash = new Hashtable<>();
        for(final XnatExperimentdataShareI pp : this.getSharing_share()){
            if (pp.getLabel()==null)
            	if (this.getId()!=null)
            		hash.put(((XnatExperimentdataShare)pp).getProjectData(), this.getId());
            	else
            		hash.put(((XnatExperimentdataShare)pp).getProjectData(), "");
            else
                hash.put(((XnatExperimentdataShare)pp).getProjectData(), pp.getLabel());
        }
        return hash;
    }

    @SuppressWarnings("unused")
    public Collection<XnatFielddefinitiongroup> getFieldDefinitionGroups(final String dataType, final String projectID) {
        final Hashtable<String, XnatFielddefinitiongroup> groups = new Hashtable<>();
        final Hashtable<XnatProjectdataI, String> projects = getProjectDatas();
        final XnatProjectdata primaryProject = getPrimaryProject(false);
        if (primaryProject != null) {
            projects.put(primaryProject, "");

            for (final Map.Entry<XnatProjectdataI, String> entry : projects.entrySet()) {
                final XnatAbstractprotocol prot = ((XnatProjectdata) entry.getKey()).getProtocolByDataType(dataType);
                if ((projectID != null && !projectID.isEmpty()) && !projectID.equals(entry.getKey().getId())) {
                    continue;
                }
                if (prot != null && prot instanceof XnatDatatypeprotocol dataProt) {
                    for (final XnatFielddefinitiongroupI group : dataProt.getDefinitions_definition()) {
                        groups.put(group.getId(), (XnatFielddefinitiongroup) group);
                    }
                }
            }
        }
        return groups.values();
    }

    public static XnatExperimentdata GetExptByProjectIdentifier(String project, String identifier,UserI user,boolean preLoad){
        if(StringUtils.isBlank(identifier)){
            return null;
        }

        final CriteriaCollection subcc1 = new CriteriaCollection("AND");
        subcc1.addClause("xnat:experimentData/project", project);
        subcc1.addClause("xnat:experimentData/label", identifier);

        ArrayList al =  XnatExperimentdata.getXnatExperimentdatasByField(subcc1, user, preLoad);
        if (al.size()>0){
            al = BaseElement.WrapItems(al);
            return (XnatExperimentdata)al.getFirst();
        }


        final CriteriaCollection subcc2 = new CriteriaCollection("AND");
        subcc2.addClause("xnat:experimentData/sharing/share/project", project);
        subcc2.addClause("xnat:experimentData/sharing/share/label", identifier);

        al =  XnatExperimentdata.getXnatExperimentdatasByField(subcc2, user, preLoad);
        if (al.size()>0){
            al = BaseElement.WrapItems(al);
            return (XnatExperimentdata)al.getFirst();
        }else{
            return null;
        }
    }

    public static synchronized String CreateNewID() throws Exception{
        return IDGeneratorFactory.getInstance().getIDGenerator("xnat_experimentData").generateIdentifier();
    }

    /**
     * newlabel can be null defaults to this.getLabel(), if that is null this.getId()
     * @param newProject    New project to move.
     * @param newLabel      New label to set.
     * @param user          User moving.
     * @param ci            Event.
     * @param assessors     Assessors to move.
     * @throws Exception
     */
    public void moveToProject(XnatProjectdata newProject, String newLabel, UserI user, EventMetaI ci,
                              List<String> assessors) throws Exception {
        String destinationProject = newProject.getId();
        if (this.getProject().equals(destinationProject)) {
            return;
        }

        if (!MoverMaker.check(this, user)) {
            throw new InvalidPermissionException(this.getXSIType());
        }
        String existingRootPath = this.getProjectData().getRootArchivePath();

        if (newLabel == null) newLabel = this.getLabel();
        if (newLabel == null) newLabel = this.getId();

        // newSessionDir = /ARCHIVE/proj_x/arc001
        final File newSessionDir = new File(new File(newProject.getRootArchivePath(), newProject.getCurrentArc()), newLabel);

        // Label defaults to this.getId()
        String current_label = this.getLabel();
        if (current_label == null) current_label = this.getId();

        for (XnatAbstractresourceI abstRes : this.getResources_resource()) {
            final MoverMaker.Mover mover = MoverMaker.moveResource(abstRes, current_label, this, newSessionDir,
                    existingRootPath, destinationProject, user, ci);
            mover.setResource((XnatAbstractresource) abstRes);
            mover.call();
        }

        MoverMaker.writeDB(this, newProject, newLabel, user, ci);
        MoverMaker.setLocal(this, newProject, newLabel);
    }

    @SuppressWarnings("unused")
    public ArrayList getCatalogSummary() throws Exception{
		String query="SELECT xnat_abstractresource_id,label,element_name ";
    	query+=", 'resources'::TEXT AS category, '" + this.getId()+"'::TEXT AS cat_id";
		query+=" FROM xnat_experimentdata_resource map " +
		" LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id" +
		" LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id";
		query+= " WHERE xnat_experimentdata_id='"+this.getId() + "'";

		XFTTable t = XFTTable.Execute(query, this.getDBName(), "system");

		return t.rowHashs();
    }

    public boolean hasProject(String proj_id){
    if (this.getProject() == null) {
    	return false;
    }
	if(this.getProject().equals(proj_id)){
	    return true;
	}else{
	    for(XnatExperimentdataShareI pp: this.getSharing_share()){
		if(pp.getProject().equals(proj_id)){
		    return true;
		}
	    }
	}

	return false;
    }



    public String canDelete(BaseXnatProjectdata proj, UserI user) {
    	BaseXnatExperimentdata expt=this;
    	if(this.getItem().getUser()!=null){
    		expt=new XnatExperimentdata(this.getCurrentDBVersion(true));
    	}
    	if(!expt.hasProject(proj.getId())){
    		return null;
    	}else {

			try {
				SecurityValues values = new SecurityValues(); 
				values.put(this.getXSIType() + "/project", proj.getId());
				SchemaElement se= SchemaElement.GetElement(this.getXSIType());

				if (!Permissions.canDelete(user,se,values))
				{
					return "User cannot delete experiments for project " + proj.getId();
				}
			} catch (Exception e1) {
				return "Unable to delete subject.";
			}

    	}
    	return null;
    }

    public String delete(final BaseXnatProjectdata proj, final UserI user, final boolean removeFiles, final EventMetaI c){
    	BaseXnatExperimentdata expt=this;
    	if(this.getItem().getUser()!=null){
    		expt= (BaseXnatExperimentdata)BaseElement.GetGeneratedItem(expt.getItem());
    	}

    	String msg=expt.canDelete(proj,user);

    	if(msg!=null){
    		logger.error(msg);
    		return msg;
    	}

    	if(expt.getProject() != null && !expt.getProject().equals(proj.getId())){
			try {
				SecurityValues values = new SecurityValues();
				values.put(this.getXSIType() + "/project", proj.getId());

				if (!Permissions.canDelete(user,expt) && !Permissions.canDelete(user,this.getSchemaElement(),values))
				{
					return "User cannot delete experiments for project " + proj.getId();
				}


				//unshare children before unsharing parent
				if(expt instanceof XnatImagesessiondata imagesessiondata){
					final  List<XnatImageassessordata> expts = imagesessiondata.getAssessors_assessor();
			        for (XnatImageassessordataI exptI : expts){
			        	final XnatImageassessordata assess = (XnatImageassessordata)exptI;
			        	if(assess.getProject().equals(proj.getId())){
			        		return "This operation would delete an experiment (rather than un-share).  Please move experiment ("+expt.getId()+") to another project or manually delete.";

			        	}
			            msg= assess.delete(proj,user,false,c);
			            if(msg!=null){
			            	return msg;
			            }
			        }
				}

				int index = 0;
				int match = -1;
				for(XnatExperimentdataShareI pp : expt.getSharing_share()){
					if(pp.getProject().equals(proj.getId())){
						SaveItemHelper.authorizedRemoveChild(expt.getItem(), "xnat:experimentData/sharing/share", ((XnatExperimentdataShare)pp).getItem(), user,c);
						match=index;
						break;
					}
					index++;
				}

				if(match==-1)return null;

				this.removeSharing_share(match);
		        return null;
			} catch (SQLException e) {
				logger.error("",e);
				return e.getMessage();
			} catch (Exception e) {
				logger.error("",e);
				return e.getMessage();
			}
		}else{

	    	if(XDAT.getBoolSiteConfigurationProperty("security.prevent-data-deletion", false)){
	    		return "User account cannot delete experiments";
	    	}
			try {

				if(!Permissions.canDelete(user,this)){
					return "User account doesn't have permission to delete this experiment.";
				}

				if(removeFiles){
					this.deleteFiles(user,c);
				}


				SaveItemHelper.authorizedDelete(expt.getItem().getCurrentDBVersion(), user,c);
				Users.clearCache(user);

			} catch (SQLException e) {
				logger.error("",e);
				return StringUtils.isBlank(e.getMessage()) ? ExceptionUtils.getStackTrace(e) : e.getMessage();
			} catch (Exception e) {
				logger.error("",e);
				return StringUtils.isBlank(e.getMessage()) ? ExceptionUtils.getStackTrace(e) : e.getMessage();
			}
		}
    	return null;
    }


    /**
     * This method looks for an existing session directory in the archive space.s
     * @return The file for the session directory.
     */
    @Nullable
    public File getSessionDir() {
        final String projectArchivePath = ArcSpecManager.GetInstance().getArchivePathForProject(this.getProject());
        if (projectArchivePath == null) {
            return null;
        }
        try (Stream<Path> stream = Files.list(Paths.get(projectArchivePath))) {
            return stream
                    .filter(path -> {
                        // Filter for `arc00x` directories, only
                        final String name = path.getFileName().toString();
                        return Files.isDirectory(path) && !"subjects".equals(name) && !"resources".equals(name);
                    })
                    .map(path -> path.resolve(this.getArchiveDirectoryName()))
                    .filter(Files::exists)
                    .findFirst()
                    .map(Path::toFile)
                    .orElse(null);
        } catch (IOException e) {
            // We cannot throw this up the chain, as it wouldn't be backward compatible
            return null;
        }
    }

    public void deleteFiles(final UserI user, final EventMetaI ci) throws Exception {
        final List<XnatAbstractresourceI> resources = getResources_resource();
        // If there are no resources to delete, then skip the rest of this.
        if (resources.isEmpty()) {
            return;
        }
        final String projectId = getProject();
        final String rootPath  = ArcSpecManager.GetInstance().getArchivePathForProject(projectId);
        OmUtils.deleteResourceFiles(user, rootPath, projectId, getSessionDir(), resources, ci);
    }

    /**
     * Converts invalid characters in the submitted value to "_".
     *
     * @param value The string to be cleaned
     *
     * @return The cleaned string
     *
     * @deprecated Use {@link OmUtils#cleanValue(String)} instead.
     */
    @Deprecated
    public static String cleanValue(final String value) {
    	return OmUtils.cleanValue(value);
    }

    /**
     * Gets root path to the primary project's archive space.
     * @return The path to the root folder of the experiment's primary project's archive space.
     */
    public String getArchiveRootPath() throws UnknownPrimaryProjectException{
    	XnatProjectdata p=getPrimaryProject(false);
    	if(p!=null){
    		return p.getRootArchivePath();
    	}else{
    		throw new UnknownPrimaryProjectException();
    	}
    }

    public static class UnknownPrimaryProjectException extends Exception{

    }

    /**
     * Gets root path to the primary project's cache space.
     * @return The path to the root folder of the subject's primary project's cache space.
     */
    public String getCachePath(){
        final XnatProjectdata primaryProject = getPrimaryProject(false);
        return primaryProject != null ? primaryProject.getCachePath() : "";
    }

    /**
     * Gets root path to the primary project's prearchive space.
     * @return The path to the root folder of the subject's primary project's prearchive space.
     */
    public String getPrearchivePath(){
        final XnatProjectdata primaryProject = getPrimaryProject(false);
        return primaryProject != null ? primaryProject.getPrearchivePath() : "";
    }

    public static class DefaultCurrentArcIdentifierImpl implements CurrentArcIdentifierI{
        @Override
        public String identify(BaseXnatExperimentdata expt){
            final XnatProjectdata primaryProject = expt.getPrimaryProject(false);
            if (primaryProject == null) {
                return null;
            }
            return primaryProject.getCurrentArc();
        }
    }

    public static CurrentArcIdentifierI GetCurrentArcIdentifier(){
        try{
           CurrentArcIdentifierI arcIdentifier = (CurrentArcIdentifierI)XDAT.getContextService().getBean(CurrentArcIdentifier.class);
            if(arcIdentifier == null){
                return new DefaultCurrentArcIdentifierImpl();
            } else {
                return arcIdentifier;
            }
        } catch (Exception e){
            return new DefaultCurrentArcIdentifierImpl();
        }
    }
    /**
     * This returns the current sub folder within the project archive folder for placing sessions (ie arc001).
     * @return The path to the current sub folder within the subject's primary project's archive space for session storage.
     * @throws InvalidArchiveStructure
     */
    public String getCurrentArchiveFolder() throws InvalidArchiveStructure, UnknownPrimaryProjectException {
        final String arcpath = getArchiveRootPath();
        final File f = new File(arcpath);

        if (!f.exists()) {
            f.mkdir();
        }

        String curA = GetCurrentArcIdentifier().identify((BaseXnatExperimentdata) this);

        if (curA == null) {
            return null;
        }

        logger.info("CURRENT_ARC:" + curA);
        if (!curA.endsWith("\\") && !curA.endsWith("/")) {
            curA += File.separator;
        }

        if (FileUtils.IsAbsolutePath(curA)) {
            final File currentArc = new File(curA);
            if (!currentArc.exists()) {
                currentArc.mkdirs();
            }

            int index = curA.indexOf(f.getName());
            if (index == -1) {
                throw new InvalidArchiveStructure(f.getName() + " does not exist in " + curA);
            } else {
                return curA.substring(index + f.getName().length() + 1);
            }
        } else {
            final File currentArc = new File(arcpath + curA);
            if (!currentArc.exists()) {
                currentArc.mkdirs();
            }
            return curA;
        }
    }

    /**
     * Returns path to the current archive folder for this experiment
     * @param absolute    Indicates whether the path should be returned as an absolute path or relative to the
     *                    {@link #getArchiveRootPath() root archive path}.
     * @return The folder location for the current session.
     * @throws InvalidArchiveStructure
     */
    public String getCurrentSessionFolder(boolean absolute) throws InvalidArchiveStructure,UnknownPrimaryProjectException{
        String session_path;

        final String currentarc = this.getCurrentArchiveFolder();
        if (currentarc ==null){
            session_path = this.getArchiveDirectoryName() + "/";
        }else{
            session_path = currentarc.replace('\\', '/') + this.getArchiveDirectoryName() + "/";
        }

        if (absolute){
            session_path= FileUtils.AppendRootPath(this.getArchiveRootPath(), session_path);
        }

        return session_path;
    }

    LightweightLocalCache<File> cachedSessionDirs=new LightweightLocalCache<File>();
    /**
     * This method looks for an existing session directory in the archive space.  If none is found, it returns the location where said directory would be created.
     * @return The folder in which the experiment is or should be located.
     */
    public File getExpectedSessionDir() throws InvalidArchiveStructure,UnknownPrimaryProjectException{
        File sessionDIR=cachedSessionDirs.getValue(this.getProject(), ONE_MINUTE);
        if(sessionDIR==null){
            sessionDIR=this.getSessionDir();

            if(sessionDIR==null){
                sessionDIR=     new File(this.getCurrentSessionFolder(true));
            }

            cachedSessionDirs.cacheValue(this.getProject(),sessionDIR);
        }

    	return sessionDIR;
    }

    protected void checkIsValidID(String s) throws IllegalArgumentException{

		if(StringUtils.isBlank(s)){
			throw new IllegalArgumentException();
		}

		if(!XftStringUtils.isValidId(s)){
			throw new IllegalArgumentException("Identifiers cannot use special characters.");
		}
    }

    public void checkUniqueLabel() throws Exception{
		if(!StringUtils.isBlank(this.getLabel())){
			Long count=(Long)PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM (SELECT label, ID FROM xnat_experimentData WHERE label='%1$s' AND ID !='%2$s' AND project='%3$s' UNION SELECT label, sharing_share_xnat_experimentda_id AS ID FROM xnat_experimentData_share WHERE label='%1$s' AND sharing_share_xnat_experimentda_id !='%2$s' AND project='%3$s') SRCH".formatted(this.getLabel(), this.getId(), this.getProject()), "count", this.getDBName(), "system");
			if(count>0){
				throw new ClientException(Status.CLIENT_ERROR_CONFLICT,"Conflict: Duplicate experiment label",new Exception());
			}
		}
    }


	@Override
	public void preSave() throws Exception {
        super.preSave();

        checkIsValidID(this.getId());

        checkIsValidID(this.getLabel());

        final XnatProjectdata proj = getPrimaryProject(false);
        if (proj == null) {
            throw new Exception("Unable to identify project for:" + this.getProject());
        }

        checkUniqueLabel();

        final String expectedPath = this.getExpectedSessionDir().getAbsolutePath().replace('\\', '/');
        OmUtils.validateXnatAbstractResources(expectedPath, getResources_resource());
    }

	public File getExpectedCurrentDirectory() throws InvalidArchiveStructure,UnknownPrimaryProjectException {
		return getExpectedSessionDir();
	}

	public String getResourceCatalogRootPathByLabel(final String label) {
        return getUriForMatchingCatalog(label, getResources_resource());
    }

	public static String getUriForMatchingCatalog(final String label, final List<XnatAbstractresourceI> resources) {
        final Optional<XnatAbstractresourceI> first = resources.stream().filter(resource -> resource instanceof XnatResourcecatalog xr
                                              && StringUtils.equalsIgnoreCase(label, resource.getLabel())
                                              && StringUtils.contains(xr.getUri(), "/"))
                .findFirst();
        return first.map(xnatAbstractresourceI -> StringUtils.substringBeforeLast(((XnatResourcecatalog) xnatAbstractresourceI).getUri(), "/"))
                    .orElse(null);
    }

	public static void SaveSharedProject(XnatExperimentdataShare pp, XnatExperimentdata expt,UserI user,final EventDetails event) throws Exception{
		PersistentWorkflowI wrk= WorkflowUtils.buildOpenWorkflow(user, expt.getItem(),event);
		EventMetaI c=wrk.buildEvent();
		PersistentWorkflowUtils.save(wrk, c);
		try {
			SaveItemHelper.authorizedSave(pp,user,false,false,c);
			PersistentWorkflowUtils.complete(wrk, c);
		} catch (Exception e) {
			logger.error("",e);
			PersistentWorkflowUtils.fail(wrk, c);
			throw e;
		}
	}

	public static EventMetaI ChangePrimaryProject(UserI user, XnatExperimentdata assessor, XnatProjectdata newProject, String newLabel, final EventDetails event, List<String> imageAssessors) throws Exception{
		PersistentWorkflowI wrk= WorkflowUtils.buildOpenWorkflow(user, assessor.getXSIType(), assessor.getId(),assessor.getProject(),event);
		EventMetaI c=wrk.buildEvent();
		PersistentWorkflowUtils.save(wrk, c);

		try {
			assessor.moveToProject(newProject,newLabel,user,c,imageAssessors);

			PersistentWorkflowUtils.complete(wrk, c);
		} catch (Exception e) {
			logger.error("",e);
			PersistentWorkflowUtils.fail(wrk,c);
			throw e;
		}

		return c;
	}

    List<WrkWorkflowdataI> workflows=null;
    public List<WrkWorkflowdataI> getWorkflows() throws Exception{
        if(workflows==null){
            workflows = new ArrayList<>();

            //search for workflow entries with a matching ID
            org.nrg.xft.search.CriteriaCollection cc = new CriteriaCollection("AND");
            cc.addClause("wrk:workflowData.ID",this.getId());
            org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(cc,null,false);

            //Sort by Launch Time
            final List<XFTItem> workItems = items.getItems("wrk:workflowData.launch_time","DESC");
            for (final XFTItem wrk : workItems)
            {
                workflows.add(new WrkWorkflowdata(wrk));
            }
        }

        return workflows;
    }

    public XnatExperimentdata getLightCopy() throws XFTInitException, ElementNotFoundException {
        XFTItem item = XFTItem.NewItem(this.getXSIType(), this.getUser());
        XnatExperimentdata new_expt=(XnatExperimentdata) BaseElement.GetGeneratedItem(item);
        new_expt.setId(this.getId());
        new_expt.setLabel(this.getLabel());
        new_expt.setProject(this.getProject());
        if (this instanceof XnatSubjectassessordata subjectassessordata) {
            ((XnatSubjectassessordata) new_expt).setSubjectId(subjectassessordata.getSubjectId());
        } else if (this instanceof XnatImageassessordata imageassessordata) {
            ((XnatImageassessordata) new_expt).setImagesessionId(imageassessordata.getImagesessionId());
        }
        return new_expt;
    }

	@Override
	public SecurityValues getSecurityTags(){
		SecurityValues projects=new SecurityValues();
		projects.getHash().put(this.getXSIType() +"/project", this.getProject());
	    for (final XnatExperimentdataShareI pp:this.getSharing_share())
        {
			projects.getHash().put(this.getXSIType() +"/sharing/share/project", pp.getProject());
        }
		return projects;
		
	}

	public XnatPvisitdata getVisitData() {
        return XnatPvisitdata.getXnatPvisitdatasById(this.getVisit(), null, false);
    }

    public static class LightweightLocalCache<T> {
        //loading each site config property can take a few milliseconds.  For common ones like logo path, site id, etc this adds up.  So, we can cache them for a minute or so.
        public class LocalCache<T>{
            T value;
            Long lastCheckInMillis;

            public LocalCache(T v){
                this.value=v;
                this.lastCheckInMillis= Calendar.getInstance().getTimeInMillis();
            }
        }

        public T getValue(String key, Integer timeoutInMillis){
            if(key !=null && localCache.containsKey(key)){
                LocalCache cache = localCache.get(key);
                if((cache.lastCheckInMillis + timeoutInMillis) > Calendar.getInstance().getTimeInMillis()){
                    return (T) cache.value;
                }
            }
            return null;
        }

        public T cacheValue(String key, T value){
            if(key!=null && value != null){
                LocalCache<T> lc = new LocalCache(value);
                localCache.put(key,lc);
            }
            return value;
        }

        private Map<String, LocalCache> localCache= new ConcurrentHashMap<>();
    }
}
