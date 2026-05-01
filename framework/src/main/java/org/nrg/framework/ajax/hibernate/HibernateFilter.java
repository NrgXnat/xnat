package org.nrg.framework.ajax.hibernate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.InvalidMappingException;
import org.nrg.framework.ajax.Filter;
import org.nrg.framework.ajax.sql.NumericFilter;
import org.nrg.framework.ajax.sql.StringFilter;
import org.nrg.framework.ajax.sql.TimestampFilter;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.orm.hibernate.BaseHibernateEntity;
import org.nrg.framework.utilities.CompareUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Provides filtering for {@link HibernatePaginatedRequest Hibernate-based paginated requests}. This is the <i>only</i>
 * filter you should use when filtering Hibernate services and DAOs that extend {@link AbstractHibernateDAO}. The other
 * core implementations–{@link NumericFilter}, {@link StringFilter}, and {@link TimestampFilter}–should only be used for
 * handling pure SQL queries.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@JsonInclude
public class HibernateFilter extends Filter {
    private static final List<Operator> NUMERIC_OPERATORS = Arrays.asList(Operator.EQ, Operator.NE, Operator.LT, Operator.GT, Operator.LE, Operator.GE, Operator.BETWEEN);

    @Builder.Default
    private       boolean               not        = false;
    @Builder.Default
    private       Object                value      = null;
    @Builder.Default
    private       Object[]              values     = null;
    @Builder.Default
    private       Object                lo         = null;
    @Builder.Default
    private       Object                hi         = null;
    @Builder.Default
    private       Operator              operator   = null;
    @Builder.Default
    private final List<HibernateFilter> andFilters = new ArrayList<>();
    @Builder.Default
    private final List<HibernateFilter> orFilters  = new ArrayList<>();

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(not)
                                    .append(value)
                                    .append(values)
                                    .append(lo)
                                    .append(hi)
                                    .append(operator)
                                    .append(andFilters)
                                    .append(orFilters)
                                    .hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        final HibernateFilter that = (HibernateFilter) object;
        if (CompareUtils.isOneNullAndOneNotNull(getValue(), that.getValue()) ||
            CompareUtils.isOneNullAndOneNotNull(getValues(), that.getValues()) ||
            CompareUtils.isOneNullAndOneNotNull(getLo(), that.getLo()) ||
            CompareUtils.isOneNullAndOneNotNull(getHi(), that.getHi())) {
            return false;
        }
        return new EqualsBuilder().append(isNot(), that.isNot())
                                  .append(getOperator(), that.getOperator())
                                  .append(convertType(getValue()), convertType(that.getValue()))
                                  .append(convertType(getValues()), convertType(that.getValues()))
                                  .append(convertType(getLo()), convertType(that.getLo()))
                                  .append(convertType(getHi()), convertType(that.getHi()))
                                  .append(getAndFilters(), that.getAndFilters())
                                  .append(getOrFilters(), that.getOrFilters())
                                  .isEquals();
    }

    @JsonIgnore
    public <E extends BaseHibernateEntity> Predicate makeCriterion(CriteriaBuilder criteriaBuilder, Root<E> root, String property) {
        Predicate predicate = null;
        if (orFilters != null && !orFilters.isEmpty()) {
            predicate = criteriaBuilder.or(getSubCriteria(orFilters, criteriaBuilder, root, property));
        }
        if (andFilters != null && !andFilters.isEmpty()) {
            Predicate[] subCriteria = getSubCriteria(andFilters, criteriaBuilder, root, property);
            predicate = predicate != null
                        ? criteriaBuilder.and(criteriaBuilder.and(subCriteria), predicate)
                        : criteriaBuilder.and(subCriteria);
        }
        if (predicate != null) {
            return predicate;
        }
        switch (operator) {
            case EQ:
            case NE:
            case LT:
            case GT:
            case LE:
            case GE:
                value = convertType(value);
                break;
            case IN:
                values = Arrays.stream(values).map(this::convertType).toArray();
                break;
            case BETWEEN:
                lo = convertType(lo);
                hi = convertType(hi);
                break;
            case ILIKE:
            case LIKE:
            default:
                if (!(value instanceof String)) {
                    throw new RuntimeException("You must pass a string to like or ilike");
                }
                break;
        }

        Expression<?> expression          = root.get(property);
        Class<?>      javaType            = expression.getJavaType();
        boolean       isTypeLongOrInteger = Long.class.isAssignableFrom(javaType) || Integer.class.isAssignableFrom(javaType);
        boolean       isTypeDoubleOrFloat = Double.class.isAssignableFrom(javaType) || Float.class.isAssignableFrom(javaType);
        boolean       isDate              = Date.class.isAssignableFrom(javaType);

        switch (operator) {
            case EQ:
                predicate = criteriaBuilder.equal(expression, value);
                break;
            case NE:
                predicate = criteriaBuilder.notEqual(expression, value);
                break;
            case LT:
                if (isTypeLongOrInteger) {
                    predicate = criteriaBuilder.lt(expression.as(Long.class), (Long) value);
                } else if (isTypeDoubleOrFloat) {
                    predicate = criteriaBuilder.lt(expression.as(Double.class), (Double) value);
                } else {
                    throw new RuntimeException("Less-than operations are only valid for numeric types but the property " + property + " is of type " + javaType.getName());
                }
                break;
            case GT:
                if (isTypeLongOrInteger) {
                    predicate = criteriaBuilder.gt(expression.as(Long.class), (Long) value);
                } else if (isTypeDoubleOrFloat) {
                    predicate = criteriaBuilder.gt(expression.as(Double.class), (Double) value);
                } else {
                    throw new RuntimeException("Greater-than operations are only valid for numeric types but the property " + property + " is of type " + javaType.getName());
                }
                break;
            case LE:
                if (isTypeLongOrInteger) {
                    predicate = criteriaBuilder.le(expression.as(Long.class), (Long) value);
                } else if (isTypeDoubleOrFloat) {
                    predicate = criteriaBuilder.le(expression.as(Double.class), (Double) value);
                } else {
                    throw new RuntimeException("Less-than-or-equal-to operations are only valid for numeric types but the property " + property + " is of type " + javaType.getName());
                }
                break;
            case GE:
                if (isTypeLongOrInteger) {
                    predicate = criteriaBuilder.ge(expression.as(Long.class), (Long) value);
                } else if (isTypeDoubleOrFloat) {
                    predicate = criteriaBuilder.ge(expression.as(Double.class), (Double) value);
                } else {
                    throw new RuntimeException("Greater-than-or-equal-to operations are only valid for numeric types but the property " + property + " is of type " + javaType.getName());
                }
                break;
            case IN:
                predicate = criteriaBuilder.in(expression);
                for (Object v : values) {
                    ((CriteriaBuilder.In<Object>) predicate).value(v);
                }
                break;
            case BETWEEN:
                if (isTypeLongOrInteger) {
                    predicate = criteriaBuilder.between(expression.as(Long.class), (Long) convertType(lo), (Long) convertType(hi));
                } else if (isTypeDoubleOrFloat) {
                    predicate = criteriaBuilder.between(expression.as(Double.class), (Double) convertType(lo), (Double) convertType(hi));
                } else if (isDate) {
                    predicate = criteriaBuilder.between(expression.as(Date.class), (Date) lo, (Date) hi);
                } else {
                    throw new InvalidMappingException("The property \"" + property + "\"'s type " + javaType.getName() + " does not match the type (" + lo.getClass().getName() + ") of the lower and upper bounds", root.getModel().getName(), property);
                }
                break;
            case LIKE:
                predicate = criteriaBuilder.like(expression.as(String.class), "%"+(String) value+"%");
                break;
            case ILIKE:
            default:
                predicate = criteriaBuilder.like(criteriaBuilder.upper(expression.as(String.class)), "%"+(String) value+"%");
                break;
        }
        return not ? criteriaBuilder.not(predicate) : predicate;
    }

    private <E extends BaseHibernateEntity> Predicate[] getSubCriteria(List<HibernateFilter> filters, CriteriaBuilder builder, Root<E> root, String property) {
        return filters.stream()
                      .map(f -> f.makeCriterion(builder, root, property))
                      .toArray(Predicate[]::new);
    }

    /**
     * Jackson deserializes numbers to Integer unless they are >32b, but Hibernate needs Longs
     *
     * @param value the deserialized value
     *
     * @return the value with proper type
     */
    private Object convertType(Object value) {
        if (value == null) {
            return null;
        }
        if (Integer.class.isAssignableFrom(value.getClass())) {
            return ((Integer) value).longValue();
        }
        if (Float.class.isAssignableFrom(value.getClass())) {
            return ((Float) value).doubleValue();
        }
        return value;
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

        final String op;

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
