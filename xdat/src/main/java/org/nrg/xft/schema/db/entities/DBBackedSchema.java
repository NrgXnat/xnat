/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xft.schema.db.entities;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import jakarta.persistence.*;


@Entity
public class DBBackedSchema extends AbstractHibernateEntity  {
    private String path = null;
    private String content = null;
    private String name = null;

    @Column(unique = true, nullable = false)
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Column(nullable = false, columnDefinition="TEXT")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Column(unique = true, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
