package org.nrg.dicom.mizer.service.impl;

import com.google.common.collect.Maps;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.values.BooleanValue;
import org.nrg.dicom.mizer.variables.BasicVariable;

import java.util.*;

/**
 * Base implementation of MizerContext
 */
public class MizerContextBase implements MizerContext {

    public final static String REJECT_ENABLE = "reject:isEnabled";
    public final static String REJECT_WARN_REJECTION_DISABLED = "reject:isWarnRejectionEnabled";

    private final Map<String, Object> _context = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

    private long _scriptId;
    private boolean _record = true;

    public MizerContextBase() {
        this(0L, null);
    }

    public MizerContextBase(final Map<String, Object> context) {
        this(0L, context);
    }

    public MizerContextBase(final long scriptId, final Map<String, Object> context) {
        _scriptId = scriptId;
        if (context != null) {
            _context.putAll(context);
        }
        setIgnoreRejection(false);
        setElement( REJECT_WARN_REJECTION_DISABLED, "false");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setElement(String key, Object value) {
        _context.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getElement(String key) {
        return _context.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getElements() {
        return _context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(Properties properties) {
        for (Object key : properties.keySet()) {
            String k = (String) key;
            _context.put((String) key, properties.getProperty(k));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getScriptId() {
        return _scriptId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setScriptId(final long scriptId) {
        _scriptId = scriptId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRecord() {
        return _record;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRecord(final boolean record) {
        _record = record;
    }

    @Override
    public boolean ignoreRejection() {
        return Boolean.parseBoolean(getElement(REJECT_ENABLE).toString());
    }

    @Override
    public void setIgnoreRejection(boolean ignoreRejection) {
        setElement(REJECT_ENABLE, Boolean.valueOf(!ignoreRejection).toString());
    }

    /**
     * Mizer contexts are equal if they have the same properties with the same values.
     *
     * @param object Object under test.
     *
     * @return true if equal
     */
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        MizerContextBase that = (MizerContextBase) object;

        return _context != null ? _context.equals(that._context) : that._context == null;
    }

    @Override
    public int hashCode() {
        return _context != null ? _context.hashCode() : 0;
    }

}
