package org.nrg.xnat.entities;

import static org.nrg.xnat.services.archive.impl.hibernate.HibernateFileStoreService.joinCoordinates;
import static org.nrg.xnat.services.archive.impl.hibernate.HibernateFileStoreService.splitCoordinates;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"coordinates"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
@Audited
@Slf4j
public class FileStoreInfo extends AbstractHibernateEntity {
    public FileStoreInfo() {
        log.info("Creating empty file-store info");
    }

    public FileStoreInfo(final List<String> coordinates, final String checksum, final long size, final URI storeUri) {
        this(joinCoordinates(coordinates), checksum, size, storeUri);
    }

    public FileStoreInfo(final String coordinates, final String checksum, final long size, final URI storeUri) {
        _coordinates = coordinates;
        _checksum = checksum;
        _size = size;
        _storeUri = storeUri;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Transient
    public String getLabel() {
        return FilenameUtils.getName(getCoordinates());
    }

    public String getCoordinates() {
        return _coordinates;
    }

    public void setCoordinates(final String coordinates) {
        _coordinates = coordinates;
    }

    public String getChecksum() {
        return _checksum;
    }

    public void setChecksum(final String checksum) {
        _checksum = checksum;
    }

    public long getSize() {
        return _size;
    }

    public void setSize(final long size) {
        _size = size;
    }

    public URI getStoreUri() {
        return _storeUri;
    }

    public void setStoreUri(final URI storeUri) {
        _storeUri = storeUri;
    }

    public static class Builder {
        public Builder coordinate(final String coordinate) {
            _coordinates.add(coordinate);
            return this;
        }

        public Builder coordinates(final List<String> coordinates) {
            if (!_coordinates.isEmpty()) {
                throw new RuntimeException("You can't add full coordinates with singular coordinates");
            }
            _coordinates.addAll(coordinates);
            return this;
        }

        public Builder coordinates(final String... coordinates) {
            if (!_coordinates.isEmpty()) {
                throw new RuntimeException("You can't add full coordinates with singular coordinates");
            }
            _coordinates.addAll(Arrays.asList(coordinates));
            return this;
        }

        public Builder checksum(final String checksum) {
            _checksum = checksum;
            return this;
        }

        public Builder size(final long size) {
            _size = size;
            return this;
        }

        public Builder storeUri(final URI storeUri) {
            _storeUri = storeUri;
            return this;
        }

        public FileStoreInfo build() {
            return new FileStoreInfo(_coordinates, _checksum, _size, _storeUri);
        }

        private final List<String> _coordinates = new ArrayList<>();

        private String _checksum;
        private long   _size;
        private URI    _storeUri;
    }

    private String _checksum;
    private long   _size;
    private URI    _storeUri;
    private String _coordinates;
}
