package org.nrg.xapi.pages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/view")
@Slf4j
public class PageController extends AbstractXapiPageController {
    @RequestMapping("/**")
    public String getDisplayView(final HttpServletRequest request) {
        log.info("Got a request for {}", request.getContextPath());
        return getWildCardPath(request);
    }
}
