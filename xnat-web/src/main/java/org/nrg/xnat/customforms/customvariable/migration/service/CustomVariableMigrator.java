package org.nrg.xnat.customforms.customvariable.migration.service;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.om.*;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.customforms.helpers.CustomVariableMigrationHelper;
import org.nrg.xnat.customforms.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class CustomVariableMigrator {

    @Autowired
    public CustomVariableMigrator(final JdbcTemplate template) {
        this.template = template;
    }

    /**
     * All existing legacy custom variable definitions
     * @param user - the user requesting
     * @param filter - filters to return the projects for which the user has Ownership permissions
     * @return List of Field Definitions collated by Projects
     */
    public List<CollatedLegacyCustomVariable> getAllFieldDefinitions(UserI user, boolean filter) {
        CustomVariableMigrationHelper helper = new CustomVariableMigrationHelper(template);
        List<LegacyCustomVariable> allLegacyCustomVariable =  helper.doQuery(QUERY_ALL);

        List<CollatedLegacyCustomVariable> collated = new ArrayList<CollatedLegacyCustomVariable>();
        if (allLegacyCustomVariable == null || allLegacyCustomVariable.isEmpty()) {
            return collated;
        }
        if (!filter) {
            return helper.collate(allLegacyCustomVariable);
        }else {
            List<LegacyCustomVariable> filtered = new ArrayList<LegacyCustomVariable>();
            List<XnatProjectdata> userAccessToProjects =  XnatProjectdata.getAllXnatProjectdatas(user, false);
            List<String> userIsOwnerOfProjects = new ArrayList<String>();
            for (XnatProjectdata p: userAccessToProjects) {
                if (Permissions.isProjectOwner(user, p.getId())) {
                    userIsOwnerOfProjects.add(p.getId());
                }
            }
            for (LegacyCustomVariable l : allLegacyCustomVariable) {
                if (l.getProject_specific() == 1) {
                    if (userIsOwnerOfProjects.contains(l.getProjectId())) {
                        filtered.add(l);
                    }
                }
            }
            return helper.collate(filtered);
        }
    }

    private final String QUERY_ALL="select f.xnat_fielddefinitiongroup_id,  f.id, f.description, f.shareable, f.project_specific, df.xnat_datatypeprotocol_fieldgroups_id as dpf_id, a.data_type,  a.xnat_projectdata_id from xnat_projectdata p"
            +  " inner join xnat_abstractprotocol a on a.xnat_projectdata_id=p.id"
            + " left join xnat_datatypeprotocol d on d.xnat_abstractprotocol_id=a.xnat_abstractprotocol_id"
            + " left  join xnat_datatypeprotocol_fieldgroups df on d.xnat_abstractprotocol_id=df.xnat_datatypeprotocol_xnat_abstractprotocol_id"
            + " left  join xnat_fielddefinitiongroup f on df.xnat_fielddefinitiongroup_xnat_fielddefinitiongroup_id = f.xnat_fielddefinitiongroup_id"
            + " where f.id != 'default'   group by a.xnat_projectdata_id, a.data_type, f.id, f.description, f.xnat_fielddefinitiongroup_id, df.xnat_datatypeprotocol_fieldgroups_id";

    private final JdbcTemplate template;

}
