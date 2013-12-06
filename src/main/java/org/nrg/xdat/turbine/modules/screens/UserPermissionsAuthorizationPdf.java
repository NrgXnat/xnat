//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
package org.nrg.xdat.turbine.modules.screens;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.turbine.util.RunData;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.PermissionItem;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.TurbineUtils;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfAcroForm;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPTableEvent;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 *
 * @author 
 */
public class UserPermissionsAuthorizationPdf extends PdfScreen
{
	private XDATUser User;
	protected ByteArrayOutputStream buildPdf (RunData data) throws Exception{
			
			User = new XDATUser(item);
			// Access the output stream.
			 ByteArrayOutputStream out = new ByteArrayOutputStream ();
			 
			//creation of the document with a certain size and certain margins
			 Document document = new Document(PageSize.A4, 36, 72, 108, 180);
			 Document.compress = false;
			 String URL = TurbineUtils.GetRelativeServerPath(data) +"/images/";
			 try {
				 // creation of the different writers
				PdfWriter writer = PdfWriter.getInstance(document, out);
				document.open();
				makeCoverPage(document, writer,User.getID());
				document.setPageCount(0);
				
				
				//Set Header and Footer
				HeaderFooter header = new HeaderFooter(new Phrase("Date: " + new Date() + "                             Experiment Authorization Sheet"), false);
				document.setHeader(header);

				HeaderFooter footer = new HeaderFooter(new Phrase("Page: "), true);
				document.setFooter(footer);
				document.newPage();
				
				//PdfAction jAction = PdfAction.javaScript("this.print(true);\r", writer);
				//writer.addJavaScript(jAction);
				loadDocument(document, writer, URL);
				 
			 }
			 catch (Exception de) {
				 de.printStackTrace();
			 }
			 document.close();	
			 	
    		return out;
    }
		 	 /**
			  * Creates an empty paragraph in the PDF, for spacing.
			  *
			  * @param p_doc The Document being created.
			  * @throws DocumentException
			  */
			 private void addWhiteSpace(Document p_doc)  throws DocumentException
			 {
					 Paragraph p = new Paragraph(" ");
					 p_doc.add(p);
 
			 }  //addWhiteSpace

	private PdfPTable emptyNestedTable(int cols, boolean border)  throws DocumentException
	{
			PdfPTable nested = new PdfPTable(cols);
			if (!border) nested.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			for (int i=0;i<cols;i++) nested.addCell("");
			return nested;
	}  

	public void makeCoverPage(Document document, PdfWriter writer, Integer UserId) {
		try {
			PdfContentByte cb = writer.getDirectContent();

			BaseFont helv = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
			
			BaseFont helvBold = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
			Date today = new Date();
			String date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(today);
			cb.beginText();
			cb.setFontAndSize(helv, 12);
			cb.setTextMatrix(350, 700);
			cb.showText("User: " + User.getFirstname() + " " + User.getLastname());
			cb.setTextMatrix(350, 680);
			cb.showText("Date: " + date);
			cb.setTextMatrix(150, 500);
			cb.setFontAndSize(helvBold, 16);
			cb.showText("CNDA User Authorization Report");
			cb.setTextMatrix(150, 470);
			cb.showText("User: " + User.getFirstname() + " " + User.getLastname());
			cb.setTextMatrix(150, 440);
			cb.showText("Date: " + date);
			cb.setFontAndSize(helv, 12);
			cb.setTextMatrix(50, 150);
			cb.showText("Authorized by:  " );
			cb.setTextMatrix(350,150);
			cb.showText("Date: " );		
			cb.endText(); 
			cb.setLineWidth(2f);
			cb.moveTo(130, 150);
			cb.lineTo(330, 150);
			cb.setLineWidth(2f);
			cb.moveTo(390, 150);
			cb.lineTo(480, 150);

			cb.stroke(); 
			cb.saveState();
			cb.restoreState();

		}catch(Exception e) {
			e.printStackTrace();
  		}	
	}

