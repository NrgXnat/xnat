/*
 * mizer: org.nrg.dicom.mizer.values.MessageFormatValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.mizer.values;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class MessageFormatValue extends AbstractMizerValue {
    public MessageFormatValue(final Value format, final Iterable<? extends Value> values) {
        _format = format;
        _values = Lists.newArrayList(values);

        // Sort out the dependencies
        final Set<Long> tags = Sets.newLinkedHashSet();
        _variables = Sets.newLinkedHashSet();
        for (final Value value : values) {
            tags.addAll(value.getTags());
            _variables.addAll(value.getVariables());
        }
        _tags = ImmutableSortedSet.copyOf(tags);
    }

    public MessageFormatValue(final String format, final Iterable<? extends Value> values) {
        this(new ConstantValue(format), values);
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Long> getTags() { return _tags; }

    /**
     * {@inheritDoc}
     */
    public Set<Variable> getVariables() { return _variables; }

    /**
     * {@inheritDoc}
     */
    public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
        final Object[] values = new Object[_values.size()];
        logger.trace("_format _values: {}", _values);
        for (int i = 0; i < _values.size(); i++) {
            values[i] = _values.get(i).on(dicomObject);
        }
        logger.trace("_format args: {}", values);
        return MessageFormat.format(_format.on(dicomObject), values);
    }

    /**
     * {@inheritDoc}
     */
    public String on(final Map<Integer,String> map) throws ScriptEvaluationException {
        final List<Object> values = new ArrayList<>();
        for (final Value value : _values) {
            values.add(value.on(map));
        }
        return MessageFormat.format(_format.on(map), values.toArray());
    }

    /**
     * {@inheritDoc}
     */
    public void replace(final Variable var) {
        _format.replace(var);
        for (final Value val : _values) {
            val.replace(var);
        }
        if (_variables.contains(var)) {
            _variables.remove(var);
            _variables.add(var);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object o) {
        if (o instanceof MessageFormatValue) {
            final MessageFormatValue other = (MessageFormatValue)o;
            return _format.equals(other._format) && _values.equals(other._values);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return Objects.hashCode(_format, _values);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        final StringBuilder sb = new StringBuilder("_format(\"");
        sb.append(_format).append("\"");
        for (final Value v : _values) {
            sb.append(", ").append(v);
        }
        sb.append(")");
        return sb.toString();
    }

    private static final Logger logger = LoggerFactory.getLogger(MessageFormatValue.class);

    private final Value           _format;
    private final List<Value>     _values;
    private final SortedSet<Long> _tags;
    private final Set<Variable>   _variables;
}
