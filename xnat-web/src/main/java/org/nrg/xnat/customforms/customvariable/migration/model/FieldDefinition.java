package org.nrg.xnat.customforms.customvariable.migration.model;

public class FieldDefinition {

    private final String name;
    private final  String datatype;
    public FieldDefinition(final String name, final String datatype) {
        this.name = name;
        this.datatype = datatype;
    }
    public String getName() {
        return name;
    }
    public String getDatatype() {
        return datatype;
    }
}
