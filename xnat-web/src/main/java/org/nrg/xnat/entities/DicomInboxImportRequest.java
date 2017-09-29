package org.nrg.xnat.entities;

import lombok.*;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.*;
import java.util.Map;

import static org.nrg.xnat.entities.DicomInboxImportRequest.Status.Queued;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DicomInboxImportRequest extends AbstractHibernateEntity {
    public enum Status {
        Queued,
        Trawling,
        Importing,
        Accepted,
        Processed,
        Failed,
        Completed
    }

    @NonNull
    private String username;

    @NonNull
    @ElementCollection //(fetch=FetchType.EAGER)
    // @MapKey(name = "parameter_name")
    // @CollectionTable(name = "dicom_inbox_import_request_parameter_values") //, joinColumns = @JoinColumn(name = "string_id"))
    // @Column(name="value")
    private Map<String, String> parameters;

    @NonNull
    private String sessionPath;

    @NonNull
    @Builder.Default
    private Status status = Queued;
}