<%@ include file="./commonHeader.inc" %>

<jsp:useBean id="listBean" class="nu.rydin.kom.webui.beans.ConferenceContentBean" scope="page"> 
	<jsp:setProperty name="listBean" property="start" param="start"/> 
	<jsp:setProperty name="listBean" property="length" value="50"/> 
	<jsp:setProperty name="listBean" property="conferenceId" param="conference"/> 
</jsp:useBean>

<table>
	<thead class="list-header">
		<th>Lokalt</th><th>Globalt</th><th>Datum</th><th>Författare</th><th>Ärende</th>
	</thead>
	<tbody>
		<c:forEach var="item" items="${listBean.messages}">
			<tr class="list-row">
				<td class="local-textid-column">${item.localId}</td>
				<td class="global-textid-column"><a href=listMessages?conference=${item.globalId}>${item.globalId}</a></td>
				<td class="time-column"><fmt:formatDate value="${item.timestamp}" type="date" dateStyle="short"/>&nbsp;<fmt:formatDate value="${item.timestamp}" type="time" dateStyle="medium"/></td></td>
				<td class="author-column"><a href=userInfo?user=${item.authorId}>${item.authorName}</a></td>
				<td class="subject-column"><a href=listMessages?conference=${item.globalId}>${item.subject}</a></td>
			</tr>
		</c:forEach>
	</tbody>
</table>

<%@ include file="./commonFooter.inc" %>
