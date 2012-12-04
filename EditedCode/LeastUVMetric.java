package org.openstreetmap.travelingsalesman.routing.metrics;

import org.openstreetmap.osm.ConfigurationSection;
import org.openstreetmap.osm.data.IDataSet;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.travelingsalesman.routing.Route.RoutingStep;
import org.openstreetmap.travelingsalesman.routing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Atindriyo Sanyal
 * 
 */

public class LeastUVMetric implements IRoutingMetric
{

    @Override
    public ConfigurationSection getSettings()
    {
        return null;
    }

    @Override
    public void setMap(IDataSet aMap)
    {
    }

    /**
     * @param step: a collection of segments who's cost is to be determined 
     * direction : true: left, false: right
     * @return totalUVLeft or Right magnitude
     */
    @Override
    public double getCost(RoutingStep step, boolean direction)
    {
        double totalUVLeft = 0, totalUVRight = 0;
        //List<WayNode> wayNodes = new ArrayList<WayNode>();
        List<Segment> segments = new ArrayList<Segment>();
        //wayNodes = step.getWay().getWayNodes();
        for (Segment s : segments)
        {
            // get each segment and calculate add UV values
            totalUVLeft += s.getUVLeft();
            totalUVRight += s.getUVRight();
        }

        return (direction) ? totalUVLeft : totalUVRight;
    }

    @Override
    public double getCost(Node crossing, RoutingStep from, RoutingStep to)
    {
        return 0;
    }

    @Override
    public double getCost(RoutingStep step)
    {
        return 0;
    }

}
