package org.nrg.xft.presentation;


public abstract class ItemHistoryBuilder {

	public boolean differBy(FlattenedItemI old, FlattenedItemI _new, String header) {
		final Object oV;
		if(old.getFields().getParams().containsKey(header)){
			oV=old.getFields().getParams().get(header);
		}else{
			oV="";
		}
		
		final Object nV;
		if(_new.getFields().getParams().containsKey(header)){
			nV=_new.getFields().getParams().get(header);
		}else{
			nV="";
		}
		if(oV.equals(nV)){
			return false;
		}else{
			return true;
		}
	}

}