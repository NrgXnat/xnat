<%@ tag description="Body Element" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>
<%@ taglib prefix="incl" tagdir="/WEB-INF/tags/page/includes" %>

<%@ attribute name="id" %>
<%@ attribute name="className" %>
<%@ attribute name="bodyTop" %>
<%@ attribute name="bodyBottom" %>

<%--
  ~ web: content.tag
  ~ XNAT http://www.xnat.org
  ~ Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
  ~ All Rights Reserved
  ~
  ~ Released under the Simplified BSD.
  --%>

<body id="${id}" class="${className}">

<input type="hidden" name="pageName" value="${requestScope.pageName}">

${bodyTop}

<incl:header/>

<jsp:doBody/>

<incl:footer/>

${bodyBottom}

</body>
