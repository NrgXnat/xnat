package org.nrg.automation.runners;

import org.springframework.stereotype.Component;

@Component
public class PythonScriptRunner extends AbstractScriptRunner {
    @Override
    public String getLanguage() {
        return "python";
    }

    @Override
    public String getLanguageVersion() {
        return "2.7";
    }
}
