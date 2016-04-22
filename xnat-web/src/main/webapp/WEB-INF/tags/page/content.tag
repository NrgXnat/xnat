<%@ tag description="Body Element" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>
<%@ taglib prefix="incl" tagdir="/WEB-INF/tags/page/includes" %>

<%@ attribute name="id" %>
<%@ attribute name="className" %>
<%@ attribute name="bodyTop" %>
<%@ attribute name="bodyBottom" %>

<body id="${id}" class="${className}">

<input type="hidden" name="pageName" value="${requestScope.pageName}">

${bodyTop}

<incl:header/>

<jsp:doBody/>

<incl:footer/>

${bodyBottom}

</body>
