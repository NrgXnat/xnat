//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 5, 2005
 *
 */
package org.nrg.xdat.presentation;
import java.sql.Timestamp;
import java.util.*;

import org.apache.log4j.Logger;
import org.nrg.xdat.display.DisplayFieldReferenceI;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.display.HTMLLink;
import org.nrg.xdat.display.HTMLLinkProperty;
import org.nrg.xdat.display.SQLQueryField;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.SecurityValues;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.XFTTable;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.StringUtils;
/**
 * @author Tim
 *
 */
public class HTMLPresenter extends PresentationA {
	static org.apache.log4j.Logger logger = Logger.getLogger(HTMLPresenter.class);
	public String getVersionExtension(){return "";}
	private String server = null;
	private boolean clickableHeaders = true;

	public HTMLPresenter(String serverLocal, boolean canClickHeaders)
	{
		server = serverLocal;
		if (! server.endsWith("/"))
		server = server + "/";
		clickableHeaders = canClickHeaders;
	}

	public HTMLPresenter(String serverLocal)
	{
		server = serverLocal;
		if (! server.endsWith("/"))
		server = server + "/";
		clickableHeaders = true;
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
		        columnHeaders.add("<TH> </TH>");
		    }
		}

		//POPULATE HEADERS

		Iterator fields = visibleFields.iterator();
		int counter = search.getInClauses().size();
		ArrayList diffs = new ArrayList();
		while (fields.hasNext())
		{
			Object o = fields.next();
		    DisplayFieldReferenceI dfr = (DisplayFieldReferenceI)o;
			StringBuffer headerLink = new StringBuffer();
			StringBuffer diffLink = new StringBuffer();
			headerLink.append("<TH ID=\"" + dfr.getElementName() + "." + dfr.getSortBy() +"\" ALIGN=\"left\"");
			diffLink.append("<TH ALIGN=\"left\"");
			if (search.isSuperSearch())
			{
				if (dfr.getLightColor().equalsIgnoreCase(""))
				{
					headerLink.append(" BGCOLOR='FFFFFF'");
					diffLink.append(" BGCOLOR='FFFFFF'");
				}else
				{
					headerLink.append(" BGCOLOR='"+ dfr.getLightColor() +"'");
					diffLink.append(" BGCOLOR='"+ dfr.getLightColor() +"'");
				}
			}
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
			headerLink.append(">");
			diffLink.append(">Diff</TH>");

			if (this.server != null && !dfr.getHeader().equalsIgnoreCase("") && clickableHeaders)
			{
			    if (!search.getSortBy().equalsIgnoreCase(dfr.getElementName() + "." + dfr.getSortBy()))
			    {
					headerLink.append("<A HREF='").append(server).append("app/action/SearchAction/sortBy/");
					headerLink.append(dfr.getElementName()).append(".");
					headerLink.append(dfr.getSortBy());
					headerLink.append("/sortOrder/DESC'>");
					headerLink.append(dfr.getHeader()).append("</A>");
			    }else{
			        if (search.getSortOrder().equalsIgnoreCase("DESC"))
			        {
			            headerLink.append("<A HREF='").append(server).append("app/action/SearchAction/sortBy/");
						headerLink.append(dfr.getElementName()).append(".");
						headerLink.append(dfr.getSortBy());
						headerLink.append("/sortOrder/ASC'>");
						headerLink.append(dfr.getHeader()).append(" <IMAGE border=0 src=\"" + server + "images/black-down-arrow.gif\"/></A>");
			        }else{
			            headerLink.append("<A HREF='").append(server).append("app/action/SearchAction/sortBy/");
						headerLink.append(dfr.getElementName()).append(".");
						headerLink.append(dfr.getSortBy());
						headerLink.append("/sortOrder/DESC'>");
						headerLink.append(dfr.getHeader()).append(" <IMAGE border=0 src=\"" + server + "images/black-up-arrow.gif\"/></A>");
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
			}
			headerLink.append("</TH>");
			if (allowDiffs)
			{
				if (!diffs.contains(dfr.getElementName()))
				{
				    diffs.add(dfr.getElementName());
				    SchemaElementI foreign = SchemaElement.GetElement(dfr.getElementName());
				    if (search.isMultipleRelationship(foreign))
				    {
					    String temp = StringUtils.SQLMaxCharsAbbr(search.getRootElement().getSQLName() + "_" + foreign.getSQLName() + "_DIFF");
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
			String status = "active";

			Object tempStatus = row.get("quarantine_status");
			if (tempStatus!=null)
			{
			    status = (String)tempStatus;
			    if (status.equals("quarantine"))
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
				        newRow[i] = "<TD>"+ v +"</TD>";
			        }else{
				        newRow[i] = "<TD> </TD>";
			        }
			    }
			}

			//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") WHILE: 2");
			diffs = new ArrayList();
			counter = search.getInClauses().size();
			while (fields.hasNext())
			{
				//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: 1");
				DisplayFieldReferenceI dfr = (DisplayFieldReferenceI)fields.next();
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
						    String temp = StringUtils.SQLMaxCharsAbbr(search.getRootElement().getSQLName() + "_" + foreign.getSQLName() + "_DIFF");
						    Integer index = ((XFTTable)table).getColumnIndex(temp);
						    if (index!=null)
						    {
							    String diff = "<TD";
							    if (search.isSuperSearch())
								{
									if(status.equals("quarantine"))
									{
									    diff+=" BGCOLOR='FFFFCC'";
									}else if(color==0)
									{
									    diff+=" BGCOLOR='";
										if (dfr.getDarkColor().equalsIgnoreCase(""))
										{
										    diff+="DEDEDE";
										}else{
										    diff+=dfr.getDarkColor();
										}
										diff+="'";
									}else{
									    diff+=" BGCOLOR='";
										if (dfr.getLightColor().equalsIgnoreCase(""))
										{
										    diff+="FFFFFF";
										}else{
										    diff+=dfr.getLightColor();
										}
										diff+="'";
									}
								}
							    diff += ">";
							    Object d = row.get(temp.toLowerCase());
						        if (d!=null)
						        {
							        diff+=  d.toString() +"</TD>";
						        }else{
						            diff+=" </TD>";
						        }
							    newRow[counter++]=diff;
						    }
							//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: diffs :IF:2");
					    }
						//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: diffs :3");
					}
				}
				//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: 2");

				if (v != null)
				{
					//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: IF : 1");
					StringBuffer sb = new StringBuffer();

					//SET TD TAG
					sb.append("<TD");
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
					if (search.isSuperSearch())
					{
					    if(status.equals("quarantine"))
						{
					        sb.append(" BGCOLOR='FFFFCC'");
						}else if(color==0)
						{
							sb.append(" BGCOLOR='");
							if (dfr.getDarkColor().equalsIgnoreCase(""))
							{
								sb.append("DEDEDE");
							}else{
								sb.append(dfr.getDarkColor());
							}
							sb.append("'");
						}else{
							sb.append(" BGCOLOR='");
							if (dfr.getLightColor().equalsIgnoreCase(""))
							{
								sb.append("FFFFFF");
							}else{
								sb.append(dfr.getLightColor());
							}
							sb.append("'");
						}
					}
					sb.append(" class=\"results\">");

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
							XDATUser user = (XDATUser)search.getUser();

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
                                        secureVariable = StringUtils.ReplaceStr(secureVariable.toString(), "<", "");
                                    }
                                    if (secureVariable.toString().indexOf(">")!=-1){
                                        secureVariable = StringUtils.ReplaceStr(secureVariable.toString(), ">", "");
                                    }
									values.put((String)link.getSecureProps().get(key),secureVariable);
								}
							}

							if (values.getHash().size()>0)
							{
								if (user.canReadByXMLPath(secureElement,values))
								{
									hasLink = true;
									sb.append("<A");
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
                                                                ArrayList<String> al = StringUtils.CommaDelimitedStringToArrayList(insertValue.toString());
                                                                insertValue =al.get(i);
                                                            } catch (Throwable e) {
                                                                logger.error("",e);
                                                            }
                                                        }
                                                    }
                                                    value = StringUtils.ReplaceStr(value,"@" + key,insertValue.toString());
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
                                                value = StringUtils.ReplaceStr(value,"@" + key,insertValue.toString());
                                            }
										}
										value = StringUtils.ReplaceStr(value,"@WEBAPP",server);
										sb.append(" ").append(prop.getName()).append("=");
										sb.append("\"").append(value).append("\"");
									}
									sb.append(">");
								}
							}
						}else{
							hasLink = true;
							sb.append("<A");
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
                                                        ArrayList<String> al = StringUtils.CommaDelimitedStringToArrayList(insertValue.toString());
                                                        insertValue =al.get(i);
                                                    } catch (Throwable e) {
                                                        logger.error("",e);
                                                    }
                                                }
                                            }
                                            value = StringUtils.ReplaceStr(value,"@" + key,insertValue.toString());
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
                                        value = StringUtils.ReplaceStr(value,"@" + key,insertValue.toString());
                                    }
								}
								value = StringUtils.ReplaceStr(value,"@WEBAPP",server);
								sb.append(" ").append(prop.getName()).append("=");
								sb.append("\"").append(value).append("\"");
							}
							sb.append(">");
						}

						//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: IF : IF 2");
					}

					//SET IMAGE
					if (dfr.isImage())
					{
						sb.append("<IMG");
                        v = StringUtils.ReplaceStr((String)v,"/@WEBAPP/",server);
                        v = StringUtils.ReplaceStr((String)v,"@WEBAPP/",server);
						if (dfr.getDisplayField().getHtmlImage().getWidth() != null)
						{
							sb.append(" width=\"" + dfr.getDisplayField().getHtmlImage().getWidth() + "\"");
						}
						if (dfr.getDisplayField().getHtmlImage().getHeight() != null)
						{
							sb.append(" height=\"" + dfr.getDisplayField().getHtmlImage().getHeight() + "\"");
						}
						sb.append(" SRC=\"").append(v.toString()).append("\" BORDER=0").append("/>");
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
                                vS= StringUtils.ReplaceStr(vS, "<", "&#60;");
                            }
							sb.append(vS);
					    }
					}

					if (hasLink)
					{
						sb.append("</A>");
					}

					sb.append("</TD>");

					newRow[counter] = sb.toString();
					//XFT.LogCurrentTime("BEGIN HTML FORMAT :: ROWS(" + table.getRowCursor() + ") FIELDS: IF : 2");
				}else{
					StringBuffer sb = new StringBuffer("<TD");
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
					if (search.isSuperSearch())
					{
					    if(status.equals("quarantine"))
						{
					        sb.append(" BGCOLOR='FFFFCC'");
						}else if(color==0)
						{
							sb.append(" BGCOLOR='");
							if (dfr.getDarkColor().equalsIgnoreCase(""))
							{
								sb.append("DEDEDE");
							}else{
								sb.append(dfr.getDarkColor());
							}
							sb.append("'");
						}else{
							sb.append(" BGCOLOR='");
							if (dfr.getLightColor().equalsIgnoreCase(""))
							{
								sb.append("FFFFFF");
							}else{
								sb.append(dfr.getLightColor());
							}
							sb.append("'");
						}
					}
					sb.append("> </TD>");
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

