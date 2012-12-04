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

import org.openstreetmap.webservices.trafficmessages.client.TrafficMessage;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class AddTrafficMessagesServiceImpl extends HttpServlet  {

	private static PersistenceManager pm = PMF.get().getPersistenceManager();
	private static final Logger LOG = Logger.getLogger(AddTrafficMessagesServiceImpl.class.getName());

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

        if (req.getParameter("eventType") != null) {
        	try {
        		int   eventid = Integer.parseInt(req.getParameter("eventType"));
        		float lat = Float.parseFloat(req.getParameter("latitude"));
        		float lon = Float.parseFloat(req.getParameter("longitude"));
        		String roadnumber = req.getParameter("roadNumber");
        		String description = req.getParameter("description");
        		Date date = new Date();
        		Date validUntil = DateFormat.getDateTimeInstance().parse(req.getParameter("validUntil"));
        		Date validFrom = DateFormat.getDateTimeInstance().parse(req.getParameter("validFrom"));

        		TrafficMessage message = new TrafficMessage(lat, lon, 0, user.getNickname(), eventid, description, validUntil );
        		message.getPrimaryLocation().setRoadNumber(roadnumber);

        		PersistenceManager pm = PMF.get().getPersistenceManager();
    			Transaction currentTransaction = pm.currentTransaction();
        		try {
//        			pm.makePersistent(message.getPrimaryLocation());
        			if (req.getParameterMap().containsKey("message")) {
        				currentTransaction.begin();
        				// edit an existing message
        				String messageStr = req.getParameter("message");
        				int messageNr = Integer.parseInt(messageStr);
        				TrafficMessage editMe = pm.getObjectById(TrafficMessage.class, messageNr);
        				pm.makeTransactional(editMe);
        				editMe.setDirection(message.getDirection());
        				editMe.setEventDescription(message.getEventDescription());
        				editMe.setEventType(message.getEventType());
//        				editMe.getPrimaryLocation().setLatitude(message.getPrimaryLocation().getLatitude());
//        				editMe.getPrimaryLocation().setLongitude(message.getPrimaryLocation().getLongitude());
//        				editMe.getPrimaryLocation().setEntityID(message.getPrimaryLocation().getEntityID());
//        				editMe.getPrimaryLocation().setEntityType(message.getPrimaryLocation().getEntityType());
        				editMe.setPrimaryLocation(message.getPrimaryLocation());
        				pm.makePersistent(editMe);
        				JDOHelper.makeDirty(pm, "primaryLocation");// just to make sure
        				currentTransaction.commit();
        			} else {
        				// create a new message
        				pm.makePersistent(message);
        			}
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
        	} catch (ParseException e) {
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

        resp.sendRedirect("/index_html.jsp");
    }

	
	public Collection<TrafficMessage> getMessages() {
		Query query = pm.newQuery(TrafficMessage.class);
		
		return new ArrayList<TrafficMessage>((Collection<TrafficMessage>) query.execute()); 	  
	}
}
