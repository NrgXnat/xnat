package org.nrg.xft.db.loaders;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.references.XFTManyToManyReference;
import org.nrg.xft.references.XFTMappingColumn;
import org.nrg.xft.references.XFTReferenceI;
import org.nrg.xft.references.XFTRelationSpecification;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;

public class XFTItemDBLoader {
	static org.apache.log4j.Logger logger = Logger.getLogger(XFTItemDBLoader.class);

	private final XFTItem i;
	private final String id;
	private final ItemCache cache;

	public static class ItemCache{
		Map<String,Map<String,ItemCollection>> cache=new Hashtable<String,Map<String,ItemCollection>>();
		public ItemCollection getDBChildren(String id, String type,String many){
			if(cache.get(id)!=null){
				Map<String,ItemCollection> map=cache.get(id);
				return map.get(type+many);
			}
			
			return null;
		}
		public ItemCollection setDBChildren(String id, String type,String many,ItemCollection items){
			if(cache.get(id)==null){
				cache.put(id,new TreeMap<String,ItemCollection>());
			}
			cache.get(id).put(type+many,items);
			return items;
		}
	}
	
	public XFTItemDBLoader(final XFTItem i, ItemCache cache) {
		this.i = i;
		this.id=XFTItem.identity(i);
		this.cache=cache;
	}

	public ItemCollection getMultiDBChildren(GenericWrapperElement foreign, XFTManyToManyReference many, String mapping_name, UserI user, boolean allowMultiples) {
		if(cache!=null){
			ItemCollection temp=cache.getDBChildren(id,foreign.getXSIType(), many.toString());
			if(temp!=null){
				return temp;
			}
		}
		try {
			boolean nullKey = false;
			String query = "SELECT * FROM " + mapping_name;
			Iterator<?> iter = many.getMappingColumns().iterator();
			int counter = 0;
			while (iter.hasNext()) {
				XFTMappingColumn map = (XFTMappingColumn) iter.next();
				if (map.getForeignElement().ignoreHistory().getFormattedName().equalsIgnoreCase(i.getGenericSchemaElement().ignoreHistory().getFormattedName())) {
					Object o = i.getProperty(map.getForeignKey().getXMLPathString());
					if (o == null) {
						nullKey = true;
						break;
					} else {
						if (counter++ == 0) {
							query += " WHERE " + map.getLocalSqlName() + XFTItem.EQUALS + DBAction.ValueParser(o, map.getXmlType().getLocalType(), true);
						} else {
							query += " AND " + map.getLocalSqlName() + XFTItem.EQUALS + DBAction.ValueParser(o, map.getXmlType().getLocalType(), true);
						}
					}
				}
			}
			query += ";";

			if (nullKey) {
				return cacheDBChildren(id,foreign.getXSIType(), many, new ItemCollection());
			} else {
				String login = null;
				if (user != null) {
					login = user.getUsername();
				}

				XFTTable table = TableSearch.Execute(query, foreign.getDbName(), login);
				if (table.size() > 0) {
					ItemSearch search = new ItemSearch(user, foreign);

					CriteriaCollection col = new CriteriaCollection("OR");

					table.resetRowCursor();
					while (table.hasMoreRows()) {
						Hashtable<?, ?> hash = table.nextRowHash();
						CriteriaCollection subCol = new CriteriaCollection("AND");
						iter = many.getMappingColumns().iterator();
						while (iter.hasNext()) {
							XFTMappingColumn map = (XFTMappingColumn) iter.next();

							if (map.getForeignElement().ignoreHistory().getFormattedName().equalsIgnoreCase(foreign.ignoreHistory().getFormattedName())) {
								Object o = hash.get(map.getLocalSqlName().toLowerCase());

								if (o == null) {
									nullKey = true;
									break;
								} else {
									subCol.addClause(map.getForeignKey().getXMLPathString(foreign.getFullXMLName()), o);
								}
							}
						}
						col.add(subCol);
					}
					search.add(col);

					if (nullKey) {
						return cacheDBChildren(id,foreign.getXSIType(), many, new ItemCollection());
					}

					return cacheDBChildren(id,foreign.getXSIType(), many, search.exec(allowMultiples));
				} else {
					return cacheDBChildren(id,foreign.getXSIType(), many, new ItemCollection());
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}
	
	public ItemCollection cacheDBChildren(String id, String type, XFTReferenceI ref, ItemCollection items){
		if(cache!=null){
			cache.setDBChildren(id,type,ref.toString(), items);
		}
		return items;
	}
	

	public ItemCollection getSingleDBChildren(GenericWrapperElement foreign, XFTSuperiorReference sup, UserI user, boolean allowMultiples, boolean preventLoop) {
		if(cache!=null){
			ItemCollection temp=cache.getDBChildren(id,foreign.getXSIType(), sup.toString());
			if(temp!=null){
				return temp;
			}
		}
		try {
			ItemSearch search = new ItemSearch(user, foreign);
			if (preventLoop) {
				search.setPreventLoop(true);
			}
			boolean nullKey = false;
			Iterator<?> keys = sup.getKeyRelations().iterator();
			while (keys.hasNext()) {
				XFTRelationSpecification spec = (XFTRelationSpecification) keys.next();
				final String foreignPath;
				final String localColumn;
				if(spec.getForeignTable().equals(i.getGenericSchemaElement().ignoreHistory().getSQLName())){
					localColumn=spec.getForeignCol();
					foreignPath=spec.getLocalXMLPath();
				}else{
					localColumn=spec.getLocalCol();
					foreignPath=spec.getForeignKey().getXMLPathString();
				}
				Object localValue = i.getProperty(localColumn);
				if (localValue == null) {
					nullKey = true;
					break;
				} else {
					search.addCriteria(foreign.getFullXMLName() + XFT.PATH_SEPERATOR + foreignPath, localValue);
				}
			}

			if (nullKey) {
				return cacheDBChildren(id,foreign.getXSIType(), sup, new ItemCollection());
			}

			return cacheDBChildren(id,foreign.getXSIType(), sup,search.exec(allowMultiples));

		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}

	public ItemCollection getCurrentDBChildren(GenericWrapperField refField, UserI user, boolean allowMultiples, boolean includeHistory) {
		try {
			GenericWrapperElement foreign = (GenericWrapperElement) refField.getReferenceElement();
			XFTReferenceI ref = refField.getXFTReference();
			if (ref.isManyToMany()) {
				XFTManyToManyReference many = (XFTManyToManyReference) ref;
				ItemCollection col = getMultiDBChildren(foreign, many, many.getMappingTable(), user, allowMultiples);
				if (includeHistory) {
					ItemCollection col_history = getMultiDBChildren(GenericWrapperElement.GetElement(foreign.getXSIType()), many, many.getMappingTable() + "_history", user, allowMultiples);
					col.merge(col_history);

					ItemCollection col_history2 = getMultiDBChildren(GenericWrapperElement.GetElement(foreign.getXSIType() + "_history"), many, many.getMappingTable() + "_history", user,	allowMultiples);
					col.merge(col_history2);
				}
				return col;
			} else {
				ItemCollection col = getSingleDBChildren(foreign, (XFTSuperiorReference) ref, user, allowMultiples, refField.getPreventLoop());
				if (includeHistory) {
					ItemCollection col_history = getSingleDBChildren(GenericWrapperElement.GetElement(foreign.getXSIType() + "_history"), (XFTSuperiorReference) ref, user, allowMultiples, refField.getPreventLoop());
					col.merge(col_history);
				}
				return col;
			}
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}
}
