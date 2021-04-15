package org.nrg.xdat.security.user.exceptions;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "XNAT_CSRF token was not properly set in the user request")
public class InvalidCsrfException extends FailedLoginException {
    public InvalidCsrfException(final String message, final String login) {
        super(StringUtils.prependIfMissing(message, "Invalid CSRF: "), login);
    }
}
