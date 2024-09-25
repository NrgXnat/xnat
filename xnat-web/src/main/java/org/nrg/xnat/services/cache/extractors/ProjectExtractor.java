package org.nrg.xnat.services.cache.extractors;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xnat.services.cache.DefaultUserProjectCache;
import org.nrg.xnat.services.cache.UserProjectCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ProjectExtractor extends AbstractUserProjectCacheDataExtractor<String, XnatProjectdata> {
    @Autowired
    public ProjectExtractor(final @Lazy UserProjectCache cache, final NamedParameterJdbcTemplate template) {
        super(cache, DefaultUserProjectCache.CACHE_PROJECTS, template, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getCacheProperties() {
        return NON_EXPIRING_TTL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatProjectdata extract(final String projectId, final Object... parameters) {
        if (isInvalidExtractRequest(projectId, parameters)) {
            return null;
        }

        log.info("Extracting project for ID '{}'", projectId);
        // Note: this uses AutoXnatProjectdata.getXnatProjectdatasById() because that doesn't try to access the cache.
        // Calling XnatProjectdata.getXnatProjectdatasById() or BaseXnatProjectdata.getXnatProjectdatasById() will lead
        // to stack overflow errors from circular recursive calls.
        return AutoXnatProjectdata.getXnatProjectdatasById(projectId, null, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getKeys() {
        return getAllProjectIds();
    }
}
