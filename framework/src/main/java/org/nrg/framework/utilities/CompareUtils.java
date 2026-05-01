package org.nrg.framework.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompareUtils {
    public static boolean isOneNullAndOneNotNull(final Object object1, final Object object2) {
        return !(ObjectUtils.allNotNull(object1, object2) || ObjectUtils.allNull(object1, object2));
    }
}