		public  void loadDocument(Document document, PdfWriter writer, String URL) {
			
			  try {
			    List<List<Object>> allElements = User.getPermissionItems();

				BaseFont helv = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
				float fontSize = 12;
				//Construct the criteria and fetch the details:
				int counter = 0;
				for(List<Object> elementManager:allElements)
				{
				    String elementName = (String)elementManager.get(0);
				    SchemaElement se = SchemaElement.GetElement(elementName);
				    ArrayList permissionItems = (ArrayList)elementManager.get(1);
				    
				    ElementDisplay ed = se.getDisplay();
				    
				    String fullDescription = se.getFullXMLName();
				    if (ed != null)
				    {
				        if (ed.getFullDescription() != null && !ed.getFullDescription().equals(""))
				        fullDescription = ed.getFullDescription();
				    }
			        PdfPTable datatable = setTable(fullDescription);
					int defaultBorder = datatable.getDefaultCell().border();
					
				    Iterator pis = permissionItems.iterator();
				    while (pis.hasNext())
				    {
				        PermissionItem pi = (PermissionItem)pis.next();
//				      datatable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
						datatable.getDefaultCell().setBorder(defaultBorder);
						datatable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
						datatable.addCell(pi.getDisplayName());
						datatable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
						datatable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
						datatable.addCell(emptyNestedTable(4,true));
				    }
				    
				    datatable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
					datatable.addCell(emptyNestedTable(2,false));			    
				    
					datatable.setTableEvent(new CNLTableEvent(se.getFullXMLName(),permissionItems, User.getID(), URL));
					document.add(datatable);
					
					datatable = new PdfPTable(1);
					datatable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
					//datatable.addCell("Authorized By: Randy Buckner");
					datatable.addCell("Initials:");
					datatable.setTotalWidth(document.right()-document.left());
					datatable.writeSelectedRows(0, -1, document.leftMargin(), document.bottom()+50, writer.getDirectContent()); 
					
					document.newPage();
				}
			}catch(Exception e) {
				  e.printStackTrace();
 		    }
	  }

	public PdfPTable setTable(String heading) {
				PdfPTable datatable = null;
				try{	
					datatable = new PdfPTable(2);
					int defaultborder = datatable.getDefaultCell().border();
					int defaultColSpan = datatable.getDefaultCell().getColspan();
					int headerwidths[] = {50, 50};
					datatable.setWidths(headerwidths);
					datatable.setTotalWidth(100);
					datatable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            		
					PdfPTable nestedHeaderTable = new PdfPTable(4);
					nestedHeaderTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
					nestedHeaderTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
					nestedHeaderTable.getDefaultCell().setNoWrap(true);
					nestedHeaderTable.addCell("Login Id:");
					nestedHeaderTable.addCell(User.getUsername());
					nestedHeaderTable.addCell("User Name:");
					nestedHeaderTable.addCell(User.getFirstname() + "  "+ User.getLastname());
									            		
					datatable.getDefaultCell().setColspan(2);
					datatable.addCell(nestedHeaderTable);
        		
					nestedHeaderTable = new PdfPTable(1);
					nestedHeaderTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
					nestedHeaderTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
					nestedHeaderTable.getDefaultCell().setBackgroundColor(new Color(0xC0, 0xC0, 0xC0));
					nestedHeaderTable.addCell(heading);							            		
					datatable.getDefaultCell().setColspan(2);
					datatable.addCell(nestedHeaderTable);
					
					datatable.getDefaultCell().setColspan(defaultColSpan);

					datatable.addCell("");
					nestedHeaderTable = new PdfPTable(1);
					nestedHeaderTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
					nestedHeaderTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
					nestedHeaderTable.addCell("Permissions");							            		
					datatable.addCell(nestedHeaderTable);

					datatable.getDefaultCell().setColspan(defaultColSpan);
					
					nestedHeaderTable = new PdfPTable(1);
					nestedHeaderTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
					nestedHeaderTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
					nestedHeaderTable.addCell("Investigator");							            		
					datatable.addCell(nestedHeaderTable);

					nestedHeaderTable = new PdfPTable(4);
					int widths[] = {25, 25, 25, 25};
					nestedHeaderTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
					nestedHeaderTable.setWidths(widths);
					nestedHeaderTable.setTotalWidth(100);

					//nestedHeaderTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
					nestedHeaderTable.addCell("Read");
					nestedHeaderTable.addCell("Create");
					nestedHeaderTable.addCell("Update");
					nestedHeaderTable.addCell("Delete");
					datatable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
					datatable.addCell(nestedHeaderTable);


					/*nestedHeaderTable = new PdfPTable(4);
					nestedHeaderTable.getDefaultCell().setBackgroundColor(Color.yellow);
					nestedHeaderTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
					nestedHeaderTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
					PdfPCell cell1 = new PdfPCell (new Phrase(""));
					cell1.setCellEvent(new CNLCellEvent(ExptCode,"R", Investigators));
					nestedHeaderTable.addCell(cell1);
					cell1 = new PdfPCell (new Phrase(""));
					cell1.setCellEvent(new CNLCellEvent(ExptCode,"C", Investigators));
					nestedHeaderTable.addCell(cell1);
					cell1 = new PdfPCell (new Phrase(""));
					cell1.setCellEvent(new CNLCellEvent(ExptCode,"U", Investigators));
					nestedHeaderTable.addCell(cell1);
					cell1 = new PdfPCell (new Phrase(""));
					cell1.setCellEvent(new CNLCellEvent(ExptCode,"D", Investigators));
					nestedHeaderTable.addCell(cell1);
					datatable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
					datatable.addCell(nestedHeaderTable);*/


					datatable.setHeaderRows(4);
					datatable.getDefaultCell().setBorder(defaultborder);
				}catch(BadElementException bex) {
					bex.printStackTrace();
				}catch(DocumentException de){
					de.printStackTrace();
				}
	
		return datatable;
	}

