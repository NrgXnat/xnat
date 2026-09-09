/*
 * web: org.nrg.xnat.restlet.resources.search.SearchElementListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.search;

import org.json.JSONObject;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTTable;
import org.nrg.xnat.restlet.representations.JSONObjectRepresentation;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.services.CreateDataTypeCallable;
import org.nrg.xnat.services.CreateDataTypeResult;
import org.nrg.xnat.services.DataTypeInfo;
import org.nrg.xnat.services.GetDataTypesCallable;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SearchElementListResource extends SecureResource {
	public SearchElementListResource(Context context, Request request, Response response) {
		super(context, request, response);
		
			this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
			this.getVariants().add(new Variant(MediaType.TEXT_HTML));
			this.getVariants().add(new Variant(MediaType.TEXT_XML));
			
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void handlePost() {
		if (Roles.isSiteAdmin(this.getUser())) {
			if ("create".equals(getQueryVariable("task"))) {
				String prefix = getQueryVariable("prefix");
				String type = getQueryVariable("complexType");
				String name = getQueryVariable("name");
				String singular = URLDecoder.decode(getQueryVariable("singular"), StandardCharsets.UTF_8);
				String plural = URLDecoder.decode(getQueryVariable("plural"), StandardCharsets.UTF_8);
				String extensionS = getQueryVariable("extends");

				CreateDataTypeCallable callable = new CreateDataTypeCallable(prefix, type, name, singular, plural, extensionS);
				try {
					CreateDataTypeResult result = callable.call();

					switch (result.getStatus()) {
						case SUCCESS:
							this.getResponse().setStatus(Status.SUCCESS_CREATED, result.getMessage());
							JSONObject r = new JSONObject();
							r.put("complexType", result.getMessage());
							this.getResponse().setEntity(new JSONObjectRepresentation(MediaType.APPLICATION_JSON, r));
							break;
						case BAD_REQUEST:
							this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, result.getMessage());
							break;
						case FORBIDDEN:
							this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, result.getMessage());
							break;
						case CONFLICT:
							this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, result.getMessage());
							break;
						case INTERNAL_ERROR:
							this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, result.getMessage());
							break;
					}
				} catch (Exception e) {
					logger.error("Error creating data type", e);
					this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
				}
			}
		}
	}

	@Override
	public Representation represent(Variant variant) {
		Hashtable<String,Object> params= new Hashtable<>();
		params.put("title", "Data-Types");

		XFTTable fields = new XFTTable();
		fields.initTable(new String[]{"ELEMENT_NAME","SINGULAR","PLURAL","SECURED","COUNT"});

		try {
			GetDataTypesCallable callable = new GetDataTypesCallable(
					getUser(),
					getQueryVariable("secured") != null,
					getQueryVariable("used") != null,
					getQueryVariable("readable") != null
			);

			List<DataTypeInfo> dataTypeInfos = callable.call();

			for (DataTypeInfo info : dataTypeInfos) {
				Object[] sub = new Object[5];
				sub[0] = info.getElementName();
				sub[1] = info.getSingular();
				sub[2] = info.getPlural();
				sub[3] = info.isSecured() ? "true" : "false";
				sub[4] = info.getCount();
				fields.rows().add(sub);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		MediaType mt = overrideVariant(variant);

		return this.representTable(fields, mt, params);
	}

}
