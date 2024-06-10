package org.nrg.xnat.restlet.util;

import java.util.Map;

public interface SecureResourceParameterMapper{
    public Map<String, String> mapParams(Map<String,String> params, String dataType);
}
