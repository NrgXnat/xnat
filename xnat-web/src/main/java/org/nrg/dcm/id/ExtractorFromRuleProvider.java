package org.nrg.dcm.id;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.dcm.Extractor;
import org.nrg.dcm.FixedDicomAttributeIndex;
import org.nrg.dcm.MatchedPatternExtractorWithLengthLimit;
import org.nrg.dcm.MatchedPatternExtractorWithReplacement;
import org.nrg.dcm.xnat.AbstractConditionalAttrDef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ExtractorFromRuleProvider
 *
 * This implementation of ExtractorProvider builds Extractors (and Rules) from expressions with syntax
 * conforming to the Custom-Rule-Pattern Regex.
 * The source of the expressions is the injected RoutingExpressionProvider.
 *
 */
@Slf4j
public class ExtractorFromRuleProvider implements ExtractorProvider {
    private final RoutingExpressionProvider _routingExpressionProvider;
    @SuppressWarnings("RegExpRedundantEscape")
    private static final Pattern CUSTOM_RULE_PATTERN =
            Pattern.compile("\\((\\p{XDigit}{4})\\,(\\p{XDigit}{4})\\):(.+?)(?::(\\d+))?(?: t:(.*) r:(.*))?");

    public ExtractorFromRuleProvider( RoutingExpressionProvider routingExpressionProvider) {
        _routingExpressionProvider = routingExpressionProvider;
    }

    @Override
    public List<Extractor> provide( CompositeDicomObjectIdentifier.ExtractorType type) {
        return _routingExpressionProvider.provide( type).stream()
                .filter(StringUtils::isNotEmpty)
                .map( this::parseDicomRuleToExtractor)
                .filter( Objects::nonNull)
                .collect( Collectors.toList());
    }

    @Override
    public List<AbstractConditionalAttrDef.Rule> provideRules(CompositeDicomObjectIdentifier.ExtractorType type) {
        return _routingExpressionProvider.provide( type).stream()
                .filter(StringUtils::isNotEmpty)
                .map(this::parseDicomRuleToRule)
                .filter( Objects::nonNull)
                .collect(Collectors.toList());
    }
    @Nullable
    public Extractor parseDicomRuleToExtractor( final String rule) {
        final DicomPatternRuleMetaData md = parseDicomRule( rule.trim());
        if (md.target != null && md.repl != null) {
            return new MatchedPatternExtractorWithReplacement(md.tag, md.regexp, md.group, md.target, md.repl);
        } else {
            return new MatchedPatternExtractorWithLengthLimit(md.tag, md.regexp, md.group, 64);
        }
    }

    @Nullable
    public AbstractConditionalAttrDef.Rule parseDicomRuleToRule(final String rule) {
        final DicomPatternRuleMetaData md = parseDicomRule(rule);
        if (md.target != null && md.repl != null) {
            return new AbstractConditionalAttrDef.MatchesPatternWithReplacementRule(new FixedDicomAttributeIndex(md.tag),
                    md.regexp, md.group, md.target, md.repl);
        } else {
            return new AbstractConditionalAttrDef.MatchesPatternRule(new FixedDicomAttributeIndex(md.tag),
                    md.regexp, md.group);
        }
    }

    @Nonnull
    public DicomPatternRuleMetaData parseDicomRule(final String expression) {
        final Matcher matcher = CUSTOM_RULE_PATTERN.matcher( expression);
        if (matcher.matches()) {
            final int    tag      = Integer.parseUnsignedInt(matcher.group(1) + matcher.group(2), 16);
            final String regexp   = matcher.group(3);
            final String groupIdx = matcher.group(4);
            final int    group    = null == groupIdx ? 1 : Integer.parseInt(groupIdx);
            final String target	  = matcher.group(5);
            final String repl     = matcher.group(6);
            return new DicomPatternRuleMetaData(tag, regexp, group, target, repl);
        } else {
            String msg = "Failed to parse routing expression '%s'".formatted(expression);
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    private static class DicomPatternRuleMetaData {
        final int     tag;
        final Pattern regexp;
        final int     group;
        final String  target;
        final String  repl;

        public DicomPatternRuleMetaData(int tag, String regexp, int group, String target, String repl) {
            this.tag = tag;
            this.regexp = Pattern.compile(regexp);
            this.group = group;
            this.target = target;
            this.repl = repl;
        }
    }

    @Nonnull
    public List<Extractor> parseAsExtractors(final Reader source) throws IOException {
        RoutingExpressionProvider routingExpressionProvider = new RoutingExpressionFromReaderProvider( source);
        return routingExpressionProvider.provide().stream()
                .map( this::parseDicomRuleToExtractor)
                .filter( Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nonnull
    public List<AbstractConditionalAttrDef.Rule> parseAsRules(final Reader source) throws IOException {
        RoutingExpressionProvider routingExpressionProvider = new RoutingExpressionFromReaderProvider( source);
        return routingExpressionProvider.provide().stream()
                .map( this::parseDicomRuleToRule)
                .filter( Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * validate
     *
     * @param type type of expression to validate.
     * @return Empty list if valid else List of error messages.
     */
     public List<String> validate( CompositeDicomObjectIdentifier.ExtractorType type) {
         return validate( _routingExpressionProvider.provide( type));
    }

    public List<String> validate() {
        return validate( _routingExpressionProvider.provide());
    }

    public List<String> validate( List<String> expressions) {
        List<String> errors = new ArrayList<>();
        for( String e: expressions) {
            errors.addAll( validate( e));
        }
        return errors;
    }
    public List<String> validate( String expression) {
        List<String> errors = new ArrayList<>();
        try {
            if( StringUtils.isNotEmpty( expression)) {
                parseDicomRule( expression);
            }
        }
        catch( Exception ex) {
            errors.add( ex.getMessage());
            if( ex.getCause() != null && ex.getCause().getMessage() != null) {
                errors.add( ex.getCause().getMessage());
            }
        }
        return errors;
    }
}
