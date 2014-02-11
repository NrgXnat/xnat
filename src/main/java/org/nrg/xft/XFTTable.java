//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Oct 21, 2004
 */
package org.nrg.xft;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.utils.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * Generic table structure used to hold tabular data and populate XFTItems.
 * 
 * <BR><BR>This table is constructed out of columns (String[]) and an ArrayList
 * of rows (Object[]).
 * 
 * <BR><BR>The XFTTable can be initialized using a collection of headers (String[]). 
 * Rows can be inserted using the insertRow() method.
 * 
 * <BR><BR>To access information in the rows directly, start by using the resetRowCursor().
 * Then, the hasMoreRows() and nextRow() methods can be used to iterate through the rows
 * in a while loop. The nextRow() method will return the next row (Object[]) or the getCellValue()
 * method can be used to access specific values based on header values.
 * 
 * <BR><BR>Data in the XFTTable can also be accessed using the populateItems() method to populate
 * XFTItems from the rows.  The XFTItems and all of their sub Items will be populated if matching
 * fields are found in the table.  
 * 
 * @author Tim
 */
public class XFTTable implements XFTTableI {
	static Logger logger = Logger.getLogger(XFTTable.class);
	private String [] columns = null;
	private ArrayList<Object[]> rows = null;
	private int numCols = 0;
	private int numRows = 0;
	
	private int rowCursor = 0;
	
	public ArrayList quarantineIndexs = new ArrayList();
		
	public static XFTTable Execute(String query, String dbName, String userName) throws SQLException,DBPoolException{
	    PoolDBUtils con = null;
		XFTTable table = null;
		try {
			con = new PoolDBUtils();
			table = con.executeSelectQuery(query,dbName,userName);
			table.resetRowCursor();
		} catch (DBPoolException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		}
		
		return table;
	}
	
	public XFTTable cloneTable()
	{
	    XFTTable t = new XFTTable();
	    t.setColumns(columns);
	    t.setRows(rows);
	    t.setNumCols(numCols);
	    t.setNumRows(numRows);
	    
	    return t;
	}
	
	/**
	 * Initializes table by setting the columns, number of columns and initialized contents.
	 * @param c
	 */
	public void initTable(String[] c)
	{
		numCols = c.length;	
		columns = c;
		rows = new ArrayList();
	}
	
	/**
	 * Initializes table by setting the columns, number of columns and initialized contents.
	 * @param c
	 */
	public void initTable(ArrayList c)
	{
		numCols = c.size();	
		columns = new String[c.size()];
		Iterator cIter = c.iterator();
		int counter = 0;
		while (cIter.hasNext())
		{
		    String header = (String)cIter.next();
		    columns[counter++]=header;
		}
		rows = new ArrayList();
	}
	
	/**
	 * Initializes table by setting the columns, number of columns and initialized contents.
	 * @param c
	 */
	public void initTable(ArrayList c, ArrayList newRows)
	{
		numCols = c.size();	
		columns = new String[c.size()];
		Iterator cIter = c.iterator();
		int counter = 0;
		while (cIter.hasNext())
		{
		    String header = (String)cIter.next();
		    columns[counter++]=header;
		}
		// Clone passed instance so this reference is different from that in the calling class
		rows = (ArrayList)newRows.clone();
		numRows+=rows.size();
	}
	
	/**
	 * returns all columns
	 * @return
	 */
	public String[] getColumns() {
		return columns;
	}
	
	public Hashtable getColumnNumberHash()
	{
		Hashtable hash = new Hashtable();
		for (int i=0;i<columns.length;i++)
		{
			hash.put(columns[i].toLowerCase(),new Integer(i));
		}
		
		return hash;
	}

	/**
	 * returns number of columns
	 * @return
	 */
	public int getNumCols() {
		return numCols;
	}
	
	/**
	 * returns true if more rows are available
	 * @return
	 */
	public boolean hasMoreRows()
	{
		if (rowCursor >= (numRows))
		{
			return false;
		}else
		{
			return true;
		}
	}
	
