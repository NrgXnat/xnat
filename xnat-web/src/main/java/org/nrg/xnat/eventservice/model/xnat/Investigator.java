package org.nrg.xnat.eventservice.model.xnat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.om.XnatInvestigatordata;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;

@Slf4j
@JsonInclude(Include.NON_NULL)
public class Investigator extends XnatModelObject {
    private XnatInvestigatordata xnatInvestigatordata;
    private String department;
    private String email;
    private String firstname;
    private String lastname;
    private String institution;
    private String phone;
    private String title;

    public Investigator() {}

    public Investigator(XnatInvestigatordata xnatInvestigatordata) {
        this.xnatInvestigatordata = xnatInvestigatordata;
        populateProperties();
    }

    private void populateProperties() {
        try {
            if (xnatInvestigatordata != null) {
                this.id = xnatInvestigatordata.getId();
                this.department = xnatInvestigatordata.getDepartment();
                this.email = xnatInvestigatordata.getEmail();
                this.firstname = xnatInvestigatordata.getFirstname();
                this.lastname = xnatInvestigatordata.getLastname();
                this.institution = xnatInvestigatordata.getInstitution();
                this.phone = xnatInvestigatordata.getPhone();
                this.title = xnatInvestigatordata.getTitle();
                this.label = title + (Strings.isNullOrEmpty(title) ? "" : "_") + firstname + (Strings.isNullOrEmpty(firstname) ? "" : "_") + lastname;
            }
        }catch (Throwable e){
            log.error("Could not populate Investigator properties.", e.getMessage());
        }
    }

    public String getDepartment() { return department; }

    public void setDepartment(String department) { this.department = department; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getFirstname() { return firstname; }

    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }

    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getInstitution() { return institution; }

    public void setInstitution(String institution) { this.institution = institution; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) {  this.phone = phone; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }


    @Override
    public String toString() {
        return "Investigator{" +
                "department='" + department + '\'' +
                ", email='" + email + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", institution='" + institution + '\'' +
                ", phone='" + phone + '\'' +
                ", title='" + title + '\'' +
                ", id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", xsiType='" + xsiType + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Investigator)) return false;
        if (!super.equals(o)) return false;
        Investigator that = (Investigator) o;
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
    public XFTItem getXftItem(UserI userI) {
        return null;
    }
}
