//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Apr 14, 2004
 */
package org.nrg.xft.generators;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xft.XFT;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.references.XFTManyToManyReference;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperFactory;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.utils.CollectionUtils;
import org.nrg.xft.utils.FileUtils;
/**
 * Generater class that is used to output all of the SQL needed to create the
 * database, including CREATE, ALTER, VIEW, AND INSERT statements.
 * 
 * @author Tim
 */
public class SQLCreateGenerator {
	static org.apache.log4j.Logger logger = Logger.getLogger(SQLCreateGenerator.class);
	
	/**
	 * outputs all of the SQL needed to create the database, including CREATE,
	 * ALTER, VIEW, AND INSERT statements.
	 * @param location
	 */
	public static void generateDoc(String location) throws Exception
	{
		try {
		    StringBuffer sb = new StringBuffer();
		    Iterator iter = GetSQLCreate().iterator();
		    while (iter.hasNext())
		    {
		        sb.append(iter.next() +"\n");
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
		ArrayList delete = new ArrayList();
		ArrayList drops2 = new ArrayList();

		XFTManager manager = XFTManager.GetInstance();
		String defaultDBType = "";
		//sb.append("CREATE DATABASE " + schema.getDbName() + ";\n\n");
		
		GenericWrapperElement XFT_ELEMENT = null;
		if (XFTManager.GetElementTable() != null)
		{
			XFT_ELEMENT = (GenericWrapperElement) GenericWrapperFactory.GetInstance().wrapElement(XFTManager.GetElementTable());
		}
		
		Iterator elements = XFTManager.GetInstance().getOrderedElements().iterator();
		while (elements.hasNext())
		{
			GenericWrapperElement element = (GenericWrapperElement)elements.next();
			if (!(element.getName().equalsIgnoreCase("meta_data") || element.getName().equalsIgnoreCase("history") || element.isSkipSQL()))
			{
				logger.debug("Generating the CREATE sql for '" + element.getDirectXMLName() + "'");
				creates.add("\n\n"+GenericWrapperUtils.GetCreateStatement(element));
		
				//delete.add("\n\nDELETE FROM "+ element.getSQLName()+ ";");
				
				//logger.debug("Generating the ALTER sql for '" + element.getDirectXMLName() + "'");
				Iterator refs = GenericWrapperUtils.GetAlterTableStatements(element).iterator();
				while (refs.hasNext())
				{
					alters.add("\n\n"+refs.next());
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
			logger.debug("Generating the CREATE sql for '" + map.getMappingTable() + "'");
			//delete.add("\n\nDELETE FROM "+ map.getMappingTable() + ";");
			creates.add("\n\n"+GenericWrapperUtils.GetCreateStatement(map));
		}

		mappingTables = XFTReferenceManager.GetInstance().getUniqueMappings().iterator();
		while (mappingTables.hasNext())
		{
			XFTManyToManyReference map = (XFTManyToManyReference)mappingTables.next();
			logger.debug("Generating the ALTER sql for '" + map.getMappingTable() + "'");
			//delete.add("\n\nDELETE FROM "+map.getMappingTable()+";");
			Iterator refs = GenericWrapperUtils.GetAlterTableStatements(map).iterator();
			while (refs.hasNext())
			{
				alters.add("\n\n"+refs.next());
			}
		}			
		
		ArrayList drops = new ArrayList();
		drops.addAll(CollectionUtils.ReverseOrder(drops2));
		
		ArrayList deletes = new ArrayList();
		deletes.addAll(CollectionUtils.ReverseOrder(delete));
		
		ArrayList all = new ArrayList();
		all.add("--CREATE STATEMENTS");
		all.addAll(creates);
		all.add("\n\n--ALTER STATEMENTS");
		all.addAll(alters);
		all.add("\n\n--FUNCTION STATEMENTS");
		all.addAll(GenericWrapperUtils.GetFunctionSQL());
		//String s = drops.toString() + "\n\n--VIEW 2 STATEMENTS" + views2.toString();
		return all;
	}
	
	public static void main(String args[]) {
		if (args.length == 2){
			try {
				XFT.init(args[0],true);
				SQLCreateGenerator.generateDoc(args[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else
		{
			System.out.println("Arguments: <Schema File location>");
			return;
		}
	}
}

