/*
 * core: org.nrg.xdat.entities.GroupFeature
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

@Auditable
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"feature", "group_id","disabled"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class GroupFeature extends AbstractHibernateEntity {
	public GroupFeature() {
    }

    /**
     * Gets the feature
     * @return A value representing the feature.
     */
    public String getFeature() {
        return _feature;
    }

    /**
     * Sets the feature
     * @param feature    A value representing the feature.
     */
    public void setFeature(final String feature) {
        _feature = feature;
    }

    /**
     * Gets the tag.
     * @return A value representing the tag.
     */
    public String getTag() {
        return _tag;
    }

    /**
     * Sets the tag.
     * @param tag    A value representing the tag.
     */
    public void setTag(final String tag) {
    	_tag = tag;
    }

    /**
     * Gets the group ID, which corresponds to the ID in the xdat_userGroup table.
     * @return A value representing the group ID.
     */
    @Column(name = "group_id")
    public String getGroupId() {
        return _groupId;
    }

    /**
     * Sets the group ID, which corresponds to the ID in the xdat_userGroup table.
     * @param groupId    A value representing the group ID.
     */
    public void setGroupId(final String groupId) {
    	_groupId = groupId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GroupFeature)) {
            return false;
        }

        final GroupFeature groupRole = (GroupFeature) o;

        return !(_groupId != null ? !_groupId.equals(groupRole._groupId) : groupRole._groupId != null) &&
               !(_feature != null ? !_feature.equals(groupRole._feature) : groupRole._feature != null) &&
               !(_tag != null ? !_tag.equals(groupRole._tag) : groupRole._tag != null);
    }

    @Override
    public int hashCode() {
        int result = _feature != null ? _feature.hashCode() : 0;
        result = 31 * result + (_tag != null ? _tag.hashCode() : 0);
        result = 31 * result + (_groupId != null ? _groupId.hashCode() : 0);
        return result;
    }

	@Column(nullable=true)
	public boolean isBlocked(){
		return (_blocked==null)?false:_blocked;
	}
	
	public void setBlocked(Boolean blocked){
		_blocked=blocked;
	}

	@Column(nullable=true)
	public boolean isOnByDefault(){
		return (_onByDefault==null)?false:_onByDefault;
	}
	
	public void setOnByDefault(Boolean onByDefault){
		_onByDefault=onByDefault;
	}

    private String _feature;
    private String _tag;
    private String _groupId;
    private Boolean _blocked;
    private Boolean _onByDefault;
}
