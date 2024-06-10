package org.nrg.xnat.customforms.pojo.formio;

import groovy.util.logging.Slf4j;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class RowIdentifier implements Serializable {

    public final static String DELIMITER = "_";
    long formId;
    long appliesToId;

    @Nullable
    public static String Marshall(RowIdentifier rowId) {
        if (rowId == null) {
            return null;
        }
        return rowId.getFormId() + DELIMITER + rowId.getAppliesToId();
    }

    @Nullable
    public static RowIdentifier Unmarshall(String str) {
        if (str == null) {
            return null;
        }
        try {
            String[] strSplit = str.split(DELIMITER);
            RowIdentifier rowId = new RowIdentifier();
            rowId.setFormId(Long.parseLong(strSplit[0].trim()));
            rowId.setAppliesToId(Long.parseLong(strSplit[1].trim()));
            return rowId;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RowIdentifier that = (RowIdentifier) o;
        return (formId == that.getFormId() && appliesToId == that.appliesToId);
    }

}
