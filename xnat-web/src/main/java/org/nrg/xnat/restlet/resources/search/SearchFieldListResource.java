/*
 * web: org.nrg.xnat.restlet.resources.search.SearchFieldListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.display.SQLQueryField;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.display.transport.services.ElementDisplayStorageService;
import org.nrg.xdat.display.transport.services.impl.HibernateElementDisplayService;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.XFTTool;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.customforms.exceptions.CustomFormFetcherNotFoundException;
import org.nrg.xnat.customforms.helpers.CustomFormDisplayFieldHelper;
import org.nrg.xnat.customforms.interfaces.CustomFormDisplayFieldsI;
import org.nrg.xnat.customforms.manager.DefaultCustomFormManager;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class SearchFieldListResource extends SecureResource{
	static Logger logger = Logger.getLogger(SearchFieldListResource.class);
	private String elementName=null;
	private String projectScope=null;
	public SearchFieldListResource(Context context, Request request, Response response) {
		super(context, request, response);

			elementName= (String)getParameter(request,"ELEMENT_NAME");
			projectScope = getQueryVariable("projectScope");
			if(elementName!=null){
				this.getVariants().add(new Variant(MediaType.TEXT_XML));
			}else{
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
	}



	@Override
	public boolean allowPut() {
		return true;
	}

	private void setBooleanProperty(XFTItem found,String field,boolean _default) {
		try {
			if(_default && !this.isQueryVariableFalse(field)){
				found.setProperty(field, Boolean.TRUE);
			}else if(!_default && !this.isQueryVariableTrue(field)){
				found.setProperty(field, Boolean.FALSE);
			}else if(_default){
				found.setProperty(field, Boolean.FALSE);
			}else
				found.setProperty(field, Boolean.TRUE);
		} catch (XFTInitException e) {
			logger.error("",e);
		} catch (ElementNotFoundException e) {
			logger.error("",e);
		} catch (FieldNotFoundException e) {
			logger.error("",e);
		} catch (InvalidValueException e) {
			logger.error("",e);
		}
	}

	private void setAction(XFTItem found,int count,String action_name,String display_name, String img, String secureAccess, String popup){
		try {
			found.setProperty("xdat:element_security.element_actions.element_action__"+count + ".element_action_name",action_name);
			found.setProperty("xdat:element_security.element_actions.element_action__"+count + ".display_name",display_name);
			found.setProperty("xdat:element_security.element_actions.element_action__"+count + ".sequence",Integer.valueOf(count));
			if(img!=null)
				found.setProperty("xdat:element_security.element_actions.element_action__"+count + ".image",img);
			if(secureAccess!=null)
				found.setProperty("xdat:element_security.element_actions.element_action__"+count + ".secureAccess",secureAccess);
			if(popup!=null)
				found.setProperty("xdat:element_security.element_actions.element_action__"+count + ".popup",popup);
		} catch (XFTInitException e) {
			logger.error("",e);
		} catch (ElementNotFoundException e) {
			logger.error("",e);
		} catch (FieldNotFoundException e) {
			logger.error("",e);
		} catch (InvalidValueException e) {
			logger.error("",e);
		}
	}

	private ElementDisplayStorageService _elementDisplayService;

	private ElementDisplayStorageService getElementDisplayService() {
		if (_elementDisplayService == null) {
			_elementDisplayService = XDAT.getContextService().getBean(ElementDisplayStorageService.class);
		}
		return _elementDisplayService;
	}


	@Override
	public void handlePut() {
		if(Roles.isSiteAdmin(this.getUser())) {
			try {
				if (XFTTool.ValidateElementName(elementName)) {
					try {
						XFTItem found = XFTItem.NewItem(elementName, getUser());
						SchemaElement se = SchemaElement.GetElement(elementName);

						if (StringUtils.isNotBlank(filepath)) {
							if (StringUtils.startsWith(filepath,"fields")) {
								if (this.isQueryVariableTrue("addSqlQueryValue")) {
									//creates a copy of a SqlQueryDisplayField with the specified configuration
									String fieldId = this.getQueryVariable("fieldId");
									String header = this.getQueryVariable("header");
									String value = this.getQueryVariable("value");

									this.getElementDisplayService().addSqlQueryValue(elementName, fieldId, header, value);
									return;
								} else {
									//modifies an existing display field
									String fieldId = this.getQueryVariable("fieldId");
									String header = this.getQueryVariable("header");
									Boolean searchable = (this.hasQueryVariable("searchable")) ? Boolean.parseBoolean(this.getQueryVariable("searchable")) : null;
									String description = this.getQueryVariable("description");

									this.getElementDisplayService().configureDisplayField(elementName, fieldId, header, searchable, description);
									return;
								}
							}
						}

						//prior implementation of element_security modification... not sure why its here, but its probably used.
						if ((!this.isQueryVariableFalse("secure")) && se.hasField(se.getFullXMLName() + "/project") && se.hasField(se.getFullXMLName() + "/sharing/share/project")) {
							found.setProperty("secure", Boolean.TRUE);
							found.setProperty("primary_security_fields.primary_security_field__0", se.getFullXMLName() + "/project");
							found.setProperty("primary_security_fields.primary_security_field__1", se.getFullXMLName() + "/sharing/share/project");
						}

						this.setBooleanProperty(found, "browseable", true);
						this.setBooleanProperty(found, "searchable", true);
						this.setBooleanProperty(found, "secure_read", true);
						this.setBooleanProperty(found, "secure_edit", true);
						this.setBooleanProperty(found, "secure_create", true);
						this.setBooleanProperty(found, "secure_delete", true);
						this.setBooleanProperty(found, "accessible", true);

						this.setBooleanProperty(found, "secondary_password", false);
						this.setBooleanProperty(found, "secure_ip", false);
						this.setBooleanProperty(found, "quarantine", false);
						this.setBooleanProperty(found, "pre_load", false);

						if (this.getQueryVariable("singular") != null) {
							found.setProperty("singular", this.getQueryVariable("singular"));
						}

						if (this.getQueryVariable("plural") != null) {
							found.setProperty("plural", this.getQueryVariable("plural"));
						}

						if (this.getQueryVariable("code") != null) {
							found.setProperty("code", this.getQueryVariable("code"));
						}

						int count = 0;

						this.setAction(found, count++, "edit", "Edit", "e.gif", "edit", null);

						this.setAction(found, count++, "xml", "View XML", "r.gif", null, null);

						this.setAction(found, count++, "xml_file", "Download XML", "save.gif", null, null);

						this.setAction(found, count++, "email_report", "Email", "right2.gif", null, "always");

					} catch (ElementNotFoundException e) {
						logger.error("", e);
					} catch (FieldNotFoundException e) {
						logger.error("", e);
					} catch (InvalidValueException e) {
						logger.error("", e);
					} catch (HibernateElementDisplayService.NoFieldFound e) {
						logger.error("", e);
					} catch (HibernateElementDisplayService.NoSuchElementException e) {
						logger.error("", e);
					}
				} else {
					this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
					return;
				}
			} catch (XFTInitException e) {
				logger.error("", e);
				this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				return;
			}
		}else{
			this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return;
		}
	}



	@Override
	public Representation getRepresentation(Variant variant) {
		Hashtable<String,Object> params= new Hashtable<>();
		params.put("title", "Search Fields");

		params.put("element_name", elementName);
		CustomFormDisplayFieldHelper customFormDisplayFieldHelper = new CustomFormDisplayFieldHelper();

		XFTTable fields = new XFTTable();
		fields.initTable(new String[]{"FIELD_ID","HEADER","SUMMARY","TYPE","REQUIRES_VALUE","DESC","ELEMENT_NAME","SRC","VISIBLE","SEARCHABLE","VALUE"});

		Boolean showHidden  =false ;
		if(isQueryVariableTrue("showAll")){
			showHidden=true;
		}

		Boolean showGenerated  =true ;
		if(isQueryVariableFalse("showGenerated")){
			showGenerated=false;
		}

		Boolean showCustomForms  =true ;
		if(isQueryVariableFalse("showCustomForms")){
			showCustomForms=false;
		}

		ArrayList<String> elementNames=XftStringUtils.CommaDelimitedStringToArrayList(elementName);
		for(String en : elementNames)
        {
            try {
				SchemaElement se = SchemaElement.GetElement(en);
				ElementDisplay ed = se.getDisplay();
				DisplayField pi=ed.getProjectIdentifierField();

				params.put("versions", ed.getVersionsJSON());

				final List<DisplayField> displays = ed.getSortedFields();
				for (DislayField df : displays)
				{
				   if(showHidden || df.isSearchable()){
					   String id = df.getId();
					   if (!showCustomForms && customFormDisplayFieldHelper.isCustomFieldDisplayField(id, en)) {
						   continue;
					   }
					   String summary = df.getSummary();
					   String header = df.getHeader();
					   String type = df.getDataType();
					   Boolean requiresValue= false;
					   Object value = null;
					   if(df instanceof SQLQueryField){
						   if(StringUtils.contains(id,"=")){
							   value =((SQLQueryField)df).getValue();
						   }else{
							   requiresValue=true;
						   }
					   }
					   Object[] sub = new Object[11];
					   sub[0]=id;
					   sub[1]=header;
					   sub[2]=summary;
					   sub[3]=type;
					   sub[4]=requiresValue;
					   sub[5]=(df.getDescription()==null)?(df.getHeader()==null)?df.getId():df.getHeader():df.getDescription();
					   sub[6]=se.getFullXMLName();
					   sub[7]=0;
					   sub[8]=df.isVisible();
					   sub[9]=df.isSearchable();
					   sub[10]=value;
					   fields.rows().add(sub);
				   }
				}

				try {
					final UserI user = getUser();
					ArrayList<Object[]> label_fields = new ArrayList<Object[]>();
					List<String> addedJsonFields = new ArrayList<>();
					if(showGenerated) {

						List<List> custom_fields = UserHelper.getUserHelperService(user).getQueryResultsAsArrayList("SELECT DISTINCT ON (name) dtp.xnat_projectdata_id AS project, fdgf.name, fdgf.datatype AS type FROM xnat_abstractprotocol dtp LEFT JOIN xnat_datatypeprotocol_fieldgroups dtp_fg ON dtp.xnat_abstractprotocol_id=dtp_fg.xnat_datatypeprotocol_xnat_abstractprotocol_id LEFT JOIN xnat_fielddefinitiongroup fdg  ON dtp_fg.xnat_fielddefinitiongroup_xnat_fielddefinitiongroup_id=fdg.xnat_fielddefinitiongroup_id LEFT JOIN xnat_fielddefinitiongroup_field fdgf ON fdg.xnat_fielddefinitiongroup_id=fdgf.fields_field_xnat_fielddefiniti_xnat_fielddefinitiongroup_id WHERE dtp.data_type='" + en + "' AND fdgf.type='custom'");

						try {
							if (GenericWrapperElement.GetFieldForXMLPath(se.getFullXMLName() + "/project") != null) {
								try {
									List<Object> av = Permissions.getAllowedValues(user, se.getFullXMLName(), se.getFullXMLName() + "/project", "read");
									for (Object o : av) {
										Object[] sub = new Object[11];
										sub[0] = pi.getId() + "=" + o;
										sub[1] = o;
										sub[2] = "Label within the " + o + " project.";
										sub[3] = "string";
										sub[4] = false;
										sub[5] = "Label within the " + o + " project.";
										sub[6] = se.getFullXMLName();
										sub[7] = 2;
										sub[8] = true;
										sub[9] = true;
										sub[10] = o;
										label_fields.add(sub);

										for (List cf : custom_fields) {
											if (cf.get(0).equals(o)) {
												sub = new Object[11];
												sub[0] = se.getSQLName().toUpperCase() + "_FIELD_MAP=" + cf.get(1).toString().toLowerCase();
												sub[1] = cf.get(1);
												sub[2] = "Custom Field: " + cf.get(1);
												sub[3] = cf.get(2);
												sub[4] = false;
												sub[5] = "Custom Field: " + cf.get(1);
												sub[6] = se.getFullXMLName();
												sub[7] = 1;
												sub[8] = true;
												sub[9] = true;
												sub[10] = cf.get(1).toString().toLowerCase();
												fields.rows().add(sub);
											}
										}
									}
								} catch (Exception e) {
									logger.error("", e);
								}
							}
						} catch (FieldNotFoundException e) {
						}
					}

					if (elementName.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
						List<Object> av=Permissions.getAllowedValues(user,se.getFullXMLName(), se.getFullXMLName() + "/ID", "read");
						for(Object o:av) {
							if(!"*".equals(o)){
								if (null != projectScope && !projectScope.equals(o)) {
									continue;
								}
								addCustomFormFields(se, o, addedJsonFields, fields);
							}
						}
					}

					if(label_fields.size()>0){
						fields.rows().addAll(label_fields);
					}
				} catch (SQLException e) {
					logger.error("",e);
				} catch (DBPoolException e) {
						logger.error("", e);
				}
			} catch (XFTInitException e) {
	            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	            return null;
			} catch (ElementNotFoundException e) {
	            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	            return null;
			}
        }
		MediaType mt = overrideVariant(variant);

		return this.representTable(fields, mt, params);
	}


	private void addCustomFormFields(final SchemaElement se, final Object o, List<String> addedJsonFields, XFTTable fields) {
		DefaultCustomFormManager defaultCustomFormManager = null;
		try {
			defaultCustomFormManager	= XDAT.getContextService().getBeanSafely(DefaultCustomFormManager.class);
		}catch(NoClassDefFoundError ncdfe) {

		}
		if (null == defaultCustomFormManager) {
			return;
		}
		try {
			CustomFormDisplayFieldsI displayBuilder = defaultCustomFormManager.getCustomFormDisplayFieldBuilderByTypeAnnotation(CustomFormsConstants.PROTOCOL_PLUGIN_AWARE);
			if (null == displayBuilder) {
				//Looks like this instance has no protocol plugin; resolve to default
				displayBuilder = defaultCustomFormManager.getCustomFormDisplayFieldBuilderByTypeAnnotation(CustomFormsConstants.PROTOCOL_UNAWARE);
				if (displayBuilder != null) {
					displayBuilder.addDisplayFields(se, o, addedJsonFields, fields);
				}
			} else {
				//This will be a protocol aware form
				displayBuilder.addDisplayFields(se, o, addedJsonFields, fields);
			}
		}catch(CustomFormFetcherNotFoundException e) {
			logger.error("Could not get CustomFormDisplayBuilder", e);
		}

	}


}

