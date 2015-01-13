package org.nrg.automation.services.impl.hibernate;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.nrg.automation.entities.Script;
import org.nrg.automation.repositories.ScriptRepository;
import org.nrg.automation.services.ScriptService;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * HibernateScriptService class.
 *
 * @author Rick Herrick <rick.herrick@wustl.edu> on 9/25/2014.
 */
@Service
public class HibernateScriptService extends AbstractHibernateEntityService<Script, ScriptRepository> implements ScriptService {
    /**
     * A convenience test for the existence of a script with the indicated script ID.
     *
     * @param scriptId The ID of the script to test for.
     *
     * @return <b>true</b> if a script with the indicated ID exists, <b>false</b> otherwise.
     */
    @Override
    @Transactional
    public boolean hasScript(final String scriptId) {
        final Session session = _sessionFactory.getCurrentSession();
        final Query query = session.createQuery("select count(*) from Script where scriptId = :scriptId and enabled = true").setString("scriptId", scriptId);
        return ((Long) query.uniqueResult()) > 0;
    }

    /**
     * Retrieves the {@link org.nrg.automation.entities.Script} with the indicated script ID.
     *
     * @param scriptId The {@link org.nrg.automation.entities.Script#getScriptId() script ID} of the script to
     *                 retrieve.
     *
     * @return The script with the indicated scriptId, if it exists, <b>null</b> otherwise.
     */
    @Override
    @Transactional
    public Script getByScriptId(final String scriptId) {
        return getDao().findByUniqueProperty("scriptId", scriptId);
    }

    @Inject
    private SessionFactory _sessionFactory;
}
