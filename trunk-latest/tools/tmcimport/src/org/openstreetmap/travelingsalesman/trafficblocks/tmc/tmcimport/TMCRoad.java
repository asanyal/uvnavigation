package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

/**
 * Data-model of a TMC location-road.<br/>
 * Used by {@link TMCLocationTable}.
 */
public final class TMCRoad extends TMCEntity {
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
    private String ROADNUMBER;
    /**
     * Road-Network ID.
     */
    private Integer RNID;
    /**
     * NameID of first name.
     */
    private Integer N1ID;
    /**
     * NameID of alternative name.
     */
    private Integer N2ID;
    /**
     * LocationCode of administrative area(POLYGON) we belong to.
     */
    private Integer POL_LCD;
    private int PES_LEV;
    private TMCType myType;
    private TMCSubType mySubType;
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
    public static Map<Integer, TMCRoad> loadAll(final File aDirectory, final TMCLocationTable aLocationTable) throws IOException {
    	NavigableMap<Integer, TMCRoad> retval = new TreeMap<Integer, TMCRoad>(new RoadComparator());

        File inFile = new File(aDirectory, "ROADS.DAT");
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "ISO8859-15"));
        in.readLine(); // header;
        String line = in.readLine();
        while (line != null) {
            String[] split = (line + " ").split(";");
            line = in.readLine();
            int i = 0;
            TMCRoad point = new TMCRoad(
                    Integer.parseInt(split[i++]), //CID;
                    Integer.parseInt(split[i++]), //TABCD;
                    Integer.parseInt(split[i++]), //LCD;
                    split[i++].charAt(0), //CLASS;
                    Integer.parseInt(split[i++]), //TCD;
                    Integer.parseInt(split[i++]), //STCD;
                    split[i++], //ROADNUMBER;
                    parseOptionalInteger(split[i++]), //RNID;
                    parseOptionalInteger(split[i++]), //N1ID;
                    parseOptionalInteger(split[i++]), //N2ID;
                    parseOptionalInteger(split[i++]), //POL_LCD;
                    Integer.parseInt(split[i++].trim()),
                    aLocationTable); //PES_LEV
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
     */
    private TMCRoad(
            final int aCid,
            final int aTabcd,
            final int aLcd,
            final char aClass,
            final int aTcd,
            final int aStcd,
            final String aROADNUMBER,
            final Integer aRnid,
            final Integer aN1id,
            final Integer aN2id,
            final Integer aPol_lcd,
            final int aPES_LEV,
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
                throw new IllegalArgumentException("illegal subtype " + aStcd + " of type " + aTabcd + " of class " + aClass + " given");
            }
        }
        ROADNUMBER = aROADNUMBER;
        RNID = aRnid;
        N1ID = aN1id;
        if (aN1id != null) {
            this.myName1 = aLocationTable.getName(aN1id);
            if (this.myName1 == null) {
                throw new IllegalArgumentException("illegal nameID " + aN1id + " given");
            }
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
            this.myAdminArea.addRoad(this);
        }
        PES_LEV = aPES_LEV;
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
     * @return the ROADNUMBER
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
     * @return the pES_LEV
     */
    public int getPES_LEV() {
        return PES_LEV;
    }


    /**
     * All segments that have this road as their "ROA_LCD".
     */
    private List<TMCSegment> mySegments = new LinkedList<TMCSegment>();
    /**
     * @param aSegment a segment that has this road as their "ROA_LCD".
     */
    public void addSegment(final TMCSegment aSegment) {
        this.mySegments.add(aSegment);
    }


    /**
     * All points that have this road as their "ROA_LCD".
     */
    private Set<TMCPoint> myPoints = new HashSet<TMCPoint>();
    /**
     * @param aPoint a point that has this road as their "ROA_LCD".
     */
    public void addPoint(final TMCPoint aPoint) {
        this.myPoints.add(aPoint);
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
    public TMCName getName1() {
        return myName1;
    }


    /**
     * @return the name2
     */
    public TMCName getName2() {
        return myName2;
    }


    /**
     * @param aLcd the lCD to set
     */
    public void setLCD(int aLcd) {
        LCD = aLcd;
    }


    /**
     * @return {@link #mySegments}
     */
    public List<TMCSegment> getSegments() {
        //TODO: sort segments
        return mySegments;
    }

    /**
     * @return {@link #mySegments}
     */
    public Set<TMCPoint> getPoints() {
        return myPoints;
    }


    /**
     * Used by {@link #generateReferenceOSM(MemoryDataSet)}.
     */
    private Relation myReferenceOSMObject = null;
    /**
     * Generate osm-entities as a reference for comaprison in an editor
     * like JOSM. These are NOT to be uploaded.
     * @param aOutData where to write to
     * @return the generated entity
     */
    public Entity generateReferenceOSM(final MemoryDataSet aOutData) {
        List<TMCSegment> segments = getSegments();
        Set<TMCPoint> points = getPoints();
        List<RelationMember> members = new ArrayList<RelationMember>();
        for (TMCSegment segment : segments) {
            Entity ref = segment.generateReferenceOSM(aOutData);
			members.add(new RelationMember(ref.getId(), EntityType.Way, "TMC:segment"));
        }
        for (TMCPoint point : points) {
            Entity ref = point.generateReferenceOSM(aOutData);
			members.add(new RelationMember(ref.getId(), EntityType.Node, "TMC:point"));
        }
        if (myReferenceOSMObject != null) {
            aOutData.addRelation(myReferenceOSMObject);
            return myReferenceOSMObject;
        }
        Relation retval = new Relation((long) (Integer.MIN_VALUE * Math.random()), 1, new Date(), OsmUser.NONE, 0);
        retval.getTags().add(new Tag("name", getROADNUMBER() + " - DO NOT UPLOAD"));
        if (this.myName1 != null) {
            retval.getTags().add(new Tag("tmc:name1", this.myName1.getNAME() + " - " + this.myName1.getNCOMMENT()));        	
        }
        if (this.myName2 != null) {
            retval.getTags().add(new Tag("tmc:name2", this.myName2.getNAME() + " - " + this.myName2.getNCOMMENT()));        	
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
        retval.getMembers().addAll(members);
        aOutData.addRelation(retval);
        myReferenceOSMObject = retval;
        return retval;
    }


    /**
     * Compare Roads by ID.
     */
    private static final class RoadComparator implements
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
