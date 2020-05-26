package org.nrg.xnat.services.archive.impl.hibernate;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.entities.FileStoreInfo;
import org.springframework.stereotype.Repository;

@Repository
public class FileStoreInfoDAO extends AbstractHibernateDAO<FileStoreInfo> {
    /**
     * Indicates whether a file-store entry with the specified coordinates exists in the system.
     *
     * @param coordinates The coordinates to test for.
     *
     * @return Returns <b>true</b> if an entry with the specified coordinates exists, <b>false</b> otherwise.
     */
    public boolean existsByCoordinates(final String coordinates) {
        return exists("coordinates", coordinates);
    }

    public FileStoreInfo findByCoordinates(final String coordinates) {
        return findByUniqueProperty("coordinates", coordinates);
    }
}
