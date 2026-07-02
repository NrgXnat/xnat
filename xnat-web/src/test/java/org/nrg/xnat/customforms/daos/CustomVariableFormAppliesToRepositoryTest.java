package org.nrg.xnat.customforms.daos;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.config.TestCustomVariableFormAppliesToRepositoryConfig;
import org.nrg.xnat.customforms.pojo.UserOptionsPojo;
import org.nrg.xnat.customforms.pojo.formio.RowIdentifier;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.entities.CustomVariableAppliesTo;
import org.nrg.xnat.entities.CustomVariableForm;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for the query methods of {@link CustomVariableFormAppliesToRepository}, written against the
 * pre-conversion implementation (before removal of the legacy Hibernate Criteria API remnants) to lock in behavior.
 *
 * <p>Noteworthy semantics locked in here:</p>
 * <ul>
 *     <li>{@code findAllFormsByExclusion} and {@code findAllSpecificProjectForm} pass {@code restrictOnlyDataType =
 *     true}, so protocol/visit/subType/scanType values on the {@link UserOptionsPojo} are <i>not</i> applied as
 *     filters; only {@code dataType} (and scope, where given) matters.</li>
 *     <li>Rows with status {@link CustomFormsConstants#OPTED_OUT_STATUS_STRING} are excluded, and — due to SQL
 *     {@code NOT (status = ...)} null semantics shared by the legacy and JPA implementations — rows with a
 *     {@code null} status are excluded as well.</li>
 *     <li>{@code findAllDistinctFormsByDatatype} does not actually de-duplicate: the distinct filter result is
 *     discarded by the current implementation, so a form applied to two matching definitions appears twice.</li>
 * </ul>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestCustomVariableFormAppliesToRepositoryConfig.class)
@Transactional
public class CustomVariableFormAppliesToRepositoryTest {
    private static final String DATA_TYPE       = "xnat:mrSessionData";
    private static final String OTHER_DATA_TYPE = "xnat:petSessionData";
    private static final String PROJECT_1       = "project1";
    private static final String PROJECT_2       = "project2";
    private static final String PROJECT_3       = "project3";
    private static final String ENABLED         = CustomFormsConstants.ENABLED_STATUS_STRING;
    private static final String DISABLED        = CustomFormsConstants.DISABLED_STATUS_STRING;
    private static final String OPTED_OUT       = CustomFormsConstants.OPTED_OUT_STATUS_STRING;

    @Autowired
    private CustomVariableFormAppliesToRepository _repository;

    @Autowired
    private CustomVariableAppliesToRepository _appliesToRepository;

    @Autowired
    private CustomVariableFormRepository _formRepository;

    @Test
    public void findByRowIdentifierReturnsMatchingRowOrNull() {
        final CustomVariableAppliesTo appliesTo1 = appliesTo(Scope.Site, null, DATA_TYPE, null);
        final CustomVariableAppliesTo appliesTo2 = appliesTo(Scope.Project, PROJECT_1, DATA_TYPE, null);
        final CustomVariableForm      form1      = form();
        final CustomVariableForm      form2      = form();
        link(appliesTo1, form1, ENABLED);
        link(appliesTo2, form2, ENABLED);

        final CustomVariableFormAppliesTo found = _repository.findByRowIdentifier(rowId(form1.getId(), appliesTo1.getId()));
        assertThat(found).isNotNull();
        assertThat(found.getCustomVariableForm().getId()).isEqualTo(form1.getId());
        assertThat(found.getCustomVariableAppliesTo().getId()).isEqualTo(appliesTo1.getId());

        // A (form, appliesTo) pairing that doesn't exist as a row must return null.
        assertThat(_repository.findByRowIdentifier(rowId(form1.getId(), appliesTo2.getId()))).isNull();
    }

    @Test
    public void findForProjectMatchesProjectScopeAndFormId() {
        final CustomVariableAppliesTo projectScoped = appliesTo(Scope.Project, PROJECT_1, DATA_TYPE, null);
        final CustomVariableAppliesTo siteScoped    = appliesTo(Scope.Site, null, DATA_TYPE, null);
        final CustomVariableForm      form1         = form();
        link(projectScoped, form1, ENABLED);
        link(siteScoped, form1, ENABLED);

        final CustomVariableFormAppliesTo found = _repository.findForProject(PROJECT_1, form1.getId());
        assertThat(found).isNotNull();
        assertThat(found.getCustomVariableAppliesTo().getId()).isEqualTo(projectScoped.getId());
        assertThat(found.getCustomVariableAppliesTo().getScope()).isEqualTo(Scope.Project);

        assertThat(_repository.findForProject(PROJECT_2, form1.getId())).isNull();
    }

