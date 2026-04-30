/*
 * DicomEdit: org.nrg.dcm.edit.fn.GetURL
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.edit.fn;

import org.nrg.dicom.mizer.scripts.ScriptFunction;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.http.HttpClient;
import org.nrg.dicom.mizer.service.http.IHttpClient;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class GetURL implements ScriptFunction {
    public static final String name = "getURL";

    public Value apply(List<? extends Value> args) throws ScriptEvaluationException {
        final Value value = args.getFirst();
        return new AbstractMizerValue(value) {
            @Override
            public SortedSet<Long> getTags() {
                return EMPTY_TAGS;
            }

            @Override
            public Set<Variable> getVariables() { return EMPTY_VARIABLES; }

            @Override
            public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
                return request(value.on(dicomObject));
            }

            @Override
            public String on(final Map<Integer,String> map) throws ScriptEvaluationException {
                return request(value.on(map));
            }

            @Override
            public void replace(final Variable v) {
                value.replace(v);
            }

            @Override
            public String toString() {
                return "getURL \"" + value + "\"";
            }
        };
    }

    private String request(final String url) throws ScriptEvaluationException {
        try {
            final String response = getHttpClient().request(URI.create(url).toURL());
            if (response == null) {
                throw new ScriptEvaluationException("Empty response from external webservice, " + url);
            }
            return response.trim();
        } catch (MalformedURLException e) {
            throw new ScriptEvaluationException("Improper URL, " + url);
        } catch (IOException e) {
            throw new ScriptEvaluationException("Error connecting to external webservice, " + url, e);
        }
    }

    // expose as protected so test case can override the implementation
    protected IHttpClient getHttpClient() {
        return new HttpClient();
    }
}
