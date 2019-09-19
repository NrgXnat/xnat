package org.nrg.xnat.utils.functions;

import com.google.common.base.Function;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.prearchive.SessionDataTriple;

public class Functions {
    public static final Function<SessionData, SessionDataTriple> SESSION_DATA_TO_SESSION_DATA_TRIPLE = new Function<SessionData, SessionDataTriple>() {
        @Override
        public SessionDataTriple apply(final SessionData sessionData) {
            return sessionData.getSessionDataTriple();
        }
    };
}
