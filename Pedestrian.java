package org.openstreetmap.travelingsalesman.routing.selectors;

import org.openstreetmap.osm.ConfigurationSection;
import org.openstreetmap.osm.data.IDataSet;
import org.openstreetmap.osm.data.NodeHelper;
import org.openstreetmap.osm.data.WayHelper;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.travelingsalesman.routing.IVehicle;

public class Pedestrian implements IVehicle
{

    @Override
    public boolean isAllowed(IDataSet aMap, Relation aRelation)
    {
        return false;
    }

    @Override
    public ConfigurationSection getSettings()
    {
        return null;
    }

    @Override
    public boolean isAllowed(IDataSet aMap, Node node)
    {
      String isWalkable= NodeHelper.getTag(node, UsedTags.TAG_ACCESS_PEDESTRIAN );
      //If it's not walkable
      if (isWalkable != null && 
         (isWalkable.equalsIgnoreCase("no") || 
          isWalkable.equalsIgnoreCase("false")))
      {
          return false;
      }
      return true;
    }

    @Override
    public boolean isReverseOneway(IDataSet aMap, Way way)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOneway(IDataSet aMap, Way way)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAllowed(IDataSet aMap, Way way)
    {
        String isWalkable= WayHelper.getTag(way, UsedTags.TAG_ACCESS_PEDESTRIAN);
        //If it's not walkable
        if (isWalkable != null && 
           (isWalkable.equalsIgnoreCase("no") || 
            isWalkable.equalsIgnoreCase("false")))
        {
            return false;
        }
        return true;
    }

}