	/**
	 * Returns next row and increments row cursor.
	 * @return returns next row.
	 */
	public Object[] nextRow()
	{
		return (Object[])this.rows.get(rowCursor++);
	}
	
	public Hashtable nextRowHash()
	{
		Object[] row = (Object[])this.rows.get(rowCursor++);
		Hashtable rowHash = new Hashtable();
		for (int i=0;i<this.numCols;i++)
		{
			Object v = row[i];
			if (v != null)
			{
				rowHash.put(columns[i],v);
			}
		}
		return rowHash;
	}
	
	/**
	 * If this header is found in the collection of column names, then that index
	 * is used to return the Object from the current row at that index.
	 * @param header
	 * @return
	 */
	public Object getCellValue(String header)
	{
		Integer index = getColumnIndex(header);
		
		if (index != null)
		{
			return ((Object[])this.rows.get(rowCursor -1))[index.intValue()];
		}else
		{
			return null;
		}
	}
	
	public Integer getColumnIndex(String header)
	{
	    Integer index = null;
		try {
            for (int i=0;i<columns.length;i++)
            {
            	if (columns[i].equalsIgnoreCase(header))
            	{
            		index = new Integer(i);
            		break;
            	}
            }
        } catch (RuntimeException e) {
            return null;
        }
		return index;
	}
	
	/**
	 * Inserts row into table and increments row counter.
	 * @param row of Objects
	 */
	public void insertRow(Object[] row)
	{
		this.rows.add(row);
		numRows++;
	}
	
	/**
	 * Resets row cursor to row 0
	 */
	public void resetRowCursor()
	{
		rowCursor = 0;
	}
	
	public ArrayList<Object[]> rows()
	{
	    return rows;
	}
	
	/**
	 * Outputs table headers and contents as a delimited string
	 * @param delimiter
	 * @return
	 */
	public String toString(String delimiter)
	{
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<this.numCols;i++)
		{
			if (i!=0)
			{
				sb.append(delimiter);
			}
			sb.append(this.getColumns()[i]);
		}
		
		resetRowCursor();
		
		while (hasMoreRows())
		{
			Object[] row = nextRow();
			for (int i=0;i<this.numCols;i++)
			{
				if (i!=0)
				{
					sb.append(delimiter);
				}else
				{
					sb.append("\n");
				}
				sb.append(StringUtils.ReplaceStr(StringUtils.ReplaceStr(StringUtils.ReplaceStr(ValueParser(row[i]),delimiter," "),"\n"," "),"\r"," "));
			}
		}
		
