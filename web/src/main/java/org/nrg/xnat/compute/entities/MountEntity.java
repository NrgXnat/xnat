package org.nrg.xnat.compute.entities;

import lombok.*;
import org.nrg.xnat.compute.models.Mount;

import javax.persistence.Embeddable;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MountEntity {

    private String volumeName;
    private String localPath;
    private String containerPath;
    private boolean readOnly;

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void update(Mount mount) {
        setVolumeName(mount.getVolumeName());
        setLocalPath(mount.getLocalPath());
        setContainerPath(mount.getContainerPath());
        setReadOnly(mount.isReadOnly());
    }

    public Mount toPojo() {
        return Mount.builder()
                .volumeName(volumeName)
                .localPath(localPath)
                .containerPath(containerPath)
                .readOnly(readOnly)
                .build();
    }

    public static MountEntity fromPojo(Mount mount) {
        final MountEntity entity = new MountEntity();
        entity.update(mount);
        return entity;
    }

}
