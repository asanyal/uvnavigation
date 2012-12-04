package org.openstreetmap.webservices.trafficmessages.server;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
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
public class RemoveTrafficMessagesServiceImpl extends HttpServlet {

	private static PersistenceManager pm = PMF.get().getPersistenceManager();
	private static final Logger LOG = Logger.getLogger(RemoveTrafficMessagesServiceImpl.class.getName());
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
    		           final HttpServletResponse resp) throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

        try {
			int   messageid = Integer.parseInt(req.getParameter("message"));
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				TrafficMessage msg = pm.getObjectById(TrafficMessage.class, messageid);
				if (msg == null) {
					LOG.log(Level.INFO, "no such message");
			    	req.setAttribute("error", "no such message");
			    } else {
//			    	pm.deletePersistent(msg.getPrimaryLocation());
//			    	pm.deletePersistentAll(msg.getSecondaryLocations());
			    	pm.deletePersistent(msg);
			    }
//			    String query = "select from " + TrafficMessage.class.getName() + " WHERE myEventID == " + messageid;
//			    List<TrafficMessage> messages = (List<TrafficMessage>)
//			          pm.newQuery(query).execute();
//			    if (messages.size() == 0) {
//					LOG.log(Level.INFO, "no such message");
//			    	req.setAttribute("error", "no such message");
//			    }
//			    for (TrafficMessage trafficMessage : messages) {
//					pm.getObjectById(arg0, arg1)deletePersistent(trafficMessage);
//				}
			} finally {
			    pm.close();
			}
		} catch (NumberFormatException e) {
			req.setAttribute("error", "Illegal message-id");
			LOG.log(Level.WARNING, "Illegal message-id given");
			resp.sendError(501, "Illegal message-id given");
			return;
		} catch (Exception e) {
			req.setAttribute("error", "internal error");
			LOG.log(Level.WARNING, "Internal error", e);
			resp.sendError(501, "internal error");
			return;
		}

        resp.sendRedirect("/index_html.jsp");
    }
}
