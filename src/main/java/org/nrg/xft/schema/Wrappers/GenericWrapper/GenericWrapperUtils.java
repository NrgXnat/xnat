/*
 * core: org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema.Wrappers.GenericWrapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.search.QueryOrganizer;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xft.TypeConverter.PGSQLMapping;
import org.nrg.xft.TypeConverter.SQLMapping;
import org.nrg.xft.TypeConverter.TypeConverter;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.generators.SQLUpdateGenerator;
import org.nrg.xft.generators.TextFunctionGenerator;
import org.nrg.xft.references.*;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.SearchCriteria;

import java.util.*;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GenericWrapperUtils {
    public static final String CREATE_CLASS = "create_class2";
    
	public static final String CREATE_FUNCTION = "create_function2";

	public static final String CREATE_SCHEMA = "create_schema2";

	public static final String CREATE_TRIGGER = "create_trigger2";
	
	public static Boolean BATCH_MODE=Boolean.FALSE;

    public static org.apache.log4j.Logger logger = Logger.getLogger(GenericWrapperUtils.class);

    public static final String TXT_FUNCTION = "i_";
    public static final String TXT_EXT_FUNCTION = "ie_";
    
    public static final String ACT_FUNCTION = "a_";
    public static final String ACT_EXT_FUNCTION = "ae_";
    /**
     * Generates the CREATE TABLE SQL statement for this element.
     * 
     * @param input
     * @return Returns the CREATE TABLE SQL statement as a StringBuffer
     */
	public static StringBuffer GetCreateStatement(GenericWrapperElement input) {
        final StringBuilder sb = new StringBuilder();
        try {
            if (input.getSchema().getDbType().equalsIgnoreCase("MYSQL")) {
                Iterator iter = input.getAllFieldsWAddIns(false, false)
                        .iterator();
                sb.append("CREATE TABLE " + input.getSQLName() + " (");
                int count = 0;
                TypeConverter converter = new TypeConverter(
                        new SQLMapping(input.getWrapped().getParentElement()
                                .getSchemaPrefix()));
                while (iter.hasNext()) {
                    GenericWrapperField field = (GenericWrapperField) iter
                            .next();
                    if (field.isReference()) {
                        try {
                            XFTReferenceI ref = field.getXFTReference();
                            if (!ref.isManyToMany()) {
                                for (XFTRelationSpecification spec : ((XFTSuperiorReference) ref)
                                        .getKeyRelations()) {
                                    if (count == 0) {
                                        sb.append("\n");
                                    } else {
                                        sb.append("\n, ");
                                    }
                                    count++;
                                    sb.append(spec.getLocalCol()).append(" ");
                                    if (spec.getLocalKey() != null) {
                                        if (spec.getLocalKey()
                                                .getAutoIncrement()
                                                .equalsIgnoreCase("true")) {
                                        	
                                        	
                                        	if(field.getType(converter).equalsIgnoreCase("BIGINT")){
                                        		sb.append(" bigserial").append(" ");
                                        	} else {
                                        		sb.append(" serial").append(" ");
                                        	}
                                            //sb.append(" serial").append("
                                            // DEFAULT nextval('" +
                                            // input.getSQLName() + "_" +
                                            // key.getSQLName() + "_seq') ");
                                            //sb = (new
                                            // StringBuffer()).append("CREATE
                                            // SEQUENCE " + input.getSQLName() +
                                            // "_" + key.getSQLName() +
                                            // "_seq;\n\n").append(sb.toString());
                                        } else {
                                            if (spec.getForeignKey() != null) {
                                                sb
                                                        .append(
                                                                spec
                                                                        .getForeignKey()
                                                                        .getType(
                                                                                converter))
                                                        .append(" ");
                                            } else {
                                                sb
                                                        .append(
                                                                converter
                                                                        .convert(spec
                                                                                .getSchemaType()
                                                                                .getFullLocalType()))
                                                        .append(" ");
                                            }
                                        }
                                        if (spec.getLocalKey().isRequired()) {
                                            sb
                                                    .append(" NOT NULL ON DELETE CASCADE ");
                                        }
                                    } else {
                                        if (spec.getForeignKey() != null) {
                                            sb
                                                    .append(
                                                            spec
                                                                    .getForeignKey()
                                                                    .getType(
                                                                            converter))
                                                    .append(" ");
                                        } else {
                                            sb
                                                    .append(
                                                            converter
                                                                    .convert(spec
                                                                            .getSchemaType()
                                                                            .getFullLocalType()))
                                                    .append(" ");
                                        }
                                    }

                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (GenericWrapperField.IsLeafNode(field.getWrapped())) {
                            if (count == 0) {
                                sb.append("\n");
                            } else {
                                sb.append("\n, ");
                            }
                            count++;
                            sb.append(field.getSQLName());
                            SQLUpdateGenerator.setTypeAndDefaultValue(sb, field, converter);
                            if (field.getAutoIncrement().equalsIgnoreCase("true")) {
                                sb.append(" AUTO_INCREMENT ");
                            }
                        }
                    }
                }

                sb.append("\n, PRIMARY KEY (");
                Iterator keys = input.getAllPrimaryKeys().iterator();
                int counter = 0;
                while (keys.hasNext()) {
                    GenericWrapperField key = (GenericWrapperField) keys.next();
                    if (counter++ == 0) {
                        sb.append(key.getSQLName());
                    } else {
                        sb.append(",").append(key.getSQLName());
                    }
                }
                sb.append(") ");

                for (Object o : input.getUniqueFields()) {
                    GenericWrapperField unique = (GenericWrapperField) o;
                    sb.append("\n, UNIQUE KEY (").append(unique.getSQLName())
                            .append(") ");
                }

                Enumeration uniqueComp = input.getUniqueCompositeFields()
                        .keys();
                if (uniqueComp.hasMoreElements()) {
                    while (uniqueComp.hasMoreElements()) {
                        sb.append("\n, UNIQUE KEY (");
                        counter = 0;
                        String s = (String) uniqueComp.nextElement();
                        ArrayList al = (ArrayList) input
                                .getUniqueCompositeFields().get(s);

                        Iterator alIter = al.iterator();
                        while (alIter.hasNext()) {
                            GenericWrapperField unique = (GenericWrapperField) alIter
                                    .next();
                            if (counter++ == 0) {
                                sb.append(unique.getSQLName());
                            } else {
                                sb.append(",").append(unique.getSQLName());
                            }
                        }
                        sb.append(") ");
                    }
                }
                sb.append(";");
            } else {

                //				boolean addedSequence = false;
                //				String sequenceName = "";
                //				if (input.isAutoIncrement())
                //				{
                //					GenericWrapperField pk =
                // (GenericWrapperField)input.getAllPrimaryKeys().get(0);
                //					sequenceName = input.getSQLName() + "_" + pk.getSQLName() +
                // "_seq";
                //					sb.append("CREATE SEQUENCE " + sequenceName +" INCREMENT 1
                // MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE
                // 1;\n\n");
                //					addedSequence = true;
                //				}
                //POSTGRES
                Iterator iter = input.getAllFieldsWAddIns(false, true)
                        .iterator();
                sb.append("CREATE TABLE " + input.getSQLName() + " (");
                int count = 0;
                TypeConverter converter = new TypeConverter(new PGSQLMapping(
                        input.getWrapped().getSchemaPrefix()));
                while (iter.hasNext()) {
                    GenericWrapperField field = (GenericWrapperField) iter
                            .next();
                    if (field.isReference()) {
                        if ((field.isMultiple()
                                && field.getRelationType().equalsIgnoreCase(
                                        "single") && field.getXMLType()
                                .getFullForeignType().equalsIgnoreCase(
                                        input.getFullXMLName()))
                                || (!field.isMultiple())) {
                            try {
                                XFTReferenceI ref = field.getXFTReference();
                                if (!ref.isManyToMany()) {
                                    for (XFTRelationSpecification spec : ((XFTSuperiorReference) ref)
                                            .getKeyRelations()) {
                                        if (count == 0) {
                                            sb.append("\n");
                                        } else {
                                            sb.append("\n, ");
                                        }
                                        count++;
                                        sb.append(spec.getLocalCol()).append(
                                                " ");
                                        if (spec.getLocalKey() != null) {
                                            if (spec.getLocalKey()
                                                    .getAutoIncrement()
                                                    .equalsIgnoreCase("true")) {
                                            	if(field.getType(converter).equalsIgnoreCase("BIGINT")){
                                            		sb.append(" bigserial").append(" ");
                                            	} else {
                                            		sb.append(" serial").append(" ");
                                            	}
                                                if (spec.getLocalKey()
                                                        .isRequired()) {
                                                    sb.append(" NOT NULL ");
                                                }

                                                //												if (addedSequence)
                                                //												{
                                                //													sb.append("DEFAULT
                                                // nextval('").append(sequenceName).append("'::text)
                                                // ");
                                                //												}
                                                //sb.append(" serial").append("
                                                // DEFAULT nextval('" +
                                                // input.getSQLName() + "_" +
                                                // key.getSQLName() + "_seq')
                                                // ");
                                                //sb = (new
                                                // StringBuffer()).append("CREATE
                                                // SEQUENCE " +
                                                // input.getSQLName() + "_" +
                                                // key.getSQLName() +
                                                // "_seq;\n\n").append(sb.toString());
                                            } else {
                                                SQLUpdateGenerator.setTypeAndDefaultValue(sb, spec, converter);
                                            }
                                        } else {
                                            if (spec.getForeignKey() != null) {
                                                sb
                                                        .append(
                                                                spec
                                                                        .getForeignKey()
                                                                        .getType(
                                                                                converter))
                                                        .append(" ");
                                            } else {
                                                sb
                                                        .append(
                                                                converter
                                                                        .convert(spec
                                                                                .getSchemaType()
                                                                                .getFullLocalType()))
                                                        .append(" ");
                                            }
                                        }

                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if (GenericWrapperField.IsLeafNode(field.getWrapped())) {
                            if (count == 0) {
                                sb.append("\n");
                            } else {
                                sb.append("\n, ");
                            }
                            count++;
                            sb.append(field.getSQLName()).append(" ");
                            final boolean required = field.isRequired();
                            if (field.getAutoIncrement().equalsIgnoreCase("true")) {
                                if (field.getType(converter).equalsIgnoreCase("BIGINT")) {
                                    sb.append(" bigserial").append(" ");
                                } else {
                                    sb.append(" serial").append(" ");
                                }
                                if (required) {
                                    sb.append(" NOT NULL ");
                                }
                                //								if (addedSequence)
                                //								{
                                //									sb.append("DEFAULT
                                // nextval('").append(sequenceName).append("'::text)
                                // ");
                                //								}
                                //sb.append(" serial").append(" DEFAULT
                                // nextval('" + input.getSQLName() + "_" +
                                // field.getSQLName() + "_seq') ");
                                //sb = (new StringBuffer()).append("CREATE
                                // SEQUENCE " + input.getSQLName() + "_" +
                                // field.getSQLName() +
                                // "_seq;\n\n").append(sb.toString());
                            } else {
                                SQLUpdateGenerator.setTypeAndDefaultValue(sb, field, converter);
                            }
                        }
                    }
                }

                sb.append("\n, PRIMARY KEY (");
                Iterator keys = input.getAllPrimaryKeys().iterator();
                int counter = 0;
                while (keys.hasNext()) {
                    GenericWrapperField key = (GenericWrapperField) keys.next();
                    if (counter++ == 0) {
                        sb.append(key.getSQLName());
                    } else {
                        sb.append(",").append(key.getSQLName());
                    }
                }
                sb.append(") ");

                int i = 1;
                Iterator uniques = input.getUniqueFields().iterator();
                while (uniques.hasNext()) {
                    GenericWrapperField unique = (GenericWrapperField) uniques
                            .next();
                    sb.append(
                            "\n, CONSTRAINT " + input.getSQLName() + "_U_"
                                    + (i++) + " UNIQUE (").append(
                            unique.getSQLName()).append(") ");
                }

                //				Iterator uniqueComp =
                // input.getUniqueCompositeFields().iterator();
                //				if (uniqueComp.hasNext())
                //				{
                //					sb.append("\n, CONSTRAINT "+ input.getSQLName() + "_U_"+
                // (i++) +" UNIQUE (");
                //					counter = 0;
                //					while (uniqueComp.hasNext())
                //					{
                //						GenericWrapperField unique =
                // (GenericWrapperField)uniqueComp.next();
                //						if (counter++ == 0)
                //						{
                //							sb.append(unique.getSQLName());
                //						}else
                //						{
                //							sb.append(",").append(unique.getSQLName());
                //						}
                //					}
                //					sb.append(") ");
                //				}

                Enumeration uniqueComp = input.getUniqueCompositeFields()
                        .keys();
                if (uniqueComp.hasMoreElements()) {
                    while (uniqueComp.hasMoreElements()) {
                        String s = (String) uniqueComp.nextElement();
                        sb.append("\n, CONSTRAINT " + input.getSQLName()
                                + "_U_" + s + " UNIQUE (");
                        counter = 0;

                        ArrayList al = (ArrayList) input
                                .getUniqueCompositeFields().get(s);

                        Iterator alIter = al.iterator();
                        while (alIter.hasNext()) {
                            GenericWrapperField unique = (GenericWrapperField) alIter
                                    .next();

                            if (counter++ != 0) {
                                sb.append(",");
                            }

                            if (unique.isReference()) {
                                XFTReferenceI ref = unique.getXFTReference();
                                if (!ref.isManyToMany()) {
                                    Iterator specs = ((XFTSuperiorReference) ref)
                                            .getKeyRelations().iterator();
                                    while (specs.hasNext()) {
                                        XFTRelationSpecification spec = (XFTRelationSpecification) specs
                                                .next();
                                        sb.append(spec.getLocalCol()).append(
                                                " ");
                                    }
                                }
                            } else {
                                sb.append(unique.getSQLName());
                            }
                        }
                        sb.append(") ");
                    }
                }
            }
            sb.append(") WITH OIDS;");

        } catch (org.nrg.xft.exception.XFTInitException e) {
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        }
        return new StringBuffer(sb.toString());
    }

    /**
     * Generates the CREATE TABLE SQL statement for this XFTManyToManyReference.
     *
     * @param input
     * @return Returns the CREATE TABLE SQL statement as a StringBuffer
     * @throws XFTInitException
     */
	public static StringBuffer GetCreateStatement(XFTManyToManyReference input)
            throws XFTInitException {
        StringBuffer sb = new StringBuffer();

        //		String sequenceName = input.getMappingTable() + "_seq";
        //		sb.append("CREATE SEQUENCE " + sequenceName +" INCREMENT 1 MINVALUE 1
        // MAXVALUE 9223372036854775807 START 1 CACHE 1;\n\n");
        //		
        Iterator iter = input.getMappingColumns().iterator();
        sb.append("CREATE TABLE " + input.getMappingTable() + " (");
        int count = 0;
        String schemaPrefix = input.getElement1().getWrapped()
                .getSchemaPrefix();
        TypeConverter converter = new TypeConverter(
                new SQLMapping(schemaPrefix));

        while (iter.hasNext()) {
            XFTMappingColumn field = (XFTMappingColumn) iter.next();
            if (count == 0) {
                sb.append("\n");
            } else {
                sb.append("\n, ");
            }
            count++;
            sb.append(field.getLocalSqlName()).append(" ");
            sb.append(
                    converter.convert(field.getXmlType()
                                           .getFullLocalType())).append(" NOT NULL ");

        }

        if (count == 0) {
            sb.append("\n");
        } else {
            sb.append("\n, ");
        }
        count++;
        sb.append(input.getMappingTable() + "_id").append(" ");

        if (input.getElement1().getSchema().getDbType().equalsIgnoreCase(
                "MYSQL")) {
            sb.append(converter.convert(schemaPrefix + ":integer")).append(" ");
        } else {
            sb.append("serial").append(" ");
            //sb.append("DEFAULT
            // nextval('").append(sequenceName).append("'::text) ");
        }

        sb.append("\n, PRIMARY KEY (");
        sb.append(input.getMappingTable() + "_id)");

        if (input.isUnique()) {
            sb.append("\n, UNIQUE KEY (");

            iter = input.getMappingColumns().iterator();
            count = 0;
            while (iter.hasNext()) {
                XFTMappingColumn field = (XFTMappingColumn) iter.next();
                if (count++ != 0) {
                    sb.append(", ");
                }
                sb.append(field.getLocalSqlName()).append(" ");
            }
            sb.append(") ");
        }
        sb.append(");");

        //CREATE HISTORY TABLE
        iter = input.getMappingColumns().iterator();
        sb.append("\n\nCREATE TABLE " + input.getHistoryTableName() + " (");
        count = 0;

        while (iter.hasNext()) {
            XFTMappingColumn field = (XFTMappingColumn) iter.next();
            if (count == 0) {
                sb.append("\n");
            } else {
                sb.append("\n, ");
            }
            count++;
            sb.append(field.getLocalSqlName()).append(" ");
            sb.append(
                    converter.convert((String) field.getXmlType()
                            .getFullLocalType())).append(" ");

        }

        if (count == 0) {
            sb.append("\n");
        } else {
            sb.append("\n, ");
        }
        count++;
        sb.append(input.getMappingTable() + "_id").append(" ");

        if (input.getElement1().getSchema().getDbType().equalsIgnoreCase(
                "MYSQL")) {
            sb.append(converter.convert(schemaPrefix + ":integer")).append(" ");
        } else {
            sb.append("int4").append(" ");
            //sb.append("DEFAULT
            // nextval('").append(sequenceName).append("'::text) ");
        }

        sb.append("\n, history_id SERIAL");

        sb.append("\n, PRIMARY KEY (history_id)");
        sb.append(") WITH OIDS;");

        return sb;
    }

    public static List<String>[] GetFunctionSQL() {
        List<String> all = new ArrayList<String>();
        List<String> post = new ArrayList<String>();
        try {
            for (Object o : XFTManager.GetInstance().getOrderedElements()) {
                GenericWrapperElement element = (GenericWrapperElement) o;
                List<String>[] func=GetFunctionStatements(element);
                all.addAll(func[0]);
                post.addAll(func[1]);
            }
        } catch (XFTInitException e) {
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }
        List[] _return={all,post};
        return _return;
    }
    
    public static List<String> GetTextOutputFunctions(GenericWrapperElement input)
    throws ElementNotFoundException, XFTInitException 
    {
        List<String> all = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();

        Object[][] keyArray = input.getSQLKeys();

        String functionName = input.getTextFunctionName() + "(";
        if (input.isExtended() && (!(input.getName().endsWith("meta_data") || input
                .getName().endsWith("history")))) {
            functionName = GenericWrapperUtils.TXT_EXT_FUNCTION + input.getFormattedName() + "(";
        }
        for (int i = 0; i < keyArray.length; i++) {
            if (i > 0)
                functionName += ",";
            functionName += " " + keyArray[i][1];
        }
        functionName += ", int4,bool,bool,bool)";
        sb.append("\n\n\nCREATE OR REPLACE FUNCTION ").append(functionName);

        String counter = "$" + (keyArray.length + 1);
        String allowMultiples = "$" + (keyArray.length + 2);
        String isRoot = "$" + (keyArray.length + 3);
        String preventLoop = "$" + (keyArray.length + 4);
        sb.append("\n  RETURNS TEXT AS");
        sb.append("\n'");
        sb.append("\n    declare");
        sb.append("\n     current_row RECORD;");
        sb.append("\n     tempText TEXT;");
        sb.append("\n     fullText TEXT;");
        sb.append("\n     row_ct int4;");
        sb.append("\n     local_count int4;");
        sb.append("\n     child_count int4;");
        sb.append("\n    begin");
        sb.append("\n    --ID,counter,allowMultiples,isRoot,preventLoop");

        //sb.append("\n RAISE NOTICE ''" +functionName + "'';");
        sb.append("\n      local_count := ").append(counter).append(";");
        sb.append("\n      child_count := ").append(counter).append(";");
        sb.append("\n      row_ct := 0;");
        sb.append("\n      fullText := ''Item:(''");
        sb.append(" || local_count || ''(").append(input.getXSIType()).append(
                ")('';");
        sb.append("\n      FOR current_row IN SELECT * FROM ").append(input.getSQLName());
        sb.append(" WHERE ");
        for (int i = 0; i < keyArray.length; i++) {
            if (i > 0)
                sb.append(" AND ");
            sb.append(" ").append(keyArray[i][0]).append("=").append(
                    keyArray[i][2]);
        }
        sb.append("\n      LOOP");
        sb.append("\n           row_ct := row_ct+1;");
        sb.append("\n          ");
        Iterator fieldIter = input.getAllFields(false, true).iterator();
        TypeConverter converter = new TypeConverter(new PGSQLMapping(input
                .getWrapped().getSchemaPrefix()));
        while (fieldIter.hasNext()) {
            GenericWrapperField field = (GenericWrapperField) fieldIter.next();
            if (field.isReference()) {
                GenericWrapperElement foreign = (GenericWrapperElement) field
                        .getReferenceElement();
                if (field.isMultiple()) {
                    if (!(input.getName().endsWith("meta_data") || input
                            .getName().endsWith("history"))) {
                        try {
                            XFTReferenceI ref = field.getXFTReference();
                            if (ref.isManyToMany()) {
                                
                                String mappingTable = ((XFTManyToManyReference) ref)
                                        .getMappingTable();
                                sb.append("\n        IF(").append(allowMultiples);
                                if (field.isOnlyRoot())
                                {
                                    sb.append(" AND "+ isRoot +"");
                                }
                                
                                if (field.isPossibleLoop()){
                                    sb.append(" AND ( NOT "+ preventLoop +")");
                                }
                                
                                sb.append(") THEN ");
          
                                sb.append("\n        DECLARE ");
                                sb.append("\n        mapping_row RECORD; ");
                                sb.append("\n        loop_count int4:=0; ");
                                sb.append("\n        BEGIN ");
                                sb
                                        .append("\n        FOR mapping_row IN SELECT * FROM "
                                                + mappingTable + " WHERE ");
                                Iterator refCols = ((XFTManyToManyReference) ref)
                                        .getMappingColumnsForElement(input)
                                        .iterator();
                                int count = 0;
                                while (refCols.hasNext()) {
                                    XFTMappingColumn spec = (XFTMappingColumn) refCols
                                            .next();
                                    if (count++ > 0)
                                        sb.append(" AND ");
                                    sb.append(" ").append(
                                            spec.getLocalSqlName()).append("=");
                                    sb.append("current_row.").append(
                                            spec.getForeignKey().getSQLName());
                                }
//                                sb.append(" ORDER BY ");
//                                refCols = ((XFTManyToManyReference) ref).getMappingColumnsForElement(foreign).iterator();
//		                        count = 0;
//		                        while (refCols.hasNext()) {
//		                            XFTMappingColumn spec = (XFTMappingColumn) refCols
//		                                    .next();
//		                            if (count++ > 0)
//		                                sb.append(", ");
//		                            sb.append(spec.getLocalSqlName());
//		                        }
                                sb.append("\n        LOOP");
                                
                                sb.append("\n           child_count := child_count+1;");
                                sb.append("\n           tempText := NULL;");
                                sb.append("\n           tempText := " + GenericWrapperUtils.TXT_FUNCTION)
                                        .append(foreign.getFormattedName()).append(
                                                "(");
                                refCols = ((XFTManyToManyReference) ref)
                                        .getMappingColumnsForElement(foreign)
                                        .iterator();
                                count = 0;
                                while (refCols.hasNext()) {
                                    XFTMappingColumn spec = (XFTMappingColumn) refCols
                                            .next();
                                    if (count++ > 0)
                                        sb.append(",");
                                    sb.append(" mapping_row.").append(
                                            spec.getLocalSqlName());
                                }
                                sb.append(", child_count," + allowMultiples + ",false," + field.getPreventLoop() + ");");
                                sb
                                        .append("\n              fullText := fullText || ''("
                                                + (field.getSQLName() + "_" + field
                                                        .getXMLType()
                                                        .getLocalType())
                                                        .toLowerCase()
                                                + "'' || loop_count || '':XFTItem)='';");
                                sb
                                        .append("\n              fullText := fullText || ''('' || tempText || '')'';");
                                sb
                                        .append("\n              loop_count := loop_count+1;");
                                sb.append("\n        END LOOP;");
                                sb.append("\n        END; ");
                                sb.append("\n        END IF; ");
                            } else {
                                XFTSuperiorReference supRef = (XFTSuperiorReference) ref;
                                sb.append("\n        IF(").append(allowMultiples);
                                if (field.isOnlyRoot())
                                {
                                    sb.append(" AND "+ isRoot +"");
                                }
                                
                                if (field.isPossibleLoop()){
                                    sb.append(" AND (NOT "+ preventLoop +")");
                                }
                                
                                sb.append(") THEN ");
                                //                              FOREIGN has the fk column
                                sb.append("\n        DECLARE ");
                                sb.append("\n        parent_row RECORD; ");
                                sb.append("\n        loop_count int4:=0; ");
                                sb.append("\n        BEGIN ");
                                sb.append("\n        FOR parent_row IN SELECT * FROM "
                                                + foreign.getSQLName()
                                                + " WHERE ");

                                Iterator refsCols = supRef.getKeyRelations()
                                        .iterator();
                                int count = 0;
                                while (refsCols.hasNext()) {
                                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
                                            .next();
                                    if (count++ > 0)
                                        sb.append(" AND ");
                                    sb.append(spec.getLocalCol()).append(
                                            "=current_row.").append(
                                            spec.getForeignCol());
                                }

                                Object[][] foreignKeyArray = foreign.getSQLKeys();
                              sb.append(" ORDER BY ");
		                        for (int i = 0; i < foreignKeyArray.length; i++) {
		                            if (i > 0)
		                                sb.append(", ");
		                            sb.append(foreignKeyArray[i][0]);
		                        }
                                sb.append("\n        LOOP");
                                sb
                                        .append("\n           child_count := child_count+1;");
                                sb.append("\n           tempText := NULL;");
                                sb.append("\n           tempText := " + GenericWrapperUtils.TXT_FUNCTION)
                                        .append(foreign.getFormattedName()).append(
                                                "(");
                                foreignKeyArray = foreign
                                        .getSQLKeys();
                                for (int i = 0; i < foreignKeyArray.length; i++) {
                                    if (i > 0)
                                        sb.append(", ");
                                    sb.append(" parent_row.").append(
                                            foreignKeyArray[i][0]);
                                }
                                sb.append(", child_count," + allowMultiples + ",false," + field.getPreventLoop() + ");");
                                sb
                                        .append("\n              fullText := fullText || ''("
                                                + (field.getSQLName() + "_" + field
                                                        .getXMLType()
                                                        .getLocalType())
                                                        .toLowerCase()
                                                + "'' || loop_count || '':XFTItem)='';");
                                sb
                                        .append("\n              fullText := fullText || ''('' || tempText || '')'';");
                                sb
                                        .append("\n              loop_count := loop_count+1;");
                                sb.append("\n        END LOOP;");
                                sb.append("\n        END;");
                                sb.append("\n        END IF; ");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    XFTSuperiorReference supRef = (XFTSuperiorReference) field
                            .getXFTReference();
                    if (supRef.getSubordinateElement().equals(input)) {
                        //INPUT has the fk column (check if it is null)
                        sb.append("\n        IF (");
                        Iterator refsCols = supRef.getKeyRelations().iterator();
                        int count = 0;
                        while (refsCols.hasNext()) {
                            XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
                                    .next();
                            if (count++ > 0)
                                sb.append(" && ");
                            sb.append("(current_row.").append(
                                    spec.getLocalCol()).append(" IS NOT NULL)");

                        }
                        
                        if (field.isOnlyRoot())
                        {
                            sb.append(" AND "+ isRoot +"");
                        }
                        
                        if (field.isPossibleLoop()){
                            sb.append(" AND (NOT "+ preventLoop +")");
                        }
                        
                        sb.append(") THEN");

                        refsCols = supRef.getKeyRelations().iterator();
                        while (refsCols.hasNext()) {
                            XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
                                    .next();
                            sb
                                    .append(
                                            "\n            fullText := fullText || ''(")
                                    .append(spec.getLocalCol().toLowerCase())
                                    .append(":" + spec.getSchemaType().getLocalType() + ")=('' || current_row.").append(
                                            spec.getLocalCol().toLowerCase())
                                    .append(" || '')'';");
                        }
                        if (!(input.getName().endsWith("meta_data") || input
                                .getName().endsWith("history"))) {
                            sb.append("\n           child_count := child_count+1;");
                            sb.append("\n           tempText := NULL;");
                            if (input.getExtensionFieldName().equalsIgnoreCase(field.getName()))
                            {
                                sb.append("\n           tempText := " + GenericWrapperUtils.TXT_EXT_FUNCTION)
                                .append(foreign.getFormattedName()).append("(");
                            }else{
                                sb.append("\n           tempText := " + GenericWrapperUtils.TXT_FUNCTION)
                                .append(foreign.getFormattedName()).append("(");
                            }
                            refsCols = supRef.getKeyRelations().iterator();
                            count = 0;
                            while (refsCols.hasNext()) {
                                XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
                                        .next();
                                if (count++ > 0)
                                    sb.append(", ");
                                sb.append("current_row.").append(
                                        spec.getLocalCol());
                            }
                            sb.append(", child_count," + allowMultiples + ",false," + field.getPreventLoop() + ");");
                            sb
                                    .append("\n              fullText := fullText || ''("
                                            + (field.getSQLName() + "_" + field
                                                    .getXMLType()
                                                    .getLocalType())
                                                    .toLowerCase() + ":XFTItem)='';");
                            sb
                                    .append("\n              fullText := fullText || ''('' || tempText || '')'';");
                        }
                        sb.append("\n        END IF;");
                    } else {

                        if (!(input.getName().endsWith("meta_data") || input
                                .getName().endsWith("history"))) {
                            //FOREIGN has the fk column
                            sb.append("\n        IF("+ allowMultiples +") THEN ");
                            sb.append("\n        DECLARE ");
                            sb.append("\n        parent_row RECORD; ");
                            sb.append("\n        loop_count int4:=0; ");
                            sb.append("\n        BEGIN ");
                            sb
                                    .append("\n        FOR parent_row IN SELECT * FROM "
                                            + foreign.getSQLName() + " WHERE ");

                            Iterator refsCols = supRef.getKeyRelations()
                                    .iterator();
                            int count = 0;
                            while (refsCols.hasNext()) {
                                XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
                                        .next();
                                if (count++ > 0)
                                    sb.append(" AND ");
                                sb.append(spec.getLocalCol()).append(
                                        "=current_row.").append(
                                        spec.getForeignCol());
                            }

                            sb.append(" ORDER BY ");
                            Object[][] foreignKeyArray = foreign.getSQLKeys();
	                        for (int i = 0; i < foreignKeyArray.length; i++) {
	                            if (i > 0)
	                                sb.append(", ");
	                            sb.append(foreignKeyArray[i][0]);
	                        }
                            sb.append("\n        LOOP");
                            sb
                                    .append("\n           child_count := child_count+1;");
                            sb.append("\n           tempText := NULL;");
                            sb.append("\n           tempText := " + GenericWrapperUtils.TXT_FUNCTION)
                                    .append(foreign.getFormattedName()).append("(");
                            foreignKeyArray = foreign.getSQLKeys();
                            for (int i = 0; i < foreignKeyArray.length; i++) {
                                if (i > 0)
                                    sb.append(", ");
                                sb.append(" parent_row.").append(
                                        foreignKeyArray[i][0]);
                            }
                            sb.append(", child_count," + allowMultiples + ",false," + field.getPreventLoop() + ");");
                            sb
                                    .append("\n              fullText := fullText || ''("
                                            + (field.getSQLName() + "_" + field
                                                    .getXMLType()
                                                    .getLocalType())
                                                    .toLowerCase()
                                            + "'' || loop_count || '':XFTItem)='';");
                            sb
                                    .append("\n              fullText := fullText || ''('' || tempText || '')'';");
                            sb
                                    .append("\n              loop_count := loop_count+1;");
                            sb.append("\n        END LOOP;");
                            sb.append("\n        END;");
                            sb.append("\n        END IF; ");
                        }
                    }
                }

            } else {
                if (GenericWrapperField.IsLeafNode(field.getWrapped())) {

                    sb.append("\n          IF (current_row.").append(
                            field.getSQLName().toLowerCase()).append(
                            " IS NOT NULL) THEN ");

                    String type = field.getType(converter);
                    
                    if (type.equals("BYTEA")) {
                        sb.append("\n              fullText := fullText || ''(");
                        sb.append(field.getSQLName().toLowerCase());
                        sb.append(":string)=('' || REPLACE(REPLACE(ENCODE(current_row.").append(field.getSQLName().toLowerCase());
                        sb.append(",''escape''),''('',''*OPEN*''),'')'',''*CLOSE*'') || '')'';");
                    }else if (type.equals("DATE")) {
                        sb.append("\n              fullText := fullText || ''(").append(field.getSQLName().toLowerCase());
                        sb.append(":date)=('' || current_row.").append(field.getSQLName().toLowerCase()).append(" || '')'';");
                    }else if (type.equals("TIMESTAMP")) {
                        sb.append("\n              fullText := fullText || ''(").append(field.getSQLName().toLowerCase());
                        sb.append(":dateTime)=('' || current_row.").append(field.getSQLName().toLowerCase()).append(" || '')'';");
                    }else if (type.equals("TIME")) {
                        sb.append("\n              fullText := fullText || ''(").append(field.getSQLName().toLowerCase());
                        sb.append(":time)=('' || current_row.").append(field.getSQLName().toLowerCase()).append(" || '')'';");
                    }else if (type.equals("FLOAT")) {
                        sb.append("\n              fullText := fullText || ''(").append(field.getSQLName().toLowerCase());
                        sb.append(":float)=('' || current_row.").append(field.getSQLName().toLowerCase()).append(" || '')'';");
                    }else if (type.equals("NUMERIC") || type.equals("DECIMAL")) {
                        sb.append("\n              fullText := fullText || ''(").append(field.getSQLName().toLowerCase());
                        sb.append(":double)=('' || current_row.").append(field.getSQLName().toLowerCase()).append(" || '')'';");
                    }else if (type.equals("INTEGER") || type.equals("BIGINT") || type.equals("SMALLINT") || type.equals("TINYINT")) {
                        sb.append("\n              fullText := fullText || ''(").append(field.getSQLName().toLowerCase());
                        sb.append(":integer)=('' || current_row.").append(field.getSQLName().toLowerCase()).append(" || '')'';");
                    } else if (type.startsWith("VARCHAR")) {
                        sb.append("\n              fullText := fullText || ''(").append(field.getSQLName().toLowerCase());
                        sb.append(":string)=('' || REPLACE(REPLACE(current_row.").append(field.getSQLName().toLowerCase()).append(",''('',''*OPEN*''),'')'',''*CLOSE*'') || '')'';");
                    } else if (type.startsWith("TEXT")) {
                        sb.append("\n              fullText := fullText || ''(").append(field.getSQLName().toLowerCase());
                        sb.append(":string)=('' || REPLACE(REPLACE(current_row.").append(field.getSQLName().toLowerCase()).append(",''('',''*OPEN*''),'')'',''*CLOSE*'') || '')'';");
                    } else {
                        sb.append("\n              fullText := fullText || ''(").append(field.getSQLName().toLowerCase());
                        sb.append(":string)=('' || current_row.").append(field.getSQLName().toLowerCase()).append(" || '')'';");
                    }
                    sb.append("\n          END IF;");
                }
            }
        }
        for (GenericWrapperField field : input.getUndefinedReferences()) {
            if (field.isReference() && (!field.isMultiple())) {
                XFTSuperiorReference supRef = (XFTSuperiorReference) field.getXFTReference();
                sb.append("\n        IF (");
                Iterator refsCols = supRef.getKeyRelations().iterator();
                int count = 0;
                while (refsCols.hasNext()) {
                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
                            .next();
                    if (count++ > 0)
                        sb.append(" && ");
                    sb.append("(current_row.").append(
                            spec.getLocalCol()).append(" IS NOT NULL)");

                }

                if (field.isOnlyRoot()) {
                    sb.append(" && " + isRoot + "");
                }

                if (field.isPossibleLoop()) {
                    sb.append(" AND NOT (" + preventLoop + ")");
                }

                sb.append(") THEN");

                refsCols = supRef.getKeyRelations().iterator();
                while (refsCols.hasNext()) {
                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
                            .next();
                    sb
                            .append(
                                    "\n            fullText := fullText || ''(")
                            .append(spec.getLocalCol().toLowerCase())
                            .append(":" + spec.getSchemaType().getLocalType() + ")=('' || current_row.").append(
                            spec.getLocalCol().toLowerCase())
                            .append(" || '')'';");
                }
                sb.append("\n        END IF;");
            }
        }
        if (!input.containsStatedKey()) {
			GenericWrapperField field = (GenericWrapperField)input.getDefaultKey();
			sb.append("\n          IF (current_row.").append(
                    field.getSQLName().toLowerCase()).append(
                    " IS NOT NULL) THEN ");
            
            sb.append("\n              fullText := fullText || ''(").append(field.getSQLName().toLowerCase());
            sb.append(":integer)=('' || current_row.").append(field.getSQLName().toLowerCase()).append(" || '')'';");
            
            sb.append("\n          END IF;");
		}
        
        sb.append("\n      END LOOP;");
        if(input.isExtension())
        {
            try {
                GenericWrapperField field = input.getExtensionField();
                sb.append("\n      IF (row_ct=0) THEN ");
	
                GenericWrapperElement foreign = (GenericWrapperElement)field.getReferenceElement();
//                Iterator refsCols = supRef.getKeyRelations().iterator();
//                while (refsCols.hasNext()) {
//                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
//                            .next();
//                    sb
//                            .append(
//                                    "\n            fullText := fullText || ''(")
//                            .append(spec.getLocalCol().toLowerCase())
//                            .append(":" + spec.getSchemaType().getLocalType() + ")=('' || current_row.").append(
//                                    spec.getLocalCol().toLowerCase())
//                            .append(" || '')'';");
//                }
                if (!(input.getName().endsWith("meta_data") || input
                        .getName().endsWith("history"))) {
                    sb.append("\n           child_count := child_count+1;");
                    sb.append("\n           tempText := NULL;");
                    sb.append("\n           tempText := "+TXT_EXT_FUNCTION)
                        .append(foreign.getFormattedName()).append("(");

                    for (int i = 0; i < keyArray.length; i++) {
                        if (i > 0)
                            sb.append(",");
                        sb.append(" ").append(keyArray[i][2]);
                    }
                    sb.append(", child_count," + allowMultiples + "," + isRoot + "," + field.getPreventLoop() + ");");
                    sb
                            .append("\n              fullText := fullText || ''("
                                    + (field.getSQLName() + "_" + field
                                            .getXMLType()
                                            .getLocalType())
                                            .toLowerCase() + ":XFTItem)='';");
                    sb
                            .append("\n              fullText := fullText || ''('' || tempText || '')'';");
                }
                sb.append("\n          ");
                sb.append("\n      END IF;");
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            } catch (FieldNotFoundException e) {
                logger.error("",e);
            } catch (XFTInitException e) {
                logger.error("",e);
            }
        }
        sb.append("\n	   fullText := fullText || '')*END_ITEM*'' || local_count || '')'';");
        sb.append("\n	");
        sb.append("\n	 RETURN fullText;");
        sb.append("\n    end;");
        sb.append("\n'");
        sb.append("\n  LANGUAGE 'plpgsql' VOLATILE;");

        all.add(sb.toString());
        sb = new StringBuffer();
        if (input.isExtended() && (!(input.getName().endsWith("meta_data") || input
                .getName().endsWith("history")))) {
	        functionName = input.getTextFunctionName() + "(";
	        for (int i = 0; i < keyArray.length; i++) {
	            if (i > 0)
	                functionName += ",";
	            functionName += " " + keyArray[i][1];
	        }
	        functionName += ", int4,bool,bool,bool)";
	        sb.append("\n\n\nCREATE OR REPLACE FUNCTION " + functionName);
	
	        sb.append("\n  RETURNS TEXT AS");
	        sb.append("\n'");
	        sb.append("\n    declare");
	        sb.append("\n     current_row RECORD;");
	        sb.append("\n     fullText TEXT;");
	        sb.append("\n    begin");
	
	        //sb.append("\n RAISE NOTICE ''" +functionName + "'';");
	        if (input.isExtended() && (!(input.getName().endsWith("meta_data") || input
	                .getName().endsWith("history")))) {
	            //Build Query to get Extension id
	            try {
	                QueryOrganizer qo = new QueryOrganizer(input, null,
	                        ViewManager.ALL);
	                qo.addField(input.getFullXMLName()
	                        + "/extension_item/element_name");
	                CriteriaCollection cc = new CriteriaCollection("AND");
	                for (int i = 0; i < keyArray.length; i++) {
	                    GenericWrapperField gwf = (GenericWrapperField) keyArray[i][3];
	                    qo.addField(gwf
	                            .getXMLPathString(input.getFullXMLName()));
	
	                    SearchCriteria sc = new SearchCriteria();
	                    sc.setFieldWXMLPath(gwf.getXMLPathString(input
	                            .getFullXMLName()));
	                    sc.setOverrideFormatting(true);
	                    sc.setValue(keyArray[i][2]);
	                    cc.add(sc);
	                }
	
	                String query = "SELECT * FROM (" + qo.buildQuery()
	                        + ") SEARCH";
	                query += " WHERE " + cc.getSQLClause(qo);
	
	                String colname = qo.translateXMLPath(input.getFullXMLName()
	                        + "/extension_item/element_name");
	
	                sb.append("\n      FOR current_row IN ").append(query);
	                sb.append("\n      LOOP");
	                sb.append("\n         IF (current_row.").append(colname)
	                        .append(" IS NULL) THEN ");
	                sb.append("\n             fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION
	                        + input.getFormattedName() + "(");
	                for (int i = 0; i < keyArray.length; i++) {
	                    if (i > 0)
	                        sb.append(",");
	                    sb.append(" ").append(keyArray[i][2]);
	                }
	                sb.append(", ").append(counter).append("," + allowMultiples + "," + isRoot +"," + preventLoop + ");");
	                sb.append("\n         ELSE");
	                sb.append("\n            --CALL EXTENDER ");
	                sb.append("\n            declare");
	                sb.append("\n               matches int4:=0;");
	                sb.append("\n            begin");
	                Iterator pEs = input.getPossibleExtenders().iterator();
	                while (pEs.hasNext()) {
	                    SchemaElementI se = (SchemaElementI) pEs.next();
	                    sb.append("\n            IF (current_row.").append(
	                            colname).append("=''").append(
	                            se.getFullXMLName()).append("'') THEN");
	                    //sb.append("\n RAISE NOTICE ''PASSING CALL FROM " +
	                    // input.getSQLName() + " TO " + se.getSQLName() +
	                    // "'';");
	                    if (se.getGenericXFTElement().isExtended())
	                        sb.append("\n                fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION
	                            + se.getFormattedName() + "(");
	                    else
		                    sb.append("\n                fullText:= " + GenericWrapperUtils.TXT_FUNCTION
		                            + se.getFormattedName() + "(");
	                    for (int i = 0; i < keyArray.length; i++) {
	                        if (i > 0)
	                            sb.append(",");
	                        sb.append(" ").append(keyArray[i][2]);
	                    }
	                    sb.append(", ").append(counter).append("," + allowMultiples + "," + isRoot +"," + preventLoop + ");");
	                    sb.append("\n                matches:=1;");
	                    sb.append("\n            END IF;");
	                }
	                sb.append("\n                IF (matches=0) THEN");
	               sb.append("\n                    fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION
	                        + input.getFormattedName() + "(");
	                for (int i = 0; i < keyArray.length; i++) {
	                    if (i > 0)
	                        sb.append(",");
	                    sb.append(" ").append(keyArray[i][2]);
	                }
	                sb.append(", ").append(counter).append("," + allowMultiples + "," + isRoot +"," + preventLoop + ");");
	                sb.append("\n                END IF;");
	                sb.append("\n            end;");
	                sb.append("\n         end IF;");
	                sb.append("\n      END LOOP;");
	            } catch (Exception e) {
	                logger.error("", e);
	                sb.append("\n      fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION
	                        + input.getFormattedName() + "(");
	                for (int i = 0; i < keyArray.length; i++) {
	                    if (i > 0)
	                        sb.append(",");
	                    sb.append(" ").append(keyArray[i][2]);
	                }
	                sb.append(", ").append(counter).append("," + allowMultiples + "," + isRoot +"," + preventLoop + ");");
	            }
	        } else {
	            sb.append("\n -- ITEM IS NOT EXTENDED BY ANYTHING... REDIRECT TO MAIN FUNCTION");
	            sb.append("\n      fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION + input.getFormattedName()
	                    + "(");
	            for (int i = 0; i < keyArray.length; i++) {
	                if (i > 0)
	                    sb.append(",");
	                sb.append(" ").append(keyArray[i][2]);
	            }
	            sb.append(", ").append(counter).append("," + allowMultiples + "," + isRoot +"," + preventLoop + ");");
	        }
	
	        sb.append("\n	");
	        sb.append("\n	RETURN fullText;");
	        sb.append("\n    end;");
	        sb.append("\n'");
	        sb.append("\n  LANGUAGE 'plpgsql' VOLATILE;");
	
	        all.add(sb.toString());
        }
        return all;
    }
    
    /**
     * @param input
     * @return Returns ArrayList of activate functions
     * @throws ElementNotFoundException
     * @throws XFTInitException
     */
    public static ArrayList GetActivateFunctions(GenericWrapperElement input)
    throws ElementNotFoundException, XFTInitException 
    {
        ArrayList all = new ArrayList();
        StringBuffer sb = new StringBuffer();

        Object[][] keyArray = input.getSQLKeys();

        String functionName = ACT_EXT_FUNCTION + input.getFormattedName() + "(";
        if (!input.isExtended())
        {
            functionName = ACT_FUNCTION + input.getFormattedName() + "(";
        }
        for (int i = 0; i < keyArray.length; i++) {
            if (i > 0)
                functionName += ",";
            functionName += " " + keyArray[i][1];
        }
        functionName += ", int4,varchar(255),bool)";
        sb.append("\n\n\nCREATE OR REPLACE FUNCTION " + functionName);

        String user = "$" + (keyArray.length + 1);
        String status = "$" + (keyArray.length + 2);
        String isRoot = "$" + (keyArray.length + 3);
        sb.append("\n  RETURNS TEXT AS");
        sb.append("\n'");
        sb.append("\n    declare");
        sb.append("\n     current_row RECORD;");
        sb.append("\n     row_ct int4;");
        sb.append("\n    begin");
        sb.append("\n      row_ct := 0;");
        if ((!(input.getName().endsWith("meta_data") || input
                .getName().endsWith("history")))) {
        //sb.append("\n RAISE NOTICE ''" +functionName + "'';");
        //UPDATE META STATUS
        sb.append("\n       --UPDATE LOCAL ROW");
        sb.append("\n      FOR current_row IN SELECT ").append(
                input.getSQLName());
        sb.append(".*, meta.meta_data_id, meta.status AS meta_status FROM ").append(input.getSQLName());
        sb.append(" LEFT JOIN ").append(input.getSQLName()).append(
                "_meta_data meta ON ").append(input.getSQLName());
        sb.append(".").append(input.getMetaDataFieldName()).append(
                "=meta.meta_data_id");
        sb.append(" WHERE ");
        for (int i = 0; i < keyArray.length; i++) {
            if (i > 0)
                sb.append(" AND ");
            sb.append(" ").append(keyArray[i][0]).append("=").append(
                    keyArray[i][2]);
        }
        sb.append("\n      LOOP");
        sb.append("\n           row_ct := row_ct+1;");

        sb.append("\n           IF (current_row.meta_status!=" + status +") THEN");
        sb
                .append("\n         UPDATE ")
                .append(input.getSQLName())
                .append(
                        "_meta_data SET status=" + status +",activation_date=NOW(),activation_user_xdat_user_id=" + user +" WHERE meta_data_id=current_row.meta_data_id;");

        sb.append("\n           END IF;");
        sb.append("\n          ");
        Iterator fieldIter = input.getAllFields(false, true).iterator();
        while (fieldIter.hasNext()) {
            GenericWrapperField field = (GenericWrapperField) fieldIter.next();
            if (field.isReference()) {
                GenericWrapperElement foreign = (GenericWrapperElement) field
                        .getReferenceElement();
                if (field.isMultiple()) {
                    if (!(foreign.getName().endsWith("meta_data") || foreign
                            .getName().endsWith("history"))) {
                        try {
                            XFTReferenceI ref = field.getXFTReference();
                            if (ref.isManyToMany()) {
                                
                                String mappingTable = ((XFTManyToManyReference) ref)
                                        .getMappingTable();
                                sb.append("\n        DECLARE ");
                                sb.append("\n        mapping_row RECORD; ");
                                sb.append("\n        BEGIN ");

                                if (field.isOnlyRoot())
                                {
                                    sb.append("\n        IF(" + isRoot + ") THEN ");
                                }
                                sb
                                        .append("\n        FOR mapping_row IN SELECT * FROM "
                                                + mappingTable + " WHERE ");
                                Iterator refCols = ((XFTManyToManyReference) ref)
                                        .getMappingColumnsForElement(input)
                                        .iterator();
                                int count = 0;
                                while (refCols.hasNext()) {
                                    XFTMappingColumn spec = (XFTMappingColumn) refCols
                                            .next();
                                    if (count++ > 0)
                                        sb.append(" AND ");
                                    sb.append(" ").append(
                                            spec.getLocalSqlName()).append("=");
                                    sb.append("current_row.").append(
                                            spec.getForeignKey().getSQLName());
                                }
                                sb.append("\n        LOOP");
                                
                                sb.append("\n          PERFORM  " + ACT_FUNCTION)
                                        .append(foreign.getFormattedName()).append(
                                                "(");
                                refCols = ((XFTManyToManyReference) ref)
                                        .getMappingColumnsForElement(foreign)
                                        .iterator();
                                count = 0;
                                while (refCols.hasNext()) {
                                    XFTMappingColumn spec = (XFTMappingColumn) refCols
                                            .next();
                                    if (count++ > 0)
                                        sb.append(",");
                                    sb.append(" mapping_row.").append(
                                            spec.getLocalSqlName());
                                }
                                sb.append(", " + user + "," + status + ",false);");
                                sb.append("\n        END LOOP;");

                                if (field.isOnlyRoot())
                                {
                                    sb.append("\n        END IF;");
                                }
                                sb.append("\n        END; ");
                            } else {
                                XFTSuperiorReference supRef = (XFTSuperiorReference) ref;

                                //                              FOREIGN has the fk column
                                sb.append("\n        DECLARE ");
                                sb.append("\n        parent_row RECORD; ");
                                sb.append("\n        BEGIN ");

                                if (field.isOnlyRoot())
                                {
                                    sb.append("\n        IF(" + isRoot + ") THEN ");
                                }
                                sb
                                        .append("\n        FOR parent_row IN SELECT * FROM "
                                                + foreign.getSQLName()
                                                + " WHERE ");

                                Iterator refsCols = supRef.getKeyRelations()
                                        .iterator();
                                int count = 0;
                                while (refsCols.hasNext()) {
                                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
                                            .next();
                                    if (count++ > 0)
                                        sb.append(" AND ");
                                    sb.append(spec.getLocalCol()).append(
                                            "=current_row.").append(
                                            spec.getForeignCol());
                                }
                                Object[][] foreignKeyArray = foreign.getSQLKeys();

                                sb.append("\n        LOOP");
                                sb.append("\n          PERFORM  " + ACT_FUNCTION)
                                        .append(foreign.getFormattedName()).append(
                                                "(");
                                foreignKeyArray = foreign
                                        .getSQLKeys();
                                for (int i = 0; i < foreignKeyArray.length; i++) {
                                    if (i > 0)
                                        sb.append(", ");
                                    sb.append(" parent_row.").append(
                                            foreignKeyArray[i][0]);
                                }
                                sb.append(", " + user + "," + status + ",false);");
                                sb.append("\n        END LOOP;");

                                if (field.isOnlyRoot())
                                {
                                    sb.append("\n        END IF; ");
                                }
                                sb.append("\n        END;");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    XFTSuperiorReference supRef = (XFTSuperiorReference) field
                            .getXFTReference();
                    if (supRef.getSubordinateElement().equals(input)) {
                        //INPUT has the fk column (check if it is null)

                        if (!(foreign.getName().endsWith("meta_element") || foreign.getName().endsWith("meta_data") || foreign
                                .getName().endsWith("history"))) {
                        sb.append("\n        IF (");
                        Iterator refsCols = supRef.getKeyRelations().iterator();
                        int count = 0;
                        while (refsCols.hasNext()) {
                            XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
                                    .next();
                            if (count++ > 0)
                                sb.append(" && ");
                            sb.append("(current_row.").append(
                                    spec.getLocalCol()).append(" IS NOT NULL)");

                        }


                        if (field.isOnlyRoot())
                        {
                            sb.append(" && " + isRoot + ") THEN ");
                        }else{
                            sb.append(") THEN");
                        }

                            if (input.getExtensionFieldName().equalsIgnoreCase(field.getName()))
                            {
                                sb.append("\n          PERFORM  " + ACT_EXT_FUNCTION)
                                .append(foreign.getFormattedName()).append("(");
                            }else{
                                sb.append("\n          PERFORM  " + ACT_FUNCTION)
                                .append(foreign.getFormattedName()).append("(");
                            }
                            refsCols = supRef.getKeyRelations().iterator();
                            count = 0;
                            while (refsCols.hasNext()) {
                                XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
                                        .next();
                                if (count++ > 0)
                                    sb.append(", ");
                                sb.append("current_row.").append(
                                        spec.getLocalCol());
                            }
                            sb.append(", " + user + "," + status + ",false);");
                            sb.append("\n        END IF;");
                        }
                    } else {

                        if (!(foreign.getName().endsWith("meta_element") || foreign.getName().endsWith("meta_data") || foreign
                                .getName().endsWith("history"))) {
                            //FOREIGN has the fk column
                            sb.append("\n        DECLARE ");
                            sb.append("\n        parent_row RECORD; ");
                            sb.append("\n        BEGIN ");

                            if (field.isOnlyRoot())
                            {
                                sb.append("\n        IF(" + isRoot + ") THEN ");
                            }
                            
                            sb
                                    .append("\n        FOR parent_row IN SELECT * FROM "
                                            + foreign.getSQLName() + " WHERE ");

                            Iterator refsCols = supRef.getKeyRelations()
                                    .iterator();
                            int count = 0;
                            while (refsCols.hasNext()) {
                                XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
                                        .next();
                                if (count++ > 0)
                                    sb.append(" AND ");
                                sb.append(spec.getLocalCol()).append(
                                        "=current_row.").append(
                                        spec.getForeignCol());
                            }
                            Object[][] foreignKeyArray = foreign.getSQLKeys();
                            sb.append("\n        LOOP");
                            sb.append("\n          PERFORM  " + ACT_FUNCTION)
                                    .append(foreign.getFormattedName()).append("(");
                            foreignKeyArray = foreign.getSQLKeys();
                            for (int i = 0; i < foreignKeyArray.length; i++) {
                                if (i > 0)
                                    sb.append(", ");
                                sb.append(" parent_row.").append(
                                        foreignKeyArray[i][0]);
                            }
                            sb.append(", " + user + "," + status + ",false);");
                            sb.append("\n        END LOOP;");

                            if (field.isOnlyRoot())
                            {
                                sb.append("\n        END IF;");
                            }
                            sb.append("\n        END;");
                        }
                    }
                }

            } else {
                
            }
        }
        
        sb.append("\n      END LOOP;");
        if(input.isExtension())
        {
                try {
                    GenericWrapperField field = input.getExtensionField();
                    sb.append("\n      IF (row_ct=0) THEN ");
                    GenericWrapperElement foreign = (GenericWrapperElement)field.getReferenceElement();

                    if (!(input.getName().endsWith("meta_data") || input
                            .getName().endsWith("history"))) {
                            sb.append("\n          PERFORM  " + ACT_EXT_FUNCTION)
                            .append(foreign.getFormattedName()).append("(");

                        for (int i = 0; i < keyArray.length; i++) {
                            if (i > 0)
                                sb.append(",");
                            sb.append(" ").append(keyArray[i][2]);
                        }
                        sb.append(", " + user + "," + status + "," + isRoot + ");");
                       
                    sb.append("\n          ");
                    sb.append("\n      END IF;");
                    }
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                } catch (FieldNotFoundException e) {
                    logger.error("",e);
                } catch (XFTInitException e) {
                    logger.error("",e);
                }
                
        }
        sb.append("\n	");
        }
        sb.append("\n	 RETURN '''';");
        sb.append("\n    end;");
        sb.append("\n'");
        sb.append("\n  LANGUAGE 'plpgsql' VOLATILE;");

        all.add(sb.toString());
        sb = new StringBuffer();
        if (input.isExtended()) {
	        functionName = ACT_FUNCTION + input.getFormattedName() + "(";
	        for (int i = 0; i < keyArray.length; i++) {
	            if (i > 0)
	                functionName += ",";
	            functionName += " " + keyArray[i][1];
	        }
	        functionName += ", int4,varchar(255),bool)";
	        
	        sb.append("\n\n\nCREATE OR REPLACE FUNCTION " + functionName);
	
	        sb.append("\n  RETURNS TEXT AS");
	        sb.append("\n'");
	        sb.append("\n    declare");
	        sb.append("\n     current_row RECORD;");
	        sb.append("\n    begin");
	
	        //sb.append("\n RAISE NOTICE ''" +functionName + "'';");
	        if ((!(input.getName().endsWith("meta_data") || input
	                .getName().endsWith("history")))) {
	        
	            //Build Query to get Extension id
	                try {
	                    QueryOrganizer qo = new QueryOrganizer(input, null,
	                            ViewManager.ALL);
	                    qo.addField(input.getFullXMLName()
	                            + "/extension_item/element_name");
	                    CriteriaCollection cc = new CriteriaCollection("AND");
	                    for (int i = 0; i < keyArray.length; i++) {
	                        GenericWrapperField gwf = (GenericWrapperField) keyArray[i][3];
	                        qo.addField(gwf
	                                .getXMLPathString(input.getFullXMLName()));
	
	                        SearchCriteria sc = new SearchCriteria();
	                        sc.setFieldWXMLPath(gwf.getXMLPathString(input
	                                .getFullXMLName()));
	                        sc.setOverrideFormatting(true);
	                        sc.setValue(keyArray[i][2]);
	                        cc.add(sc);
	                    }
	
	                    String query = "SELECT * FROM (" + qo.buildQuery()
	                            + ") SEARCH";
	                    query += " WHERE " + cc.getSQLClause(qo);
	
	                    String colname = qo.translateXMLPath(input.getFullXMLName()
	                            + "/extension_item/element_name");
	
	                    sb.append("\n      FOR current_row IN ").append(query);
	                    sb.append("\n      LOOP");
	                    sb.append("\n         IF (current_row.").append(colname)
	                            .append(" IS NULL) THEN ");
	                    sb.append("\n            PERFORM  ");
	                    if(input.isExtended()){
	                    	sb.append(ACT_EXT_FUNCTION);
	                    }else{
	                    	sb.append(ACT_FUNCTION);
	                    }
	                    sb.append(input.getFormattedName() + "(");
	                    for (int i = 0; i < keyArray.length; i++) {
	                        if (i > 0)
	                            sb.append(",");
	                        sb.append(" ").append(keyArray[i][2]);
	                    }
	                    sb.append(", ").append(user).append("," + status + "," + isRoot + ");");
	                    sb.append("\n         ELSE");
	                    sb.append("\n            --CALL EXTENDER ");
	                    sb.append("\n            declare");
	                    sb.append("\n               matches int4:=0;");
	                    sb.append("\n            begin");
	                    Iterator pEs = input.getPossibleExtenders().iterator();
	                    while (pEs.hasNext()) {
	                        SchemaElementI se = (SchemaElementI) pEs.next();
	                        sb.append("\n            IF (current_row.").append(
	                                colname).append("=''").append(
	                                se.getFullXMLName()).append("'') THEN");
	                        //sb.append("\n RAISE NOTICE ''PASSING CALL FROM " +
	                        // input.getSQLName() + " TO " + se.getSQLName() +
	                        // "'';");
		                    sb.append("\n            PERFORM  ");
		                    if(se.getGenericXFTElement().isExtended()){
		                    	sb.append(ACT_EXT_FUNCTION);
		                    }else{
		                    	sb.append(ACT_FUNCTION);
		                    }
		                    sb.append(se.getFormattedName() + "(");
	                        for (int i = 0; i < keyArray.length; i++) {
	                            if (i > 0)
	                                sb.append(",");
	                            sb.append(" ").append(keyArray[i][2]);
	                        }
	                        sb.append(", ").append(user).append("," + status + "," + isRoot + ");");
	                        sb.append("\n                matches:=1;");
	                        sb.append("\n            END IF;");
	                    }
	                    sb.append("\n                IF (matches=0) THEN");
                sb.append("\n            PERFORM  ");
                if(input.isExtended()){
                	sb.append(ACT_EXT_FUNCTION);
                }else{
                	sb.append(ACT_FUNCTION);
                }
                sb.append(input.getFormattedName() + "(");
	                    for (int i = 0; i < keyArray.length; i++) {
	                        if (i > 0)
	                            sb.append(",");
	                        sb.append(" ").append(keyArray[i][2]);
	                    }
	                    sb.append(", ").append(user).append("," + status + "," + isRoot + ");");
	                    sb.append("\n                END IF;");
	                    sb.append("\n            end;");
	                    sb.append("\n         end IF;");
	                    sb.append("\n      END LOOP;");
	                } catch (ElementNotFoundException e) {
	                    logger.error("",e);
	                } catch (XFTInitException e) {
	                    logger.error("",e);
	                } catch (FieldNotFoundException e) {
	                    logger.error("",e);
	                } catch (Exception e) {
	                    logger.error("",e);
	                }
	        }
	        sb.append("\n	");
	        sb.append("\n	RETURN '''';");
	        sb.append("\n    end;");
	        sb.append("\n'");
	        sb.append("\n  LANGUAGE 'plpgsql' VOLATILE;");
	
	        all.add(sb.toString());
        }
        return all;
    }
    
    public static List<String>[] GetUpdateFunctions(GenericWrapperElement input)
    throws ElementNotFoundException, XFTInitException {
        StringBuffer sb = new StringBuffer();
        List<String> all =new ArrayList();
        List<String> post = new ArrayList();
        Object[][] keyArray = input.getSQLKeys();
        if (input.isExtended()) {
            sb.append("\n\n\nCREATE OR REPLACE FUNCTION update_ls_ext_"
                + input.getFormattedName() + "(");
        }else{
            sb.append("\n\n\nCREATE OR REPLACE FUNCTION update_ls_"
                    + input.getFormattedName() + "(");
        }
        for (int i = 0; i < keyArray.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(" ").append(keyArray[i][1]);
        }
        sb.append(", int4)");
        String userVariable = "$" + (keyArray.length + 1);
        sb.append("\n  RETURNS \"varchar\" AS");
        sb.append("\n'");
        sb.append("\n    declare");
        sb.append("\n     current_row RECORD;");
        sb.append("\n    begin");


        if (input.canBeRoot()){
            //System.out.println(input.getFullXMLName() + " DELETE CACHE");
            sb.append("\n\n       --DELETE CACHE");
            sb.append("\n      declare");
            sb.append("\n        current_cache RECORD;");
            sb.append("\n      BEGIN");
            sb.append("\n        FOR current_cache IN SELECT oid FROM xs_item_cache WHERE elementname=''" + input.getFullXMLName() + "'' AND ids=");
                    
            for (int i = 0; i < keyArray.length; i++) {
                if (i > 0)
                    sb.append(" || '','' || ");
                sb.append("CAST(" + keyArray[i][2] + " AS text)");
            }
            sb.append("\n        LOOP");
                sb.append("\n         DELETE FROM xs_item_cache WHERE oid=current_cache.oid;");
            sb.append("\n        END LOOP;");
            sb.append("\n      END;");
        }
        //sb.append("\n RAISE NOTICE ''UPDATING LAST MODIFICATION OF " +
        // input.getSQLName() + "(%)'', $1;");
        sb.append("\n\n       --UPDATE LOCAL ROW");
        sb.append("\n      FOR current_row IN SELECT ").append(
                input.getSQLName());
        sb.append(".*, meta.meta_data_id FROM ").append(input.getSQLName());
        sb.append(" LEFT JOIN ").append(input.getSQLName()).append(
                "_meta_data meta ON ").append(input.getSQLName());
       sb.append(".").append(input.getMetaDataFieldName()).append(
                "=meta.meta_data_id");
        sb.append(" WHERE ");
        for (int i = 0; i < keyArray.length; i++) {
            if (i > 0)
                sb.append(" AND ");
            sb.append(" ").append(keyArray[i][0]).append("=").append(
                    keyArray[i][2]);
        }
        sb.append("\n      LOOP");
        if (input.getWrapped().canBeRoot())
        {
            sb
                .append("\n         UPDATE ")
                .append(input.getSQLName())
                .append(
                        "_meta_data SET last_modified=NOW(), modified=1 WHERE meta_data_id=current_row.meta_data_id;");
        }
        sb.append("\n      END LOOP;");
        
        sb.append("\n\n      --UPDATE PARENTS;");
        ArrayList possibleParents = input.getPossibleParents(false);

        if (possibleParents.size() > 0) {
            Iterator iter = possibleParents.iterator();
            while (iter.hasNext()) {
                Object[] pp = (Object[]) iter.next();
                GenericWrapperElement foreign = (GenericWrapperElement) pp[0];
                String xmlPath = (String) pp[1];
                GenericWrapperField gwf = (GenericWrapperField) pp[2];

                if (!gwf.isOnlyRoot()){
	                String extensionType = "";
	
	                if (foreign.isExtension()) {
	                    extensionType = foreign.getExtensionType()
	                            .getFullForeignType();
	                }
	
	                if (!input.getFullXMLName().equalsIgnoreCase(extensionType)) {
	                    if (foreign.getAddin().equalsIgnoreCase("") && !("xdat:user".equals(input.getFullXMLName()) && ("xdat:change_info".equals(foreign.getFullXMLName())))) {
	                        XFTReferenceI ref = gwf.getXFTReference();
	                        if (ref.isManyToMany()) {
	                            String mappingTable = ((XFTManyToManyReference) ref)
	                                    .getMappingTable();
	                            sb
	                                    .append("\n\n        --PROCESS MAPPING TABLE RELATION "
	                                            + xmlPath);
	                            sb.append("\n        DECLARE ");
	                            sb.append("\n        mapping_row RECORD; ");
	                            sb.append("\n        BEGIN ");
	                            sb
	                                    .append("\n        FOR mapping_row IN SELECT * FROM "
	                                            + mappingTable + " WHERE ");
	                            Iterator refCols = ((XFTManyToManyReference) ref)
	                                    .getMappingColumnsForElement(input)
	                                    .iterator();
	                            int count = 0;
	                            while (refCols.hasNext()) {
	                                XFTMappingColumn spec = (XFTMappingColumn) refCols
	                                        .next();
	                                if (count++ > 0)
	                                    sb.append(" AND ");
	                                sb.append(" ").append(
	                                        spec.getLocalSqlName()).append("=");
	                                sb.append("current_row.").append(
	                                        spec.getForeignKey().getSQLName());
	                            }
	                            sb.append("\n           LOOP ");
	                            sb
	                                    .append(
	                                            "\n               PERFORM update_ls_")
	                                    .append(foreign.getFormattedName()).append(
	                                            "(");
	                            refCols = ((XFTManyToManyReference) ref)
	                                    .getMappingColumnsForElement(foreign)
	                                    .iterator();
	                            count = 0;
	                            while (refCols.hasNext()) {
	                                XFTMappingColumn spec = (XFTMappingColumn) refCols
	                                        .next();
	                                if (count++ > 0)
	                                    sb.append(",");
	                                sb.append(" mapping_row.").append(
	                                        spec.getLocalSqlName());
	                            }
	                            sb.append(", ").append(userVariable).append(
	                                    ");");
	                            sb.append("\n           END LOOP; ");
	                            sb.append("\n        END; ");
	
	                        } else {
	                            XFTSuperiorReference supRef = (XFTSuperiorReference) ref;
	
	                            if (supRef.getSubordinateElement()
	                                    .equals(input)) {
	                                //INPUT has the fk column (check if it is
	                                // null)
	
	                                sb
	                                        .append("\n\n        --PROCESS SUBORDINATE RELATION "
	                                                + xmlPath);
	                                sb.append("\n        IF (");
	                                Iterator refsCols = supRef
	                                        .getKeyRelations().iterator();
	                                int count = 0;
	                                while (refsCols.hasNext()) {
	                                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
	                                            .next();
	                                    if (count++ > 0)
	                                        sb.append(" && ");
	                                    sb.append("(current_row.").append(
	                                            spec.getLocalCol()).append(
	                                            " IS NOT NULL)");
	                                }
	                                sb.append(") THEN");
	                                sb.append("\n        PERFORM update_ls_")
	                                        .append(foreign.getFormattedName())
	                                        .append("(");
	                                refsCols = supRef.getKeyRelations()
	                                        .iterator();
	                                count = 0;
	                                while (refsCols.hasNext()) {
	                                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
	                                            .next();
	                                    if (count++ > 0)
	                                        sb.append(", ");
	                                    sb.append("current_row.").append(
	                                            spec.getLocalCol());
	                                }
	                                sb.append(", ").append(userVariable)
	                                        .append(");");
	                                sb.append("\n        END IF;");
	                            } else {
	                                //FOREIGN has the fk column
	                            	if (!("xdat:user".equals(input.getFullXMLName()) && ("xdat:user_login".equals(foreign.getFullXMLName())))) {
		                                sb
		                                        .append("\n\n        --PROCESS SUPERIOR RELATION "
		                                                + xmlPath);
		                                sb.append("\n        DECLARE ");
		                                sb.append("\n        parent_row RECORD; ");
		                                sb.append("\n        BEGIN ");
		                                sb
		                                        .append("\n        FOR parent_row IN SELECT * FROM "
		                                                + foreign.getSQLName()
		                                                + " WHERE ");
		
		                                Iterator refsCols = supRef
		                                        .getKeyRelations().iterator();
		                                int count = 0;
		                                while (refsCols.hasNext()) {
		                                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
		                                            .next();
		                                    if (count++ > 0)
		                                        sb.append(" AND ");
		                                    sb.append(spec.getLocalCol()).append(
		                                            "=current_row.").append(
		                                            spec.getForeignCol());
		                                }
		
		                                sb.append("\n        LOOP");
		                                sb.append("\n        PERFORM update_ls_")
		                                        .append(foreign.getFormattedName())
		                                        .append("(");
		                                Object[][] foreignKeyArray = foreign
		                                        .getSQLKeys();
		                                for (int i = 0; i < foreignKeyArray.length; i++) {
		                                    if (i > 0)
		                                        sb.append(", ");
		                                    sb.append(" parent_row.").append(
		                                            foreignKeyArray[i][0]);
		                                }
		                                sb.append(", ").append(userVariable)
		                                        .append(");");
		                                sb.append("\n        END LOOP;");
		                                sb.append("\n        END;");
		                            	}
	                            }
	                        }
	                    }
	                }
                }
            }
        }

        if (input.isExtension()) {
            try {
                GenericWrapperElement ext = (GenericWrapperElement) input
                        .getExtensionField().getReferenceElement();
                sb.append("\n\n        -- PROCESS EXTENSION");
                sb.append("\n        PERFORM update_ls_ext_").append(
                        ext.getFormattedName()).append("(");
                for (int i = 0; i < keyArray.length; i++) {
                    if (i > 0)
                        sb.append(",");
                    sb.append(" ").append(keyArray[i][2]);
                }

                sb.append(", ").append(userVariable).append(");");
            } catch (XFTInitException e) {
                logger.error("", e);
            } catch (ElementNotFoundException e) {
                logger.error("", e);
            } catch (FieldNotFoundException e) {
                logger.error("", e);
            }
        }

        sb.append("\n	");
        sb.append("\n	RETURN ''DONE'';");
        sb.append("\n    end;");
        sb.append("\n'");
        sb.append("\n  LANGUAGE 'plpgsql' VOLATILE;");
        
        all.add(sb.toString());
        if (input.isExtended()) {
	        sb = new StringBuffer();
	
	        //UPDATE SHOULD ALWAYS START WITH HIGHEST EXTENSION AND WORK DONE.
	        sb.append("\n\n\nCREATE OR REPLACE FUNCTION update_ls_"
	                + input.getFormattedName() + "(");
	        for (int i = 0; i < keyArray.length; i++) {
	            if (i > 0)
	                sb.append(",");
	            sb.append(" ").append(keyArray[i][1]);
	        }
	        sb.append(", int4)");
	        sb.append("\n  RETURNS \"varchar\" AS");
	        sb.append("\n'");
	        sb.append("\n    declare");
	        sb.append("\n     current_row RECORD;");
	        sb.append("\n    begin");
	
            //Build Query to get Extension id
            try {
                QueryOrganizer qo = new QueryOrganizer(input, null,
                        ViewManager.ALL);
                qo.addField(input.getFullXMLName()
                        + "/extension_item/element_name");
                CriteriaCollection cc = new CriteriaCollection("AND");
                for (int i = 0; i < keyArray.length; i++) {
                    GenericWrapperField gwf = (GenericWrapperField) keyArray[i][3];
                    qo.addField(gwf
                            .getXMLPathString(input.getFullXMLName()));

                    SearchCriteria sc = new SearchCriteria();
                    sc.setFieldWXMLPath(gwf.getXMLPathString(input
                            .getFullXMLName()));
                    sc.setOverrideFormatting(true);
                    sc.setValue(keyArray[i][2]);
                    cc.add(sc);
                }

                String query = "SELECT * FROM (" + qo.buildQuery()
                        + ") SEARCH";
                query += " WHERE " + cc.getSQLClause(qo);

                String colname = qo.translateXMLPath(input.getFullXMLName()
                        + "/extension_item/element_name");

                sb.append("\n      FOR current_row IN ").append(query);
                sb.append("\n      LOOP");
                sb.append("\n         IF (current_row.").append(colname)
                        .append(" IS NULL) THEN ");
                sb.append("\n             PERFORM update_ls_ext_"
                        + input.getFormattedName() + "(");
                for (int i = 0; i < keyArray.length; i++) {
                    if (i > 0)
                        sb.append(",");
                    sb.append(" ").append(keyArray[i][2]);
                }
                sb.append(", ").append(userVariable).append(");");
                sb.append("\n         ELSE");
                sb.append("\n            --CALL EXTENDER ");
                sb.append("\n            declare");
                sb.append("\n               matches int4:=0;");
                sb.append("\n            begin");
                Iterator pEs = input.getPossibleExtenders().iterator();
                while (pEs.hasNext()) {
                    SchemaElementI se = (SchemaElementI) pEs.next();
                    sb.append("\n            IF (current_row.").append(
                            colname).append("=''").append(
                            se.getFullXMLName()).append("'') THEN");
                    //sb.append("\n RAISE NOTICE ''PASSING CALL FROM " +
                    // input.getSQLName() + " TO " + se.getSQLName() +
                    // "'';");
                    if (se.getGenericXFTElement().isExtended())
                    {
                        sb.append("\n                PERFORM update_ls_ext_"
                                + se.getFormattedName() + "(");
                    }else{
                        sb.append("\n                PERFORM update_ls_"
                                + se.getFormattedName() + "(");
                    }
                    for (int i = 0; i < keyArray.length; i++) {
                        if (i > 0)
                            sb.append(",");
                        sb.append(" ").append(keyArray[i][2]);
                    }
                    sb.append(", ").append(userVariable).append(");");
                    sb.append("\n                matches:=1;");
                    sb.append("\n            END IF;");
                }
                sb.append("\n                IF (matches=0) THEN");
                sb
                        .append("\n       RAISE NOTICE ''ERROR: NO MATCHING EXTENSION FOUND FOR (%)'', current_row."
                                + colname + ";");
                sb.append("\n                    PERFORM update_ls_ext_"
                        + input.getFormattedName() + "(");
                for (int i = 0; i < keyArray.length; i++) {
                    if (i > 0)
                        sb.append(",");
                    sb.append(" ").append(keyArray[i][2]);
                }
                sb.append(", ").append(userVariable).append(");");
                sb.append("\n                END IF;");
                sb.append("\n            end;");
                sb.append("\n         end IF;");
                sb.append("\n      END LOOP;");
            } catch (Exception e) {
                logger.error("", e);
                sb.append("\n      PERFORM update_ls_ext_"
                        + input.getFormattedName() + "(");
                for (int i = 0; i < keyArray.length; i++) {
                    if (i > 0)
                        sb.append(",");
                    sb.append(" ").append(keyArray[i][2]);
                }
                sb.append(", ").append(userVariable).append(");");
            }
	
	        sb.append("\n	");
	        sb.append("\n	RETURN ''DONE'';");
	        sb.append("\n    end;");
	        sb.append("\n'");
	        sb.append("\n  LANGUAGE 'plpgsql' VOLATILE;");
	
	        all.add(sb.toString());
        }
        
            sb = new StringBuffer();
            //		CREATE TRIGGER REFERENCE FUNCTION.
//            sb.append("\n\nSELECT " + GenericWrapperUtils.CREATE_FUNCTION + "('after_update_"
//                    + input.getSQLName() + "','");
            sb.append("CREATE OR REPLACE FUNCTION after_update_"
                    + input.getFormattedName() + "()");
            sb.append("\n  RETURNS TRIGGER AS");
            sb.append("\n'");
            sb.append("\n    begin");
            try {
                if (input.getSchema().getTargetNamespacePrefix().equals("xdat"))
                {
                    sb.append("\n--       PERFORM update_ls_" + input.getFormattedName() + "(");
                    for (int i = 0; i < keyArray.length; i++) {
                        if (i > 0)
                            sb.append(",");
                        sb.append(" OLD.").append(keyArray[i][0]);
                    }
                    sb.append(", NULL);");
                }else if(SecurityManager.GetInstance().isSecurityElement(input.getXSIType())){
                    sb.append("\n--       PERFORM update_ls_" + input.getFormattedName() + "(");
                    for (int i = 0; i < keyArray.length; i++) {
                        if (i > 0)
                            sb.append(",");
                        sb.append(" OLD.").append(keyArray[i][0]);
                    }
                    sb.append(", NULL);");
                }else if(input.getXSIType().equals("xnat:investigatorData")){
                    sb.append("\n--       PERFORM update_ls_" + input.getFormattedName() + "(");
                    for (int i = 0; i < keyArray.length; i++) {
                        if (i > 0)
                            sb.append(",");
                        sb.append(" OLD.").append(keyArray[i][0]);
                    }
                    sb.append(", NULL);");
                }else{
                    sb.append("\n--       PERFORM update_ls_" + input.getFormattedName() + "(");
                    for (int i = 0; i < keyArray.length; i++) {
                        if (i > 0)
                            sb.append(",");
                        sb.append(" OLD.").append(keyArray[i][0]);
                    }
                    sb.append(", NULL);");
                }
            } catch (Exception e) {
                logger.error("",e);
            }
            
            sb.append("\n	");
            sb.append("\n	    RETURN NULL;");
            sb.append("\n    end;");
            sb.append("\n'");
            sb.append("\n  LANGUAGE 'plpgsql' VOLATILE;");

            all.add(sb.toString());
            sb = new StringBuffer();
            sb.append("\n\nSELECT " + GenericWrapperUtils.CREATE_TRIGGER + "('a_u_" + input.getFormattedName()
                    + "','");
            sb.append("CREATE TRIGGER a_u_" + input.getFormattedName());
            sb.append("  AFTER UPDATE OR DELETE ON ")
                    .append(input.getSQLName());
            sb.append(" FOR EACH ROW EXECUTE PROCEDURE after_update_"
                    + input.getFormattedName() + "()');");

            post.add(sb.toString());
            List[] _return={all,post};
        return _return;
    }

    public static List<String>[] GetFunctionStatements(GenericWrapperElement input) throws ElementNotFoundException, XFTInitException,Exception {
        List<String> sb= new ArrayList<String>();
        List<String> post= new ArrayList<String>();
        
        /*******************************
         * TEXT OUTPUT FUNCTIONS
         *******************************/
        sb.addAll(TextFunctionGenerator.GetTextOutputFunctions(input));
        
        //ONLY DATA TABLES
        if (!(input.getName().endsWith("meta_data") || input.getName()
                .endsWith("history"))) {
            /*******************************
             * REMOVE WITH HISTORY
             *******************************/
            
            
            /*******************************
             * CLEAN REMOVAL
             *******************************/
            
            
            /*******************************
             * ACTIVATE
             *******************************/	
            sb.addAll(GetActivateFunctions(input));
            
            /*******************************
             * UPDATE
             *******************************/
            List[] updates=GetUpdateFunctions(input);
            sb.addAll(updates[0]);
            post.addAll(updates[1]);
        }
        List[] _return={sb,post};
        return _return;
    }

    /**
     * Generates ALTER TABLE SQL statements for this element.
     * 
     * @param input
     * @return Returns a list of ALTER TABLE SQL statement Strings
     * @throws ElementNotFoundException
     * @throws XFTInitException
     */
    public static List GetAlterTableStatements(GenericWrapperElement input)
            throws ElementNotFoundException, XFTInitException {
        Hashtable indexs = new Hashtable();
        ArrayList al = new ArrayList();
        try {
        	
            Iterator refs = input.getReferenceFieldsWAddIns().iterator();
            while (refs.hasNext()) {
                GenericWrapperField localCol = (GenericWrapperField) refs
                        .next();
                if ((localCol.isMultiple()
                        && localCol.getRelationType()
                                .equalsIgnoreCase("single") && localCol
                        .getXMLType().getFullForeignType().equalsIgnoreCase(
                                input.getFullXMLName()))
                        || (!localCol.isMultiple())) {
                    try {
                        XFTReferenceI ref = localCol.getXFTReference();
                        if (!ref.isManyToMany()) {
                            ArrayList specArray = ((XFTSuperiorReference) ref)
                                    .getKeyRelations();
                            if (specArray.size() > 1) {
                                String localCols = "";
                                String localTable = "";
                                String foreignCols = "";
                                String foreignTable = "";
                                String onDelete = "";
                                int counter2 = 0;
                                Iterator specs = specArray.iterator();
                                while (specs.hasNext()) {
                                    XFTRelationSpecification spec = (XFTRelationSpecification) specs
                                            .next();

                                    StringBuffer sb = new StringBuffer();
                                    if (input.getSchema().getDbType()
                                            .equalsIgnoreCase("MYSQL")) {
                                        sb.append("ALTER TABLE ").append(
                                                spec.getLocalTable());
                                        sb.append("\n ADD KEY fk_");
                                        sb.append(spec.getLocalCol()).append(
                                                " (")
                                                .append(spec.getLocalCol())
                                                .append(");");
                                        al.add(sb.toString());
                                    }

                                    if ((counter2++) == 0) {
                                        localCols += spec.getLocalCol();
                                        foreignCols += spec.getForeignCol();
                                    } else {
                                        localCols += "," + spec.getLocalCol();
                                        foreignCols += ","
                                                + spec.getForeignCol();
                                    }

                                    if (spec.getLocalKey() != null) {
                                        if (spec.getLocalKey().getOnDelete()
                                                .equalsIgnoreCase("cascade") || spec.getLocalKey().isRequired()) {
                                            onDelete = " CASCADE";
                                        } else {
                                            onDelete = " SET NULL";
                                        }
                                    } else {
                                        if (localCol.getOnDelete()
                                                .equalsIgnoreCase("cascade") || localCol.isRequired()) {
                                            onDelete = " CASCADE";
                                        } else {
                                            onDelete = " SET NULL";
                                        }
                                    }

                                    localTable = spec.getLocalTable();
                                    foreignTable = spec.getForeignTable();

                                    if (!input.getAddin().equalsIgnoreCase(
                                            "history")) {
                                        sb = new StringBuffer();
                                        String temp = spec.getLocalTable()
                                                .toLowerCase()
                                                + "_"
                                                + spec.getLocalCol()
                                                        .toLowerCase();
                                        boolean maxLengthExceeded = false;
                                        if (temp.length() > 57) {
                                            temp = temp.substring(0, 57);
                                            maxLengthExceeded = true;
                                        }

                                        int counter = 0;
                                        while (indexs.containsKey(temp
                                                + ++counter)) {
                                        }
                                        sb.append("CREATE INDEX ").append(
                                                temp + counter);
                                        sb.append(" ON ").append(
                                                spec.getLocalTable()).append(
                                                "(");
                                        sb.append(spec.getLocalCol()).append(
                                                ");");
                                        indexs.put(temp + counter, sb
                                                .toString());

                                        if (maxLengthExceeded) {
                                            temp = temp.substring(0, 57);
                                        }
                                        temp += counter;

                                        sb = new StringBuffer();
                                        sb.append("CREATE INDEX ").append(
                                                temp + "_hash");
                                        sb.append(" ON ").append(
                                                spec.getLocalTable()).append(
                                                " USING HASH (");
                                        sb.append(spec.getLocalCol()).append(
                                                ");");
                                        indexs.put(temp + "_hash", sb
                                                .toString());
                                    }
                                }

                                StringBuffer sb = new StringBuffer();
                                sb.append("ALTER TABLE ").append(localTable);
                                sb.append("\n ADD FOREIGN KEY (");
                                sb.append(localCols).append(") REFERENCES ");
                                sb.append(foreignTable).append("(");
                                sb.append(foreignCols).append(") ON DELETE ");
                                sb.append(onDelete);
                                sb.append(" ON UPDATE CASCADE;");
                                al.add(sb.toString());
                            } else {
                                XFTRelationSpecification spec = (XFTRelationSpecification) specArray
                                        .get(0);

                                if (!input.getAddin().equalsIgnoreCase(
                                        "history")) {
                                    StringBuffer sb = new StringBuffer();
                                    if (input.getSchema().getDbType()
                                            .equalsIgnoreCase("MYSQL")) {
                                        sb.append("ALTER TABLE ").append(
                                                spec.getLocalTable());
                                        sb.append("\n ADD KEY fk_");
                                        sb.append(spec.getLocalCol()).append(
                                                " (")
                                                .append(spec.getLocalCol())
                                                .append(");");
                                        al.add(sb.toString());
                                    }

                                    sb = new StringBuffer();
                                    sb.append("ALTER TABLE ").append(
                                            spec.getLocalTable());
                                    sb.append("\n ADD FOREIGN KEY (");
                                    sb.append(spec.getLocalCol()).append(
                                            ") REFERENCES ");
                                    sb.append(spec.getForeignTable()).append(
                                            "(");
                                    sb.append(spec.getForeignCol()).append(
                                            ") ON DELETE ");
                                    if (spec.getLocalKey() != null) {
                                        if (spec.getLocalKey().getOnDelete()
                                                .equalsIgnoreCase("cascade") || spec.getLocalKey().isRequired()) {
                                            sb.append(" CASCADE");
                                        } else {
                                            sb.append(" SET NULL");
                                        }
                                    } else {
                                        if (localCol.getOnDelete()
                                                .equalsIgnoreCase("cascade")|| localCol.isRequired()) {
                                            sb.append(" CASCADE");
                                        } else {
                                            sb.append(" SET NULL");
                                        }
                                    }
                                    sb.append(" ON UPDATE CASCADE;");
                                    al.add(sb.toString());

                                    sb = new StringBuffer();
                                    String temp = spec.getLocalTable()
                                            .toLowerCase()
                                            + "_"
                                            + spec.getLocalCol().toLowerCase();
                                    boolean maxLengthExceeded = false;
                                    if (temp.length() > 57) {
                                        temp = temp.substring(0, 57);
                                        maxLengthExceeded = true;
                                    }

                                    int counter = 0;
                                    while (indexs.containsKey(temp + ++counter)) {
                                    }
                                    sb.append("CREATE INDEX ").append(
                                            temp + counter);
                                    sb.append(" ON ").append(
                                            spec.getLocalTable()).append("(");
                                    sb.append(spec.getLocalCol()).append(");");
                                    indexs.put(temp + counter, sb.toString());

                                    if (maxLengthExceeded) {
                                        temp = temp.substring(0, 57);
                                    }
                                    temp += counter;

                                    sb = new StringBuffer();
                                    sb.append("CREATE INDEX ").append(
                                            temp + "_hash");
                                    sb.append(" ON ").append(
                                            spec.getLocalTable()).append(
                                            " USING HASH (");
                                    sb.append(spec.getLocalCol()).append(");");
                                    indexs.put(temp + "_hash", sb.toString());
                                }
                            }

                        }
                    } catch (org.nrg.xft.exception.XFTInitException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            Iterator relations = input.getRelationFields().iterator();
            while (relations.hasNext()) {
                GenericWrapperField relation = (GenericWrapperField) relations
                        .next();

                if (relation.getForeignCol() != null
                        && !relation.getForeignCol().equalsIgnoreCase("")) {
                    StringBuffer sb = new StringBuffer();
                    if (input.getSchema().getDbType().equalsIgnoreCase("MYSQL")) {
                        sb.append("ALTER TABLE ").append(input.getSQLName());
                        sb.append("\n ADD KEY fk_");
                        sb.append(relation.getSQLName()).append(" (").append(
                                relation.getSQLName()).append(");");
                        al.add(sb.toString());
                    }

                    sb = new StringBuffer();
                    sb.append("ALTER TABLE ").append(input.getSQLName());
                    sb.append("\n ADD FOREIGN KEY (");
                    sb.append(relation.getSQLName()).append(") REFERENCES ");
                    sb.append(relation.getForeignKeyTable()).append("(");
                    sb.append(relation.getForeignCol()).append(") ON DELETE ");
                    if (relation.getOnDelete().equalsIgnoreCase("cascade")) {
                        sb.append(" CASCADE");
                    } else {
                        sb.append(" SET NULL");
                    }
                    sb.append(" ON UPDATE CASCADE;");

                    al.add(sb.toString());

                    sb = new StringBuffer();
                    String temp = input.getSQLName().toLowerCase() + "_"
                            + relation.getSQLName().toLowerCase();
                    boolean maxLengthExceeded = false;
                    if (temp.length() > 57) {
                        temp = temp.substring(0, 57);
                        maxLengthExceeded = true;
                    }

                    if (!input.getAddin().equalsIgnoreCase("history")) {
                        int counter = 0;
                        while (indexs.containsKey(temp + ++counter)) {
                        }
                        sb.append("CREATE INDEX ").append(temp + counter);
                        sb.append(" ON ").append(input.getSQLName())
                                .append("(");
                        sb.append(relation.getSQLName()).append(");");
                        indexs.put(temp + counter, sb.toString());

                        if (maxLengthExceeded) {
                            temp = temp.substring(0, 57);
                        }
                        temp += counter;

                        sb = new StringBuffer();
                        sb.append("CREATE INDEX ").append(temp + "_hash");
                        sb.append(" ON ").append(input.getSQLName()).append(
                                " USING HASH (");
                        sb.append(relation.getSQLName()).append(");");
                        indexs.put(temp, sb.toString());
                    }
                }
            }

            Iterator keys = input.getAllPrimaryKeys().iterator();
            while (keys.hasNext()) {
                GenericWrapperField key = (GenericWrapperField) keys.next();
                if (key.isReference()) {

                } else {
                    if (!input.getAddin().equalsIgnoreCase("history")) {
                        StringBuffer sb = new StringBuffer();
                        String temp = input.getSQLName().toLowerCase() + "_"
                                + key.getSQLName().toLowerCase();
                        boolean maxLengthExceeded = false;
                        if (temp.length() > 57) {
                            temp = temp.substring(0, 57);
                            maxLengthExceeded = true;
                        }

                        int counter = 0;
                        while (indexs.containsKey(temp + ++counter)) {
                        }
                        sb.append("CREATE INDEX ").append(temp + counter);
                        sb.append(" ON ").append(input.getSQLName())
                                .append("(");
                        sb.append(key.getSQLName()).append(");");
                        indexs.put(temp + counter, sb.toString());

                        if (maxLengthExceeded) {
                            temp = temp.substring(0, 57);
                        }
                        temp += counter;

                        sb = new StringBuffer();
                        sb.append("CREATE INDEX ").append(temp + "_hash");
                        sb.append(" ON ").append(input.getSQLName()).append(
                                " USING HASH (");
                        sb.append(key.getSQLName()).append(");");
                        indexs.put(temp, sb.toString());
                    }
                }
            }
            
            Hashtable<String,ArrayList<ArrayList<String>>> fTables = new Hashtable<String,ArrayList<ArrayList<String>>>();
            
            

            Iterator allFields = input.getAllFields().iterator();
            while (allFields.hasNext()) {
                GenericWrapperField gwf = (GenericWrapperField) allFields
                        .next();
                if (gwf.getBaseCol() != null && !gwf.getBaseCol().equals("")) {
                    if (gwf.getBaseElement() != null
                            && !gwf.getBaseElement().equals("")) {
                        try {
                            GenericWrapperElement foriegn = GenericWrapperElement
                                    .GetElement(gwf.getBaseElement());

                            ArrayList<String> row =new ArrayList<String>();
                            String foreignSQLName = foriegn.getSQLName();
                            row.add(gwf.getSQLName());
                            row.add(gwf.getBaseCol());
                            if (gwf.getOnDelete().equalsIgnoreCase("cascade") || gwf.isRequired()) {
                            	row.add(" CASCADE");
                            } else {
                            	row.add(" SET NULL");
                            }

                            if (fTables.containsKey(foreignSQLName))
                            {
                            	fTables.get(foreignSQLName).add(row);
                            }else{
                                fTables.put(foreignSQLName, new ArrayList<ArrayList<String>>());
                            	fTables.get(foreignSQLName).add(row);
                            }
                        } catch (ElementNotFoundException e1) {
                            logger.error("", e1);
                        }

                    }
                }
            }
            

            for (String key :fTables.keySet()){
            	ArrayList<ArrayList<String>> row = fTables.get(key);
            	
            	StringBuffer sb = new StringBuffer();
                sb.append("ALTER TABLE ")
                        .append(input.getSQLName());
                sb.append("\n ADD FOREIGN KEY (");
                for (int i=0;i<row.size();i++)
                {
                	if (i>0){
                		sb.append(", ");
                	}
                	sb.append(row.get(i).get(0));
                }
                sb.append(") REFERENCES ");
                sb.append(key).append("(");
                for (int i=0;i<row.size();i++)
                {
                	if (i>0){
                		sb.append(", ");
                	}
                	sb.append(row.get(i).get(1));
                }
                sb.append(") ON DELETE ").append(row.get(0).get(2));
                sb.append(" ON UPDATE CASCADE;");
                al.add(sb.toString());
            }

        } catch (org.nrg.xft.exception.XFTInitException e) {
            logger.error(e);
        }

        al.addAll(indexs.values());
        return al;
    }

    /**
     * Generates ALTER TABLE SQL statements for this element.
     * 
     * @param input
     * @return Returns a list of ALTER TABLE SQL statement Strings
     * @throws XFTInitException
     */
    public static List GetAlterTableStatements(XFTManyToManyReference input)
            throws XFTInitException {
        ArrayList al = new ArrayList();

        Iterator mappingCols = input.getMappingColumns().iterator();
        while (mappingCols.hasNext()) {
            XFTMappingColumn col = (XFTMappingColumn) mappingCols.next();

            StringBuffer sb = new StringBuffer();
            if (input.getElement1().getSchema().getDbType().equalsIgnoreCase(
                    "MYSQL")) {
                sb.append("ALTER TABLE ").append(input.getMappingTable());
                sb.append("\n ADD KEY fk_");
                sb.append(col.getLocalSqlName()).append(" (").append(
                        col.getLocalSqlName()).append(");");
                al.add(sb.toString());
            }

            sb = new StringBuffer();
            sb.append("ALTER TABLE ").append(input.getMappingTable());
            sb.append("\n ADD FOREIGN KEY (");
            sb.append(col.getLocalSqlName()).append(") REFERENCES ");
            sb.append(col.getForeignElement().getSQLName()).append("(");
            sb.append(col.getForeignKey().getSQLName()).append(") ON DELETE ");
            sb.append(" CASCADE ");
            sb.append(" ON UPDATE CASCADE;");

            al.add(sb.toString());
        }

        return al;
    }

    public static Collection<String> GetExtensionTables() {
        List<String> statements = new ArrayList<String>();

        StringBuilder functions = new StringBuilder();
        functions.append("\n\n\nCREATE OR REPLACE FUNCTION class_exists( VARCHAR)");
        functions.append("\nRETURNS BOOLEAN AS");
        functions.append("\n'");
        functions.append("\n    declare");
        functions.append("\n     current_row RECORD;");
        functions.append("\n    begin");
        functions.append("\n      FOR current_row IN SELECT * FROM pg_catalog.pg_class WHERE  relname=LOWER($1)");
        functions.append("\n     LOOP");
        functions.append("\n          RETURN TRUE;");
        functions.append("\n      END LOOP;");
        functions.append("\n");
        functions.append("\n	 RETURN FALSE;");
        functions.append("\n    end;");
        functions.append("\n'");
        functions.append("\n  LANGUAGE 'plpgsql' VOLATILE;");
        statements.add(functions.toString());
        functions = new StringBuilder();
        functions.append("\n\n\nCREATE OR REPLACE FUNCTION schema_exists( VARCHAR)");
        functions.append("\nRETURNS BOOLEAN AS");
        functions.append("\n'");
        functions.append("\n    declare");
        functions.append("\n     current_row RECORD;");
        functions.append("\n    begin");
        functions.append("\n      FOR current_row IN SELECT * FROM pg_namespace WHERE nspname=LOWER($1)");
        functions.append("\n     LOOP");
        functions.append("\n          RETURN TRUE;");
        functions.append("\n      END LOOP;");
        functions.append("\n");
        functions.append("\n	 RETURN FALSE;");
        functions.append("\n    end;");
        functions.append("\n'");
        functions.append("\n  LANGUAGE 'plpgsql' VOLATILE;");
        statements.add(functions.toString());
        functions = new StringBuilder();
        functions.append("\n\nCREATE OR REPLACE FUNCTION function_exists( VARCHAR)");
        functions.append("\nRETURNS BOOLEAN AS");
        functions.append("\n'");
        functions.append("\n    declare");
        functions.append("\n     current_row RECORD;");
        functions.append("\n    begin");
        functions.append("\n      FOR current_row IN SELECT * FROM pg_proc WHERE  proname=LOWER($1)");
        functions.append("\n     LOOP");
        functions.append("\n          RETURN TRUE;");
        functions.append("\n      END LOOP;");
        functions.append("\n");
        functions.append("\n	 RETURN FALSE;");
        functions.append("\n    end;");
        functions.append("\n'");
        functions.append("\n  LANGUAGE 'plpgsql' VOLATILE;");
        statements.add(functions.toString());
        functions = new StringBuilder();
        functions.append("\n\nCREATE OR REPLACE FUNCTION trigger_exists( VARCHAR)");
        functions.append("\nRETURNS BOOLEAN AS");
        functions.append("\n'");
        functions.append("\n    declare");
        functions.append("\n     current_row RECORD;");
        functions.append("\n    begin");
        functions.append("\n      FOR current_row IN SELECT * FROM pg_trigger WHERE  tgname=LOWER($1)");
        functions.append("\n     LOOP");
        functions.append("\n          RETURN TRUE;");
        functions.append("\n      END LOOP;");
        functions.append("\n");
        functions.append("\n	 RETURN FALSE;");
        functions.append("\n    end;");
        functions.append("\n'");
        functions.append("\n  LANGUAGE 'plpgsql' VOLATILE;");
        statements.add(functions.toString());
        functions = new StringBuilder();
        functions.append("\n\nCREATE OR REPLACE FUNCTION " + GenericWrapperUtils.CREATE_TRIGGER + "(VARCHAR,VARCHAR)");
        functions.append("\nRETURNS void AS");
        functions.append("\n'");
        functions.append("\n    begin");
        functions.append("\n	IF(NOT TRIGGER_EXISTS($1)) THEN");
        functions.append("\n		EXECUTE $2;");
        functions.append("\n		RETURN;");
        functions.append("\n	END IF;");
        functions.append("\n	RETURN;");
        functions.append("\n    end;");
        functions.append("\n'");
        functions.append("\n  LANGUAGE 'plpgsql' VOLATILE;");
        statements.add(functions.toString());
        functions = new StringBuilder();
        functions.append("\n");
        functions.append("\n\nCREATE OR REPLACE FUNCTION " + GenericWrapperUtils.CREATE_CLASS + "(VARCHAR,VARCHAR)");
        functions.append("\nRETURNS void AS");
        functions.append("\n'");
        functions.append("\n    begin");
        functions.append("\n	IF(NOT CLASS_EXISTS($1)) THEN");
        functions.append("\n		EXECUTE $2;");
        functions.append("\n		RETURN;");
        functions.append("\n	END IF;");
        functions.append("\n	RETURN;");
        functions.append("\n    end;");
        functions.append("\n'");
        functions.append("\n  LANGUAGE 'plpgsql' VOLATILE;");
        statements.add(functions.toString());
        functions = new StringBuilder();
        functions.append("\n");
        functions.append("\n\nCREATE OR REPLACE FUNCTION " + GenericWrapperUtils.CREATE_SCHEMA + "(VARCHAR,VARCHAR)");
        functions.append("\nRETURNS void AS");
        functions.append("\n'");
        functions.append("\n    begin");
        functions.append("\n	IF(NOT SCHEMA_EXISTS($1)) THEN");
        functions.append("\n		EXECUTE $2;");
        functions.append("\n		RETURN ;");
        functions.append("\n	END IF;");
        functions.append("\n	RETURN ;");
        functions.append("\n    end;");
        functions.append("\n'");
        functions.append("\n  LANGUAGE 'plpgsql' VOLATILE;");
        statements.add(functions.toString());
        functions = new StringBuilder();
        functions.append("\n");
        functions.append("\n");
        functions.append("\nCREATE OR REPLACE FUNCTION " + GenericWrapperUtils.CREATE_FUNCTION + "(VARCHAR,VARCHAR)");
        functions.append("\nRETURNS void AS");
        functions.append("\n'");
        functions.append("\n    begin");
        functions.append("\n	IF(NOT FUNCTION_EXISTS($1)) THEN");
        functions.append("\n		EXECUTE $2;");
        functions.append("\n		RETURN ;");
        functions.append("\n	END IF;");
        functions.append("\n");
        functions.append("\n	RETURN ;");
        functions.append("\n    end;");
        functions.append("\n'");
        functions.append("\n  LANGUAGE 'plpgsql' VOLATILE;");
        statements.add(functions.toString());
        functions = new StringBuilder();
        functions.append("\n");
        functions.append("\n");
        functions.append("\nCREATE OR REPLACE FUNCTION xs_concat(text, text) RETURNS text AS");
        functions.append("\n'");
        functions.append("\n    DECLARE");
        functions.append("\n       t text;");
        functions.append("\n    begin");
        functions.append("\n       IF(character_length($1) > 0) THEN");
        functions.append("\n          t = $1 ||'', ''|| $2;");
        functions.append("\n       ELSE");
        functions.append("\n          t = $2;");
        functions.append("\n       END IF;");
        functions.append("\n       RETURN t;");
        functions.append("\n    END;");
        functions.append("\n'");
        functions.append("\n  LANGUAGE 'plpgsql' VOLATILE;");
        statements.add(functions.toString());
        functions.append("\nSELECT " + GenericWrapperUtils.CREATE_FUNCTION + "('xs_a_concat','CREATE AGGREGATE xs_a_concat(");
        functions.append("\n    BASETYPE = text,");
        functions.append("\n    SFUNC = textcat,");
        functions.append("\n    STYPE = text,");
        functions.append("\n    INITCOND = '''')');");
        statements.add(functions.toString());
        
        
        StringBuilder buffer = new StringBuilder();
        buffer.append("SELECT " + GenericWrapperUtils.CREATE_CLASS + "('analytics', '");
        buffer.append("CREATE TABLE analytics\n");
        buffer.append("(\n");
        buffer.append("        id serial NOT NULL,\n");
        buffer.append("        entry_date timestamp without time zone,\n");
        buffer.append("        entry_level character varying(10),\n");
        buffer.append("        entry_location character varying(512),\n");
        buffer.append("        entry_type character varying(32),\n");
        buffer.append("        entry_subtype character varying(32),\n");
        buffer.append("        duration bigint,\n");
        buffer.append("        message text,\n");
        buffer.append("        PRIMARY KEY (id)\n");
        buffer.append(") WITH (OIDS=FALSE)');");

        statements.add(buffer.toString());
        


        statements.add("\n\n--XDAT SEARCH ENTRIES\n" +
				"SELECT " + GenericWrapperUtils.CREATE_SCHEMA + "('xdat_search','CREATE SCHEMA xdat_search;');");
		
		statements.add("\n\n--XDAT SEARCH ENTRIES\n" +
				"SELECT " + GenericWrapperUtils.CREATE_CLASS + "('xdat_searches','CREATE TABLE xdat_searches"+
		"\n("+
				"\n  search_name varchar(255) NOT NULL,"+
				"\n  last_access timestamp DEFAULT now(),"+
				"\n  created timestamp DEFAULT now(),"+
				"\n  owner varchar(255)"+
				"\n) "+
				"\nWITHOUT OIDS');");

		statements.add("GRANT ALL ON TABLE xdat_searches TO public;");


		statements.add("\n\n--XDAT SEARCH ENTRIES\n" +
				"SELECT " + GenericWrapperUtils.CREATE_CLASS + "('xs_fav_entries','CREATE TABLE xdat_search.xs_fav_entries "+
				"\n( "+
				"\n  datatype character varying(255), "+
				"\n  id character varying(255), "+
				"\n  xdat_user_id integer "+
				"\n) "+
				"\nWITH OIDS;');");

		statements.add("GRANT ALL ON TABLE xdat_search.xs_fav_entries TO public;");
		
		statements.add("\n\n--XDAT SEARCH ENTRIES\n" +
				"SELECT " + GenericWrapperUtils.CREATE_CLASS + "('xs_materialized_views','CREATE TABLE xdat_search.xs_materialized_views"+
				"\n("+
				"\ntable_name character varying(255),"+
				"\ncreated timestamp without time zone DEFAULT now(),"+
				"\nlast_access timestamp without time zone DEFAULT now(),"+
				"\nusername character varying(255),"+
				"\nsearch_id text,"+
				"\ntag character varying(255),"+
				"\nsearch_sql text,"+
				"\nsearch_xml text"+
				"\n) "+
				"\nWITH OIDS;');");

		statements.add("GRANT ALL ON TABLE xdat_search.xs_materialized_views TO public;");
		
		statements.add("\n\n--XDAT SEARCH ENTRIES\n" +
				"SELECT " + GenericWrapperUtils.CREATE_CLASS + "('xs_item_access','CREATE TABLE xdat_search.xs_item_access"+
				"\n("+
				"\nsearch_value character varying(255),"+
				"\nsearch_element character varying(255),"+
				"\nsearch_field character varying(255),"+
				"\naccessed timestamp without time zone DEFAULT now(),"+
				"\nxdat_user_id character varying(255),"+
				"\nmethod character varying(255)"+
				"\n)"+
				"\nWITH OIDS;');");
        
        return statements;
    }
}
