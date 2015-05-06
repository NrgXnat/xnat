package org.nrg.automation.runners;

import org.springframework.stereotype.Component;

@Component
public class JavaScriptScriptRunner extends AbstractScriptRunner {
    @Override
    public String getLanguage() {
        return "JavaScript";
    }

    @Override
    public String getLanguageVersion() {
        return "1.6";
    }
}
