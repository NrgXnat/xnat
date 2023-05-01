/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * @author: Mohana Ramaratnam (mohana@radiologics.com)
 * @since: 07-03-2021
 */
package org.nrg.xnat.customforms.pojo;

import lombok.Getter;
import lombok.Setter;
import org.nrg.xdat.forms.models.pojo.FormFieldPojo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * POJO for the XNAT Custom Field
 */

@Getter
@Setter
public class FormIOJsonToXnatCustomField extends FormFieldPojo {

    public FormIOJsonToXnatCustomField(UUID formUUID, String label,
                                       String key,
                                       String fieldName,
                                       String type, 
                                       List<String> jsonPaths) {
        setLabel(label);
        setKey(key);
        setFieldName(fieldName);
        setType(type);
        setFormUUID(formUUID);
        setJsonPaths(null == jsonPaths ? new ArrayList<>() : jsonPaths);
    }

    public String getJsonRootName() {
       return getJsonPaths().isEmpty() ? getFieldName() : getJsonPaths().get(0);
    }


}
