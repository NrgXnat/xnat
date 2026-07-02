package org.nrg.dcm.xnat.daos;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.dcm.xnat.entities.DicomMappingEntity;
import org.nrg.dcm.xnat.pojos.FieldType;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.config.TestDicomMappingEntityDaoConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for {@link DicomMappingEntityDao#findInScopeByProperty(String, String, String)}, written against
 * the original Hibernate Criteria implementation before conversion to the JPA Criteria API.
 *
 * <p>Semantics under test:</p>
 * <ul>
 *     <li>Always: {@code property = value}.</li>
 *     <li>Blank project: only site-scope rows.</li>
 *     <li>Project given: project-scope rows for that project, plus site-scope rows whose DICOM tag is <i>not</i>
 *     already covered by a project-scope row with the same property value (project scope overrides site scope
 *     per DICOM tag).</li>
 *     <li>No matches: returns {@code null} rather than an empty list.</li>
 * </ul>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestDicomMappingEntityDaoConfig.class)
@Transactional
public class DicomMappingEntityDaoTest {
    private static final String PROPERTY       = "schemaElement";
    private static final String SCHEMA_ELEMENT = "xnat:mrSessionData/fields/field";
    private static final String OTHER_ELEMENT  = "xnat:petSessionData/fields/field";
    private static final String TAG_1          = "(0010,0010)";
    private static final String TAG_2          = "(0008,0080)";
    private static final String PROJECT_A      = "projectA";
    private static final String PROJECT_B      = "projectB";

    @Autowired
    private DicomMappingEntityDao _dao;

    private int _fieldNameCounter;

    @Test
    public void returnsNullWhenNothingMatches() {
        seed(Scope.Site, null, TAG_1, OTHER_ELEMENT);

        assertThat(_dao.findInScopeByProperty(null, PROPERTY, SCHEMA_ELEMENT)).isNull();
        assertThat(_dao.findInScopeByProperty(PROJECT_A, PROPERTY, SCHEMA_ELEMENT)).isNull();
    }

    @Test
    public void blankProjectReturnsOnlySiteScopeRows() {
        final DicomMappingEntity site = seed(Scope.Site, null, TAG_1, SCHEMA_ELEMENT);
        seed(Scope.Project, PROJECT_A, TAG_1, SCHEMA_ELEMENT);

        final List<DicomMappingEntity> forNull = _dao.findInScopeByProperty(null, PROPERTY, SCHEMA_ELEMENT);
        assertThat(forNull).isNotNull().extracting(DicomMappingEntity::getId).containsExactly(site.getId());

        final List<DicomMappingEntity> forBlank = _dao.findInScopeByProperty("  ", PROPERTY, SCHEMA_ELEMENT);
        assertThat(forBlank).isNotNull().extracting(DicomMappingEntity::getId).containsExactly(site.getId());
    }

    @Test
    public void projectScopeRowSuppressesSiteScopeRowWithSameTag() {
        seed(Scope.Site, null, TAG_1, SCHEMA_ELEMENT);
        final DicomMappingEntity siteTag2   = seed(Scope.Site, null, TAG_2, SCHEMA_ELEMENT);
        final DicomMappingEntity projectTag1 = seed(Scope.Project, PROJECT_A, TAG_1, SCHEMA_ELEMENT);

        final List<DicomMappingEntity> results = _dao.findInScopeByProperty(PROJECT_A, PROPERTY, SCHEMA_ELEMENT);
        assertThat(results).isNotNull()
                           .extracting(DicomMappingEntity::getId)
                           .containsExactlyInAnyOrder(projectTag1.getId(), siteTag2.getId());
    }

    @Test
    public void projectWithoutProjectScopeRowsFallsBackToSiteScope() {
        final DicomMappingEntity siteTag1 = seed(Scope.Site, null, TAG_1, SCHEMA_ELEMENT);
        final DicomMappingEntity siteTag2 = seed(Scope.Site, null, TAG_2, SCHEMA_ELEMENT);

        final List<DicomMappingEntity> results = _dao.findInScopeByProperty(PROJECT_A, PROPERTY, SCHEMA_ELEMENT);
        assertThat(results).isNotNull()
                           .extracting(DicomMappingEntity::getId)
                           .containsExactlyInAnyOrder(siteTag1.getId(), siteTag2.getId());
    }

    @Test
    public void otherProjectRowsAreNeitherReturnedNorSuppressSiteScope() {
        final DicomMappingEntity siteTag1 = seed(Scope.Site, null, TAG_1, SCHEMA_ELEMENT);
        seed(Scope.Project, PROJECT_B, TAG_1, SCHEMA_ELEMENT);

        final List<DicomMappingEntity> results = _dao.findInScopeByProperty(PROJECT_A, PROPERTY, SCHEMA_ELEMENT);
        assertThat(results).isNotNull()
                           .extracting(DicomMappingEntity::getId)
                           .containsExactly(siteTag1.getId());
    }

    @Test
    public void projectRowForDifferentPropertyValueDoesNotSuppressSiteScope() {
        final DicomMappingEntity siteTag1 = seed(Scope.Site, null, TAG_1, SCHEMA_ELEMENT);
        // Same tag, same project, but mapped for a different schema element: must not suppress the site-scope row.
        seed(Scope.Project, PROJECT_A, TAG_1, OTHER_ELEMENT);

        final List<DicomMappingEntity> results = _dao.findInScopeByProperty(PROJECT_A, PROPERTY, SCHEMA_ELEMENT);
        assertThat(results).isNotNull()
                           .extracting(DicomMappingEntity::getId)
                           .containsExactly(siteTag1.getId());
    }

    @Test
    public void projectScopeRowsAreReturnedForMatchingProjectOnly() {
        final DicomMappingEntity projectA = seed(Scope.Project, PROJECT_A, TAG_1, SCHEMA_ELEMENT);
        seed(Scope.Project, PROJECT_B, TAG_2, SCHEMA_ELEMENT);

        final List<DicomMappingEntity> results = _dao.findInScopeByProperty(PROJECT_A, PROPERTY, SCHEMA_ELEMENT);
        assertThat(results).isNotNull()
                           .extracting(DicomMappingEntity::getId)
                           .containsExactly(projectA.getId());
    }

    private DicomMappingEntity seed(final Scope scope, final String scopeObjectId, final String dicomTag, final String schemaElement) {
        final DicomMappingEntity entity = new DicomMappingEntity();
        entity.setScope(scope);
        entity.setScopeObjectId(scopeObjectId);
        entity.setFieldName("field-" + _fieldNameCounter++);
        entity.setFieldType(FieldType.STRING);
        entity.setDicomTag(dicomTag);
        entity.setSchemaElement(schemaElement);
        _dao.create(entity);
        return entity;
    }
}
