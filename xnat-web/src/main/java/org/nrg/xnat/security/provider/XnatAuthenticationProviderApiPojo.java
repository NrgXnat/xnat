package org.nrg.xnat.security.provider;

import lombok.Value;

@Value
public class XnatAuthenticationProviderApiPojo {
    private String providerId;
    private String name;
    private String authMethod;
}
