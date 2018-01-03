package org.nrg.xnat.restlet.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.springframework.web.filter.GenericFilterBean;

public class SimpleCORSFilter extends GenericFilterBean {

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest  request  = (HttpServletRequest)  req;
		
		try{
			if (XDAT.getBoolSiteConfigurationProperty("enableCors", Boolean.FALSE)) {
				
				response.setHeader("Access-Control-Allow-Origin",      this.getConfig("corsAllowedOrigin",      "*"));
				response.setHeader("Access-Control-Allow-Credentials", this.getConfig("corsAllowedCredentials", "true"));
				response.setHeader("Access-Control-Allow-Methods",     this.getConfig("corsAllowedMethods",     "POST, GET, PUT, OPTIONS, DELETE"));
				response.setHeader("Access-Control-Max-Age",           this.getConfig("corsMaxAge",             "3600"));
				response.setHeader("Access-Control-Allow-Headers",     this.getConfig("corsAllowedHeaders",     "Vary,User-Agent,Connection,Accept-Language,User-Agent, Host,Access-Control-Request-Method,Origin, Content-Type, Accept, X-Requested-With, Authorization, Access-Control-Request-Method,Access-Control-Request-Headers,Host,User-Agent,Accept,Accept-Language,Origin,Access-Control-Request-Method,Connection"));
				
				if ("OPTIONS".equals(request.getMethod())) {
					response.getWriter().print("OK");
					response.flushBuffer();
				} else {
					chain.doFilter(request, response);
				}
			} else {
				chain.doFilter(request, response);
			}
		}catch(Exception ex){
			chain.doFilter(request, response);
		}
	}
	
	public String getConfig(String label, String thedefault) {
		try {
			return XDAT.getSiteConfigurationProperty(label, thedefault);
		} catch (ConfigServiceException e) {
			return thedefault;
		}
	}
}