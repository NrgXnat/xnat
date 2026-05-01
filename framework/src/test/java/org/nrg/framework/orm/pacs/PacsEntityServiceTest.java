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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.ConstraintViolationException;
import java.util.Date;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PacsEntityServiceTestConfiguration.class)
@Slf4j
public class PacsEntityServiceTest {
    public static final String AE_TITLE            = "TIP-DEV-PACS";
    public static final String HOST                = "10.28.16.215";
    public static final int    STORAGE_PORT        = 11112;
    public static final int    QUERY_RETRIEVE_PORT = 11112;
    public static final String DICOM_ORM_STRATEGY  = "dicomOrmStrategy";

    private final PacsEntityService _pacsEntityService;

    @Autowired
    public PacsEntityServiceTest(final PacsEntityService pacsEntityService) {
        _pacsEntityService = pacsEntityService;
    }

    @AfterEach
    public void teardown() {
        _pacsEntityService.getAll().forEach(_pacsEntityService::delete);
    }

    @Test
    public void testNullPacs() {
        assertThrows(ConstraintViolationException.class, () -> {
            final Pacs entity = _pacsEntityService.newEntity();
            entity.setAeTitle("testNullPacs");
            _pacsEntityService.create(entity);
            final Pacs retrieved = _pacsEntityService.findByAeTitle("testNullPacs");
            log.info("Retrieved PACS entry with ID {}, AE title {}, storage port {}, and Q/R port {}", retrieved.getId(), retrieved.getAeTitle(), retrieved.getStoragePort(), retrieved.getQueryRetrievePort());
        });
    }

    @Test
    public void testCreateAndUpdateEntities() {
        final Pacs pacs1 = buildTestPacs();
        _pacsEntityService.create(pacs1);
        assertThat(_pacsEntityService.getAll()).isNotNull().hasSize(1);

        final Pacs retrieved1 = _pacsEntityService.findByAeTitle(AE_TITLE);
        assertThat(retrieved1).isNotNull().hasFieldOrPropertyWithValue("aeTitle", AE_TITLE);

        Date created1 = retrieved1.getCreated();
        Date updated1 = retrieved1.getTimestamp();

        assertThat(created1).isNotNull().isCloseTo(updated1, 1000L);
        
        retrieved1.setHost("foo");
        _pacsEntityService.update(retrieved1);

        final Pacs retrieved2 = _pacsEntityService.findByAeTitle(AE_TITLE);
        assertThat(retrieved2).isNotNull().extracting(Pacs::getAeTitle, Pacs::getHost).containsExactly(AE_TITLE, "foo");

        Date created2 = retrieved2.getCreated();
        Date updated2 = retrieved2.getTimestamp();

        assertThat(created2).isNotNull().isCloseTo(created1, 1000L).isCloseTo(updated1, 1000L).isNotEqualTo(updated2);
        assertThat(updated2).isNotNull().isAfter(updated1);
    }

    @Test
    public void testAllServiceMethods() {
        assertThat(_pacsEntityService.getAll()).isNotNull().isEmpty();

        final Pacs pacs1 = buildTestPacs();
        _pacsEntityService.create(pacs1);
        assertThat(_pacsEntityService.getAll()).isNotNull().hasSize(1);

        final Pacs pacs2 = _pacsEntityService.findByAeTitle("TIP-DEV-PACS");
        assertThat(pacs2).isNotNull().hasFieldOrPropertyWithValue("aeTitle", "TIP-DEV-PACS");
        pacs2.setAeTitle("FOO");
        _pacsEntityService.update(pacs2);

        final Pacs pacs3 = _pacsEntityService.retrieve(pacs2.getId());
        assertThat(pacs3).isNotNull().hasFieldOrPropertyWithValue("aeTitle", "FOO");
        _pacsEntityService.delete(pacs3);
        assertThat(_pacsEntityService.getAll()).isNotNull().isEmpty();
    }

    @Test
    public void testQueries() {
        final Pacs pacs1 = buildTestPacs();
        _pacsEntityService.create(pacs1);
        assertThat(_pacsEntityService.getAll()).isNotNull().hasSize(1);

        assertThat(_pacsEntityService.exists("host", HOST)).isTrue();
        assertThat(_pacsEntityService.exists(ImmutableMap.of("aeTitle", AE_TITLE, "host", HOST, "storagePort", STORAGE_PORT))).isTrue();
        assertThat(_pacsEntityService.exists(ImmutableMap.of("aeTitle", "garbage", "host", HOST, "storagePort", STORAGE_PORT))).isFalse();
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
}
