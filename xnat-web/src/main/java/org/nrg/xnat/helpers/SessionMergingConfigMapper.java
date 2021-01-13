package org.nrg.xnat.helpers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SessionMergingConfigMapper {
    public SessionMergingConfigMapper() {
        _configService = XDAT.getConfigService();
    }

    public Map<String, String> getSessionMergingConfigMap(final Configuration configuration) {
        return configuration == null ? getDefaultSessionMergingConfigMap() : getSessionMergingConfigMap(configuration.getContents());
    }

    public Map<String, String> getSessionMergingConfigMap(final String contents) {
        try {
            return StringUtils.isBlank(contents) ? getDefaultSessionMergingConfigMap() : MAPPER.readValue(contents, MAP_TYPE_REFERENCE);
        } catch (IOException exception) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Something went wrong unmarshalling the configuration.", exception);
        }
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "deprecation"})
    public boolean getUidModSetting(final String project) {
        // check project config
        final Configuration config = _configService.getConfig("sessionmerging", "script", XnatProjectdata.getProjectInfoIdFromStringId(project));
        if (config != null && StringUtils.equals(config.getStatus(), "enabled")) {
            return Boolean.parseBoolean(getSessionMergingConfigMap(config).get("sessionmerging_uid_mod"));
        }
        // if nothing there, check site config
        return XDAT.getBoolSiteConfigurationProperty("sessionmerging_uid_mod", false);

    }

    private Map<String, String> getDefaultSessionMergingConfigMap() {
        final Map<String, String> map = new HashMap<>();
        map.put("enabled", "false");
        map.put("sessionmerging_uid_mod", "");
        return map;
    }

    private static final ObjectMapper                           MAPPER             = new ObjectMapper(new JsonFactory());
    private static final TypeReference<HashMap<String, String>> MAP_TYPE_REFERENCE = new TypeReference<HashMap<String, String>>() {
    };

    private final ConfigService _configService;
}