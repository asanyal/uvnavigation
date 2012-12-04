package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;


/**
 * Data-model of a TMC location-table.
 */
public class TMCLocationTable {


    /**
     * Directory where the location-table is
     * stored.
     */
    private File myDirectory;

    /**
     * All areas index by ID.
     */
    private NavigableMap<Integer, TMCAdminArea> myAdminAreas = null;
    /**
     * All areas index by ID.
     */
    private NavigableMap<Integer, TMCOtherArea> myOtherAreas = null;
    /**
     * All roads index by ID.
     */
    private Map<Integer, TMCRoad> myRoads;
    /**
     * All types index by class and type-ID.
     */
    private Map<Character, Map<Integer, TMCType>> myTypes;

    /**
     * All subtypes index by class, type and subtype-ID.
     */
    private Map<Character, Map<Integer, Map<Integer, TMCSubType>>> mySubTypes;

    /**
     * All segments index by ID.
     */
    private Map<Integer, TMCSegment> mySegments;
    /**
     * All points index by ID.
     */
    private Map<Integer, TMCPoint> myPoints;

    private Map<Integer, TMCName> myNames;
    /**
     * @param aDirectory the directory with the files in TMC-LocationTable-Exchange-Format
     * @throws IOException if we cannot read it
     */
    public TMCLocationTable(final File aDirectory) throws IOException {
        super();
        myDirectory = aDirectory;
        //the order of the import is important and specified in "exchange_form-1.pdf"
        //ignored Countries
        //ignored LocationDataSets
        //ignored LocationCodes
        // Classes
        this.myTypes = TMCType.loadAll(aDirectory, this); // Types
        this.mySubTypes = TMCSubType.loadAll(aDirectory, this); // SubTypes
        //ignored Languages
        //ignored EuroRoadNo
        myNames = TMCName.loadAll(aDirectory, this); // Names
        //ignored NameTranslations
        //ignored SubtypeTranslations
        //ignored ERno_belongs_to_country
        //AdministrativeArea
        myAdminAreas = TMCAdminArea.loadAll(myDirectory, this);
        //OtherArea
        myOtherAreas = TMCOtherArea.loadAll(myDirectory, this);
        myRoads = TMCRoad.loadAll(myDirectory, this);
        //ignored RoadNetworkLevelTypes
        mySegments = TMCSegment.loadAll(myDirectory, this);
        //ignored SegHasErno
        myPoints = TMCPoint.loadAll(myDirectory, this);
        //irrelevant Intersections
    }

    /**
     * @return the directory
     */
    public File getDirectory() {
        return myDirectory;
    }

    /**
     * @param aPol_lcd an administrative area-id
     * @return the area with that ID or null
     */
    public TMCAdminArea getAdminArea(final Integer aPol_lcd) {
        if (aPol_lcd == null) {
            return null;
        }
        return myAdminAreas.get(aPol_lcd);
    }

    /**
     * @param aRoadLocationCode a road-id
     * @return the road with that ID
     */
    public TMCRoad getRoad(final int aRoadLocationCode) {
        return myRoads.get(aRoadLocationCode);
    }

    /**
     * @param aSegmentLocationCode a segment-id
     * @return the road with that ID
     */
    public TMCSegment getSegment(final int aSegmentLocationCode) {
        return mySegments.get(aSegmentLocationCode);
    }

    /**
     * @param aPointLocationCode a point-id
     * @return the point with that ID
     */
    public TMCPoint getPoint(final int aPointLocationCode) {
        return myPoints.get(aPointLocationCode);
    }

    public TMCType getType(final char aClass, final int aTcd) {
        return myTypes.get(aClass).get(aTcd);
    }
    public TMCSubType getSubType(final char aClass, final int aTcd, final int aSTcd) {
        return mySubTypes.get(aClass).get(aTcd).get(aSTcd);
    }

    /**
     * Lookup a name.
     * @param aN1id the nameID
     * @return the name or null
     */
    public TMCName getName(final int aN1id) {
        return myNames.get(aN1id);
    }

    /**
     * Get all administrative areas of this table.
     * @return All areas
     */
    public Collection<TMCAdminArea> getAllAdminAreas() {
        return myAdminAreas.values();
    }

    /**
     * Get all other areas of this table.
     * @return All areas
     */
    public Collection<TMCOtherArea> getAllOtherAreas() {
        return myOtherAreas.values();
    }

    /**
     * Get all roads of this table.
     * @return All roads
     */
    public Collection<TMCRoad> getAllRoads() {
        return myRoads.values();
    }

    /**
     * Get all segments of this table.
     * @return All segments
     */
    public Collection<TMCSegment> getAllSegments() {
        return mySegments.values();
    }

    /**
     * Get all points of this table.
     * @return All points
     */
    public Collection<TMCPoint> getAllPoints() {
        return myPoints.values();
    }

}
