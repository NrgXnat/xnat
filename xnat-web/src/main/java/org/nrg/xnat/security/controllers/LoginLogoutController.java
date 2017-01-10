/*
 * web: org.nrg.xnat.security.controllers.LoginLogoutController
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")

public class LoginLogoutController extends BaseController{
	@RequestMapping("login")
	public String SpringLogin() {
		return "SpringLogin";
	}
}
