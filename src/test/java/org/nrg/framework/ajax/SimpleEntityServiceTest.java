/*
 * framework: org.nrg.framework.ajax.SimpleEntityServiceTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.ajax;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.assertj.core.api.ObjectAssert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.services.SerializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SimpleEntityServiceTestConfiguration.class)
@Transactional
public class SimpleEntityServiceTest {
    @Test(expected = ConstraintViolationException.class)
    public void testNullSimpleEntity() {
        final SimpleEntity entity = _service.newEntity();
        entity.setName(NULL_NAME);
        _service.create(entity);
    }

    @Test
    public void testPaginatedRequestPropertiesPopulated() {
        final SimpleEntityPaginatedRequest fromConstructor = new SimpleEntityPaginatedRequest();
        fromConstructor.setSortColumn("description");
        fromConstructor.setSortDir(PaginatedRequest.SortDir.ASC);

        final ObjectAssert<SimpleEntityPaginatedRequest> fromConstructorAssert = assertThat(fromConstructor).hasFieldOrPropertyWithValue("sortColumn", "description").hasFieldOrPropertyWithValue("sortDir", PaginatedRequest.SortDir.ASC).hasFieldOrProperty("filtersMap").hasFieldOrProperty("sortBys");
        fromConstructorAssert.extracting("filtersMap").isNotNull();
        fromConstructorAssert.extracting("sortBys").isNotNull();
        final ObjectAssert<?> fromConstructorCloneAssert = assertThat(fromConstructor.toBuilder().build()).hasFieldOrPropertyWithValue("sortColumn", "description").hasFieldOrPropertyWithValue("sortDir", PaginatedRequest.SortDir.ASC).hasFieldOrProperty("filtersMap").hasFieldOrProperty("sortBys");
        fromConstructorCloneAssert.extracting("filtersMap").isNotNull();
        fromConstructorCloneAssert.extracting("filtersMap").isNotNull();

        final SimpleEntityPaginatedRequest fromBasicBuilder = SimpleEntityPaginatedRequest.builder().sortColumn("description").sortDir(PaginatedRequest.SortDir.ASC).build();

        final ObjectAssert<SimpleEntityPaginatedRequest> fromBasicBuilderAssert = assertThat(fromBasicBuilder).hasFieldOrPropertyWithValue("sortColumn", "description").hasFieldOrPropertyWithValue("sortDir", PaginatedRequest.SortDir.ASC).hasFieldOrProperty("filtersMap").hasFieldOrProperty("sortBys");
        fromBasicBuilderAssert.extracting("filtersMap").isNotNull();
        fromBasicBuilderAssert.extracting("sortBys").isNotNull();
        final ObjectAssert<?> fromBasicBuilderCloneAssert = assertThat(fromBasicBuilder.toBuilder().build()).hasFieldOrPropertyWithValue("sortColumn", "description").hasFieldOrPropertyWithValue("sortDir", PaginatedRequest.SortDir.ASC).hasFieldOrProperty("filtersMap").hasFieldOrProperty("sortBys");
        fromBasicBuilderCloneAssert.extracting("filtersMap").isNotNull();
        fromBasicBuilderCloneAssert.extracting("filtersMap").isNotNull();
    }

    @Test
    public void testMultiColumnSort() {
        Collections.shuffle(SORTABLES);
        final List<SimpleEntity> entities = SORTABLES.stream().map(properties -> _service.create(SimpleEntity.builder().name(properties.getLeft()).description(properties.getMiddle()).total(properties.getRight()).build())).collect(Collectors.toList());
        assertThat(entities).isNotNull().isNotEmpty().hasSize(SORTABLES.size());

        final SimpleEntityPaginatedRequest request = SimpleEntityPaginatedRequest.builder().pageSize(3).sortBy(Pair.of("description", PaginatedRequest.SortDir.ASC)).sortBy(Pair.of("total", PaginatedRequest.SortDir.DESC)).build();

        final List<SimpleEntity> page1 = _service.getPaginated(request);
        request.setPageNumber(2);
        final List<SimpleEntity> page2 = _service.getPaginated(request);
        request.setPageNumber(3);
        final List<SimpleEntity> page3 = _service.getPaginated(request);
        assertThat(page1.stream().map(SimpleEntity::getName)).isNotNull().isNotEmpty().hasSize(3).containsExactly("one-3", "one-2", "one-1");
        assertThat(page2.stream().map(SimpleEntity::getName)).isNotNull().isNotEmpty().hasSize(3).containsExactly("two-3", "two-2", "two-1");
        assertThat(page3.stream().map(SimpleEntity::getName)).isNotNull().isNotEmpty().hasSize(3).containsExactly("three-3", "three-2", "three-1");
    }

    @Test
    public void testAllServiceMethods() {
        final List<SimpleEntity> getAll1 = _service.getAll();
        assertThat(getAll1).isNotNull().isEmpty();

        final SimpleEntity simple1 = buildTestSimpleEntity();
        _service.create(simple1);

        final List<SimpleEntity> getAll2 = _service.getAll();
        assertThat(getAll2).isNotNull().hasSize(1);

        final SimpleEntity simple2 = _service.findByName(DEFAULT_NAME);
        assertThat(simple2).isNotNull().hasFieldOrPropertyWithValue("name", DEFAULT_NAME);

        simple2.setName(FOO_NAME);
        _service.update(simple2);

        final SimpleEntity simple3 = _service.retrieve(simple2.getId());
        assertThat(simple3).isNotNull().hasFieldOrPropertyWithValue("name", FOO_NAME);

        _service.delete(simple3);

        final List<SimpleEntity> getAll3 = _service.getAll();
        assertThat(getAll3).isNotNull().isEmpty();
    }

    @Test
    public void testQueries() {
        final SimpleEntity simple1 = buildTestSimpleEntity();
        _service.create(simple1);

        final List<SimpleEntity> getAll1 = _service.getAll();
        assertThat(getAll1).isNotNull().hasSize(1);

        assertThat(_service.exists("description", DESCRIPTION)).isTrue();
        assertThat(_service.exists(ImmutableMap.of("name", DEFAULT_NAME, "description", DESCRIPTION, "total", TOTAL))).isTrue();
        assertThat(_service.exists(ImmutableMap.of("name", "garbage", "description", DESCRIPTION, "total", TOTAL))).isFalse();
    }

    @Test
    public void testPaginatedAndSerialized() {
        final List<SimpleEntity> allEntities     = IntStream.range(1, 101).mapToObj(index -> _service.create("simple" + index, index % 2 == 0 ? "even" : "odd", index)).collect(Collectors.toList());
        final List<SimpleEntity> evenEntities    = allEntities.stream().filter(entity -> StringUtils.equals(entity.getDescription(), "even")).collect(Collectors.toList());
        final List<SimpleEntity> first50Entities = allEntities.subList(0, 50);

        assertThat(allEntities).isNotNull().isNotEmpty().hasSize(100);
        assertThat(evenEntities).isNotNull().isNotEmpty().hasSize(50);
        assertThat(first50Entities).isNotNull().isNotEmpty().hasSize(50);

        final SimpleEntityPaginatedRequest request1 = new SimpleEntityPaginatedRequest();
        request1.setPageSize(10);
        request1.setPageNumber(1);
        request1.setFiltersMap(ImmutableMap.of("name", HibernateFilter.builder().operator(HibernateFilter.Operator.LIKE).value("simple%").build()));
        final SimpleEntityPaginatedRequest request2 = new SimpleEntityPaginatedRequest();
        request2.setPageSize(10);
        request2.setPageNumber(1);
        request2.setFiltersMap(ImmutableMap.of("description", HibernateFilter.builder().operator(HibernateFilter.Operator.EQ).value("even").build()));
        final SimpleEntityPaginatedRequest request3 = new SimpleEntityPaginatedRequest();
        request3.setPageSize(10);
        request3.setPageNumber(1);
        request3.setSortDir(PaginatedRequest.SortDir.ASC);
        request3.setFiltersMap(ImmutableMap.of("total", HibernateFilter.builder().operator(HibernateFilter.Operator.LE).value(50).build()));

        final List<String> json = Stream.of(request1, request2, request3).map(this::toJson).collect(Collectors.toList());
        assertThat(json).isNotNull().isNotEmpty().hasSize(3);

        final List<SimpleEntityPaginatedRequest> requests = json.stream().map(this::toRequest).collect(Collectors.toList());
        assertThat(requests).isNotNull().isNotEmpty().hasSize(3).containsExactlyInAnyOrder(request1, request2, request3);

        final List<SimpleEntity> results1 = _service.getPaginated(request1);
        assertThat(results1).isNotNull().isNotEmpty().hasSize(10).containsExactlyInAnyOrderElementsOf(allEntities.subList(90, 100));

        final List<SimpleEntity> results2 = _service.getPaginated(request2);
        assertThat(results2).isNotNull().isNotEmpty().hasSize(10).containsExactlyInAnyOrderElementsOf(evenEntities.subList(40, 50));

        final List<SimpleEntity> results3 = _service.getPaginated(request3);
        assertThat(results3).isNotNull().isNotEmpty().hasSize(10).containsExactlyInAnyOrderElementsOf(first50Entities.subList(0, 10));
    }

    @Test
    @Ignore("Support for JSON types across PostgreSQL and H2 doesn't work properly with the current way of configuring json and jsonb columns: see https://github.com/vladmihalcea/hibernate-types/issues/179 for info on possible fixes")
    public void testAttributes() {
        final SimpleEntity entity = buildTestSimpleEntity();
        // entity.setAttributes(_serializer.deserializeJson(JSON));
        _service.create(entity);
        final SimpleEntity retrieved = _service.findByName(DEFAULT_NAME);
        assertThat(retrieved).isNotNull()
                             .hasFieldOrPropertyWithValue("name", DEFAULT_NAME)
                             .hasFieldOrPropertyWithValue("description", DESCRIPTION)
                             .hasFieldOrPropertyWithValue("total", TOTAL)
                             .hasFieldOrProperty("attributes");
        // final JsonNode            attributes = retrieved.getAttributes();
        // final Map<String, String> map        = _serializer.getObjectMapper().convertValue(attributes, TYPE_REF_MAP_STRING_STRING);
        // assertThat(map).isNotNull().hasSize(3).containsOnlyKeys("1", "2", "3").containsEntry("1", "one").containsEntry("2", "two").containsEntry("3", "three");
    }

    private String toJson(final PaginatedRequest request) {
        try {
            return _serializer.toJson(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SimpleEntityPaginatedRequest toRequest(final String json) {
        try {
            return _serializer.deserializeJson(json, SimpleEntityPaginatedRequest.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SimpleEntity buildTestSimpleEntity() {
        final SimpleEntity pacs = new SimpleEntity();
        pacs.setName(DEFAULT_NAME);
        pacs.setDescription(DESCRIPTION);
        pacs.setTotal(TOTAL);
        return pacs;
    }

    private static final String DEFAULT_NAME = "Simple Thing";
    private static final String DESCRIPTION  = "This is a very simple thing that we don't care about much";
    private static final int    TOTAL        = 1234;
    private static final String NULL_NAME    = "testNullSimpleEntity";
    private static final String FOO_NAME     = "FOO";
    // private static final String JSON         = "{\"1\": \"one\", \"2\": \"two\", \"3\": \"three\"}";

    private static final List<Triple<String, String, Integer>> SORTABLES = Arrays.asList(Triple.of("one-1", "1", 1),
                                                                                         Triple.of("one-2", "1", 2),
                                                                                         Triple.of("one-3", "1", 3),
                                                                                         Triple.of("two-1", "2", 1),
                                                                                         Triple.of("two-2", "2", 2),
                                                                                         Triple.of("two-3", "2", 3),
                                                                                         Triple.of("three-1", "3", 1),
                                                                                         Triple.of("three-2", "3", 2),
                                                                                         Triple.of("three-3", "3", 3));

    @Autowired
    private SimpleEntityService _service;

    @Autowired
    private SerializerService _serializer;
}
