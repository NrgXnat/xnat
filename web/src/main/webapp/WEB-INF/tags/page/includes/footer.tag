<%@ tag description="Body Footer" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
  ~ web: footer.tag
  ~ XNAT http://www.xnat.org
  ~ Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
  ~ All Rights Reserved
  ~
  ~ Released under the Simplified BSD.
  --%>

<div id="sticky-footer">
    <div>
        <div class="pad">
            <a class="xnat-version" target="_blank" href="http://www.xnat.org/" style="">XNAT v1.7</a>
            <a id="xnat_power" class="pull-right" target="_blank" href="http://www.xnat.org/" style="">
                <img src="${sessionScope.themeRoot}/images/xnat_power_small.jpg">
            </a>
        </div>
    </div>
</div>
