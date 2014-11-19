//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.generators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.search.QueryOrganizer;
import org.nrg.xft.TypeConverter.PGSQLMapping;
import org.nrg.xft.TypeConverter.TypeConverter;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.references.XFTManyToManyReference;
import org.nrg.xft.references.XFTMappingColumn;
import org.nrg.xft.references.XFTReferenceI;
import org.nrg.xft.references.XFTRelationSpecification;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.SearchCriteria;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TextFunctionGenerator {

	public static ArrayList GetTextOutputFunctions(GenericWrapperElement primary_input)
	    throws ElementNotFoundException, XFTInitException, Exception
	    {
	        ArrayList all = new ArrayList();
	        boolean isHistory=false;	        
	        final GenericWrapperElement surrogate_input;
	        
	        if(primary_input.getName().endsWith("_history")){
	        	isHistory=true;
	        	surrogate_input=GenericWrapperElement.GetElement(primary_input.getXSIType().substring(0,primary_input.getXSIType().indexOf("_history")));
	        }else{
	        	surrogate_input=primary_input;
	        }
	
	        all.add(TextFunctionGenerator.getPrimaryTextFunction(primary_input,surrogate_input,isHistory));
	        
	        if (surrogate_input.isExtended() && (!(primary_input.getName().endsWith("meta_data")))) {
		        if(isHistory){
		        	all.add(TextFunctionGenerator.getTextHisFunction(primary_input,surrogate_input));
		        }else{
		        	all.add(TextFunctionGenerator.getTextExtFunction(primary_input,surrogate_input));
		        }
	        }
	        return all;
	    }
	
	public static String getPrimaryTextFunction(GenericWrapperElement primary_input,GenericWrapperElement surrogate_input,boolean isHistory) throws XFTInitException, ElementNotFoundException{
		StringBuffer sb = new StringBuffer();
		
        Object[][] keyArray = primary_input.getSQLKeys();

        String functionName = primary_input.getTextFunctionName() + "(";
        if (surrogate_input.isExtended() && (!(surrogate_input.getName().endsWith("meta_data")))) {
            functionName = GenericWrapperUtils.TXT_EXT_FUNCTION + primary_input.getFormattedName() + "(";
        }
        for (int i = 0; i < keyArray.length; i++) {
            if (i > 0)
                functionName += ",";
            functionName += " " + keyArray[i][1];
        }
        functionName += ", int4,bool,bool,bool)";
        sb.append("\n\n\nCREATE OR REPLACE FUNCTION " + functionName);

        String counter = "$" + (keyArray.length + 1);
        String allowMultiples = "$" + (keyArray.length + 2);
        String isRoot = "$" + (keyArray.length + 3);
        String preventLoop = "$" + (keyArray.length + 4);
        sb.append("\n  RETURNS TEXT AS");
        sb.append("\n'");
        
        TextInitCode(sb, counter, primary_input, keyArray);
        sb.append("\n      fullText := ''Item:(''");
        sb.append(" || local_count || ''(").append(primary_input.getXSIType()).append(
                ")('';");
        CurrentRowLoopBegin(sb, primary_input, keyArray);
        
        Iterator fieldIter = surrogate_input.getAllFields(false, true).iterator();
        TypeConverter converter = new TypeConverter(new PGSQLMapping(primary_input.getWrapped().getSchemaPrefix()));
        while (fieldIter.hasNext()) {
            GenericWrapperField field = (GenericWrapperField) fieldIter.next();
            if (field.isReference()) {
                GenericWrapperElement foreign = (GenericWrapperElement) field.getReferenceElement();
                if (field.isMultiple()) {
                    if (!(primary_input.getName().endsWith("meta_data"))) {
                        try {
                            XFTReferenceI ref = field.getXFTReference();
                            if (ref.isManyToMany()) {
                                
                                String mappingTable = ((XFTManyToManyReference) ref)
                                        .getMappingTable();
                                sb.append("\n        IF("+ allowMultiples);
                                if (field.isOnlyRoot())
                                {
                                    sb.append(" AND "+ isRoot +"");
                                }
                                
                                if (field.isPossibleLoop()){
                                    sb.append(" AND ( NOT "+ preventLoop +")");
                                }
                                
                                sb.append(") THEN ");
          
                                sb.append("\n        DECLARE ");
	                            sb.append("\n  --    120");
                                sb.append("\n        mapping_row RECORD; ");
                                sb.append("\n        loop_count int4:=0; ");
                                sb.append("\n        BEGIN ");
                                sb.append("\n        FOR mapping_row IN SELECT * FROM ");
                                if(isHistory){
                                    sb.append(mappingTable + "_history WHERE ");
                                }else{
                                    sb.append(mappingTable + " WHERE ");
                                }
                                Iterator refCols = ((XFTManyToManyReference) ref)
                                        .getMappingColumnsForElement(surrogate_input)
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
                                if(isHistory){
                                    sb.append(" AND xft_version=current_row.xft_version");
                                }

                                sb.append("\n        LOOP");
                                
                                sb.append("\n           child_count := child_count+1;");
                                sb.append("\n           tempText := NULL;");
                                sb.append("\n           tempText := " + GenericWrapperUtils.TXT_FUNCTION)
                                        .append(foreign.getFormattedName());
                                if(isHistory)sb.append("_history");
                                sb.append("(");
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
                                sb.append("\n        IF("+ allowMultiples);
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
	                            sb.append("\n  --    198");
                                sb.append("\n        parent_row RECORD; ");
                                sb.append("\n        loop_count int4:=0; ");
                                sb.append("\n        BEGIN ");
                                sb.append("\n        FOR parent_row IN SELECT * FROM "
                                                + foreign.getSQLName());
                                
                                if(isHistory){
                                	sb.append("_history WHERE ");
                                }else{
                                	sb.append(" WHERE ");
                                }

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
                                
                                if(isHistory){
                                	sb.append(" AND xft_version=current_row.xft_version");
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
                                	.append(foreign.getFormattedName());
		                        if(isHistory)sb.append("_history");
		                        sb.append("(");
		                        
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
                    XFTSuperiorReference supRef = (XFTSuperiorReference) field.getXFTReference();
                    if (supRef.getSubordinateElement().equals(surrogate_input)) {
                        //INPUT has the fk column (check if it is null)
                    	IfCurrentRow(primary_input, surrogate_input, sb, isRoot, supRef, preventLoop, field, isHistory, foreign, allowMultiples);
                    	ForeignKeys(primary_input, surrogate_input, sb, isRoot, supRef, preventLoop, field, isHistory, foreign, allowMultiples);
                        DefaultReferenceCall(primary_input,surrogate_input, sb, isRoot, supRef, preventLoop, field, isHistory, foreign, allowMultiples);
                        EndIf(sb);
                    } else {

                        if (!(primary_input.getName().endsWith("meta_data"))) {
                            //FOREIGN has the fk column
                        	
                            sb.append("\n        IF("+ allowMultiples +") THEN ");
                            sb.append("\n  --    354");
                            sb.append("\n        DECLARE ");
                            sb.append("\n        parent_row RECORD; ");
                            sb.append("\n        loop_count int4:=0; ");
                            sb.append("\n        BEGIN ");
                            sb.append("\n        FOR parent_row IN SELECT * FROM ").append(foreign.getSQLName());
                            if(isHistory)sb.append("_history");
                            sb.append(" WHERE ");

                            Iterator refsCols = supRef.getKeyRelations().iterator();
                            int count = 0;
                            while (refsCols.hasNext()) {
                                XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
                                if (count++ > 0)
                                    sb.append(" AND ");
                                sb.append(spec.getLocalCol()).append("=current_row.").append(spec.getForeignCol());
                            }
                            if(isHistory)sb.append(" AND xft_version=current_row.xft_version ");

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
                            	.append(foreign.getFormattedName());
                            if(isHistory)sb.append("_history");
                            sb.append("(");
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
                    }else if (type.equals("INTEGER") || type.equals("SMALLINT") || type.equals("TINYINT")) {
                        sb.append("\n              fullText := fullText || ''(").append(field.getSQLName().toLowerCase());
                        sb.append(":integer)=('' || current_row.").append(field.getSQLName().toLowerCase()).append(" || '')'';");
                    }else if (type.equals("BIGINT")) {
                        sb.append("\n              fullText := fullText || ''(").append(field.getSQLName().toLowerCase());
                        sb.append(":long)=('' || current_row.").append(field.getSQLName().toLowerCase()).append(" || '')'';");
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
        Iterator iter2 = surrogate_input.getUndefinedReferences().iterator();
        while (iter2.hasNext())
        {
            GenericWrapperField field = (GenericWrapperField)iter2.next();
            if (field.isReference() && (!field.isMultiple()))
            {
                XFTSuperiorReference supRef = (XFTSuperiorReference)field.getXFTReference();
                sb.append("\n  -- 475");
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
                    sb.append(" && "+ isRoot +"");
                }
                
                if (field.isPossibleLoop()){
                    sb.append(" AND NOT ("+ preventLoop +")");
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
        if (!surrogate_input.containsStatedKey()) {
			GenericWrapperField field = (GenericWrapperField)surrogate_input.getDefaultKey();
			sb.append("\n          IF (current_row.").append(
                    field.getSQLName().toLowerCase()).append(
                    " IS NOT NULL) THEN ");
            
            sb.append("\n              fullText := fullText || ''(").append(field.getSQLName().toLowerCase());
            sb.append(":integer)=('' || current_row.").append(field.getSQLName().toLowerCase()).append(" || '')'';");
            
            sb.append("\n          END IF;");
		}
        if (isHistory) {
			sb.append("\n          IF (current_row.xft_version IS NOT NULL) THEN ");
            sb.append("\n              fullText := fullText || ''(xft_version:integer)=('' || current_row.xft_version || '')'';");
            sb.append("\n          END IF;");
            
			sb.append("\n          IF (current_row.history_id IS NOT NULL) THEN ");
            sb.append("\n              fullText := fullText || ''(history_id:integer)=('' || current_row.history_id || '')'';");
            sb.append("\n          END IF;");
            
			sb.append("\n          IF (current_row.change_user IS NOT NULL) THEN ");
            sb.append("\n              fullText := fullText || ''(change_user:integer)=('' || current_row.change_user || '')'';");
            sb.append("\n          END IF;");
            
			sb.append("\n          IF (current_row.change_date IS NOT NULL) THEN ");
            sb.append("\n              fullText := fullText || ''(change_date:dateTime)=('' || current_row.change_date || '')'';");
            sb.append("\n          END IF;");
            
			sb.append("\n          IF (current_row.previous_change_date IS NOT NULL) THEN ");
            sb.append("\n              fullText := fullText || ''(previous_change_date:dateTime)=('' || current_row.previous_change_date || '')'';");
            sb.append("\n          END IF;");
		}
                
        EndLoop(sb);
        if(surrogate_input.isExtension())
        {
            try {
                GenericWrapperField field = surrogate_input.getExtensionField();
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
                if (!(primary_input.getName().endsWith("meta_data"))) {
                    sb.append("\n           child_count := child_count+1;");
                    sb.append("\n           tempText := NULL;");
                    sb.append("\n           tempText := "+GenericWrapperUtils.TXT_EXT_FUNCTION)
                        	.append(foreign.getFormattedName());
                    if(isHistory)sb.append("_history");
                    sb.append("(");

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
                GenericWrapperUtils.logger.error("",e);
            } catch (FieldNotFoundException e) {
                GenericWrapperUtils.logger.error("",e);
            } catch (XFTInitException e) {
                GenericWrapperUtils.logger.error("",e);
            }
        }
        sb.append("\n	   fullText := fullText || '')*END_ITEM*'' || local_count || '')'';");
        sb.append("\n	");
        sb.append("\n	 RETURN fullText;");
        sb.append("\n    end;");
        sb.append("\n'");
        sb.append("\n  LANGUAGE 'plpgsql' VOLATILE;");
        
        return sb.toString();
	}

	private static void EndLoop(StringBuffer sb) {
        sb.append("\n      END LOOP;");
	}
	
	private static void CurrentRowLoopBegin(StringBuffer sb, GenericWrapperElement primary_input,Object[][] keyArray)
	{
		sb.append("\n      FOR current_row IN SELECT * FROM ").append(primary_input.getSQLName());
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
	}

	private static void TextInitCode(StringBuffer sb, String counter, GenericWrapperElement primary_input,Object[][] keyArray) {
		sb.append("\n    declare");
        sb.append("\n     current_row RECORD;");
        sb.append("\n     tempText TEXT;");
        sb.append("\n     fullText TEXT;");
        sb.append("\n     row_ct int4;");
        sb.append("\n     local_count int4;");
        sb.append("\n     child_count int4;");
        sb.append("\n    begin");
        sb.append("\n  --    72");

        //sb.append("\n RAISE NOTICE ''" +functionName + "'';");
        sb.append("\n      local_count := ").append(counter).append(";");
        sb.append("\n      child_count := ").append(counter).append(";");
        sb.append("\n      row_ct := 0;");
        
	}

	private static void IfCurrentRow(GenericWrapperElement primary_input,GenericWrapperElement surrogate_input, StringBuffer sb, String isRoot,XFTSuperiorReference supRef,String preventLoop,GenericWrapperField field,boolean isHistory,GenericWrapperElement foreign, String allowMultiples){
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
	}
	
	private static void ForeignKeys(GenericWrapperElement primary_input,GenericWrapperElement surrogate_input, StringBuffer sb, String isRoot,XFTSuperiorReference supRef,String preventLoop,GenericWrapperField field,boolean isHistory,GenericWrapperElement foreign, String allowMultiples){
		Iterator refsCols = supRef.getKeyRelations().iterator();
	    while (refsCols.hasNext()) {
	        XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
	        sb.append( "\n            fullText := fullText || ''(").append(spec.getLocalCol().toLowerCase()).append(":" + spec.getSchemaType().getLocalType() + ")=('' || current_row.").append(spec.getLocalCol().toLowerCase()).append(" || '')'';");
	    }
	}
	
	private static void HistoricalReferenceBegin(StringBuffer sb, GenericWrapperElement foreign, XFTSuperiorReference supRef){
		sb.append("\n           child_count := child_count+1;");
        sb.append("\n           tempText := NULL;");
    	sb.append("\n           DECLARE");
    	sb.append("\n           	ext_row RECORD;");
    	sb.append("\n           BEGIN ");
    	sb.append("\n           	FOR ext_row IN SELECT * FROM ");
    	sb.append(foreign.getFormattedName());
    	sb.append("_history WHERE ");
    	
    	Iterator refsCols = supRef.getKeyRelations().iterator();
    	int count = 0;
        while (refsCols.hasNext()) {
            XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
            if (count++ > 0)
                sb.append(" AND ");
            sb.append(spec.getLocalCol()).append("=current_row.").append(spec.getLocalCol());
        }
        
    	sb.append(" AND xft_version=current_row.xft_version");
    	sb.append("\n           	LOOP");
	}
	
	private static void HistoricalReferenceEnd(StringBuffer sb){
    	sb.append("\n           	END LOOP;");
    	sb.append("\n           END;");
	}
	
	private static String BuildLocalColumnArguments(String prefix,XFTSuperiorReference supRef){
		StringBuffer sb=new StringBuffer();
		Iterator refsCols = supRef.getKeyRelations().iterator();
    	int count = 0;
        while (refsCols.hasNext()) {
            XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
                    .next();
            if (count++ > 0)
                sb.append(", ");
            sb.append(prefix).append(spec.getLocalCol());
        }
        return sb.toString();
	}
	
	private static void DefaultReferenceCall(GenericWrapperElement primary_input,GenericWrapperElement surrogate_input, StringBuffer sb, String isRoot,XFTSuperiorReference supRef,String preventLoop,GenericWrapperField field,boolean isHistory,GenericWrapperElement foreign, String allowMultiples) {

		
        if (!(primary_input.getName().endsWith("meta_data"))) {
            if(isHistory && (surrogate_input.getExtensionFieldName().equalsIgnoreCase(field.getName()))){
                HistoricalReferenceBegin(sb, foreign, supRef);
        		sb.append("\n -- 632");                
            	sb.append("\n           	   tempText := ie_");
            	sb.append(foreign.getFormattedName());
            	sb.append("_history(ext_row.history_id, child_count,$3,false,false);");
            	sb.append("\n           	      fullText := fullText || ''("
                                + (field.getSQLName() + "_" + field
                                        .getXMLType()
                                        .getLocalType())
                                        .toLowerCase() + ":XFTItem)=('' || tempText || '')'';");
            	
            	HistoricalReferenceEnd(sb);
            	
            }else{
        		String prefix=null;
        		if (surrogate_input.getExtensionFieldName().equalsIgnoreCase(field.getName()))
                {
                    prefix=GenericWrapperUtils.TXT_EXT_FUNCTION;
                }else{
                    prefix=GenericWrapperUtils.TXT_FUNCTION;
                }
            	if(isHistory && !(foreign.getXSIType().equals("xdat:meta_element") || foreign.getXSIType().endsWith("_meta_data"))){
            		sb.append("\n -- 661");
            		
            		sb.append("\n           DECLARE"); 
            		sb.append("\n           mapping_row RECORD; "); 
            		sb.append("\n           loop_count int4:=0; "); 
            		sb.append("\n           BEGIN "); 
            		sb.append("\n           	FOR mapping_row IN SELECT * FROM "+foreign.getSQLName()+"_history WHERE  ");
            		Iterator refsCols = supRef.getKeyRelations().iterator();
                	int count = 0;
                    while (refsCols.hasNext()) {
                        XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
                                .next();
                        if (count++ > 0)
                            sb.append(", ");
                        sb.append(spec.getForeignCol()).append("=").append("current_row.").append(spec.getLocalCol());
                    }
            		sb.append(" AND xft_version=current_row.xft_version LIMIT 1"); 
                    sb.append("\n           	LOOP");        
                    
                    RetrieveItemI(prefix,foreign.getFormattedName()+"_history","mapping_row.history_id",allowMultiples,field,sb);
                	
                    sb.append("\n           	END LOOP;"); 
            		sb.append("\n           END; "); 
            	}else{
            		sb.append("\n -- 693");
            		RetrieveItemI(prefix,foreign.getFormattedName(),BuildLocalColumnArguments("current_row.",supRef),allowMultiples,field,sb);
            	}
            }
            
        }
		
	}
	
	private static void RetrieveItemI(String prefix, String xsiType,String localCols,String allowMultiples, GenericWrapperField field, StringBuffer sb){
		sb.append("\n           child_count := child_count+1;");
	    sb.append("\n           tempText := NULL;");
		sb.append("\n           tempText := " + prefix);
	
		sb.append(xsiType);
		
	    sb.append("("+localCols +", child_count," + allowMultiples + ",false," + field.getPreventLoop()+");");
	   // if(isHistory)sb.append(",current_row.xft_version");
	    sb.append("\n              fullText := fullText || ''("
	                    + (field.getSQLName() + "_" + field
	                            .getXMLType()
	                            .getLocalType())
	                            .toLowerCase() + ":XFTItem)=('' || tempText || '')'';");
	}
	
	private static void EndIf(StringBuffer sb){
        sb.append("\n        END IF;");
	}

	public static String getTextExtFunction(GenericWrapperElement primary_input, GenericWrapperElement surrogate_input){
		StringBuffer sb=new StringBuffer();
		String functionName = primary_input.getTextFunctionName() + "(";
	
	    final Object[][] keyArray = primary_input.getSQLKeys();
	     
        for (int i = 0; i < keyArray.length; i++) {
            if (i > 0)
                functionName += ",";
            functionName += " " + keyArray[i][1];
        }
        functionName += ", int4,bool,bool,bool)";
        
        final String counter = "$" + (keyArray.length + 1);
	     final String allowMultiples = "$" + (keyArray.length + 2);
	     final String isRoot = "$" + (keyArray.length + 3);
	     final String preventLoop = "$" + (keyArray.length + 4);
	     
        sb.append("\n\n\nCREATE OR REPLACE FUNCTION " + functionName);

        sb.append("\n  RETURNS TEXT AS");
        sb.append("\n'");
        sb.append("\n    declare");
        sb.append("\n     current_row RECORD;");
        sb.append("\n     fullText TEXT;");
        sb.append("\n    begin");

        //sb.append("\n RAISE NOTICE ''" +functionName + "'';");
        if (primary_input.isExtended() && (!(primary_input.getName().endsWith("meta_data")))) {
            //Build Query to get Extension id
            try {
                QueryOrganizer qo = new QueryOrganizer(primary_input, null, ViewManager.ALL);
                qo.addField(primary_input.getFullXMLName() + "/extension_item/element_name");
                
                String query;
//	                if(isHistory){		
//		                query = "SELECT * FROM (" + qo.buildQuery() + ") SEARCH WHERE "+ primary_input.getSQLName()+".history_id="+keyArray[0][2];
//		                query.replaceAll(surrogate_input.getSQLName() +" ", primary_input.getSQLName() +" ");
//		                for(List extension:surrogate_input.getExtendedElements()){
//		                	GenericWrapperElement e=(GenericWrapperElement)extension.get(0);
//		                	query.replaceAll(e.getSQLName() +" ", e.getSQLName() +"_history ");
//			            }
//	                }else{
                	CriteriaCollection cc = new CriteriaCollection("AND");
	                for (int i = 0; i < keyArray.length; i++) {
	                    GenericWrapperField gwf = (GenericWrapperField) keyArray[i][3];
	                    qo.addField(gwf.getXMLPathString(primary_input.getFullXMLName()));
	
	                    SearchCriteria sc = new SearchCriteria();
	                    sc.setFieldWXMLPath(gwf.getXMLPathString(primary_input.getFullXMLName()));
	                    sc.setOverrideFormatting(true);
	                    sc.setValue(keyArray[i][2]);
	                    cc.add(sc);
	                }
	
	                query = "SELECT * FROM (" + qo.buildQuery() + ") SEARCH  WHERE " + cc.getSQLClause(qo);
//	                }
                
                String colname = qo.translateXMLPath(primary_input.getFullXMLName()
                        + "/extension_item/element_name");

                sb.append("\n      FOR current_row IN ").append(query);
                sb.append("\n      LOOP");
                sb.append("\n         IF (current_row.").append(colname)
                        .append(" IS NULL) THEN ");
                sb.append("\n             fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION)
                     .append(surrogate_input.getFormattedName());
                sb.append("(");
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
                Iterator pEs = surrogate_input.getPossibleExtenders().iterator();
                while (pEs.hasNext()) {
                    SchemaElementI se = (SchemaElementI) pEs.next();
                    sb.append("\n            IF (current_row.").append(
                            colname).append("=''").append(
                            se.getFullXMLName()).append("'') THEN");
                    //sb.append("\n RAISE NOTICE ''PASSING CALL FROM " +
                    // input.getSQLName() + " TO " + se.getSQLName() +
                    // "'';");
                    if (se.getGenericXFTElement().isExtended()){
                        sb.append("\n                fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION);
                    } else{
	                    sb.append("\n                fullText:= " + GenericWrapperUtils.TXT_FUNCTION);
	   	                     
                    }
                    sb.append(se.getFormattedName());   
                       sb.append("(");
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
               sb.append("\n                    fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION)
               	.append(surrogate_input.getFormattedName());
               sb.append("(");
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
            	System.out.println(e);
                GenericWrapperUtils.logger.error("", e);
                sb.append("\n-- EXCEPTION IN SQL GENERATION" );
                sb.append("\n      fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION)
               	.append(surrogate_input.getFormattedName());
                   sb.append("(");
                for (int i = 0; i < keyArray.length; i++) {
                    if (i > 0)
                        sb.append(",");
                    sb.append(" ").append(keyArray[i][2]);
                }
                sb.append(", ").append(counter).append("," + allowMultiples + "," + isRoot +"," + preventLoop + ");");
            }
        } else {
            sb.append("\n -- ITEM IS NOT EXTENDED BY ANYTHING... REDIRECT TO MAIN FUNCTION");
            sb.append("\n      fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION)
           	.append(surrogate_input.getFormattedName());
            sb.append("(");
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
        
        return sb.toString();
	}
	
	public static String getTextHisFunction(GenericWrapperElement primary_input, GenericWrapperElement surrogate_input) throws Exception{
		StringBuffer sb=new StringBuffer();
		String functionName = primary_input.getTextFunctionName() + "(";
	
	    final Object[][] keyArray = primary_input.getSQLKeys();
	
	    final Object[][] surrogateKeyArray = surrogate_input.getSQLKeys();
	     
        for (int i = 0; i < keyArray.length; i++) {
            if (i > 0)
                functionName += ",";
            functionName += " " + keyArray[i][1];
        }
        functionName += ", int4,bool,bool,bool)";
        
        final String counter = "$" + (keyArray.length + 1);
	     final String allowMultiples = "$" + (keyArray.length + 2);
	     final String isRoot = "$" + (keyArray.length + 3);
	     final String preventLoop = "$" + (keyArray.length + 4);
	     
        sb.append("\n\n\nCREATE OR REPLACE FUNCTION " + functionName);

        sb.append("\n  RETURNS TEXT AS");
        sb.append("\n'");

        //sb.append("\n RAISE NOTICE ''" +functionName + "'';");
        if (surrogate_input.isExtension() && (!(primary_input.getName().endsWith("meta_data")))) {
        	TextInitCode(sb, counter, primary_input, surrogateKeyArray);
            CurrentRowLoopBegin(sb, primary_input, keyArray);
        	
        	GenericWrapperField field=surrogate_input.getExtensionField();
            GenericWrapperElement foreign = (GenericWrapperElement) field.getReferenceElement();
            XFTSuperiorReference supRef = (XFTSuperiorReference) field.getXFTReference();
        	
        	IfCurrentRow(primary_input, surrogate_input, sb, isRoot, supRef, preventLoop, field, true, foreign, allowMultiples);
        	HistoricalReferenceBegin(sb, foreign, supRef);
             
         	sb.append("\n           	   tempText := i_");
         	sb.append(foreign.getFormattedName());
         	sb.append("_history(ext_row.history_id, child_count,$3,false,false);");
         	
         	sb.append("\n RETURN tempText;");
         	
         	HistoricalReferenceEnd(sb);
            EndIf(sb);
            EndLoop(sb);
            
            sb.append("\n      fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION)
           	.append(surrogate_input.getFormattedName());
            sb.append("_history");
               sb.append("(");
            for (int i = 0; i < keyArray.length; i++) {
                if (i > 0)
                    sb.append(",");
                sb.append(" ").append(keyArray[i][2]);
            }
            sb.append(", ").append(counter).append("," + allowMultiples + "," + isRoot +"," + preventLoop + ");");
        }else if (surrogate_input.isExtended() && (!(primary_input.getName().endsWith("meta_data")))) {
            sb.append("\n    declare");
            sb.append("\n     current_row RECORD;");
            sb.append("\n     fullText TEXT;");
            sb.append("\n    begin");
            //Build Query to get Extension id
            try {
                QueryOrganizer qo = new QueryOrganizer(primary_input, null, ViewManager.ALL);
                qo.addField(primary_input.getFullXMLName() + "/extension_item/element_name");
                qo.addField(primary_input.getFullXMLName() + "/xft_version");
                qo.addField(primary_input.getFullXMLName() + "/history_id");
                
                String query;
//	                if(isHistory){		
//		                query = "SELECT * FROM (" + qo.buildQuery() + ") SEARCH WHERE "+ primary_input.getSQLName()+".history_id="+keyArray[0][2];
//		                query.replaceAll(surrogate_input.getSQLName() +" ", primary_input.getSQLName() +" ");
//		                for(List extension:surrogate_input.getExtendedElements()){
//		                	GenericWrapperElement e=(GenericWrapperElement)extension.get(0);
//		                	query.replaceAll(e.getSQLName() +" ", e.getSQLName() +"_history ");
//			            }
//	                }else{
                	CriteriaCollection cc = new CriteriaCollection("AND");
                	
                    SearchCriteria sc = new SearchCriteria();
                    sc.setFieldWXMLPath(primary_input.getFullXMLName() + "/history_id");
                    sc.setOverrideFormatting(true);
                    sc.setValue(keyArray[0][2]);
                    cc.add(sc);
	                for (int i = 0; i < surrogateKeyArray.length; i++) {
	                    GenericWrapperField gwf = (GenericWrapperField) surrogateKeyArray[i][3];
	                    qo.addField(gwf.getXMLPathString(primary_input.getFullXMLName()));
	                }
//	                    SearchCriteria sc = new SearchCriteria();
//	                    sc.setFieldWXMLPath(gwf.getXMLPathString(primary_input.getFullXMLName()));
//	                    sc.setOverrideFormatting(true);
//	                    sc.setValue(keyArray[i][2]);
//	                    cc.add(sc);
//	                }
	
	                query = "SELECT * FROM (" + qo.buildQuery() + ") SEARCH  WHERE " + cc.getSQLClause(qo);
//	                }
                
                String colname = qo.translateXMLPath(primary_input.getFullXMLName()
                        + "/extension_item/element_name");
                String verCol = primary_input.getSQLName() + "_xft_version";
                
                List<String> pksCols=new ArrayList();
                for (int i = 0; i < surrogateKeyArray.length; i++) {
                    GenericWrapperField gwf = (GenericWrapperField) surrogateKeyArray[i][3];
                    pksCols.add(qo.translateXMLPath(gwf.getXMLPathString(primary_input.getFullXMLName())));
                }

                sb.append("\n      FOR current_row IN ").append(query);
                sb.append("\n      LOOP");
                sb.append("\n         IF (current_row.").append(colname)
                        .append(" IS NULL) THEN ");
                sb.append("\n             fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION)
                     .append(surrogate_input.getFormattedName());
                sb.append("(");
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
                Iterator pEs = surrogate_input.getPossibleExtenders().iterator();
                while (pEs.hasNext()) {
                    SchemaElementI se = (SchemaElementI) pEs.next();
                    sb.append("\n            IF (current_row.").append(
                            colname).append("=''").append(
                            se.getFullXMLName()).append("'') THEN");
                    
                    //sb.append("\n RAISE NOTICE ''PASSING CALL FROM " +
                    // input.getSQLName() + " TO " + se.getSQLName() +
                    // "'';");\
                    sb.append("\n            --907 ");
                    sb.append("\n            DECLARE");
                    sb.append("\n            	ext_row RECORD;");
                    sb.append("\n            BEGIN ");
                    sb.append("\n            	FOR ext_row IN SELECT * FROM ");
                    sb.append(se.getFormattedName()); 
                    sb.append("_history WHERE ");
                    for (int i = 0; i < surrogateKeyArray.length; i++) {
                      sb.append(surrogateKeyArray[i][0]).append("=").append("current_row.").append(pksCols.get(i)).append(" AND ");
                    }
                    sb.append("xft_version=current_row.").append(verCol);
                    sb.append("\n           	LOOP");
                    if (se.getGenericXFTElement().isExtended()){
                        sb.append("\n                fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION);
                    } else{
	                    sb.append("\n                fullText:= " + GenericWrapperUtils.TXT_FUNCTION);
	   	                     
                    }
                    sb.append(se.getFormattedName());   
                    sb.append("_history(ext_row.history_id, ").append(counter).append("," + allowMultiples + "," + isRoot +"," + preventLoop + ");");
                    sb.append("\n                matches:=1;");
                    sb.append("\n            	END LOOP;");
                    sb.append("\n            END;");
                    
//                    if (se.getGenericXFTElement().isExtended()){
//                        sb.append("\n                fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION);
//                    } else{
//	                    sb.append("\n                fullText:= " + GenericWrapperUtils.TXT_FUNCTION);
//	   	                     
//                    }
//                    sb.append(se.getFormattedName());   
//                       sb.append("(");
//                    for (int i = 0; i < keyArray.length; i++) {
//                        if (i > 0)
//                            sb.append(",");
//                        sb.append(" ").append(keyArray[i][2]);
//                    }
//                    sb.append(", ").append(counter).append("," + allowMultiples + "," + isRoot +"," + preventLoop + ");");
//                    sb.append("\n                matches:=1;");
                    sb.append("\n            END IF;");
                }
                sb.append("\n                IF (matches=0) THEN");
               sb.append("\n                    fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION)
               	.append(surrogate_input.getFormattedName());
               sb.append("_history");
               sb.append("(");
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
            	System.out.println(e);
                GenericWrapperUtils.logger.error("", e);
                sb.append("\n-- EXCEPTION IN SQL GENERATION" );
                sb.append("\n      fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION)
               	.append(surrogate_input.getFormattedName());
                sb.append("_history");
                   sb.append("(");
                for (int i = 0; i < keyArray.length; i++) {
                    if (i > 0)
                        sb.append(",");
                    sb.append(" ").append(keyArray[i][2]);
                }
                sb.append(", ").append(counter).append("," + allowMultiples + "," + isRoot +"," + preventLoop + ");");
            }
        } else {
            sb.append("\n -- ITEM IS NOT EXTENDED BY ANYTHING... REDIRECT TO MAIN FUNCTION");
            sb.append("\n      fullText:= " + GenericWrapperUtils.TXT_EXT_FUNCTION)
           	.append(surrogate_input.getFormattedName());
            sb.append("_history");
            sb.append("(");
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
        
        return sb.toString();
	}

}
