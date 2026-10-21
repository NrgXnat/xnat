package org.nrg.xnat.entities;

import lombok.Data;

import jakarta.persistence.Entity;

// @Entity
// @Data
public class AuthenticationProviderDefinition {
    private String providerId;

    private String name;

    private String type;


}
