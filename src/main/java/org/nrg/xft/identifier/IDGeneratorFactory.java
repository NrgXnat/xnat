// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.identifier;

import org.apache.log4j.Logger;

public class IDGeneratorFactory {
	static org.apache.log4j.Logger logger = Logger.getLogger(IDGeneratorFactory.class);
	
	public static IDGeneratorI GetIDGenerator(String classname){
		try {
			Class c= Class.forName(classname);
			return (IDGeneratorI)c.newInstance();
		} catch (ClassNotFoundException e) {
			logger.error(e);
			return null;
		} catch (InstantiationException e) {
			logger.error(e);
			return null;
		} catch (IllegalAccessException e) {
			logger.error(e);
			return null;
		}
	}
}
