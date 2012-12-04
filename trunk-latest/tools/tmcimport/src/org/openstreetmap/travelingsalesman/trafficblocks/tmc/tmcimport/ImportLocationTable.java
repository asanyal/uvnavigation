package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.openstreetmap.osm.Tags;
import org.openstreetmap.osm.data.IDataSet;
import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osm.data.WayHelper;
import org.openstreetmap.osm.data.coordinates.Bounds;
import org.openstreetmap.osm.data.coordinates.LatLon;
import org.openstreetmap.osm.data.osmbin.v1_0.OsmBinDataSetV10;
import org.openstreetmap.osm.io.DataSetSink;
import org.openstreetmap.osm.io.FileLoader;
import org.openstreetmap.osm.io.FileWriter;
import org.openstreetmap.osm.io.URLDownloader;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.travelingsalesman.routing.selectors.UsedTags;

/**
 * This class imports a TMC-location-table into
 * the live OpenStreetMap on the Internet.
 * Read instructions carefully before using.
 */
public class ImportLocationTable {

    private TMCImportStatus myStatus;
    private Properties mySettings;
    private TMCLocationTable myLocationTable;
    /**
     * The directory with the location-table.
     */
    private File myDirectory;
    /**
     * The directory below the location-table
     * where we write some status-files for the user.
     */
    private File myLogDirectory;

    /**
     * Automatically created logger for debug and error-output.
     */
    private static final Logger LOG = Logger.getLogger(ImportLocationTable.class
            .getName());


