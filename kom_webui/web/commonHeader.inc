<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 

<HTML>
  <HEAD>
    <TITLE>OpenKOM</TITLE>
    <META http-equiv="Pragma" content="no-cache">
    <META http-equiv="Cache-Control" content="no-cache,no-store">
  </HEAD>
<link rel="stylesheet" href="css/default.css" type="text/css"/>

<BODY>
	<jsp:useBean id="sessionStatusBean" class="nu.rydin.kom.webui.beans.SessionStatusBean" scope="page"/> 
<form>
	<table>
		<tr class="app-header">
			<td colspan="2" class="app-header">
				<table class="app-status">
					<tr>
						<td><img src="images/openkom_logo.png"/></td>
						<td width="100%">
							<div>
								${sessionStatusBean.username} &lt;${sessionStatusBean.userId}&gt;<br>
								Totalt Olästa: ${sessionStatusBean.totalUnread}<br>
								Antal Inloggade: ${sessionStatusBean.loggedInUsers}<br>
							<div>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr class="app-header">
			<td class="panel-left">
				<table>
					<tr>
						<td>
							Sök globalt<br><input/><br><input type="button" value="Sök"/>
						</td>
					</tr>
					<tr>
						<td>
							Sök i detta möte<br><input/><br><input type="button" value="Sök"/>
						</td>
					</tr>
				</table>
			</td>
			<td class="panel-right">