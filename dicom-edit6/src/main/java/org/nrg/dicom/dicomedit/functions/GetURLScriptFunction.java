/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.GetURLFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.http.HttpClient;
import org.nrg.dicom.mizer.service.http.IHttpClient;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by drm on 4/24/16.
 */
public class GetURLScriptFunction extends AbstractScriptFunction {

    public GetURLScriptFunction() {
        super("getURL", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {
        String url = values.get(0).asString();
        try {
            final List<String> response = getHttpClient().requestAsList(new URL(url));
            if (response.isEmpty()) {
                throw new ScriptEvaluationException("Empty response from external webservice: " + url);
            }
            StringBuffer sb = new StringBuffer();
            for( String s: response) {
                sb.append(s);
            }
            return new ConstantValue(sb.toString());
        } catch (MalformedURLException e) {
            throw new ScriptEvaluationException("Improper URL: " + url);
        } catch (IOException e) {
            throw new ScriptEvaluationException("Error connecting to external webservice: " + url, e);
        }
    }

    // expose as protected so test case can override the implementation
    protected IHttpClient getHttpClient() {
        return new HttpClient();
    }

}
