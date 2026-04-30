/*
 * DicomEdit: org.nrg.dcm.edit.Statement
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicomtools.exceptions.AttributeException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

/**
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class Statement {
    private final Constraint c;
    private final Operation op;

    Statement(final Operation a) {
        this(null, a);
    }

    public Statement(final Constraint c, final Operation a) {
        this.op = a;
        this.c = c;
    }

    public SortedSet<Long> getScriptTags() {
        final SortedSet<Long> tags = Sets.newTreeSet();
        if (null != c) {
            tags.addAll(c.getTags());
        }
        tags.addAll(op.getScriptTags());
        return tags;
    }

    public long getTopTag() {
       return getScriptTags().getLast();
    }
    
    public boolean isConstrained() { return null != c; }

    public boolean matchesConstraint(final File file, final DicomObjectI dicomObject) throws ScriptEvaluationException {
        return null == c || c.matches(file, dicomObject);
    }

    public Operation getOperation() { return op; }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" ");
        if (c != null) {
            sb.append(c).append(" => ");
        }
        sb.append(op);    
        return sb.toString();
    }

    /**
     * For all contained Statements, get all Operations that
     * apply to the File f, with corresponding DicomObject o.
     * In many cases (mostly, if we have no constraints) we can get by
     * without o, so o=null is valid.  If o is null, the object is loaded
     * from file if it's needed.
     * @param statements The statements to process.
     * @param file the File containing the object
     * @return List of Operations applicable to the named File
     * @throws IOException When an error occurs reading a stream.
     * @throws ScriptEvaluationException When an error occurs with the script.
     */
    @SuppressWarnings("unused")
    public static List<Operation> getOperations(final Iterable<Statement> statements, final File file) throws IOException, MizerException {
        final List<Operation> ops = Lists.newArrayList();
        for (final Statement s : statements) {
            final Operation op = s.getOperation();
            if (null != op && s.isConstrained()) {
                final DicomObjectI dicomObject = DicomObjectFactory.newInstance(file);
                if (s.matchesConstraint(file, dicomObject)) {
                    ops.add(op);
                }
            } else {
                ops.add(op);
            }
        }
        return ops;
    }

    public static List<Action> getActions(final Iterable<Statement> statements, final File f, final DicomObjectI dicomObject)
            throws AttributeException, ScriptEvaluationException {
        final List<Action> actions = Lists.newArrayList();
        for (final Statement s : statements) {
            if (s.matchesConstraint(f, dicomObject)) {
                final Operation operation = s.getOperation();
                if (null != operation) {
                    final Action action = operation.makeAction(dicomObject);
                    if (null != action) {
                        actions.add(action);
                    }
                }
            }
        }
        return actions;     
    }
}
