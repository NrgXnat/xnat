package org.nrg.xapi.model.xft;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.model.XnatInvestigatordataI;
import org.nrg.xdat.om.XnatInvestigatordata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.investigators.impl.xft.DefaultInvestigatorService;

import java.io.Writer;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Investigator extends BaseModel<XnatInvestigatordata> implements XnatInvestigatordataI {
    public Investigator(final Integer xnatInvestigatordataId, final String title, final String firstname, final String lastname, final String institution, final String department, final String email, final String phone, final Set<String> primaryProjects, final Set<String> projects, final String id, final Date insertDate, final String insertUser) {
        super(id, insertDate, insertUser);
        setXnatInvestigatordataId(xnatInvestigatordataId);
        setTitle(title);
        setFirstname(firstname);
        setLastname(lastname);
        setInstitution(institution);
        setDepartment(department);
        setEmail(email);
        setPhone(phone);
        setPrimaryProjects(primaryProjects);
        setProjects(projects);
    }

    public Investigator(final XnatInvestigatordata investigator) {
        this(investigator.getXnatInvestigatordataId(),
             investigator.getTitle(),
             investigator.getFirstname(),
             investigator.getLastname(),
             investigator.getInstitution(),
             investigator.getDepartment(),
             investigator.getEmail(),
             investigator.getPhone(),
             Collections.emptySet(),
             Collections.emptySet(),
             investigator.getId(),
             investigator.getInsertDate(),
             investigator.getInsertUser() != null ? investigator.getInsertUser().getUsername() : null);
    }

    public Investigator(final XnatInvestigatordata investigator, final Collection<String> primaryProjects, final Collection<String> projects) {
        this(investigator.getXnatInvestigatordataId(),
             investigator.getTitle(),
             investigator.getFirstname(),
             investigator.getLastname(),
             investigator.getInstitution(),
             investigator.getDepartment(),
             investigator.getEmail(),
             investigator.getPhone(),
             new HashSet<>(primaryProjects),
             new HashSet<>(projects),
             investigator.getId(),
             investigator.getInsertDate(),
             investigator.getInsertUser() != null ? investigator.getInsertUser().getUsername() : null);
    }

    public Investigator(final ResultSet resultSet) throws SQLException {
        this(resultSet.getInt(DefaultInvestigatorService.COL_XNAT_INVESTIGATORDATA_ID),
             resultSet.getString(DefaultInvestigatorService.COL_TITLE),
             resultSet.getString(DefaultInvestigatorService.COL_FIRSTNAME),
             resultSet.getString(DefaultInvestigatorService.COL_LASTNAME),
             resultSet.getString(DefaultInvestigatorService.COL_INSTITUTION),
             resultSet.getString(DefaultInvestigatorService.COL_DEPARTMENT),
             resultSet.getString(DefaultInvestigatorService.COL_EMAIL),
             resultSet.getString(DefaultInvestigatorService.COL_PHONE),
             getProjectIds(resultSet.getArray(DefaultInvestigatorService.COL_PRIMARY_PROJECTS)),
             getProjectIds(resultSet.getArray(DefaultInvestigatorService.COL_PROJECTS)),
             resultSet.getString(DefaultInvestigatorService.COL_ID),
             resultSet.getDate(DefaultInvestigatorService.COL_INSERT_DATE),
             resultSet.getString(DefaultInvestigatorService.COL_INSERT_USERNAME));
    }

    @Override
    public XnatInvestigatordata toXftItem() {
        return toXftItem(null);
    }

    @Override
    public XnatInvestigatordata toXftItem(final UserI user) {
        final XnatInvestigatordata investigator;
        if (xnatInvestigatordataId != null) {
            investigator = XnatInvestigatordata.getXnatInvestigatordatasByXnatInvestigatordataId(xnatInvestigatordataId, user, false);
        } else if (StringUtils.isNotBlank(getId())) {
            final List<XnatInvestigatordata> investigators = XnatInvestigatordata.getXnatInvestigatordatasByField("ID", getId(), user, false);
            if (investigators == null || investigators.isEmpty()) {
                investigator = new XnatInvestigatordata();
                investigator.setId(getId());
            } else {
                investigator = investigators.get(0);
            }
        } else {
            investigator = new XnatInvestigatordata();
        }

        investigator.setTitle(title);
        investigator.setFirstname(firstname);
        investigator.setLastname(lastname);
        investigator.setInstitution(institution);
        investigator.setDepartment(department);
        investigator.setEmail(email);
        investigator.setPhone(phone);

        return null;
    }

    @Override
    public String getXSIType() {
        return XnatInvestigatordata.SCHEMA_ELEMENT_NAME;
    }

    @Override
    public String toString() {
        return String.format(FORMAT_TO_STRING, title, firstname, lastname, institution, department, email, phone, getId(), xnatInvestigatordataId);
    }

    @Override
    public void toXML(final Writer writer) {
        //
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Investigator) || !super.equals(object)) {
            return false;
        }
        final Investigator that = (Investigator) object;
        // If xnat_investigatordata_id or ID is non-null for either one of the objects, just compare those.
        if (!ObjectUtils.allNull(getXnatInvestigatordataId(), that.getXnatInvestigatordataId())) {
            return Objects.equal(getXnatInvestigatordataId(), that.getXnatInvestigatordataId());
        }
        if (!StringUtils.isAllBlank(getId(), that.getId())) {
            return StringUtils.equals(getId(), that.getId());
        }

        // At this point, there is no persistent identity indicator, so just compare by value.
        return Objects.equal(title, that.title) &&
               Objects.equal(firstname, that.firstname) &&
               Objects.equal(lastname, that.lastname) &&
               Objects.equal(institution, that.institution) &&
               Objects.equal(department, that.department) &&
               Objects.equal(email, that.email) &&
               Objects.equal(phone, that.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), title, firstname, lastname, institution, department, email, phone);
    }

    private static Set<String> getProjectIds(final Array array) throws SQLException {
        return array == null ? Collections.emptySet() : new HashSet<>(Arrays.asList((String[]) array.getArray()));
    }

    private static final String FORMAT_TO_STRING = "%s %s %s, %s: %s, email='%s', phone='%s', id='%s', xnat_investigatordata_id=%d";

    private Integer     xnatInvestigatordataId;
    private String      id;
    private String      title;
    private String      firstname;
    private String      lastname;
    private String      institution;
    private String      department;
    private String      email;
    private String      phone;
    @Singular
    private Set<String> primaryProjects;
    @Singular
    private Set<String> projects;
}
