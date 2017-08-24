/*
 * core: org.nrg.xdat.entities.UserChangeRequest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.entities;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

import javax.persistence.*;

@Auditable
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"username", "fieldToChange", "disabled"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class UserChangeRequest extends AbstractHibernateEntity{

    private String  _username;
	private String  _changeId;
	private String  _fieldToChange;
	private String  _newValue;
	private String  _guid;

	public UserChangeRequest() {
	}

	public UserChangeRequest(String user, String fieldToChange, String newValue, String guid) {
		_username = user;
		_fieldToChange = fieldToChange;
		_newValue = newValue;
		_guid = guid;
	}

	public String getUsername() {
		return _username;
	}

	public void setUsername(String username) {
		_username = username;
	}

	public String getChangeId() {
		return _changeId;
	}

	public void setChangeId(String changeId) {
		_changeId = changeId;
	}

	public String getFieldToChange() {
		return _fieldToChange;
	}

	public void setFieldToChange(String fieldToChange) {
		_fieldToChange = fieldToChange;
	}

	public String getNewValue() {
		return _newValue;
	}

	public void setNewValue(String newValue) {
		_newValue = newValue;
	}

	public String getGuid() {
		return _guid;
	}

	public void setGuid(String guid) {
		_guid = guid;
	}

    public boolean equals(Object object) {
        if (object == null || !(object instanceof UserChangeRequest)) {
            return false;
        }
        UserChangeRequest other = (UserChangeRequest) object;
        return           StringUtils.equals(getChangeId(), other.getChangeId()) &&
                         StringUtils.equals(getNewValue(), other.getNewValue()) &&
                         StringUtils.equals(getUsername(), other.getUsername()) &&
                         StringUtils.equals(getGuid(), other.getGuid());
    }

}
