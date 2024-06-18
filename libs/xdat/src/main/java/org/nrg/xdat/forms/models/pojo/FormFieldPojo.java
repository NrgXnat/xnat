/** Copyright Radiologics Inc 2021
 * @author Mohana Ramaratnam <mohana@radiologics.com>
 */
package org.nrg.xdat.forms.models.pojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class FormFieldPojo {
	 private String label;
	 private String key;
	 private String fieldName;
	 private String type;
	 private UUID formUUID;
	 @Singular
	 private List<String> jsonPaths;
}
