<%@ tag description="Document Wrapper" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>
<%--
  ~ web: wrapper.tag
  ~ XNAT http://www.xnat.org
  ~ Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
  ~ All Rights Reserved
  ~
  ~ Released under the Simplified BSD.
  --%>

<!DOCTYPE html>
<!--[if lt IE 9]><html class="ie ltie9 ltie10 no-js"><![endif]-->
<!--[if IE 9]><html class="ie ie9 ltie10 no-js"><![endif]-->
<!--[if gt IE 9]><!--><html class="no-js"><!--<![endif]-->

<c:if test="${empty requestScope.hasInit}">
    <pg:init/>
</c:if>

<jsp:doBody/>

</html>
