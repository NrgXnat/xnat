//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Apr 14, 2004
 */
package org.nrg.xft.generators;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.TypeConverter.PGSQLMapping;
import org.nrg.xft.TypeConverter.TypeConverter;
import org.nrg.xft.db.DBConfig;
import org.nrg.xft.db.DBPool;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.references.XFTManyToManyReference;
import org.nrg.xft.references.XFTReferenceI;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.references.XFTRelationSpecification;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.utils.FileUtils;
/**
 * Generater class that is used to output all of the SQL needed to create the
 * database, including CREATE, ALTER, VIEW, AND INSERT statements.
 * 
 * @author Tim
 */
public class SQLUpdateGenerator {
	static org.apache.log4j.Logger logger = Logger.getLogger(SQLUpdateGenerator.class);
	
	/**
	 * outputs all of the SQL needed to create the database, including CREATE,
	 * ALTER, VIEW, AND INSERT statements.
	 * @param location
	 */
	public static void generateDoc(String location) throws Exception
	{
		try {
		    StringBuffer sb =new StringBuffer();
		    Iterator iter = GetSQLCreate().iterator();
		    while (iter.hasNext())
		    {
		        sb.append(iter.next());
		    }
			FileUtils.OutputToFile(sb.toString(),location);
			System.out.println("File Created: " + location);
		} catch (org.nrg.xft.exception.XFTInitException e) {
			logger.error("",e);
		} catch (ElementNotFoundException e) {
			logger.error("",e);
		}
	}
	
