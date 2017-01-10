/*
 * web: org.nrg.xnat.restlet.presentation.RESTHTMLPresenter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xnat.restlet.presentation;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.display.DisplayFieldReferenceI;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.display.HTMLLink;
import org.nrg.xdat.display.HTMLLinkProperty;
import org.nrg.xdat.display.SQLQueryField;
import org.nrg.xdat.presentation.PresentationA;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.SecurityValues;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;
/**
 * @author Tim
 *
 */
public class RESTHTMLPresenter extends PresentationA {
	static org.apache.log4j.Logger logger = Logger.getLogger(RESTHTMLPresenter.class);
	public String getVersionExtension(){return "";}
	private String server = null;
	private boolean clickableHeaders = true;
	String searchURI=null;
	UserI user=null;
	public String sortBy=null;

	public RESTHTMLPresenter(String serverLocal, boolean canClickHeaders,String searchURI,UserI u,String sortBy)
	{
		server = serverLocal;
		if (! server.endsWith("/"))
		server = server + "/";
		clickableHeaders = canClickHeaders;
		this.searchURI=searchURI;
		this.user=u;
		this.sortBy=sortBy;
	}

	public RESTHTMLPresenter(String serverLocal,String searchURI,UserI u,String sortBy)
	{
		server = serverLocal;
		if (! server.endsWith("/"))
		server = server + "/";
		clickableHeaders = true;
		this.searchURI=searchURI;
		this.user=u;
		this.sortBy=sortBy;
	}


	public XFTTableI formatTable(XFTTableI table, DisplaySearch search) throws Exception
	{
	    return formatTable(table,search,true);
	}

