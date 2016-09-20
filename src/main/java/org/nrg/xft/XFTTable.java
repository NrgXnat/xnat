/*
 * core: org.nrg.xft.XFTTable
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.DBPoolException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"unchecked", "unused"})
public class XFTTable implements XFTTableI {
    private static final Logger logger = LoggerFactory.getLogger(XFTTable.class);
    private String[] columns = null;
    private ArrayList<Object[]> rows = null;
    private int numCols = 0;
    private int numRows = 0;

    private int rowCursor = 0;
    public ArrayList quarantineIndexs = new ArrayList();

    public static XFTTable Execute(String query, String dbName, String userName) throws SQLException, DBPoolException {
        final PoolDBUtils con = new PoolDBUtils();
        final XFTTable table = con.executeSelectQuery(query, dbName, userName);
        table.resetRowCursor();
        return table;
    }

    public XFTTable cloneTable() {
        XFTTable t = new XFTTable();
        t.setColumns(columns);
        t.setRows(rows);
        t.setNumCols(numCols);
        t.setNumRows(numRows);

        return t;
    }

    /**
     * Initializes table by setting the columns, number of columns and initialized contents.
     *
     * @param columns The columns to set for the table.
     */
    public void initTable(final String[] columns) {
        numCols = columns.length;
        this.columns = columns;
        rows = new ArrayList();
    }

    /**
     * Initializes table by setting the columns, number of columns and initialized contents.
     *
     * @param columns The columns to set for the table.
     */
    public void initTable(final ArrayList columns) {
        numCols = columns.size();
        this.columns = new String[columns.size()];
        int counter = 0;
        for (final Object object : columns) {
            String header = (String) object;
            this.columns[counter++] = header;
        }
        rows = new ArrayList();
    }

    /**
     * Initializes table by setting the columns, number of columns and initialized contents.
     *
     * @param columns The columns to set for the table.
     */
    public void initTable(ArrayList columns, ArrayList newRows) {
        initTable(columns);
        // Clone passed instance so this reference is different from that in the calling class
        rows.addAll(newRows);
        numRows += rows.size();
    }

    /**
     * Returns all column names.
     *
     * @return All of the column names in the table as an array of strings.
     */
    public String[] getColumns() {
        return columns;
    }

    @SuppressWarnings("unused")
    public Hashtable getColumnNumberHash() {
        Hashtable hash = new Hashtable();
        for (int i = 0; i < columns.length; i++) {
            hash.put(columns[i].toLowerCase(), i);
        }

        return hash;
    }

    /**
     * returns number of columns
     *
     * @return The number of columns in the table.
     */
    public int getNumCols() {
        return numCols;
    }

    /**
     * returns true if more rows are available
     *
     * @return Whether more rows are available.
     */
    public boolean hasMoreRows() {
        return rowCursor < (numRows);
    }

    /**
     * Returns next row and increments row cursor.
     *
     * @return returns next row.
     */
    public Object[] nextRow() {
        return this.rows.get(rowCursor++);
    }

    public Hashtable nextRowHash() {
        Object[] row = this.rows.get(rowCursor++);
        Hashtable rowHash = new Hashtable();
        for (int i = 0; i < this.numCols; i++) {
            Object v = row[i];
            if (v != null) {
                rowHash.put(columns[i], v);
            }
        }
        return rowHash;
    }

    /**
     * If this header is found in the collection of column names, then that index
     * is used to return the Object from the current row at that index.
     *
     * @param header The header for the column from which to retrieve a value from the current row.
     * @return The value for the specified column in the current row.
     */
    public Object getCellValue(String header) {
        Integer index = getColumnIndex(header);

        if (index != null) {
            return this.rows.get(rowCursor - 1)[index];
        } else {
            return null;
        }
    }

    public Integer getColumnIndex(String header) {
        Integer index = null;
        try {
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].equalsIgnoreCase(header)) {
                    index = i;
                    break;
                }
            }
        } catch (RuntimeException e) {
            return null;
        }
        return index;
    }

    /**
     * This is a pass-through method to avoid having to create an array for the {@link #insertRow(Object[]) method}.
     *
     * @param items The items to pass through as an array.
     */
    public void insertRowItems(Object... items) {
        insertRow(items);
    }

    /**
     * Inserts row into table and increments row counter.
     *
     * @param row of Objects
     */
    public void insertRow(Object[] row) {
        this.rows.add(row);
        numRows++;
    }

    /**
     * Resets row cursor to row 0
     */
    public void resetRowCursor() {
        rowCursor = 0;
    }

    /* (non-Javadoc)
     * Warning: Don't modify the contents of this ArrayList directly.  Use the insertRow and removeRow methods.  XFTTable is an old dumb ox and is not robust to your fancy pants ways.
     * @see org.nrg.xft.XFTTableI#rows()
     */
    public ArrayList<Object[]> rows() {
        return rows;
    }

    /**
     * Outputs table headers and contents as a delimited string
     *
     * @param delimiter The delimiter to use when formatting the table headers and contents.
     * @return The contents of the table in delimiter-separated form.
     */
    public String toString(String delimiter) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < this.numCols; i++) {
            if (i != 0) {
                buffer.append(delimiter);
            }
            buffer.append(StringEscapeUtils.escapeCsv(this.getColumns()[i]));
        }

        resetRowCursor();

        while (hasMoreRows()) {
            Object[] row = nextRow();
            for (int i = 0; i < this.numCols; i++) {
                if (i != 0) {
                    buffer.append(delimiter);
                } else {
                    buffer.append("\n");
                }
                buffer.append(StringEscapeUtils.escapeCsv(ValueParser(row[i])));
            }
        }

        return buffer.toString();
    }

    public String toHTML(boolean insertTDTags, String lightColor, String darkColor, Hashtable tableProperties, int startCount) {
        if (tableProperties == null) {
            tableProperties = new Hashtable();
        }
        StringBuilder sb = new StringBuilder("<TABLE");
        Enumeration enumer = tableProperties.keys();
        while (enumer.hasMoreElements()) {
            String key = (String) enumer.nextElement();
            String value = (String) tableProperties.get(key);
            sb.append(" ").append(key).append("=\"").append(value).append("\"");
        }

        sb.append(">\n<THEAD>\n<TR class=\"resultsHEADER\">");
        sb.append("<TH> </TH>");
        for (int i = 0; i < this.numCols; i++) {
            if (insertTDTags) {
                sb.append("<TH>").append(this.getColumns()[i]).append("</TH>");
            } else {
                sb.append(this.getColumns()[i]);
            }
        }
        sb.append("</TR>\n</THEAD>\n<TBODY ID=\"dataRows\">\n");

        resetRowCursor();

        int color = 0;
        while (hasMoreRows()) {
            if (isQuarantineRow(getRowCursor() + 1)) {
                sb.append("\n<TR  class=\"quarantine\">");
            } else if (color == 0) {
                sb.append("\n<TR class=\"odd\">");
                color = 1;
            } else {
                sb.append("\n<TR class=\"even\">");
                color = 0;
            }
            sb.append("<TD>").append(startCount++).append("</TD>");
            Object[] row = nextRow();
            for (int i = 0; i < this.numCols; i++) {
                if (i == 0) {
                    sb.append("\n");
                }
                if (insertTDTags) {
                    sb.append("<TD>").append(ValueParserNoNewline(row[i])).append("</TD>");
                } else {
                    sb.append(ValueParserNoNewline(row[i]));
                }
            }
            sb.append("</TR>");
        }
        sb.append("\n</TBODY>\n</TABLE>");

        return sb.toString();
    }

    public void toHTML(boolean insertTDTags, String lightColor, String darkColor, Hashtable tableProperties, int startCount, OutputStream out) {
        if (tableProperties == null) {
            tableProperties = new Hashtable();
        }
        boolean alternateColors = true;
        if (lightColor == null || darkColor == null) {
            alternateColors = false;
        }
        PrintStream pw = new PrintStream(out, true);
        pw.print("<TABLE");
        Enumeration enumer = tableProperties.keys();
        while (enumer.hasMoreElements()) {
            String key = (String) enumer.nextElement();
            String value = (String) tableProperties.get(key);
            pw.print(" ");
            pw.print(key);
            pw.print("=\"");
            pw.print(value);
            pw.print("\"");
        }

        pw.print(">\n<THEAD>\n<TR style=\"border-style:none;\">");
        pw.print("<TH>&nbsp;</TH>");
        for (int i = 0; i < this.numCols; i++) {
            if (insertTDTags) {
                pw.print("<TH>");
                pw.print(this.getColumns()[i]);
                pw.print("</TH>");
            } else {
                pw.print(this.getColumns()[i]);
            }
        }
        pw.print("</TR>\n</THEAD>\n<TBODY ID=\"dataRows\">\n");

        resetRowCursor();

        int color = 0;
        while (hasMoreRows()) {
            if (alternateColors) {
                if (isQuarantineRow(getRowCursor() + 1)) {
                    pw.print("\n<TR class=\"quarantine\">");
                } else if (color == 0) {
                    pw.print("\n<TR class=\"odd\">");
                    color = 1;
                } else {
                    pw.print("\n<TR class=\"even\">");
                    color = 0;
                }
            } else {
                pw.print("\n<TR>");
            }
            pw.print("<TD>");
            pw.print(startCount++);
            pw.print("</TD>");
            Object[] row = nextRow();
            for (int i = 0; i < this.numCols; i++) {
                if (i == 0) {
                    pw.print("\n");
                }
                if (insertTDTags) {
                    pw.print("<TD>");
                    pw.print(ValueParserNoNewline(row[i]));
                    pw.print("</TD>");
                } else {
                    pw.print(ValueParserNoNewline(row[i]));
                }
            }
            pw.print("</TR>");
        }
        pw.print("\n</TBODY>\n</TABLE>");
    }

    /**
     * Outputs table headers and contents into an HTML Table
     *
     * @param insertTDTags Indicates whether TD tags should be inserted.
     * @return The HTML representatin of the table.
     */
    public String toHTML(boolean insertTDTags) {
        StringBuilder sb = new StringBuilder("<TABLE>");
        sb.append("\n<THEAD>\n<TR>");
        for (int i = 0; i < this.numCols; i++) {
            if (insertTDTags) {
                sb.append("<TH>").append(this.getColumns()[i]).append("</TH>");
            } else {
                sb.append(this.getColumns()[i]);
            }
        }
        sb.append("</TR>\n</THEAD>\n<TBODY ID=\"dataRows\" STYLE=\"overflow:auto;\">\n");

        resetRowCursor();

        while (hasMoreRows()) {
            sb.append("<TR>");
            Object[] row = nextRow();
            for (int i = 0; i < this.numCols; i++) {
                if (i == 0) {
                    sb.append("\n");
                }
                if (insertTDTags) {
                    sb.append("<TH>").append(ValueParserNoNewline(row[i])).append("</TH>");
                } else {
                    sb.append(ValueParserNoNewline(row[i]));
                }
            }
            sb.append("</TR>");
        }
        sb.append("</TBODY>\n</TABLE>");

        return sb.toString();
    }

    /**
     * Outputs table headers and contents without delimiters
     *
     * @return A string representation of the table.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        XFTTable temp = cloneTable();
        for (int i = 0; i < temp.numCols; i++) {
            sb.append(temp.getColumns()[i]);
        }

        temp.resetRowCursor();

        while (temp.hasMoreRows()) {
            Object[] row = temp.nextRow();
            for (int i = 0; i < temp.numCols; i++) {
                if (i == 0) {
                    sb.append("\n");
                }
                sb.append(ValueParser(row[i]).replace("<", "&lt;").replace(">", "&gt;"));
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

    public static String ValueParser(Object o) {
        return ValueParser(o, "", "");
    }

    public static String ValueParserNoNewline(Object o) {
        return ValueParser(o, "\\r\\n|\\r|\\n", " ");
    }

    /**
     * Formats BYTEA type to string
     *
     * @param object         The object to format to a string..
     * @param regexToReplace The regex within the formatted string to replace.
     * @param replacement    The value with which the regex should be replaced.
     * @return The formatted value.
     */
    public static String ValueParser(Object object, String regexToReplace, String replacement) {
        if (object != null) {
            if (object.getClass().getName().equalsIgnoreCase("[B")) {
                byte[] b = (byte[]) object;
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                try {
                    baos.write(b);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
                return baos.toString().replaceAll(regexToReplace, replacement);
            }
            return object.toString().replaceAll(regexToReplace, replacement);
        } else {
            return "";
        }
    }

    /**
     * @return The number of rows in the table.
     */
    public int getNumRows() {
        return numRows;
    }

    public int size() {
        return getNumRows();
    }

    public ArrayList<Hashtable> rowHashs() {
        ArrayList<Hashtable> al = new ArrayList<>();
        this.resetRowCursor();
        while (this.hasMoreRows()) {
            Hashtable hash = this.nextRowHash();
            al.add(hash);
        }
        return al;
    }

    public Map<Object, Object> convertToHashtable(String keyColumn, String valueColumn) {
        return convertToMap(keyColumn, valueColumn, new Hashtable<>());
    }

    public Map<Object, Object> convertToMap(String keyColumn, String valueColumn, Map<Object, Object> al) {
        XFTTable t = this.cloneTable();

        Integer keyIndex = t.getColumnIndex(keyColumn);
        Integer valueIndex = t.getColumnIndex(valueColumn);

        t.resetRowCursor();
        while (t.hasMoreRows()) {
            Object[] row = t.nextRow();

            Object key = row[keyIndex];
            Object value = row[valueIndex];


            if (key != null && value != null) al.put(key, value);
        }

        return al;
    }

    public ArrayList convertColumnToArrayList(String colName) {
        ArrayList al = new ArrayList();

        XFTTable t = this.cloneTable();

        Integer index = t.getColumnIndex(colName);

        t.resetRowCursor();
        while (t.hasMoreRows()) {
            Object[] row = t.nextRow();
            Object v = row[index];
            if (v != null) al.add(v);
        }

        al.trimToSize();
        return al;
    }

    /**
     * ArrayList of ArrayLists
     *
     * @param sqlNames The columns to convert to a list.
     * @return A list of lists containing the requested columns.
     */
    public ArrayList convertColumnsToArrayList(ArrayList sqlNames) {
        ArrayList al = new ArrayList();

        XFTTable t = this.cloneTable();

        t.resetRowCursor();
        while (t.hasMoreRows()) {
            Object[] row = t.nextRow();
            Iterator iter = sqlNames.iterator();
            ArrayList sub = new ArrayList();
            while (iter.hasNext()) {
                String s = (String) iter.next();
                Integer index = t.getColumnIndex(s);
                Object v = row[index];
                if (v != null)
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
     * @return Returns the rowCursor.
     */
    public int getRowCursor() {
        return rowCursor;
    }

    /**
     * Converts each row into a hashtable and inserts them into an ArrayList.
     *
     * @return A list of maps containing the table data.
     */
    public ArrayList<Hashtable> toArrayListOfHashtables() {
        ArrayList<Hashtable> al = new ArrayList<>();
        XFTTable t = cloneTable();
        t.resetRowCursor();
        while (t.hasMoreRows()) {
            Hashtable row = t.nextRowHash();
            al.add(row);
        }
        al.trimToSize();
        return al;
    }

    /**
     * Converts each row into a list and inserts them into an ArrayList.
     *
     * @return A list of lists.
     */
    public ArrayList<List> toArrayListOfLists() {
        ArrayList<List> al = new ArrayList<>();
        XFTTable t = cloneTable();
        t.resetRowCursor();
        while (t.hasMoreRows()) {
            Object[] row = t.nextRow();
            al.add(Arrays.asList(row));
        }
        al.trimToSize();
        return al;
    }

    public Object getFirstObject() {
        XFTTable t = cloneTable();

        Object[] row = t.rows().get(0);
        return row[0];
    }

    public void addQuarantineRow(int i) {
        this.quarantineIndexs.add(i);
    }

    public boolean isQuarantineRow(int i) {
        return quarantineIndexs.contains(i);
    }

    public void sort(String colname, String order) {
        Integer index = getColumnIndex(colname);
        sort(index, order);
    }

    public void sort(int col, String order) {
        Comparator byColumn = new TableRowComparator(col, order);
        Collections.sort(rows, byColumn);
    }

    public class TableRowComparator implements Comparator {
        private int col = 0;
        private boolean asc = true;

        public TableRowComparator(int sortColumn, String sortOrder) {
            col = sortColumn;
            if (sortOrder.equalsIgnoreCase("DESC")) {
                asc = false;
            }
        }

        public int compare(Object o1, Object o2) {
            try {
                Comparable value1 = (Comparable) ((Object[]) o1)[col];
                Comparable value2 = (Comparable) ((Object[]) o2)[col];
                if (value1 == null) {
                    if (value2 == null) {
                        return 0;
                    } else {
                        if (asc)
                            return -1;
                        else
                            return 1;
                    }
                }
                if (value2 == null) {
                    if (asc)
                        return 1;
                    else
                        return -1;
                }
                int i = value1.compareTo(value2);
                if (asc) {
                    return i;
                } else {
                    if (i > 0) {
                        return -1;
                    } else if (i < 0) {
                        return 1;
                    } else {
                        return i;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

        public void setSortColumn(int s) {
            col = s;
        }
    }


    /* (non-Javadoc)
     * @see org.nrg.xft.XFTTableI#removeRow(int)
     */
    public Object[] removeRow(int rowNumber) throws Exception {
        if (rowNumber >= rows.size()) {
            throw new Exception("XFTTable index undefined.");
        } else {
            numRows--;
            return rows.remove(rowNumber);
        }
    }

    public Hashtable toHashtable(String key, String value) {
        XFTTable t = cloneTable();
        t.resetRowCursor();

        Integer keyI = t.getColumnIndex(key);
        Integer valueI = t.getColumnIndex(value);

        Hashtable al = new Hashtable();
        while (t.hasMoreRows()) {
            Object[] row = t.nextRow();
            Object keyV = "";
            Object valueV = "";

            if (row[keyI] != null) {
                keyV = row[keyI];
            }
            if (row[valueI] != null) {
                valueV = row[valueI];
            }

            al.put(keyV, valueV);
        }
        return al;
    }

    public void toXMLList(Writer w, String title) {
        toXMLList(w, new Hashtable<String, Map<String, String>>(), title);
    }

    public void toXMLList(Writer w, Map<String, Map<String, String>> columnProperties, String title) {
        try {
            Writer writer = new BufferedWriter(w);
            writer.write("<results");
            if (title != null)
                writer.write(" title=\"" + title + "\"");

            writer.write("><columns>");
            for (int i = 0; i < this.numCols; i++) {
                writer.write("<column");
                if (columnProperties.get(this.getColumns()[i]) != null) {
                    Map<String, String> map = columnProperties.get(this.getColumns()[i]);
                    for (Map.Entry<String, String> entry : map.entrySet()) {
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
            for (Object[] row : rows) {
                writer.write("<row>");
                for (int i = 0; i < this.numCols; i++) {
                    writer.write("<cell>" + ValueParserNoNewline(row[i]).replace(">", "&gt;").replace("<", "&lt;") + "</cell>");
                }
                writer.write("</row>\n");
                writer.flush();
            }
            writer.write("</rows></results>");
            writer.flush();
        } catch (IOException e) {
            logger.error("I/O error occurred", e);
        }
    }

    public void toCSV(Writer w) {
        try {
            Writer writer = new BufferedWriter(w);
            for (int i = 0; i < this.numCols; i++) {
                if (i > 0) {
                    writer.write(",");
                }
                writer.write(StringEscapeUtils.escapeCsv(this.getColumns()[i]));
            }
            writer.write("\n");
            writer.flush();

            for (Object[] row : rows) {
                for (int i = 0; i < this.numCols; i++) {
                    if (i > 0)
                        writer.write(",");
                    if (null != row[i]) {
                        writer.write(StringEscapeUtils.escapeCsv(ValueParserNoNewline(row[i])));
                    }
                }
                writer.write("\n");
                writer.flush();
            }
            writer.flush();
        } catch (IOException e) {
            logger.error("I/O error occurred", e);
        }
    }


    public void toJSON(Writer writer) throws IOException {
        toJSON(writer, null);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public void toJSON(Writer writer, Map<String, Map<String, String>> cp) throws IOException {
        org.json.JSONArray array = new org.json.JSONArray();
        ArrayList<ArrayList<String>> columnsWType = new ArrayList<>();
        for (int i = 0; i < this.numCols; i++) {
            ArrayList col = new ArrayList();
            col.add(this.getColumns()[i]);
            if (cp.containsKey(col.get(0))) {
                Map<String, String> props = cp.get(col.get(0));
                if (props.containsKey("type")) {
                    col.add(props.get("type"));
                }
            }
            columnsWType.add(col);
        }

        for (Object[] row : rows) {
            JSONObject json = new JSONObject();
            for (int i = 0; i < this.numCols; i++) {
                ArrayList<String> columnSpec = columnsWType.get(i);
                try {
                    json.put(columnSpec.get(0), ValueParser(row[i]));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            array.put(json);
        }
        try {
            array.write(writer);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Outputs table headers and contents into an HTML Table
     *
     * @param insertTDTags Indicates whether TD tags should be inserted.
     * @param writer       The writer to use for output.
     */
    public void toHTML(boolean insertTDTags, Writer writer) throws IOException {
        toHTML(insertTDTags, writer, new Hashtable<String, Map<String, String>>());
    }

    /**
     * Outputs table headers and contents into an HTML Table
     *
     * @param insertTDTags Indicates whether TD tags should be inserted.
     * @param writer       The writer to use for output.
     * @param cp           Column properties.
     */
    public void toHTML(boolean insertTDTags, Writer writer, Map<String, Map<String, String>> cp) throws IOException {
        writer.write("<table class=\"x_rs_t\" cellpadding=\"0\" cellspacing=\"0\">");
        writer.write("\n<thead class=\"x_rs_thead\">\n<tr class=\"x_rs_tr_head\">");
        for (int i = 0; i < this.numCols; i++) {
            if (insertTDTags) {
                writer.write("<th>" + columns[i] + "</th>");
            } else {
                writer.write(this.getColumns()[i]);
            }
        }
        writer.write("</tr>\n</thead>\n");
        writer.flush();
        writer.write("<tbody id=\"dataRows\">\n");

        int rowC = 0;
        if (rows != null) {
            for (Object[] row : rows) {
                writer.write("<tr class=\"x_rs_tr_data");
                if (rowC++ % 2 != 0) writer.write(" even");
                else writer.write(" odd");
                writer.write("\">");
                for (int i = 0; i < this.numCols; i++) {
                    if (i == 0) {
                        writer.write("\n");
                    }

                    String value = ValueParserNoNewline(row[i]);

                    if (cp != null && cp.containsKey(this.getColumns()[i]) && cp.get(this.getColumns()[i]).containsKey("serverRoot")) {
                        value = "<a href='" + cp.get(this.getColumns()[i]).get("serverRoot") + value + "'>" + value + "</a>";
                    }
                    if (insertTDTags) {
                        writer.write("<td>" + value + "</td>");
                    } else {
                        writer.write(value);
                    }
                }
                writer.write("</tr>");
            }
        }
        writer.write("</tbody>\n</table>");
        writer.flush();
    }

    public void sort(final List<String> sortColumns) {
        final List<Integer> indexes = new ArrayList<>();

        for (final String col : sortColumns) {
            final Integer i = this.getColumnIndex(col);
            if (i != null) {
                indexes.add(i);
            }
        }

        Collections.sort(rows, new Comparator<Object[]>() {
            public int compare(Object[] o1, Object[] o2) {
                for (final Integer i : indexes) {
                    try {
                        //contents could be String, Number or Date
                        if (o1[i] == null) {
                            if (o2[i] == null) {
                                return 0;
                            } else {
                                return 1;
                            }
                        } else if (o2[i] == null) {
                            return -1;
                        } else {
                            int c = ((Comparable) o1[i]).compareTo(o2[i]);
                            if (c != 0) {
                                return c;
                            }
                        }
                    } catch (ClassCastException e) {
                        //ignore non comparables for now.
                        logger.error("", e);
                    }
                }

                return 0;
            }
        });
    }

    public void reverse() {
        Collections.reverse(rows);
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
}
