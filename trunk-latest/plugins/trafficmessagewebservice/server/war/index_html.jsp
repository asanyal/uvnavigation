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
	if (user != null) {
%>
<p>Hello, <%=user.getNickname()%>! (You can <a
	href="<%=userService.createLogoutURL(request.getRequestURI())%>">sign
out</a>.)</p>
<%
	} else {
%>
<p>Hello! <a
	href="<%=userService.createLoginURL(request.getRequestURI())%>">Sign
in</a> to post traffic-messages.</p>
<%
	}
%>

<%
	String pageStr = request.getParameter("page");
	if (pageStr == null) {
		pageStr = "0";
	}
	int pagei = Integer.parseInt(pageStr);
	final int pageSize = 25;
	PersistenceManager pm = PMF.get().getPersistenceManager();
	String query = "select from " + TrafficMessage.class.getName()
			+ " order by myValidFrom desc range " + (pagei * pageSize)
			+ "," + (pagei * pageSize + pageSize);
	List<TrafficMessage> messages = new LinkedList();
	try {
		messages = (List<TrafficMessage>) pm.newQuery(query).execute();
		if (messages.isEmpty()) {
%>
<p>No traffic messages.</p>
<%
  	      if (pagei > 0) {
%><a href="index_html.jsp?page=<%=(pagei - 1)%>">&lt;&lt;&lt;</a>
<%
	      }
		} else {
%>
<table>
	<%
		for (TrafficMessage g : messages) {
	%><tr>
		<td>
		<%
			if (g.getUsers() == null) {
		%>
		<p>An anonymous person wrote:</p>
		<%
			} else {
		%>
		<p><b><%=g.getUsers()%></b> reported:</p>
		<%
			}
		%>
		</td>
		<td>
		<blockquote><%=g.getEventDescription()%></blockquote>
		</td>
		<td>at: <a
			href="http://openstreetmap.org/?lat=<%=g.getPrimaryLocation().getLatitude()%>&lon=<%=g.getPrimaryLocation().getLongitude()%>&zoom=16">
		<%=(g.getPrimaryLocation()
												.getRoadNumber() == null) ? ""
										: g.getPrimaryLocation()
												.getRoadNumber()%>(<%=g.getPrimaryLocation().getLatitude()%>,
		<%=g.getPrimaryLocation().getLongitude()%>) </a>&nbsp;
		</td>
		<%if (user != null) {			
		%>
		<td><a href="/edit_html.jsp?message=<%=g.getEventID()%>">[edit]</a>&nbsp;
		</td>
		<td><a href="/removemessage?message=<%=g.getEventID()%>">[x]</a>
		</td>
		<%} else {%>
		<td style="color:grey">[edit]&nbsp;
		</td>
		<td style="color:grey">[x]
		</td>
		<%}%>
	</tr>
<% 
     List<Location> secondary = g.getSecondaryLocations();
     if (secondary != null && secondary.size() > 0) {
	 %> 
	<tr>
	  <td colspan="7">
This message extends to the following secondary locations:<br/>
       <table>
<%
       for(Location sec : secondary) {%>
           <tr>
        	<td><%=(sec.getRoadNumber() == null) ? ""
					: sec.getRoadNumber()
		%>(<%=sec.getLatitude()
		%>,<%=sec.getLongitude()%>)</td>
           </tr>
   <%    }
%>
        </table>
	  <td>
	</tr>    
<% 
     } // has secondary locations

 } // foreach message
%>
</table>
<%
	   if (pagei > 0) {
%><a href="index_html.jsp?page=<%=(pagei - 1)%>">&lt;&lt;&lt;</a>
		         <a href="index_html.jsp?page=<%=(pagei + 1)%>">&gt;&gt;&gt;</a>
               <%
       } else {
               %><a href="index_html.jsp?page=<%=(pagei + 1)%>">&gt;&gt;&gt;</a>
		        <%
	   }
    }
	pm.close();

	} catch (Exception x) {
		        		out.println("<pre>");
		        		x.printStackTrace(new PrintWriter(out));
		        		out.println("</pre>");
	}

   if (user != null) {
		        %>

<hr/>
<form action="/postmessage" method="post">
<table>
	<tr>
		<td>Event type:</td>
		<td><select name="eventType">
		<%
			TrafficMessage.TYPES[] types = TrafficMessage.TYPES.values();
				for (TrafficMessage.TYPES type : types) {
		%><option value="<%=type.ordinal()%>"><%=type.name()%></option><%
			}
		%>
		</select></td>
	</tr>
	<tr>
		<td>Description:</td>
		<td><textarea name="description" rows="3" cols="60"></textarea></td>
	</tr>
	<tr>
		<td>valid from:</td>
		<td><input type="text" name="validFrom"
			value="<%=DateFormat.getDateTimeInstance().format(new Date())%>" /></td>
	</tr>
	<tr>
		<td>valid until:</td>
		<td><input type="text" name="validUntil"
			value="<%=DateFormat.getDateTimeInstance().format(new Date())%>" /></td>
	</tr>
	<tr>
		<td colspan="2"><b>primary location</b></td>
	</tr>
	<tr>
		<td>latitude:</td>
		<td><input type="text" name="latitude" value="" lengh="10" /></td>
	</tr>
	<tr>
		<td>longitude:</td>
		<td><input type="text" name="longitude" value="" lengh="10" /></td>
	</tr>
	<tr>
		<td>ref:</td>
		<td><input type="text" name="roadNumber" value="" /></td>
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
		<td colspan="2" /><input type="submit" value="Post traffic message" /></td>
	</tr>
</table>
</form>
<%
	}
%>

</body>
</html>
