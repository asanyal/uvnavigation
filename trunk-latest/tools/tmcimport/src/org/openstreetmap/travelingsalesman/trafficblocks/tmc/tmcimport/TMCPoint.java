package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

/**
 * Data-model of a TMC location-point.<br/>
 * Used by {@link TMCLocationTable}.
 */
public final class TMCPoint extends TMCEntity {
    /**
     * Country-ID.
     */
    private int CID;
    /**
     * Location-Table-ID. (unique in a country)
     * @see #CID
     */
    private int TABCD;
    /**
     * Location-code. (unique in a location-table).
     * @see #TABCD
     */
    private int LCD;
    /**
     * Class (must be "P" for Point).
     */
    private char CLASS;
    /**
     * Type-ID.
     */
    private int TCD;
    /**
     * Subtype ID.
     */
    private int STCD;
    private String JUNCTIONNUMBER;
    /**
     * All other nodes that make up this intesection and
     * are considered to be {@link #isSame(TMCPoint, TMCPoint)}.
     */
    private Set<TMCPoint> myIntersection = null;
    /**
     * Road-Network ID.
     */
    private Integer RNID;
    /**
     * NameID of first name.
     */
    private int N1ID;
    /**
     * NameID of alternative name.
     */
    private Integer N2ID;
    /**
     * LocationCode of administrative area(POLYGON) we belong to.
     */
    private Integer POL_LCD;
    /**
     * Optional LocationCode of other area we belong to.
     */
    private Integer OTH_LCD;
    /**
     * Optional LocationCode of other segment we belong to.
     */
    private Integer SEG_LCD;
    /**
     * Optional LocationCode of other road we belong to.
     */
    private Integer ROA_LCD;
    private Integer INPOS;
    private Integer INNEG;
    private Integer OUTPOS;
    private Integer OUTNEG;
    private Integer PRESENTPOS;
    private Integer PRESENTNEG;
    private String DIVERSIONPOS;
    private String DIVERSIONNEG;
    private String XCOORD;
    private String YCOORD;
    private int INTERRUPTSROAD;
    private int URBAN;
    private TMCPoint myPreviousPoint;
    private TMCPoint myNextPoint;
    private TMCRoad myRoad;
    private TMCSegment mySegment;
    private TMCSubType mySubType;
    private TMCType myType;
    private TMCName myName1;
    private TMCName myName2;
    /**
     * The administrative area we belong to.
     */
    private TMCAdminArea myAdminArea;

