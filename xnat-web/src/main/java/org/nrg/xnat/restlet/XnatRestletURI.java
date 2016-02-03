package org.nrg.xnat.restlet;

import org.restlet.util.Template;

public @interface XnatRestletURI {
	String value();
	int matchingMode() default Template.MODE_STARTS_WITH;
}
