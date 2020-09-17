package org.nrg.framework.ajax;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.ajax.sql.NumericFilter;
import org.nrg.framework.ajax.sql.StringFilter;
import org.nrg.framework.ajax.sql.TimestampFilter;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;

/**
 * Provides the base class for filtering paginated requests, as well as JSON type mapping to the core implementations of
 * the filter class.
 *
 * Note that, of the four core implementations, you should <i>always</i> use {@link HibernateFilter} when you're working
 * with Hibernate services and DAOs, such as those that extend {@link AbstractHibernateDAO}. The other core
 * implementations–{@link NumericFilter}, {@link StringFilter}, and {@link TimestampFilter}–should only be used for
 * handling pure SQL queries.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "backend")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HibernateFilter.class, name = "hibernate"),
        @JsonSubTypes.Type(value = StringFilter.class, name = "sql_string"),
        @JsonSubTypes.Type(value = TimestampFilter.class, name = "sql_datetime"),
        @JsonSubTypes.Type(value = NumericFilter.class, name = "sql_number")
})
public abstract class Filter {
}
