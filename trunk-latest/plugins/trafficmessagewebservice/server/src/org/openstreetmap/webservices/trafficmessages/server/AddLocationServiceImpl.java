package org.openstreetmap.webservices.trafficmessages.server;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openstreetmap.webservices.trafficmessages.client.Location;
import org.openstreetmap.webservices.trafficmessages.client.TrafficMessage;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class AddLocationServiceImpl extends HttpServlet  {

	private static PersistenceManager pm = PMF.get().getPersistenceManager();
	private static final Logger LOG = Logger.getLogger(AddLocationServiceImpl.class.getName());

	/**
     * {@inheritDoc}
     */
	public void doGet(final HttpServletRequest req,
	           final HttpServletResponse resp) throws IOException {
    	doPost(req, resp);
    }

	/**
     * {@inheritDoc}
     */
	public void doPost(final HttpServletRequest req,
    		           final HttpServletResponse resp)
                throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

        if (req.getParameter("message") != null) {
        	try {
        		float lat = Float.parseFloat(req.getParameter("latitude"));
        		float lon = Float.parseFloat(req.getParameter("longitude"));
        		String roadnumber = req.getParameter("roadNumber");


        		PersistenceManager pm = PMF.get().getPersistenceManager();
    			Transaction currentTransaction = pm.currentTransaction();
        		try {
        			currentTransaction.begin();
        			String messageStr = req.getParameter("message");
        			int messageNr = Integer.parseInt(messageStr);
        			TrafficMessage editMe = pm.getObjectById(TrafficMessage.class, messageNr);
        			Location secondaryLocation = new Location(editMe, lat, lon);
        			if (roadnumber != null && roadnumber.trim().length() > 0) {
        				secondaryLocation.setRoadNumber(roadnumber);
        			}
        			editMe.getSecondaryLocations().add(secondaryLocation);
        			pm.makeTransactional(editMe);
        			JDOHelper.makeDirty(pm, "secondaryLocations");// just to make sure
        			currentTransaction.commit();
        		} finally {
        			if (currentTransaction.isActive()) {
        				currentTransaction.rollback();
        			}
        			pm.close();
        		}
        	} catch (NumberFormatException e) {
        		req.setAttribute("error", "Illegal values");
        		LOG.log(Level.WARNING, "Illegal values given");
    			resp.sendError(501, "Illegal values given");
    			return;
        	} catch (Exception e) {
        		req.setAttribute("error", "internal error");
        		LOG.log(Level.WARNING, "internal error", e);
    			resp.sendError(501, "internal error");
    			return;
        	}
        }

        resp.sendRedirect("/edit_html.jsp?message=" + req.getParameter("message"));
    }

	
	public Collection<TrafficMessage> getMessages() {
		Query query = pm.newQuery(TrafficMessage.class);
		
		return new ArrayList<TrafficMessage>((Collection<TrafficMessage>) query.execute()); 	  
	}
}