		return sb.toString();
	}
	
	public String toHTML(boolean insertTDTags,String lightColor,String darkColor,Hashtable tableProperties,int startCount)
	{
	    if (tableProperties ==null)
	    {
	        tableProperties=new Hashtable();
	    }
		StringBuffer sb = new StringBuffer("<TABLE");
		Enumeration enumer = tableProperties.keys();
		while (enumer.hasMoreElements())
		{
			String key = (String)enumer.nextElement();
			String value = (String)tableProperties.get(key);
			sb.append(" ").append(key).append("=\"").append(value).append("\"");
		}
		
		sb.append(">\n<THEAD>\n<TR class=\"resultsHEADER\">");
		sb.append("<TH> </TH>");
		for (int i=0;i<this.numCols;i++)
		{
			if (insertTDTags)
			{
				sb.append("<TH>").append(this.getColumns()[i]).append("</TH>");
			}else
			{
				sb.append(this.getColumns()[i]);
			}
		}
		sb.append("</TR>\n</THEAD>\n<TBODY ID=\"dataRows\">\n");

		resetRowCursor();

		int color=0;
		while (hasMoreRows())
		{
		    if (isQuarantineRow(getRowCursor() + 1))
		    {
		        sb.append("\n<TR  class=\"quarantine\">");
		    }else if (color==0)
			{
				sb.append("\n<TR class=\"odd\">");
				color = 1;
			}else{
				sb.append("\n<TR class=\"even\">");
				color = 0;
			}
			sb.append("<TD>").append(startCount++).append("</TD>");
			Object[] row = nextRow();
			for (int i=0;i<this.numCols;i++)
			{
				if (i!=0)
				{
				}else
				{
					sb.append("\n");
				}
				if (insertTDTags)
				{
					sb.append("<TD>").append(StringUtils.ReplaceStr(StringUtils.ReplaceStr(ValueParser(row[i]),"\n"," "),"\r"," ")).append("</TD>");
				}else
				{
					sb.append(StringUtils.ReplaceStr(StringUtils.ReplaceStr(ValueParser(row[i]),"\n"," "),"\r"," "));
				}
			}
			sb.append("</TR>");
		}
		sb.append("\n</TBODY>\n</TABLE>");

		return sb.toString();
	}
    
    public void toHTML(boolean insertTDTags,String lightColor,String darkColor,Hashtable tableProperties,int startCount, OutputStream out)
    {
        if (tableProperties ==null)
        {
            tableProperties=new Hashtable();
        }
        boolean alternateColors= true;
        if (lightColor==null || darkColor==null){
            alternateColors=false;
        }
        PrintStream pw = new PrintStream(out,true);
        pw.print("<TABLE");
        Enumeration enumer = tableProperties.keys();
        while (enumer.hasMoreElements())
        {
            String key = (String)enumer.nextElement();
            String value = (String)tableProperties.get(key);
            pw.print(" ");
            pw.print(key);
            pw.print("=\"");
            pw.print(value);
            pw.print("\"");
        }
        
        pw.print(">\n<THEAD>\n<TR style=\"border-style:none;\">");
        pw.print("<TH>&nbsp;</TH>");
        for (int i=0;i<this.numCols;i++)
        {
            if (insertTDTags)
            {
                pw.print("<TH>");
                pw.print(this.getColumns()[i]);
                pw.print("</TH>");
            }else
            {
                pw.print(this.getColumns()[i]);
            }
        }
        pw.print("</TR>\n</THEAD>\n<TBODY ID=\"dataRows\">\n");

        resetRowCursor();

        int color=0;
        while (hasMoreRows())
        {
            if(alternateColors){
                if (isQuarantineRow(getRowCursor() + 1))
                {
                    pw.print("\n<TR class=\"quarantine\">");
                }else if (color==0)
                {
                    pw.print("\n<TR class=\"odd\">");
                    color = 1;
                }else{
                    pw.print("\n<TR class=\"even\">");
                    color = 0;
                }
            }else{
                pw.print("\n<TR>");
            }
            pw.print("<TD>");
            pw.print(startCount++);
            pw.print("</TD>");
            Object[] row = nextRow();
            for (int i=0;i<this.numCols;i++)
            {
                if (i!=0)
                {
                }else
                {
                    pw.print("\n");
                }
                if (insertTDTags)
                {
                    pw.print("<TD>");
                    pw.print(StringUtils.ReplaceStr(StringUtils.ReplaceStr(ValueParser(row[i]),"\n"," "),"\r"," "));
                    pw.print("</TD>");
                }else
                {
                    pw.print(StringUtils.ReplaceStr(StringUtils.ReplaceStr(ValueParser(row[i]),"\n"," "),"\r"," "));
                }
            }
            pw.print("</TR>");
        }
        pw.print("\n</TBODY>\n</TABLE>");
    }

	/**
	 * Outputs table headers and contents into an HTML Table
	 * @param delimiter
	 * @return
	 */
	public String toHTML(boolean insertTDTags)
	{
		StringBuffer sb = new StringBuffer("<TABLE>");
		sb.append("\n<THEAD>\n<TR>");
		for (int i=0;i<this.numCols;i++)
		{
			if (insertTDTags)
			{
				sb.append("<TH>").append(this.getColumns()[i]).append("</TH>");
			}else
			{
				sb.append(this.getColumns()[i]);
			}
		}
		sb.append("</TR>\n</THEAD>\n<TBODY ID=\"dataRows\" STYLE=\"overflow:auto;\">\n");
	
		resetRowCursor();
	
		while (hasMoreRows())
		{
			sb.append("<TR>");
			Object[] row = nextRow();
			for (int i=0;i<this.numCols;i++)
			{
				if (i!=0)
				{
				}else
				{
					sb.append("\n");
				}
				if (insertTDTags)
				{
					sb.append("<TH>").append(StringUtils.ReplaceStr(StringUtils.ReplaceStr(ValueParser(row[i]),"\n"," "),"\r"," ")).append("</TH>");
				}else
				{
					sb.append(StringUtils.ReplaceStr(StringUtils.ReplaceStr(ValueParser(row[i]),"\n"," "),"\r"," "));
				}
			}
			sb.append("</TR>");
		}
		sb.append("</TBODY>\n</TABLE>");
	
		return sb.toString();
	}
	
	/**
	 * Outputs table headers and contents without delimiters
	 * @param delimiter
	 * @return
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		XFTTable temp= cloneTable();
		for (int i=0;i<temp.numCols;i++)
		{
			sb.append(temp.getColumns()[i]);
		}
		
		temp.resetRowCursor();
		
		while (temp.hasMoreRows())
		{
			Object[] row = temp.nextRow();
			for (int i=0;i<temp.numCols;i++)
			{
				if (i!=0)
				{
				}else
				{
					sb.append("\n");
				}
				sb.append(StringUtils.ReplaceStr(StringUtils.ReplaceStr(StringUtils.ReplaceStr(StringUtils.ReplaceStr(ValueParser(row[i]),"\n"," "),"\r"," "),"<","&lt;"),">","&gt;"));
			}
		}
		
		return sb.toString();
	}
//	
//	/**
//	 * returns list of items with all available sub-items populated.
//	 * @param name of schema element
//	 * @return ArrayList of XFTItems
//	 */
//	public ArrayList populateItems(String name) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,Exception
//	{
//		ArrayList al = new ArrayList();
//		
//		resetRowCursor();
//		
//		XFTItem lastItem = null;
//		while (hasMoreRows())
//		{
//			Object[] row = nextRow();
//			XFTItem item = XFTItem.PopulateItemsFromObjectArray(row,this.getColumnNumberHash(),name,"",new ArrayList());
//			if (lastItem == null)
//			{
//				lastItem = item;
//			}else
//			{
//				
//				if (XFTItem.CompareItemsByPKs(lastItem,item))
//				{
//					//duplicate item
//					lastItem = XFTItem.ReconcileItems(lastItem,item);
//				}else
//				{
//					if (lastItem.getPropertyCount() > 0)
//					{
//						al.add(lastItem);
//					}
//					lastItem = item;
//				}
//			}
//		}
//		if ( lastItem != null && lastItem.getPropertyCount() > 0)
//		{
//			al.add(lastItem);
//		}
//		
//		return al;
//	}
//	
	/**
	 * Formats BYTEA type to string
	 * @param o
	 * @return
	 */
	public static String ValueParser(Object o)
	{
		if (o != null)
		{
			if (o.getClass().getName().equalsIgnoreCase("[B"))
			{
				byte[] b = (byte[]) o;
				java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
				try {
					baos.write(b);
				} catch (java.io.IOException e) {
					e.printStackTrace();
				}
				return baos.toString();
			}
			return o.toString();
		}else
		{
			return "";
		}
	}
	/**
	 * @return
	 */
	public int getNumRows() {
		return numRows;
	}
	
	public int size(){
	    return getNumRows();
	}
