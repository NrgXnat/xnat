CREATE TABLE test_data
(
    id        INTEGER PRIMARY KEY,
    item_id   VARCHAR(15),
    group_id  VARCHAR(15),
    is_active BOOLEAN
);

INSERT INTO test_data (id, item_id, group_id, is_active)
VALUES (1, 'zero', 'single', TRUE),
       (2, 'one', 'single', FALSE),
       (3, 'two', 'single', TRUE),
       (4, 'three', 'single', FALSE),
       (5, 'four', 'single', TRUE),
       (6, 'five', 'single', FALSE),
       (7, 'six', 'single', TRUE),
       (8, 'seven', 'single', FALSE),
       (9, 'eight', 'single', TRUE),
       (10, 'nine', 'single', FALSE),
       (11, 'ten', 'double', TRUE),
       (12, 'eleven', 'double', FALSE),
       (13, 'twelve', 'double', TRUE),
       (14, 'thirteen', 'double', FALSE),
       (15, 'fourteen', 'double', TRUE),
       (16, 'fifteen', 'double', FALSE),
       (17, 'sixteen', 'double', TRUE),
       (18, 'seventeen', 'double', FALSE),
       (19, 'eighteen', 'double', TRUE),
       (20, 'nineteen', 'double', FALSE);

CREATE ALIAS get_data AS
$$
ResultSet query(final Connection connection, final String groupId) throws SQLException {
    return connection.createStatement().executeQuery("SELECT id, item_id, is_active FROM test_data WHERE group_id = '" + groupId + "'");
}
$$;

-- The alias above works with H2. The equivalent for PL/PGSQL would look like this:
--
-- CREATE OR REPLACE FUNCTION get_data(groupId VARCHAR(255))
--     RETURNS TABLE
--             (
--                 id        INTEGER,
--                 item_id   VARCHAR(255),
--                 is_active BOOLEAN
--             )
-- AS
-- $$
-- BEGIN
--     RETURN QUERY
--         SELECT d.id,
--                d.item_id,
--                d.is_active
--         FROM test_data d
--         WHERE
--             d.group_id = groupId;
-- END
-- $$
--     LANGUAGE plpgsql;
