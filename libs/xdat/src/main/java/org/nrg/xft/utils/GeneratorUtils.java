/*
 * core: org.nrg.xft.utils.GeneratorUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.utils;
import java.io.File;

import org.nrg.xft.XFT;
import org.nrg.xft.generators.SQLCreateGenerator;
import org.nrg.xft.generators.TorqueSchemaGenerator;
/**
 * @author Tim
 */
public class GeneratorUtils {
	public static void GenerateDocs(String sourceFile)
	{
		File file = new File(sourceFile);
		String sourceDir = file.getParent();
		try {
			XFT.init(sourceFile);
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