	public static ArrayList GetSQLCreate() throws XFTInitException,ElementNotFoundException,Exception
	{
		ArrayList creates = new ArrayList();
		ArrayList alters = new ArrayList();

		Hashtable databases  = new Hashtable();
		Iterator dbconfigs = DBPool.GetPool().getDBConfigs().iterator();
		while (dbconfigs.hasNext())
		{
			DBConfig config = (DBConfig) dbconfigs.next();
			
			//LOAD CURRENT TABLES FROM DB
			ArrayList lowerCaseLoadedTables = new ArrayList();
			PoolDBUtils con = null;
			try {
				con = new PoolDBUtils();
				XFTTable t = con.executeSelectQuery("SELECT c.relname  FROM      pg_catalog.pg_class AS c            LEFT JOIN pg_catalog.pg_namespace AS n                 ON n.oid = c.relnamespace  WHERE     c.relkind IN ('r') AND            n.nspname NOT IN ('pg_catalog', 'pg_toast') AND            pg_catalog.pg_table_is_visible(c.oid)  ORDER BY  c.relname;",config.getDbIdentifier(),null);
				while (t.hasMoreRows())
				{
					t.nextRow();
					lowerCaseLoadedTables.add(t.getCellValue("relname").toString().toLowerCase());
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			databases.put(config.getDbIdentifier().toLowerCase(),lowerCaseLoadedTables);
		}
		
		//ITERATE THROUGH ELEMENTS
		Iterator elements = XFTManager.GetInstance().getOrderedElements().iterator();
		while (elements.hasNext())
		{
			GenericWrapperElement element = (GenericWrapperElement)elements.next();
			if (!(element.getName().equalsIgnoreCase("meta_data") || element.getName().equalsIgnoreCase("history") || element.isSkipSQL()))
			{
				String dbname = element.getDbName().toLowerCase();
				ArrayList lowerCaseLoadedTables = (ArrayList)databases.get(dbname);
				if (lowerCaseLoadedTables !=null)
				{
					if (lowerCaseLoadedTables.contains(element.getSQLName().toLowerCase()))
					{
						ArrayList updSts= new ArrayList();
						updSts= GetUpdateStatements(element);
						if (updSts.size()>0)
						{
							Iterator refs = updSts.iterator();
							while (refs.hasNext())
							{
								creates.add("\n\n" + refs.next());
							}
						}
						
	//					ArrayList alterSts= new ArrayList();
	//					alterSts= GetUpdatedAlterStatements(element);
	//					if (alterSts.size()>0)
	//					{
	//						Iterator refs = alterSts.iterator();
	//						while (refs.hasNext())
	//						{
	//							alters.append("\n\n").append(refs.next());
	//						}
	//					}
					}else{
						creates.add("\n\n" + GenericWrapperUtils.GetCreateStatement(element));
						
						//delete.add("\n\nDELETE FROM "+ element.getSQLName()+ ";");
						
						//logger.debug("Generating the ALTER sql for '" + element.getDirectXMLName() + "'");
						Iterator refs = GenericWrapperUtils.GetAlterTableStatements(element).iterator();
						while (refs.hasNext())
						{
							alters.add("\n\n" + refs.next());
						}
					}
				}else{
				    throw new Exception("Unable to connect to database.  Check your InstanceSettings.xml.");
				}
		
			}else{
			    if (XFT.VERBOSE)
			        System.out.print(" ");
			}
		}
		

		Iterator mappingTables = XFTReferenceManager.GetInstance().getUniqueMappings().iterator();
		while (mappingTables.hasNext())
		{
			XFTManyToManyReference map = (XFTManyToManyReference)mappingTables.next();
			ArrayList lowerCaseLoadedTables = (ArrayList)databases.get(map.getElement1().getDbName().toLowerCase());
			if (!lowerCaseLoadedTables.contains(map.getMappingTable().toLowerCase()))
			{
				logger.debug("Generating the CREATE sql for '" + map.getMappingTable() + "'");
				//delete.add("\n\nDELETE FROM "+ map.getMappingTable() + ";");
				creates.add("\n\n" + GenericWrapperUtils.GetCreateStatement(map));
				
				logger.debug("Generating the ALTER sql for '" + map.getMappingTable() + "'");
				//delete.add("\n\nDELETE FROM "+map.getMappingTable()+";");
				Iterator refs = GenericWrapperUtils.GetAlterTableStatements(map).iterator();
				while (refs.hasNext())
				{
					alters.add("\n\n" + refs.next());
				}
			}
		}
		
		ArrayList all = new ArrayList();
		all.addAll(creates);
		all.addAll(alters);
		all.addAll(GenericWrapperUtils.GetFunctionSQL());
		all.trimToSize();
		return all;
	}
	
	public static ArrayList GetUpdateStatements(GenericWrapperElement e){
		ArrayList stmts = new ArrayList();
		
		ArrayList lowerCaseColumns = new ArrayList();
        ArrayList columnTypes = new ArrayList();
        ArrayList columnRequireds = new ArrayList();
		PoolDBUtils con = null;
		try {
			con = new PoolDBUtils();
			XFTTable t = con.executeSelectQuery("select LOWER(attname) as col_name,typname, attnotnull from pg_attribute, pg_class,pg_type where attrelid = pg_class.oid AND atttypid=pg_type.oid AND attnum>0 and LOWER(relname) = '" + e.getSQLName().toLowerCase() +"';",e.getDbName(),null);
			while (t.hasMoreRows())
			{
				t.nextRow();
				lowerCaseColumns.add(t.getCellValue("col_name").toString().toLowerCase());
                
                String type = t.getCellValue("typname").toString().toLowerCase();
                if (type.equals("int4"))
                {
                    columnTypes.add("integer");
                }else if (type.equals("float8"))
                {
                    columnTypes.add("float");
                }else{
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
		
        ArrayList matched= new ArrayList();
		try {
			String s = "ALTER TABLE " + e.getSQLName() + " ";
			Iterator iter = e.getAllFieldsWAddIns(false,true).iterator();
			TypeConverter converter = new TypeConverter(new PGSQLMapping(e.getWrapped().getSchemaPrefix()));
			while (iter.hasNext())
			{
				GenericWrapperField field = (GenericWrapperField)iter.next();
				if (field.isReference())
				{
					if ((field.isMultiple() && field.getRelationType().equalsIgnoreCase("single") && field.getXMLType().getFullForeignType().equalsIgnoreCase(e.getFullXMLName())) || (!field.isMultiple()))
					{
						try {
							XFTReferenceI ref = field.getXFTReference();
							if (! ref.isManyToMany())
							{
								Iterator specs = ((XFTSuperiorReference)ref).getKeyRelations().iterator();
								while (specs.hasNext())
								{
									XFTRelationSpecification spec = (XFTRelationSpecification)specs.next();
								
									if(!lowerCaseColumns.contains(spec.getLocalCol().toLowerCase()))
									{
									    if (XFT.VERBOSE)System.out.println("WARNING: Database column " + e.getSQLName() +"." + spec.getLocalCol()+" is missing. Execute update sql.");
										if (spec.getLocalKey() != null)
										{
											if (spec.getLocalKey().getAutoIncrement().equalsIgnoreCase("true"))
											{
												if (spec.getLocalKey().isRequired())
												{
													stmts.add(s+ " ADD COLUMN " + spec.getLocalCol() +" serial NOT NULL");
												}else{
													stmts.add(s + " ADD COLUMN " +  spec.getLocalCol() +" serial");
												}
												
											}else
											{
												String temp = s + " ADD COLUMN " +  spec.getLocalCol();
												if (spec.getForeignKey() != null)
												{
													temp += " " +spec.getForeignKey().getType(converter);
												}else
												{
													temp += " " +converter.convert(spec.getSchemaType().getFullLocalType());
												}
												
												if (spec.getLocalKey().isRequired())
												{
													temp += " NOT NULL ";
												}
												
												stmts.add(temp +";");
											}
											
										}else{
											String temp = s + " ADD COLUMN " +  spec.getLocalCol();
											if (spec.getForeignKey() != null)
											{
												temp += " " +spec.getForeignKey().getType(converter);
											}else
											{
												temp += " " +converter.convert(spec.getSchemaType().getFullLocalType());
											}
											
											stmts.add(temp +";");
										}	
									}else if(!e.getName().endsWith("_history")){
                                        String fieldSQLName=spec.getLocalCol();
                                        matched.add(fieldSQLName);
                                        int index = lowerCaseColumns.indexOf(fieldSQLName.toLowerCase());
                                        String t = (String)columnTypes.get(index);
                                        String req = (String)columnRequireds.get(index);
                                        boolean exptR= false;
                                        String exptType = null;
                                        if (spec.getLocalKey() != null)
                                        {
                                            if (spec.getLocalKey().getAutoIncrement().equalsIgnoreCase("true"))
                                            {
                                                if (spec.getLocalKey().isRequired())
                                                {
                                                    exptR=true;
                                                }
                                                
                                                exptType=t;
                                            }else
                                            {
                                                String temp = s + " ADD COLUMN " +  spec.getLocalCol();
                                                if (spec.getForeignKey() != null)
                                                {
                                                    exptType=spec.getForeignKey().getType(converter);
                                                }else
                                                {
                                                    exptType=converter.convert(spec.getSchemaType().getFullLocalType());
                                                }
                                                
                                                if (spec.getLocalKey().isRequired())
                                                {
                                                    exptR=true;
                                                }
                                                
                                            }
                                            
                                        }else{
                                            if (spec.getForeignKey() != null)
                                            {
                                                exptType=spec.getForeignKey().getType(converter);
                                            }else
                                            {
                                                exptType=converter.convert(spec.getSchemaType().getFullLocalType());
                                            }
                                            
                                        }   

                                        if (exptType.indexOf("(")!=-1)
                                            exptType = exptType.substring(0,exptType.indexOf("("));
                                        
                                        if (!t.equalsIgnoreCase(exptType))
                                        {
                                            //COLUMN TYPE MIS-MATCH
                                            if (XFT.VERBOSE)System.out.println("WARNING: Database column " + e.getSQLName() +"." + fieldSQLName+" type mis-match ('" + exptType + "'!='" + t + "'). Unable to resolve.");
                                        }
                                        
                                        if (exptR)
                                        {
                                            if (req.equals("false"))
                                            {
//                                                if (XFT.VERBOSE)System.out.println("WARNING: Database column " + e.getSQLName() +"." + fieldSQLName+" is now required. Uncomment line in update sql to fix.");
//                                                String temp ="\n--Database column " + e.getSQLName() +"." + fieldSQLName+" is now required.\n";
//                                                temp += "--" + s + " ALTER COLUMN " +  fieldSQLName  + " SET NOT NULL";                                    
//                                                stmts.add(temp +";");
                                            }
                                        }else{
                                            if (req.equals("true"))
                                            {
                                                if (XFT.VERBOSE)System.out.println("WARNING: Database column " + e.getSQLName() +"." + fieldSQLName+" is no longer required. Execute update sql.");
                                                String temp="\n--Database column " + e.getSQLName() +"." + fieldSQLName+" is no longer required.\n";
                                                temp += s + " ALTER COLUMN " +  fieldSQLName  + " DROP NOT NULL";                                    
                                                stmts.add(temp +";");
                                            }
                                        }
                                    }
								
								}
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}else
				{
					if (GenericWrapperField.IsLeafNode(field.getWrapped()))
					{
					    String fieldSQLName = field.getSQLName().toLowerCase();
						if(!lowerCaseColumns.contains(fieldSQLName))
						{
                            if (XFT.VERBOSE)System.out.println("WARNING: Database column " + e.getSQLName() +"." + fieldSQLName+" is missing. Execute update sql.");
    						String temp = s + " ADD COLUMN " +  fieldSQLName;
    						if (field.getAutoIncrement().equalsIgnoreCase("true"))
    						{
    							temp += " serial";
    							if (field.isRequired())
    							{
    								temp +=" NOT NULL ";
    							}
    //						if (addedSequence)
    //						{
    //							sb.append("DEFAULT nextval('").append(sequenceName).append("'::text) ");
    //						}
    							//sb.append(" serial").append(" DEFAULT nextval('" + input.getSQLName() + "_" + field.getSQLName() + "_seq') ");
    							//sb = (new StringBuffer()).append("CREATE SEQUENCE " + input.getSQLName() + "_" + field.getSQLName() + "_seq;\n\n").append(sb.toString());							
    						}else
    						{
    							if (field.getType(converter) != "")
    							{
    								temp += " " +field.getType(converter);
    							}else
    							{
    								temp += " " +converter.convert(e.getWrapped().getSchemaPrefix()+":string",50) + "(255) ";
    							}
    							if (field.isRequired())
    							{
    								temp += " NOT NULL ";
    							}
    						}
    						stmts.add(temp +";");
						}else if(!e.getName().endsWith("_history")){
                            matched.add(fieldSQLName);
						    int index = lowerCaseColumns.indexOf(fieldSQLName);
                            String t = (String)columnTypes.get(index);
                            String req = (String)columnRequireds.get(index);
                            
                            String exptType = null;
                            if (field.getType(converter) != "")
                            {
                                exptType=field.getType(converter);
                            }else
                            {
                                exptType=converter.convert(e.getWrapped().getSchemaPrefix()+":string",50);
                            }
                            
                            if (exptType.indexOf("(")!=-1)
                                exptType = exptType.substring(0,exptType.indexOf("("));
                            
                            if (!t.equalsIgnoreCase(exptType))
                            {
                                //COLUMN TYPE MIS-MATCH
                                
                                try {
                                    String query = "SELECT count(" + fieldSQLName + ") AS value_count FROM " + e.getSQLName();
                                    if (t.equalsIgnoreCase("text") || t.equalsIgnoreCase("varchar") || t.equalsIgnoreCase("bytea"))
                                        query += " WHERE " + fieldSQLName + " IS NOT NULL AND " + fieldSQLName + " !=''";
                                    Number values  = (Number)org.nrg.xft.db.PoolDBUtils.ReturnStatisticQuery(query + ";", "value_count", e.getDbName(), "system");
                                    
                                    query = "SELECT relname, attname, COUNT(conname) AS value_count FROM ("
                                        + " SELECT pg_constraint.oid, conname, contype, tb.relname, pg_attribute.attname FROM pg_constraint, pg_class tb, pg_attribute WHERE conrelid = tb.oid AND ((conrelid=pg_attribute.attrelid AND pg_attribute.attnum=ANY(conkey)))"
                                        + " UNION"
                                        + " SELECT pg_constraint.oid, conname, contype, fk.relname, pg_attribute.attname FROM pg_constraint, pg_class fk, pg_attribute WHERE confrelid = fk.oid AND ((confrelid=pg_attribute.attrelid AND pg_attribute.attnum=ANY(confkey)))"
                                        + " ) SEARCH WHERE relname='" + e.getSQLName().toLowerCase() + "' AND attname='" + fieldSQLName + "' GROUP BY relname, attname";
                                    Number constraints =(Number)org.nrg.xft.db.PoolDBUtils.ReturnStatisticQuery(query + ";", "value_count", e.getDbName(), "system");
                                    String temp="";
                                    temp += "\n--Database column " + e.getSQLName() +"." + fieldSQLName+" type mis-match ('" + exptType + "'!='" + t + "').\n";
                                    
                                    if (values ==null)
                                    {
                                        values = new Integer(0);
                                    }
                                    if (constraints ==null)
                                    {
                                        constraints = new Integer(0);
                                    }
                                    if (values.intValue()>0 || constraints.intValue() > 0)
                                    {
                                        if (XFT.VERBOSE)System.out.println("WARNING: Database column " + e.getSQLName() +"." + fieldSQLName+" type mis-match ('" + exptType + "'!='" + t + "'). Uncomment appropriate lines in update sql to resolve.");
                                        temp += "----Unable to resolve type mis-match for the following reason(s).\n";
                                        temp += "----Please review these factors before uncommenting this code.\n";
                                        if(values.intValue()>0)
                                            temp += "----" + values.intValue() + " row(s) contain values.\n";
                                        if(constraints.intValue()>0)
                                            temp += "----" + constraints.intValue() + " column constraint(s).\n";
                                        
                                        temp += "----Fix " + e.getSQLName() + "_history table.\n";
                                        temp += "--ALTER TABLE " + e.getSQLName() + "_history ADD COLUMN " + fieldSQLName + "_cp " + t +";\n";
                                        temp += "--UPDATE " + e.getSQLName() + "_history SET " + fieldSQLName + "_cp=" + fieldSQLName + ";\n";
                                        temp += "--ALTER TABLE " + e.getSQLName() + "_history DROP COLUMN " + fieldSQLName + ";\n";
                                        temp += "--ALTER TABLE " + e.getSQLName() + "_history ADD COLUMN " + fieldSQLName + " " + exptType + ";\n";
                                        if (t.equalsIgnoreCase("BYTEA"))
                                        {
                                            temp += "--UPDATE " + e.getSQLName() + "_history SET " + fieldSQLName + "=ENCODE(" + fieldSQLName + "_cp,'escape');\n";
                                        }else{
                                            temp += "--UPDATE " + e.getSQLName() + "_history SET " + fieldSQLName + "=CAST(" + fieldSQLName + "_cp AS " + exptType + ");\n";
                                        }
                                        
                                        temp +="--CREATE OR REPLACE FUNCTION after_update_"
                                                + e.getSQLName() + "()";
                                        temp +="  RETURNS TRIGGER AS";
                                        temp +=" '";
                                        temp +="    begin";
                                        temp +="        RETURN NULL;";
                                        temp +="     end;";
                                        temp +=" '";
                                        temp +="   LANGUAGE 'plpgsql' VOLATILE;\n";
                                        
                                        temp += "----Fix " + e.getSQLName() + " table.\n";
                                        temp += "--ALTER TABLE " + e.getSQLName() + " ADD COLUMN " + fieldSQLName + "_cp " + t +";\n";
                                        temp += "--UPDATE " + e.getSQLName() + " SET " + fieldSQLName + "_cp=" + fieldSQLName + ";\n";
                                        temp += "--ALTER TABLE " + e.getSQLName() + " DROP COLUMN " + fieldSQLName + ";\n";
                                        temp += "--ALTER TABLE " + e.getSQLName() + " ADD COLUMN " + fieldSQLName + " " + exptType + ";\n";
                                        if (t.equalsIgnoreCase("BYTEA"))
                                        {
                                            temp += "--UPDATE " + e.getSQLName() + " SET " + fieldSQLName + "=ENCODE(" + fieldSQLName + "_cp,'escape');\n";
                                        }else{
                                            temp += "--UPDATE " + e.getSQLName() + " SET " + fieldSQLName + "=CAST(" + fieldSQLName + "_cp AS " + exptType + ");\n";
                                        }
                                    }else{
                                        if (XFT.VERBOSE)System.out.println("WARNING: Database column " + e.getSQLName() +"." + fieldSQLName+" type mis-match ('" + exptType + "'!='" + t + "'). Uncomment appropriate lines in update sql to resolve.");
                                        temp += "--Existing column has no values or constraints.\n";
                                        query = "SELECT count(" + fieldSQLName + ") AS value_count FROM " + e.getSQLName() + "_history";
                                        if (t.equalsIgnoreCase("text") || t.equalsIgnoreCase("varchar") || t.equalsIgnoreCase("bytea"))
                                            query += " WHERE " + fieldSQLName + " IS NOT NULL AND " + fieldSQLName + " !=''";
                                        values  = (Number)org.nrg.xft.db.PoolDBUtils.ReturnStatisticQuery(query + ";", "value_count", e.getDbName(), "system");
                                        
                                        temp += "--Fix " + e.getSQLName() + "_history table.\n";
                                        temp += "ALTER TABLE " + e.getSQLName() + "_history DROP COLUMN " + fieldSQLName + ";\n";
                                        temp += "ALTER TABLE " + e.getSQLName() + "_history ADD COLUMN " + fieldSQLName + " " + exptType + ";\n";
                                        temp += "--Fix " + e.getSQLName() + " table.\n";
                                        temp += "ALTER TABLE " + e.getSQLName() + " DROP COLUMN " + fieldSQLName + ";\n";
                                        temp += "ALTER TABLE " + e.getSQLName() + " ADD COLUMN " + fieldSQLName + " " + exptType + ";\n";
                                        
                                    }
                                    stmts.add(temp);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    logger.error("",e1);
                                }
                            }
                            
                            if (field.isRequired())
                            {
                                if (req.equals("false"))
                                {
                                    if (XFT.VERBOSE)System.out.println("WARNING: Database column " + e.getSQLName() +"." + fieldSQLName+" is now required. Uncomment line in update sql to fix.");
                                    String temp="\n--Database column " + e.getSQLName() +"." + fieldSQLName+" is now required.\n";
                                    temp += "--" + s + " ALTER COLUMN " +  fieldSQLName  + " SET NOT NULL";                                    
                                    stmts.add(temp +";");
                                }
                            }else{
                                if (req.equals("true"))
                                {
                                    if (XFT.VERBOSE)System.out.println("WARNING: Database column " + e.getSQLName() +"." + fieldSQLName+" is no longer required. Execute update sql.");
                                    String temp="\n--Database column " + e.getSQLName() +"." + fieldSQLName+" is no longer required.\n";
                                    temp += s + " ALTER COLUMN " +  fieldSQLName  + " DROP NOT NULL";                                    
                                    stmts.add(temp +";");
                                }
                            }
                        }
					}
				}
			}
            
            if(!e.getName().endsWith("_history"))
            {
                for(int i=0;i<lowerCaseColumns.size();i++){
                    if (!matched.contains(lowerCaseColumns.get(i))){
                        String fieldSQLName = (String)lowerCaseColumns.get(i);
                        
                        String req = (String)columnRequireds.get(i);
                        if (req.equalsIgnoreCase("true"))
                        {
                            if (XFT.VERBOSE)System.out.println("WARNING: Required database column " + e.getSQLName() +"." + fieldSQLName+" is no longer valid. Execute update sql.");
                            String temp="\n--Required database column " + e.getSQLName() +"." + fieldSQLName+" is no longer in valid.\n";
                            stmts.add(s + " ALTER COLUMN " +  fieldSQLName  + " DROP NOT NULL;");  
                        }
                    }
                }
            }
		} catch (ElementNotFoundException e1) {
			e1.printStackTrace();
		} catch (XFTInitException e1) {
			e1.printStackTrace();
		}
		
		return stmts;
	}
	
	public static ArrayList GetUpdatedAlterStatements(GenericWrapperElement e)
	{
		return new ArrayList();
	}
	
	
	public static void main(String args[]) {
		if (args.length == 2){
			try {
				XFT.init(args[0],true);
				SQLUpdateGenerator.generateDoc(args[1]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else
		{
			System.out.println("Arguments: <Schema File location>");
			return;
		}
	}
}

