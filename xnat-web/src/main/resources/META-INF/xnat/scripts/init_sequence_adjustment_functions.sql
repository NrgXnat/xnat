--
-- web: src/main/resources/META-INF/xnat/scripts/init_sequence_adjustment_functions.sql
-- XNAT http://www.xnat.org
-- Copyright (c) 2020, Washington University School of Medicine and Howard Hughes Medical Institute
-- All Rights Reserved
--  
-- Released under the Simplified BSD.
--

DROP FUNCTION IF EXISTS public.adjust_sequences();
DROP FUNCTION IF EXISTS public.get_maladjusted_sequences();
DROP FUNCTION IF EXISTS public.get_table_pk_max_and_next_value();
DROP FUNCTION IF EXISTS public.get_table_max_pk_value(table_name TEXT, primary_key TEXT);
DROP VIEW IF EXISTS public.table_primary_keys_and_sequences;;
DROP FUNCTION IF EXISTS public.get_sequences_and_last_values();
DROP FUNCTION IF EXISTS public.get_sequence_last_value(sequence_name TEXT, schema_name TEXT);
DROP FUNCTION IF EXISTS public.get_server_version();

-- Gets the server version as a simple float: 9.6, 10.7, etc.
CREATE OR REPLACE FUNCTION public.get_server_version()
  RETURNS FLOAT AS
$$
DECLARE
  version FLOAT;
BEGIN
  SELECT
    CAST((split_part(server_version, '.', 1) || '.' || split_part(server_version, '.', 2)) AS FLOAT) FROM (SELECT split_part(setting, ' ', 1) AS server_version FROM pg_settings WHERE name = 'server_version') AS trimmed INTO version;
  RETURN version;
END
$$
  LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.get_sequence_last_value(sequence_name TEXT, schema_name TEXT DEFAULT 'public')
  RETURNS BIGINT AS
$$
DECLARE
  last_value BIGINT;
BEGIN
  IF get_server_version() < 10.0
  THEN
    EXECUTE format('SELECT CASE WHEN (start_value >= last_value) THEN start_value WHEN last_value IS NULL THEN 0 ELSE last_value END AS LAST_COUNT from %I', sequence_name) INTO last_value;
  ELSE
    EXECUTE format('SELECT CASE WHEN (start_value >= last_value) THEN start_value WHEN last_value IS NULL THEN 0 ELSE last_value END AS LAST_COUNT FROM pg_sequences WHERE schemaname = %L AND sequencename = %L', schema_name, sequence_name) INTO last_value;
  END IF;
  RETURN last_value;
END
$$
  LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.get_sequences_and_last_values()
  RETURNS TABLE (
                  sequence_name  TEXT,
                  next_seq_value BIGINT
                ) AS
$$
DECLARE
  seq_query VARCHAR(255);
BEGIN
  IF (SELECT get_server_version()) < 10.0
  THEN
    seq_query := 'SELECT c.relname::TEXT AS sequencename, get_sequence_last_value(c.relname) AS last_value FROM pg_class c WHERE c.relkind = ''S''';
  ELSE
    seq_query := 'SELECT sequencename::TEXT, get_sequence_last_value(sequencename) FROM pg_sequences';
  END IF;

  RETURN QUERY EXECUTE seq_query;
END
$$
  LANGUAGE plpgsql;

