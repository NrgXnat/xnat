package org.nrg.dcm.id;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.config.services.ConfigService;
import org.nrg.xdat.XDAT;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * RoutingExpressionFromConfigProvider is an implementation of RoutingExpressionProvider that reads expressions
 * from the ConfigService.
 *
 */
@Slf4j
public class RoutingExpressionFromConfigProvider implements RoutingExpressionProvider {
    private ConfigService _configService;

    public static final String DICOM_ROUTING_TOOL = "dicom";
    public static final String PROJECT_ROUTING_CONFIG = "projectRules";
    public static final String SUBJECT_ROUTING_CONFIG = "subjectRules";
    public static final String SESSION_ROUTING_CONFIG = "sessionRules";
    public static final String AA_ROUTING_CONFIG = "aaRules";

    public RoutingExpressionFromConfigProvider() {
        _configService = getConfigService();
    }

    @Override
    public List<String> provide(CompositeDicomObjectIdentifier.ExtractorType type) {
        List<String> rules = new ArrayList<>();
        String config = null;
        switch (type) {
            case PROJECT:
                config = PROJECT_ROUTING_CONFIG;
                break;
            case SUBJECT:
                config = SUBJECT_ROUTING_CONFIG;
                break;
            case SESSION:
                config = SESSION_ROUTING_CONFIG;
                break;
            case AA:
                config = AA_ROUTING_CONFIG;
                break;
        }
        final Configuration configuration = getConfigService().getConfig(DICOM_ROUTING_TOOL, config);
        if (configuration == null
                || !configuration.isEnabled()
                || !configuration.getStatus().equals(Configuration.ENABLED_STRING)
                || StringUtils.isBlank(configuration.getContents())) {
            return rules;
        }
        rules.addAll( parseConfig( configuration.getContents()));
        return rules;
    }

    public List<String> provide() {
        List<String> rules = new ArrayList<>();
        rules.addAll( provide(CompositeDicomObjectIdentifier.ExtractorType.PROJECT));
        rules.addAll( provide(CompositeDicomObjectIdentifier.ExtractorType.SUBJECT));
        rules.addAll( provide(CompositeDicomObjectIdentifier.ExtractorType.SESSION));
        rules.addAll( provide(CompositeDicomObjectIdentifier.ExtractorType.AA));
        return rules;
    }

    /**
     * parseConfig
     * Convert the string from configuration to possibly multiple routing expressions.
     *
     * The configuration string can contain new-line separated routing expressions.
     *
     * @param s string from config
     * @return List of routing expressions.
     */
    @Nonnull
    protected List<String> parseConfig( String s) {
        return Arrays.asList( s.split("\\R"));
    }

    protected ConfigService getConfigService() {
        if( _configService == null) {
            _configService = XDAT.getConfigService();
        }
        return _configService;
    }

}
