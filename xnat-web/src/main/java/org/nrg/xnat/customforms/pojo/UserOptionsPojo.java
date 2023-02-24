package org.nrg.xnat.customforms.pojo;

import lombok.Getter;
import lombok.Setter;
import org.nrg.xnat.entities.CustomVariableAppliesTo;

@Getter
@Setter
public class UserOptionsPojo {
    String dataType;
    String protocol;
    String visit;
    String subType;
    String scanType;
    int zIndex = - 1;

    public UserOptionsPojo(final String dataType, final String protocol, final String visit, final String subType) {
        this.dataType = dataType;
        this.protocol = protocol;
        this.visit = visit;
        this.subType = subType;
        this.scanType = null;
    }

    public static UserOptionsPojo Get(CustomVariableAppliesTo appliesTo) {
        if (null == appliesTo) {
            return null;
        }
        UserOptionsPojo userOptions = new UserOptionsPojo(appliesTo.getDataType(), appliesTo.getProtocol(), appliesTo.getVisit(), appliesTo.getSubType());
        return userOptions;
    }

}
