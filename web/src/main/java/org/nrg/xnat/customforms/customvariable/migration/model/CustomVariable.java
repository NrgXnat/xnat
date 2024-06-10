package org.nrg.xnat.customforms.customvariable.migration.model;

public class CustomVariable {
    private String name;
    private String field;
    public CustomVariable(final String name, final String field) {
        this.name = name;
        this.field = field;
    }
    public String getName() {
        return name;
    }
    public String getField() {
        return field;
    }
}
