package org.nrg.xnat.eventservice.model.xnat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Objects;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.model.XnatInvestigatordataI;
import org.nrg.xdat.om.XnatInvestigatordata;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;

import java.io.Writer;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
@JsonInclude(Include.NON_NULL)
public class Investigator extends XnatModelObject implements XnatInvestigatordataI {
    public Investigator(final XnatInvestigatordataI investigator) {
        try {
            if (investigator != null) {
                id = investigator.getId();
                department = investigator.getDepartment();
                email = investigator.getEmail();
                firstname = investigator.getFirstname();
                lastname = investigator.getLastname();
                institution = investigator.getInstitution();
                phone = investigator.getPhone();
                title = investigator.getTitle();
                label = Stream.of(title, firstname, lastname).filter(StringUtils::isNotBlank).collect(Collectors.joining("_"));
            }
        } catch (Throwable e) {
            log.error("Could not populate Investigator properties.", e);
        }
    }

    public Investigator(final XnatInvestigatordataI investigator, final Collection<String> primaryProjects, final Collection<String> investigatorProjects) {
        this(investigator);
        this.primaryProjects.addAll(primaryProjects);
        this.investigatorProjects.addAll(investigatorProjects);
    }

    public Investigator(final ResultSet resultSet) throws SQLException {
        xnatInvestigatordataId = resultSet.getInt(1);
        id = resultSet.getString(2);
        title = resultSet.getString(3);
        firstname = resultSet.getString(4);
        lastname = resultSet.getString(5);
        institution = resultSet.getString(6);
        department = resultSet.getString(7);
        email = resultSet.getString(8);
        phone = resultSet.getString(9);
        primaryProjects.addAll(getProjectIds(resultSet.getArray(10)));
        investigatorProjects.addAll(getProjectIds(resultSet.getArray(11)));
    }

    @Override
    public String getXSIType() {
        return XnatInvestigatordata.SCHEMA_ELEMENT_NAME;
    }

    @Override
    public String toString() {
        return String.format(FORMAT_TO_STRING, department, email, firstname, lastname, institution, phone, title, id, label, xsiType);
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
        return Objects.equal(department, that.department) &&
               Objects.equal(email, that.email) &&
               Objects.equal(firstname, that.firstname) &&
               Objects.equal(lastname, that.lastname) &&
               Objects.equal(institution, that.institution) &&
               Objects.equal(phone, that.phone) &&
               Objects.equal(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), department, email, firstname, lastname, institution, phone, title);
    }

    @Override
    public XFTItem getXftItem(final UserI userI) {
        return null;
    }

    private static Collection<? extends String> getProjectIds(final Array array) throws SQLException {
        return array == null ? Collections.emptyList() : Arrays.asList((String[]) array.getArray());
    }

    private static final String FORMAT_TO_STRING = "Investigator{department='%s', email='%s', firstname='%s', lastname='%s', institution='%s', phone='%s', title='%s', id='%s', label='%s', xsiType='%s'}";

    private       Integer     xnatInvestigatordataId;
    private       String      department;
    private       String      email;
    private       String      firstname;
    private       String      lastname;
    private       String      institution;
    private       String      phone;
    private       String      title;
    private final Set<String> primaryProjects      = new HashSet<>();
    private final Set<String> investigatorProjects = new HashSet<>();
}
