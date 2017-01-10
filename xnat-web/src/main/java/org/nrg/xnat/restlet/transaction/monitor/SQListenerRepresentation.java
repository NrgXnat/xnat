/*
 * web: org.nrg.xnat.restlet.transaction.monitor.SQListenerRepresentation
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.transaction.monitor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nrg.action.InvalidParamsException;
import org.nrg.status.StatusList;
import org.nrg.status.StatusMessage;
import org.nrg.status.StatusMessage.Status;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xnat.helpers.transactions.HTTPSessionStatusManagerQueue;
import org.nrg.xnat.helpers.transactions.PersistentStatusQueueManagerI;
import org.nrg.xnat.restlet.representations.JSONObjectRepresentation;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class SQListenerRepresentation extends SecureResource {
	final String transaction_id;
	private static final Logger logger = LoggerFactory.getLogger(SQListenerRepresentation.class);
	
	public SQListenerRepresentation(Context context, Request request, Response response) {
		super(context, request, response);

		transaction_id= (String)getParameter(request,"TRANSACTION_ID");
		
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}

	@Override
	public boolean allowDelete() {
		return true;
	}

	@Override
	public boolean allowPost() {
		return true;
	}
	
	private PersistentStatusQueueManagerI retrieveSQManager(){
		return new HTTPSessionStatusManagerQueue(this.getHttpSession());
	}
	
	public StatusList retrieveStatusQueue() {
		final PersistentStatusQueueManagerI manager=retrieveSQManager();
		StatusList sq=manager.retrieveStatusQueue(transaction_id);
		if(sq==null){
			sq = new StatusList();
			manager.storeStatusQueue(transaction_id, sq);
		}
		
		return sq;
	}

	@Override
	public void removeRepresentations() throws ResourceException {
		try {
			retrieveSQManager().deleteStatusQueue(transaction_id);
		} catch (IllegalArgumentException e) {
			throw new ResourceException(e);
		}
	}
	
	private Status buildStatus(final String s) throws InvalidParamsException {
		if(Status.COMPLETED.toString().equals(s)){
			return Status.COMPLETED;
		}else if(Status.FAILED.toString().equals(s)){
			return Status.FAILED;
		}else if(Status.PROCESSING.toString().equals(s)){
			return Status.PROCESSING;
		}else if(Status.WARNING.toString().equals(s)){
			return Status.WARNING;
		}else{
			throw new InvalidParamsException("Status", s + " is not a valid Status.");
		}
	}
	
	private StatusMessage buildMessage(final Status s,final String msg) throws InvalidParamsException {
		return new StatusMessage(this.transaction_id,s,msg);
	}

	@Override
	public void acceptRepresentation(Representation entity)
			throws ResourceException {
		@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        Form form = new Form(entity);
		
		try {
			final StatusMessage msg=buildMessage(buildStatus(TurbineUtils.escapeParam(form.getFirstValue("status"))),TurbineUtils.escapeParam(form.getFirstValue("message")));
			retrieveStatusQueue().notify(msg);
		} catch (InvalidParamsException e) {
			logger.error("Invalid params passed", e);
			throw new ResourceException(e);
		}
	}

	@Override
	public Representation represent(Variant variant) throws ResourceException {
		final MediaType mt = overrideVariant(variant);
		
		try {
			final StatusList sq = retrieveStatusQueue();
					
			if (mt.equals(MediaType.APPLICATION_JSON)){
				return new JSONObjectRepresentation(MediaType.APPLICATION_JSON,buildJSONObject(sq));
			}else if (mt.equals(MediaType.TEXT_PLAIN)){
				return new StringRepresentation(sq.toString(),MediaType.TEXT_PLAIN);
			}else{
				return new HTMLStatusListRepresentation(MediaType.TEXT_XML,sq);
			}
		} catch (JSONException e) {
			throw new ResourceException(e);
		}
	}
	
	private JSONObject buildJSONObject(final StatusList sq) throws JSONException{
		JSONObject o= new JSONObject();

		JSONArray messages = new JSONArray();
		o.append("msgs", messages);
		
		for(final StatusMessage msg:sq.getMessages()){
			JSONObject i=new JSONObject();
			i.put("status", msg.getStatus());
			i.put("msg", msg.getMessage());
			messages.put(i);
		}
		
		return o;
	}
	
	class HTMLStatusListRepresentation extends OutputRepresentation {
		final StatusList o;
		
		public HTMLStatusListRepresentation(MediaType mediaType,final StatusList o) {
			super(mediaType);
			this.o=o;		
		}

		@Override
		public void write(OutputStream os) throws IOException {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
			writer.write("<html><body><table>");
			
			for(final StatusMessage msg:o.getMessages()){
				writer.write("<tr><td class='s" + msg.getStatus().toString() + "'>");
				writer.write(msg.getStatus().toString());
				writer.write("</td><td>");
				writer.write(msg.getMessage());
				writer.write("</td></tr>");
				writer.newLine();
			}
			
			writer.write("</table></body></html>");
			writer.flush();
		}
	}
}
