package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osm.io.FileWriter;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.BoundingBoxVisitor;
import org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.TMCAdminArea;
import org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.TMCLocationTable;
import org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.TMCOtherArea;
import org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.TMCPoint;
import org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.TMCRoad;
import org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport.TMCSegment;

/**
 * This class read a TMC-location-table and outputs
 * a mediawiki-table of the contained elements.
 */
public class CreateWikiTables {

	private static Properties mySettings;

	/**
	 * Each wiki-page shall contain at most this many
	 * table-rows.
	 */
    final int blocksize = 100;

    /**
     * The TMC LocationCodeTable we are reading.
     */
    private TMCLocationTable myLocationTable;

//    /**
//     * The directory with the location-table.
//     */
//    private File myDirectory;

    /**
     * The directory below the location-table
     * where we write some status-files for the user.
     */
    private File myLogDirectory;

    /**
     * Automatically created logger for debug and error-output.
     */
    private static final Logger LOG = Logger.getLogger(CreateWikiTables.class
            .getName());


    /**
     * Run the import.
     * @param dir the directory with the location-table.
     * @param aOutputDir the director to write the wiki-tables to.
     * @throws IOException if something goes wrong
     */
    public CreateWikiTables(final File dir, final File aOutputDir) throws IOException {
//    	this.myDirectory = dir;
        this.myLogDirectory = aOutputDir;
        myLocationTable = new TMCLocationTable(dir);
        LOG.info("Import of location-table " + dir.getPath() + " started...");

        readAreas();
        readOtherAreas();
        readRoads();
        readSegments();
        readPoints();
    }


