/*
 * org.nrg.xft.entities.XftFieldExclusion
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 9/4/13 4:37 PM
 */
package org.nrg.xft.entities;

import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

@XmlRootElement
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"scope", "targetId", "pattern"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class XftFieldExclusion extends AbstractHibernateEntity {

    private static final long serialVersionUID = 3198994247860829365L;

    public void setScope(XftFieldExclusionScope scope) {
        _scope = scope;
    }
    public XftFieldExclusionScope getScope() {
        return _scope;
    }
    public void setTargetId(String targetId) {
        _targetId = targetId;
    }
    public String getTargetId() {
        return _targetId;
    }
    public void setPattern(String pattern) {
        _pattern = Pattern.compile(pattern);
    }
    public String getPattern() {
        if (_pattern == null) {
            return null;
        }
        return _pattern.pattern();
    }
    
    @Transient
    public boolean matches(String candidate) {
        return _pattern.matcher(candidate).matches();
    }

    @Override
    public String toString() {
        return _scope.toString() + "[" + (_scope != XftFieldExclusionScope.System ? _targetId : "N/A") + "]" + ": " + _pattern;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof XftFieldExclusion)) {
            return false;
        }
        XftFieldExclusion other = (XftFieldExclusion) object;
        return _scope == other.getScope() && 
                         StringUtils.equals(getTargetId(), other.getTargetId()) &&
                         StringUtils.equals(getPattern(), other.getPattern());
    }

    private XftFieldExclusionScope _scope = XftFieldExclusionScope.Default;
    private String _targetId;
    private Pattern _pattern;
}
