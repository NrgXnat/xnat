//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Apr 14, 2004
 */
package org.nrg.xft.generators;

import org.apache.log4j.Logger;
import org.nrg.xft.TypeConverter.PGSQLMapping;
import org.nrg.xft.TypeConverter.TypeConverter;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.DBConfig;
import org.nrg.xft.db.DBPool;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.references.*;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.utils.FileUtils;

import java.net.URI;
import java.sql.SQLException;
import java.util.*;

/**
 * Generator class that is used to output all of the SQL needed to create the
 * database, including CREATE, ALTER, VIEW, AND INSERT statements.
 *
 * @author Tim
 */
public class SQLUpdateGenerator {
    static org.apache.log4j.Logger logger = Logger.getLogger(SQLUpdateGenerator.class);

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
        } catch (XFTInitException | ElementNotFoundException e) {
            logger.error("", e);
        }
    }

    /**
     * 2 lists of statements: 0=required, 1=optional (to be commented out).
     * @return
     * @throws Exception
     */
    public static List<String>[] GetSQLCreate() throws Exception {
        List<String> creates = new ArrayList<String>();
        List<String> optional = new ArrayList<String>();
        List<String> alters = new ArrayList<String>();

        Map<String, List<String>> databases = new Hashtable<String, List<String>>();
        for (DBConfig config : DBPool.GetPool().getDBConfigs()) {
            //LOAD CURRENT TABLES FROM DB
            List<String> lowerCaseLoadedTables = new ArrayList<String>();
            PoolDBUtils con;
            try {
                con = new PoolDBUtils();
                XFTTable t = con.executeSelectQuery("SELECT c.relname  FROM      pg_catalog.pg_class AS c            LEFT JOIN pg_catalog.pg_namespace AS n                 ON n.oid = c.relnamespace  WHERE     c.relkind IN ('r') AND            n.nspname NOT IN ('pg_catalog', 'pg_toast') AND            pg_catalog.pg_table_is_visible(c.oid)  ORDER BY  c.relname;", config.getDbIdentifier(), null);
                while (t.hasMoreRows()) {
                    t.nextRow();
                    lowerCaseLoadedTables.add(t.getCellValue("relname").toString().toLowerCase());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            databases.put(config.getDbIdentifier().toLowerCase(), lowerCaseLoadedTables);
        }

        //ITERATE THROUGH ELEMENTS
        for (Object o : XFTManager.GetInstance().getOrderedElements()) {
            GenericWrapperElement element = (GenericWrapperElement) o;
            if (!(element.getName().equalsIgnoreCase("meta_data") || element.getName().equalsIgnoreCase("history") || element.isSkipSQL())) {
                String dbname = element.getDbName().toLowerCase();
                ArrayList lowerCaseLoadedTables = (ArrayList) databases.get(dbname);
                if (lowerCaseLoadedTables != null) {
                    if (lowerCaseLoadedTables.contains(element.getSQLName().toLowerCase())) {
                    	//table exists, make sure its up to date
                        List<String>[] updSts = GetUpdateStatements(element);
                        if (updSts[0].size() > 0) {
                            for (Object updSt : updSts[0]) {
                                creates.add("\n\n" + updSt);
                            }
                        }
                        if(updSts[1].size()>0){
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
                    throw new Exception("Unable to connect to database.  Check your InstanceSettings.xml.");
                }

            } else {
                if (XFT.VERBOSE)
                    System.out.print(" ");
            }
        }

        //create mapping tables, these don't have corresponding elements in the schema representation, they are added by XNAT
        for (Object o : XFTReferenceManager.GetInstance().getUniqueMappings()) {
            XFTManyToManyReference map = (XFTManyToManyReference) o;
            ArrayList lowerCaseLoadedTables = (ArrayList) databases.get(map.getElement1().getDbName().toLowerCase());
            if (!lowerCaseLoadedTables.contains(map.getMappingTable().toLowerCase())) {
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

        List<String> all = new ArrayList<String>();
        all.addAll(creates);
        all.addAll(alters);
        
        List[] _return={all,optional};
        return _return;
    }

    public static List<String>[] GetUpdateStatements(GenericWrapperElement e) {
        List<String> statements = new ArrayList<String>();
        List<String> optional = new ArrayList<String>();

        List<String> lowerCaseColumns = new ArrayList<String>();
        List<String> columnTypes = new ArrayList<String>();
        List<String> columnRequireds = new ArrayList<String>();
        PoolDBUtils con;
        try {
            con = new PoolDBUtils();
            XFTTable t = con.executeSelectQuery("select LOWER(attname) as col_name,typname, attnotnull from pg_attribute, pg_class,pg_type where attrelid = pg_class.oid AND atttypid=pg_type.oid AND attnum>0 and LOWER(relname) = '" + e.getSQLName().toLowerCase() + "';", e.getDbName(), null);
            //iterate the existing columns for this table
            while (t.hasMoreRows()) {
                t.nextRow();
                lowerCaseColumns.add(t.getCellValue("col_name").toString().toLowerCase());

                String type = t.getCellValue("typname").toString().toLowerCase();

                if (type.equals("int4")) {
                    columnTypes.add("integer");
                } else if (type.equals("int8")) {
                	columnTypes.add("bigint");
                } else if (type.equals("float8")) {
                    columnTypes.add("float");
                } else if (type.equals("int8")) {
                    columnTypes.add("bigint");
                } else {
                    columnTypes.add(type);
                }

                String notnull = t.getCellValue("attnotnull").toString().toLowerCase();
                columnRequireds.add(notnull);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        List<String> matched = new ArrayList<String>();
        try {
            String s = "ALTER TABLE " + e.getSQLName() + " ";
            Iterator iter = e.getAllFieldsWAddIns(false, true).iterator();
            TypeConverter converter = new TypeConverter(new PGSQLMapping(e.getWrapped().getSchemaPrefix()));
            while (iter.hasNext()) {
                GenericWrapperField field = (GenericWrapperField) iter.next();
                if (field.isReference()) {
                    if ((field.isMultiple() && field.getRelationType().equalsIgnoreCase("single") && field.getXMLType().getFullForeignType().equalsIgnoreCase(e.getFullXMLName())) || (!field.isMultiple())) {
                        try {
                            XFTReferenceI ref = field.getXFTReference();
                            if (!ref.isManyToMany()) {
                                for (XFTRelationSpecification spec : ((XFTSuperiorReference) ref).getKeyRelations()) {
                                    if (!lowerCaseColumns.contains(spec.getLocalCol().toLowerCase())) {
                                        if (XFT.VERBOSE)
                                        	logger.error("WARNING: Database column " + e.getSQLName() + "." + spec.getLocalCol() + " is missing. Execute update sql.");
                                        if (spec.getLocalKey() != null) {
                                            if (spec.getLocalKey().getAutoIncrement().equalsIgnoreCase("true")) {
                                                if (spec.getLocalKey().isRequired()) {
                                                    statements.add(s + " ADD COLUMN " + spec.getLocalCol() + " serial NOT NULL");
                                                } else {
                                                    statements.add(s + " ADD COLUMN " + spec.getLocalCol() + " serial");
                                                }

                                            } else {
                                                String temp = s + " ADD COLUMN " + spec.getLocalCol();
                                                if (spec.getForeignKey() != null) {
                                                    temp += " " + spec.getForeignKey().getType(converter);
                                                } else {
                                                    temp += " " + converter.convert(spec.getSchemaType().getFullLocalType());
                                                }

                                                if (spec.getLocalKey().isRequired()) {
                                                    temp += " NOT NULL ";
                                                }

                                                statements.add(temp + ";");
                                            }

                                        } else {
                                            String temp = s + " ADD COLUMN " + spec.getLocalCol();
                                            if (spec.getForeignKey() != null) {
                                                temp += " " + spec.getForeignKey().getType(converter);
                                            } else {
                                                temp += " " + converter.convert(spec.getSchemaType().getFullLocalType());
                                            }

                                            statements.add(temp + ";");
                                        }
                                    } else if (!e.getName().endsWith("_history")) {
                                        String fieldSQLName = spec.getLocalCol();
                                        matched.add(fieldSQLName);
                                        int index = lowerCaseColumns.indexOf(fieldSQLName.toLowerCase());
                                        String t = columnTypes.get(index);
                                        String req = columnRequireds.get(index);
                                        boolean exptR = false;
                                        String exptType;
                                        if (spec.getLocalKey() != null) {
                                            if (spec.getLocalKey().getAutoIncrement().equalsIgnoreCase("true")) {
                                                if (spec.getLocalKey().isRequired()) {
                                                    exptR = true;
                                                }

                                                exptType = t;
                                            } else {
                                                if (spec.getForeignKey() != null) {
                                                    exptType = spec.getForeignKey().getType(converter);
                                                } else {
                                                    exptType = converter.convert(spec.getSchemaType().getFullLocalType());
                                                }

                                                if (spec.getLocalKey().isRequired()) {
                                                    exptR = true;
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

                                        if (exptR) {
                                            if (req.equals("false")) {
//                                                if (XFT.VERBOSE)System.out.println("WARNING: Database column " + e.getSQLName() +"." + fieldSQLName+" is now required. Uncomment line in update sql to fix.");
//                                                String temp ="\n--Database column " + e.getSQLName() +"." + fieldSQLName+" is now required.\n";
//                                                temp += "--" + s + " ALTER COLUMN " +  fieldSQLName  + " SET NOT NULL";                                    
//                                                stmts.add(temp +";");
                                            }
                                        } else {
                                            if (req.equals("true")) {
                                            	logger.error("WARNING: Database column " + e.getSQLName() + "." + fieldSQLName + " is no longer required. Execute update sql.");
                                                String temp = "\n--Database column " + e.getSQLName() + "." + fieldSQLName + " is no longer required.\n";
                                                temp += s + " ALTER COLUMN " + fieldSQLName + " DROP NOT NULL";
                                                statements.add(temp + ";");
                                            }
                                        }
                                    }

                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    if (GenericWrapperField.IsLeafNode(field.getWrapped())) {
                        String fieldSQLName = field.getSQLName().toLowerCase();
                        if (!lowerCaseColumns.contains(fieldSQLName)) {
                            if (XFT.VERBOSE)
                            	logger.error("WARNING: Database column " + e.getSQLName() + "." + fieldSQLName + " is missing. Execute update sql.");
                            String temp = s + " ADD COLUMN " + fieldSQLName;
                            if (field.getAutoIncrement().equalsIgnoreCase("true")) {
                                temp += " serial";
                                if (field.isRequired()) {
                                    temp += " NOT NULL ";
                                }
                                //						if (addedSequence)
                                //						{
                                //							sb.append("DEFAULT nextval('").append(sequenceName).append("'::text) ");
                                //						}
                                //sb.append(" serial").append(" DEFAULT nextval('" + input.getSQLName() + "_" + field.getSQLName() + "_seq') ");
                                //sb = (new StringBuffer()).append("CREATE SEQUENCE " + input.getSQLName() + "_" + field.getSQLName() + "_seq;\n\n").append(sb.toString());
                            } else {
                                if (!field.getType(converter).equals("")) {
                                    temp += " " + field.getType(converter);
                                } else {
                                    temp += " " + converter.convert(e.getWrapped().getSchemaPrefix() + ":string", 50) + "(255) ";
                                }
                                if (field.isRequired()) {
                                    temp += " NOT NULL ";
                                }
                            }
                            statements.add(temp + ";");
                        } else if (!e.getName().endsWith("_history")) {
                            matched.add(fieldSQLName);
                            int index = lowerCaseColumns.indexOf(fieldSQLName);
                            String t = columnTypes.get(index);
                            String req = columnRequireds.get(index);

                            String exptType;
                            if (!field.getType(converter).equals("")) {
                                exptType = field.getType(converter);
                            } else {
                                exptType = converter.convert(e.getWrapped().getSchemaPrefix() + ":string", 50);
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

                            if (field.isRequired()) {
                                if (req.equals("false")) {
                                    if (XFT.VERBOSE)
                                        System.out.println("WARNING: Database column " + e.getSQLName() + "." + fieldSQLName + " is now required. Uncomment line in update sql to fix.");
                                    String temp = "\n--Database column " + e.getSQLName() + "." + fieldSQLName + " is now required.\n";
                                    temp += "--" + s + " ALTER COLUMN " + fieldSQLName + " SET NOT NULL";
                                    optional.add(temp + ";");
                                }
                            } else {
                                if (req.equals("true")) {
                                    if (XFT.VERBOSE)
                                        System.out.println("WARNING: Database column " + e.getSQLName() + "." + fieldSQLName + " is no longer required. Execute update sql.");
                                    String temp = "\n--Database column " + e.getSQLName() + "." + fieldSQLName + " is no longer required.\n";
                                    temp += s + " ALTER COLUMN " + fieldSQLName + " DROP NOT NULL";
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
                            statements.add(s + " ALTER COLUMN " + fieldSQLName + " DROP NOT NULL;");
                        }
                    }
                }
            }
        } catch (ElementNotFoundException | XFTInitException e1) {
            e1.printStackTrace();
        }

        return new List[]{statements,optional};
    }


    public static void main(String args[]) {
        if (args.length == 2) {
            try {
                // MIGRATE: No init options.
                XFT.init();
                SQLUpdateGenerator.generateDoc(args[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Arguments: <Schema File location>");
        }
    }
}

