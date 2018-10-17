package org.nrg.xnat.helpers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.nrg.config.entities.Configuration;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;

public class SessionMergingConfigMapper {
	 private  final ObjectMapper MAPPER = new ObjectMapper(new JsonFactory());
	 private  final TypeReference<HashMap<String, String>> MAP_TYPE_REFERENCE = new TypeReference<HashMap<String, String>>() {};
	    
	    
	    public Map<String, String> getSessionMergingConfigMap(Configuration configuration) {
	        if (configuration == null) {
	            return getSessionMergingConfigMap();
	        }
	        return getSessionMergingConfigMap(configuration.getContents());
	    }

	    public  Map<String, String> getSessionMergingConfigMap(String contents) {
	        if (StringUtils.isBlank(contents)) {
	            return getSessionMergingConfigMap();
	        }
	        try {
	            return MAPPER.readValue(contents, MAP_TYPE_REFERENCE);
	        } catch (IOException exception) {
	            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Something went wrong unmarshalling the configuration.", exception);
	        }
	    }
	    
	    private  Map<String, String> getSessionMergingConfigMap() {
	        Map<String, String> map = new HashMap<String, String>();
	        map.put("enabled", "false");
	        map.put("sessionmerging_uid_mod", "");
	        return map;
	    }
}