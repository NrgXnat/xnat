//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Apr 14, 2004
 */
package org.nrg.xft.generators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.xft.XFT;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.references.XFTManyToManyReference;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.utils.FileUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generator class that is used to output all of the SQL needed to create the
 * database, including CREATE, ALTER, VIEW, AND INSERT statements.
 *
 * @author Tim
 */
public class SQLCreateGenerator {
    private static final Log logger = LogFactory.getLog(SQLCreateGenerator.class);

    /**
     * outputs all of the SQL needed to create the database, including CREATE,
     * ALTER, VIEW, AND INSERT statements.
     *
     * @param location The location where the SQL should be placed.
     * @throws Exception When an exception is encountered.
     */
    public static void generateDoc(String location) throws Exception {
        try {
            StringBuilder builder = new StringBuilder();
            for (Object o : GetSQLCreate(true)) {
                builder.append(o).append("\n");
            }
            FileUtils.OutputToFile(builder.toString(), location);
            System.out.println("File Created: " + location);
        } catch (org.nrg.xft.exception.XFTInitException e) {
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        }
    }

    public static List<String> GetSQLCreate(boolean includeFunctions) throws Exception {
        List<String> creates = new ArrayList<String>();
        List<String> alters = new ArrayList<String>();

        for (Object o : XFTManager.GetInstance().getOrderedElements()) {
            GenericWrapperElement element = (GenericWrapperElement) o;
            if (!(element.getName().equalsIgnoreCase("meta_data") || element.getName().equalsIgnoreCase("history") || element.isSkipSQL())) {
                logger.debug("Generating the CREATE sql for '" + element.getDirectXMLName() + "'");
                creates.add("\n\n" + GenericWrapperUtils.GetCreateStatement(element));

                for (Object o1 : GenericWrapperUtils.GetAlterTableStatements(element)) {
                    alters.add("\n\n" + o1);
                }
            } else {
                if (XFT.VERBOSE) {
                    System.out.print(" ");
                }
            }
        }

        Iterator mappingTables = XFTReferenceManager.GetInstance().getUniqueMappings().iterator();
        while (mappingTables.hasNext()) {
            XFTManyToManyReference map = (XFTManyToManyReference) mappingTables.next();
            logger.debug("Generating the CREATE sql for '" + map.getMappingTable() + "'");
            creates.add("\n\n" + GenericWrapperUtils.GetCreateStatement(map));
        }

        mappingTables = XFTReferenceManager.GetInstance().getUniqueMappings().iterator();
        while (mappingTables.hasNext()) {
            XFTManyToManyReference map = (XFTManyToManyReference) mappingTables.next();
            logger.debug("Generating the ALTER sql for '" + map.getMappingTable() + "'");
            for (Object o : GenericWrapperUtils.GetAlterTableStatements(map)) {
                alters.add("\n\n" + o);
            }
        }

        List<String> all = new ArrayList<String>();
        all.add("--CREATE STATEMENTS");
        all.addAll(creates);
        all.add("\n\n--ALTER STATEMENTS");
        all.addAll(alters);
        if(includeFunctions){
	        all.add("\n\n--EXTENSION TABLES");
	        all.addAll(GenericWrapperUtils.GetExtensionTables());
	        all.add("\n\n--FUNCTION STATEMENTS");
	        List<String>[]func=GenericWrapperUtils.GetFunctionSQL();
	        all.addAll(func[0]);
	        all.addAll(func[1]);
        }

        return all;
    }

    public static void main(String args[]) {
        if (args.length == 2) {
            try {
                XFT.init(new URI(args[0]));
                SQLCreateGenerator.generateDoc(args[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Arguments: <Schema File location>");
        }
    }
}
