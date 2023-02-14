/** Copyright Radiologics Inc 2021
 * @author Mohana Ramaratnam <mohana@radiologics.com>
 */
package org.nrg.xdat.forms.models.pojo;
import java.util.UUID;

public abstract class FormFieldPojo {
	protected String label;
	protected String key;
	protected String fieldName;
	protected String type;
	protected UUID formUUID;

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}
	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	public UUID getFormUUID() {
	  return  formUUID;
	}	

	public void setFormUUID(UUID f) {
	  this.formUUID = f;	
	}
	
}