    @Test
    public void findAllFormsByExclusionFiltersDataTypeStatusAndExcludedForm() {
        final CustomVariableForm formKept        = form();
        final CustomVariableForm formOptedOut    = form();
        final CustomVariableForm formExcluded    = form();
        final CustomVariableForm formOtherType   = form();
        final CustomVariableForm formNullStatus  = form();

        // Distinct protocols keep the unique constraint on (scope, dataType, entityId, visit, protocol, subtype) happy.
        link(appliesTo(Scope.Site, null, DATA_TYPE, "protocol1"), formKept, ENABLED);
        link(appliesTo(Scope.Site, null, DATA_TYPE, "protocol2"), formOptedOut, OPTED_OUT);
        link(appliesTo(Scope.Site, null, DATA_TYPE, "protocol3"), formExcluded, ENABLED);
        link(appliesTo(Scope.Site, null, OTHER_DATA_TYPE, "protocol1"), formOtherType, ENABLED);
        link(appliesTo(Scope.Site, null, DATA_TYPE, "protocol4"), formNullStatus, null);

        // Protocol/visit/subType on the pojo must be ignored (restrictOnlyDataType = true): the surviving row has
        // protocol "protocol1", not "someProtocol".
        final UserOptionsPojo pojo = new UserOptionsPojo(DATA_TYPE, "someProtocol", "someVisit", "someSubType");

        final List<CustomVariableFormAppliesTo> results = _repository.findAllFormsByExclusion(pojo, formExcluded.getId());
        assertThat(results).extracting(row -> row.getCustomVariableForm().getId())
                           .containsExactly(formKept.getId());
    }

    @Test
    public void findAllSpecificProjectFormFiltersByProjectScopeEntityIdsAndFormId() {
        final CustomVariableAppliesTo project1 = appliesTo(Scope.Project, PROJECT_1, DATA_TYPE, null);
        final CustomVariableAppliesTo project2 = appliesTo(Scope.Project, PROJECT_2, DATA_TYPE, null);
        final CustomVariableAppliesTo project3 = appliesTo(Scope.Project, PROJECT_3, DATA_TYPE, null);
        final CustomVariableAppliesTo site     = appliesTo(Scope.Site, null, DATA_TYPE, null);

        final CustomVariableForm form1 = form();
        final CustomVariableForm form2 = form();
        link(project1, form1, ENABLED);
        link(project2, form1, ENABLED);
        link(project3, form1, ENABLED);
        link(site, form1, ENABLED);
        link(project1, form2, ENABLED);

        final UserOptionsPojo pojo = new UserOptionsPojo(DATA_TYPE, null, null, null);

        final List<CustomVariableFormAppliesTo> scoped = _repository.findAllSpecificProjectForm(pojo, Arrays.asList(PROJECT_1, PROJECT_2), form1.getId());
        assertThat(scoped).extracting(row -> row.getCustomVariableAppliesTo().getEntityId())
                          .containsExactlyInAnyOrder(PROJECT_1, PROJECT_2);
        assertThat(scoped).extracting(row -> row.getCustomVariableForm().getId())
                          .containsOnly(form1.getId());

        // Null entity ID list: no entity restriction, but the site-scope row is still excluded by scope.
        final List<CustomVariableFormAppliesTo> unscoped = _repository.findAllSpecificProjectForm(pojo, null, form1.getId());
        assertThat(unscoped).extracting(row -> row.getCustomVariableAppliesTo().getEntityId())
                            .containsExactlyInAnyOrder(PROJECT_1, PROJECT_2, PROJECT_3);
    }

    @Test
    public void findAllDistinctFormsByDatatypeFiltersStatusButKeepsDuplicates() {
        final CustomVariableForm sharedForm   = form();
        final CustomVariableForm disabledForm = form();
        link(appliesTo(Scope.Project, PROJECT_1, DATA_TYPE, null), sharedForm, ENABLED);
        link(appliesTo(Scope.Project, PROJECT_2, DATA_TYPE, null), sharedForm, ENABLED);
        link(appliesTo(Scope.Project, PROJECT_3, DATA_TYPE, null), disabledForm, DISABLED);

        // With a status filter, only the enabled rows match; the shared form appears once per matching row because
        // the current implementation discards the result of its distinct filter.
        final List<CustomVariableForm> enabledForms = _repository.findAllDistinctFormsByDatatype(DATA_TYPE, ENABLED);
        assertThat(enabledForms).hasSize(2)
                                .extracting(CustomVariableForm::getId)
                                .containsOnly(sharedForm.getId());

        // Without a status filter all rows for the data type match.
        final List<CustomVariableForm> allForms = _repository.findAllDistinctFormsByDatatype(DATA_TYPE, null);
        assertThat(allForms).hasSize(3)
                            .extracting(CustomVariableForm::getId)
                            .containsExactlyInAnyOrder(sharedForm.getId(), sharedForm.getId(), disabledForm.getId());
    }

