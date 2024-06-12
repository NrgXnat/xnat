/*
 * framework: org.nrg.framework.services.MarshallerCacheService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import java.util.List;

import org.nrg.framework.exceptions.NrgServiceException;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.w3c.dom.Document;

public interface MarshallerCacheService extends Marshaller, Unmarshaller {
    
    /**
     * Marshal the given object to a string-based XML representation.
     *
     * @param object The object to be marshaled.
     * @return A string-based XML representation of the marshaled object.
     * @throws NrgServiceException When something goes wrong.
     */
    abstract public String marshal(Object object) throws NrgServiceException;

    abstract public Document marshalToDocument(Object object);

    /**
     * Gets the marshalable packages.
     *
     * @return the marshalable packages
     */
    abstract public List<String> getMarshalablePackages();
    
    /**
     * Sets the marshalable packages.
     *
     * @param packages the new marshalable packages
     */
    abstract public void setMarshalablePackages(List<String> packages);
}
