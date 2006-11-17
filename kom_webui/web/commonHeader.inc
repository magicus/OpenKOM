<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="kom" %>

<HTML>
  <HEAD>
    <TITLE>OpenKOM</TITLE>
    <META http-equiv="Pragma" content="no-cache">
    <META http-equiv="Cache-Control" content="no-cache,no-store">
  </HEAD>
<link rel="stylesheet" href="css/default.css" type="text/css"/>

<BODY>
	<jsp:useBean id="sessionStatusBean" class="nu.rydin.kom.webui.beans.SessionStatusBean" scope="page"/> 	
	
	<!-- Set up locale, timezone etc. -->
	
	<fmt:setTimeZone value="${sessionStatusBean.user.timeZone}"/>
	<fmt:setLocale value="${sessionStatusBean.user.locale}"/>
	<fmt:setBundle basename="messages"/>
<form>
	<table>
		<tr class="app-header">
			<td colspan="2" class="app-header">
				<table class="app-status">
					<tr>
						<td><img src="images/openkom_logo.png"/></td>
						<td width="100%">
							<div>
								${sessionStatusBean.user.name} &lt;${sessionStatusBean.user.id}&gt;<br>
								<fmt:message key="common.header.total.unread"/>&nbsp;${sessionStatusBean.totalUnread}<br>
								<fmt:message key="common.header.total.numUsers"/>&nbsp;${sessionStatusBean.loggedInUsers}<br>
							<div>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td class="panel-left" valign="top">
				<table>
					<tr class="panel-left-row">
						<td>
							<fmt:message key="panel.right.search.globally"/><br><input/><br><input type="button" value="Sök"/>
						</td>
					</tr>
					<tr class="panel-left-row">
						<td>
							<fmt:message key="panel.right.search.here"/><br><input/><br><input type="button" value="Sök"/>
						</td>
					</tr>
					<tr class="panel-left-row">
						<td>
							<a href="listNews.jsp"><fmt:message key="panel.right.listNews"/></a>
						</td>
					</tr>
				</table>
			</td>
			<td class="panel-right">