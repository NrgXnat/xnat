/*
 * core: org.nrg.xft.generators.SQLUpdateGenerator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.generators;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.TypeConverter.PGSQLMapping;
import org.nrg.xft.TypeConverter.TypeConverter;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.references.*;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLUpdateGenerator {
    private static final Logger logger = LoggerFactory.getLogger(SQLUpdateGenerator.class);

    /**
     * outputs all of the SQL needed to create the database, including CREATE,
     * ALTER, VIEW, AND INSERT statements.
     *
     * @param location The location where the SQL should be placed.
     * @throws Exception When an exception is encountered.
     */
    public static void generateDoc(String location) throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            for (Object o : GetSQLCreate()) {
                sb.append(o);
            }
    	    for (Object item : GenericWrapperUtils.GetExtensionTables())
    	    {
    	        sb.append(item);
    	    }
    	    for (Object item : GenericWrapperUtils.GetFunctionSQL())
    	    {
    	        sb.append(item);
    	    }
            FileUtils.OutputToFile(sb.toString(), location);
            System.out.println("File Created: " + location);
        } catch (XFTInitException e) {
            logger.error("There was an error initializing XFT", e);
        } catch (ElementNotFoundException e) {
            logger.error("Couldn't find the requested element " + e.ELEMENT, e);
        }
    }

    /**
     * 2 lists of statements: 0=required, 1=optional (to be commented out).
     * @return A list of two lists, the first a list of required statements, the second a list of optional statements.
     * @throws Exception When an error occurs.
     */
    public static List[] GetSQLCreate() throws Exception {
        List<String> creates = new ArrayList<>();
        List<String> optional = new ArrayList<>();
        List<String> alters = new ArrayList<>();

        //LOAD CURRENT TABLES FROM DB
        final List<String> tables = new ArrayList<>();
        PoolDBUtils con;
        try {
            con = new PoolDBUtils();
            XFTTable t = con.executeSelectQuery("SELECT c.relname  FROM      pg_catalog.pg_class AS c            LEFT JOIN pg_catalog.pg_namespace AS n                 ON n.oid = c.relnamespace  WHERE     c.relkind IN ('r') AND            n.nspname NOT IN ('pg_catalog', 'pg_toast') AND            pg_catalog.pg_table_is_visible(c.oid)  ORDER BY  c.relname;", PoolDBUtils.getDefaultDBName(), null);
            while (t.hasMoreRows()) {
                t.nextRow();
                tables.add(t.getCellValue("relname").toString().toLowerCase());
            }
        } catch (SQLException ex) {
            logger.error("An SQL error occurred [" + ex.getErrorCode() + "] " + ex.getSQLState(), ex);
        } catch (Exception ex) {
            logger.error("An unknown error occurred.", ex);
        }

        //ITERATE THROUGH ELEMENTS
        for (Object o : XFTManager.GetInstance().getOrderedElements()) {
            GenericWrapperElement element = (GenericWrapperElement) o;
            if (!(element.getName().equalsIgnoreCase("meta_data") || element.getName().equalsIgnoreCase("history") || element.isSkipSQL())) {
                if (tables.contains(element.getSQLName().toLowerCase())) {
                    //table exists, make sure its up to date
                    List<String>[] updSts = GetUpdateStatements(element);
                    if (updSts[0].size() > 0) {
                        for (Object updSt : updSts[0]) {
                            creates.add("\n\n" + updSt);
                        }
                    }
                    if (updSts[1].size() > 0) {
                        optional.addAll(updSts[1]);
                    }
                } else {
                    //table missing, add it.
                    creates.add("\n\n" + GenericWrapperUtils.GetCreateStatement(element));

                    for (Object o1 : GenericWrapperUtils.GetAlterTableStatements(element)) {
                        alters.add("\n\n" + o1);
                    }
                }

            } else {
                if (XFT.VERBOSE) {
                    System.out.print(" ");
                }
            }
        }

        //create mapping tables, these don't have corresponding elements in the schema representation, they are added by XNAT
        for (Object o : XFTReferenceManager.GetInstance().getUniqueMappings()) {
            XFTManyToManyReference map = (XFTManyToManyReference) o;
            if (!tables.contains(map.getMappingTable().toLowerCase())) {
                logger.debug("Generating the CREATE sql for '" + map.getMappingTable() + "'");
                //delete.add("\n\nDELETE FROM "+ map.getMappingTable() + ";");
                creates.add("\n\n" + GenericWrapperUtils.GetCreateStatement(map));

                logger.debug("Generating the ALTER sql for '" + map.getMappingTable() + "'");
                //delete.add("\n\nDELETE FROM "+map.getMappingTable()+";");
                for (Object o1 : GenericWrapperUtils.GetAlterTableStatements(map)) {
                    alters.add("\n\n" + o1);
                }
            }
        }

        List<String> all = new ArrayList<>();
        all.addAll(creates);
        all.addAll(alters);

        return new List[]{all,optional};
    }

    public static List<String>[] GetUpdateStatements(GenericWrapperElement e) {
        List<String> statements = new ArrayList<>();
        List<String> optional = new ArrayList<>();

        List<String> lowerCaseColumns = new ArrayList<>();
        List<String> columnTypes = new ArrayList<>();
        List<String> columnRequireds = new ArrayList<>();
        PoolDBUtils con;
        try {
            con = new PoolDBUtils();
            XFTTable t = con.executeSelectQuery("select LOWER(attname) as col_name,typname, attnotnull from pg_attribute, pg_class,pg_type where attrelid = pg_class.oid AND atttypid=pg_type.oid AND attnum>0 and LOWER(relname) = '" + e.getSQLName().toLowerCase() + "';", e.getDbName(), null);
            //iterate the existing columns for this table
            while (t.hasMoreRows()) {
                t.nextRow();
                lowerCaseColumns.add(t.getCellValue("col_name").toString().toLowerCase());

                String type = t.getCellValue("typname").toString().toLowerCase();

                switch (type) {
                    case "int4":
                        columnTypes.add("integer");
                        break;
                    case "int8":
                        columnTypes.add("bigint");
                        break;
                    case "float8":
                        columnTypes.add("float");
                        break;
                    default:
                        columnTypes.add(type);
                        break;
                }

                String notnull = t.getCellValue("attnotnull").toString().toLowerCase();
                columnRequireds.add(notnull);
            }
        } catch (SQLException ex) {
            logger.error("An SQL error occurred [" + ex.getErrorCode() + "] " + ex.getSQLState(), e);
        } catch (Exception ex) {
            logger.error("An unknown error occurred.", e);
        }

        List<String> matched = new ArrayList<>();
        try {
            final String prefix = "ALTER TABLE " + e.getSQLName() + " ";
            final TypeConverter converter = new TypeConverter(new PGSQLMapping(e.getWrapped().getSchemaPrefix()));
            for (final Object object : e.getAllFieldsWAddIns(false, true)) {
                final GenericWrapperField field = (GenericWrapperField) object;
                if (field.isReference()) {
                    if ((field.isMultiple() && field.getRelationType().equalsIgnoreCase("single") && field.getXMLType().getFullForeignType().equalsIgnoreCase(e.getFullXMLName())) || (!field.isMultiple())) {
                        try {
                            final XFTReferenceI ref = field.getXFTReference();
                            if (!ref.isManyToMany()) {
                                for (final XFTRelationSpecification spec : ((XFTSuperiorReference) ref).getKeyRelations()) {
                                    final GenericWrapperField localKey = spec.getLocalKey();
                                    final boolean             required = localKey != null && localKey.isRequired();
                                    if (!lowerCaseColumns.contains(spec.getLocalCol().toLowerCase())) {
                                        if (XFT.VERBOSE) {
                                        	logger.error("WARNING: Database column " + e.getSQLName() + "." + spec.getLocalCol() + " is missing. Execute update sql.");
                                        }
                                        if (localKey != null) {
                                            if (localKey.getAutoIncrement().equalsIgnoreCase("true")) {
                                                statements.add(prefix + " ADD COLUMN " + spec.getLocalCol() + " serial" + (required ? " NOT NULL" : ""));
                                            } else {
                                                final StringBuilder statement         = new StringBuilder(prefix).append(" ADD COLUMN ").append(spec.getLocalCol());
                                                if (!setTypeAndDefaultValue(statement, spec, converter)) {
                                                    logger.warn("The property {}.{} is required but does not have a default value. This may cause issues when updating the database.", e.getName(), field.getName());
                                                }
                                                statement.append(";\n");
                                                statements.add(statement.toString());
                                            }
                                        } else {
                                            statements.add(prefix + " ADD COLUMN " + spec.getLocalCol() + (spec.getForeignKey() != null ? " " + spec.getForeignKey().getType(converter) : converter.convert(spec.getSchemaType().getFullLocalType())) + ";");
                                        }
                                    } else if (!e.getName().endsWith("_history")) {
                                        String fieldSQLName = spec.getLocalCol();
                                        matched.add(fieldSQLName);
                                        int index = lowerCaseColumns.indexOf(fieldSQLName.toLowerCase());
                                        String t = columnTypes.get(index);
                                        String req = columnRequireds.get(index);
                                        String exptType;
                                        if (localKey != null) {
                                            if (localKey.getAutoIncrement().equalsIgnoreCase("true")) {
                                                exptType = t;
                                            } else {
                                                if (spec.getForeignKey() != null) {
                                                    exptType = spec.getForeignKey().getType(converter);
                                                } else {
                                                    exptType = converter.convert(spec.getSchemaType().getFullLocalType());
                                                }
                                            }
                                        } else {
                                            if (spec.getForeignKey() != null) {
                                                exptType = spec.getForeignKey().getType(converter);
                                            } else {
                                                exptType = converter.convert(spec.getSchemaType().getFullLocalType());
                                            }
                                        }

                                        if (exptType.contains("("))
                                            exptType = exptType.substring(0, exptType.indexOf("("));

                                        if (!t.equalsIgnoreCase(exptType)) {
                                            //COLUMN TYPE MIS-MATCH
                                            logger.error("WARNING: Database column " + e.getSQLName() + "." + fieldSQLName + " type mis-match ('" + exptType + "'!='" + t + "'). Unable to resolve.");
                                        }

                                        if (!required && req.equals("true")) {
                                            logger.error("WARNING: Database column " + e.getSQLName() + "." + fieldSQLName + " is no longer required. Execute update sql.");
                                            statements.add("\n--Database column " + e.getSQLName() + "." + fieldSQLName + " is no longer required.\nALTER COLUMN " + fieldSQLName + " DROP NOT NULL;");
                                        }
                                    }

                                }
                            }
                        } catch (Exception ex) {
                            logger.error("An exception occurred trying to process the reference field {}", field.getName(), ex);
                        }
                    }
                } else {
                    if (GenericWrapperField.IsLeafNode(field.getWrapped())) {
                        final String fieldSQLName = field.getSQLName().toLowerCase();
                        final boolean required    = field.isRequired();
                        if (!lowerCaseColumns.contains(fieldSQLName)) {
                            if (XFT.VERBOSE) {
                                logger.error("WARNING: Database column " + e.getSQLName() + "." + fieldSQLName + " is missing. Execute update sql.");
                            }
                            final StringBuilder statement = new StringBuilder(prefix);
                            statement.append(" ADD COLUMN ").append(fieldSQLName);
                            if (field.getAutoIncrement().equalsIgnoreCase("true")) {
                                statement.append(" serial");
                                if (required) {
                                    statement.append(" NOT NULL ");
                                }
                            } else {
                                if (!setTypeAndDefaultValue(statement, field, converter)) {
                                    logger.warn("The property {}.{} is required but does not have a default value. This may cause issues when updating the database.", e.getName(), field.getName());
                                }
                                statement.append(";\n");
                                statements.add(statement.toString());
                            }
                        } else if (!e.getName().endsWith("_history")) {
                            matched.add(fieldSQLName);
                            int index = lowerCaseColumns.indexOf(fieldSQLName);
                            String t = columnTypes.get(index);
                            String req = columnRequireds.get(index);

                            String exptType;
                            if (!field.getType(converter).equals("")) {
                                exptType = field.getType(converter);
                            } else {
                                exptType = converter.convert("xs:string", 50);
                            }

                            if (exptType.contains("("))
                                exptType = exptType.substring(0, exptType.indexOf("("));

                            if (!t.equalsIgnoreCase(exptType)) {
                                //COLUMN TYPE MIS-MATCH

                                try {
                                    String query = "SELECT count(" + fieldSQLName + ") AS value_count FROM " + e.getSQLName();
                                    if (t.equalsIgnoreCase("text") || t.equalsIgnoreCase("varchar") || t.equalsIgnoreCase("bytea"))
                                        query += " WHERE " + fieldSQLName + " IS NOT NULL AND " + fieldSQLName + " !=''";
                                    Number values = (Number) org.nrg.xft.db.PoolDBUtils.ReturnStatisticQuery(query + ";", "value_count", e.getDbName(), "system");

                                    query = "SELECT relname, attname, COUNT(conname) AS value_count FROM ("
                                            + " SELECT pg_constraint.oid, conname, contype, tb.relname, pg_attribute.attname FROM pg_constraint, pg_class tb, pg_attribute WHERE conrelid = tb.oid AND ((conrelid=pg_attribute.attrelid AND pg_attribute.attnum=ANY(conkey)))"
                                            + " UNION"
                                            + " SELECT pg_constraint.oid, conname, contype, fk.relname, pg_attribute.attname FROM pg_constraint, pg_class fk, pg_attribute WHERE confrelid = fk.oid AND ((confrelid=pg_attribute.attrelid AND pg_attribute.attnum=ANY(confkey)))"
                                            + " ) SEARCH WHERE relname='" + e.getSQLName().toLowerCase() + "' AND attname='" + fieldSQLName + "' GROUP BY relname, attname";
                                    Number constraints = (Number) org.nrg.xft.db.PoolDBUtils.ReturnStatisticQuery(query + ";", "value_count", e.getDbName(), "system");
                                    optional.add("\n--Database column " + e.getSQLName() + "." + fieldSQLName + " type mis-match ('" + exptType + "'!='" + t + "').\n");

                                    if (values == null) {
                                        values = 0;
                                    }
                                    if (constraints == null) {
                                        constraints = 0;
                                    }
                                    if (values.intValue() > 0 || constraints.intValue() > 0) {
                                        if (XFT.VERBOSE)
                                            System.out.println("WARNING: Database column " + e.getSQLName() + "." + fieldSQLName + " type mis-match ('" + exptType + "'!='" + t + "'). Uncomment appropriate lines in update sql to resolve.");
                                        optional.add( "----Unable to resolve type mis-match for the following reason(s).\n");
                                        optional.add( "----Please review these factors before uncommenting this code.\n");
                                        if (values.intValue() > 0)
                                        	optional.add( "----" + values.intValue() + " row(s) contain values.\n");
                                        if (constraints.intValue() > 0)
                                        	optional.add( "----" + constraints.intValue() + " column constraint(s).\n");

                                        	optional.add( "----Fix " + e.getSQLName() + "_history table.\n");
                                        	optional.add( "--ALTER TABLE " + e.getSQLName() + "_history ADD COLUMN " + fieldSQLName + "_cp " + t + ";\n");
                                        	optional.add( "--UPDATE " + e.getSQLName() + "_history SET " + fieldSQLName + "_cp=" + fieldSQLName + ";\n");
                                        	optional.add( "--ALTER TABLE " + e.getSQLName() + "_history DROP COLUMN " + fieldSQLName + ";\n");
                                        	optional.add( "--ALTER TABLE " + e.getSQLName() + "_history ADD COLUMN " + fieldSQLName + " " + exptType + ";\n");
                                        if (t.equalsIgnoreCase("BYTEA")) {
                                        	optional.add( "--UPDATE " + e.getSQLName() + "_history SET " + fieldSQLName + "=ENCODE(" + fieldSQLName + "_cp,'escape');\n");
                                        } else {
                                        	optional.add( "--UPDATE " + e.getSQLName() + "_history SET " + fieldSQLName + "=CAST(" + fieldSQLName + "_cp AS " + exptType + ");\n");
                                        }

                                        optional.add("--CREATE OR REPLACE FUNCTION after_update_" + e.getSQLName() + "()"+
                                        	"  RETURNS TRIGGER AS"+
                                        	" '"+
                                        	"    begin"+
                                        	"        RETURN NULL;"+
                                        	"     end;"+
                                        	" '"+
                                        	"   LANGUAGE 'plpgsql' VOLATILE;\n");

                                        	optional.add( "----Fix " + e.getSQLName() + " table.\n");
                                        	optional.add( "--ALTER TABLE " + e.getSQLName() + " ADD COLUMN " + fieldSQLName + "_cp " + t + ";\n");
                                        	optional.add( "--UPDATE " + e.getSQLName() + " SET " + fieldSQLName + "_cp=" + fieldSQLName + ";\n");
                                        	optional.add( "--ALTER TABLE " + e.getSQLName() + " DROP COLUMN " + fieldSQLName + ";\n");
                                        	optional.add( "--ALTER TABLE " + e.getSQLName() + " ADD COLUMN " + fieldSQLName + " " + exptType + ";\n");
                                        if (t.equalsIgnoreCase("BYTEA")) {
                                        	optional.add( "--UPDATE " + e.getSQLName() + " SET " + fieldSQLName + "=ENCODE(" + fieldSQLName + "_cp,'escape');\n");
                                        } else {
                                        	optional.add( "--UPDATE " + e.getSQLName() + " SET " + fieldSQLName + "=CAST(" + fieldSQLName + "_cp AS " + exptType + ");");
                                        }
                                    } else {
                                        if (XFT.VERBOSE)
                                            System.out.println("WARNING: Database column " + e.getSQLName() + "." + fieldSQLName + " type mis-match ('" + exptType + "'!='" + t + "'). Uncomment appropriate lines in update sql to resolve.");

                                    	String temp = "";
                                        temp += "--Existing column has no values or constraints.\n";
                                        query = "SELECT count(" + fieldSQLName + ") AS value_count FROM " + e.getSQLName() + "_history";
                                        if (t.equalsIgnoreCase("text") || t.equalsIgnoreCase("varchar") || t.equalsIgnoreCase("bytea")) {
                                            query += " WHERE " + fieldSQLName + " IS NOT NULL AND " + fieldSQLName + " !=''";
                                        }

                                        temp += "--Fix " + e.getSQLName() + "_history table.\n";
                                        temp += "ALTER TABLE " + e.getSQLName() + "_history DROP COLUMN " + fieldSQLName + ";\n";
                                        temp += "ALTER TABLE " + e.getSQLName() + "_history ADD COLUMN " + fieldSQLName + " " + exptType + ";\n";
                                        temp += "--Fix " + e.getSQLName() + " table.\n";
                                        temp += "ALTER TABLE " + e.getSQLName() + " DROP COLUMN " + fieldSQLName + ";\n";
                                        temp += "ALTER TABLE " + e.getSQLName() + " ADD COLUMN " + fieldSQLName + " " + exptType + ";\n";
                                        
                                        statements.add(temp);
                                    }
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    logger.error("", e1);
                                }
                            }

                            if (required) {
                                if (req.equals("false")) {
                                    if (XFT.VERBOSE)
                                        System.out.println("WARNING: Database column " + e.getSQLName() + "." + fieldSQLName + " is now required. Uncomment line in update sql to fix.");
                                    String temp = "\n--Database column " + e.getSQLName() + "." + fieldSQLName + " is now required.\n";
                                    temp += "--" + prefix + " ALTER COLUMN " + fieldSQLName + " SET NOT NULL";
                                    optional.add(temp + ";");
                                }
                            } else {
                                if (req.equals("true")) {
                                    if (XFT.VERBOSE)
                                        System.out.println("WARNING: Database column " + e.getSQLName() + "." + fieldSQLName + " is no longer required. Execute update sql.");
                                    String temp = "\n--Database column " + e.getSQLName() + "." + fieldSQLName + " is no longer required.\n";
                                    temp += prefix + " ALTER COLUMN " + fieldSQLName + " DROP NOT NULL";
                                    statements.add(temp + ";");
                                }
                            }
                        }
                    }
                }
            }

            if (!e.getName().endsWith("_history")) {
                for (int i = 0; i < lowerCaseColumns.size(); i++) {
                    if (!matched.contains(lowerCaseColumns.get(i))) {
                        String fieldSQLName = lowerCaseColumns.get(i);

                        String req = columnRequireds.get(i);
                        if (req.equalsIgnoreCase("true")) {
                            if (XFT.VERBOSE) {
                                System.out.println("WARNING: Required database column " + e.getSQLName() + "." + fieldSQLName + " is no longer valid. Execute update sql.");
                            }
                            statements.add(prefix + " ALTER COLUMN " + fieldSQLName + " DROP NOT NULL;");
                        }
                    }
                }
            }
        } catch (XFTInitException ex) {
            logger.error("There was an error initializing XFT", ex);
        } catch (ElementNotFoundException ex) {
            logger.error("Couldn't find the requested element " + ex.ELEMENT, ex);
        }

        return new List[]{statements,optional};
    }

    public static boolean setTypeAndDefaultValue(final StringBuilder statement, final XFTRelationSpecification relation, final TypeConverter converter) {
        final GenericWrapperField foreignKey   = relation.getForeignKey();
        final GenericWrapperField localKey     = relation.getLocalKey();
        final String              columnType   = foreignKey != null ? foreignKey.getType(converter) : converter.convert(relation.getSchemaType().getFullLocalType());
        final String              javaType     = foreignKey != null ? foreignKey.getType(XFTItem.JAVA_CONVERTER) : XFTItem.JAVA_CONVERTER.convert(relation.getSchemaType().getFullLocalType());
        final boolean             required     = localKey != null && localKey.isRequired();
        final String              defaultValue = localKey != null ? localKey.getDefaultValue() : relation.getForeignKey().getDefaultValue();
        return setTypeAndDefaultValue(statement, columnType, javaType, required, defaultValue);
    }

    public static boolean setTypeAndDefaultValue(final StringBuilder statement, final GenericWrapperField field, final TypeConverter converter) {
        final String  columnType   = StringUtils.defaultIfBlank(field.getType(converter), converter.convert("xs:string", 50) + "(255)");
        final String  javaType     = StringUtils.defaultIfBlank(field.getType(XFTItem.JAVA_CONVERTER), XFTItem.JAVA_CONVERTER.convert("xs:string", 50) + "(255)");
        final boolean required     = field.isRequired();
        final String  defaultValue = field.getDefaultValue();
        return setTypeAndDefaultValue(statement, columnType, javaType, required, defaultValue);
    }

    public static void main(String args[]) {
        if (args.length == 2) {
            try {
                XFT.init(args[0]);
                SQLUpdateGenerator.generateDoc(args[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Arguments: <Schema File location>");
        }
    }

    private static boolean setTypeAndDefaultValue(final StringBuilder statement, final String columnType, final String javaType, final boolean required, final String defaultValue) {
        statement.append(" ").append(columnType);
        if (required) {
            statement.append(" NOT NULL ");
            if (defaultValue != null) {
                statement.append("DEFAULT ");
                switch (javaType) {
                    case "java.lang.Boolean":
                        statement.append(BooleanUtils.toInteger(BooleanUtils.toBoolean(StringUtils.defaultIfBlank(defaultValue, "false"))));
                        break;

                    case "java.lang.Double":
                    case "java.lang.Integer":
                    case "java.lang.Long":
                        statement.append(StringUtils.defaultIfEmpty(defaultValue, "0"));
                        break;

                    case "java.lang.String":
                        statement.append("'").append(defaultValue).append("'");
                        break;

                    case "java.util.Date":
                        // If there's a value
                        final boolean hasDefaultValue = StringUtils.isNotBlank(defaultValue);
                        if (hasDefaultValue) {
                            // And that value is NOT a function call (denoted by not having the '(' or ')' characters), then
                            // insert the value as a string.
                            if (!StringUtils.containsAny(defaultValue, "()")) {
                                statement.append("'").append(defaultValue).append("'");
                            } else {
                                statement.append(defaultValue);
                            }
                        } else {
                            statement.append("now()");
                        }
                        break;
                }
            }
        }
        return !(required && defaultValue == null);
    }
}

