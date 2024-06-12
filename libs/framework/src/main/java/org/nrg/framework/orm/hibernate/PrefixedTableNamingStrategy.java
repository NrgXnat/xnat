/*
 * framework: org.nrg.framework.orm.hibernate.PrefixedTableNamingStrategy
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate;

import org.hibernate.cfg.ImprovedNamingStrategy;

public class PrefixedTableNamingStrategy extends ImprovedNamingStrategy {
    public PrefixedTableNamingStrategy() {
        //
    }

    public PrefixedTableNamingStrategy(final String prefix) {
        setPrefix(prefix);
    }

    /**
     * Sets the prefix to be used for naming tables. If the prefix doesn't end with the
     * underscore character, it's automatically added to the end. Thus setting the prefix
     * to "this" or "this_" results in tables being named "this_table".
     * @param prefix Sets the prefix property.
     */
    public void setPrefix(String prefix) {
        if (!prefix.endsWith("_")) {
            _prefix = prefix + "_";
        } else {
            _prefix = prefix;
        }
    }
    /**
     * @return Returns the prefix property.
     */
    public String getPrefix() {
        return _prefix;
    }

    /**
     * Return the unqualified class name, mixed case converted to
     * underscores, with prefix prepended.
     */
    @Override
    public String classToTableName(String className) {
        return prefixIfNeeded(super.classToTableName(className));
    }

    /**
     * Convert mixed case to underscores
     */
    @Override
    public String tableName(String tableName) {
        return prefixIfNeeded(super.tableName(tableName));
    }

    @Override
    public String collectionTableName(String ownerEntity, String ownerEntityTable, String associatedEntity, String associatedEntityTable, String propertyName) {
        return prefixIfNeeded(super.collectionTableName(ownerEntity, ownerEntityTable, associatedEntity, associatedEntityTable, propertyName));
    }

    /**
     * Returns either the table name if explicit or if there is an associated table, the 
     * concatenation of owner entity table and associated table, otherwise the concatenation
     * of owner entity table and the unqualified property name.
     */
    public String logicalCollectionTableName(String tableName, String ownerEntityTable, String associatedEntityTable, String propertyName ) {
        return prefixIfNeeded(super.logicalCollectionTableName(tableName, ownerEntityTable, associatedEntityTable, propertyName));
    }

    /**
     * Checks whether the submitted name is already prefixed with the {@link #getPrefix() prefix}.
     * If so, the name is returned as-is. If not, the name is prefixed and returned.
     * @param name The name to be prefixed.
     * @return The prefixed name.
     */
    private String prefixIfNeeded(String name) {
        if (_prefix == null || _prefix.length() == 0) {
            throw new RuntimeException("You must specify a prefix to use this naming strategy!");
        }
        if (name.startsWith(_prefix)) {
            return name;
        }
        return _prefix + name;
    }

    private static final long serialVersionUID = -472772844764070830L;
    private String _prefix;
}
