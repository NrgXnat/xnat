package org.nrg.xnat.customforms.customvariable.migration.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataIntegrityItem {

    private String fieldName;
    private String dataFound;
    private String expectedFormat;


}
