package org.nrg.xdat.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;

@AllArgsConstructor
@Getter
public class UsernameAuthMappingNotFoundException extends NrgServiceRuntimeException {
    @NonNull
    private final String username;

    @NonNull
    private final String authMethod;

    private final String authMethodId;

    private final String email;

    private final String lastName;

    private final String firstName;
}
