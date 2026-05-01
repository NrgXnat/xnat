package org.nrg.dicom.dicomedit.mizer;

import org.nrg.dicom.dicomedit.DE6Script;
import org.nrg.dicom.dicomedit.BaseScriptApplicator;
import org.nrg.dicom.mizer.exceptions.MizerContextException;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.*;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.VersionString;
import org.nrg.dicom.mizer.service.impl.AbstractMizer;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handle anonymization with DicomEdit v6 contexts.
 *
 * The Mizer is injected with a ScriptApplicatorFactory.
 *
 */
@Component
public class DE6Mizer extends AbstractMizer {

    // keep in sync with VersionManager.
    private static final List<VersionString> supportedVersions = Stream.of("6.0", "6.1", "6.2", "6.3", "6.4", "6.5", "6.6", "6.7").map(VersionString::new).collect(Collectors.toList());
    private static final Logger              logger            = LoggerFactory.getLogger(DE6Mizer.class);

    private final Map<MizerContextWithScript, BaseScriptApplicator> _contextMap;

    @Autowired
    public DE6Mizer() {
        super(supportedVersions);
        _contextMap = new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void aggregate(final MizerContext context, final Set<Variable> variables) {
        for (final Variable variable : variables) {
            context.setElement(variable.getName(), variable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<TagPath> getScriptTags(final List<MizerContext> contexts) {
        return contexts.stream()
                       .map(this::getScriptTags)
                       .flatMap(Collection::stream)
                       .collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<TagPath> getScriptTags(final MizerContext context) {
        final Set<TagPath> tags = new HashSet<>();
        if (context instanceof MizerContextWithScript) {
            final MizerContextWithScript scriptContext = (MizerContextWithScript) context;
            try {
                final DE6Script script = new DE6Script.Builder().statements(scriptContext.getScript()).build();
                tags.addAll(script.getReferencedTags());
            } catch (MizerException e) {
                logger.warn("Error looking for referenced tags in script:\n{}", scriptContext.getScriptAsString(), e);
            }
        }
        return tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getReferencedVariables(final MizerContext context, final Set<Variable> variables) throws MizerContextException {
        if (context instanceof MizerContextWithScript) {
            final MizerContextWithScript scriptContext = (MizerContextWithScript) context;
            try {
                final DE6Script script = new DE6Script.Builder().statements(scriptContext.getScript()).build();
                variables.addAll(script.getVariables().values());
            } catch (MizerException e) {
                logger.warn("Error looking for referenced variables in script:\n{}", scriptContext.getScriptAsString(), e);
                if (e instanceof MizerContextException) {
                    throw (MizerContextException) e;
                }
                throw new MizerContextException(scriptContext, e);
            }
        }
    }

    @Override
    protected AnonymizationResult anonymizeImpl(final DicomObjectI dicomObject, final MizerContextWithScript context) throws MizerException {
        BaseScriptApplicator applicator = getScriptApplicator( context);
        return applicator.apply(dicomObject);
    }

    private BaseScriptApplicator getScriptApplicator(MizerContextWithScript context) throws MizerException {
        return _contextMap.containsKey(context)
               ? _contextMap.get(context)
               : BaseScriptApplicator.getInstance(context);
    }

    @Override
    public void setContext( MizerContextWithScript context) throws MizerException {
        _contextMap.putIfAbsent(context, BaseScriptApplicator.getInstance(context));
    }

    @Override
    public void removeContext( MizerContextWithScript context) {
        _contextMap.remove( context);
    }

    @Override
    protected String getMeaning() {
        return "XNAT DicomEdit 6 Script";
    }

    @Override
    protected String getSchemeDesignator() {
        return "XNAT";
    }

    @Override
    protected String getSchemeVersion() {
        return "1.0";
    }
}
