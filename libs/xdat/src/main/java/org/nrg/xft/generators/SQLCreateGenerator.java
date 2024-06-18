/*
 * core: org.nrg.xft.generators.SQLCreateGenerator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.generators;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.references.XFTManyToManyReference;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SQLCreateGenerator {
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
            for (final Object o : GetSQLCreate(true)) {
                builder.append(o).append("\n");
            }
            FileUtils.OutputToFile(builder.toString(), location);
            System.out.println("File Created: " + location);
        } catch (XFTInitException e) {
            log.error(XDAT.XFT_INIT_EXCEPTION_MESSAGE, e);
        } catch (ElementNotFoundException e) {
            log.error(XDAT.ELEMENT_NOT_FOUND_MESSAGE, e.ELEMENT, e);
        } catch (FieldNotFoundException e) {
            log.error(XDAT.FIELD_NOT_FOUND_MESSAGE, e.FIELD, e);
        }
    }

    public static List<String> GetSQLCreate(final boolean includeFunctions) throws Exception {
        final List<String> creates = new ArrayList<>();
        final List<String> alters  = new ArrayList<>();

        for (final Object object : XFTManager.GetInstance().getOrderedElements()) {
            final GenericWrapperElement element = (GenericWrapperElement) object;
            if (!(element.getName().equalsIgnoreCase("meta_data") || element.getName().equalsIgnoreCase("history") || element.isSkipSQL())) {
                log.debug("Generating the CREATE sql for '{}'", element.getDirectXMLName());
                creates.add("\n\n" + GenericWrapperUtils.GetCreateStatement(element));

                for (final Object object1 : GenericWrapperUtils.GetAlterTableStatements(element)) {
                    alters.add("\n\n" + object1);
                }
            }
        }

        for(final Object object : XFTReferenceManager.GetInstance().getUniqueMappings()) {
            final XFTManyToManyReference map = (XFTManyToManyReference) object;
            log.debug("Generating the CREATE sql for '{}'", map.getMappingTable());
            creates.add("\n\n" + GenericWrapperUtils.GetCreateStatement(map));
        }

        for (final Object object : XFTReferenceManager.GetInstance().getUniqueMappings()) {
            final XFTManyToManyReference map = (XFTManyToManyReference) object;
            log.debug("Generating the ALTER sql for '{}'", map.getMappingTable());
            for (final Object object1 : GenericWrapperUtils.GetAlterTableStatements(map)) {
                alters.add("\n\n" + object1);
            }
        }

        final List<String> all = new ArrayList<>();
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

    public static void main(final String[] args) {
        if (args.length == 2) {
            try {
                XFT.init(args[0]);
                SQLCreateGenerator.generateDoc(args[1]);
            } catch (Exception e) {
                log.error("An error occurred", e);
            }
        } else {
            log.error("Arguments: <Schema File location>");
        }
    }
}
