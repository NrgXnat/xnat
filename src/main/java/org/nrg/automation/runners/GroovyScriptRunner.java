package org.nrg.automation.runners;

import org.springframework.stereotype.Component;

@Component
public class GroovyScriptRunner extends AbstractScriptRunner {
    @Override
    public String getLanguage() {
        return "groovy";
    }

    @Override
    public String getLanguageVersion() {
        return "2.3.6";
    }
}
