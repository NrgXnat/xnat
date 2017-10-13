package org.nrg.xnat.entities;

import lombok.Data;

import javax.persistence.Entity;

// @Entity
// @Data
public class AuthenticationProviderDefinition {
    private String providerId;

    private String name;

    private String type;


}