	public void setPageNumbers(Document document, PdfWriter writer) {
		try {
				// step 4: we grab the ContentByte and do some stuff with it
				  PdfContentByte cb = writer.getDirectContent();
            
				  // we create a PdfTemplate
				  PdfTemplate template = cb.createTemplate(50, 50);
				  BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
				  // we add a number of pages
				  int i;
				  for (i = 1; i < 5; i++) {
					  String text = "Page " + writer.getPageNumber() + " of ";
					  float len = bf.getWidthPoint(text, 12);
					  cb.beginText();
					  cb.setFontAndSize(bf, 12);
					  cb.setTextMatrix(280, 40);
					  cb.showText(text);
					  cb.endText();
					  cb.addTemplate(template, 280 + len, 40);
					  document.newPage();
				  }
				  template.beginText();
				  template.setFontAndSize(bf, 12);
				  template.showText(String.valueOf(writer.getPageNumber() - 1));
				  template.endText();
		}catch(DocumentException de) {
			de.printStackTrace();		  	
		}catch(IOException io) {
			io.printStackTrace();
		}		  	
	}
}


class CNLCellEvent implements PdfPCellEvent {
	String elementName, Action;
	List PermissionItems;
	public CNLCellEvent (String name,String ActionCode, List pis) {
	    elementName = name;
		Action = ActionCode;
		PermissionItems = pis;
	}
	
