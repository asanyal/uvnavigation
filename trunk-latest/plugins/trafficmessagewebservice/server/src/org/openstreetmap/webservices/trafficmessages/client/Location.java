package org.openstreetmap.webservices.trafficmessages.client;

import java.io.Serializable;

//import javax.jdo.annotations.IdGeneratorStrategy;
//import javax.jdo.annotations.IdentityType;
//import javax.jdo.annotations.PersistenceCapable;
//import javax.jdo.annotations.Persistent;
//import javax.jdo.annotations.PrimaryKey;

//@PersistenceCapable(identityType=IdentityType.APPLICATION)
public class Location implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8517982347087181250l;

	//@PrimaryKey
	//@Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
	private Long myLocationID;

	//@Persistent
	//private TrafficMessage myTrafficMessage;
	private float myLatitude;
	private float myLongitude;
	private String myRoadNumber;
	private short myEntityType;
	private long myEntityID;

	/**
	 * @return the latitude
	 */
	public float getLatitude() {
		return myLatitude;
	}
	/**
	 * @param aLatitude the latitude to set
	 */
	public void setLatitude(float aLatitude) {
		myLatitude = aLatitude;
	}
	/**
	 * @return the longitude
	 */
	public float getLongitude() {
		return myLongitude;
	}
	/**
	 * @param aLongitude the longitude to set
	 */
	public void setLongitude(float aLongitude) {
		myLongitude = aLongitude;
	}
	/**
	 * @return the roadNumber
	 */
	public String getRoadNumber() {
		return myRoadNumber;
	}
	/**
	 * @param aRoadNumber the roadNumber to set
	 */
	public void setRoadNumber(String aRoadNumber) {
		myRoadNumber = aRoadNumber;
	}
	/**
	 * @return the entityType
	 */
	public short getEntityType() {
		return myEntityType;
	}
	/**
	 * @param aEntityType the entityType to set
	 */
	public void setEntityType(short aEntityType) {
		myEntityType = aEntityType;
	}
	/**
	 * @return the entityID
	 */
	public long getEntityID() {
		return myEntityID;
	}
	/**
	 * @param aEntityID the entityID to set
	 */
	public void setEntityID(long aEntityID) {
		myEntityID = aEntityID;
	}
	/**
	 * @param aTrafficMessage 
	 * @param aLatitude
	 * @param aLongitude
	 */
	public Location(final TrafficMessage aTrafficMessage, float aLatitude, float aLongitude) {
		super();
		//this.myTrafficMessage = aTrafficMessage;
		myLatitude = aLatitude;
		myLongitude = aLongitude;
	}

}
