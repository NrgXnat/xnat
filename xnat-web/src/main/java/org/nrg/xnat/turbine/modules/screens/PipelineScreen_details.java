/*
 * org.nrg.xnat.turbine.modules.screens.PipelineScreen_details
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */

package org.nrg.xnat.turbine.modules.screens;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.pipeline.PipelineRepositoryManager;
import org.nrg.pipeline.utils.PipelineFileUtils;
import org.nrg.pipeline.xmlbeans.PipelineData.Documentation;
import org.nrg.pipeline.xmlbeans.PipelineData.Documentation.Authors.Author;
import org.nrg.pipeline.xmlbeans.PipelineData.Documentation.Authors.Author.Contact;
import org.nrg.pipeline.xmlbeans.PipelineData.Documentation.InputParameters;
import org.nrg.pipeline.xmlbeans.PipelineData.Documentation.InputParameters.Parameter;
import org.nrg.pipeline.xmlbeans.PipelineData.ResourceRequirements.Property;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step;
import org.nrg.pipeline.xmlbeans.PipelineDocument;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.PipePipelinedetails;
import org.nrg.xdat.om.PipePipelinerepository;
import org.nrg.xdat.turbine.modules.screens.XDATScreen_pdf;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PipelineScreen_details extends XDATScreen_pdf {
		static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PipelineScreen_details.class);

	 	XFTItem item = null;

		protected ByteArrayOutputStream buildPdf (RunData data) throws 	Exception {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				PipePipelinerepository pipelineRepository = PipelineRepositoryManager.GetInstance();
				PipePipelinedetails pipeline = (PipePipelinedetails)BaseElement.GetGeneratedItem(item);
				String pipelineDescriptorPath = pipeline.getPath();
				PipelineDocument pipelineDoc = getDocument(pipelineDescriptorPath);
				Document document = new Document();
				PdfWriter.getInstance(document, baos);
				
				DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			    Date now = Calendar.getInstance().getTime();
				DateFormat ydateFormat = new SimpleDateFormat("yyyy-MM-dd");


			HeaderFooter footer  = new HeaderFooter(new Phrase("Document Production Date: " + dateFormat.format(now),new Font(Font.TIMES_ROMAN,9,Font.BOLD)),false);
				footer.setAlignment(Element.ALIGN_RIGHT);
				document.setFooter(footer);
				document.open();
				

				Font titleFont = new Font (Font.HELVETICA, 18, Font.BOLD, 
		                 new Color (0, 0, 128));

				Font textFont = new Font(Font.TIMES_ROMAN,10,Font.BOLD);

				   Paragraph title = new Paragraph ("Pipeline " + pipelineDescriptorPath, titleFont);
				   title.setAlignment (Element.ALIGN_CENTER);
				   title.setSpacingAfter (18.0f);
				   document.add(title);

				    document.add(Chunk.NEWLINE);

				   Paragraph p  = new Paragraph ("Runs on: " + pipelineRepository.getDisplayName(pipeline.getAppliesto()), textFont);
				   p.setAlignment (Element.ALIGN_LEFT);
				   document.add(p);

				   
				   p = new Paragraph ("Generates: " + pipelineRepository.getElementsGeneratedBy(pipeline), textFont);
				   p.setAlignment (Element.ALIGN_LEFT);
				   document.add(p);

				   p = new Paragraph ("Description: " + pipeline.getDescription(), textFont);
				   p.setAlignment (Element.ALIGN_LEFT);
				   document.add(p);

				   
				   if (pipelineDoc.getPipeline().isSetResourceRequirements()) {
					   String resourceRequirements = "";
					   Property[] properties = pipelineDoc.getPipeline().getResourceRequirements().getPropertyArray();
					   for (int i = 0; i < properties.length; i++) {
						   resourceRequirements += properties[i].getStringValue() + ", ";
					   }
					   if (resourceRequirements.endsWith(", ")) {
						   int index = resourceRequirements.lastIndexOf(", ");
						   resourceRequirements = resourceRequirements.substring(0, index);
					   }
					   p = new Paragraph ("Resource Requirements: " + resourceRequirements, textFont);
					   p.setAlignment (Element.ALIGN_LEFT);
					   document.add(p);
				   }

				   if (pipelineDoc.getPipeline().isSetDocumentation()) {
					   Documentation doc = pipelineDoc.getPipeline().getDocumentation();
					   if (doc.isSetWebsite()) {
						   p = new Paragraph ("More Information: " + doc.getWebsite(), textFont);
						   p.setAlignment (Element.ALIGN_LEFT);
						   document.add(p);
					   }
					   if (doc.isSetPublications()) {
						   p = new Paragraph ("Publications: " , textFont);
						   String[] pubs = doc.getPublications().getPublicationArray();
						   p.setAlignment (Element.ALIGN_LEFT);
						    document.add(p);
						   PdfPTable table = new PdfPTable (1);
						    for (int i = 0; i < pubs.length; i++)
						    	table.addCell(makeCell(pubs[i],new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
						    table.setSpacingAfter(9f);
						    document.add(table);
					   }
					   if (doc.isSetAuthors()) {
						   Author[] authors = doc.getAuthors().getAuthorArray();
						   p = new Paragraph ("Authors: ", textFont);
						   p.setAlignment (Element.ALIGN_LEFT);
						   document.add(p);
						    PdfPTable table = new PdfPTable (3);
						    table.addCell(makeCell("Name",new Font(Font.TIMES_ROMAN,10,Font.BOLD)));  
						    table.addCell(makeCell("Email",new Font(Font.TIMES_ROMAN,10,Font.BOLD))); 
						    table.addCell(makeCell("Phone",new Font(Font.TIMES_ROMAN,10,Font.BOLD))); 
						    for (int i = 0; i < authors.length; i++) {
						    	Author aAuthor = authors[i];
						    	table.addCell(makeCell(aAuthor.getFirstname() + " " + aAuthor.getLastname(),new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
						    	if (aAuthor.isSetContact()) {
						    		Contact contact = aAuthor.getContact();
						    		if (contact.isSetEmail()) {
							    		table.addCell(makeCell(contact.getEmail(),new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
						    		}else {
							    		table.addCell(makeCell(" " ,new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
						    		}
						    		if (contact.isSetPhone()) {
							    		table.addCell(makeCell(contact.getPhone(),new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
						    		}else {
							    		table.addCell(makeCell(" " ,new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
						    		}
						    	}else {
						    		table.addCell(makeCell(" " ,new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
						    		table.addCell(makeCell(" " ,new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
						    	}
						    }
						    table.setSpacingAfter(9f);
						    document.add(table);
					   }
					   if (doc.isSetVersion()) {
						   p = new Paragraph ("Version: " + doc.getVersion(), textFont);
						   p.setAlignment (Element.ALIGN_LEFT);
						   document.add(p);
					   }
					   if (doc.isSetInputParameters()) {
						   InputParameters parameters = doc.getInputParameters();
						   Parameter[] params =  parameters.getParameterArray();
						   p = new Paragraph ("Input Parameters Required: " , textFont);
						   p.setAlignment (Element.ALIGN_LEFT);
						   document.add(p);
						   document.add(Chunk.NEWLINE);
						   
						    PdfPTable table = new PdfPTable (4);
						    table.addCell(makeCell("Name",new Font(Font.TIMES_ROMAN,10,Font.BOLD)));  
						    table.addCell(makeCell("Description",new Font(Font.TIMES_ROMAN,10,Font.BOLD))); 
						    table.addCell(makeCell("CSVValue",new Font(Font.TIMES_ROMAN,10,Font.BOLD))); 
						    table.addCell(makeCell("Schema Link",new Font(Font.TIMES_ROMAN,10,Font.BOLD))); 
						    for (int i = 0; i < params.length; i++) {
						    	Parameter aParam = params[i];
						    	table.addCell(makeCell(aParam.getName(),new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
						    	table.addCell(makeCell(aParam.getDescription(),new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
						    	if (aParam.isSetValues()) {
							    	if (aParam.getValues().isSetCsv()) {
							    		table.addCell(makeCell(aParam.getValues().getCsv(),new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
							    	}else {
							    		table.addCell(makeCell("",new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
							    	}
							    	if (aParam.getValues().isSetSchemalink()) {
							    		table.addCell(makeCell(aParam.getValues().getSchemalink(),new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
							    	}else {
							    		table.addCell(makeCell("",new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
							    	}
						    	}else {
						    		table.addCell(makeCell("",new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
						    		table.addCell(makeCell("",new Font(Font.TIMES_ROMAN,10,Font.NORMAL)));
						    	}
						    }
						    table.setSpacingAfter(9f);
						    document.add(table);

					   }
				   }
				   
				   Step[] steps = pipelineDoc.getPipeline().getSteps().getStepArray();
				   for (int i = 0; i < steps.length; i++) {
					   Step aStep = steps[i];
					   p = new Paragraph ("Step " + aStep.getId() + ":" + aStep.getDescription(), textFont);
					   p.setAlignment (Element.ALIGN_LEFT);
					   document.add(p);
				   }
				   document.close();
				
			}catch(Exception e) {
				logger.error(e);
			}
			return baos;
		}
	
		
		private PipelineDocument getDocument(String pathToPipelineXmlFile) throws Exception {
			return PipelineFileUtils.GetDocument(pathToPipelineXmlFile);
		}

	   public void doBuildTemplate(RunData data, Context context)	{
	        try {
	            item = TurbineUtils.GetItemBySearch(data);
	        } catch (Exception e1) {}
			if (item == null)		{
				data.setMessage("Error: No item found.");
				TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
			}else{
				try {
					finalProcessing(data,context);
				} catch (Exception e) {
					data.setMessage(e.toString());
				}
			}
		}

		
		private  PdfPCell makeCell(String text, Font font) {
			Rectangle border = getBorder();
	        Paragraph p = null;
	        if (font == null)
	        	p = new Paragraph(text);
	        else
	         p = new Paragraph(text, font);

	        PdfPCell cell = new PdfPCell(p);
	        cell.cloneNonPositionParameters(border);
	        cell.setUseBorderPadding(true);
	        cell.setPadding(2f);
	        return cell;
	    }
		
	    private Rectangle getBorder() {
		Rectangle border = new Rectangle(0f, 0f);
	        border.setBorderWidthLeft(0f);
	        border.setBorderWidthBottom(0f);
	        border.setBorderWidthRight(0f);
	        border.setBorderWidthTop(0f);
	        border.setBorderColor(Color.BLACK);
		return border;
	   }
		
}
