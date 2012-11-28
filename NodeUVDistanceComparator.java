package org.openstreetmap.travelingsalesman.routing.routers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.openstreetmap.osm.data.IDataSet;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;

/**
 * 
 * @author Atindriyo Sanyal
 *
 */
public class NodeUVDistanceComparator implements Comparator<Node>
{

    private HashMap<Node, Double> bestDistances;

    public NodeUVDistanceComparator(Map<Node, Double>bestNodeDistances)
    {
        super();
        this.bestDistances = new HashMap<Node, Double>(bestNodeDistances);
    }

    /**
     * @param stepA first node to compare
     * @param stepB second node to compare
     * @return -1 0 or 1 depending on the metrics of both
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(final Node stepA, final Node stepB) {

        double a = (bestDistances.get(stepA)!= null) ? bestDistances.get(stepA) : 0;
        double b = (bestDistances.get(stepB)!= null) ? bestDistances.get(stepB) : 0;
        
        if (a < b) {
            return -1;
        }
        if (b < a) {
            return +1;
        }
        return 0;
    }
}
