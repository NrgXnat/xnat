--
-- web: src/main/resources/META-INF/xnat/scripts/init_schema_maintenance_functions.sql
-- XNAT http://www.xnat.org
-- Copyright (c) 2020, Washington University School of Medicine and Howard Hughes Medical Institute
-- All Rights Reserved
--  
-- Released under the Simplified BSD.
--
-- The table and functions here are adapted from a contribution on stackoverflow.com by user Hambone:
--
--   https://stackoverflow.com/a/49000321/165462
--
-- PostgreSQL won't allow you to alter a table as long as any functions or views reference that table.
-- To work around this, you can store and remove all referencing views and functions by calling the
-- dependencies_save_and_drop() function, altering the table as needed, then restoring the views and
-- functions by calling the dependencies_restore() function:
--
--   xnat=> SELECT * FROM xdat_search.dependencies_save_and_drop('public', 'xnat_imagescandata');
--   dependencies_save_and_drop
--   ----------------------------
--   
--   (1 row)
--   
--   xnat=> ALTER TABLE xnat_imagescandata ALTER COLUMN note TYPE TEXT;
--   ALTER TABLE
--   xnat=> SELECT * FROM xdat_search.dependencies_restore('public', 'xnat_imagescandata');
--   dependencies_restore
--   ----------------------
--   
--   (1 row)
--   

DROP FUNCTION IF EXISTS public.dependencies_clear_cache_entries(NAME, NAME, NAME);
DROP FUNCTION IF EXISTS public.dependencies_restore(NAME, NAME);
DROP FUNCTION IF EXISTS public.dependencies_save_and_drop(NAME, NAME);
DROP FUNCTION IF EXISTS public.dependencies_identify(NAME, NAME);
DROP TABLE IF EXISTS xdat_search.dependencies_saved_ddl;

CREATE TABLE xdat_search.dependencies_saved_ddl (
    id          SERIAL NOT NULL PRIMARY KEY,
    view_schema CHARACTER VARYING(255),
    view_name   CHARACTER VARYING(255),
    ddl_to_run  TEXT
);

CREATE OR REPLACE FUNCTION public.dependencies_identify(pViewName NAME, pViewSchema NAME DEFAULT 'public')
    RETURNS TABLE (
        view_schema CHARACTER VARYING(255),
        view_name   CHARACTER VARYING(255),
        view_type   CHARACTER VARYING(255)
    )
    LANGUAGE plpgsql
    COST 100
AS
$_$
BEGIN
    RETURN QUERY
        SELECT
            obj_schema::VARCHAR,
            obj_name::VARCHAR,
            obj_type::VARCHAR
        FROM
            (WITH
                 RECURSIVE
                 recursive_deps(obj_schema, obj_name, obj_type, depth) AS
                     (SELECT
                          pViewSchema,
                          pViewName,
                          NULL::VARCHAR,
                          0
                      UNION
                      SELECT
                          dep_schema::VARCHAR,
                          dep_name::VARCHAR,
                          dep_type::VARCHAR,
                          recursive_deps.depth + 1
                      FROM
                          (SELECT
                               ref_nsp.nspname ref_schema,
                               ref_cl.relname  ref_name,
                               rwr_cl.relkind  dep_type,
                               rwr_nsp.nspname dep_schema,
                               rwr_cl.relname  dep_name
                           FROM
                               pg_depend dep
                                   JOIN pg_class ref_cl ON dep.refobjid = ref_cl.oid
                                   JOIN pg_namespace ref_nsp ON ref_cl.relnamespace = ref_nsp.oid
                                   JOIN pg_rewrite rwr ON dep.objid = rwr.oid
                                   JOIN pg_class rwr_cl ON rwr.ev_class = rwr_cl.oid
                                   JOIN pg_namespace rwr_nsp ON rwr_cl.relnamespace = rwr_nsp.oid
                           WHERE
                               dep.deptype = 'n' AND
                               dep.classid = 'pg_rewrite'::REGCLASS) deps
                              JOIN recursive_deps ON deps.ref_schema = recursive_deps.obj_schema AND deps.ref_name = recursive_deps.obj_name
                      WHERE
                          deps.ref_schema != deps.dep_schema OR
                          deps.ref_name != deps.dep_name)
             SELECT
                 obj_schema,
                 obj_name,
                 obj_type,
                 depth
             FROM
                 recursive_deps
             WHERE
                 depth > 0) t
        GROUP BY
            obj_schema,
            obj_name,
            obj_type
        ORDER BY
            max(depth) DESC;
END;
$_$;

CREATE OR REPLACE FUNCTION public.dependencies_save_and_drop(pViewName NAME, pViewSchema NAME DEFAULT 'public')
    RETURNS INTEGER
    LANGUAGE plpgsql
    COST 100
AS
$_$
DECLARE
    current_record RECORD;
    total_records  INTEGER := 0;