    /**
     * @param args either empty or 1 directory-name
     */
    public static void main(final String[] args) {

        try {
            configureLoggingConsole();
            configureLoggingLevelAll();
        	mySettings = new Properties();
            mySettings.load(CreateWikiTables.class.getClassLoader()
                     .getResourceAsStream("org/openstreetmap/travelingsalesman/trafficblocks/tmc/tmcimport/tools/tmcimport.properties"));

            // default input-directory to use
            // is shall contain a TMC LocationCodeList
            String dirname = "."
                + File.separator + "data"
                + File.separator + "Germany"
                + File.separator + "LCL8.00.D-081201";
            if (args.length == 1) {
                dirname = args[0];
            }
            String outputdirname = "."
                + File.separator + "generated"
                + File.separator + new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss_ssss").format(new Date());
            if (args.length == 2) {
            	outputdirname = args[1];
            }
            File dir = new File(dirname);
            if (!dir.exists()) {
                System.err.println("Directory: " + dir.getAbsolutePath() + " does not exist.");
                return;
            }
            File outdir = new File(outputdirname);
            if (!outdir.exists()) {
            	outdir.mkdirs();
            }
            new CreateWikiTables(dir, outdir);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Read all TMC-areas and output them as a mediawiki-
     * table.
     * @throws IOException if something goes wrong
     */
    private void readAreas() throws IOException {
        LOG.info("reading areas...( logging into " + myLogDirectory.getAbsolutePath() + ")");
        myLogDirectory.mkdirs();

        // the file that links to the individual wiki-pages
        java.io.Writer wiki = new java.io.OutputStreamWriter(new FileOutputStream(new File(myLogDirectory, "Areas.wiki.txt")), "UTF8");
        java.io.FileWriter wikisection = null;

        //---------- write the areas
        wiki.write("==areas==\n");
        wiki.write("status:\n");
        wiki.write("* TODO\n");
        wiki.write("* INPROGRESS\n");
        wiki.write("* DONE\n");
        wiki.write("==areas to import==\n");

        Collection<TMCAdminArea> allAreas = myLocationTable.getAllAdminAreas();
        int block = -1;
        for (TMCAdminArea area : allAreas) {

        	// start a new wiki-page is needed
            if (area.getLCD() > blocksize * block) {
                if (block > 0) {
                    wikisection.write("|}\n");
                    wikisection.close();
                    wiki.flush();
                }
                block = area.getLCD() / blocksize;
                String sectionName = (block * blocksize) + " to " + (block * blocksize + blocksize);
                wikisection = new java.io.FileWriter(new File(myLogDirectory, "Areas.wiki." + sectionName.replace(' ', '_') + ".txt"));

                wiki.write("* [[" + mySettings.getProperty("wiki.prefix", "TMC/TMC_Import_Germany") + "/Areas/" + sectionName.replace(' ', '_') + "]]\n");

                wikisection.write("This page belongs to: [[" + mySettings.getProperty("wiki.prefix", "TMC/TMC_Import_Germany") + "/Areas]]<br/>\n");
                wikisection.write("To use the JOSM-links you need the [[JOSM/Plugins/RemoteControl|RemoteControl-Plugin]]."
                + " This plugin does not work with the Webstart-version of JOSM.\n");
                wikisection.write("=== " + sectionName + " ===\n");
                block++;

                wikisection.write("{| class=\"wikitable\"\n");
                wikisection.write("! LocationCode\n");
                wikisection.write("! download\n");
                wikisection.write("! OSM relation-ID\n");
                wikisection.write("! status\n");
                wikisection.write("! user\n");
            }

            String name = Integer.toString(area.getLCD());
            if (area.getName() != null) {
                name = area.getName().getNAME();
            }
            LOG.info("importing administrative area " + name + "...");
            BoundingBoxVisitor visitor = new BoundingBoxVisitor();
            visitor.visit(area);
            File outFile = new File(myLogDirectory, "TMCAdminArea_" + area.getLCD() + ".osm");
            MemoryDataSet outData = new MemoryDataSet();
            Entity relation = area.generateReferenceOSM(outData);
            long boundingBoxID = visitor.getOSMRectangle(outData);
            ((Relation) relation).getMembers().add(new RelationMember(boundingBoxID, EntityType.Way, "boundingBox"));
//            int n = outData.getNodesCount();
//            int w = outData.getWaysCount();
//            int r = outData.getRelationsCount();
            FileWriter fileWriter = new FileWriter(outFile);

            fileWriter.writeOsm(outData);

            wikisection.write("|-\n");
            wikisection.write("| " + Integer.toString(area.getLCD()) + "\n");
            String url = "https://wolschon.kleinbetrieb.biz/tmclcl/" + outFile.getName();
            wikisection.write("| [" + url + " admin area " + name);
            if (area.getType().getTDESC() != null) {
                wikisection.write(" of type \"" + area.getType().getTDESC() + "\"");
                if (area.getType().getTNATDESC() != null) {
                    wikisection.write("/\"" + area.getType().getTNATDESC() + "\"");
                }
            }
            if (area.getSubType() != null) {
                wikisection.write(" of sub type \"" + area.getSubType().getTDESC() + "\"");
                if (area.getSubType().getTNATDESC() != null) {
                    wikisection.write("/\"" + area.getSubType().getTNATDESC() + "\"");
                }
            }
            wikisection.write("]<br/>\n");
            wikisection.write("[http://127.0.0.1:8111/import?url=" + URLEncoder.encode(url.replace("https://", "http://"), "utf8")
                    + "&select=relation" + Integer.MIN_VALUE + " Open in] "
                    + "JOSM\n");
            wikisection.write("| none<br/>\n");
            if (area.getName() != null) {
                String name2 = area.getName().getNAME();
                wikisection.write("([http://www.informationfreeway.org/api/0.6/relation%5Bname%7Cref%7Cname:de%7Cshort_name:de=" + name2.replace(" ", "%20")
                        + "%5D search relations])<br/>\n");
                wikisection.write("([http://www.informationfreeway.org/api/0.6/way%5Bname%7Cname:de%7Cref=" + name2.replace(" ", "%20") + "%5D search polygons])<br/>\n");
                wikisection.write("([http://www.informationfreeway.org/api/0.6/node%5Bname%7Cname:de%7Cref=" + name2.replace(" ", "%20") + "%5D search nodes])<br/>\n");
            }
            wikisection.write("([http://127.0.0.1:8111/load_and_zoom?left=" + visitor.getLeft()
                    + "&right=" + visitor.getRight()
                    + "&top=" + visitor.getTop()
                    + "&bottom=" + visitor.getBottom()
                    + " load area in JOSM])<br/>\n");
            wikisection.write("\n");
            wikisection.write("| TODO\n");
            wikisection.write("| your name here\n");
        }
        LOG.info("importing administrative areas...DONE");
        wikisection.write("|}");
        wikisection.close();
        wiki.close();
    }


    /**
     * Read all TMC-other-areas and output them as a mediawiki-
     * table.
     * @throws IOException if something goes wrong
     */
    private void readOtherAreas() throws IOException {
        LOG.info("reading other areas...( logging into " + myLogDirectory.getAbsolutePath() + ")");
        myLogDirectory.mkdirs();

        // the file that links to the individual wiki-pages
        java.io.FileWriter wiki = new java.io.FileWriter(new File(myLogDirectory, "OtherAreas.wiki.txt"));
        java.io.FileWriter wikisection = null;

        //---------- write the areas
        wiki.write("==areas==\n");
        wiki.write("status:\n");
        wiki.write("* TODO\n");
        wiki.write("* INPROGRESS\n");
        wiki.write("* DONE\n");
        wiki.write("==areas to import==\n");

        Collection<TMCOtherArea> allAreas = myLocationTable.getAllOtherAreas();
        int block = -1;
        for (TMCOtherArea area : allAreas) {

        	// start a new wiki-page is needed
            if (area.getLCD() > blocksize * block) {
                if (block > 0) {
                    wikisection.write("|}\n");
                    wikisection.close();
                    wiki.flush();
                }
                block = area.getLCD() / blocksize;
                String sectionName = (block * blocksize) + " to " + (block * blocksize + blocksize);
                wikisection = new java.io.FileWriter(new File(myLogDirectory, "OtherAreas.wiki." + sectionName.replace(' ', '_') + ".txt"));

                wiki.write("* [[" + mySettings.getProperty("wiki.prefix", "TMC/TMC_Import_Germany") + "/OtherAreas/" + sectionName.replace(' ', '_') + "]]\n");

                wikisection.write("This page belongs to: [[" + mySettings.getProperty("wiki.prefix", "TMC/TMC_Import_Germany") + "/OtherAreas]]<br/>\n");
                wikisection.write("To use the JOSM-links you need the [[JOSM/Plugins/RemoteControl|RemoteControl-Plugin]]."
                + " This plugin does not work with the Webstart-version of JOSM.\n");
                wikisection.write("=== " + sectionName + " ===\n");
                block++;

                wikisection.write("{| class=\"wikitable\"\n");
                wikisection.write("! LocationCode\n");
                wikisection.write("! download\n");
                wikisection.write("! OSM relation-ID\n");
                wikisection.write("! status\n");
                wikisection.write("! user\n");
            }

            String name = Integer.toString(area.getLCD());
            if (area.getName() != null) {
                name = area.getName().getNAME();
            }
            LOG.info("importing administrative area " + name + "...");
            File outFile = new File(myLogDirectory, "TMOtherArea_" + area.getLCD() + ".osm");
            MemoryDataSet outData = new MemoryDataSet();
            Entity relation = area.generateReferenceOSM(outData);
//            int n = outData.getNodesCount();
//            int w = outData.getWaysCount();
//            int r = outData.getRelationsCount();
            FileWriter fileWriter = new FileWriter(outFile);

            fileWriter.writeOsm(outData);

            wikisection.write("|-\n");
            wikisection.write("| " + Integer.toString(area.getLCD()) + "\n");
            String url = "https://wolschon.kleinbetrieb.biz/tmclcl/" + outFile.getName();
            wikisection.write("| [" + url + " other area " + name);
            if (area.getType().getTDESC() != null) {
                wikisection.write(" of type \"" + area.getType().getTDESC() + "\"");
                if (area.getType().getTNATDESC() != null) {
                    wikisection.write("/\"" + area.getType().getTNATDESC() + "\"");
                }
            }
            if (area.getSubType() != null) {
                wikisection.write(" of sub type \"" + area.getSubType().getTDESC() + "\"");
                if (area.getSubType().getTNATDESC() != null) {
                    wikisection.write("/\"" + area.getSubType().getTNATDESC() + "\"");
                }
            }
            wikisection.write("]<br/>\n");
            wikisection.write("[http://127.0.0.1:8111/import?url=" + URLEncoder.encode(url.replace("https://", "http://"), "utf8")
                    + "&select=relation" + Integer.MIN_VALUE + " Open in] "
                    + "JOSM\n");
            wikisection.write("| none<br/>\n");
            if (area.getName() != null) {
                String name2 = area.getName().getNAME();
                wikisection.write("([http://www.informationfreeway.org/api/0.6/relation%5Bname%7Cref%7Cname:de%7Cshort_name:de=" + name2.replace(" ", "%20")
                        + "%5D search relations])<br/>\n");
                wikisection.write("([http://www.informationfreeway.org/api/0.6/way%5Bname%7Cname:de%7Cref=" + name2.replace(" ", "%20") + "%5D search polygons])<br/>\n");
                wikisection.write("([http://www.informationfreeway.org/api/0.6/node%5Bname%7Cname:de%7Cref=" + name2.replace(" ", "%20") + "%5D search nodes])<br/>\n");
            }
            wikisection.write("\n");
            wikisection.write("| TODO\n");
            wikisection.write("| your name here\n");
        }
        LOG.info("importing other areas...DONE");
        wikisection.write("|}");
        wikisection.close();
        wiki.close();
    }

    /**
     * Read all TMC-roads and output them as a mediawiki-
     * table.
     * @throws IOException if something goes wrong
     */
    private void readRoads() throws IOException {
        LOG.info("reading roads...( logging into " + myLogDirectory.getAbsolutePath() + ")");
        myLogDirectory.mkdirs();

        // the file that links to the individual wiki-pages
        java.io.FileWriter wiki = new java.io.FileWriter(new File(myLogDirectory, "Roads.wiki.txt"));
        java.io.FileWriter wikisection = null;

        //---------- write the roads
        wiki.write("==roads==\n");
//        {
//            File outFile = new File(myLogDirectory, "TMC_location_network.osm");
//            MemoryDataSet outData = new MemoryDataSet();
//            Collection<TMCRoad> allRoads = myLocationTable.getAllRoads();
//            for (TMCRoad road : allRoads) {
//                road.generateReferenceOSM(outData);
//            }
//            Collection<TMCSegment> allSegments = myLocationTable.getAllSegments();
//            for (TMCSegment segment : allSegments) {
//                segment.generateReferenceOSM(outData);
//            }
//            Collection<TMCPoint> allPoints = myLocationTable.getAllPoints();
//            for (TMCPoint point : allPoints) {
//                point.generateReferenceOSM(outData);
//            }
//            int n = outData.getNodesCount();
//            int w = outData.getWaysCount();
//            int r = outData.getRelationsCount();
//            FileWriter fileWriter = new FileWriter(outFile);
//            fileWriter.writeOsm(outData);
//            log.append("We created the TMC road-network (#relations=" + r + ", #ways=" + w + ", #nodes=" + n + ") in " + outFile.getAbsolutePath() + "\n");
//            log.append("java -jar josm-latest.jar " + outFile.getAbsolutePath() + "\n");
//            log.flush();
//
//            System.out.println("[" + outFile.getName() + " download all roads]");
//        }


        wiki.write("status:\n");
        wiki.write("* TODO\n");
        wiki.write("* INPROGRESS\n");
        wiki.write("* DONE\n");
        wiki.write("==roads to import==\n");

        Collection<TMCRoad> allRoads = myLocationTable.getAllRoads();
        int block = -1;
        for (TMCRoad road : allRoads) {
            LOG.info("importing road " + road.getLCD() + " \"" + road.getROADNUMBER() + "\"...");

        	// start a new wiki-page is needed
            if (road.getLCD() > blocksize * block) {
                if (block > 0) {
                    wikisection.write("|}\n");
                    wikisection.close();
                    wiki.flush();
                }
                block = road.getLCD() / blocksize;
                String sectionName = (block * blocksize) + " to " + (block * blocksize + blocksize);
                wikisection = new java.io.FileWriter(new File(myLogDirectory, "Roads.wiki." + sectionName.replace(' ', '_') + ".txt"));

                wiki.write("* [[" + mySettings.getProperty("wiki.prefix", "TMC/TMC_Import_Germany") + "/Roads/" + sectionName.replace(' ', '_') + "]]\n");

                wikisection.write("This page belongs to: [[" + mySettings.getProperty("wiki.prefix", "TMC/TMC_Import_Germany") + "/Roads]]<br/>\n");
                wikisection.write("To use the JOSM-links you need the [[JOSM/Plugins/RemoteControl|RemoteControl-Plugin]]."
                + " This plugin does not work with the Webstart-version of JOSM.\n");
                wikisection.write("=== " + sectionName + " ===\n");
                block++;

                wikisection.write("{| class=\"wikitable\"\n");
                wikisection.write("! LocationCode\n");
                wikisection.write("! download\n");
                wikisection.write("! OSM relation-ID\n");
                wikisection.write("! status\n");
                wikisection.write("! user\n");
            }
            //            if (road.getSegments().size() > 0) {
//                Iterator<TMCSegment> iterator = road.getSegments().iterator();
//                while (iterator.hasNext()) {
//                    TMCSegment next = iterator.next();
//                    if (next.getPoints().size() > 0) {
//                        TMCPoint next2 = next.getPoints().iterator().next();
//                        wiki.write("should be here: http://openstreetmap.org/?lat=" + next2.getLatitude() + "&lon=" + next2.getLongitude() + "&zoom=15&layers=B000FTF \n");
//                        break;
//                    }
//                }
//            }
            File outFile = new File(myLogDirectory, "TMCRoad_" + road.getLCD() + ".osm");
            MemoryDataSet outData = new MemoryDataSet();
            road.generateReferenceOSM(outData);
//            int n = outData.getNodesCount();
//            int w = outData.getWaysCount();
//            int r = outData.getRelationsCount();
            BoundingBoxVisitor visitor = new BoundingBoxVisitor();
            visitor.visit(road);
            FileWriter fileWriter = new FileWriter(outFile);
            fileWriter.writeOsm(outData);
//            log.append("We created a missing relation (#relations=" + r + ", #ways=" + w + ", #nodes=" + n + ") for the road " + road.getROADNUMBER() + "\n");
//            log.append("A representation that is NOT TO BE UPLOADED of the tmc-road has also been added to that file.\n");
//            log.append("Please compare with the file below and tag the correct roads in OSM as\n");
//            log.append("\"ref=" + road.getROADNUMBER() + "\"\n");
//            log.append("\"" + getTagPrefix() + ":LocationCode=" + Integer.toString(road.getLCD()) + "\"\n");
//            log.append("\"" + getTagPrefix() + ":Road:Roadnumber="   + road.getROADNUMBER() + "\"\n");
//            log.append("\"" + getTagPrefix() + ":Road:TypeName=" + road.getType().getTDESC() + "\"\n");


            wikisection.write("|-\n");
            wikisection.write("| " + Integer.toString(road.getLCD()) + "\n");
            wikisection.write("| [https://wolschon.kleinbetrieb.biz/tmclcl/" + outFile.getName() + " road " + road.getROADNUMBER());
            if (road.getType().getTDESC() != null) {
            	wikisection.write(" of type \"" + road.getType().getTDESC() + "\"");
                if (road.getType().getTNATDESC() != null) {
                	wikisection.write("/\"" + road.getType().getTNATDESC() + "\"");
                }
            }
            if (road.getSubType() != null) {
            	wikisection.write(" of sub type \"" + road.getSubType().getTDESC() + "\"");
                if (road.getSubType().getTNATDESC() != null) {
                	wikisection.write("/\"" + road.getSubType().getTNATDESC() + "\"");
                }
            }
            wikisection.write("]\n");
            wikisection.write("| none<br/>\n");
            if (road.getROADNUMBER() != null) {
                String name2 = road.getROADNUMBER();
                wikisection.write("([http://www.informationfreeway.org/api/0.6/relation%5Bname%7Cref%7Cnat_ref%7Cname:de%7Cshort_name:de=" + name2.replace(" ", "%20")
                        + "%5D search relations])<br/>\n");
                wikisection.write("([http://www.informationfreeway.org/api/0.6/way%5Bname%7Cname:de%7Cref%7Cnat_ref=" + name2.replace(" ", "%20") + "%5D search ways to create missing relation])<br/>\n");
            }
            wikisection.write("([http://127.0.0.1:8111/load_and_zoom?left=" + visitor.getLeft()
                    + "&right=" + visitor.getRight()
                    + "&top=" + visitor.getTop()
                    + "&bottom=" + visitor.getBottom()
                    + " load area in JOSM])<br/>\n");
            wikisection.write("\n");
            wikisection.write("| TODO\n");
            wikisection.write("| your name here\n");
        }
        LOG.info("importing roads...DONE");
        wikisection.write("|}");
        wikisection.close();
        wiki.close();
    }

    /**
     * Import all TMC-segments.
     * @throws IOException if something goes wrong
     */
    private void readSegments() throws IOException {
        LOG.info("readinging segments...");

        // the file that links to the individual wiki-pages
        java.io.FileWriter wiki = new java.io.FileWriter(new File(myLogDirectory, "Segments.wiki.txt"));
        java.io.FileWriter wikisection = null;

        wiki.write("status:\n");
        wiki.write("* {{BGColor|yellow|INPROGRESS}}\n");
        wiki.write("* {{BGColor|lime|DONE}}\n");
        wiki.write("* {{BGColor|red|PROBLEM}}\n");
        wiki.write("==segments to import==\n");
        wiki.write("{| class=\"wikitable\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" width=\"100%\"\n");
        wiki.write("|-\n");
        wiki.write("! Segments\n");
        wiki.write("! status\n");


        Collection<TMCSegment> allSegments = myLocationTable.getAllSegments();
        int block = -1;
        for (TMCSegment segment : allSegments) {
            LOG.info("importing segment " + segment.getLCD() + "...");

            	// start a new wiki-page is needed
                if (segment.getLCD() > blocksize * block) {
                    if (block > 0) {
                        wikisection.write("|}\n");
                        wikisection.close();
                        wiki.flush();
                    }
                    block = segment.getLCD() / blocksize;
                    String sectionName = (block * blocksize) + " to " + (block * blocksize + blocksize);
                    wikisection = new java.io.FileWriter(new File(myLogDirectory, "Segments.wiki." + sectionName.replace(' ', '_') + ".txt"));

                    wiki.write("|-\n");
                    wiki.write("| [[" + mySettings.getProperty("wiki.prefix", "TMC/TMC_Import_Germany") + "/Segments/" + sectionName.replace(' ', '_') + "]]\n");
                    wiki.write("| not started yet\n");
                    
                    wikisection.write("This page belongs to: [[" + mySettings.getProperty("wiki.prefix", "TMC/TMC_Import_Germany") + "/Segments]]<br/>\n");
                    wikisection.write("To use the JOSM-links you need the [[JOSM/Plugins/RemoteControl|RemoteControl-Plugin]]."
                    + " This plugin does not work with the Webstart-version of JOSM.<br/>\n");
                    wikisection.write("status:\n");
                    wikisection.write("* {{BGColor|yellow|INPROGRESS}}\n");
                    wikisection.write("* {{BGColor|lime|DONE}}\n");
                    wikisection.write("* {{BGColor|red|PROBLEM}}\n");
                    wikisection.write("=== " + sectionName + " ===\n");
                    block++;

                    wikisection.write("{| class=\"wikitable\"\n");
                    wikisection.write("! LocationCode\n");
                    wikisection.write("! download\n");
                    wikisection.write("! OSM relation-ID\n");
                    wikisection.write("! status\n");
                    wikisection.write("! user\n");
                }

                File outFile = new File(myLogDirectory, "TMCSegment_" + segment.getLCD() + ".osm");
                MemoryDataSet outData = new MemoryDataSet();
                segment.generateReferenceOSM(outData);
//                int n = outData.getNodesCount();
//                int w = outData.getWaysCount();
//                int r = outData.getRelationsCount();
                BoundingBoxVisitor visitor = new BoundingBoxVisitor();
                visitor.visit(segment);
                FileWriter fileWriter = new FileWriter(outFile);
                fileWriter.writeOsm(outData);


                wikisection.write("|-\n");
                wikisection.write("| " + Integer.toString(segment.getLCD()) + "\n");
                wikisection.write("| [https://wolschon.kleinbetrieb.biz/tmclcl/" + outFile.getName() + " segment " + Integer.toString(segment.getLCD()));
                if (segment.getType().getTDESC() != null) {
                	wikisection.write(" of type \"" + segment.getType().getTDESC() + "\"");
                    if (segment.getType().getTNATDESC() != null) {
                    	wikisection.write("/\"" + segment.getType().getTNATDESC() + "\"");
                    }
                }
                if (segment.getSubType() != null) {
                	wikisection.write(" of sub type \"" + segment.getSubType().getTDESC() + "\"");
                    if (segment.getSubType().getTNATDESC() != null) {
                    	wikisection.write("/\"" + segment.getSubType().getTNATDESC() + "\"");
                    }
                }
                wikisection.write("]\n");
                wikisection.write("| none<br/>\n");
                if (segment.getROADNUMBER() != null) {
                    String name2 = segment.getROADNUMBER();
                    wikisection.write("([http://www.informationfreeway.org/api/0.6/relation%5Bname%7Cref%7Cnat_ref%7Cname:de%7Cshort_name:de=" + name2.replace(" ", "%20")
                            + "%5D search relations])<br/>\n");
                    wikisection.write("([http://www.informationfreeway.org/api/0.6/way%5Bname%7Cname:de%7Cref%7Cnat_ref=" + name2.replace(" ", "%20") + "%5D search ways to create missing relation])<br/>\n");
                }
                wikisection.write("([http://127.0.0.1:8111/load_and_zoom?left=" + visitor.getLeft()
                        + "&right=" + visitor.getRight()
                        + "&top=" + visitor.getTop()
                        + "&bottom=" + visitor.getBottom()
                        + " load area in JOSM])<br/>\n");
                wikisection.write("([http://www.informationfreeway.org/api/0.6/relation%5BTMC:cid_58:tabcd_1:LocationCode=" + segment.getLCD() + "%5D has this segment been tagged yet?])<br/>\n");
                wikisection.write("\n");
                wikisection.write("| TODO\n");
                wikisection.write("| your name here\n");
            }
            LOG.info("importing segments...DONE");
            wikisection.write("|}");
            wikisection.close();
            wiki.write("|}");
            wiki.close();
    }




    /**
     * Import all TMC-points.
     * @throws IOException if something goes wrong
     */
    private void readPoints() throws IOException {
        LOG.info("reading points...");
        // the file that links to the individual wiki-pages
        java.io.FileWriter wiki = new java.io.FileWriter(new File(myLogDirectory, "Points.wiki.txt"));
        java.io.FileWriter wikisection = null;

        wiki.write("status:\n");
        wiki.write("* {{BGColor|yellow|INPROGRESS}}\n");
        wiki.write("* {{BGColor|lime|DONE}}\n");
        wiki.write("* {{BGColor|red|PROBLEM}}\n");
        wiki.write("==points to import==\n");
        wiki.write("{| class=\"wikitable\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" width=\"100%\"\n");
        wiki.write("|-\n");
        wiki.write("! Points\n");
        wiki.write("! status\n");

        Collection<TMCPoint> allPoints = myLocationTable.getAllPoints();
        int block = -1;
        for (TMCPoint point : allPoints) {
            LOG.info("importing point " + point.getLCD() + "...");

        	// start a new wiki-page is needed
            if (point.getLCD() > blocksize * block) {
                if (block > 0) {
                    wikisection.write("|}\n");
                    wikisection.close();
                    wiki.flush();
                }
                block = point.getLCD() / blocksize;
                String sectionName = (block * blocksize) + " to " + (block * blocksize + blocksize);
                wikisection = new java.io.FileWriter(new File(myLogDirectory, "Points.wiki." + sectionName.replace(' ', '_') + ".txt"));

                wiki.write("|-\n");
                wiki.write("| [[" + mySettings.getProperty("wiki.prefix", "TMC/TMC_Import_Germany") + "/Points/" + sectionName.replace(' ', '_') + "]]\n");
                wiki.write("| not started yet\n");

                wikisection.write("This page belongs to: [[" + mySettings.getProperty("wiki.prefix", "TMC/TMC_Import_Germany") + "/Points]]<br/>\n");
                wikisection.write("To use the JOSM-links you need the [[JOSM/Plugins/RemoteControl|RemoteControl-Plugin]]."
                + " This plugin does not work with the Webstart-version of JOSM.<br/>\n");
                wikisection.write("status:\n");
                wikisection.write("* {{BGColor|yellow|INPROGRESS}}\n");
                wikisection.write("* {{BGColor|lime|DONE}}\n");
                wikisection.write("* {{BGColor|red|PROBLEM}}\n");
                wikisection.write("=== " + sectionName + " ===\n");
                block++;

                wikisection.write("{| class=\"wikitable\"\n");
                wikisection.write("! LocationCode\n");
                wikisection.write("! download\n");
                wikisection.write("! OSM-node-ID(s)\n");
                wikisection.write("! status\n");
                wikisection.write("! user\n");
            }

            File outFile = new File(myLogDirectory, "TMCPoint_" + point.getLCD() + ".osm");
            MemoryDataSet outData = new MemoryDataSet();
            point.generateReferenceOSM(outData);
//            int n = outData.getNodesCount();
//            int w = outData.getWaysCount();
//            int r = outData.getRelationsCount();
            BoundingBoxVisitor visitor = new BoundingBoxVisitor();
            visitor.visit(point);
            FileWriter fileWriter = new FileWriter(outFile);
            fileWriter.writeOsm(outData);


            wikisection.write("|-\n");
            wikisection.write("| " + Integer.toString(point.getLCD()) + "\n");
            wikisection.write("| [https://wolschon.kleinbetrieb.biz/tmclcl/" + outFile.getName() + " point " + Integer.toString(point.getLCD()));
            if (point.getType().getTDESC() != null) {
            	wikisection.write(" of type \"" + point.getType().getTDESC() + "\"");
                if (point.getType().getTNATDESC() != null) {
                	wikisection.write("/\"" + point.getType().getTNATDESC() + "\"");
                }
            }
            if (point.getSubType() != null) {
            	wikisection.write(" of sub type \"" + point.getSubType().getTDESC() + "\"");
                if (point.getSubType().getTNATDESC() != null) {
                	wikisection.write("/\"" + point.getSubType().getTNATDESC() + "\"");
                }
            }
            wikisection.write("]\n");
            wikisection.write("| none<br/>\n");
//            if (road.getROADNUMBER() != null) {
//                String name2 = road.getROADNUMBER();
//                wikisection.write("([http://www.informationfreeway.org/api/0.6/relation%5Bname%7Cref%7Cnat_ref%7Cname:de%7Cshort_name:de=" + name2.replace(" ", "%20")
//                        + "%5D search relations])<br/>\n");
//                wikisection.write("([http://www.informationfreeway.org/api/0.6/way%5Bname%7Cname:de%7Cref%7Cnat_ref=" + name2.replace(" ", "%20") + "%5D search ways to create missing relation])<br/>\n");
//            }
            wikisection.write("([http://127.0.0.1:8111/load_and_zoom?left=" + visitor.getLeft()
                    + "&right=" + visitor.getRight()
                    + "&top=" + visitor.getTop()
                    + "&bottom=" + visitor.getBottom()
                    + " load area in JOSM])<br/>\n");
            wikisection.write("([http://www.informationfreeway.org/api/0.6/node%5BTMC:cid_58:tabcd_1:LocationCode=" + point.getLCD() + "%5D has this node been tagged yet?])<br/>\n");
            wikisection.write("\n");
            wikisection.write("| TODO\n");
            wikisection.write("| your name here\n");
        }
        LOG.info("importing points...DONE");
        wikisection.write("|}");
        wikisection.close();
        wiki.write("|}");
        wiki.close();
    }

    /**
     * @return the prefix to all our tags
     */
    private String getTagPrefix() {
      return "TMC:cid_58"
      + ":tabcd_1";
//        return "TMC:cid_" + mySettings.getProperty("tmcimport.CountryID")
//        + ":tabcd_" + mySettings.getProperty("tmcimport.TMCTableNumber", "1");
    }




    /**
     * Configures logging to write all output to the console.
     */
    private static void configureLoggingConsole() {
        Logger rootLogger = Logger.getLogger("");

        // Remove any existing handlers.
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // add a debug-file
        //        try {
        //            Handler fileHandler = new FileHandler("e:\\libosm.osmbin.debug.log");
        //            fileHandler.setLevel(Level.FINEST);
        //            fileHandler.setFormatter(new SimpleFormatter());
        //            Logger.getLogger("org.openstreetmap.osm.data.osmbin").addHandler(fileHandler);
        //        } catch (Exception e) {
        //            System.err.println("Could not create debug-log for tracing osmbin");
        //            e.printStackTrace();
        //        }

        // Add a new console handler.
        Handler consoleHandler = new StdoutConsoleHandler();
        consoleHandler.setLevel(Level.FINER);
        consoleHandler.setFilter(new Filter() {

            @Override
            public boolean isLoggable(final LogRecord aRecord) {
                Level level = aRecord.getLevel();
                return !level.equals(Level.WARNING) && !level.equals(Level.SEVERE);
            }
        });
        rootLogger.addHandler(consoleHandler);

        Handler consoleHandlerErr = new ConsoleHandler();
        consoleHandlerErr.setLevel(Level.WARNING);
        rootLogger.addHandler(consoleHandlerErr);
    }
    /**
     * Configures the logging level.
     */
    private static void configureLoggingLevelAll() {
        Logger.getLogger("").setLevel(Level.ALL);
        Logger.getLogger("sun").setLevel(Level.WARNING);
        Logger.getLogger("com.sun").setLevel(Level.WARNING);
        Logger.getLogger("java").setLevel(Level.WARNING);
        Logger.getLogger("javax").setLevel(Level.WARNING);

        // general log-level
        Logger.getLogger("org.openstreetmap").setLevel(Level.INFO);
    }

    /**
     * This <tt>Handler</tt> publishes log records to <tt>System.out</tt>.
     * By default the <tt>SimpleFormatter</tt> is used to generate brief summaries.
     * <p>
     * <b>Configuration:</b>
     * By default each <tt>ConsoleHandler</tt> is initialized using the following
     * <tt>LogManager</tt> configuration properties.  If properties are not defined
     * (or have invalid values) then the specified default values are used.
     * <ul>
     * <li>   java.util.logging.ConsoleHandler.level
     *    specifies the default level for the <tt>Handler</tt>
     *    (defaults to <tt>Level.INFO</tt>).
     * <li>   java.util.logging.ConsoleHandler.filter
     *    specifies the name of a <tt>Filter</tt> class to use
     *    (defaults to no <tt>Filter</tt>).
     * <li>   java.util.logging.ConsoleHandler.formatter
     *    specifies the name of a <tt>Formatter</tt> class to use
     *        (defaults to <tt>java.util.logging.SimpleFormatter</tt>).
     * <li>   java.util.logging.ConsoleHandler.encoding
     *    the name of the character set encoding to use (defaults to
     *    the default platform encoding).
     * </ul>
     * <p>
     * @version 1.13, 11/17/05
     * @since 1.4
     */

    public static class StdoutConsoleHandler extends StreamHandler {
        /**
         *  Private method to configure a ConsoleHandler.
         */
        private void configure() {
            setLevel(Level.INFO);
            setFilter(null);
            setFormatter(new SimpleFormatter());
        }

        /**
         * Create a <tt>ConsoleHandler</tt> for <tt>System.err</tt>.
         * <p>
         * The <tt>ConsoleHandler</tt> is configured based on
         * <tt>LogManager</tt> properties (or their default values).
         */
        public StdoutConsoleHandler() {
            configure();
            setOutputStream(System.out);
        }

        /**
         * Publish a <tt>LogRecord</tt>.
         * <p>
         * The logging request was made initially to a <tt>Logger</tt> object,
         * which initialized the <tt>LogRecord</tt> and forwarded it here.
         * <p>
         * @param  record  description of the log event. A null record is
         *                 silently ignored and is not published
         */
        public void publish(final LogRecord record) {
            super.publish(record);
            flush();
        }

        /**
         * Override <tt>StreamHandler.close</tt> to do a flush but not
         * to close the output stream.  That is, we do <b>not</b>
         * close <tt>System.err</tt>.
         */
        public void close() {
            flush();
        }
    }


}
