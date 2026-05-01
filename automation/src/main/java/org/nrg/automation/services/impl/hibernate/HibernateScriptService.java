/*
 * automation: org.nrg.automation.services.impl.hibernate.HibernateScriptService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HibernateScriptService class.
 */
@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
@Slf4j
public class HibernateScriptService extends AbstractHibernateEntityService<Script, ScriptRepository> implements ScriptService {
    private final SessionFactory _sessionFactory;

    @Value("${automation.enabled:true}")
    private boolean _automationEnabled;

    @Autowired
    public HibernateScriptService(final SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }

    /**
     * A convenience test for the existence of a script with the indicated script ID.
     *
     * @param scriptId The ID of the script to test for.
     *
     * @return <b>true</b> if a script with the indicated ID exists, <b>false</b> otherwise.
     */
    @Override
    public boolean hasScript(final String scriptId) {
        if (!_automationEnabled) {
            return false;
        }
        final Map<String, Object> properties = new HashMap<>();
        properties.put("scriptId", scriptId);
        properties.put("enabled", true);
        return getDao().exists(properties);
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
    public Script getByScriptId(final String scriptId) {
        if (!_automationEnabled) {
            return null;
        }
        return getDao().findByUniqueProperty("scriptId", scriptId);
    }

    @Override
    public Object getVersion(final String scriptId, final String version) {
        if (!_automationEnabled) {
            return null;
        }
        final Session session = _sessionFactory.getCurrentSession();
        final Long    id      = (Long) session.createQuery("select id from Script where script_id='" + scriptId + "'").list().get(0);
        AuditReader   reader  = AuditReaderFactory.get(session);
        return ((Object[]) reader.createQuery().forRevisionsOfEntity(Script.class, false, true).add(AuditEntity.id().eq(id)).add(AuditEntity.revisionNumber().eq(Integer.parseInt(version))).getResultList().get(0))[0];
    }

    @Override
    public List<String> getVersions(final String scriptId) {
        if (!_automationEnabled) {
            return Collections.emptyList();
        }
        final Session     session             = _sessionFactory.getCurrentSession();
        final Long        id                  = (Long) session.createQuery("select id from Script where script_id = :scriptId").setParameter("scriptId", scriptId).list().get(0);
        AuditReader       reader              = AuditReaderFactory.get(session);
        AuditQuery        query               = reader.createQuery().forRevisionsOfEntity(Script.class, false, true).add(AuditEntity.id().eq(id));
        List<?>           revisionsList       = query.getResultList();
        ArrayList<String> revisionNumbersList = new ArrayList<>();
        for (Object revision : revisionsList) {
            Object[] rev = (Object[]) revision;
            revisionNumbersList.add(String.valueOf(((DefaultRevisionEntity) rev[1]).getId()));
        }
        return revisionNumbersList;
    }

    @Override
    public void writeScriptToFile(final String scriptId, final String filePath) {
        if (_automationEnabled) {
            Script script = getByScriptId(scriptId);
            try {
                FileUtils.writeStringToFile(new File(filePath), script.getContent(), Charset.defaultCharset());
            } catch (IOException e) {
                log.error("An error occurred trying to write the script for ID {} out to the file path {}", scriptId, filePath, e);
            }
        }
    }
}
