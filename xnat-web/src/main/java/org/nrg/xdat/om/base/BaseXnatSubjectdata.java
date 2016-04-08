/*
 * org.nrg.xdat.om.base.BaseXnatSubjectdata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/18/14 5:54 PM
 */
package org.nrg.xdat.om.base;

import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.auto.AutoXnatSubjectdata;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.SecurityValues;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.*;
import org.nrg.xft.identifier.IDGeneratorFactory;
import org.nrg.xft.identifier.IDGeneratorI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.turbine.utils.XNATUtils;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.data.Status;

import java.io.File;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatSubjectdata extends AutoXnatSubjectdata implements ArchivableItem, MoveableI{
    protected ArrayList<ItemI> minLoadAssessors = null;

	public BaseXnatSubjectdata(ItemI item)
	{
		super(item);
	}

	public BaseXnatSubjectdata(UserI user)
	{
		super(user);
	}

	public BaseXnatSubjectdata()
	{}

	public BaseXnatSubjectdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}
    
    public String getArchiveDirectoryName(){
    	if(this.getLabel()!=null)
    		return this.getLabel();
    	else 
    		return this.getId();
    }

	@SuppressWarnings("unused")
    public String getAddIdString(){
        StringBuilder sb = new StringBuilder();
        for(int j=0;j<getAddid().size();j++)
        {
            XnatSubjectdataAddid addid =(XnatSubjectdataAddid) getAddid().get(j);
            if (j==0)
            {
                sb.append(addid.getAddid()).append(" (").append(addid.getName()).append(")");
            }else{
                sb.append(", ").append(addid.getAddid()).append(" (").append(addid.getName()).append(")");
            }
        }
        return sb.toString();
    }

	public String getGenderText()
	{
	    String s = null;
        try {
            XnatAbstractdemographicdataI ame = this.getDemographics();
    	    if (ame instanceof XnatDemographicdata)
    	    {
    	        s= ((XnatDemographicdata)ame).getGender();
    	    }
        } catch (Exception e) {
            logger.error("",e);
        }
        if (s==null)
	    {
	        return "";
	    }else{
	        if (s.equalsIgnoreCase("m"))
	        {
	            return "Male";
	        }else if (s.equalsIgnoreCase("f"))
	        {
	            return "Female";
	        }else if (s.equalsIgnoreCase("o"))
	        {
	            return "Other";
	        }else if (s.equalsIgnoreCase("u"))
	        {
	            return "Unknown";
	        }else{
	            return StringUtils.capitalize(s.toLowerCase());
	        }
	    }
	}

	public Date getDOB()
	{
	    XnatAbstractdemographicdataI ame = this.getDemographics();
	    if (ame instanceof XnatDemographicdata)
	    {
	        return (Date)((XnatDemographicdata)ame).getDob();
	    }
	    return null;
	}

	public String getGender()
	{
	    XnatAbstractdemographicdataI ame = this.getDemographics();
	    if (ame instanceof XnatDemographicdata)
	    {
	        return ((XnatDemographicdata)ame).getGender();
	    }
	    return null;
	}

	public String getHandedness()
	{
	    XnatAbstractdemographicdataI ame = this.getDemographics();
	    if (ame instanceof XnatDemographicdata)
	    {
	        return ((XnatDemographicdata)ame).getHandedness();
	    }
	    return null;
	}

	public Integer getYOB()
	{
	    XnatAbstractdemographicdataI ame = this.getDemographics();
	    if (ame instanceof XnatDemographicdata)
	    {
	        return ((XnatDemographicdata)ame).getYob();
	    }
	    return null;
	}

	public String getHandedText()
	{
	    String s = null;
	    try {
            XnatAbstractdemographicdataI ame = this.getDemographics();
    	    if (ame instanceof XnatDemographicdata)
    	    {
    	        s= ((XnatDemographicdata)ame).getHandedness();
    	    }
        } catch (Exception e) {
            logger.error("",e);
        }
        if (s==null)
	    {
	        return "";
	    }else{
	        if (s.equalsIgnoreCase("l"))
	        {
	            return "Left";
	        }else if (s.equalsIgnoreCase("r"))
	        {
	            return "Right";
	        }else if (s.equalsIgnoreCase("a"))
	        {
	            return "Ambidextrous";
	        }else if (s.equalsIgnoreCase("u"))
	        {
	            return "Unknown";
	        }else{
	            return StringUtils.capitalize(s.toLowerCase());
	        }
	    }
	}

    @SuppressWarnings({"deprecation", "unused"})
	public String getDOBDisplay()
	{
	    try {
	        if (this.getYOB()!=null)
            {
                return (getYOB()) + "";
            }else if(this.getDOB()!=null){
                return (this.getDOB().getYear() + 1900) + "";
            }else{
                return "--";
            }
        } catch (Exception e) {
            logger.error("",e);
            return "--";
        }
	}

	public String getLongCreateTime()
	{
	    if (this.getItem().getInsertDate() == null)
		{
			return "--";
		}else
		{
		    Date date = this.getItem().getInsertDate();
			return DateFormat.getDateInstance(DateFormat.LONG).format(date);
		}
	}

	public String getAge(Date experimentDate) {
		if (experimentDate == null) {
			return failSafeAge();
		}
		try {
			Date dob = getDOB();
			if (dob != null) {
				return formatAge(calculateAge(experimentDate, dob));
			} else {
				Integer year = getYOB();
				if (year == null) {
					return failSafeAge();
				} else {
					Calendar cal = Calendar.getInstance();
					cal.set(year, Calendar.JANUARY, 1);
					dob = cal.getTime();
					return formatAge(calculateAge(experimentDate, dob));
				}
			}
		} catch (Exception e) {
			logger.error("",e);
			return failSafeAge();
		}
	}

	private Calendar makeCalendar(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return calendar;
	}

	private int calculateAge(Date current, Date dob) {
		Calendar currentCalendar = makeCalendar(current);
		Calendar dobCalendar = makeCalendar(dob);
		int age = currentCalendar.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);
		if (currentCalendar.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
			age--;
		}
		return age;
	}

	private String formatAge(double age) {
		NumberFormat formatter = NumberFormat.getInstance();
		formatter.setGroupingUsed(false);
		formatter.setMaximumFractionDigits(2);
		formatter.setMinimumFractionDigits(2);
		return formatter.format(age);
	}

	private String failSafeAge() {
		if (getAge() != null) {
			return getAge().toString();
		} else {
			return "--";
		}
	}
	
	public String getRace(){
	    try {
            XnatAbstractdemographicdataI ame = this.getDemographics();
    	    if (ame instanceof XnatDemographicdata)
    	    {
    	        return ((XnatDemographicdata)ame).getRace();
    	    }
        } catch (Exception e) {
            logger.error("",e);
        }
        return null;
	}

	public String getEthnicity(){
	    try {
            XnatAbstractdemographicdataI ame = this.getDemographics();
    	    if (ame instanceof XnatDemographicdata)
    	    {
    	        return ((XnatDemographicdata)ame).getEthnicity();
    	    }
        } catch (Exception e) {
            logger.error("",e);
        }
        return null;
	}

	public Integer getAge(){
	    try {
            XnatAbstractdemographicdataI ame = this.getDemographics();
    	    if (ame instanceof XnatDemographicdata)
    	    {
    	        return ((XnatDemographicdata)ame).getAge();
    	    }
        } catch (Exception e) {
            logger.error("",e);
        }
        return null;
	}

	public Integer getEducation(){
	    try {
            XnatAbstractdemographicdataI ame = this.getDemographics();
    	    if (ame instanceof XnatDemographicdata)
    	    {
    	        return ((XnatDemographicdata)ame).getEducation();
    	    }
        } catch (Exception e) {
            logger.error("",e);
        }
        return null;
	}

    public String getEducationDesc(){
        try {
            XnatAbstractdemographicdataI ame = this.getDemographics();
            if (ame instanceof XnatDemographicdata)
            {
                return ((XnatDemographicdata)ame).getEducationdesc();
            }
        } catch (Exception e) {
            logger.error("",e);
        }
        return null;
    }

	public Integer getSes(){
	    try {
            XnatAbstractdemographicdataI ame = this.getDemographics();
    	    if (ame instanceof XnatDemographicdata)
    	    {
    	        return ((XnatDemographicdata)ame).getSes();
    	    }
        } catch (Exception e) {
            logger.error("",e);
        }
        return null;
	}

	public String getCohort(){
	    try {
	        XnatAbstractsubjectmetadataI ame = this.getMetadata();
		    if (ame instanceof XnatSubjectmetadata)
		    {
		        return ((XnatSubjectmetadata)ame).getCohort();
		    }
        } catch (Exception e) {
            logger.error("",e);
        }
        return null;
	}


	public ArrayList<ItemI> getMinimalLoadAssessors()
	{
	    if (minLoadAssessors==null)
	    {
	        minLoadAssessors = new ArrayList<>();

	        try {
                XFTTable table = TableSearch.Execute("SELECT ex.id,ex.date,me.element_name AS type,ex.project,me.element_name,ex.note AS note,projects,label,visit as visit, protocol as protocol FROM xnat_subjectAssessorData assessor LEFT JOIN xnat_experimentData ex ON assessor.ID=ex.ID LEFT JOIN xnat_experimentdata_meta_data meta ON ex.experimentdata_info=meta.meta_data_id LEFT JOIN xdat_meta_element me ON ex.extension=me.xdat_meta_element_id LEFT JOIN (SELECT xs_a_concat(project || ':' || label || ',') AS PROJECTS, sharing_share_xnat_experimentda_id FROM xnat_experimentData_share GROUP BY sharing_share_xnat_experimentda_id) PROJECT_SEARCH ON ex.id=PROJECT_SEARCH.sharing_share_xnat_experimentda_id WHERE assessor.subject_id='" + this.getId() +"' AND meta.status!='obsolete'  ORDER BY ex.date ASC",getDBName(),null);
                table.resetRowCursor();
                while (table.hasMoreRows())
                {
                	final Hashtable row = table.nextRowHash();
                    final String element = (String)row.get("element_name");
                    try {
                    	final XFTItem child = XFTItem.NewItem(element,this.getUser());

                        final Object date = row.get("date");
                        final Object id = row.get("id");
                        final Object note = row.get("note");
                        final Object project = row.get("project");
                        final Object label = row.get("label");
                        final Object visit = row.get("visit");
                        final Object protocol = row.get("protocol");

                        if (date!=null)
                        {
                            try {
                                child.setProperty("date",date);
                            } catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                logger.error("",e);
                            }
                        }
                        if (id!=null)
                        {
                            try {
                                child.setProperty("ID",id);
                            } catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                logger.error("",e);
                            }
                        }
                        if (label!=null)
                        {
                            try {
                                child.setProperty("label",label);
							} catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                logger.error("",e);
                            }
                        }
                        if (visit!=null)
                        {
                            try {
                                child.setProperty("visit",visit);
							} catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                logger.error("",e);
                            }
                        }
                        if (protocol!=null)
                        {
                            try {
                                child.setProperty("protocol",protocol);
							} catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                logger.error("",e);
                            }
                        }
                        if (note!=null)
                        {
                            try {
                                child.setProperty("note",note);
							} catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                logger.error("",e);
                            }
                        }
                        if (project!=null)
                        {
                            try {
                                child.setProperty("project",project);
							} catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                logger.error("",e);
                            }
                        }

                        final String projects = (String)row.get("projects");
                        if (projects!=null)
                        {
                            for(final String projectName:XftStringUtils.CommaDelimitedStringToArrayList(projects, true))
                            {
                               if(projectName.contains(":")){
                            	   XnatExperimentdataShare es = new XnatExperimentdataShare(this.getUser());
                            	   es.setProject(projectName.substring(0,projectName.indexOf(":")));
                            	   if(!projectName.endsWith(":")){
                            		   es.setLabel(projectName.substring(projectName.indexOf(":")+1));
                            	   }
                            	   child.setChild("xnat:experimentData/sharing/share",es.getItem(),false);
                               }else{
                                   child.setProperty("sharing.share.project", projectName);
                               }
                            }
                        }


                        if (child.instanceOf("xnat:imageSessionData"))
                        {
                            minLoadAssessors.add(BaseElement.GetGeneratedItem(child));
                            try {
                            	XFTTable table2 = TableSearch.Execute("SELECT ex.id,ex.date,me.element_name AS type,ex.project,me.element_name,ex.note AS note,projects,label,assessor.imagesession_id,visit as visit, protocol as protocol FROM xnat_imageAssessorData assessor LEFT JOIN xnat_experimentData ex ON assessor.ID=ex.ID LEFT JOIN xnat_experimentdata_meta_data meta ON ex.experimentdata_info=meta.meta_data_id LEFT JOIN xdat_meta_element me ON ex.extension=me.xdat_meta_element_id LEFT JOIN (SELECT xs_a_concat(project || ':' || label || ',') AS PROJECTS, sharing_share_xnat_experimentda_id FROM xnat_experimentData_share GROUP BY sharing_share_xnat_experimentda_id) PROJECT_SEARCH ON ex.id=PROJECT_SEARCH.sharing_share_xnat_experimentda_id WHERE assessor.imagesession_id='" + id +"' AND meta.status!='obsolete'  ORDER BY ex.date ASC",getDBName(),null);
                                table2.resetRowCursor();
                                while (table2.hasMoreRows())
                                {
                                	final Hashtable row2 = table2.nextRowHash();
                                	final String element2 = (String)row2.get("element_name");
                                    try {
                                    	final XFTItem child2 = XFTItem.NewItem(element2,this.getUser());

                                        final Object date2 = row2.get("date");
                                        final  Object id2 = row2.get("id");
                                        final Object project2 = row2.get("project");
                                        final Object note2 = row2.get("note");
                                        final Object label2 = row2.get("label");
                                        final Object visit2 = row2.get("visit");
                                        final Object protocol2 = row2.get("protocol");
                                        final Object imgsession2 = row2.get("imagesession_id");


                                        if (imgsession2!=null)
                                        {
                                            try {
                                                child2.setProperty("imageSession_ID",imgsession2);
											} catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                                logger.error("",e);
                                            }
                                        }
                                        if (date2!=null)
                                        {
                                            try {
                                                child2.setProperty("date",date2);
											} catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                                logger.error("",e);
                                            }
                                        }
                                        if (id2!=null)
                                        {
                                            try {
                                                child2.setProperty("ID",id2);
											} catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                                logger.error("",e);
                                            }
                                        }
                                        if (label2!=null)
                                        {
                                            try {
                                                child2.setProperty("label",label2);
											} catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                                logger.error("",e);
                                            }
                                        }
                                        if (visit2!=null)
                                        {
                                            try {
                                                child2.setProperty("visit_id",visit2);
											} catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                                logger.error("",e);
                                            }
                                        }
                                        if (protocol2!=null)
                                        {
                                            try {
                                                child2.setProperty("protocol",protocol2);
											} catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                                logger.error("",e);
                                            }
                                        }
                                        if (note2!=null)
                                        {
                                            try {
                                                child2.setProperty("note",note2);
											} catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                                logger.error("",e);
                                            }
                                        }
                                        if (project2!=null)
                                        {
                                            try {
                                                child2.setProperty("project",project2);
											} catch (XFTInitException | ElementNotFoundException | InvalidValueException | FieldNotFoundException e) {
                                                logger.error("",e);
                                            }
                                        }

                                        final String projects2 = (String)row2.get("projects");
                                        if (projects2!=null)
                                        {
                                        	for(final String projectName:XftStringUtils.CommaDelimitedStringToArrayList(projects2, true))
                                            {
                                               if(projectName.contains(":")){
                                            	   final XnatExperimentdataShare es = new XnatExperimentdataShare(this.getUser());
                                            	   es.setProject(projectName.substring(0,projectName.indexOf(":")));
                                            	   if(!projectName.endsWith(":")){
                                            		   es.setLabel(projectName.substring(projectName.indexOf(":")+1));
                                            	   }
                                            	   child2.setChild("xnat:experimentData/sharing/share",es.getItem(),false);
                                               }else{
                                                   child2.setProperty("sharing.share.project", projectName);
                                               }
                                            }
                                        }

                                        minLoadAssessors.add(BaseElement.GetGeneratedItem(child2));
                                    } catch (XFTInitException | ElementNotFoundException e) {
                                        logger.error("",e);
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("",e);
                            }
                        }else{
                            minLoadAssessors.add(BaseElement.GetGeneratedItem(child));
                        }
                    } catch (XFTInitException | ElementNotFoundException e) {
                        logger.error("",e);
                    }
                }
            } catch (Exception e) {
                logger.error("",e);
            }
	    }

	    return minLoadAssessors;
	}

	@SuppressWarnings("unused")
	public int getMinimalLoadAssessorsCount(String elementName)
	{
	    return getMinimalLoadAssessors(elementName).size();
	}

	@SuppressWarnings("unused")
	public Map<String,List<MinLoadExptByP>> getMinimalLoadAssessorsByProject()
	{
		final Map<String,List<MinLoadExptByP>> al = new TreeMap<>();
		final Map<String,String> projects= new Hashtable<>();
         for (final ItemI assessor:this.getMinimalLoadAssessors())
         {
        	 try{
        		 final XnatExperimentdata expt=(XnatExperimentdata)assessor;
        		 if(projects.get(expt.getProject())==null){
            		 final String pAlias=expt.getProjectDisplayID();
        			 projects.put(expt.getProject(), pAlias);
        			 al.put(pAlias, new ArrayList<MinLoadExptByP>());
        		 }
        		 
        		 al.get(projects.get(expt.getProject())).add(new MinLoadExptByP(expt.getProject(), expt.getIdentifier(expt.getProject()),expt));
        		 
        		 for(final XnatExperimentdataShareI share:expt.getSharing_share()){
            		 if(projects.get(share.getProject())==null){
                		 final String pAlias=((XnatExperimentdataShare)share).getProjectDisplayID();
            			 projects.put(share.getProject(), pAlias);
            			 al.put(pAlias, new ArrayList<MinLoadExptByP>());
            		 }

            		 al.get(projects.get(share.getProject())).add(new MinLoadExptByP(share.getProject(), expt.getIdentifier(share.getProject()),expt));
        		 }
        	 }catch (Throwable e){
        		 logger.error("", e);
        	 }
         }

	     return al;
	}
	
	public class MinLoadExptByP{
		String project=null;
		String label=null;
		ItemI item =null;
		
		public MinLoadExptByP(String p, String l, ItemI i){
			project=p;
			label=l;
			item=i;
		}

		public ItemI getItem() {
			return item;
		}

		public void setItem(ItemI item) {
			this.item = item;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getProject() {
			return project;
		}

		public void setProject(String project) {
			this.project = project;
		}
		
		
	}

	public ArrayList getMinimalLoadAssessors(String elementName)
	{
	    ArrayList al = new ArrayList();
	    try {
            SchemaElement e = SchemaElement.GetElement(elementName);
			for (final ItemI assessor : this.getMinimalLoadAssessors()) {
				if (assessor.getXSIType().equalsIgnoreCase(e.getFullXMLName())) {
                      al.add(assessor);
                 }
             }
        } catch (XFTInitException | ElementNotFoundException e) {
            logger.error("",e);
        }

	     al.trimToSize();
	     return al;
	}


	public ArrayList<XnatSubjectassessordata> getExperiments_experiment(String type)
	{
	    ArrayList<XnatSubjectassessordata> al = new ArrayList<>();
		for (final XnatSubjectassessordataI o : getExperiments_experiment()) {
			XnatSubjectassessordata expt = (XnatSubjectassessordata) o;
			try {
				if (expt.getItem().instanceOf(type)) {
					al.add(expt);
				}
			} catch (ElementNotFoundException e) {
				logger.error("",e);
			}
		}

	    return al;
	}

	@SuppressWarnings("unused")
	public ArrayList getSessionsByType(String type) {
        ArrayList al = new ArrayList();
		for (final XnatSubjectassessordata assessor : getExperiments_experiment("xnat:mrSessionData")) {
			XnatMrsessiondata expt = (XnatMrsessiondata) assessor;
            if (expt.getSessionType()!=null){
				if (expt.getSessionType().equalsIgnoreCase(type)) {
                    al.add(expt);
                }
            }else{
                if(type==null)
                    al.add(expt);
            }
        }
        return al;
    }

	@SuppressWarnings("unused")
	public int getExperiments_experiment_Count(String type)
	{
	    return getExperiments_experiment(type).size();
	}

	@SuppressWarnings("unused")
	public boolean isMrAssessor(ItemI i) {
		return i instanceof BaseXnatMrassessordata;
	}

	public boolean isImageAssessor(ItemI i)
	{
		return i instanceof BaseXnatImageassessordata;
	}

	public XnatMrsessiondata getLastSession()
	{
	    return XNATUtils.getLastSessionForParticipant(getId(), getUser());
	}

	@SuppressWarnings("unused")
	public ArrayList getScannerSortedSessions()
	{
	    ArrayList al = this.getExperiments_experiment("xnat:mrSessionData");
	    Collections.sort(al,BaseXnatMrsessiondata.GetScannerDelayComparator());
	    return al;
	}

	@SuppressWarnings("unused")
    public String getAddFieldValueByName(String name)
    {
        return (String)this.getFieldByName(name);
    }

	// XNAT-2865 - Function performs case insensitive search for subject
    public static XnatSubjectdata GetSubjectByProjectIdentifierCaseInsensitive(String project, String identifier, UserI user, boolean preLoad){
        try{
           String id = (String)PoolDBUtils.ReturnStatisticQuery("SELECT id, label, project FROM xnat_subjectdata WHERE LOWER(project) = '" + project.toLowerCase() + "' AND LOWER(label) = '" + identifier.toLowerCase() + "';", "id", null, null);
           return XnatSubjectdata.getXnatSubjectdatasById(id, user, preLoad);
        }catch(Exception e){
           logger.debug("Unable to find subject.",e);
           return null;
        }
    }
    
    public static XnatSubjectdata GetSubjectByProjectIdentifier(String project, String identifier,UserI user,boolean preLoad){
        CriteriaCollection cc=new CriteriaCollection("OR");
            	
    	CriteriaCollection subcc1 = new CriteriaCollection("AND");
        subcc1.addClause("xnat:subjectData/project", project);
        subcc1.addClause("xnat:subjectData/label", identifier);
        
        cc.add(subcc1);
            	
    	CriteriaCollection subcc2 = new CriteriaCollection("AND");
    	subcc2.addClause("xnat:subjectData/sharing/share/project", project);
    	subcc2.addClause("xnat:subjectData/sharing/share/label", identifier);
        
        cc.add(subcc2);

        ArrayList al =  XnatSubjectdata.getXnatSubjectdatasByField(cc, user, preLoad);
        if (al.size()>0){
           return new XnatSubjectdata((ItemI)al.get(0));
        }else{
            return null;
        }

    }

    public String getIdentifier(String project){
    	 return this.getIdentifier(project, true);
    }

    public String getIdentifiers(){
        final Hashtable<String,String> ids = new Hashtable<>();
        
        if (this.getProject()!=null){
        	if (this.getLabel()!=null){
        		ids.put(this.getLabel(), this.getProject());
        	}else{
        		ids.put(this.getId(), this.getProject());
        	}
        }
        for (final XnatProjectparticipantI pp:this.getSharing_share())
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

        int counter=0;
        for (String key: ids.keySet()){
            if (counter++>0) {
				identifiers.append(", ");
			}
            identifiers.append(key).append(" (").append(ids.get(key)).append(")");
        }

        return identifiers.toString();
    }

    public Hashtable<XnatProjectdataI,String> getProjectDatas(){
        final Hashtable<XnatProjectdataI,String> hash = new Hashtable<>();
        for (final XnatProjectparticipantI pp:this.getSharing_share())
        {
            if (pp.getLabel()==null)
                hash.put(((XnatProjectparticipant)pp).getProjectData(), this.getId());
            else
                hash.put(((XnatProjectparticipant)pp).getProjectData(), pp.getLabel());
        }
        return hash;
    }

    Hashtable fieldsByName = null;
    public Hashtable getFieldsByName(){
        if (fieldsByName == null){
            fieldsByName=new Hashtable();
            for (final XnatSubjectdataFieldI field : this.getFields_field()){
                fieldsByName.put(field.getName(), field);
            }
        }

        return fieldsByName;
    }

    public Object getFieldByName(String s){
        final XnatSubjectdataFieldI field = (XnatSubjectdataFieldI)getFieldsByName().get(s);
        if (field!=null){
            return field.getField();
        }else{
            return null;
        }
    }


    public XnatProjectdataI getProject(String projectID, boolean preLoad)
    {
        XnatProjectparticipant ep = null;
        for (final XnatProjectparticipantI tempep: this.getSharing_share())
        {
            if (tempep.getProject().equals(projectID))
            {
                ep=(XnatProjectparticipant)tempep;
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


    public XnatProjectdataI getFirstProject()
    {
        XnatProjectparticipant ep = null;
        if (!this.getSharing_share().isEmpty()){
        	ep = (XnatProjectparticipant)this.getSharing_share().get(0);
        }

        try {
            if (ep!=null){
                return XnatProjectdata.getXnatProjectdatasById(ep.getProject(), this.getUser(), false);
            }
        } catch (RuntimeException e) {
            logger.error("",e);
        }

        return null;
    }

    public String getIdentifier(String project,boolean returnNULL){
        if (project!=null){
        	if (this.getProject().equals(project)){
        		if (this.getLabel()!=null){
        			return this.getLabel();
        		}
        	}
        	
            for (final XnatProjectparticipantI pp: this.getSharing_share())
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

    public int getSubjectAssessorCount(){
        try {
           final  XFTTable table = XFTTable.Execute("SELECT COUNT(*) FROM xnat_subjectassessordata WHERE subject_id='" + getId() + "';", getDBName(), null);

            Long i = (Long)table.getFirstObject();
            if (i!=null){
                return i.intValue();
            }
        } catch (SQLException | DBPoolException e) {
            logger.error("",e);
        }
        return -1;
    }


//    public String createNewAssessorId(String xsiType) throws SQLException{
//        String newID= "";
//        String prefix= "";
//        long i = this.getExperiments_experiment_Count(xsiType)+1;
//        prefix+=this.getId();
//
//        NumberFormat nf = NumberFormat.getInstance();
//        nf.setMinimumIntegerDigits(3);
//        
//        String code =ElementSecurity.GetCode(xsiType);
//        if(code!=null && !code.equals(""))
//            prefix+="_" + code;
//        
//        newID+=prefix +"_"+ StringUtils.replace(nf.format(i), ",", "");
//        
//        String query = "SELECT count(ID) AS id_count FROM xnat_experimentdata WHERE ID='";
//
//        String login = null;
//        if (this.getUser()!=null){
//            login=this.getUser().getUsername();
//        }
//        try {
//            Long idCOUNT= (Long)PoolDBUtils.ReturnStatisticQuery(query + newID + "';", "id_count", this.getDBName(), login);
//            while (idCOUNT > 0){
//                i++;
//                newID=prefix + "_"+ StringUtils.replace(nf.format(i), ",", "");
//                idCOUNT= (Long)PoolDBUtils.ReturnStatisticQuery(query + newID + "';", "id_count", this.getDBName(), login);
//            }
//        } catch (Exception e) {
//            logger.error("",e);
//        }
//
//        return newID;
//    }

    private String name = null;
    private String description = null;
    private String secondaryID = null;
    private boolean initd = false;

    public void loadProjectDetails(){
        if (!initd)
        {
            initd=true;
            try {
                XFTTable table = XFTTable.Execute("SELECT name,description,secondary_ID FROM xnat_projectData WHERE ID ='" + this.getProject() + "';", this.getDBName(), null);

                if (table.size()>0)
                {
                    final Object[] row = table.rows().get(0);
                    name = (String)row[0];
                    description = (String)row[1];
                    secondaryID = (String)row[2];
                }
            } catch (SQLException | DBPoolException e) {
                logger.error("",e);
            }
        }
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

	@SuppressWarnings("unused")
	public Collection<XnatFielddefinitiongroup> getFieldDefinitionGroups(final String dataType, final String projectID) {
		final Hashtable<String, XnatFielddefinitiongroup> groups = new Hashtable<>();
		final Hashtable<XnatProjectdataI, String> projects = getProjectDatas();
		final XnatProjectdata primaryProject = getPrimaryProject(false);
		if (primaryProject != null) {
			projects.put(primaryProject, "");

			for (final Map.Entry<XnatProjectdataI, String> entry : projects.entrySet()) {
				final XnatAbstractprotocol prot = ((XnatProjectdata) entry.getKey()).getProtocolByDataType(dataType);
            if((projectID != null && !projectID.isEmpty()) && !projectID.equals(entry.getKey().getId())){
               continue;
            }
            if (prot!=null && prot instanceof XnatDatatypeprotocol){
					final XnatDatatypeprotocol dataProt = (XnatDatatypeprotocol) prot;
					for (final XnatFielddefinitiongroupI group : dataProt.getDefinitions_definition()) {
                    groups.put(group.getId(), (XnatFielddefinitiongroup)group);
                }
            }
        }
		}
        return groups.values();
    }
    
    public static String CreateNewID() throws Exception{
    	IDGeneratorI generator = IDGeneratorFactory.GetIDGenerator("org.nrg.xnat.turbine.utils.IDGenerator");
		assert generator != null;
    	generator.setTable("xnat_subjectData");
    	generator.setDigits(5);
    	generator.setColumn("id");
    	return generator.generateIdentifier();
    }
    
    public void moveToProject(XnatProjectdata newProject,String newLabel,UserI user,EventMetaI ci) throws Exception{
    	if(!this.getProject().equals(newProject.getId()))
    	{
    		if (!MoverMaker.check(this, user)) {
    			throw new InvalidPermissionException(this.getXSIType());
    		}
    		
    		String existingRootPath=this.getProjectData().getRootArchivePath();
    		
    		if(newLabel==null)newLabel = this.getLabel();
    		if(newLabel==null)newLabel = this.getId();
    		
    		File newSessionDir = new File(new File(newProject.getRootArchivePath(),newProject.getCurrentArc()),newLabel);
    		
    		String current_label=this.getLabel();
    		
    		if(current_label==null)current_label=this.getId();
    		for(XnatAbstractresourceI abstRes:this.getResources_resource()){
    			MoverMaker.Mover m = MoverMaker.moveResource(abstRes, current_label, this, newSessionDir, existingRootPath, user,ci);
    			m.setResource((XnatAbstractresource) abstRes);
    			m.call();
    		}
    		    		
    		MoverMaker.writeDB(this, newProject, newLabel, user,ci);
    		MoverMaker.setLocal(this, newProject, newLabel);
    	}
    }
    
    public boolean hasProject(String proj_id){
		if(this.getProject().equals(proj_id)){
		    return true;
		}else{
		    for(XnatProjectparticipantI pp: this.getSharing_share()){
			if(pp.getProject().equals(proj_id)){
			    return true;
			}
		    }
		}
		
		return false;
    }
    
    public String canDelete(BaseXnatProjectdata proj, UserI user) {
    	BaseXnatSubjectdata subj=this;
    	if(this.getItem().getUser()!=null){
    		subj=new XnatSubjectdata(this.getCurrentDBVersion(true));
    	}
    	if(!subj.hasProject(proj.getId())){
    		return "Subject is not assigned to specified project " + proj.getId();
    	}else {

			try {
				SecurityValues values = new SecurityValues();
				values.put(this.getXSIType() + "/project", proj.getId());
				
				if (!Permissions.canDelete(user, this.getSchemaElement(), values))
				{
					return "User cannot delete subjects for project " + proj.getId();
				}
			} catch (Exception e1) {
				return "Unable to delete subject.";
			}
			
    		for(XnatSubjectassessordataI sad: subj.getExperiments_experiment()){
    			String msg=((XnatSubjectassessordata)sad).canDelete(proj,user);
    			if(msg!=null){
    				return msg;
    			}
    		}
			
			return null;
    	}
    }
    
    public String delete(BaseXnatProjectdata proj, UserI user, boolean removeFiles,final EventMetaI c){
    	BaseXnatSubjectdata sub=this;
    	if(this.getItem().getUser()!=null){
    		sub=new XnatSubjectdata(this.getCurrentDBVersion(true));
    	}
    	
    	String msg=sub.canDelete(proj,user);
    	if(msg!=null){
    		logger.error(msg);
    		return msg;
    	}
    	
    	if(!sub.getProject().equals(proj.getId())){
			try {
				SecurityValues values = new SecurityValues();
				values.put(this.getXSIType() + "/project", proj.getId());
				
				if (!Permissions.canDelete(user, sub) && !Permissions.canDelete(user,this.getSchemaElement(),values))
				{
					return null;
				}

				//unshare children before unsharing parent
				final  List<XnatSubjectassessordataI> expts = sub.getExperiments_experiment();
		        for (XnatSubjectassessordataI exptI : expts){
		        	final XnatSubjectassessordata expt = (XnatSubjectassessordata)exptI;
		        	if(expt.getProject().equals(proj.getId())){
		        		return "This operation would delete an experiment (rather than un-share).  Please modify experiment ("+expt.getId()+").";
		        		
		        	}
		            msg=expt.delete(proj,user,false,c);
		            if(msg!=null){
		            	return msg;
		            }
		        }
				
				int index = 0;
				int match = -1;
				for(XnatProjectparticipantI pp : sub.getSharing_share()){
					if(pp.getProject().equals(proj.getId())){
						SaveItemHelper.authorizedRemoveChild(sub.getItem(), "xnat:subjectData/sharing/share", ((XnatProjectparticipant)pp).getItem(), user,c);
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
			
				if(!Permissions.canDelete(user, this)){
					return "User account doesn't have permission to delete this subject.";
				}
							
				if(removeFiles){
					this.deleteFiles(user,c);
				}

				final  List<XnatSubjectassessordataI> expts = sub.getExperiments_experiment();
		        for (XnatSubjectassessordataI exptI : expts){
		        	final XnatSubjectassessordata expt = (XnatSubjectassessordata)exptI;
		            msg=expt.delete(proj,user,removeFiles,c);
		            if(msg!=null)return msg;
		        }
		        
		        SaveItemHelper.authorizedDelete(sub.getItem().getCurrentDBVersion(), user,c);
				
				Users.clearCache(user);
				MaterializedView.deleteByUser(user);
			} catch (SQLException e) {
				logger.error("",e);
				return e.getMessage();
			} catch (Exception e) {
				logger.error("",e);
				return e.getMessage();
			}
		}
    	return null;
    }
    
    public void deleteFiles(UserI user, EventMetaI ci) throws Exception{
    	final XnatProjectdata project = this.getPrimaryProject(false);
		if (project != null) {
			String archive=project.getRootArchivePath();
    	File dir=new File(archive,"subjects/"+ this.getArchiveDirectoryName());
    	if(dir.exists()){
    		FileUtils.MoveToCache(dir);
    	}
    	
    	for(XnatAbstractresourceI abstRes:this.getResources_resource()){
    		((XnatAbstractresource)abstRes).deleteWithBackup(archive,user,ci);
    	}
		} else {
			logger.warn("User " + user.getUsername() + " tried to delete the files for the primary project for subject " + getLabel() + ", but there was no primary project found.");
		}
    }
    
    public int getAssessmentCount(String project){
    	int count=0;
    	for(int i=0;i<this.getMinimalLoadAssessors().size();i++){
    		XnatExperimentdata expt=(XnatExperimentdata)this.getMinimalLoadAssessors().get(i);
    		if(expt.getProject().equals(project)){
    			count++;
    		}
    	}
    	return count;
    }

    public static String cleanValue(String v){
    	v= StringUtils.replace(v, " ", "_");
    	v= StringUtils.replace(v, "`", "_");
    	v= StringUtils.replace(v, "~", "_");
    	v= StringUtils.replace(v, "@", "_");
    	v= StringUtils.replace(v, "#", "_");
    	v= StringUtils.replace(v, "$", "_");
    	v= StringUtils.replace(v, "%", "_");
    	v= StringUtils.replace(v, "^", "_");
    	v= StringUtils.replace(v, "&", "_");
    	v= StringUtils.replace(v, "*", "_");
    	v= StringUtils.replace(v, "(", "_");
    	v= StringUtils.replace(v, ")", "_");
    	v= StringUtils.replace(v, "+", "_");
    	v= StringUtils.replace(v, "=", "_");
    	v= StringUtils.replace(v, "[", "_");
    	v= StringUtils.replace(v, "]", "_");
    	v= StringUtils.replace(v, "{", "_");
    	v= StringUtils.replace(v, "}", "_");
    	v= StringUtils.replace(v, "|", "_");
    	v= StringUtils.replace(v, "\\", "_");
    	v= StringUtils.replace(v, "/", "_");
    	v= StringUtils.replace(v, "?", "_");
    	v= StringUtils.replace(v, ":", "_");
    	v= StringUtils.replace(v, ";", "_");
    	v= StringUtils.replace(v, "\"", "_");
    	v= StringUtils.replace(v, "'", "_");
    	v= StringUtils.replace(v, ",", "_");
    	v= StringUtils.replace(v, ".", "_");
    	v= StringUtils.replace(v, "<", "_");
    	v= StringUtils.replace(v, ">", "_");
    	
    	return v;
    }
    
    public void checkUniqueLabel() throws Exception{
		if(StringUtils.isNotBlank(this.getLabel())){
			Long count=(Long)PoolDBUtils.ReturnStatisticQuery(String.format("SELECT COUNT(*) FROM (SELECT label,id FROM xnat_subjectData WHERE label='%1$s' AND ID !='%2$s' AND project='%3$s' UNION SELECT label, subject_id as ID FROM xnat_projectParticipant WHERE label='%1$s' AND subject_id !='%2$s' AND project='%3$s')SRCH",this.getLabel(),this.getId(),this.getProject()), "count", this.getDBName(), "system");
			if(count>0){
				throw new ClientException(Status.CLIENT_ERROR_CONFLICT,"Conflict: Duplicate subject label",new Exception());
			}
		}
    }


	@Override
	public void preSave() throws Exception{
		super.preSave();
		
		if(StringUtils.isBlank(this.getId())){
			throw new IllegalArgumentException();
		}	
		
		if(StringUtils.isBlank(this.getLabel())){
			throw new IllegalArgumentException();
		}
		
		if(!XftStringUtils.IsAlphaNumericUnderscore(getId())){
			throw new IllegalArgumentException("Identifiers cannot use special characters.");
		}
		
		if(!XftStringUtils.IsAlphaNumericUnderscore(getLabel())){
			throw new IllegalArgumentException("Labels cannot use special characters.");
		}
		
		
		final XnatProjectdata proj = this.getPrimaryProject(false);
		if(proj==null){
			throw new Exception("Unable to identify project for:" + this.getProject());
		}
		
		checkUniqueLabel();
		
		final String expectedPath=getExpectedCurrentDirectory().getAbsolutePath().replace('\\', '/');
		
		for(final XnatAbstractresourceI res: this.getResources_resource()){
			final String uri;
			if(res instanceof XnatResource){
				uri=((XnatResource)res).getUri();
			}else if(res instanceof XnatResourceseries){
				uri=((XnatResourceseries)res).getPath();
			}else{
				continue;
			}
			
			File u = new File(uri);
			if (u.isFile()) {
				FileUtils.ValidateUriAgainstRoot(u.getParent(),expectedPath,"URI references data outside of the project: " + uri);
			}
			else {
				FileUtils.ValidateUriAgainstRoot(uri,expectedPath,"URI references data outside of the project: " + uri);
			}
		}
		
		for(final XnatSubjectassessordataI expt:this.getExperiments_experiment()){
			((XnatSubjectassessordata)expt).preSave();
		}
	}

	public File getExpectedCurrentDirectory() throws InvalidArchiveStructure {
		final XnatProjectdata primaryProject = this.getPrimaryProject(false);
		return primaryProject == null ? null : new File(primaryProject.getRootArchivePath(),"subjects/"+ this.getArchiveDirectoryName());
	}
	
	public static void save(XnatSubjectdata subject,boolean overrideSecurity, boolean allowItemRemoval, UserI user,EventDetails event) throws Exception{
		PersistentWorkflowI wrk= WorkflowUtils.buildOpenWorkflow(user, subject.getXSIType(), subject.getId(),subject.getProject(),event);
		EventMetaI c=wrk.buildEvent();
		
		try {
			SaveItemHelper.authorizedSave(subject,user, overrideSecurity, allowItemRemoval,c);
			WorkflowUtils.complete(wrk, c);
		} catch (Exception e) {
			WorkflowUtils.fail(wrk, c);
			throw e;
		}
	}

	public static void SaveSharedProject(XnatProjectparticipant pp, XnatSubjectdata expt,UserI user,EventDetails event) throws Exception{
		PersistentWorkflowI wrk= WorkflowUtils.buildOpenWorkflow(user, expt.getItem(), event);
		EventMetaI c=wrk.buildEvent();
		PersistentWorkflowUtils.save(wrk, c);
		try {
			SaveItemHelper.authorizedSave(pp, user, false,false,c);
			PersistentWorkflowUtils.complete(wrk, c);
		} catch (Exception e) {
			logger.error("",e);
			PersistentWorkflowUtils.fail(wrk, c);
			throw e;
		}
	}
	
	public static EventMetaI ChangePrimaryProject(UserI user, XnatSubjectdata assessor, XnatProjectdata newProject, String newLabel,EventDetails event) throws Exception{
		PersistentWorkflowI wrk= WorkflowUtils.buildOpenWorkflow(user, assessor.getXSIType(), assessor.getId(),assessor.getProject(),event);
		wrk.setDetails("Move subject from project " + assessor.getProject() + " to " + newProject.getId());
		EventMetaI c=wrk.buildEvent();
		PersistentWorkflowUtils.save(wrk, c);

		try {
			assessor.moveToProject(newProject,newLabel,user,c);

			PersistentWorkflowUtils.complete(wrk, c);
		} catch (Exception e) {
			logger.error("",e);
			PersistentWorkflowUtils.fail(wrk,c);
			throw e;
		}
		
		return c;
	}

    /**
     * Gets root path to the primary project's archive space.
     * @return The path to the root folder of the subject's primary project's archive space.
     */
    public String getArchiveRootPath(){
		final XnatProjectdata primaryProject = getPrimaryProject(false);
		return primaryProject != null ? primaryProject.getRootArchivePath() : null;
    }
   
    public XnatSubjectdata getLightCopy() throws XFTInitException, ElementNotFoundException {
        XFTItem item = XFTItem.NewItem(this.getXSIType(), this.getUser());
        XnatSubjectdata new_expt=(XnatSubjectdata) BaseElement.GetGeneratedItem(item);
        new_expt.setId(this.getId());
        new_expt.setLabel(this.getLabel());
        new_expt.setProject(this.getProject());
        return new_expt;
    }

	@Override
	public SecurityValues getSecurityTags() {
		SecurityValues projects=new SecurityValues();
		projects.getHash().put(this.getXSIType() +"/project", this.getProject());
	    for (final XnatProjectparticipantI pp:this.getSharing_share())
        {
			projects.getHash().put(this.getXSIType() +"/sharing/share/project", pp.getProject());
        }
		return projects;
	}
}
