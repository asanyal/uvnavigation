package org.openstreetmap.webservices.trafficmessages.client;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType=IdentityType.APPLICATION)
public class TrafficMessage {

	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
	private Long myEventID;

	private int mySourceSystem;
	private String myUsers;
	private int myEventType;
    /**
     * Types of traffic-obstruction.
     */
    public static enum TYPES {
        TRAFFICJAM,
        SLOWTRAFFIC,
        ROADBLOCK,
        IGNORED
    }

    @Persistent
	private String myEventDescription;

    @Persistent
	private Date myValidUntil;

	@Persistent
	private Date myValidFrom;

	@Persistent
	private float myDirection;

	//@Persistent(mappedBy="myTrafficMessage", dependentElement="true")
	@Persistent(serialized="true")
	private List<Location> mySecondaryLocations;

	@Persistent
	private Map<String, String> myAdditionalData;

	//@Persistent(mappedBy="myTrafficMessage", dependent="true")
	@Persistent(serialized="true")
	private Location myPrimaryLocation;

	/**
	 * @param aLatitude
	 * @param aLongitude
	 * @param aEventID
	 * @param aSourceSystem
	 * @param aUsers
	 * @param aEventType
	 * @param aEventDescription
	 * @param aValidUntil
	 */
	public TrafficMessage(float aLatitude, float aLongitude,
			int aSourceSystem, String aUsers, int aEventType,
			String aEventDescription, Date aValidUntil) {
		myPrimaryLocation = new Location(this, aLatitude, aLongitude);
		mySourceSystem = aSourceSystem;
		myUsers = aUsers;
		myEventType = aEventType;
		myEventDescription = aEventDescription;
		myValidUntil = aValidUntil;
	}

	/**
	 * @return the primatyLocation
	 */
	public Location getPrimaryLocation() {
		return myPrimaryLocation;
	}

	/**
	 * @param aPrimatyLocation the primatyLocation to set
	 */
	public void setPrimaryLocation(Location aPrimatyLocation) {
		myPrimaryLocation = aPrimatyLocation;
	}

	/**
	 * @return the sourceSystem
	 */
	public int getSourceSystem() {
		return mySourceSystem;
	}

	/**
	 * @param aSourceSystem the sourceSystem to set
	 */
	public void setSourceSystem(int aSourceSystem) {
		mySourceSystem = aSourceSystem;
	}

	/**
	 * @return the users
	 */
	public String getUsers() {
		return myUsers;
	}

	/**
	 * @param aUsers the users to set
	 */
	public void setUsers(String aUsers) {
		myUsers = aUsers;
	}

	/**
	 * @return the eventType
	 */
	public int getEventType() {
		return myEventType;
	}

	/**
	 * @param aEventType the eventType to set
	 */
	public void setEventType(int aEventType) {
		myEventType = aEventType;
	}

	/**
	 * @return the eventDescription
	 */
	public String getEventDescription() {
		return myEventDescription;
	}

	/**
	 * @param aEventDescription the eventDescription to set
	 */
	public void setEventDescription(String aEventDescription) {
		myEventDescription = aEventDescription;
	}

	/**
	 * @return the validUntil
	 */
	public Date getValidUntil() {
		return myValidUntil;
	}

	/**
	 * @param aValidUntil the validUntil to set
	 */
	public void setValidUntil(Date aValidUntil) {
		myValidUntil = aValidUntil;
	}

	/**
	 * @return the validFrom
	 */
	public Date getValidFrom() {
		return myValidFrom;
	}

	/**
	 * @param aValidFrom the validFrom to set
	 */
	public void setValidFrom(Date aValidFrom) {
		myValidFrom = aValidFrom;
	}

	/**
	 * @return the direction
	 */
	public float getDirection() {
		return myDirection;
	}

	/**
	 * @param aDirection the direction to set
	 */
	public void setDirection(float aDirection) {
		myDirection = aDirection;
	}

	/**
	 * @return the eventID
	 */
	public long getEventID() {
		return myEventID;
	}

	/**
	 * @return the secondaryLocations
	 */
	public List<Location> getSecondaryLocations() {
		if (mySecondaryLocations == null) {
			mySecondaryLocations = new LinkedList<Location>();
		}
		return mySecondaryLocations;
	}

	/**
	 * @return the additionalData
	 */
	public Map<String, String> getAdditionalData() {
		if (myAdditionalData == null) {
			myAdditionalData = new HashMap<String, String>();
		}
		return myAdditionalData;
	}
}
