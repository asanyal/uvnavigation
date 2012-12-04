This is Traveling Salesman,

a navigation and route-planing application.

Homepage:   http://apps.sourceforge.net/mediawiki/travelingsales/
Help-Forum: http://apps.sourceforge.net/phpbb/travelingsales/index.php


=== Compiling ===

To compile Traveling Salesman, OSMNavigation and LibOSM from source,
please do the following:
Linux:
 chmod a+x ./build/apache-ant-1.7.0/bin/ant 
 ./build/apache-ant-1.7.0/bin/ant distrecursive
Windows:
 ./build/apache-ant-1.7.0/bin/ant.bat distrecursive


=== Starting ===


java -jar dist/traveling_salesman.jar


=== First Time? ===

After starting the first time you need to

a) import a map by either loading a local map
via map->import file...

or
b)
using any regionin the "download"-menu

To find a route from A to B you then enter
city and street and select the proper result
first for the start, then for the target.

To navigate you only need a target but a
gps-device is required.
a) using gpsd
If you have a local gpsd running, select
"preferences"->"osmnavigation" and
"get GPS-location from" "GPSDprovider"
b) using a serial port
"preferences"->"osmnavigation" and
"get GPS-location from" "JGPSprovider"
and a few lines above enter the serial-port
you wish to use.


If you have any questions, feel free to ask at:
http://apps.sourceforge.net/phpbb/travelingsales/index.php

Marcus Wolschon
Maintainer of Traveling Salesman
 	  	 