CREATE OR REPLACE VIEW public.table_primary_keys_and_sequences AS
  WITH
    tables_and_key_columns AS (
      SELECT DISTINCT
        t.table_catalog AS table_catalog,
        t.table_schema AS table_schema,
        t.table_name AS table_name,
        k.column_name AS column_name,
        cols.data_type AS data_type,
        c.constraint_type AS constraint_type
      FROM
        information_schema.tables t
          LEFT JOIN information_schema.table_constraints c ON t.table_name = c.table_name
          LEFT JOIN information_schema.key_column_usage k ON c.constraint_name = k.constraint_name
          LEFT JOIN information_schema.columns cols ON t.table_name = cols.table_name AND k.column_name = cols.column_name
      WHERE
          c.constraint_type = 'PRIMARY KEY' AND
          cols.data_type IN ('integer', 'bigint') AND
        k.column_name IS NOT NULL),
    table_sequences AS (
      WITH
        all_sequences AS (SELECT sequence_name FROM get_sequences_and_last_values())
      SELECT
        tab.relname AS table_name,
        seq.relname AS sequence_name
      FROM
        pg_class seq
          JOIN pg_namespace seq_ns ON seq.relnamespace = seq_ns.oid
          JOIN pg_depend d ON d.objid = seq.oid AND d.deptype = 'a'
          JOIN pg_class tab ON d.objid = seq.oid AND d.refobjid = tab.oid
          JOIN pg_namespace tab_ns ON tab.relnamespace = tab_ns.oid
      WHERE
          seq.relname IN (SELECT sequence_name FROM all_sequences)
      ORDER BY
        table_name,
        sequence_name)
  SELECT
    pk_tables.table_catalog AS table_catalog,
    pk_tables.table_schema AS table_schema,
    pk_tables.table_name AS table_name,
    pk_tables.column_name AS column_name,
    seqs.sequence_name AS sequence_name
  FROM
    (SELECT DISTINCT
       table_catalog,
       table_schema,
       table_name,
       column_name,
       data_type,
       constraint_type,
       count(table_name) AS key_count
     FROM
       tables_and_key_columns
     WHERE
         constraint_type = 'PRIMARY KEY'
     GROUP BY
       table_catalog,
       table_schema,
       table_name,
       column_name,
       data_type,
       constraint_type) AS pk_tables
      LEFT JOIN table_sequences seqs ON pk_tables.table_name = seqs.table_name
  WHERE
      key_count = 1 AND
    sequence_name IS NOT NULL;

CREATE OR REPLACE FUNCTION public.get_table_max_pk_value(table_name TEXT, primary_key TEXT)
  RETURNS BIGINT AS
$$
DECLARE
  max_value BIGINT;
BEGIN
  EXECUTE format('SELECT max(%I) AS max_value FROM %I', primary_key, table_name) INTO max_value;
  RETURN max_value;
END
$$
  LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.get_table_pk_max_and_next_value()
  RETURNS TABLE (
                  table_name     TEXT,
                  column_name    TEXT,
                  sequence_name  TEXT,
                  max_key_value  BIGINT,
                  next_seq_value BIGINT
                ) AS
$$
DECLARE
BEGIN
  RETURN QUERY
    WITH sequence_data AS (SELECT
                             tpkas.table_catalog::TEXT AS table_catalog,
                             tpkas.table_schema::TEXT AS table_schema,
                             tpkas.table_name::TEXT AS table_name,
                             tpkas.column_name::TEXT AS column_name,
                             tpkas.sequence_name::TEXT AS sequence_name
                           FROM
                             table_primary_keys_and_sequences tpkas)
    SELECT
      tpkas.table_name,
      tpkas.column_name,
      tpkas.sequence_name,
      get_table_max_pk_value(tpkas.table_name, tpkas.column_name) AS max_value,
      s.next_seq_value
    FROM
      sequence_data tpkas
        LEFT JOIN get_sequences_and_last_values() s ON tpkas.sequence_name = s.sequence_name;
END
$$
  LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.get_maladjusted_sequences()
  RETURNS TABLE (
                  table_name     TEXT,
                  column_name    TEXT,
                  sequence_name  TEXT,
                  max_key_value  BIGINT,
                  next_seq_value BIGINT
                ) AS
$$
DECLARE
BEGIN
  RETURN QUERY SELECT
                 v.table_name,
                 v.column_name,
                 v.sequence_name,
                 v.max_key_value,
                 v.next_seq_value
               FROM
                 get_table_pk_max_and_next_value() v
               WHERE
                 v.max_key_value IS NOT NULL AND
                   v.max_key_value > v.next_seq_value;
END
$$
  LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.adjust_sequences()
  RETURNS INTEGER AS
$$
DECLARE
  adjusted_sequences INTEGER = 0;
  current_row        RECORD;
BEGIN
  FOR current_row IN SELECT * FROM get_maladjusted_sequences()
    LOOP
      adjusted_sequences := adjusted_sequences + 1;
      EXECUTE format('SELECT setval(''%s'', %L)', current_row.sequence_name, current_row.max_key_value);
    END LOOP;
  RETURN adjusted_sequences;
END
$$
  LANGUAGE plpgsql;
