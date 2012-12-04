contained applications:

package: org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.tools

CreateTMCLocationProperties
	Take a TMC LocationCodeList(LCL) and create a file "tmclocations.properties".
	Such a file can be used by osmnavigation to display the location of an event even
	if the LCL is not imported into OpenStreetMap.
	
CreateWikiTables
	Create text-files of wiki-text in /output to be put into the
	OpenStreetMap-wiki to manually import a LocationCodeList.
	
UploadWikiTables
	Upload the files created by CreateWikiTable, keeping manual changes
	done in the wiki.
	
ImportLocationTable
	old code. Tried an automatic import here.

AutoImportAreas
	search the uploaded wiki-pages for TMC-areas that are not yet imported and
	if we can import them automatically.
	Creates an osc-file for the map-changes and updates the wiki to mark them as
	imported.
AutoImportRoads
	search the uploaded wiki-pages for TMC-roads that are not yet imported and
	if we can import them automatically.
	Creates an osc-file for the map-changes and updates the wiki to mark them as
	imported.
