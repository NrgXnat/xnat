package org.nrg.xnat.archive.daos;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.orm.hibernate.QueryBuilder;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.ajax.Prearchive;
import org.nrg.xnat.archive.entities.DirectArchiveSession;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

    /**
     * Moves a session to {@code target} only if it is currently in one of the {@code allowed} statuses, as a single
     * conditional UPDATE so that two callers racing on the same row cannot both pass the check: the second one to
     * reach the row sees the first one's status and updates nothing. Callers that read the row afterwards rely on
     * it not already being in the persistence context, i.e. on each service call being its own transaction with no
     * open-session-in-view; a bulk update does not refresh an entity loaded earlier in the same session.
     *
     * @return the number of rows updated: 1 when the transition happened, 0 when the row was not in an allowed status
     * or does not exist
     */
    public int transitionStatus(final long id, final PrearcStatus target, final Set<PrearcStatus> allowed) {
        // getSession() rather than createQuery(String): the base class types its queries to the entity, which
        // Hibernate refuses for an UPDATE
        return getSession().createQuery("update DirectArchiveSession set status = :target, timestamp = :now where id = :id and status in (:allowed)")
                .setParameter("target", target)
                .setParameter("now", new Date())
                .setParameter("id", id)
                .setParameterList("allowed", allowed)
                .executeUpdate();
    }

    /**
     * Records that the session just received a file, touching only the timestamps. The importer calls this for every
     * file before it takes its file lock; writing the whole entity back here would put the status it had just read
     * over a delete's claim.
     *
     * @return the number of rows updated: 0 when the session no longer exists
     */
    public int touch(final long id) {
        return getSession().createQuery("update DirectArchiveSession set lastBuiltDate = :now, timestamp = :now where id = :id")
                .setParameter("now", new Date())
                .setParameter("id", id)
                .executeUpdate();
    }

    private final SiteConfigPreferences preferences;

}