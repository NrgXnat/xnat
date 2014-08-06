package org.nrg.xdat.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;
import org.nrg.xdat.security.helpers.FeatureDefinitionI;

@Auditable
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"key","disabled"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class FeatureDefinition extends AbstractHibernateEntity implements FeatureDefinitionI{

	@Override
	public String getKey() {
		return _key;
	}

	@Override
	@Column(nullable=true)
	public String getDescription() {
		return _description;
	}

	@Column(nullable=true)
	public boolean isBanned() {
		return (_banned==null)?false:_banned;
	}

	@Column(nullable=true)
	public boolean isOnByDefault() {
		return (_onByDefault==null)?false:_onByDefault;
	}

	@Override
	public String getName() {
		return _name;
	}

	public void setName(String _name) {
		this._name = _name;
	}

	public void setKey(String _key) {
		this._key = _key;
	}

	public void setDescription(String _description) {
		this._description = _description;
	}

	public void setBanned(Boolean _banned) {
		this._banned = _banned;
	}

	public void setOnByDefault(Boolean def) {
		this._onByDefault = def;
	}

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FeatureDefinition)) {
            return false;
        }

        final FeatureDefinition roleDef = (FeatureDefinition) o;

        return !(_name != null ? !_name.equals(roleDef._name) : roleDef._name != null) &&
               !(_key != null ? !_key.equals(roleDef._key) : roleDef._key != null) &&
               !(_description != null ? !_description.equals(roleDef._description) : roleDef._description != null);
    }

    @Override
    public int hashCode() {
        int result = _name != null ? _name.hashCode() : 0;
        result = 31 * result + (_description != null ? _description.hashCode() : 0);
        result = 31 * result + (_key != null ? _key.hashCode() : 0);
        return result;
    }

	
	private String _name;
	private String _key;
	private String _description;
	private Boolean _banned;
	private Boolean _onByDefault;
}
