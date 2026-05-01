package org.nrg.dcm.xnat.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FieldType {

    STRING("string"),
    INTEGER("integer"),
    FLOAT("float"),
    BOOLEAN("boolean"),
    DATE("date"),
    TIME("time");

    private final String name;

    @JsonCreator
    FieldType(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
