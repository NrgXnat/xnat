package org.nrg.xft.entities;

import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

/**
 * Represents the basic XFT field exclusion criteria. This simple object includes
 * three unique attributes, exclusion scope, scope target ID, and element pattern.
 * The scope indicates the level at which the exclusion should be applied, e.g.
 * system, project, or something else. The scope target ID is used when the scope
 * indicates that the exclusion is restricted to a particular scope, e.g. to a project
 * or particular data type. Lastly, the element pattern is a regular expression that,
 * when an element names matches that pattern, indicates an element that should be
 * excluded from the final output.
 */
@XmlRootElement
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"scope", "targetId", "pattern"}))
public class XftFieldExclusion extends AbstractHibernateEntity {

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
        StringBuilder buffer = new StringBuilder(_scope.toString());
        buffer.append("[").append(_scope != XftFieldExclusionScope.System ? _targetId : "N/A").append("]");
        buffer.append(": ").append(_pattern);
        return buffer.toString();
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
