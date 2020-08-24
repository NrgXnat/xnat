package org.nrg.framework.ajax.hibernate;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;
import org.nrg.framework.ajax.Filter;
import org.nrg.framework.ajax.sql.SortOrFilterException;

@JsonInclude
public class HibernateFilter extends Filter {
    public boolean not = false;
    public Object value;
    public Object[] values;
    public Object lo;
    public Object hi;
    public Operator operator;

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

    public Object getLo() {
        return lo;
    }

    public void setLo(Object lo) {
        this.lo = lo;
    }

    public Object getHi() {
        return hi;
    }

    public void setHi(Object hi) {
        this.hi = hi;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

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
                Object[] newVals = new Object[values.length];
                for (int i = 0; i < values.length; i++) {
                    newVals[i] = handleLongType(values[i], classMetadata.getPropertyType(property));
                }
                values = newVals;
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
        private Operator(String op) {
            this.op = op;
        }

        @JsonValue
        public String getOp() {
            return op;
        }
    }
}
