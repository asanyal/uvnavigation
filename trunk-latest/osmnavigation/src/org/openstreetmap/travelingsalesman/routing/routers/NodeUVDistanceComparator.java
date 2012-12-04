
package org.openstreetmap.travelingsalesman.routing.routers;

import java.util.Comparator;
import java.util.Iterator;

import javax.swing.text.html.HTML.Tag;

import org.openstreetmap.osm.data.IDataSet;
import org.openstreetmap.osm.data.coordinates.Coordinate;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;

/**
 * Uses the Linear Weight Function method to assign priority weights to distance and Ultraviolet exposure on streets.
 * 
 * This comparator is the place where the different factors affecting the decision to choose
 * whether to take a particular road or not is decided.
 * 
 * A key point is that the comparator compares based on the distance to a "target-node "instead of strictly 
 * following the Dijkstra's Paradigm of deciding a Route solely based on the next node's distance from
 * the current.  
 * 
 * @author Atindriyo Sanyal, UCLA
 */
public class NodeUVDistanceComparator implements Comparator<Node> {

    /**
     * The node to compare the distance to.
     */
    private Node targetNode;

    /**
     * The map we operate on.
     */
    //private IDataSet myMap;

    /**
     * @param aTargetNode The node to compare the distance to.
     * @param aMap The map we operate on.
     * one for comparison.
     */
    public NodeUVDistanceComparator(final IDataSet aMap, final Node aTargetNode) {
        super();
        //this.myMap = aMap;
        this.targetNode = aTargetNode;
    }



    /**
     * @param stepA first node to compare
     * @param stepB second node to compare
     * @return -1 0 or 1 depending on the metrics of both
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(final Node stepA, final Node stepB) {

        double a = getMetric(stepA, stepA.getUvValue());
        double b = getMetric(stepB, stepB.getUvValue());


        if (a < b)
        {
          return -1;
        }
        if (b < a)
        {
          return +1;
        }
        return 0;
    }

    /**
     * @param nodeA the node to compare the distance for
     * @return the distance
     */
    private double getMetric(final Node nodeA, double uv) {
        double dist = (linearWeight(0)*uv) + (linearWeight(1)* Math.pow(10, 5) * Coordinate.distance(nodeA.getLatitude(), nodeA.getLongitude(),
                                       this.targetNode.getLatitude(), this.targetNode.getLongitude()));
        System.out.println("Node ID : " + nodeA.getId() + " Dist : " +  Coordinate.distance(nodeA.getLatitude(), nodeA.getLongitude(),
                this.targetNode.getLatitude(), this.targetNode.getLongitude()) + " UV : " + uv );
        return dist;
        
    }
    
    private Double linearWeight(int i)
    {
        switch (i)
        {
        case 0:
            return 0.05;
        case 1:
            return 0.95;
        default:
            return (double) 0;
        }
    }
}
