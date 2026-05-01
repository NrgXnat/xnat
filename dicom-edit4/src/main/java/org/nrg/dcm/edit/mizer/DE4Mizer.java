package org.nrg.dcm.edit.mizer;

import lombok.extern.slf4j.Slf4j;
import org.nrg.dcm.edit.ScriptApplicator;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.exceptions.ScriptVariableMismatchException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.AnonymizationResultSuccess;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.VersionString;
import org.nrg.dicom.mizer.service.impl.AbstractMizer;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.tags.TagPublic;
import org.nrg.dicom.mizer.variables.Variable;
import org.nrg.dicomtools.exceptions.AttributeException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handle anonymization with DicomEdit v4 scripts.
 */
@Component
@Slf4j
public class DE4Mizer extends AbstractMizer {
    public DE4Mizer() {
        super(Collections.singletonList(new VersionString("4.0")));
        _contextMap = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void aggregate(final MizerContext context, final Set<Variable> variables) {
        getReferencedVariables(context, variables);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<TagPath> getScriptTags(final List<MizerContext> contexts) {
        return new HashSet<TagPath>() {{
            for (final MizerContext context : contexts) {
                addAll(getScriptTags(context));
            }
        }};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<TagPath> getScriptTags(final MizerContext context) {
        if (context instanceof MizerContextWithScript) {
            final MizerContextWithScript scriptContext = (MizerContextWithScript) context;
            try (final InputStream is = scriptContext.getScriptInputStream()) {
                final ScriptApplicator sa = new ScriptApplicator(is, context.getElements());
                return sa.getScriptTags().stream().mapToInt(Math::toIntExact).mapToObj(TagPublic::new).map(TagPath::new).collect(Collectors.toSet());
            } catch (IOException e) {
                log.info("An error occurred reading or creating the submitted script object", e);
            } catch (ScriptEvaluationException e) {
                log.info("There was an error in the submitted script", e);
            }
        }
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getReferencedVariables(final MizerContext context, final Set<Variable> variables) {
        if (!(context instanceof MizerContextWithScript)) {
            return;
        }

        final MizerContextWithScript scriptContext = (MizerContextWithScript) context;

        try (final InputStream input = scriptContext.getScriptInputStream()) {
            final ScriptApplicator applicator = new ScriptApplicator(input, context.getElements());
            for (final Variable variable : variables) {
                applicator.unify(variable);
            }
            variables.addAll(applicator.getVariables().values());
            context.setElement("variables", new LinkedHashSet<>(variables));
        } catch (IOException e) {
            log.info("An error occurred reading or creating the submitted script object", e);
        } catch (ScriptEvaluationException e) {
            log.info("There was an error in the submitted script", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AnonymizationResult anonymizeImpl(final DicomObjectI dicomObject, final MizerContextWithScript context) throws MizerException {
        DicomObjectI processedDicomObject = null;
        try {
            final ScriptApplicator applicator = getScriptApplicator(context);

            //noinspection unchecked
            final Set<Variable> unified = (Set<Variable>) context.getElement("variables");
            if (unified != null) {
                for (final Variable variable : unified) {
                    applicator.unify(variable);
                }
            }
            final Map<String, Variable> variables = applicator.getVariables();
            for (final String name : variables.keySet()) {
                final Variable variable = variables.get(name);
                if (variable.getInitialValue() == null) {
                    final String value = (String) context.getElement(name);
                    if (value == null) {
                        throw new ScriptVariableMismatchException("Missing value for script-required variable(s)", Collections.singletonList(name));
                    }
                    variable.setValue(value);
                }
            }
            processedDicomObject = applicator.apply(null, dicomObject);
            return new AnonymizationResultSuccess(processedDicomObject);

        } catch (ScriptEvaluationException | AttributeException e) {
            return new AnonymizationResultError(processedDicomObject, e.getMessage());
        }
    }

    private ScriptApplicator createScriptApplicator(MizerContextWithScript context) throws MizerException {
        try (InputStream is = context.getScriptInputStream()) {
            return new ScriptApplicator(is, context.getElements());
        } catch (IOException | ScriptEvaluationException e) {
            throw new MizerException(e);
        }
    }

    @Override
    public void setContext(MizerContextWithScript context) throws MizerException {
        if (!_contextMap.containsKey(context)) {
            _contextMap.put(context, createScriptApplicator(context));
        }
    }

    @Override
    public void removeContext(MizerContextWithScript context) {
        _contextMap.remove(context);
    }

    private ScriptApplicator getScriptApplicator(MizerContextWithScript context) throws MizerException {
        return (_contextMap.containsKey(context)) ? _contextMap.get(context) : createScriptApplicator(context);
    }

    @Override
    protected String getMeaning() {
        return "XNAT DicomEdit 4 Script";
    }

    @Override
    protected String getSchemeDesignator() {
        return "XNAT";
    }

    @Override
    protected String getSchemeVersion() {
        return "1.0";
    }

    private final Map<MizerContextWithScript, ScriptApplicator> _contextMap;
}
