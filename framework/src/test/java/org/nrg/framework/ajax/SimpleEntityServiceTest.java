/*
 * framework: org.nrg.framework.ajax.SimpleEntityServiceTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.ajax;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.compare.ComparableUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.jcache.JCacheHelper;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.BaseHibernateEntity;
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.utilities.StreamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SimpleEntityServiceTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
public class SimpleEntityServiceTest {
    private static final String             DEFAULT_NAME                   = "Simple Thing";
    private static final String             DEFAULT_USERNAME               = "simple.thing";
    private static final String             DESCRIPTION                    = "This is a very simple thing that we don't care about much";
    private static final int                TOTAL                          = 1234;
    private static final String             NULL_NAME                      = "testNullSimpleEntity";
    private static final String             FOO_NAME                       = "FOO";
    private static final List<Long>         USED_DATES                     = new ArrayList<>();
    private static final Long               VALUE_UPPER_LIMIT              = 20L;
    private static final Long               VALUE_MEDIAN                   = VALUE_UPPER_LIMIT / 2;
    private static final Predicate<Long>    LONG_GREATER_THAN_MEDIAN       = ComparableUtils.gt(VALUE_MEDIAN);
    private static final Predicate<Float>   FLOAT_BETWEEN_50_AND_10000     = ComparableUtils.between(-500.0f, 500.0f);
    private static final Predicate<Double>  DOUBLE_LESS_THAN_OR_EQUAL_TO_0 = ComparableUtils.le(0.0d);
    private static final List<String>       PREFIXES                       = Arrays.asList("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten");
    private static final int                ITEMS_PER_PREFIX               = 100;
    private static final int                TOTAL_ITEMS                    = PREFIXES.size() * ITEMS_PER_PREFIX;
    private static final List<String>       USERNAMES                      = Arrays.asList("Adyson Barrett", "Alani Bean", "Arabella Summers", "Harold Mckinney", "Hayden Wiley", "Jameson Lynn", "Jayvon Rangel", "Julie Higgins", "Reece Burns", "Rosemary Little", "Vincent Murray", "Yadira Santos");
    private static final Random             RANDOM                         = new Random();
    private static final List<SimpleEntity> SORTABLES                      = Arrays.asList(getSimpleEntity("One 1", "one-1", "1", 1),
                                                                                           getSimpleEntity("One 2", "one-2", "1", 2),
                                                                                           getSimpleEntity("One 3", "one-3", "1", 3),
                                                                                           getSimpleEntity("Two 1", "two-1", "2", 1),
                                                                                           getSimpleEntity("Two 2", "two-2", "2", 2),
                                                                                           getSimpleEntity("Two 3", "two-3", "2", 3),
                                                                                           getSimpleEntity("Three 1", "three-1", "3", 1),
                                                                                           getSimpleEntity("Three 2", "three-2", "3", 2),
                                                                                           getSimpleEntity("Three 3", "three-3", "3", 3));
    private static final List<String>       DISTINCT_NAMES                 = Arrays.asList("One 1", "One 2", "One 3", "Two 1", "Two 2", "Two 3", "Three 1", "Three 2", "Three 3");
    private static final List<String>       DISTINCT_USERNAMES             = Arrays.asList("one-1", "one-2", "one-3", "two-1", "two-2", "two-3", "three-1", "three-2", "three-3");
    private static final List<String>       DISTINCT_DESCRIPTIONS          = Arrays.asList("1", "2", "3");
    private static final List<Integer>      DISTINCT_TOTALS                = Arrays.asList(1, 2, 3);
    private static final String[]           SIMPLE_ENTITY_PROPERTIES       = new String[] {"name", "username", "description", "value", "total", "price", "warpSpeed"};

    private final SimpleEntityService _service;
    private final SerializerService   _serializer;
    private final JCacheHelper        _helper;

    @Autowired
    public SimpleEntityServiceTest(final SimpleEntityService service, final SerializerService serializer, final JCacheHelper helper) {
        _service    = service;
        _serializer = serializer;
        _helper     = helper;
    }

    @AfterEach
    public void teardown() {
        _service.getAll().forEach(_service::delete);
    }

    @Test
    public void testCachesExist() {
        final List<String> jCacheCaches = StreamUtils.asStream(_helper.getJavaXCacheManager().getCacheNames().iterator()).collect(Collectors.toList());
        assertThat(jCacheCaches).isNotNull().isNotEmpty().hasSize(2).containsExactlyInAnyOrder("simpleEntityById", "simpleEntityByName");
        _helper.getSpringCache("simpleEntityById").put(1L, SimpleEntity.builder().name("name").username("username").value(12L).description("description").price(3.25f).total(3).warpSpeed(4.23).build());
        final List<String> springCaches = StreamUtils.asStream(_helper.getSpringCacheManager().getCacheNames().iterator()).collect(Collectors.toList());
        assertThat(springCaches).isNotNull().isNotEmpty().contains("simpleEntityById");
        final SimpleEntity entity = (SimpleEntity) _helper.getCache("simpleEntityById").get(1L);
        assertThat(entity).isNotNull().extracting(SimpleEntity::getName, SimpleEntity::getUsername, SimpleEntity::getValue).contains("name", "username", 12L);
        assertThrows(CacheException.class, () -> _helper.getCache("simpleEntityById", Long.class, BaseHibernateEntity.class));
        final Cache<Long, SimpleEntity> simpleEntityByIdCache = _helper.getCache("simpleEntityById", Long.class, SimpleEntity.class);
        log.info("Got cache simpleEntityById, it is a {} instance", simpleEntityByIdCache.getClass());
    }

    @Test
    public void testNullSimpleEntity() {
        assertThrows(ConstraintViolationException.class, () -> {
            final SimpleEntity entity = _service.newEntity();
            entity.setName(NULL_NAME);
            _service.create(entity);
        });
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
        final List<SimpleEntity> entities = SORTABLES.stream().map(_service::create).collect(Collectors.toList());
        assertThat(entities).isNotNull().isNotEmpty().hasSize(SORTABLES.size());

        final SimpleEntityPaginatedRequest request = SimpleEntityPaginatedRequest.builder().pageSize(3).sortBy(Pair.of(PaginatedRequest.SortDir.ASC, "description")).sortBy(Pair.of(PaginatedRequest.SortDir.DESC, "total")).build();

        final List<SimpleEntity> page1 = _service.getPaginated(request);
        request.setPageNumber(2);
        final List<SimpleEntity> page2 = _service.getPaginated(request);
        request.setPageNumber(3);
        final List<SimpleEntity> page3 = _service.getPaginated(request);
        assertThat(page1.stream().map(SimpleEntity::getUsername)).isNotNull().isNotEmpty().hasSize(3).containsExactly("one-3", "one-2", "one-1");
        assertThat(page2.stream().map(SimpleEntity::getUsername)).isNotNull().isNotEmpty().hasSize(3).containsExactly("two-3", "two-2", "two-1");
        assertThat(page3.stream().map(SimpleEntity::getUsername)).isNotNull().isNotEmpty().hasSize(3).containsExactly("three-3", "three-2", "three-1");
    }

    @Test
    public void testQueryByPropertiesAndDistinctAndExample() {
        final List<SimpleEntity> entities = SORTABLES.stream().map(_service::create).collect(Collectors.toList());
        assertThat(entities).isNotNull().isNotEmpty().hasSize(SORTABLES.size());

        final List<SimpleEntity> retrieved = _service.getAll();
        assertThat(retrieved).isNotNull().isNotEmpty().hasSize(SORTABLES.size());

        final SimpleEntity           single = retrieved.get(0);
        final Optional<SimpleEntity> unique = _service.findByIdAndUsername(single.getId(), single.getUsername());
        assertThat(unique).isNotNull().isPresent().get().hasFieldOrPropertyWithValue("id", single.getId()).hasFieldOrPropertyWithValue("username", single.getUsername());

        Set<String> names = _service.distinct(String.class, "name");
        assertThat(names).isNotNull().isNotEmpty().hasSize(DISTINCT_NAMES.size()).containsExactlyInAnyOrderElementsOf(DISTINCT_NAMES);

        Set<String> usernames = _service.distinct(String.class, "username");
        assertThat(usernames).isNotNull().isNotEmpty().hasSize(DISTINCT_USERNAMES.size()).containsExactlyInAnyOrderElementsOf(DISTINCT_USERNAMES);

        Set<String> descriptions = _service.distinct(String.class, "description");
        assertThat(descriptions).isNotNull().isNotEmpty().hasSize(DISTINCT_DESCRIPTIONS.size()).containsExactlyInAnyOrderElementsOf(DISTINCT_DESCRIPTIONS);

        Set<Integer> totals = _service.distinct(Integer.class, "total");
        assertThat(totals).isNotNull().isNotEmpty().hasSize(DISTINCT_TOTALS.size()).containsExactlyInAnyOrderElementsOf(DISTINCT_TOTALS);

        final List<SimpleEntity> retrieved1 = _service.findByExample(SimpleEntity.builder().total(1).build(), getExcludedProperties("total")); // Should match 3
        final List<SimpleEntity> retrieved2 = _service.findByExample(SimpleEntity.builder().description("2").build(), getExcludedProperties("description")); // Should match 3
        final List<SimpleEntity> retrieved3 = _service.findByExample(SimpleEntity.builder().name("One 1").build(), getExcludedProperties("name")); // Should match 1

        assertThat(retrieved1).isNotNull().isNotEmpty().hasSize(3);
        assertThat(retrieved2).isNotNull().isNotEmpty().hasSize(3);
        assertThat(retrieved3).isNotNull().isNotEmpty().hasSize(1);

        List<SimpleEntity> byNamesAndDescription = _service.findByNamesAndDescription(Arrays.asList("Two 1", "Two 2", "Two 3"), "2");
        assertThat(byNamesAndDescription).isNotNull()
                                         .isNotEmpty()
                                         .hasSize(3)
                                         .extracting(SimpleEntity::getName, SimpleEntity::getDescription)
                                         .containsExactlyInAnyOrder(tuple("Two 1", "2"), tuple("Two 2", "2"), tuple("Two 3", "2"));
        List<SimpleEntity> byNamesAndTotal = _service.findByNamesAndTotal(Arrays.asList("Two 1", "Two 2", "Two 3"), 1);
        assertThat(byNamesAndTotal).isNotNull()
                                   .isNotEmpty()
                                   .hasSize(1)
                                   .extracting(SimpleEntity::getName, SimpleEntity::getTotal)
                                   .containsExactlyInAnyOrder(tuple("Two 1", 1));
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
        final List<SimpleEntity> allEntities = IntStream.range(1, 101)
                                                        .mapToObj(index -> _service.create(SimpleEntity.builder()
                                                                                                       .name("Simple " + index)
                                                                                                       .username("simple" + index)
                                                                                                       .description(index % 2 == 0 ? "even" : "odd")
                                                                                                       .value(getRandomValue())
                                                                                                       .total(index)
                                                                                                       .price(RandomUtils.nextFloat(0.0f, 2000.0f) - 1000.f)
                                                                                                       .warpSpeed(getRandomWarpSpeed())
                                                                                                       .build()))
                                                        .collect(Collectors.toList());
        final List<SimpleEntity> evenEntities         = allEntities.stream().filter(entity -> StringUtils.equals(entity.getDescription(), "even")).collect(Collectors.toList());
        final List<SimpleEntity> first50Entities      = allEntities.subList(0, 50);
        final long               allEntitiesCount     = _service.getCount();
        final long               evenEntitiesCount    = _service.getCount("description", "even");
        final Map<Long, Long>    valueCounts          = allEntities.stream().filter(entity -> StringUtils.equals(entity.getDescription(), "odd")).mapToLong(SimpleEntity::getValue).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.summingLong(count -> 1)));
        final Map<Long, Long>    retrievedValueCounts = valueCounts.keySet().stream().collect(Collectors.toMap(Function.identity(), value -> _service.getCount(ImmutableMap.of("description", "odd", "value", value))));

        assertThat(allEntities).isNotNull().isNotEmpty().hasSize(100);
        assertThat(evenEntities).isNotNull().isNotEmpty().hasSize(50);
        assertThat(first50Entities).isNotNull().isNotEmpty().hasSize(50);
        assertThat(allEntitiesCount).isEqualTo(100);
        assertThat(evenEntitiesCount).isEqualTo(50);
        assertThat(retrievedValueCounts).isNotNull().isNotEmpty().containsExactlyEntriesOf(valueCounts);

        final SimpleEntityPaginatedRequest request1 = createRequest("username", HibernateFilter.builder().operator(HibernateFilter.Operator.LIKE).value("simple%").build());
        final SimpleEntityPaginatedRequest request2 = createRequest("description", HibernateFilter.builder().operator(HibernateFilter.Operator.EQ).value("even").build());
        final SimpleEntityPaginatedRequest request3 = createRequest("value", HibernateFilter.builder().operator(HibernateFilter.Operator.GT).value(VALUE_MEDIAN).build());
        final SimpleEntityPaginatedRequest request4 = createRequest("total", HibernateFilter.builder().operator(HibernateFilter.Operator.LE).value(50).build(), PaginatedRequest.SortDir.ASC);
        final SimpleEntityPaginatedRequest request5 = createRequest("price", HibernateFilter.builder().operator(HibernateFilter.Operator.BETWEEN).lo(-500.0f).hi(500.0f).build());
        final SimpleEntityPaginatedRequest request6 = createRequest("warpSpeed", HibernateFilter.builder().operator(HibernateFilter.Operator.LT).value(0.0D).build());

        final List<String> json = Stream.of(request1, request2, request3, request4, request5, request6).map(this::toJson).collect(Collectors.toList());
        assertThat(json).isNotNull().isNotEmpty().hasSize(6);

        final Map<Integer, SimpleEntityPaginatedRequest> requestsByIndex = ImmutableMap.<Integer, SimpleEntityPaginatedRequest>builder()
                                                                                       .put(0, request1)
                                                                                       .put(1, request2)
                                                                                       .put(2, request3)
                                                                                       .put(3, request4)
                                                                                       .put(4, request5)
                                                                                       .put(5, request6)
                                                                                       .build();
        assertThat(requestsByIndex.get(4)).isEqualTo(request5);
        final List<SimpleEntityPaginatedRequest> requests = json.stream().map(this::toRequest).collect(Collectors.toList());
        assertThat(requestsByIndex.get(4)).isEqualTo(requests.get(4));

        assertThat(requests).isNotNull().isNotEmpty().hasSize(6).containsExactlyInAnyOrder(request1, request2, request3, request4, request5, request6);

        final List<SimpleEntity> results1 = _service.getPaginated(request1);
        assertThat(results1).isNotNull().isNotEmpty().hasSize(10).containsExactlyInAnyOrderElementsOf(allEntities.subList(90, 100));

        final List<SimpleEntity> results2 = _service.getPaginated(request2);
        assertThat(results2).isNotNull().isNotEmpty().hasSize(10).containsExactlyInAnyOrderElementsOf(evenEntities.subList(40, 50));

        final List<SimpleEntity> results3 = _service.getPaginated(request3);
        assertThat(results3).isNotNull().isNotEmpty().hasSize(10).extracting("value", Long.class).allMatch(LONG_GREATER_THAN_MEDIAN);

        final List<SimpleEntity> results4 = _service.getPaginated(request4);
        assertThat(results4).isNotNull().isNotEmpty().hasSize(10).containsExactlyInAnyOrderElementsOf(first50Entities.subList(0, 10));

        final List<SimpleEntity> results5 = _service.getPaginated(request5);
        assertThat(results5).isNotNull().isNotEmpty().hasSize(10).extracting("price", Float.class).allMatch(FLOAT_BETWEEN_50_AND_10000);

        final List<SimpleEntity> results6 = _service.getPaginated(request6);
        assertThat(results6).isNotNull().isNotEmpty().hasSize(10).extracting("warpSpeed", Double.class).allMatch(DOUBLE_LESS_THAN_OR_EQUAL_TO_0);
    }

    @Test
    public void testPaging() {
        final List<String>       names    = PREFIXES.stream().map(prefix -> IntStream.range(0, ITEMS_PER_PREFIX).boxed().map(suffix -> prefix + "-" + suffix).collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toList());
        final List<SimpleEntity> entities = names.stream().map(SimpleEntityServiceTest::getSimpleEntity).map(_service::create).collect(Collectors.toList());
        assertThat(entities).isNotNull().isNotEmpty().hasSize(TOTAL_ITEMS);

        final Map<String, Long> expectedCounts = entities.stream().collect(Collectors.groupingBy(SimpleEntity::getUsername, Collectors.counting()));
        assertThat(expectedCounts).isNotNull().isNotEmpty().hasSize(USERNAMES.size()).containsOnlyKeys(USERNAMES);
        assertThat(expectedCounts.values().stream().reduce(0L, Long::sum)).isEqualTo(TOTAL_ITEMS);

        final Map<String, Long> actualCounts = USERNAMES.stream().collect(Collectors.toMap(Function.identity(), _service::getAllForUserCount));
        assertThat(actualCounts).isNotNull().isNotEmpty().containsExactlyInAnyOrderEntriesOf(expectedCounts);
        assertThat(actualCounts.values().stream().reduce(0L, Long::sum)).isEqualTo(TOTAL_ITEMS);

        final List<SimpleEntity> orderedByDate = _service.findAllOrderedByTimestamp();
        assertThat(orderedByDate).isNotNull().isNotEmpty().hasSize(TOTAL_ITEMS);

        final SimpleEntityPaginatedRequest request               = SimpleEntityPaginatedRequest.builder().pageSize(200).build();
        final List<SimpleEntity>           first200OrderedByDate = _service.findAllOrderedByTimestamp(request);
        assertThat(first200OrderedByDate).isNotNull().isNotEmpty().hasSize(200).containsAll(orderedByDate.subList(0, 200));
    }

    @Test
    @Disabled("Support for JSON types across PostgreSQL and H2 doesn't work properly with the current way of configuring json and jsonb columns: see https://github.com/vladmihalcea/hibernate-types/issues/179 for info on possible fixes")
    public void testAttributes() {
        final SimpleEntity entity = buildTestSimpleEntity();
        _service.create(entity);
        final SimpleEntity retrieved = _service.findByName(DEFAULT_NAME);
        assertThat(retrieved).isNotNull()
                             .hasFieldOrPropertyWithValue("name", DEFAULT_NAME)
                             .hasFieldOrPropertyWithValue("description", DESCRIPTION)
                             .hasFieldOrPropertyWithValue("total", TOTAL)
                             .hasFieldOrProperty("attributes");
    }

    private SimpleEntityPaginatedRequest createRequest(String property, HibernateFilter filter) {
        return createRequest(property, filter, PaginatedRequest.SortDir.DESC);
    }

    private SimpleEntityPaginatedRequest createRequest(String property, HibernateFilter filter, PaginatedRequest.SortDir sortDir) {
        return SimpleEntityPaginatedRequest.builder()
                                           .pageNumber(1)
                                           .pageSize(10)
                                           .sortDir(sortDir)
                                           .filtersMap(ImmutableMap.of(property, filter))
                                           .build();
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
        return SimpleEntity.builder()
                           .name(DEFAULT_NAME)
                           .username(DEFAULT_USERNAME)
                           .description(DESCRIPTION)
                           .value(getRandomValue())
                           .total(TOTAL)
                           .price(getRandomPrice())
                           .warpSpeed(getRandomWarpSpeed())
                           .build();
    }

    private static SimpleEntity getSimpleEntity(final String name) {
        return getSimpleEntity(name, null, null, null);
    }

    private static SimpleEntity getSimpleEntity(final String name, final String username, final String description, final Integer total) {
        Date uniqueRandomDate = getUniqueRandomDate();
        return SimpleEntity.builder()
                           .name(name)
                           .username(StringUtils.getIfBlank(username, () -> USERNAMES.get(RANDOM.nextInt(USERNAMES.size()))))
                           .description(StringUtils.getIfBlank(description, () -> RandomStringUtils.randomAscii(10, RANDOM.nextInt(30) + 11)))
                           .value(getRandomValue())
                           .total(ObjectUtils.getIfNull(total, () -> RANDOM.nextInt(1000) + 1))
                           .price(getRandomPrice())
                           .warpSpeed(getRandomWarpSpeed())
                           .created(uniqueRandomDate)
                           .timestamp(uniqueRandomDate)
                           .build();
    }

    private static long getRandomValue() {
        return RandomUtils.nextLong(0L, VALUE_UPPER_LIMIT);
    }

    private static float getRandomPrice() {
        return RandomUtils.nextFloat(0.0f, 2000.0f) - 1000.0f;
    }

    private static double getRandomWarpSpeed() {
        return RandomUtils.nextDouble(0, 200000.0d) - 100000.0d;
    }

    private static Date getUniqueRandomDate() {
        final Date date = Stream.generate(SimpleEntityServiceTest::getRandomDate).filter(generated -> !USED_DATES.contains(generated.getTime())).findFirst().orElseThrow(() -> new RuntimeException("This shouldn't happen tbh"));
        USED_DATES.add(date.getTime());
        log.info("Got random date: {}", date.getTime());
        return date;
    }

    private static Date getRandomDate() {
        return Date.from(Instant.now().minus(Duration.ofDays(RANDOM.nextInt(365))));
    }

    private String[] getExcludedProperties(final String... includes) {
        final List<String> list = Arrays.asList(includes);
        return Arrays.stream(AbstractHibernateEntity.getExcludedProperties(SIMPLE_ENTITY_PROPERTIES)).filter(include -> !list.contains(include)).toArray(String[]::new);
    }
}
