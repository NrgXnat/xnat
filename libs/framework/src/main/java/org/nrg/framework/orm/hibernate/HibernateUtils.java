/*
 * framework: org.nrg.framework.orm.hibernate.HibernateUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate;

import com.google.common.collect.ImmutableSet;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HibernateUtils {

    /**
     * This is the default date that basically maps to null for the purpose of identifying
     * {@link Auditable auditable} entities that have <i>not</i> been deleted (or, really,
     * disabled: auditable entities should never actually be deleted from the database).
     * Entities that have been "deleted" will have a {@link BaseHibernateEntity#getDisabled()
     * disabled timestamp} that indicates the date and time the entity was actually disabled.
     */
    public static Date DEFAULT_DATE = new Date(0L);

    /**
     * Tests whether the entity is auditable. Auditable entities are not deleted in delete operations,
     * but instead are disabled by calling the {@link BaseHibernateEntity#setEnabled(boolean)} method
     * with the value <b>false</b>.
     *
     * Classes are by default not auditable. You can declare an entity class to be auditable by adding
     * the {@link Auditable} annotation to the class declaration.
     *
     * @param entity The entity to check for auditability.
     * @param <E>    The type of the entity to be checked.
     *
     * @return Whether the class is auditable or not.
     */
    @SuppressWarnings("unused")
    public static <E> boolean isAuditable(E entity) {
        return isAuditable(entity.getClass());
    }

    /**
     * Tests whether the entity is auditable. Auditable entities are not deleted in delete operations,
     * but instead are disabled by calling the {@link BaseHibernateEntity#setEnabled(boolean)} method
     * with the value <b>false</b>.
     *
     * Classes are by default not auditable. You can declare an entity class to be auditable by adding
     * the {@link Auditable} annotation to the class declaration.
     *
     * @param clazz The class type to check for auditability.
     * @param <E>   The type of the entity to be checked.
     *
     * @return Whether the class is auditable or not.
     */
    public static <E> boolean isAuditable(Class<E> clazz) {
        return clazz.isAnnotationPresent(Auditable.class);
    }

    /**
     * Indicates whether the indicated class type has eagerly fetched collections.
     *
     * @param clazz The class type to check for eagerly fetched collections.
     * @param <E>   The type of the entity to be checked.
     *
     * @return Returns true if the class has eagerly fetched collections, false otherwise.
     */
    public static <E> boolean hasEagerlyFetchedCollection(Class<E> clazz) {
        for (final Method method : clazz.getMethods()) {
            final ManyToMany manyToMany = method.getAnnotation(ManyToMany.class);
            if (manyToMany != null) {
                if (manyToMany.fetch() == FetchType.EAGER) {
                    return true;
                }
            }
            final OneToMany oneToMany = method.getAnnotation(OneToMany.class);
            if (oneToMany != null) {
                if (oneToMany.fetch() == FetchType.EAGER) {
                    return true;
                }
            }
            final ElementCollection elementCollection = method.getAnnotation(ElementCollection.class);
            if (elementCollection != null) {
                if (elementCollection.fetch() == FetchType.EAGER) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks for the existence of each table specified in the <b>tables</b> array. This returns a map with each table
     * name as the key and whether the table was found in the database indicated by the corresponding value.
     *
     * @param dataSource The data source to use to make the database connection.
     * @param tables     The tables to be tested for.
     *
     * @return A map containing each table name and whether the table exists.
     *
     * @throws SQLException If something goes wrong while querying the database.
     */
    public static Map<String, Boolean> checkTablesExist(final DataSource dataSource, final String... tables) throws SQLException {
        final Map<String, Boolean> exists = new HashMap<>(tables.length);
        for (final String table : tables) {
            exists.put(table.toLowerCase(), false);
        }
        final Set<String> names = ImmutableSet.copyOf(exists.keySet());
        try (final Connection connection = dataSource.getConnection();
             final ResultSet results = connection.getMetaData().getTables("catalog", null, "xhbm_%", new String[]{"table"})) {
            while (results.next()) {
                final String tableName = results.getString("table_name").toLowerCase();
                if (names.contains(tableName)) {
                    exists.put(tableName, true);
                }
                // If we've found all of the requested table names, we can just stop.
                if (!exists.containsValue(false)) {
                    return exists;
                }
            }
        }
        return exists;
    }
}
