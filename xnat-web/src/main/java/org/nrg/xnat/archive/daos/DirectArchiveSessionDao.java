package org.nrg.xnat.archive.daos;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.NonUniqueResultException;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.ajax.Prearchive;
import org.nrg.xnat.archive.entities.DirectArchiveSession;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class DirectArchiveSessionDao extends AbstractHibernateDAO<DirectArchiveSession> {
    @Autowired
    public DirectArchiveSessionDao(final SiteConfigPreferences preferences) {
        this.preferences = preferences;
    }

    /**
     * Find DirectArchiveSession by project, folderName, timestamp
     * @param session the sessiondata
     * @return the direct archive session entity or null if none found
     * @throws NonUniqueResultException if multiple sessions match
     */
    @Nullable
    public DirectArchiveSession findBySessionData(SessionData session) throws NonUniqueResultException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("project", session.getProject());
        properties.put("tag", session.getTag());
        properties.put("name", session.getName());
        List<DirectArchiveSession> matches = findByProperties(properties);
        if (matches == null) {
            return null;
        }
        if (matches.size() > 1) {
            throw new NonUniqueResultException(matches.size());
        }
        return matches.get(0);
    }

    /**
     * Find direct archive sessions in status receiving, updated more than getSessionXmlRebuilderInterval minutes ago
     * @return list of sessions
     */
    @Nullable
    public List<DirectArchiveSession> findReadyForArchive() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -1 * preferences.getSessionXmlRebuilderInterval());

        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.le("lastBuiltDate", cal.getTime()));
        criteria.add(Restrictions.eq("status", Prearchive.PrearcStatus.RECEIVING));
        return emptyToNull(GenericUtils.convertToTypedList(criteria.list(), getParameterizedType()));
    }

    /**
     * Find direct archive session by location
     * @param location the session data url, will be the archive dir
     * @return any matching sessions or null if none found
     */
    @Nullable
    public List<DirectArchiveSession> findByLocation(String location) {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("location", location));
        return emptyToNull(GenericUtils.convertToTypedList(criteria.list(), getParameterizedType()));
    }

    private final SiteConfigPreferences preferences;

}