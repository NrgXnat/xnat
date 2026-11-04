package org.nrg.xft.db;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.support.GenericApplicationContext;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A save takes its new primary keys from the item's sequence in batches that grow within the save: one value, then
 * four, then sixteen at a time. Each value is used once, in ascending order, and two saves running at once never get
 * the same value. Values fetched and not used are lost, as with any sequence, and a new save starts again at one.
 * Needs Docker; skipped without it.
 */
public class DBItemCacheSequenceTest {
    private static final String TABLE    = "seq_item";
    private static final String PK       = "id";
    private static final String SEQUENCE = TABLE + "_" + PK + "_seq";
    private static final String OTHER    = "seq_other_id_seq";

    private static PostgreSQLContainer<?> postgres;
    private static DataSource             dataSource;

    @BeforeClass
    public static void startDatabase() throws Exception {
        Assume.assumeTrue("Docker is needed for the PostgreSQL container", DockerClientFactory.instance().isDockerAvailable());
        postgres = new PostgreSQLContainer<>("postgres:12");
        postgres.start();

        final PGSimpleDataSource pg = new PGSimpleDataSource();
        pg.setUrl(postgres.getJdbcUrl());
        pg.setUser(postgres.getUsername());
        pg.setPassword(postgres.getPassword());
        dataSource = pg;

        // PoolDBUtils takes its connections from XDAT.getDataSource(), which looks the bean up through the
        // ContextService bean of the current context (Spring hands it the context on refresh) and keeps it.
        final GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(DataSource.class, () -> pg);
        context.registerBean(ContextService.class);
        context.refresh();
        resetXdatDataSource();
    }

    @AfterClass
    public static void stopDatabase() throws Exception {
        if (postgres != null) {
            ContextService.getInstance().setApplicationContext(null);
            resetXdatDataSource();
            postgres.stop();
        }
    }

    @Before
    public void createSequences() throws SQLException {
        execute("DROP SEQUENCE IF EXISTS " + SEQUENCE, "DROP SEQUENCE IF EXISTS " + OTHER,
                "CREATE SEQUENCE " + SEQUENCE, "CREATE SEQUENCE " + OTHER);
    }

    @Test
    public void aSaveFetchesOneValueThenFourThenSixteenAtATime() throws Exception {
        final DBItemCache  save   = new DBItemCache(null, null);
        final List<Object> values = new ArrayList<>();
        final List<Long>   issued = new ArrayList<>();
        for (int i = 0; i < 38; i++) {
            values.add(next(save, SEQUENCE));
            issued.add(lastValue(SEQUENCE));
        }

        assertEquals("each value should be used once, in order", longs(1, 38), values);
        // The sequence moves only when a batch is fetched: at the 1st value (1), the 2nd (4 more), the 6th (16 more),
        // the 22nd (16 more, the cap) and the 38th.
        assertEquals(Long.valueOf(1), issued.get(0));
        assertEquals(Long.valueOf(5), issued.get(1));
        assertEquals(Long.valueOf(5), issued.get(4));
        assertEquals(Long.valueOf(21), issued.get(5));
        assertEquals(Long.valueOf(21), issued.get(20));
        assertEquals(Long.valueOf(37), issued.get(21));
        assertEquals(Long.valueOf(37), issued.get(36));
        assertEquals(Long.valueOf(53), issued.get(37));
    }

    @Test
    public void aResetSaveStartsAgainAtOneValue() throws Exception {
        final DBItemCache save = new DBItemCache(null, null);
        next(save, SEQUENCE);
        next(save, SEQUENCE);   // fetches 2 to 5

        save.reset();

        assertEquals("the values left from the earlier batch are not reused", 6L, ((Number) next(save, SEQUENCE)).longValue());
        assertEquals("a reset save fetches one value first", 6L, lastValue(SEQUENCE));
    }

    @Test
    public void eachSequenceHasItsOwnBatch() throws Exception {
        final DBItemCache save = new DBItemCache(null, null);
        next(save, SEQUENCE);
        next(save, SEQUENCE);   // the table's batch is now four
        next(save, OTHER);

        assertEquals("another table's sequence starts at one value", 1L, lastValue(OTHER));
        assertEquals(5L, lastValue(SEQUENCE));
    }

    @Test
    public void twoSavesAtOnceNeverGetTheSameValue() throws Exception {
        final DBItemCache  first     = new DBItemCache(null, null);
        final DBItemCache  second    = new DBItemCache(null, null);
        final List<Object> fromFirst = new ArrayList<>();
        final List<Object> fromSecond = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            fromFirst.add(next(first, SEQUENCE));
            if (i % 3 == 0) {
                fromSecond.add(next(second, SEQUENCE));
            }
        }

        final Set<Object> all = new HashSet<>(fromFirst);
        all.addAll(fromSecond);
        assertEquals("no value should be issued twice", fromFirst.size() + fromSecond.size(), all.size());
        assertAscending(fromFirst);
        assertAscending(fromSecond);
    }

    @Test
    public void anUnknownSequenceNameFallsBackToTheTablesSequence() throws Exception {
        // A PoolDBUtils closes its connection when a query is done, so each fetch takes a new one, as a save does.
        assertEquals(longs(1, 4), new PoolDBUtils().getNextIDs(null, TABLE, PK, "no_such_seq", 4));
        assertEquals(longs(5, 5), new PoolDBUtils().getNextIDs(null, TABLE, PK, "no_such_seq", 1));
        assertEquals("without a sequence name, the table's sequence is used", longs(6, 9), new PoolDBUtils().getNextIDs(null, TABLE, PK, null, 4));
    }

    private static Object next(final DBItemCache save, final String sequence) throws Exception {
        return save.nextSequenceValue(new PoolDBUtils(), null, sequence.equals(SEQUENCE) ? TABLE : "seq_other", PK, sequence);
    }

    private static long lastValue(final String sequence) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT last_value FROM " + sequence)) {
            result.next();
            return result.getLong(1);
        }
    }

    private static List<Object> longs(final long from, final long to) {
        final List<Object> values = new ArrayList<>();
        for (long value = from; value <= to; value++) {
            values.add(value);
        }
        return values;
    }

    private static void assertAscending(final List<Object> values) {
        for (int i = 1; i < values.size(); i++) {
            assertTrue("values should come in ascending order: " + values, ((Number) values.get(i - 1)).longValue() < ((Number) values.get(i)).longValue());
        }
    }

    private static void execute(final String... sql) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (final String s : sql) {
                statement.execute(s);
            }
        }
    }

    private static void resetXdatDataSource() throws ReflectiveOperationException {
        final Field field = XDAT.class.getDeclaredField("_dataSource");
        field.setAccessible(true);
        field.set(null, null);
    }
}
