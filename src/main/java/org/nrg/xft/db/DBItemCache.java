//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Oct 17, 2005
 *
 */
package org.nrg.xft.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.nrg.xft.XFT;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.security.UserI;

/**
 * @author Tim
 *
 */
public class DBItemCache {
    private ItemCollection saved = new ItemCollection();
    private ItemCollection removed = new ItemCollection();
    private ItemCollection preexisting = new ItemCollection();
    private ItemCollection modified = new ItemCollection();


    private ArrayList<String> sql = new ArrayList<String>();

    /**
     *
     */
    public DBItemCache() {
        super();
    }

    /**
     * @param al
     */
    public DBItemCache(ArrayList al) {
        saved.addAll(al);
    }

    public void addStatement(String query)
    {
        if (!query.endsWith(";"))
        {
            query += ";";
        }

        sql.add("\n" + query);
    }

    public ArrayList<String> getStatements()
    {
        return sql;
    }

    public String getSQL()
    {
        StringBuffer sb = new StringBuffer();
        Iterator iter = sql.iterator();
        while (iter.hasNext())
        {
            sb.append(iter.next());
        }
        return sb.toString();
    }

    public void reset()
    {
        this.sql = new ArrayList();

        saved.clear();
        removed.clear();
        preexisting.clear();
    }

    public String toString()
    {
        return this.sql.toString();
    }
    /**
     * @return Returns the preexisting.
     */
    public ItemCollection getPreexisting() {
        return preexisting;
    }
    /**
     * @param preexisting The preexisting to set.
     */
    public void setPreexisting(ItemCollection preexisting) {
        this.preexisting = preexisting;
    }
    /**
     * @return Returns the removed.
     */
    public ItemCollection getRemoved() {
        return removed;
    }
    /**
     * @param removed The removed to set.
     */
    public void setRemoved(ItemCollection removed) {
        this.removed = removed;
    }
    /**
     * @return Returns the saved.
     */
    public ItemCollection getSaved() {
        return saved;
    }
    /**
     * @param saved The saved to set.
     */
    public void setSaved(ItemCollection saved) {
        this.saved = saved;
    }

    public void store(String fileName, String dbName,UserI user) throws Exception
    {
       XFT.LogInsert(getSQL(),fileName);
 	   if (!getSQL().equals("") && !getSQL().equals("[]"))
 	   {
 		   PoolDBUtils con = null;
 			try {
 				con = new PoolDBUtils();
 				String username = null;
 				if (user != null)
 				{
 				    username = user.getUsername();
 				}
 				con.sendBatch(this,dbName,username);
 			} catch (SQLException e) {
 				throw e;
 			} catch (Exception e) {
 				throw e;
 			}
 	   }
    }

    /**
     * @return the modified
     */
    public ItemCollection getModified() {
        return modified;
    }

    /**
     * @param modified the modified to set
     */
    public void setModified(ItemCollection modified) {
        this.modified = modified;
    }

    public void prependStatments(ArrayList<String> new_statements){
    	this.sql.addAll(0, new_statements);
    }

    public void appendStatments(ArrayList<String> new_statements){
    	this.sql.addAll(new_statements);
    }
}
