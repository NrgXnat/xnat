package org.nrg.xnat.archive.daos;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.orm.hibernate.QueryBuilder;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.ajax.Prearchive;
import org.nrg.xnat.archive.entities.DirectArchiveSession;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Repository
public class DirectArchiveSessionDao extends AbstractHibernateDAO<DirectArchiveSession> {

    public static final String PROJECT         = "project";
    public static final String TAG             = "tag";
    public static final String NAME            = "name";
    public static final String LAST_BUILT_DATE = "lastBuiltDate";
    public static final String STATUS          = "status";
    public static final String LOCATION        = "location";

    @Autowired
    public DirectArchiveSessionDao(final SiteConfigPreferences preferences) {
        this.preferences = preferences;
    }

    /**
     * Find direct archive session by project, tag, and name, per {@link
     * PrearcDatabase#eitherGetOrCreateSession(SessionData, File, PrearchiveCode)}.
     *
     * @param session The session data
     * @return the direct archive session entity or null if none found
     */
    @Nullable
    public DirectArchiveSession findBySessionData(SessionData session) {
        return findByProjectTagName(session.getProject(), session.getTag(), session.getName());
    }

    /**
     * Find DirectArchiveSession by project, tag, name (per PrearcDatabase#eitherGetOrCreateSession)
     *
     * @param project project
     * @param tag     studyInstanceUID
     * @param name    session name
     * @return the direct archive session entity or null if none found
     */
    @Nullable
    public DirectArchiveSession findByProjectTagName(String project, String tag, String name) {
        return findByUniqueProperties(parameters(PROJECT, project, TAG, tag, NAME, name));
    }

    /**
     * Find direct archive sessions in status receiving, updated more than getSessionXmlRebuilderInterval minutes ago
     *
     * @return list of sessions
     */
    @Nullable
    public List<DirectArchiveSession> findReadyForArchive() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -1 * preferences.getSessionXmlRebuilderInterval());
        QueryBuilder<DirectArchiveSession> builder = newQueryBuilder();
        builder.where(builder.and(builder.le(LAST_BUILT_DATE, cal.getTime()),
                                  builder.eq(STATUS, Prearchive.PrearcStatus.RECEIVING)));
        return builder.getResults();
    }

    /**
     * Find direct archive session by location
     *
     * @param location the session data url, will be the archive dir
     * @return any matching sessions or null if none found
     */
    @Nullable
    public List<DirectArchiveSession> findByLocation(String location) {
        return findByProperty(LOCATION, location);
    }

    private final SiteConfigPreferences preferences;

}