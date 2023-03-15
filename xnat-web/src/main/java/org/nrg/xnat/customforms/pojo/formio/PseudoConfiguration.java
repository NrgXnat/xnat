package org.nrg.xnat.customforms.pojo.formio;

import lombok.Getter;
import lombok.Setter;
import org.nrg.framework.constants.Scope;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class PseudoConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<FormAppliesToPoJo> appliesToList;
    private String contents;
    private String formId;
    private String formUUID;
    private Integer formDisplayOrder;
    private String path;
    private Scope scope;
    private boolean doProjectsShareForm;
    private String username;
    private Date dateCreated;
    private boolean hasData;

}