    /**
     * Run the import.
     * @param dir the directory with the location-table.
     * @throws IOException if something goes wrong
     */
    public ImportLocationTable(final File dir) throws IOException {
        this.myDirectory = dir;
        this.myLogDirectory = new  File(myDirectory, new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss_ssss").format(new Date()));
        myLocationTable = new TMCLocationTable(dir);
        LOG.info("Import of location-table " + dir.getPath() + " started...");

        mySettings = new Properties();
        myStatus = new TMCImportStatus(dir);
        mySettings.load(myLocationTable.getClass().getClassLoader()
                 .getResourceAsStream("org/openstreetmap/travelingsalesman/trafficblocks/tmc/tmcimport/tmcimport.properties"));
        //Step 1 - import administrative areas

        //Step 2 - import TMC "ROADS"
        importRoads();
        //Step 3 - import segments of roads
   //TODO     importSegments();
        //Step 4 - import points
      //TODO     importPoints();

    }


    /**
     * @param args either empty or 1 directory-name
     */
    public static void main(final String[] args) {

        try {
            configureLoggingConsole();
            configureLoggingLevelAll();
            String dirname = "."
                + File.separator + "src"
                + File.separator + "org"
                + File.separator + "openstreetmap"
                + File.separator + "travelingsalesman"
                + File.separator + "trafficblocks"
                + File.separator + "tmc"
                + File.separator + "TMC-Testdata"
                + File.separator + "Germany"
                + File.separator + "LCL8.00.D-081201";
            if (args.length == 1) {
                dirname = args[0];
            }
            File dir = new File(dirname);
            if (!dir.exists()) {
                System.err.println("Directory: " + dir.getAbsolutePath() + " does not exist.");
                return;
            }
            ImportLocationTable me = new ImportLocationTable(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Import all TMC-roads.
     * @throws IOException if something goes wrong
     */
    private void importRoads() throws IOException {
        LOG.info("importing roads...( logging into " + myLogDirectory.getAbsolutePath() + ")");
        myLogDirectory.mkdirs();
        java.io.FileWriter log = new java.io.FileWriter(new File(myLogDirectory, "Roads.TODO.txt"));
        java.io.FileWriter doneLog = new java.io.FileWriter(new File(myLogDirectory, "Roads.DONE.txt"));
        log.append("This file contains all the road-elemnts form the imported TMC location-table\n");
        log.append("that we could not find in the OpenStreetMap.\n\n");
        log.append("You must now:\n");
        log.append("Find all parts of that road and group them in a relation tagged as:\n");
        log.append(" type=route\n");
        log.append(" route=road\n");
        log.append(" ref={specified ref of the road}\n");
        log.append(" You may leave the role of the relation-elements empty\n");
        log.append("================\n");
        log.flush();
        //---------- write the road-network for reference
        {
            File outFile = new File(myLogDirectory, "TMC_location_network.osm");
            MemoryDataSet outData = new MemoryDataSet();
            Collection<TMCRoad> allRoads = myLocationTable.getAllRoads();
            for (TMCRoad road : allRoads) {
                road.generateReferenceOSM(outData);
            }
            Collection<TMCSegment> allSegments = myLocationTable.getAllSegments();
            for (TMCSegment segment : allSegments) {
                segment.generateReferenceOSM(outData);
            }
            Collection<TMCPoint> allPoints = myLocationTable.getAllPoints();
            for (TMCPoint point : allPoints) {
                point.generateReferenceOSM(outData);
            }
            int n = outData.getNodesCount();
            int w = outData.getWaysCount();
            int r = outData.getRelationsCount();
            FileWriter fileWriter = new FileWriter(outFile);
            fileWriter.writeOsm(outData);
            log.append("We created the TMC road-network (#relations=" + r + ", #ways=" + w + ", #nodes=" + n + ") in " + outFile.getAbsolutePath() + "\n");
            log.append("java -jar josm-latest.jar " + outFile.getAbsolutePath() + "\n");
            log.flush();
        }
         // This is Step 2 - import TMC "ROADS" as relations of
        // type=route
        // route=road
        // ref={roadnumber or roadnumber with one of the specified
        //                    replacements applied}
        Collection<TMCRoad> allRoads = myLocationTable.getAllRoads();
        for (TMCRoad road : allRoads) {
            if (isImported(road.getCLASS(), road.getLCD())) {
                LOG.info("importing road " + road.getLCD() + " - skipping - already impored");
                road.addUserProperty("osmEntityType", myStatus.getImportedEntityType(road.getCLASS(), road.getLCD()));
                road.addUserProperty("osmEntityID", myStatus.getImportedEntityId(road.getCLASS(), road.getLCD()));

                doneLog.append("Road " + road.getLCD() + " = " + road.getROADNUMBER()
                        + " was ALREADY imported as relation http://api.openstreetmap.org/api/0.5/relation/"
                        + myStatus.getImportedEntityId(road.getCLASS(), road.getLCD())[0] + "/full\n");
                doneLog.flush();
                continue;
            }
            LOG.info("importing road " + road.getLCD() + "...");
            Relation osmRoad = findOSMRoad(road.getROADNUMBER(), log, road);
            int count = Integer.parseInt(mySettings.getProperty("tmcimport.national.refSeachRule.count", "0"));
            for (int i = 0; i < count; i++) {
                if (osmRoad != null) {
                    break;
                }
                String match = mySettings.getProperty("tmcimport.national.refSeachRule." + i + ".search");
                String replace = mySettings.getProperty("tmcimport.national.refSeachRule." + i + ".replace");
                if (match == null) {
                    throw new IllegalStateException("missing entry 'tmcimport.national.refSeachRule." + i + ".search' in tmcimport.properties");
                }
                if (replace == null) {
                    throw new IllegalStateException("missing entry 'tmcimport.national.refSeachRule." + i + ".replace' in tmcimport.properties");
                }
                String newRef = road.getROADNUMBER().replaceAll(match, replace);
                if (newRef.equals(road.getROADNUMBER()) || newRef.trim().length() == 0) {
                    continue; // no replacement made
                }
                LOG.info("importing road " + road.getLCD() + " - '" + road.getROADNUMBER() + "' not found. Trying to find it by as ref=\"" + newRef + "\"");

                osmRoad = findOSMRoad(newRef, log, road);
            }
            if (osmRoad == null) {
                // no matching OSM-entity found
                LOG.warning("importing road " + road.getLCD() + "...not found in OSM. Please create the relation for the road \"" + road.getROADNUMBER() + "\"");
                log.append("================\n");
                log.append("Missing road: #" + road.getLCD() + " with ref=" + road.getROADNUMBER() + "\n");
                if (road.getSegments().size() > 0) {
                    Iterator<TMCSegment> iterator = road.getSegments().iterator();
                    while (iterator.hasNext()) {
                        TMCSegment next = iterator.next();
                        if (next.getPoints().size() > 0) {
                            TMCPoint next2 = next.getPoints().iterator().next();
                            log.append("should be here: http://openstreetmap.org/?lat=" + next2.getLatitude() + "&lon=" + next2.getLongitude() + "&zoom=15&layers=B000FTF \n");
                            break;
                        }
                    }
                }
                File outFile = new File(myLogDirectory, "missingRoad_TMC" + road.getLCD() + ".osm");
                MemoryDataSet outData = new MemoryDataSet();
                road.generateReferenceOSM(outData);
                int n = outData.getNodesCount();
                int w = outData.getWaysCount();
                int r = outData.getRelationsCount();
                FileWriter fileWriter = new FileWriter(outFile);
                fileWriter.writeOsm(outData);
                log.append("We created a missing relation (#relations=" + r + ", #ways=" + w + ", #nodes=" + n + ") for the road " + road.getROADNUMBER() + "\n");
                log.append("A representation that is NOT TO BE UPLOADED of the tmc-road has also been added to that file.\n");
                log.append("Please compare with the file below and tag the correct roads in OSM as\n");
                log.append("\"ref=" + road.getROADNUMBER() + "\"\n");
                log.append("\"" + getTagPrefix() + ":LocationCode=" + Integer.toString(road.getLCD()) + "\"\n");
                log.append("\"" + getTagPrefix() + ":Road:Roadnumber="   + road.getROADNUMBER() + "\"\n");
                log.append("\"" + getTagPrefix() + ":Road:TypeName=" + road.getType().getTDESC() + "\"\n");
                if (road.getType().getTNATDESC() != null) {
                    log.append("\"" + getTagPrefix() + ":Road:TypeName:loc=" + road.getType().getTNATDESC() + "\"\n");
                }
                if (road.getSubType() != null) {
                    log.append("\"" + getTagPrefix() + ":Road:SubTypeName=" + road.getSubType().getTDESC() + "\"\n");
                    if (road.getSubType().getTNATDESC() != null) {
                        log.append("\"" + getTagPrefix() + ":Road:SubTypeName:loc=" + road.getSubType().getTNATDESC() + "\"\n");
                    }
                }
                log.append("java -jar josm-latest.jar " + outFile.getAbsolutePath() + "\n");
                log.flush();
            } else {
                osmRoad.getTags().add(new Tag(getTagPrefix() + ":LocationCode", Integer.toString(road.getLCD())));
                osmRoad.getTags().add(new Tag(getTagPrefix() + ":Road:Roadnumber", road.getROADNUMBER()));
                //TODO: these are wrong
//                if (road.getName1() != null) {
//                    osmRoad.getTags().add(new Tag(getTagPrefix() + ":Road:Name1", road.getName1().getNAME()));
//                }
//                if (road.getName2() != null) {
//                    osmRoad.getTags().add(new Tag(getTagPrefix() + ":Road:Name2", road.getName2().getNAME()));
//                }
                osmRoad.getTags().add(new Tag(getTagPrefix() + ":Road:TypeName", road.getType().getTDESC()));
                if (road.getType().getTNATDESC() != null) {
                    osmRoad.getTags().add(new Tag(getTagPrefix() + ":Road:TypeName:loc", road.getType().getTNATDESC()));
                }
                if (road.getSubType() != null) {
                    osmRoad.getTags().add(new Tag(getTagPrefix() + ":Road:SubTypeName", road.getSubType().getTDESC()));
                    if (road.getSubType().getTNATDESC() != null) {
                        osmRoad.getTags().add(new Tag(getTagPrefix() + ":Road:SubTypeName:loc", road.getSubType().getTNATDESC()));
                    }
                }
                /*
                * <osmChange version="{0}" generator="TMCImport">
                *   <modify>
                *      <relation id="{1}" timestamp="{2}" changeset="{3}">
                *      {4 = original content}
                *      <tag k="TMC...." value="{5}"/>
                *      </relation>
                *   </modify>
                * </osmChange>
                */

                getCache().addRelation(osmRoad);
                saveCache();
                doneLog.append("Road " + road.getLCD() + " = " + road.getROADNUMBER() + " imported as relation http://api.openstreetmap.org/api/0.5/relation/" + osmRoad.getId() + "/full\n");
                doneLog.flush();
                myStatus.setImported(road.getCLASS(), road.getLCD(), osmRoad.getId(), osmRoad.getId(), osmRoad.getType());
            }
        }
        LOG.info("importing roads...DONE");
        log.close();
        doneLog.close();
    }

    /**
     * Import all TMC-segments.
     * @throws IOException if something goes wrong
     */
    private void importSegments() throws IOException {
        LOG.info("importing segments...");
        //TODO
        Collection<TMCSegment> allSegments = myLocationTable.getAllSegments();
        for (TMCSegment segment : allSegments) {
            if (isImported(segment.getCLASS(), segment.getLCD())) {
                LOG.info("importing segment " + segment.getLCD() + " - skipping - already impored");
                segment.addUserProperty("osmEntityType", myStatus.getImportedEntityType(segment.getCLASS(), segment.getLCD()));
                segment.addUserProperty("osmEntityID", myStatus.getImportedEntityId(segment.getCLASS(), segment.getLCD()));
                continue;
            }
            //LOG.info("importing segment " + segment.getLCD() + "...");
            List<TMCPoint> points = segment.getPoints();
            TMCRoad road = segment.getRoad();
            if (road == null) {
                //TODO: free segment
                LOG.info("importing free segment " + segment.getLCD() + "...");
            } else {
                // segment in road
                if (!isImported(road.getCLASS(), road.getLCD())) {
                    LOG.info("skipping segment " + segment.getLCD() + " of road " + road.getROADNUMBER() + " because the road was not imported yet");
                    continue;
                }
                LOG.info("importing segment " + segment.getLCD() + " of road " + road.getROADNUMBER() + " ...");
                long roadRelationID = myStatus.getImportedEntityId(road.getCLASS(), road.getLCD())[0];
                Relation roadRelation = getRelation(roadRelationID);
                List<RelationMember> members = roadRelation.getMembers();
                Set<Way> ways = new HashSet<Way>();
                Set<Node> nodes = new HashSet<Node>();
                for (RelationMember relationMember : members) {
                    if (relationMember.getMemberRole() != null || relationMember.getMemberRole().trim().length() != 0) {
                        continue;
                    }
                    if (!relationMember.getMemberType().equals(EntityType.Way)) {
                        continue;
                    }
                    Way w = getWay(relationMember.getMemberId());
                    if (w == null) {
                        continue;
                    }
                    ways.add(w);
                    List<WayNode> wayNodes = w.getWayNodes();
                    for (WayNode wayNode : wayNodes) {
                        Node n = getNode(wayNode.getNodeId());
                        if (n != null) {
                            nodes.add(n);
                        }
                    }
                }

                Node nodeOfLastPointForward = null;
                Node nodeOfLastPointReverse = null;
                for (TMCPoint point : points) {
                    if (isImported(point.getCLASS(), point.getLCD())) {
                        continue;
                    }
                    Collection<Node> possibleForward = geNearNodesForward(point, nodes).values();
                    Collection<Node> possibleReverse = geNearNodesReverse(point, nodes).values();

                    // if this is the first point, remove nodes that can not be reached from the last point of the prev segment
                    if (segment.getPreviousSegment() != null && isImported(segment.getPreviousSegment().getCLASS(), segment.getPreviousSegment().getLCD())) {
                        List<TMCPoint> otherPoints = segment.getPreviousSegment().getPoints();
                        TMCPoint otherPoint = otherPoints.get(otherPoints.size() - 1);
                        Node otherNodeF = getNode(myStatus.getImportedEntityId(otherPoint.getCLASS(), otherPoint.getLCD())[0]);
                        Node otherNodeR = getNode(myStatus.getImportedEntityId(otherPoint.getCLASS(), otherPoint.getLCD())[1]);
                        List<Node> p2 = new LinkedList<Node>();
                        List<Node> p2r = new LinkedList<Node>();
                        for (Node n : possibleForward) {
                            if (isConnected(otherNodeF, n)) {
                                p2.add(n);
                            }
                        }
                        for (Node n : possibleReverse) {
                            if (isConnected(n, otherNodeR)) {
                                p2r.add(n);
                            }
                        }
                        possibleForward = p2;
                        possibleReverse = p2r;
                    }

                    // if this is the first point, remove nodes that can not reach the first point of the next segment
                    if (segment.getNextSegment() != null && isImported(segment.getPreviousSegment().getCLASS(), segment.getNextSegment().getLCD())) {
                        List<TMCPoint> otherPoints = segment.getPreviousSegment().getPoints();
                        TMCPoint otherPoint = otherPoints.get(0);
                        Node otherNodeF = getNode(myStatus.getImportedEntityId(otherPoint.getCLASS(), otherPoint.getLCD())[0]);
                        Node otherNodeR = getNode(myStatus.getImportedEntityId(otherPoint.getCLASS(), otherPoint.getLCD())[1]);
                        List<Node> p2 = new LinkedList<Node>();
                        List<Node> p2r = new LinkedList<Node>();
                        for (Node n : possibleForward) {
                            if (isConnected(n, otherNodeF)) {
                                p2.add(n);
                            }
                        }
                        for (Node n : possibleReverse) {
                            if (isConnected(otherNodeR, n)) {
                                p2r.add(n);
                            }
                        }
                        possibleForward = p2;
                        possibleReverse = p2r;
                    }
                    // for each direction, find a route from the first point´s node to the last along this segment
                    Node nodeForward = null;
                    for (Node n : possibleForward) {
                        if (nodeOfLastPointForward == null || isConnected(nodeOfLastPointForward, n)) {
                            nodeForward = n;
                            break;
                        }
                    }
                    Node nodeReverse = null;
                    for (Node n : possibleReverse) {
                        if (nodeOfLastPointReverse == null || isConnected(n, nodeOfLastPointReverse)) {
                            nodeForward = n;
                            break;
                        }
                    }
                    if (nodeForward == null) {
                        LOG.warning("Found no candidate (direction=forward) in OSM for TMC-point" + point.getLCD());
                        return;
                    }
                    if (nodeReverse == null) {
                        LOG.warning("Found no candidate (direction=reverse) in OSM for TMC-point" + point.getLCD());
                        return;
                    }
                    //TODO: tag point
                    nodeOfLastPointForward = nodeForward;
                    nodeOfLastPointReverse = nodeReverse;
                } // for(Points)
                //TODO create a relation of all ways and nodes of this segment for both directions
                //TODO: tag the individual ways too
            }
        }
    }



    /**
     * Get all nodes from the given list that are near enough to
     * the give point to represent it.
     * @param aPoint the point that needs representation
     * @param aNodes the nodes to filter
     * @return [0=forward, 1=reverse] all nodes within a given distance in ascending order of distance
     * @throws IOException if we cannot read the map
     */
    private SortedMap<Integer, Node> geNearNodesForward(final TMCPoint aPoint, final Set<Node> aNodes) throws IOException {
        SortedMap<Integer, Node> retvalF = new TreeMap<Integer, Node>();
        final double maxDistance = 50;
        LatLon p = new LatLon(aPoint.getLatitude(), aPoint.getLongitude());
        for (Node node : aNodes) {
            double distance = LatLon.distanceInMeters(p, new LatLon(node.getLatitude(), node.getLongitude()));
            if (distance < maxDistance) {
                // test if prev-node is reachable
                if (aPoint.getNextPoint() != null
                        && isImported(aPoint.getNextPoint().getCLASS(), aPoint.getNextPoint().getLCD())) {
                    long[] otherNodeID = myStatus.getImportedEntityId(aPoint.getNextPoint().getCLASS(), aPoint.getNextPoint().getLCD());
                    Node node2 = getNode(otherNodeID[0]);
                    if (!isConnected(node, node2)) {
                        continue;
                    }
                }
                // test if next-node is reachable
                if (aPoint.getPreviousPoint() != null
                        && isImported(aPoint.getPreviousPoint().getCLASS(), aPoint.getPreviousPoint().getLCD())) {
                    long[] otherNodeID = myStatus.getImportedEntityId(aPoint.getPreviousPoint().getCLASS(), aPoint.getPreviousPoint().getLCD());
                    Node node2 = getNode(otherNodeID[0]);
                    if (!isConnected(node2, node)) {
                        continue;
                    }
                }
                retvalF.put((int) distance, node);
            }
        }
        return retvalF;
    }

    /**
     * Get all nodes from the given list that are near enough to
     * the give point to represent it.
     * @param aPoint the point that needs representation
     * @param aNodes the nodes to filter
     * @return [0=forward, 1=reverse] all nodes within a given distance in ascending order of distance
     * @throws IOException if we cannot read the map
     */
    private SortedMap<Integer, Node> geNearNodesReverse(final TMCPoint aPoint, final Set<Node> aNodes) throws IOException {
        SortedMap<Integer, Node> retvalR = new TreeMap<Integer, Node>();
        final double maxDistance = 50;
        LatLon p = new LatLon(aPoint.getLatitude(), aPoint.getLongitude());

        for (Node node : aNodes) {
            double distance = LatLon.distanceInMeters(p, new LatLon(node.getLatitude(), node.getLongitude()));
            if (distance < maxDistance) {
                // test if prev-node is reachable
                if (aPoint.getNextPoint() != null
                        && isImported(aPoint.getNextPoint().getCLASS(), aPoint.getNextPoint().getLCD())) {
                    long[] otherNodeID = myStatus.getImportedEntityId(aPoint.getNextPoint().getCLASS(), aPoint.getNextPoint().getLCD());
                    Node node2 = getNode(otherNodeID[1]);
                    if (!isConnected(node2, node)) {
                        continue;
                    }
                }
                // test if next-node is reachable
                if (aPoint.getPreviousPoint() != null
                        && isImported(aPoint.getPreviousPoint().getCLASS(), aPoint.getPreviousPoint().getLCD())) {
                    long[] otherNodeID = myStatus.getImportedEntityId(aPoint.getPreviousPoint().getCLASS(), aPoint.getPreviousPoint().getLCD());
                    Node node2 = getNode(otherNodeID[1]);
                    if (!isConnected(node, node2)) {
                        continue;
                    }
                }
                retvalR.put((int) distance, node);
            }
        }
        return retvalR;
    }


    /**
     * Can we get from aNode to aNode2 using roads
     * a ways can drive on.
     * @param aNode
     * @param aNode2
     * @return
     */
    private boolean isConnected(final Node aNode, final Node aNode2) {
        // TODO Auto-generated method stub
        return false;
    }


    /**
     * @param aRoadRelationID the relation
     * @return the relation or null
     */
    private Relation getRelation(final long aRoadRelationID) {
        Relation ret = getCache().getRelationByID(aRoadRelationID);
        if (ret != null) {
            return ret;
        }
        // TODO use the API
        // http://api.openstreetmap.org/api/0.5/relation/{0}/full
        return null;
    }

    /**
     * @param aWayID the way
     * @return the way or null
     */
    private Way getWay(final long aWayID) {
        Way ret = getCache().getWaysByID(aWayID);
        if (ret != null) {
            return ret;
        }
        // TODO use the API
        // http://api.openstreetmap.org/api/0.5/way/{0}/full
        return null;
    }


    /**
     * @param aNodeID the node
     * @return the node or null
     */
    private Node getNode(final long aNodeID) {
        Node ret = getCache().getNodeByID(aNodeID);
        if (ret != null) {
            return ret;
        }
        // TODO use the API
        // http://api.openstreetmap.org/api/0.5/node/{0}/full
        return null;
    }

    /**
     * Import all TMC-points.
     * @throws IOException if something goes wrong
     */
    private void importPoints() throws IOException {
        LOG.info("importing points...");
        //TODO
    }
    /**
     * @return the prefix to all our tags
     */
    private String getTagPrefix() {
        return "TMC:cid_" + mySettings.getProperty("tmcimport.CountryID")
            + ":tabcd_" + mySettings.getProperty("tmcimport.TMCTableNumber", "1");
    }

    /**
     * The map we are using locally.
     */
    private IDataSet myCache = null;

    private void saveCache() {
        IDataSet cache = getCache();
        File cacheFile = new File(myDirectory, "cache.osm");
        FileWriter writer = new FileWriter(cacheFile);
        writer.writeOsm(cache);
    }
    private IDataSet getCache() {
        if (myCache == null) {
            File cacheFile = new File(myDirectory, "cache.osm");
            if (cacheFile.exists()) {
                LOG.info("loading cache.osm...");
                FileLoader loader = new FileLoader(cacheFile);
                myCache = loader.parseOsm();
                LOG.info("loading cache.osm...done");
            } else {
                myCache = new MemoryDataSet();
            }
        }
        return myCache;
    }
    /**
     * Find a road in OpenStreetMap by it`s ref.
     * @param aRef the value of the ref-attribute
     * @param aLog a log to write to if we create a missing relation from the ways
     * @return the relation or null
     * @throws IOException if we cannot write to the log
     */
    private Relation findOSMRoad(final String aRef, final java.io.FileWriter aLog, final TMCEntity anEntity) throws IOException {
        //TODO: DEBUG
//        if (!aRef.startsWith("A")) {
//            return null;
//        }
        if (aRef == null || aRef.trim().length() == 0) {
            return null;
        }
        // try local map first, then osmxapi
        IDataSet cache = getCache();
        Iterator<Relation> cachedRelations = cache.getRelations(Bounds.WORLD);
        while (cachedRelations.hasNext()) {
            Relation next = cachedRelations.next();
            String ref = WayHelper.getTag(next, Tags.TAG_REF);
            if (next != null && ref != null && ref.equalsIgnoreCase(aRef)) {
                return next;
            }
        }

        // try OSMXAPI
        try {
            final int sleepTime = 10000; // sleep time to not overload the server
            Thread.sleep(sleepTime);
            final String urlFormat = "http://osmxapi.hypercube.telascience.org/api/0.5/relation[ref={0}]";
            URL url = new URL(MessageFormat.format(urlFormat, aRef));
            URLDownloader dl = new URLDownloader(url);
            dl.setSink(new DataSetSink(getCache()));
            LOG.info("downloading: " + url);
            dl.run();
            saveCache();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "[Exception] Problem in "
                       + getClass().getName(),
                         e);
            return null;
        }

        cachedRelations = cache.getRelations(Bounds.WORLD);
        while (cachedRelations.hasNext()) {
            Relation next = cachedRelations.next();
            String ref = WayHelper.getTag(next, Tags.TAG_REF);
            if (next != null && ref != null && ref.equalsIgnoreCase(aRef)) {
                return next;
            }
        }

        // try OSMXAPI with ways
        try {
            final int sleepTime = 10000; // sleep time to not overload the server
            Thread.sleep(sleepTime);
            final String urlFormat = "http://osmxapi.hypercube.telascience.org/api/0.5/way[ref={0}]";
            URL url = new URL(MessageFormat.format(urlFormat, aRef));
            URLDownloader dl = new URLDownloader(url);
            dl.setSink(new DataSetSink(getCache()));
            LOG.info("downloading: " + url);
            dl.run();
            saveCache();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "[Exception] Problem in "
                       + getClass().getName(),
                         e);
            return null;
        }

        // try downloading all ways and creating that relation
        Iterator<Way> ways = cache.getWaysByTag(UsedTags.TAG_REF, aRef);
        if (ways.hasNext()) {
            Long id = (long) (Integer.MIN_VALUE * Math.random());
            File outFile = new File(myLogDirectory, "missingRelation_" + id + ".osm");
            MemoryDataSet outData = new MemoryDataSet();
            anEntity.generateReferenceOSM(outData);
            int n = outData.getNodesCount();
            int w = outData.getWaysCount();
            int r = outData.getRelationsCount();
            if (w == 0) {
                return null;
            }
            Relation missingRelation = new Relation(id, 1, new Date(), OsmUser.NONE, 0);
            missingRelation.getTags().add(new Tag(UsedTags.TAG_REF, aRef));
            missingRelation.getTags().add(new Tag("type", "route"));
            missingRelation.getTags().add(new Tag("route", "road"));
            while (ways.hasNext()) {
                Way way = ways.next();
                List<Node> nodes = cache.getWayHelper().getNodes(way);
                for (Node node : nodes) {
                    outData.addNode(node);
                }
                outData.addWay(way);
                missingRelation.getMembers().add(new RelationMember(way.getId(), way.getType(), ""));
            }
            outData.addRelation(missingRelation);
            FileWriter fileWriter = new FileWriter(outFile);
            fileWriter.writeOsm(outData);
            LOG.info("We created a missing relation (#relations=" + r + ", #ways=" + w + ", #nodes=" + n + ") for the road " + aRef + "\n");
            aLog.append("###########################\n");
            aLog.append("We created a missing relation (#relations=" + r + ", #ways=" + w + ", #nodes=" + n + ") for the road " + aRef + "\n");
            aLog.append("A representation that is NOT TO BE UPLOADED of the tmc-road has also been added to that file.\n");
            aLog.append("Please review and upload\n");
            aLog.append("java -jar josm_latest " + outFile.getAbsolutePath() + "\n");
            return missingRelation;
        }
        return null;
    }


    /**
     * Has this TMC-entity been imported yet?
     * @param aClass 'L'ine, 'P'oint or 'A'rea
     * @param aLocationCode the TMC-entity
     * @return true if imported already
     * @throws IOException if we cannot load our status.
     */
    public boolean isImported(final char aClass, final int aLocationCode) throws IOException {
        if (this.myStatus.isImported(aClass, aLocationCode)) {
            return true;
        }
     // TODO ask OSMXAPI for all entities tagged for this LCL
        return false;
    }


    /**
     * Configures logging to write all output to the console.
     */
    private static void configureLoggingConsole() {
        Logger rootLogger = Logger.getLogger("");

        // Remove any existing handlers.
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // add a debug-file
//        try {
//            Handler fileHandler = new FileHandler("e:\\libosm.osmbin.debug.log");
//            fileHandler.setLevel(Level.FINEST);
//            fileHandler.setFormatter(new SimpleFormatter());
//            Logger.getLogger("org.openstreetmap.osm.data.osmbin").addHandler(fileHandler);
//        } catch (Exception e) {
//            System.err.println("Could not create debug-log for tracing osmbin");
//            e.printStackTrace();
//        }

        // Add a new console handler.
        Handler consoleHandler = new StdoutConsoleHandler();
        consoleHandler.setLevel(Level.FINER);
        consoleHandler.setFilter(new Filter() {

            @Override
            public boolean isLoggable(final LogRecord aRecord) {
                Level level = aRecord.getLevel();
                return !level.equals(Level.WARNING) && !level.equals(Level.SEVERE);
            }
        });
        rootLogger.addHandler(consoleHandler);

        Handler consoleHandlerErr = new ConsoleHandler();
        consoleHandlerErr.setLevel(Level.WARNING);
        rootLogger.addHandler(consoleHandlerErr);
    }
    /**
     * Configures the logging level.
     */
    private static void configureLoggingLevelAll() {
        Logger.getLogger("").setLevel(Level.ALL);
        Logger.getLogger("sun").setLevel(Level.WARNING);
        Logger.getLogger("com.sun").setLevel(Level.WARNING);
        Logger.getLogger("java").setLevel(Level.WARNING);
        Logger.getLogger("javax").setLevel(Level.WARNING);

        // general log-level
        Logger.getLogger("org.openstreetmap").setLevel(Level.INFO);
    }

    /**
     * This <tt>Handler</tt> publishes log records to <tt>System.out</tt>.
     * By default the <tt>SimpleFormatter</tt> is used to generate brief summaries.
     * <p>
     * <b>Configuration:</b>
     * By default each <tt>ConsoleHandler</tt> is initialized using the following
     * <tt>LogManager</tt> configuration properties.  If properties are not defined
     * (or have invalid values) then the specified default values are used.
     * <ul>
     * <li>   java.util.logging.ConsoleHandler.level
     *    specifies the default level for the <tt>Handler</tt>
     *    (defaults to <tt>Level.INFO</tt>).
     * <li>   java.util.logging.ConsoleHandler.filter
     *    specifies the name of a <tt>Filter</tt> class to use
     *    (defaults to no <tt>Filter</tt>).
     * <li>   java.util.logging.ConsoleHandler.formatter
     *    specifies the name of a <tt>Formatter</tt> class to use
     *        (defaults to <tt>java.util.logging.SimpleFormatter</tt>).
     * <li>   java.util.logging.ConsoleHandler.encoding
     *    the name of the character set encoding to use (defaults to
     *    the default platform encoding).
     * </ul>
     * <p>
     * @version 1.13, 11/17/05
     * @since 1.4
     */

    public static class StdoutConsoleHandler extends StreamHandler {
        /**
         *  Private method to configure a ConsoleHandler.
         */
        private void configure() {
        setLevel(Level.INFO);
        setFilter(null);
        setFormatter(new SimpleFormatter());
        }

        /**
         * Create a <tt>ConsoleHandler</tt> for <tt>System.err</tt>.
         * <p>
         * The <tt>ConsoleHandler</tt> is configured based on
         * <tt>LogManager</tt> properties (or their default values).
         */
        public StdoutConsoleHandler() {
        configure();
        setOutputStream(System.out);
        }

        /**
         * Publish a <tt>LogRecord</tt>.
         * <p>
         * The logging request was made initially to a <tt>Logger</tt> object,
         * which initialized the <tt>LogRecord</tt> and forwarded it here.
         * <p>
         * @param  record  description of the log event. A null record is
         *                 silently ignored and is not published
         */
        public void publish(final LogRecord record) {
        super.publish(record);
        flush();
        }

        /**
         * Override <tt>StreamHandler.close</tt> to do a flush but not
         * to close the output stream.  That is, we do <b>not</b>
         * close <tt>System.err</tt>.
         */
        public void close() {
        flush();
        }
    }


}
