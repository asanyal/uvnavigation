<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="java.util.List"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.google.appengine.api.users.User"%>
<%@ page import="com.google.appengine.api.users.UserService"%>
<%@ page import="com.google.appengine.api.users.UserServiceFactory"%>
<%@ page
	import="org.openstreetmap.webservices.trafficmessages.client.TrafficMessage"%>
<%@ page
	import="org.openstreetmap.webservices.trafficmessages.client.Location"%>
<%@ page
	import="org.openstreetmap.webservices.trafficmessages.server.PMF"%>
<%@ page import="java.text.DateFormat"%>


<%@page import="java.util.Date"%>
<%@page import="java.util.LinkedList"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="org.apache.jasper.tagplugins.jstl.ForEach"%><html>
<head>
<link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
</head>

<body>

<%
	String error = (String) request.getAttribute("error");
	if (error != null) {
%>
<p><%=error%></p>
<%
	}
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();
	if (user == null) {
%>
<p>Hello! <a
	href="<%=userService.createLoginURL(request.getRequestURI())%>">Sign
in</a> to post or edit traffic-messages.</p>
<%
	} else {
%>
		<p>Hello, <%=user.getNickname()%>! (You can <a
			href="<%=userService.createLogoutURL(request.getRequestURI())%>">sign
		out</a>.)</p>

<%
	String messageStr = request.getParameter("message");
		if (messageStr == null) {
			messageStr = "0";
		}
		int messageNr = Integer.parseInt(messageStr);
		PersistenceManager pm = PMF.get().getPersistenceManager();
		TrafficMessage message = null;
		message = (TrafficMessage) pm.getObjectById(
				TrafficMessage.class, messageNr);
		if (message == null) {
%>
<p>No such traffic messages.</p><br/>
<a href="index_html.jsp">go back</a>
<%
	} else {
%>
<form action="/postmessage" method="post">
<input type="hidden" name="message" value="<%=messageNr%>"/>
<table>
	<tr>
		<td>Event type:</td>
		<td><select name="eventType">
		<%
			TrafficMessage.TYPES[] types = TrafficMessage.TYPES
							.values();
			for (TrafficMessage.TYPES type : types) {
				String selected = "";
				if (message.getEventType() == type.ordinal()) {
					selected = "selected=\"true\"";
				}
		%><option value="<%=type.ordinal()%>" <%=selected%>><%=type.name()%></option><%
			}
		%>
		</select></td>
	</tr>
	<tr>
		<td>Description:</td>
		<td><textarea name="description" rows="3" cols="60"><%=message.getEventDescription()%></textarea></td>
	</tr>
	<tr>
		<td>valid from:</td>
		<td><input type="text" name="validFrom"
			value="<%=DateFormat.getDateTimeInstance().format(
									message.getValidFrom()==null?new Date():message.getValidFrom())%>" /></td>
	</tr>
	<tr>
		<td>valid until:</td>
		<td><input type="text" name="validUntil"
			value="<%=DateFormat.getDateTimeInstance().format(
									message.getValidUntil()==null?new Date():message.getValidUntil())%>" /></td>
	</tr>
	<tr>
		<td colspan="2"><b>primary location</b></td>
	</tr>
	<tr>
		<td>latitude:</td>
		<td><input type="text" name="latitude" lengh="10" value="<%=message.getPrimaryLocation().getLatitude()%>"/></td>
	</tr>
	<tr>
		<td>longitude:</td>
		<td><input type="text" name="longitude" lengh="10" value="<%=message.getPrimaryLocation().getLongitude()%>"/></td>
	</tr>
	<tr>
		<td>ref:</td>
		<td><input type="text" name="roadNumber"  value="<%=message.getPrimaryLocation()
											.getRoadNumber()%>" /></td>
	</tr>
	<tr>
		<td>direction of traffic:</td>
		<td><select name="direction">
			<option value="0">north</option>
			<option value="90">east</option>
			<option value="180">south</option>
			<option value="270">west</option>
		</select></td>
	</tr>
	<tr>
		<td colspan="2" /><input type="submit" value="Edit traffic message" /></td>
	</tr>
</table>
</form>
<%
     List<Location> secondary = message.getSecondaryLocations();
     if (secondary != null && secondary.size() > 0) {
%> 
This message extends to the following secondary locations:<br/>
  <table>
<%
     for(Location sec : secondary) {%>
      <tr>
   	<td><%=(sec.getRoadNumber() == null) ? ""
				: sec.getRoadNumber()
	%>(<%=sec.getLatitude()
	%>,<%=sec.getLongitude()%>)</td>
	 <td>[edit]</td>
	 <td>[x]</td>
      </tr>
<%    }
%>
   </table>
<% 
      } // has secondary locations
%>


<form action="/postlocation" method="post">
<input type="hidden" name="message" value="<%=messageNr%>"/>
<table>
	<tr>
		<td>latitude:</td>
		<td><input type="text" name="latitude" lengh="10" value=""/></td>
	</tr>
	<tr>
		<td>longitude:</td>
		<td><input type="text" name="longitude" lengh="10" value=""/></td>
	</tr>
	<tr>
		<td>ref:</td>
		<td><input type="text" name="roadNumber"  value="" /></td>
	</tr>
	<tr>
		<td colspan="2" /><input type="submit" value="Add secondary location" /></td>
	</tr>
</table>
</form>

<% 
	} // message exists
	} // logged in
%>

</body>
</html>
