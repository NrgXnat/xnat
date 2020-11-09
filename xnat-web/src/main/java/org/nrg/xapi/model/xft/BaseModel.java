package org.nrg.xapi.model.xft;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xft.security.UserI;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseModel<T extends BaseElement> implements Serializable {
    public abstract String getXSIType();

    public abstract T toXftItem();

    public abstract T toXftItem(UserI user);

    private String id;
    private Date   insertDate;
    private String insertUser;
}
