package org.nrg.xdat.bean;

public class ClassMappingFactory {
	private static ClassMappingI mapping=null;
	
	public static ClassMappingI getInstance() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		if(mapping==null){
			mapping=(ClassMappingI)Class.forName("org.nrg.xdat.bean.ClassMapping").newInstance();
		}
		return mapping;
	}
}