//	
//	public ItemCollection toItems(String elementName,boolean withChildren)
//	{
//	    try {
//            ItemSearch search = new ItemSearch();
//            return search.populateItems(elementName,this,withChildren,false,true);
//        } catch (ElementNotFoundException e) {
//            logger.error("",e);
//            return new ItemCollection();
//        } catch (XFTInitException e) {
//            logger.error("",e);
//            return new ItemCollection();
//        } catch (FieldNotFoundException e) {
//            logger.error("",e);
//            return new ItemCollection();
//        } catch (Exception e) {
//            logger.error("",e);
//            return new ItemCollection();
//        }
//	}
//	
//	public ItemCollection toItems(String elementName)
//	{
//	    try {
//            ItemSearch search = new ItemSearch();
//            return search.populateItems(elementName,this,true,false,true);
//        } catch (ElementNotFoundException e) {
//            logger.error("",e);
//            return new ItemCollection();
//        } catch (XFTInitException e) {
//            logger.error("",e);
//            return new ItemCollection();
//        } catch (FieldNotFoundException e) {
//            logger.error("",e);
//            return new ItemCollection();
//        } catch (Exception e) {
//            logger.error("",e);
//            return new ItemCollection();
//        }
//	}

	public ArrayList<Hashtable> rowHashs()
	{
	    ArrayList<Hashtable> al = new ArrayList<Hashtable>();
	    this.resetRowCursor();
	    while (this.hasMoreRows())
	    {
	        Hashtable hash = this.nextRowHash();
	        al.add(hash);
	    }
	    return al;
	}
    
    public Map<Object,Object> convertToHashtable(String keyColumn, String valueColumn){
        return convertToMap(keyColumn,valueColumn,new Hashtable<Object,Object>());
    }
    
    public Map<Object,Object> convertToMap(String keyColumn, String valueColumn,Map<Object,Object> al){
        XFTTable t = this.cloneTable();
        
        Integer keyIndex = t.getColumnIndex(keyColumn);
        Integer valueIndex = t.getColumnIndex(valueColumn);
        
        t.resetRowCursor();
        while (t.hasMoreRows())
        {
            Object[] row = t.nextRow();
            
            Object key = row[keyIndex.intValue()];
            Object value = row[valueIndex.intValue()];
            
            
            if (key!=null && value !=null) al.put(key, value);
        }
        
        return al;
    }

	public ArrayList convertColumnToArrayList(String colName)
	{
	    ArrayList al = new ArrayList();

	    XFTTable t = this.cloneTable();
	    
	    Integer index = t.getColumnIndex(colName);
	    
	    t.resetRowCursor();
	    while (t.hasMoreRows())
	    {
	        Object[] row = t.nextRow();
	        Object v = row[index.intValue()];
	        if (v!=null) al.add(v);
	    }
	    
	    al.trimToSize();
	    return al;
	}

	/**
	 * ArrayList of ArrayLists
	 * @param sqlNames
	 * @return
	 */
	public ArrayList convertColumnsToArrayList(ArrayList sqlNames)
	{
	    ArrayList al = new ArrayList();

	    XFTTable t = this.cloneTable();
	    	    
	    t.resetRowCursor();
	    while (t.hasMoreRows())
	    {
	        Object[] row = t.nextRow();
	        Iterator iter = sqlNames.iterator();
	        ArrayList sub = new ArrayList();
	        while(iter.hasNext())
	        {
	            String s = (String)iter.next();
	    	    Integer index = t.getColumnIndex(s);
		        Object v = row[index.intValue()];
		        if (v!=null) 
		            sub.add(v);
		        else
		            sub.add("");
	        }
	        al.add(sub);
	    }
	    
	    al.trimToSize();
	    return al;
	}
	
    /**
     * @param columns The columns to set.
     */
    public void setColumns(String[] columns) {
        this.columns = columns;
    }
    /**
     * @param numCols The numCols to set.
     */
    private void setNumCols(int numCols) {
        this.numCols = numCols;
    }
    /**
     * @param numRows The numRows to set.
     */
    private void setNumRows(int numRows) {
        this.numRows = numRows;
    }
    /**
     * @param rows The rows to set.
     */
    private void setRows(ArrayList rows) {
        this.rows = rows;
    }
    
    
    /**
     * @return Returns the rowCursor.
     */
    public int getRowCursor() {
        return rowCursor;
    }
    
    
    /**
     * Converts each row into a hashtable and inserts them into an ArrayList.
     * @return
     */
    public ArrayList<Hashtable> toArrayListOfHashtables()
    {
        ArrayList<Hashtable> al = new ArrayList<Hashtable>();
        XFTTable t = cloneTable();
        t.resetRowCursor();
	    while (t.hasMoreRows())
	    {
	        Hashtable row = t.nextRowHash();
	        al.add(row);
	    }
	    al.trimToSize();
	    return al;
    }
    
    
    /**
     * Converts each row into a hashtable and inserts them into an ArrayList.
     * @return
     */
    public ArrayList<List> toArrayListOfLists()
    {
        ArrayList<List> al = new ArrayList<List>();
        XFTTable t = cloneTable();
        t.resetRowCursor();
        while (t.hasMoreRows())
        {
            Object[] row = t.nextRow();
            al.add(Arrays.asList(row));
        }
        al.trimToSize();
        return al;
    }
    
    public Hashtable getRowHash(String columnName,Object v)
    {
        XFTTable t = cloneTable();
        t.resetRowCursor();
        
        Hashtable found = null;
	    while (t.hasMoreRows())
	    {
	        Hashtable row = t.nextRowHash();
	        if (row.get(columnName.toLowerCase()).toString().equals(v.toString()))
            {
	            found = row;
	            break;
            }
	    }
	    return found;
    }
    
    public ArrayList getRowHashs(String columnName,Object v)
    {
        XFTTable t = cloneTable();
        t.resetRowCursor();

        ArrayList al = new ArrayList();
	    while (t.hasMoreRows())
	    {
	        Hashtable row = t.nextRowHash();
	        if (row.get(columnName.toLowerCase()).toString().equals(v.toString()))
            {
	            al.add(row);
            }
	    }
	    al.trimToSize();
	    return al;
    }
    
    public Object getFirstObject()
    {
        XFTTable t = cloneTable();
        
        Object[] row = (Object[]) t.rows().get(0);
        return row[0];
    }
    
    public void addQuarantineRow(Integer i)
    {
        this.quarantineIndexs.add(i);
    }

    
    public void addQuarantineRow(int i)
    {
        this.quarantineIndexs.add(new Integer(i));
    }
    
    public boolean isQuarantineRow(int i)
    {
        Integer I = new Integer(i);
        return quarantineIndexs.contains(I);
    }
    
    public void sort(String colname,String order)
    {
        Integer index = getColumnIndex(colname);
        sort(index.intValue(),order);
    }
    
    public void sort(int col, String order)
    {
        Comparator byColumn = new TableRowComparator(col,order);
		Collections.sort(rows,byColumn);
    }
    
    public class TableRowComparator implements Comparator{
		private int col = 0;
		private boolean asc = true;
		public TableRowComparator(int sortColumn,String sortOrder)
		{
		    col = sortColumn;
		    if (sortOrder.equalsIgnoreCase("DESC"))
		    {
		        asc=false;
		    }
		}
		public int compare(Object o1, Object o2) {
			try {
				Comparable value1 = (Comparable)((Object[])o1)[col];
				Comparable value2 = (Comparable)((Object[])o2)[col];
				if (value1 == null){
					if (value2 == null)
					{
						return 0;
					}else{
					    if (asc)
					        return -1;
					    else
					        return 1;
					}
				}
				if (value2== null)
				{
				    if (asc)
				        return 1;
				    else
				        return -1;
				}
				int i =  value1.compareTo(value2);
				if (asc)
				{
				    return i;
				}else{
				    if (i>0)
				    {
				        return -1;
				    }else if (i<0){
				        return 1;
				    }else{
				        return i;
				    }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}
		public void setSortColumn(int s){col = s;}
	}

    
    /* (non-Javadoc)
     * @see org.nrg.xft.XFTTableI#removeRow(int)
     */
    public Object[] removeRow(int rowNumber) throws Exception{
        if (rowNumber>= rows.size()){
            throw new Exception("XFTTable index undefined.");
        }else{
            numRows--;
            return (Object[])rows.remove(rowNumber);
        }
    }
    
    public Hashtable toHashtable(String key, String value){
        XFTTable t = cloneTable();
        t.resetRowCursor();

        Integer keyI=t.getColumnIndex(key);
        Integer valueI=t.getColumnIndex(value);
        
        Hashtable al = new Hashtable();
        while (t.hasMoreRows())
        {
            Object[] row = t.nextRow();
            Object keyV = "";
            Object valueV ="";
            
            if (row[keyI]!=null){
                keyV=row[keyI];
            }
            if (row[valueI]!=null){
                valueV=row[valueI];
            }
            
            al.put(keyV, valueV);
        }
        return al;
    }
    public void toXMLList(Writer w,String title){
    	toXMLList(w,new Hashtable<String,Map<String,String>>(),title);
    }
    
    public void toXMLList(Writer w,Map<String,Map<String,String>> columnProperties,String title){
		try {
			Writer writer = new BufferedWriter(w);
			writer.write("<results");
			if (title !=null)
				writer.write(" title=\"" + title + "\"");
			
			writer.write("><columns>");
			for (int i=0;i<this.numCols;i++)
			{
				writer.write("<column");
				if(columnProperties.get(this.getColumns()[i])!=null){
					Map<String,String> map=columnProperties.get(this.getColumns()[i]);
					for(Map.Entry<String, String> entry: map.entrySet()){
						writer.write(" ");
						writer.write(entry.getKey());
						writer.write("=\"");
						writer.write(entry.getValue());
						writer.write("\"");
					}
				}
				writer.write(">" + this.getColumns()[i] + "</column>");
	
			}
			writer.write("</columns>\n");
			writer.flush();
	
			writer.write("<rows>");
			for (Object[] row:rows)
			{
				writer.write("<row>");
				for (int i=0;i<this.numCols;i++)
				{
					writer.write("<cell>" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(StringUtils.ReplaceStr(StringUtils.ReplaceStr(ValueParser(row[i]),"\n"," "),"\r"," "),">","&gt;"),"<","&lt;") + "</cell>");
					
				}
				writer.write("</row>\n");
				writer.flush();
			}
			writer.write("</rows></results>");
			writer.flush();
		} catch (IOException e) {
			logger.error(e);
		}
    }
    
    public void toCSV(Writer w,Map<String,Map<String,String>> columnProperties,String title){
		try {
			Writer writer = new BufferedWriter(w);
			for (int i=0;i<this.numCols;i++)
			{
				if(i>0)
					writer.write(",");
				writer.write("\"");
				writer.write(this.getColumns()[i]);
				writer.write("\"");
			}
			writer.write("\n");
			writer.flush();
	
			for (Object[] row:rows)
			{
				for (int i=0;i<this.numCols;i++)
				{
					if(i>0)
						writer.write(",");
					if(null!=row[i]){
						if(row[i] instanceof String)writer.write("\"");
						writer.write(StringUtils.ReplaceStr(ValueParser(row[i]),"\"","'"));
						if(row[i] instanceof String)writer.write("\"");		
					}			
				}
				writer.write("\n");
				writer.flush();
			}
			writer.flush();
		} catch (IOException e) {
			logger.error(e);
		}
    }


	public void toJSON(Writer writer) throws IOException
	{
		toJSON(writer,null);
	}
    
	public void toJSON (Writer writer, Map<String,Map<String,String>> cp) throws IOException{
		org.json.JSONArray array = new org.json.JSONArray();
		ArrayList<ArrayList<String>> columnsWType=new ArrayList<ArrayList<String>>();
		for (int i=0;i<this.numCols;i++)
		{
			ArrayList col= new ArrayList();
			col.add(this.getColumns()[i]);
			if(cp.containsKey(col.get(0))){
				Map<String,String> props=cp.get(col.get(0));
				if(props.containsKey("type")){
					col.add(props.get("type"));
				}
			}
			columnsWType.add(col);
		}
		
		for (int j = 0; j<rows.size();j++){
			Object[] row = rows.get(j);
			org.json.JSONObject json = new org.json.JSONObject();
			for (int i = 0; i <this.numCols;i++){
				ArrayList<String> columnSpec=columnsWType.get(i);
				try {
					json.put(columnSpec.get(0), ValueParser(row[i]));
				} catch (JSONException e) {
					e.printStackTrace();
			}
				}
			array.put(json);
		}
							try{
			array.write(writer);
		} catch (JSONException e) {
			e.printStackTrace();
							}
						}

	/**
	 * Outputs table headers and contents into an HTML Table
	 * @param delimiter
	 * @return
	 */
	public void toHTML(boolean insertTDTags,Writer writer) throws IOException
	{
		toHTML(insertTDTags,writer,new Hashtable<String,Map<String,String>>());
	}

	/**
	 * Outputs table headers and contents into an HTML Table
	 * @param delimiter
	 * @return
	 */
	public void toHTML(boolean insertTDTags,Writer writer,Map <String,Map<String,String>> cp) throws IOException
	{
		writer.write("<table class=\"x_rs_t\" cellpadding=\"0\" cellspacing=\"0\">");
		writer.write("\n<thead class=\"x_rs_thead\">\n<tr class=\"x_rs_tr_head\">");
		for (int i=0;i<this.numCols;i++)
		{
			if (insertTDTags)
			{
				writer.write("<th>" + columns[i] + "</th>");
			}else
			{
				writer.write(this.getColumns()[i]);
			}
		}
		writer.write("</tr>\n</thead>\n");
		writer.flush();
		writer.write("<tbody id=\"dataRows\">\n");
	
		int rowC=0;
		if (rows != null) {
            for(Object[] row: rows) {
                writer.write("<tr class=\"x_rs_tr_data");
                if(rowC++% 2 != 0)writer.write(" even");
                else writer.write(" odd");
                writer.write("\">");
                for (int i=0;i<this.numCols;i++)
                {
                    if (i!=0)
                    {
                    }else
                    {
                        writer.write("\n");
                    }

                    String value=StringUtils.ReplaceStr(StringUtils.ReplaceStr(ValueParser(row[i]),"\n"," "),"\r"," ");

                    if(cp !=null &&cp.containsKey(this.getColumns()[i]) && cp.get(this.getColumns()[i]).containsKey("serverRoot"))
                    {
                        value= "<a href='" + cp.get(this.getColumns()[i]).get("serverRoot") + value + "'>" + value + "</a>";
                    }
                    if (insertTDTags)
                    {
                        writer.write("<td>" + value + "</td>");
                    }else
                    {
                        writer.write(value);
                    }
                }
                writer.write("</tr>");
            }
        }
		writer.write("</tbody>\n</table>");
		writer.flush();
	}
	
	public void sort(final List<String> sortColumns){
		final List<Integer> indexes=new ArrayList<Integer>();
		
		for(final String col:sortColumns){
			final Integer i=this.getColumnIndex(col);
			if(i!=null){
				indexes.add(i);
			}
}

		Collections.sort(rows,new Comparator<Object[]>(){
			public int compare(Object[] o1, Object[] o2) {
				for(final Integer i:indexes){
					try {
						//contents could be String, Number or Date
						if(o1[i]==null){
							if(o2[i]==null){
								return 0;
							}else{
								return 1;
							}
						}else if(o2[i]==null){
							return -1;
						}else{
							int c=((Comparable)o1[i]).compareTo(((Comparable)o2[i]));
							if(c!=0){
								return c;
							}
						}
					} catch (ClassCastException e) {
						//ignore non comparables for now.
						logger.error("",e);
					}
				}
				
				return 0;
			}});		
	}
	
	public void reverse(){
		Collections.reverse(rows);
	}
}

