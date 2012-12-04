package org.openstreetmap.travelingsalesman.routing.routers;

import java.util.Comparator;
import java.util.Map;

public class UVComparator implements Comparator<Long>
{

    Map<Long, Double> map;

    public UVComparator(Map<Long, Double> map)
    {
      this.map = map;
    }
    @Override
    public int compare(Long o1, Long o2)
    {
        if(map.get(o1) > map.get(o2))
            return -1;
        else
            return 1;
    }

}
