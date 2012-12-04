package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openstreetmap.osm.data.IDataSet;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

public class BoundingBoxVisitor {

    /**
     * The bounds. (minimum latitude )
     */
    private double myBottom = Double.MAX_VALUE;
    /**
     * The bounds. (maximum latitude )
     */
    private double myTop = Double.MIN_VALUE;
    /**
     * The bounds. (minimum longitude)
     */
    private double myLeft = Double.MAX_VALUE;
    /**
     * The bounds. (maximum longitude )
     */
    private double myRight = Double.MIN_VALUE;
    /**
     * @return the bottom
     */
    public double getBottom() {
        return myBottom;
    }
    /**
     * @return the top
     */
    public double getTop() {
        return myTop;
    }
    /**
     * @return the left
     */
    public double getLeft() {
        return myLeft;
    }
    /**
     * @return the right
     */
    public double getRight() {
        return myRight;
    }
    /**
     * Add a rectangle with 4 nodes to the map and return the
     * ID of the rectangle-way.
     * @param aMap the map to add to
     * @return the id of the way
     */
    public long getOSMRectangle(final IDataSet aMap) {
        Node lu = new Node((long) (Integer.MIN_VALUE * Math.random()), 0, new Date(), OsmUser.NONE, 0, getBottom(), getLeft());
        Node ru = new Node((long) (Integer.MIN_VALUE * Math.random()), 0, new Date(), OsmUser.NONE, 0, getBottom(), getRight());
        Node lo = new Node((long) (Integer.MIN_VALUE * Math.random()), 0, new Date(), OsmUser.NONE, 0, getTop(), getLeft());
        Node ro = new Node((long) (Integer.MIN_VALUE * Math.random()), 0, new Date(), OsmUser.NONE, 0, getTop(), getRight());
        aMap.addNode(lu);
        aMap.addNode(ru);
        aMap.addNode(lo);
        aMap.addNode(ro);
        ArrayList<WayNode> wayNodes = new ArrayList<WayNode>();
        wayNodes.add(new WayNode(lu.getId()));
        wayNodes.add(new WayNode(ru.getId()));
        wayNodes.add(new WayNode(ro.getId()));
        wayNodes.add(new WayNode(lo.getId()));
        wayNodes.add(new WayNode(lu.getId()));
        Way rect = new Way((long) (Integer.MIN_VALUE * Math.random()), 0, new Date(), OsmUser.NONE, 0, new LinkedList<Tag>(), wayNodes);
        aMap.addWay(rect);
        return rect.getId();
    }

    public void visit(final TMCAdminArea anArea) {
        Set<TMCPoint> childPoints = anArea.getChildPoints();
        for (TMCPoint point : childPoints) {
            visit(point);
        }
        Set<TMCSegment> childSegments = anArea.getChildSegments();
        for (TMCSegment segment : childSegments) {
            visit(segment);
        }
        Set<TMCRoad> childRoads = anArea.getChildRoads();
        for (TMCRoad road : childRoads) {
            visit(road);
        }
        Set<TMCAdminArea> childArea = anArea.getChildAreas();
        for (TMCAdminArea area : childArea) {
            visit(area);
        }
    }
    public void visit(final TMCPoint aPoint) {
        double latitude = aPoint.getLatitude();
        double longitude = aPoint.getLongitude();
//        System.out.println("visiting point at " + latitude + " - " + longitude);
        this.myTop    = Math.max(this.myTop,    latitude);
        this.myBottom = Math.min(this.myBottom, latitude);
        this.myRight  = Math.max(this.myRight,  longitude);
        this.myLeft   = Math.min(this.myLeft,   longitude);
    }
    public void visit(final TMCSegment aSegment) {
//        System.out.println("visiting segment " + aSegment.getLCD() + " named " + aSegment.getROADNUMBER());
        List<TMCPoint> childPoints = aSegment.getPoints();
        for (TMCPoint point : childPoints) {
            visit(point);
        }
//        System.out.println("visiting segment " + aSegment.getLCD() + " named " + aSegment.getROADNUMBER() + " DONE");
    }
    public void visit(final TMCRoad aRoad) {
    	List<TMCSegment> childSegments = aRoad.getSegments();
    	Set<TMCPoint> childPoints = aRoad.getPoints();
//        System.out.println("visiting road " + aRoad.getLCD()
//        		+ " named " + aRoad.getROADNUMBER()
//        		+ " with " + childSegments.size() + " segments"
//        		+ " with " + childPoints.size() + " points");
        for (TMCSegment segment : childSegments) {
            visit(segment);
        }
        for (TMCPoint point : childPoints) {
            visit(point);
        }
//        System.out.println("visiting road " + aRoad.getLCD() + " named " + aRoad.getROADNUMBER() + " DONE");
    }
}
