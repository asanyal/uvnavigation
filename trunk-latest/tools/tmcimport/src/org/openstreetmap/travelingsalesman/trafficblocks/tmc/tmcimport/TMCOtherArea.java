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
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
//import java.util.concurrent.atomic.AtomicBoolean;

import org.openstreetmap.osm.data.MemoryDataSet;
//import org.openstreetmap.osm.data.coordinates.Bounds;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

/**
 * Data-model of a TMC location other area.<br/>
 * Used by {@link TMCLocationTable}.
 */
public final class TMCOtherArea extends TMCEntity {
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
    /**
     * NameID of the name.
     */
    private Integer NID;
    /**
     * LocationCode of administrative area(POLYGON) we belong to.
     */
    private Integer POL_LCD;
    private TMCType myType;
    private TMCSubType mySubType;
    private TMCName myName;
    /**
     * Load all elements of this type from the TMC LocationCodeList.
     * @param aDirectory the directioy where the location-list is stored.
     * @param aLocationTable the top-level class we are working for
     * @return all elements indexed by LocationCode
     * @throws IOException if we cannot read
     */
    public static NavigableMap<Integer, TMCOtherArea> loadAll(final File aDirectory, final TMCLocationTable aLocationTable) throws IOException {
        NavigableMap<Integer, TMCOtherArea> retval = new TreeMap<Integer, TMCOtherArea>(new OtherAreaComparator());

        File inFile = new File(aDirectory, "OTHERAREAS.DAT");
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "ISO8859-15"));
        in.readLine(); // header;
        String line = in.readLine();
        while (line != null) {
            String[] split = (line + " ").split(";");
            line = in.readLine();
            int i = 0;
            TMCOtherArea point = new TMCOtherArea(
                    Integer.parseInt(split[i++]), //CID;
                    Integer.parseInt(split[i++]), //TABCD;
                    Integer.parseInt(split[i++]), //LCD;
                    split[i++].charAt(0), //CLASS;
                    Integer.parseInt(split[i++]), //TCD;
                    Integer.parseInt(split[i++]), //STCD;
                    parseOptionalInteger(split[i++]), //NID;
                    parseOptionalInteger(split[i++]), //POL_LCD;
                    aLocationTable);
            retval.put(point.getLCD(), point);
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
     * @param aNid
     * @param aPol_lcd
     */
    private TMCOtherArea(
            final int aCid,
            final int aTabcd,
            final int aLcd,
            final char aClass,
            final int aTcd,
            final int aStcd,
            final Integer aNid,
            final Integer aPol_lcd,
            final TMCLocationTable aLocationTable) {
        super(aLocationTable);
        this.CID = aCid;
        this.TABCD = aTabcd;
        this.LCD = aLcd;
        this.CLASS = aClass;
        this.TCD = aTcd;
        this.myType = aLocationTable.getType(aClass, aTcd);
        if (this.myType == null) {
            throw new IllegalArgumentException("illegal type " + aTcd + " given");
        }
        STCD = aStcd;
        if (aStcd != 0) {
            this.mySubType = aLocationTable.getSubType(aClass, aTcd, aStcd);
            if (this.mySubType == null) {
                throw new IllegalArgumentException("illegal subtype " + aStcd + " of type " + aTabcd + " of class " + aClass + " given");
            }
        }
        NID = aNid;
        if (aNid != null) {
            this.myName = aLocationTable.getName(aNid);
            if (this.myName == null) {
                throw new IllegalArgumentException("illegal nameID " + aNid + " given");
            }
        }
        this.POL_LCD = aPol_lcd;
        if (aPol_lcd != null && aPol_lcd != LCD) {
            addChildArea(aPol_lcd, this);
        }
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
        return Integer.parseInt(aString.trim());
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
     * @return the nameID
     */
    public int getNID() {
        return NID.intValue();
    }

    /**
     * @return the pOL_LCD
     */
    public Integer getPOL_LCD() {
        return POL_LCD;
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
     * @return the name1
     */
    public TMCName getName() {
        return myName;
    }

    /**
     * @param aLcd the lCD to set
     */
    public void setLCD(final int aLcd) {
        LCD = aLcd;
    }

    /**
     * Used by {@link #generateReferenceOSM(MemoryDataSet)}.
     */
    private Relation myReferenceOSMObject = null;
    /**
     * Areas directly contained in this area.
     */
    private static Map<Integer, Set<TMCOtherArea>> myChildAreas = new HashMap<Integer, Set<TMCOtherArea>>();
    /**
     * Points directly contained in this area.
     */
    private static Map<Integer, Set<TMCPoint>> myChildPoints = new HashMap<Integer, Set<TMCPoint>>();
    /**
     * Segments directly contained in this area.
     */
    private static Map<Integer, Set<TMCSegment>> myChildSegments = new HashMap<Integer, Set<TMCSegment>>();
    /**
     * Points directly contained in this area.
     */
    private static Map<Integer, Set<TMCRoad>> myChildRoads = new HashMap<Integer, Set<TMCRoad>>();
    /**
     * @return the myChildAreas
     */
    public Set<TMCOtherArea> getChildAreas() {
        Set<TMCOtherArea> areas = myChildAreas.get(getLCD());
        if (areas == null) {
            return new HashSet<TMCOtherArea>();
        }
        return areas;
    }

    /**
     * @return the myChildPoints
     */
    public Set<TMCPoint> getChildPoints() {
        Set<TMCPoint> points = myChildPoints.get(getLCD());
        if (points == null) {
            return new HashSet<TMCPoint>();
        }
        return points;
    }


    /**
     * @return the myChildRoads
     */
    public Set<TMCSegment> getChildSegments() {
        Set<TMCSegment> segments = myChildSegments.get(getLCD());
        if (segments == null) {
            return new HashSet<TMCSegment>();
        }
        return segments;
    }

    /**
     * @return the myChildRoads
     */
    public Set<TMCRoad> getChildRoads() {
        Set<TMCRoad> roads = myChildRoads.get(getLCD());
        if (roads == null) {
            return new HashSet<TMCRoad>();
        }
        return roads;
    }

    /**
     * Generate osm-entities as a reference for comaprison in an editor
     * like JOSM. These are NOT to be uploaded.
     * @param aOutData where to write to
     * @return the generated entity
     */
    public Entity generateReferenceOSM(final MemoryDataSet aOutData) {
        List<RelationMember> members = new ArrayList<RelationMember>();
        if (myReferenceOSMObject != null) {
            aOutData.addRelation(myReferenceOSMObject);
            return myReferenceOSMObject;
        }
        Relation retval = new Relation((long) (Integer.MIN_VALUE), 1, new Date(), OsmUser.NONE, 0);
        if (getName() != null) {
            retval.getTags().add(new Tag("name", getName().getNAME() + " " + getLCD() + " - DO NOT UPLOAD"));
        } else {
            retval.getTags().add(new Tag("name", "TMC Admin Area " + getLCD() + " - DO NOT UPLOAD"));
        }
        retval.getTags().add(new Tag("note", "DO NOT UPLOAD THIS ENTITY! IT IS FOR REFERENCE ONLY."));
        retval.getTags().add(new Tag("TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":LocationCode", "" + getLCD()));
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
            String name = "TMC:cid_" + getCID() + ":tabcd_" + getTABCD() + ":LocationCode=" + area.getLCD();
            if (area.getName() != null) {
                name += " \"" + area.getName().getNAME() + "\"";
            }
            retval.getTags().add(new Tag("note:TMC:area", "belongs to area \"" + name + "\""));
        }
        // Members
        Set<TMCOtherArea> children = myChildAreas.get(getLCD());
        if (children != null) {
            int i = -1;
            for (TMCOtherArea childArea : children) {
                String childName = "";
                if (childArea.getName() != null) {
                    childName = " \"" + childArea.getName().getNAME() + "\"";
                }
                RelationMember childMember = new RelationMember(i--, EntityType.Relation, "contains: TMC:cid_" + getCID()
                        + ":tabcd_" + getTABCD() + ":LocationCode=" + childArea.getLCD() + childName);
                members.add(childMember);
            }
        }
        retval.getMembers().addAll(members);
        aOutData.addRelation(retval);
        myReferenceOSMObject = retval;
        return retval;
    }


    public void addPoint(final TMCPoint aPoint) {
        //TODO: write these to OSM as mebres of the relation
        Integer parent = aPoint.getPOL_LCD();
        Set<TMCPoint> children = myChildPoints.get(parent);
        if (children == null) {
            children = new HashSet<TMCPoint>();
            myChildPoints.put(parent, children);
        }
        children.add(aPoint);
    }


    public void addSegment(final TMCSegment aSegment) {
        //TODO: write these to OSM as mebres of the relation
        Integer parent = aSegment.getPOLLCD();
        Set<TMCSegment> children = myChildSegments.get(parent);
        if (children == null) {
            children = new HashSet<TMCSegment>();
            myChildSegments.put(parent, children);
        }
        children.add(aSegment);
    }
    public void addRoad(final TMCRoad aRoad) {
        //TODO: write these to OSM as mebres of the relation
        Integer parent = aRoad.getPOL_LCD();
        Set<TMCRoad> children = myChildRoads.get(parent);
        if (children == null) {
            children = new HashSet<TMCRoad>();
            myChildRoads.put(parent, children);
        }
        children.add(aRoad);
    }

    private void addChildArea(final int aParentArea, final TMCOtherArea aAdminArea) {
        Integer parent = aAdminArea.getPOL_LCD();
        Set<TMCOtherArea> children = myChildAreas.get(parent);
        if (children == null) {
            children = new HashSet<TMCOtherArea>();
            myChildAreas.put(parent, children);
        }
        children.add(aAdminArea);
    }

    /**
     * Compare Areas by ID.
     */
    private static final class OtherAreaComparator implements
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
