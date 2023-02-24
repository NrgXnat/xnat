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
                                       String type) {
        this.setLabel(label);
        this.setKey(key);
        this.setFieldName(fieldName);
        this.setType(type);
        this.setFormUUID(formUUID);
    }

}
