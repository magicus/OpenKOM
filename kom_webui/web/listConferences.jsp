<%@ include file="./commonHeader.inc" %>

<jsp:useBean id="newsBean" class="nu.rydin.kom.webui.beans.ConferenceCatalogBean" scope="page"> 
	<jsp:setProperty name="newsBean" property="sort" value="${empty param.sort ? 0 : param.sort}"/> 
</jsp:useBean>
<c:set var="start" value="${empty param.start ? 0 : param.start}"/>

<table>
	<thead class="list-header">
		<th><a class="list-header" href=listConferences.jsp?sort=0><fmt:message key="listConferences.order"/></a></th>
		<th><a class="list-header" href=listConferences.jsp?sort=1><fmt:message key="listConferences.unread"/></a></th>
		<th><a class="list-header" href=listConferences.jsp?sort=2><fmt:message key="listConferences.conference"/></a></th>
	</thead>
	<tbody>
		<c:forEach var="item" items="${newsBean.news}" begin="${start}" end="${start + 24}">
			<tr class="list-row">
				<td class="number-column">${item.order}</td>			
				<td class="number-column">${item.unread}</td>
				<td class="conf-name-column"><a href=listMessagesInConf.jsp?conference=${item.id}&start=0>${item.name}</a></td>
			</tr>
		</c:forEach>
	</tbody>
</table>

<kom:pageNavigation 
	url="listConferences.jsp?dummy=0"
	start="${start}"
	length="25"
	count="${fn:length(newsBean.news)}"/>

<%@ include file="./commonFooter.inc" %>