    @Test
    public void findByFormIdAndStatusVariants() {
        final CustomVariableAppliesTo appliesTo1 = appliesTo(Scope.Project, PROJECT_1, DATA_TYPE, null);
        final CustomVariableAppliesTo appliesTo2 = appliesTo(Scope.Project, PROJECT_2, DATA_TYPE, null);
        final CustomVariableForm      form1      = form();
        final CustomVariableForm      form2      = form();
        link(appliesTo1, form1, ENABLED);
        link(appliesTo2, form1, DISABLED);
        link(appliesTo1, form2, ENABLED);

        assertThat(_repository.findByFormId(form1.getId())).hasSize(2);
        assertThat(_repository.findByFormId(-1L)).isEmpty();

        final List<CustomVariableFormAppliesTo> enabledRows = _repository.findByFormIdAndStatus(form1.getId(), ENABLED);
        assertThat(enabledRows).hasSize(1);
        assertThat(enabledRows.get(0).getCustomVariableAppliesTo().getId()).isEqualTo(appliesTo1.getId());

        final List<CustomVariableFormAppliesTo> byAppliesTo = _repository.findByAppliesToId(appliesTo1.getId());
        assertThat(byAppliesTo).extracting(row -> row.getCustomVariableForm().getId())
                               .containsExactlyInAnyOrder(form1.getId(), form2.getId());
    }

    @Test
    public void saveOrUpdateCreatesNewRowThenUpdatesExistingRow() {
        final CustomVariableAppliesTo appliesTo1 = appliesTo(Scope.Project, PROJECT_1, DATA_TYPE, null);
        final CustomVariableForm      form1      = form();

        final CustomVariableFormAppliesTo row = new CustomVariableFormAppliesTo();
        row.setCustomVariableAppliesTo(appliesTo1);
        row.setCustomVariableForm(form1);
        row.setStatus(ENABLED);
        row.setXnatUser("admin");
        _repository.saveOrUpdate(row);

        final CustomVariableFormAppliesTo created = _repository.findByRowIdentifier(rowId(form1.getId(), appliesTo1.getId()));
        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo(ENABLED);

        row.setStatus(DISABLED);
        _repository.saveOrUpdate(row);

        assertThat(_repository.findByFormId(form1.getId())).hasSize(1);
        final CustomVariableFormAppliesTo updated = _repository.findByRowIdentifier(rowId(form1.getId(), appliesTo1.getId()));
        assertThat(updated).isNotNull();
        assertThat(updated.getStatus()).isEqualTo(DISABLED);
    }

    private static RowIdentifier rowId(final long formId, final long appliesToId) {
        final RowIdentifier rowId = new RowIdentifier();
        rowId.setFormId(formId);
        rowId.setAppliesToId(appliesToId);
        return rowId;
    }

    private CustomVariableAppliesTo appliesTo(final Scope scope, final String entityId, final String dataType, final String protocol) {
        final CustomVariableAppliesTo appliesTo = new CustomVariableAppliesTo();
        appliesTo.setScope(scope);
        appliesTo.setEntityId(entityId);
        appliesTo.setDataType(dataType);
        appliesTo.setProtocol(protocol);
        _appliesToRepository.create(appliesTo);
        return appliesTo;
    }

    private CustomVariableForm form() {
        final CustomVariableForm form = new CustomVariableForm();
        form.setFormUuid(UUID.randomUUID());
        form.setFormIOJsonDefinition(JsonNodeFactory.instance.objectNode().put("title", "form-" + UUID.randomUUID()));
        _formRepository.create(form);
        return form;
    }

    private CustomVariableFormAppliesTo link(final CustomVariableAppliesTo appliesTo, final CustomVariableForm form, final String status) {
        final CustomVariableFormAppliesTo row = new CustomVariableFormAppliesTo();
        row.setCustomVariableAppliesTo(appliesTo);
        row.setCustomVariableForm(form);
        row.setStatus(status);
        row.setXnatUser("admin");
        _repository.create(row);
        return row;
    }
}