    /**
     * Load all elements of this type from the TMC LocationCodeList.
     * @param aDirectory the directioy where the location-list is stored.
     * @param aLocationTable the top-level class we are working for
     * @return all elements indexed by LocationCode
     * @throws IOException if we cannot read
     */
    public static Map<Integer, TMCPoint> loadAll(final File aDirectory, final TMCLocationTable aLocationTable) throws IOException {
    	NavigableMap<Integer, TMCPoint> retval = new TreeMap<Integer, TMCPoint>(new PointComparator());

        File inFile = new File(aDirectory, "POINTS.DAT");
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "ISO8859-15"));
        in.readLine(); // header;
        String line = in.readLine();
        while (line != null) {
            String[] split = (line + " ").split(";");
            line = in.readLine();
            int i = 0;
            TMCPoint point = new TMCPoint(
                    Integer.parseInt(split[i++]), //CID;
                    Integer.parseInt(split[i++]), //TABCD;
                    Integer.parseInt(split[i++]), //LCD;
                    split[i++].charAt(0), //CLASS;
                    Integer.parseInt(split[i++]), //TCD;
                    Integer.parseInt(split[i++]), //STCD;
                    split[i++], //JUNCTIONNUMBER;
                    parseOptionalInteger(split[i++]), //RNID;
                    Integer.parseInt(split[i++]), //N1ID;
                    parseOptionalInteger(split[i++]), //N2ID;
                    parseOptionalInteger(split[i++]), //POL_LCD;
                    parseOptionalInteger(split[i++]), //OTH_LCD;
                    parseOptionalInteger(split[i++]), //SEG_LCD;
                    parseOptionalInteger(split[i++]), //ROA_LCD;
                    parseOptionalInteger(split[i++]), //INPOS;
                    parseOptionalInteger(split[i++]), //INNEG;
                    parseOptionalInteger(split[i++]), //OUTPOS;
                    parseOptionalInteger(split[i++]), //OUTNEG;
                    parseOptionalInteger(split[i++]), //PRESENTPOS;
                    parseOptionalInteger(split[i++]), //PRESENTNEG;
                    split[i++], //DIVERSIONPOS;
                    split[i++], //DIVERSIONNEG;
                    split[i++], //XCOORD;
                    split[i++], //YCOORD;
                    Integer.parseInt(split[i++]), //INTERRUPTSROAD;
                    Integer.parseInt(split[i++].trim()),
                    aLocationTable); //URBAN
            retval.put(point.getLCD(), point);
        }

        inFile = new File(aDirectory, "POFFSETS.DAT");
        in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "ISO8859-15"));
        in.readLine(); // header;
        line = in.readLine();
        while (line != null) {
            String[] split = (line + " ").split(";");
            line = in.readLine();
            int i = 2;
            //CID;TABCD;LCD;NEG_OFF_LCD;POS_OFF_LCD

            int LCD = Integer.parseInt(split[i++]);
            Integer NEG_OFF_LCD = parseOptionalInteger(split[i++]);
            Integer POS_OFF_LCD = parseOptionalInteger(split[i++].trim());
            TMCPoint point = retval.get(LCD);
            if (NEG_OFF_LCD != null) {
                TMCPoint other = retval.get(NEG_OFF_LCD);
                point.myPreviousPoint = other;
            }
            if (POS_OFF_LCD != null) {
                TMCPoint other = retval.get(POS_OFF_LCD);
                point.myNextPoint = other;
            }
        }


        inFile = new File(aDirectory, "INTERSECTIONS.DAT");
        in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "ISO8859-15"));
        in.readLine(); // header;
        line = in.readLine();
        while (line != null) {
            String[] split = (line + " ").split(";");
            line = in.readLine();
            int i = 2;
            //CID;TABCD;LCD;INT_CID;INT_TABCD;INT_LCD
            int pointA = Integer.parseInt(split[i++]);
            i++;
            i++;
            int pointB = Integer.parseInt(split[i++].trim());
            TMCPoint a = retval.get(pointA);
            TMCPoint b = retval.get(pointB);
            Set<TMCPoint> intersection = a.getIntersection();
            if (intersection == null) {
                intersection = new HashSet<TMCPoint>();
            }
            intersection.add(a);
            intersection.add(b);
            boolean added = true;
            while (added) {
                added = false;
                for (TMCPoint point : intersection) {
                	if (point.getIntersection() == null) {
                		point.setIntersection(intersection);
                	} else {
                		// they all get the same instance off the list.
                		if (point.getIntersection() != null
                				&& point.getIntersection() != intersection) {
                			intersection.addAll(point.getIntersection());
                			point.setIntersection(intersection);
                			added = true;
                			break;
                		}
                	}
                }
            }
            for (TMCPoint point : intersection) {
                point.setIntersection(intersection);
            }
        }
        return retval;
    }


    /**
     * @param aCid
     * @param aTabcd
     * @param aLcd
     * @param aClass
     * @param aTcd
     * @param aStcd
     * @param aJunctionnumber
     * @param aRnid
     * @param aN1id
     * @param aN2id
     * @param aPol_lcd
     * @param aOth_lcd
     * @param aSeg_lcd
     * @param aRoa_lcd
     * @param aInpos
     * @param aInneg
     * @param aOutpos
     * @param aOutneg
     * @param aPresentpos
     * @param aPresentneg
     * @param aDiversionpos
     * @param aDiversionneg
     * @param aXcoord
     * @param aYcoord
     * @param aInterruptsroad
     * @param aUrban
     * @param aLocationTable
     */
    private TMCPoint(
            final int aCid,
            final int aTabcd, int aLcd, char aClass, int aTcd,
            final int aStcd, String aJunctionnumber, Integer aRnid, int aN1id,
            final Integer aN2id, Integer aPol_lcd, Integer aOth_lcd, Integer aSeg_lcd,
            final Integer aRoa_lcd,
            final Integer aInpos,
            final Integer aInneg,
            final Integer aOutpos,
            final Integer aOutneg,
            final Integer aPresentpos,
            final Integer aPresentneg, String aDiversionpos,
            String aDiversionneg, String aXcoord, String aYcoord,
            int aInterruptsroad, int aUrban,
            final TMCLocationTable aLocationTable) {
        super(aLocationTable);
        CID = aCid;
        TABCD = aTabcd;
        LCD = aLcd;
        CLASS = aClass;
        TCD = aTcd;
        this.myType = aLocationTable.getType(aClass, aTcd);
        if (this.myType == null) {
            throw new IllegalArgumentException("illegal type " + aTcd + " given");
        }
        STCD = aStcd;
        if (aStcd != 0) {
            this.mySubType = aLocationTable.getSubType(aClass, aTcd, aStcd);
            if (this.mySubType == null) {
                throw new IllegalArgumentException("illegal subtype " + aStcd + " given");
            }
        }
        JUNCTIONNUMBER = aJunctionnumber;
        RNID = aRnid;
        N1ID = aN1id;
        this.myName1 = aLocationTable.getName(aN1id);
        if (this.myName1 == null) {
            throw new IllegalArgumentException("illegal nameID " + aN1id + " given");
        }
        N2ID = aN2id;
        if (aN2id != null) {
            this.myName2 = aLocationTable.getName(aN2id);
            if (this.myName2 == null) {
                throw new IllegalArgumentException("illegal nameID " + aN2id + " given");
            }
        }
        POL_LCD = aPol_lcd;
        if (aPol_lcd != null) {
            this.myAdminArea = aLocationTable.getAdminArea(aPol_lcd);
            if (this.myAdminArea == null) {
                throw new IllegalArgumentException("Referenced TMCAdminArea " + aPol_lcd + " does not exist");
            }
            this.myAdminArea.addPoint(this);
        }
        OTH_LCD = aOth_lcd;
        SEG_LCD = aSeg_lcd;
        if (aSeg_lcd != null) {
            this.mySegment = aLocationTable.getSegment(aSeg_lcd);
            if (this.mySegment == null) {
                throw new IllegalArgumentException("Referenced TMCSegment " + aSeg_lcd + " does not exist");
            }
            this.mySegment.addPoint(this);
        }
        ROA_LCD = aRoa_lcd;
        if (aRoa_lcd != null) {
            this.myRoad = aLocationTable.getRoad(aRoa_lcd);
            if (this.myRoad == null) {
                throw new IllegalArgumentException("Referenced TMCRoad " + aRoa_lcd + " does not exist");
            }
            this.myRoad.addPoint(this);
        }
        INPOS = aInpos;
        INNEG = aInneg;
        OUTPOS = aOutpos;
        OUTNEG = aOutneg;
        PRESENTPOS = aPresentpos;
        PRESENTNEG = aPresentneg;
        DIVERSIONPOS = aDiversionpos;
        DIVERSIONNEG = aDiversionneg;
        XCOORD = aXcoord;
        YCOORD = aYcoord;
        INTERRUPTSROAD = aInterruptsroad;
        URBAN = aUrban;
    }
    /**
     * @return {@link #getYCOORD()} translated
     */
    public double getLatitude() {
        final int factor = 100000;
        return Double.parseDouble(YCOORD) / factor;
    }
    /**
     * @return {@link #getXCOORD()} translated
     */
    public double getLongitude() {
        final int factor = 100000;
        return Double.parseDouble(XCOORD) / factor;
    }
    /**
     * Helper-method.
     * @param aString input
     * @return may be null
     */
    private static Integer parseOptionalInteger(final String aString) {
        if (aString == null || aString.trim().length() == 0) {
            return null;
        }
        return Integer.parseInt(aString);
    }


    /**
     * @return the lCD
     */
    public int getLCD() {
        return LCD;
    }


    /**
     * @return the cID
     */
    public int getCID() {
        return CID;
    }


    /**
     * @return the tABCD
     */
    public int getTABCD() {
        return TABCD;
    }


    /**
     * @return the cLASS
     */
    public char getCLASS() {
        return CLASS;
    }


    /**
     * @return the tCD
     */
    public int getTCD() {
        return TCD;
    }


    /**
     * @return the sTCD
     */
    public int getSTCD() {
        return STCD;
    }


    /**
     * @return the jUNCTIONNUMBER
     */
    public String getJUNCTIONNUMBER() {
        return JUNCTIONNUMBER;
    }


    /**
     * @return the rNID
     */
    public Integer getRNID() {
        return RNID;
    }


    /**
     * @return the n1ID
     */
    public int getN1ID() {
        return N1ID;
    }


    /**
     * @return the n2ID
     */
    public Integer getN2ID() {
        return N2ID;
    }


    /**
     * @return the pOL_LCD
     */
    public Integer getPOL_LCD() {
        return POL_LCD;
    }


    /**
     * @return the oTH_LCD
     */
    public Integer getOTH_LCD() {
        return OTH_LCD;
    }


    /**
     * @return the sEG_LCD
     */
    public Integer getSEG_LCD() {
        return SEG_LCD;
    }


    /**
     * @return the rOA_LCD
     */
    public Integer getROA_LCD() {
        return ROA_LCD;
    }


    /**
     * @return the iNPOS
     */
    public Integer getINPOS() {
        return INPOS;
    }


    /**
     * @return the iNNEG
     */
    public Integer getINNEG() {
        return INNEG;
    }


    /**
     * @return the oUTPOS
     */
    public Integer getOUTPOS() {
        return OUTPOS;
    }


    /**
     * @return the oUTNEG
     */
    public Integer getOUTNEG() {
        return OUTNEG;
    }


    /**
     * @return the pRESENTPOS
     */
    public Integer getPRESENTPOS() {
        return PRESENTPOS;
    }


    /**
     * @return the pRESENTNEG
     */
    public Integer getPRESENTNEG() {
        return PRESENTNEG;
    }


    /**
     * @return the dIVERSIONPOS
     */
    public String getDIVERSIONPOS() {
        return DIVERSIONPOS;
    }


    /**
     * @return the dIVERSIONNEG
     */
    public String getDIVERSIONNEG() {
        return DIVERSIONNEG;
    }


    /**
     * @return the xCOORD
     */
    public String getXCOORD() {
        return XCOORD;
    }


    /**
     * @return the yCOORD
     */
    public String getYCOORD() {
        return YCOORD;
    }


    /**
     * @return the iNTERRUPTSROAD
     */
    public int getINTERRUPTSROAD() {
        return INTERRUPTSROAD;
    }


    /**
     * @return the uRBAN
     */
    public int getURBAN() {
        return URBAN;
    }


    /**
     * @return the previousPoint
     */
    public TMCPoint getPreviousPoint() {
        return myPreviousPoint;
    }


    /**
     * @return the nextPoint
     */
    public TMCPoint getNextPoint() {
        return myNextPoint;
    }

    /**
     * Used by {@link #generateReferenceOSM(MemoryDataSet)}.
     */
    private Node myReferenceOSMObject = null;
    /**
     * Generate osm-entities as a reference for comaprison in an editor
     * like JOSM. These are NOT to be uploaded.
     * @param aOutData where to write to
     * @return the generated entity
     */
    public Entity generateReferenceOSM(final MemoryDataSet aOutData) {
        if (myReferenceOSMObject != null) {
            aOutData.addNode(myReferenceOSMObject);
            return myReferenceOSMObject;
        }
        Node retval = new Node((long) (Integer.MIN_VALUE * Math.random()), 1, new Date(), OsmUser.NONE, 0, getLatitude(), getLongitude());
        String name = getLCD() + " - DO NOT UPLOAD";
        if (getSEG_LCD() != null) {
            name = getLocationTable().getSegment(getSEG_LCD()).getROADNUMBER() + ":" + name;
        }
        retval.getTags().add(new Tag("name", name));
        if (this.myName1 != null) {
            retval.getTags().add(new Tag("tmc:name1", this.myName1.getNAME() + " - " + this.myName1.getNCOMMENT()));        	
        }
        if (this.myName2 != null) {
            retval.getTags().add(new Tag("tmc:name2", this.myName2.getNAME() + " - " + this.myName2.getNCOMMENT()));        	
        }
        retval.getTags().add(new Tag("note", "DO NOT UPLOAD THIS ENTITY! IT IS FOR REFERENCE ONLY."));
        retval.getTags().add(new Tag("TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":LocationCode", "" + getLCD()));
        if (getNextPoint() != null) {
            retval.getTags().add(new Tag("TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":NextLocationCode", "" + getNextPoint().getLCD()));
        }
        if (getSEG_LCD() != null) {
            retval.getTags().add(new Tag("Segment:", Integer.toString(getSEG_LCD())));
        }
        if (getPreviousPoint() != null) {
            retval.getTags().add(new Tag("TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":PrevLocationCode", "" + getPreviousPoint().getLCD()));
        }
        retval.getTags().add(new Tag("TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":Class", "" + getCLASS()));
        if (getType() != null) {
            retval.getTags().add(new Tag("TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":TypeName", getType().getTDESC()));
            retval.getTags().add(new Tag("TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":TypeName:loc", getType().getTNATDESC()));
        }
        if (getSubType() != null) {
            retval.getTags().add(new Tag("TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":SubTypeName", getSubType().getTDESC()));
            retval.getTags().add(new Tag("TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":SubTypeName:loc", getSubType().getTNATDESC()));
        }
        TMCAdminArea area = getLocationTable().getAdminArea(getPOL_LCD());
        if (area != null) {
            String aname = "TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":LocationCode=" + area.getLCD();
            if (area.getName() != null) {
                aname += " \"" + area.getName().getNAME() + "\"";
            }
            retval.getTags().add(new Tag("note:TMC:area", "belongs to area \"" + aname + "\""));
        }
        if (getROA_LCD() != null) {
        	TMCRoad road = getLocationTable().getRoad(getROA_LCD());
        	if (road != null) {
        		String rname = "TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":LocationCode=" + road.getLCD();
        		if (road.getName1() != null) {
        			rname += " \"" + road.getName1().getNAME() + "\"";
        		}
        		if (road.getName2() != null) {
        			rname += " \"" + road.getName2().getNAME() + "\"";
        		}
        		retval.getTags().add(new Tag("note:TMC:road", "belongs to road \"" + rname + "\""));
        	}
        }
        if (getSEG_LCD() != null) {
        	TMCSegment segment = getLocationTable().getSegment(getSEG_LCD());
        	if (segment != null) {
        		String sname = "TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":LocationCode=" + segment.getLCD();
        		retval.getTags().add(new Tag("note:TMC:segment", "belongs to segment \"" + sname + "\""));
        	}
        }

        if (this.getIntersection() != null && !this.getIntersection().isEmpty()) {
        	StringBuilder intersectionText = new StringBuilder("intersecting with: ");
        	final Set<TMCPoint> intersection = this.getIntersection();
        	for (TMCPoint point : intersection) {
				intersectionText.append("(point " + point.toString() + ")");
			}
    		retval.getTags().add(new Tag("note:TMC:intersections", intersectionText.toString()));
        }
        aOutData.addNode(retval);
        myReferenceOSMObject = retval;
        return retval;
    }


    /**
     * @return the subType
     */
    public TMCSubType getSubType() {
        return mySubType;
    }


    /**
     * @return the type
     */
    public TMCType getType() {
        return myType;
    }


    /**
     * Is this the same point?
     * @param aPoint first point
     * @param aN other point
     * @return true if this is the same point (can have different IDs on intersection and be the same)
     */
    public static boolean isSame(final TMCPoint aPoint, final TMCPoint aN) {
        if (aPoint == aN) {
            return true;
        }
        if (aPoint.getIntersection() != null) {
            if (aPoint.getIntersection().contains(aN)) {
                return true;
            }
        }
        if (aN.getIntersection() != null) {
            if (aN.getIntersection().contains(aPoint)) {
                return true;
            }
        }
        return false;
    }


    /**
     * @return the intersection
     */
    public Set<TMCPoint> getIntersection() {
        return myIntersection;
    }


    /**
     * @param aIntersection the intersection to set
     */
    protected void setIntersection(final Set<TMCPoint> aIntersection) {
        myIntersection = aIntersection;
    }


    public TMCSegment getSegment() {
        return mySegment;
    }

    /**
     * {@inheritDoc}.
     */
    public String toString() {
    	StringBuffer sb = new StringBuffer("Point ");
    	sb.append(getLCD());
    	if (this.myName1 != null) {
    		sb.append("(Name1: " + this.myName1.getNAME() + ")");
    	}
    	if (this.myName2 != null) {
    		sb.append("(Name2: " + this.myName2.getNAME() + ")");
    	}
    	final Integer roadID = getROA_LCD();
    	if (roadID != null) {
    		final TMCRoad road = getLocationTable().getRoad(roadID);
    		if (road == null) {
    			sb.append(" on missing road ").append(roadID);
    		} else {
    			sb.append(" on road ").append(roadID)
    			.append(" \"").append(road.getROADNUMBER()).append("\"");    			
    		}
    	} else {
        	final TMCSegment segment = getSegment();
        	if (segment != null) {
    			sb.append(" on segment ").append(segment.getLCD())
    			.append(" \"").append(segment.getROADNUMBER()).append("\"");        		
        	}
    	}
    	return sb.toString();
    }

    /**
     * Compare Points by ID.
     */
    private static final class PointComparator implements
    Comparator<Integer> {
        /**
         * {@inheritDoc}.
         */
        @Override
        public int compare(final Integer aO1, final Integer aO2) {
            return aO1.compareTo(aO2);
        }
    }

}
