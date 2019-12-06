package org.nrg.xnat.utils.functions;

import com.google.common.base.Function;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcSession;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(prefix = "_")
@Slf4j
public class FileToPrearcSession implements Function<File, PrearcSession> {
    public FileToPrearcSession(final UserI user) {
        _user = user;
    }

    @Nullable
    @Override
    public PrearcSession apply(final File file) {
        try {
            return new PrearcSession(file, getUser());
        } catch (Exception e) {
            getErrors().add(e);
            return null;
        }
    }

    /**
     * Indicates whether any errors occurred during transformation.
     *
     * @return Returns <b>true</b> if any errors occurred, <b>false</b> otherwise.
     */
    public boolean hasErrors() {
        return !getErrors().isEmpty();
    }

    private final List<Exception> _errors = new ArrayList<>();
    private final UserI           _user;
}
