/*
 * core: org.nrg.xft.entities.XftFieldExclusion
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.entities;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.regex.Pattern;

@XmlRootElement
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"scope", "targetId", "pattern"}))
@Cacheable
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
        return _pattern == null ? null : _pattern.pattern();
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
        if (!super.equals(object)) {
            return false;
        }
        XftFieldExclusion other = (XftFieldExclusion) object;
        return _scope == other.getScope() &&
               StringUtils.equals(getTargetId(), other.getTargetId()) &&
               StringUtils.equals(getPattern(), other.getPattern());
    }

    private XftFieldExclusionScope _scope = XftFieldExclusionScope.Default;
    private String                 _targetId;
    private Pattern                _pattern;
}
