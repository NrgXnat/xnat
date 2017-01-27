/*
 * web: org.nrg.xapi.model.investigators.Investigator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model.investigators;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.nrg.xdat.bean.XnatInvestigatordataBean;
import org.nrg.xdat.model.XnatInvestigatordataI;
import org.nrg.xdat.om.XnatInvestigatordata;

import java.io.Writer;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Provides a wrapper around the {@link XnatInvestigatordata}, {@link XnatInvestigatordataI}, and {@link
 * XnatInvestigatordataBean} classes. This omits extraneous properties (e.g. XFT integration attributes) and provides
 * extra attributes for aggregated or synthesized properties, such as lists of projects with which the investigator is
 * associated.
 */
public class Investigator implements XnatInvestigatordataI {
    public Investigator() {
        //
    }

    public Investigator(final XnatInvestigatordataI investigator, final Collection<String> primaryProjects, final Collection<String> investigatorProjects) {
        _xnatInvestigatordataId = investigator.getXnatInvestigatordataId();
        _id = investigator.getId();
        _title = investigator.getTitle();
        _firstname = investigator.getFirstname();
        _lastname = investigator.getLastname();
        _institution = investigator.getInstitution();
        _department = investigator.getDepartment();
        _email = investigator.getEmail();
        _phone = investigator.getPhone();
        _primaryProjects.addAll(primaryProjects);
        _investigatorProjects.addAll(investigatorProjects);
    }

    public Investigator(final ResultSet resultSet) throws SQLException {
        _xnatInvestigatordataId = resultSet.getInt(1);
        _id = resultSet.getString(2);
        _title = resultSet.getString(3);
        _firstname = resultSet.getString(4);
        _lastname = resultSet.getString(5);
        _institution = resultSet.getString(6);
        _department = resultSet.getString(7);
        _email = resultSet.getString(8);
        _phone = resultSet.getString(9);
        _primaryProjects.addAll(getProjectIds(resultSet.getArray(10)));
        _investigatorProjects.addAll(getProjectIds(resultSet.getArray(11)));
    }

    @Override
    @JsonIgnore
    public String getXSIType() {
        return XnatInvestigatordata.SCHEMA_ELEMENT_NAME;
    }

    @Override
    public void toXML(final Writer writer) throws Exception {

    }

    @Override
    public String getTitle() {
        return _title;
    }

    @Override
    public void setTitle(final String title) {
        _title = title;
    }

    @Override
    public String getFirstname() {
        return _firstname;
    }

    @Override
    public void setFirstname(final String firstname) {
        _firstname = firstname;
    }

    @Override
    public String getLastname() {
        return _lastname;
    }

    @Override
    public void setLastname(final String lastname) {
        _lastname = lastname;
    }

    @Override
    public String getInstitution() {
        return _institution;
    }

    @Override
    public void setInstitution(final String institution) {
        _institution = institution;
    }

    @Override
    public String getDepartment() {
        return _department;
    }

    @Override
    public void setDepartment(final String department) {
        _department = department;
    }

    @Override
    public String getEmail() {
        return _email;
    }

    @Override
    public void setEmail(final String email) {
        _email = email;
    }

    @Override
    public String getPhone() {
        return _phone;
    }

    @Override
    public void setPhone(final String phone) {
        _phone = phone;
    }

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public void setId(final String id) {
        _id = id;
    }

    @Override
    public Integer getXnatInvestigatordataId() {
        return _xnatInvestigatordataId;
    }

    @SuppressWarnings("unused")
    public void setXnatInvestigatordataId(final Integer xnatInvestigatordataId) {
        _xnatInvestigatordataId = xnatInvestigatordataId;
    }

    /**
     * Gets the list of IDs for projects on which the investigator is the primary investigator.
     *
     * @return The projects on which the investigator is the primary investigator.
     */
    @SuppressWarnings("unused")
    public Set<String> getPrimaryProjects() {
        return new HashSet<>(_primaryProjects);
    }

    /**
     * Indicates whether the investigator is a PI on the indicated project.
     *
     * @param project The project to test.
     *
     * @return Returns true if the investigator is the PI on the indicated project, false otherwise.
     */
    @SuppressWarnings("unused")
    public boolean hasPrimaryProject(final String project) {
        return _primaryProjects.contains(project);
    }

    /**
     * Gets the list of IDs for projects on which the investigator is the primary investigator.
     *
     * @return The projects on which the investigator is the primary investigator.
     */
    @SuppressWarnings("unused")
    public Set<String> getInvestigatorProjects() {
        return new HashSet<>(_investigatorProjects);
    }

    /**
     * Indicates whether the investigator is a member of the indicated project team. Note that this does not check
     * whether the investigator is the project's PI.
     *
     * @param project The project to test.
     *
     * @return Returns true if the investigator is a member of the indicated project team, false otherwise.
     */
    @SuppressWarnings("unused")
    public boolean hasInvestigatorProject(final String project) {
        return _investigatorProjects.contains(project);
    }

    private static Collection<? extends String> getProjectIds(final Array array) throws SQLException {
        if (array == null) {
            return Collections.emptyList();
        }
        final String[] projectIds = (String[]) array.getArray();
        if (projectIds.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(projectIds);
    }

    private Integer _xnatInvestigatordataId;
    private String _id;
    private String _title;
    private String _firstname;
    private String _lastname;
    private String _institution;
    private String _department;
    private String _email;
    private String _phone;
    private final Set<String> _primaryProjects      = new HashSet<>();
    private final Set<String> _investigatorProjects = new HashSet<>();
}
