package org.nrg.xnat.security.alias;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;

public class AliasTokenException extends NrgServiceException {
    public AliasTokenException(final String token) {
        super(NrgServiceError.UserNotFoundError);
        _token = token;
    }

    public String getToken() {
        return _token;
    }

    private final String _token;
}
