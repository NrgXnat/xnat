/*
 * web: org.nrg.xnat.turbine.modules.screens.ManageProjectAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatStudyprotocol;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

public class ManageProjectAccess  extends SecureReport {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ManageProjectAccess.class);
    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
        XnatProjectdata project = (XnatProjectdata)om;
        UserI user = TurbineUtils.getUser(data);
        try {
            Hashtable protocolDataTypes = new Hashtable();
            protocolDataTypes.put("xnat:subjectData","Subjects");
            for(int i=0;i<project.getStudyprotocol().size();i++){
                XnatStudyprotocol protocol=(XnatStudyprotocol)project.getStudyprotocol().get(i);
                String properName;
                try {
                    properName = SchemaElement.GetElement(protocol.getDataType()).getProperName();
                } catch (XFTInitException e) {
                    logger.error("",e);
                    properName=protocol.getDataType();
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                    properName=protocol.getDataType();
                }
                protocolDataTypes.put(protocol.getDataType(),properName);
            }
            
            XFTTable users = XFTTable.Execute("SELECT login, lastname || ', ' || firstname, email, enabled, xdat_user_id, verified FROM xdat_user ORDER BY lastname || ', ' || firstname;", item.getDBName(), null);
            
            ArrayList userRecords = new ArrayList();
            
            users.resetRowCursor();
            while (users.hasMoreRows())
            {
                Object[] userHash = users.nextRow();
                ArrayList userInfo = new ArrayList();
                userInfo.add((String)userHash[0]);
                userInfo.add((String)userHash[1]);
                userInfo.add((String)userHash[2]);
                userInfo.add((Integer)userHash[3]);
                userInfo.add((Integer)userHash[4]);
                userInfo.add((Integer)userHash[5]);
                userInfo.add((Integer)userHash[6]);
                
                if (!userInfo.get(0).equals(user.getLogin()))
                {
                    String query = "SELECT element_name, create_element, read_element, edit_element, delete_element, active_element, xdat_field_mapping_id FROM xdat_element_access ea LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id=fms.permissions_allow_set_xdat_elem_xdat_element_access_id LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id=fm.xdat_field_mapping_set_xdat_field_mapping_set_id" +
                        " WHERE xdat_user_xdat_user_id=" + userInfo.get(5) + " " +
                        " AND field_value='" + project.getId() + "';";
                    XFTTable permissions = XFTTable.Execute(query, item.getDBName(), null);
                    
                    ArrayList permissionAL = new ArrayList();
                    permissionAL.addAll(permissions.toArrayListOfLists());
                    
                    for (int i=0;i<protocolDataTypes.keySet().size();i++)
                    {
                        boolean matched = false;
                        permissions.resetRowCursor();
                        while (permissions.hasMoreRows())
                        {
                            Object[] row =permissions.nextRow();
                            if (row[0].equals(protocolDataTypes.keySet().toArray()[i]))
                            {
                                matched=true;
                            }
                        }
                        
                        if (!matched){
                            ArrayList newRow = new ArrayList();
                            newRow.add(protocolDataTypes.keySet().toArray()[i]);
                            newRow.add(new Integer(0));
                            newRow.add(new Integer(0));
                            newRow.add(new Integer(0));
                            newRow.add(new Integer(0));
                            newRow.add(new Integer(0));
                            newRow.add("");
                            permissionAL.add(newRow);
                        }
                    }
                    
                    userInfo.add(permissionAL);
                    userRecords.add(userInfo);
                }
            }
            
            context.put("users", userRecords);
            context.put("protocols", protocolDataTypes);
        } catch (SQLException e) {
            logger.error("",e);
        } catch (DBPoolException e) {
            logger.error("",e);
        }
        
    }
}
