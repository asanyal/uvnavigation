uvnavigation
============

Multi-Atrribute Navigation Dijkstra's based on Ultra violet exposure on streets

The Traveling Salesman project consists of 3 sub-projects:
1. osmnavigation
2. traveling-salesman
3. libosm

trunk-latest: 
This is the folder that contains the Traveling Salesman Navigation project.

core:
This project is called Osmosis. It's an external dependency to Traveling Salesman. As we have modified the basic classes like Node, Way etc. This project contains the source code behind them. Traveling Salesman uses the jar generated from this project.

Procedure:

Incorporate these 2 projects separately into your IDE.
The following are the project Dependencies:

1. libosm - depends on osmosis-core.jar which is generated from the Osmosis-Core project. Building libosm generates libosm.jar.

2. osmnavigation - depends on libosm.jar. Add this to the build-path.

3. traveling-salesman - depends on osmnavigation.jar. Add this to it's build path.

To run the project we follow the following steps:
1. Build osmosis-core
2. build libosm
3. build osmnavigation
4. build traveling-salesman

5. Run traveling-salesman using the following commands
import : import <path to the OSM file>
route : route -gpx <filename.gpx> [lat1,long1] [lat2,long2]

We have 3 routing experiments to show how the navigation works.

EXPERIMENT 1:
Corner of Ophir and Veteran -- Corner of Levering and Glenrock
In the run configuration for Traveling-Salesman:

import path_to_ucla-uv2.osm>
route -gpx route.gpx [34.0689618,-118.4549004] [34.066308,-118.450813]

import path_to_ucla-uv3.osm
route -gpx route.gpx [34.0689618,-118.4549004] [34.066308,-118.450813]


EXPERIMENT 2:
Corner of Malcolm and Manning -- A point on Glenmont

import path_to_ucla-uv2.osm
route -gpx route.gpx [34.0660665,-118.4385079] [34.0654861,-118.4365909]

import path_to_ucla-uv3.osm
route -gpx route.gpx [34.0660665,-118.4385079] [34.0654861,-118.4365909]


EXPERIMENT 3:
Corner of Hilgard and Manning -- A point on Le Conte

import path_to_ucla-uv2.osm
route -gpx route.gpx [34.0666168,-118.4395786] [34.0644844,-118.4371181]

import path_to_ucla-uv3.osm
route -gpx route.gpx [34.0666168,-118.4395786] [34.0644844,-118.4371181]

