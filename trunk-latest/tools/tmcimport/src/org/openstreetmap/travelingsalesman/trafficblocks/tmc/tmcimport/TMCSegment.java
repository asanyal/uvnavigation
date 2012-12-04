package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

/**
 * Data-model of a TMC location-point.<br/>
 * Used by {@link TMCLocationTable}.
 */

public final class TMCSegment extends TMCEntity {


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
     * Class (must be "L" for Line).
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
    /**
     * "ref" of this segment.
     */
    private String ROADNUMBER;
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
    private int N2ID;
    /**
     * LocationCode of ROAD we belong to.
     */
    private int ROA_LCD;
    /**
     * other segment?
     */
    private Integer SEG_LCD;
    /**
     * LocationCode of administrative area(POLYGON) we belong to.
     */
    private int POLLCD;
    /**
     * Previous segment according to SOFFSETS.DAT .
     */
    private TMCSegment myPreviousSegment;
    /**
     * Next segment according to SOFFSETS.DAT .
     */
    private TMCSegment myNextSegment;
    /**
     * @see #ROA_LCD .
     */
    private TMCRoad myRoad;
    private TMCType myType;
    private TMCSubType mySubType;
    private TMCName myName1;
    private TMCName myName2;
    /**
     * Administrative area we belong to.
     */
    private TMCAdminArea myAdminArea;
    /**
     * @param aCid
     * @param aTabcd
     * @param aLcd
     * @param aClass
     * @param aTcd
     * @param aStcd
     * @param aRoadnumber
     * @param aRnid
     * @param aN1id
     * @param aN2id
     * @param aRoa_lcd
     * @param aSeg_lcd
     * @param aPollcd
     * @param aLocationTable 
     */
    private TMCSegment(int aCid, int aTabcd, int aLcd, char aClass, int aTcd,
            int aStcd, String aRoadnumber, Integer aRnid, int aN1id,
            int aN2id,
            final int aRoa_lcd, Integer aSeg_lcd, int aPollcd,
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
        ROADNUMBER = aRoadnumber;
        RNID = aRnid;
        N1ID = aN1id;
        this.myName1 = aLocationTable.getName(aN1id);
        if (this.myName1 == null) {
            throw new IllegalArgumentException("illegal nameID " + aN1id + " given");
        }
        N2ID = aN2id;
        this.myName2 = aLocationTable.getName(aN2id);
        if (this.myName2 == null) {
            throw new IllegalArgumentException("illegal nameID " + aN2id + " given");
        }
        ROA_LCD = aRoa_lcd;
        this.myRoad = aLocationTable.getRoad(aRoa_lcd);
        if (this.myRoad == null) {
            throw new IllegalArgumentException("Referenced TMCRoad " + aRoa_lcd + " does not exist");
        }
        this.myRoad.addSegment(this);
        SEG_LCD = aSeg_lcd;
        POLLCD = aPollcd;
        if (aPollcd >= 0) {
            this.myAdminArea = aLocationTable.getAdminArea(aPollcd);
            if (this.myAdminArea == null) {
                throw new IllegalArgumentException("Referenced TMCAdminArea " + aPollcd + " does not exist");
            }
            this.myAdminArea.addSegment(this);
        }
    }
    /**
     * Load all elements of this type from the TMC LocationCodeList.
     * @param aDirectory the directioy where the location-list is stored.
     * @param aLocationTable the top-level class we are working for
     * @return all elements indexed by LocationCode
     * @throws IOException if we cannot read
     */
    public static Map<Integer, TMCSegment> loadAll(final File aDirectory, final TMCLocationTable aLocationTable) throws IOException {
    	NavigableMap<Integer, TMCSegment> retval = new TreeMap<Integer, TMCSegment>(new SegmentComparator());

        File inFile = new File(aDirectory, "SEGMENTS.DAT");
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "ISO8859-15"));
        in.readLine(); // header;
        String line = in.readLine();
        while (line != null) {
            String[] split = (line + " ").split(";");
            line = in.readLine();
            int i = 0;
            TMCSegment segment = new TMCSegment(
                    Integer.parseInt(split[i++]),
                    Integer.parseInt(split[i++]),
                    Integer.parseInt(split[i++]),
                    split[i++].charAt(0),
                    Integer.parseInt(split[i++]),
                    Integer.parseInt(split[i++]),
                    split[i++],
                    parseOptionalInteger(split[i++]),
                    Integer.parseInt(split[i++]),
                    Integer.parseInt(split[i++]),
                    Integer.parseInt(split[i++]),
                    parseOptionalInteger(split[i++]),
                    Integer.parseInt(split[i++].trim()),
                    aLocationTable);
            retval.put(segment.getLCD(), segment);
        }

        inFile = new File(aDirectory, "SOFFSETS.DAT");
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
            TMCSegment segment = retval.get(LCD);
            if (NEG_OFF_LCD != null) {
                TMCSegment other = retval.get(NEG_OFF_LCD);
                segment.myPreviousSegment = other;
            }
            if (POS_OFF_LCD != null) {
                TMCSegment other = retval.get(POS_OFF_LCD);
                segment.myNextSegment = other;
            }
        }
        return retval;
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
     * @return the lCD
     */
    public int getLCD() {
        return LCD;
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
     * @return the rOADNUMBER
     */
    public String getROADNUMBER() {
        return ROADNUMBER;
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
    public int getN2ID() {
        return N2ID;
    }
    /**
     * @return the rOA_LCD
     */
    public int getROA_LCD() {
        return ROA_LCD;
    }
    /**
     * @return the sEG_LCD
     */
    public Integer getSEG_LCD() {
        return SEG_LCD;
    }
    /**
     * @return the pOLLCD
     */
    public int getPOLLCD() {
        return POLLCD;
    }
    /**
     * @return the previousSegment
     */
    public TMCSegment getPreviousSegment() {
        return myPreviousSegment;
    }
    /**
     * @return the nextSegment
     */
    public TMCSegment getNextSegment() {
        return myNextSegment;
    }

    /**
     * All points that have this segment as their "SEG_LCD".
     */
    private List<TMCPoint> myPoints = new LinkedList<TMCPoint>();
    /**
     * @param aPoint a point that has this segment as their "SEG_LCD".
     */
    public void addPoint(final TMCPoint aPoint) {
        this.myPoints.add(aPoint);
    }
    /**
     * @return {@link #myRoad} (may be null)
     */
    public TMCRoad getRoad() {
        return myRoad;
    }
    /**
     * @return {@link #myPoints}
     */
    public List<TMCPoint> getPoints() {
        if (myPoints.size() < 2) {
            return myPoints;
        }
        Set<List<TMCPoint>> allWays = new HashSet<List<TMCPoint>>();
        List<TMCPoint> pointsLeft = new ArrayList<TMCPoint>(myPoints);
        while (pointsLeft.size() > 0) {
            TMCPoint current = pointsLeft.remove(0);
            List<TMCPoint> currentWay = new LinkedList<TMCPoint>();
            currentWay.add(current);
            List<TMCPoint> temp = new LinkedList<TMCPoint>();

            // walk to the left of currentWay
            TMCPoint n = current.getNextPoint();
            TMCPoint lastn = current;
            while (n != null
                    //&& !(currentWay.contains(n))
                    && !(allWays.contains(n))
                    && !(temp.contains(n))
                    && pointsLeft.size() > 0) {
//                if (n.getPreviousPoint() == null || n.getPreviousPoint() != lastn) {
//                    break;
//                }
                if (pointsLeft.contains(n)) {
                    pointsLeft.remove(pointsLeft.indexOf(n));
                    currentWay.addAll(temp);
                    temp.clear();
                }
                if (TMCPoint.isSame(currentWay.get(0), n)) {
                    currentWay.addAll(0, temp);
                    temp.clear();
                    break;
                }
                if (currentWay.contains(n)) {
                    break;
                }
                temp.add(n);
                lastn = n;
                n = n.getNextPoint();
            }

            // walk to the right of currentWay
            temp.clear();
            n = current.getPreviousPoint();
            lastn = current;
            while (n != null
                    //&& !(currentWay.contains(n))
                    && !(allWays.contains(n))
                    && !(temp.contains(n))
                    && pointsLeft.size() > 0) {
//                if (n.getPreviousPoint() == null || n.getPreviousPoint() != lastn) {
//                    break;
//                }
                if (pointsLeft.contains(n)) {
                    pointsLeft.remove(pointsLeft.indexOf(n));
                    currentWay.addAll(0, temp);
                    temp.clear();
                }
                if (TMCPoint.isSame(currentWay.get(currentWay.size() - 1), n)) {
                    currentWay.addAll(temp);
                    temp.clear();
                    break;
                }
                if (currentWay.contains(n)) {
                    break;
                }

                temp.add(0, n);
                lastn = n;
                n = n.getNextPoint();
            }

            // we can't get any more by looking left and right of currentWay
//            if (allWays.size() > 0 && currentWay.size() > 0) {
//                for (List<TMCPoint> way : allWays) {
//                    if (way.get(0) == currentWay.get(currentWay.size() - 1)) {
//                        way.addAll(0, currentWay);
//                        currentWay.clear();
//                        break;
//                    }
//                    if (way.get(way.size() - 1) == currentWay.get(0)) {
//                        way.addAll(currentWay);
//                        currentWay.clear();
//                        break;
//                    }
//                }
//            }
            if (currentWay.size() > 0) {
                allWays.add(currentWay);
                currentWay = null;
            }
        }

        while (allWays.size() > 2) {
            boolean combined = false;
            for (List<TMCPoint> way : allWays) {
                for (List<TMCPoint> otherWay : allWays) {
                    if (otherWay == way) {
                        continue;
                    }
                    if (TMCPoint.isSame(way.get(0), otherWay.get(otherWay.size() - 1))) {
                        otherWay.addAll(way);
                        way.clear();
                        allWays.remove(way);
                        combined = true;
                    }
                }
                if (combined) {
                    break;
                }
            }
            if (!combined) {
                break;
            }
        }
        // append what´s left
        List<TMCPoint> retval = new LinkedList<TMCPoint>();
        for (List<TMCPoint> way : allWays) {
            retval.addAll(way);
        }
        return retval;
    }

    /**
     * Used by {@link #generateReferenceOSM(MemoryDataSet)}.
     */
    private Way myReferenceOSMObject = null;
    /**
     * Generate osm-entities as a reference for comaprison in an editor
     * like JOSM. These are NOT to be uploaded.
     * @param aOutData where to write to
     * @return the generated entity
     */
    public Entity generateReferenceOSM(final MemoryDataSet aOutData) {
        List<TMCPoint> points = getPoints();
        List<WayNode> wayNodes = new ArrayList<WayNode>();
        for (TMCPoint point : points) {
            wayNodes.add(new WayNode(point.generateReferenceOSM(aOutData).getId()));
        }
        if (myReferenceOSMObject != null) {
            aOutData.addWay(myReferenceOSMObject);
            return myReferenceOSMObject;
        }
        Way retval = new Way((long) (Integer.MIN_VALUE * Math.random()), 1, new Date(), OsmUser.NONE, 0);
        retval.getTags().add(new Tag("name", getROADNUMBER() + ":" + getLCD() + " - DO NOT UPLOAD"));
        if (this.myName1 != null) {
            retval.getTags().add(new Tag("tmc:name1", this.myName1.getNAME() + " - " + this.myName1.getNCOMMENT()));        	
        }
        if (this.myName2 != null) {
            retval.getTags().add(new Tag("tmc:name2", this.myName2.getNAME() + " - " + this.myName2.getNCOMMENT()));        	
        }
        retval.getTags().add(new Tag("note", "DO NOT UPLOAD THIS ENTITY! IT IS FOR REFERENCE ONLY."));
        retval.getTags().add(new Tag("TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":LocationCode", "" + getLCD()));
        if (getNextSegment() != null) {
            retval.getTags().add(new Tag("TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":NextLocationCode", "" + getNextSegment().getLCD()));
        }
        if (getPreviousSegment() != null) {
            retval.getTags().add(new Tag("TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":PrevLocationCode", "" + getPreviousSegment().getLCD()));
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
        TMCAdminArea area = getLocationTable().getAdminArea(getPOLLCD());
        if (area != null) {
            String name = "TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":LocationCode=" + area.getLCD();
            if (area.getName() != null) {
                name += " \"" + area.getName().getNAME() + "\"";
            }
            retval.getTags().add(new Tag("note:TMC:area", "belongs to area \"" + name + "\""));
        }
        retval.getWayNodes().addAll(wayNodes);
        aOutData.addWay(retval);
        myReferenceOSMObject = retval;
        return retval;
    }
    /**
     * @return the type
     */
    public TMCType getType() {
        return myType;
    }
    /**
     * @return the subType
     */
    public TMCSubType getSubType() {
        return mySubType;
    }
    /**
     * Compare Segments by ID.
     */
    private static final class SegmentComparator implements
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
