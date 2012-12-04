<%@ page contentType="xml;charset=UTF-8" language="java"
%><%@ page import="java.util.List"
%><%@ page import="javax.jdo.PersistenceManager"
%><%@ page import="com.google.appengine.api.users.User"
%><%@ page import="com.google.appengine.api.users.UserService"
%><%@ page import="com.google.appengine.api.users.UserServiceFactory"
%><%@ page
	import="org.openstreetmap.webservices.trafficmessages.client.TrafficMessage"
	%><%@ page
	import="org.openstreetmap.webservices.trafficmessages.client.Location"
	%><%@ page
	import="org.openstreetmap.webservices.trafficmessages.server.PMF"
	%><%@ page import="java.text.DateFormat"
	%><%@page import="java.util.Date"
	%><%@page import="java.util.LinkedList"
	%><%@page import="java.io.PrintWriter"
%><trafficmeages>
<%
    String pageStr = request.getParameter("page");
    if (pageStr == null) {
    	pageStr = "0";
    }
    int pagei = Integer.parseInt(pageStr);
    final int pageSize = 500;
	PersistenceManager pm = PMF.get().getPersistenceManager();
	String query = "select from " + TrafficMessage.class.getName()
			+ " order by myValidFrom desc range " + (pagei * pageSize) + ","
			+ (pagei * pageSize + pageSize);
	List<TrafficMessage> messages = new LinkedList();
	messages = (List<TrafficMessage>) pm.newQuery(query).execute();
	for (TrafficMessage g : messages) {
%>  <trafficmessage id="<%=g.getEventID()%>" type="<%=g.getEventType()%>">
<%if (g.getUsers() == null) {
%>    <user/>
<%} else {
%>    <user><%=g.getUsers()%></user>
<%}
%>    <description><%=g.getEventDescription()%></description>
    <primaryLocation lat="<%=g.getPrimaryLocation().getLatitude()%>" lon="<%=g.getPrimaryLocation().getLongitude()%>">
<%
		if (g.getPrimaryLocation().getRoadNumber() == null) {
%>       <roadnumber/>
<%} else {
%>       <roadnumber><%=g.getPrimaryLocation().getRoadNumber()%></roadnumber>
<%}
%>    </primaryLocation>
    <secondaryLocations>
<% List<Location> secondary = g.getSecondaryLocations();
   for(Location sec : secondary) {%>
	    <secondaryLocation lat="<%=sec.getLatitude()%>" lon="<%=sec.getLongitude()%>">
	    <%
	    		if (sec.getRoadNumber() == null) {
	    %>       <roadnumber/>
	    <%} else {
	    %>       <roadnumber><%=sec.getRoadNumber()%></roadnumber>
	    <%}
	    %>
	    </secondaryLocation>	   
<%   }
%>    </secondaryLocations>
  </trafficmessage>
<%
		}
%></trafficmeages>
