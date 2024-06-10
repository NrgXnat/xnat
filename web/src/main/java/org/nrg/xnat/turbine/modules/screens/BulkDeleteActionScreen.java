/*
 * web: org.nrg.xnat.turbine.modules.screens.BulkDeleteActionScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

public class BulkDeleteActionScreen extends SecureScreen {

    // Enumeration to determine what type of item is stored in an ItemContainer object. 
    private enum ITEM_TYPE { SUBJECT, SUBJ_ASSESSOR, IMG_ASSESSOR };

    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {

        //retrieve passed search object
        DisplaySearch search = TurbineUtils.getSearch(data);
        search.setPagingOn(false);

        //Load search results into a table
        XFTTable table = (org.nrg.xft.XFTTable)search.execute(null,TurbineUtils.getUser(data).getLogin());

        XFTTable experiments = null;
        UserI user = TurbineUtils.getUser(data);

        // Build the query to retrieve information about the items we want to delete.
        StringBuilder query = new StringBuilder();
        query.append("SELECT subj.id AS subj_id, subj.label AS subj_label, subj.project AS subj_project, session_expt.id AS session_id, session_expt.label AS session_label, session_expt.project AS session_project, session_expt.date AS session_date, session_meta.element_name AS session_xsi, expt.id AS assess_id, expt.label AS assess_label, expt.project AS assess_project, expt.date AS assess_date, meta.element_name AS assess_xsi FROM xnat_imageassessordata iad  FULL JOIN xnat_experimentData expt ON iad.id=expt.id  FULL JOIN xdat_meta_element meta ON expt.extension=meta.xdat_meta_element_id FULL JOIN xnat_subjectAssessorData session_sad ON iad.imagesession_id=session_sad.id FULL JOIN xnat_experimentData session_expt ON session_sad.id=session_expt.id FULL JOIN xdat_meta_element session_meta ON session_expt.extension=session_meta.xdat_meta_element_id FULL JOIN xnat_subjectData subj ON session_sad.subject_id=subj.id ");

        // Build the WHERE Clause based on what type of query we are performing. (Subject, Subject Assessor, or Image Assessor)
        // Also, set the searchType, we need this in the velocity to determine if a user is allowed to delete an item. (see ItemContainer.canDelete() below)
        if (search.getRootElement().getFullXMLName().equals("xnat:subjectData")){
            context.put("searchType", "subject");
            query.append("WHERE subj.id IN ('")
                    .append(buildWhereClause(table.convertColumnToArrayList("subjectid")))
                    .append("');");
            // XNAT-2829: If the search was for imaging sessions
        }else if(search.getRootElement().getGenericXFTElement().instanceOf("xnat:imageSessionData")){
            context.put("searchType", "subject_assessor"); // Set the searchType
            query.append("WHERE session_expt.id IN ('")
                    .append(buildWhereClause(table.convertColumnToArrayList("session_id")))
                    .append("');");
            // XNAT-2829: If the search was for subject assessors (non imaging)
        }else if(search.getRootElement().getGenericXFTElement().instanceOf("xnat:subjectAssessorData")){
            context.put("searchType", "subject_assessor"); // Set the searchType
            query.append("WHERE session_expt.id IN ('")
                    .append(buildWhereClause(table.convertColumnToArrayList("expt_id")))
                    .append("');");
        }else if(search.getRootElement().getGenericXFTElement().instanceOf("xnat:imageAssessorData")){
            context.put("searchType", "image_assessor"); // Set the searchType
            query.append("WHERE expt.id IN ('")
                    .append(buildWhereClause(table.convertColumnToArrayList("expt_id")))
                    .append("');");
        }

        // Execute the query to get a list of experiments
        experiments = XFTTable.Execute(query.toString(), user.getDBName(), user.getUsername());

        // Insert the items into the context.
        Hashtable<String, ItemContainer> items = getItems(experiments, search);
        if(null != items && items.size() != 0){
            context.put("items", items);
        }else{
            context.put("errMsg", "There is nothing to delete.");
        }
        context.put("turbineUtils", TurbineUtils.GetInstance());
    }

    /**
     * Function takes a XFTtable and converts it into a Hashtable of ItemContainers.
     * @param t - XFTTable
     * @param search - DisplaySearch
     * @return Hashtable<String, ItemContainer>
     * @throws Exception - if the Hashtable is null or empty after function executes.
     */
    private Hashtable<String, ItemContainer> getItems(XFTTable t, DisplaySearch search) throws Exception{
        Hashtable<String,ItemContainer> subjects = new Hashtable<String,ItemContainer>();

        // For each experiment in the hashtable.
        for(Hashtable exp : t.rowHashs()){

            String subj_id = (String)exp.get("subj_id");
            if(null != subj_id){
                // Get the container for the subject if it exists.
                ItemContainer subj = subjects.get(subj_id);
                if(null == subj){
                    // If it doesn't exist create it.

                    String xsiType = null; //The xsiType should be null in cases where the bulk delete is not at the
                    // subject level. This way the subjects will only have checkboxes next to them if the bulk delete is
                    // being done at the subject level, which is how this has always worked.
                    if(search.getRootElement().getFullXMLName().equals("xnat:subjectData")){
                        xsiType="xnat:subjectData";
                    }
                    subj = new ItemContainer(ITEM_TYPE.SUBJECT, subj_id,(String)exp.get("subj_project"),(String)exp.get("subj_label"),xsiType);
                }

                String session_id = (String)exp.get("session_id");
                if(null != session_id){

                    // Get a list of subject assessors
                    Hashtable<String,ItemContainer> subjAssessors = subj.getAssessors();
                    ItemContainer experiment = subjAssessors.get(session_id);

                    if(null == experiment){
                        // If the experiment doesn't exist create it.
                        experiment = new ItemContainer(ITEM_TYPE.SUBJ_ASSESSOR, session_id, (String)exp.get("session_label"), (String)exp.get("session_project"), (Date)exp.get("session_date"), (String)exp.get("session_xsi"));
                    }

                    // Get a list of image assessors for the experiment
                    Hashtable<String,ItemContainer> imgAssessors = experiment.getAssessors();
                    String assess_id = (String)exp.get("assess_id");

                    if(assess_id != null && !imgAssessors.contains(assess_id)){
                        //If the assessor doesn't exist, create it, add it to the list of assessors and attach it to the experiment.
                        imgAssessors.put(assess_id, new ItemContainer(ITEM_TYPE.IMG_ASSESSOR, assess_id, (String)exp.get("assess_label"), (String)exp.get("assess_project"), (Date)exp.get("assess_date"), (String)exp.get("assess_xsi")));
                        experiment.addAssessors(imgAssessors);
                    }

                    // Update the list of subject Assessors and attach it to the subject
                    subjAssessors.put(experiment.getId(), experiment);
                    subj.addAssessors(subjAssessors);
                }
                // Add the new ItemContainer  to the Hashtable
                subjects.put(subj.getId(), subj);
            }
        }

        return subjects;
    }

    /**
     * Function builds a comma delimited string of id's to be
     * inserted in the where clause of a sql query.
     * @param ids - a list of ids
     * @return a comma delimited string of id's
     */
    private final String buildWhereClause(ArrayList<String> ids){
        StringBuilder whereClause = new StringBuilder();
        for(Object id : ids){
            if(id.equals(ids.get(ids.size()-1))){
                whereClause.append(id.toString());
            }else{
                whereClause.append(id.toString()).append("','");
            }
        }
        return whereClause.toString();
    }

    /**
     * Class holds information on an item (subject, experiment, image assessors)
     * It has helper methods to make it easier to access the information from the velocity.
     */
    public class ItemContainer{
        private final String id,label, project, xsi;
        private final ITEM_TYPE itemType;
        private final Date date;
        private Hashtable<String,ItemContainer> assessors = new Hashtable<String,ItemContainer>();

        /**
         * Constructor for subject_assessor and image_assessor item types.
         * @param itemType
         * @param id
         * @param label
         * @param project
         * @param date
         * @param xsiType
         */
        public ItemContainer(ITEM_TYPE itemType, String id, String label, String project, Date date, String xsiType){
            this.id       = id;
            this.label    = label;
            this.project  = project;
            this.date     = date;
            this.xsi      = xsiType;
            this.itemType = itemType;
        }

        /**
         *  Constructor for subject item types. XSI type is xnat:subjectData in cases where we want a checkbox to show up for subjects and null otherwise. Date is null.
         * @param itemType
         * @param id
         * @param project
         * @param label
         */
        public ItemContainer(ITEM_TYPE itemType, String id, String project, String label, String xsiType){
            this.id        = id;
            this.project   = project;
            this.label     = label;
            this.itemType  = itemType;
            this.date      = null;
            this.xsi       = xsiType;
        }

        public Hashtable<String,ItemContainer> getAssessors(){ return this.assessors; }

        public void addAssessors(Hashtable<String,ItemContainer> assessors){ this.assessors = assessors; }

        public String getId(){ return this.id; }

        public String getLabel(){ return this.label; }

        public String getProject() { return this.project; }

        public String getDate() { return (this.date == null) ? "" : this.date.toString(); }

        public String getXsiType() { return this.xsi; }

        /**
         * Function will determine if the given user is allowed to delete this item.
         * Also, some items should not be delete based on the search type.
         * (e.g.) If user searches for Image Assessors, they should not be allowed to delete the entire subject.
         * @param u - the UserI
         * @param searchType - subject, subject_assessor, image_assessor (this is set on lines 49, 54, and 59 above)
         * @return
         */
        public String canDelete(UserI u, String searchType){
            try{
                // Is the user allowed to delete this item
                boolean canDelete = Permissions.canAny(u,this.xsi, this.project, "delete");

                // The search type determines which items a user is allowed to delete.
                if(searchType.equals("subject")){
                    // If the user searched for subjects, they should be allowed to delete subjects, subject assessors, and image assessors.
                    return String.valueOf(canDelete);
                }else if(searchType.equals("subject_assessor")){
                    // If the user searched for subject assessors, we should allow them to delete subject assessors and image assessors.
                    return String.valueOf((this.itemType == ITEM_TYPE.SUBJ_ASSESSOR || this.itemType == ITEM_TYPE.IMG_ASSESSOR) && canDelete);
                }else if(searchType.equals("image_assessor")){
                    // If the user searched for image assessors, we should allow them to delete image assessors only.
                    return String.valueOf((this.itemType == ITEM_TYPE.IMG_ASSESSOR) && canDelete);
                }else {
                    // If the search type is anything else, something went wrong so we return false.
                    return "false";
                }
            }catch(Exception e){
                // Something went wrong so return false.
                return "false";
            }
        }
    }
}
