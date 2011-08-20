/**
 * Subscriber
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 17, 2011
 */
package org.nrg.xdat.notifications.api;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

/**
 * The Class Subscriber.
 */
@Entity
public class Subscriber {
    @Id
    @SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
    public long getId() {
        return _id;
    }

    public void setId(long id) {
        _id = id;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public List<String> getEmails() {
        return _emails;
    }

    public void setEmails(List<String> emails) {
        _emails = emails;
    }

    private long _id;
    private String _name;
    private List<String> _emails;
}
