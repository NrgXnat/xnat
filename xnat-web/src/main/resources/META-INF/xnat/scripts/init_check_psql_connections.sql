--
-- web: src/main/resources/META-INF/xnat/scripts/init_check_psql_connections.sql
-- XNAT http://www.xnat.org
-- Copyright (c) 2020, Washington University School of Medicine and Howard Hughes Medical Institute
-- All Rights Reserved
--  
-- Released under the Simplified BSD.
--

DROP FUNCTION IF EXISTS public.activity_summary(dbName VARCHAR);
DROP FUNCTION IF EXISTS public.activity_entries(dbName VARCHAR);
DROP FUNCTION IF EXISTS public.terminate_connection(pid INTEGER);
DROP FUNCTION IF EXISTS public.terminate_all_connections(dbName VARCHAR);

CREATE OR REPLACE FUNCTION public.activity_summary(dbName VARCHAR(255) DEFAULT 'xnat')
    RETURNS TABLE (
        total_connections BIGINT,
        applications      TEXT[],
        ip_addresses      INET[],
        users             VARCHAR[])
    LANGUAGE plpgsql
AS
$_$
BEGIN
    RETURN QUERY
        WITH
            activity AS (SELECT * FROM pg_stat_activity WHERE datname = dbName)
        SELECT
                (SELECT count(*) FROM activity),
                (SELECT
                     array_replace(array(SELECT
                                             CASE WHEN application_name = '' THEN '<empty>' ELSE application_name END || ': ' || connection_count
                                         FROM
                                             (SELECT
                                                  application_name,
                                                  count(application_name) AS connection_count
                                              FROM
                                                  activity
                                              GROUP BY
                                                  application_name) connections), '', '<empty>')),
                (SELECT array(SELECT DISTINCT client_addr FROM activity)),
                (SELECT array(SELECT DISTINCT usename::VARCHAR(255) FROM activity));
END
$_$;

CREATE OR REPLACE FUNCTION public.activity_entries(dbName VARCHAR(255) DEFAULT 'xnat')
    RETURNS TABLE (
        username         VARCHAR(255),
        ip_address       VARCHAR(255),
        pid              INTEGER,
        application_name TEXT,
        state            TEXT,
        last_query       TEXT,
        state_change     TIMESTAMPTZ,
        wait_event       TEXT)
    LANGUAGE plpgsql
AS
$_$
BEGIN
    RETURN QUERY
        SELECT
            a.usename::VARCHAR(255),
            coalesce(client_addr::VARCHAR(255), 'local')::VARCHAR(255),
            a.pid,
            CASE WHEN a.application_name = '' THEN '<empty>' ELSE a.application_name END,
            a.state,
            a.query,
            a.state_change,
            a.wait_event
        FROM
            pg_stat_activity a
        WHERE
            a.datname = dbName;
END
$_$;

CREATE OR REPLACE FUNCTION public.terminate_connection(pid INTEGER)
    RETURNS BOOLEAN
    LANGUAGE plpgsql
AS
$_$
DECLARE
    return_status BOOLEAN;
BEGIN
    SELECT
        pg_terminate_backend(pid)
    INTO
        return_status;
    RETURN return_status;
END
$_$;

CREATE OR REPLACE FUNCTION public.terminate_all_connections(dbName VARCHAR(255))
    RETURNS BOOLEAN
    LANGUAGE plpgsql
AS
$_$
DECLARE
    return_status BOOLEAN;
BEGIN
    SELECT
        pg_terminate_backend(pid)
    FROM
        pg_stat_activity
    WHERE
        datname = dbName
    INTO return_status;
    RETURN return_status;
END
$_$;