	public void cellLayout(PdfPCell cell, Rectangle r, PdfContentByte[] canvases) {
		try{
			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);		
			PdfContentByte cb = canvases[PdfPTable.TEXTCANVAS];
			PdfWriter writer = cb.getPdfWriter();
			PdfAcroForm acroForm =writer.getAcroForm();
			cb.saveState();
			cb.beginText();
			cb.setFontAndSize(bf,10);
			cb.setColorFill(Color.red);
			cb.setTextMatrix(r.left()+(3*(r.right()-r.left())/4), r.bottom());
			cb.showText("All");
			cb.endText(); 
			PdfFormField field = acroForm.addCheckBox(elementName+"_"+Action, elementName+"_"+Action, false, r.left(), r.bottom(),r.left()+((r.right()-r.left())/2), r.top());
			cb.restoreState();
		
			cb = canvases[PdfPTable.BASECANVAS];
			cb.saveState();
			writer = cb.getPdfWriter();

			java.util.Iterator iterInv = PermissionItems.iterator();
					
			String jScript = "var chkBoxObjects = [ ";
			String invIds="";
			while (iterInv.hasNext()) {
				invIds += "\"" + elementName +"_"+((PermissionItem)iterInv.next()).getValue() + "_"+Action+"\" ,";
			}			
			jScript += invIds.substring(0,invIds.lastIndexOf(",")) +" ]; \n";
			jScript += "for (var i=0; i<chkBoxObjects.length; i++) {" ;
			jScript += "var f = this.getField(chkBoxObjects[i]); ";
			jScript += "f.checkThisBox(0); ";
			jScript += "}";
			PdfAction aak = PdfAction.javaScript(jScript, writer);
			field.setAdditionalActions(PdfAnnotation.AA_JS_KEY, aak);
			writer.addAnnotation(field);
   		
		}catch(IOException ioE){
			ioE.printStackTrace();
		}catch(DocumentException ioE){
			ioE.printStackTrace();
		}
		
	}
}

 class CNLTableEvent implements PdfPTableEvent {
 	private List perimissionItems;
 	private String elementName = null;
 	
	private Hashtable permissionsHash = null;
	private Image imgAuthorized = null;
	private Image imgPendingAuthorization = null;
	public CNLTableEvent(String elementName,List pis, Integer UserId, String URL) {
		this.perimissionItems= pis;
		this.elementName=elementName;
		getPermissionsSetImage(UserId, URL);
	}

	public void getPermissionsSetImage(Integer UserId, String URL) {
			try {
				imgAuthorized = Image.getInstance(URL+"checkmarkGreen.gif");
				imgPendingAuthorization = Image.getInstance(URL+"checkmarkRed.gif");
				permissionsHash = new Hashtable();

				java.util.Iterator iterPermissions = perimissionItems.iterator();
				while (iterPermissions.hasNext()) {
					PermissionItem tuiep = (PermissionItem)iterPermissions.next();
					List permission = new ArrayList();
					permission.add(0,new Boolean(tuiep.canRead()));
					permission.add(1,new Boolean(tuiep.canCreate()));
					permission.add(2,new Boolean(tuiep.canEdit()));
					permission.add(3,new Boolean(tuiep.canDelete()));
					permission.add(4,new Boolean(tuiep.isAuthenticated()));
					permissionsHash.put(tuiep,permission);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

	public void tableLayout(PdfPTable table, float[][] width, float[] heights, int headerRows, int rowStart, PdfContentByte[] canvases)  {
		PdfContentByte cb = canvases[PdfPTable.TEXTCANVAS];
		//PdfAcroForm acroForm = cb.getPdfWriter().getAcroForm();
		try {	
					int line;
					float lly=0;
					float[] last=new float[3];
					for (line = rowStart; line < heights.length - 1; ++line) {
						if ((line-rowStart)==perimissionItems.size()) break;
						cb.saveState();
						float widths[] = width[line]; //Here widths.length=3 as there are 2 columns				
						int ColWidth = (int)((widths[2] - widths[1])/4);
						float llx=widths[1];
						last[0] = widths[0]; last[1]=widths[1]; last[2] = widths[2];
						lly = heights[line+1];
						float urx = llx+ColWidth, ury = heights[line];
						
						PermissionItem pi = (PermissionItem)perimissionItems.get(line-4);
						
						if (pi.canRead())
						{
						    if (pi.isAuthenticated())
						    {
						        cb.addImage(imgAuthorized,14,0,0,13,(llx+urx)/2,lly);
						    }else{
						        cb.addImage(imgPendingAuthorization,14,0,0,13,(llx+urx)/2,lly);
						    }
						}
					    llx += ColWidth;
						urx += ColWidth;
						
						if (pi.canCreate())
						{
						    if (pi.isAuthenticated())
						    {
						        cb.addImage(imgAuthorized,14,0,0,13,(llx+urx)/2,lly);
						    }else{
						        cb.addImage(imgPendingAuthorization,14,0,0,13,(llx+urx)/2,lly);
						    }
						}
					    llx += ColWidth;
						urx += ColWidth;
						
						if (pi.canEdit())
						{
						    if (pi.isAuthenticated())
						    {
						        cb.addImage(imgAuthorized,14,0,0,13,(llx+urx)/2,lly);
						    }else{
						        cb.addImage(imgPendingAuthorization,14,0,0,13,(llx+urx)/2,lly);
						    }
						}
					    llx += ColWidth;
						urx += ColWidth;
						
						if (pi.canDelete())
						{
						    if (pi.isAuthenticated())
						    {
						        cb.addImage(imgAuthorized,14,0,0,13,(llx+urx)/2,lly);
						    }else{
						        cb.addImage(imgPendingAuthorization,14,0,0,13,(llx+urx)/2,lly);
						    }
						}
					    llx += ColWidth;
						urx += ColWidth;
						
						cb.moveTo(widths[0], heights[line+1]);
						cb.stroke();
						cb.restoreState();
					}
				cb.saveState();
				//cb.addImage(imgAuthorized,14,0,0,13,last[0],lly-30);
				//cb.addImage(imgPendingAuthorization,14,0,0,13,last[1],lly-30);
				//cb.beginText();
            	//BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
				//cb.setFontAndSize(bf, 12);
				//cb.showTextAligned(PdfContentByte.ALIGN_LEFT,"Status: Authorized", last[0]+50, lly-30, 0);
				//cb.showTextAligned(PdfContentByte.ALIGN_LEFT,"Status: UnAuthorized", last[1]+50, lly-30, 0);
				//cb.endText();
				cb.restoreState();			
		}catch(DocumentException de) {
			de.printStackTrace();	
		}
	}
	
	public String getName(int i, int row) {
		String rtn=elementName + "_";
		PermissionItem inv = (PermissionItem)perimissionItems.get(row);
		rtn+=inv.getValue() + "_";
		if (i==0) rtn+= "R";
		else if	(i==1) rtn+= "C";
		else if (i==2) rtn+= "U";
		else if (i==3) rtn+= "D";
		return rtn;	
	}

}
