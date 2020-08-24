package org.nrg.framework.ajax;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.ajax.sql.NumericFilter;
import org.nrg.framework.ajax.sql.StringFilter;
import org.nrg.framework.ajax.sql.TimestampFilter;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "backend")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HibernateFilter.class, name = "hibernate"),
        @JsonSubTypes.Type(value = StringFilter.class, name = "sql_string"),
        @JsonSubTypes.Type(value = TimestampFilter.class, name = "sql_datetime"),
        @JsonSubTypes.Type(value = NumericFilter.class, name = "sql_number")
})
public abstract class Filter {
}
