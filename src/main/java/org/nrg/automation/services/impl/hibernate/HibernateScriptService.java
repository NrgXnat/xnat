/*
 * automation: org.nrg.automation.services.impl.hibernate.HibernateScriptService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services.impl.hibernate;

import org.apache.commons.io.FileUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.nrg.automation.entities.Script;
import org.nrg.automation.repositories.ScriptRepository;
import org.nrg.automation.services.ScriptService;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * HibernateScriptService class.
 */
@SuppressWarnings("JpaQlInspection")
@Service
public class HibernateScriptService extends AbstractHibernateEntityService<Script, ScriptRepository> implements ScriptService {
    /**
     * A convenience test for the existence of a script with the indicated script ID.
     *
     * @param scriptId The ID of the script to test for.
     * @return <b>true</b> if a script with the indicated ID exists, <b>false</b> otherwise.
     */
    @Override
    @Transactional
    public boolean hasScript(final String scriptId) {
        final Session session = _sessionFactory.getCurrentSession();
        final Query   query   = session.createQuery("select count(*) from Script where scriptId = :scriptId and enabled = true").setString("scriptId", scriptId);
        return ((Long) query.uniqueResult()) > 0;
    }

    /**
     * Retrieves the {@link org.nrg.automation.entities.Script} with the indicated script ID.
     *
     * @param scriptId The {@link org.nrg.automation.entities.Script#getScriptId() script ID} of the script to
     *                 retrieve.
     * @return The script with the indicated scriptId, if it exists, <b>null</b> otherwise.
     */
    @Override
    @Transactional
    public Script getByScriptId(final String scriptId) {
        List<Script> scripts = getDao().findByProperty("scriptId", scriptId);
        Script scriptToReturn = null;
        if(scripts!=null){
            scriptToReturn = scripts.get(0);
        }
//        int maxVersion = -1;
//        for(Script script : scripts){
//            if(maxVersion==-1 || (script.getScriptVersion()!=null && Integer.parseInt(script.getScriptVersion())>maxVersion)){
//                maxVersion=Integer.parseInt(script.getScriptVersion());
//                scriptToReturn=script;
//            }
//        }
        return scriptToReturn;
    }

    @Override
    @Transactional
    public Object getVersion(final String scriptId, final String version) {
        final Session session = _sessionFactory.getCurrentSession();
        //Integer id = Integer.parseInt(scriptId);
        final Long id = (Long)session.createQuery("select id from Script where script_id='"+scriptId+"'").list().get(0);
        AuditReader reader = AuditReaderFactory.get(session);
//        AuditQuery query = reader.createQuery()
//                .forRevisionsOfEntity(Script.class, false, true)
//                .add(AuditEntity.id().eq(id))
//                .add(AuditEntity.revisionNumber().eq(Integer.parseInt(version)));
//        Object revision = query.getResultList().get(0);
//        return revision;
        return ((Object[])reader.createQuery().forRevisionsOfEntity(Script.class, false, true).add(AuditEntity.id().eq(id)).add(AuditEntity.revisionNumber().eq(Integer.parseInt(version))).getResultList().get(0))[0];

//        Integer vers = Integer.parseInt(version);
//        final Query query = session.createQuery("select id, rev as version, content, language, description from xhbm_script_aud where rev='"+vers+"' id in (select id from xhbm_script where script_id='"+scriptId+"')");
//        return query.list().get(0);
    }

    @Override
    @Transactional
    public List<String> getVersions(final String scriptId) {
        final Session session = _sessionFactory.getCurrentSession();
        final Long id = (Long)session.createQuery("select id from Script where script_id='"+scriptId+"'").list().get(0);
        AuditReader reader = AuditReaderFactory.get(session);
        AuditQuery query = reader.createQuery().forRevisionsOfEntity(Script.class, false, true).add(AuditEntity.id().eq(id));
        List revisionsList = query.getResultList();
        ArrayList<String> revisionNumbersList = new ArrayList<String>();
        for(Object revision : revisionsList){
            Object[] rev = (Object[]) revision;
            revisionNumbersList.add(""+((DefaultRevisionEntity)rev[1]).getId());
        }
        return revisionNumbersList;
    }

    @Override
    @Transactional
    public void writeScriptToFile(final String scriptId, final String filePath){
        Script script = getByScriptId(scriptId);
        try {
            FileUtils.writeStringToFile(new File(filePath), script.getContent());
        } catch (IOException e) {
            _log.error("",e);
        }
    }

    @Inject
    private SessionFactory _sessionFactory;

    private static final Logger _log = LoggerFactory.getLogger(HibernateScriptService.class);
}
