/*
 * DicomDB: org.nrg.dcm.DicomMetadataStore
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.TransferCapability;
import org.nrg.attr.ConversionFailureException;
import org.nrg.progress.ProgressMonitorI;

import com.google.common.base.Function;
import com.google.common.collect.SetMultimap;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public interface DicomMetadataStore extends Closeable {
    void add(URI resource, DicomObject o, Map<String,String> addCols) throws IOException,SQLException;
    void add(URI resource, DicomObject o) throws IOException,SQLException;
    void add(Iterable<URI> resources, Map<String,String> addCols, ProgressMonitorI pm) throws IOException,SQLException;
    void add(Iterable<URI> resources, ProgressMonitorI pm) throws IOException,SQLException;
    void add(Iterable<URI> resources, Map<String,String> addCols) throws IOException,SQLException;
    void add(Iterable<URI> resources) throws IOException,SQLException;
    void add(Iterable<URI> resources, Function<DicomObject,DicomObject> fn) throws IOException, SQLException;

    Set<URI> getResources() throws SQLException;
    Set<URI> getResourcesForValues(Map<?,String> values, Map<DicomAttributeIndex,ConversionFailureException> failed)
    throws IOException,SQLException;

    int getSize() throws SQLException;

    TransferCapability[] getTransferCapabilities(String role, Map<?,String> constraints) throws SQLException;
    TransferCapability[] getTransferCapabilities(String role, Iterable<URI> resources) throws SQLException;

    Set<Map<DicomAttributeIndex,String>> getUniqueCombinations(Collection<DicomAttributeIndex> tags,
            Map<DicomAttributeIndex,ConversionFailureException> failed)
            throws IOException,SQLException;

    Set<Map<DicomAttributeIndex,String>> getUniqueCombinationsGivenValues(
            Map<?,? extends String> given, Collection<? extends DicomAttributeIndex> requested,
            Map<? extends DicomAttributeIndex,ConversionFailureException> failed)
            throws IOException,SQLException;

    Set<String> getUniqueValues(DicomAttributeIndex index)
    throws ConversionFailureException,IOException,SQLException;

    SetMultimap<DicomAttributeIndex,String> getUniqueValues(Collection<DicomAttributeIndex> tags,
            Map<DicomAttributeIndex,ConversionFailureException> failed)
            throws IOException,SQLException;

    SetMultimap<DicomAttributeIndex,String> getUniqueValuesGiven(Map<?,String> given,
            Collection<DicomAttributeIndex> requested, Map<DicomAttributeIndex,ConversionFailureException> failed)
            throws IOException,SQLException;

    Map<URI,Map<DicomAttributeIndex,String>> getValuesForResourcesMatching(Collection<DicomAttributeIndex> tags,
            Map<?,String> constraints) throws SQLException;

    void remove(Iterable<URI> resources) throws SQLException;
    
    void remove(Map<?,String> constraints) throws SQLException;
}