	public XFTTableI formatTable(XFTTableI table, DisplaySearch search,boolean allowDiffs) throws Exception
	{
	    logger.debug("BEGIN HTML FORMAT");
		XFTTable csv = new XFTTable();
		ElementDisplay ed = DisplayManager.GetElementDisplay(getRootElement().getFullXMLName());
		ArrayList visibleFields = search.getVisibleFields(this.getVersionExtension());

		//int fieldCount = visibleFields.size() + search.getInClauses().size();

		ArrayList columnHeaders = new ArrayList();

		if (search.getInClauses().size()>0)
		{
		    for(int i=0;i<search.getInClauses().size();i++)
		    {
		        columnHeaders.add("<th class=\"x_rs_th x_rs_th_in\"> </th>");
		    }
		}

		//POPULATE HEADERS

		Iterator fields = visibleFields.iterator();
		int counter = search.getInClauses().size();
		ArrayList diffs = new ArrayList();
		int headerC=0;

		int random = (new Random()).nextInt(1000000);
		
		while (fields.hasNext())
		{
			Object o = fields.next();
		    DisplayFieldReferenceI dfr = (DisplayFieldReferenceI)o;
			StringBuffer headerLink = new StringBuffer();
			StringBuffer diffLink = new StringBuffer();


			String id=null;
			if(dfr.getValue()!=null && !dfr.getValue().equals("")){
				if(dfr.getValue().equals("{XDAT_USER_ID}")){
					dfr.setValue(user.getID());
				}
			}
			if (dfr.getElementName().equalsIgnoreCase(search.getRootElement().getFullXMLName()))
			{
				id = dfr.getRowID().toLowerCase();
			}else{
				id = dfr.getElementSQLName().toLowerCase() + "_" + dfr.getRowID().toLowerCase();
			}
			
			
			String tHclass="x_rs_th";
			if(this.sortBy!=null && this.sortBy.equals(id)){
				tHclass+=" sorted";
			}
			if(headerC++==0){
				tHclass+=" first-td";
			}
			
			String title = "";
			if(dfr.getDisplayField()!=null && dfr.getDisplayField().getDescription()!=null){
				title=dfr.getDisplayField().getDescription();
			}
			
			// The id must be unique across tabs or document.getElementByID will not work as expected. 
			// We add a random number to the id to make it *likely* unique. 
			headerLink.append("<th class=\"" + tHclass + "\" id=\"" + id + random +"\" name=\"" + id +"\" align=\"left\"");
			diffLink.append("<th align=\"left\" class=\"x_rs_th\" id=\"diff_" + id + random +"\"");
			if (dfr.getHeaderCellWidth() != null)
			{
				headerLink.append(" width=\"" + dfr.getHeaderCellWidth() + "\"");
			}
			if (dfr.getHeaderCellHeight() != null)
			{
				headerLink.append(" height=\"" + dfr.getHeaderCellHeight() + "\"");
			}
			if (dfr.getHeaderCellAlign() != null)
			{
				headerLink.append(" align=\"" + dfr.getHeaderCellAlign() + "\"");
			}
			if (dfr.getHeaderCellVAlign() != null)
			{
				headerLink.append(" valign=\"" + dfr.getHeaderCellVAlign() + "\"");
			}
			headerLink.append("><div class=\"yui-dt-liner\" title=\"").append(TurbineUtils.GetInstance().escapeHTML(title)).append("\">");
			diffLink.append(">Diff</th>");
			
			if (this.searchURI != null && !dfr.getHeader().equalsIgnoreCase("") && clickableHeaders)
			{
				headerLink.append("<a href='" + this.searchURI + "?format=xList&sortBy=");
				headerLink.append(dfr.getElementName()).append(".");
				headerLink.append(dfr.getSortBy());
			    if (!search.getSortBy().equalsIgnoreCase(dfr.getElementName() + "." + dfr.getSortBy()))
			    {
					headerLink.append("&sortOrder=DESC'>");
					headerLink.append(dfr.getHeader()).append("</A>");
			    }else{
			        if (search.getSortOrder().equalsIgnoreCase("DESC"))
			        {
						headerLink.append("&sortOrder=ASC'>");
						headerLink.append(dfr.getHeader()).append(" <img border=\"0\" src=\"" + server + "images/black-down-arrow.gif\"/></a>");
			        }else{
						headerLink.append("&sortOrder=DESC'>");
						headerLink.append(dfr.getHeader()).append(" <img border=\"0\" src=\"" + server + "images/black-up-arrow.gif\"/></a>");
			        }
			    }
			}else
			{
				if (dfr.getHeader().equalsIgnoreCase(""))
				{
					headerLink.append(" ");
				}else{
					headerLink.append(dfr.getHeader());
				}

				if(this.sortBy!=null && this.sortBy.equals(id)){
					//headerLink.append(" <IMAGE border=0 src=\"" + server + "images/black-down-arrow.gif\"/>");
				}
			}
			
			headerLink.append("</div></th>");
			if (allowDiffs)
			{
				if (!diffs.contains(dfr.getElementName()))
				{
				    diffs.add(dfr.getElementName());
				    SchemaElementI foreign = SchemaElement.GetElement(dfr.getElementName());
				    if (search.isMultipleRelationship(foreign))
				    {
					    String temp = XftStringUtils.SQLMaxCharsAbbr(search.getRootElement().getSQLName() + "_" + foreign.getSQLName() + "_DIFF");
					    Integer index = ((XFTTable)table).getColumnIndex(temp);
					    if (index!=null)
					    {
						    columnHeaders.add(diffLink.toString());
					    }
				    }
				}
			}
			columnHeaders.add(headerLink.toString());
		}
		csv.initTable(columnHeaders);


		//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS");

		//POPULATE DATA
		table.resetRowCursor();

		int color =0;//0=dark,1=light

		while (table.hasMoreRows())
		{
			Hashtable row = table.nextRowHash();
			Object[] newRow = new Object[columnHeaders.size()];
			fields = visibleFields.iterator();
			String status = ViewManager.ACTIVE;

			Object tempStatus = row.get("quarantine_status");
			if (tempStatus!=null)
			{
			    status = (String)tempStatus;
			    if (status.equals(ViewManager.QUARANTINE))
			        csv.addQuarantineRow(table.getRowCursor());
			}

			//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") WHILE: 1");

			if (search.getInClauses().size()>0)
			{
			    for(int i=0;i<search.getInClauses().size();i++)
			    {
			        Object v = row.get("search_field"+i);
			        if (v!=null)
			        {
				        newRow[i] = "<td class=\"x_rs_td x_rs_td_in\">"+ v +"</td>";
			        }else{
				        newRow[i] = "<td class=\"x_rs_td x_rs_td_in\"> </td>";
			        }
			    }
			}

			//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") WHILE: 2");
			diffs = new ArrayList();
			counter = search.getInClauses().size();
			int fieldC=0;
			while (fields.hasNext())
			{
				//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: 1");
				DisplayFieldReferenceI dfr = (DisplayFieldReferenceI)fields.next();

				String field_row_id=null;
				if(dfr.getValue()!=null && !dfr.getValue().equals("")){
					if(dfr.getValue().equals("{XDAT_USER_ID}")){
						dfr.setValue(user.getID());
					}
				}
				if (dfr.getElementName().equalsIgnoreCase(search.getRootElement().getFullXMLName()))
				{
					field_row_id = dfr.getRowID().toLowerCase();
				}else{
					field_row_id = dfr.getElementSQLName().toLowerCase() + "_" + dfr.getRowID().toLowerCase();
				}
				
				Object v = null;
				if (dfr.getElementName().equalsIgnoreCase(search.getRootElement().getFullXMLName()))
				{
					v = row.get(dfr.getRowID().toLowerCase());
				}else{
					v = row.get(dfr.getElementSQLName().toLowerCase() + "_" + dfr.getRowID().toLowerCase());
				}

				if (allowDiffs)
				{
					if (!diffs.contains(dfr.getElementName()))
					{
						//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: diffs :1");
					    diffs.add(dfr.getElementName());
					    SchemaElementI foreign = SchemaElement.GetElement(dfr.getElementName());
						//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: diffs :2");
					    if (search.isMultipleRelationship(foreign))
					    {
							//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: diffs :IF:1");
						    String temp = XftStringUtils.SQLMaxCharsAbbr(search.getRootElement().getSQLName() + "_" + foreign.getSQLName() + "_DIFF");
						    Integer index = ((XFTTable)table).getColumnIndex(temp);
						    if (index!=null)
						    {
							    String diff = "<td";
							    String classNames="x_rs_td";
								if(status.equals(ViewManager.QUARANTINE))
								{
									classNames+=" quarantine";
								}
								diff+="  class=\"" + classNames + "\"";
							    diff += ">";
							    Object d = row.get(temp.toLowerCase());
						        if (d!=null)
						        {
							        diff+=  d.toString() +"</td>";
						        }else{
						            diff+="&nbsp;</td>";
						        }
							    newRow[counter++]=diff;
						    }
							//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: diffs :IF:2");
					    }
						//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: diffs :3");
					}
				}
				//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: 2");

				if (v != null && !v.toString().trim().equals(""))
				{
					//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: IF : 1");
					StringBuffer sb = new StringBuffer();

					//SET TD TAG
					sb.append("<td");
					if (dfr.getHTMLCellWidth() != null)
					{
						sb.append(" width=\"" + dfr.getHTMLCellWidth() + "\"");
					}
					if (dfr.getHTMLCellHeight() != null)
					{
						sb.append(" height=\"" + dfr.getHTMLCellHeight() + "\"");
					}
					if (dfr.getHTMLCellAlign() != null)
					{
						sb.append(" align=\"" + dfr.getHTMLCellAlign() + "\"");
					}
					if (dfr.getHTMLCellVAlign() != null)
					{
						sb.append(" valign=\"" + dfr.getHTMLCellVAlign() + "\"");
					}

					String classNames="x_rs_td";
					if(status.equals(ViewManager.QUARANTINE))
					{
						classNames+=" quarantine";
					}
					if(this.sortBy!=null && this.sortBy.equals(field_row_id)){
						classNames+=" sorted";
					}
					if(fieldC++==0){
						classNames+=" first-td";
					}
					if(v.toString().length()>50){
						classNames+=" ext-content";
					}
					sb.append("  class=\"" + classNames +"\"");
					sb.append(">");

					boolean hasLink = false;

					if (dfr.getHTMLLink() != null)
					{
						//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: IF : IF 1");
						//HAS HTML LINK - CREATE ANCHOR

						HTMLLink link = dfr.getHTMLLink();
						//INSERT SECURITY VALIDATION
						if (link.isSecure() && dfr.getElementName()!=null && !dfr.getElementName().equals(getRootElement().getFullXMLName()))
						{
							SchemaElementI secureElement = SchemaElement.GetElement(link.getSecureLinkTo());
							UserI user = (UserI)search.getUser();

							SecurityValues values = new SecurityValues();
							Enumeration secureKeys = link.getSecureProps().keys();
							while (secureKeys.hasMoreElements())
							{
								String key = (String)secureKeys.nextElement();
								Object secureVariable = null;
								if (! dfr.getElementName().equalsIgnoreCase(search.getRootElement().getFullXMLName()))
								{
								    secureVariable = row.get(dfr.getElementSQLName().toLowerCase() + "_" + key.toLowerCase());
								}else{
								    secureVariable = row.get(key.toLowerCase());
								}
								if (secureVariable != null)
								{
                                    if (secureVariable.toString().indexOf("<")!=-1){
                                        secureVariable = StringUtils.replace(secureVariable.toString(), "<", "");
                                    }
                                    if (secureVariable.toString().indexOf(">")!=-1){
                                        secureVariable = StringUtils.replace(secureVariable.toString(), ">", "");
                                    }
									values.put((String)link.getSecureProps().get(key),secureVariable.toString());
								}
							}

							if (values.getHash().size()>0)
							{
								if (Permissions.canRead(user,secureElement,values))
								{
									hasLink = true;
									sb.append("<a");
									Iterator iter = link.getProperties().iterator();
									while (iter.hasNext())
									{
										HTMLLinkProperty prop = (HTMLLinkProperty)iter.next();
										String value = prop.getValue();
										for(Map.Entry<String, String> entry :prop.getInsertedValues().entrySet())
										{
											String key = entry.getKey();
											String id = entry.getValue();
                                            if (id.startsWith("@WHERE")){
                                                if (dfr.getDisplayField() instanceof SQLQueryField){
                                                    Object insertValue = dfr.getValue();

                                                    if (insertValue == null)
                                                    {
                                                        insertValue = "NULL";
                                                    }else{
                                                        if (insertValue.toString().indexOf(",")!=-1){
                                                            id = id.substring(6);
                                                            try {
                                                                Integer i = Integer.parseInt(id);
                                                                ArrayList<String> al = XftStringUtils.CommaDelimitedStringToArrayList(insertValue.toString());
                                                                insertValue =al.get(i);
                                                            } catch (Throwable e) {
                                                                logger.error("",e);
                                                            }
                                                        }
                                                    }
                                                    value = StringUtils.replace(value,"@" + key,insertValue.toString());
                                                }
                                            }else{
                                                Object insertValue = row.get(id.toLowerCase());
                                                if (! dfr.getElementName().equalsIgnoreCase(search.getRootElement().getFullXMLName()))
                                                {
                                                    insertValue = row.get(dfr.getElementSQLName().toLowerCase() + "_" + id.toLowerCase());
                                                }
                                                if (insertValue == null)
                                                {
                                                    insertValue = "NULL";
                                                }
                                                value = StringUtils.replace(value,"@" + key,insertValue.toString());
                                            }
										}
										value = StringUtils.replace(value,"@WEBAPP",server);
										sb.append(" ").append(prop.getName().toLowerCase()).append("=");
										
										if(StringUtils.isBlank(value)){
											sb.append("\"&nbsp;\"");
										}else{
											sb.append("\"").append(value).append("\"");
										}
									}
									sb.append(">");
								}
							}
						}else{
							hasLink = true;
							sb.append("<a");
							Iterator iter = link.getProperties().iterator();
							while (iter.hasNext())
							{
								HTMLLinkProperty prop = (HTMLLinkProperty)iter.next();
								String value = prop.getValue();
								for(Map.Entry<String, String> entry :prop.getInsertedValues().entrySet())
								{
									String key = entry.getKey();
									String id = entry.getValue();
                                    if (id.startsWith("@WHERE")){
                                        if (dfr.getDisplayField() instanceof SQLQueryField){
                                            Object insertValue = dfr.getValue();

                                            if (insertValue == null)
                                            {
                                                insertValue = "NULL";
                                            }else{
                                                if (insertValue.toString().indexOf(",")!=-1){
                                                    id = id.substring(6);
                                                    try {
                                                        Integer i = Integer.parseInt(id);
                                                        ArrayList<String> al = XftStringUtils.CommaDelimitedStringToArrayList(insertValue.toString());
                                                        insertValue =al.get(i);
                                                    } catch (Throwable e) {
                                                        logger.error("",e);
                                                    }
                                                }
                                            }
                                            value = StringUtils.replace(value,"@" + key,insertValue.toString());
                                        }
                                    }else{
                                        Object insertValue = row.get(id.toLowerCase());
                                        if (! dfr.getElementName().equalsIgnoreCase(search.getRootElement().getFullXMLName()))
                                        {
                                            insertValue = row.get(dfr.getElementSQLName().toLowerCase() + "_" + id.toLowerCase());
                                        }
                                        if (insertValue == null)
                                        {
                                            insertValue = "NULL";
                                        }
                                        value = StringUtils.replace(value,"@" + key,insertValue.toString());
                                    }
								}
								value = StringUtils.replace(value,"@WEBAPP",server);
								sb.append(" ").append(prop.getName()).append("=");

								if(StringUtils.isBlank(value)){
									sb.append("\"&nbsp;\"");
								}else{
									sb.append("\"").append(value).append("\"");
								}
							}
							sb.append(">");
						}

						//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: IF : IF 2");
					}

					//SET IMAGE
					if (dfr.isImage())
					{
						sb.append("<img");
                        v = StringUtils.replace((String)v,"/@WEBAPP/",server);
                        v = StringUtils.replace((String)v,"@WEBAPP/",server);
                        v = StringUtils.replace((String)v,"/@WEBAPP",server);
                        v = StringUtils.replace((String)v,"@WEBAPP",server);
						if (dfr.getDisplayField().getHtmlImage().getWidth() != null)
						{
							sb.append(" width=\"" + dfr.getDisplayField().getHtmlImage().getWidth() + "\"");
						}
						if (dfr.getDisplayField().getHtmlImage().getHeight() != null)
						{
							sb.append(" height=\"" + dfr.getDisplayField().getHtmlImage().getHeight() + "\"");
						}
						sb.append(" src=\"").append(v.toString()).append("\" border=\"0\"/>");
					}else
					{
					    if (v instanceof Timestamp)
					    {
					       // String s = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ENGLISH).format(DateUtils.parseDateTime(v.toString()));
							sb.append(v.toString());
					    }else{
                            String vS = v.toString();
                            if (vS.indexOf("<")!=-1 && vS.indexOf(">")==-1)
                            {
                                vS= StringUtils.replace(vS, "<", "&#60;");
                            }
							sb.append(vS);
					    }
					}

					if (hasLink)
					{
						sb.append("</a>");
					}

					sb.append("</td>");

					newRow[counter] = sb.toString();
					//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: IF : 2");
				}else{
					StringBuffer sb = new StringBuffer("<td");
					if (dfr.getHTMLCellWidth() != null)
					{
						sb.append(" width=\"" + dfr.getHTMLCellWidth() + "\"");
					}
					if (dfr.getHTMLCellHeight() != null)
					{
						sb.append(" height=\"" + dfr.getHTMLCellHeight() + "\"");
					}
					if (dfr.getHTMLCellAlign() != null)
					{
						sb.append(" align=\"" + dfr.getHTMLCellAlign() + "\"");
					}
					if (dfr.getHTMLCellVAlign() != null)
					{
						sb.append(" valign=\"" + dfr.getHTMLCellVAlign() + "\"");
					}
					String classNames="x_rs_td";
					if(status.equals(ViewManager.QUARANTINE))
					{
						classNames+=" quarantine";
					}
					if(this.sortBy!=null && this.sortBy.equals(field_row_id)){
						classNames+=" sorted";
					}
					if(fieldC++==0){
						classNames+=" first-td";
					}
					sb.append("  class=\"" + classNames +"\"");
					sb.append(">&nbsp;</td>");
					newRow[counter] = sb.toString();
				}

				counter++;
			}
			//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") 5");
			csv.insertRow(newRow);
			if (color==0)
			{
				color=1;
			}else{
				color=0;
			}
		}


		logger.debug("END HTML FORMAT");
		return csv;
	}
}

