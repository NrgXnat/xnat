package org.nrg.xnat.utils.functions;

import com.google.common.base.Function;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.helpers.prearchive.SessionDataTriple;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(prefix = "_")
@Slf4j
public class UriToSessionDataTriple implements Function<String, SessionDataTriple> {
    @Nullable
    @Override
    public SessionDataTriple apply(final String uri) {
        try {
            return SessionDataTriple.fromURI(uri);
        } catch (MalformedURLException e) {
            log.error("The source value contains a malformed URL: {}", uri, e);
            getMalformedUrls().add(uri);
            return null;
        }
    }

    /**
     * Indicates whether any malformed URLs were found during transformation.
     *
     * @return Returns <b>true</b> if any malformed URLs were found, <b>false</b> otherwise.
     */
    public boolean hasMalformedUrls() {
        return !getMalformedUrls().isEmpty();
    }

    private final List<String> _malformedUrls = new ArrayList<>();
}
