package org.openstreetmap.travelingsalesman.routing;

import java.util.Date;

import org.openstreetmap.osmosis.core.domain.common.SimpleTimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

/**
 * 
 * A segment is a part of a Routing Step.
 * Consists of a Start Node, an end node and the UV Exposure along the left and right walkways.
 * It is a serializable entity and implements the Comparable interface.
 * 
 * @author Atindriyo Sanyal, UCLA
 *
 */

public class Segment extends Entity implements ISegment, Comparable<Segment>
{
    public WayNode start;
    public WayNode end;
    double uvLeft, uvRight;

    public Segment(long id, int version, Date timestamp, OsmUser user,
            long changesetId, WayNode start, WayNode end)
    {
        // Chain to the more-specific constructor
        this(id, version, new SimpleTimestampContainer(timestamp), user,
                changesetId, start, end);
    }

    @SuppressWarnings("deprecation")
    public Segment(long id, int version, TimestampContainer timestampContainer,
            OsmUser user, long changesetId, WayNode start, WayNode end)
    {
        super(id, version, timestampContainer, user, changesetId); // sets the
                                                                   // entity
                                                                   // data using
                                                                   // the params
                                                                   // passed
        this.start = start;
        this.end = end;
    }

    public double getUVLeft()
    {
        return this.uvLeft;
    }

    public double getUVRight()
    {
        return this.uvRight;
    }

    @Override
    public int compareTo(Segment segment)
    {
        return ((this.uvLeft + this.uvRight)
                - (segment.uvLeft + segment.uvRight) > 0) ? 1 : -1;
    }

    @Override
    public EntityType getType()
    {
        return null;
    }

    @Override
    public Entity getWriteableInstance()
    {
        return null;
    }

}