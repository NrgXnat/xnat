INSERT INTO xhbm_tool (id, created, disabled, timestamp, strict, tool_description, tool_id, tool_name)
VALUES (1000, CURRENT_TIMESTAMP, TIMESTAMP '1969-12-31 18:00:00', CURRENT_TIMESTAMP, true, 'Some tool description', 'aliasMigration', 'Alias Migration Test');
INSERT INTO xhbm_preference (id, created, disabled, timestamp, name, scope, entity_id, value, tool)
VALUES (1000, CURRENT_TIMESTAMP, TIMESTAMP '1969-12-31 18:00:00', CURRENT_TIMESTAMP, 'prefAAlias', 0, '', 'importValueA', 1000),
       (1001, CURRENT_TIMESTAMP, TIMESTAMP '1969-12-31 18:00:00', CURRENT_TIMESTAMP, 'prefB', 0, '', 'importValueB', 1000),
       (1002, CURRENT_TIMESTAMP, TIMESTAMP '1969-12-31 18:00:00', CURRENT_TIMESTAMP, 'prefCAlias', 0, '', 'importValueC', 1000);
