package org.nrg.xdat.security;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

public class RegExpValidator implements PasswordValidator{

	String regexp="";
	String message="Password is not sufficiently complex.";

    @SuppressWarnings("unused")
	public RegExpValidator(){
		
	}

    public RegExpValidator(final String regexp, final String message) {
        setRegexp(regexp);
        setMessage(message);
    }

    @Override
	public boolean isValid(String password, XDATUser user) {
        return StringUtils.isBlank(regexp) || Pattern.matches(regexp, password);
	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		if(regexp!=null && !(regexp.equals(""))){
			this.regexp = regexp;
		}
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		if(message!=null && !(message.equals(""))){
			this.message = message;
		}
	}
}
