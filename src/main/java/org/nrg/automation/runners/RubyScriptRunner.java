package org.nrg.automation.runners;

import org.springframework.stereotype.Component;

@Component
public class RubyScriptRunner extends AbstractScriptRunner {
    @Override
    public String getLanguage() {
        return "ruby";
    }

    @Override
    public String getLanguageVersion() {
        return "1.7.19";
    }
}
