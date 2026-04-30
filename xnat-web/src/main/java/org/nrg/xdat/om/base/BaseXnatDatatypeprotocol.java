/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatDatatypeprotocol
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.XdatSearchField;
import org.nrg.xdat.om.XnatDatatypeprotocol;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatDatatypeprotocol;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

import javax.annotation.Nonnull;

import java.io.Serial;
import java.util.*;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatDatatypeprotocol extends AutoXnatDatatypeprotocol {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatDatatypeprotocol(ItemI item)
	{
		super(item);
	}

	public BaseXnatDatatypeprotocol(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatDatatypeprotocol(UserI user)
	 **/
	public BaseXnatDatatypeprotocol()
	{}

	public BaseXnatDatatypeprotocol(Hashtable properties, UserI user)
	{
		super(properties,user);
	}


    public XdatStoredSearch getDefaultSearch(XnatProjectdataI project){
        XdatStoredSearch xss = super.getDefaultSearch((XnatProjectdata)project);

        for(XnatFielddefinitiongroupI group : this.getDefinitions_definition()){
            for(XnatFielddefinitiongroupFieldI field : group.getFields_field()){

                XdatSearchField xsf = new XdatSearchField(this.getUser());
                xsf.setElementName(this.getDataType());
                String fieldID=null;
                if (field.getType().equals("custom"))
                {
                    fieldID=this.getDatatypeSchemaElement().getSQLName().toUpperCase() + "_FIELD_MAP="+field.getName().toLowerCase();
                    
                }else{
                    try {
                        SchemaElement se=SchemaElement.GetElement(this.getDataType());
                        
                        try {
                            DisplayField df=se.getDisplayFieldForXMLPath(field.getXmlpath());
                            if (df!=null){
                                fieldID=df.getId();
                            }
                        } catch (Exception e) {
                            logger.error("",e);
                        }
                    } catch (XFTInitException e) {
                        logger.error("",e);
                    } catch (ElementNotFoundException e) {
                        logger.error("",e);
                    }
                }
                    
                if (fieldID!=null){
                    xsf.setFieldId(fieldID);

                    xsf.setHeader(field.getName());
                    xsf.setType(field.getDatatype());
                    xsf.setSequence(xss.getSearchField().size());
                    if (field.getType().equals("custom"))xsf.setValue(field.getName().toLowerCase());
                    try {
                        xss.setSearchField(xsf);
                    	System.out.println("LOADED " + field.getXmlpath());
                    } catch (Exception e) {
                        logger.error("",e);
                    	System.out.println("FAILED to load " + field.getXmlpath());
                    }
                }else{
                	System.out.println("FAILED to load " + field.getXmlpath());
                }
            }
        }
        
            

        return xss;
    }

    /**
     * Special implementation for this class.
     * 
     * @param object The object to check for equality.
     *               
     * @return Returns true if the data contained by the two objects is unchanged, false otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if (!XnatDatatypeprotocolI.class.isAssignableFrom(object.getClass())) {
            return false;
        }

        final XnatDatatypeprotocolI other = (XnatDatatypeprotocolI) object;

        if (isProtocolHeaderDataEqual(this, other)) {
            return false;
        }

        // If the state of the protocol vary from project specific to site wide or vice versa,
        // then they're different. Quit now.
        if (isProjectSpecific(this) != isProjectSpecific(other)) {
            return false;
        }

        // Lastly check the field definition groups. If they're equal, then the data-type
        // protocol objects are equal.
        return !areFieldDefinitionGroupsEqual(this, other);
    }

    public static boolean isProtocolHeaderDataEqual(final XnatDatatypeprotocolI first, final XnatDatatypeprotocolI second) {
        return StringUtils.equals(first.getId(), second.getId()) &&
               StringUtils.equals(first.getName(), second.getName()) &&
               StringUtils.equals(first.getDescription(), second.getDescription()) &&
               StringUtils.equals(first.getDataType(), second.getDataType()) &&
               StringUtils.equals(first.getXSIType(), second.getXSIType()) &&
               Objects.equals(first.getXnatAbstractprotocolId(), second.getXnatAbstractprotocolId());
    }

    public static boolean isProjectSpecific(final XnatDatatypeprotocolI protocol) {
        for (final XnatFielddefinitiongroupI definition : protocol.getDefinitions_definition()) {
            final List<XnatFielddefinitiongroupFieldI> fields = definition.getFields_field();
            // If there are no fields in this group, we don't care whether the group is project specific
            // or not, so we can just continue on.
            if (fields.isEmpty()) {
                continue;
            }
            // If it's not project specific, we're done here, this update will affect the entire site.
            if (!definition.getProjectSpecific()) {
                return false;
            }
        }
        // If everything was project specific, then return that.
        return true;
    }

    public static boolean areFieldDefinitionGroupsEqual(final XnatDatatypeprotocolI first, final XnatDatatypeprotocolI second) {
        final List<XnatFielddefinitiongroupI> firstDefinitions  = first.getDefinitions_definition();
        final List<XnatFielddefinitiongroupI> secondDefinitions = second.getDefinitions_definition();
        if (!areListsBasicMatches(firstDefinitions, secondDefinitions)) {
            return false;
        }

        // Go through
        for (int definitionIndex = 0; definitionIndex < firstDefinitions.size(); definitionIndex++) {
            final XnatFielddefinitiongroupI firstDefinition = firstDefinitions.get(definitionIndex);
            final XnatFielddefinitiongroupI secondDefinition = secondDefinitions.get(definitionIndex);

            final List<XnatFielddefinitiongroupFieldI> firstFields = firstDefinition.getFields_field();
            final List<XnatFielddefinitiongroupFieldI> secondFields = secondDefinition.getFields_field();

            if (!areListsBasicMatches(firstFields, secondFields)) {
                return false;
            }

            // If there are no fields in this group, we don't care whether the group is project specific
            // or not, so we can just continue on.
            if (firstFields.isEmpty() && secondFields.isEmpty()) {
                continue;
            }

            // Go through each field...
            for (int fieldIndex = 0; fieldIndex < firstFields.size(); fieldIndex++) {
                // Get the first one and the second one...
                final XnatFielddefinitiongroupFieldI firstItem = firstFields.get(fieldIndex);
                final XnatFielddefinitiongroupFieldI secondItem = secondFields.get(fieldIndex);

                // And compare everything. If anything changed...
                if (!Objects.equals(firstItem.getSequence(), secondItem.getSequence()) ||
                    !Objects.equals(firstItem.getRequired(), secondItem.getRequired()) ||
                    !Objects.equals(firstItem.getGroup(), secondItem.getGroup()) ||
                    !Objects.equals(firstItem.getDatatype(), secondItem.getDatatype()) ||
                    !Objects.equals(firstItem.getType(), secondItem.getType()) ||
                    !Objects.equals(firstItem.getName(), secondItem.getName()) ||
                    !Objects.equals(firstItem.getXmlpath(), secondItem.getXmlpath()) ||
                    !Objects.equals(firstItem.getXSIType(), secondItem.getXSIType()) ||
                    !compareValueLists(firstItem.getPossiblevalues_possiblevalue(), secondItem.getPossiblevalues_possiblevalue())) {
                    // Then the items aren't equal.
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean compareValueLists(final List<XnatFielddefinitiongroupFieldPossiblevalueI> first, final List<XnatFielddefinitiongroupFieldPossiblevalueI> second) {
        if (!areListsBasicMatches(first, second)) {
            return false;
        }

        for (int index = 0; index < first.size(); index++) {
            final XnatFielddefinitiongroupFieldPossiblevalueI currentValue = first.get(index);
            final XnatFielddefinitiongroupFieldPossiblevalueI updatedValue = second.get(index);

            // As soon as something doesn't match, they're different so return false without checking any more.
            if (!StringUtils.equals(currentValue.getDisplay(), updatedValue.getDisplay()) ||
                !StringUtils.equals(currentValue.getPossiblevalue(), updatedValue.getPossiblevalue())) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean areListsBasicMatches(final List<T> first, final List<T> second) {
        final boolean isCurrentNull = first == null;
        final boolean isUpdatedNull = second == null;
        if (isCurrentNull && isUpdatedNull) {
            return true;
        }
        if (isCurrentNull || isUpdatedNull) {
            return false;
        }
        if (first.isEmpty() && second.isEmpty()) {
            return true;
        }
        return first.size() == second.size();
    }
}
