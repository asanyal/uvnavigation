package org.openstreetmap.travelingsalesman.trafficblocks.tmc.tmcimport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;


/**
 * This class imports a TMC-location-table
 * and crates a file tmclocations.properties.
 */
public class CreateTMCLocationProperties {

    /**
     * The LCL.
     */
    private TMCLocationTable myLocationTable;
    /**
     * The directory with the location-table.
     */
    private File myDirectory;
    /**
     * Automatically created logger for debug and error-output.
     */
    private static final Logger LOG = Logger.getLogger(CreateTMCLocationProperties.class
            .getName());


    /**
     * Run the import.
     * @param dir the directory with the location-table.
     * @throws IOException if something goes wrong
     */
    public CreateTMCLocationProperties(final File dir) throws IOException {
        this.myDirectory = dir;
        myLocationTable = new TMCLocationTable(dir);
        Properties out = new Properties();
        Collection<TMCPoint> allPoints = myLocationTable.getAllPoints();
        for (TMCPoint point : allPoints) {
            TMCSegment segment = point.getSegment();
            if (segment != null) {
                out.put(point.getCID() + "." + point.getTABCD() + "." + point.getLCD() + ".name", segment.getROADNUMBER());
            }
            out.put(point.getCID() + "." + point.getTABCD() + "." + point.getLCD() + ".lat", "" + point.getLatitude());
            out.put(point.getCID() + "." + point.getTABCD() + "." + point.getLCD() + ".lon", "" + point.getLongitude());
            if (point.getNextPoint() != null) {
                out.put(point.getCID() + "." + point.getTABCD() + "." + point.getLCD() + ".next", "" + point.getNextPoint().getLCD());
            }
            if (point.getPreviousPoint() != null) {
                out.put(point.getCID() + "." + point.getTABCD() + "." + point.getLCD() + ".prev", "" + point.getPreviousPoint().getLCD());
            }
        }
        Collection<TMCSegment> allSegments = myLocationTable.getAllSegments();
        for (TMCSegment segment : allSegments) {
            out.put(segment.getCID() + "." + segment.getTABCD() + "." + segment.getLCD() + ".name", segment.getROADNUMBER());
            List<TMCPoint> points = segment.getPoints();
            if (points != null && points.size() > 0) {
                TMCPoint point = points.get(0);
                out.put(segment.getCID() + "." + segment.getTABCD() + "." + segment.getLCD() + ".lat", "" + point.getLatitude());
                out.put(segment.getCID() + "." + segment.getTABCD() + "." + segment.getLCD() + ".lon", "" + point.getLongitude());
            }
            if (segment.getNextSegment() != null) {
                out.put(segment.getCID() + "." + segment.getTABCD() + "." + segment.getLCD() + ".next", "" + segment.getNextSegment().getLCD());
            }
            if (segment.getPreviousSegment() != null) {
                out.put(segment.getCID() + "." + segment.getTABCD() + "." + segment.getLCD() + ".prev", "" + segment.getPreviousSegment().getLCD());
            }
        }
        Collection<TMCRoad> allRoads = myLocationTable.getAllRoads();
        for (TMCRoad road : allRoads) {
            out.put(road.getCID() + "." + road.getTABCD() + "." + road.getLCD() + ".name", road.getROADNUMBER());
            List<TMCSegment> segments = road.getSegments();
            if (segments != null) {
                for (TMCSegment segment2 : segments) {
                    List<TMCPoint> points = segment2.getPoints();
                    if (points != null && points.size() > 0) {
                        TMCPoint point = points.get(0);
                        out.put(road.getCID() + "." + road.getTABCD() + "." + road.getLCD() + ".lat", "" + point.getLatitude());
                        out.put(road.getCID() + "." + road.getTABCD() + "." + road.getLCD() + ".lon", "" + point.getLongitude());
                        break;
                    }
                }
            }
        }
        File outfile = new File(myDirectory, "tmclocations.properties");
        out.store(new FileWriter(outfile), "Created with CreateTMCLocationProperties");
        System.out.println("writing " + outfile.getAbsolutePath() + "... done");
    }


    /**
     * @param args either empty or 1 directory-name
     */
    public static void main(final String[] args) {

        try {
            configureLoggingConsole();
            configureLoggingLevelAll();
            String dirname = "."
                + File.separator + "src"
                + File.separator + "org"
                + File.separator + "openstreetmap"
                + File.separator + "travelingsalesman"
                + File.separator + "trafficblocks"
                + File.separator + "tmc"
                + File.separator + "TMC-Testdata"
                + File.separator + "Germany"
                + File.separator + "LCL8.00.D-081201";
            if (args.length == 1) {
                dirname = args[0];
            }
            File dir = new File(dirname);
            if (!dir.exists()) {
                System.err.println("Directory: " + dir.getAbsolutePath() + " does not exist.");
                return;
            }
            CreateTMCLocationProperties me = new CreateTMCLocationProperties(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