BEGIN
    FOR current_record IN
        SELECT view_schema, view_name, view_type FROM dependencies_identify(pViewName, pViewSchema)
        LOOP
            total_records := total_records + 1;
            INSERT INTO xdat_search.dependencies_saved_ddl(view_schema, view_name, ddl_to_run)
            SELECT
                pViewSchema,
                pViewName,
                'COMMENT ON ' ||
                CASE
                    WHEN c.relkind = 'v' THEN 'VIEW'
                    WHEN c.relkind = 'm' THEN 'MATERIALIZED VIEW'
                    ELSE ''
                END
                    || ' ' || n.nspname || '.' || c.relname || ' IS ''' || replace(d.description, '''', '''''') || ''';'
            FROM
                pg_class c
                    JOIN pg_namespace n ON n.oid = c.relnamespace
                    JOIN pg_description d ON d.objoid = c.oid AND d.objsubid = 0
            WHERE
                n.nspname = current_record.view_schema AND
                c.relname = current_record.view_name AND
                d.description IS NOT NULL;

            INSERT INTO xdat_search.dependencies_saved_ddl(view_schema, view_name, ddl_to_run)
            SELECT
                pViewSchema,
                pViewName,
                'COMMENT ON COLUMN ' || n.nspname || '.' || c.relname || '.' || a.attname || ' IS ''' || replace(d.description, '''', '''''') || ''';'
            FROM
                pg_class c
                    JOIN pg_attribute a ON c.oid = a.attrelid
                    JOIN pg_namespace n ON n.oid = c.relnamespace
                    JOIN pg_description d ON d.objoid = c.oid AND d.objsubid = a.attnum
            WHERE
                n.nspname = current_record.view_schema AND
                c.relname = current_record.view_name AND
                d.description IS NOT NULL;

            INSERT INTO xdat_search.dependencies_saved_ddl(view_schema, view_name, ddl_to_run)
            SELECT
                pViewSchema,
                pViewName,
                'GRANT ' || privilege_type || ' ON ' || table_schema || '.' || table_name || ' TO "' || grantee || '"'
            FROM
                information_schema.role_table_grants
            WHERE
                table_schema = current_record.view_schema AND
                table_name = current_record.view_name;

            IF current_record.view_type = 'v'
            THEN
                INSERT INTO xdat_search.dependencies_saved_ddl(view_schema, view_name, ddl_to_run)
                SELECT
                    pViewSchema,
                    pViewName,
                    'CREATE VIEW ' || current_record.view_schema || '.' || current_record.view_name || ' AS ' || view_definition
                FROM
                    information_schema.views
                WHERE
                    table_schema = current_record.view_schema AND
                    table_name = current_record.view_name;
            ELSIF current_record.view_type = 'm'
            THEN
                INSERT INTO xdat_search.dependencies_saved_ddl(view_schema, view_name, ddl_to_run)
                SELECT
                    pViewSchema,
                    pViewName,
                    'CREATE MATERIALIZED VIEW ' || current_record.view_schema || '.' || current_record.view_name || ' AS ' || definition
                FROM
                    pg_matviews
                WHERE
                    schemaname = current_record.view_schema AND
                    matviewname = current_record.view_name;
            END IF;

            EXECUTE 'DROP ' ||
                    CASE
                        WHEN current_record.view_type = 'v' THEN 'VIEW'
                        WHEN current_record.view_type = 'm' THEN 'MATERIALIZED VIEW'
                    END
                        || ' ' || current_record.view_schema || '.' || current_record.view_name;

        END LOOP;
    RETURN total_records;
END;
$_$;

CREATE OR REPLACE FUNCTION public.dependencies_restore(pViewName NAME, pViewSchema NAME DEFAULT 'public')
    RETURNS INTEGER
AS
$_$
DECLARE
    current_record RECORD;
    total_records  INTEGER := 0;
BEGIN
    FOR current_record IN
        (SELECT
             ddl_to_run
         FROM
             xdat_search.dependencies_saved_ddl
         WHERE
             view_schema = pViewSchema AND
             view_name = pViewName
         ORDER BY
             id DESC)
        LOOP
            IF current_record.ddl_to_run LIKE 'CREATE %'
            THEN
                total_records := total_records + 1;
            END IF;
            EXECUTE current_record.ddl_to_run;
        END LOOP;
    DELETE
    FROM
        xdat_search.dependencies_saved_ddl
    WHERE
        view_schema = pViewSchema AND
        view_name = pViewName;
    RETURN total_records;
END;
$_$
    LANGUAGE plpgsql
    VOLATILE
    COST 100;

CREATE OR REPLACE FUNCTION public.dependencies_clear_cache_entries(pElementName NAME, pColumn NAME, pDataType NAME)
    RETURNS INTEGER
AS
$_$
DECLARE
    affected INTEGER;
BEGIN
    EXECUTE format('DELETE FROM xs_item_cache WHERE elementname = ''%s'' AND contents ~ ''^.*\(%s:%s\).*$''', pElementName, pColumn, pDataType);
    GET DIAGNOSTICS affected = ROW_COUNT;
    RETURN affected;
END;
$_$
    LANGUAGE plpgsql;
