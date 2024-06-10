/*
 * web: org.nrg.xnat.turbine.modules.screens.ExampleListingActionScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.util.Hashtable;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.search.QueryOrganizer;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.security.UserI;

public class ExampleListingActionScreen extends SecureScreen {

    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        
        //retrieve passed search object
        DisplaySearch search = TurbineUtils.getSearch(data);
        search.setPagingOn(false);
        //Load search results into a table
        org.nrg.xft.XFTTable table = (org.nrg.xft.XFTTable)search.execute(null,TurbineUtils.getUser(data).getLogin());
        search.setPagingOn(true);
        
        UserI user = TurbineUtils.getUser(data);
        if (user == null)
        {
            throw new Exception("Invalid User.");
        }

        
        //The 'session_id' value is specified as the DisplayField ID for the xnat:mrSessionData/ID field in the Display docs.
        //This value should match the value at the header of the session id column in the previous ExampleListingActionScreen implementation.
        String sessionIDHeader ="session_id";

        //Build a comma-delimited list from the passed search table to use in our WHERE clause.
        StringBuffer session_ids = new StringBuffer("(");
        int counter = 0;
        table.resetRowCursor();
        while (table.hasMoreRows()){
            Hashtable row= table.nextRowHash();
            if (counter++>0)
            {
                session_ids.append(",");
            }
            session_ids.append("'" + row.get(sessionIDHeader)  + "'");
        }
        
        session_ids.append(")");
        
        //Set key fields- You can change these
        String idXMLField= "xnat:mrScanData/id";
        String typeXMLField = "xnat:mrScanData/type";
        String sessionIDXMLField = "xnat:mrScanData/image_session_id";
        
        //Initialize Query builder, with the root object we are searching for.  
        QueryOrganizer qo = new QueryOrganizer("xnat:mrScanData",user,ViewManager.ALL);
        qo.addField(idXMLField);
        qo.addField(sessionIDXMLField);
        qo.addField(typeXMLField);
        
        //Build search criteria
        CriteriaCollection cc =new CriteriaCollection("AND");
        SearchCriteria sc = new SearchCriteria();
        sc.setField_name("xnat:mrScanData/image_session_id");
        sc.setComparison_type(" IN ");
        sc.setValue(session_ids);
        sc.setOverrideFormatting(true);
        cc.addClause(sc);
        
        //Build SQL query
        String query = "SELECT * FROM (" + qo.buildQuery() + ") SEARCH";
        query += " WHERE " + cc.getSQLClause(qo);
        query += ";";
        
        XFTTable scans =  XFTTable.Execute(query,SchemaElement.GetElement("xnat:mrScanData").getDbName(),user.getLogin());
          
        
        //HERE you will probably iterate through the rows in this table, reorganize them as need be, and then pass them to whatever is generating your image.
//        String scan_idHeader= qo.translateXMLPath(idXMLField);
//        String typeHeader= qo.translateXMLPath(typeXMLField);
//        String sessionIDHeader= qo.translateXMLPath(sessionIDXMLField);
//        
//        scans.resetRowCursor();
//        while (scans.hasMoreRows()){
//            Hashtable row= scans.nextRowHash();
//            
//            //retrieve the values from the table
//            Object scan_idValue = row.get(scan_idHeader);
//            Object typeValue = row.get(typeHeader);
//            Object session_idValue = row.get(sessionIDHeader);
//            
//            // Do something with these values.
//        }
        
        //for the example, we will just pass this new table to the VM template, as we previously passed the search.
        context.put("table", scans);
    }

}
