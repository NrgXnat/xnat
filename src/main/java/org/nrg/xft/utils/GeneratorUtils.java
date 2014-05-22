//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Apr 20, 2004
 */
package org.nrg.xft.utils;
import org.nrg.xft.XFT;
import org.nrg.xft.generators.SQLCreateGenerator;
import org.nrg.xft.generators.TorqueSchemaGenerator;

import java.io.File;
/**
 * @author Tim
 */
public class GeneratorUtils {
	public static void GenerateDocs(String sourceFile)
	{
		File file = new File(sourceFile);
		String sourceDir = file.getParent();
		try {
			XFT.init(file.toURI(), true);
			SQLCreateGenerator.generateDoc(sourceDir + File.separator + "createDB.sql");
			TorqueSchemaGenerator.generateDoc(sourceDir + File.separator + "base-schema.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		if (args.length != 1){
			if(XFT.VERBOSE)System.out.println("Arguments: <Source Schema>");
			return;
		}
		GeneratorUtils.GenerateDocs(args[0]);
	}
}

