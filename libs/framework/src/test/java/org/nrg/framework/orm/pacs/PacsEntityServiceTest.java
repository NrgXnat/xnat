/*
 * framework: org.nrg.framework.orm.pacs.PacsEntityServiceTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.pacs;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.orm.utils.TestDBUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PacsEntityServiceTestConfiguration.class)
@Transactional
public class PacsEntityServiceTest {
    public static final String AE_TITLE            = "TIP-DEV-PACS";
    public static final String HOST                = "10.28.16.215";
    public static final int    STORAGE_PORT        = 11112;
    public static final int    QUERY_RETRIEVE_PORT = 11112;
    public static final String DICOM_ORM_STRATEGY  = "dicomOrmStrategy";

    @Test(expected = ConstraintViolationException.class)
    public void testNullPacs() {
        final Pacs entity = pacsEntityService.newEntity();
        entity.setAeTitle("testNullPacs");
        pacsEntityService.create(entity);
    }

    @Test
    public void testAllServiceMethods() {
        assertEquals(0, pacsEntityService.getAll().size());

        final Pacs pacs1 = buildTestPacs();
        pacsEntityService.create(pacs1);
        assertEquals(1, pacsEntityService.getAll().size());

        final Pacs pacs2 = pacsEntityService.findByAeTitle("TIP-DEV-PACS");
        assertNotNull(pacs2);
        assertEquals("TIP-DEV-PACS", pacs2.getAeTitle());
        pacs2.setAeTitle("FOO");
        pacsEntityService.update(pacs2);

        final Pacs pacs3 = pacsEntityService.retrieve(pacs2.getId());
        assertEquals("FOO", pacs3.getAeTitle());
        pacsEntityService.delete(pacs3);
        assertEquals(0, pacsEntityService.getAll().size());
    }

    @Test
    public void testQueries() {
        final Pacs pacs1 = buildTestPacs();
        pacsEntityService.create(pacs1);
        assertEquals(1, pacsEntityService.getAll().size());

        assertTrue(pacsEntityService.exists("host", HOST));
        assertTrue(pacsEntityService.exists(ImmutableMap.<String, Object>of("aeTitle", AE_TITLE, "host", HOST, "storagePort", STORAGE_PORT)));
        assertFalse(pacsEntityService.exists(ImmutableMap.<String, Object>of("aeTitle", "garbage", "host", HOST, "storagePort", STORAGE_PORT)));
    }

    private Pacs buildTestPacs() {
        Pacs pacs = new Pacs();
        pacs.setAeTitle(AE_TITLE);
        pacs.setHost(HOST);
        pacs.setStoragePort(STORAGE_PORT);
        pacs.setQueryRetrievePort(QUERY_RETRIEVE_PORT);
        pacs.setOrmStrategySpringBeanId(DICOM_ORM_STRATEGY);
        return pacs;
    }

    @Inject
    private PacsEntityService pacsEntityService;
}
