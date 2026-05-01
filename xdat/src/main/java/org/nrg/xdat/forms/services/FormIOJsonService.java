/** Copyright Radiologics Inc 2021
 * @author Mohana Ramaratnam <mohana@radiologics.com>
 */
package org.nrg.xdat.forms.services;

import java.util.List;

import javax.annotation.Nonnull;

import org.nrg.xdat.forms.models.pojo.FormFieldPojo;


public interface FormIOJsonService {

	   /**
	   * Retrieve FormIOJsons for the given dataType at the Site and the Project level
	   * All forms at the path /datatype/{dataType}/..... are returned
	   * @param dataType - the xnat dataType (xnat:mrSessionData, xnat:subjectData, etc)
	   * @param project - the project of the experiment or subject
	   * @param protocol - the protocol name of the protocol which is configured for the project
	   * @param visit - the visit name within the protocol
	   * @param visitSubType - the subtype within the visit for the datatype.
	   * @return
	   */
	   List<FormFieldPojo> getFormsForObject(@Nonnull String dataType,  String project, String protocol, String visit, String visitSubType);

}
