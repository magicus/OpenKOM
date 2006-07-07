<%@ include file="./commonHeader.inc" %>

<jsp:useBean id="newsBean" class="nu.rydin.kom.webui.beans.NewsBean" scope="page"/> 

<table>
	<thead class="list-header">
		<th>Olästa</th><th>Mötesnamn</th>
	</thead>
	<tbody>
		<c:forEach var="item" items="${newsBean.news}">
			<tr class="list-row">
				<td class="number-column">${item.unread}</td>
				<td class="conf-name-column"><a href=listMessagesInConf.jsp?conference=${item.id}&start=0>${item.name}</a></td>
			</tr>
		</c:forEach>
	</tbody>
</table>

<%@ include file="./commonFooter.inc" %>
