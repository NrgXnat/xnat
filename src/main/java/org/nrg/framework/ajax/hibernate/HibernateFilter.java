package org.nrg.framework.ajax.hibernate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;
import org.nrg.framework.ajax.Filter;
import org.nrg.framework.ajax.sql.NumericFilter;
import org.nrg.framework.ajax.sql.StringFilter;
import org.nrg.framework.ajax.sql.TimestampFilter;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;

import java.util.Arrays;

/**
 * Provides filtering for {@link HibernatePaginatedRequest Hibernate-based paginated requests}. This is the <i>only</i>
 * filter you should use when filtering Hibernate services and DAOs that extend {@link AbstractHibernateDAO}. The other
 * core implementations–{@link NumericFilter}, {@link StringFilter}, and {@link TimestampFilter}–should only be used for
 * handling pure SQL queries.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude
public class HibernateFilter extends Filter {
    private boolean not;
    private Object value;
    private Object[] values;
    private Object lo;
    private Object hi;
    private Operator operator;

    @JsonIgnore
    public Criterion makeCriterion(String property, ClassMetadata classMetadata) {
        Criterion c;
        switch (operator) {
            case EQ:
            case NE:
            case LT:
            case GT:
            case LE:
            case GE:
                value = handleLongType(value, classMetadata.getPropertyType(property));
                break;
            case IN:
                values = Arrays.stream(values).map(value1 -> handleLongType(value1, classMetadata.getPropertyType(property))).toArray();
            case BETWEEN:
                lo = handleLongType(lo, classMetadata.getPropertyType(property));
                hi = handleLongType(hi, classMetadata.getPropertyType(property));
                break;
            case ILIKE:
            case LIKE:
            default:
                if (!(value instanceof String)) {
                    throw new RuntimeException("You must pass a string to like or ilike");
                }
                break;
        }
        switch (operator) {
            case EQ:
                c = Restrictions.eq(property, value);
                break;
            case NE:
                c = Restrictions.ne(property, value);
                break;
            case LT:
                c = Restrictions.lt(property, value);
                break;
            case GT:
                c = Restrictions.gt(property, value);
                break;
            case LE:
                c = Restrictions.le(property, value);
                break;
            case GE:
                c = Restrictions.ge(property, value);
                break;
            case IN:
                c = Restrictions.in(property, values);
                break;
            case BETWEEN:
                c = Restrictions.between(property, lo, hi);
                break;
            case ILIKE:
                c = Restrictions.like(property, (String) value, MatchMode.ANYWHERE);
                break;
            case LIKE:
            default:
                c = Restrictions.ilike(property, (String) value, MatchMode.ANYWHERE);
                break;
        }
        return not ? Restrictions.not(c) : c;
    }

    /**
     * Jackson deserializes numbers to Integer unless they are >32b, but Hibernate needs Longs
     * @param v the deserialized value
     * @param type the column type
     * @return the value with proper type
     */
    private Object handleLongType(Object v, Type type) {
        if (v instanceof Number && type instanceof LongType) {
            v = ((Number) v).longValue();
        }
        return v;
    }

    public enum Operator {
        EQ("eq"),
        NE("ne"),
        LT("lt"),
        GT("gt"),
        LE("le"),
        GE("ge"),
        LIKE("like"),
        ILIKE("ilike"),
        IN("in"),
        BETWEEN("between");

        String op;
        Operator(String op) {
            this.op = op;
        }

        @SuppressWarnings({"unused", "RedundantSuppression"})
        @JsonValue
        public String getOp() {
            return op;
        }
    }
}
