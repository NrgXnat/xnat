/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * @author: Mohana Ramaratnam (mohana@radiologics.com)
 * @since: 07-03-2021
 */
package org.nrg.xnat.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan({"org.nrg.xnat.customforms.api",
	            "org.nrg.xnat.customforms.interfaces",
	            "org.nrg.xnat.customforms.manager",
				"org.nrg.xnat.customforms.daos",
		        "org.nrg.xnat.customforms.security",
				"org.nrg.xnat.customforms.helpers",
				"org.nrg.xnat.customforms.eventListeners",
				"org.nrg.xnat.customforms.customvariable.migration",
	            "org.nrg.xnat.customforms.service"})
public class FormIOConfig {

}


